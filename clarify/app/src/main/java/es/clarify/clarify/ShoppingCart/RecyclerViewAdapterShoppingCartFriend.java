package es.clarify.clarify.ShoppingCart;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import es.clarify.clarify.Objects.PurchaseLocal;
import es.clarify.clarify.Objects.PurchaseRemote;
import es.clarify.clarify.R;
import es.clarify.clarify.Utilities.Database;
import es.clarify.clarify.Utilities.GoogleUtilities;

public class RecyclerViewAdapterShoppingCartFriend extends RecyclerView.Adapter<RecyclerViewAdapterShoppingCartFriend.MyViewHolder> {

    public Context mContext;
    List<PurchaseRemote> mData;

    public RecyclerViewAdapterShoppingCartFriend(Context mContext, List<PurchaseRemote> mData) {
        this.mContext = mContext;
        this.mData = mData;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v;
        v = LayoutInflater.from(mContext).inflate(R.layout.item_purchase, parent, false);
        MyViewHolder vHolder = new MyViewHolder(v);

        return vHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.purchase_name.setText(mData.get(holder.getAdapterPosition()).getName());
        Boolean checkData = mData.get(holder.getAdapterPosition()).getCheck();
        CheckBox checkBox = holder.checkBox;
        checkBox.setChecked(checkData);
        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PurchaseLocal purchaseLocal = new PurchaseLocal();
                purchaseLocal.setIdFirebase(mData.get(holder.getAdapterPosition()).getIdFirebase());
                new GoogleUtilities().changeCheckStatusFromLocal(purchaseLocal, holder.checkBox.isChecked());
            }
        });
        LinearLayout linearLayoutAux = holder.linearLayoutDelete;
        linearLayoutAux.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PurchaseLocal purchaseLocal = new PurchaseLocal();
                new GoogleUtilities().deletePurchaseFromRemote(purchaseLocal, false);
                mData.remove(holder.getAdapterPosition());
                notifyItemRemoved(holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        private TextView purchase_name;
        private CheckBox checkBox;
        private LinearLayout linearLayoutDelete;
        private CardView cardView;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            purchase_name = (TextView) itemView.findViewById(R.id.shopping_card_item_txt);
            checkBox = (CheckBox) itemView.findViewById(R.id.checkbox_product);
            linearLayoutDelete = (LinearLayout) itemView.findViewById(R.id.linear_layout_delete);
            cardView = (CardView) itemView.findViewById(R.id.card_view_stores);
        }
    }
}
