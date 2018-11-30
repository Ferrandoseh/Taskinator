package com.ferran.taskinator.dialogs;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.res.ResourcesCompat;
import android.text.format.DateFormat;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.ferran.taskinator.DatabaseConnector;
import com.ferran.taskinator.ImageCompressor;
import com.ferran.taskinator.R;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;


public abstract class MyDialogFragment extends DialogFragment {

    private static final int CAMERA_REQUEST = 1888;
    protected TextView priorityOne, priorityTwo, priorityThree, tvDone;
    protected EditText etTitle, etStartTime;
    private Spinner sCategory;
    protected Button btAction;
    protected ImageView ivCamera;
    protected boolean done;
    protected DatabaseConnector dbc;

    protected String title, startTime, image;
    protected int priority, category, id;

    private OnDismissListener onDismissListener;
    private DialogInterface.OnCancelListener onCancelListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_dialog, container, false);

        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }
        etTitle = v.findViewById(R.id.etTitle);
        priorityOne = v.findViewById(R.id.priorityOne);
        priorityTwo = v.findViewById(R.id.priorityTwo);
        priorityThree = v.findViewById(R.id.priorityThree);
        tvDone = v.findViewById(R.id.tvDone);
        ivCamera = v.findViewById(R.id.ivCamera);
        etStartTime = v.findViewById(R.id.etStartTime);
        sCategory = v.findViewById(R.id.sCategory);
        btAction = v.findViewById(R.id.btAction);

        image = title = "";
        priority = 3;

        setUpDialog();

        setPriority(priority);

        ArrayAdapter<CharSequence> adapter =  ArrayAdapter.createFromResource(
                v.getContext(), R.array.categories, android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sCategory.setAdapter(adapter);
        sCategory.setSelection(category-1);

        priorityOne.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                setPriority(1);
            }
        });
        priorityTwo.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                setPriority(2);
            }
        });
        priorityThree.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                setPriority(3);
            }
        });
        etStartTime.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                showStartDatePickerDialog(v);
            }
        });
        ivCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, CAMERA_REQUEST);
            }
        });
        btAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                title = etTitle.getText().toString();
                if(!title.equals("")) {
                    dbc = null;
                    try {
                        dbc = new DatabaseConnector(view.getContext()).open();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }

                    Cursor cursor = null;
                    try {
                        cursor = dbc.getCategoryName(sCategory.getSelectedItem().toString());
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }

                    int index;
                    index = cursor.getColumnIndexOrThrow("_id");

                    category = cursor.getInt(index);

                    connectToDB();

                    dbc.close();

                    dismiss();
                }
                else
                    Toast.makeText(getContext(), getString(R.string.error_title), Toast.LENGTH_SHORT).show();
            }
        });
        return v;
    }

    public void setOnDismissListener(OnDismissListener onDismissListener) {
        this.onDismissListener = onDismissListener;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (onDismissListener != null) {
            onDismissListener.onDismiss(dialog);
        }
    }


    public void setOnCancelListener(DialogInterface.OnCancelListener onCancelListener) {
        this.onCancelListener = onCancelListener;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        if (onCancelListener != null) {
            onCancelListener.onCancel(dialog);
        }
    }

    protected abstract void connectToDB();

    protected abstract void setUpDialog();

    protected void setDoneText(Boolean done) {
        this.done = done;
        if (done) {
            tvDone.setText("Task done");
            tvDone.setTextColor(Color.GREEN);
        } else {
            tvDone.setText("Not finished");
            tvDone.setTextColor(Color.RED);
        }
    }

    private void setPriority(int priority) {
        this.priority = priority;
        int p1Color  = ResourcesCompat.getColor(getResources(), R.color.priority1, null);
        int p2Color  = ResourcesCompat.getColor(getResources(), R.color.priority2, null);
        int p3Color  = ResourcesCompat.getColor(getResources(), R.color.priority3, null);

        priorityOne.setTextColor(Color.GRAY);
        priorityTwo.setTextColor(Color.GRAY);
        priorityThree.setTextColor(Color.GRAY);
        if(priority == 1) priorityOne.setTextColor( p1Color );
        else if(priority == 2) priorityTwo.setTextColor( p2Color );
        else priorityThree.setTextColor( p3Color );
    }

    Calendar myCalendar = Calendar.getInstance();
    DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker datePicker, int y, int m, int d) {
            myCalendar.set(Calendar.YEAR, y);
            myCalendar.set(Calendar.MONTH, m);
            myCalendar.set(Calendar.DAY_OF_MONTH, d);
            showStartTimePickerDialog(getView());
        }
    };
    TimePickerDialog.OnTimeSetListener time = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker timePicker, int h, int m) {
            myCalendar.set(Calendar.HOUR, h);
            myCalendar.set(Calendar.MINUTE, m);
            updateLabel();
        }
    };

    private void updateLabel() {
        java.text.DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String startDate = dateFormat.format(myCalendar.getTime());
        etStartTime.setText(startDate);
    }

    private void showStartDatePickerDialog(View v) {
        new DatePickerDialog(v.getContext(), date, myCalendar
                .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showStartTimePickerDialog(View v) {
        new TimePickerDialog(v.getContext(),time, myCalendar
                .get(Calendar.HOUR), myCalendar.get(Calendar.MINUTE),
                DateFormat.is24HourFormat(v.getContext())).show();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent i) {
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            ivCamera.setImageBitmap((Bitmap) i.getExtras().get("data"));
            ivCamera.setScaleType(ImageView.ScaleType.CENTER_CROP);
            image = ImageCompressor.INSTANCE.compressAndEncodeAsBase64(
                    ((BitmapDrawable) ivCamera.getDrawable()).getBitmap());
        }
    }

    protected Bitmap getImageFromString(String image) {

        if (!image.equals("")) {
            byte[] decodeString = Base64.decode(image, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(decodeString, 0, decodeString.length);
        }
        return null;
    }
}
