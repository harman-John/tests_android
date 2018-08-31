package jbl.stc.com.listener;

import jbl.stc.com.activity.DashboardActivity;
import jbl.stc.com.fragment.BaseFragment;

public interface OnMainAppListener {
    void backToDashboardPage();

    void refreshPage();

    DashboardActivity getMainActivity();

    void showOrHideFragment(boolean isShow, BaseFragment baseFragment);
}
