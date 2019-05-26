package com.jbproductions.wmbr;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Custom RecyclerView Adapter to sort and display archives grouped by show
 */
public class ArchiveAdapter extends RecyclerView.Adapter<ArchiveAdapter.ViewHolder> {

    private Context context;
    private SortedList<Show> list;

    ArchiveAdapter(Context c) {
        context = c;
        list = new SortedList<>(Show.class, new SortedList.Callback<Show>() {
            @Override
            public int compare(Show show1, Show show2) {
                return show1.getName().compareTo(show2.getName());
            }

            @Override
            public void onChanged(int position, int count) {
                notifyItemRangeChanged(position, count);
            }

            @Override
            public boolean areContentsTheSame(Show oldItem, Show newItem) {
                return oldItem.getName().equals(newItem.getName());
            }

            @Override
            public boolean areItemsTheSame(Show show1, Show show2) {
                return show1.getName().equals(show2.getName());
            }

            @Override
            public void onInserted(int position, int count) {
                notifyItemRangeInserted(position, count);
            }

            @Override
            public void onRemoved(int position, int count) {
                notifyItemRangeRemoved(position, count);
            }

            @Override
            public void onMoved(int fromPosition, int toPosition) {
                notifyItemMoved(fromPosition, toPosition);
            }
        });
    }

    public void addAll(List<Show> shows) {
        list.beginBatchedUpdates();
        for(int i=0; i < shows.size(); i++) {
            list.add(shows.get(i));
        }
        list.endBatchedUpdates();
    }

    public Show get(int position) {
        return list.get(position);
    }

    public void clear() {
        list.beginBatchedUpdates();
        //remove items at end, to avoid unnecessary array shifting
        while(list.size() > 0) {
            list.removeItemAt(list.size() - 1);
        }
        list.endBatchedUpdates();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_archive, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        final Show show = list.get(position);
        viewHolder.nameTextView.setText(show.getName());
        viewHolder.hostsTextView.setText(show.getHosts());
        viewHolder.numArchivesTextView.setText(Integer.toString(show.getArchiveList().size()));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView nameTextView, hostsTextView, numArchivesTextView;

        ViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.name_text_view);
            hostsTextView = itemView.findViewById(R.id.host_text_view);
            numArchivesTextView = itemView.findViewById(R.id.num_archives_text_view);
        }
    }
}
