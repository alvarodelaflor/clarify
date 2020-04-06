package es.clarify.clarify;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import es.clarify.clarify.Objects.StoreLocal;
import es.clarify.clarify.Store.ShowStore;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder> {

    Context mContext;
    List<StoreLocal> mData;

    public RecyclerViewAdapter(Context mContext, List<StoreLocal> mData) {
        this.mContext = mContext;
        this.mData = mData;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v;
        v = LayoutInflater.from(mContext).inflate(R.layout.item_contact, parent, false);
        MyViewHolder vHolder = new MyViewHolder(v);

        vHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Context context = v.getContext();
                Intent intent = new Intent(context, ShowStore.class);
                intent.putExtra("store_name", mData.get(vHolder.getAdapterPosition()).getName());
                context.startActivity(intent);
            }
        });

        return vHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        holder.tv_name.setText(mData.get(position).getName());
        holder.tv_phone.setText(mData.get(position).getLastUpdate().toString());

    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        private TextView tv_name;
        private TextView tv_phone;
        private ImageView img;
        private CardView cardView;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            tv_name = (TextView) itemView.findViewById(R.id.name_contact);
            tv_phone = (TextView) itemView.findViewById(R.id.phone_contact);
            img = (ImageView) itemView.findViewById(R.id.img_contact);
            cardView = (CardView) itemView.findViewById(R.id.card_view_list_stores);
        }
    }

}
