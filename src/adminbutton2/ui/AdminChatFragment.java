// SPDX-License-Identifier: GPL-3.0
package adminbutton2.ui;

import arc.scene.Group;
import arc.util.Log;
import mindustry.ui.fragments.ChatFragment;

import adminbutton2.AdminVars;
import adminbutton2.Secret;

public class AdminChatFragment extends ChatFragment {
    private ChatFragment chatfrag;

    public AdminChatFragment(ChatFragment orig) {
        chatfrag = orig;
    }

    @Override
    public void build(Group parent) {
        chatfrag.build(parent);
    }

    @Override
    public void clearMessages() {
        chatfrag.clearMessages();
    }

    @Override
    public void draw() {
        chatfrag.draw();
    }

    @Override
    public void toggle() {
        chatfrag.toggle();
    }

    @Override
    public void hide() {
        chatfrag.hide();
    }

    @Override
    public void updateChat() {
        chatfrag.updateChat();
    }

    @Override
    public void nextMode() {
        chatfrag.nextMode();
    }

    @Override
    public void clearChatInput() {
        chatfrag.clearChatInput();
    }

    @Override
    public void updateCursor() {
        chatfrag.updateCursor();
    }

    @Override
    public boolean shown() {
        return chatfrag.shown();
    }

    @Override
    public void addMessage(String message) {
        addMessageFinal(message);
        String secret = Secret.readSecretMessage(message);
        if (!secret.isEmpty()) addMessageFinal("[Admin button]: " + secret);
    }

    private void addMessageFinal(String message) {
        AdminVars.messages.addMessage(message);
        chatfrag.addMessage(message);
    }
}
