package edu.utep.cs.cs4330.firebaseproject;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.w3c.dom.Text;

import java.io.IOException;

public class DataEntry extends AppCompatActivity {
    EditText txtItem, txtURL, txtCP;
    private int receivedPosition;
    Button btnAdd, btnEdit, btnRefresh, btnBrowse;
    PriceFinder myPriceFinder;
    public double itemCurrentPrice, currentPriceX, oldPrice, newPrice;
    public String firebaseItemID, priceChangeUpdate, itemX, urlX, priceChangeX, url;
    SimpleCursorAdapter simpleCA;
    private Handler myHandler = new Handler();

    DatabaseReference firebaseDBref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //ma = new MainActivity();
        myPriceFinder = new PriceFinder();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_entry);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        txtItem = (EditText)findViewById(R.id.txtItem);
        txtURL = (EditText)findViewById(R.id.txtURL);
        txtCP = (EditText)findViewById(R.id.txtCP);

        btnAdd = findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(this::AddClicked);

        btnEdit = findViewById(R.id.btnEdit);
        btnEdit.setOnClickListener(this::EditClicked);

        btnRefresh = findViewById(R.id.btnRefresh);
        btnRefresh.setOnClickListener(this::refreshClicked);

        btnBrowse = findViewById(R.id.btnBrowse);
        btnBrowse.setOnClickListener(this::browseClicked);

        //Check for passed data between activities...
        Bundle b;
        b = getIntent().getExtras();
        if(b!=null){//Add new Item
            if(b.getString("Operation").equals("AddNewItem")){
                btnEdit.setEnabled(false);
                txtCP.setEnabled(false);
                btnBrowse.setEnabled(false);
                Toast.makeText(this, "Please enter item name, URL and fetch price", Toast.LENGTH_SHORT).show();
            }
            if(b.getString("Operation").equals("EditItem")){
                btnAdd.setEnabled(false);
                //btnRefresh.setEnabled(false);
                txtCP.setEnabled(false);

                //Load text fields with selected item data from the listview
                firebaseItemID = b.getString("ID");
                txtItem.setText(b.getString("Item"));
                txtURL.setText(b.getString("URL"));
                Double cp = b.getDouble("CP");
                txtCP.setText(cp.toString());
                priceChangeUpdate = b.getString("PC");
                receivedPosition = b.getInt("position");

                oldPrice = cp;
            }
        }

        txtURL.setOnFocusChangeListener(new View.OnFocusChangeListener() {//fill in wwww and http://
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {//Once URL loses focus, fetch price
                    checkFields();
                }
            }
        });
    }

    public void AddClicked(View view) {
        if(checkFields()){
            if(txtCP.getText().toString().equals("")){
                fetchPrice();
            }
            //Firebase item addition
            firebaseDBref = FirebaseDatabase.getInstance().getReference("items"); //Referencing items node
            //Creating a unique id for each item using firebase post method
            firebaseItemID = firebaseDBref.push().getKey(); //create a unique reference for the new entry

            Item i = new Item(firebaseItemID, itemX, urlX, currentPriceX, "Newly Added item");
            //store the item in database
            firebaseDBref.child(firebaseItemID).setValue(i); //create a new child and insert record
            Toast.makeText(this, "New item added in database", Toast.LENGTH_SHORT).show();
            goBacktoMainActivity();
        }
    }

    public void EditClicked(View view) {
        if(checkFields()){
            //Firebase item addition
            firebaseDBref = FirebaseDatabase.getInstance().getReference("items"); //Referencing items node
            priceChangeUpdate = myPriceFinder.calculateChange(newPrice, oldPrice);

            Item i = new Item(firebaseItemID, itemX, urlX, currentPriceX, priceChangeUpdate);
            //save existing item
            firebaseDBref.child(firebaseItemID).setValue(i); //If new id is not provided, existing item will be updated/overwritten
            Toast.makeText(this, "Item updated!", Toast.LENGTH_SHORT).show();

            goBacktoMainActivity();
        }
    }
    //Fetch the item price.
    public void refreshClicked(View view){
        if(checkFields()){
            fetchPrice();
        }
    }

    public void browseClicked(View view){
        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(txtURL.getText().toString()));
        startActivity(i);
    }

    //Method to verify field data
    public boolean checkFields(){
        if (txtItem.getText().toString().matches("")||txtURL.getText().toString().matches("")) {
            Toast.makeText(this, "Please provide item details", Toast.LENGTH_SHORT).show();
            txtItem.requestFocus();
            return false;
        }else {
            url = txtURL.getText().toString();
            if(!url.startsWith("www.")&& !url.startsWith("http")){
                url = "www."+url;
            }
            if(!url.startsWith("http")){
                url = "http://"+url;
            }
            txtURL.setText(url);

            itemX = txtItem.getText().toString();
            urlX = url;
            if(!txtCP.getText().toString().equals("")){
                currentPriceX = Double.parseDouble(txtCP.getText().toString());
            }
            priceChangeX = priceChangeUpdate;
            return true;
        }
    }
    public void fetchPrice(){
        Runnable r = new Runnable() {
            @Override
            public void run() {
                try {
                    itemCurrentPrice = myPriceFinder.getNewPrice(txtURL.getText().toString());
                }catch(IOException e){e.printStackTrace();}
                setPrice();
            }
        };
        new Thread(r).start();
    }
    public void setPrice(){
        myHandler.post(new Runnable() {
            @Override
            public void run() {
                txtCP.setText((String.format("%.2f", itemCurrentPrice)));
                priceChangeUpdate = "Newly added item";
                currentPriceX = Double.parseDouble(txtCP.getText().toString());
                newPrice = currentPriceX;
                if(currentPriceX==0) {//if no price found, show a message
                    Toast ifx = Toast.makeText(getApplicationContext(), "The provided URL does not have any recognized pattern product information!", Toast.LENGTH_SHORT);
                    ifx.show();
                }else{
                    Toast ifx = Toast.makeText(getApplicationContext(), "Pattern matched!\nFound item price: "+newPrice, Toast.LENGTH_SHORT);
                    ifx.show();
                }
            }
        });
    }
    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
    }
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }
    //After Add or Edit item, calling back the main activity.
    public void goBacktoMainActivity(){
        //Passing the intent to Main Activity!
        Intent j = new Intent(DataEntry.this, edu.utep.cs.cs4330.firebaseproject.MainActivity.class);
        DataEntry.this.finish();
        startActivity(j);
    }
}
