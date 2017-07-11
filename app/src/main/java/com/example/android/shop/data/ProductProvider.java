package com.example.android.shop.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.android.shop.R;

import static com.example.android.shop.data.ProductContract.ShopEntry;

// Created by Daria Kalashnikova 11.07.2017

public class ProductProvider extends ContentProvider {

    // Tag for the log messages
    public static final String LOG_TAG = ProductProvider.class.getSimpleName();
    // URI matcher code for the content URI for the products/(shop) table
    private static final int PRODUCT = 1;
    // URI matcher code for the content URI for a single product in the products/(shop) table
    private static final int PRODUCT_ID = 2;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(ProductContract.CONTENT_AUTHORITY, ProductContract.PATH_SHOP, PRODUCT);
        sUriMatcher.addURI(ProductContract.CONTENT_AUTHORITY, ProductContract.PATH_SHOP + "/#", PRODUCT_ID);
    }

    // Database helper object
    private DbHelper mDbHelper;
    private SQLiteDatabase database;

    @Override
    public boolean onCreate() {
        mDbHelper = new DbHelper(getContext());
        return false;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Get readable database
        database = mDbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCT:
                cursor = database.query(ProductContract.ShopEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case PRODUCT_ID:
                selection = ProductContract.ShopEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(ShopEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        // Set notification URI on the Cursor
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        // Return the cursor
        return cursor;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCT:
                return ShopEntry.CONTENT_LIST_TYPE;
            case PRODUCT_ID:
                return ShopEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCT:
                return insertProduct(uri, values);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    private Uri insertProduct(Uri uri, ContentValues values) {
        // Check that the image uri is not null
        String image = values.getAsString(ShopEntry.COLUMN_PRODUCT_IMAGE);
        if (image == null) {
            throw new IllegalArgumentException(String.valueOf(R.string.error_msg_picture_required));
        }
        // Check that the name is not null
        String name = values.getAsString(ShopEntry.COLUMN_PRODUCT_NAME);
        if (name == null) {
            throw new IllegalArgumentException(String.valueOf(R.string.error_msg_name_required));
        }
        // If the price is provided, check that it's greater than or equal to 0 USD
        Integer price = values.getAsInteger(ShopEntry.COLUMN_PRODUCT_PRICE);
        if (price != null && price < 0) {
            throw new IllegalArgumentException(String.valueOf(R.string.error_msg_valid_price_required));
        }
        // If the quantity is provided, check that it's greater than or equal to 0
        Integer quantity = values.getAsInteger(ShopEntry.COLUMN_PRODUCT_QUANTITY);
        if (quantity != null && quantity < 0) {
            throw new IllegalArgumentException(String.valueOf(R.string.error_msg_valid_quantity_required));
        }
        // If the message is provided, check that it is not null
        String email = values.getAsString(ShopEntry.COLUMN_PRODUCT_EMAIL);
        if (email == null) {
            throw new IllegalArgumentException(String.valueOf(R.string.error_msg_email_required));
        }

        // Get writable database
        database = mDbHelper.getWritableDatabase();

        // Insert the new product with the given values
        long id = database.insert(ShopEntry.TABLE_NAME, null, values);
        // If the ID is -1, then the insertion failed. Log an error and return null
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        // Notify all listeners that the data has changed for the products content URI
        getContext().getContentResolver().notifyChange(uri, null);

        // Return the new URI with the ID (of the newly inserted row) appended at the end
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        // Get writable database
        database = mDbHelper.getWritableDatabase();

        // Track the number of rows that were deleted
        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCT:
                // Delete all rows that match the selection and selection args
                rowsDeleted = database.delete(ShopEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case PRODUCT_ID:
                // Delete a single row given by the ID in the URI
                selection = ShopEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(ShopEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        // If 1 or more rows were deleted, then notify all listeners that the data at the
        // given URI has changed
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows deleted
        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCT:
                // Update all rows that match the selection and selection args
                return updateProduct(uri, values, selection, selectionArgs);
            case PRODUCT_ID:
                // Update a single row given by the ID in the URI
                selection = ShopEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateProduct(uri, values, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    private int updateProduct(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        // Check that the image uri is not null
        if (values.containsKey(ShopEntry.COLUMN_PRODUCT_IMAGE)) {
            String image = values.getAsString(ShopEntry.COLUMN_PRODUCT_IMAGE);
            if (image == null) {
                throw new IllegalArgumentException(String.valueOf(R.string.error_msg_picture_required));
            }
        }
        // Check that the name value is not null
        if (values.containsKey(ShopEntry.COLUMN_PRODUCT_NAME)) {
            String name = values.getAsString(ShopEntry.COLUMN_PRODUCT_NAME);
            if (name == null) {
                throw new IllegalArgumentException(String.valueOf(R.string.error_msg_name_required));
            }
        }

        // Check that the price value is valid
        if (values.containsKey(ShopEntry.COLUMN_PRODUCT_PRICE)) {
            Integer price = values.getAsInteger(ShopEntry.COLUMN_PRODUCT_PRICE);
            if (price != null && price < 0) {
                throw new IllegalArgumentException(String.valueOf(R.string.error_msg_price_required));
            }
        }

        // Check that the quantity value is valid
        if (values.containsKey(ShopEntry.COLUMN_PRODUCT_QUANTITY)) {
            // Check that the weight is greater than or equal to 0 kg
            Integer quantity = values.getAsInteger(ShopEntry.COLUMN_PRODUCT_QUANTITY);
            if (quantity != null && quantity < 0) {
                throw new IllegalArgumentException(String.valueOf(R.string.error_msg_quantity_required));
            }
        }

        // Check that the e-mail uri is not null
        if (values.containsKey(ShopEntry.COLUMN_PRODUCT_EMAIL)) {
            String email = values.getAsString(ShopEntry.COLUMN_PRODUCT_EMAIL);
            if (email == null) {
                throw new IllegalArgumentException(String.valueOf(R.string.error_msg_email_required));
            }
        }

        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }

        // Otherwise, get writable database to update the data
        database = mDbHelper.getWritableDatabase();

        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update(ShopEntry.TABLE_NAME, values, selection, selectionArgs);

        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows updated
        return rowsUpdated;
    }
}
