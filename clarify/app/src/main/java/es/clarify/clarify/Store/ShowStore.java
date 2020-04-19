package es.clarify.clarify.Store;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.android.material.appbar.AppBarLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import es.clarify.clarify.Objects.ScannedTagLocal;
import es.clarify.clarify.R;
import es.clarify.clarify.Utilities.Database;

public class ShowStore extends AppCompatActivity {


    //    RecyclerView recyclerView;
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
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
        String dateString = "";
        try {
            dateString = format.format(lastUpdateAux);
        } catch (Exception e) {
            Log.e("ShowStore", "onCreate: ", e);
            dateString = "Indeterminda";
        }
        lastUpdate.setText(dateString);

        RecyclerView recycler = (RecyclerView) findViewById(R.id.show_store_recyclerView);
        adapter = new MyAdapter(recycler, this, items, ShowStore.this);
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

//                            List<ScannedTagLocal> aux = database.getAllScannedTagLocal().stream().filter(x -> !items.contains(x)).collect(Collectors.toList());

                            List<ScannedTagLocal> aux = database.getScannedTagLocalPagination(store, end)
                                    .stream()
                                    .filter(x -> !items.contains(x))
                                    .collect(Collectors.toList());

//                            for (ScannedTagLocal s: aux) {
//                                items.add(s);
//                            }

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
    }

    public void populate() {
        this.items = database.getScannedTagLocalPagination(store, 10);
    }


}
