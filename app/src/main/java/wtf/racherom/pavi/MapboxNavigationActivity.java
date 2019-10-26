package wtf.racherom.pavi;

import android.location.Location;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.mapbox.services.android.navigation.ui.v5.NavigationView;
import com.mapbox.services.android.navigation.ui.v5.map.NavigationMapboxMap;
import com.mapbox.services.android.navigation.v5.navigation.MapboxNavigation;
import com.mapbox.services.android.navigation.v5.routeprogress.ProgressChangeListener;
import com.mapbox.services.android.navigation.v5.routeprogress.RouteProgress;

public class MapboxNavigationActivity extends com.mapbox.services.android.navigation.ui.v5.MapboxNavigationActivity implements ProgressChangeListener {
    private MapboxNavigation mapboxNavigation;

    @Override
    public void onNavigationRunning() {
        super.onNavigationRunning();
        NavigationView navigationView = findViewById(R.id.navigationView);
        mapboxNavigation = navigationView.retrieveMapboxNavigation();
        mapboxNavigation.addProgressChangeListener(this);
    }

    @Override
    public void onProgressChange(Location location, RouteProgress routeProgress) {
        System.out.println("asd");
    }
}
