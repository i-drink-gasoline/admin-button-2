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
            t.row();
            t.table(t2 -> {
                Slider slider = new Slider(0f, 5f, 0.05f, false);
                float value = Core.settings.getFloat("adminbutton2.autofill.interaction_cooldown", 0.25f);
                slider.setValue(value);
                Label label = new Label("" + value);
                label.touchable = Touchable.disabled;
                slider.changed(() -> {
                    float v = slider.getValue();
                    Core.settings.put("adminbutton2.autofill.interaction_cooldown", v);
                    AdminVars.autofill.interactionCooldown = v;
                    label.setText("" + v);
                });
                t2.stack(slider, label).width(300f);
                t2.add("@adminbutton2.autofill.interaction_cooldown");
            }).row();
            t.table(t2 -> {
                Slider slider = new Slider(1f, 500f, 1f, false);
                int value = Core.settings.getInt("adminbutton2.autofill.core_minimum_request_amount", 30);
                slider.setValue(value);
                Label label = new Label("" + value);
                label.touchable = Touchable.disabled;
                slider.changed(() -> {
                    int v = (int)slider.getValue();
                    Core.settings.put("adminbutton2.autofill.core_minimum_request_amount", v);
                    AdminVars.autofill.coreMinimumRequestAmount = v;
                    label.setText("" + v);
                });
                t2.stack(slider, label).width(300f);
                t2.add("@adminbutton2.autofill.core_minimum_request_amount");
            });
        });
    }
}
