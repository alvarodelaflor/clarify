package es.clarify.clarify.Store;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import es.clarify.clarify.Objects.StoreLocal;
import es.clarify.clarify.R;
import es.clarify.clarify.Utilities.Database;

public class StoreFragment extends Fragment {

    View v;
    private RecyclerView myRecyclerView;
    private List<StoreLocal> listStoreLocal;
    private TextView textViewPrincipal;
    private Database database;
    RecyclerViewAdapter recyclerViewAdapter;
    private CardView noStores;

    public StoreFragment() {
        database = new Database();
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_store, container, false);
        noStores = (CardView) v.findViewById(R.id.card_view_no_stores);
        myRecyclerView = (RecyclerView) v.findViewById(R.id.store_recyclerview);
        textViewPrincipal = (TextView) v.findViewById(R.id.text_second);
        textViewPrincipal.setText(listStoreLocal.size() + " boxes");
        recyclerViewAdapter = new RecyclerViewAdapter(getContext(), listStoreLocal);
        myRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        myRecyclerView.setAdapter(recyclerViewAdapter);
        updateData();
        return v;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        listStoreLocal = database.getAllStoreLocal();
    }


    public void updateData() {
        int lastSize = listStoreLocal.size()-1;
        List<StoreLocal> listStoreLocalAux = database.getAllStoreLocal();
        Boolean check =  listStoreLocal.size() == listStoreLocalAux.size() && (listStoreLocalAux.stream().allMatch(x -> listStoreLocal.contains(x)));
        if (check) {
            textViewPrincipal.setText(listStoreLocal.size() + " boxes");
            recyclerViewAdapter.notifyDataSetChanged();
            recyclerViewAdapter.notifyItemRangeChanged(0, lastSize);
        }
        updateNoStores();
        refresh(1000);
    }

    public void updateNoStores () {
        if (listStoreLocal.size() < 1) {
            noStores.setVisibility(View.VISIBLE);
        } else {
            noStores.setVisibility(View.GONE);
        }
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
