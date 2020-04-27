package es.clarify.clarify.ShoppingCart;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import es.clarify.clarify.Objects.PurchaseLocal;
import es.clarify.clarify.R;
import es.clarify.clarify.Utilities.GoogleUtilities;

public class RecyclerViewAdapterShoppingCart extends RecyclerView.Adapter<RecyclerViewAdapterShoppingCart.MyViewHolder> {

    Context mContext;
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
        vHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    new GoogleUtilities().deletePurchaseFromRemote(mData.get(vHolder.getAdapterPosition()));
                } catch (Exception e) {
                    Log.e("Opening a Store", "onClick: ", e);
                }
            }
        });

        return vHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.purchare_name.setText(mData.get(position).getName());
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        private TextView purchare_name;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            purchare_name = (TextView) itemView.findViewById(R.id.shopping_card_item_txt);
        }
    }
}
