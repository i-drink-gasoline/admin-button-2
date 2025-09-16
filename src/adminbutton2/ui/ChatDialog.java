// SPDX-License-Identifier: GPL-3.0
package adminbutton2.ui;

import arc.Core;
import arc.graphics.Color;
import arc.scene.ui.Label;
import mindustry.Vars;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;

import adminbutton2.AdminVars;

public class ChatDialog extends BaseDialog {
    private Color prefixColor = Color.white.cpy();

    public ChatDialog() {
        super("@adminbutton2.chatdialog.title");
        addCloseButton();
        cont.table(t -> {
            t.defaults().pad(0f, 4f, 0f, 4f).growY();
            t.buttonRow("@adminbutton2.message_list.title", Icon.chat, () -> AdminVars.messages.show()).get().getLabel().setWrap(false);
            t.buttonRow("@adminbutton2.command.help.dialog.title", Icon.bookOpen, () -> AdminVars.commands.runCommand(AdminVars.commands.handler.getPrefix() + "help")).get().getLabel().setWrap(false);
        });
        cont.row();
        cont.table(Tex.button, t -> {
            t.button(Tex.whiteui, Styles.squarei, 40, () -> {
                Vars.ui.picker.show(prefixColor, false, c -> {
                    String color = "[#" + c.toString().substring(0, 6) + "]";
                    if (color.equals("[#ffffff]")) color = "";
                    Core.settings.put("adminbutton2.chat.messagePrefix", color);
                    Core.settings.put("adminbutton2.chat.messagePostfix", "");
                });
            }).size(54f).update(b -> {
                String color = Core.settings.getString("adminbutton2.chat.messagePrefix", "");
                if (color.startsWith("[#") && color.endsWith("]") && color.length() == 9) {
                    prefixColor = Color.valueOf(color.substring(2, 8));
                } else {
                    prefixColor = Color.white.cpy();
                }
                b.getStyle().imageUpColor = prefixColor;
            });
            t.row();
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
                t2.field("", s -> Core.settings.put("adminbutton2.chat.messagePrefix", s)).growX().update(f -> f.setText(Core.settings.getString("adminbutton2.chat.messagePrefix", "")));
                t2.add().width(24f);
                t2.field("", s -> Core.settings.put("adminbutton2.chat.messagePostfix", s)).growX().update(f -> f.setText(Core.settings.getString("adminbutton2.chat.messagePostfix", "")));
                t2.row();

            }).growX();
        });
    }
}
