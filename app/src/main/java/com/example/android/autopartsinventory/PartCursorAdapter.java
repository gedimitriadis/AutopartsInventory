package com.example.android.autopartsinventory;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.autopartsinventory.data.PartContract.PartEntry;

import java.sql.Blob;

import static android.R.attr.data;
import static android.R.attr.name;

/**
 * Created by georgeD on 16/07/2017.
 */

public class PartCursorAdapter extends CursorAdapter {

    public PartCursorAdapter(Context context, Cursor c, boolean autoRequery) {
        super(context, c, autoRequery);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Inflate and return a new view without binding any data
        return LayoutInflater.from(context).inflate(R.layout.part_list_item, parent, false);
    }

    /**
     * This method binds the product data (in the current row pointed to by cursor) to the given
     * list item layout.
     */
    @Override
    public void bindView(final View view, final Context context, final Cursor cursor) {
        // Find part name, price, quantity, suppliers mail and part image fields to populate when inflating view
        TextView partName = (TextView) view.findViewById(R.id.name_TextView);
        TextView partPrice = (TextView) view.findViewById(R.id.price_TextView);
        TextView partQuantity = (TextView) view.findViewById(R.id.quantity_TextView_listItem);
        TextView partSupplierMail = (TextView) view.findViewById(R.id.supplier_TextView);
        ImageView partImage = (ImageView) view.findViewById(R.id.Image_View);

        // Extract values from Cursor object
        String name = cursor.getString(cursor.getColumnIndexOrThrow(PartEntry.COLUMN_NAME));
        double price = cursor.getDouble(cursor.getColumnIndexOrThrow(PartEntry.COLUMN_PRICE));
        final int quantity = cursor.getInt(cursor.getColumnIndexOrThrow(PartEntry.COLUMN_QUANTITY));
        String supplierMail = cursor.getString(cursor.getColumnIndexOrThrow(PartEntry.COLUMN_SUPPLIER_MAIL));
        byte[] imageResource = cursor.getBlob(cursor.getColumnIndexOrThrow(PartEntry.COLUMN_IMAGE));
        final Uri uri = ContentUris.withAppendedId(PartEntry.CONTENT_URI, cursor.getInt(cursor.getColumnIndexOrThrow(PartEntry._ID)));

        // Populate TextViews with values extracted from Cursor object
        partName.setText(name);
        partPrice.setText(price + " €");
        partQuantity.setText(quantity + " pieces");
        partSupplierMail.setText(supplierMail);
        Bitmap bmp = BitmapFactory.decodeByteArray(imageResource, 0, imageResource.length);
        partImage.setImageBitmap(bmp);

        // Find sale Button
        Button saleButton = (Button) view.findViewById(R.id.ButtonSale);
        // Set Button click listener
        saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check if quantity in stock is higher than zero
                if (quantity > 0) {
                    // Assign a new quantity value of minus one to represent one item sold
                    int newQuantity = quantity - 1;
                    // Create and initialise a new ContentValue object with the new quantity
                    ContentValues values = new ContentValues();
                    values.put(PartEntry.COLUMN_QUANTITY, newQuantity);
                    // Update the database
                    context.getContentResolver().update(uri, values, null, null);
                } else {
                    // Inform the user that quantity is zero and can't be updated
                    Toast.makeText(context, R.string.No_available_parts, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}

