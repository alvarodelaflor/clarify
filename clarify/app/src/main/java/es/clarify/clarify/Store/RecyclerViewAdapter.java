package es.clarify.clarify.Store;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import es.clarify.clarify.Objects.StoreLocal;
import es.clarify.clarify.R;
import es.clarify.clarify.Utilities.Utilities;

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
        v = LayoutInflater.from(mContext).inflate(R.layout.item_store, parent, false);
        GridLayout gridLayout = (GridLayout) v.findViewById(R.id.grid_layout_stores);
        MyViewHolder vHolder = new MyViewHolder(v);
        vHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Context context = v.getContext();
                    Intent intent = new Intent(context, ShowStore.class);
                    intent.putExtra("store_name", mData.get(vHolder.getAdapterPosition()).getName());
                    context.startActivity(intent);
                } catch (Exception e) {
                    Log.e("Opening a Store", "onClick: ", e);
                }
            }
        });

        return vHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        String name = mData.get(position).getName();
        holder.box_name.setText(name);
        holder.box_img.setImageResource(new Utilities().getPhotoByStore(name));
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        private GridLayout box_grid_layout;
        private TextView box_name;
        private ImageView box_img;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            box_img = (ImageView) itemView.findViewById(R.id.image_view_store);
            box_name = (TextView) itemView.findViewById(R.id.text_view_stores);
            box_grid_layout = (GridLayout) itemView.findViewById(R.id.grid_layout_stores);
        }
    }
}
