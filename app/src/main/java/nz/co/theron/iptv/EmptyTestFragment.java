package nz.co.theron.iptv;

import android.os.Bundle;
import android.text.InputType;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.leanback.app.GuidedStepSupportFragment;
import androidx.leanback.widget.GuidanceStylist;
import androidx.leanback.widget.GuidedAction;

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





    @Override
    public void onCreateActions(@NonNull List<GuidedAction> actions, Bundle savedInstanceState) {

    }



    @Override
    public void onGuidedActionClicked(GuidedAction action) {

    }
}

