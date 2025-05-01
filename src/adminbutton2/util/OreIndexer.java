package adminbutton2.util;

import arc.Events;
import arc.math.Mathf;
import arc.struct.IntSeq;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.game.EventType;
import mindustry.type.Item;
import mindustry.world.Tile;

public class OreIndexer {
    private static final int quadrantSize = 20;
    private IntSeq[][][] ores;
    private int quadrantWidth = 0, quadrantHeight = 0;

    public OreIndexer() {
        Events.run(EventType.WorldLoadEvent.class, () -> {
            ores = new IntSeq[Vars.content.blocks().size][][];
            quadrantWidth = Vars.world.width() / quadrantSize + (Vars.world.width() % quadrantSize == 0 ? 0 : 1);
            quadrantHeight = Vars.world.height() / quadrantSize + (Vars.world.height() % quadrantSize == 0 ? 0 : 1);
            for (Tile tile : Vars.world.tiles) {
                int quadrantX = tile.x / quadrantSize;
                int quadrantY = tile.y / quadrantSize;
                Item overlayDrop = tile.overlay().itemDrop;
                Item blockDrop = tile.block().itemDrop;
                Item floorDrop = tile.floor().itemDrop;
                Item drop = overlayDrop != null ? overlayDrop : (blockDrop != null ? blockDrop : floorDrop);
                if (drop == null) continue;
                int id = overlayDrop != null ? tile.overlay().id : (blockDrop != null ? tile.block().id : tile.floor().id);
                if (ores[id] == null) {
                    ores[id] = new IntSeq[quadrantWidth][quadrantHeight];
                }
                if (ores[id][quadrantX][quadrantY] == null) {
                    ores[id][quadrantX][quadrantY] = new IntSeq(false, 16);
                }
                ores[id][quadrantX][quadrantY].add(tile.pos());
            }
        });
    }

    public boolean hasOre(int id) {
        return (ores[id] != null);
    }

    public float findClosestOreDistance(float x, float y, int id) {
        float closestDistance = Float.MAX_VALUE;
        if (ores[id] == null) return closestDistance;
        for (int qx = 0; qx < quadrantWidth; qx++) {
            for (int qy = 0; qy < quadrantHeight; qy++) {
                IntSeq quadrant = ores[id][qx][qy];
                if (quadrant == null) continue;
                for (int i = 0; i < quadrant.size; i++) {
                    Tile tile = Vars.world.tile(quadrant.get(i));
                    if (tile.block().id == id || (tile.overlay().id == id && tile.block() == Blocks.air) || tile.overlay().wallOre || (tile.floor().id == id && tile.block() == Blocks.air && tile.overlay() == Blocks.air)) {
                        float distance = Mathf.dst2(tile.x, tile.y, x, y);
                        quadrant.swap(0, i);
                        if (closestDistance > distance) {
                            closestDistance = distance;
                            break;
                        }
                    }
                }
            }
        }
        return closestDistance;
    }

    public Tile findClosestOre(float x, float y, int id) {
        if (ores[id] == null) return null;
        float closestDistance = Float.MAX_VALUE;
        Tile closest = null;
        for (int qx = 0; qx < quadrantWidth; qx++) {
            for (int qy = 0; qy < quadrantHeight; qy++) {
                IntSeq quadrant = ores[id][qx][qy];
                if (quadrant == null) continue;
                for (int i = 0; i < quadrant.size; i++) {
                    Tile tile = Vars.world.tile(quadrant.get(i));
                    if (tile.block().id == id || (tile.overlay().id == id && tile.block() == Blocks.air) || tile.overlay().wallOre || (tile.floor().id == id && tile.block() == Blocks.air && tile.overlay() == Blocks.air)) {
                        float distance = Mathf.dst2(tile.x, tile.y, x, y);
                        quadrant.swap(0, i);
                        if (closestDistance > distance) {
                            closestDistance = distance;
                            closest = tile;
                            break;
                        }
                    }
                }
            }
        }
        return closest;
    }
}
