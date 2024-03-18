package rmit.aad.cleanup.views.cleanup;

import static rmit.aad.cleanup.views.cleanup.JoinCleanUp.marker_address;
import static rmit.aad.cleanup.views.cleanup.JoinCleanUp.marker_name;
import static rmit.aad.cleanup.views.cleanup.JoinCleanUp.marker_target;
import static rmit.aad.cleanup.views.cleanup.JoinCleanUp.selected_site_marker_info;
import static rmit.aad.cleanup.views.cleanup.JoinCleanUp.site;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

import rmit.aad.cleanup.R;
import rmit.aad.cleanup.model.CleanUpSite;
import rmit.aad.cleanup.utils.CommonFunctions;
import rmit.aad.cleanup.utils.PreferenceManager;

public class MapSiteSelectFragment extends Fragment implements
        ActivityCompat.OnRequestPermissionsResultCallback,
        GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener,
        GoogleMap.OnMarkerClickListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener {

    final String TAG = "MapSiteSelectFragment";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private boolean permissionDenied = false;
    GoogleMap mMap;
    GoogleApiClient mGoogleApiClient = null;
    Location mLastLocation;
    Marker mCurrLocationMarker;
    BitmapDescriptor marker_public_site;

    public MapSiteSelectFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map_cleanup, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        marker_public_site = CommonFunctions.getMarkerIconFromDrawable(getContext(), R.drawable.ic_location_marker);

        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }
    }

    private OnMapReadyCallback callback = new OnMapReadyCallback() {
        @Override
        public void onMapReady(GoogleMap googleMap) {
            mMap = googleMap;
            if (mGoogleApiClient == null) {
                buildGoogleApiClient();
            }
            enableMyLocation();
            mMap.getUiSettings().setZoomControlsEnabled(true);
            mMap.getUiSettings().setZoomGesturesEnabled(true);
            mMap.getUiSettings().setCompassEnabled(true);

            mMap.setOnMarkerClickListener(MapSiteSelectFragment.this);
            mMap.setOnMyLocationClickListener(MapSiteSelectFragment.this);
            mMap.setOnMyLocationButtonClickListener(MapSiteSelectFragment.this);

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            Query query = db.collection("sites");
            PreferenceManager pm = new PreferenceManager(getActivity().getApplicationContext());
            if (!(pm.isLoggedIn() && pm.isSuperUser())) {
                query = query.whereNotEqualTo("isPrivate", true);
            }

            query
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if(task.isSuccessful()) {
                                QuerySnapshot result = task.getResult();
                                try {
                                    List<CleanUpSite> sites = result.toObjects(CleanUpSite.class);
                                    if (sites.size() != 0) {
                                        for (CleanUpSite site: sites) {
                                            LatLng location = new LatLng(site.getLatitude(), site.getLongitude());
                                            BitmapDescriptor marker = !site.getIsPrivate() ? BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN) : marker_public_site;
                                            mMap
                                                    .addMarker(new MarkerOptions()
                                                            .position(location)
                                                            .title(site.getLocationName())
                                                            .snippet(site.getEventDescription())
                                                            .icon(marker))
                                                    .setTag(site.getId());

                                            mMap.animateCamera(CameraUpdateFactory.zoomTo(15.0f));
                                            mMap.moveCamera(CameraUpdateFactory.newLatLng(location));

                                        }
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
    };

    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("sites").document(marker.getTag().toString())
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            site = task.getResult().toObject(CleanUpSite.class);
                            marker_name.setText(site.getLocationName());
                            marker_address.setText(site.getLocationAddress());
                            marker_target.setText(site.getTargetCategory().toString());
                            selected_site_marker_info.setVisibility(View.VISIBLE);
                        }
                    }
                });

        return false;
    }

    @Override
    public boolean onMyLocationButtonClick() {
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        Toast.makeText(getActivity().getApplicationContext(), "Current location:\n" + location, Toast.LENGTH_LONG).show();
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    private boolean checkLocationPermission() {
        return (ActivityCompat.checkSelfPermission(getActivity().getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(getActivity().getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
        );
    }

    @SuppressLint("MissingPermission")
    private void enableMyLocation() {
        //if location permission is granted -> enable for map
        if (checkLocationPermission()) {
            mMap.setMyLocationEnabled(true);
        }
        //if location permission is not granted -> request
        else {
            String permissions[] = {
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
            };
            ActivityCompat.requestPermissions(getActivity(), permissions, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults.length > 0 && (grantResults[0] == PackageManager.PERMISSION_GRANTED || grantResults[1] == PackageManager.PERMISSION_GRANTED)) {
            enableMyLocation();
        } else {
            Toast.makeText(getActivity().getApplicationContext(), "Permission denied", Toast.LENGTH_LONG).show();
            permissionDenied = true;
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        if (permissionDenied) {
            enableMyLocation();
            permissionDenied = false;
        }
    }

    LocationRequest mLocationRequest;

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (checkLocationPermission()) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                    mLocationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    @Override
    public void onLocationChanged(Location location) {

    }

}