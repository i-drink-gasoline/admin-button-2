// SPDX-License-Identifier: GPL-3.0
package adminbutton2;

import arc.Core;
import arc.Events;
import arc.scene.Element;
import arc.scene.ui.ButtonGroup;
import arc.scene.ui.ScrollPane;
import arc.scene.ui.TextButton;
import arc.scene.ui.layout.Table;
import arc.struct.ObjectMap;
import arc.util.Log;
import arc.util.Timer;
import mindustry.Vars;
import mindustry.ctype.UnlockableContent;
import mindustry.game.EventType;
import mindustry.gen.Icon;
import mindustry.gen.Iconc;
import mindustry.mod.Mod;
import mindustry.ui.Styles;

import adminbutton2.AdminPanel;
import adminbutton2.controller.Controller;
import adminbutton2.input.ControllerDesktopInput;
import adminbutton2.input.ControllerMobileInput;
import adminbutton2.ui.AdminChatFragment;
import adminbutton2.ui.AdminDialog;
import adminbutton2.ui.AutoFillConfigDialog;
import adminbutton2.ui.ControllerDialog;
import adminbutton2.ui.ImageGeneratorDialog;
import adminbutton2.ui.MessageList;
import adminbutton2.ui.PanelConfigDialog;
import adminbutton2.ui.SecretsDialog;
import adminbutton2.ui.SettingsDialog;
import adminbutton2.ui.WavesDialog;
import adminbutton2.util.AutoFill;
import adminbutton2.util.Commands;
import adminbutton2.util.OreIndexer;

public class AdminVars extends Mod {
    private static final String admin_locale = "admin-button-2";

    public static AdminPanel panel;

    public static AdminDialog admin;
    public static MessageList messages;
    public static WavesDialog waves;
    public static SecretsDialog secrets;
    public static ControllerDialog control;
    public static SettingsDialog settings;
    public static ImageGeneratorDialog image;
    public static PanelConfigDialog panelConfig;
    public static AutoFillConfigDialog autofillConfig;

    public static Controller controller;

    public static OreIndexer oreIndexer;
    public static Commands commands;
    public static AutoFill autofill;

    public static boolean controllerEnabled = false;

    @Override
    public void init() {
        panel = new AdminPanel();
        admin = new AdminDialog();
        messages = new MessageList();
        waves = new WavesDialog();
        secrets = new SecretsDialog();
        control = new ControllerDialog();
        settings = new SettingsDialog();
        image = new ImageGeneratorDialog();
        panelConfig = new PanelConfigDialog();
        autofillConfig = new AutoFillConfigDialog();
        control.setController(control.controllers[0]);
        if (Core.settings.getBool("adminbutton2.settings.override_input_handler", true)) {
            Vars.control.input = Vars.mobile ? new ControllerMobileInput() : new ControllerDesktopInput();
        }
        updateController();
        if (Core.settings.getBool("adminbutton2.settings.override_chatfrag", true)) {
            AdminChatFragment cf = new AdminChatFragment();
            Core.scene.root.addChild(cf);
            Core.scene.root.swapActor(Vars.ui.chatfrag, cf);
            Core.scene.root.removeChild(Vars.ui.chatfrag);
            Vars.ui.chatfrag = cf;
        }
        oreIndexer = new OreIndexer();
        commands = new Commands();
        autofill = new AutoFill();
        addLanguageOption();
        loadLanguage();
        if (Vars.mobile && Core.settings.getBool("adminbutton2.settings.pause_building_button", true)) Events.run(EventType.ClientLoadEvent.class, () -> Timer.schedule(() -> addPauseBuildingButton(), 4));
    }

    private static void addPauseBuildingButton() {
        Table table = (Table) Vars.control.input.uiGroup.getChildren().get(0);
        Element cancel = table.getChildren().get(0);
        table.removeChild(cancel);
        table.row();
        table.button("@adminbutton2.pause_building", Icon.pause, Styles.togglet, () -> Vars.control.input.isBuilding = !Vars.control.input.isBuilding).width(155f).height(50f).margin(12f).checked(!Vars.control.input.isBuilding);
        table.row();
        table.add(cancel).width(155f).height(50f).margin(12f);
    }

    @SuppressWarnings("unchecked")
    public static void addLanguageOption() {
        TextButton button = new TextButton(Secret.generateSecretMessage(Iconc.admin, "Admin Button 2"), Styles.flatTogglet);
        button.clicked(() -> {
            if(Core.settings.getString("locale").equals(admin_locale)) return;
            Core.settings.put("locale", admin_locale);
            Log.info("Setting locale: @", admin_locale);
            Vars.ui.showInfo("@language.restart");
        });
        Table _1 = (Table) Vars.ui.language.getChildren().get(1);
        ScrollPane _2 = (ScrollPane) _1.getChildren().get(0);
        Table widget = (Table) _2.getWidget();
        TextButton firstButton = (TextButton) widget.getChildren().get(0);
        ButtonGroup<TextButton> group = (ButtonGroup<TextButton>) firstButton.getButtonGroup();
        widget.add(button).group(group).update(t -> t.setChecked(Core.settings.getString("locale", "").equals(admin_locale))).size(400f, 50f).row();
    }

    public static void loadLanguage() {
        if (Core.settings.getString("locale").equals(admin_locale)) {
            ObjectMap<String, String> properties = Core.bundle.getProperties();
            properties.each((k, v) -> {
                properties.put(k, Secret.generateSecretMessage(Iconc.admin, v));
            });
            Vars.content.each(c -> {
                if (!(c instanceof UnlockableContent)) return;
                UnlockableContent uc = (UnlockableContent) c;
                uc.localizedName = Secret.generateSecretMessage(Iconc.admin, uc.localizedName);
                if (uc.description != null) uc.description = Secret.generateSecretMessage(Iconc.admin, uc.description);
                if (uc.details != null) uc.details = Secret.generateSecretMessage(Iconc.admin, uc.details);
            });
        }
    }

    private static void updateController() {
        Events.run(EventType.Trigger.update, () -> {
            if (AdminVars.controllerEnabled) {
                AdminVars.controller.control();
            }
        });
    }
}
