package com.example.android.shop;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NavUtils;
import android.support.v4.app.ShareCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.android.shop.data.ProductContract.ShopEntry;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import static com.example.android.shop.data.ProductProvider.LOG_TAG;

// Created by Daria Kalashnikova 11.07.2017

public class EditorActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {

    private static final int PICK_IMAGE_REQUEST = 0;
    private static final int EXISTING_LOADER = 0;
    private static final int SEND_MAIL_REQUEST = 0;
    Button toIncrease;
    Button toDecrease;
    private int quantity;
    private Uri mCurrentUri;
    private Uri mUri;
    private ImageView mImage;
    private EditText mNameEditText;
    private EditText mPriceEditText;
    private EditText mQuantityEditText;
    private EditText mEmail;
    private Button toOrder;
    // Boolean flag that keeps track of whether the product has been edited or not
    private boolean mProductHasChanged = false;

    // OnClickListener for three buttons in the Activity
    private View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.increase:
                    if (mQuantityEditText.getText().toString().equals("")) {
                        toIncrease.setEnabled(false);
                        Toast.makeText(EditorActivity.this, getResources().getString(R.string.msg_write_the_quantity),
                                Toast.LENGTH_SHORT).show();
                    } else
                        increaseQuantity();
                    break;
                case R.id.decrease:
                    if (mQuantityEditText.getText().toString().equals("")) {
                        toIncrease.setEnabled(false);
                        Toast.makeText(EditorActivity.this, getResources().getString(R.string.msg_write_the_quantity),
                                Toast.LENGTH_SHORT).show();
                    } else
                        decreaseQuantity();
                    break;
                case R.id.order_now:
                    orderTheProduct();
                    break;
                default:
                    break;
            }
        }
    };

    // OnTouchListener that listens for any user touches on a View, implying that they are modifying
    // the view, and we change the mProductHasChanged boolean to true
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mProductHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        // Examine the intent that was used to launch this activity,
        // in order to figure out if we're creating a new product or editing an existing one
        Intent intent = getIntent();
        mCurrentUri = intent.getData();
        // Find all relevant views that we will need to read user input from
        mImage = (ImageView) findViewById(R.id.image);
        mNameEditText = (EditText) findViewById(R.id.edit_name);
        mPriceEditText = (EditText) findViewById(R.id.edit_price);
        mQuantityEditText = (EditText) findViewById(R.id.edit_quantity);
        mEmail = (EditText) findViewById(R.id.message);
        toIncrease = (Button) findViewById(R.id.increase);
        toDecrease = (Button) findViewById(R.id.decrease);
        toOrder = (Button) findViewById(R.id.order_now);
        // If the intent DOES NOT contain a product content URI, then we know that we are
        // creating a new product
        if (mCurrentUri == null) {
            setTitle(getString(R.string.editor_activity_title_new_product));
            // Invalidate the options menu, so the "Delete" menu option can be hidden
            // (It doesn't make sense to delete a product that hasn't been created yet)
            invalidateOptionsMenu();
        } else {
            // Otherwise this is an existing product, so change app bar to say "Edit a Product"
            setTitle(getString(R.string.editor_activity_title_edit_product));
            getLoaderManager().initLoader(EXISTING_LOADER, null, this);
        }

        // To hide the keyboard on the activity start
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the editor without saving
        mNameEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mEmail.setOnTouchListener(mTouchListener);
        // Setup OnClickListeners on all buttons in the Activity
        toDecrease.setOnClickListener(mClickListener);
        toIncrease.setOnClickListener(mClickListener);
        toOrder.setOnClickListener(mClickListener);

        // Picture selector
        mImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openImageSelector();
            }
        });
    }

    private void increaseQuantity() {
        quantity = Integer.parseInt(mQuantityEditText.getText().toString().trim());
        int maximumIncreaseQuantity = 1000;
        if (quantity < maximumIncreaseQuantity) {
            mProductHasChanged = true;
            quantity++;
            mQuantityEditText.setText(String.valueOf(quantity));
        }
    }

    private void decreaseQuantity() {
        quantity = Integer.parseInt(mQuantityEditText.getText().toString().trim());
        if (quantity > 0) {
            mProductHasChanged = true;
            quantity -= 1;
            mQuantityEditText.setText(String.valueOf(quantity));
        }
    }

    private void orderTheProduct() {
        if (mCurrentUri != null) {
            String subject = getString(R.string.mail_subject);
            String stream = (mEmail.getText().toString() + "\n");
            String recipient = getString(R.string.mail_email);

            Intent shareIntent = ShareCompat.IntentBuilder.from(this)
                    .setStream(mUri)
                    .setSubject(subject)
                    .setText(stream)
                    .setEmailTo(new String[]{recipient})
                    .getIntent();

            // Provide read access
            shareIntent.setData(mCurrentUri);
            shareIntent.setType("message/rfc822");
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            if (Build.VERSION.SDK_INT < 21) {
                shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            } else {
                shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
            }

            startActivityForResult(Intent.createChooser(shareIntent, "Share with"), SEND_MAIL_REQUEST);

        } else {
            Snackbar.make(toOrder, R.string.snak_bar_msg, Snackbar.LENGTH_LONG)
                    .setAction("Select",
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    openImageSelector();
                                }
                            }).show();
        }
    }

    public void openImageSelector() {
        Intent intent;

        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
        } else {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        }

        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, getString(R.string.image_selector)), PICK_IMAGE_REQUEST);
    }

    @Override
    // After Picture selector
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        // The ACTION_OPEN_DOCUMENT intent was sent with the request code READ_REQUEST_CODE = 0;
        // If the request code seen here doesn't match, it's the response to some other intent,
        // and the below code shouldn't run at all
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter. Pull that uri using "resultData.getData()"
            if (resultData != null) {
                mUri = resultData.getData();
                Log.v(LOG_TAG, "Uri: " + mUri.toString());
                mImage.setImageBitmap(getBitmapFromUri(mUri));
            }
        }
    }

    public Bitmap getBitmapFromUri(Uri uri) {

        if (uri == null || uri.toString().isEmpty())
            return null;

        // Get the dimensions of the View
        int targetW = mImage.getWidth();
        int targetH = mImage.getHeight();

        InputStream input = null;
        try {
            input = this.getContentResolver().openInputStream(uri);

            // Get the dimensions of the bitmap
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(input, null, bmOptions);
            assert input != null;
            input.close();

            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;

            // Determine how much to scale down the image
            int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

            // Decode the image file into a Bitmap sized to fill the View
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;
            bmOptions.inPurgeable = true;

            input = this.getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(input, null, bmOptions);
            assert input != null;
            input.close();
            return bitmap;

        } catch (FileNotFoundException fne) {
            Log.e(LOG_TAG, "Failed to load image.", fne);
            return null;
        } catch (Exception e) {
            Log.e(LOG_TAG, "Failed to load image.", e);
            return null;
        } finally {
            try {
                assert input != null;
                input.close();
            } catch (IOException ignored) {
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file
        // This adds menu items to the app bar
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    /**
     * This method is called after invalidateOptionsMenu(), so that the
     * menu can be updated (some menu items can be hidden or made visible)
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new product, hide the "Delete" menu item
        if (mCurrentUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save product to database
                saveProduct();
                // Exit activity
                finish();
                return true;
            case R.id.action_order:
                // Send an e-mail
                orderTheProduct();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the product hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}
                if (!mProductHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user
                // Create a click listener to handle the user confirming that
                // changes should be discarded
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This method is called when the back button is pressed
     */
    @Override
    public void onBackPressed() {
        // If the product hasn't changed, continue with handling back button press
        if (!mProductHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user
        // Create a click listener to handle the user confirming that changes should be discarded
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    private void saveProduct() {
        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String nameString = mNameEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String quantityString = mQuantityEditText.getText().toString().trim();
        String emailString = mEmail.getText().toString().trim();
        // Check if this is supposed to be a new product
        // and check if all the fields in the editor are blank
        if (mCurrentUri == null && TextUtils.isEmpty(nameString) && TextUtils.isEmpty(priceString)
                && TextUtils.isEmpty(quantityString) && TextUtils.isEmpty(emailString)) {
            // Since no fields were modified, we can return early without creating a new product
            // No need to create ContentValues and no need to do any ContentProvider operations
            return;
        }
        // Create a ContentValues object where column names are the keys,
        // and product attributes from the editor are the values
        ContentValues values = new ContentValues();

        if (!TextUtils.isEmpty(nameString)) {
            values.put(ShopEntry.COLUMN_PRODUCT_NAME, nameString);
        } else {
            Toast.makeText(this, getResources().getString(R.string.error_msg_name_required), Toast.LENGTH_SHORT).show();
            return;
        }
        int price;
        if (!TextUtils.isEmpty(priceString)) {
            price = Integer.parseInt(priceString);
            values.put(ShopEntry.COLUMN_PRODUCT_PRICE, price);
        } else {
            Toast.makeText(this, getResources().getString(R.string.error_msg_price_required), Toast.LENGTH_SHORT).show();
            return;
        }
        int quantity;
        if (!TextUtils.isEmpty(quantityString)) {
            quantity = Integer.parseInt(quantityString);
            values.put(ShopEntry.COLUMN_PRODUCT_QUANTITY, quantity);
        } else {
            Toast.makeText(this, getResources().getString(R.string.error_msg_quantity_required), Toast.LENGTH_SHORT).show();
            return;
        }

        if (!TextUtils.isEmpty(emailString)) {
            values.put(ShopEntry.COLUMN_PRODUCT_EMAIL, emailString);
        } else {
            Toast.makeText(this, getResources().getString(R.string.error_msg_email_required), Toast.LENGTH_SHORT).show();
            return;
        }

        if (mUri != null) {
            values.put(ShopEntry.COLUMN_PRODUCT_IMAGE, mUri.toString());
        } else {
            Toast.makeText(this, getResources().getString(R.string.error_msg_picture_required), Toast.LENGTH_SHORT).show();
            return;
        }

        // Determine if this is a new or existing product by checking if mCurrentUri is null or not
        if (mCurrentUri == null) {
            // This is a NEW product, so insert a new product into the provider,
            // returning the content URI for the new product
            Uri newUri = getContentResolver().insert(ShopEntry.CONTENT_URI, values);
            // Show a toast message depending on whether or not the insertion was successful
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion
                Toast.makeText(this, getString(R.string.insert_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast
                Toast.makeText(this, getString(R.string.insert_product_success),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            // Otherwise this is an EXISTING product, so update the product with content URI: mCurrentUri
            // and pass in the new ContentValues. Pass in null for the selection and selection args
            // because mCurrentUri will already identify the correct row in the database that
            // we want to modify
            int rowsAffected = getContentResolver().update(mCurrentUri, values, null, null);

            // Show a toast message depending on whether or not the update was successful
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update
                Toast.makeText(this, getString(R.string.update_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast
                Toast.makeText(this, getString(R.string.update_product_success),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Since the editor shows all product attributes, define a projection that contains
        // all columns from the shop table
        String[] projection = {
                ShopEntry._ID,
                ShopEntry.COLUMN_PRODUCT_IMAGE,
                ShopEntry.COLUMN_PRODUCT_NAME,
                ShopEntry.COLUMN_PRODUCT_PRICE,
                ShopEntry.COLUMN_PRODUCT_QUANTITY,
                ShopEntry.COLUMN_PRODUCT_EMAIL};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                mCurrentUri,            // Query the content URI for the current product
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }
        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of product attributes that we're interested in
            int imageColumnIndex = cursor.getColumnIndex(ShopEntry.COLUMN_PRODUCT_IMAGE);
            int nameColumnIndex = cursor.getColumnIndex(ShopEntry.COLUMN_PRODUCT_NAME);
            int priceColumnIndex = cursor.getColumnIndex(ShopEntry.COLUMN_PRODUCT_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(ShopEntry.COLUMN_PRODUCT_QUANTITY);
            int emailColumnIndex = cursor.getColumnIndex(ShopEntry.COLUMN_PRODUCT_EMAIL);

            // Extract out the value from the Cursor for the given column index
            String image = cursor.getString(imageColumnIndex);
            String name = cursor.getString(nameColumnIndex);
            String email = cursor.getString(emailColumnIndex);

            final Uri mImageUri = Uri.parse(image);
            int price = cursor.getInt(priceColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);

            mUri = mImageUri;
            mImage.setImageBitmap(getBitmapFromUri(mImageUri));
            mNameEditText.setText(name);
            mPriceEditText.setText(Integer.toString(price));
            mQuantityEditText.setText(Integer.toString(quantity));
            mEmail.setText(email);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields
        mNameEditText.setText("");
        mPriceEditText.setText("");
        mQuantityEditText.setText("");
        mEmail.setText("");
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the product.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    // Prompt the user to confirm that they want to delete this product
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the product
                deleteProduct();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the product
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    // Perform the deletion of the product in the database
    private void deleteProduct() {
        // Only perform the delete if this is an existing product
        if (mCurrentUri != null) {
            // Call the ContentResolver to delete the product at the given content URI
            // Pass in null for the selection and selection args because the mCurrentUri
            // content URI already identifies the product that we want
            int rowsDeleted = getContentResolver().delete(mCurrentUri, null, null);

            // Show a toast message depending on whether or not the delete was successful
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete
                Toast.makeText(this, getString(R.string.delete_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast
                Toast.makeText(this, getString(R.string.delete_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }

        // Close the activity
        finish();
    }
}
