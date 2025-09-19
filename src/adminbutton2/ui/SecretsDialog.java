// SPDX-License-Identifier: GPL-3.0
package adminbutton2.ui;

import arc.Core;
import arc.scene.ui.TextButton;
import arc.scene.ui.TextField;
import arc.util.Interval;
import mindustry.Vars;
import mindustry.game.Schematic;
import mindustry.gen.Call;
import mindustry.gen.Iconc;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;

import adminbutton2.AdminVars;
import adminbutton2.Secret;

public class SecretsDialog extends BaseDialog {
    private char icon = Core.settings.getString("adminbutton2-icon", String.valueOf(Iconc.admin)).charAt(0);
    private int sendViaChat = 1, sendViaBuilding = 2;
    private int sendVia = Core.settings.getInt("adminbutton2.secrets.sendVia", sendViaChat);
    private Interval chatInterval = new Interval();
    private boolean chatWarningShown = false;

    public SecretsDialog() {
        super("@adminbutton2.secrets.title");
        addCloseButton();
        cont.table(t -> {
                t.button("", () -> iconChooserDialog().show()).update(b -> b.setText(String.valueOf(icon))).size(50f);
            t.field(AdminVars.secretMessageFormat, s -> {
                AdminVars.secretMessageFormat = s;
                Core.settings.put("adminbutton2.secrets.format", s);
            }).width(350f).update(f -> f.setText(AdminVars.secretMessageFormat)).get().setMessageText("/t {}");
        }).row();
        TextField secretField = cont.field("", s -> {}).width(400f).height(50f).get();
        secretField.setMessageText("@adminbutton2.admindialog.secret_message");
        cont.row();
        cont.table(t -> {
            t.defaults().width(200f).height(100f);
            t.button("@adminbutton2.admindialog.send_secret_message", () -> {
                if (sendVia == sendViaChat) sendMessage(Secret.generateSecretMessage(icon, secretField.getText()));
                else sendMessage(secretField.getText());
            });
            t.button("@adminbutton2.admindialog.send_random_message", () ->
                sendMessage(Secret.generateRandomMessage(icon, Vars.maxTextLength - AdminVars.secretMessageFormat.length())))
                .get().setDisabled(() -> sendVia != sendViaChat);
        }).marginTop(8).row();
        cont.table(t -> {
            t.defaults().size(50f);
            t.button(String.valueOf(Iconc.chat), Styles.togglet, () ->
                Core.settings.put("adminbutton2.secrets.sendVia", sendVia = sendViaChat))
                .update(b -> b.setChecked(sendVia == sendViaChat));
            t.button(String.valueOf(Iconc.blockMessage), Styles.togglet, () ->
                Core.settings.put("adminbutton2.secrets.sendVia", sendVia = sendViaBuilding))
                .update(b -> b.setChecked(sendVia == sendViaBuilding));
        }).row();
        cont.button("@adminbutton2.communication.sendSchematicFromClipboard", () -> {
            Schematic schematic;
            try {
                schematic = Vars.schematics.readBase64(Core.app.getClipboardText());
            } catch (Exception e) {
                Vars.ui.showException(e);
                return;
            }
            if (AdminVars.communication.selectedBuildingExists()) {
                AdminVars.communication.sendSchematic(schematic);
            } else {
                AdminVars.communication.selectBuildingAndRun(() -> AdminVars.communication.sendSchematic(schematic));
                Vars.ui.showInfo("@adminbutton2.communication.selectBuilding");
            }
        }).disabled(b -> Core.app.getClipboardText() == null || !Core.app.getClipboardText().startsWith(Vars.schematicBaseStart)).width(400f);
    }

    private void sendMessage(String message) {
        if (sendVia == sendViaChat) {
            if (!chatWarningShown && !chatInterval.get(60f)) {
                chatWarningShown = true;
                Vars.ui.showInfo("@adminbutton2.secrets.chat_warning");
            }
            if (!AdminVars.secretMessageFormat.contains("{}")) AdminVars.secretMessageFormat = ("{}");
            String msg = AdminVars.secretMessageFormat.replaceFirst("\\{\\}", message);
            if (msg.length() > Vars.maxTextLength) {
                Vars.ui.showErrorMessage("@adminbutton2.admindialog.message_above_limit");
                return;
            }
            Call.sendChatMessage(msg);
        } else if (sendVia == sendViaBuilding) {
            if (AdminVars.communication.selectedBuildingExists()) {
                AdminVars.communication.sendChatMessage(message);
            } else {
                AdminVars.communication.selectBuildingAndRun(() -> AdminVars.communication.sendChatMessage(message));
                Vars.ui.showInfo("@adminbutton2.communication.selectBuilding");
            }
        }
    }

    private BaseDialog iconChooserDialog() {
        BaseDialog iconChooser = new BaseDialog("@adminbutton2.admindialog.icon_chooser.title");
        iconChooser.addCloseButton();
        iconChooser.cont.pane(t -> {
            t.table(t2 -> {
                int icons = 0;
                for (char c : Iconc.all.toCharArray()) {
                    if (icons % 8 == 0 && icons != 0) t2.row();
                    String sc = String.valueOf(c);
                    t2.button(sc, () -> {
                        icon = c;
                        Core.settings.put("adminbutton2-icon", sc);
                        iconChooser.hide();
                    }).size(50);
                    icons += 1;
                }
            }).growX().row();
            t.field("" + icon, s -> {
                if (s.length() == 1) {
                    char c = s.charAt(0);
                    icon = c;
                    Core.settings.put("adminbutton2-icon", String.valueOf(c));
                    iconChooser.hide();
                }
            }).get().setMaxLength(1);
        }).growX();
        return iconChooser;
    }
}
