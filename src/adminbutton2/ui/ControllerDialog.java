// SPDX-License-Identifier: GPL-3.0
package adminbutton2.ui;

import arc.scene.ui.ButtonGroup;
import arc.scene.ui.TextButton;
import arc.scene.ui.layout.Table;
import mindustry.graphics.Pal;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;

import adminbutton2.AdminVars;
import adminbutton2.controller.AutomaticMovementController;
import adminbutton2.controller.Controller;

public class ControllerDialog extends BaseDialog {
    private Table controllerTable = new Table();
    public Controller controllers[] = {new AutomaticMovementController()};

    public ControllerDialog() {
        super("@adminbutton2.controller.title");
        addCloseButton();
        cont.top();
        Table table = new Table();
        cont.add(table);
        cont.row();
        cont.pane(controllerTable).growX();
        table.check("@mod.enable", AdminVars.controllerEnabled, e -> AdminVars.controllerEnabled = e).row();
        Table buttons = new Table();
        buttons.defaults().height(45).pad(5);
        table.add(buttons).marginTop(5);
        ButtonGroup<TextButton> group = new ButtonGroup<>();
        for (Controller controller : controllers) {
            TextButton button = new TextButton("@" + controller.name(), Styles.togglet);
            button.getLabel().setWrap(false);
            button.clicked(() -> setController(controller));
            buttons.add(button).group(group).update(b -> b.setChecked(AdminVars.controller.name().equals(controller.name())));
        }
    }

    public void setController(Controller controller) {
        AdminVars.controller = controller;
        controllerTable.clearChildren();
        controllerTable.add(controller.table);
    }
}
