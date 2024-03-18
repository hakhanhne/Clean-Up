package rmit.aad.cleanup.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.util.List;

import rmit.aad.cleanup.R;
import rmit.aad.cleanup.model.CleanUpSite;
import rmit.aad.cleanup.utils.CommonFunctions;
import rmit.aad.cleanup.views.cleanup.JoinCleanUp;
import rmit.aad.cleanup.views.cleanup.SiteDetails;

public class CleanUpSiteFullInfoAdapter extends RecyclerView.Adapter<CleanUpSiteFullInfoAdapter.ViewHolder>  {
    private Context context;
    private int resource;
    private List<CleanUpSite> list;

    public CleanUpSiteFullInfoAdapter(Context context, int resource, List<CleanUpSite> list) {
        this.context = context;
        this.resource = resource;
        this.list = list;
    }


    @NonNull
    @Override
    public CleanUpSiteFullInfoAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(resource, parent, false);
        return new CleanUpSiteFullInfoAdapter.ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull CleanUpSiteFullInfoAdapter.ViewHolder holder, int position) {
        CleanUpSite site = list.get(position);
        if (!site.getIsRecurring()) {
            holder.recurring_info.setVisibility(View.GONE);
        }
        else {
            holder.recurring_info.setVisibility(View.VISIBLE);
            holder.frequency.setText(site.getRecurringInfo().getFrequency().toString());

            String end_date_str = "";
            try {
                end_date_str = CommonFunctions.formatDateToString(site.getRecurringInfo().getEndDate());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            holder.end_date.setText(end_date_str);
        }
        holder.type.setText(site.getLocationType().toString());
        holder.site_name.setText(site.getLocationName());
        holder.target.setText(site.getTargetCategory().toString());
        holder.address.setText(site.getLocationAddress());
        holder.event_description.setText(site.getEventDescription());
        holder.start_time.setText(site.getStartTime());
        holder.end_time.setText(site.getEndTime());
        String event_date_str = "";
        try {
            event_date_str = CommonFunctions.formatDateToString(site.getEventDate());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        holder.event_date.setText(event_date_str);
        holder.site_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), SiteDetails.class);
                intent.putExtra(SiteDetails.SITE_ID_KEY, site.getId());
                v.getContext().startActivity(intent);

            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView site_name;
        private final TextView address;
        private final TextView target;
        private final TextView event_description;
        private final TextView start_time;
        private final TextView end_time;
        private final TextView event_date;
        private final TextView end_date;
        private final TextView frequency;
        private final TextView type;
        private final LinearLayout recurring_info;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            site_name = itemView.findViewById(R.id.card_site_full_name);
            target = itemView.findViewById(R.id.card_site_full_target);
            address = itemView.findViewById(R.id.card_site_full_address);
            event_description = itemView.findViewById(R.id.card_site_event_description);
            start_time = itemView.findViewById(R.id.card_site_start_time);
            end_time = itemView.findViewById(R.id.card_site_end_time);
            event_date = itemView.findViewById(R.id.card_site_event_date);
            end_date = itemView.findViewById(R.id.card_recurring_end_date);
            frequency = itemView.findViewById(R.id.card_recurring_frequency);
            recurring_info = itemView.findViewById(R.id.card_recurring_info);
            type = itemView.findViewById(R.id.card_site_location_type);
        }
    }

}
