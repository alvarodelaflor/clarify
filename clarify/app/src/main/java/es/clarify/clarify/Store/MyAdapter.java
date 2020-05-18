package es.clarify.clarify.Store;

import android.app.Activity;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

import es.clarify.clarify.Objects.ScannedTag;
import es.clarify.clarify.Objects.ScannedTagLocal;
import es.clarify.clarify.R;
import es.clarify.clarify.Utilities.Database;
import es.clarify.clarify.Utilities.Utilities;
import io.realm.Realm;

class LoadingViewHoler extends RecyclerView.ViewHolder {

    public ProgressBar progressBar;

    public LoadingViewHoler(View itemView) {
        super(itemView);
        progressBar = (ProgressBar) itemView.findViewById(R.id.progressBar_items);
    }
}

class ItemViewHoder extends RecyclerView.ViewHolder {

    public TextView name;
    public TextView brand;
    public ImageView img;
    public TextView price;
    public TextView expirationDate;
    public Dialog mydialog;
    TextView dialog_name;
    TextView dialog_brand;
    Button dialog_btn_delete;
    Button dialog_btn_info;
    ImageView dialog_img;

    public ItemViewHoder(View itemView, Context context) {
        super(itemView);
        name = (TextView) itemView.findViewById(R.id.model_product_item_rom);
        brand = (TextView) itemView.findViewById(R.id.brand_product_item_rom);
        img = (ImageView) itemView.findViewById(R.id.img_profile_product_show);
        price = (TextView) itemView.findViewById(R.id.price_product);
        expirationDate = (TextView) itemView.findViewById(R.id.expiration_date);

        mydialog = new Dialog(context);
        mydialog.setContentView(R.layout.dialog_product);
        mydialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog_name = (TextView) mydialog.findViewById(R.id.dialog_name);
        dialog_brand = (TextView) mydialog.findViewById(R.id.dialog_brand);
        dialog_btn_delete = (Button) mydialog.findViewById(R.id.dialog_btn_delete);
        dialog_btn_info = (Button) mydialog.findViewById(R.id.dialog_bnt_info);
        dialog_img = (ImageView) mydialog.findViewById(R.id.dialog_img);
    }
}

