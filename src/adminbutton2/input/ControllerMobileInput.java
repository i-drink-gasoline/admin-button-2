// SPDX-License-Identifier: GPL-3.0
package adminbutton2.input;

import mindustry.gen.Unit;
import mindustry.input.MobileInput;

import adminbutton2.AdminVars;

public class ControllerMobileInput extends MobileInput {
    @Override
    protected void updateMovement(Unit unit) {
        if (!AdminVars.controllerEnabled) {
            super.updateMovement(unit);
        }
    }
}
