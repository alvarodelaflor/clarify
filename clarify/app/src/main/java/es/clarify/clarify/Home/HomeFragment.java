package es.clarify.clarify.Home;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import es.clarify.clarify.Objects.PurchaseLocal;
import es.clarify.clarify.Objects.ShoppingCartLocal;
import es.clarify.clarify.R;
import es.clarify.clarify.ShoppingCart.ShoppingCart;
import es.clarify.clarify.Utilities.GoogleUtilities;
import io.realm.Realm;

public class HomeFragment extends Fragment {

    private Button openShoppingCart;
    private TextView userName;
    private TextView purchaseNumber;
    private TextView checkNumber;
    private TextView invitationNumber;
    private LinearLayout showInvitation;
    private LinearLayout checkPurchase;
    private LinearLayout purchaseLy;

    public HomeFragment() {

    }



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v =  inflater.inflate(R.layout.fragment_home, container, false);
        openShoppingCart = (Button) v.findViewById(R.id.open_shopping_cart);
        openShoppingCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Context context = v.getContext();
                Intent intent = new Intent(context, ShoppingCart.class);
                context.startActivity(intent);
            }
        });
        userName = (TextView) v.findViewById(R.id.tv_name_user);
        String userNameFirebase = new GoogleUtilities().getCurrentUser().getDisplayName().trim().split(" ")[0];
        userName.setText("tus datos, " + userNameFirebase);
        purchaseNumber = v.findViewById(R.id.purchase_number);
        checkNumber = v.findViewById(R.id.check_number);
        invitationNumber = v.findViewById(R.id.invitation_number);
        showInvitation = v.findViewById(R.id.show_invitation);
        showInvitation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initShoppingCard(v, true);
            }
        });
        checkPurchase = v.findViewById(R.id.check_purchase);
        checkPurchase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initShoppingCard(v, false);
            }
        });
        purchaseLy = v.findViewById(R.id.purchase_ly);
        purchaseLy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initShoppingCard(v, false);
            }
        });
        updateData();
        return v;
    }

    private void initShoppingCard(View v, Boolean shareView) {
        Context context = v.getContext();
        Intent intent = new Intent(context, ShoppingCart.class);
        intent.putExtra("goToShare", shareView);
        context.startActivity(intent);
    }

    private void updateData() {
        List<TextView> params = Arrays.asList(purchaseNumber, checkNumber, invitationNumber);
        Realm realm = Realm.getDefaultInstance();
        List<PurchaseLocal> purchaseLocalsAux = realm.where(PurchaseLocal.class).findAll();
        List<PurchaseLocal> purchaseLocals = purchaseLocalsAux != null ? realm.copyFromRealm(purchaseLocalsAux) : new ArrayList<>();
        Integer dbPuchaseSize = purchaseLocals.size();
        Integer dbCheckSize = purchaseLocals.stream().filter(x -> x.getCheck().equals(true)).collect(Collectors.toList()).size();
        ShoppingCartLocal shoppingCartLocalAux = realm.where(ShoppingCartLocal.class).equalTo("id", new GoogleUtilities().getCurrentUser().getUid()).findFirst();
        ShoppingCartLocal shoppingCartLocal = shoppingCartLocalAux != null ? realm.copyFromRealm(shoppingCartLocalAux) : null;
        Integer dbInvitationSize = shoppingCartLocal != null && shoppingCartLocal.getFriendInvitation() != null ? shoppingCartLocal.getFriendInvitation().size() : 0;
        List<Integer> util = Arrays.asList(dbPuchaseSize, dbCheckSize, dbInvitationSize);
        IntStream.range(0, util.size())
                .filter(x -> util.get(x) != Integer.getInteger(params.get(x).getText().toString()))
                .boxed()
                .forEach(x -> params.get(x).setText(util.get(x).toString()));
        realm.close();
        refresh(1000);
    }

    public void refresh(int milliseconds) {
        Handler handler = new Handler();

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (new GoogleUtilities().getCurrentUser() != null) {
                    updateData();
                }
            }
        };

        handler.postDelayed(runnable, milliseconds);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }
}
