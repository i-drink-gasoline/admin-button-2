// SPDX-License-Identifier: GPL-3.0
package adminbutton2.ui;

import arc.Core;
import arc.scene.ui.ScrollPane;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import mindustry.gen.Icon;
import mindustry.ui.dialogs.BaseDialog;

public class MessageList extends BaseDialog {
    private Table messageTable;
    private Seq<String> messages = new Seq<String>();
    private String text = "";
    public MessageList() {
        super("@adminbutton2.message_list.title");
        addCloseButton();
        cont.top();
        cont.table(t -> {
            t.image(Icon.zoom);
            t.field(text, a -> {
                text = a;
                rebuild();
            }).growX().row();
        }).fillX().row();
        messageTable = new Table();
        ScrollPane pane = new ScrollPane(messageTable);
        cont.pane(pane).padLeft(80).padRight(80).growX();
    }

    public void addMessage(String message) {
        addMessage(message, true);
    }

    public void addMessage(String message, boolean add) {
        if (add) messages.add(message);
        if (message.contains(text)) {
            messageTable.button(Icon.copy, () -> Core.app.setClipboardText(message));
            messageTable.add(message).growX().padLeft(8).row();
        }
    }

    private void rebuild() {
        messageTable.clearChildren();
        messages.each(m -> addMessage(m, false));
    }
}
