package com.ferran.taskinator.dialogs;

import android.view.View;

public class InsertDialogFragment extends MyDialogFragment {
    @Override
    protected void connectToDB() {
        dbc.createTask(etTitle.getText().toString(),
                etStartTime.getText().toString(), priority, category, image);
    }

    @Override
    protected void setUpDialog() {
        tvDone.setVisibility(View.GONE);
        btAction.setText("ADD");
    }
}
