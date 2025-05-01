// SPDX-License-Identifier: GPL-3.0
package adminbutton2.controller;

import arc.Core;
import arc.scene.ui.Button;
import arc.scene.ui.Image;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Interval;
import mindustry.Vars;
import mindustry.gen.Building;
import mindustry.gen.Call;
import mindustry.type.Item;
import mindustry.type.UnitType;
import mindustry.ui.Styles;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.blocks.environment.Floor;

import adminbutton2.AdminVars;

public class AutomaticMovementController extends Controller {
    private Seq<Block> drillables = new Seq<Block>();
    private Interval interval = new Interval(2);
    private int dropInterval = 0, findInterval = 1;
    private Tile oreTile;
    private Item targetItem;
    private UnitType previousUnit;
    private boolean mine = true;
    private Table mineButtons;
    private int buttons = 0;

    public AutomaticMovementController() {
        super("automatic_movement");
    }

    public void reloadDrillables() {
        drillables.clear();
        for (Block b : Vars.content.blocks()) {
            if (b.itemDrop != null && unit.type.mineTier >= b.itemDrop.hardness) {
                if (b instanceof Floor) {
                    Floor f = (Floor) b;
                    if (!((unit.type.mineFloor && !f.wallOre) || (unit.type.mineWalls && f.wallOre))) continue;
                } else if (!(unit.type.mineWalls)) continue;
                drillables.add(b);
            }
        }
        mineButtons.clear();
        buttons = 0;
        drillables.each(d -> {
            String settingName = mineSettingName(d.name);
            Button button = new Button(Styles.togglet);
            buttons += 1;
            button.add(new Image(d.uiIcon)).row();
            button.add(d.localizedName).padTop(10);
            button.setChecked(Core.settings.getBool(settingName, true));
            button.clicked(() -> Core.settings.put(settingName, button.isChecked()));
            mineButtons.add(button).width(220).height(100);
            if (buttons % 2 == 0 && buttons != 0) mineButtons.row();
        });
    }

    private String mineSettingName(String name) {
        return name() + ".mine." + name;
    }

    @Override
    public void controlPlayer() {
        Building core = unit.closestCore();
        if (core == null) return;
        if (unit.type.canBoost) Vars.player.boosting = true;
        if (!unit.plans.isEmpty() && Vars.control.input.isBuilding) {
            approach(unit.plans.first(), unit.type.buildRange / 1.5f);
        } else if (unit.canMine() && mine) {
            if (unit.type != previousUnit) {
                previousUnit = unit.type;
                reloadDrillables();
                interval.getTimes()[findInterval] = 0;
            }
            if (interval.get(findInterval, 10 * 60)) {
                Block targetBlock = drillables.min(i -> AdminVars.oreIndexer.hasOre(i.id) && Core.settings.getBool(mineSettingName(i.name), true), i -> core.items.get(i.itemDrop));
                if (targetBlock == null) {
                    oreTile = null;
                    return;
                }
                targetItem = targetBlock.itemDrop;
                int sameDrop = drillables.count(i -> i.itemDrop == targetItem);
                if (sameDrop > 1) targetBlock = drillables.min(i -> i.itemDrop == targetItem, i -> AdminVars.oreIndexer.findClosestOreDistance(unit.x / Vars.tilesize, unit.y / Vars.tilesize, i.id));
                oreTile = AdminVars.oreIndexer.findClosestOre(unit.x / Vars.tilesize, unit.y / Vars.tilesize, targetBlock.id);
            }
            if (targetItem == null) return;
            if ((unit.stack.item != targetItem || unit.stack.amount >= unit.type.itemCapacity) && unit.stack.amount > 0) {
                approach(core, unit.type.range / 2);
                if (unit.within(core, unit.type.range / 2)) {
                    if (!interval.get(dropInterval, 60)) return;
                    if (core.acceptStack(unit.stack.item, unit.stack.amount, unit) > 0) {
                        Call.transferInventory(Vars.player, core);
                    } else {
                        Call.dropItem(0);
                    }
                }
            } else {
                if (oreTile != null) {
                    approach(oreTile, unit.type.mineRange);
                    if (unit.within(oreTile, unit.type.mineRange)) {
                        unit.mineTile = oreTile;
                    }
                }
            }
        }
    }

    @Override
    protected void buildTable() {
        table.check("@" + name() + ".mine", !mine, t -> {
            mine = t;
            previousUnit = null;
            mineButtons.clear();
        }).row();
        mineButtons = new Table();
        table.add(mineButtons);
    }
}
