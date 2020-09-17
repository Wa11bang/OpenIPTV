package com.openiptv.code;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainFragment extends Fragment {
    private static final String URL =
            "http://github.com/googlesamples/androidtv-sample-inputs/blob/master/README.md";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.main_fragment, null);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        WebView webView = (WebView) getView();
        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl(URL);
    }
}
