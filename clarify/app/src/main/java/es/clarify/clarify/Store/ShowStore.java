package es.clarify.clarify.Store;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import es.clarify.clarify.Objects.ScannedTagLocal;
import es.clarify.clarify.R;
import es.clarify.clarify.Utilities.Database;

public class ShowStore extends AppCompatActivity {


    //    RecyclerView recyclerView;
    List<ScannedTagLocal> items = new ArrayList<>();
    MyAdapter adapter;
    Database database = new Database();
    String store;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_store);
        store = getIntent().getStringExtra("store_name");

        populate();

        RecyclerView recycler = (RecyclerView) findViewById(R.id.show_store_recyclerView);
        adapter = new MyAdapter(recycler, this, items);
        recycler.setAdapter(adapter);

        adapter.setLoadMore(new ILoadMore() {
            @Override
            public void onLoadMore() {
                if (items.size() < database.getNumberScannedTagLocalByStore(store)) {
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
