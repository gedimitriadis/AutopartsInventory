package com.example.android.autopartsinventory.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.autopartsinventory.data.PartContract.PartEntry;

/**
 * Created by georgeD on 15/07/2017.
 */

public class PartDbHelper extends SQLiteOpenHelper {
    public static final String LOG_TAG = PartDbHelper.class.getSimpleName();

    /**
     * Name of the database file
     */
    private static final String DATABASE_NAME = "autoparts.db";

    /**
     * Database version. If you change the database schema, you must increment the database version.
     */
    private static final int DATABASE_VERSION = 1;

    /**
     * Constructs a new instance of {@link PartDbHelper}.
     *
     * @param context of the app
     */
    public PartDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * This is called when the database is created for the first time.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create a String that contains the SQL statement to create the parts table
        String SQL_CREATE_PARTS_TABLE = "CREATE TABLE " + PartEntry.TABLE_NAME + " ("
                + PartEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + PartEntry.COLUMN_IMAGE + " BLOB , "
                + PartEntry.COLUMN_NAME + " TEXT NOT NULL, "
                + PartEntry.COLUMN_QUANTITY + " INTEGER NOT NULL DEFAULT 0 , "
                + PartEntry.COLUMN_PRICE + " INTEGER NOT NULL , "
                + PartEntry.COLUMN_SUPPLIER_MAIL + " TEXT NOT NULL );";

        // Execute the SQL statement
        db.execSQL(SQL_CREATE_PARTS_TABLE);
    }

    /**
     * This is called when the database needs to be upgraded.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // The database is still at version 1, so there's nothing to do be done here.
    }
}
