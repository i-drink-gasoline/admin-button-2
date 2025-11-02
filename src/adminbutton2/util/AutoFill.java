// SPDX-License-Identifier: GPL-3.0
package adminbutton2.util;

import arc.Core;
import arc.Events;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Time;
import mindustry.Vars;
import mindustry.entities.bullet.BulletType;
import mindustry.game.EventType;
import mindustry.gen.Building;
import mindustry.gen.Call;
import mindustry.gen.Unit;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.world.Block;
import mindustry.world.blocks.defense.turrets.ItemTurret;
import mindustry.world.blocks.storage.CoreBlock;
import mindustry.world.blocks.storage.StorageBlock;
import mindustry.world.consumers.Consume;
import mindustry.world.consumers.ConsumeItemDynamic;
import mindustry.world.consumers.ConsumeItemExplode;
import mindustry.world.consumers.ConsumeItemFilter;
import mindustry.world.consumers.ConsumeItems;

import adminbutton2.AdminVars;

public class AutoFill {
    public boolean selectBuildings = false;
    public boolean enabled = false;
    public boolean fillOnlySelectedBuildings = false;
    public int coreMinimumRequestAmount;
    public boolean[] fillMap;
    static Seq<Building> selected = new Seq<>();
    public Color colorSelected = Pal.accent.cpy().a(0.75f).premultiplyAlpha(), colorSelectedNear = Pal.accent.cpy().a(0.75f), colorNear = Pal.plastanium.cpy().a(0.75f), colorCore = Pal.reactorPurple2.cpy().a(0.75f), colorStorage = Pal.techBlue.cpy().a(0.75f);
    Seq<Building> validCloseBuildings = new Seq<>();
    Building core;
    Unit unit;
    Seq<Building> coreBuildings = new Seq<>();
    Seq<Building> storageBuildings = new Seq<>();

    static {
        Events.on(EventType.TapEvent.class, e -> {
            if (e.player == Vars.player) {
                if (AdminVars.autofill.selectBuildings && AdminVars.autofill.enabled) {
                    Building b = e.tile.build;
                    if (b != null) {
                        if (selected.contains(b)) {
                            selected.remove(b);
                        } else {
                            if (AdminVars.autofill.shouldFillBuilding(e.tile.build, true)) selected.add(b);
                        }
                    }
                }
            };
        });
        Events.run(EventType.Trigger.drawOver, () -> AdminVars.autofill.draw());
        Events.run(EventType.Trigger.update, () -> AdminVars.autofill.update());
        Events.run(EventType.WorldLoadEvent.class, () -> selected.clear());
    }

    public AutoFill() {
        Seq<Block> blocks = Vars.content.blocks();
        fillMap = new boolean[blocks.size];
        for (int i = 0; i < blocks.size; i++) {
            fillMap[i] = Core.settings.getBool("adminbutton2.autofill.fill." + blocks.get(i).name, true);
        }
    }

    private boolean shouldFillBuilding(Building building, boolean select) {
        if (building.team != Vars.player.team()) return false;
        if (!fillMap[building.block.id] && !select) return false;
        return validBlock(building.block);
    }

    public boolean validBlock(Block block) {
        for (Consume c : block.consumers) {
            if (c instanceof ConsumeItems || c instanceof ConsumeItemDynamic || c instanceof ConsumeItemFilter) {
                if (c instanceof ConsumeItemExplode) continue;
                return true;
            }
        }
        return false;
    }

