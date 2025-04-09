// SPDX-License-Identifier: GPL-3.0
package adminbutton2.ui;

import arc.Core;
import arc.scene.ui.TextButton;
import arc.scene.ui.TextField;
import arc.scene.ui.layout.Table;
import mindustry.Vars;
import mindustry.gen.Call;
import mindustry.gen.Iconc;
import mindustry.ui.dialogs.BaseDialog;

import adminbutton2.Secret;

public class AdminDialog extends BaseDialog {
    private String secretMessage = "";
    private char icon = Core.settings.getString("adminbutton2-icon", String.valueOf(Iconc.admin)).charAt(0);
    private TextButton chooserButton = new TextButton(String.valueOf(icon));
    TextField formatField = new TextField();

    public AdminDialog() {
        super("@adminbutton2.admindialog.title");
        BaseDialog iconChooser = iconChooserDialog();
        formatField.setMessageText("/t {}");
        addCloseButton();
        cont.table(t -> {
            t.table(t2 -> {
                t2.add(chooserButton).width(50).height(50).get().changed(() -> iconChooser.show());
                t2.add(formatField).width(350);
            });;
            t.row();
            t.field("", text -> secretMessage = text).width(400).height(50).get().setMessageText("@adminbutton2.admindialog.secret_message");
            t.row();
            t.table(t2 -> {
                t2.button("@adminbutton2.admindialog.send_secret_message", () -> sendMessage(Secret.generateSecretMessage(icon, secretMessage))).width(200).height(50);
                t2.button("@adminbutton2.admindialog.send_random_message", () -> sendMessage(Secret.generateRandomMessage(icon, Vars.maxTextLength - formatField.getText().length()))).width(200).height(50);
            }).marginTop(8);
            t.row();
            t.button("@adminbutton2.message_list.title", () -> adminbutton2.AdminVars.messages.show()).width(200).height(50).left();
        });
    }

    private void sendMessage(String message) {
        if (!formatField.getText().contains("{}")) formatField.setText("{}");
        String msg = formatField.getText().replaceFirst("\\{\\}", message);
        if (msg.length() > Vars.maxTextLength) {
            Vars.ui.showErrorMessage("@adminbutton2.admindialog.message_above_limit");
            return;
        }
        Call.sendChatMessage(msg);
    }

    private BaseDialog iconChooserDialog() {
        BaseDialog iconChooser = new BaseDialog("@adminbutton2.admindialog.icon_chooser.title");
        iconChooser.addCloseButton();
        iconChooser.cont.pane(t -> {
            int icons = 0;
            for (char c : Iconc.all.toCharArray()) {
                if (icons % 8 == 0 && icons != 0) t.row();
                String sc = String.valueOf(c);
                t.button(sc, () -> {
                    icon = c;
                    Core.settings.put("adminbutton2-icon", sc);
                    chooserButton.setText(sc);
                    iconChooser.hide();
                }).size(50);
                icons += 1;
            }
        }).growX();
        return iconChooser;
    }
}
