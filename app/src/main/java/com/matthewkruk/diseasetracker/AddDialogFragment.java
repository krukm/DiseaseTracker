package com.matthewkruk.diseasetracker;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Spinner;

public class AddDialogFragment extends DialogFragment {
    public static final String TAG = AddDialogFragment.class.getName();

    public interface Listener {
        void onAdd(String schoolName);
    }

    private Listener mListener;

    public static AddDialogFragment newInstance(Listener listener){
        AddDialogFragment addDialogFragment = new AddDialogFragment();
        addDialogFragment.mListener = listener;
        return addDialogFragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Context context = getActivity();
        final View view = LayoutInflater.from(context).inflate(R.layout.dialog_add, null, false);
        final Spinner schoolSpinner = view.findViewById(R.id.schools_spinner);

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);
        builder.setCancelable(true);
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (mListener != null) {
                    mListener.onAdd(
                            schoolSpinner.getSelectedItem().toString());
                }
            }
        });

        return builder.create();
    }
}
