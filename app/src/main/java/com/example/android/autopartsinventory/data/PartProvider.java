package com.example.android.autopartsinventory.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.android.autopartsinventory.data.PartContract.PartEntry;

import java.sql.Blob;

/**
 * Created by georgeD on 15/07/2017.
 */

public class PartProvider extends ContentProvider {

    // Set the integer value for multiple rows in table
    private static final int PARTS = 100;
    // Set the integer value for a single row in table
    private static final int PART_ID = 101;
    // Creates a UriMatcher object.
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        // Add Uri matcher for multiple rows in table
        sUriMatcher.addURI(PartContract.CONTENT_AUTHORITY, PartContract.PATH_PARTS, PARTS);
        // Add Uri matcher for a single row in table
        sUriMatcher.addURI(PartContract.CONTENT_AUTHORITY, PartContract.PATH_PARTS + "/#", PART_ID);
    }

    /**
     * Tag for the log messages
     */
    public static final String LOG_TAG = PartProvider.class.getSimpleName();

    /**
     * public Database helper
     */
    private PartDbHelper mDbHelper;

    @Override
    public boolean onCreate() {
        mDbHelper = new PartDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        // Open a database to read from it
        SQLiteDatabase database = mDbHelper.getReadableDatabase();
        // Define Cursor object
        Cursor cursor = null;
        // Match the Uri to know which type of query is being done by using switch/case
        int match = sUriMatcher.match(uri);
        switch (match) {
            case PARTS:
                cursor = database.query(PartEntry.TABLE_NAME, projection, null, null, null, null, sortOrder);
                break;
            case PART_ID:
                selection = PartEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(PartEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:

        }
        // Set notification URI so the cursor can be updated automatically
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        // Return Cursor object
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PARTS:
                return PartEntry.CONTENT_LIST_TYPE;
            case PART_ID:
                return PartEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        // Match the Uri to know which table we're inserting new data
        int match = sUriMatcher.match(uri);
        switch (match) {
            case PARTS:
                return insertPart(uri, values);
            default:
                throw new IllegalArgumentException("Cannot query unknown URI" + uri);
        }
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        // Track the number of rows that were deleted
        int rowsDeleted;
        // Match the Uri to know which type of query is being done by using switch/case
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PARTS:
                // Delete all rows that match the selection and selection args
                rowsDeleted = database.delete(PartEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case PART_ID:
                // Delete a single row given by the ID in the URI
                selection = PartEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(PartEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
        // If 1 or more rows were deleted, then notify all listeners that the data at the
        // given URI has changed
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PARTS:
                // Set notification URI so the cursor can be updated automatically
                getContext().getContentResolver().notifyChange(uri, null);
                return updatePart(uri, values, selection, selectionArgs);
            case PART_ID:
                // Set notification URI so the cursor can be updated automatically
                getContext().getContentResolver().notifyChange(uri, null);
                // For the Part_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = PartEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updatePart(uri, values, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    private Uri insertPart(Uri uri, ContentValues values) {
        // Check if there is a name value
        String name = values.getAsString(PartEntry.COLUMN_NAME);
        if (name == null) {
            throw new IllegalArgumentException("Part requires a name");
        }

        // Check if there is a quantity value
        Integer quantity = values.getAsInteger(PartEntry.COLUMN_QUANTITY);
        if (quantity != null && quantity < 0) {
            throw new IllegalArgumentException("Part requires valid quantity");
        }

        // Check if there is a price value
        Integer price = values.getAsInteger(PartEntry.COLUMN_PRICE);
        if (price != null && price < 0) {
            throw new IllegalArgumentException("Part requires valid price");
        }

        // Check if there is a suppliers mail
        String supplierMail = values.getAsString(PartEntry.COLUMN_SUPPLIER_MAIL);
        if (supplierMail == null) {
            throw new IllegalArgumentException("Part requires a valid supplier email to contact to");
        }

        /// Open a database to write mode
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Insert into the database and return the new register ID
        long id = database.insert(PartEntry.TABLE_NAME, null, values);
        // Return the Uri with the new ID to be used on the UI
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }
        // Set notification URI so the cursor can be updated automatically
        getContext().getContentResolver().notifyChange(uri, null);
        // Return the new URI with the ID (of the newly inserted row) appended at the end
        return ContentUris.withAppendedId(uri, id);
    }

    private int updatePart(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // if name is there
        if (values.containsKey(PartEntry.COLUMN_NAME)) {
            String name = values.getAsString(PartEntry.COLUMN_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Part requires a name");
            }
        }

        // if quantity is there
        if (values.containsKey(PartEntry.COLUMN_QUANTITY)) {
            Integer quantity = values.getAsInteger(PartEntry.COLUMN_QUANTITY);
            if (quantity != null && quantity < 0) {
                throw new IllegalArgumentException("Part requires valid quantity");
            }
        }

        // if price is there
        if (values.containsKey(PartEntry.COLUMN_PRICE)) {
            Integer price = values.getAsInteger(PartEntry.COLUMN_PRICE);
            if (price != null && price < 0) {
                throw new IllegalArgumentException("Part requires valid price");
            }
        }

        // if suppliers mail is there
        if (values.containsKey(PartEntry.COLUMN_SUPPLIER_MAIL)) {
            String supplierMail = values.getAsString(PartEntry.COLUMN_SUPPLIER_MAIL);
            if (supplierMail == null) {
                throw new IllegalArgumentException("Part requires a valid supplier email to contact to");
            }
        }

        // Open a database to write mode
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }

        // Update the selected Part in the inventory database table with the given ContentValues
        int rowsUpdated = database.update(PartEntry.TABLE_NAME, values, selection, selectionArgs);

        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed

        if (rowsUpdated != 0) {
            // Set notification URI so the cursor can be updated automatically
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows that were affected
        return rowsUpdated;
    }

}
