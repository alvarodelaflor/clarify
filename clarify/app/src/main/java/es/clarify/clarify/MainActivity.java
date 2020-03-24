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
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import es.clarify.clarify.Login.Login;
import es.clarify.clarify.Search.NfcIdentifyFragment;
import es.clarify.clarify.NFC.NfcUtility;
import es.clarify.clarify.Objects.ScannedTag;
import es.clarify.clarify.Utilities.UtilitiesFirebase;

public class MainActivity extends AppCompatActivity {

    private Button logout_button;
    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private NfcUtility nfcUtility = new NfcUtility();
    private BottomNavigationView bottomNavigationView;
    private FrameLayout mainFrame;
    private NfcIdentifyFragment nfcIdentifyFragment;
    private HomeFragment homeFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // NFC instances
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        // Toolbar instances
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set Fragment options
        nfcIdentifyFragment = new NfcIdentifyFragment();
        homeFragment = new HomeFragment();
        mainFrame = (FrameLayout) findViewById(R.id.mainFrame);
        setContentView(R.layout.activity_main); // Init main frame for the first boot

        // Set navBar
        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottomNavigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.nav_home:
                        setFragment(homeFragment);
                    case R.id.nav_folder:
//                        TODO
                        return true;
                    case R.id.nav_explore:
                        setFragment(nfcIdentifyFragment);
                        return true;
                }
                return false;
            }
        });

        pendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, this.getClass())
                        .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);



        // Firebase instances

        ///////////////////   CREATE INSTANCE    /////////////////////////////////////////////////////////////////////////
//        FirebaseDatabase database2 = FirebaseDatabase.getInstance();
//        DatabaseReference databaseReference2 = database2.getReference("private");
//        ScannedTag scannedTagPush = new ScannedTag("41544", "Kappa", "Camiseta Real Betis XL", false, "verde y blanca", "2020-12-31", "aldkmfalkdmflakdml", "https://a0.soysuper.com/cfcf9443216df9227ed464e54b684edc.1500.0.0.0.wmark.cf933c27.jpg");
//        databaseReference2.child("carlosjavier@gmail,com").child("stores").child("wardrobe").push().setValue(scannedTagPush);
        ///////////////////   CREATE INSTANCE    /////////////////////////////////////////////////////////////////////////


//        logout_button = (Button) findViewById(R.id.logOut)
//        logout_button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                logout(v);
//                startActivity(new Intent(getApplicationContext(),Login.class));
//            }
//        });


    }

    private void setFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.mainFrame, fragment);
        fragmentTransaction.commit();

    }

    public void logout(View view) {
        FirebaseAuth.getInstance().signOut();//logout
        startActivity(new Intent(getApplicationContext(), Login.class));
        finish();
    }

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
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        nfcIdentifyFragment.resolveIntent(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

//    private void resolveIntent(Intent intent) {
//        String action = intent.getAction();
//
//        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
//                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
//                || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
//            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
//            NdefMessage[] msgs;
//
//            if (rawMsgs != null) {
//                msgs = new NdefMessage[rawMsgs.length];
//
//                for (int i = 0; i < rawMsgs.length; i++) {
//                    msgs[i] = (NdefMessage) rawMsgs[i];
//                }
//
//            } else {
//                byte[] empty = new byte[0];
//                byte[] id = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID);
//                Tag tag = (Tag) intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
//                byte[] payload = nfcUtility.dumpTagData(tag).getBytes();
//                NdefRecord record = new NdefRecord(NdefRecord.TNF_UNKNOWN, empty, id, payload);
//                NdefMessage msg = new NdefMessage(new NdefRecord[]{record});
//                msgs = new NdefMessage[]{msg};
//            }
//            UtilitiesFirebase utilitiesFirebase = new UtilitiesFirebase(msgs, Arrays.asList(text, text_company, text_model, text_expiration_date));
//            utilitiesFirebase.printInfo();
//        }
//    }
}