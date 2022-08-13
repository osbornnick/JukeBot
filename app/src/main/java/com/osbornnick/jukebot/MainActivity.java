package com.osbornnick.jukebot;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.osbornnick.jukebot.databinding.ActivityHomeBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private SharedPreferences.Editor editor;
    private SharedPreferences msharedPreferences;
    private final String token = null;
    private URL mUrl = null;
    private HttpURLConnection conn = null;
    private InputStream mInputStream = null;
    private String error = null;
    private String result = null;
    private String display_name = null;
    private String connectedUserName = null;
    private final List<String> hostUIDList = new ArrayList<>();
    private final List<String> connectedList = new ArrayList<>();
    private String bluetoothName = null;
    private String notificationUID = null;

    // initialize bluetooth UI elements
    ListView mListView;
    Button mListDevices, mHost, mSendHostUID;
    TextView mBTStatus, mHostUID, mJoinStatus;
    FirebaseUser user;
    String hostUID = null;
    SendReceive sendReceive;
    String username = null;
    FirebaseFirestore db;

    TextView mName;
    ExtendedFloatingActionButton newSession;
    ImageButton img_settings;

    private BluetoothAdapter mBluetoothAdapter;
    BluetoothDevice[] btArray;

    private static final String FCMToken = "fVKN20GcScS9IcRiObHJzP:APA91bE8fcP0lEyZbfGBglXg2nnVMn8E67NbYqAB1JSDattM3Z3YbOmJk0DZhLVvP6WSl0d5j856yfZIkvoO4oZ6Wxp1YCzf-6hJcRf5WJjj-avaGhrjBlYQoKK6POzQL882CJzc15le";
    private static final String FCMToken1 = "da9EoBepSjGs7xqJDLkEwc:APA91bGcHb-NAp8aIJpyVueBSXSpKgLNgEKM8zjB9dNTbDVl5WrwW9Nw6wOwAkr3I3x5xTnmJZe72qRiuCbuf1LKn0iBvy5l-wrdqedFlff7NdtbTFLdjPafFAHeRGfkCylJg1k9IW3r";
    static final int STATE_LISTENING = 1;
    static final int STATE_CONNECTING = 2;
    static final int STATE_CONNECTED = 3;
    static final int STATE_CONNECTION_FAILED = 4;
    static final int STATE_MESSAGE_RECEIVED = 5;

    int REQUEST_ENABLE_BLUETOOTH = 1;

    private static final String APP_NAME = "JukeBot";
    private static final UUID MY_UUID = UUID.fromString("38400000-8cf0-11bd-b23e-10b96e4ef00d");

    // RecyclerView SessionName/Host
    SessionRecyclerViewAdapter mAdapter;
    ArrayList<Session> mList;
    private ActivityHomeBinding binding;
    RecyclerView mRecyclerView;

    // firebase cloud message
    ArrayList<String> tokenList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        newSession = findViewById(R.id.newSession);
        //joinSessionTest = findViewById(R.id.joinSessionTest);
        img_settings = findViewById(R.id.personalSettings);

        // initialize firestore db
        db = FirebaseFirestore.getInstance();
        mList = new ArrayList<>();
        mName = findViewById(R.id.greeting);

        // bluetooth UI elements
        mListView = findViewById(R.id.lv_deviceslist);
        mListDevices = findViewById(R.id.btn_listDevices);
        mHost = findViewById(R.id.btn_host);
        mSendHostUID = findViewById(R.id.btn_shareHostUID);
        mBTStatus = findViewById(R.id.tv_btstatus);
        mHostUID = findViewById(R.id.tv_hostUID);
        mJoinStatus = findViewById(R.id.tv_hostclient);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // recyclerview UI elements
        mRecyclerView = findViewById(R.id.joinedSession_rv);

        // retrieve token from Firestore for push notifications
        tokenList = new ArrayList<>();
