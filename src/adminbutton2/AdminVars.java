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
import mindustry.graphics.Pal;
import mindustry.input.MobileInput;
import mindustry.mod.Mod;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.SettingsMenuDialog;

import adminbutton2.AdminPanel;
import adminbutton2.controller.Controller;
import adminbutton2.input.ControllerDesktopInput;
import adminbutton2.input.ControllerMobileInput;
import adminbutton2.ui.AdminChatFragment;
import adminbutton2.ui.AdminDialog;
import adminbutton2.ui.AutoFillConfigDialog;
import adminbutton2.ui.ChatDialog;
import adminbutton2.ui.ConsoleDialog;
import adminbutton2.ui.ControllerDialog;
import adminbutton2.ui.ImageGeneratorDialog;
import adminbutton2.ui.MessageList;
import adminbutton2.ui.PanelConfigDialog;
import adminbutton2.ui.SecretsDialog;
import adminbutton2.ui.WavesDialog;
import adminbutton2.util.AutoFill;
import adminbutton2.util.BlockReplacer;
import adminbutton2.util.Commands;
import adminbutton2.util.Communication;
import adminbutton2.util.Interaction;
import adminbutton2.util.KeyBindings;

public class AdminVars extends Mod {
    private static final String admin_locale = "admin-button-2";
    public static final String chatNotificationPrefix = "[AB2] ";

    public static String secretMessageFormat = Core.settings.getString("adminbutton2.secrets.format", "{}");

    public static AdminPanel panel;

    public static AdminDialog admin;
    public static MessageList messages;
    public static WavesDialog waves;
    public static SecretsDialog secrets;
    public static ControllerDialog control;
    public static ImageGeneratorDialog image;
    public static PanelConfigDialog panelConfig;
    public static AutoFillConfigDialog autofillConfig;
    public static ChatDialog chat;
    public static ConsoleDialog console;

    public static Controller controller;

    public static Commands commands;
    public static AutoFill autofill;
    public static Communication communication;
    public static Interaction interaction;
    public static BlockReplacer blockReplacer;
    public static KeyBindings keys;

    public static boolean controllerEnabled = false;

    @Override
    public void init() {
        commands = new Commands();
        autofill = new AutoFill();
        communication = new Communication();
        interaction = new Interaction();
        blockReplacer = new BlockReplacer();
        keys = new KeyBindings();

        panel = new AdminPanel();
        admin = new AdminDialog();
        messages = new MessageList();
        waves = new WavesDialog();
        secrets = new SecretsDialog();
        control = new ControllerDialog();
        image = new ImageGeneratorDialog();
        panelConfig = new PanelConfigDialog();
        autofillConfig = new AutoFillConfigDialog();
        chat = new ChatDialog();
        console = new ConsoleDialog();

        blockReplacer.replaceBlocks();
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
        addLanguageOption();
        loadLanguage();
        if (Vars.control.input instanceof MobileInput && Core.settings.getBool("adminbutton2.settings.pause_building_button", true)) Events.run(EventType.ClientLoadEvent.class, () -> Timer.schedule(() -> addPauseBuildingButton(), 4));
        addSettingsCategory();
    }

    private static void addPauseBuildingButton() {
        try {
            Table table = (Table) Vars.control.input.uiGroup.getChildren().get(0);
            Element cancel = table.getChildren().get(0);
            table.removeChild(cancel);
            table.row();
            table.button("@adminbutton2.pause_building", Icon.pause, Styles.clearTogglet, () -> Vars.control.input.isBuilding = !Vars.control.input.isBuilding).width(155f).height(50f).margin(12f).checked(b -> !Vars.control.input.isBuilding);
            table.row();
            table.add(cancel).width(155f).height(50f).margin(12f);

            Table commandTable = (Table) Vars.control.input.uiGroup.getChildren().get(1);
            commandTable.getChildren().get(2).remove();
            commandTable.spacerY(() -> ((MobileInput)Vars.control.input).showCancel() ? 100f : 0f).row();
        } catch (Exception e) {
            Vars.ui.showException(e);
        }
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

    private void addSettingsCategory() {
        Vars.ui.settings.addCategory("Admin Button 2", Icon.admin, builder -> {
            builder.pref(new CategorySetting("Admin Button 2"));
            builder.checkPref("adminbutton2.settings.override_input_handler", true, v -> Vars.ui.showInfo("@setting.macnotch.description"));
            builder.checkPref("adminbutton2.settings.override_chatfrag", true, v -> Vars.ui.showInfo("@setting.macnotch.description"));
            if (!Vars.android) builder.checkPref("adminbutton2.settings.block_replacer.enabled", false, v -> Vars.ui.showInfo("@setting.macnotch.description"));
            if (Vars.mobile) builder.checkPref("adminbutton2.settings.pause_building_button", true, v -> Vars.ui.showInfo("@setting.macnotch.description"));
            builder.textPref("adminbutton2.commands.prefix", ".", v -> AdminVars.commands.handler.setPrefix(v));
            builder.sliderPref("adminbutton2.interaction.interaction_cooldown_milliseconds", 250, 0, 5000, 25, v -> {
                AdminVars.interaction.interactionCooldown = (float)v / 1000;
                return Core.bundle.format("setting.milliseconds", v);
            });
            builder.pref(new CategorySetting("AutoFill"));
            builder.sliderPref("adminbutton2.autofill.core_minimum_request_amount", 30, 1, 500, 1, v -> {
                AdminVars.autofill.coreMinimumRequestAmount = v;
                return "" + v;
            });
        });
    }

    private class CategorySetting extends SettingsMenuDialog.SettingsTable.Setting {
        private String text;

        public CategorySetting(String text) {
            super(null);
            this.text = text;
        }

        @Override
        public void add(SettingsMenuDialog.SettingsTable table) {
            table.add(text).color(Pal.accent).row();
            table.image().color(Pal.accent).height(3f).growX().padBottom(8f).row();
        }
    }
}
