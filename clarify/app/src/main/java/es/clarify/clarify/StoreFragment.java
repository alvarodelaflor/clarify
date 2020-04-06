package es.clarify.clarify;


import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
    private List<Contact> listContact;
    private List<StoreLocal> listStoreLocal;


    public StoreFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_store, container, false);
        myRecyclerView = (RecyclerView) v.findViewById(R.id.store_recyclerview);
        RecyclerViewAdapter recyclerViewAdapter = new RecyclerViewAdapter(getContext(), listStoreLocal);
        myRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        myRecyclerView.setAdapter(recyclerViewAdapter);
        return v;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        listContact = new ArrayList<>();
//        listContact.add(new Contact("Alvaro de la Flor", "665381121", R.drawable.risketos));
//        listContact.add(new Contact("Alvaro1 de la Flor", "665381121", R.drawable.risketos));
//        listContact.add(new Contact("Alvaro2 de la Flor", "665381121", R.drawable.risketos));
//        listContact.add(new Contact("Alvaro3 de la Flor", "665381121", R.drawable.risketos));
//        listContact.add(new Contact("Alvaro4 de la Flor", "665381121", R.drawable.risketos));
//        listContact.add(new Contact("Alvaro5 de la Flor", "665381121", R.drawable.risketos));
//        listContact.add(new Contact("Alvaro6 de la Flor", "665381121", R.drawable.risketos));
//        listContact.add(new Contact("Alvaro7 de la Flor", "665381121", R.drawable.risketos));
//        listContact.add(new Contact("Alvaro8 de la Flor", "665381121", R.drawable.risketos));
//        listContact.add(new Contact("Alvaro9 de la Flor", "665381121", R.drawable.risketos));
//        listContact.add(new Contact("Alvaro10 de la Flor", "665381121", R.drawable.risketos));
//        listContact.add(new Contact("Alvaro11 de la Flor", "665381121", R.drawable.risketos));
//        listContact.add(new Contact("Alvaro12 de la Flor", "665381121", R.drawable.risketos));
//        listContact.add(new Contact("Alvaro13 de la Flor", "665381121", R.drawable.risketos));

        listStoreLocal = new Database().getAllStoreLocal();



    }
}
