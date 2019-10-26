package wtf.racherom.pavi;

import android.content.Intent;
import android.graphics.BitmapFactory;

import java.util.ArrayList;
import java.util.List;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.location.Location;
import android.widget.Toast;
import android.os.Bundle;

// classes needed to initialize map
import com.google.gson.JsonElement;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Geometry;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

// classes needed to add the location component
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.modes.CameraMode;

// classes needed to add a marker
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.style.expressions.Expression;
import com.mapbox.mapboxsdk.style.layers.CircleLayer;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import static com.mapbox.mapboxsdk.style.expressions.Expression.exponential;
import static com.mapbox.mapboxsdk.style.expressions.Expression.get;
import static com.mapbox.mapboxsdk.style.expressions.Expression.interpolate;
import static com.mapbox.mapboxsdk.style.expressions.Expression.linear;
import static com.mapbox.mapboxsdk.style.expressions.Expression.literal;
import static com.mapbox.mapboxsdk.style.expressions.Expression.rgb;
import static com.mapbox.mapboxsdk.style.expressions.Expression.rgba;
import static com.mapbox.mapboxsdk.style.expressions.Expression.stop;
import static com.mapbox.mapboxsdk.style.expressions.Expression.zoom;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleOpacity;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleRadius;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.heatmapRadius;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;

// classes to calculate a route
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncherOptions;
import com.mapbox.services.android.navigation.ui.v5.NavigationView;
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute;
import com.mapbox.services.android.navigation.v5.milestone.Milestone;
import com.mapbox.services.android.navigation.v5.milestone.MilestoneEventListener;
import com.mapbox.services.android.navigation.v5.navigation.NavigationEventListener;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import android.util.Log;

