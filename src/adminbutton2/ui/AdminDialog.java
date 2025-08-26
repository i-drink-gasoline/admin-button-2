// SPDX-License-Identifier: GPL-3.0
package adminbutton2.ui;

import arc.Events;
import arc.scene.ui.TextButton;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.gen.Icon;
import mindustry.ui.dialogs.BaseDialog;

import adminbutton2.AdminVars;

public class AdminDialog extends BaseDialog {
    public AdminDialog() {
        super("@adminbutton2.admindialog.title");
        Events.run(EventType.ClientLoadEvent.class, () -> {
            Vars.ui.settings.getCategories().each(c -> {
                if (c.name.equals("Admin Button 2")) {
                    BaseDialog settings = new BaseDialog("@settings");
                    settings.addCloseButton();
                    settings.cont.add(c.table);
                    buttons.button("@settings", Icon.settings, () -> {
                        settings.show();
                    });
                }
            });
        });
        addCloseButton();
        cont.defaults().size(130).pad(5);
        cont.buttonRow("@adminbutton2.secrets.title", Icon.admin, () -> AdminVars.secrets.show());
        cont.buttonRow("@adminbutton2.message_list.title", Icon.chat, () -> AdminVars.messages.show());
        TextButton waves = cont.buttonRow("@rules.waves", Icon.waves, () -> AdminVars.waves.show()).get();
        Events.run(EventType.WorldLoadEvent.class, () -> waves.setDisabled(!Vars.state.rules.waves));
        cont.row();
        cont.buttonRow("@adminbutton2.controller.title", Icon.logic, () -> AdminVars.control.show());
        cont.buttonRow("@adminbutton2.imagegenerator.title", Icon.image, () -> AdminVars.image.show());
        cont.buttonRow("@adminbutton2.panelconfig.title", Icon.wrench, () -> AdminVars.panelConfig.show());
        cont.row();
        cont.buttonRow("@adminbutton2.autofill_config.title", Icon.box, () -> AdminVars.autofillConfig.show());
    }
}
