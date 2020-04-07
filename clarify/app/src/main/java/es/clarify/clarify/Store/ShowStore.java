package es.clarify.clarify.Store;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.os.Handler;

import java.util.ArrayList;

import es.clarify.clarify.Objects.ScannedTagLocal;
import es.clarify.clarify.R;
import es.clarify.clarify.RecyclerViewAdapter;
import es.clarify.clarify.Utilities.Database;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;

public class ShowStore extends AppCompatActivity {


    RecyclerView recyclerView;
    RecyclerViewAdapterShowStore recyclerViewAdapter;
//    ArrayList<String> rowsArrayList = new ArrayList<>();
    RealmList<ScannedTagLocal> realmResults = new RealmList<>();
    Database database = new Database();
    Boolean fullLoad = false;

    boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_store);

        recyclerView = findViewById(R.id.show_store_recyclerView);
        populateData();
        initAdapter();
        initScrollListener();


    }

    private void populateData() {
        int intLimit = 10;
        String store = getIntent().getStringExtra("store_name");
        RealmList<ScannedTagLocal> res = database.getScannedTagPagination(store, intLimit);
        this.realmResults = res;
        this.fullLoad = res.size() < 10 ? true : false;

//        int i = 0;
//        while (i < 10) {
//            rowsArrayList.add("Item " + i);
//            i++;
//        }
    }

    private void initAdapter() {

        recyclerViewAdapter = new RecyclerViewAdapterShowStore(realmResults, ShowStore.this);
        recyclerView.setAdapter(recyclerViewAdapter);
    }

    private void initScrollListener() {
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();

                if (!isLoading) {
                    if (linearLayoutManager != null && linearLayoutManager.findLastCompletelyVisibleItemPosition() == realmResults.size() - 1) {
                        //bottom of list!
                        if (!fullLoad) {
                            loadMore();
                            isLoading = true;
                        }
                    }
                }
            }
        });


    }

    private void loadMore() {
        recyclerViewAdapter.notifyItemInserted(realmResults.size() - 1);


        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                realmResults.remove(realmResults.size() - 1);
                int scrollPosition = realmResults.size();
                recyclerViewAdapter.notifyItemRemoved(scrollPosition);
                int currentSize = scrollPosition;
                int nextLimit = currentSize + 10;

                int initSize = realmResults.size();
                String store = getIntent().getStringExtra("store_name");
                RealmList<ScannedTagLocal> res = database.getScannedTagPagination(store, nextLimit);
                realmResults = res;
                int newResult = initSize - res.size();
                fullLoad = newResult < 10 ? true : false;

//                while (currentSize - 1 < nextLimit) {
//                    rowsArrayList.add("Item " + currentSize);
//                    currentSize++;
//                }

                recyclerViewAdapter.notifyDataSetChanged();
                isLoading = false;
            }
        }, 2000);


    }
}
