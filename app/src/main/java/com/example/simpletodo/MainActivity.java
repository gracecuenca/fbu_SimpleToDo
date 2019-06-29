package com.example.simpletodo;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;

// activity is basically just a screen in the app
// service has no UI (no onCreate() to set a content view) ex. playing music in the background, etc.
// AppCompatActivity extension allows us to not worry about compatibility every time new versions come out
// an Activity extends (is-a) context which is why you can pass it in when a context is demanded
public class MainActivity extends AppCompatActivity {

    // a numeric code to identify the edit activity
    public static final int EDIT_REQUEST_CODE = 20;

    // keys used for passing data between activities
    public static final String ITEM_TEXT = "itemText";
    public static final String ITEM_POSITION = "itemPosition";

    // static are usually only used for primitives, not for complex objects like ListViews
    // if you see this it's probably fishy o.o, technically created a memory leak

    // instance variables
    ArrayList<String> items; // contains the actual items
    ArrayAdapter<String> itemsAdapter; // is an adapter, translating between the data and the views
    ListView lvItems; // the list view

    @Override // this annotation generates code or gives IDE hints
    // by convention, you typically do all setup in onCreate method
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // R is short for resources

        readItems();
        itemsAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,items);
        lvItems = (ListView) findViewById(R.id.lvitems);
        lvItems.setAdapter(itemsAdapter);

        setupListViewListener();
    }

    public void onAddItem(View v){
        EditText etNewItem = (EditText) findViewById(R.id.editText);
        String itemText = etNewItem.getText().toString();
        itemsAdapter.add(itemText);
        etNewItem.setText("");
        writeItems();
        Toast.makeText(getApplicationContext(), "Item added to list.", Toast.LENGTH_SHORT).show();
    }

    private void setupListViewListener(){
        Log.i("MainActivity", "Setting up listener on list view.");
        lvItems.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Log.i("MainActivity", "Item "+position + " removed from list");
                items.remove(position);
                itemsAdapter.notifyDataSetChanged();
                writeItems();
                return true;
            }
        });

        // setup item listener for edit (regular click)
        lvItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // create the new activity
                Intent i = new Intent(MainActivity.this, EditItemActivity.class);
                // pass the data being edited
                i.putExtra(ITEM_TEXT, items.get(position));
                i.putExtra(ITEM_POSITION, position);
                // display the activity
                startActivityForResult(i, EDIT_REQUEST_CODE);
            }
        });
    }

    // handle results from edit activity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // if the edit activity completed ok
        if(resultCode == RESULT_OK && requestCode == EDIT_REQUEST_CODE){
            // extract the updated item text from result intent extras
            String updatedItem = data.getExtras().getString(ITEM_TEXT);
            // extract original position
            int position = data.getExtras().getInt(ITEM_POSITION);
            // update model with the new item text at the edited position
            items.set(position, updatedItem);
            // notify the adapter that the model changed
            itemsAdapter.notifyDataSetChanged();
            // persist the changed model
            writeItems();
            // notify the user the operation completed ok
            Toast.makeText(this, "Item updated successfully", Toast.LENGTH_SHORT).show();
        }
    }

    private File getDataFile(){
        return new File(getFilesDir(), "todo.txt");
    }

    private void readItems(){
        try {
            items = new ArrayList<>(FileUtils.readLines(getDataFile(), Charset.defaultCharset()));
        } catch (IOException e) {
            Log.e("MainActivity", "Error reading file.", e);
            items = new ArrayList<>();
        }
    }

    private void writeItems(){
        try {
            FileUtils.writeLines(getDataFile(), items);
        } catch (IOException e) {
            Log.e("MainActivity", "Error writing file.", e);
        }
    }
}
