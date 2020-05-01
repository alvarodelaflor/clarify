package es.clarify.clarify.ShoppingCart;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

import es.clarify.clarify.Objects.FriendLocal;
import es.clarify.clarify.R;

public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.FriendAdapterViewHolder>{

    private List<FriendLocal> myAccessList;

    public FriendAdapter(List<FriendLocal> myAccessList) {
        this.myAccessList = myAccessList;
    }

    @NonNull
    @Override
    public FriendAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new FriendAdapterViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.access_friends_layout_container,
                        parent,
                        false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull FriendAdapterViewHolder holder, int position) {
        holder.setUserData(myAccessList.get(position));
    }

    @Override
    public int getItemCount() {
        return myAccessList.size();
    }

    static class FriendAdapterViewHolder extends RecyclerView.ViewHolder {

        private ImageView imageUserProfile;
        private TextView statusUser, nameUser, emailUser;

        FriendAdapterViewHolder(@NonNull View itemView) {
            super(itemView);
            imageUserProfile = itemView.findViewById(R.id.img_user_profile);
            statusUser = itemView.findViewById(R.id.status_accep_user);
            nameUser = itemView.findViewById(R.id.text_name_user);
            emailUser = itemView.findViewById(R.id.text_email_user);
        }

        public void setUserData(FriendLocal friendLocal) {
            Picasso.get().load(friendLocal.getPhoto()).into(imageUserProfile);
            if (friendLocal.getStatus()) {
                statusUser.setText("Aceptada");
            } else {
                statusUser.setText("Pendiente");
            }
            nameUser.setText(friendLocal.getName().trim().split(" ")[0]);
            emailUser.setText(friendLocal.getEmail());
        }

    }
}
