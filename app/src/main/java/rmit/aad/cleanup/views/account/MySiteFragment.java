package rmit.aad.cleanup.views.account;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

import rmit.aad.cleanup.R;
import rmit.aad.cleanup.adapter.CleanUpSiteAdapter;
import rmit.aad.cleanup.adapter.CleanUpSiteFullInfoAdapter;
import rmit.aad.cleanup.model.CleanUpSite;
import rmit.aad.cleanup.utils.PreferenceManager;

public class MySiteFragment extends Fragment {

    final String TAG = "MySiteFragment";
    RecyclerView  my_site_list;
    TextView message_no_site;
    List<CleanUpSite> sites;

    public MySiteFragment() {}


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_my_site, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initPage();
    }

    void initPage() {
        message_no_site = getActivity().findViewById(R.id.message_no_site);
        my_site_list = getActivity().findViewById(R.id.my_site_list);
        initData();
    }

    void initData() {
        PreferenceManager pm = new PreferenceManager(getActivity().getApplicationContext());
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("sites")
                .whereEqualTo("owner", pm.getPrefEmail())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()) {
                            QuerySnapshot result = task.getResult();
                            try {
                                sites = result.toObjects(CleanUpSite.class);

                                Log.d(TAG, String.valueOf(sites.size()));
                                if (sites != null && !sites.isEmpty()) {
                                    CleanUpSiteFullInfoAdapter cleanUpSiteFullInfoAdapter = new CleanUpSiteFullInfoAdapter(getContext(), R.layout.card_site_info_full, sites);
                                    LinearLayoutManager linear_sites = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
                                    my_site_list.setLayoutManager(linear_sites);
                                    my_site_list.setAdapter(cleanUpSiteFullInfoAdapter);
                                    message_no_site.setVisibility(View.GONE);
                                }
                                else {
                                    message_no_site.setVisibility(View.VISIBLE);
                                }
                            } catch (Exception e) {
                                Log.d(TAG, e.toString());
                            }
                        }
                        else {
                            Log.d(TAG, task.getException().toString());
                        }
                    }
                });
    }
}