package com.example.android.autopartsinventory;

import android.Manifest;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.autopartsinventory.data.PartContract.PartEntry;

import java.io.ByteArrayOutputStream;

import static android.R.attr.data;

public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * EditText field to enter part name
     */
    private EditText mNameEditText;

    /**
     * EditText field to enter product price
     */
    private EditText mPriceEditText;

    /**
     * TextView to show current part quantity
     */
    private TextView mQuantityTextViewDetail;

    /**
     * EditText to show supplier's email
     */
    private EditText mSupplierMailEditText;

    /**
     * Product information variables
     */
    private String mPartName;
    private int mPartQuantity = 0;

    /**
     * ImageViews that will be used to modify quantity
     */
    private ImageView mIncreaseQuantity;    // Increase by one
    private ImageView mDecreaseQuantity;    // Decrease by one


    /**
     * Final for the image intent request code
     */
    private final static int SELECT_PHOTO = 200;

    /**
     * Constant to be used when asking for storage read
     */
    private final static int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 777;

    /**
     * Button to select image, ImageView to display selected image
     * and Bitmap to store/retrieve from the database
     */
    private Button mSelectImageButton;
    private ImageView mPartImageView;
    private Bitmap mPartBitmap;

    /**
     * Button to order more quantity from supplier
     */
    private Button mOrderButton;

    /**
     * Constant field for email intent
     */

    /**
     * Uri loader
     */
    private static final int URI_LOADER = 0;

    /**
     * Uri received with the Intent from {@link CatalogActivity}
     */
    private Uri mPartUri;

    /**
     * Boolean to check whether or not the register has changed
     */
    private boolean mPartHasChanged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // Receive Uri data from intent
        Intent intent = getIntent();
        mPartUri = intent.getData();

        // Check if Uri is null or not
        if (mPartUri != null) {
            // If not null means that a product register will be edited
            setTitle(R.string.Detail_title_Edit_part);
            // Kick off LoaderManager
            getLoaderManager().initLoader(URI_LOADER, null, this);
        } else {
            // If null means that a new product register will be created
            setTitle(R.string.Detail_title_New_part);
            // Invalidate options menu (delete button) since there's no record
            invalidateOptionsMenu();
        }

        // Find all relevant views that we will need to read or show user input
        initialiseViews();

        // Set on touch listener to all relevant views
        setOnTouchListener();
    }

    private void initialiseViews() {
        // Check if it's an existing partt to make the button visible so
        // the user can order more from existing product
        if (mPartUri != null) {
            // Initialise Button to order more from supplier
            mOrderButton = (Button) findViewById(R.id.orberButton);
            // Make button visible
            mOrderButton.setVisibility(View.VISIBLE);
            mOrderButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //open email client to send mail to supplier
                    Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                    emailIntent.setData(Uri.parse("mailto:" + mSupplierMailEditText.getText().toString()));
                    emailIntent.putExtra(Intent.EXTRA_SUBJECT, mPartName + " order");
                    emailIntent.putExtra(Intent.EXTRA_TEXT, "I would like to order");
                    startActivity(Intent.createChooser(emailIntent, "Send feedback"));

                }
            });
        }

        // Initialise EditTexts
        mNameEditText = (EditText) findViewById(R.id.name_editText);
        mQuantityTextViewDetail = (TextView) findViewById(R.id.quantity_TextView_detail);
        mPriceEditText = (EditText) findViewById(R.id.price_editText);
        mSupplierMailEditText = (EditText) findViewById(R.id.supplier_editText);

        // Initialise increase Button and set click listener
        mIncreaseQuantity = (ImageView) findViewById(R.id.increaseQuantity);
        mIncreaseQuantity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Add +1 to product quantity
                mPartQuantity++;
                // Update UI
                mQuantityTextViewDetail.setText(String.valueOf(mPartQuantity));
            }
        });

        // Initialise decrease Button and set click listener
        mDecreaseQuantity = (ImageView) findViewById(R.id.decreaseQuantity);
        mDecreaseQuantity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Decrease 1 to product quantity if higher than 0
                if (mPartQuantity > 0) {
                    mPartQuantity--;
                    // Update UI
                    mQuantityTextViewDetail.setText(String.valueOf(mPartQuantity));
                } else {
                    Toast.makeText(DetailActivity.this, getString(R.string.Invalid_quantity), Toast.LENGTH_SHORT).show();
                }
            }
        });


        // Initialise the image view to show preview of the product image
        mPartImageView = (ImageView) findViewById(R.id.part_ImageView);

        // Initialise button to select the product image
        mSelectImageButton = (Button) findViewById(R.id.SelectImageButton);
        mSelectImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Ask for user permission to explore image gallery
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        // If not authorized, ask for authorization
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                                // Do something
                            }
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                        }
                        return;
                    }
                    // If permission granted, create a new intent and prompt
                    // user to pick image from Gallery
                    Intent getIntent = new Intent(Intent.ACTION_PICK);
                    getIntent.setType("image/*");
                    startActivityForResult(getIntent, SELECT_PHOTO);
                }


            }
        });
    }


    /**
     * Handle the result of the image chooser intent launch
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Check if request code, result and intent match the image chooser
        if (requestCode == SELECT_PHOTO && resultCode == RESULT_OK && data != null) {
            // Get image Uri
            Uri selectedImage = data.getData();
            // Get image file path
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            // Create cursor object and query image
            Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            // Get image path from cursor
            String picturePath = cursor.getString(columnIndex);
            // Close cursor to avoid memory leak
            cursor.close();
            // Set the image to a Bitmap object
            mPartBitmap = BitmapFactory.decodeFile(picturePath);
            // Set Bitmap to the image view
            mPartImageView.setImageBitmap(mPartBitmap);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_main_activity.xml file.
        // This adds the given menu to the app bar.
        getMenuInflater().inflate(R.menu.menu_detail_activity, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new product, hide the "Delete" menu item.
        if (mPartUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Add" menu option
            case R.id.action_add:
                if (mPartHasChanged) {
                    // Call save/edit method
                    saveProduct();
                } else {
                    // Show toast when no product is updated nor created
                    Toast.makeText(this, getString(R.string.Insert_update_failure), Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.id.action_delete:
                // Call delete confirmation dialog
                showDeleteConfirmationDialog();
                return true;
            case android.R.id.home:
                // If part hasn't changed, continue with navigating up to parent activity
                if (!mPartHasChanged) {
                    NavUtils.navigateUpFromSameTask(DetailActivity.this);
                    return true;
                } else {
                    // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                    // Create a click listener to handle the user confirming that
                    // changes should be discarded.
                    DialogInterface.OnClickListener discardButtonClickListener =
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    // User clicked "Discard" button, navigate to parent activity
                                    NavUtils.navigateUpFromSameTask(DetailActivity.this);
                                }
                            };

                    // Show a dialog that notifies the user they have unsaved changes
                    showUnsavedChangesDialog(discardButtonClickListener);
                    return true;
                }
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Handle the back button press on the device
     */
    @Override
    public void onBackPressed() {
        // If the product hasn't changed, continue with closing and back to parent activity
        if (!mPartHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user
        // Create a click listener to handle the user confirming that changes should be discarded
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Close the current activity without adding/saving
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    /**
     * Add new part or commit changes to existing register being edited
     */
    private void saveProduct() {
        // Define whether or not EditText fields are empty
        boolean nameIsEmpty = checkFieldEmpty(mNameEditText.getText().toString().trim());
        boolean priceIsEmpty = checkFieldEmpty(mPriceEditText.getText().toString().trim());
        boolean SupplierMailIsEmpty = checkFieldEmpty(mSupplierMailEditText.getText().toString().trim());

        // Check if Name, Quantity, Price, mail, image are null/zero and inform the user to change to a valid value
        if (nameIsEmpty) {
            Toast.makeText(this, getString(R.string.Invalid_name), Toast.LENGTH_SHORT).show();
        } else if (mPartQuantity <= 0) {
            Toast.makeText(this, getString(R.string.Invalid_quantity), Toast.LENGTH_SHORT).show();
        } else if (priceIsEmpty) {
            Toast.makeText(this, getString(R.string.Invalid_price), Toast.LENGTH_SHORT).show();
        } else if (SupplierMailIsEmpty) {
            Toast.makeText(this, getString(R.string.Invalid_SupplierMail), Toast.LENGTH_SHORT).show();
        } else if (mPartBitmap == null) {
            Toast.makeText(this, getString(R.string.Invalid_image), Toast.LENGTH_SHORT).show();
        } else {
            // Assuming that all fields are valid, pass the edit text
            // value to a String for easier manipulation
            String name = mNameEditText.getText().toString().trim();
            String supplierMail = mSupplierMailEditText.getText().toString().trim();
            // Pass the edit text value to a double for easier manipulation
            double price = Double.parseDouble(mPriceEditText.getText().toString().trim());

            // Create new ContentValues and put the part info into it
            ContentValues values = new ContentValues();
            values.put(PartEntry.COLUMN_NAME, name);
            values.put(PartEntry.COLUMN_QUANTITY, mPartQuantity);
            values.put(PartEntry.COLUMN_PRICE, price);
            values.put(PartEntry.COLUMN_SUPPLIER_MAIL, supplierMail);
            byte[] image = getBytes(mPartBitmap);
            values.put(PartEntry.COLUMN_IMAGE, image);

            // Check if Uri is valid to determine whether is new product insertion or existing product update
            if (mPartUri == null) {
                // If Uri is null then we're inserting a new product
                Uri newUri = getContentResolver().insert(PartEntry.CONTENT_URI, values);
                // Inform user of the successful product insertion
                Toast.makeText(this, getString(R.string.Successful_addition),
                        Toast.LENGTH_SHORT).show();
            } else {
                // If Uri is not null then we're updating an existing product
                int newUri = getContentResolver().update(mPartUri, values, null, null);
                // Inform user of the successful product update
                Toast.makeText(this, getString(R.string.Successful_update),
                        Toast.LENGTH_SHORT).show();
            }
            finish();
        }
    }

    /**
     * Convert from bitmap to byte array
     *
     * @param bitmap: Data retrieved from the user galery that will be
     *                converted to byte[] in order to store in database BLOB
     */
    public static byte[] getBytes(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);
        return stream.toByteArray();
    }

    /**
     * Convert from byte array to bitmap
     *
     * @param image: BLOB from the database converted to a Bitmap
     *               in order to display in the UI
     */
    public static Bitmap getImage(byte[] image) {
        return BitmapFactory.decodeByteArray(image, 0, image.length);
    }

    /**
     * Method to define if any of the EditText fields are empty or contain invalid inputs
     *
     * @param string: String received as a parameter to be checked with this method
     */
    private boolean checkFieldEmpty(String string) {
        return TextUtils.isEmpty(string) || string.equals(".");
    }

    /**
     * Perform the deletion of the part record in the database.
     */
    private void deleteProduct() {
        if (mPartUri != null) {
            int rowsDeleted = getContentResolver().delete(mPartUri, null, null);
            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.Unsuccessful_deletion),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.Successful_deletion),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Ask for user confirmation before deleting part from database
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.before_deletion));
        builder.setPositiveButton(getString(R.string.SayYes), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Call deleteProduct method, so delete the part register from database.
                deleteProduct();
                finish();
            }
        });
        builder.setNegativeButton(getString(R.string.SayNo), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Dismiss the dialog and continue editing the product record.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Ask for user confirmation to exit activity before saving
     */
    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.leaveWithoutSave));
        builder.setPositiveButton(getString(R.string.SayYes), discardButtonClickListener);
        builder.setNegativeButton(getString(R.string.SayNo), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the product register
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Set touch listeners to the UI
     */
    private void setOnTouchListener() {
        mNameEditText.setOnTouchListener(mTouchListener);
        mQuantityTextViewDetail.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mSupplierMailEditText.setOnTouchListener(mTouchListener);
        mIncreaseQuantity.setOnTouchListener(mTouchListener);
        mDecreaseQuantity.setOnTouchListener(mTouchListener);
        mSelectImageButton.setOnTouchListener(mTouchListener);
    }

    /**
     * Set onTouchListener on the UI and changes the boolean value to TRUE in order to indicate
     * that the user is changing the current product register
     */
    View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            mPartHasChanged = true;
            return false;
        }
    };

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case URI_LOADER:
                return new CursorLoader(
                        this,
                        mPartUri,
                        null,
                        null,
                        null,
                        null
                );
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data.moveToFirst()) {
            mPartName = data.getString(data.getColumnIndex(PartEntry.COLUMN_NAME));
            mNameEditText.setText(mPartName);
            mPriceEditText.setText(data.getString(data.getColumnIndex(PartEntry.COLUMN_PRICE)));
            mPartQuantity = data.getInt(data.getColumnIndex(PartEntry.COLUMN_QUANTITY));
            mQuantityTextViewDetail.setText(String.valueOf(mPartQuantity));
            mSupplierMailEditText.setText(data.getString(data.getColumnIndex(PartEntry.COLUMN_SUPPLIER_MAIL)));
            if (data.getBlob(data.getColumnIndex(PartEntry.COLUMN_IMAGE)) != null) {
                mPartImageView.setImageBitmap(getImage(data.getBlob(data.getColumnIndex(PartEntry.COLUMN_IMAGE))));
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mNameEditText.getText().clear();
        mQuantityTextViewDetail.setText("0");
        mSupplierMailEditText.setText(" ");
    }
}

