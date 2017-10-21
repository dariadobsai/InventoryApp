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
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.android.shop.data.ProductContract.ShopEntry;

import java.util.HashMap;
import java.util.List;

// Created by Daria Kalashnikova 11.07.2017

public class CatalogActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {

    private static final int PRODUCT_LOADER = 0;
    ProductAdapter mCursorAdapter;
    ListView productList;
    View emptyView;
    Intent intent;
    private Uri mCurrentUri;

    private SelectionAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        mAdapter = new SelectionAdapter(this, R.layout.list_item, R.id.construction, productList);

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

        productList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);

        productList.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {

            private int itemsSelected = 0;

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                mAdapter.clearSelection();
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                itemsSelected = 0;
                MenuInflater inflater = getMenuInflater();
                inflater.inflate(R.menu.menu_catalog, menu);
                return true;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_delete_all_entries:
                        deleteAllProducts();
                        itemsSelected = 0;
                        mAdapter.clearSelection();
                        mode.finish();
                }
                return false;
            }

            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                if (checked) {
                    itemsSelected++;
                    mAdapter.setNewSelection(position, checked);
                } else {
                    itemsSelected--;
                    mAdapter.removeSelection(position);
                }
                mode.setTitle(itemsSelected + " selected");

            }
        });

        productList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                productList.setItemChecked(position, !mAdapter.isPositionChecked(position));
                return false;
            }
        });

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

    private class SelectionAdapter extends ArrayAdapter<ListView> {

        private HashMap<Integer, Boolean> mSelection = new HashMap<>();

        public SelectionAdapter(CatalogActivity act, int resource, int construction, ListView list) {
            super(act, resource, construction, (List<ListView>) list);
        }

        public void setNewSelection(int position, boolean value) {
            mSelection.put(position, value);
            mCursorAdapter.notifyDataSetChanged();
        }

        public boolean isPositionChecked(int position) {
            Boolean result = mSelection.get(position);
            return result == null ? false : result;
        }

//        public Set<Integer> getCurrentCheckedPosition() {
//            return mSelection.keySet();
//        }

        public void removeSelection(int position) {
            mSelection.remove(position);
            mCursorAdapter.notifyDataSetChanged();
        }

        public void clearSelection() {
            mSelection = new HashMap<>();
            mCursorAdapter.notifyDataSetChanged();
        }

//        @NonNull
//        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
//            View v = mAdapter.getView(position, convertView, parent);//let the adapter handle setting up the row views
//            v.setBackgroundColor(getResources().getColor(android.R.color.background_light)); //default color
//
//            if (mSelection.get(position) != null) {
//                v.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_light));// this is a selected position so make it red
//            }
//            return v;
//        }
    }
}
