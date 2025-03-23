// SPDX-License-Identifier: GPL-3.0
package adminbutton2.ui;

import arc.scene.ui.TextField;
import arc.scene.ui.layout.Table;
import mindustry.Vars;
import mindustry.gen.Call;
import mindustry.ui.dialogs.BaseDialog;

import adminbutton2.Secret;

public class AdminDialog extends BaseDialog {
    private String messagePrefix = "";
    private boolean messagePrefixEnabled = false;
    private String secretMessage = "";

    public AdminDialog() {
        super("Admin Button");
        addCloseButton();
        cont.table(t -> {
            t.table(t2 -> {
                TextField prefix = t2.field("", text -> messagePrefix = text).get();
                prefix.setMessageText("/t");
                prefix.setDisabled(!messagePrefixEnabled);
                t2.check("Message prefix", messagePrefixEnabled, enabled -> prefix.setDisabled(!(messagePrefixEnabled = enabled))).get();
                t2.getCells().swap(0, 1);
            });;
            t.row();
            t.field("", text -> secretMessage = text).width(400).height(50).get().setMessageText("Secret message");
            t.row();
            t.table(t2 -> {
                t2.button("Send secret message", () -> sendMessage(Secret.generateSecretMessage(secretMessage))).width(200).height(50);
                t2.button("Send random message", () -> sendMessage(Secret.generateRandomMessage(Vars.maxTextLength - messagePrefix.length()))).width(200).height(50);
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
}
