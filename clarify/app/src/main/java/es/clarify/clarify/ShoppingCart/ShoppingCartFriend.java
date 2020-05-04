package es.clarify.clarify.ShoppingCart;

import androidx.annotation.NonNull;
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
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import de.hdodenhof.circleimageview.CircleImageView;
import es.clarify.clarify.Objects.FriendRemote;
import es.clarify.clarify.Objects.PurchaseRemote;
import es.clarify.clarify.Objects.ShoppingCartRemote;
import es.clarify.clarify.Objects.UserData;
import es.clarify.clarify.R;
import es.clarify.clarify.Utilities.GoogleUtilities;

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
    private ViewPager2 accessListViewPager;
    private FriendAdapterRemote adapterFriend;

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
        userData = new UserData("username", "user@mail.com", null, null, null);
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
        IntStream.range(0, acessList.size())
                .filter(x -> allowUsers.stream().map(FriendRemote::getUid).noneMatch(y ->acessList.get(x).getUid().equals(y)))
                .boxed()
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
            }
            lyNoPurchasesFriend.setVisibility(View.VISIBLE);
            recycler.setVisibility(View.GONE);
        } else {
            if (!hideFloatingButton) {
                floatingActionButton.show();
            }
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
        getMenuInflater().inflate(R.menu.menu_shopping_cart, menu);
        MenuItem itemAdd = menu.findItem(R.id.search_icon);
        MenuItem itemShare = menu.findItem(R.id.share_list);
        itemShare.setVisible(false);
        searchView = (SearchView) itemAdd.getActionView();
        searchView.setQueryHint("Nombre del producto");
        itemAdd.setVisible(false);
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                int id = 1000 + mPurchase.stream().map(PurchaseRemote::getIdFirebase).findFirst().orElse(0);
                new GoogleUtilities().savePurchase(query, id, -1, false, uid);

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
                itemShare.setVisible(false);
                hideFloatingButton = true;
                floatingActionButton.hide();
                buttonInitial.setVisibility(View.INVISIBLE);
            }
        });
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                itemAdd.expandActionView();
                itemShare.setVisible(false);
                hideFloatingButton = true;
                floatingActionButton.hide();
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onPause() {
        super.onPause();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference1 = database.getReference().child(("private")).child(uid).child("listaCompra");
        databaseReference1.removeEventListener(listener);
        DatabaseReference databaseReference2 = database.getReference().child(("private")).child(uid);
        databaseReference2.removeEventListener(listenerOwner);
        finish();
    }

}
