package com.example.mediciones;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.mediciones.R;

public class MedicionesFragment extends Fragment {
    TextView tvSenal, tvPaquetes, tvRuido, tvRadio, tvTemperatura, tvVoltaje2, tvVoltaje3, tvVoltaje4, tvCorriente1, tvCorriente2, tvCorriente3, tvCorriente4, tvFactor1, tvFactor2, tvFactor3, tvFactor4, tvPotencia1, tvPotencia2, tvPotencia3, tvPotencia4, tvControl, tvConsecutivo, tvTipo, tvContador, tvCalidad;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.mediciones_fragment, null);
        tvVoltaje2 = (TextView)view.findViewById(R.id.tvVoltaje2);
        tvVoltaje3 = (TextView)view.findViewById(R.id.tvVoltaje3);
        tvVoltaje4 = (TextView)view.findViewById(R.id.tvVoltaje4);

        tvCorriente2 = (TextView)view.findViewById(R.id.tvCorriente2);
        tvCorriente3 = (TextView)view.findViewById(R.id.tvCorriente3);
        tvCorriente4 = (TextView)view.findViewById(R.id.tvCorriente4);

        tvFactor2 = (TextView)view.findViewById(R.id.tvFactor2);
        tvFactor3 = (TextView)view.findViewById(R.id.tvFactor3);
        tvFactor4 = (TextView)view.findViewById(R.id.tvFactor4);

        tvPotencia2 = (TextView)view.findViewById(R.id.tvPotencia2);
        tvPotencia3 = (TextView)view.findViewById(R.id.tvPotencia3);
        tvPotencia4 = (TextView)view.findViewById(R.id.tvPotencia4);

        tvConsecutivo = (TextView)view.findViewById(R.id.tvConsecutivo);
        tvContador = (TextView)view.findViewById(R.id.tvContador);
        tvRadio = (TextView)view.findViewById(R.id.tvRadio);
        tvTemperatura = (TextView)view.findViewById(R.id.tvTemperatura);

        tvSenal = (TextView)view.findViewById(R.id.tvSeñal);
        tvPaquetes = (TextView)view.findViewById(R.id.tvPorcentaje);
        tvRuido = (TextView)view.findViewById(R.id.tvRuido);

        return view;

    }
}

