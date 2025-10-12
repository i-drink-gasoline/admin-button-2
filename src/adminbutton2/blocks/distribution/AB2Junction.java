// SPDX-License-Identifier: GPL-3.0
package adminbutton2.blocks.distribution;

import arc.struct.Seq;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.entities.units.BuildPlan;
import mindustry.world.blocks.distribution.Junction;

public class AB2Junction extends Junction {
    public AB2Junction() {
        super("ab2unused");
        Vars.content.removeLast();
    }

    public void handlePlacementLine(Seq<BuildPlan> plans){
        if (plans.size < 1) return;
        BuildPlan first = plans.first();
        if (Vars.world.tile(first.x, first.y).block() == this) {
            first.block = Blocks.sorter;
        }
        for (BuildPlan plan : Vars.player.unit().plans) {
            if (plan.block == this && plan.x == first.x && plan.y == first.y) {
                first.block = Blocks.sorter;
            }
        }
    }

    public class AB2JunctionBuild extends Junction.JunctionBuild {
    }
}