    public void draw() {
        if (!enabled || Vars.state.rules.onlyDepositCore) return;
        float draw_rot = Time.time;
        Draw.z(Layer.effect);
        selected.each(b -> {
            Color color = colorSelected;
            if (Vars.player.within(b, Vars.itemTransferRange)) color = colorSelectedNear;
            Draw.color(color);
            Lines.square(b.x, b.y, b.block.size * Vars.tilesize / 1.5f, draw_rot);
        });
        if (!fillOnlySelectedBuildings) {
            Draw.color(colorNear);
            Vars.indexer.eachBlock(unit, Vars.itemTransferRange, b -> {
                return shouldFillBuilding(b, false) && !selected.contains(b);
            }, b -> {
                Lines.square(b.x, b.y, b.block.size * Vars.tilesize / 1.5f, draw_rot);
            });
        }
        Draw.color(colorCore);
        core = getClosestCore();
        if (core != null && Vars.player.within(core, Vars.itemTransferRange)) {
            Draw.color(colorCore);
            Lines.square(core.x, core.y, core.block.size * Vars.tilesize / 1.5f, draw_rot);
        }
        Draw.color(colorStorage);
        getStorageBuildings();
        storageBuildings.each(b -> Lines.square(b.x, b.y, b.block.size * Vars.tilesize / 1.5f, draw_rot));
        Draw.reset();
    }

    public void update() {
        if (!enabled || AdminVars.interaction.interacting) return;
        selected.removeAll(b -> b.tile.build != b);
        if (Vars.state.rules.onlyDepositCore) return;
        if (!AdminVars.interaction.willInteract()) return;
        unit = Vars.player.unit();
        core = getClosestCore();
        getStorageBuildings();
        Target target = getBestTarget(selected);
        if ((target == null || target.amount < 5) && !fillOnlySelectedBuildings) {
            validCloseBuildings.clear();
            Vars.indexer.eachBlock(unit, Vars.itemTransferRange, b -> {
                return shouldFillBuilding(b, false);
            }, b -> {
                validCloseBuildings.add(b);
            });
            target = getBestTarget(validCloseBuildings);
        }
        if (target == null) return;
        if (unit.stack.item == target.item && unit.stack.amount != 0) {
            if (unit.within(target.building, Vars.itemTransferRange)) {
                Call.transferInventory(Vars.player, target.building);
            }
        } else {
            if (core != null && unit.within(core, Vars.itemTransferRange)) {
                if (unit.stack.amount == 0) {
                    if (core.items.has(target.item, coreMinimumRequestAmount)) {
                        Call.requestItem(Vars.player, core, target.item, unit.maxAccepted(target.item));
                        return;
                    }
                } else if (core.acceptStack(unit.stack.item, unit.stack.amount, unit) == unit.stack.amount && !(coreIncinerates(core) && !unit.stack.item.buildable)) {
                    Call.transferInventory(Vars.player, core);
                    return;
                }
            }
            for (Building storage : storageBuildings) {
                if (unit.stack.amount == 0) {
                    if (storage.items.has(target.item, 1)) {
                        Call.requestItem(Vars.player, storage, target.item, unit.maxAccepted(target.item));
                        return;
                    }
                } else if (storage.acceptStack(unit.stack.item, unit.stack.amount, unit) == unit.stack.amount) {
                    Call.transferInventory(Vars.player, storage);
                    return;
                }
            }
            if (unit.stack.item != target.item && unit.stack.amount != 0) {
                Call.dropItem(0);
            }
        }
    }

    private boolean coreIncinerates(Building b) {
        if (b.block instanceof CoreBlock) return (((CoreBlock)b.block).incinerateNonBuildable);
        return false;
    }

    private boolean itemTransferPossible(Building b, Item i) {
        for (Consume c2 : b.block.consumers) {
            if (c2 instanceof ConsumeItemExplode) {
                if (((ConsumeItemExplode)c2).filter.get(i)) return false;
            }
        }
        if (core == null || !core.items.has(i, coreMinimumRequestAmount) || !unit.within(core, Vars.itemTransferRange)) {
            if (unit.stack.item == i && unit.stack.amount > 0) return true;
            for (Building storage : storageBuildings) {
                if (storage.items.has(i, 1)) return true;
            }
            return false;
        }
        return true;
    }

