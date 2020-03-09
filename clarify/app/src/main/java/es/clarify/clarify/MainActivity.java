package es.clarify.clarify;

import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import es.clarify.clarify.NFC.NdefMessageParser;
import es.clarify.clarify.NFC.NfcUtility;
import es.clarify.clarify.NFC.ParsedNdefRecord;
import es.clarify.clarify.Objects.FirebaseReferences;
import es.clarify.clarify.Objects.ScannedTag;
import es.clarify.clarify.Search.IdentifyFragment;
import es.clarify.clarify.Utilities.UtilitiesFirebase;

public class MainActivity extends AppCompatActivity {

    private TextView text;
    private TextView text_company;
    private TextView text_model;
    private TextView text_expiration_date;
    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private BottomNavigationView bottomNavigationView;
    private FrameLayout frameLayout;
    private IdentifyFragment identifyFragment;
    private NfcUtility nfcUtility = new NfcUtility();
    List<ScannedTag> scannedTagList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        text = (TextView) findViewById(R.id.text);
        text_company = (TextView) findViewById(R.id.text_company);
        text_model = (TextView) findViewById(R.id.text_model);
        text_expiration_date = (TextView) findViewById(R.id.text_expiration_date);

        identifyFragment = new IdentifyFragment();

        // Toolbar instances
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // NFC instances
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        // Firebase instances

        ///////////////////   CREATE INSTANCE    /////////////////////////////////////////////////////////////////////////
//        FirebaseDatabase database2 = FirebaseDatabase.getInstance();
//        DatabaseReference databaseReference2 = database2.getReference("public");
//        ScannedTag scannedTagPush = new ScannedTag("41521", "Hacendado", "Leche Entera", false, null, "2020-12-31", "aldkmfalkdmflakdml", "https://a0.soysuper.com/cfcf9443216df9227ed464e54b684edc.1500.0.0.0.wmark.cf933c27.jpg");
//        databaseReference2.child("tags").push().setValue(scannedTagPush);
        ///////////////////   CREATE INSTANCE    /////////////////////////////////////////////////////////////////////////

        scannedTagList = new ArrayList<>();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = database.getReference().child(("public"));
        Query query = databaseReference.child("tags").orderByChild("id").equalTo("41520");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // dataSnapshot is the "issue" node with all children with id 0
                    for (DataSnapshot scannedTagFirebase : dataSnapshot.getChildren()) {
                        Log.d("FIREBASE_ASYN_1", scannedTagFirebase.toString());
                        ScannedTag scannedTag = scannedTagFirebase.getValue(ScannedTag.class);
                        Log.d("FIREBASE_ASYN_2", scannedTag.toString());
                        scannedTagList.add(scannedTag);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        if (nfcAdapter == null) {
            Toast.makeText(this, "Dispositivo incompatible", Toast.LENGTH_LONG).show();
            finish();
            return;
        }


        pendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, this.getClass())
                        .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        /*
        setFragment(identifyFragment);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.nav_search:
                        setFragment(identifyFragment);
                        bottomNavigationView.setItemBackgroundResource(R.color.gray);
                        return true;
                    case R.id.nav_folder:
                        bottomNavigationView.setItemBackgroundResource(R.color.colorPrimary);
                        return true;
                    case R.id.nav_explore:
                        bottomNavigationView.setItemBackgroundResource(R.color.colorAccent);
                        return true;
                }
                return false;
            }
        });

         */

    }
    /*
    private void setFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.identifyFragment, fragment);
        fragmentTransaction.commit();

    }
     */

    @Override
    protected void onResume() {
        super.onResume();

        if (nfcAdapter != null) {
            if (!nfcAdapter.isEnabled())
                // Device has a NFC module but it is not enable. Sending the user to Android's configuration panel.
                showWirelessSettings();

            // Give the priority to our app
            nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
        }
    }

    private void showWirelessSettings() {
        Toast.makeText(this, "¡Activa el NFC aquí!", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
        startActivity(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        resolveIntent(intent);
    }

    private void resolveIntent(Intent intent) {
        String action = intent.getAction();

        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            NdefMessage[] msgs;

            if (rawMsgs != null) {
                msgs = new NdefMessage[rawMsgs.length];

                for (int i = 0; i < rawMsgs.length; i++) {
                    msgs[i] = (NdefMessage) rawMsgs[i];
                }

            } else {
                byte[] empty = new byte[0];
                byte[] id = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID);
                Tag tag = (Tag) intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                byte[] payload = nfcUtility.dumpTagData(tag).getBytes();
                NdefRecord record = new NdefRecord(NdefRecord.TNF_UNKNOWN, empty, id, payload);
                NdefMessage msg = new NdefMessage(new NdefRecord[] {record});
                msgs = new NdefMessage[] {msg};
            }
            UtilitiesFirebase utilitiesFirebase = new UtilitiesFirebase(msgs, Arrays.asList(text, text_company, text_model, text_expiration_date));
            utilitiesFirebase.printInfo();
        }
    }
}