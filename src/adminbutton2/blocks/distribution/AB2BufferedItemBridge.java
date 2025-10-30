// SPDX-License-Identifier: GPL-3.0
package adminbutton2.blocks.distribution;

import arc.Core;
import arc.math.geom.Point2;
import arc.struct.Seq;
import mindustry.Vars;
import mindustry.entities.units.BuildPlan;
import mindustry.input.Binding;
import mindustry.world.blocks.distribution.BufferedItemBridge;

public class AB2BufferedItemBridge extends BufferedItemBridge {
    public AB2BufferedItemBridge() {
        super("ab2unused");
        Vars.content.removeLast();
    }

    @Override
    public void handlePlacementLine(Seq<BuildPlan> plans) {
        boolean diagonal = Core.input.keyDown(Binding.diagonalPlacement);
        if (Vars.mobile && Core.settings.getBool("swapdiagonal")) diagonal = !diagonal;
        if (!diagonal) {
            super.handlePlacementLine(plans);
        } else {
            BuildPlan last = plans.peek();
            if (plans.size > 1) {
                int c = plans.size <= range ? plans.size : range;
                for (int i = 0; i < plans.size - 1; i++) {
                    BuildPlan cur = plans.get(i);
                    BuildPlan next;
                    if (plans.size - i - 1 <= c) {
                        next = last;
                    } else {
                        next = plans.get(i + range);
                    }
                    if (positionsValid(cur.x, cur.y, next.x, next.y)) {
                        cur.config = new Point2(next.x - cur.x, next.y - cur.y);
                    }
                }
            }
        }
    }

    @Override
    public void changePlacementPath(Seq<Point2> points, int rotation) {
        boolean diagonal = Core.input.keyDown(Binding.diagonalPlacement);
        if (Vars.mobile && Core.settings.getBool("swapdiagonal")) diagonal = !diagonal;
        if (!diagonal) {
            super.changePlacementPath(points, rotation);
        }
    }

    public class AB2BufferedItemBridgeBuild extends BufferedItemBridge.BufferedItemBridgeBuild {
    }
}
