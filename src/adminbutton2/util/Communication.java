// SPDX-License-Identifier: GPL-3.0
package adminbutton2.util;

import arc.Core;
import arc.Events;
import arc.math.Interp;
import arc.math.Mathf;
import arc.scene.event.Touchable;
import arc.scene.ui.Label;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Time;
import arc.util.io.Streams.OptimizedByteArrayOutputStream;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.game.Schematic;
import mindustry.gen.Building;
import mindustry.gen.Icon;
import mindustry.gen.Player;
import mindustry.gen.Tex;
import mindustry.input.InputHandler;
import mindustry.world.Tile;
import mindustry.world.blocks.logic.CanvasBlock;
import mindustry.world.blocks.logic.LogicBlock;
import mindustry.world.blocks.logic.MessageBlock;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import adminbutton2.AdminVars;

public class Communication {
    public static final String chatMessagePrefix = "<AB2> ";
    public static final String messageMagic = "AB2\uffff";
    public static final String logicMagicStart = "stop\nprint \"(Admin Button 2) mod communication\uffff\"\nprint \"";
    public static final String logicMagicEnd = "\"";
    public static final byte canvasMagic = -79;
    public BaseUTF16 messageBase = new BaseUTF16('!', Character.MAX_VALUE);
    public BaseUTF16 logicBase = new BaseUTF16((char)0x80, (char)0xD7FF);
    public boolean selectingBuilding = false;
    public Building selectedBuilding = null;
    public Runnable runOnSelect = null;
    public int chatMessageMaxLength = Vars.maxTextLength * 2;

    static {
        Events.on(EventType.ConfigEvent.class, e -> {
            if (e.player == null) return;
            if (e.tile.block instanceof MessageBlock || e.tile.block instanceof CanvasBlock || e.tile.block instanceof LogicBlock) {
                AdminVars.communication.readMessage(e.tile, e.player);
            }
        });
        Events.on(EventType.TapEvent.class, e -> {
            if (AdminVars.communication.selectingBuilding) {
                if (e.player == Vars.player) {
                    Tile tile = Vars.world.tileWorld(Core.input.mouseWorldX(), Core.input.mouseWorldY());
                    if (tile.build == null) return;
                    if (!tile.build.interactable(Vars.player.team())) return;
                    if (tile.build.block instanceof MessageBlock) {
                        if (!((MessageBlock)tile.build.block).accessible()) return;
                    } else if (tile.build.block instanceof LogicBlock) {
                        if (!((LogicBlock)tile.build.block).accessible()) return;
                    } else if (!(tile.build.block instanceof CanvasBlock)) return;
                    AdminVars.communication.selectedBuilding = tile.build;
                    AdminVars.communication.runOnSelect.run();
                    AdminVars.communication.selectingBuilding = false;
                    AdminVars.communication.runOnSelect = null;
                }
            }
        });
        Events.run(EventType.WorldLoadEvent.class, () -> AdminVars.communication.selectedBuilding = null);
    }

    public boolean selectedBuildingExists() {
        return selectedBuilding != null && selectedBuilding.tile().build == selectedBuilding;
    }

