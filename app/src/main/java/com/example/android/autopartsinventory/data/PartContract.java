package com.example.android.autopartsinventory.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by georgeD on 15/07/2017.
 */

public class PartContract {

    /**
     * Define Content Authority according to {@link android.Manifest} and {@link PartProvider}
     */
    public static final String CONTENT_AUTHORITY = "com.example.android.autopartsinventory";

    /**
     * Define Content URI
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /**
     * Define path for the table which will be appended to the base content URI
     */
    public static final String PATH_PARTS = "parts";

    private PartContract() {
    }

    public static final class PartEntry implements BaseColumns {

        // Create a full URI for the class
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_PARTS);

        // Table name
        public static final String TABLE_NAME = "parts";

        // The _id field to index the table content
        public static final String _ID = BaseColumns._ID;

        // The part image
        public static final String COLUMN_IMAGE = "image";

        // The part name
        public static final String COLUMN_NAME = "name";

        // The part quantity
        public static final String COLUMN_QUANTITY = "quantity";

        // The part price
        public static final String COLUMN_PRICE = "price";

        // The mail of the part supplier
        public static final String COLUMN_SUPPLIER_MAIL = "supplier_mail";

        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of parts.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PARTS;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single part.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PARTS;
    }

}
