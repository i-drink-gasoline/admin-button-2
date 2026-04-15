// SPDX-License-Identifier: GPL-3.0
package adminbutton2.util;

import arc.Core;
import arc.Events;
import arc.util.Timer;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.gen.Call;

public class MessageSender {
    private boolean connecting = false;
    private boolean wasGameOver = false;

    public MessageSender() {
        Events.on(EventType.ClientServerConnectEvent.class, e -> {
            connecting = true;
        });

        Events.on(EventType.WorldLoadEvent.class, e -> {
            wasGameOver = false;
            String message = Core.settings.getString("adminbutton2.message_sender.on_join", "");
            if (!connecting || !Vars.net.active() || message.isEmpty()) return;
            Timer.schedule(() -> Call.sendChatMessage(message), 1f);
            connecting = false;
        });

        Events.run(EventType.Trigger.update, () -> {
            if (Vars.state.gameOver && !wasGameOver) {
                wasGameOver = true;
                String message = Vars.state.won ? Core.settings.getString("adminbutton2.message_sender.on_win", "") : Core.settings.getString("adminbutton2.message_sender.on_loss", "");
                if (message.isEmpty()) return;
                Call.sendChatMessage(message);
            }
        });
    }
}
