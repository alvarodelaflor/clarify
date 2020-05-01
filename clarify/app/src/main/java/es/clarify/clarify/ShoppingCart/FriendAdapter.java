package es.clarify.clarify.ShoppingCart;

import android.app.Application;
import android.app.Dialog;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

import es.clarify.clarify.Objects.FriendLocal;
import es.clarify.clarify.R;
import es.clarify.clarify.Utilities.Utilities;

public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.FriendAdapterViewHolder>{

    private List<FriendLocal> myAccessList;
    private Application application;

    public FriendAdapter(List<FriendLocal> myAccessList, Application application) {
        this.myAccessList = myAccessList;
        this.application = application;
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
        holder.deleteFriendFromList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Boolean check = new Utilities().deleteAccessFriendFromLocal(myAccessList.get(holder.getAdapterPosition()));
                if (check) {
                    deleteAccessToAnUser(myAccessList.get(holder.getAdapterPosition()));
                    Toast.makeText(application, "¡Borrado!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void deleteAccessToAnUser(FriendLocal friendLocal) {
        int position = myAccessList.indexOf(friendLocal);
        myAccessList.remove(friendLocal);
        notifyItemRemoved(position);
    }

    @Override
    public int getItemCount() {
        return myAccessList.size();
    }

    static class FriendAdapterViewHolder extends RecyclerView.ViewHolder {

        private ImageView imageUserProfile;
        private TextView statusUser, nameUser, emailUser;
        private LinearLayout statusInfoUser;
        private ImageView deleteFriendFromList;

        FriendAdapterViewHolder(@NonNull View itemView) {
            super(itemView);
            imageUserProfile = itemView.findViewById(R.id.img_user_profile);
            statusUser = itemView.findViewById(R.id.status_accep_user);
            nameUser = itemView.findViewById(R.id.text_name_user);
            emailUser = itemView.findViewById(R.id.text_email_user);
            statusInfoUser = itemView.findViewById(R.id.status_info_user);
            deleteFriendFromList = (ImageView) itemView.findViewById(R.id.delete_from_shopping_cart);
        }

        public void setUserData(FriendLocal friendLocal) {
            Picasso.get().load(friendLocal.getPhoto()).into(imageUserProfile);
            if (friendLocal.getStatus()) {
                statusUser.setText("Aceptada");
                statusInfoUser.setVisibility(View.INVISIBLE);

            } else {
                statusUser.setText("Pendiente");
                statusInfoUser.setVisibility(View.VISIBLE);
            }
            nameUser.setText(friendLocal.getName().trim().split(" ")[0]);
            emailUser.setText(friendLocal.getEmail());
        }

    }
}
