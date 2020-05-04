package com.example.mediciones;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import static java.lang.Long.toBinaryString;
import static java.lang.Long.valueOf;


public class MainActivity extends FragmentActivity implements View.OnClickListener {

    Button btnConnect, btnConfiguracion, btnRadio, btnMediciones;

    boolean connected = false;
    String cadena = "F1 00 1C 00 09 29 03 30 00 DA 04 DA 04 DA 04 05 00 05 00 05 00 00 00 9D FF 9D FF 00 00 00 00 00 00 19 00 7D 00 0C 00 B4 6C 00 00 64 DA 62 F2";
    //String cadena = "F1 00 1C 00 52 23 02 2A 00 D6 04 D6 04 D6 04 C4 00 C5 00  20 00 63 00 63 00 63 00 F4 00 F5 00 F6 00 18 00 00 00 00 00 00 F2";
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

    final static int SOURCE_ADDRESS = 0;
    final static int DESTINATION_ADDRESS = 1;
    final static int VOLTAGE_CONTROL_CHECKBOX = 2;
    final static int MINIMUM_VOLTAGE = 3;
    final static int MAXIMUM_VOLTAGE = 4;
    final static int AC_VOLTAGE = 5;


    static MedicionesFragment medFrag;
    static RadioFragment radioFrag;
    static ConfiguracionFragment confFrag;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initItems();
        medFrag = new MedicionesFragment();
        radioFrag = new RadioFragment();
        confFrag = new ConfiguracionFragment();

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().add(R.id.fragment_container, medFrag).commit();

