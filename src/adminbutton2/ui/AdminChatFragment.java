// SPDX-License-Identifier: GPL-3.0
package adminbutton2.ui;

import mindustry.ui.fragments.ChatFragment;

import adminbutton2.AdminVars;
import adminbutton2.Secret;

public class AdminChatFragment extends ChatFragment {
    @Override
    public void addMessage(String message) {
        addMessageFinal(message);
        String secret = Secret.readSecretMessage(message);
        if (!secret.isEmpty()) addMessageFinal("[Admin button]: " + secret);
    }

    private void addMessageFinal(String message) {
        AdminVars.messages.addMessage(message);
        super.addMessage(message);
    }
}
