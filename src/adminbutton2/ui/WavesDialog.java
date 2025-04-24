// SPDX-License-Identifier: GPL-3.0
package adminbutton2.ui;

import arc.Core;
import arc.Events;
import arc.scene.ui.ScrollPane;
import arc.scene.ui.TextField;
import arc.scene.ui.layout.Table;
import mindustry.Vars;
import mindustry.content.StatusEffects;
import mindustry.game.EventType;
import mindustry.game.SpawnGroup;
import mindustry.gen.Icon;
import mindustry.ui.Fonts;
import mindustry.ui.dialogs.BaseDialog;

public class WavesDialog extends BaseDialog {
    private Table wavesTable = new Table();
    private int page = 0;
    private int entriesPerPage = 50;
    private TextField pageField = new TextField(String.valueOf(page));
    public WavesDialog() {
        super("@rules.waves");
        addCloseButton();
        wavesTable.defaults().left();
        cont.pane(wavesTable).growX().row();
        Events.run(EventType.WorldLoadEvent.class, () -> displayWaves(page));
        Events.run(EventType.CoreChangeEvent.class, () -> displayWaves(page));
        cont.table(t -> {
            t.button(Icon.left, () -> setPage(page - 1));
            t.button(Icon.right, () -> setPage(page + 1));
            t.add(pageField);
            pageField.changed(() -> {
                try {
                    page = Integer.parseInt(pageField.getText());
                    setPage(page);
                } catch (NumberFormatException e) {}
            });
        });
    }

    private void setPage(int newPage) {
        page = newPage;
        pageField.setText(String.valueOf(page));
        displayWaves(page);
    }

    private void displayWaves(int page) {
        displayWaves(page * entriesPerPage + 1, page * entriesPerPage + entriesPerPage);
    }

    private void displayWaves(int first, int last) {
        if (!Vars.state.rules.waves) return;
        wavesTable.clearChildren();
        StringBuilder sb = new StringBuilder();
        for (int wave = first; wave <= last; wave++) {
            sb.append(Core.bundle.format("wave", wave));
            sb.append("[]");
            int totalSpawned = 0;
            for (SpawnGroup group : Vars.state.rules.spawns) {
                int spawned = group.getSpawned(wave - 1);
                if (spawned == 0) continue;
                int spawns = 0;
                if (group.spawn != -1) {
                    spawns = 1;
                } else if (group.type.flying) {
                    spawns = Vars.spawner.countFlyerSpawns();
                } else {
                    spawns = Vars.spawner.countGroundSpawns();
                }
                int unitsSpawned = spawned * spawns;
                totalSpawned += unitsSpawned;
                if (unitsSpawned != 0) {
                    sb.append(' ');
                    sb.append(unitsSpawned);
                    if (group.effect != null && group.effect != StatusEffects.none) {
                        String effect = Fonts.hasUnicodeStr(group.effect.name) ? Fonts.getUnicodeStr(group.effect.name) : ' ' + group.effect.localizedName;
                        sb.append(effect);
                    }
                    String unit = Fonts.hasUnicodeStr(group.type.name) ? Fonts.getUnicodeStr(group.type.name) : ' ' + group.type.localizedName;
                    sb.append(unit);
                    if (group.items != null) {
                        String items = Fonts.hasUnicodeStr(group.items.item.name) ? Fonts.getUnicodeStr(group.items.item.name) : ' ' + group.items.item.localizedName;
                        sb.append(items);
                        sb.append('(');
                        sb.append(group.items.amount);
                        sb.append(')');
                    }
                }
            }
            if (totalSpawned == 0) {
                sb.append(' ');
                sb.append(Core.bundle.get("empty"));
            }
            String string = sb.toString();
            wavesTable.button(Icon.copy, () -> Core.app.setClipboardText(string));
            wavesTable.add(string).padLeft(8).row();
            sb.delete(0, sb.length());
        }
    }
}
