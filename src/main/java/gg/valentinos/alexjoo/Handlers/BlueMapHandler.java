package gg.valentinos.alexjoo.Handlers;

import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.BlueMapMap;
import de.bluecolored.bluemap.api.markers.ExtrudeMarker;
import de.bluecolored.bluemap.api.markers.MarkerSet;
import de.bluecolored.bluemap.api.markers.POIMarker;
import de.bluecolored.bluemap.api.markers.ShapeMarker;
import de.bluecolored.bluemap.api.math.Color;
import de.bluecolored.bluemap.api.math.Shape;

import static gg.valentinos.alexjoo.VClans.Log;

public class BlueMapHandler {

    private MarkerSet markerSet;

    public BlueMapHandler() {
        // Register BlueMap enable/disable listeners
        BlueMapAPI.onEnable(this::onEnable);
        BlueMapAPI.onDisable(this::onDisable);
    }

    private void onEnable(BlueMapAPI api) {
        Log("BlueMap API is enabled!");


        markerSet = MarkerSet.builder().label("Test").build();

        POIMarker marker = POIMarker.builder()
                .label("My Marker")
                .position(20.0, 65.0, -23.0)
                .maxDistance(1000)
                .build();


        markerSet.getMarkers().put("my-marker-id", marker);

        Shape outer = Shape.createRect(0, 0, 64, 64);
        Shape hole = Shape.createRect(24, 24, 40, 40);

        ShapeMarker markerShape = ShapeMarker.builder()
                .label("Test Area")
                .shape(outer, 500)
                .holes(hole)
                .lineColor(new Color(50, 50, 255, 0.2f))
                .fillColor(new Color(30, 30, 155, 0.5f))
                .build();

        ExtrudeMarker extrudeMarker = ExtrudeMarker.builder()
                .label("Test Extrude")
                .shape(outer, -100, 200)
                .holes(hole)
                .lineColor(new Color(50, 50, 255, 0.2f))
                .fillColor(new Color(30, 30, 155, 0.5f))
                .build();


        // shape marker sucks ass cause its either too high or below ground.
//        markerSet.getMarkers().put("my-area-marker-id", markerShape);
        markerSet.getMarkers().put("my-extrude-marker-id", extrudeMarker);

        api.getWorld("world").ifPresent(world -> {
            for (BlueMapMap map : world.getMaps()) {
                map.getMarkerSets().put("my-marker-set-id", markerSet);
            }
        });


    }

    private void onDisable(BlueMapAPI api) {
        Log("BlueMap API is disabled.");
        markerSet = null;
    }
}
