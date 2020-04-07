package es.clarify.clarify.Store;

import android.app.Application;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import es.clarify.clarify.Objects.ScannedTagLocal;
import es.clarify.clarify.R;
import es.clarify.clarify.RecyclerViewAdapter;
import es.clarify.clarify.Utilities.Database;
import es.clarify.clarify.Utilities.Utilities;
import io.realm.RealmList;
import io.realm.RealmResults;

public class RecyclerViewAdapterShowStore extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_LOADING = 1;
    private Dialog mydialog;

    public RealmList<ScannedTagLocal> mItemList;
    public Context myContext;


    public RecyclerViewAdapterShowStore(RealmList<ScannedTagLocal> itemList, Context context) {

        myContext = context;
        mItemList = itemList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ITEM) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_row_showstore, parent, false);

            ItemViewHolder vHolder = new ItemViewHolder(view);

            // Dialog init

            mydialog = new Dialog(myContext);
            mydialog.setContentView(R.layout.dialog_product);
            mydialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            TextView dialog_name = (TextView) mydialog.findViewById(R.id.dialog_name);
            TextView dialog_brand = (TextView) mydialog.findViewById(R.id.dialog_brand);
            Button dialog_btn_delete = (Button) mydialog.findViewById(R.id.dialog_btn_delete);
            Button dialog_btn_info = (Button) mydialog.findViewById(R.id.dialog_bnt_info);
            ImageView dialog_img = (ImageView) mydialog.findViewById(R.id.dialog_img);

            vHolder.itemProduct.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog_name.setText(mItemList.get(vHolder.getAdapterPosition()).getModel());
                    dialog_brand.setText(mItemList.get(vHolder.getAdapterPosition()).getBrand());
                    mydialog.show();

                    dialog_btn_delete.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ScannedTagLocal scannedTagLocal = mItemList.get(vHolder.getAdapterPosition());
                            Boolean result = new Utilities().deleteItemFromPrivateStore(scannedTagLocal.getStore(), scannedTagLocal.getIdFirebase());
                            if (result) {
                                Toast.makeText(myContext, "¡Se ha borrado el producto!", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(myContext, "¡Error!", Toast.LENGTH_LONG).show();
                                Log.i("RecyclerViewAdaptarShowStore", "Product couldn't be deleted");
                            }
                        }
                    });
                }
            });

            return vHolder;
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_loading, parent, false);
            return new LoadingViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {

        if (viewHolder instanceof ItemViewHolder) {

            populateItemRows((ItemViewHolder) viewHolder, position);
        } else if (viewHolder instanceof LoadingViewHolder) {
            showLoadingView((LoadingViewHolder) viewHolder, position);
        }

    }

    @Override
    public int getItemCount() {
        return mItemList == null ? 0 : mItemList.size();
    }

    /**
     * The following method decides the type of ViewHolder to display in the RecyclerView
     *
     * @param position
     * @return
     */
    @Override
    public int getItemViewType(int position) {
        return mItemList.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }


    private class ItemViewHolder extends RecyclerView.ViewHolder {

        TextView tvItem;
        private CardView itemProduct;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);

            tvItem = itemView.findViewById(R.id.tvItem);
            itemProduct = (CardView) itemView.findViewById(R.id.product_item_id);
        }
    }

    private class LoadingViewHolder extends RecyclerView.ViewHolder {

        ProgressBar progressBar;

        public LoadingViewHolder(@NonNull View itemView) {
            super(itemView);
            progressBar = itemView.findViewById(R.id.progressBar);
        }
    }

    private void showLoadingView(LoadingViewHolder viewHolder, int position) {
        //ProgressBar would be displayed

    }

    private void populateItemRows(ItemViewHolder viewHolder, int position) {

        String item = mItemList.get(viewHolder.getAdapterPosition()).getModel();
        viewHolder.tvItem.setText(item);

    }


}

