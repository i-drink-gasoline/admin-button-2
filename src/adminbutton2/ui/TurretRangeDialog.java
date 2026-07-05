// SPDX-License-Identifier: GPL-3.0
package adminbutton2.ui;

import arc.Core;
import mindustry.ui.dialogs.BaseDialog;

import adminbutton2.AdminVars;

public class TurretRangeDialog extends BaseDialog {
    public TurretRangeDialog() {
        super("@adminbutton2.turret_range.title");
        addCloseButton();
        cont.defaults().left();
        cont.check("@adminbutton2.turret_range.draw_air", e -> {
            boolean value = !AdminVars.turretRange.drawAir;
            AdminVars.turretRange.drawAir = value;
            Core.settings.put("adminbutton2.turret_range.draw_air", value);
        }).checked(b -> AdminVars.turretRange.drawAir).row();
        cont.check("@adminbutton2.turret_range.draw_ground", e -> {
            boolean value = !AdminVars.turretRange.drawGround;
            AdminVars.turretRange.drawGround = value;
            Core.settings.put("adminbutton2.turret_range.draw_ground", value);
        }).checked(b -> AdminVars.turretRange.drawGround).row();
        cont.check("@adminbutton2.turret_range.draw_player_team", e -> {
            boolean value = !AdminVars.turretRange.drawPlayerTeam;
            AdminVars.turretRange.drawPlayerTeam = value;
            Core.settings.put("adminbutton2.turret_range.draw_player_team", value);
        }).checked(b -> AdminVars.turretRange.drawPlayerTeam).row();
    }
}
