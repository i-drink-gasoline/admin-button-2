// SPDX-License-Identifier: GPL-3.0
package adminbutton2.util;

//import arc.graphics.Color;
import arc.Core;
import arc.Events;
import arc.graphics.g2d.Draw;
import arc.math.geom.Intersector;
import arc.math.geom.Rect;
import arc.util.Tmp;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.game.Team;
import mindustry.game.Teams;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.world.blocks.defense.turrets.TractorBeamTurret;
import mindustry.world.blocks.defense.turrets.Turret;

import adminbutton2.AdminVars;

public class TurretRange {
    private Rect cameraBounds = new Rect();
    private Team playerTeam;
    public boolean drawAir = Core.settings.getBool("adminbutton2.turret_range.draw_air", false);
    public boolean drawGround = Core.settings.getBool("adminbutton2.turret_range.draw_ground", false);
    public boolean drawPlayerTeam = Core.settings.getBool("adminbutton2.turret_range.draw_player_team", false);

    static {
        Events.run(EventType.Trigger.drawOver, () -> AdminVars.turretRange.draw());
    }

    public void draw() {
        //if (!enabled) return;
        Core.camera.bounds(cameraBounds);
        playerTeam = Vars.player.team();
        Draw.z(Layer.effect);
        for (Teams.TeamData team : Vars.state.teams.present) {
            team.buildings.each(b -> {
                if (b.block instanceof Turret) {
                    Turret turret = (Turret)b.block;
                    Turret.TurretBuild turretBuild = (Turret.TurretBuild)b;
                    drawRange(b.x, b.y, turretBuild.range(), turret.targetAir, turret.targetGround, b.team);
                } else if (b.block instanceof TractorBeamTurret) {
                    TractorBeamTurret turret = (TractorBeamTurret)b.block;
                    drawRange(b.x, b.y, turret.range, turret.targetAir, turret.targetGround, b.team);
                }
            });
        }
    }

    private void drawRange(float x, float y, float range, boolean targetAir, boolean targetGround, Team team) {
        if (!((drawAir && targetAir) || (drawGround && targetGround))) return;
        if (!drawPlayerTeam && team.equals(playerTeam)) return;
        if (!Intersector.overlaps(Tmp.cr1.set(x, y, range), cameraBounds)) return;
        //Drawf.dashCircle(x, y, range, targetAir && targetGround ? Color.purple : targetAir ? Color.blue : targetGround ? Color.red : Color.green);
        //Drawf.dashCircle(x, y, range, drawAir && targetAir && drawGround && targetGround ? Color.purple : drawAir && targetAir ? Color.blue : drawGround && targetGround ? Color.red : Color.green);
        Drawf.dashCircle(x, y, range, team.color);
    }
}
