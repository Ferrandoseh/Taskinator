package com.ferran.taskinator.dialogs;

import android.support.v4.content.res.ResourcesCompat;
import android.view.View;
import android.widget.ImageView;

import com.ferran.taskinator.R;

public class UpdateDialogFragment extends MyDialogFragment {
    @Override
    protected void connectToDB() {
        System.out.println("---\nId-"+id+"\nCategory-"+category);
        dbc.updateTask(id, etStartTime.getText().toString(), priority, category, image);
    }

    @Override
    protected void setUpDialog() {
        etTitle.setEnabled(false);
        etTitle.setTextColor(ResourcesCompat.getColor(getResources(), R.color.colorTextItems, null));

        id = getArguments().getInt("id");
        title = getArguments().getString("title");
        startTime = getArguments().getString("start_time");
        priority = getArguments().getInt("priority");
        category = getArguments().getInt("category");
        image = getArguments().getString("image");
        done = getArguments().getBoolean("done");

        tvDone.setVisibility(View.VISIBLE);

        etTitle.setText(title);
        setDoneText(done);
        etStartTime.setText(startTime);
        btAction.setText("UPDATE");

        if(!image.equals("")) {
            ivCamera.setImageBitmap( getImageFromString(image) );
            ivCamera.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }
    }
}
