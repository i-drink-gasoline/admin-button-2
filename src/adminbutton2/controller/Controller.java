// SPDX-License-Identifier: GPL-3.0
package adminbutton2.controller;

import arc.math.geom.Position;
import arc.math.geom.Vec2;
import arc.scene.ui.layout.Table;
import mindustry.Vars;
import mindustry.gen.Unit;

public class Controller {
    public static Vec2 vec = new Vec2();
    public Table table = new Table();
    public static Unit unit;
    private String name;

    public Controller() {
        this("unnamed");
    }

    public Controller(String name) {
        this.name = "adminbutton2.controller." + name;
        buildTable();
    }

    public void controlPlayer() {
    }

    protected void buildTable() {
    }

    public String name() {
        return name;
    }

    public void control() {
        if (Vars.player.unit() != null) {
            unit = Vars.player.unit();
            controlPlayer();
        }
    }

    public void approach(Position target, float radius) {
        vec.set(target).sub(unit);
        unit.lookAt(target);
        if (vec.len() > radius) {
            vec.nor().setLength(unit.type.speed);
            unit.movePref(vec);
        } else {
            unit.vel.setZero();
        }
    }
}
