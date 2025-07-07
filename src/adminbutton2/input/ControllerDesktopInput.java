// SPDX-License-Identifier: GPL-3.0
package adminbutton2.input;

import arc.Core;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.Vars;
import mindustry.gen.Unit;
import mindustry.input.Binding;
import mindustry.input.DesktopInput;

import adminbutton2.AdminVars;

public class ControllerDesktopInput extends DesktopInput {
    @Override
    public void update() {
        if (AdminVars.controllerEnabled) {
            Tmp.v1.set(Core.camera.position);
            super.update();
            if (!panning && !Vars.player.dead()) {
                if (!Core.scene.hasField()) {
                    float speed = (!Core.input.keyDown(Binding.boost) ? panSpeed : panBoostSpeed) * Time.delta;
                    Tmp.v1.add(Tmp.v2.set(Core.input.axis(Binding.move_x), Core.input.axis(Binding.move_y)).nor().scl(speed));
                }
                Core.camera.position.set(Tmp.v1);
            }
        } else super.update();
    }

    @Override
    protected void updateMovement(Unit unit) {
        if (!AdminVars.controllerEnabled) {
            super.updateMovement(unit);
        }
    }
}
