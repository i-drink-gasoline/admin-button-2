// SPDX-License-Identifier: GPL-3.0
package adminbutton2.ui;

import arc.Core;
import mindustry.Vars;
import mindustry.ui.dialogs.BaseDialog;

public class SettingsDialog extends BaseDialog {
    public SettingsDialog() {
        super("@settings");
        addCloseButton();
        cont.defaults().left();
        if (Vars.mobile) check("adminbutton2.settings.pause_building_button", true, true);
        check("adminbutton2.settings.override_input_handler", true, true);
        check("adminbutton2.settings.override_chatfrag", true, true);
    }

    private void check(String name, boolean def, boolean restart) {
        boolean enabled = Core.settings.getBool(name, def);
        cont.check(Core.bundle.get(name), enabled, e -> {
            Core.settings.put(name, e);
            if (restart) Vars.ui.showInfo("@setting.macnotch.description");
        }).row();
    }
}
