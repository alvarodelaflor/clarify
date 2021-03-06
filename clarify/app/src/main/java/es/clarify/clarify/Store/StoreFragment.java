package es.clarify.clarify.Store;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import es.clarify.clarify.MainActivity;
import es.clarify.clarify.Objects.StoreLocal;
import es.clarify.clarify.R;
import es.clarify.clarify.ShoppingCart.ShoppingCart;
import es.clarify.clarify.Utilities.Database;

public class StoreFragment extends Fragment {

    View v;
    private RecyclerView myRecyclerView;
    public List<StoreLocal> listStoreLocal;
    private TextView textViewPrincipal;
    private Database database;
    RecyclerViewAdapter recyclerViewAdapter;
    private CardView noStores;
    private Button goToFind;
    private MainActivity mainActivity;
    private FloatingActionButton shoppingCard;

    public StoreFragment(MainActivity mainActivity) {
        database = new Database();
        this.mainActivity = mainActivity;
    }

    public StoreFragment() {
        database = new Database();
        this.mainActivity = null;
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_store, container, false);
        noStores = (CardView) v.findViewById(R.id.card_view_no_stores);
        goToFind = (Button) v.findViewById(R.id.go_to_find);
        goToFind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mainActivity != null) {
                    mainActivity.viewPager.setCurrentItem(3);
                }
            }
        });
        shoppingCard = (FloatingActionButton)v.findViewById(R.id.shopping_card);
        shoppingCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Context context = v.getContext();
                Intent intent = new Intent(context, ShoppingCart.class);
                context.startActivity(intent);
            }
        });
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

    @Override
    public void onPause() {
        super.onPause();
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
