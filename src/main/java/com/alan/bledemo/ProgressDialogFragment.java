package com.alan.bledemo;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;


public class ProgressDialogFragment extends DialogFragment {

    protected static final String PROGRESS_DIALOG_TAG = "Progress_Dialog";

    public static void show(FragmentActivity activity, String msg, boolean cancelable) {
        if (activity == null || activity.isFinishing()) {
            return;
        }
        Bundle args = new Bundle();
        args.putString("msg", msg);

        ProgressDialogFragment f = new ProgressDialogFragment();
        f.setCancelable(cancelable);
        f.setArguments(args);

        FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction();
        ft.add(f, PROGRESS_DIALOG_TAG);
        ft.commitAllowingStateLoss();
    }

    public static void hide(FragmentActivity activity) {
        if (activity != null && !activity.isFinishing()) {
            FragmentManager manager = activity.getSupportFragmentManager();
            ProgressDialogFragment fragment = (ProgressDialogFragment) manager.findFragmentByTag(PROGRESS_DIALOG_TAG);
            if (fragment != null) {
                manager.beginTransaction().remove(fragment).commitAllowingStateLoss();
            }
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = getArguments();
        String message = args.getString("msg");

        ProgressDialog dialog = new ProgressDialog(getActivity());
        if (!TextUtils.isEmpty(message)) {
            dialog.setMessage(message);
        }
        dialog.setIndeterminate(true);
        dialog.setCanceledOnTouchOutside(false);

        return dialog;
    }

}