public class MyAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int VIEW_TYPE_ITEM = 0, VIEW_TYPE_LOADING = 1;
    ILoadMore loadMore;
    boolean isLoading;
    Activity activity;
    List<ScannedTagLocal> items;
    int visibleThreshold = 5;
    int lastVisibibleItem, totalItemCount;
    Context myContext;
    TextView lastUpdate;
    TextView lastUpdate_time;
    TextView totalCount;
    String store;
    LinearLayout msg_empty_1;

    public MyAdapter(RecyclerView recyclerView, Activity activity, List<ScannedTagLocal> items, Context context, List<TextView> textViews, String store, LinearLayout msg_empty_1) {
        this.activity = activity;
        this.items = items;
        this.myContext = context;
        this.totalCount = textViews.get(0);
        this.lastUpdate = textViews.get(1);
        this.lastUpdate_time = textViews.get(2);
        this.store = store;
        this.msg_empty_1 = msg_empty_1;

        LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                totalItemCount = linearLayoutManager.getItemCount();
                lastVisibibleItem = linearLayoutManager.findLastCompletelyVisibleItemPosition();
                if (!isLoading && totalItemCount <= (lastVisibibleItem + visibleThreshold)) {
                    if (loadMore != null) {
                        Realm realm = Realm.getDefaultInstance();
                        if (items.size() == realm.where(ScannedTagLocal.class).equalTo("store", store).findAll().size()) {
                            isLoading = false;
                        } else {
                            loadMore.onLoadMore();
                            isLoading = true;
                        }
                    }
                }
                uploadNothingView();
            }
        });
    }

    public List<ScannedTagLocal> getItems() {
        return items;
    }

    public void uploadNothingView() {
        if (items.size() > 0) {
            msg_empty_1.setVisibility(View.GONE);
        } else {
            msg_empty_1.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }

    public void setLoadMore(ILoadMore loadMore) {
        this.loadMore = loadMore;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ITEM) {
            View view = LayoutInflater.from(activity).inflate(R.layout.item_row_showstore, parent, false);

            ItemViewHoder vHolder = new ItemViewHoder(view, myContext);

            return vHolder;
        } else if (viewType == VIEW_TYPE_LOADING) {
            View view = LayoutInflater.from(activity).inflate(R.layout.item_loading, parent, false);
            return new LoadingViewHoler(view);
        }
        return null;

    }

    public void removeItem(int position) {
        if (position>-1) {
            items.remove(position);
        }
        String auxTotalCount = new Database().getNumberScannedTagLocalByStore(store).toString();
        totalCount.setText(auxTotalCount);
        Date lastUpdateAux = new Database().getLastUpadateByStore(store);
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
        String dateString = "";
        try {
            dateString = format.format(lastUpdateAux);
        } catch (Exception e) {
            Log.e("ShowStore", "onCreate: ", e);
            dateString = "Indeterminda";
        }
        lastUpdate.setText(dateString);

        SimpleDateFormat format2 = new SimpleDateFormat("hh:mm:ss");
        String dateString2 = "";
        try {
            dateString2 = format2.format(lastUpdateAux);
        } catch (Exception e) {
            Log.e("ShowStore", "onCreate: ", e);
            dateString2 = "Indeterminda";
        }
        lastUpdate_time.setText(dateString2);

        notifyItemRemoved(position);
        if (position > -1) {
            if (items.size() == 1) {
                notifyItemRangeChanged(position, items.size()-1);
            } else {
                notifyItemRangeChanged(0, items.size() - 1);
            }
            notifyDataSetChanged();
        }
        uploadNothingView();
    }

    public void addItem(ScannedTagLocal scannedTagLocal) {
        items.add(scannedTagLocal);
        String auxTotalCount = String.valueOf(items.size());
        totalCount.setText(auxTotalCount);
        Date lastUpdateAux = scannedTagLocal.getStorageDate();
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
        String dateString = "";
        try {
            dateString = format.format(lastUpdateAux);
        } catch (Exception e) {
            Log.e("ShowStore", "onCreate: ", e);
            dateString = "Indeterminada";
        }
        lastUpdate.setText(dateString);

        SimpleDateFormat format2 = new SimpleDateFormat("hh:mm:ss");
        String dateString2 = "";
        try {
            dateString2 = format2.format(lastUpdateAux);
        } catch (Exception e) {
            Log.e("ShowStore", "onCreate: ", e);
            dateString = "Indeterminada";
        }
        lastUpdate_time.setText(dateString2);

        notifyDataSetChanged();
        uploadNothingView();
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ItemViewHoder) {
            Realm realm = Realm.getDefaultInstance();
            ItemViewHoder viewHoder = (ItemViewHoder) holder;
            ScannedTagLocal resAux = items.get(position);
            ScannedTagLocal res = realm.copyFromRealm(resAux);
            String idFirebase = res.getIdFirebase();
            viewHoder.name.setText(res.getModel());
            viewHoder.brand.setText(res.getBrand());
            Picasso.get().load(res.getImage()).into(viewHoder.img);
            viewHoder.price.setText(res.getPrice().toString() + " €");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-d");
            String date = "";
            try {
                LocalDate dateAux = LocalDate.parse(res.getExpiration_date(), formatter);
                date = dateAux.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            } catch (Exception e) {
                Log.e("Parsing date", "onBindViewHolder: ", e);
            }
            viewHoder.expirationDate.setText(date);

            viewHoder.itemView.setOnClickListener(new View.OnClickListener() {


                @Override
                public void onClick(View view) {
                    viewHoder.dialog_name.setText(res.getModel());
                    viewHoder.dialog_brand.setText(res.getBrand());
                    Picasso.get().load(res.getImage()).into(viewHoder.dialog_img);
                    viewHoder.mydialog.show();

                    viewHoder.dialog_btn_delete.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
//                            ScannedTagLocal scannedTagLocal = res;
                            Realm realm = Realm.getDefaultInstance();
                            ScannedTagLocal scannedTagLocal = realm.where(ScannedTagLocal.class).equalTo("idFirebase", idFirebase).findFirst();
                            if (scannedTagLocal == null) {
                                viewHoder.mydialog.dismiss();
                            } else {
                                Boolean result = new Utilities().deleteItemFromPrivateStore(scannedTagLocal.getStore(), scannedTagLocal.getIdFirebase());
                                if (result) {
                                    viewHoder.mydialog.dismiss();
                                    try {
                                        Toast.makeText(myContext, "¡Se ha borrado el producto!", Toast.LENGTH_LONG).show();
                                        removeItem(position);
                                    } catch (Exception e) {
                                        Toast.makeText(myContext, "¡No se ha podido guardar!", Toast.LENGTH_LONG).show();
                                    }
                                } else {
                                    Toast.makeText(myContext, "¡Error!", Toast.LENGTH_LONG).show();
                                    Log.i("RecyclerViewAdaptarShowStore", "Product couldn't be deleted");
                                }
                            }
                            realm.close();
                        }
                    });
                }
            });

        } else if (holder instanceof LoadingViewHoler) {
            LoadingViewHoler loadingViewHoler = (LoadingViewHoler) holder;
            loadingViewHoler.progressBar.setIndeterminate(true);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void setLoaded() {
        isLoading = false;
    }
}
