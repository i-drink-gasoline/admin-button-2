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
import mindustry.gen.Unit;
import mindustry.type.Item;
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
    private Unit previousUnit;
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
        unit.aim(Core.input.mouseWorld());
        Vars.player.mouseX = unit.aimX();
        Vars.player.mouseY = unit.aimY();
        if (unit.type.canBoost) Vars.player.boosting = true;
        boolean boosted = (unit instanceof mindustry.gen.Mechc && unit.isFlying());
        if (!unit.plans.isEmpty() && Vars.control.input.isBuilding) {
            approach(unit.plans.first(), unit.type.buildRange / 1.5f);
        } else if (Vars.player.shooting) {
            approach(Core.input.mouseWorld(), unit.range());
            unit.controlWeapons(true, Vars.player.shooting && !boosted);
        } else if (unit.canMine() && mine) {
            Building core = unit.closestCore();
            if (core == null) return;
            if (unit != previousUnit) {
                previousUnit = unit;
                reloadDrillables();
                interval.getTimes()[findInterval] = 0;
            }
            if (interval.get(findInterval, 10 * 60)) {
                Block targetBlock = drillables.min(i -> AdminVars.oreIndexer.hasOre(i.id) && Core.settings.getBool(mineSettingName(i.name), true) && AdminVars.oreIndexer.findClosestOre(0, 0, i.id) != null, i -> core.items.get(i.itemDrop));
                if (targetBlock == null) {
                    oreTile = null;
                    return;
                }
                targetItem = targetBlock.itemDrop;
                int sameDrop = drillables.count(i -> i.itemDrop == targetItem);
                if (sameDrop > 1) targetBlock = drillables.min(i -> i.itemDrop == targetItem, i -> AdminVars.oreIndexer.findClosestOreDistance(unit.x / Vars.tilesize, unit.y / Vars.tilesize, i.id));
                oreTile = AdminVars.oreIndexer.findClosestOre(unit.x / Vars.tilesize, unit.y / Vars.tilesize, targetBlock.id);
            }
            if ((targetItem == null || oreTile == null) || unit.getMineResult(oreTile) == null) {
                interval.getTimes()[findInterval] = 0;
                return;
            }
            here: if ((unit.stack.item != targetItem || unit.stack.amount >= unit.type.itemCapacity) && unit.stack.amount > 0) {
                if (!unit.within(core, Vars.itemTransferRange)) {
                    approach(core, Vars.itemTransferRange - 40);
                } else {
                    if (unit.stack.item != targetItem && oreTile.within(core, Vars.itemTransferRange + unit.type.mineRange - 1)) break here;
                    if (core.acceptStack(unit.stack.item, unit.stack.amount, unit) > 0) {
                        if (!interval.get(dropInterval, 60)) return;
                        Call.transferInventory(Vars.player, core);
                    } else {
                        Call.dropItem(0);
                    }
                }
                return;
            }
            approach(oreTile, unit.type.mineRange - 1);
            if (unit.within(oreTile, unit.type.mineRange)) {
                unit.mineTile = oreTile;
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
