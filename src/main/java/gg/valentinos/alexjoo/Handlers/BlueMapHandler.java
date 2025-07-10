package gg.valentinos.alexjoo.Handlers;

import com.flowpowered.math.vector.Vector2i;
import com.technicjelle.BMUtils.Cheese;
import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.BlueMapMap;
import de.bluecolored.bluemap.api.markers.ExtrudeMarker;
import de.bluecolored.bluemap.api.markers.MarkerSet;
import de.bluecolored.bluemap.api.math.Shape;
import gg.valentinos.alexjoo.Data.Clan;

import java.util.Collection;

import static gg.valentinos.alexjoo.VClans.Log;

public class BlueMapHandler {

    private MarkerSet markerSet;
    private final static String MARKER_SET_ID = "clan-zones";

    public BlueMapHandler() {
        BlueMapAPI.onEnable(this::onEnable);
        BlueMapAPI.onDisable(this::onDisable);
    }

    private void onEnable(BlueMapAPI api) {
        Log("BlueMap API is enabled!");

        markerSet = MarkerSet.builder().label("Test").build();

//        Vector2i test = new Vector2i();
//        Shape shape = new Shape();

        api.getWorld("world").ifPresent(world -> {
            for (BlueMapMap map : world.getMaps()) {
                map.getMarkerSets().put(MARKER_SET_ID, markerSet);
            }
        });
    }
    private void onDisable(BlueMapAPI api) {
        Log("BlueMap API is disabled.");
        markerSet = null;
    }

    private final static int CHUNK_SIZE = 16;
    private final static int EXTRUDE_MIN_Y = -10;
    private final static int EXTRUDE_MAX_Y = 120;

    public void drawClanTerritory(Clan clan) {
        Vector2i[] chunkCoordinates = clan.getChunks().stream()
                .map(chunk -> new Vector2i(chunk.getX(), chunk.getZ()))
                .toArray(Vector2i[]::new);
        Collection<Cheese> platter = Cheese.createPlatterFromChunks(chunkCoordinates);
        int i = 0;
        for (Cheese cheese : platter) {
            ExtrudeMarker chunkMarker = new ExtrudeMarker.Builder()
                    .label(clan.getName())
                    .shape(cheese.getShape(), -100, 200)
                    .holes(cheese.getHoles().toArray(Shape[]::new))
                    .build();
            markerSet.put("town-" + clan.getName() + "-segment-" + (i++), chunkMarker);
        }


    }

}
