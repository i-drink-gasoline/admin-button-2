// SPDX-License-Identifier: GPL-3.0
package adminbutton2.util;

import arc.Core;
import arc.scene.ui.layout.Table;
import arc.util.CommandHandler;
import mindustry.Vars;
import mindustry.gen.Call;
import mindustry.gen.Iconc;
import mindustry.gen.Tex;
import mindustry.ui.dialogs.BaseDialog;
import mindustry.world.Tile;
import mindustry.world.blocks.logic.MessageBlock;

import adminbutton2.AdminVars;
import adminbutton2.Secret;
import adminbutton2.util.BaseUTF16;

public class Commands {
    public CommandHandler handler = new CommandHandler(Core.settings.getString("adminbutton2.commands.prefix", "."));
    public boolean smt = false;

    public Commands() {
        registerCommands();
    }

    public boolean runCommand(String command) {
        CommandHandler.CommandResponse response = handler.handleMessage(command);
        if (response.type == CommandHandler.ResponseType.valid) return true;
        if (response.type == CommandHandler.ResponseType.fewArguments) {
            Vars.ui.chatfrag.addMessage(Core.bundle.format("adminbutton2.command.error.few_arguments", handler.getPrefix() + response.command.text));
            return true;
        }
        if (response.type == CommandHandler.ResponseType.manyArguments) {
            Vars.ui.chatfrag.addMessage(Core.bundle.format("adminbutton2.command.error.many_arguments", handler.getPrefix() + response.command.text));
            return true;
        }
        return false;
    }

    private void registerCommands() {
        handler.register("help", "adminbutton2.command.help.description", args -> {
            BaseDialog dialog = new BaseDialog("@adminbutton2.command.help.dialog.title");
            dialog.addCloseButton();
            Table table = new Table();
            dialog.cont.pane(table).grow().fill();
            handler.getCommandList().each(c -> {
                table.table(Tex.button, t -> {
                    t.add(handler.getPrefix() + c.text + "[lightgray] " + c.paramText).row();
                    t.add("[orange] " + Core.bundle.get(c.description));
                }).pad(2f).fillX();
                table.row();
            });
            dialog.show();
        });

        handler.register("js", "<script...>", "adminbutton2.command.js.description", args -> {
            Vars.ui.chatfrag.addMessage(Vars.mods.getScripts().runConsole(args[0]));
        });

        handler.register("sc", "[message...]", "adminbutton2.command.sc.description", args -> {
            char icon = Core.settings.getString("adminbutton2-icon", String.valueOf(Iconc.admin)).charAt(0);
            String message = args.length == 0 ? Secret.generateRandomMessage(icon, Vars.maxTextLength) : Secret.generateSecretMessage(icon, args[0]);
            if (message.length() > Vars.maxTextLength) {
                Vars.ui.chatfrag.addMessage("[scarlet]" + Core.bundle.get("adminbutton2.admindialog.message_above_limit"));
                return;
            }
            Call.sendChatMessage(message);
        });

        handler.register("sm", "<message...>", "adminbutton2.command.sm.description", args -> {
            if (AdminVars.comms.selectedBuildingExists()) {
                AdminVars.comms.sendMessage(args[0]);
            } else {
                AdminVars.comms.selectBuildingAndSendMessage(args[0]);
            }
        });

        handler.register("smt", "adminbutton2.command.smt.description", args -> {
            smt = !smt;
            Vars.ui.chatfrag.addMessage("" + smt);
        });
    }
}
