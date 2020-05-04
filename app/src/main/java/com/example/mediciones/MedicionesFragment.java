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

    String cadena  = "F1 00 1C 00 09 29 03 30 00 DA 04 DA 04 DA 04 05 00 05 00 05 00 00 00 9D FF 9D FF 00 00 00 00 00 00 19 00 7D 00 0C 00 B4 6C 00 00 64 DA 62 F2";
    String cadena2 = "F1 00 1C 00 12 29 03 00 00 C2 04 C2 04 C2 04 DC 02 DB 02 E1 02 00 00 63 00 63 00 7D 03 7C 03 83 03 19 00 7D 00 0C 00 5D 75 00 00 64 EF 65 F2";
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

    @Override
    public void onStart() {
        ((MainActivity)getActivity()).readString(cadena2);
        super.onStart();
    }
}

