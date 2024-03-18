package rmit.aad.cleanup.views.cleanup;

import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
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

import rmit.aad.cleanup.R;
import rmit.aad.cleanup.model.CleanUpSite;
import rmit.aad.cleanup.utils.CommonFunctions;

public class MapSiteDetailFragment extends Fragment implements
        ActivityCompat.OnRequestPermissionsResultCallback,
        GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener,
        GoogleMap.OnMarkerClickListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener {

    final String TAG = "MapSiteDetailFragment";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private boolean permissionDenied = false;
    GoogleMap mMap;
    GoogleApiClient mGoogleApiClient = null;
    BitmapDescriptor marker_public_site;

    public MapSiteDetailFragment() {}

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

            mMap.setOnMarkerClickListener(MapSiteDetailFragment.this);
            mMap.setOnMyLocationClickListener(MapSiteDetailFragment.this);
            mMap.setOnMyLocationButtonClickListener(MapSiteDetailFragment.this);

            CleanUpSite site = SiteDetails.site;
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
    };

    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
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