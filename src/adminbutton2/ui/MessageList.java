// SPDX-License-Identifier: GPL-3.0
package adminbutton2.ui;

import arc.Core;
import arc.scene.ui.ScrollPane;
import arc.scene.ui.layout.Table;
import mindustry.gen.Icon;
import mindustry.ui.dialogs.BaseDialog;

public class MessageList extends BaseDialog {
    private Table messageTable;
    public MessageList() {
        super("Message List");
        addCloseButton();
        messageTable = new Table();
        ScrollPane pane = new ScrollPane(messageTable);
        cont.pane(pane).padLeft(80).padRight(80).growX();
    }

    public void addMessage(String message) {
        messageTable.button(Icon.copy, () -> Core.app.setClipboardText(message));
        messageTable.add(message).growX().padLeft(8).row();
    }
}
