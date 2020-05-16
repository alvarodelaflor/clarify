package es.clarify.clarify.ShoppingCart;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
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
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityWindowInfo;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import es.clarify.clarify.Objects.FriendLocal;
import es.clarify.clarify.Objects.FriendRemote;
import es.clarify.clarify.Objects.PurchaseLocal;
import es.clarify.clarify.Objects.PurchaseRemote;
import es.clarify.clarify.Objects.ShoppingCartRemote;
import es.clarify.clarify.R;
import es.clarify.clarify.Utilities.Database;
import es.clarify.clarify.Utilities.GoogleUtilities;
import es.clarify.clarify.Utilities.Utilities;

public class ShoppingCart extends AppCompatActivity {

    private Toolbar toolbar;
    List<PurchaseLocal> mData;
    RecyclerViewAdapterShoppingCart recyclerViewAdapter;
    private RecyclerView myRecyclerView;
    private RecyclerView myRecyclerViewFriends;
    private Database realmDatabase;
    private LinearLayout noPurchase;
    private LinearLayout noFriendsInvitation;
    private FloatingActionButton addListButton;
    private Button addButtonInitial;
    private SearchView searchView;
    Boolean hideFloatingButton = false;
    private CardView cardView;
    private LinearLayout deleteAll;
    private View view;
    private Dialog dialog;
    private Dialog dialogShareOption;
    private EditText email;
    Button confirmDelete;
    private ImageView closeDialog;
    private List<FriendLocal> myAccessList;
    private FriendAdapter myFriendAccessAdapter;
    private TextView textViewPager2;
    private ViewPager2 myAccessListViewPager;
    private CardView cardViewDialogShare;
    private ScrollView scrollViewPersonal;
    private ScrollView scrollViewShare;
    private LinearLayout changeToShare;
    private LinearLayout changeToPersonal;
    private List<FriendLocal> mDataFriendsInvitation;
    private RecyclerViewAdapterFriendsInvitation recyclerViewAdapterFriendsInvitation;
    private LinearLayout shareList;
    private Boolean goToShare = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_cart);

        goToShare = getIntent().getBooleanExtra("goToShare", false);

        scrollViewShare = (ScrollView) findViewById(R.id.card_view_list_share_carts);
        scrollViewPersonal = (ScrollView) findViewById(R.id.card_view_list_stores);
        changeToShare = (LinearLayout) findViewById(R.id.go_to_share);
        changeToPersonal = (LinearLayout) findViewById(R.id.go_to_personal);
        shareList = (LinearLayout) findViewById(R.id.share_my_list);

        // Init Toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar_shopping_card);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        changeColor(R.color.colorPrimary);

        deleteAll = (LinearLayout)findViewById(R.id.delete_all_ly);
        view = (View) findViewById(R.id.view_1);

        realmDatabase = new Database();
        List<PurchaseLocal> mDataFromLocalAux1 = realmDatabase.getAllPurchaseLocalOwnerLogin();
        mDataFromLocalAux1.sort(Comparator.comparing(PurchaseLocal::getIdFirebase).reversed());
        mData = new ArrayList<>(mDataFromLocalAux1);
        myRecyclerView = (RecyclerView) findViewById(R.id.purchase_recyclerview);
        recyclerViewAdapter = new RecyclerViewAdapterShoppingCart(this, mData);
        myRecyclerView.setLayoutManager(new GridLayoutManager(getApplication(), 1));
        myRecyclerView.setAdapter(recyclerViewAdapter);

        List<FriendLocal> friendsInvitationAux1 = realmDatabase.getAllFriendsInvitation();
        mDataFriendsInvitation = new ArrayList<>(friendsInvitationAux1);
        myRecyclerViewFriends = (RecyclerView) findViewById(R.id.share_list_recyclerview);
        recyclerViewAdapterFriendsInvitation = new RecyclerViewAdapterFriendsInvitation(this, mDataFriendsInvitation);
        myRecyclerViewFriends.setLayoutManager(new GridLayoutManager(getApplication(), 1));
        myRecyclerViewFriends.setAdapter(recyclerViewAdapterFriendsInvitation);

        noPurchase = (LinearLayout) findViewById(R.id.no_purchase);
        noFriendsInvitation = (LinearLayout) findViewById(R.id.no_share_list);

        addListButton = (FloatingActionButton) findViewById(R.id.add_item);
        addButtonInitial = (Button) findViewById(R.id.add_item_initial);

        cardView = (CardView)findViewById(R.id.card_view_stores);

        dialog = new Dialog(ShoppingCart.this);
        dialog.setContentView(R.layout.dialog_alert_delete);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow()
                .setLayout(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        dialogShareOption = new Dialog(ShoppingCart.this);
        dialogShareOption.setContentView(R.layout.dialog_edit_share);
        dialogShareOption.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialogShareOption.getWindow()
                .setLayout(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
        cardViewDialogShare = dialogShareOption.findViewById(R.id.id_card_view_share_option);

        closeDialog = dialogShareOption.findViewById(R.id.close_dialog);
        closeDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogShareOption.dismiss();
            }
        });
        textViewPager2 = dialogShareOption.findViewById(R.id.hello_msg_3);
        myAccessListViewPager = dialogShareOption.findViewById(R.id.friends);
        myAccessList = realmDatabase.getAccessListUserLogin();
        myFriendAccessAdapter = new FriendAdapter(myAccessList, getApplication());
        myAccessListViewPager.setAdapter(myFriendAccessAdapter);
        myAccessListViewPager.setClipToPadding(false);
        myAccessListViewPager.setClipChildren(false);
        myAccessListViewPager.setOffscreenPageLimit(3);
        myAccessListViewPager.getChildAt(0).setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);

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
        myAccessListViewPager.setPageTransformer(compositePageTransformer);

        confirmDelete = (Button)dialog.findViewById(R.id.button_cancel_delete_all);
        email = (EditText)dialogShareOption.findViewById(R.id.email);
        deleteAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.show();
            }
        });
        confirmDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                Boolean check = realmDatabase.deleteAllPurchaseFromLocal(new GoogleUtilities().getCurrentUser().getUid());
            }
        });

        changeToPersonal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeScrollViewMode(false);
            }
        });

        changeToShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeScrollViewMode(true);
            }
        });

        if (goToShare) {
            changeScrollViewMode(true);
        }

        shareList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogShareOption.show();
            }
        });

        updateData();
        updateNoPurchase();
        updateNoShareList();
        updateVisibilityViewPager2();
    }

    public void changeColor(int resourseColor) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(getApplicationContext(), resourseColor));
        }
    }

    private void changeScrollViewMode(Boolean mode) {
        if (mode) {
            scrollViewPersonal.setVisibility(View.GONE);
            scrollViewShare.setVisibility(View.VISIBLE);
            setHideFloatingButton(true);
            addListButton.hide();
        } else {
            scrollViewPersonal.setVisibility(View.VISIBLE);
            scrollViewShare.setVisibility(View.GONE);
            if (mData.size() > 0) {
                setHideFloatingButton(false);
                addListButton.show();
            }
        }
    }

    public void updateNoShareList() {
        if (mDataFriendsInvitation.size() < 1) {
            noFriendsInvitation.setVisibility(View.VISIBLE);
        } else {
            noFriendsInvitation.setVisibility(View.GONE);
        }
    }

    public void updateNoPurchase() {
        if (mData.size() < 1) {
            view.setVisibility(View.GONE);
            deleteAll.setVisibility(View.GONE);
            if (!hideFloatingButton) {
                noPurchase.setVisibility(View.VISIBLE);
                addButtonInitial.setVisibility(View.VISIBLE);
            }
            addListButton.hide();
        } else {
            view.setVisibility(View.VISIBLE);
            deleteAll.setVisibility(View.VISIBLE);
            noPurchase.setVisibility(View.GONE);
            if (!hideFloatingButton) {
                addListButton.show();
            }
        }
    }

    public void updateData() {
        List<PurchaseLocal> mDataFromLocalAux2 = realmDatabase.getAllPurchaseLocalOwnerLogin();
        mDataFromLocalAux2.sort(Comparator.comparing(PurchaseLocal::getIdFirebase).reversed());
        List<PurchaseLocal> mDataAux = new ArrayList<>(mDataFromLocalAux2);
        List<FriendLocal> accessLocal = realmDatabase.getAccessListUserLogin();
        List<FriendLocal> invitationLocal = realmDatabase.getAllFriendsInvitation();

        IntStream
                .range(0, mData.size())
                .filter(x -> mDataAux.stream().map(PurchaseLocal::getIdFirebase).noneMatch(y -> mData.get(x).getIdFirebase() == y))
                .boxed()
                .sorted(Comparator.reverseOrder())
                .forEach( x -> deleteItem(x));

        IntStream
                .range(0, mDataAux.size())
                .filter(x -> mData.stream().noneMatch(y -> y.getIdFirebase() == mDataAux.get(x).getIdFirebase()))
                .boxed()
                .forEach(x -> insertItem(x, mDataAux.get(x)));

        IntStream
                .range(0, mData.size())
                .filter(x -> mDataAux.stream().anyMatch(y -> y.getIdFirebase() == mData.get(x).getIdFirebase() && y.getCheck() != mData.get(x).getCheck()))
                .boxed()
                .forEach(z -> updateCheck(z, !mData.get(z).getCheck()));

        IntStream
                .range(0, accessLocal.size())
                .filter(x -> myAccessList.stream().map(FriendLocal::getUid).noneMatch(y -> accessLocal.get(x).getUid().equals(y)))
                .boxed()
                .forEach(x -> insertAccessUser(x, accessLocal.get(x)));

        IntStream
                .range(0, myAccessList.size())
                .filter(x -> accessLocal.stream().map(FriendLocal::getUid).noneMatch(y -> myAccessList.get(x).getUid().equals(y)))
                .boxed()
                .sorted(Comparator.reverseOrder())
                .forEach( x -> deleteAccessUser(x));

        IntStream
                .range(0, myAccessList.size())
                .filter(x -> isDifferent(myAccessList.get(x), accessLocal.get(x)))
                .boxed()
                .forEach(x -> applyChangeToAccessFriend(x, accessLocal.get(x)));

        ////////////////////////

        IntStream
                .range(0, invitationLocal.size())
                .filter(x -> mDataFriendsInvitation.stream().map(FriendLocal::getUid).noneMatch(y -> invitationLocal.get(x).getUid().equals(y)))
                .boxed()
                .forEach(x -> insertInvitation(x, invitationLocal.get(x)));

        IntStream
                .range(0, mDataFriendsInvitation.size())
                .filter(x -> invitationLocal.stream().map(FriendLocal::getUid).noneMatch(y -> mDataFriendsInvitation.get(x).getUid().equals(y)))
                .boxed()
                .sorted(Comparator.reverseOrder())
                .forEach( x -> deleteInvitation(x));

        IntStream
                .range(0, mDataFriendsInvitation.size())
                .filter(x -> isDifferent(mDataFriendsInvitation.get(x), invitationLocal.get(x)))
                .boxed()
                .forEach(x -> applyChangeInvitation(x, invitationLocal.get(x)));

        refresh(1000);
    }

    private Boolean isDifferent(FriendLocal friendLocalAux1, FriendLocal friendLocalAux2) {
        Boolean res = false;
        if (friendLocalAux1.getUid().equals(friendLocalAux2.getUid())) {
            if (!friendLocalAux1.getStatus().equals(friendLocalAux2.getStatus())) {
                res = true;
            } else if (!friendLocalAux1.getPhoto().equals(friendLocalAux2.getPhoto())) {
                res = true;
            } else if (!friendLocalAux1.getName().equals(friendLocalAux2.getName())) {
                res = true;
            } else if (!friendLocalAux1.getEmail().equals(friendLocalAux2.getEmail())) {
                res = true;
            }
        }
        return res;
    }

    public void deleteItem(int position) {
        recyclerViewAdapter.notifyItemRemoved(position);
        if (mData.size() > position) {
            mData.remove(position);
        }
    }

    public void insertItem(int position, PurchaseLocal purchaseLocal) {
        mData.add(position, purchaseLocal);
        recyclerViewAdapter.notifyItemInserted(position);
    }

    public void applyChangeToAccessFriend(int position, FriendLocal friendLocalToChange) {
        myAccessList.remove(position);
        myAccessList.add(position, friendLocalToChange);
        myFriendAccessAdapter.notifyDataSetChanged();
    }

    public void applyChangeInvitation(int position, FriendLocal friendLocalToChange) {
        mDataFriendsInvitation.remove(position);
        mDataFriendsInvitation.add(position, friendLocalToChange);
        recyclerViewAdapterFriendsInvitation.notifyDataSetChanged();
    }

    public void insertAccessUser(int position, FriendLocal friendLocal) {
        myAccessList.add(position, friendLocal);
        myFriendAccessAdapter.notifyItemInserted(position);
    }

    public void insertInvitation(int position, FriendLocal friendLocal) {
        mDataFriendsInvitation.add(position, friendLocal);
        recyclerViewAdapterFriendsInvitation.notifyItemInserted(position);
    }

    public void deleteAccessUser(int position) {
        if (myAccessList.size() > position) {
            myAccessList.remove(position);
            myFriendAccessAdapter.notifyItemRemoved(position);
        }
    }

    public void deleteInvitation(int position) {
        if (mDataFriendsInvitation.size() > position) {
            mDataFriendsInvitation.remove(position);
            recyclerViewAdapterFriendsInvitation.notifyItemRemoved(position);
        }
    }

    public void updateCheck(int position, Boolean check) {
        mData.get(position).setCheck(check);
        recyclerViewAdapter.notifyDataSetChanged();
    }

    public void updateVisibilityViewPager2() {
        ViewGroup.LayoutParams cardViewParams = (ViewGroup.LayoutParams) cardViewDialogShare.getLayoutParams();
        if (myAccessList.size() < 1) {
            textViewPager2.setVisibility(View.GONE);
            myAccessListViewPager.setVisibility(View.GONE);

            cardViewParams.height = CardView.LayoutParams.WRAP_CONTENT;
            cardViewParams.width = CardView.LayoutParams.MATCH_PARENT;
            cardViewDialogShare.setLayoutParams(cardViewParams);
        } else {
            textViewPager2.setVisibility(View.VISIBLE);
            myAccessListViewPager.setVisibility(View.VISIBLE);

            cardViewParams.height = CardView.LayoutParams.MATCH_PARENT;
            cardViewParams.width = CardView.LayoutParams.MATCH_PARENT;
            cardViewDialogShare.setLayoutParams(cardViewParams);
        }
    }

    public void refresh(int milliseconds) {
        Handler handler = new Handler();

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (new GoogleUtilities().getCurrentUser() != null) {
                    updateData();
                    updateNoPurchase();
                    updateNoShareList();
                    updateVisibilityViewPager2();
                } else {
                    return;
                }
            }
        };

        handler.postDelayed(runnable, milliseconds);
    }

    public void setHideFloatingButton(Boolean visibility) {
        this.hideFloatingButton = visibility;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_shopping_cart, menu);
        MenuItem itemAdd = menu.findItem(R.id.search_icon);
        searchView = (SearchView)itemAdd.getActionView();
        searchView.setQueryHint("Nombre del producto");
        itemAdd.setVisible(false);
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                String uid = new GoogleUtilities().getCurrentUser().getUid();
                new Utilities().savePurchase(query, -1, ShoppingCart.this, false, uid);
                searchView.clearFocus();
                itemAdd.collapseActionView();
                setHideFloatingButton(false);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        addListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                itemAdd.expandActionView();
                setHideFloatingButton(true);
                addListButton.hide();
            }
        });
        addButtonInitial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                itemAdd.expandActionView();
                setHideFloatingButton(true);
                addButtonInitial.setVisibility(View.INVISIBLE);
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.microphone:
                voiceAutomationMenu();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void shareList(View view) {
        try {
            String emailAux = email.getText().toString().trim();
            if (emailAux != null) {
                if (!emailAux.isEmpty() && android.util.Patterns.EMAIL_ADDRESS.matcher(emailAux).matches()) {
                    String emailUser = new GoogleUtilities().getCurrentUser().getEmail();
                    email.getText().clear();
                    InputMethodManager inputManager = (InputMethodManager) this.getSystemService(this.INPUT_METHOD_SERVICE);
                    inputManager.hideSoftInputFromWindow(email.getWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
                    if (emailAux.equals(emailUser)) {
                        Toast.makeText(ShoppingCart.this, "¡No puedes invitarte a ti mismo!", Toast.LENGTH_SHORT).show();
                    } else {
                        new GoogleUtilities().shareShoppingCart(emailAux, ShoppingCart.this);
                    }
                } else {
                    Toast.makeText(ShoppingCart.this, "El correo no es válido", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(ShoppingCart.this, "Inténtalo otra vez", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("ShoppingCart", "shareList: ", e);
            Toast.makeText(ShoppingCart.this, "Se ha producido un error", Toast.LENGTH_SHORT).show();
        }
    }

    public void voiceAutomationMenu() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Habla ahora");
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            checkVoiceControlMenu(requestCode, requestCode, data);
        }
    }

    public void checkVoiceControlMenu(int requestCode, int resultCode, @Nullable Intent data) {
        Boolean check = false;
        List<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
        String firstResult = results.get(0).toLowerCase();
        if (checkContains(firstResult, Arrays.asList("borra", "bórra", "elimin", "elimín")) && checkContains(firstResult, Arrays.asList("todo"))) {
            realmDatabase.deleteAllPurchaseFromLocal(new GoogleUtilities().getCurrentUser().getUid());
            check = true;
        } else if ((checkContains(firstResult, Arrays.asList("borra", "bórra", "elimin", "elimín","añád", "añad", "inserta", "mete", "méte")) && checkContains(firstResult, Arrays.asList("amigo"))) || checkContains(firstResult, Arrays.asList("compart"))) {
            dialogShareOption.show();
            check = true;
        } else if (checkContains(firstResult, Arrays.asList("amigo")) && checkContains(firstResult, Arrays.asList("mi"))) {
            changeScrollViewMode(true);
            Toast.makeText(this, "Ahora se muestran las listas que han compartido tus amigos contigo", Toast.LENGTH_LONG).show();
            check = true;
        }
        if (!check) {
            if (checkContains(firstResult, Arrays.asList("añád", "añad", "inserta", "mete", "méte"))) {
                String query = getProduct(firstResult, Arrays.asList("añád", "añad", "insert", "mete", "méte", "a la lista", "a la cesta", "a los productos", "en la lista", "en la cesta"));
                if (query.length() > 0) {
                    String uid = new GoogleUtilities().getCurrentUser().getUid();
                    new Utilities().savePurchase(query, -1, ShoppingCart.this, false, uid);
                    Toast.makeText(this, query + " se ha añadido", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "No se ha reconocido el nombre del producto que ha dicho\nInténtelo de nuevo.", Toast.LENGTH_LONG).show();
                }
            } else if (checkContains(firstResult, Arrays.asList("borr", "borrá", "elimin", "quit"))) {
                if (mData != null && mData.size() > 0) {
                    List<PurchaseLocal> toDeletePurchases = new ArrayList<>();
                    if (checkContains(firstResult, Arrays.asList("posición"))) {
                        Integer number = getNumber(firstResult);
                        if (number != null && number > 0 && mData.size() > (number - 1) && mData.get(number - 1) != null) {
                            toDeletePurchases.add(mData.get(number - 1));
                        } else {
                            Toast.makeText(this, "No hay ningún producto en esa posición", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        String queryPurchase = getProduct(firstResult, Arrays.asList("borr", "borrá", "elimin", "quit", "el producto", "elemento", "de la lista", "de la cesta", "de los productos"));
                        toDeletePurchases = mData.stream().filter(x -> x.getName().contains(queryPurchase)).collect(Collectors.toList());
                    }

                    Database realmDatabase = new Database();
                    for (PurchaseLocal elem : toDeletePurchases) {
                        String name = elem.getName();
                        realmDatabase.deletePurchaseFromLocal(elem);
                        Toast.makeText(this, name + " se ha eliminado", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(this, "Su lista está vacía, no puede borrar nada", Toast.LENGTH_LONG).show();
                }
            } else if (checkContains(firstResult, Arrays.asList("deselecciona", "desmarca"))) {
                if (mData != null && mData.size() > 0) {
                    List<PurchaseLocal> toCheckPurchases = new ArrayList<>();
                    if (checkContains(firstResult, Arrays.asList("posición"))) {
                        Integer number = getNumber(firstResult);
                        if (number != null && number > 0 && mData.size() > (number - 1) && mData.get(number - 1) != null) {
                            toCheckPurchases.add(mData.get(number - 1));
                        } else {
                            Toast.makeText(this, "No hay ningún producto en esa posición", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        String queryPurchase = getProduct(firstResult, Arrays.asList("desselecciona", "desmarca", "producto", "elemento", "el producto", "el elemento", "de la lista", "de la cesta", "de los productos"));
                        toCheckPurchases = mData.stream().filter(x -> x.getName().contains(queryPurchase)).collect(Collectors.toList());
                    }

                    Database realmDatabase = new Database();
                    for (PurchaseLocal elem : toCheckPurchases) {
                        String name = elem.getName();
                        if (!elem.getCheck()) {
                            Toast.makeText(this, "¡" + name + " ya estaba desmarcado!", Toast.LENGTH_LONG).show();
                        } else {
                            realmDatabase.changeCheckStatusFromLocal(elem, !elem.getCheck());
                            Toast.makeText(this, name + " se ha desmarcado como seleccionado", Toast.LENGTH_LONG).show();
                        }
                    }
                } else {
                    Toast.makeText(this, "Su lista está vacía, no puede desmarcar nada", Toast.LENGTH_LONG).show();
                }
            } else if (checkContains(firstResult, Arrays.asList("selecciona", "marca"))) {
                if (mData != null && mData.size() > 0) {
                    List<PurchaseLocal> toCheckPurchases = new ArrayList<>();
                    if (checkContains(firstResult, Arrays.asList("posición"))) {
                        Integer number = getNumber(firstResult);
                        if (number != null && number > 0 && mData.size() > (number - 1) && mData.get(number - 1) != null) {
                            toCheckPurchases.add(mData.get(number - 1));
                        } else {
                            Toast.makeText(this, "No hay ningún producto en esa posición", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        String queryPurchase = getProduct(firstResult, Arrays.asList("selecciona", "marca", "producto", "elemento", "el producto", "el elemento", "de la lista", "de la cesta", "de los productos"));
                        toCheckPurchases = mData.stream().filter(x -> x.getName().contains(queryPurchase)).collect(Collectors.toList());
                    }

                    Database realmDatabase = new Database();
                    for (PurchaseLocal elem : toCheckPurchases) {
                        String name = elem.getName();
                        if (elem.getCheck()) {
                            Toast.makeText(this, "¡" + name + " ya estaba marcado!", Toast.LENGTH_LONG).show();
                        } else {
                            realmDatabase.changeCheckStatusFromLocal(elem, !elem.getCheck());
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


    public void populate() {
        String uid = new GoogleUtilities().getCurrentUser().getUid();
        PurchaseRemote purchaseRemote1 = new PurchaseRemote(1, -1, uid, "Pate de cerdo", false);
        PurchaseRemote purchaseRemote2 = new PurchaseRemote(2, -1, uid, "Camiseta de diario", false);
        PurchaseRemote purchaseRemote3 = new PurchaseRemote(3, -1, uid, "PC HP", false);
        List<PurchaseRemote> listPurcharse = Arrays.asList(purchaseRemote1, purchaseRemote2, purchaseRemote3);
        ShoppingCartRemote shoppingCartRemote = new ShoppingCartRemote(uid, new Date(), true, listPurcharse, new ArrayList<>(), new ArrayList<>());
        FirebaseDatabase databaseShoppingCart = FirebaseDatabase.getInstance();
        DatabaseReference databaseReferenceShoppingCart = databaseShoppingCart.getReference("private").child(uid).child("listaCompra");
        databaseReferenceShoppingCart.push().setValue(shoppingCartRemote);
    }
}
