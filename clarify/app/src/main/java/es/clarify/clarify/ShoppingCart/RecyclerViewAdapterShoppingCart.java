package es.clarify.clarify.ShoppingCart;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import es.clarify.clarify.Objects.PurchaseLocal;
import es.clarify.clarify.R;
import es.clarify.clarify.Utilities.Database;

public class RecyclerViewAdapterShoppingCart extends RecyclerView.Adapter<RecyclerViewAdapterShoppingCart.MyViewHolder> {

    public Context mContext;
    List<PurchaseLocal> mData;

    public RecyclerViewAdapterShoppingCart(Context mContext, List<PurchaseLocal> mData) {
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

    private void checkTextStatus(Integer position, TextView purchaseNameAux) {
        if (mData.size() > position) {
            Boolean checkData = mData.get(position).getCheck();
            if (checkData) {
                purchaseNameAux.setPaintFlags(purchaseNameAux.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            } else {
                purchaseNameAux.setPaintFlags(purchaseNameAux.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            }
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.purchase_name.setText(mData.get(holder.getAdapterPosition()).getName());
        Boolean checkData = mData.get(holder.getAdapterPosition()).getCheck();
        checkTextStatus(holder.getAdapterPosition(), holder.purchase_name);
        CheckBox checkBox = holder.checkBox;
        checkBox.setChecked(checkData);
        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Boolean check = new Database().changeCheckStatusFromLocal(mData.get(holder.getAdapterPosition()), holder.checkBox.isChecked());
            }
        });
        LinearLayout linearLayoutAux = holder.linearLayoutDelete;
        linearLayoutAux.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (holder.getAdapterPosition() >= 0) {
                    Boolean check = new Database().deletePurchaseFromLocal(mData.get(holder.getAdapterPosition()));
                    if (check) {
                        mData.remove(holder.getAdapterPosition());
                        notifyItemRemoved(holder.getAdapterPosition());
                    }
                }
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
