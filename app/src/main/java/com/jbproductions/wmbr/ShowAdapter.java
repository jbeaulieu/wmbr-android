package com.jbproductions.wmbr;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.button.MaterialButton;
import android.support.design.card.MaterialCardView;
import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

/***
 * Custom RecyclerView Adapter to sort and display shows in order when scrolling through the schedule
 */
public class ShowAdapter extends RecyclerView.Adapter<ShowAdapter.ViewHolder> {

    private Context context;
    private SortedList<Show> list;

    ShowAdapter(Context c) {
        context = c;
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
        final Show show = list.get(position);
        holder.nameTextView.setText(show.getName());
        holder.hostTextView.setText(show.getHosts());
        holder.timeTextView.setText(show.getTimeString());
        holder.descriptionTextView.setText(show.getDescription());

        /*
         * If a show has a url set, enable the visibility of the appropriate button icon
         * and assign an OnClickListener to handle opening the show's URL via the browser
         */
        if(!"".equals(show.getUrl())) {
            holder.webButton.setVisibility(View.VISIBLE);
            holder.webButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(show.getUrl()));
                    context.startActivity(intent);
                }
            });
        }

        /*
         * If a show has an email set, enable the visibility of the appropriate button icon and
         * assign an OnClickListener to handle sending an email via the user's choice of email app
         */
        if(!"".equals(show.getEmail())) {
            holder.emailButton.setVisibility(View.VISIBLE);
            holder.emailButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String uri = "mailto:" + show.getEmail();
                    Intent intent = new Intent(Intent.ACTION_SENDTO);
                    intent.setData(Uri.parse(uri));
                    context.startActivity(intent);
                }
            });
        }

        // If a show alternates weekly, enable the visibility of the TextView
        if(show.getAlternates() != 0) {
            holder.alternatesTextView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView showCardView;
        LinearLayout expandableLayout;
        TextView nameTextView, hostTextView, timeTextView, descriptionTextView, alternatesTextView;
        ImageButton expandImageButton;
        MaterialButton webButton, emailButton, reminderButton;

        ViewHolder(View itemView) {
            super(itemView);
            showCardView = itemView.findViewById(R.id.card_view_show);
            expandableLayout = itemView.findViewById(R.id.expand_layout);
            nameTextView = itemView.findViewById(R.id.text_view_name);
            hostTextView = itemView.findViewById(R.id.text_view_host);
            timeTextView = itemView.findViewById(R.id.text_view_time);
            descriptionTextView = itemView.findViewById(R.id.text_view_description);
            alternatesTextView = itemView.findViewById(R.id.text_view_alternates);
            expandImageButton = itemView.findViewById(R.id.expand_button);

            webButton = itemView.findViewById(R.id.url_button);
            emailButton = itemView.findViewById(R.id.email_button);
            reminderButton = itemView.findViewById(R.id.reminder_button);

            /*
             * OnClickListener to toggle expanding/collapsing additional show details, including
             * descriptions, urls, emails, and info about alternating (as applicable). Uses the
             * current visible state of expandableLayout to either call expand() or collapse, and
             * changes the drop-down arrow to a upward-facing collapse arrow if expanding.
             */
            View.OnClickListener expandShowCardViewListener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(expandableLayout.getVisibility() == View.VISIBLE) {
                        expandImageButton.setImageResource(R.drawable.ic_keyboard_arrow_down_black_24dp);
                        collapse(expandableLayout);
                    }
                    else {
                        expandImageButton.setImageResource(R.drawable.ic_keyboard_arrow_up_black_24dp);
                        expand(expandableLayout);
                    }
                }
            };
            showCardView.setOnClickListener(expandShowCardViewListener);
            expandImageButton.setOnClickListener(expandShowCardViewListener);
        }

        /**
         * Function to smoothly expand a View object via animation. Used to expand CardViews on the
         * schedule and display their extra details (description, contact info, alternates)
         * @param v View object to be expanded
         */
        void expand(final View v) {
            int matchParentMeasureSpec = View.MeasureSpec.makeMeasureSpec(((View) v.getParent()).getWidth(), View.MeasureSpec.EXACTLY);
            int wrapContentMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
            v.measure(matchParentMeasureSpec, wrapContentMeasureSpec);
            final int targetHeight = v.getMeasuredHeight();

            // Older versions of android (pre API 21) cancel animations for views with a height of 0.
            v.getLayoutParams().height = 1;
            v.setVisibility(View.VISIBLE);
            Animation a = new Animation()
            {
                @Override
                protected void applyTransformation(float interpolatedTime, Transformation t) {
                    v.getLayoutParams().height = interpolatedTime == 1
                            ? ViewGroup.LayoutParams.WRAP_CONTENT
                            : (int)(targetHeight * interpolatedTime);
                    v.requestLayout();
                }

                @Override
                public boolean willChangeBounds() {
                    return true;
                }
            };

            // 4dp/ms
            a.setDuration(((int)(targetHeight / v.getContext().getResources().getDisplayMetrics().density))*4);
            v.startAnimation(a);
        }

        /**
         * Function to smoothly collapse a View object via animation. Used to collapse CardViews on
         * the schedule and hide their extra details (description, contact info, alternates)
         * @param v View object to be collapsed
         */
        void collapse(final View v) {
            final int initialHeight = v.getMeasuredHeight();

            Animation a = new Animation()
            {
                @Override
                protected void applyTransformation(float interpolatedTime, Transformation t) {
                    if(interpolatedTime == 1){
                        v.setVisibility(View.GONE);
                    }else{
                        v.getLayoutParams().height = initialHeight - (int)(initialHeight * interpolatedTime);
                        v.requestLayout();
                    }
                }

                @Override
                public boolean willChangeBounds() {
                    return true;
                }
            };

            // 4dp/ms
            a.setDuration(((int)(initialHeight / v.getContext().getResources().getDisplayMetrics().density))*4);
            v.startAnimation(a);
        }
    }
}
