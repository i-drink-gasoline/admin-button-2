// SPDX-License-Identifier: GPL-3.0
package adminbutton2.util;

import arc.Core;
import arc.Events;
import arc.struct.Queue;
import mindustry.Vars;
import mindustry.entities.units.BuildPlan;
import mindustry.game.EventType;
import mindustry.gen.Unit;

import adminbutton2.AdminVars;

public class PlanSaver {
    public Unit lastUnit = null;
    public Queue<BuildPlan> plans = new Queue<>();
    public boolean enabled = Core.settings.getBool("adminbutton2.settings.plan_saver.enabled", false);

    static {
        Events.run(EventType.Trigger.update, () -> {
            if (!AdminVars.planSaver.enabled) return;
            if (Vars.player.unit() != AdminVars.planSaver.lastUnit) {
                Vars.player.unit().plans.clear();
                AdminVars.planSaver.plans.each(p -> {
                    Vars.player.unit().plans.add(p);
                });
                AdminVars.planSaver.lastUnit = Vars.player.unit();
            }
            AdminVars.planSaver.plans.clear();
            Vars.player.unit().plans.each(p -> {
                AdminVars.planSaver.plans.add(p);
            });
        });
        Events.run(EventType.WorldLoadEvent.class, () -> AdminVars.planSaver.plans.clear());
    }
}
