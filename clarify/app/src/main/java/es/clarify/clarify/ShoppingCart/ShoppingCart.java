package es.clarify.clarify.ShoppingCart;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import es.clarify.clarify.Objects.PurchaseLocal;
import es.clarify.clarify.Objects.PurchaseRemote;
import es.clarify.clarify.Objects.ShoppingCartRemote;
import es.clarify.clarify.R;
import es.clarify.clarify.Utilities.Database;
import es.clarify.clarify.Utilities.GoogleUtilities;

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

        realmDatabase = new Database();
        mData = new ArrayList<>(realmDatabase.getAllPurchaseLocalOwnerLogin());

        noPurchase = (LinearLayout) findViewById(R.id.no_purchase);

        addListButton = (FloatingActionButton) findViewById(R.id.add_item);
        addButtonInitial = (Button) findViewById(R.id.add_item_initial);

        myRecyclerView = (RecyclerView) findViewById(R.id.purchase_recyclerview);
        recyclerViewAdapter = new RecyclerViewAdapterShoppingCart(this, mData);
        myRecyclerView.setLayoutManager(new GridLayoutManager(getApplication(), 1));
        myRecyclerView.setAdapter(recyclerViewAdapter);

        updateData();
        updateNoPurchase();
    }

    public void changeColor(int resourseColor) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(getApplicationContext(), resourseColor));
        }
    }

    public void updateNoPurchase() {
        if (mData.size() < 1) {
            if (!hideFloatingButton) {
                noPurchase.setVisibility(View.VISIBLE);
                addButtonInitial.setVisibility(View.VISIBLE);
            }
            addListButton.hide();
        } else {
            noPurchase.setVisibility(View.GONE);
            if (!hideFloatingButton) {
                addListButton.show();
            }
        }
    }

    public void updateData() {
        int lastSize = mData.size();
        List<PurchaseLocal> mDataAux = realmDatabase.getAllPurchaseLocalOwnerLogin();
        List<Integer> ids1 = mData.stream().map(x -> x.getIdFirebase()).collect(Collectors.toList());
        List<Integer> ids2 = mDataAux.stream().map(x -> x.getIdFirebase()).collect(Collectors.toList());
        Boolean check = mData.size() != mDataAux.size() || ids1.stream().anyMatch(x -> !ids2.contains(x));
        if (check) {
            recyclerViewAdapter.mData = new ArrayList<>(mDataAux);
            mData = new ArrayList<>(mDataAux);
            recyclerViewAdapter.notifyDataSetChanged();
        }
        updateNoPurchase();
        refresh(1000);
    }

    public void refresh(int milliseconds) {
        Handler handler = new Handler();

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                updateData();
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
                Toast.makeText(ShoppingCart.this, query, Toast.LENGTH_SHORT).show();
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


    public void populate() {
        String uid = new GoogleUtilities().getCurrentUser().getUid();
        PurchaseRemote purchaseRemote1 = new PurchaseRemote(1, -1, uid, "Pate de cerdo");
        PurchaseRemote purchaseRemote2 = new PurchaseRemote(2, -1, uid, "Camiseta de diario");
        PurchaseRemote purchaseRemote3 = new PurchaseRemote(3, -1, uid, "PC HP");
        List<PurchaseRemote> listPurcharse = Arrays.asList(purchaseRemote1, purchaseRemote2, purchaseRemote3);
        ShoppingCartRemote shoppingCartRemote = new ShoppingCartRemote(uid, new Date(), true, listPurcharse, new ArrayList<>());
        FirebaseDatabase databaseShoppingCart = FirebaseDatabase.getInstance();
        DatabaseReference databaseReferenceShoppingCart = databaseShoppingCart.getReference("private").child(uid).child("listaCompra");
        databaseReferenceShoppingCart.push().setValue(shoppingCartRemote);
    }
}
