package es.clarify.clarify;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import es.clarify.clarify.Objects.ScannedTag;

import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.nfc.NfcAdapter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

import java.io.InputStream;
import java.net.URL;

import es.clarify.clarify.Login.Login;
import es.clarify.clarify.Search.NfcIdentifyFragment;
import es.clarify.clarify.NFC.NfcUtility;
import es.clarify.clarify.Utilities.Database;
import es.clarify.clarify.Utilities.Utilities;
import io.realm.Realm;
import io.realm.RealmConfiguration;

import static com.firebase.ui.auth.AuthUI.getApplicationContext;

public class MainActivity extends AppCompatActivity {

    private Button logout_button;
    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private NfcUtility nfcUtility = new NfcUtility();
    private BottomNavigationView bottomNavigationView;
    private FrameLayout mainFrame;
    private NfcIdentifyFragment nfcIdentifyFragment;
    private HomeFragment homeFragment;
    private StoreFragment storeFragment;
    private Database database;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Realm.init(this);
        RealmConfiguration config = new RealmConfiguration.Builder()
                .name(Realm.DEFAULT_REALM_NAME)
                .schemaVersion(0)
                .deleteRealmIfMigrationNeeded()
                .build();
        Realm.setDefaultConfiguration(config);

        // Firebase instances

        ///////////////////   CREATE INSTANCE    /////////////////////////////////////////////////////////////////////////
//        FirebaseDatabase database2 = FirebaseDatabase.getInstance();
//        DatabaseReference databaseReference2 = database2.getReference("public");
//        ScannedTag scannedTagPush1 = new ScannedTag("41521", "Hacendado", "Leche Semidesnatada 1L", false, "verde y blanca", "2020-12-31", "aldkmfalkdmflakdml", "http://i.imgur.com/aN80yXm.png", "Frigorífico");
//        ScannedTag scannedTagPush2 = new ScannedTag("41522", "Hacendado", "Pizza mediterránea", false, "None", "2020-06-31", "aldkmfalkdmflakdml", "https://i.imgur.com/VJA0WIX.png", "Frigorífico");
//        ScannedTag scannedTagPush3 = new ScannedTag("41523", "Hacendado", "Paté Iberico 160 gramos", false, "None", "2020-06-31", "aldkmfalkdmflakdml", "https://i.imgur.com/HRvAsFR.png", "Despensa");
//        ScannedTag scannedTagPush4 = new ScannedTag("41524", "Hacendado", "Paté Iberico 260 gramos", false, "None", "2020-06-31", "aldkmfalkdmflakdml", "https://i.imgur.com/HRvAsFR.png", "Despensa");
//        ScannedTag scannedTagPush5 = new ScannedTag("41525", "Hacendado", "Paté Iberico 360 gramos", false, "None", "2020-06-31", "aldkmfalkdmflakdml", "https://i.imgur.com/HRvAsFR.png", "Despensa");
//        ScannedTag scannedTagPush6 = new ScannedTag("41526", "Hacendado", "Paté Iberico 460 gramos", false, "None", "2020-06-31", "aldkmfalkdmflakdml", "https://i.imgur.com/HRvAsFR.png", "Despensa");
//        ScannedTag scannedTagPush7 = new ScannedTag("41527", "Hacendado", "Paté Iberico 560 gramos", false, "None", "2020-06-31", "aldkmfalkdmflakdml", "https://i.imgur.com/HRvAsFR.png", "Despensa");
//        ScannedTag scannedTagPush8 = new ScannedTag("41528", "Hacendado", "Paté Iberico 660 gramos", false, "None", "2020-06-31", "aldkmfalkdmflakdml", "https://i.imgur.com/HRvAsFR.png", "Despensa");
//        ScannedTag scannedTagPush9 = new ScannedTag("41529", "Hacendado", "Paté Iberico 760 gramos", false, "None", "2020-06-31", "aldkmfalkdmflakdml", "https://i.imgur.com/HRvAsFR.png", "Despensa");
//        ScannedTag scannedTagPush10 = new ScannedTag("41530", "Hacendado", "Paté Iberico 860 gramos", false, "None", "2020-06-31", "aldkmfalkdmflakdml", "https://i.imgur.com/HRvAsFR.png", "Despensa");
//        ScannedTag scannedTagPush11 = new ScannedTag("41531", "Hacendado", "Paté Iberico 960 gramos", false, "None", "2020-06-31", "aldkmfalkdmflakdml", "https://i.imgur.com/HRvAsFR.png", "Despensa");
//        ScannedTag scannedTagPush12 = new ScannedTag("41532", "Hacendado", "Paté Iberico 1060 gramos", false, "None", "2020-06-31", "aldkmfalkdmflakdml", "https://i.imgur.com/HRvAsFR.png", "Despensa");
////        databaseReference2.child("carlosjavier@gmail,com").child("stores").child("wardrobe").push().setValue(scannedTagPush);
//        databaseReference2.child("tags").push().setValue(scannedTagPush1);
//        databaseReference2.child("tags").push().setValue(scannedTagPush2);
//        databaseReference2.child("tags").push().setValue(scannedTagPush3);
//        databaseReference2.child("tags").push().setValue(scannedTagPush4);
//        databaseReference2.child("tags").push().setValue(scannedTagPush5);
//        databaseReference2.child("tags").push().setValue(scannedTagPush6);
//        databaseReference2.child("tags").push().setValue(scannedTagPush7);
//        databaseReference2.child("tags").push().setValue(scannedTagPush8);
//        databaseReference2.child("tags").push().setValue(scannedTagPush9);
//        databaseReference2.child("tags").push().setValue(scannedTagPush10);
//        databaseReference2.child("tags").push().setValue(scannedTagPush11);
//        databaseReference2.child("tags").push().setValue(scannedTagPush12);
        ///////////////////   CREATE INSTANCE    /////////////////////////////////////////////////////////////////////////

        // NFC instances
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        // Toolbar instances
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set Fragment options
        nfcIdentifyFragment = new NfcIdentifyFragment();
        homeFragment = new HomeFragment();
        storeFragment = new StoreFragment();
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
                        setFragment(storeFragment);
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


        logout_button = (Button) findViewById(R.id.logOut);
        logout_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logOut();
            }
        });


    }

    private void setFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.mainFrame, fragment);
        fragmentTransaction.commit();

    }

    public void logOut() {
        FirebaseAuth.getInstance().signOut();
        // Google sign out
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        mGoogleSignInClient.signOut().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        startActivity(new Intent(getApplicationContext(), Login.class));
                        finish();
                    }
                });
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

}