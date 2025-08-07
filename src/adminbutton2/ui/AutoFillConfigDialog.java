// SPDX-License-Identifier: GPL-3.0
package adminbutton2.ui;

import arc.Core;
import arc.scene.event.Touchable;
import arc.scene.ui.Label;
import arc.scene.ui.Slider;
import arc.util.Strings;
import arc.util.Timer;
import mindustry.ui.dialogs.BaseDialog;

import adminbutton2.AdminVars;

public class AutoFillConfigDialog extends BaseDialog {
    public AutoFillConfigDialog() {
        super("@adminbutton2.autofill_config.title");
        addCloseButton();
        cont.table(t -> {
            t.defaults().left();
            t.check("@adminbutton2.autofill.enabled", b -> AdminVars.autofill.enabled = !AdminVars.autofill.enabled).checked(b -> AdminVars.autofill.enabled).row();
            t.check("@adminbutton2.autofill.select_buildings", b -> AdminVars.autofill.selectBuildings = !AdminVars.autofill.selectBuildings).checked(b -> AdminVars.autofill.selectBuildings).row();
            t.check("@adminbutton2.autofill.fill_only_selected_buildings", b -> AdminVars.autofill.fillOnlySelectedBuildings = !AdminVars.autofill.fillOnlySelectedBuildings).checked(b -> AdminVars.autofill.fillOnlySelectedBuildings).row();
        });
    }
}
