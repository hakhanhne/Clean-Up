package rmit.aad.cleanup.views.cleanup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

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
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import rmit.aad.cleanup.R;
import rmit.aad.cleanup.model.CleanUpSite;
import rmit.aad.cleanup.utils.CommonFunctions;
import rmit.aad.cleanup.utils.DirectionsJSONParser;
import rmit.aad.cleanup.utils.PreferenceManager;

public class MapSitesFragment extends Fragment implements
        ActivityCompat.OnRequestPermissionsResultCallback,
        GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener,
        GoogleMap.OnMarkerClickListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener {

    final String TAG = "MapSitesFragment";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private boolean permissionDenied = false;
    GoogleMap mMap;
    GoogleApiClient mGoogleApiClient = null;
    BitmapDescriptor marker_public_site;
    ProgressDialog getRouteProgressDialog;

    public MapSitesFragment() {}

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

            mMap.setOnMarkerClickListener(MapSitesFragment.this);
            mMap.setOnMyLocationClickListener(MapSitesFragment.this);
            mMap.setOnMyLocationButtonClickListener(MapSitesFragment.this);

            mMap.clear();
            List<CleanUpSite> sites = CleanUpSites.sites;
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
        }
    };

    private void showSiteDetail(CleanUpSite site) {

    }

    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        LatLng origin = new LatLng(mMap.getMyLocation().getLatitude(), mMap.getMyLocation().getLongitude());
        drawPolylines(origin, marker.getPosition());
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

//    @Override
//    public void onLocationChanged(@NonNull Location location) {
//        mLastLocation = location;
//        if (mCurrLocationMarker != null) {
//            mCurrLocationMarker.remove();
//        }
//        //Showing Current Location Marker on Map
//        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
//        MarkerOptions markerOptions = new MarkerOptions();
//        markerOptions.position(latLng);
//        LocationManager locationManager = null;
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
//            locationManager = (LocationManager)
//                    getActivity().getSystemService(Context.LOCATION_SERVICE);
//        }
//        String provider = locationManager.getBestProvider(new Criteria(), true);
//        if (!checkLocationPermission()) {
//            return;
//        }
//        Location locations = locationManager.getLastKnownLocation(provider);
//        List<String> providerList = locationManager.getAllProviders();
//        if (null != locations && null != providerList && providerList.size() > 0) {
//            double longitude = locations.getLongitude();
//            double latitude = locations.getLatitude();
//            Geocoder geocoder = new Geocoder(getActivity().getApplicationContext(),
//                    Locale.getDefault());
//            try {
//                List<Address> listAddresses = geocoder.getFromLocation(latitude,
//                        longitude, 1);
//                if (null != listAddresses && listAddresses.size() > 0) {
//                    String state = listAddresses.get(0).getAdminArea();
//                    String country = listAddresses.get(0).getCountryName();
//                    String subLocality = listAddresses.get(0).getSubLocality();
//                    markerOptions.title("" + latLng + "," + subLocality + "," + state
//                            + "," + country);
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
//        mCurrLocationMarker = mMap.addMarker(markerOptions);
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
//        mMap.animateCamera(CameraUpdateFactory.zoomTo(11));
//        if (mGoogleApiClient != null) {
//            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient,
//                    this);
//        }
//    }

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

    private void drawPolylines(LatLng origin, LatLng dest) {
        getRouteProgressDialog = new ProgressDialog(getContext());
        getRouteProgressDialog.setMessage("Please Wait, Polyline between two locations is building.");
        getRouteProgressDialog.setCancelable(false);
        getRouteProgressDialog.show();
        String url = getDirectionsUrl(origin, dest);
        Log.d("url", url);
        DownloadTask downloadTask = new DownloadTask();
        downloadTask.execute(url);
    }


//    @Override
//    public void onPause() {
//        super.onPause();
//        //stop location updates when Activity is no longer active
//        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
//            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, MapSitesFragment.this);
//        }
//    }

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

    private class DownloadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... url) {
            String data = "";
            try {
                data = downloadUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            ParserTask parserTask = new ParserTask();
            parserTask.execute(result);

        }
    }


    /**
     * A class to parse the Google Places in JSON format
     */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {
            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();
                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            getRouteProgressDialog.dismiss();
            if ( result == null || result.isEmpty()) {
                Toast.makeText(getActivity().getApplicationContext(), "No available route from your current location\nTry another site :( ", Toast.LENGTH_LONG).show();
                return;
            }
            Log.d("result", result.toString());

            ArrayList points;
            PolylineOptions lineOptions = null;

            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList();
                lineOptions = new PolylineOptions();

                List<HashMap<String, String>> path = result.get(i);

                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                lineOptions.addAll(points);
                lineOptions.width(12);
                lineOptions.color(Color.RED);
                lineOptions.geodesic(true);

            }

            // Drawing polyline in the Google Map for the i-th route
            mMap.addPolyline(lineOptions);
        }
    }

    public static String getMetadata(Context context, String key) {
        try {
            Bundle metaData = context.getPackageManager()
                    .getApplicationInfo(context.getPackageName(),
                            PackageManager.GET_META_DATA).metaData;
            return metaData.get(key).toString();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getDirectionsUrl(LatLng origin, LatLng dest) {
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        // Sensor enabled
        String sensor = "sensor=false";
        String mode = "mode=driving";
        // Maps API Key
        String api_key = "key="+getMetadata(getActivity().getApplicationContext(), "com.google.android.geo.API_KEY");

        String parameters = str_origin + "&" + str_dest + "&" + api_key;

        String url = "https://maps.googleapis.com/maps/api/directions/json?" + sensor + "&" + mode + "&" + parameters;

        return url;
    }

    /**
     * A method to download json data from url
     */
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.connect();

            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();

            br.close();
            Log.d("data", data);

        } catch (Exception e) {
            Log.d("Exception", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }


}