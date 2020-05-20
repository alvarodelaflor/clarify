package es.clarify.clarify.ShoppingCart;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import de.hdodenhof.circleimageview.CircleImageView;
import es.clarify.clarify.Objects.FriendRemote;
import es.clarify.clarify.Objects.PurchaseLocal;
import es.clarify.clarify.Objects.PurchaseRemote;
import es.clarify.clarify.Objects.ShoppingCartRemote;
import es.clarify.clarify.Objects.UserData;
import es.clarify.clarify.R;
import es.clarify.clarify.Utilities.Database;
import es.clarify.clarify.Utilities.GoogleUtilities;
import es.clarify.clarify.Utilities.Utilities;

public class ShoppingCartFriend extends AppCompatActivity {

    private Toolbar toolbar;
    private String uid;
    private List<PurchaseRemote> mPurchase;
    private UserData userData;
    private List<FriendRemote> acessList;
    private RecyclerView recycler;
    private RecyclerViewAdapterShoppingCartFriend adapter;
    private LinearLayout lyNoPurchasesFriend;
    private FloatingActionButton floatingActionButton;
    private Button buttonInitial;
    private Boolean hideFloatingButton;
    private SearchView searchView;
    private ValueEventListener listenerOwner;
    private ValueEventListener listener;
    private CircleImageView imgOwner;
    private TextView ownerName;
    private TextView sizeAllows;
    private LinearLayout showAccess;
    private Dialog dialog;
    private LinearLayout closeDialog;
    private ViewPager2 accessListViewPager;
    private FriendAdapterRemote adapterFriend;
    private Boolean voiceControl = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_cart_friend);

        // Init Toolbar
        toolbar = (Toolbar) findViewById(R.id.tb_sc_friend);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        changeColor(R.color.colorPrimary);

        uid = getIntent().getStringExtra("uid");

        hideFloatingButton = true;
        lyNoPurchasesFriend = (LinearLayout) findViewById(R.id.ly_no_purchase_friend);
        buttonInitial = (Button) findViewById(R.id.btn_add_purchase);
        floatingActionButton = (FloatingActionButton) findViewById(R.id.fab_add_purchase);
        floatingActionButton.hide();

        mPurchase = new ArrayList<>();
        userData = new UserData("username", "user@mail.com", null, null, null, "");
        ownerName = (TextView) findViewById(R.id.name_of_owner);
        sizeAllows = (TextView) findViewById(R.id.size_allows);
        acessList = new ArrayList<>();
        dialog = new Dialog(ShoppingCartFriend.this);
        dialog.setContentView(R.layout.dialog_user_with_access);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow()
                .setLayout(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
        showAccess = (LinearLayout) findViewById(R.id.show_access);
        showAccess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.show();
            }
        });
        listenerOwner = createValueEventListenerOwner();
        enableListenerOwner(listenerOwner);
        recycler = (RecyclerView) findViewById(R.id.rv_sc_friend);
        adapter = new RecyclerViewAdapterShoppingCartFriend(this, mPurchase);
        recycler.setLayoutManager(new GridLayoutManager(getApplication(), 1));
        recycler.setAdapter(adapter);

        imgOwner = (CircleImageView) findViewById(R.id.owner_img);

        listener = createValueEventListener();
        enableListener(listener);

        adapterFriend = new FriendAdapterRemote(acessList, getApplication());
        closeDialog = dialog.findViewById(R.id.ly_close_dialog);
        closeDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        accessListViewPager = (ViewPager2) dialog.findViewById(R.id.vp2_user_with_access);
        accessListViewPager.setAdapter(adapterFriend);
        accessListViewPager.setClipToPadding(false);
        accessListViewPager.setClipChildren(false);
        accessListViewPager.setOffscreenPageLimit(3);
        accessListViewPager.getChildAt(0).setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);

        CompositePageTransformer compositePageTransformer = new CompositePageTransformer();
        compositePageTransformer.addTransformer(new MarginPageTransformer(40));
        compositePageTransformer.addTransformer(new ViewPager2.PageTransformer() {
            @Override
            public void transformPage(@NonNull View page, float position) {
                float r = 1 - Math.abs(position);
                page.setScaleY(0.95f + r * 0.15f);
                page.setScaleX(0.95f + r * 0.15f);
            }
        });
        accessListViewPager.setPageTransformer(compositePageTransformer);
    }

    private void enableListenerOwner(ValueEventListener listenerOwner) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = database.getReference().child(("private")).child(uid);
        databaseReference
                .addValueEventListener(listenerOwner);
    }

    private ValueEventListener createValueEventListenerOwner() {
        return new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Boolean check = true;
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    if (data.getKey().equals("user_profile")) {
                        userData.setName(data.child("name").getValue(String.class).trim().split(" ")[0]);
                        userData.setPhoto(data.child("photo").getValue(String.class));
                        ownerName.setText(userData.getName());
                        if (userData.getPhoto()!= null) {
                            Glide.with(ShoppingCartFriend.this).load(userData.getPhoto()).into(imgOwner);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
    }

    private void enableListener(ValueEventListener listener) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = database.getReference().child(("private")).child(uid).child("listaCompra");
        databaseReference
                .addValueEventListener(listener);
    }

    private ValueEventListener createValueEventListener() {
        return new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Boolean check = true;
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    if (check) {
                        ShoppingCartRemote shoppingCartRemote = data.getValue(ShoppingCartRemote.class);
                        if (shoppingCartRemote != null) {
                            if (shoppingCartRemote.getAllowUsers() != null) {
                                List<FriendRemote> allowUsers = shoppingCartRemote.getAllowUsers();
                                Boolean checkAllow = allowUsers.stream()
                                        .filter(x -> x.getUid().equals(new GoogleUtilities().getCurrentUser().getUid() + "access"))
                                        .findFirst()
                                        .orElse(null) != null
                                        ?
                                        true : false;
                                if (checkAllow) {
                                    sizeAllows.setText(String.valueOf(allowUsers.size()));
                                    checkAccessList(allowUsers);
                                } else {
                                    dialog.dismiss();
                                    finish();
                                }
                            } else {
                                dialog.dismiss();
                                finish();
                            }
                            List<PurchaseRemote> purchaseRemoteFirebase = shoppingCartRemote.getPurcharse() != null ? shoppingCartRemote.getPurcharse() : new ArrayList<>();
                            purchaseRemoteFirebase.sort(Comparator.comparing(PurchaseRemote::getIdFirebase).reversed());
                            check(purchaseRemoteFirebase);
                            updateLayout();
                        }
                    }
                    check = false;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
    }

    private void checkAccessList(List<FriendRemote> allowUsers) {
        checkNewUser(allowUsers);
        checkRemoveUser(allowUsers);
        checkEditUser(allowUsers);
    }

    private void checkEditUser(List<FriendRemote> allowUsers) {
        if (acessList != null && allowUsers != null && acessList.size() == allowUsers.size()) {
            IntStream.range(0, acessList.size())
                    .filter(x -> !acessList.get(x).equals(allowUsers.get(x)))
                    .boxed()
                    .forEach(x -> updateUser(x, allowUsers.get(x)));
        }
    }

    private void updateUser(Integer index, FriendRemote friendRemote) {
        FriendRemote friendRemoteAux = acessList.stream().filter(x -> x.getUid().equals(friendRemote.getUid())).findFirst().orElse(null);
        if (friendRemoteAux != null) {
            acessList.remove(friendRemoteAux);
            acessList.add(index, friendRemote);
            adapterFriend.notifyDataSetChanged();
        }
    }

    private void checkRemoveUser(List<FriendRemote> allowUsers) {
        List<Integer> toDeleteIndex = IntStream.range(0, acessList.size())
                .filter(x -> !allowUsers.contains(acessList.get(x)))
                .boxed()
                .collect(Collectors.toList());
        toDeleteIndex
                .forEach(x -> removeUser(acessList.get(x)));
    }

    private void removeUser(FriendRemote friendRemote) {
        Integer index = acessList.indexOf(friendRemote);
        if (index != null) {
            acessList.remove(friendRemote);
            adapterFriend.notifyItemRemoved(index);
        }
    }

    private void checkNewUser(List<FriendRemote> allowUsers) {
        IntStream.range(0, allowUsers.size())
                .filter(x -> acessList.stream().map(FriendRemote::getUid).noneMatch(y -> allowUsers.get(x).getUid().equals(y)))
                .boxed()
                .forEach(x -> addUser(x, allowUsers.get(x)));
    }

    private void addUser(Integer index, FriendRemote friendRemote) {
        acessList.add(index, friendRemote);
        adapterFriend.notifyItemInserted(index);
    }

    private void updateLayout() {
        if (mPurchase.size() < 1) {
            if (hideFloatingButton) {
                floatingActionButton.hide();
                buttonInitial.setVisibility(View.VISIBLE);
            }
            lyNoPurchasesFriend.setVisibility(View.VISIBLE);
            recycler.setVisibility(View.GONE);
        } else {
            if (!hideFloatingButton) {
                floatingActionButton.show();
            }
            buttonInitial.setVisibility(View.GONE);
            recycler.setVisibility(View.VISIBLE);
            lyNoPurchasesFriend.setVisibility(View.GONE);
        }
    }

    private void check(List<PurchaseRemote> purchaseRemoteFirebase) {
        checkDeletePurchases(purchaseRemoteFirebase);
        checkNewPurchases(purchaseRemoteFirebase);
        checkChangesPurchases(purchaseRemoteFirebase);
    }

    private void checkChangesPurchases(List<PurchaseRemote> purchaseRemoteFirebase) {
        if (purchaseRemoteFirebase != null && mPurchase != null && purchaseRemoteFirebase.size() == mPurchase.size()) {
            IntStream
                    .range(0, mPurchase.size())
                    .filter(x -> purchaseRemoteFirebase.stream().anyMatch(y -> y.getIdFirebase() == mPurchase.get(x).getIdFirebase() && y.getCheck() != mPurchase.get(x).getCheck()))
                    .boxed()
                    .forEach(z -> applyChangeStatus(z, !mPurchase.get(z).getCheck()));
        }
    }

    private void applyChangeStatus(Integer x, Boolean check) {
        mPurchase.get(x).setCheck(check);
        adapter.notifyDataSetChanged();
    }

    private void checkNewPurchases(List<PurchaseRemote> purchaseRemoteFirebase) {
        if (purchaseRemoteFirebase != null) {
            List<PurchaseRemote> toAdd = purchaseRemoteFirebase.stream()
                    .filter(x -> mPurchase.stream().noneMatch(y -> y.getIdFirebase() == x.getIdFirebase()))
                    .collect(Collectors.toList());
            toAdd.forEach(x -> addToRecycler(purchaseRemoteFirebase.indexOf(x), x));
        }
    }

    private void addToRecycler(int index, PurchaseRemote purchaseRemote) {
        if (!mPurchase.contains(purchaseRemote)) {
            mPurchase.add(index, purchaseRemote);
            adapter.notifyItemInserted(index);
        }
        if (mPurchase.size() > 0) {
            hideFloatingButton = false;
        }
    }

    private void checkDeletePurchases(List<PurchaseRemote> purchaseRemoteFirebase) {
        if (purchaseRemoteFirebase != null) {
            IntStream
                    .range(0, mPurchase.size())
                    .filter(x -> purchaseRemoteFirebase.stream().map(PurchaseRemote::getIdFirebase).noneMatch(y -> mPurchase.get(x).getIdFirebase() == y))
                    .boxed()
                    .sorted(Comparator.reverseOrder())
                    .forEach(x -> deleteFromRecycler(x));

        } else {
            mPurchase.stream().forEach(x -> adapter.notifyItemRemoved(mPurchase.indexOf(x)));
            mPurchase = new ArrayList<>();
        }
        if (mPurchase.size() < 1) {
            hideFloatingButton = true;
        }
    }

    private void deleteFromRecycler(int index) {
        PurchaseRemote purchaseRemote = mPurchase.size() > index ? mPurchase.get(index) : null;
        if (purchaseRemote != null) {
            mPurchase.remove(purchaseRemote);
            adapter.notifyItemRemoved(index);
        }
    }

    public void changeColor(int resourseColor) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(getApplicationContext(), resourseColor));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_shopping_cart_2, menu);
        MenuItem itemAdd = menu.findItem(R.id.search_icon);
        searchView = (SearchView) itemAdd.getActionView();
        searchView.setQueryHint("Nombre del producto");
        itemAdd.setVisible(false);
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                int id = 1000 + mPurchase.stream().map(PurchaseRemote::getIdFirebase).findFirst().orElse(0);
                GoogleUtilities googleUtilities = new GoogleUtilities();
                List<String> lastUpdate = Arrays.asList(googleUtilities.getCurrentUser().getPhotoUrl().toString(), googleUtilities.getCurrentUser().getDisplayName());
                FriendRemote friendRemote = new FriendRemote();
                friendRemote.setPhoto(googleUtilities.getCurrentUser().getPhotoUrl().toString());
                friendRemote.setName(googleUtilities.getCurrentUser().getDisplayName());
                googleUtilities.savePurchase(query, id, -1, false, uid, friendRemote);
                googleUtilities.searchUserAndSendNotification(uid, googleUtilities.getCurrentUser().getDisplayName() + " ha añadido " + query, "Nuevo producto", "ShoppingCart.class-false");

                searchView.clearFocus();
                itemAdd.collapseActionView();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        buttonInitial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                itemAdd.expandActionView();
                hideFloatingButton = true;
                floatingActionButton.hide();
                buttonInitial.setVisibility(View.INVISIBLE);
            }
        });
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                itemAdd.expandActionView();
                hideFloatingButton = true;
                floatingActionButton.hide();
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.microphone_2:
                voiceAutomationMenu();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!voiceControl) {
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference databaseReference1 = database.getReference().child(("private")).child(uid).child("listaCompra");
            databaseReference1.removeEventListener(listener);
            DatabaseReference databaseReference2 = database.getReference().child(("private")).child(uid);
            databaseReference2.removeEventListener(listenerOwner);
            finish();
        }
    }

    public void voiceAutomationMenu() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Habla ahora para añadir");
        voiceControl = true;
        this.startActivityForResult(intent, 50000);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        voiceControl = false;
        if (requestCode == 50000 && resultCode == RESULT_OK && data != null) {
            checkVoiceControlMenu(requestCode, requestCode, data);
        }
    }

    public void checkVoiceControlMenu(int requestCode, int resultCode, @Nullable Intent data) {
        Boolean check = false;
        List<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
        String firstResult = results.get(0).toLowerCase();
        if (checkContains(firstResult, Arrays.asList("propietari", "dueñ", "creador"))) {
            Toast.makeText(this, "El dueño de esta lista es " + userData.getName(), Toast.LENGTH_LONG).show();
            check = true;
        }else if (checkContains(firstResult, Arrays.asList("número", "cuant", "cuánt")) && checkContains(firstResult, Arrays.asList("acceso", "persona", "usuario"))) {
            Toast.makeText(this, "El número de personas que tienen acceso a esta lista es " + sizeAllows.getText(), Toast.LENGTH_LONG).show();
            check = true;
        } else if (checkContains(firstResult, Arrays.asList("ver", "quien", "quién")) && checkContains(firstResult, Arrays.asList("usuario", "persona", "acceso", "permiso"))) {
            dialog.show();
            check = true;
        }
        if (!check) {
            if (checkContains(firstResult, Arrays.asList("añád", "añad", "inserta", "mete", "méte"))) {
                String query = getProduct(firstResult, Arrays.asList("añád", "añad", "insert", "mete", "méte", "a la lista", "a la cesta", "a los productos", "en la lista", "en la cesta"));
                if (query.length() > 0) {
                    int id = 1000 + mPurchase.stream().map(PurchaseRemote::getIdFirebase).findFirst().orElse(0);
                    GoogleUtilities googleUtilities = new GoogleUtilities();
                    FriendRemote friendRemote = new FriendRemote();
                    friendRemote.setName(googleUtilities.getCurrentUser().getDisplayName());
                    friendRemote.setPhoto(googleUtilities.getCurrentUser().getPhotoUrl().toString());
                    new GoogleUtilities().savePurchase(query, id, -1, false, uid, friendRemote);
                    Toast.makeText(this, query + " se ha añadido", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "No se ha reconocido el nombre del producto que ha dicho\nInténtelo de nuevo.", Toast.LENGTH_LONG).show();
                }
            } else if (checkContains(firstResult, Arrays.asList("borr", "borrá", "elimin", "quit"))) {
                if (mPurchase != null && mPurchase.size() > 0) {
                    List<PurchaseRemote> toDeletePurchases = new ArrayList<>();
                    if (checkContains(firstResult, Arrays.asList("posición"))) {
                        Integer number = getNumber(firstResult);
                        if (number != null && number > 0 && mPurchase.size() > (number - 1) && mPurchase.get(number - 1) != null) {
                            toDeletePurchases.add(mPurchase.get(number - 1));
                        } else {
                            Toast.makeText(this, "No hay ningún producto en esa posición", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        String queryPurchase = getProduct(firstResult, Arrays.asList("borr", "borrá", "elimin", "quit", "el producto", "elemento", "de la lista", "de la cesta", "de los productos"));
                        toDeletePurchases = mPurchase.stream().filter(x -> x.getName().contains(queryPurchase)).collect(Collectors.toList());
                    }

                    for (PurchaseRemote elem : toDeletePurchases) {
                        String name = elem.getName();
                        PurchaseLocal purchaseLocal = new PurchaseLocal();
                        purchaseLocal.setIdFirebase(elem.getIdFirebase());
                        new GoogleUtilities().deletePurchaseFromRemote(purchaseLocal, false, elem.getIdShoppingCart());
                        Toast.makeText(this, name + " se ha eliminado", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(this, "Su lista está vacía, no puede borrar nada", Toast.LENGTH_LONG).show();
                }
            } else if (checkContains(firstResult, Arrays.asList("deselecciona", "desmarca"))) {
                if (mPurchase != null && mPurchase.size() > 0) {
                    List<PurchaseRemote> toCheckPurchases = new ArrayList<>();
                    if (checkContains(firstResult, Arrays.asList("posición"))) {
                        Integer number = getNumber(firstResult);
                        if (number != null && number > 0 && mPurchase.size() > (number - 1) && mPurchase.get(number - 1) != null) {
                            toCheckPurchases.add(mPurchase.get(number - 1));
                        } else {
                            Toast.makeText(this, "No hay ningún producto en esa posición", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        String queryPurchase = getProduct(firstResult, Arrays.asList("desselecciona", "desmarca", "producto", "elemento", "el producto", "el elemento", "de la lista", "de la cesta", "de los productos"));
                        toCheckPurchases = mPurchase.stream().filter(x -> x.getName().contains(queryPurchase)).collect(Collectors.toList());
                    }

                    for (PurchaseRemote elem : toCheckPurchases) {
                        String name = elem.getName();
                        if (!elem.getCheck()) {
                            Toast.makeText(this, "¡" + name + " ya estaba desmarcado!", Toast.LENGTH_LONG).show();
                        } else {
                            PurchaseLocal purchaseLocal = new PurchaseLocal();
                            purchaseLocal.setIdFirebase(elem.getIdFirebase());
                            purchaseLocal.setIdShoppingCart(elem.getIdShoppingCart());
                            new GoogleUtilities().changeCheckStatusFromLocal(purchaseLocal, false, purchaseLocal.getIdShoppingCart());
                            Toast.makeText(this, name + " se ha desmarcado como seleccionado", Toast.LENGTH_LONG).show();
                        }
                    }
                } else {
                    Toast.makeText(this, "Su lista está vacía, no puede desmarcar nada", Toast.LENGTH_LONG).show();
                }
            } else if (checkContains(firstResult, Arrays.asList("selecciona", "marca"))) {
                if (mPurchase != null && mPurchase.size() > 0) {
                    List<PurchaseRemote> toCheckPurchases = new ArrayList<>();
                    if (checkContains(firstResult, Arrays.asList("posición"))) {
                        Integer number = getNumber(firstResult);
                        if (number != null && number > 0 && mPurchase.size() > (number - 1) && mPurchase.get(number - 1) != null) {
                            toCheckPurchases.add(mPurchase.get(number - 1));
                        } else {
                            Toast.makeText(this, "No hay ningún producto en esa posición", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        String queryPurchase = getProduct(firstResult, Arrays.asList("selecciona", "marca", "producto", "elemento", "el producto", "el elemento", "de la lista", "de la cesta", "de los productos"));
                        toCheckPurchases = mPurchase.stream().filter(x -> x.getName().contains(queryPurchase)).collect(Collectors.toList());
                    }

                    for (PurchaseRemote elem : toCheckPurchases) {
                        String name = elem.getName();
                        if (elem.getCheck()) {
                            Toast.makeText(this, "¡" + name + " ya estaba marcado!", Toast.LENGTH_LONG).show();
                        } else {
                            PurchaseLocal purchaseLocal = new PurchaseLocal();
                            purchaseLocal.setIdFirebase(elem.getIdFirebase());
                            purchaseLocal.setIdShoppingCart(elem.getIdShoppingCart());
                            new GoogleUtilities().changeCheckStatusFromLocal(purchaseLocal, true, purchaseLocal.getIdShoppingCart());
                            Toast.makeText(this, name + " se ha marcado como seleccionado", Toast.LENGTH_LONG).show();
                        }
                    }
                } else {
                    Toast.makeText(this, "Su lista está vacía, no puede marcar nada", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(this, "Comando no reconocido, inténtelo de nuevo.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private Integer getNumber(String firstResult) {
        Integer res = null;
        List<String> aux = Arrays.stream(firstResult.split(" ")).collect(Collectors.toList());
        Boolean isNumber = false;
        for (String elem : aux) {
            try {
                res = Integer.parseInt(elem);
                isNumber = true;
            } catch (NumberFormatException e) {
                isNumber = false;
            }
            if (isNumber) {
                break;
            }
        }
        return res;
    }

    public String getProduct(String command, List<String> toDelete) {
        String res = command;
        List<String> aux = Arrays.stream(command.split(" ")).filter(x -> checkContains(x, toDelete)).collect(Collectors.toList());
        for (String elem : aux) {
            res = res.replace(elem, "");
        }
        List<String> toDeleteAux = toDelete.stream().filter(x -> x.split(" ").length > 0).collect(Collectors.toList());
        for (String elem : toDeleteAux) {
            res = res.replace(elem, "");
        }
        res = res.trim();
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

}
