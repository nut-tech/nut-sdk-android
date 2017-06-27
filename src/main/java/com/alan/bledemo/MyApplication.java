package com.alan.bledemo;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import java.util.LinkedList;

/**
 * Created by alan on 2016/12/20.
 */

public class MyApplication extends Application implements Application.ActivityLifecycleCallbacks{

    private static LinkedList<Activity> mActivities = new LinkedList<>();

    @Override
    public void onCreate() {
        super.onCreate();
        registerActivityLifecycleCallbacks(this);
    }

    public static boolean isAppInBackground() {
        return mActivities.size() == 0;
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(Activity activity) {
        mActivities.add(activity);
    }

    @Override
    public void onActivityResumed(Activity activity) {

    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {
        mActivities.remove(activity);
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }
}
