package es.clarify.clarify.Home;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import es.clarify.clarify.Utilities.Database;
import es.clarify.clarify.Utilities.GoogleUtilities;
import es.clarify.clarify.Utilities.Utilities;
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
    private LinearLayout pendingInvitation;
    private TextView numberPending;
    private LinearLayout checkAll;
    private LinearLayout uncheckAll;
    private LinearLayout deleteAll;
    private LinearLayout cancelAccess;
    private Database realmDatabase;
    private Dialog deleteAllPurchaseDialog;
    private Button confirmDelete;
    private Button cancelDelete;
    private Button confirmDeleteAccess;
    private Button cancelDeleteAccess;
    private Dialog deleteAllAccessDialog;
    private LinearLayout empty;

    public HomeFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_home, container, false);
        realmDatabase = new Database();
        openShoppingCart = (Button) v.findViewById(R.id.open_shopping_cart);
        empty = (LinearLayout) v.findViewById(R.id.empty);
        openShoppingCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Context context = v.getContext();
                Intent intent = new Intent(context, ShoppingCart.class);
                context.startActivity(intent);
            }
        });
        deleteAllPurchaseDialog = new Dialog(getContext());
        deleteAllPurchaseDialog.setContentView(R.layout.dialog_alert_delete);
        deleteAllPurchaseDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        deleteAllPurchaseDialog.getWindow()
                .setLayout(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
        confirmDelete = deleteAllPurchaseDialog.findViewById(R.id.button_cancel_delete_all);
        cancelDelete = deleteAllPurchaseDialog.findViewById(R.id.no_accept);

        deleteAllAccessDialog = new Dialog(getContext());
        deleteAllAccessDialog.setContentView(R.layout.dialog_alert_delete_2);
        deleteAllAccessDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        deleteAllAccessDialog.getWindow()
                .setLayout(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
        confirmDeleteAccess = deleteAllAccessDialog.findViewById(R.id.button_cancel_delete_all_access);
        cancelDeleteAccess = deleteAllAccessDialog.findViewById(R.id.no_accept_access);


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
        pendingInvitation = v.findViewById(R.id.pending_invitation);
        pendingInvitation.setVisibility(View.GONE);
        numberPending = v.findViewById(R.id.pending_invitation_txt);

        checkAll = (LinearLayout) v.findViewById(R.id.check_all);
        checkAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeStatusAllPurchase(true);
            }
        });
        uncheckAll = (LinearLayout) v.findViewById(R.id.uncheck_all);
        uncheckAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeStatusAllPurchase(false);
            }
        });
        deleteAll = (LinearLayout) v.findViewById(R.id.fast_delete_all);
        deleteAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteAllPurchaseDialog.show();
            }
        });
        cancelDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteAllPurchaseDialog.dismiss();
            }
        });
        cancelDeleteAccess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteAllAccessDialog.dismiss();
            }
        });
        confirmDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteAllPurchaseDialog.dismiss();
                Boolean check = realmDatabase.deleteAllPurchaseFromLocal(new GoogleUtilities().getCurrentUser().getUid());
                if (check) {
                    Toast.makeText(getContext(), "Se han borrado todos los productos", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Ha ocurrido un error borrando los productos", Toast.LENGTH_SHORT).show();
                }
            }
        });
        confirmDeleteAccess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Boolean check = new Utilities().deleteAllAccessFriendFromLocal();
                deleteAllAccessDialog.dismiss();
                if (check) {
                    Toast.makeText(getContext(), "Nadie tiene acceso a tu lista ahora", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Ha ocurrido un error borrando los accesos", Toast.LENGTH_SHORT).show();
                }
            }
        });
        cancelAccess = v.findViewById(R.id.delete_all_friend);
        cancelAccess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteAllAccessDialog.show();
            }
        });
        updateData();
        return v;
    }

    private void changeStatusAllPurchase(Boolean status) {
        String aux = status ? "marcardo" : "desmarcardo";
        Boolean check = realmDatabase.changeStatusAllPurchaseOwner(status);
        if (check) {
            Toast.makeText(getContext(), String.format("Se han %s todas", aux), Toast.LENGTH_SHORT).show();
            new GoogleUtilities().changeStatusAllPurchaseFromUserLogin(status);
        } else {
            Toast.makeText(getContext(), String.format("No se han podido %s todas", aux), Toast.LENGTH_SHORT).show();
        }
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
        String uid = new GoogleUtilities().getCurrentUser().getUid();
        ShoppingCartLocal shoppingCartLocalAux = realm.where(ShoppingCartLocal.class).equalTo("id", uid).findFirst();
        ShoppingCartLocal shoppingCartLocal = shoppingCartLocalAux != null ? realm.copyFromRealm(shoppingCartLocalAux) : null;
        List<PurchaseLocal> purchaseLocals = shoppingCartLocal != null && shoppingCartLocal.getPurcharse() != null ?
                shoppingCartLocal.getPurcharse() : new ArrayList<>();
        Integer dbPuchaseSize = purchaseLocals.size();
        Integer dbCheckSize = purchaseLocals.stream().filter(x -> x.getCheck().equals(true)).collect(Collectors.toList()).size();
        Integer dbInvitationSize = shoppingCartLocal != null && shoppingCartLocal.getFriendInvitation() != null ? shoppingCartLocal.getFriendInvitation().size() : 0;
        Integer nPending = shoppingCartLocal != null ? shoppingCartLocal.getFriendInvitation().stream().filter(x -> x.getStatus().equals(false)).collect(Collectors.toList()).size() : 0;
        if (purchaseLocals.size() > 0) {
            deleteAll.setVisibility(View.VISIBLE);
            checkAll.setVisibility(View.VISIBLE);
            uncheckAll.setVisibility(View.VISIBLE);
        } else {
            deleteAll.setVisibility(View.GONE);
            checkAll.setVisibility(View.GONE);
            uncheckAll.setVisibility(View.GONE);
        }
        if (nPending > 0) {
            pendingInvitation.setVisibility(View.VISIBLE);
            String number = nPending > 99 ? "+99" : nPending.toString();
            numberPending.setText(number + " nuevas");
        } else {
            pendingInvitation.setVisibility(View.GONE);
        }
        if (purchaseLocals.size() > 0 && dbCheckSize.equals(purchaseLocals.size())) {
            checkAll.setVisibility(View.GONE);
            uncheckAll.setVisibility(View.VISIBLE);
        } else if (purchaseLocals.size() > 0 && dbCheckSize.equals(0)) {
            checkAll.setVisibility(View.VISIBLE);
            uncheckAll.setVisibility(View.GONE);
        }
        if (shoppingCartLocal != null && shoppingCartLocal.getAllowUsers().size() > 0) {
            cancelAccess.setVisibility(View.VISIBLE);
        } else {
            cancelAccess.setVisibility(View.GONE);
        }
        if (purchaseLocals.size() <= 0 && (shoppingCartLocal == null || shoppingCartLocal.getAllowUsers().size() <= 0)) {
            empty.setVisibility(View.VISIBLE);
        } else {
            empty.setVisibility(View.GONE);
        }
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
