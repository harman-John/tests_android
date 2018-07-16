package jbl.stc.com.fragment;


import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import jbl.stc.com.R;


public class ProductHelpFragment extends BaseFragment implements View.OnClickListener {
    private static final String TAG = ProductHelpFragment.class.getSimpleName();
    private View view;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_product_help,
                container, false);
        view.findViewById(R.id.relative_layout_product_help_item_one).setOnClickListener(this);
        view.findViewById(R.id.image_view_product_help_back).setOnClickListener(this);
        return view;
    }


    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.relative_layout_product_help_item_one: {

                break;
            }
            case R.id.image_view_product_help_back:{
                getActivity().onBackPressed();
                break;
            }
        }

    }
}
