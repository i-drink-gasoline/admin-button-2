// SPDX-License-Identifier: GPL-3.0
package adminbutton2;

import mindustry.Vars;
import mindustry.gen.Icon;
import mindustry.mod.Mod;
import mindustry.ui.Styles;

import adminbutton2.AdminVars;

public class AdminButton extends Mod {
    @Override
        public void init() {
        AdminVars.loadLanguage();
        AdminVars.init();
        Vars.ui.hudGroup.fill(t -> {
            t.button(Icon.admin, Styles.cleari, () -> {
                AdminVars.admin.show();
            }).width(40).height(40).visible(() -> !Vars.ui.minimapfrag.shown() && Vars.ui.hudfrag.shown);
            t.top().right().marginTop(150);
        });
    }
}
