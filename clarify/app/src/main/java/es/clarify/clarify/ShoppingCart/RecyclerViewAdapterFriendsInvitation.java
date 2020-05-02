package es.clarify.clarify.ShoppingCart;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
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

import com.bumptech.glide.Glide;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import es.clarify.clarify.Objects.FriendLocal;
import es.clarify.clarify.Objects.PurchaseLocal;
import es.clarify.clarify.R;
import es.clarify.clarify.Utilities.Database;

public class RecyclerViewAdapterFriendsInvitation extends RecyclerView.Adapter<RecyclerViewAdapterFriendsInvitation.MyViewHolder> {

    public Context mContext;
    List<FriendLocal> mDataFriendsInvitation;

    public RecyclerViewAdapterFriendsInvitation(Context mContext, List<FriendLocal> mDataFriendsInvitation) {
        this.mContext = mContext;
        this.mDataFriendsInvitation = mDataFriendsInvitation;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v;
        v = LayoutInflater.from(mContext).inflate(R.layout.item_friend_invitation, parent, false);
        MyViewHolder vHolder = new MyViewHolder(v);

        return vHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        Dialog dialog = new Dialog(mContext);
        dialog.setContentView(R.layout.dialog_show_invitation);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        holder.name.setText(mDataFriendsInvitation.get(holder.getAdapterPosition()).getName());
        holder.email.setText(mDataFriendsInvitation.get(holder.getAdapterPosition()).getEmail());
        Picasso.get().load(mDataFriendsInvitation.get(holder.getAdapterPosition()).getPhoto()).into(holder.img);
        if (mDataFriendsInvitation.get(holder.getAdapterPosition()).getStatus()) {
            holder.pending.setVisibility(View.GONE);
        } else {
            holder.pending.setVisibility(View.VISIBLE);
        }
        CircleImageView imgDialog = (CircleImageView) dialog.findViewById(R.id.image_friend_dialog);
        TextView nameDialog = (TextView) dialog.findViewById(R.id.name_user_dialog);
        TextView emailDialog = (TextView) dialog.findViewById(R.id.email_user_dialog);
        nameDialog.setText(mDataFriendsInvitation.get(holder.getAdapterPosition()).getName());
        emailDialog.setText(mDataFriendsInvitation.get(holder.getAdapterPosition()).getEmail());
        Glide.with(mContext).load(mDataFriendsInvitation.get(holder.getAdapterPosition()).getPhoto()).into(imgDialog);
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.show();
            }
        });

    }

    @Override
    public int getItemCount() {
        return mDataFriendsInvitation.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        private TextView name;
        private TextView email;
        private ImageView img;
        private TextView pending;
        private CardView cardView;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            name = (TextView) itemView.findViewById(R.id.name_user_friend);
            email = (TextView) itemView.findViewById(R.id.email_friend_invitation);
            img = (ImageView) itemView.findViewById(R.id.img_friend);
            pending = (TextView) itemView.findViewById(R.id.pending);
            cardView = (CardView) itemView.findViewById(R.id.card_view_stores);
        }
    }
}
