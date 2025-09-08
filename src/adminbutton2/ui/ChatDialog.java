// SPDX-License-Identifier: GPL-3.0
package adminbutton2.ui;

import arc.Core;
import arc.scene.ui.Label;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.ui.dialogs.BaseDialog;

import adminbutton2.AdminVars;

public class ChatDialog extends BaseDialog {
    public ChatDialog() {
        super("@adminbutton2.chatdialog.title");
        addCloseButton();
        cont.buttonRow("@adminbutton2.message_list.title", Icon.chat, () -> AdminVars.messages.show()).get().getLabel().setWrap(false);
        cont.row();
        cont.table(Tex.button, t -> {
            Label exampleLabel = t.add("").padBottom(8f).get();
            exampleLabel.update(() -> {
                String exampleMessage = Core.settings.getString("adminbutton2.chat.messagePrefix", "") + Core.bundle.get("adminbutton2.chatdialog.example_message") + Core.settings.getString("adminbutton2.chat.messagePostfix", "");
                exampleLabel.setText(exampleMessage);
            });
            t.row();
            t.table(t2 -> {
                t2.add("@adminbutton2.chatdialog.message_prefix");
                t2.add().width(24f);
                t2.add("@adminbutton2.chatdialog.message_postfix");
                t2.row();
                t2.field(Core.settings.getString("adminbutton2.chat.messagePrefix", ""), s -> Core.settings.put("adminbutton2.chat.messagePrefix", s)).growX();
                t2.add().width(24f);
                t2.field(Core.settings.getString("adminbutton2.chat.messagePostfix", ""), s -> Core.settings.put("adminbutton2.chat.messagePostfix", s)).growX();
            }).growX();
        });
    }
}
