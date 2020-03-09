package es.clarify.clarify.Objects;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import es.clarify.clarify.R;

public class ScannedTagsAdapter extends RecyclerView.Adapter<ScannedTagsAdapter.ScannedTagViewHolder> {
    private Context mCtx;
    private List<ScannedTag> artistList;

    public ScannedTagsAdapter(Context mCtx, List<ScannedTag> artistList) {
        this.mCtx = mCtx;
        this.artistList = artistList;
    }

    @NonNull
    @Override
    public ScannedTagViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(mCtx).inflate(R.layout.recyclerview_artists, parent, false);
//        return new ScannedTagViewHolder(view);
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull ScannedTagViewHolder holder, int position) {
        ScannedTag artist = artistList.get(position);
//        holder.textViewName.setText(artist.name);
//        holder.textViewGenre.setText("Genre: " + artist.genre);
//        holder.textViewAge.setText("Age: " + artist.age);
//        holder.textViewCountry.setText("Country: " + artist.country);
    }

    @Override
    public int getItemCount() {
        return artistList.size();
    }

    class ScannedTagViewHolder extends RecyclerView.ViewHolder {

        TextView textViewName, textViewGenre, textViewAge, textViewCountry;

        public ScannedTagViewHolder(@NonNull View itemView) {
            super(itemView);

//            textViewName = itemView.findViewById(R.id.text_view_name);
//            textViewGenre = itemView.findViewById(R.id.text_view_genre);
//            textViewAge = itemView.findViewById(R.id.text_view_age);
//            textViewCountry = itemView.findViewById(R.id.text_view_country);
        }
    }
}