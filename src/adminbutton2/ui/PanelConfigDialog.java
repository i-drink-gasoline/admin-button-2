// SPDX-License-Identifier: GPL-3.0
package adminbutton2.ui;

import arc.scene.ui.layout.Table;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.ui.dialogs.BaseDialog;

import adminbutton2.AdminVars;

public class PanelConfigDialog extends BaseDialog {
    private Table table = new Table();
    public PanelConfigDialog() {
        super("@adminbutton2.panelconfig.title");
        addCloseButton();
        cont.pane(table);
        buttons.button("@add", Icon.add, this::addButton);
        rebuild();
    }

    private void rebuild() {
        table.clearChildren();
        AdminVars.panel.activeButtons.each(a -> {
            table.table(Tex.button, t -> {
                t.left();
                t.button(Icon.up, () -> {
                    int index = AdminVars.panel.activeButtons.indexOf(a);
                    if (index - 1 < 0) return;
                    AdminVars.panel.activeButtons.swap(index, index - 1);
                    buttonsUpdated();
                });
                t.button(Icon.down, () -> {
                    int index = AdminVars.panel.activeButtons.indexOf(a);
                    if (index + 1 >= AdminVars.panel.activeButtons.size) return;
                    AdminVars.panel.activeButtons.swap(index, index + 1);
                    buttonsUpdated();
                });
                t.add(a.name).padLeft(10f);
            }).fillX();
            if (!a.mandatory) {
                table.button(Icon.cancel, () -> {
                    AdminVars.panel.activeButtons.remove(a);
                    buttonsUpdated();
                }).padLeft(10f);
            }
            table.row();
        });
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
}
