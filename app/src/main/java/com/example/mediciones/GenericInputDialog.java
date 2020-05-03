package com.example.mediciones;

import android.app.Activity;
import android.app.DialogFragment;
import android.os.Bundle;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Created by sergiotrevino on 1/19/18.
 */

public class GenericInputDialog extends DialogFragment {

    public static GenericInputDialog newInstance(Bundle dialogInfo) {
        GenericInputDialog dialog = new GenericInputDialog();

        // Supply parameters
        dialog.setArguments(dialogInfo);

        return dialog;
    }

    View rootView;

    Activity callerActivity;

    String inputText, inputHint;
    int inputType;
    int field;
    int minValue, maxValue;

    TextView inputTextLabel, inputTextHint;
    EditText inputEditText;
    TextView errorTextLabel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        rootView = inflater.inflate(R.layout.genericinputdialog, container, false);

        Bundle arguments = getArguments();

        field = arguments.getInt("field", -1);

        inputText = arguments.getString("inputText", "");
        inputHint = arguments.getString("inputHint", "");

        inputType = arguments.getInt("inputType", InputType.TYPE_CLASS_NUMBER);

        minValue = arguments.getInt("minValue", 0);
        maxValue = arguments.getInt("maxValue", 0);

        inputTextLabel = (TextView) rootView.findViewById(R.id.inputDialogTextLabel);
        inputTextLabel.setText(inputText);

        inputTextHint = (TextView) rootView.findViewById(R.id.inputDialogTextHint);
        inputTextHint.setText(inputHint);

        if(inputHint.equals(""))
            inputTextHint.setVisibility(View.GONE);
        else
            inputTextHint.setVisibility(View.VISIBLE);

        inputEditText = (EditText) rootView.findViewById(R.id.inputDialogEditText);
        inputEditText.setOnEditorActionListener(actionListener);
        inputEditText.setInputType(inputType);

        errorTextLabel = (TextView) rootView.findViewById(R.id.errorTextLabel);

        callerActivity = getActivity();

        return rootView;
    }

    float decodeInput(EditText inputView)
    {
        String inputString = inputView.getText().toString();
        float value = -1; //Initialized with invalid value

        try{
            value = Float.parseFloat(inputString);
        } catch(NumberFormatException e) {}

        return value;
    }

    TextView.OnEditorActionListener actionListener = new TextView.OnEditorActionListener() {

        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

            if(actionId == EditorInfo.IME_ACTION_DONE)
            {
                switch(v.getId())
                {
                    case R.id.inputDialogEditText:

                        float value = decodeInput((EditText) v);

                        if(value >= minValue && value <= maxValue)
                        {
                            //NOTE: Should be replaced by an interface
                            if(callerActivity instanceof MainActivity)
                                ((MainActivity) callerActivity).updateConfigField(field, value);

                            getDialog().dismiss();
                        }
                        else
                        {
                            //Notify of invalid input
                            inputEditText.setText("");
                            errorTextLabel.setVisibility(View.VISIBLE);
                        }

                        return true;
                }
            }

            return false;
        }
    };}