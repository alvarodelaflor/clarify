package es.clarify.clarify;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.provider.Settings;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import de.hdodenhof.circleimageview.CircleImageView;
import es.clarify.clarify.Login.Login;
import es.clarify.clarify.Search.NfcIdentifyFragment;
import es.clarify.clarify.NFC.NfcUtility;
import es.clarify.clarify.Store.StoreFragment;
import es.clarify.clarify.Utilities.Database;
import es.clarify.clarify.Utilities.GoogleUtilities;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private NfcUtility nfcUtility = new NfcUtility();
    private BottomNavigationView bottomNavigationView;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private ViewPageAdapter viewPageAdapter;
    private NfcIdentifyFragment nfcIdentifyFragment;
    private HomeFragment homeFragment;
    private StoreFragment storeFragment;
    private Database database = new Database();
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private MenuItem menuItemLogOut;
    private CircleImageView imgHeader;
    private CircleImageView profileImg;
    private TextView nameUser;
    private TextView emailUser;
    private FirebaseUser firebaseUser;
    private Boolean identify = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        firebaseUser = new GoogleUtilities().getCurrentUser();

//        Realm.init(this);
//        RealmConfiguration config = new RealmConfiguration.Builder()
//                .name(Realm.DEFAULT_REALM_NAME)
//                .schemaVersion(0)
//                .deleteRealmIfMigrationNeeded()
//                .build();
//        Realm.setDefaultConfiguration(config);

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
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        profileImg = (CircleImageView) toolbar.findViewById(R.id.toolbar_profile_image);
        Glide.with(this).load(firebaseUser.getPhotoUrl()).into(profileImg);
        setSupportActionBar(toolbar);

        profileImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        drawerLayout = findViewById(R.id.main_layout2);
        navigationView = findViewById(R.id.navigation_view);

        View headerView = navigationView.getHeaderView(0);
        headerView.getBackground().setAlpha(70);
        nameUser = (TextView) headerView.findViewById(R.id.name_user);
        nameUser.setText(firebaseUser.getDisplayName());
        emailUser = (TextView) headerView.findViewById(R.id.email_user);
        emailUser.setText(firebaseUser.getEmail());
        imgHeader = (CircleImageView) headerView.findViewById(R.id.img_profile);
        Glide.with(this).load(firebaseUser.getPhotoUrl()).into(imgHeader);

        // Set navBar
        tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        viewPager = (ViewPager) findViewById(R.id.mainFrame);
        viewPageAdapter = new ViewPageAdapter(getSupportFragmentManager());

        // Add fragment
        homeFragment = new HomeFragment();
        storeFragment = new StoreFragment();
        nfcIdentifyFragment = new NfcIdentifyFragment();

        viewPageAdapter.addFragment(homeFragment, "");
        viewPageAdapter.addFragment(storeFragment, "");
        viewPageAdapter.addFragment(nfcIdentifyFragment, "");

        viewPager.setAdapter(viewPageAdapter);
        tabLayout.setupWithViewPager(viewPager);

        tabLayout.getTabAt(0).setIcon(R.drawable.ic_home_black_24dp);
        tabLayout.getTabAt(1).setIcon(R.drawable.ic_folder_black_24dp);
        tabLayout.getTabAt(2).setIcon(R.drawable.ic_explore_black_24dp);

        int tabIconColor = ContextCompat.getColor(getApplicationContext(), R.color.white);
        int tabIconColor2 = ContextCompat.getColor(getApplicationContext(), R.color.colorAccent);
        tabLayout.getTabAt(0).getIcon().setColorFilter(tabIconColor2, PorterDuff.Mode.SRC_IN);
        tabLayout.getTabAt(1).getIcon().setColorFilter(tabIconColor, PorterDuff.Mode.SRC_IN);
        tabLayout.getTabAt(2).getIcon().setColorFilter(tabIconColor, PorterDuff.Mode.SRC_IN);

        tabLayout.setOnTabSelectedListener(
                new TabLayout.ViewPagerOnTabSelectedListener(viewPager) {

                    @Override
                    public void onTabSelected(TabLayout.Tab tab) {
                        super.onTabSelected(tab);
                        int tabIconColor = ContextCompat.getColor(getApplicationContext(), R.color.colorAccent);
                        tab.getIcon().setColorFilter(tabIconColor, PorterDuff.Mode.SRC_IN);
                        if (tab.getPosition() == 2) {
                            identify = true;
                        } else {
                            identify = false;
                        }
                    }

                    @Override
                    public void onTabUnselected(TabLayout.Tab tab) {
                        super.onTabUnselected(tab);
                        int tabIconColor = ContextCompat.getColor(getApplicationContext(), R.color.white);
                        tab.getIcon().setColorFilter(tabIconColor, PorterDuff.Mode.SRC_IN);
                    }

                    @Override
                    public void onTabReselected(TabLayout.Tab tab) {
                        super.onTabReselected(tab);
                    }
                }
        );

        ActionBar actionBar = getSupportActionBar();
        actionBar.setElevation(0);

        setNavigationViewListener();

        pendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, this.getClass())
                        .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()) {

            case R.id.logout: {
                logOut();
                break;
            }
        }
        return true;
    }

    private void setNavigationViewListener() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);
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
                        database.deleteAllDataFromDevice();
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
        if(identify) {
            nfcIdentifyFragment.resolveIntent(intent);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

}