        //readString(cadena);

    }


    public void initItems(){
        btnConnect = (Button)findViewById(R.id.btnConectar);
        btnConnect.setOnClickListener(this);
        btnConfiguracion = (Button)findViewById(R.id.btnConfiguracion);
        btnConfiguracion.setOnClickListener(this);
        btnRadio = (Button)findViewById(R.id.btnRadio);
        btnRadio.setOnClickListener(this);
        btnMediciones = (Button)findViewById(R.id.btnVoltaje);
        btnMediciones.setOnClickListener(this);
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
                                //print("Tiene F1 pero no F2");
                                control = 1;
                            }else if(!sb.toString().contains("F1") && sb.toString().contains("F2")){
                                //print("Tiene F2 pero no F1");
                                control = 0;
                                boolDos = true;
                                //print(uno + dos);
                            }else if(sb.toString().contains("F1") && sb.toString().contains("F2")){
                                //Mandarlo asi como está
                                //print("Ya estaba completo: " + sb.toString());
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
            medFrag.tvRadio.setText("Radio: " + intarray[1] + "" + intarray[2]);
            //tvControl.setText("Control: " + intarray[3]);
            medFrag.tvConsecutivo.setText("# Consecutivo: " + intarray[4]);
            //tvTipo.setText("Dispositivo: " + intarray[6]); /////
            medFrag.tvVoltaje2.setText(String.valueOf(Double.valueOf(intarray[10]*256 + intarray[9])/10));
            medFrag.tvVoltaje3.setText(String.valueOf(Double.valueOf(intarray[12]*256 + intarray[11])/10));
            medFrag.tvVoltaje4.setText(String.valueOf(Double.valueOf(intarray[14]*256 + intarray[13])/10));
            medFrag.tvCorriente2.setText(String.valueOf(Double.valueOf(intarray[16]*256 + intarray[15])/100));
            medFrag.tvCorriente3.setText(String.valueOf(Double.valueOf(intarray[18]*256 + intarray[17])/100));
            medFrag.tvCorriente4.setText(String.valueOf(Double.valueOf(intarray[20]*256 + intarray[19])/100));
            medFrag.tvFactor2.setText(String.valueOf(Double.valueOf(intarray[22]*256 + intarray[21])/100));
            medFrag.tvFactor3.setText(String.valueOf(Double.valueOf(intarray[24]*256 + intarray[23])/100));
            medFrag.tvFactor4.setText(String.valueOf(Double.valueOf(intarray[26]*256 + intarray[25])/100));
            medFrag.tvPotencia2.setText("" + (intarray[28]*256 + intarray[27]));
            medFrag.tvPotencia3.setText("" + (intarray[30]*256 + intarray[29]));
            medFrag.tvPotencia4.setText("" + (intarray[32]*256 + intarray[31]));
            int aux = intarray[42]*256 + intarray[41]*256 + intarray[40]*256 + intarray[39];
            print("Esto es aux: " + aux);
            medFrag.tvContador.setText("Contador W/Hr: " + aux);
            medFrag.tvTemperatura.setText("Temperatura: " + intarray[33]);
            int dbmSeñal = (intarray[44]/2) - 158;
            int dbmRuido = (intarray[45]/2) - 158;
            medFrag.tvSenal.setText("Señal: " + dbmSeñal + "dB");
            medFrag.tvPaquetes.setText("% de Paquetes: " + intarray[43]);
            medFrag.tvRuido.setText("Ruido: " + dbmRuido + "dB");
            //Voltaje de bateria es el 37
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
                            medFrag.tvVoltaje2.setTextColor(Color.RED);
                            break;
                        case 4:
                            print("Alarma 1");
                            medFrag.tvVoltaje3.setTextColor(Color.RED);
                            break;
                        case 3:
                            print("Alarma 2");
                            medFrag.tvVoltaje4.setTextColor(Color.RED);
                            break;
                        case 2:
                            print("Alarma 3");
                            medFrag.tvPotencia2.setTextColor(Color.RED);
                            break;
                        case 1:
                            print("Alarma 4");
                            medFrag.tvPotencia3.setTextColor(Color.RED);
                            break;
                        case 0:
                            print("Alarma 5");
                            medFrag.tvPotencia4.setTextColor(Color.RED);
                            break;
                        default:
                            print("Alarma Ninguna anteriores");
                    }
                }else{// Quitar alarmas
                    switch(cont) {
                        case 5:
                            print("Normal 0");
                            medFrag.tvVoltaje2.setTextColor(Color.BLACK);
                            break;
                        case 4:
                            print("Normal 1");
                            medFrag.tvVoltaje3.setTextColor(Color.BLACK);
                            break;
                        case 3:
                            print("Normal 2");
                            medFrag.tvVoltaje4.setTextColor(Color.BLACK);
                            break;
                        case 2:
                            print("Normal 3");
                            medFrag.tvPotencia2.setTextColor(Color.BLACK);
                            break;
                        case 1:
                            print("Normal 4");
                            medFrag.tvPotencia3.setTextColor(Color.BLACK);
                            break;
                        case 0:
                            print("Normal 5");
                            medFrag.tvPotencia4.setTextColor(Color.BLACK);
                            break;
                        default:
                            print("Normal Ninguna anteriores");
                    }
                }
            }
        }
    }

    public void resetFields(){
        medFrag.tvVoltaje2.setText("");
        medFrag.tvVoltaje3.setText("");
        medFrag.tvVoltaje4.setText("");
        medFrag.tvCorriente2.setText("");
        medFrag.tvCorriente3.setText("");
        medFrag.tvCorriente4.setText("");
        medFrag.tvFactor2.setText("");
        medFrag.tvFactor3.setText("");
        medFrag.tvFactor4.setText("");
        medFrag.tvPotencia2.setText("");
        medFrag.tvPotencia3.setText("");
        medFrag.tvPotencia4.setText("");
        medFrag.tvConsecutivo.setText("# Consecutivo: ");
        medFrag.tvContador.setText("Contador Watts/Hora: ");
        medFrag.tvRadio.setText("Radio: ");
        medFrag.tvTemperatura.setText("Temperatura: ");
    }

    public void redTextViews(){
        medFrag.tvPotencia2.setTextColor(Color.RED);
        medFrag.tvPotencia3.setTextColor(Color.RED);
        medFrag.tvPotencia4.setTextColor(Color.RED);
        medFrag.tvVoltaje2.setTextColor(Color.RED);
        medFrag.tvVoltaje3.setTextColor(Color.RED);
        medFrag.tvVoltaje4.setTextColor(Color.RED);
    }
    public void blackTextViews(){
        medFrag. tvPotencia2.setTextColor(Color.BLACK);
        medFrag.tvPotencia3.setTextColor(Color.BLACK);
        medFrag.tvPotencia4.setTextColor(Color.BLACK);
        medFrag.tvVoltaje2.setTextColor(Color.BLACK);
        medFrag.tvVoltaje3.setTextColor(Color.BLACK);
        medFrag.tvVoltaje4.setTextColor(Color.BLACK);
    }
    public void editSourceAddress(View view)
    {
        System.out.println("Estoy en el editSourceAddress");

        if(connected)
        {
            Bundle arguments = new Bundle();
            arguments.putInt("field", SOURCE_ADDRESS);
            arguments.putString("inputText", getString(R.string.sourceAddress_label));
            arguments.putString("inputHint", getString(R.string.sourceAddress_hint));
            arguments.putInt("inputType", InputType.TYPE_CLASS_NUMBER);
            arguments.putInt("minValue", 1);
            arguments.putInt("maxValue", 65535);

            DialogFragment dialog;

            dialog = GenericInputDialog.newInstance(arguments);
            dialog.show(getFragmentManager(), "generic_input");
        }
        else
        {
            Toast.makeText(getApplicationContext(), getString(R.string.disconnected_message), Toast.LENGTH_SHORT).show();
        }
    }

    public void editDestinationAddress(View view)
    {
        System.out.println("Estoy en el editDestinationAddress");

        if(connected)
        {
            Bundle arguments = new Bundle();
            arguments.putInt("field", DESTINATION_ADDRESS);
            arguments.putString("inputText", getString(R.string.destinationAddress_label));
            arguments.putString("inputHint", getString(R.string.destinationAddress_hint));
            arguments.putInt("inputType", InputType.TYPE_CLASS_NUMBER);
            arguments.putInt("minValue", 1);
            arguments.putInt("maxValue", 65535);

            DialogFragment dialog;

            dialog = GenericInputDialog.newInstance(arguments);
            dialog.show(getFragmentManager(), "generic_input");
        }
        else
        {
            Toast.makeText(getApplicationContext(), getString(R.string.disconnected_message), Toast.LENGTH_SHORT).show();
        }
    }

    public void toggleVoltageControl(View view)
    {
        if(!connected)
        {
            Toast.makeText(getApplicationContext(), getString(R.string.disconnected_message), Toast.LENGTH_SHORT).show();
        }
    }

    public void editMinimumVoltage(View view)
    {
        if(connected)
        {
            Bundle arguments = new Bundle();
            arguments.putInt("field", MINIMUM_VOLTAGE);
            arguments.putString("inputText", getString(R.string.minimumVoltage_label));
            //arguments.putString("inputHint", getString(R.string.destinationAddress_hint));
            arguments.putInt("inputType", InputType.TYPE_CLASS_NUMBER);
            arguments.putInt("minValue", 0);
            arguments.putInt("maxValue", 1000);

            DialogFragment dialog;

            dialog = GenericInputDialog.newInstance(arguments);
            dialog.show(getFragmentManager(), "generic_input");
        }
        else
        {
            Toast.makeText(getApplicationContext(), getString(R.string.disconnected_message), Toast.LENGTH_SHORT).show();
        }
    }

    public void editMaximumVoltage(View view)
    {
        if(connected)
        {
            Bundle arguments = new Bundle();
            arguments.putInt("field", MAXIMUM_VOLTAGE);
            arguments.putString("inputText", getString(R.string.maximumVoltage_label));
            //arguments.putString("inputHint", getString(R.string.destinationAddress_hint));
            arguments.putInt("inputType", InputType.TYPE_CLASS_NUMBER);
            arguments.putInt("minValue", 0);
            arguments.putInt("maxValue", 1000);

            DialogFragment dialog;

            dialog = GenericInputDialog.newInstance(arguments);
            dialog.show(getFragmentManager(), "generic_input");
        }
        else
        {
            Toast.makeText(getApplicationContext(), getString(R.string.disconnected_message), Toast.LENGTH_SHORT).show();
        }
    }

    public void editNivelAC(View view)
    {
        if(connected)
        {
            Bundle arguments = new Bundle();
            arguments.putInt("field", AC_VOLTAGE);
            arguments.putString("inputText", "Nivel AC:");
            arguments.putInt("inputType", InputType.TYPE_CLASS_NUMBER);
            arguments.putInt("minValue", 0);
            arguments.putInt("maxValue", 1000);

            DialogFragment dialog;

            dialog = GenericInputDialog.newInstance(arguments);
            dialog.show(getFragmentManager(), "generic_input");
        }
        else
        {
            Toast.makeText(getApplicationContext(), getString(R.string.disconnected_message), Toast.LENGTH_SHORT).show();
        }
    }
    public void programConfiguration()
    {
        int newSourceAddress, newDestinationAddress;
        float newMinimumVoltage, newMaximumVoltage, newNivelAC;
        String stringToSend;
        if(connected)
        {
            try {
                if(!confFrag.sourceAddressTextInput.getText().toString().matches("") && !confFrag.destinationAddressTextInput.getText().toString().matches("") && !confFrag.minimumVoltageTextInput.getText().toString().matches("") && !confFrag.maximumVoltageTextInput.getText().toString().matches(""))
                {
                    // not null not empty
                    newSourceAddress = Integer.parseInt(confFrag.sourceAddressTextInput.getText().toString());
                    newDestinationAddress = Integer.parseInt(confFrag.destinationAddressTextInput.getText().toString());
                    //newVoltageControl = (conFrag.voltageControlCheckbox.isChecked() ? 1 : 0);
                    newMinimumVoltage = Float.parseFloat(confFrag.minimumVoltageTextInput.getText().toString());
                    newMaximumVoltage = Float.parseFloat(confFrag.maximumVoltageTextInput.getText().toString());
                    newNivelAC = Float.parseFloat(confFrag.nivelACInput.getText().toString());


                    stringToSend = String.format(Locale.ENGLISH, "$Config,%04d,%04d,%1.1f,%1.1f,%1.1f,&",
                            newSourceAddress, newDestinationAddress, newMinimumVoltage, newMaximumVoltage, newNivelAC);
                    if(socket.isConnected()){
                        System.out.println("Este es el string que mando: " + stringToSend);
                        outputStream.write(stringToSend.getBytes());
                        //waitMs(10);
                        //Guardar configuración
                        showToast("Configuración Enviada");
                    }else{
                        showToast("Conexión Perdida");

                    }

                }else {
                    //null or empty
                    showToast("Favor de llenar todos los datos");
                }
            } catch (Exception e)
            {
                Toast.makeText(getApplicationContext(), getString(R.string.invalidConfig_message), Toast.LENGTH_SHORT).show();
            }

        }
        else
        {
            Toast.makeText(getApplicationContext(), getString(R.string.disconnected_message), Toast.LENGTH_SHORT).show();
        }
    }


    public void updateConfigField(int field, float value)
    {
        switch(field)
        {
            case SOURCE_ADDRESS:
                confFrag.sourceAddressTextInput.setText(String.format(Locale.ENGLISH,"%05d", (int)value));
                break;
            case DESTINATION_ADDRESS:
                confFrag.destinationAddressTextInput.setText(String.format(Locale.ENGLISH,"%05d", (int)value));
                break;
            case VOLTAGE_CONTROL_CHECKBOX:
                if((int)value == 0)
                    confFrag.voltageControlCheckbox.setChecked(false);
                else
                    confFrag.voltageControlCheckbox.setChecked(true);
                break;
            case MINIMUM_VOLTAGE:
                confFrag.minimumVoltageTextInput.setText(String.format(Locale.ENGLISH,"%1.1f", value));
                break;
            case MAXIMUM_VOLTAGE:
                confFrag.maximumVoltageTextInput.setText(String.format(Locale.ENGLISH,"%1.1f", value));
                break;
            case AC_VOLTAGE:
                confFrag.nivelACInput.setText(String.format(Locale.ENGLISH,"%1.1f", value));
                break;
        }
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


            case R.id.btnConfiguracion:
                if (connected) {
                    print("Estoy en conf");
                    //FragmentManager fragmentManager = getSupportFragmentManager();
                    //fragmentManager.beginTransaction().replace(R.id.fragment_container, confFrag).commit();

                } else {
                    showToast("Bluetooth Desconectado");
                }
                break;
            case R.id.btnRadio:
                if (connected) {
                    print("Estoy en Radio");
                    //FragmentManager fragmentManager = getSupportFragmentManager();
                    //fragmentManager.beginTransaction().replace(R.id.fragment_container, radioFrag).commit();
                } else {
                    showToast("Bluetooth Desconectado");
                }
                break;

            case R.id.btnVoltaje:
                if (connected) {
                    print("Estoy en Mediciones");
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    fragmentManager.beginTransaction().replace(R.id.fragment_container, medFrag).commit();
                } else {
                    showToast("Bluetooth Desconectado");
                }
                break;



        }
    }
    void sendStop() throws IOException{
        System.out.println("Estoy en sendStop");
        String msg = "$Parar&";
        outputStream.write(msg.getBytes());
        outputStream.write(msg.getBytes());
        outputStream.write(msg.getBytes());
        outputStream.write(msg.getBytes());

    }
    void sendOpen() throws IOException
    {
        System.out.println("Estoy en el Open");
        String msg = "$Abrir&";
        outputStream.write(msg.getBytes());
    }
    void sendClose() throws IOException
    {
        System.out.println("Estoy en el Close");
        String msg = "$Cerrar&";
        outputStream.write(msg.getBytes());
    }
    void sendPassword(String pass) throws IOException
    {
        System.out.println("El control = Pass");
        //control = "Pass"; PROBABLEMENTE SE USE DESPUES
        System.out.println("Estoy en el sendPassword");
        String msg = "$PASS=" + pass + ",& ";
        System.out.println("Este es el pass que mando " + msg);
        outputStream.write(msg.getBytes());
    }

    //Input Dialog para ingresar el password para permitir el cambio a remoto
    public void showPasswordDialog(final String title, final String message){
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        final EditText edittext = new EditText(getApplicationContext());
        edittext.setInputType(InputType.TYPE_CLASS_NUMBER);
        edittext.setTextColor(getResources().getColor(R.color.black));
        edittext.setRawInputType(Configuration.KEYBOARD_12KEY);
        alert.setMessage(message);
        alert.setTitle(title);
        alert.setView(edittext);

        alert.setPositiveButton("Ingresar", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String pass = edittext.getText().toString();
                //Pasar el pass con este comando = $PASS=12345,& regresa OK si es correcto o error si incorrecto

                try{
                    sendPassword(pass);
                }catch(IOException e){

                }


            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Cerrar el input dialog
                dialog.dismiss();

            }
        });

        alert.show();
    }

    public void showToast(final String message) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