    public void selectBuildingAndRun(Runnable runnable) {
        selectingBuilding = true;
        runOnSelect = runnable;
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

    public void sendChatMessage(String message) {
        byte[] data = message.getBytes(StandardCharsets.UTF_8);
        sendMessage(MessageType.ChatMessage, data);
    }

    public void sendSchematic(Schematic schematic) {
        OptimizedByteArrayOutputStream out = new OptimizedByteArrayOutputStream(1024);
        try {
            Vars.schematics.write(schematic, out);
        } catch (IOException e) {
            Vars.ui.showException(e);
        }
        byte[] data = new byte[out.size()];
        System.arraycopy(out.getBuffer(), 0, data, 0, out.size());
        sendMessage(MessageType.Schematic, data);
    }

    public void sendMessage(MessageType type, byte[] data) {
        sendMessage(type.value, data);
    }

    public void sendMessage(byte type, byte[] data) {
        byte[] fullData = new byte[data.length + 1];
        fullData[0] = type;
        System.arraycopy(data, 0, fullData, 1, data.length);
        sendMessage(fullData);
    }
    
    public void sendMessage(byte[] data) {
        if (selectedBuilding == null || !selectedBuilding.interactable(Vars.player.team())) return;
        byte[] deflated = deflate(data);
        if (selectedBuilding.block instanceof MessageBlock) {
            if (!((MessageBlock)selectedBuilding.block()).accessible()) return;
            String msg = messageMagic + messageBase.encode(deflated);
            if (msg.length() <= ((MessageBlock)selectedBuilding.block).maxTextLength) {
                selectedBuilding.configure(msg);
            } else {
                Vars.ui.chatfrag.addMessage(AdminVars.chatNotificationPrefix + "[scarlet]" + Core.bundle.get("adminbutton2.admindialog.message_above_limit"));
            }
        } else if (selectedBuilding.block instanceof LogicBlock) {
            if (!((LogicBlock)selectedBuilding.block()).accessible()) return;
            String msg = logicMagicStart + logicBase.encode(deflated) + logicMagicEnd;
            byte[] bytes = LogicBlock.compress(msg, new Seq<>());
            if (bytes.length - Integer.BYTES <= Short.MAX_VALUE / 2) {;
                selectedBuilding.configure(bytes);
            } else {
                Vars.ui.chatfrag.addMessage(AdminVars.chatNotificationPrefix + "[scarlet]" + Core.bundle.get("adminbutton2.admindialog.message_above_limit"));
            }
        } else if (selectedBuilding.block instanceof CanvasBlock) {
            if (deflated.length <= Byte.MAX_VALUE - 2 && deflated.length + 2 <= ((CanvasBlock.CanvasBuild)selectedBuilding).data.length) {
                byte[] msg = new byte[((CanvasBlock.CanvasBuild)selectedBuilding).data.length];
                msg[0] = canvasMagic;
                msg[1] = (byte)deflated.length;
                System.arraycopy(deflated, 0, msg, 2, deflated.length);
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
                bytes = messageBase.decode(message);
            } catch (IllegalArgumentException e) {
                return;
            }
        } else if (build.block instanceof LogicBlock) {
            String message = ((LogicBlock.LogicBuild)build).code;
            if (!message.startsWith(logicMagicStart) || !message.endsWith(logicMagicEnd)) return;
            message = message.substring(logicMagicStart.length(), message.length() - logicMagicEnd.length());
            try {
                bytes = logicBase.decode(message);
            } catch (IllegalArgumentException e) {
                return;
            }
        } else if (build.block instanceof CanvasBlock) {
            byte[] msg = (byte[])build.config();
            if (msg[0] != canvasMagic) return;
            if (msg[1] < 0 || msg[1] > msg.length - 2) return;
            bytes = new byte[msg[1]];
            System.arraycopy(msg, 2, bytes, 0, msg[1]);
        } else {
            return;
        }
        if (bytes == null) return;
        byte[] fullData = inflate(bytes, 1<<16);
        if (fullData == null || fullData.length == 0) return;
        byte[] data = new byte[fullData.length - 1];
        System.arraycopy(fullData, 1, data, 0, data.length);
        processMessage(fullData[0], data, player);
    }

    public void processMessage(byte type, byte[] data, Player player) {
        if (data == null) return;
        if (type == MessageType.ChatMessage.value) {
            String message = new String(data, 0, data.length, StandardCharsets.UTF_8);
            message = message.length() > chatMessageMaxLength ? message.substring(0, chatMessageMaxLength) : message;
            Vars.ui.chatfrag.addMessage(chatMessagePrefix + "[coral][[[#FFFFFFFF]" + player.coloredName() + "[coral]]:[white] " + message);
            player.lastText(message);
            player.textFadeTime(1f);
        } else if (type == MessageType.Schematic.value) {
            Table firstTable = new Table();
            firstTable.bottom();
            Table table = firstTable.table(Tex.paneSolid).get();
            table.touchable = Touchable.enabled;
            String name = player.name == null ? "null" : player.name;
            table.add(Core.bundle.format("adminbutton2.communication.playerSharedSchematic", name)).padBottom(8f).row();
            table.table(t -> {
                t.defaults().size(40f).pad(0f, 4f, 0f, 4f);
                t.add().width(40f);
                t.add().growX();
                t.button(Icon.eyeSmall, () -> {
                    try {
                        Vars.ui.schematics.showInfo(Vars.schematics.read(new ByteArrayInputStream(data)));
                    } catch (IOException e) {
                        Vars.ui.showException(e);
                    }
                });
                t.button(Icon.saveSmall, () -> {
                    try {
                        Vars.control.input.lastSchematic = Vars.schematics.read(new ByteArrayInputStream(data));
                        Method showSchematicSave = InputHandler.class.getDeclaredMethod("showSchematicSave");
                        showSchematicSave.setAccessible(true);
                        showSchematicSave.invoke(Vars.control.input);
                    } catch (Exception e) {
                        Vars.ui.showException(e);
                    }
                });
                t.button(Icon.cancelSmall, () -> table.remove()).width(40f);
                t.add().growX();
                Label timer = t.add("").width(40f).get();
                float time = Time.time + (30 * Time.toSeconds);
                timer.update(() -> {
                    float timeLeft = ((time - Time.time) / Time.toSeconds);
                    timer.setText("" + Mathf.ceil(timeLeft));
                    if (timeLeft < 0) firstTable.remove();
                });
            });
            firstTable.setFillParent(true);
            Core.scene.add(firstTable);
        }
    }

    public enum MessageType {
        ChatMessage((byte)0),
        Schematic((byte)1),
        ;

        public final byte value;
        MessageType(byte value) {
            this.value = value;
        }
    }
}
