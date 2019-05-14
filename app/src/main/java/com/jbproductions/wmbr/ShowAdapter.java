package com.jbproductions.wmbr;

import android.support.annotation.NonNull;
import android.support.design.card.MaterialCardView;
import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

/***
 * Custom RecyclerView Adapter to sort and display shows in order when scrolling through the schedule
 */
public class ShowAdapter extends RecyclerView.Adapter<ShowAdapter.ViewHolder> {

    private SortedList<Show> list;

    ShowAdapter() {
        list = new SortedList<>(Show.class, new SortedList.Callback<Show>() {
            @Override
            public int compare(Show show1, Show show2) {
                /* This custom compare function performs two additional checks to see if either show
                    being compared starts before 4am. If either show airs before 4am, it is a
                    late-night show and should be listed at the end of the day's schedule, rather
                    than at the beginning.
                */
                if(show1.getTime() < 400 && show2.getTime() > 400) {
                    return 1;   // show1 airs before 4am. Return 1 to move show1 towards the end of the list.
                }
                if(show1.getTime() > 400 && show2.getTime() < 400) {
                    return -1;   // show2 airs before 4am. Return -1 to move show2 towards the end of the list.
                }
                else {
                    return Integer.compare(show1.getTime(), show2.getTime());
                }
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
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_show, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Show show = list.get(position);
        holder.nameTextView.setText(show.getName());
        holder.hostTextView.setText(show.getHosts());
        holder.timeTextView.setText(Integer.toString(show.getTime()));
        holder.descriptionTextView.setText(show.getDescription());
        holder.urlTextView.setText(show.getUrl());
        holder.emailTextView.setText(show.getEmail());

        if(show.getAlternates() != 0) {
            holder.alternatesTextView.setText("Alternates Weekly");
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView showCardView;
        TextView nameTextView, hostTextView, timeTextView, descriptionTextView, urlTextView, emailTextView, alternatesTextView;
        ImageButton toggleImageButton;

        public ViewHolder(View itemView) {
            super(itemView);
            showCardView = itemView.findViewById(R.id.card_view_show);
            nameTextView = itemView.findViewById(R.id.text_view_name);
            hostTextView = itemView.findViewById(R.id.text_view_host);
            timeTextView = itemView.findViewById(R.id.text_view_time);
            descriptionTextView = itemView.findViewById(R.id.text_view_description);
            urlTextView = itemView.findViewById(R.id.text_view_url);
            emailTextView = itemView.findViewById(R.id.text_view_email);
            alternatesTextView = itemView.findViewById(R.id.text_view_alternates);
            toggleImageButton = itemView.findViewById(R.id.toggle_show_button);

            showCardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(descriptionTextView.getVisibility() == View.VISIBLE) {
                        toggleImageButton.setImageResource(R.drawable.ic_keyboard_arrow_down_black_24dp);
                        descriptionTextView.setVisibility(View.GONE);
                        urlTextView.setVisibility(View.GONE);
                        emailTextView.setVisibility(View.GONE);
                        alternatesTextView.setVisibility(View.GONE);
                    }
                    else {
                        toggleImageButton.setImageResource(R.drawable.ic_keyboard_arrow_up_black_24dp);
                        descriptionTextView.setVisibility(View.VISIBLE);
                        if(!"".equals(urlTextView.getText())) {urlTextView.setVisibility(View.VISIBLE);}
                        if(!"".equals(emailTextView.getText())) {emailTextView.setVisibility(View.VISIBLE);}
                        if(!"".equals(alternatesTextView.getText())) {alternatesTextView.setVisibility(View.VISIBLE);}
                    }
                }
            });

            toggleImageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(descriptionTextView.getVisibility() == View.VISIBLE) {
                        toggleImageButton.setImageResource(R.drawable.ic_keyboard_arrow_down_black_24dp);
                        descriptionTextView.setVisibility(View.GONE);
                        urlTextView.setVisibility(View.GONE);
                        emailTextView.setVisibility(View.GONE);
                        alternatesTextView.setVisibility(View.GONE);
                    }
                    else {
                        toggleImageButton.setImageResource(R.drawable.ic_keyboard_arrow_up_black_24dp);
                        descriptionTextView.setVisibility(View.VISIBLE);
                        if(!"".equals(urlTextView.getText())) {urlTextView.setVisibility(View.VISIBLE);}
                        if(!"".equals(emailTextView.getText())) {emailTextView.setVisibility(View.VISIBLE);}
                        if(!"".equals(alternatesTextView.getText())) {alternatesTextView.setVisibility(View.VISIBLE);}
                    }
                }
            });

        }
    }
}
