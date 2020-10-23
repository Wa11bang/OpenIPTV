package com.openiptv.code.fragments;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.openiptv.code.PreferenceUtils;

import java.util.Calendar;

public class TimeStartFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        //use current time as default
        final Calendar current = Calendar.getInstance();
        int hour = current.get(Calendar.HOUR_OF_DAY);
        int minute = current.get(Calendar.MINUTE);

        return new TimePickerDialog(getActivity(), this, hour, minute,
                DateFormat.is24HourFormat(getActivity()));
    }

    @Override
    public void onTimeSet(TimePicker timePicker, int i, int i1) {
        int startHour = timePicker.getHour();
        int startMinute = timePicker.getMinute();

        PreferenceUtils preferenceUtils = new PreferenceUtils(getContext());
        preferenceUtils.setInteger("startHour", startHour);
        preferenceUtils.setInteger("startMinute", startMinute);

        TimeEndFragment fragment = new TimeEndFragment();
        fragment.show(getActivity().getSupportFragmentManager(), null);
    }
}
