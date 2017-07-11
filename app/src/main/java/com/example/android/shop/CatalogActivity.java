package com.example.android.shop;

import android.app.LoaderManager.LoaderCallbacks;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.android.shop.data.ProductContract.ShopEntry;

// Created by Daria Kalashnikova 11.07.2017

public class CatalogActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {

    private static final int PRODUCT_LOADER = 0;
    ProductAdapter mCursorAdapter;
    ListView productList;
    View emptyView;
    Intent intent;
    private Uri mCurrentUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        // Find the ListView which will be populated with the product data
        productList = (ListView) findViewById(R.id.list_products);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items
        emptyView = findViewById(R.id.empty_view);
        productList.setEmptyView(emptyView);

        // Setup an Adapter to create a list item for each row of product data in the Cursor
        mCursorAdapter = new ProductAdapter(this, null);
        productList.setAdapter(mCursorAdapter);

        productList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                intent = new Intent(CatalogActivity.this, EditorActivity.class);
                mCurrentUri = ContentUris.withAppendedId(ShopEntry.CONTENT_URI, id);
                intent.setData(mCurrentUri);
                startActivity(intent);
            }
        });

        // Kick off the loader
        getLoaderManager().initLoader(PRODUCT_LOADER, null, this);
    }

    private void deleteAllProducts() {
        int rowsDeleted = getContentResolver().delete(ShopEntry.CONTENT_URI, null, null);
        Log.v("CatalogActivity", rowsDeleted + " rows deleted from shop database");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                deleteAllProducts();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Define a projection that specifies the columns from the table we care about
        String[] projection = {
                ShopEntry._ID,
                ShopEntry.COLUMN_PRODUCT_IMAGE,
                ShopEntry.COLUMN_PRODUCT_NAME,
                ShopEntry.COLUMN_PRODUCT_PRICE,
                ShopEntry.COLUMN_PRODUCT_QUANTITY,
                ShopEntry.COLUMN_PRODUCT_EMAIL};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                ShopEntry.CONTENT_URI,  // Provider content URI to query
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Update {@link ProductAdapter} with this new cursor containing updated product data
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Callback called when the data needs to be deleted
        mCursorAdapter.swapCursor(null);
    }
}
