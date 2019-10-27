package wtf.racherom.pavi;

import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import android.location.Location;
import android.os.Bundle;
import android.widget.Toast;

import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.services.android.navigation.ui.v5.NavigationView;
import com.mapbox.services.android.navigation.ui.v5.NavigationViewOptions;
import com.mapbox.services.android.navigation.ui.v5.OnNavigationReadyCallback;
import com.mapbox.services.android.navigation.ui.v5.map.NavigationMapboxMap;
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute;
import com.mapbox.services.android.navigation.v5.milestone.Milestone;
import com.mapbox.services.android.navigation.v5.milestone.MilestoneEventListener;
import com.mapbox.services.android.navigation.v5.navigation.DirectionsRouteType;
import com.mapbox.services.android.navigation.v5.navigation.MapboxNavigation;
import com.mapbox.services.android.navigation.v5.navigation.MapboxNavigationOptions;
import com.mapbox.services.android.navigation.v5.navigation.NavigationEventListener;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;
import com.mapbox.services.android.navigation.v5.navigation.RefreshCallback;
import com.mapbox.services.android.navigation.v5.navigation.RefreshError;
import com.mapbox.services.android.navigation.v5.offroute.OffRoute;
import com.mapbox.services.android.navigation.v5.offroute.OffRouteListener;
import com.mapbox.services.android.navigation.v5.route.FasterRoute;
import com.mapbox.services.android.navigation.v5.routeprogress.ProgressChangeListener;
import com.mapbox.services.android.navigation.v5.routeprogress.RouteProgress;
import com.mapbox.api.directions.v5.models.DirectionsRoute;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;


public class NavigationActivity extends AppCompatActivity implements ProgressChangeListener, NavigationEventListener,
        MilestoneEventListener, OffRouteListener, RefreshCallback, OnNavigationReadyCallback {

    private NavigationView navigationView;
    private DirectionsRoute currentRoute;
    private MapboxNavigation navigation;
    private int locationsIndex = 0;
    private boolean loadRoute = false;
    private int reRouteMax = 1;
    private int reRoutCount = 0;
    private long lastLoadRoute = 0;
    private boolean once = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token));
        setContentView(R.layout.activity_navigation);
        navigationView = findViewById(R.id.navigationView);
        Intent intent = getIntent();
        currentRoute = (DirectionsRoute) intent.getSerializableExtra("route");
        Point origin = (Point) intent.getSerializableExtra("origin");
        CameraPosition initialPosition = new CameraPosition.Builder()
                .target(new LatLng(origin.latitude(), origin.longitude()))
                .zoom(16)
                .build();

        navigationView.onCreate(savedInstanceState);
        navigationView.initialize(this, initialPosition);
    }

    @Override
    protected void onStart() {
        super.onStart();
        navigationView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        navigationView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        navigationView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        navigationView.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        navigationView.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        // Toast.makeText(getApplicationContext(), "onDestroy", Toast.LENGTH_SHORT).show();
        super.onDestroy();
        navigationView.onDestroy();
        shutdownNavigation();
    }

    @Override
    public void onLowMemory() {
        // Toast.makeText(getApplicationContext(), "onLowMemory", Toast.LENGTH_SHORT).show();
        super.onLowMemory();
        navigationView.onLowMemory();
    }

    @Override
    public void onMilestoneEvent(RouteProgress routeProgress, String instruction, Milestone milestone) {
        // Toast.makeText(getApplicationContext(), "onMilestoneEvent", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRunning(boolean running) {
        // Toast.makeText(getApplicationContext(), "onRunning", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRefresh(DirectionsRoute directionsRoute) {
        // Toast.makeText(getApplicationContext(), "onRefresh", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onError(RefreshError error) {
        // Toast.makeText(getApplicationContext(), "onError", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void userOffRoute(Location location) {
        // Toast.makeText(getApplicationContext(), "onError", Toast.LENGTH_SHORT).show();
    }


    private void getRoute(Point origin, Point destination) {
        // Toast.makeText(getApplicationContext(), "getRoute", Toast.LENGTH_SHORT).show();
        NavigationRoute.builder(this)
                .origin(origin)
                .destination(destination)
                .accessToken(Mapbox.getAccessToken())
                .build().getRoute(new Callback<DirectionsResponse>() {
            @Override
            public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                // Toast.makeText(getApplicationContext(), "getRoute+onResponse", Toast.LENGTH_SHORT).show();
                // You can get the generic HTTP info about the response
                System.out.println("onResponse+loadRoute:" + loadRoute);

                Timber.d("Response code: %s", response.code());
                System.out.println("onResponse+response.code:" + response.code());
                if (response.body() == null) {
                    Timber.e("No routes found, make sure you set the right user and access token.");
                    return;
                } else if (response.body().routes().size() < 1) {
                    Timber.e("No routes found");
                    return;
                }

                currentRoute = response.body().routes().get(0);
                /*
                NavigationMapboxMap map = navigationView.retrieveNavigationMapboxMap();
                map.clearMarkers();
                navigation.startNavigation(currentRoute, DirectionsRouteType.NEW_ROUTE);
                */
                navigationView.stopNavigation();
                navigateTo(currentRoute);
                loadRoute = false;
                lastLoadRoute = System.currentTimeMillis();
            }

            @Override
            public void onFailure(Call<DirectionsResponse> call, Throwable throwable) {
                Timber.e("Error: %s", throwable.getMessage());
                loadRoute = false;
            }

        });
    }

    @Override
    public void onProgressChange(Location location, RouteProgress routeProgress) {
        double dist = routeProgress.distanceRemaining();
        System.out.println("onProgressChange+dist:" + dist);
        long difference = System.currentTimeMillis() - lastLoadRoute;
        // !loadRoute && difference > 1000 && reRoutCount <= reRouteMax
        // todo fake second goal
        if (once && dist < 300.0){
            once = false;
            loadRoute = true;
            System.out.println("onProgressChange+loadRoute:" + loadRoute);
            getRoute(
                    Point.fromLngLat(location.getLongitude(), location.getLatitude()),
                    KnownLocations.presentation_offenburg_parkplatz
            );
            reRoutCount++;
        }
    }

    @Override
    public void onNavigationReady(boolean isRunning) {
        // Toast.makeText(getApplicationContext(), "onNavigationReady", Toast.LENGTH_SHORT).show();
        if (!isRunning) {
            navigateTo(currentRoute);
        }
    }

    private void navigateTo(DirectionsRoute route) {
        NavigationViewOptions options =
                NavigationViewOptions.builder()
                        .shouldSimulateRoute(true)
                        .directionsRoute(route)
                        .navigationOptions(MapboxNavigationOptions.builder()
                                .enableRefreshRoute(false)
                                .enableFasterRouteDetection(false)
                                .isDebugLoggingEnabled(true)
                                .build())
                        .build();

        navigationView.startNavigation(options);
        navigation = navigationView.retrieveMapboxNavigation();
        navigation.removeNavigationEventListener(this);
        navigation.removeProgressChangeListener(this);
        navigation.addProgressChangeListener(this);
    }

    private void shutdownNavigation() {
        navigation.removeNavigationEventListener(this);
        navigation.removeProgressChangeListener(this);
        navigation.onDestroy();
    }
}