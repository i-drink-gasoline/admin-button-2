// SPDX-License-Identifier: GPL-3.0
package adminbutton2.util;

import arc.Core;
import arc.Events;
import arc.struct.Seq;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.world.Tile;

import adminbutton2.AdminVars;

public class MineClosestOre {
    static {
        Events.run(EventType.Trigger.update, () -> AdminVars.mineClosestOre.update());
    }

    public void update() {
        if (!Core.scene.hasField() && Core.input.keyDown(AdminVars.keys.mineClosestOre)) {
            if (Vars.player.unit() == null) return;
            Seq<Tile> validTiles = new Seq<>();
            int mineRange = (int)(Vars.player.unit().type.mineRange / Vars.tilesize) + 1;
            int px = (int)(Vars.player.x / Vars.tilesize); int py = (int)(Vars.player.y / Vars.tilesize);
            for (int wx = px - mineRange; wx <= px + mineRange; wx++) {
                for (int wy = py - mineRange; wy <= py + mineRange; wy++) {
                    Tile tile = Vars.world.tile(wx, wy);
                    if (Vars.player.unit().validMine(tile, true)) {
                        validTiles.add(tile);
                    }
                }
            }
            Tile tile = validTiles.min(t -> {
                    return t.dst(Vars.player);
                });
            Vars.player.unit().mineTile = tile;
        }
    }
}
