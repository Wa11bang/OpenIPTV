package com.openiptv.code.fragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.openiptv.code.DatabaseActions;
import com.openiptv.code.R;

public class SigninFragment extends DialogFragment {
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = requireActivity().getLayoutInflater();


        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(inflater.inflate(R.layout.dialog_signin, null))
                // Add action buttons
                .setPositiveButton(R.string.signin, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        DatabaseActions dbAction = new DatabaseActions(getContext());

                        TextView textView = (TextView) getDialog().findViewById(R.id.username);
                        String username = textView.getText().toString();

                        TextView pcTextView = (TextView) getDialog().findViewById(R.id.signin_password);
                        String password = pcTextView.getText().toString();

                        boolean result = dbAction.checkParentControlPassword(username, password);

                        dbAction.close();

                        if (result == false) {
                            textView.setText("");
                            pcTextView.setText("");
                            //Toast.makeText(getDialog().getContext(), "Username or parent control password not correct!", Toast.LENGTH_SHORT);
                        } else if (result) {

                            TimerFragment fragment = new TimerFragment();
                            fragment.show(getActivity().getSupportFragmentManager(), null);
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //SigninFragment.this.getDialog().cancel();

                        getActivity().finish();
                    }
                });
        return builder.create();
    }

}
