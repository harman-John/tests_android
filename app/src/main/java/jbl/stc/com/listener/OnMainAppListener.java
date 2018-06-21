package jbl.stc.com.listener;

import jbl.stc.com.activity.DashboardActivity;
import jbl.stc.com.fragment.BaseFragment;

/**
 * OnMainAppListener
 * Created by darren.lu on 2017/8/24.
 */

public interface OnMainAppListener {
    void backToDashboardPage();

    void refreshPage();

    DashboardActivity getMainActivity();

    void showOrHideFragment(boolean isShow, BaseFragment baseFragment);
}
