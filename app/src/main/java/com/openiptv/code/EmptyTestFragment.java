package com.openiptv.code;

import android.database.Cursor;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.leanback.app.GuidedStepSupportFragment;
import androidx.leanback.widget.GuidanceStylist;
import androidx.leanback.widget.GuidedAction;

import java.util.ArrayList;
import java.util.List;

//
public class EmptyTestFragment extends GuidedStepSupportFragment {
    @NonNull
    @Override
    public GuidanceStylist.Guidance onCreateGuidance(Bundle savedInstanceState) {

        return new GuidanceStylist.Guidance(
                "Empty Fragment For Testing",
                "Empty Fragment For Testing",
                getString(R.string.account_label),
                ContextCompat.getDrawable(getActivity(), R.drawable.setup_logo2));
    }




    //TODO Remove in final
    @Override
    public void onCreateActions(@NonNull List<GuidedAction> actions, Bundle savedInstanceState) {
        DatabaseActions databaseActions = new DatabaseActions(getContext());

        //get the data and append to a list


        Cursor accountList = databaseActions.getAccounts();

        ArrayList<String> listData = new ArrayList<>();
        while(accountList.moveToNext()){
            //get the value from the database in column 1
            //then add it to the ArrayList
            listData.add(accountList.getString(1));
        }



        for (int i = 0; i < accountList.getCount(); i++) {
            Log.i("Account_List", listData.get(i));
        }
    }



    @Override
    public void onGuidedActionClicked(GuidedAction action) {

    }
}

