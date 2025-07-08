package gg.valentinos.alexjoo.Handlers;

import gg.valentinos.alexjoo.Data.LogType;
import org.dynmap.DynmapCommonAPI;
import org.dynmap.DynmapCommonAPIListener;
import org.dynmap.markers.AreaMarker;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerSet;

import static gg.valentinos.alexjoo.VClans.Log;

public class DynmapHandler extends DynmapCommonAPIListener {
    private MarkerSet markerSet;
    @Override
    public void apiEnabled(DynmapCommonAPI api) {
        MarkerAPI markerAPI = api.getMarkerAPI();
        if (markerAPI == null) {
            Log("Dynmap Marker API not available!", LogType.SEVERE);
            return;
        }

        markerSet = markerAPI.getMarkerSet("test");
        if (markerSet == null) {
            markerSet = markerAPI.createMarkerSet("test", "Clan Territories", null, false);
        }

        markerSet.setLayerPriority(10);
        markerSet.setHideByDefault(false);

        // TEST MARKER

//        Marker marker = markerSet.findMarker("test_marker");
//        if (marker == null) {
//            marker = markerSet.createMarker(
//                    "test_marker",         // Unique ID
//                    "Test Marker",          // Label
//                    "world",                // World name
//                    0.5,                    // X (can be integer or double)
//                    64,                     // Y
//                    0.5,                    // Z
//                    markerAPI.getMarkerIcon("default"), // Icon ID
//                    true                    // Persist
//            );
//        }
//        Log("Added a test Marker");

        // TEST ZONE

//        double[] xCorners = {100.0, 116.0, 116.0, 100.0}; // 16x16 area
//        double[] zCorners = {200.0, 200.0, 216.0, 216.0};
//
//        AreaMarker testZone = markerSet.findAreaMarker("test_zone");
//        if (testZone == null) {
//            testZone = markerSet.createAreaMarker(
//                    "test_zone",            // ID
//                    "Test Zone",            // Label
//                    false,                  // Boost flag
//                    "world",                // World name
//                    xCorners,
//                    zCorners,
//                    false                   // Persist (true = saves to markers.yml)
//            );
//        }
//
//        // Style: 50% transparent blue fill, solid blue outline
//        testZone.setFillStyle(0.5, 0x0000FF);
//        testZone.setLineStyle(2, 1.0, 0x0000FF);

        drawTestZoneShape();
        drawTestCombinedZone();
    }

    @Override
    public void apiDisabled(DynmapCommonAPI api) {
        // Optional cleanup logic
    }

    public void drawTestZoneShape() {
        // Pretend these are claimed by clan "test"
        String clanId = "test";
        String world = "world";
        int color = 0x00FF00; // Green

        int[][] claimedChunks = {
                {0, 0}, {1, 0}, {2, 0}, {3, 0},
                {0, 1}, {1, 1}, {3, 1},
                {2, 2}, {3, 2}
        };

        for (int[] pos : claimedChunks) {
            int chunkX = pos[0];
            int chunkZ = pos[1];
            createOrUpdateChunkZone(clanId, chunkX, chunkZ, world, color);
        }
    }
    public void createOrUpdateChunkZone(String clanId, int chunkX, int chunkZ, String world, int colorRGB) {
        String id = "clan_" + clanId + "_chunk_" + chunkX + "_" + chunkZ;
        String label = "Clan " + clanId;

        double x1 = chunkX * 16;
        double x2 = x1 + 16;
        double z1 = chunkZ * 16;
        double z2 = z1 + 16;

        double[] x = {x1, x2, x2, x1};
        double[] z = {z1, z1, z2, z2};

        AreaMarker area = markerSet.findAreaMarker(id);
        if (area == null) {
            area = markerSet.createAreaMarker(id, label, false, world, x, z, true);
        } else {
            area.setCornerLocations(x, z);
        }

        area.setFillStyle(0.5, colorRGB);
        area.setLineStyle(1, 1.0, colorRGB);
        area.setLabel(label);
    }
    public void drawTestCombinedZone() {
        String id = "test_zone_combined";
        String label = "Combined Test Zone";
        String world = "world";
        int color = 0xFF0000; // Red

        // Block coordinates, not chunk coords!
        double[] xCorners = {
                0 * 16,  // (0,0)
                4 * 16,  // (4,0)
                4 * 16,  // (4,1)
                3 * 16,  // (3,1)
                3 * 16,  // (3,3)
                1 * 16,  // (1,3)
                1 * 16,  // (1,2)
                0 * 16,  // (0,2)
                0 * 16   // (0,0) - back to start
        };

        double[] zCorners = {
                0 * 16,
                0 * 16,
                1 * 16,
                1 * 16,
                3 * 16,
                3 * 16,
                2 * 16,
                2 * 16,
                0 * 16
        };

        AreaMarker area = markerSet.findAreaMarker(id);
        if (area == null) {
            area = markerSet.createAreaMarker(id, label, false, world, xCorners, zCorners, true);
        } else {
            area.setCornerLocations(xCorners, zCorners);
        }

        area.setFillStyle(0.3, color);     // Semi-transparent red
        area.setLineStyle(2, 1.0, color);  // Solid red border
    }
}
