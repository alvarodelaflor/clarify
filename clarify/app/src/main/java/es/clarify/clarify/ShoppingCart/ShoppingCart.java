package es.clarify.clarify.ShoppingCart;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.LinearLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

import es.clarify.clarify.Objects.PurchaseLocal;
import es.clarify.clarify.R;
import es.clarify.clarify.Utilities.Database;

public class ShoppingCart extends AppCompatActivity {

    private Toolbar toolbar;
    List<PurchaseLocal> mData;
    RecyclerViewAdapterShoppingCart recyclerViewAdapter;
    private RecyclerView myRecyclerView;
    private Database realmDatabase;
    private LinearLayout noPurchase;
    private FloatingActionButton addListButton;

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
        mData = realmDatabase.getAllPurchaseLocalOwnerLogin();

        noPurchase = (LinearLayout)findViewById(R.id.no_purchase);
        addListButton = (FloatingActionButton)findViewById(R.id.add_item);

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
            noPurchase.setVisibility(View.VISIBLE);
            addListButton.hide();
        } else {
            noPurchase.setVisibility(View.GONE);
            addListButton.show();
        }
    }

    public void updateData() {
        List<PurchaseLocal> mDataAux = realmDatabase.getAllPurchaseLocalOwnerLogin();
        Boolean check =  mData.size() == mDataAux.size() && (mDataAux.stream().allMatch(x -> mData.contains(x)));
        if (check) {
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
}
