package edu.utep.cs.cs4330.firebaseproject;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.PersistableBundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    public PriceFinder myPriceFinder;
    public double oldPrice, newPrice;
    public ArrayList<Item> itemArrayList;
    private ListView myListView;
    public ItemListAdapter myAdapter;
    private static DecimalFormat df2 = new DecimalFormat(".##");
    public int lastPosition = -1;
    private String itemId, url;
    public boolean connectionStatus = false;
    public String iid="", itemX="", urlX="";
    public double resultX=0;

    DatabaseReference firebaseDBref;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        //load existing items if any from firebase
        itemArrayList = new ArrayList<>();
        firebaseDBref = FirebaseDatabase.getInstance().getReference("items"); //Referencing items node
        myListView = (ListView) findViewById(R.id.myListView);
        progressBar = (ProgressBar) findViewById(R.id.update_progress);
        loadItemData();

        //context menu
        registerForContextMenu(myListView); //inflate context menu using onCreateContextMenu and the onContextItemSelected
        //On list click
        myListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                lastPosition = position;
                //prepare data and call DataEntry class

                itemId = ((TextView)view.findViewById(R.id.lblItemID1)).getText().toString();
                String name = ((TextView)view.findViewById(R.id.lblItem1)).getText().toString();
                url = ((TextView)view.findViewById(R.id.lblURL1)).getText().toString();
                double price = Double.parseDouble(((TextView)view.findViewById(R.id.lblCP1)).getText().toString());
                String change = ((TextView)view.findViewById(R.id.lblPC1)).getText().toString();

                //Toast.makeText(getApplicationContext(), "Item position="+lastPosition+" ItemID="+itemId, Toast.LENGTH_SHORT).show();
                transferListItem(itemId, name, url, price, change);
            }
        });

        //Long click on the listview
        myListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                lastPosition = position;//capture item position in long click also.
                //long click is only for deleting item or visiting item url so capturing two variables only.
                itemId = ((TextView)view.findViewById(R.id.lblItemID1)).getText().toString();
                url = ((TextView)view.findViewById(R.id.lblURL1)).getText().toString();
                return false;
            }
        });
    }

    public void loadItemData() {
        progressBar.setVisibility(View.VISIBLE);
        myPriceFinder = new PriceFinder();
        myListView = (ListView) findViewById(R.id.myListView);

        //firebase change listener
        itemArrayList.clear();//clearing list initially
        firebaseDBref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //clear the itemArrayList before adding fresh data
                itemArrayList.clear();
                for(DataSnapshot itemSnapshot : dataSnapshot.getChildren()){
                    Item item = itemSnapshot.getValue(Item.class);
                    //add all items to the list
                    itemArrayList.add(item);
                }
                //load found items in the adapter
                myAdapter = new ItemListAdapter(MainActivity.this, R.layout.adapter_view, itemArrayList);
                myListView.setAdapter(myAdapter);
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), "Something went wrong while trying to fetch data from database!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void getNewItemPrices() {
        Toast.makeText(this, "Function not available", Toast.LENGTH_SHORT).show();
    }

    public void transferListItem(String id, String name, String url, double price, String change){
        Intent i = new Intent(MainActivity.this, DataEntry.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//keeping track of activity stack

        Bundle bundle = new Bundle();
        bundle.putString("Operation", "EditItem");
        bundle.putString("ID", id);
        bundle.putString("Item", name);
        bundle.putString("URL", url);
        bundle.putDouble("CP", price);
        bundle.putInt("position", lastPosition);
        i.putExtras(bundle);

        startActivity(i);
    }

    //Context Menu...
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater mInf = getMenuInflater();
        mInf.inflate(R.menu.my_context_menu, menu);
    }
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo ACM = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        Intent i;
        switch (item.getItemId()){//which item is selected

            case R.id.viewWeb:
                i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//keeping track of activity stack
                startActivity(i);
                return true;

            case R.id.deleteItem:
                //Yes/No custom dialog
                AlertDialog.Builder bd = new AlertDialog.Builder(MainActivity.this);
                bd.setTitle("Confirm");
                bd.setMessage("Are you sure you want to delete the selected item?");
                //adding options to the button
                bd.setPositiveButton("Yes", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface di, int i){
                        //Firebase item addition
                        firebaseDBref = FirebaseDatabase.getInstance().getReference("items").child(itemId); //Referencing items child node
                        //Delete it
                        firebaseDBref.removeValue();
                        itemArrayList.remove(ACM.position);//remove from adapter
                        myAdapter.notifyDataSetChanged();//update list
                        Toast.makeText(getApplicationContext(),"Item Deleted!", Toast.LENGTH_SHORT).show();
                    }
                });
                bd.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {//do nothing.
                    }
                });
                bd.create().show();
                return true;

            default:
                return super.onContextItemSelected(item);
        }
    }

    //Options menu code
    @Override
    public boolean onCreateOptionsMenu (Menu menu) {
        getMenuInflater().inflate(R.menu.my_options_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();
        if(id==R.id.mnuNewItem){
            Intent i = new Intent(MainActivity.this, DataEntry.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//keeping track of activity stack
            Bundle bundle = new Bundle();
            bundle.putString("Operation", "AddNewItem");
            i.putExtras(bundle);
            startActivity(i);
        }
        if(id==R.id.mnuFetch){
            getNewItemPrices();
        }
        if(id==R.id.mnuLogout){
            Toast.makeText(getApplicationContext(),"Signing out...", Toast.LENGTH_SHORT).show();
            FirebaseAuth.getInstance().signOut();
            finish();
            System.exit(0);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
    }
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }
    public void onBackPressed() {
        //moveTaskToBack(true);
        Toast.makeText(getApplicationContext(),"Signing out...", Toast.LENGTH_SHORT).show();
        FirebaseAuth.getInstance().signOut();
        finish();
        Intent i = new Intent(MainActivity.this, LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//keeping track of activity stack
        startActivity(i);
    }
}