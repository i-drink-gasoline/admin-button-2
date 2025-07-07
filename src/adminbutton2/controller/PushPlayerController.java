// SPDX-License-Identifier: GPL-3.0
package adminbutton2.controller;

import arc.math.geom.Vec2;
import arc.scene.ui.ButtonGroup;
import arc.scene.ui.TextButton;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Strings;
import mindustry.Vars;
import mindustry.gen.Groups;
import mindustry.gen.Icon;
import mindustry.gen.Player;
import mindustry.ui.Styles;

public class PushPlayerController extends Controller {
    private Table playersTable;
    private Player targetPlayer;
    private static final int push_to_coordinates = 1, push_from_behind = 2;
    private int push = push_to_coordinates;
    private Vec2 targetPosition = new Vec2(0f, 0f);

    public PushPlayerController() {
        super("push_player");
    }

    private void reloadPlayers() {
        playersTable.clearChildren();
        Seq<Player> players = Groups.player.copy();
        for (Player player : players) {
            playersTable.button(player.coloredName(), () -> targetPlayer = player).fillX().get().getLabel().setWrap(false);
            playersTable.row();
        }
    }

    @Override
    public void controlPlayer() {
        if (targetPlayer == null) return;
        if (targetPlayer.unit() == null) return;
        if (push == push_to_coordinates) {
            vec.set(targetPlayer).sub(targetPosition);
        } else if (push == push_from_behind) {
            vec.set(-1f, 0f).rotate(targetPlayer.unit().rotation);
        }
        vec.nor().scl(targetPlayer.unit().type.hitSize/2f).add(targetPlayer);
        if (!targetPlayer.within(targetPosition, 16f)) {
                moveTo(vec);
        }
    }

    @Override
    protected void buildTable() {
        playersTable = new Table();
        ButtonGroup<TextButton> group = new ButtonGroup<>();
        table.table(t -> {
            t.defaults().pad(5);
            t.button("@" + this.name() + ".push_to_coordinates", Styles.togglet, () -> push = push_to_coordinates)
                .group(group).update(b -> b.setChecked(push == push_to_coordinates)).get().getLabel().setWrap(false);
            t.button("@" + this.name() + ".push_from_behind", Styles.togglet, () -> push = push_from_behind)
                .group(group).update(b -> b.setChecked(push == push_from_behind)).get().getLabel().setWrap(false);
        }).row();
        table.table(t -> {
            t.button(Icon.refresh, () -> reloadPlayers());
            t.add("x:").padLeft(10f);
            t.field("0", a -> targetPosition.x = Strings.parseFloat(a, 0f) * (float) Vars.tilesize);
            t.add("y:");
            t.field("0", a -> targetPosition.y = Strings.parseFloat(a, 0f) * (float) Vars.tilesize).row();
        }).row();
        table.add(playersTable).marginTop(5);
    }
}
