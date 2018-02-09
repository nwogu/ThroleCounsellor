package org.throle.throlecounsellor;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by TEST on 11/9/2017.
 */

public class Users extends AppCompatActivity {
    ListView usersList;
    TextView noUsersText;
    PrefManager prefManager;
    PrefMan prefMan;
    ArrayList<String> al = new ArrayList<>();
    ArrayList<String> ab = new ArrayList<>();
    int totalUsers = 0;
    ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);
        prefManager = new PrefManager(Users.this);
        prefMan = new PrefMan(Users.this);
        setTitle(prefMan.getUserName());
        prefManager.setFirstTimeLaunch(false);
        resgisterCounsellorOnline();
        usersList = (ListView)findViewById(R.id.usersList);
        noUsersText = (TextView)findViewById(R.id.noUsersText);

            fetchUsers();

        usersList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(Users.this);
                alertDialog.setTitle("New Session");
                alertDialog.setMessage("You are about to start a session with a user");
                alertDialog.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        UserDetails.chatWith = al.get(position);
                        prefManager.setSessionStart(UserDetails.chatWith);
                        startActivity(new Intent(Users.this, Chat.class));
                    }
                });// Setting Negative "NO" Button
                alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                alertDialog.show();

            }
        });
    }


    public void fetchUsers(){
        pd = new ProgressDialog(Users.this);
        pd.setMessage("Loading...");
        pd.setCancelable(false);
        pd.show();

        String url = "https://throle-81230.firebaseio.com/messages2.json";

        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>(){
            @Override
            public void onResponse(String s) {
                doOnSuccess(s);
            }
        },new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                System.out.println("" + volleyError);
            }
        });

        RequestQueue rQueue = Volley.newRequestQueue(Users.this);
        rQueue.add(request);
    }

    public void doOnSuccess(String s){
        try {
            JSONObject obj = new JSONObject(s);

            Iterator i = obj.keys();
            String key = "";

            while(i.hasNext()){
                key = i.next().toString();

                String[] parts = key.split("_");
                if (parts.length <= 1){
                    Log.v("Users", "This is parts-only-one: "+ parts[0]);
                }
                else {
                    String chat_with = parts[0];
                    String user_name = parts[1];
                    if(!chat_with.equals(prefMan.getUserName()) && user_name.equals(prefMan.getUserName())) {
                        al.add(chat_with);
                    }
                }

                totalUsers++;
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        

        Log.v("Users", "This is Userdetails.chatwith: "+ UserDetails.chatWith);
        Log.v("Users", "This is prefManager.getSessionStart: "+ prefManager.getSessionStart());




        if(totalUsers < 1){
            noUsersText.setVisibility(View.VISIBLE);
            usersList.setVisibility(View.GONE);
        }
        else{
            noUsersText.setVisibility(View.GONE);
            usersList.setVisibility(View.VISIBLE);
            usersList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, al));
        }

            pd.dismiss();

    }

    private void resgisterCounsellorOnline(){
        String url = "https://throle-81230.firebaseio.com/counsellorsonline.json";

        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>(){
            @Override
            public void onResponse(String s) {
                Firebase.setAndroidContext(Users.this);
                Firebase reference = new Firebase("https://throle-81230.firebaseio.com/counsellorsonline");

                if(s.equals("null")) {
                    reference.child(prefMan.getUserName()).child("online").setValue("yes");
                }
                try {
                    JSONObject obj = new JSONObject(s);

                    if (!obj.has(prefMan.getUserName())) {
                        reference.child(prefMan.getUserName()).child("password").setValue("yes");

                    } else {
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        },new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                System.out.println("" + volleyError );

            }
        });

        RequestQueue rQueue = Volley.newRequestQueue(Users.this);
        rQueue.add(request);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refresh:
                al.clear();
                fetchUsers();
                return true;

            case R.id.go_offline:

                resgisterCounsellorOffline();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void resgisterCounsellorOffline(){
        String url = "https://throle-81230.firebaseio.com/counsellorsonline.json";

        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>(){
            @Override
            public void onResponse(String s) {
                Firebase.setAndroidContext(Users.this);
                Firebase reference = new Firebase("https://throle-81230.firebaseio.com/counsellorsonline");

                if(s.equals("null")) {
                    return;
                }
                try {
                    JSONObject obj = new JSONObject(s);

                    if (obj.has(prefMan.getUserName())) {
                        reference.child(prefMan.getUserName()).removeValue();

                    } else {
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        },new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                System.out.println("" + volleyError );

            }
        });

        RequestQueue rQueue = Volley.newRequestQueue(Users.this);
        rQueue.add(request);
    }
}

