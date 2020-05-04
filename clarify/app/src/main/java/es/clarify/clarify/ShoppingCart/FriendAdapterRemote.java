package es.clarify.clarify.ShoppingCart;

import android.app.Application;
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
import es.clarify.clarify.Objects.FriendRemote;
import es.clarify.clarify.R;
import es.clarify.clarify.Utilities.Utilities;

public class FriendAdapterRemote extends RecyclerView.Adapter<FriendAdapterRemote.FriendAdapterViewHolder>{

    private List<FriendRemote> myAccessList;
    private Application application;

    public FriendAdapterRemote(List<FriendRemote> myAccessList, Application application) {
        this.myAccessList = myAccessList;
        this.application = application;
    }

    @NonNull
    @Override
    public FriendAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new FriendAdapterViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.access_friends_layout_container_2,
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
        private LinearLayout statusInfoUser;

        FriendAdapterViewHolder(@NonNull View itemView) {
            super(itemView);
            imageUserProfile = itemView.findViewById(R.id.img_user_profile);
            statusUser = itemView.findViewById(R.id.status_accep_user);
            nameUser = itemView.findViewById(R.id.text_name_user);
            emailUser = itemView.findViewById(R.id.text_email_user);
            statusInfoUser = itemView.findViewById(R.id.status_info_user);
        }

        public void setUserData(FriendRemote friendRemote) {
            Picasso.get().load(friendRemote.getPhoto()).into(imageUserProfile);
            if (friendRemote.getStatus()) {
                statusUser.setText("Aceptada");
                statusInfoUser.setVisibility(View.INVISIBLE);

            } else {
                statusUser.setText("Pendiente");
                statusInfoUser.setVisibility(View.VISIBLE);
            }
            nameUser.setText(friendRemote.getName().trim().split(" ")[0]);
            emailUser.setText(friendRemote.getEmail());
        }

    }
}
