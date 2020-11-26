package com.alan.bledemo;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;


/**
 * Created by hanbing on 15/7/14.
 */
public class LoadingDialogFragment extends ProgressDialogFragment {

    public static void show(FragmentActivity activity) {
        if (activity == null || activity.isFinishing()) {
            return;
        }
        LoadingDialogFragment f = new LoadingDialogFragment();

        FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction();
        ft.add(f, PROGRESS_DIALOG_TAG);
        ft.commitAllowingStateLoss();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_loading, null);
        LinearLayout layout = v.findViewById(R.id.dialog_loading_view);
        Dialog loadingDialog = new Dialog(getActivity(), R.style.MyDialogLoading);

        loadingDialog.setCancelable(true);
        loadingDialog.setCanceledOnTouchOutside(false);
        loadingDialog.setContentView(layout,
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT));
        return loadingDialog;
    }
}
