package gg.valentinos.alexjoo.Handlers;

import com.flowpowered.math.vector.Vector2i;
import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.BlueMapMap;
import de.bluecolored.bluemap.api.markers.MarkerSet;

import static gg.valentinos.alexjoo.VClans.Log;

public class BlueMapHandler {

    private MarkerSet markerSet;
    private final static String MARKER_SET_ID = "clan-zones";
    private final static int CHUNK_SIZE = 16;
    private final static int EXTRUDE_MIN_Y = -10;
    private final static int EXTRUDE_MAX_Y = 120;

//    private record Vec2(int x, int z) {
//    }
//
//    private record ChunkCorners(Vec2 northWest, Vec2 southEast) {
//    }

    public BlueMapHandler() {
        // Register BlueMap enable/disable listeners
        BlueMapAPI.onEnable(this::onEnable);
        BlueMapAPI.onDisable(this::onDisable);
    }

    private void onEnable(BlueMapAPI api) {
        Log("BlueMap API is enabled!");
        Vector2i test = new Vector2i();

        markerSet = MarkerSet.builder().label("Test").build();

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

//    public void drawClanTerritory2(Clan clan) {
//        Vector2i[] chunkCoordinates = clan.getChunks().stream()
//                .map(chunk -> new Vector2i(chunk.getX(), chunk.getZ()))
//                .toArray(Vector2i[]::new);
//        Collection<Cheese> platter = Cheese.createPlatterFromChunks(chunkCoordinates);
//        int i = 0;
//        for (Cheese cheese : platter) {
//            ExtrudeMarker chunkMarker = new ExtrudeMarker.Builder()
//                    .label(clan.getName())
//                    .shape(cheese.getShape(), -100, 200)
//                    .holes(cheese.getHoles().toArray(Shape[]::new))
//                    .build();
//            markerSet.put("town-" + clan.getName() + "-segment-" + (i++), chunkMarker);
//        }
//    }

//    public void drawClanTerritory(Clan clan) {
//        HashSet<ClanChunk> clanChunks = clan.getChunks();
//
//        Set<Vec2> claimedChunks = new HashSet<>();
//
//        int minX = 0, maxX = 0, minZ = 0, maxZ = 0;
//        boolean valuesDefined = false;
//        for (ClanChunk chunk : clanChunks) {
//            ChunkCorners chunkCorners = getChunkCorners(chunk.getX(), chunk.getZ());
//            claimedChunks.add(new Vec2(chunk.getX(), chunk.getZ()));
//            if (!valuesDefined) {
//                minX = chunkCorners.northWest().x();
//                minZ = chunkCorners.northWest().z();
//                maxX = chunkCorners.southEast().x();
//                maxZ = chunkCorners.southEast().z();
//                valuesDefined = true;
//            } else {
//                minX = Math.min(minX, chunkCorners.northWest().x());
//                minZ = Math.min(minZ, chunkCorners.northWest().z());
//                maxX = Math.max(maxX, chunkCorners.southEast().x());
//                maxZ = Math.max(maxZ, chunkCorners.southEast().z());
//            }
//        }
//        Shape outerShape = Shape.createRect(minX, minZ, maxX, maxZ);
//        List<Shape> holes = getHolesFromTerritory(new Vec2(minX, minZ), new Vec2(maxX, maxZ), claimedChunks);
//
//        Log("Outer Shape: " + outerShape.getMin() + ":" + outerShape.getMax());
//        for (Shape hole : holes) {
//            Log("Hole: " + hole.getMin() + ":" + hole.getMax());
//        }
//        addClanTerritoryMarker(clan, outerShape, holes);
//    }
//
//    private ChunkCorners getChunkCorners(int x, int z) {
//        // returns the north-west and south-east corners of a chunk
//        return new ChunkCorners(
//                new Vec2(CHUNK_SIZE * x, CHUNK_SIZE * z),
//                new Vec2(CHUNK_SIZE * (x + 1), CHUNK_SIZE * (z + 1))
//        );
//    }
//
//    private List<Shape> getHolesFromTerritory(Vec2 min, Vec2 max, Set<Vec2> claimedChunks) {
//        List<Shape> result = new ArrayList<>();
//
//        int minChunkX = min.x() / CHUNK_SIZE;
//        int maxChunkX = (max.x() - 1) / CHUNK_SIZE;
//        int minChunkZ = min.z() / CHUNK_SIZE;
//        int maxChunkZ = (max.z() - 1) / CHUNK_SIZE;
//
//        for (int x = minChunkX; x <= maxChunkX; x++) {
//            for (int z = minChunkZ; z < maxChunkZ; z++) {
//                if (!claimedChunks.contains(new Vec2(x, z))) {
//                    ChunkCorners corners = getChunkCorners(x, z);
//                    result.add(Shape.createRect(
//                            corners.northWest().x(), corners.northWest().z(),
//                            corners.southEast().x(), corners.southEast().z()));
//                }
//            }
//        }
//
//        Log(String.valueOf(result.size()));
//        return result;
//    }
//
//    private void addClanTerritoryMarker(Clan clan, Shape outerShape, List<Shape> holes) {
//        if (markerSet == null) {
//            Log("Tried to draw clan territory but BlueMap markerSet is null!");
//            return;
//        }
//
//
//        String label = clan.getName();
//        String id = label + "-marker";
//        Color fillColor = new Color(30, 30, 155, 0.2f);
//        Color lineColor = new Color(50, 50, 255, 1f);
//
//        ExtrudeMarker extrudeMarker = ExtrudeMarker.builder()
//                .label(label)
//                .shape(outerShape, EXTRUDE_MIN_Y, EXTRUDE_MAX_Y)
//                .holes(holes.toArray(new Shape[0]))
//                .lineColor(lineColor)
//                .fillColor(fillColor)
//                .build();
//
//        markerSet.getMarkers().put(id, extrudeMarker);
//    }
}