    private Item getItemTurretItem(Building b) {
        Item bestItem = null;
        float bestDamage = 0;
        for (ObjectMap.Entry<Item, BulletType> i : ((ItemTurret)b.block).ammoTypes) {
            if (!itemTransferPossible(b, i.key)) continue;
            float damage = i.value.damage + i.value.splashDamage;
            if (i.value.fragBullet != null) {
                damage += i.value.fragBullet.damage * (i.value.fragBullets > 1 ? 2 : 1);
            }
            if (i.value.lightning > 0) {
                damage += i.value.lightning * i.value.lightningDamage;
            }
            damage *= i.value.reloadMultiplier;
            if (damage > bestDamage) {
                bestItem = i.key;
                bestDamage = damage;
            }
        }
        return bestItem;
    }

    private Item getItem(Building b) {
        Seq<Item> items = new Seq<>();
        for (Consume c : b.block.consumers) {
            if (c instanceof ConsumeItems) {
                for (ItemStack i : ((ConsumeItems)c).items) {
                    items.add(i.item);
                }
            } else if (c instanceof ConsumeItemDynamic) {
                for (ItemStack i : ((ConsumeItemDynamic)c).items.get(b)) {
                    items.add(i.item);
                }
            } else if (c instanceof ConsumeItemFilter) {
                for (Item i : Vars.content.items()) {
                    if (((ConsumeItemFilter)c).filter.get(i)) {
                        items.add(i);
                    }
                }
            }
        }
        int biggestFill = 0;
        Item biggestItem = null;
        for (Item item : items) {
            if (!itemTransferPossible(b, item)) continue;
            int thisFill = b.acceptStack(item, Integer.MAX_VALUE, unit);
            if (thisFill > biggestFill) {
                biggestFill = thisFill;
                biggestItem = item;
            }
        }
        return biggestItem;
    }

    private Item getNeededItem(Building b) {
        if (!unit.within(b, Vars.itemTransferRange)) return null;
        Item item = null;
        if (b.block instanceof ItemTurret) {
            item = getItemTurretItem(b);
        } else {
            item = getItem(b);
        }
        if (item == null) return null;
        return item;
    }

    private Target getBestTarget(Seq<Building> buildings) {
        Item item = null;
        Building building = null;
        int biggestFill = 0;
        boolean isBiggestFillInUnit = false;
        for (Building b : buildings) {
            Item thisItem = getNeededItem(b);
            if (thisItem != null) {
                int thisFill = b.acceptStack(thisItem, Integer.MAX_VALUE, unit);
                here: if (!isBiggestFillInUnit && thisItem == unit.stack.item && unit.stack.amount > 0) {
                    if (b.acceptStack(thisItem, Integer.MAX_VALUE, unit) < 5) break here;
                    item = thisItem;
                    biggestFill = 0;
                    isBiggestFillInUnit = true;
                }
                if (isBiggestFillInUnit && thisItem != item) continue;
                if ((thisFill > biggestFill)) {
                    item = thisItem;
                    biggestFill = thisFill;
                    building = b;
                }
            }
        }
        if (biggestFill < 1) return null;
        return new Target(building, item, biggestFill);
    }

    private Building getClosestCore() {
        coreBuildings.clear();
        Vars.indexer.eachBlock(unit, Vars.itemTransferRange, b -> {
            return b.block instanceof CoreBlock || b.block instanceof StorageBlock && ((StorageBlock.StorageBuild)b).linkedCore != null;
        }, b -> {
            coreBuildings.add(b);
        });
        if (coreBuildings.contains(b -> !coreIncinerates(b))) {
            coreBuildings.retainAll(b -> !coreIncinerates(b));
        }
        return coreBuildings.min(b -> b.dst(unit));
    }

    private void getStorageBuildings() {
        storageBuildings.clear();
        Vars.indexer.eachBlock(unit, Vars.itemTransferRange, b -> {
            if (b.block instanceof CoreBlock) return false;
            return b.block instanceof StorageBlock && ((StorageBlock.StorageBuild)b).linkedCore == null;
        }, b -> {
            storageBuildings.add(b);
        });
    }

    private class Target {
        Building building;
        Item item;
        int amount;
        Target(Building building, Item item, int amount) {
            this.building = building;
            this.item = item;
            this.amount = amount;
        }
    }
}
