package com.example.dcp.mapcontacts;


import android.Manifest;
import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.pm.PackageManager;
import android.nfc.Tag;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.AsyncTask;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import static android.content.ContentValues.TAG;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback{

    private static final String TAG = "MainActivity";
    public static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 1;
    public static final int MY_PERMISSIONS_REQUEST_WRITE_CONTACTS = 1;

    public Button showMap;
    public GoogleMap contMap;
    private SupportMapFragment mapFragment;
    String lat[]=new String[6],lon[]=new String[6],name[]=new String[6];
    int i=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        showMap = (Button) findViewById(R.id.button);
        mapFragment = new SupportMapFragment();
        requestPermission();
        GetContacts newContacts = new GetContacts();
        newContacts.execute();
        showMap.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                v.setClickable(false);
                mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapView);
                mapFragment.getMapAsync(MainActivity.this);
            }
        });

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        contMap = googleMap;
        i=0;
        Log.d(TAG, "onMapReady: ");
        // Add a marker in Sydney, Australia, and move the camera.
        while (name[i]!=null) {
            Log.d(TAG, "onMapReady: "+lat[i]);
            LatLng pos = new LatLng(Integer.parseInt(lat[i])/1000000,Integer.parseInt(lon[i])/1000000);
            contMap.addMarker(new MarkerOptions().position(pos).title(name[i]));
            contMap.moveCamera(CameraUpdateFactory.newLatLng(pos));
            i++;
        }
    }

    private class GetContacts extends AsyncTask<String, Void, String> {
        String str;
        String displayName,mailId,mobileNo,homeNo;

        @Override
        protected String doInBackground(String... params) {
            try {
                URL url = new URL("http://www.cs.columbia.edu/~coms6998-8/assignments/homework2/contacts/contacts.txt");

                BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));

                while ((str = in.readLine()) != null) {
                    // str is one line of text; readLine() strips the newline character(s)
                    String[] details = str.trim().split("\\s+");
                    displayName = details[0];
                    mailId =details[1];
                    mobileNo = details[2];
                    homeNo = details[3];
                    lat[i] =  mobileNo;
                    lon[i] = homeNo;
                    name[i]=displayName;
                    Log.d(TAG, "doInBackground: "+name[i]);
                    ArrayList<ContentProviderOperation> cntPro = new ArrayList<>();


                    cntPro.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                            .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE,null)
                            .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE,null)
                            .build());

                    cntPro.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID,0)
                            .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                            .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,displayName)
                            .build());

                    cntPro.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID,0)
                            .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
                            .withValue(ContactsContract.CommonDataKinds.Email.DATA,mailId)
                            .withValue(ContactsContract.CommonDataKinds.Email.TYPE, ContactsContract.CommonDataKinds.Email.TYPE_WORK)
                            .build());

                    cntPro.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID,0)
                            .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                            .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, mobileNo)
                            .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                            .build());

                    cntPro.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID,0)
                            .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                            .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, homeNo)
                            .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_HOME)
                            .build());
                    try
                    {
                        getContentResolver().applyBatch(ContactsContract.AUTHORITY,cntPro);
                    }
                    catch(RemoteException exp){
                    }
                    catch(OperationApplicationException exp){
                    }
                    i++;
                }
                in.close();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            
            return str;
        }

        protected void onPostExecute() {



        }

    }

    private void requestPermission() {
        if (ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.READ_CONTACTS)!= PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.READ_CONTACTS)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.READ_CONTACTS},
                        MY_PERMISSIONS_REQUEST_READ_CONTACTS);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.WRITE_CONTACTS)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.WRITE_CONTACTS},
                        MY_PERMISSIONS_REQUEST_WRITE_CONTACTS);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
    }

}