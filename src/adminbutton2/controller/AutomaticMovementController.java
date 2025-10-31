// SPDX-License-Identifier: GPL-3.0
package adminbutton2.controller;

import arc.Core;
import arc.scene.ui.Button;
import arc.scene.ui.Image;
import arc.scene.ui.layout.Table;
import arc.util.Interval;
import arc.util.Time;
import mindustry.Vars;
import mindustry.gen.Building;
import mindustry.gen.Call;
import mindustry.gen.Unit;
import mindustry.input.MobileInput;
import mindustry.type.Item;
import mindustry.ui.Styles;
import mindustry.world.Tile;

import adminbutton2.AdminVars;

public class AutomaticMovementController extends Controller {
    private Interval interval = new Interval(2);
    private int dropInterval = 0, findInterval = 1;
    private Tile ore;
    private Item targetItem;
    private Unit previousUnit;
    private boolean mine = true;

    public AutomaticMovementController() {
        super("automatic_movement");
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
        } else if (Vars.player.shooting && !(Vars.control.input instanceof MobileInput)) {
            approach(Core.input.mouseWorld(), unit.range());
            unit.controlWeapons(true, Vars.player.shooting && !boosted);
        } else if (unit.canMine() && mine) {
            Building core = unit.closestCore();
            if (core == null) return;
            if (unit != previousUnit) {
                previousUnit = unit;
                interval.getTimes()[findInterval] = 0;
            }
            if (interval.get(findInterval, 10 * 60)) {
                targetItem = Vars.content.items().min(i -> ((unit.type.mineFloor && Vars.indexer.hasOre(i)) || (unit.type.mineWalls && Vars.indexer.hasWallOre(i))) && unit.canMine(i) && Core.settings.getBool(mineSettingName(i.name), true), i -> core.items.get(i));
                if (targetItem == null) {
                arc.util.Log.info("targetItem is null");
                    return;
                }
                arc.util.Log.info("targetItem is  not null");
                if (unit.type.mineFloor) ore = Vars.indexer.findClosestOre(unit.x, unit.y, targetItem);
                if (ore == null && unit.type.mineWalls) Vars.indexer.findClosestWallOre(unit.x, unit.y, targetItem);
            }
            if (ore == null || unit.getMineResult(ore) == null) {
                arc.util.Log.info("ore is null");
                interval.getTimes()[findInterval] = Time.time + 20;
                return;
            }
            here: if ((unit.stack.item != targetItem || unit.stack.amount >= unit.type.itemCapacity) && unit.stack.amount > 0) {
                if (!unit.within(core, Vars.itemTransferRange)) {
                    approach(core, Vars.itemTransferRange - 40);
                } else {
                    if (unit.stack.item != targetItem && ore.within(core, Vars.itemTransferRange + unit.type.mineRange - 1)) break here;
                    if (core.acceptStack(unit.stack.item, unit.stack.amount, unit) > 0) {
                        if (!interval.get(dropInterval, 60)) return;
                        Call.transferInventory(Vars.player, core);
                    } else {
                        Call.dropItem(0);
                    }
                }
                return;
            }
            approach(ore, unit.type.mineRange - 1);
            if (unit.within(ore, unit.type.mineRange)) {
                unit.mineTile = ore;
            }
        }
    }

    @Override
    protected void buildTable() {
        table.check("@" + name() + ".mine", !mine, t -> {
            mine = t;
            previousUnit = null;
        }).row();
        table.table(t -> {
            int buttons = 0;
            for (Item i : Vars.content.items()) {
                String settingName = mineSettingName(i.name);
                Button button = new Button(Styles.togglet);
                buttons += 1;
                button.add(new Image(i.uiIcon)).row();
                button.add(i.localizedName).padTop(10);
                button.setChecked(Core.settings.getBool(settingName, true));
                button.clicked(() -> Core.settings.put(settingName, button.isChecked()));
                t.add(button).width(220).height(100);
                if (buttons % 2 == 0 && buttons != 0) t.row();
            }
        });
    }
}
