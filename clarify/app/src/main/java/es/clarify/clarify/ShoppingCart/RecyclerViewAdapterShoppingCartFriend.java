package es.clarify.clarify.ShoppingCart;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
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
    public void onBindViewHolder(final @NonNull MyViewHolder holder, int position) {

        Boolean checkData = mData.get(holder.getAdapterPosition()).getCheck();
        checkTextStatus(holder.getAdapterPosition(), holder.purchase_name);
        LinearLayout linearLayoutAux = holder.linearLayoutDelete;
        linearLayoutAux.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PurchaseRemote purchaseRemote = mData.get(holder.getAdapterPosition());
                if (purchaseRemote != null) {
                    PurchaseLocal purchaseLocal = new PurchaseLocal();
                    purchaseLocal.setIdFirebase(purchaseRemote.getIdFirebase());
                    new GoogleUtilities().deletePurchaseFromRemote(purchaseLocal, false, purchaseRemote.getIdShoppingCart());
                    mData.remove(holder.getAdapterPosition());
                    notifyItemRemoved(holder.getAdapterPosition());
                }
            }
        });
        CheckBox checkBox = holder.checkBox;
        checkBox.setChecked(checkData);
        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PurchaseLocal purchaseLocal = new PurchaseLocal();
                purchaseLocal.setIdFirebase(mData.get(holder.getAdapterPosition()).getIdFirebase());
                purchaseLocal.setIdShoppingCart(mData.get(holder.getAdapterPosition()).getIdShoppingCart());
                new GoogleUtilities().changeCheckStatusFromLocal(purchaseLocal, holder.checkBox.isChecked(), purchaseLocal.getIdShoppingCart());
            }
        });
        holder.purchase_name.setText(mData.get(holder.getAdapterPosition()).getName());
        if (!mData.get(holder.getAdapterPosition()).getLastUpdate().getName().equals(new GoogleUtilities().getCurrentUser().getDisplayName())) {
            holder.lastUpdate.setVisibility(View.VISIBLE);
            holder.nameFriend.setText(mData.get(holder.getAdapterPosition()).getLastUpdate().getName());
            Glide.with(mContext).load(mData.get(holder.getAdapterPosition()).getLastUpdate().getPhoto()).into(holder.photoFriend);
        } else {
            holder.lastUpdate.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        private TextView purchase_name;
        private CheckBox checkBox;
        private LinearLayout linearLayoutDelete;
        private LinearLayout linearLayoutCard;
        private CardView cardView;
        private LinearLayout lastUpdate;
        private CircleImageView photoFriend;
        private TextView nameFriend;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            purchase_name = (TextView) itemView.findViewById(R.id.shopping_card_item_txt);
            checkBox = (CheckBox) itemView.findViewById(R.id.checkbox_product);
            linearLayoutDelete = (LinearLayout) itemView.findViewById(R.id.linear_layout_delete);
            linearLayoutCard = (LinearLayout) itemView.findViewById(R.id.linear_layout_stores_2);
            cardView = (CardView) itemView.findViewById(R.id.card_view_stores);
            lastUpdate = (LinearLayout) itemView.findViewById(R.id.user_friend);
            photoFriend = (CircleImageView) itemView.findViewById(R.id.id_friend);
            nameFriend = (TextView) itemView.findViewById(R.id.id_add_friend);
        }
    }
}
