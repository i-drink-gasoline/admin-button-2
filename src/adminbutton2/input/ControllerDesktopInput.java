// SPDX-License-Identifier: GPL-3.0
package adminbutton2.input;

import arc.Core;
import arc.input.KeyCode;
import arc.math.Mathf;
import arc.math.geom.Vec2;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.Vars;
import mindustry.gen.Unit;
import mindustry.input.Binding;
import mindustry.input.DesktopInput;

import adminbutton2.AdminVars;

public class ControllerDesktopInput extends DesktopInput {
    private boolean panSpeedChanged = false;
    private float panSpeedSaved, panBoostSpeedSaved;
    private Vec2 cam = new Vec2();
    private boolean staticCamera = false;

    @Override
    public void update() {
        if (!Core.scene.hasField()) {
            if (Core.input.keyTap(AdminVars.keys.staticCamera)) staticCamera = !staticCamera;
        }
        if (Core.input.keyDown(KeyCode.altLeft)) {
            panSpeedSaved = panSpeed;
            panBoostSpeedSaved = panBoostSpeed;
            panSpeed = panBoostSpeed * 2;
            panBoostSpeed = panSpeed;
            panSpeedChanged = true;
        }
        cam.set(Core.camera.position);
        super.update();
        if (AdminVars.controllerEnabled && !staticCamera) {
            if (!panning) {
                if (!Core.scene.hasField()) {
                    float speed = (!Core.input.keyDown(Binding.boost) ? panSpeed : panBoostSpeed) * Time.delta;
                    cam.add(Tmp.v1.set(Core.input.axis(Binding.moveX), Core.input.axis(Binding.moveY)).nor().scl(speed));
                }
                Core.camera.position.set(cam);
            }
        }
        if (staticCamera) {
            if(Core.input.keyDown(Binding.pan) && !Core.scene.hasField()) {
                float camSpeed = (!Core.input.keyDown(Binding.boost) ? panSpeed : panBoostSpeed) * Time.delta;
                cam.x += Mathf.clamp((Core.input.mouseX() - Core.graphics.getWidth() / 2f) * panScale, -1, 1) * camSpeed;
                cam.y += Mathf.clamp((Core.input.mouseY() - Core.graphics.getHeight() / 2f) * panScale, -1, 1) * camSpeed;
            }
            Core.camera.position.set(cam);
        }
        if (panSpeedChanged) {
            panSpeed = panSpeedSaved; panBoostSpeed = panBoostSpeedSaved;
            panSpeedChanged = false;
        }
    }

    @Override
    protected void updateMovement(Unit unit) {
        if (!AdminVars.controllerEnabled) {
            super.updateMovement(unit);
        }
    }
}
