package es.clarify.clarify.ShoppingCart;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.purchase_name.setText(mData.get(position).getName());
        ImageView img_aux = holder.img_delete;
        img_aux.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Boolean check = new Database().deletePurchaseFromLocal(mData.get(holder.getAdapterPosition()));
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
        private ImageView img_delete;
        private CardView cardView;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            purchase_name = (TextView) itemView.findViewById(R.id.shopping_card_item_txt);
            img_delete = (ImageView) itemView.findViewById(R.id.shopping_card_item_img);
            cardView = (CardView) itemView.findViewById(R.id.card_view_stores);
        }
    }
}