//        retrieveToken();
//        listenForAuthChanges();

        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        }
        if (user != null) {
            db.collection("users").document(user.getUid()).get().addOnSuccessListener(snap -> {
                mName.setText(snap.get("username", String.class));
                username = snap.get("username", String.class);
            });
        }
        FirebaseAuth.getInstance().addAuthStateListener(firebaseAuth -> {
            FirebaseUser u = firebaseAuth.getCurrentUser();
            if (u != null) {
                db.collection("users").document(u.getUid()).get().addOnSuccessListener(snap -> {
                    mName.setText(snap.get("username", String.class));
                    username = snap.get("username", String.class);
                });
            } else {
                mName.setText("");
            }
        });

        //set onClickListeners
        newSession.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                    Toast.makeText(MainActivity.this, "Please login to start the session", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(MainActivity.this, StartSessionActivity.class);

                startActivity(intent);
                user = FirebaseAuth.getInstance().getCurrentUser();
                hostUID = user.getUid();
                storeHostUID(hostUID);
            }
        });

        img_settings.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, PersonalSettingsActivity.class);
            startActivity(intent);
        });

        try {
            if (mBluetoothAdapter == null) {
                //status.setText("Bluetooth is not available");
                return;
            } else {
                //status.setText("Bluetooth is available");
                Log.d(TAG, "onCreate: calling from try catch ");
                requestBlePermissions(MainActivity.this, REQUEST_ENABLE_BLUETOOTH);
            }
        } catch (Exception e) {
            e.printStackTrace();
            //status.setText("Bluetooth is not available");
        }

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            Log.d(TAG, "onCreate: calling");
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                requestBlePermissions(MainActivity.this, REQUEST_ENABLE_BLUETOOTH);

            }
            startActivityForResult(enableIntent, REQUEST_ENABLE_BLUETOOTH);
        }

        // fire messaging
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                            return;
                        }

                        // Get new FCM registration token
                        String token = task.getResult();
                        sendToFirestoreDB(token);
                        Log.d(TAG, "Token: " + token);
                        // Toast.makeText(MainActivity.this, token, Toast.LENGTH_SHORT).show();
                    }
                });


        //listenForChange();
        //updateSessionName();


        if (user != null) {
            retrieveToken();
            listenForAuthChanges();
            listeners();
            checkSessionName();
            updateRecyclerView();
        }
    }

    public void updateRecyclerView() {
        mAdapter = new SessionRecyclerViewAdapter(MainActivity.this, mList);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(MainActivity.this, RecyclerView.VERTICAL, false);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
        swipeToDelete().attachToRecyclerView(mRecyclerView);
    }

    private void listeners() {

        mListDevices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    requestBlePermissions(MainActivity.this, REQUEST_ENABLE_BLUETOOTH);

                }
                notificationUID = PushNotificationService.getHostUID();
                Log.d(TAG, "onClick: " + notificationUID);
                Set<BluetoothDevice> bt = mBluetoothAdapter.getBondedDevices();
                String[] strings = new String[bt.size()];
                btArray = new BluetoothDevice[bt.size()];
                int index = 0;

                if (bt.size() > 0) {
                    for (BluetoothDevice device : bt) {
                        btArray[index] = device;
                        strings[index] = device.getName();
                        index++;
                    }
                    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, strings);
                    mListView.setAdapter(arrayAdapter);
                }

            }
        });

        // when the user decided to be a host get a host uid otherwise return null
        mHost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // itereate through the string array
                //String[] testArray = {FCMToken, FCMToken1};
                bluetoothName = getLocalBluetoothName();
                for (int i = 0; i < tokenList.size(); i++) {
                    FireBaseCloudMessage.pushNotification(MainActivity.this, tokenList.get(i), "JukeBot", username + " started the session on " + bluetoothName);
                }
                Log.d(TAG, "onClick: tokenList " + tokenList);
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                prefs.edit().remove("HostUID").apply();
                user = FirebaseAuth.getInstance().getCurrentUser();
                hostUID = user.getUid();
                Log.d(TAG, "onClick: " + hostUID);
                mJoinStatus.setText("Host");
                MainActivity.ServerClass serverClass = new MainActivity.ServerClass();
                serverClass.start();
            }
        });

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                // if the host uid's session setting allow joins is set to false then session should not be requestable
                Log.d(TAG, "onItemClick: " + notificationUID);
                isAllowJoins(notificationUID, i);
            }
        });

        // host sends the uid to other
        mSendHostUID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: " + hostUID);
                // store the host uid to the FireStore
                storeHostUID(hostUID);
                // if you are not the host, hostuid should return null and prevents you from clicking the button
                try {
                    if (hostUID == null) {
                        Toast.makeText(MainActivity.this, "You are not the host", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    sendReceive.write(hostUID.getBytes());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void retrieveToken() {
        db.collection("users")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                //Log.d(TAG, document.getId() + " => " + document.get("token"));

                                if (document.getString("token") == null) {
                                    //Log.d(TAG, "onComplete: token is null");
                                } else {
                                    String array = document.getString("token");
                                    if (array.equals("null")) {
                                        continue;
                                    }
                                    //Log.d(TAG, "onComplete: " + array);

                                    tokenList.add(array);
                                    Log.d(TAG, "onComplete: tokenList " + tokenList);
                                }

                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });

    }

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {

            switch (msg.what) {
                case STATE_LISTENING:
                    mBTStatus.setText("Listening");
                    break;
                case STATE_CONNECTING:
                    mBTStatus.setText("Connecting");
                    break;
                case STATE_CONNECTED:
                    mBTStatus.setText("Connected");
                    break;
                case STATE_CONNECTION_FAILED:
                    mBTStatus.setText("Connection Failed");
                    break;
                case STATE_MESSAGE_RECEIVED:
                    byte[] readBuff = (byte[]) msg.obj;
                    String tempMsg = new String(readBuff, 0, msg.arg1);
                    mJoinStatus.setText("Client");
                    mHostUID.setText(tempMsg);
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("HostUID", tempMsg);
                    editor.apply();
                    storeHostUID(tempMsg);
                    //getUserNameFromHostUID(tempMsg);
                    Log.d(TAG, "handleMessage: " + tempMsg);

                    break;
            }
            return true;
        }
    });

    private class ServerClass extends Thread {
        private BluetoothServerSocket serverSocket;

        public ServerClass() {
            try {
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    requestBlePermissions(MainActivity.this, REQUEST_ENABLE_BLUETOOTH);
                }
                serverSocket = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(APP_NAME, MY_UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {
            BluetoothSocket socket = null;
            try {
                Message message = Message.obtain();
                message.what = STATE_CONNECTING;
                handler.sendMessage(message);

                socket = serverSocket.accept();
            } catch (IOException e) {
                e.printStackTrace();
                Message message = Message.obtain();
                message.what = STATE_CONNECTION_FAILED;
                handler.sendMessage(message);
            }

            if (socket != null) {
                Message message = Message.obtain();
                message.what = STATE_CONNECTED;
                handler.sendMessage(message);
                sendReceive = new MainActivity.SendReceive(socket);
                sendReceive.start();

            }
        }
    }

    private class ClientClass extends Thread {
        private final BluetoothDevice device;
        private BluetoothSocket socket;

        public ClientClass(BluetoothDevice device1) {
            device = device1;

            try {
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for
                    //
                    requestBlePermissions(MainActivity.this, REQUEST_ENABLE_BLUETOOTH);

                }
                socket = device.createRfcommSocketToServiceRecord(MY_UUID);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {
            try {
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    requestBlePermissions(MainActivity.this, REQUEST_ENABLE_BLUETOOTH);
                }
                socket.connect();
                Message message = Message.obtain();
                message.what = STATE_CONNECTED;
                handler.sendMessage(message);
                sendReceive = new MainActivity.SendReceive(socket);
                sendReceive.start();


            } catch (IOException e) {
                e.printStackTrace();
                Message message = Message.obtain();
                message.what = STATE_CONNECTION_FAILED;
                handler.sendMessage(message);
            }
        }
    }

    private class SendReceive extends Thread {
        private final BluetoothSocket bluetoothSocket;
        private final InputStream inputStream;
        private final OutputStream outputStream;

        public SendReceive(BluetoothSocket socket) {
            bluetoothSocket = socket;
            InputStream tempIn = null;
            OutputStream tempOut = null;

            try {
                tempIn = bluetoothSocket.getInputStream();
                tempOut = bluetoothSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            inputStream = tempIn;
            outputStream = tempOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;


            try {
                bytes = inputStream.read(buffer);
                handler.obtainMessage(STATE_MESSAGE_RECEIVED, bytes, -1, buffer).sendToTarget();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        public void write(byte[] bytes) {
            try {
                outputStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String doInBackground(String... strings) {
        try {
            mUrl = new URL(strings[0]);
            conn = (HttpURLConnection) mUrl.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "Bearer " + token);
            conn.setDoInput(true);
            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                error = "Server returned HTTP " + conn.getResponseCode() + " " + conn.getResponseMessage();
                Log.d(TAG, "doInBackground: " + "Server returned HTTP " + conn.getResponseCode() + " " + conn.getResponseMessage());
                return null;
            }
            mInputStream = conn.getInputStream();
            StringBuilder sb = new StringBuilder();
            try {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(mInputStream));
                String len;
                while ((len = bufferedReader.readLine()) != null) {
                    sb.append(len);
                }
                bufferedReader.close();
                result = sb.toString().replace(",", ",\n");
                Log.d(TAG, "doInBackground: " + result);
                conn.disconnect();
                return result;

            } catch (Exception e) {
                e.printStackTrace();
            }
            conn.disconnect();
            return "";

        } catch (Exception e) {
            return e.toString();
        }
    }

    public void onPostExecute(String result) {

        if (result == null) {
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            JSONObject obj = new JSONObject(result);
            Log.d(TAG, "onPostExecute: " + obj);
            display_name = obj.getString("display_name");
            String email = obj.getString("email");
            String country = obj.getString("country");
            Log.d(TAG, "onPostExecute: display_name " + display_name);
            Log.d(TAG, "onPostExecute: email " + email);
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("Name", display_name);
            editor.putString("Email", email);
            editor.putString("Country", country);
            editor.apply();
            Intent intent = new Intent(MainActivity.this, StartSessionActivity.class);
            intent.putExtra("display_name", display_name);
            intent.putExtra("email", email);
            startActivity(intent);


        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "No data available", Toast.LENGTH_SHORT).show();

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.nav_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout:
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                prefs.edit().remove("Name").apply();
                prefs.edit().remove("Email").apply();
                prefs.edit().remove("Country").apply();
                Intent intent = new Intent(MainActivity.this, MainActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private static final String[] BLE_PERMISSIONS = new String[]{
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
    };

    private static final String[] ANDROID_12_BLE_PERMISSIONS = new String[]{
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.ACCESS_FINE_LOCATION,

    };

    public static void requestBlePermissions(Activity activity, int requestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            ActivityCompat.requestPermissions(activity, ANDROID_12_BLE_PERMISSIONS, requestCode);
        else
            ActivityCompat.requestPermissions(activity, BLE_PERMISSIONS, requestCode);

    }

    // client stores host uid in connectedSessionArray
    public void storeHostUID(String hostUID) {
        hostUIDList.add(hostUID);
        Log.d(TAG, "storeHostUID: " + hostUIDList);
        db.collection("users").document(hostUID).update("connectedSession", FieldValue.arrayUnion(hostUID));
    }

    // get username from hostuid. associate host uid with the username for recycler view
    public void getUserNameFromHostUID(String hostUID) {
        db.collection("users")
                .document(hostUID).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            connectedUserName = task.getResult().getString("username");
                            if (connectedUserName == null) {
                                connectedUserName = "Anonymous";
                            }
                            Log.d(TAG, "onComplete: username " + connectedUserName);
                            Map<String, Object> map = new HashMap<>();
                            map.put("connectedUserName", FieldValue.arrayUnion(connectedUserName));
                            db.collection("users")
                                    .document(user.getUid()).collection("SessionInfo").document(user.getUid()).set(map, SetOptions.merge());
                        }
                    }
                });
    }

    // update the recycler view to display both the host uid aka session name and username
    private void updateSessionName() {
        user = FirebaseAuth.getInstance().getCurrentUser();

        db.collection("users").document(user.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot documentSnapshot = task.getResult();
                Log.d(TAG, documentSnapshot.getId() + " => " + documentSnapshot.getData());
                Map<String, Object> map;
                map = documentSnapshot.getData();

//                Log.d(TAG, "onComplete: " + map.get("connectedSession"));
//                Object array = map.get("connectedSession");


                List<String> group = (List<String>) documentSnapshot.get("connectedSession");
                List<String> usernameList = (List<String>) documentSnapshot.get("connectedUserName");
                Log.d(TAG, "onComplete: " + usernameList);
                Log.d(TAG, "onComplete: String Lis　" + group);
                // Session session = new Session()

                int count = mList.size();
                try {

                    for (int i = 0; i < group.size(); i++) {
                        Session session = new Session();
                        String s1 = group.get(i);
                        String s2 = usernameList.get(i);
                        session.mSessionName = s1;
                        session.mSessionHost = s2;
                        mList.add(session);
                    }
                    if (count == 0) {
                        mAdapter.notifyDataSetChanged();
                    } else {
                        mAdapter.notifyItemRangeInserted(mList.size(), mList.size());
                    }
                    binding.joinedSessionRv.setVisibility(View.VISIBLE);
//                    for (String s : group) {
//
//                        session.mSessionName = s;
//                        mList.add(session);
//                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
    }

    // get the host uid and host username
    private void listenForAuthChanges() {
        FirebaseAuth.getInstance().addAuthStateListener(auth -> {

        });
        user = FirebaseAuth.getInstance().getCurrentUser();
        //db.collection("users").document(user.getUid()).collection("SessionInfo").addSnapshotListener(eventListener);
        if (user != null) {
            db.collection("users").document(user.getUid()).addSnapshotListener(eventListener);
        }
    }

    // event listener for getting host name and username
    private final EventListener<DocumentSnapshot> eventListener = new EventListener<DocumentSnapshot>() {
        @Override
        public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
            user = FirebaseAuth.getInstance().getCurrentUser();
            if (error != null) return;
            if (value != null) {
                //Log.d(TAG, "onEvent: " + value.get("connectedSession"));
                mList.clear();
                List<String> array = (List<String>) value.get("connectedSession");
                try {
                    for (String s : array) {
                        Log.d(TAG, "onEvent: " + s);
                        Session session = new Session();
                        session.mSessionName = s;
                        mList.add(session);
                        connectedList.add(s);
                        //Log.d(TAG, "onEvent: " + connectedList);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    mAdapter.notifyItemChanged(mList.size());
                    //mAdapter.notifyItemRangeInserted(mList.size(),mList.size());
                    binding.joinedSessionRv.smoothScrollToPosition(mList.size() - 1);
                    binding.joinedSessionRv.setVisibility(View.VISIBLE);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };

    public String getLocalBluetoothName() {
        String name = null;
        try {
            name = mBluetoothAdapter.getName();
            Log.d(TAG, "getLocalBluetoothName: " + name);
            if (name == null) {
                System.out.println("Name is null!");
                name = mBluetoothAdapter.getAddress();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return name;
    }

    public ItemTouchHelper swipeToDelete() {
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getLayoutPosition();
                mList.remove(position);
                deleteSession(position);
                mAdapter.notifyItemRemoved(position);
                Snackbar.make(mRecyclerView, "deleted the session", Snackbar.LENGTH_SHORT).show();
            }
        });
        return itemTouchHelper;
    }

    // allow users to delete the session
    private void deleteSession(int position) {
        user = FirebaseAuth.getInstance().getCurrentUser();
        //db.collection("users").document(user.getUid()).collection("SessionInfo").addSnapshotListener(eventListener);
        //db.collection("users").document(user.getUid()).d
        String toBeDeleted = connectedList.get(position);
        // if the user deletes the host session then host gets to start session again
        if (toBeDeleted.equals(user.getUid())){
            newSession.setVisibility(View.VISIBLE);
        }
        Map<String, Object> session = new HashMap<>();
        session.put("connectedSession", FieldValue.delete());
        db.collection("users").document(user.getUid()).update("connectedSession", FieldValue.arrayRemove(toBeDeleted));
    }

    // disconnect from host if the host has set allowjoins to false
    private void isAllowJoins(String hostUID, int i) {
        db.collection("Session").document(hostUID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    boolean allowJoins = (boolean) task.getResult().get("allowJoins");
                    Log.d(TAG, "onComplete: " + allowJoins);
                    if (allowJoins) {
                        MainActivity.ClientClass clientClass = new MainActivity.ClientClass(btArray[i]);
                        clientClass.start();
                        mBTStatus.setText("Connecting");
                    } else {
                        Snackbar.make(mListView, "You are not allowed to join the session", Snackbar.LENGTH_SHORT).show();
                        mBTStatus.setText("Disconnected");
                    }
                }
            }
        });
    }

    // hide a start session button if logged in user is already hosting a session
    private void checkSessionName(){
        user = FirebaseAuth.getInstance().getCurrentUser();
        db.collection("users").document(user.getUid()).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                List<String> array = (List<String>) value.get("connectedSession");
                Log.d(TAG, "onEvent: checksession name " + array);

                if (array != null) {
                    for (String s : array) {
                        try {
                            if (s.equals(user.getUid())){
                                Log.d(TAG, "onEvent: found match => " + s);
                                newSession.setVisibility(View.GONE);
                            }
                        } catch (Exception e){
                                e.printStackTrace();
                            }
                    }
                }
            }
        });
    }

    private void sendToFirestoreDB(String token) {

        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            Map<String, Object> tokenData = new HashMap<>();
            tokenData.put("token", token);
            db.collection("users").document(user.getUid()).set(tokenData, SetOptions.merge());
        }
    }

}