// classes needed to launch navigation UI
import android.view.View;
import android.widget.Button;
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncher;
import com.mapbox.services.android.navigation.v5.navigation.RefreshCallback;
import com.mapbox.services.android.navigation.v5.navigation.RefreshError;
import com.mapbox.services.android.navigation.v5.offroute.OffRouteListener;
import com.mapbox.services.android.navigation.v5.routeprogress.ProgressChangeListener;
import com.mapbox.services.android.navigation.v5.routeprogress.RouteProgress;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, MapboxMap.OnMapClickListener, PermissionsListener, ProgressChangeListener, NavigationEventListener,
        MilestoneEventListener, OffRouteListener, RefreshCallback {
    // variables for adding location layer
    private MapView mapView;
    private MapboxMap mapboxMap;

    private Point origin;
    // variables for adding location layer
    private PermissionsManager permissionsManager;
    private LocationComponent locationComponent;
    // variables for calculating and drawing a route
    private DirectionsRoute currentRoute;
    private static final String TAG = "DirectionsActivity";
    private NavigationMapRoute navigationMapRoute;
    // variables needed to initialize navigation
    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token));
        setContentView(R.layout.activity_main);
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
        mapboxMap.setStyle(getString(R.string.navigation_guidance_day), new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                enableLocationComponent(style);

                addDestinationIconSymbolLayer(style);
                addParkingIconSymbolLayer(style);
                mapboxMap.addOnMapClickListener(MainActivity.this);
                button = findViewById(R.id.startButton);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(MainActivity.this, NavigationActivity.class);
                        intent.putExtra("route", currentRoute);
                        intent.putExtra("origin", origin);
                        startActivity(intent);
                    }
                });
            }
        });
    }

    private void addDestinationIconSymbolLayer(@NonNull Style loadedMapStyle) {
        loadedMapStyle.addImage("destination-icon-id",
                BitmapFactory.decodeResource(this.getResources(), R.drawable.mapbox_marker_icon_default));
        GeoJsonSource geoJsonSource = new GeoJsonSource("destination-source-id");
        loadedMapStyle.addSource(geoJsonSource);
        SymbolLayer destinationSymbolLayer = new SymbolLayer("destination-symbol-layer-id", "destination-source-id");
        destinationSymbolLayer.withProperties(
                iconImage("destination-icon-id"),
                iconAllowOverlap(true),
                iconIgnorePlacement(true)
        );
        loadedMapStyle.addLayer(destinationSymbolLayer);
    }

    List<Feature> featureCollection = new ArrayList<Feature>();

    private void addParkingIconSymbolLayer(@NonNull Style loadedMapStyle) {

        loadedMapStyle.addImage("parking-icon-id",
                BitmapFactory.decodeResource(this.getResources(), R.drawable.mapbox_marker_icon_default));
        GeoJsonSource geoJsonSource = new GeoJsonSource("parking-source-id");

        Feature f1 = Feature.fromGeometry( Point.fromLngLat( 7.956282,48.465226));
        f1.addNumberProperty("occupancy",0);
        f1.addNumberProperty("name",0);
        f1.addNumberProperty("radius",20);

        Feature f2 = Feature.fromGeometry( Point.fromLngLat(  8.033090,48.338843));
        f2.addNumberProperty("occupancy",1);
        f2.addNumberProperty("radius",40);


       /* Pendelerparkplatz
        48.474951, 7.937955

        Offenburg Ei
        48.473521, 7.909156

        Offenburg Edeka
        48.482172, 7.925121

        TBO
        48.485415, 7.933278

        DB1
        48.479872, 7.946796

        Maria-und-Georg-Dietrich-Straße (Banhof)
        48.478834, 7.948965

        Cityparkhaus
        48.473003, 7.938601

        Zentrum west
        48.469580, 7.936054

        MArktplatz
        48.470325, 7.941675

        Tiefgarage MArktplatz
        48.469647, 7.941572

        Vinci park
        48.471070, 7.947908

        Kronenpl. 25-24
        77652 Offenburg
        48.468405, 7.938779

        Forum
        48.468405, 7.938779

        Öffi
        48.469218, 7.945319

        alt Offenburg
        48.468947, 7.948078

        HS-Offenburg
        48.458125, 7.943336*/
       // List<Feature> featureCollection = new ArrayList<Feature>();
        featureCollection.add(f1);
        featureCollection.add(f2);


        geoJsonSource.setGeoJson(FeatureCollection.fromFeatures(featureCollection));
        //geoJsonSource.setGeoJson(f2);

        loadedMapStyle.addSource(geoJsonSource);



        CircleLayer destinationSymbolLayer = new CircleLayer("parking-symbol-layer-id","parking-source-id");
        /*destinationSymbolLayer.withProperties(
                iconImage("parking-icon-id"),
                iconAllowOverlap(true),
                iconIgnorePlacement(true)
        );*/
        destinationSymbolLayer.setProperties(
                // Color.rgb(117,52,175)   grün
                // Color.rgb(91,255,159) grün
                // Color.rgb(179,39,29) dunkelste rot
                circleRadius(
                        interpolate(exponential(1.0f), get("DBH"),
                                stop(0, 0f),
                                stop(1, 1f),
                                stop(110, 11f)
                        )
                ),
               //circleRadius(get("radius")),
               /* circleRadius(
                        interpolate(linear(), get("radius") ,
                                stop(0, 10f),
                                stop(10, 20f),
                                stop(20, 30f),
                                stop(110, 40f)
                        )
                ),*/
               /* circleRadius(
                        interpolate(linear(),zoom(),
                                stop(1, 10),
                                stop(16,160)
                        )

                ),*/
                circleColor(
                        interpolate(
                                linear(), get("occupancy"),
                                literal(0), rgb(91,255,159),
                                literal(1), rgb(179,39,29)
                        )
                ),
                circleOpacity(0.5f)
               );

        loadedMapStyle.addLayer(destinationSymbolLayer);
    }


    /*private void addParkingIconSymbolLayer(@NonNull Style loadedMapStyle) {
        loadedMapStyle.addImage("parking-icon-id",
                BitmapFactory.decodeResource(this.getResources(), R.drawable.mapbox_marker_icon_default));
        GeoJsonSource geoJsonSource = new GeoJsonSource("parking-source-id");
        geoJsonSource.setGeoJson(Feature.fromGeometry( Point.fromLngLat( 7.956282,48.465226)));
        loadedMapStyle.addSource(geoJsonSource);

        SymbolLayer destinationSymbolLayer = new SymbolLayer("parking-symbol-layer-id","parking-source-id");
        destinationSymbolLayer.withProperties(
                iconImage("parking-icon-id"),
                iconAllowOverlap(true),
                iconIgnorePlacement(true)
        );
        loadedMapStyle.addLayer(destinationSymbolLayer);
    }*/

    @SuppressWarnings( {"MissingPermission"})
    @Override
    public boolean onMapClick(@NonNull LatLng point) {

        Point destinationPoint = Point.fromLngLat(point.getLongitude(), point.getLatitude());
        origin = Point.fromLngLat(
                locationComponent.getLastKnownLocation().getLongitude(),
                locationComponent.getLastKnownLocation().getLatitude()
        );
// todo fake start + goal
        //destinationPoint = KnownLocations.offenburg_ritterhaus;
        //origin = KnownLocations.offenburg_start;

        GeoJsonSource source = mapboxMap.getStyle().getSourceAs("destination-source-id");
        if (source != null) {
            source.setGeoJson(Feature.fromGeometry(destinationPoint));
        }

        getRoute(origin, destinationPoint);
        button.setEnabled(true);
        button.setBackgroundResource(R.color.mapboxBlue);
        return true;
    }

    private void getRoute(Point origin, Point destination) {
        assert Mapbox.getAccessToken() != null;
        NavigationRoute.builder(this)
                .accessToken(Mapbox.getAccessToken())
                .origin(origin)
                .destination(destination)
                .build()
                .getRoute(new Callback<DirectionsResponse>() {
                    @Override
                    public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                        // You can get the generic HTTP info about the response
                        Timber.d("Response code: %s", response.code());
                        if (response.body() == null) {
                            Timber.e("No routes found, make sure you set the right user and access token.");
                            return;
                        } else if (response.body().routes().size() < 1) {
                            Timber.e("No routes found");
                            return;
                        }

                        currentRoute = response.body().routes().get(0);

                        // Draw the route on the map
                        if (navigationMapRoute != null) {
                            navigationMapRoute.removeRoute();
                        } else {
                            navigationMapRoute = new NavigationMapRoute(null, mapView, mapboxMap, R.style.NavigationMapRoute);
                        }
                        navigationMapRoute.addRoute(currentRoute);
                    }

                    @Override
                    public void onFailure(Call<DirectionsResponse> call, Throwable throwable) {
                        Timber.e("Error: %s", throwable.getMessage());
                    }
                });
    }

    @SuppressWarnings( {"MissingPermission"})
    private void enableLocationComponent(@NonNull Style loadedMapStyle) {
        // Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            // Activate the MapboxMap LocationComponent to show user location
            // Adding in LocationComponentOptions is also an optional parameter
            locationComponent = mapboxMap.getLocationComponent();
            locationComponent.activateLocationComponent(LocationComponentActivationOptions
                    .builder(this, loadedMapStyle).build());
            locationComponent.setLocationComponentEnabled(true);
            // Set the component's camera mode
            locationComponent.setCameraMode(CameraMode.TRACKING);
        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        Toast.makeText(this, R.string.user_location_permission_explanation, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            enableLocationComponent(mapboxMap.getStyle());
        } else {
            Toast.makeText(this, R.string.user_location_permission_not_granted, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
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
}