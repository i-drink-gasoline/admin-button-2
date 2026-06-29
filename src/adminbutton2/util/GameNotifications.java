// SPDX-License-Identifier: GPL-3.0
package adminbutton2.util;

import arc.Core;
import arc.Events;
import arc.graphics.Color;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Interval;
import mindustry.Vars;
import mindustry.ctype.UnlockableContent;
import mindustry.game.EventType;
import mindustry.game.Team;
import mindustry.game.Teams;
import mindustry.gen.Building;
import mindustry.gen.Groups;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.type.Liquid;
import mindustry.type.LiquidStack;
import mindustry.world.Block;
import mindustry.world.blocks.power.ConsumeGenerator;
import mindustry.world.blocks.power.ThermalGenerator;
import mindustry.world.blocks.production.BeamDrill;
import mindustry.world.blocks.production.Drill;
import mindustry.world.blocks.production.GenericCrafter;
import mindustry.world.blocks.production.Pump;
import mindustry.world.blocks.production.Separator;
import mindustry.world.blocks.production.WallCrafter;

import adminbutton2.AdminVars;

public class GameNotifications {
    private Interval updateInterval = new Interval();
    private ObjectMap<Team, Data> teams = new ObjectMap<>();
    private Seq<UnlockableContent> unlockedContent = new Seq<>();

    static {
        Events.run(EventType.WorldLoadEvent.class, () -> {
            AdminVars.notifications.onWorldLoad();
        });

        Events.on(EventType.BlockBuildEndEvent.class, e -> {
            if (e.tile.build == null) return;
            if (e.breaking == false) AdminVars.notifications.onBuildChanged(e.tile.build);
        });

        Events.run(EventType.Trigger.update, () -> {
            AdminVars.notifications.onUpdate();
        });
    }

    public void onWorldLoad() {
        teams.clear();
        for (Teams.TeamData teamData : Vars.state.teams.present) {
            if (!teams.containsKey(teamData.team)) teams.put(teamData.team, new Data(teamData.team));
        }
    }

    public void onBuildChanged(Building build) {
        Team team = build.team;
        if (!teams.containsKey(team)) teams.put(team, new Data(team));
        teams.get(team).buildPlaced(build);
    }

    public void onUpdate() {
        if (!Vars.state.isPlaying() || !AdminVars.notifications.updateInterval.get(60f * 3f)) return;
        for (Teams.TeamData teamData : Vars.state.teams.present) {
            if (!teams.containsKey(teamData.team)) teams.put(teamData.team, new Data(teamData.team));
        }
        for (Data data : teams.values().iterator()) {
            data.update();
        }
    }

    private StuffProduced getStuffProduced(Building build) {
        StuffProduced stuff = new StuffProduced();
        Block block = build.block;
        if (block instanceof GenericCrafter) {
            GenericCrafter crafter = (GenericCrafter)block;
            if (crafter.outputItems != null) {
                for (ItemStack i : crafter.outputItems) {
                    stuff.items.add(i.item);
                }
            }
            if (crafter.outputLiquids != null) {
                for (LiquidStack i : crafter.outputLiquids) {
                    stuff.liquids.add(i.liquid);
                }
            }
        } else if (block instanceof Separator) {
            Separator separator = (Separator)block;
            for (ItemStack i : separator.results) {
                stuff.items.add(i.item);
            }
        } else if (block instanceof Drill) {
            Item item = ((Drill.DrillBuild)build).dominantItem;
            if (item != null) stuff.items.add(item);
        } else if (block instanceof BeamDrill) {
            Item item = ((BeamDrill.BeamDrillBuild)build).lastItem;
            if (item != null) stuff.items.add(item);
        } else if (block instanceof Pump) {
            Liquid liquid = ((Pump.PumpBuild)build).liquidDrop;
            if (liquid != null) stuff.liquids.add(liquid);
        } else if (block instanceof WallCrafter) {
            WallCrafter wallCrafter = (WallCrafter)block;
            stuff.items.add(wallCrafter.output);
        } else if (block instanceof ConsumeGenerator) {
            ConsumeGenerator consumeGenerator = (ConsumeGenerator)block;
            if (consumeGenerator.outputLiquid != null) stuff.liquids.add(consumeGenerator.outputLiquid.liquid);
        } else if (block instanceof ThermalGenerator) {
            ThermalGenerator thermalGenerator = (ThermalGenerator)block;
            if (thermalGenerator.outputLiquid != null) stuff.liquids.add(thermalGenerator.outputLiquid.liquid);
        }
        return stuff;
    }

    public void sendStartedProducingMessage(Team team, Seq<UnlockableContent> content) {
        if (!Core.settings.getBool("adminbutton2.notifications.show", true)) return;
        if (!Vars.state.rules.pvp && Core.settings.getBool("adminbutton2.notifications.only_pvp", true)) return;
        if (team == Vars.player.team() && Core.settings.getBool("adminbutton2.notifications.ignore_player_team", true)) return;
        String contentString = "";
        for (UnlockableContent c : content) {
            Color color = (c instanceof Item) ? ((Item)c).color : (c instanceof Liquid) ? ((Liquid)c).color : Color.white;
            contentString += (c.hasEmoji() ? c.emoji() : "") + "[#" + color + "]" + c.localizedName + "[] ";
        }
        AdminVars.sendChatNotification(Core.bundle.format("adminbutton2.notifications.started_producing", team.coloredName(), contentString));
    }

    private class StuffProduced {
        private final Seq<Item> items = new Seq<>();
        private final Seq<Liquid> liquids = new Seq<>();
    }

    private class Data {
        Team team;
        boolean[] itemsUnlocked = new boolean[Vars.content.items().size];
        boolean[] liquidsUnlocked = new boolean[Vars.content.liquids().size];
        boolean[] unitsUnlocked = new boolean[Vars.content.units().size];
        boolean initialized = false, initializedUnits = false;

        public Data(Team team) {
            this.team = team;
            for (Building build : team.data().buildings) {
                buildPlaced(build);
            }
            initialized = true;
        }

        public void buildPlaced(Building build) {
            unlockedContent.clear();
            StuffProduced stuff = getStuffProduced(build);
            for (Item item : stuff.items) {
                if (itemsUnlocked[item.id] == false) {
                    itemsUnlocked[item.id] = true;
                    if (initialized) unlockedContent.add(item);
                }
            }
            for (Liquid liquid : stuff.liquids) {
                if (liquidsUnlocked[liquid.id] == false) {
                    liquidsUnlocked[liquid.id] = true;
                    if (initialized) unlockedContent.add(liquid);
                }
            }
            if (!unlockedContent.isEmpty()) sendStartedProducingMessage(team, unlockedContent);
        }

        public void update() {
            unlockedContent.clear();
            if (Groups.unit.size() != 0) {
                team.data().units.each(unit -> {
                    if (unit.spawnedByCore) return;
                    if (unitsUnlocked[unit.type.id] == false) {
                        unitsUnlocked[unit.type.id] = true;
                        if (initializedUnits) unlockedContent.add(unit.type);
                    }
                });
                initializedUnits = true;
                if (!unlockedContent.isEmpty()) sendStartedProducingMessage(team, unlockedContent);
            }
        }
    }
}
