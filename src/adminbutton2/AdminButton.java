// SPDX-License-Identifier: GPL-3.0
package adminbutton2;

import arc.Events;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.gen.Icon;
import mindustry.mod.Mod;
import mindustry.ui.Styles;

import adminbutton2.AdminVars;
import adminbutton2.ui.AdminChatFragment;

public class AdminButton extends Mod {
    public AdminButton() {
        Events.run(EventType.ContentInitEvent.class, () -> {
            AdminVars.loadLanguage();
        });
        Events.run(EventType.ClientLoadEvent.class, () -> {
            AdminVars.init();
            Vars.ui.hudGroup.fill(t -> {
                t.button(Icon.admin, Styles.cleari, () -> {
                    AdminVars.admin.show();
                }).width(40).height(40);
                t.top().right().marginTop(150);
            });
            Vars.ui.chatfrag = new AdminChatFragment(Vars.ui.chatfrag);
        });
    }
}
