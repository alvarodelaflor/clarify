package es.clarify.clarify;


import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import es.clarify.clarify.Objects.StoreLocal;
import es.clarify.clarify.Utilities.Database;
import io.realm.Realm;
import io.realm.RealmList;


/**
 * A simple {@link Fragment} subclass.
 */
public class StoreFragment extends Fragment {

    View v;
    private RecyclerView myRecyclerView;
    private List<StoreLocal> listStoreLocal;
    private TextView textViewPrincipal;


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
        RecyclerViewAdapter recyclerViewAdapter = new RecyclerViewAdapter(getContext(), listStoreLocal);
        myRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        myRecyclerView.setAdapter(recyclerViewAdapter);
        return v;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        listStoreLocal = new Database().getAllStoreLocal();



    }
}
