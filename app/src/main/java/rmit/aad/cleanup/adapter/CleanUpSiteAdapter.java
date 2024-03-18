package rmit.aad.cleanup.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import rmit.aad.cleanup.R;
import rmit.aad.cleanup.model.CleanUpSite;
import rmit.aad.cleanup.views.cleanup.JoinCleanUp;
import rmit.aad.cleanup.views.cleanup.SiteDetails;

public class CleanUpSiteAdapter extends RecyclerView.Adapter<CleanUpSiteAdapter.ViewHolder>  {
    private Context context;
    private int resource;
    private List<CleanUpSite> list;

    public CleanUpSiteAdapter(Context context, int resource, List<CleanUpSite> list) {
        this.context = context;
        this.resource = resource;
        this.list = list;
    }


    @NonNull
    @Override
    public CleanUpSiteAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(resource, parent, false);
        return new CleanUpSiteAdapter.ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull CleanUpSiteAdapter.ViewHolder holder, int position) {
        CleanUpSite site = list.get(position);
        holder.name.setText(site.getLocationName());
        holder.target.setText(site.getTargetCategory().toString());
        holder.address.setText(site.getLocationAddress());
        holder.textJoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JoinCleanUp.site = site;
                v.getContext().startActivity(new Intent(v.getContext(), JoinCleanUp.class));
            }
        });
        holder.name.setOnClickListener(new View.OnClickListener() {
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
        private final TextView name;
        private final TextView address;
        private final TextView target;
        private final TextView textJoin;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.card_site_name);
            target = itemView.findViewById(R.id.card_site_target);
            address = itemView.findViewById(R.id.card_site_address);
            textJoin = itemView.findViewById(R.id.card_site_text_join);
        }
    }

}
