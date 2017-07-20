package com.example.android.autopartsinventory;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.android.autopartsinventory.data.PartContract.PartEntry;

public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private PartCursorAdapter mPartCursorAdapter;
    private static final int URI_LOADER = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        // Start LoaderManager
        getLoaderManager().initLoader(URI_LOADER, null, this);

        // Start ListView to show data on the UI (if available)
        initialiseListView();

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case URI_LOADER:
                // Define projection for the Cursor so that contains all rows from the pets table.
                String projection[] = {
                        PartEntry._ID,
                        PartEntry.COLUMN_NAME,
                        PartEntry.COLUMN_PRICE,
                        PartEntry.COLUMN_QUANTITY,
                        PartEntry.COLUMN_SUPPLIER_MAIL,
                        PartEntry.COLUMN_IMAGE
                };
                // Define sort order
                String sortOrder =
                        PartEntry._ID + " DESC";
                // Return cursor loader
                return new CursorLoader(
                        this,
                        PartEntry.CONTENT_URI,
                        projection,
                        null,
                        null,
                        sortOrder
                );
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        try {
            mPartCursorAdapter.swapCursor(data);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mPartCursorAdapter.swapCursor(null);
    }

    private void initialiseListView() {
        // Find list view from the layout file
        ListView listView = (ListView) findViewById(R.id.list_view);
        // Define empty view so a specific layout can be displayed when
        // there's no data to be shown in the UI
        View emptyView = findViewById(R.id.empty_view);
        // Attach the empty view to the list view when there's no data to show
        listView.setEmptyView(emptyView);
        // Initialise cursor adapter
        mPartCursorAdapter = new PartCursorAdapter(this, null, false);
        // Attach cursor adapter to the list view
        listView.setAdapter(mPartCursorAdapter);
        // Set click listener to the listView
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(CatalogActivity.this, DetailActivity.class);
                intent.setData(ContentUris.withAppendedId(PartEntry.CONTENT_URI, id));
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_main_activity.xml file.
        // This adds the given menu to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Add" menu option
            case R.id.action_add:
                Intent intent = new Intent(CatalogActivity.this, DetailActivity.class);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
