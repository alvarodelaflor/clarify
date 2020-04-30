package es.clarify.clarify.ShoppingCart;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
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
    private Database realmDatabase;
    private LinearLayout noPurchase;
    private FloatingActionButton addListButton;
    private Button addButtonInitial;
    private SearchView searchView;
    Boolean hideFloatingButton = false;
    private CardView cardView;
    private LinearLayout deleteAll;
    private View view;
    private Dialog dialog;
    private Dialog dialogShareOption;
    Button confirmDelete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_cart);

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

        noPurchase = (LinearLayout) findViewById(R.id.no_purchase);

        addListButton = (FloatingActionButton) findViewById(R.id.add_item);
        addButtonInitial = (Button) findViewById(R.id.add_item_initial);

        myRecyclerView = (RecyclerView) findViewById(R.id.purchase_recyclerview);
        recyclerViewAdapter = new RecyclerViewAdapterShoppingCart(this, mData);
        myRecyclerView.setLayoutManager(new GridLayoutManager(getApplication(), 1));
        myRecyclerView.setAdapter(recyclerViewAdapter);

        cardView = (CardView)findViewById(R.id.card_view_stores);

        updateData();
        updateNoPurchase();

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

        confirmDelete = (Button)dialog.findViewById(R.id.button_cancel_delete_all);
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
    }

    public void changeColor(int resourseColor) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(getApplicationContext(), resourseColor));
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

        refresh(1000);
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

    public void updateCheck(int position, Boolean check) {
        mData.get(position).setCheck(check);
        recyclerViewAdapter.notifyDataSetChanged();
    }

    public void refresh(int milliseconds) {
        Handler handler = new Handler();

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                updateData();
                updateNoPurchase();
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
        MenuItem itemShare = menu.findItem(R.id.share_list);
        searchView = (SearchView)itemAdd.getActionView();
        searchView.setQueryHint("Nombre del producto");
        itemAdd.setVisible(false);
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                new Utilities().savePurchase(query, -1, ShoppingCart.this, false);
                searchView.clearFocus();
                itemAdd.collapseActionView();
                itemShare.setVisible(true);
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
                itemShare.setVisible(false);
                setHideFloatingButton(true);
                addListButton.hide();
            }
        });
        addButtonInitial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                itemAdd.expandActionView();
                setHideFloatingButton(true);
                itemShare.setVisible(false);
                addButtonInitial.setVisibility(View.INVISIBLE);
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.share_list:
                dialogShareOption.show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void populate() {
        String uid = new GoogleUtilities().getCurrentUser().getUid();
        PurchaseRemote purchaseRemote1 = new PurchaseRemote(1, -1, uid, "Pate de cerdo", false);
        PurchaseRemote purchaseRemote2 = new PurchaseRemote(2, -1, uid, "Camiseta de diario", false);
        PurchaseRemote purchaseRemote3 = new PurchaseRemote(3, -1, uid, "PC HP", false);
        List<PurchaseRemote> listPurcharse = Arrays.asList(purchaseRemote1, purchaseRemote2, purchaseRemote3);
        ShoppingCartRemote shoppingCartRemote = new ShoppingCartRemote(uid, new Date(), true, listPurcharse, new ArrayList<>());
        FirebaseDatabase databaseShoppingCart = FirebaseDatabase.getInstance();
        DatabaseReference databaseReferenceShoppingCart = databaseShoppingCart.getReference("private").child(uid).child("listaCompra");
        databaseReferenceShoppingCart.push().setValue(shoppingCartRemote);
    }
}
