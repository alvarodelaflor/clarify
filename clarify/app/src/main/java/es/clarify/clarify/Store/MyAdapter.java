package es.clarify.clarify.Store;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.w3c.dom.Text;

import java.util.List;

import es.clarify.clarify.Objects.ScannedTagLocal;
import es.clarify.clarify.R;
import io.realm.RealmList;

class LoadingViewHoler extends RecyclerView.ViewHolder {

    public ProgressBar progressBar;

    public LoadingViewHoler(View itemView) {
        super(itemView);
        progressBar = (ProgressBar) itemView.findViewById(R.id.progressBar_items);
    }
}

class ItemViewHoder extends RecyclerView.ViewHolder {

    public TextView name;

    public ItemViewHoder(View itemView) {
        super(itemView);
        name = (TextView) itemView.findViewById(R.id.txtName);
    }
}

public class MyAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int VIEW_TYPE_ITEM=0,VIEW_TYPE_LOADING=1;
    ILoadMore loadMore;
    boolean isLoading;
    Activity activity;
    List<ScannedTagLocal> items;
    int visibleThreshold=5;
    int lastVisibibleItem, totalItemCount;

    public MyAdapter(RecyclerView recyclerView, Activity activity, List<ScannedTagLocal> items) {
        this.activity = activity;
        this.items = items;

        LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                totalItemCount = linearLayoutManager.getItemCount();
                lastVisibibleItem = linearLayoutManager.findLastCompletelyVisibleItemPosition();
                if (!isLoading && totalItemCount <= (lastVisibibleItem+visibleThreshold)) {
                    if (loadMore != null) {
                        loadMore.onLoadMore();
                        isLoading = true;
                    }
                }
            }
        });
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position) == null ? VIEW_TYPE_LOADING:VIEW_TYPE_ITEM;
    }

    public void setLoadMore(ILoadMore loadMore) {
        this.loadMore = loadMore;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ITEM) {
            View view = LayoutInflater.from(activity).inflate(R.layout.item_row_showstore, parent, false);
            return new ItemViewHoder(view);
        } else if (viewType == VIEW_TYPE_LOADING) {
            View view = LayoutInflater.from(activity).inflate(R.layout.item_loading, parent, false);
            return new LoadingViewHoler(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ItemViewHoder) {
            ItemViewHoder viewHoder = (ItemViewHoder) holder;
            viewHoder.name.setText(items.get(position).getModel());
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
