package com.example.taskapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "TaskDatabase.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_TASKS = "Tasks";


    private static final String COLUMN_ID = "id";
    private static final String COLUMN_TITLE = "title";
    private static final String COLUMN_DESCRIPTION = "description";
    private static final String COLUMN_DUE_DATE = "due_date";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableQuery = "CREATE TABLE " + TABLE_TASKS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_TITLE + " TEXT NOT NULL, "
                + COLUMN_DESCRIPTION + " TEXT, "
                + COLUMN_DUE_DATE + " TEXT NOT NULL)";
        db.execSQL(createTableQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TASKS);
        onCreate(db);
    }

    public long insertTask(String title, String description, String dueDate) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, title);
        values.put(COLUMN_DESCRIPTION, description);
        values.put(COLUMN_DUE_DATE, dueDate);

        return db.insert(TABLE_TASKS, null, values);
    }

    public Cursor getAllTasks() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(
                TABLE_TASKS,
                null,
                null,
                null,
                null,
                null,
                COLUMN_DUE_DATE + " ASC"
        );
    }

    public Cursor getTaskById(long taskId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(
                TABLE_TASKS,
                null,
                COLUMN_ID + "=?",
                new String[]{String.valueOf(taskId)},
                null,
                null,
                null
        );
    }

    public boolean updateTask(long taskId, String title, String description, String dueDate) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, title);
        values.put(COLUMN_DESCRIPTION, description);
        values.put(COLUMN_DUE_DATE, dueDate);

        int rowsAffected = db.update(
                TABLE_TASKS,
                values,
                COLUMN_ID + "=?",
                new String[]{String.valueOf(taskId)}
        );

        return rowsAffected > 0;
    }

    public boolean deleteTask(long taskId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsAffected = db.delete(
                TABLE_TASKS,
                COLUMN_ID + "=?",
                new String[]{String.valueOf(taskId)}
        );

        return rowsAffected > 0;
    }

}
