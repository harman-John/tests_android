package jbl.stc.com.lifecycle;


import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import java.util.Stack;



public class ActivityLifecycleMgr extends ActivityLifecycle {
    private static final ActivityLifecycleMgr instance = new ActivityLifecycleMgr();
    private Stack<Activity> mActivityStack = new Stack<>();

    private ActivityLifecycleMgr() {
    }

    public static ActivityLifecycleMgr getInstance() {
        return instance;
    }

    public void init(Application mApplication) {
        mApplication.registerActivityLifecycleCallbacks(this);
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        mActivityStack.add(activity);
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        mActivityStack.remove(activity);
    }

    public Activity getPenultimateActivity() {
        return mActivityStack.size() >= 2 ? mActivityStack.get(mActivityStack.size() - 2) : null;
    }

    public int getActivitySize() {
        return mActivityStack.size();
    }

    public Activity getAllActivity(int i) {
        if (mActivityStack.size()<1){
            return null;
        }
        return mActivityStack.get(i);
    }

}
