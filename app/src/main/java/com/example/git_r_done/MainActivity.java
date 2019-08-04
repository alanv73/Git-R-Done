package com.example.git_r_done;

import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.text.InputType;
import android.text.TextUtils;
import android.text.format.Time;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.AdapterView;
import android.widget.SimpleCursorAdapter;

import android.widget.Toast;


public class MainActivity extends AppCompatActivity {

    Time today = new Time(Time.getCurrentTimezone());
    DBAdapter myDB;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.mipmap.ic_launcher);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Enter New Task");

                // Set up the input
                final EditText input = new EditText(MainActivity.this);

                // Specify the type of input expected; this, for example, sets the input as normal text
                input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_NORMAL);
                builder.setView(input);

                // Set up the buttons
                builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        addTask(input.getText().toString());
                        populateListView();
                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
//                Snackbar.make(view, m_Task, Snackbar.LENGTH_LONG)
//                        .setAction("AddTask", null).show();

            }
        });

        openDB();
        populateListView();
        listViewItemClick();
        listViewItemLongClick();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_clearDB) {
            DeleteAllTasks();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void openDB(){
        myDB = new DBAdapter(this);
        myDB.open();
    }

    private void populateListView(){
        Cursor cursor = myDB.getAllRows();
        String[] fromFieldNames = new String[] {DBAdapter.KEY_ROWID, DBAdapter.KEY_TASK};
        int[] toViewIDs = new int[] {R.id.textViewItemNumber, R.id.textViewItemTask};
        SimpleCursorAdapter myCursorAdapter;
        myCursorAdapter = new SimpleCursorAdapter(getBaseContext(), R.layout.item_layout, cursor, fromFieldNames, toViewIDs, 0);
        ListView myList = (ListView) findViewById(R.id.listViewTasks);
        myList.setAdapter(myCursorAdapter);
    }

    private void addTask(String task){
        today.setToNow();
        String timeStamp = today.format("%Y-%m-%d %H:%M:%S");
        if(!TextUtils.isEmpty(task)){
            myDB.insertRow(task, timeStamp);
        }
    }

    private void updateTask(long id, String newTask){
        Cursor cursor = myDB.getRow(id);
        if(cursor.moveToFirst()){
            String task = newTask;
            today.setToNow();
            String date = today.format("%Y-%m-%d %H:%M:%S");
            myDB.updateRow(id, task, date);
        }

        cursor.close();
    }

    private void listViewItemClick(){
        final ListView myList = (ListView) findViewById(R.id.listViewTasks);
        myList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long id) {
                Cursor cursor = myDB.getRow(id);
                if(cursor.moveToFirst()) {
                    long idDB = cursor.getLong(DBAdapter.COL_ROWID);
                    String task = cursor.getString(DBAdapter.COL_TASK);
                    String date = cursor.getString(DBAdapter.COL_DATE);
                    showEditTaskDialog(idDB, task);
                }
            }
        });
    }

    public void DeleteAllTasks(){
        myDB.deleteAll();
        populateListView();
    }

    public void listViewItemLongClick(){
        ListView myList = (ListView) findViewById(R.id.listViewTasks);
        myList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long id) {
                myDB.deleteRow(id);
                populateListView();
                return false;
            }
        });
    }

    private void displayToast(long id){
        Cursor cursor = myDB.getRow(id);
        if(cursor.moveToFirst()){
            long idDB = cursor.getLong(DBAdapter.COL_ROWID);
            String task = cursor.getString(DBAdapter.COL_TASK);
            String date = cursor.getString(DBAdapter.COL_DATE);

            String message = String.format("ID: %d\nTask: %s\nDate: %s", idDB, task, date);

            Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
        }

        cursor.close();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        closeDB();
    }

    private void closeDB(){
        myDB.close();
    }

    private void showEditTaskDialog(long id, String task){
        final long taskID = id;

        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Edit Task");

        // Set up the input
        final EditText input = new EditText(MainActivity.this);

        // Specify the type of input expected; this, for example, sets the input as normal text
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_NORMAL);
        input.setText(task);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String taskData = input.getText().toString();
                updateTask(taskID, taskData);
                populateListView();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

}
