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
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.nfc.NfcAdapter;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import de.hdodenhof.circleimageview.CircleImageView;
import es.clarify.clarify.Home.HomeFragment;
import es.clarify.clarify.Login.Login;
import es.clarify.clarify.Objects.PurchaseRemote;
import es.clarify.clarify.Objects.ScannedTag;
import es.clarify.clarify.Objects.ShoppingCartRemote;
import es.clarify.clarify.Objects.StoreLocal;
import es.clarify.clarify.Search.NfcIdentifyFragment;
import es.clarify.clarify.NFC.NfcUtility;
import es.clarify.clarify.ShoppingCart.ShoppingCart;
import es.clarify.clarify.Store.ShowStore;
import es.clarify.clarify.Store.StoreFragment;
import es.clarify.clarify.Utilities.Database;
import es.clarify.clarify.Utilities.GoogleUtilities;
import es.clarify.clarify.Utilities.Utilities;
import io.realm.Realm;
import io.realm.RealmConfiguration;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private NfcUtility nfcUtility = new NfcUtility();
    private TabLayout tabLayout;
    public ViewPager2 viewPager;
    public HomeFragment homeFragment;
    public StoreFragment storeFragment;
    private NfcIdentifyFragment nfcIdentifyFragment;
    private Database database;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private CircleImageView imgHeader;
    private CircleImageView profileImg;
    private TextView nameUser;
    private TextView emailUser;
    private FirebaseUser firebaseUser;
    private Utilities utilities;
    public List<Thread> threads;
    private ValueEventListener valueEventListenerStores;
    private ValueEventListener valueEventListenerShoppingCart;
    private ViewPageAdapter viewPageAdapter;
    private LinearLayout voiceControl;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        populate();

        setContentView(R.layout.activity_main);
        firebaseUser = new GoogleUtilities().getCurrentUser();
        database = new Database();

        Realm.init(this);
        RealmConfiguration config = new RealmConfiguration.Builder()
                .name(Realm.DEFAULT_REALM_NAME)
                .schemaVersion(0)
                .deleteRealmIfMigrationNeeded()
                .build();
        Realm.setDefaultConfiguration(config);

        utilities = new Utilities();

        Thread threadA = new Thread(new Runnable() {
            @Override
            public void run() {
                utilities.synchronizationWithFirebaseFirstLoginTags();
            }
        });
        threadA.run();

        Thread threadB = new Thread(new Runnable() {
            @Override
            public void run() {
                utilities.synchronizationWithFirebaseFirstLoginShoppingCart();
            }
        });
        threadB.run();

        valueEventListenerStores = utilities.createStoreListenerFirebase();
        Thread threadC = new Thread(new Runnable() {
            @Override
            public void run() {
                utilities.storeListenerFirebase(valueEventListenerStores);
            }
        });
        threadC.run();

        valueEventListenerShoppingCart = utilities.createListenerOwnShoppingCart();
        Thread threadD = new Thread(new Runnable() {
            @Override
            public void run() {
                utilities.listenerOwnShoppingCart(valueEventListenerShoppingCart);
            }
        });
        threadD.run();

        threads = Arrays.asList(threadA, threadB, threadC, threadD);

        // NFC instances
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        // Toolbar instances
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        profileImg = (CircleImageView) toolbar.findViewById(R.id.toolbar_profile_image);
        Glide.with(this).load(firebaseUser.getPhotoUrl()).into(profileImg);
        setSupportActionBar(toolbar);

        changeColor(R.color.colorPrimary);

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

        viewPager = (ViewPager2) findViewById(R.id.mainFrame);

        homeFragment = new HomeFragment();
        storeFragment = new StoreFragment(MainActivity.this);
        nfcIdentifyFragment = new NfcIdentifyFragment(viewPager);


        // Set navBar
        viewPageAdapter = new ViewPageAdapter(this, homeFragment, storeFragment, nfcIdentifyFragment);
        viewPager.setAdapter(viewPageAdapter);

        tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        TabLayoutMediator tabLayoutMediator = new TabLayoutMediator(
                tabLayout, viewPager, new TabLayoutMediator.OnConfigureTabCallback() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                switch (position) {
                    case 0: {
                        tab.setIcon(R.drawable.ic_home_black_24dp);
                        tab.setContentDescription("Inicio");
                        tab.getIcon().setTint(Color.parseColor("#FFFFFF"));
                        break;
                    }
                    case 1: {
                        tab.setIcon(R.drawable.ic_folder_black_24dp);
                        tab.setContentDescription("Almacenes");
                        tab.getIcon().setTint(Color.parseColor("#FFFFFF"));
                        break;
                    }
                    case 2: {
                        tab.setIcon(R.drawable.ic_explore_black_24dp);
                        tab.setContentDescription("Identificador");
                        tab.getIcon().setTint(Color.parseColor("#FFFFFF"));
                        break;
                    }
                }
            }
        }
        );
        tabLayoutMediator.attach();
        tabLayout.setBackgroundResource(R.drawable.bg_home);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setElevation(0);

        setNavigationViewListener();

        pendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, this.getClass())
                        .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        voiceControl = (LinearLayout) findViewById(R.id.start_voice_command);
        voiceControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                voiceAutomationMenu();
            }
        });

    }

    public void changeColor(int resourseColor) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(getApplicationContext(), resourseColor));
        }
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
        FirebaseDatabase databaseAux = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference1 = databaseAux.getReference().child(("private")).child(new GoogleUtilities().getCurrentUser().getUid()).child("stores");
        databaseReference1.removeEventListener(valueEventListenerStores);

        DatabaseReference databaseReference2 = databaseAux.getReference().child(("private")).child(new GoogleUtilities().getCurrentUser().getUid()).child("listaCompra");
        databaseReference2.removeEventListener(valueEventListenerShoppingCart);

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
                        threads.stream().forEach(x -> x.interrupt());
                        database.deleteAllDataFromDevice();
                        Intent intent = new Intent(getApplicationContext(), Login.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
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
        super.onNewIntent(intent);
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            setIntent(intent);
            nfcIdentifyFragment.resolveIntent(intent);
        } else {
            // TODO
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            checkVoiceControlMenu(requestCode, requestCode, data);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    public void voiceAutomationMenu() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Habla ahora");
        startActivityForResult(intent, 1);
    }

    public void checkVoiceControlMenu(int requestCode, int resultCode, @Nullable Intent data) {
        Boolean check = false;
        List<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
        String firstResult = results.get(0).toLowerCase();
        if (viewPager.getCurrentItem() == 1) {
            check = checkStore(firstResult);
        } else if (viewPager.getCurrentItem() == 2) {
            check = checkFind(firstResult);
        }
        if (!check) {
            if (checkContains(firstResult, Arrays.asList("almacén", "almacen"))) {
                viewPager.setCurrentItem(1);
            } else if (checkContains(firstResult, Arrays.asList("compra", "carrito", "carro", "cesta", "marcado", "seleccionado", "check"))) {
                Context context = MainActivity.this;
                Intent intent = new Intent(context, ShoppingCart.class);
                intent.putExtra("goToShare", false);
                context.startActivity(intent);
            } else if (checkContains(firstResult, Arrays.asList("invitado", "invitaciones", "invitación", "pendiente", "pendientes", "amigos"))) {
                Context context = MainActivity.this;
                Intent intent = new Intent(context, ShoppingCart.class);
                intent.putExtra("goToShare", true);
                context.startActivity(intent);
            } else if (checkContains(firstResult, Arrays.asList("buscar", "identificar", "identificame", "qué es", "que es", "que estoy viendo", "que tengo", "nfc", "etiqueta"))) {
                viewPager.setCurrentItem(2);
            } else if (checkContains(firstResult, Arrays.asList("home", "pantalla inicial", "inicio"))) {
                viewPager.setCurrentItem(0);
            } else {
                Toast.makeText(this, "Comando no reconocido, inténtelo de nuevo.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private Boolean checkFind(String firstResult) {
        Boolean res = false;
        if (nfcIdentifyFragment != null && nfcIdentifyFragment.myDialog_info != null) {
            if (checkContains(firstResult, Arrays.asList("abrir", "escanea", "escaner", "identificar", "qué es", "indentifica")) && !firstResult.contains("carrito") && !firstResult.contains("cesta") && !firstResult.contains("compra")) {
                nfcIdentifyFragment.myDialog_info.show();
                res = true;
            }
        }
        return res;
    }

    private Boolean checkStore(String firstResult) {
        Boolean res = false;
        if (firstResult.contains("abrir") && !firstResult.contains("carrito") && !firstResult.contains("cesta") && !firstResult.contains("compra")) {
            if (storeFragment != null && storeFragment.listStoreLocal != null && storeFragment.listStoreLocal.size() > 0) {
                List<String> almacenes = storeFragment.listStoreLocal.stream().map(StoreLocal::getName).collect(Collectors.toList());
                Boolean open = false;
                for (String name : almacenes) {
                    if (firstResult.contains(name.toLowerCase())) {
                        Toast.makeText(this, "¡" + name + " abierta!", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(this, ShowStore.class);
                        intent.putExtra("store_name", name);
                        this.startActivity(intent);
                        open = true;
                        break;
                    }
                }
                if (!open) {
                    String almacen = firstResult.replace("abrir", "").trim().split(" ")[0];
                    Toast.makeText(this, "Vaya, no tienes ningún almacén con el nombre " + almacen, Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(this, "Vaya, no tienes ningún almacén", Toast.LENGTH_LONG).show();
            }
            res = true;
        }
        return res;
    }

    public Boolean checkContains(String result, List<String> candidates) {
        Boolean res = false;
        for (String elem : candidates) {
            if (result.contains(elem)) {
                res = true;
                break;
            }
        }
        return res;
    }

    public void populate() {
        // Firebase instances

        ///////////////////   CREATE INSTANCE    /////////////////////////////////////////////////////////////////////////
        FirebaseDatabase database2 = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference2 = database2.getReference("public");
        ScannedTag scannedTagPush1 = new ScannedTag("41521", "Hacendado", "Leche Semidesnatada 1L", false, "verde y blanca", "2020-12-31", "aldkmfalkdmflakdml", "http://i.imgur.com/aN80yXm.png", "Frigorífico", 2.34);
        ScannedTag scannedTagPush2 = new ScannedTag("41522", "Hacendado", "Pizza mediterránea", false, "None", "2020-06-31", "aldkmfalkdmflakdml", "https://i.imgur.com/VJA0WIX.png", "Frigorífico", 2.36);
        ScannedTag scannedTagPush3 = new ScannedTag("41523", "Hacendado", "Paté Iberico 160 gramos", false, "None", "2020-06-31", "aldkmfalkdmflakdml", "https://i.imgur.com/HRvAsFR.png", "Despensa", 2.36);
        ScannedTag scannedTagPush4 = new ScannedTag("41524", "Hacendado", "Paté Iberico 260 gramos", false, "None", "2020-06-31", "aldkmfalkdmflakdml", "https://i.imgur.com/HRvAsFR.png", "Despensa", 2.36);
        ScannedTag scannedTagPush5 = new ScannedTag("41525", "Hacendado", "Paté Iberico 360 gramos", false, "None", "2020-06-31", "aldkmfalkdmflakdml", "https://i.imgur.com/HRvAsFR.png", "Despensa", 2.36);
        ScannedTag scannedTagPush6 = new ScannedTag("41526", "Hacendado", "Paté Iberico 460 gramos", false, "None", "2020-06-31", "aldkmfalkdmflakdml", "https://i.imgur.com/HRvAsFR.png", "Despensa", 2.36);
        ScannedTag scannedTagPush7 = new ScannedTag("41527", "Hacendado", "Paté Iberico 560 gramos", false, "None", "2020-06-31", "aldkmfalkdmflakdml", "https://i.imgur.com/HRvAsFR.png", "Despensa", 2.36);
        ScannedTag scannedTagPush8 = new ScannedTag("41528", "Hacendado", "Paté Iberico 660 gramos", false, "None", "2020-06-31", "aldkmfalkdmflakdml", "https://i.imgur.com/HRvAsFR.png", "Despensa", 2.36);
        ScannedTag scannedTagPush9 = new ScannedTag("41529", "Hacendado", "Paté Iberico 760 gramos", false, "None", "2020-06-31", "aldkmfalkdmflakdml", "https://i.imgur.com/HRvAsFR.png", "Despensa", 2.36);
        ScannedTag scannedTagPush10 = new ScannedTag("41530", "Hacendado", "Paté Iberico 860 gramos", false, "None", "2020-06-31", "aldkmfalkdmflakdml", "https://i.imgur.com/HRvAsFR.png", "Despensa", 2.36);
        ScannedTag scannedTagPush11 = new ScannedTag("41531", "Hacendado", "Paté Iberico 960 gramos", false, "None", "2020-06-31", "aldkmfalkdmflakdml", "https://i.imgur.com/HRvAsFR.png", "Despensa", 2.36);
        ScannedTag scannedTagPush12 = new ScannedTag("41532", "Hacendado", "Paté Iberico 1060 gramos", false, "None", "2020-06-31", "aldkmfalkdmflakdml", "https://i.imgur.com/HRvAsFR.png", "Despensa", 2.36);
//        databaseReference2.child("carlosjavier@gmail,com").child("stores").child("wardrobe").push().setValue(scannedTagPush);
        databaseReference2.child("tags").push().setValue(scannedTagPush1);
        databaseReference2.child("tags").push().setValue(scannedTagPush2);
        databaseReference2.child("tags").push().setValue(scannedTagPush3);
        databaseReference2.child("tags").push().setValue(scannedTagPush4);
        databaseReference2.child("tags").push().setValue(scannedTagPush5);
        databaseReference2.child("tags").push().setValue(scannedTagPush6);
        databaseReference2.child("tags").push().setValue(scannedTagPush7);
        databaseReference2.child("tags").push().setValue(scannedTagPush8);
        databaseReference2.child("tags").push().setValue(scannedTagPush9);
        databaseReference2.child("tags").push().setValue(scannedTagPush10);
        databaseReference2.child("tags").push().setValue(scannedTagPush11);
        databaseReference2.child("tags").push().setValue(scannedTagPush12);
//        String uid = new GoogleUtilities().getCurrentUser().getUid();
//        PurchaseRemote purchaseRemote1 = new PurchaseRemote(1, -1, uid, "Pate de cerdo", false);
//        PurchaseRemote purchaseRemote2 = new PurchaseRemote(2, -1, uid, "Camiseta de diario", false);
//        PurchaseRemote purchaseRemote3 = new PurchaseRemote(3, -1, uid, "PC HP", false);
//        List<PurchaseRemote> listPurcharse = Arrays.asList(purchaseRemote1, purchaseRemote2, purchaseRemote3);
//        ShoppingCartRemote shoppingCartRemote = new ShoppingCartRemote(uid, new Date(), true, listPurcharse, new ArrayList<>(), new ArrayList<>());
//        FirebaseDatabase databaseShoppingCart = FirebaseDatabase.getInstance();
//        DatabaseReference databaseReferenceShoppingCart = databaseShoppingCart.getReference("private").child(uid).child("listaCompra");
//        databaseReferenceShoppingCart.push().setValue(shoppingCartRemote);
        ///////////////////   CREATE INSTANCE    /////////////////////////////////////////////////////////////////////////
    }

}