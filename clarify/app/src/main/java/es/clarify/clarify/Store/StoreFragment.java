package es.clarify.clarify.Store;


import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import es.clarify.clarify.Objects.StoreLocal;
import es.clarify.clarify.R;
import es.clarify.clarify.Utilities.Database;
import io.realm.Realm;


/**
 * A simple {@link Fragment} subclass.
 */
public class StoreFragment extends Fragment {

    View v;
    private RecyclerView myRecyclerView;
    private List<StoreLocal> listStoreLocal;
    private TextView textViewPrincipal;
    private Database database = new Database();
    RecyclerViewAdapter recyclerViewAdapter;


    public StoreFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_store, container, false);
        myRecyclerView = (RecyclerView) v.findViewById(R.id.store_recyclerview);
        textViewPrincipal = (TextView) v.findViewById(R.id.text_second);
        textViewPrincipal.setText(listStoreLocal.size() + " boxes");
        recyclerViewAdapter = new RecyclerViewAdapter(getContext(), listStoreLocal);
        myRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        myRecyclerView.setAdapter(recyclerViewAdapter);
        return v;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        listStoreLocal = database.getAllStoreLocal();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser){
            updateData();
        }
    }

    public void updateData() {
        listStoreLocal = database.getAllStoreLocal();
        textViewPrincipal.setText(listStoreLocal.size() + " boxes");
        recyclerViewAdapter.notifyDataSetChanged();
    }
}
