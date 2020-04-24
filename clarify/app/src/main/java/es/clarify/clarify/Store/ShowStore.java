package es.clarify.clarify.Store;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import es.clarify.clarify.Objects.ScannedTagLocal;
import es.clarify.clarify.R;
import es.clarify.clarify.Utilities.Database;
import es.clarify.clarify.Utilities.GoogleUtilities;
import es.clarify.clarify.Utilities.Utilities;

public class ShowStore extends AppCompatActivity {

    private List<ScannedTagLocal> items = new ArrayList<>();
    private MyAdapter adapter;
    private Database database = new Database();
    private String store;
    private Toolbar toolbar;
    private ImageView storeImg;
    private ImageView img_to_rotate;
    private AppBarLayout appBarLayout;
    private Long numberProducts;
    private TextView totalCountProducts;
    private TextView lastUpdate;
    private TextView lastUpdate_time;
    private Utilities utilities;
    private ValueEventListener valueEventListener;
    private LinearLayout msg_empty_1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_store);
        store = getIntent().getStringExtra("store_name");
        numberProducts = database.getNumberScannedTagLocalByStore(store);

        appBarLayout = (AppBarLayout) findViewById(R.id.toolbar_show_store);
        toolbar = (Toolbar) findViewById(R.id.toolbar_level_2);
        toolbar.setTitle(store);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        changeColor(R.color.colorPrimary);

        img_to_rotate = (ImageView) findViewById(R.id.img_to_rotate);
        img_to_rotate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                appBarLayout.setExpanded(false, true);
            }
        });

        storeImg = (ImageView) findViewById(R.id.store_img);
        if (store.equals("Frigorífico")) {
            storeImg.setImageResource(R.drawable.fridge_opt);
        } else if (store.equals("Despensa")) {
            storeImg.setImageResource(R.drawable.despensa_opt);
        }

        populate();

        totalCountProducts = (TextView)findViewById(R.id.number_products);
        totalCountProducts.setText(numberProducts.toString());

        lastUpdate = (TextView)findViewById(R.id.last_update);
        Date lastUpdateAux = database.getLastUpadateByStore(store);
        if (lastUpdateAux == null && items.size() > 0) {
            lastUpdateAux = items.get(items.size()-1).getStorageDate();
        }
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
        String dateString = "";
        try {
            dateString = format.format(lastUpdateAux);
        } catch (Exception e) {
            Log.e("ShowStore", "onCreate: ", e);
            dateString = "Indeterminda";
        }
        lastUpdate.setText(dateString);

        lastUpdate_time = (TextView)findViewById(R.id.last_update_time);
        SimpleDateFormat format2 = new SimpleDateFormat("hh:mm:ss");
        String dateString2 = "";
        try {
            dateString2 = format2.format(lastUpdateAux);
        } catch (Exception e) {
            Log.e("ShowStore", "onCreate: ", e);
            dateString = "Indeterminda";
        }
        lastUpdate_time.setText(dateString2);

        RecyclerView recycler = (RecyclerView) findViewById(R.id.show_store_recyclerView);
        List<TextView> textViews = Arrays.asList(totalCountProducts, lastUpdate, lastUpdate_time);
        msg_empty_1 = (LinearLayout)findViewById(R.id.msg_empty_1);
        adapter = new MyAdapter(recycler, this, items, ShowStore.this, textViews, store, msg_empty_1);
        recycler.setAdapter(adapter);

        adapter.setLoadMore(new ILoadMore() {
            @Override
            public void onLoadMore() {
                if (items.size() < numberProducts) {
                    items.add(null);
                    adapter.notifyItemInserted(items.size() - 1);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            items.remove(items.size() - 1);
                            adapter.notifyItemRemoved(items.size());

                            int index = items.size();
                            int end = index + 10;

                            List<ScannedTagLocal> aux = database.getScannedTagLocalPagination(store, end)
                                    .stream()
                                    .filter(x -> !items.contains(x))
                                    .collect(Collectors.toList());
                            items.addAll(aux);
                            adapter.notifyDataSetChanged();
                            adapter.setLoaded();
                        }
                    }, 2000);
                } else {
//                    Toast.makeText(ShowStore.this, "Load data completed !", Toast.LENGTH_LONG).show();
                }
            }
        });

        utilities = new Utilities();
        valueEventListener = utilities.createValueEventListenerShowStore(adapter, store);
        utilities.showStoreListenerFirebase(adapter, store, valueEventListener);
    }

    public void changeColor(int resourseColor) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(getApplicationContext(), resourseColor));
        }
    }

    public void populate() {
        this.items = database.getScannedTagLocalPagination(store, 10);
    }

    @Override
    protected void onPause() {
        super.onPause();
        String userId = new GoogleUtilities().getCurrentUser().getUid();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = database.getReference().child(("private")).child(userId).child("stores").child(store);
        databaseReference.removeEventListener(valueEventListener);
        finish();
    }
}
