package jbl.stc.com.swipe;

import android.content.Context;
import android.support.v4.app.Fragment;



public class BaseSwipeBackFragment extends SwipeBackFragment {
    protected OnAddFragmentListener mAddFragmentListener;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnAddFragmentListener) {
            mAddFragmentListener = (OnAddFragmentListener) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mAddFragmentListener = null;
    }

    public interface OnAddFragmentListener {
        void onAddFragment(Fragment fromFragment, Fragment toFragment);
    }
}
