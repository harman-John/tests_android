package jbl.stc.com.fragment;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.TextView;

import jbl.stc.com.R;

public class WebViewFragment extends BaseFragment implements View.OnClickListener {
    public static final String TAG = WebViewFragment.class.getSimpleName();
    private View view;
    private WebView webView;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_web_view,
                container, false);
        webView = view.findViewById(R.id.web_view_how_to_pair);
        view.findViewById(R.id.image_view_web_view_back).setOnClickListener(this);
        WebSettings webSettings = webView.getSettings();

        webSettings.setJavaScriptEnabled(true);
        webView.setVisibility(View.VISIBLE);
        webView.loadUrl("https://www.youtube.com/embed/hbjG1lCTZio?iv_load_policy=3");
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.image_view_web_view_back:{
                getActivity().onBackPressed();
                break;
            }

        }
    }
}
