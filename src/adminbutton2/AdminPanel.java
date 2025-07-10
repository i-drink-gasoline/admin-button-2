// SPDX-License-Identifier: GPL-3.0
package adminbutton2;

import arc.Core;
import arc.Events;
import arc.func.Boolf;
import arc.input.KeyCode;
import arc.scene.event.InputEvent;
import arc.scene.event.InputListener;
import arc.scene.style.Drawable;
import arc.scene.ui.ImageButton;
import arc.scene.ui.layout.Cell;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.gen.Icon;
import mindustry.ui.Styles;

import adminbutton2.AdminVars;

public class AdminPanel {
    private Table table = new Table();
    public Seq<PanelButton> buttons = new Seq<>();
    public Seq<PanelButton> activeButtons = new Seq<>();

    public AdminPanel() {
        Core.scene.root.addChild(table);
        table.update(() -> table.toFront());
        table.visible(() -> !Vars.ui.minimapfrag.shown() && Vars.ui.hudfrag.shown);
        addDefaultButtons();
        loadButtons();
        rebuild();
        Events.run(EventType.ResizeEvent.class, () -> setTablePosition());
    }

    private void addDefaultButtons() {
        PanelButton drag = new PanelButton("@adminbutton2.adminbutton.adminpanel.button.drag", Icon.move, () -> {});
        drag.drag = true;
        buttons.add(drag);
        PanelButton row = new PanelButton("@adminbutton2.adminbutton.adminpanel.button.row", Icon.down, () -> {});
        row.row = true;
        row.multiple = true;
        buttons.add(row);
        PanelButton admin = new PanelButton("@adminbutton2.admindialog.title", Icon.admin, () -> AdminVars.admin.show());
        admin.mandatory = true;
        addButton(admin);
        addButton("@adminbutton2.controller.title", Icon.logic, () -> AdminVars.control.show());
        addButton("@adminbutton2.imagegenerator.title", Icon.image, () -> AdminVars.image.show());
        addButton("@adminbutton2.message_list.title", Icon.chat, () -> AdminVars.messages.show());
        addButton("@adminbutton2.secrets.title", Icon.admin, () -> AdminVars.secrets.show());
        addButton("@rules.waves", Icon.waves, () -> AdminVars.waves.show());
        addButton("@adminbutton2.panelconfig.title", Icon.wrench, () -> AdminVars.panelConfig.show());

        addButton("@adminbutton2.controller.enable_controller", Icon.logic, () -> AdminVars.controllerEnabled = !AdminVars.controllerEnabled, b -> AdminVars.controllerEnabled);
    }

    private void setTablePosition() {
        setTablePosition(Core.settings.getFloat("adminbutton2.table_position.x", 0), Core.settings.getFloat("adminbutton2.table_position.y", 0));
    }

    private void setTablePosition(float x, float y) {
        if (x - table.getPrefWidth()/2 < 100) x = table.getPrefWidth()/2;
        if (x + table.getPrefWidth()/2 > Core.graphics.getWidth() - 100) x = Core.graphics.getWidth() - table.getPrefWidth()/2;
        if (y - table.getPrefHeight()/2 < 0) y = table.getPrefHeight()/2;
        if (y + table.getPrefHeight()/2 > Core.graphics.getHeight()) y = Core.graphics.getHeight() - table.getPrefHeight()/2;
        table.setPosition(x, y);
    }

    public void rebuild() {
        table.clearChildren();
        table.defaults().width(40).height(40);
        activeButtons.each(b -> {
            if (b.row) {
                table.row();
                return;
            }
            ImageButton.ImageButtonStyle style = b.checked == null ? Styles.cleari : Styles.clearTogglei;
            Cell<ImageButton> cell = table.button(b.icon, style, b.runnable);
            ImageButton button = cell.get();
            if (b.drag) button.addListener(new DragListener());
            if (b.checked != null) cell.checked(b.checked);
        });
        setTablePosition();
    }

    public void saveButtons() {
        Seq<String> actives = new Seq<>(activeButtons.size);
        activeButtons.each(b -> actives.add(b.name));
        Core.settings.putJson("adminbutton2.adminpanel.activeButtons", actives);
    }

    @SuppressWarnings("unchecked")
    public void loadButtons() {
        Seq<String> activeNames = Core.settings.getJson("adminbutton2.adminpanel.activeButtons", Seq.class, Seq::new);
        if (activeNames.indexOf("@adminbutton2.admindialog.title") == -1) {
            activeNames.add("@adminbutton2.admindialog.title");
            activeNames.add("@adminbutton2.adminbutton.adminpanel.button.drag");
        }
        activeButtons.clear();
        activeNames.each(a -> {
            buttons.each(b -> {
                if (b.name.equals(a)) activeButtons.add(b);
            });
        });
    }

    public void addButton(PanelButton button) {
        buttons.add(button);
    }

    public void addButton(String name, Drawable icon, Runnable runnable) {
        addButton(new PanelButton(name, icon, runnable));
    }

    public void addButton(String name, Drawable icon, Runnable runnable, Boolf<ImageButton> checked) {
        addButton(new PanelButton(name, icon, runnable, checked));
    }

    public class PanelButton {
        public String name;
        public Drawable icon;
        public Runnable runnable;
        public Boolf<ImageButton> checked = null;

        public boolean mandatory = false;
        public boolean multiple = false;
        public boolean drag;
        public boolean row;

        public PanelButton(String name, Drawable icon, Runnable runnable) {
            this.name = name;
            this.icon = icon;
            this.runnable = runnable;
        }

        public PanelButton(String name, Drawable icon, Runnable runnable, Boolf<ImageButton> checked) {
            this.name = name;
            this.icon = icon;
            this.runnable = runnable;
            this.checked = checked;
        }
    }

    private class DragListener extends InputListener {
        private float touchX, touchY;
        private boolean dragged;
        @Override
        public boolean touchDown(InputEvent event, float x, float y, int pointer, KeyCode button) {
            touchX = x;
            touchY = y;
            dragged = false;
            return true;
        }
        @Override
        public void touchDragged(InputEvent event, float x, float y, int pointer) {
            dragged = true;
            float newX = table.x + x - touchX;
            float newY = table.y + y - touchY;
            setTablePosition(newX, newY);
        }
        @Override
        public void touchUp(InputEvent event, float x, float y, int pointer, KeyCode button) {
            if (dragged) {
                Core.settings.put("adminbutton2.table_position.x", table.x);
                Core.settings.put("adminbutton2.table_position.y", table.y);
            }
        }
    }
}
