package com.aps490.drdc.prototype;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.IOException;

public class task extends AppCompatActivity implements AdapterView.OnItemClickListener {
    ListView l ;
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_view);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        String[] values;
        intent = getIntent();
        String moduleName = intent.getStringExtra("name");
        System.out.println(moduleName);

<<<<<<< HEAD
        try{
            Assembly assembly = new Assembly( getAssets().open(CourseModules.map.get(moduleName) ) );
            values = assembly.getModules().toArray(new String[0]);
            System.out.println(values);
            l = (ListView) findViewById(R.id.listView);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,R.layout.list_view_layout,R.id.list_content,values);
            l.setAdapter(adapter);
            l.setOnItemClickListener(this);
=======
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        l = (ListView) findViewById(R.id.listView);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,R.layout.list_view_layout,R.id.list_content,values);
        l.setAdapter(adapter);
        l.setOnItemClickListener(this);
>>>>>>> e3347f4bd4ec06de8b3c767c7d6116d57486657b

        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
<<<<<<< HEAD
        Intent newIntent = new Intent(this, instructions.class);
        newIntent.putExtra("name",intent.getStringExtra("name"));
        newIntent.putExtra("task",position);
        startActivity(newIntent);
=======
        Intent intent = new Intent(this, Main2Activity.class);
        intent.putExtra("position",intent.getIntExtra("position",0));
        intent.putExtra("task", position);
        startActivity(intent);
>>>>>>> e3347f4bd4ec06de8b3c767c7d6116d57486657b

    }

    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                Intent homeIntent = new Intent(this, listView.class);
                homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(homeIntent);
        }
        return (super.onOptionsItemSelected(menuItem));
    }
}
