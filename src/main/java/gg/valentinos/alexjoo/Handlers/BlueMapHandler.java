package gg.valentinos.alexjoo.Handlers;

import com.flowpowered.math.vector.Vector2d;
import com.flowpowered.math.vector.Vector2i;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.technicjelle.BMUtils.Cheese;
import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.BlueMapMap;
import de.bluecolored.bluemap.api.markers.ExtrudeMarker;
import de.bluecolored.bluemap.api.markers.MarkerSet;
import de.bluecolored.bluemap.api.math.Color;
import de.bluecolored.bluemap.api.math.Shape;
import gg.valentinos.alexjoo.Data.ClanData.Clan;
import gg.valentinos.alexjoo.VClans;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static gg.valentinos.alexjoo.VClans.Log;

public class BlueMapHandler {

    private MarkerSet markerSet;
    private final static String MARKER_SET_ID = "vclans-zones";
    private final static String MARKER_SET_LABEL = "VClans Zones";
    private final static float ALPHA_VALUE = 0.2f;
    private final static float LINE_ALPHA_VALUE = 1f;
    private final int EXTRUDE_MIN_Y;
    private final int EXTRUDE_MAX_Y;

    public BlueMapHandler() {
        EXTRUDE_MAX_Y = VClans.getInstance().getConfig().getInt("settings.extrude-max-y");
        EXTRUDE_MIN_Y = VClans.getInstance().getConfig().getInt("settings.extrude-min-y");
        BlueMapAPI.onEnable(this::onEnable);
        BlueMapAPI.onDisable(this::onDisable);
    }

    private void onEnable(BlueMapAPI api) {
        Log("BlueMap API is enabled!");

        markerSet = MarkerSet.builder().label(MARKER_SET_LABEL).build();

        api.getWorld("world").ifPresent(world -> {
            for (BlueMapMap map : world.getMaps()) {
                map.getMarkerSets().put(MARKER_SET_ID, markerSet);
            }
        });

        drawAllClanTerritories();
        drawAllWorldGuardRegions();
    }
    private void onDisable(BlueMapAPI api) {
        markerSet = null;
    }

    public void drawAllWorldGuardRegions() {
        if (!VClans.getInstance().getWorldGuardHandler().isEnabled()) return;
        if (markerSet == null) return;
        Map<String, ProtectedRegion> regionsMap = VClans.getInstance().getWorldGuardHandler().getRegions();
        for (Map.Entry<String, ProtectedRegion> regionEntry : regionsMap.entrySet()) {
            ProtectedRegion region = regionEntry.getValue();
            List<BlockVector2> blockPoints = region.getPoints();
            List<Vector2d> points = new ArrayList<>();
            for (BlockVector2 point : blockPoints) {
                points.add(new Vector2d(point.x(), point.z()));
            }
            Shape shape = new Shape(points);
            String label = regionEntry.getKey();
            org.bukkit.Color color = VClans.getInstance().getWorldGuardHandler().getColor();
            Color fillColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), ALPHA_VALUE);
            // Have NO idea on why the line color is invisible
            Color lineColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), LINE_ALPHA_VALUE);
            Log("!!!!!!!" + "lineColor: " + lineColor + " alpha: " + lineColor.getAlpha());
            ExtrudeMarker regionMarker = new ExtrudeMarker.Builder()
                    .label(label)
                    .fillColor(fillColor)
                    .lineColor(lineColor)
                    .shape(shape, region.getMaximumPoint().y(), region.getMinimumPoint().y())
                    .build();
            markerSet.put("region-" + region.getId(), regionMarker);
        }
    }
    private void drawAllClanTerritories() {
        if (markerSet == null) return;
        for (Clan clan : VClans.getInstance().getClanHandler().getClans()) {
            drawClanTerritory(clan);
        }
    }
    public void drawClanTerritory(Clan clan) {
        if (markerSet == null) return;

        Vector2i[] chunkCoordinates = clan.getChunks().stream()
                .map(chunk -> new Vector2i(chunk.getX(), chunk.getZ()))
                .toArray(Vector2i[]::new);

        Collection<Cheese> platter = Cheese.createPlatterFromChunks(chunkCoordinates);
        int i = 0;
        List<Integer> color = clan.getColor();
        Color fillColor = new Color(color.get(0), color.get(1), color.get(2), ALPHA_VALUE);
        Color lineColor = new Color(color.get(0), color.get(1), color.get(2), LINE_ALPHA_VALUE);
        for (Cheese cheese : platter) {
            ExtrudeMarker chunkMarker = new ExtrudeMarker.Builder()
                    .label(clan.getStrippedName())
                    .fillColor(fillColor)
                    .lineColor(lineColor)
                    .shape(cheese.getShape(), EXTRUDE_MIN_Y, EXTRUDE_MAX_Y)
                    .holes(cheese.getHoles().toArray(Shape[]::new))
                    .build();
            markerSet.put("clan-" + clan.getId() + "-segment-" + (i++), chunkMarker);
        }
    }

    public void removeClanTerritory(Clan clan) {
        markerSet.getMarkers().keySet().removeIf(key -> key.startsWith("clan-") && key.contains("-segment-") && key.contains(clan.getId()));
    }

}
