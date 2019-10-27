package wtf.racherom.pavi;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.Point;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class KnownLocations {

    public static Point presentation_offenburg_ritterhaus = Point.fromLngLat(7.944517, 48.468788);
    public static Point presentation_offenburg_parkplatz = Point.fromLngLat(7.947398, 48.468690);
    public static Point presentation_offenburg_start = Point.fromLngLat(7.946401, 48.467129);

    //public static Point offenburg_ritterhaus = Point.fromLngLat(7.944517, 48.468788);
    public static Point offenburg_parkplatz = Point.fromLngLat(7.947398, 48.468690);
    public static Point offenburg_parkplatz_al = Point.fromLngLat(7.949293, 48.467907);
    public static Point offenburg_parkplatz_al2 = Point.fromLngLat(7.952467, 48.468518);
    public static Point offenburg_parkplatz_al3 = Point.fromLngLat(7.955771, 48.465910);
    public static Point offenburg_start = Point.fromLngLat(7.941117, 48.467413);
    public static Point offenburg_start_long = Point.fromLngLat(7.904301, 48.453933);


    public static Point Pendelerparkplatz = Point.fromLngLat(7.937955, 48.474951);
    public static Point Offenburg_Ei = Point.fromLngLat(7.909156, 48.473521);
    public static Point Offenburg_Edeka = Point.fromLngLat(7.925121, 48.482172);
    public static Point TBO = Point.fromLngLat(7.904301, 48.453933);
    public static Point DB1 = Point.fromLngLat(7.933278, 48.485415);
    public static Point  MariaUndGeorgDietrichStraße_Banhof = Point.fromLngLat(7.948965,  48.478834);
    public static Point Cityparkhaus = Point.fromLngLat(7.938601, 48.453933);
    public static Point ZentrumWest = Point.fromLngLat(7.936054, 48.469580);
    public static Point Marktplatz = Point.fromLngLat(7.941675, 48.470325);
    public static Point Tiefgarage_Marktplatz = Point.fromLngLat(7.941572, 48.469647);
    public static Point VinciPark = Point.fromLngLat(7.947908, 48.471070);
    public static Point Kronenpl = Point.fromLngLat(7.938779, 48.468405);
    public static Point Forum = Point.fromLngLat(7.938779, 48.468405);
    public static Point oeffi = Point.fromLngLat(7.945319, 48.469218);
    public static Point altOffenburg = Point.fromLngLat( 7.947495,48.469227);
    public static Point HSOffenburg = Point.fromLngLat(7.943336, 48.458125);

    private static Point[] locations = {
            Point.fromLngLat(
                    7.956282,
                    48.465226
            ),
            Point.fromLngLat(
                    7.983138,
                    48.433708
            ),
            Point.fromLngLat(
                    8.033090,
                    48.338843
            ),
            Point.fromLngLat(
                    7.909156,
                    48.473521
            ),
            Point.fromLngLat(
                    7.925121,
                    48.482172
            ),
            Point.fromLngLat(
                    7.904301,
                    48.453933
            ),
            Point.fromLngLat(
                    7.937955,
                    48.474951
            ),
            Point.fromLngLat(
                    7.933278,
                    48.485415
            ),
            Point.fromLngLat(
                    7.948965,
                    48.478834
            ),
            Point.fromLngLat(
                    7.938601,
                    48.453933
            ),
            Point.fromLngLat(
                    7.936054,
                    48.469580
            ),
            Point.fromLngLat(
                    7.941675,
                    48.470325
            ),
            Point.fromLngLat(
                    7.941572,
                    48.469647
            ),
            Point.fromLngLat(
                    7.947908,
                    48.471070
            ),
            Point.fromLngLat(
                    7.938779,
                    48.468405
            ),
            Point.fromLngLat(
                    7.938779,
                    48.468405
            ),
            Point.fromLngLat(
                    7.945319,
                    48.469218
            ),
            Point.fromLngLat(
                    7.948078,
                    48.468947
            ),
            Point.fromLngLat(
                    7.943336,
                    48.458125
            )
    };

    public static List<Feature> getFeatuers(){
        List<Feature> features = new ArrayList<Feature>();

        for (int i=0;i<locations.length;i++)
        {


            Feature feature = Feature.fromGeometry(locations[i]);
            feature.addNumberProperty("occupancy", Math.random()%1);
            if(i==16) {
                feature.addNumberProperty("occupancy", 1);
            }
            if(i==17) {
                feature.addNumberProperty("occupancy", 0);
            }
            feature.addNumberProperty("radius",Math.random()%40);
            features.add(feature);
        }

        return features;
    }
}
