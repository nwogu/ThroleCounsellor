package org.throle.throlecounsellor;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by TEST on 11/9/2017.
 */

public class Chat extends AppCompatActivity {
    LinearLayout layout;
    ImageView sendButton;
    EditText messageArea;
    PrefManager prefManager;
    PrefMan prefMan;
    String problem = "No";
    ScrollView scrollView;
    Firebase reference1, reference2, reference3, reference4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        prefManager = new PrefManager(Chat.this);
        prefMan = new PrefMan(Chat.this);
        layout = (LinearLayout)findViewById(R.id.layout1);
        sendButton = (ImageView)findViewById(R.id.sendButton);
        messageArea = (EditText)findViewById(R.id.messageArea);
        scrollView = (ScrollView)findViewById(R.id.scrollView);

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        Log.v("Chat", "This is Userdetails.chatwith: "+ UserDetails.chatWith);
        Log.v("Chat", "This is prefManager.getSessionStart: "+ prefManager.getSessionStart());
        Log.v("Caht", "This is prefMangager.getUserName: "+ prefMan.getUserName());
        Log.v("Chat", "This is Userdetails.UserName: "+ UserDetails.username);

        Firebase.setAndroidContext(this);
        reference1 = new Firebase("https://throle-81230.firebaseio.com/messages/" + prefMan.getUserName() + "_" + UserDetails.chatWith);
        reference2 = new Firebase("https://throle-81230.firebaseio.com/messages/" + UserDetails.chatWith + "_" + prefMan.getUserName());
        reference3 = new Firebase("https://throle-81230.firebaseio.com/messages2/" + prefMan.getUserName() + "_" + UserDetails.chatWith);
        reference4 = new Firebase("https://throle-81230.firebaseio.com/messages2/" + UserDetails.chatWith + "_" + prefMan.getUserName());


        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String messageText = messageArea.getText().toString();

                if(!messageText.equals("")){
                    Map<String, String> map = new HashMap<String, String>();
                    map.put("message", messageText);
                    map.put("user", prefMan.getUserName());
                    reference1.push().setValue(map);
                    reference2.push().setValue(map);
                    reference3.push().setValue(map);
                    reference4.push().setValue(map);
                    sendNotificationToUser(prefMan.getUserName(), messageText, UserDetails.chatWith);
                }
                messageArea.setText("");
            }
        });

        reference1.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Map map = dataSnapshot.getValue(Map.class);
                String message = map.get("message").toString();
                String userName = map.get("user").toString();
                if (map.get("problem") != null){
                problem = map.get("problem").toString();}

                TextView problemTextView= (TextView)findViewById(R.id.user_problem);
                problemTextView.setText("This user has " + problem + " issues");

                if (message.equals(prefMan.getUserName()+" has ended this session") ||
                        message.equals(UserDetails.chatWith + " has logged out") ||
                        message.equals(prefMan.getUserName() + " has logged out") ||
                        message.equals(UserDetails.chatWith + " has ended this session")){
                    addMessageBox(message, 3);

                }

                else if(userName.equals(prefMan.getUserName())){
                    addMessageBox("You:-\n" + message, 1);
                }

                else{
                    addMessageBox(UserDetails.chatWith + ":-\n" + message, 2);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

        reference3.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                prefManager.setSessionStart("");
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(Chat.this, Users.class)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK));
                    }
                }, 2 * 1000);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_chat, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.end_session:
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(Chat.this);
                alertDialog.setTitle("End Session");
                alertDialog.setMessage("Are you sure you want to end this session? Messages may not be retained");
                alertDialog.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        prefManager.setSessionStart("");
                        final Map<String, String> map = new HashMap<String, String>();
                        map.put("message", prefMan.getUserName() + " has ended this session");
                        map.put("user", prefMan.getUserName());
                        reference1.push().setValue(map);
                        reference2.push().setValue(map);
                        reference3.push().setValue(map);
                        reference4.push().setValue(map);
                        clearMessages();
                        Toast.makeText(Chat.this, "Session Ended", Toast.LENGTH_SHORT).show();
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                finish();
                            }
                        }, 3 * 1000);
                    }
                });// Setting Negative "NO" Button
                alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                alertDialog.show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void addMessageBox(String message, int type){
        TextView textView = new TextView(Chat.this);
        textView.setText(message);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, 10);
        textView.setLayoutParams(lp);

        if(type == 1) {
            textView.setBackgroundResource(R.drawable.rounded_corner1);
            textView.setTextColor(getResources().getColor(R.color.white_color));
        }
        else if (type == 3){
            textView.setBackgroundResource(R.drawable.rounded_corner3);
            textView.setTextColor(getResources().getColor(R.color.white_color));
        }
        else{
            textView.setBackgroundResource(R.drawable.rounded_corner2);
            textView.setTextColor(getResources().getColor(R.color.white_color));
        }

        layout.addView(textView);
        scrollView.post(new Runnable() {
            @Override
            public void run() {
                scrollView.fullScroll(View.FOCUS_DOWN);
            }
        });
    }

    private void clearMessages(){
        String url = "https://throle-81230.firebaseio.com/messages2.json";

        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>(){
            @Override
            public void onResponse(String s) {
                Firebase.setAndroidContext(Chat.this);
                Firebase reference = new Firebase("https://throle-81230.firebaseio.com/messages2");

                if(s.equals("null")) {
                    return;
                }
                try {
                    JSONObject obj = new JSONObject(s);

                    if (obj.has(prefMan.getUserName() + "_" + UserDetails.chatWith) ||
                            obj.has(UserDetails.chatWith + "_" + prefMan.getUserName())) {
                        reference.child(prefMan.getUserName() + "_" + UserDetails.chatWith).removeValue();
                        reference.child(UserDetails.chatWith + "_" + prefMan.getUserName()).removeValue();

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

        RequestQueue rQueue = Volley.newRequestQueue(Chat.this);
        rQueue.add(request);
    }

    public static void sendNotificationToUser(String user, final String message, String chatwith) {
        Firebase ref = new Firebase("https://throle-81230.firebaseio.com/");
        final Firebase notifications = ref.child("notificationRequests");

        Map notification = new HashMap<>();
        notification.put("username", user);
        notification.put("message", message);
        notification.put("chatwith", chatwith);

        notifications.push().setValue(notification);
    }

}
