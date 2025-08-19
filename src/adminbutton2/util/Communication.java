// SPDX-License-Identifier: GPL-3.0
package adminbutton2.util;

import arc.Core;
import arc.Events;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.gen.Building;
import mindustry.gen.Player;
import mindustry.world.Tile;
import mindustry.world.blocks.logic.MessageBlock;

import java.nio.charset.StandardCharsets;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import adminbutton2.AdminVars;

public class Communication {
    public static final String chatMessagePrefix = "<AB2> ";
    public static final String messageMagic = "AB2\uffff";
    public boolean selectingBuilding = false;
    public Building selectedBuilding = null;
    public String sendOnSelect = null;

    static {
        Events.on(EventType.ConfigEvent.class, e -> {
            if (e.player == null) return;
            if (e.tile.block instanceof MessageBlock) {
                AdminVars.comms.readMessage(e.tile, e.player);
            }
        });
        Events.on(EventType.TapEvent.class, e -> {
            if (AdminVars.comms.selectingBuilding) {
                if (e.player == Vars.player) {
                    Tile tile = Vars.world.tileWorld(Core.input.mouseWorldX(), Core.input.mouseWorldY());
                    if (tile.build == null) return;
                    if (!(tile.build.block instanceof MessageBlock)) return;
                    AdminVars.comms.selectedBuilding = tile.build;
                    AdminVars.comms.sendMessage(AdminVars.comms.sendOnSelect);
                    AdminVars.comms.selectingBuilding = false;
                    AdminVars.comms.sendOnSelect = null;
                }
            }
        });
        Events.run(EventType.WorldLoadEvent.class, () -> AdminVars.comms.selectedBuilding = null);
    }

    public boolean selectedBuildingExists() {
        return selectedBuilding != null && selectedBuilding.tile().build == selectedBuilding;
    }

    public void selectBuildingAndSendMessage(String message) {
        selectingBuilding = true;
        sendOnSelect = message;
        Vars.ui.chatfrag.addMessage(AdminVars.chatNotificationPrefix + "[scarlet]" + Core.bundle.get("adminbutton2.communication.selectBuilding"));
    }

    public byte[] deflate(byte[] bytes) {
        Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION);
        deflater.setInput(bytes);
        deflater.finish();
        byte[] buffer = new byte[bytes.length * 2 + 16];
        int length = deflater.deflate(buffer);
        byte[] compressed = new byte[length];
        System.arraycopy(buffer, 0, compressed, 0, length);
        deflater.end();
        return compressed;
    }

    public byte[] inflate(byte[] bytes, int size) {
        Inflater inflater = new Inflater();
        inflater.setInput(bytes);
        byte[] inflated = new byte[size];
        try {
            int length = inflater.inflate(inflated);
            inflater.end();
            byte[] result = new byte[length];
            System.arraycopy(inflated, 0, result, 0, length);
            return result;
        } catch (java.util.zip.DataFormatException e) {
            return null;
        }
    }

    public void sendMessage(String message) {
        if (selectedBuilding == null) return;
        if (selectedBuilding.block instanceof MessageBlock) {
            if (!selectedBuilding.interactable(Vars.player.team()) || !((MessageBlock)selectedBuilding.block()).accessible()) return;
            byte[] tmpData = message.getBytes(StandardCharsets.UTF_8);
            byte[] data = new byte[tmpData.length + 1];
            data[0] = MessageType.ChatMessage.value;
            System.arraycopy(tmpData, 0, data, 1, tmpData.length);
            String msg = messageMagic + BaseUTF16.encode(deflate(data));
            if (msg.length() <= ((MessageBlock)selectedBuilding.block).maxTextLength) {
                selectedBuilding.configure(msg);
            } else {
                Vars.ui.chatfrag.addMessage(AdminVars.chatNotificationPrefix + "[scarlet]" + Core.bundle.get("adminbutton2.admindialog.message_above_limit"));
            }
        }
    }

    public void readMessage(Building build, Player player) {
        byte[] bytes;
        if (build.block instanceof MessageBlock) {
            String message = (String)build.config();
            if (!message.startsWith(messageMagic)) return;
            message = message.substring(messageMagic.length());
            try {
                bytes = BaseUTF16.decode(message);
            } catch (IllegalArgumentException e) {
                return;
            }
        } else {
            return;
        }
        if (bytes == null) return;
        byte[] data = inflate(bytes, 256);
        if (data == null) return;
        if (data[0] == MessageType.ChatMessage.value) {
            Vars.ui.chatfrag.addMessage(chatMessagePrefix + "[coral][[[#FFFFFFFF]" + player.name + "[coral]]:[white] " + new String(data, 1, data.length - 1, StandardCharsets.UTF_8));
        }
    }

    public enum MessageType {
        ChatMessage((byte)0);

        public final byte value;
        MessageType(byte value) {
            this.value = value;
        }
    }
}
