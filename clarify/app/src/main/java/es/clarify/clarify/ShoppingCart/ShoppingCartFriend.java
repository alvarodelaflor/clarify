package es.clarify.clarify.ShoppingCart;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ScrollView;

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

import es.clarify.clarify.Objects.PurchaseLocal;
import es.clarify.clarify.Objects.PurchaseRemote;
import es.clarify.clarify.Objects.ShoppingCartRemote;
import es.clarify.clarify.R;

public class ShoppingCartFriend extends AppCompatActivity {

    private Toolbar toolbar;
    private String uid;
    private List<PurchaseRemote> mPurchase;
    private RecyclerView recycler;
    private RecyclerViewAdapterShoppingCartFriend adapter;

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

        mPurchase = new ArrayList<>();
        recycler = (RecyclerView) findViewById(R.id.rv_sc_friend);
        adapter = new RecyclerViewAdapterShoppingCartFriend(this, mPurchase);
        recycler.setLayoutManager(new GridLayoutManager(getApplication(), 1));
        recycler.setAdapter(adapter);
        ValueEventListener listener = createValueEventListener();
        enableListener(listener);
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
                            List<PurchaseRemote> purchaseRemoteFirebase = shoppingCartRemote.getPurcharse() != null ? shoppingCartRemote.getPurcharse() : new ArrayList<>();
                            purchaseRemoteFirebase.sort(Comparator.comparing(PurchaseRemote::getIdFirebase).reversed());
                            check(purchaseRemoteFirebase);
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

    private void check(List<PurchaseRemote> purchaseRemoteFirebase) {
        checkDeletePurchases(purchaseRemoteFirebase);
        checkNewPurchases(purchaseRemoteFirebase);
        checkChangesPurchases(purchaseRemoteFirebase);
    }

    private void checkChangesPurchases(List<PurchaseRemote> purchaseRemoteFirebase) {
        if (purchaseRemoteFirebase != null && purchaseRemoteFirebase.size() > 1 && mPurchase != null && purchaseRemoteFirebase.size() > 1 && purchaseRemoteFirebase.size() == mPurchase.size()) {
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
    }

    private void checkDeletePurchases(List<PurchaseRemote> purchaseRemoteFirebase) {
        if (purchaseRemoteFirebase != null) {
            IntStream
                    .range(0, mPurchase.size())
                    .filter(x -> purchaseRemoteFirebase.stream().map(PurchaseRemote::getIdFirebase).noneMatch(y -> mPurchase.get(x).getIdFirebase() == y))
                    .boxed()
                    .sorted(Comparator.reverseOrder())
                    .forEach( x -> deleteFromRecycler(x));

        } else {
            mPurchase.stream().forEach(x -> adapter.notifyItemRemoved(mPurchase.indexOf(x)));
            mPurchase = new ArrayList<>();
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
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
