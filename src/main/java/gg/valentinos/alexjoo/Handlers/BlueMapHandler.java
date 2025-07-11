package gg.valentinos.alexjoo.Handlers;

import com.flowpowered.math.vector.Vector2i;
import com.technicjelle.BMUtils.Cheese;
import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.BlueMapMap;
import de.bluecolored.bluemap.api.markers.ExtrudeMarker;
import de.bluecolored.bluemap.api.markers.MarkerSet;
import de.bluecolored.bluemap.api.math.Color;
import de.bluecolored.bluemap.api.math.Shape;
import gg.valentinos.alexjoo.Data.Clan;
import gg.valentinos.alexjoo.VClans;

import java.util.Collection;
import java.util.List;

import static gg.valentinos.alexjoo.VClans.Log;

public class BlueMapHandler {

    private MarkerSet markerSet;
    private final static String MARKER_SET_ID = "clan-zones";
    private final static String MARKER_SET_LABEL = "Clan Zones";
    private final static float ALPHA_VALUE = 0.2f;
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
    }
    private void onDisable(BlueMapAPI api) {
        markerSet = null;
    }

    private void drawAllClanTerritories() {
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
        Color lineColor = new Color(color.get(0), color.get(1), color.get(2), 1);
        for (Cheese cheese : platter) {
            ExtrudeMarker chunkMarker = new ExtrudeMarker.Builder()
                    .label(clan.getName())
                    .fillColor(fillColor)
                    .lineColor(lineColor)
                    .shape(cheese.getShape(), EXTRUDE_MIN_Y, EXTRUDE_MAX_Y)
                    .holes(cheese.getHoles().toArray(Shape[]::new))
                    .build();
            markerSet.put("clan-" + clan.getName() + "-segment-" + (i++), chunkMarker);
        }


    }

    public void removeClanTerritory(Clan clan) {
        markerSet.getMarkers().keySet().removeIf(key -> key.startsWith("clan-") && key.contains("-segment-") && key.contains(clan.getName()));
    }

}
