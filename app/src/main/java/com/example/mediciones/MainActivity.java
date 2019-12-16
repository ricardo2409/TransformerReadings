package com.example.mediciones;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;

import static java.lang.Long.toBinaryString;
import static java.lang.Long.valueOf;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    TextView tvRadio, tvVoltaje1, tvVoltaje2, tvVoltaje3, tvVoltaje4, tvCorriente1, tvCorriente2, tvCorriente3, tvCorriente4, tvFactor1, tvFactor2, tvFactor3, tvFactor4, tvPotencia1, tvPotencia2, tvPotencia3, tvPotencia4, tvControl, tvConsecutivo, tvTipo, tvContador, tvCalidad;
    Button btnConnect;

    boolean connected = false;
    String cadena = "F1 00 1C 00 52 23 02 2A 00 D6 04 D6 04 D6 04 C4 00 C5 00 C6 00 63 00 63 00 63 00 F4 00 F5 00 F6 00 18 00 00 00 00 00 00 F2";
    private static final UUID PORT_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    public static BluetoothDevice device;
    public static BluetoothSocket socket;
    public static OutputStream outputStream;
    public static InputStream inputStream;

    static Button btnDiagnostico;
    static Handler handler = new Handler();


    static String tokens[];
    static String tokensRSSI[];

    static boolean socketConectado;
    boolean stopThread;

    static String s;

    static String a;
    static String b;

    static Thread thread;
    StringBuilder sb, sbAux;
    int control;
    String uno, dos;
    Boolean boolDos;
    boolean boolPassword;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initItems();
        readString(cadena);
    }

    public void initItems(){
        tvVoltaje2 = (TextView)findViewById(R.id.tvVoltaje2);
        tvVoltaje3 = (TextView)findViewById(R.id.tvVoltaje3);
        tvVoltaje4 = (TextView)findViewById(R.id.tvVoltaje4);

        tvCorriente2 = (TextView)findViewById(R.id.tvCorriente2);
        tvCorriente3 = (TextView)findViewById(R.id.tvCorriente3);
        tvCorriente4 = (TextView)findViewById(R.id.tvCorriente4);

        tvFactor2 = (TextView)findViewById(R.id.tvFactor2);
        tvFactor3 = (TextView)findViewById(R.id.tvFactor3);
        tvFactor4 = (TextView)findViewById(R.id.tvFactor4);

        tvPotencia2 = (TextView)findViewById(R.id.tvPotencia2);
        tvPotencia3 = (TextView)findViewById(R.id.tvPotencia3);
        tvPotencia4 = (TextView)findViewById(R.id.tvPotencia4);

        tvConsecutivo = (TextView)findViewById(R.id.tvConsecutivo);
        tvContador = (TextView)findViewById(R.id.tvContador);
        tvRadio = (TextView)findViewById(R.id.tvRadio);

        btnConnect = (Button)findViewById(R.id.btnConectar);
        btnConnect.setOnClickListener(this);
    }

    public void print(String message){
        System.out.println(message);
    }

    //Identifica el device BT
    public boolean BTinit()
    {
        boolean found = false;
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter == null) //Checks if the device supports bluetooth
        {
            Toast.makeText(getApplicationContext(), "Este dispositivo no soporta bluetooth", Toast.LENGTH_SHORT).show();
        }
        if(!bluetoothAdapter.isEnabled()) //Checks if bluetooth is enabled. If not, the program will ask permission from the user to enable it
        {
            Intent enableAdapter = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableAdapter,0);
            try
            {
                Thread.sleep(1000);
            }
            catch(InterruptedException e)
            {
                e.printStackTrace();
            }
        }
        Set<BluetoothDevice> bondedDevices = bluetoothAdapter.getBondedDevices();
        if(bondedDevices.isEmpty()) //Checks for paired bluetooth devices
        {
            Toast.makeText(getApplicationContext(), "Favor de conectar un dispositivo", Toast.LENGTH_SHORT).show();
        }
        else
        {
            for(BluetoothDevice iterator : bondedDevices)
            {

                //Suponiendo que solo haya un bondedDevice
                device = iterator;
                found = true;
                //Toast.makeText(getApplicationContext(), "Conectado a: " + device.getName(), Toast.LENGTH_SHORT).show();
            }
        }
        return found;
    }
    //Conexión al device BT
    public boolean BTconnect()
    {
        try
        {
            conectar();

        }
        catch(IOException e)
        {
            Toast.makeText(getApplicationContext(), "Conexión no exitosa", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            connected = false;
        }

        return connected;
    }

    public void conectar() throws IOException{
        socket = device.createRfcommSocketToServiceRecord(PORT_UUID); //Crea un socket para manejar la conexión
        socket.connect();
        socketConectado = true;
        Log.d("Socket ", String.valueOf(socket.isConnected()));
        Toast.makeText(getApplicationContext(), "Conexión exitosa", Toast.LENGTH_SHORT).show();
        connected = true;
        btnConnect.setText("Desconectar módulo Bluetooth");
        outputStream = socket.getOutputStream();
        inputStream = socket.getInputStream();
        beginListenForData();
    }

    public void desconectarBluetooth() throws IOException{
        //Desconectar bluetooth
        if(socketConectado){
            System.out.println("Socket Conectado");
            outputStream.close();
            outputStream = null;
            inputStream.close();
            inputStream = null;
            socket.close();
            socket = null;
        }
        resetFields();
        connected = false;
        btnConnect.setText("Conectar a módulo Bluetooth");
        device = null;
        stopThread = true;
        socketConectado = false;
        boolPassword = false; //Para que lo vuelva a pedir la siguiente vez que se conecte a otro equipo
    }

    void beginListenForData() {
        stopThread = false;
        final String[] uno = {null};
        final String[] dos = { null };
        thread = new Thread(new Runnable() {
            public void run() {
                while(!Thread.currentThread().isInterrupted() && !stopThread) {
                    try {
                        //waitMs(1000);
                        final int byteCount = inputStream.available();
                        if(byteCount > 0) {


                            byte[] packetBytes = new byte[byteCount];
                            inputStream.read(packetBytes);

                            sb = new StringBuilder();
                            for (byte b : packetBytes) {
                                sb.append(String.format("%02X ", b));
                                //print("Esto tiene en el append: " + sb.toString());
                            }
                            if(control == 0){
                                uno[0] = sb.toString();
                            }else{
                                dos[0] = sb.toString();
                                print("Esto es todo completo: " + uno[0] + dos[0]);
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        //Status
                                        readString(uno[0] + dos[0]); //String final que se lee y analiza
                                    }
                                });
                            }
                            System.out.println("Hex as string: " + sb.toString()); //Hex as string
                            if(sb.toString().contains("F1") && !sb.toString().contains("F2")){
                                print("Tiene F1 pero no F2");
                                control = 1;
                            }else if(!sb.toString().contains("F1") && sb.toString().contains("F2")){
                                print("Tiene F2 pero no F1");
                                control = 0;
                                boolDos = true;
                                //print(uno + dos);

                            }else if(sb.toString().contains("F1") && sb.toString().contains("F2")){
                                //Mandarlo asi como está
                                print("Ya estaba completo: " + sb.toString());
                            }

                        }

                    }
                    catch (IOException ex) {
                        stopThread = true;
                    }
                }
                System.out.println("Stop thread es true");
            }
        });
        thread.start();
    }
    public void readString(String message){
        String [] arrOfStr = message.split(" ");

        Integer[] intarray=new Integer[arrOfStr.length];
        int i=0;
        for (String str : arrOfStr){
            int temp1 = Integer.parseInt(arrOfStr[i].trim(), 16 );//De hex a decimal
            intarray[i] = temp1;
            print("Esto es lo que convierto: " + i + " " + temp1);
            i++;
        }

        if(Arrays.asList(arrOfStr).contains("F0")){
            print("Si tiene F0");
            //convertString(arrOfStr);

        }else{
            print("No tiene F0");
            tvRadio.setText("Radio: " + intarray[1] + "" + intarray[2]);
            //tvControl.setText("Control: " + intarray[3]);
            tvConsecutivo.setText("# Consecutivo: " + intarray[4]);
            //tvTipo.setText("Dispositivo: " + intarray[6]); /////
            tvVoltaje2.setText(String.valueOf(Double.valueOf(intarray[10]*256 + intarray[9])/10));
            tvVoltaje3.setText(String.valueOf(Double.valueOf(intarray[12]*256 + intarray[11])/10));
            tvVoltaje4.setText(String.valueOf(Double.valueOf(intarray[14]*256 + intarray[13])/10));
            tvCorriente2.setText(String.valueOf(Double.valueOf(intarray[16]*256 + intarray[15])/100));
            tvCorriente3.setText(String.valueOf(Double.valueOf(intarray[18]*256 + intarray[17])/100));
            tvCorriente4.setText(String.valueOf(Double.valueOf(intarray[20]*256 + intarray[19])/100));
            tvFactor2.setText(String.valueOf(Double.valueOf(intarray[22]*256 + intarray[21])/100));
            tvFactor3.setText(String.valueOf(Double.valueOf(intarray[24]*256 + intarray[23])/100));
            tvFactor4.setText(String.valueOf(Double.valueOf(intarray[26]*256 + intarray[25])/100));
            tvPotencia2.setText("" + intarray[27]);
            tvPotencia3.setText("" + intarray[29]);
            tvPotencia4.setText("" + intarray[31]);
            int aux = intarray[36]*256 + intarray[35]*256 + intarray[34]*256 + intarray[33];
            tvContador.setText("Contador Watts/Hora: " + aux);
            print("Esto es el binary: " + Integer.toBinaryString(intarray[7]));
            String binario = Integer.toBinaryString(intarray[7]);
            for(int a=binario.length(); a < 6; a++){
                binario = '0' + binario;

            }
            print("Esto tiene el nuevo numero: " + binario);
            for (int cont=0; cont < binario.length(); cont++){
                System.out.println("Char " + cont + " is " + binario.charAt(cont));
                if(binario.charAt(cont) == '1'){
                    // Poner alarmas
                    switch(cont) {
                        case 5:
                            print("Alarma 0");
                            tvVoltaje2.setTextColor(Color.RED);
                            break;
                        case 4:
                            print("Alarma 1");
                            tvVoltaje3.setTextColor(Color.RED);
                            break;
                        case 3:
                            print("Alarma 2");
                            tvVoltaje4.setTextColor(Color.RED);
                            break;
                        case 2:
                            print("Alarma 3");
                            tvPotencia2.setTextColor(Color.RED);
                            break;
                        case 1:
                            print("Alarma 4");
                            tvPotencia3.setTextColor(Color.RED);
                            break;
                        case 0:
                            print("Alarma 5");
                            tvPotencia4.setTextColor(Color.RED);
                            break;
                        default:
                            print("Alarma Ninguna anteriores");
                    }
                }else{// Quitar alarmas
                    switch(cont) {
                        case 5:
                            print("Normal 0");
                            tvVoltaje2.setTextColor(Color.BLACK);
                            break;
                        case 4:
                            print("Normal 1");
                            tvVoltaje3.setTextColor(Color.BLACK);
                            break;
                        case 3:
                            print("Normal 2");
                            tvVoltaje4.setTextColor(Color.BLACK);
                            break;
                        case 2:
                            print("Normal 3");
                            tvPotencia2.setTextColor(Color.BLACK);
                            break;
                        case 1:
                            print("Normal 4");
                            tvPotencia3.setTextColor(Color.BLACK);
                            break;
                        case 0:
                            print("Normal 5");
                            tvPotencia4.setTextColor(Color.BLACK);
                            break;
                        default:
                            print("Normal Ninguna anteriores");
                    }
                }
            }
        }
    }

    public void resetFields(){
        tvVoltaje2.setText("");
        tvVoltaje3.setText("");
        tvVoltaje4.setText("");
        tvCorriente2.setText("");
        tvCorriente3.setText("");
        tvCorriente4.setText("");
        tvFactor2.setText("");
        tvFactor3.setText("");
        tvFactor4.setText("");
        tvPotencia2.setText("");
        tvPotencia3.setText("");
        tvPotencia4.setText("");
        tvConsecutivo.setText("# Consecutivo: ");
        tvContador.setText("Contador Watts/Hora: ");
        tvRadio.setText("Radio: ");
    }

    public void redTextViews(){
        tvPotencia2.setTextColor(Color.RED);
        tvPotencia3.setTextColor(Color.RED);
        tvPotencia4.setTextColor(Color.RED);
        tvVoltaje2.setTextColor(Color.RED);
        tvVoltaje3.setTextColor(Color.RED);
        tvVoltaje4.setTextColor(Color.RED);
    }
    public void blackTextViews(){
        tvPotencia2.setTextColor(Color.BLACK);
        tvPotencia3.setTextColor(Color.BLACK);
        tvPotencia4.setTextColor(Color.BLACK);
        tvVoltaje2.setTextColor(Color.BLACK);
        tvVoltaje3.setTextColor(Color.BLACK);
        tvVoltaje4.setTextColor(Color.BLACK);
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnConectar:
                if (!connected) {
                    if (BTinit()) {
                        BTconnect();
                    }
                } else {
                    try {
                        desconectarBluetooth();
                    } catch (IOException ex) {
                    }
                }
                break;


        }
    }
    private void showToast(final String message) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
