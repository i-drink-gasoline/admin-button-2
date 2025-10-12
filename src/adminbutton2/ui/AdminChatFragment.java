// SPDX-License-Identifier: GPL-3.0
package adminbutton2.ui;

import arc.Core;
import arc.Input.TextInput;
import arc.scene.ui.Label;
import arc.scene.ui.TextField;
import arc.struct.Seq;
import arc.util.Time;
import mindustry.Vars;
import mindustry.gen.Call;
import mindustry.gen.Iconc;
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
    private Label fieldlabel;
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
            Field fieldlabelf = superclass.getDeclaredField("fieldlabel");
            fieldlabelf.setAccessible(true);
            fieldlabel = (Label) fieldlabelf.get(this);
            fieldlabel.update(() -> fieldlabel.setText(AdminVars.commands.smt ? "" + Iconc.logic : ">"));
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
            String prefix = Core.settings.getString("adminbutton2.chat.messagePrefix", "");
            String postfix = Core.settings.getString("adminbutton2.chat.messagePostfix", "");
            if (!(message.startsWith("/") && !(message.startsWith("/t ") || message.startsWith("/a "))) &&
                !message.isEmpty() && !(message.startsWith(prefix) && message.endsWith(postfix))) {
                String formattedMessage;
                if (message.startsWith("/t ") || message.startsWith("/a ")) {
                    formattedMessage = message.substring(0, 3) + prefix + message.substring(3) + postfix;
                } else {
                    formattedMessage = prefix + message + postfix;
                }
                if (formattedMessage.length() <= Vars.maxTextLength) {
                    chatfield.setText(formattedMessage);
                    message = formattedMessage;
                }
            }
            if (AdminVars.commands.smt && !message.startsWith("/") && !message.isEmpty()) {
                history.insert(1, message);
                if (AdminVars.communication.selectedBuildingExists()) {
                    AdminVars.communication.sendChatMessage(message);
                } else {
                    String msg = message;
                    AdminVars.communication.selectBuildingAndRun(() -> AdminVars.communication.sendChatMessage(msg));
                }
                chatfield.setText("");
                sendMessage.invoke(this);
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
        if (!secret.isEmpty()) addMessageFinal(AdminVars.chatNotificationPrefix + secret);
    }

    private void addMessageFinal(String message) {
        AdminVars.messages.addMessage(message);
        super.addMessage(message);
    }
}
