// SPDX-License-Identifier: GPL-3.0
package adminbutton2.util;

import arc.Core;
import arc.func.Cons;
import arc.math.Mathf;
import arc.math.geom.Point2;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.CommandHandler;
import mindustry.Vars;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.gen.Call;
import mindustry.gen.Iconc;
import mindustry.gen.Tex;
import mindustry.ui.dialogs.BaseDialog;
import mindustry.world.Tile;
import mindustry.world.blocks.logic.MessageBlock;
import mindustry.world.blocks.power.PowerNode;

import adminbutton2.AdminVars;
import adminbutton2.Secret;
import adminbutton2.util.BaseUTF16;

import java.lang.reflect.Method;

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
            message = AdminVars.secrets.formatSecretMessage(message);
            if (message.length() > Vars.maxTextLength) {
                Vars.ui.chatfrag.addMessage("[scarlet]" + Core.bundle.get("adminbutton2.admindialog.message_above_limit"));
                return;
            }
            Call.sendChatMessage(message);
        });

        handler.register("sm", "<message...>", "adminbutton2.command.sm.description", args -> {
            if (AdminVars.communication.selectedBuildingExists()) {
                AdminVars.communication.sendChatMessage(args[0]);
            } else {
                AdminVars.communication.selectBuildingAndRun(() -> AdminVars.communication.sendChatMessage(args[0]));
            }
        });

        handler.register("smt", "adminbutton2.command.smt.description", args -> {
            smt = !smt;
            Vars.ui.chatfrag.addMessage("" + smt);
        });

        handler.register("linknodes", "adminbutton2.command.linknodes.description", args -> {
            if (Vars.player.team().data().buildingTree == null) return;
            if (AdminVars.interaction.interacting) return;
            Seq<Building> buildings = new Seq<>();
            Vars.player.team().data().buildingTree.getObjects(buildings);
            Seq<PowerNode.PowerNodeBuild> nodes = new Seq<>();
            for (int i = buildings.size - 1; i >= 0; i--) {
                Building building = buildings.get(i);
                if (!(building instanceof PowerNode.PowerNodeBuild)) continue;
                PowerNode.PowerNodeBuild build = (PowerNode.PowerNodeBuild) building;
                nodes.add(build);
            }
            Thread thread = new Thread(() -> {
                Method method;
                try {
                    method = PowerNode.class.getDeclaredMethod("getPotentialLinks", Tile.class, Team.class, Cons.class);
                    method.setAccessible(true);
                } catch (NoSuchMethodException e) {
                    Vars.ui.showException(e);
                    return;
                }
                AdminVars.interaction.interacting = true;
                String interactor = "linknodes";
                AdminVars.interaction.interactor = interactor;
                int newLinks = 0;
                for (PowerNode.PowerNodeBuild node : nodes) {
                    if (node.tile().build != node) continue;
                    Seq<Point2> points = new Seq<>();
                    node.power.links.each(l -> {
                        points.add(Point2.unpack(l).sub(node.tile.x, node.tile.y));
                    });
                    Cons<Building> a = link -> {
                        if(node.power.links.contains(link.pos()) || points.size >= ((PowerNode)node.block).maxNodes) return;
                        points.add(new Point2(link.tileX() - node.tile.x, link.tileY() - node.tile.y));
                    };
                    try {
                        method.invoke(node.block, node.tile, node.team, a);
                    } catch (Exception e) {
                        Vars.ui.showException(e);
                        break;
                    }
                    if (points.size == node.power.links.size || points.size == 0) continue;
                    newLinks += points.size - node.power.links.size;
                    try {
                        if (node.power.links.size != 0) {
                            while (true) {
                                if (AdminVars.interaction.willInteract(interactor)) {
                                    node.configure(new Seq<Point2>().toArray(Point2.class));
                                    Thread.sleep(Mathf.ceil(AdminVars.interaction.interactionCooldown * 1000f));
                                    break;
                                } else Thread.sleep(Mathf.ceil(AdminVars.interaction.timeLeft() / 60f * 1000f));
                            }
                        }
                        while (true) {
                            if (AdminVars.interaction.willInteract(interactor)) {
                                node.configure(points.toArray(Point2.class));
                                Thread.sleep(Mathf.ceil(AdminVars.interaction.interactionCooldown * 1000f));
                                break;
                            } else Thread.sleep(Mathf.ceil(AdminVars.interaction.timeLeft() / 60f * 1000f));
                        }
                    } catch (InterruptedException e) {
                        Vars.ui.showException(e);
                        break;
                    }
                }
                Vars.ui.chatfrag.addMessage(AdminVars.chatNotificationPrefix + "[stat]" + Core.bundle.format("adminbutton2.command.linknodes.new_links", newLinks));
                AdminVars.interaction.interacting = false;
            }, "[AB2] command linknodes");
            thread.setDaemon(true);
            thread.start();
        });
    }
}
