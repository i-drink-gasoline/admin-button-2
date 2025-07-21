// SPDX-License-Identifier: GPL-3.0
package adminbutton2.ui;

import arc.Core;
import arc.Input.TextInput;
import arc.scene.ui.TextField;
import arc.struct.Seq;
import arc.util.Time;
import mindustry.Vars;
import mindustry.gen.Call;
import mindustry.ui.fragments.ChatFragment;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import adminbutton2.AdminVars;
import adminbutton2.Secret;

public class AdminChatFragment extends ChatFragment {
    private Field shown;
    private Field scrollPos;
    private TextField chatfield;
    private Method sendMessage;
    private Seq<String> history;
    private boolean reflected = false;

    @SuppressWarnings("unchecked")
    public AdminChatFragment() {
        super();
        try {
            Class<?> superclass = getClass().getSuperclass();
            shown = superclass.getDeclaredField("shown");
            shown.setAccessible(true);
            Field chatf = superclass.getDeclaredField("chatfield");
            chatf.setAccessible(true);
            chatfield = (TextField) chatf.get(this);
            scrollPos = superclass.getDeclaredField("scrollPos");
            scrollPos.setAccessible(true);
            sendMessage = superclass.getDeclaredMethod("sendMessage");
            sendMessage.setAccessible(true);
            Field historyf = superclass.getDeclaredField("history");
            historyf.setAccessible(true);
            history = (Seq<String>) historyf.get(this);
            reflected = true;
        } catch (Exception e) {}
    }

    private void sendMessage() {
        String message = chatfield.getText().trim();
        try {
            if (AdminVars.commands.runCommand(message)) {
                clearChatInput();
                history.insert(1, message);
                return;
            }
            sendMessage.invoke(this);
        } catch (Exception e) {
            reflected = false;
            Vars.ui.showException(e);
        }
    }

    @Override
    public void toggle() {
        if (!reflected) {
            super.toggle();
            return;
        }
        try {
            if (!shown()) {
                Core.scene.setKeyboardFocus(chatfield);
                shown.setBoolean(this, true);
                if (Vars.mobile) {
                    TextInput input = new TextInput();
                    input.maxLength = Vars.maxTextLength;
                    input.accepted = text -> {
                        chatfield.setText(text);
                        sendMessage();
                        hide();
                        Core.input.setOnscreenKeyboardVisible(false);
                    };
                    input.canceled = this::hide;
                    Core.input.getTextInput(input);
                } else {
                    chatfield.fireClick();
                }
            } else {
                Time.runTask(2f, () -> {
                    try {
                        Core.scene.setKeyboardFocus(null);
                        shown.setBoolean(this, false);
                        scrollPos.setInt(this, 0);
                        sendMessage();
                    } catch (IllegalAccessException e) {
                        Vars.ui.showException(e);
                        reflected = false;
                    }
                });
            }
        } catch (IllegalAccessException e) {
            Vars.ui.showException(e);
            reflected = false;
        }
    }

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
