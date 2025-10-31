// SPDX-License-Identifier: GPL-3.0
package adminbutton2.ui;

import arc.Core;
import arc.scene.ui.Label;
import arc.scene.ui.layout.Table;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.ui.dialogs.BaseDialog;

import adminbutton2.AdminPanel;
import adminbutton2.AdminVars;

public class PanelConfigDialog extends BaseDialog {
    private Table table = new Table();
    public PanelConfigDialog() {
        super("@adminbutton2.panelconfig.title");
        addCloseButton();
        cont.pane(table);
        buttons.button("@add", Icon.add, this::addButton);
        buttons.button("@settings.reset", Icon.trash, this::resetButtons);
        rebuild();
    }

    private void rebuild() {
        table.clearChildren();
        table.table(t -> {
            float buttonSize = Core.settings.getFloat("adminbutton2.adminpanel.button_size", 40);
            Label field = t.add(String.format("%.0f", buttonSize)).get();
            t.slider(40, 80, 10, buttonSize, true, size -> {
                Core.settings.put("adminbutton2.adminpanel.button_size", size);
                buttonsUpdated();
                field.setText(String.format("%.0f", size));
            }).padLeft(8f);
        }).row();
        for (int i = 0; i < AdminVars.panel.activeButtons.size; i++) {
            AdminPanel.PanelButton button = AdminVars.panel.activeButtons.get(i);
            int index = i;
            table.table(Tex.button, t -> {
                t.left();
                t.button(Icon.up, () -> {
                    if (index - 1 < 0) return;
                    AdminVars.panel.activeButtons.swap(index, index - 1);
                    buttonsUpdated();
                });
                t.button(Icon.down, () -> {
                    if (index + 1 >= AdminVars.panel.activeButtons.size) return;
                    AdminVars.panel.activeButtons.swap(index, index + 1);
                    buttonsUpdated();
                });
                t.add(button.name).padLeft(10f);
            }).fillX();
            if (!button.mandatory) {
                table.button(Icon.cancel, () -> {
                    AdminVars.panel.activeButtons.remove(index);
                    buttonsUpdated();
                }).padLeft(10f);
            }
            table.row();
        };
    }

    private void addButton() {
        BaseDialog dialog = new BaseDialog("@add");
        dialog.addCloseButton();
        dialog.cont.pane(p -> {
            p.defaults().growX();
            AdminVars.panel.buttons.each(a -> {
                if (AdminVars.panel.activeButtons.indexOf(a) != -1 && !a.multiple) return;
                p.table(Tex.button, t -> {
                    t.left();
                    t.button(Icon.add, () -> {
                        AdminVars.panel.activeButtons.add(a);
                        buttonsUpdated();
                        dialog.hide();
                    });
                    t.add(a.name).padLeft(10f);
                });
                p.row();
            });
        });
        dialog.show();
    }

    private void buttonsUpdated() {
        AdminVars.panel.rebuild();
        rebuild();
        AdminVars.panel.saveButtons();
    }

    private void resetButtons() {
        Core.settings.remove("adminbutton2.adminpanel.activeButtons");
        Core.settings.remove("adminbutton2.adminpanel.button_size");
        AdminVars.panel.loadButtons();
        buttonsUpdated();
    }
}
