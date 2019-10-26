package wtf.racherom.pavi;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;

import android.location.Location;
import android.os.Bundle;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.services.android.navigation.ui.v5.NavigationView;
import com.mapbox.services.android.navigation.ui.v5.NavigationViewOptions;
import com.mapbox.services.android.navigation.ui.v5.OnNavigationReadyCallback;
import com.mapbox.services.android.navigation.v5.milestone.Milestone;
import com.mapbox.services.android.navigation.v5.milestone.MilestoneEventListener;
import com.mapbox.services.android.navigation.v5.navigation.NavigationEventListener;
import com.mapbox.services.android.navigation.v5.navigation.RefreshCallback;
import com.mapbox.services.android.navigation.v5.navigation.RefreshError;
import com.mapbox.services.android.navigation.v5.offroute.OffRouteListener;
import com.mapbox.services.android.navigation.v5.routeprogress.ProgressChangeListener;
import com.mapbox.services.android.navigation.v5.routeprogress.RouteProgress;
import com.mapbox.api.directions.v5.models.DirectionsRoute;


public class NavigationActivity extends AppCompatActivity implements ProgressChangeListener, NavigationEventListener,
        MilestoneEventListener, OffRouteListener, RefreshCallback, OnNavigationReadyCallback {

    private NavigationView navigationView;
    private DirectionsRoute currentRoute;


    private String[] locations = {"48.465226, 7.956282", "48.433708, 7.983138", "48.338843, 8.033090"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token));
        setContentView(R.layout.activity_navigation);
        navigationView = findViewById(R.id.navigationView);
        navigationView.onCreate(savedInstanceState);
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
        super.onDestroy();
        navigationView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        navigationView.onLowMemory();
    }

    @Override
    public void onMilestoneEvent(RouteProgress routeProgress, String instruction, Milestone milestone) {

    }

    @Override
    public void onRunning(boolean running) {

    }

    @Override
    public void onRefresh(DirectionsRoute directionsRoute) {

    }

    @Override
    public void onError(RefreshError error) {

    }

    @Override
    public void userOffRoute(Location location) {

    }

    @Override
    public void onProgressChange(Location location, RouteProgress routeProgress) {

    }

    @Override
    public void onNavigationReady(boolean isRunning) {
        navigationView.startNavigation(
                NavigationViewOptions.builder()
                        .shouldSimulateRoute(true)
                        .directionsRoute(currentRoute)
                        .build()
        );
        navigationView.retrieveMapboxNavigation().addProgressChangeListener(this);
    }
}