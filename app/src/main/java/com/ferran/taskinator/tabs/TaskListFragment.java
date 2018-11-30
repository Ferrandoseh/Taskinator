package com.ferran.taskinator.tabs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.ferran.taskinator.DatabaseConnector;
import com.ferran.taskinator.R;
import com.ferran.taskinator.dialogs.InsertDialogFragment;
import com.ferran.taskinator.dialogs.MyDialogFragment;
import com.ferran.taskinator.dialogs.UpdateDialogFragment;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;

public abstract class TaskListFragment extends ListFragment {

    protected DatabaseConnector dbc = null;
    protected Cursor cursor;

    private CheckBox cbDone;
    private TextView tvTitle, tvPriority;

    private FloatingActionButton fab;

    private boolean canceled;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View root = inflater.inflate(R.layout.fragment_main, container, false);

        fab = root.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                canceled = false;

                FragmentTransaction ft = getFragmentManager().beginTransaction();
                Fragment prev = getFragmentManager().findFragmentByTag("dialog");
                if (prev != null) {
                    ft.remove(prev);
                }
                ft.addToBackStack(null);

                MyDialogFragment dialogFragment = new InsertDialogFragment();

                dialogFragment.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        isCanceled();
                    }

                });
                dialogFragment.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        refreshList();
                        System.out.println("dismision");
                        if(!getCanceled())
                            Snackbar.make(view, "Task added", Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();


                    }
                });
                dialogFragment.show(ft, "dialog");
            }
        });
        try {
            dbc = new DatabaseConnector(inflater.getContext()).open();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        //preSetDB(dbc);

        getTasks();

        setTaskList(inflater.getContext());

        return root;
    }

    protected void isCanceled() {
        canceled = true;
    }

    protected boolean getCanceled() {
        return canceled;
    }

    private void setTaskList(final Context ctx) {
        String[] col = new String[]{"title", "priority"};
        int[] id_layout = new int[]{R.id.tvTitle, R.id.tvPriority};
        setListAdapter(new SimpleCursorAdapter(ctx, R.layout.row, cursor, col, id_layout) {
            @Override
            public View getView(final int position, final View convertView, ViewGroup parent) {
                final View row = super.getView(position, convertView, parent);

                cbDone = row.findViewById(R.id.cbDone);
                tvTitle = row.findViewById(R.id.tvTitle);
                tvPriority = row.findViewById(R.id.tvPriority);

                setPriorityStyle(cursor);
                setCategoryStyle(cursor);

                setDoneBehaviour(position, row, cursor);

                Integer index;
                index = cursor.getColumnIndexOrThrow("_id");
                final int id = cursor.getInt(index);
                index = cursor.getColumnIndexOrThrow("title");
                final String title = String.valueOf( cursor.getString(index) );
                index = cursor.getColumnIndexOrThrow("start_time");
                final String start_time = String.valueOf( cursor.getString(index) );
                index = cursor.getColumnIndexOrThrow("priority");
                final int priority = cursor.getInt(index);
                index = cursor.getColumnIndexOrThrow("category");
                final int category = cursor.getInt(index);
                index = cursor.getColumnIndexOrThrow("image");
                final String image = String.valueOf( cursor.getString(index) );
                index = cursor.getColumnIndexOrThrow("done");
                final boolean doneArg = Boolean.valueOf( cursor.getString(index) );

                row.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        FragmentTransaction ft = getFragmentManager().beginTransaction();
                        Fragment prev = getFragmentManager().findFragmentByTag("dialog");
                        if (prev != null) {
                            ft.remove(prev);
                        }
                        ft.addToBackStack(null);

                        MyDialogFragment dialogFragment = new UpdateDialogFragment();
                        Bundle args = new Bundle();
                        args.putInt("id", id);
                        args.putString("title", title);
                        args.putString("start_time", start_time);
                        args.putInt("priority", priority);
                        args.putInt("category", category);
                        args.putString("image", image);
                        args.putBoolean("done", doneArg);
                        dialogFragment.setArguments(args);

                        dialogFragment.setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                refreshList();
                            }
                        });

                        dialogFragment.show(ft, "dialog");
                    }
                });
                row.setOnLongClickListener(new View.OnLongClickListener()
                {
                    @Override
                    public boolean onLongClick(View v)
                    {
                        new AlertDialog.Builder(row.getContext())
                                .setTitle("Delete task")
                                .setMessage("Do you really want to delete the task?")
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        deleteTask(id, row);
                                    }})
                                .setNegativeButton(android.R.string.no, null).show();
                        return true;
                    }
                });
                return row;
            }
        });
    }

    public void refreshList() {
        getTasks();
        setTaskList(getContext());
    }

    protected abstract void getTasks();

    protected void setDoneBehaviour(int position, final View row, final Cursor cursor) {
        int index = cursor.getColumnIndexOrThrow("done");
        final Boolean done = Boolean.valueOf( cursor.getString(index) );
        cbDone.setChecked(done ? true : false);

        updateDoneStyle(done);
        int color;
        if(position%2 == 0)
            color = ResourcesCompat.getColor(getResources(), R.color.colorBgDark, null);
        else
            color = ResourcesCompat.getColor(getResources(), R.color.colorBgLight, null);

        row.setBackgroundColor(color);

        index = cursor.getColumnIndexOrThrow("_id");
        final int id = Integer.valueOf( cursor.getString(index) );

        cbDone.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                dbc.updateTask_Done(id, isChecked);

                String text;
                if(isChecked) text = "Task completed";
                else text = "Task not finished";
                Snackbar.make(getView(), text, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

                refreshList();
            }
        });
    }

    private void updateDoneStyle(Boolean done) {
        if(done) {
            tvTitle.setPaintFlags(tvTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            tvPriority.setVisibility(View.GONE);
        }
        else {
            tvTitle.setPaintFlags(tvTitle.getPaintFlags() & (~ Paint.STRIKE_THRU_TEXT_FLAG));
            tvPriority.setVisibility(View.VISIBLE);
        }
    }

    private void deleteTask(int id, View row) {
        dbc.deleteTask(id);
        refreshList();
        Log.i("Deleted task with id", String.valueOf( id ));
        Snackbar.make(getView(), "Task deleted", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }

    private void setCategoryStyle(Cursor cursor) {
        int index, color;
        index = cursor.getColumnIndexOrThrow("category");
        Integer category = Integer.valueOf( cursor.getString(index) );
        /*
        if(category == 1) color = Color.rgb(83, 140, 58);
        else if(category == 2) color = Color.rgb(115, 58, 140);
        else if(category == 3) color = Color.rgb(58, 83, 140);
        else if(category == 4) color = Color.rgb(140, 74, 58);
        else if(category == 5) color = Color.rgb(58, 124, 140);
        else color = Color.rgb(0, 0, 0);
        */
        tvTitle.setTextColor(
                ResourcesCompat.getColor(getResources(), R.color.colorTextItems, null)
        );
    }

    private void setPriorityStyle(Cursor cursor) {
        int color, index = cursor.getColumnIndexOrThrow("priority");
        int priority = Integer.valueOf( cursor.getString(index) );

        if (priority == 1) color = ResourcesCompat.getColor(getResources(), R.color.priority1, null);
        else if (priority == 2)  color = ResourcesCompat.getColor(getResources(), R.color.priority2, null);
        else  color = ResourcesCompat.getColor(getResources(), R.color.priority3, null);
        tvPriority.setTextColor(color);
    }

    private void preSetDB(DatabaseConnector dbc) {
        dbc.deleteAll();

        /*dbc.insertCategory("School", "Green");
        dbc.insertCategory("Home", "Brown");
        dbc.insertCategory("Sport", "Blue");
        dbc.insertCategory("Work", "Red");
        dbc.insertCategory("Personal", "Yellow");
        dbc.insertCategory("Others", "Grey");*/

        dbc.insertCategory("School");
        dbc.insertCategory("Home");
        dbc.insertCategory("Sport");
        dbc.insertCategory("Work");
        dbc.insertCategory("Personal");
        dbc.insertCategory("Others");

        Date dt = new Date();
        dbc.insertTask("Go to the club", dt, 2, 4,"");
        dbc.insertTask("Make friends", dt, 2, 5,"");
        dbc.insertTask("Call my mum", dt, 1, 3, "");
        dbc.insertTask("Learn a new language", dt, 1, 6, "");
        dbc.insertTask("Buy water", dt, 3, 2, "");
        dbc.insertTask("Look for a job", dt, 1, 6, "");
        Calendar c = Calendar.getInstance();
        c.setTime(dt);
        c.add(Calendar.DATE, 20);
        dt = c.getTime();
        dbc.insertTask("Do grocery shopping", null, 1, 1, "");
        dbc.insertTask("House work", null, 2, 2, "");
        dbc.insertTask("Whatever", null, 3, 3, "");
        dbc.insertTask("Go for a walk", null, 2, 4, "");
        dbc.insertTask("Wash clothes", null, 2, 5, "");
        dbc.insertTask("Help my brother with his homework", null, 1, 6, "");
        dbc.insertTask("Eat cheese", dt, 1, 1, "");
        dbc.insertTask("Play football", dt, 2, 2, "");
        dbc.insertTask("Read a book", dt, 3, 3, "");
        dbc.insertTask("Buy milk", dt, 2, 4, "");
        dbc.insertTask("Brush teeth", dt, 2, 5, "");
        dbc.insertTask("Hiking to the closest mountain", dt, 1, 6, "");
        dbc.insertTask("Fly to Manchester", dt, 1, 1, "");
        dbc.insertTask("Surfing in Bundoran", dt, 2, 2, "");
        dbc.insertTask("Turn the lights on", dt, 3, 3, "");
        dbc.insertTask("Fix my computer", dt, 3, 2, "");
        dbc.insertTask("Study spanish", dt, 3, 2, "");
        dbc.insertTask("Watch a monkeys documentary", dt, 3, 1, "");
        dbc.insertTask("Sleep well", dt, 3, 3, "");
    }


}
