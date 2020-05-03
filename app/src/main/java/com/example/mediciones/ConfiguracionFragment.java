package com.example.mediciones;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.io.IOException;

public class ConfiguracionFragment extends Fragment implements View.OnClickListener {
    Button programButton, solicitarButton;
    TextView sourceAddressTextInput, destinationAddressTextInput, minimumVoltageTextInput, maximumVoltageTextInput, nivelACInput;
    CheckBox voltageControlCheckbox;
    View view;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.configuracion_fragment, null);
        initItems();
        return view;
    }

    public void initItems(){
        sourceAddressTextInput = (TextView) view.findViewById(R.id.sourceAddressTextInput);
        destinationAddressTextInput = (TextView) view.findViewById(R.id.destinationAddressTextInput);
        //voltageControlCheckbox = (CheckBox) view.findViewById(R.id.voltageControlCheckbox);
        minimumVoltageTextInput = (TextView) view.findViewById(R.id.minimumVoltageTextInput);
        maximumVoltageTextInput = (TextView) view.findViewById(R.id.maximumVoltageTextInput);
        nivelACInput = (TextView) view.findViewById(R.id.NivelACInput);

        //Bordes de los inputs
        sourceAddressTextInput.setBackgroundResource(R.drawable.bordercolor);
        destinationAddressTextInput.setBackgroundResource(R.drawable.bordercolor);
        minimumVoltageTextInput.setBackgroundResource(R.drawable.bordercolor);
        maximumVoltageTextInput.setBackgroundResource(R.drawable.bordercolor);
        nivelACInput.setBackgroundResource(R.drawable.bordercolor);

        solicitarButton = (Button) view.findViewById(R.id.solicitarButton);
        solicitarButton.setOnClickListener(this);
        programButton = (Button) view.findViewById(R.id.programButton);
        programButton.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (view.getId()){
            case R.id.solicitarButton:
                if(((MainActivity) getActivity()).boolPassword == true)
                {
                    System.out.println("Estoy en el solicitarButton");
                    try{
                        ((MainActivity)getActivity()).sendStop();
                    }catch (IOException e){
                        System.out.println("Error: " + e);
                    }
                }else{
                    ((MainActivity) getActivity()).showPasswordDialog("Ingrese la contraseña", "");
                }
                break;

            case R.id.programButton:
                if(((MainActivity) getActivity()).boolPassword == true)
                {
                    System.out.println("Estoy en el programConfiguration");
                    ((MainActivity)getActivity()).programConfiguration();
                    ((MainActivity)getActivity()).showToast("¡ Configuración Enviada !");

                }else{
                    ((MainActivity) getActivity()).showPasswordDialog("Ingrese la contraseña", "");
                }

                break;
        }
    }
}
