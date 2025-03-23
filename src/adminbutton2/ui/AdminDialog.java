// SPDX-License-Identifier: GPL-3.0
package adminbutton2.ui;

import arc.scene.ui.TextButton;
import arc.scene.ui.TextField;
import arc.scene.ui.layout.Table;
import mindustry.Vars;
import mindustry.gen.Call;
import mindustry.gen.Iconc;
import mindustry.ui.dialogs.BaseDialog;

import adminbutton2.Secret;

public class AdminDialog extends BaseDialog {
    private String messagePrefix = "";
    private boolean messagePrefixEnabled = false;
    private String secretMessage = "";
    private char icon = Iconc.admin;
    private TextButton chooserButton = new TextButton(String.valueOf(icon));

    public AdminDialog() {
        super("Admin Button");
        BaseDialog iconChooser = iconChooserDialog();
        addCloseButton();
        cont.table(t -> {
            t.table(t2 -> {
                t2.add(chooserButton).width(50).height(50).get().changed(() -> iconChooser.show());
                TextField prefix = t2.field("", text -> messagePrefix = text).get();
                prefix.setMessageText("/t");
                prefix.setDisabled(!messagePrefixEnabled);
                t2.check("Message prefix", messagePrefixEnabled, enabled -> prefix.setDisabled(!(messagePrefixEnabled = enabled))).padLeft(4).get();
                t2.getCells().swap(1, 2);
            });;
            t.row();
            t.field("", text -> secretMessage = text).width(400).height(50).get().setMessageText("Secret message");
            t.row();
            t.table(t2 -> {
                t2.button("Send secret message", () -> sendMessage(Secret.generateSecretMessage(icon, secretMessage))).width(200).height(50);
                t2.button("Send random message", () -> sendMessage(Secret.generateRandomMessage(icon, Vars.maxTextLength - messagePrefix.length()))).width(200).height(50);
            }).marginTop(8);
            t.row();
            t.button("Message list", () -> adminbutton2.AdminVars.messages.show()).width(200).height(50).left();
        });
    }

    private void sendMessage(String message) {
        String msg = (messagePrefixEnabled == true ? messagePrefix : "") + message;
        if (msg.length() > Vars.maxTextLength) {
            Vars.ui.showErrorMessage("Final message length is above the text limit.");
            return;
        }
        Call.sendChatMessage(msg);
    }

    private BaseDialog iconChooserDialog() {
        BaseDialog iconChooser = new BaseDialog("Icon Chooser");
        iconChooser.addCloseButton();
        iconChooser.cont.pane(t -> {
            int icons = 0;
            for (char c : Iconc.all.toCharArray()) {
                if (icons % 8 == 0 && icons != 0) t.row();
                String sc = String.valueOf(c);
                t.button(sc, () -> {
                    icon = c;
                    chooserButton.setText(sc);
                    iconChooser.hide();
                }).size(50);
                icons += 1;
            }
        }).growX();
        return iconChooser;
    }
}
