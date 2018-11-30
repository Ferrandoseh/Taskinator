package com.ferran.taskinator;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DatabaseConnector
{
    // database columns
    private static final String KEY_ROWID 	                = "_id";
    private static final String KEY_NAME 	                = "name";
    private static final String KEY_TITLE    	            = "title";
    private static final String KEY_TIME                    = "start_time";
    private static final String KEY_PRIORITY 	            = "priority";
    private static final String KEY_DONE     	            = "done";
    private static final String KEY_IMG     	            = "image";
    private static final String KEY_CATEGORY 	            = "category";
    private static final String DATABASE_NAME           	= "taskinator";
    private static final String TASK_DATABASE_TABLE 	    = "Task";
    private static final String CATEGORY_DATABASE_TABLE 	= "Category";
    private static final int DATABASE_VERSION 	= 1;

    // SQL statement to create the database

    private static final String CATEGORY_DATABASE_CREATE =
            "CREATE TABLE Category (" +
                    "    _id integer NOT NULL CONSTRAINT Category_pk PRIMARY KEY AUTOINCREMENT," +
                    "    name varchar(255) NOT NULL" +
                    ");";

    private static final String TASK_DATABASE_CREATE =
            "CREATE TABLE Task (" +
                    "    _id integer NOT NULL CONSTRAINT Task_pk PRIMARY KEY AUTOINCREMENT," +
                    "    title varchar(255) NOT NULL," +
                    "    start_time DATETIME," +
                    "    priority integer NOT NULL," +
                    "    done boolean NOT NULL," +
                    "    image varchar(50000) NOT NULL," +
                    "    category integer NOT NULL," +
                    "    CONSTRAINT Task_Category FOREIGN KEY (category)" +
                    "    REFERENCES Category (_id)" +
                    ");";

    private final Context context;
    private DatabaseHelper DBHelper;
    private SQLiteDatabase db;

    // Constructor
    public DatabaseConnector(Context ctx)
    {
        //
        this.context 	= ctx;
        DBHelper 		= new DatabaseHelper(context);
    }

    public DatabaseConnector open() throws SQLException
    {
        db     = DBHelper.getWritableDatabase();
        return this;
    }

    public void close()
    {
        DBHelper.close();
    }

    private String fromDatetoString(Date date) {
        String dateStr = "";
        if (date != null) {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            dateStr = dateFormat.format(date);
        }
        return dateStr;
    }

    public long insertTask(String title, Date start_time, int priority, int category, String imageStr)
    {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_TITLE, title);
        initialValues.put(KEY_TIME, fromDatetoString(start_time));
        initialValues.put(KEY_PRIORITY, priority);
        initialValues.put(KEY_DONE, false);
        initialValues.put(KEY_IMG, imageStr);
        initialValues.put(KEY_CATEGORY, category);
        return db.insert("Task", null, initialValues);
    }

    public long insertCategory(String name)
    {
        ContentValues initialValues = new ContentValues();
        initialValues.put("name", name);
        return db.insert("Category", null, initialValues);
    }

    public boolean deleteTask(long rowId)
    {
        return db.delete(TASK_DATABASE_TABLE, KEY_ROWID +
                "=" + rowId, null) > 0;
    }

    public boolean deleteAll()
    {
        return db.delete(TASK_DATABASE_TABLE, null, null) > 0;
    }

    public Cursor getAllTasks()
    {
        return db.query(TASK_DATABASE_TABLE, new String[]
                        {
                                KEY_ROWID,
                                KEY_TITLE,
                                KEY_TIME,
                                KEY_PRIORITY,
                                KEY_DONE,
                                KEY_IMG,
                                KEY_CATEGORY
                        },
                null, null, null, null, KEY_TIME);
    }

    public Cursor getDayTasks()
    {
        return db.query(TASK_DATABASE_TABLE, new String[]
                        {
                                KEY_ROWID,
                                KEY_TITLE,
                                KEY_TIME,
                                KEY_PRIORITY,
                                KEY_DONE,
                                KEY_IMG,
                                KEY_CATEGORY
                        },
                "date("+KEY_TIME+") = date('now')", null, null, null, KEY_TIME);
    }

    public Cursor getMonthTasks()
    {
        return db.query(TASK_DATABASE_TABLE, new String[]
                        {
                                KEY_ROWID,
                                KEY_TITLE,
                                KEY_TIME,
                                KEY_PRIORITY,
                                KEY_DONE,
                                KEY_IMG,
                                KEY_CATEGORY
                        },
                        "date(" + KEY_TIME + ") > date('now') AND  date(" + KEY_TIME + ")" +
                                " <= date('now','+1 month')", null, null, null, KEY_TIME);
    }

    public Cursor getSomedayTasks()
    {
        return db.query(TASK_DATABASE_TABLE, new String[]
                        {
                                KEY_ROWID,
                                KEY_TITLE,
                                KEY_TIME,
                                KEY_PRIORITY,
                                KEY_DONE,
                                KEY_IMG,
                                KEY_CATEGORY
                        },
                KEY_TIME + "=" + "\"\"" , null, null, null, KEY_TIME);
    }

    public Cursor getTask(long rowId) throws SQLException
    {
        Cursor mCursor =
                db.query(true, TASK_DATABASE_TABLE, new String[]
                                {
                                        KEY_ROWID,
                                        KEY_TITLE,
                                        KEY_TIME,
                                        KEY_PRIORITY,
                                        KEY_DONE,
                                        KEY_IMG,
                                        KEY_CATEGORY
                                },
                        KEY_ROWID + "=" + rowId,  null, null, null, null, null);

        if (mCursor != null)
        {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    public Cursor getCategoryName(String categoryName) throws SQLException
    {
        Cursor mCursor =
                db.query(true, CATEGORY_DATABASE_TABLE, new String[]
                                {
                                        KEY_ROWID,
                                        KEY_NAME
                                },
                        KEY_NAME + "=" + "\"" + categoryName + "\"",  null, null, null, null, null);

        if (mCursor != null)
        {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    public boolean updateTask_Done(int rowId, boolean done)
    {
        ContentValues args = new ContentValues();
        args.put(KEY_DONE, done ? "true" : "false");
        return db.update(TASK_DATABASE_TABLE, args,
                KEY_ROWID + "=" + rowId, null) > 0;
    }

    public boolean updateTask(int rowId, String start_time, int priority, int category, String imageStr)
    {
        ContentValues args = new ContentValues();
        args.put(KEY_TIME, start_time);
        args.put(KEY_PRIORITY, priority);
        args.put(KEY_CATEGORY, category);
        args.put(KEY_IMG, imageStr);
        return db.update(TASK_DATABASE_TABLE, args,
                KEY_ROWID + "=" + rowId, null) > 0;
    }

    public boolean createTask(String title, String start_time, int priority, int category, String imageStr) {
        ContentValues args = new ContentValues();
        args.put(KEY_TITLE, title);
        args.put(KEY_TIME, start_time);
        args.put(KEY_PRIORITY, priority);
        args.put(KEY_DONE, false);
        args.put(KEY_CATEGORY, category);
        args.put(KEY_IMG, imageStr);
        return db.insert(TASK_DATABASE_TABLE,null, args) > 0;
    }

    ///////////////////////// nested dB helper class ///////////////////////////////////////
    private static class DatabaseHelper extends SQLiteOpenHelper
    {
        DatabaseHelper(Context context)
        {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db)
        {
            db.execSQL(CATEGORY_DATABASE_CREATE);
            db.execSQL(TASK_DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion,
                              int newVersion)
        {
            /// on upgrade drop older tables
            db.execSQL("DROP TABLE IF EXISTS " + TASK_DATABASE_TABLE);
            db.execSQL("DROP TABLE IF EXISTS " + CATEGORY_DATABASE_TABLE);

            // create new tables
            onCreate(db);
        }
    }
    //////////////////////////// end nested dB helper class //////////////////////////////////////

}

