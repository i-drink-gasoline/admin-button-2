// SPDX-License-Identifier: GPL-3.0
package adminbutton2.ui;

import arc.Core;
import arc.Events;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.ScrollPane;
import arc.scene.ui.TextField;
import arc.scene.ui.layout.Table;
import arc.util.Strings;
import mindustry.Vars;
import mindustry.content.StatusEffects;
import mindustry.game.EventType;
import mindustry.game.SpawnGroup;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.ui.Fonts;
import mindustry.ui.dialogs.BaseDialog;

public class WavesDialog extends BaseDialog {
    private Table wavesTable = new Table();
    private int page = 1;
    private int entriesPerPage = 50;
    private TextField pageField = new TextField(String.valueOf(page));

    public WavesDialog() {
        super("@rules.waves");
        addCloseButton();
        wavesTable.defaults().left();
        Table outerTable = new Table();
        outerTable.add(wavesTable);
        cont.pane(outerTable).growX().row();
        Events.run(EventType.WorldLoadEvent.class, () -> displayWaves(page));
        Events.run(EventType.CoreChangeEvent.class, () -> displayWaves(page));
        displayWaves(page);
        cont.table(t -> {
            t.button(Icon.left, () -> setPage(page - 1));
            t.add(pageField);
            t.button(Icon.right, () -> setPage(page + 1));
            pageField.changed(() -> {
                setPage(Strings.parseInt(pageField.getText(), 1));
            });
            pageField.update(() -> {
                if (!pageField.hasKeyboard()) pageField.setText(String.valueOf(page));
            });
        });
    }

    private void setPage(int newPage) {
        if (newPage < 1) newPage = 1;
        page = newPage;
        displayWaves(page);
    }

    private void displayWaves(int page) {
        displayWaves((page - 1) * entriesPerPage + 1, (page - 1) * entriesPerPage + entriesPerPage);
    }

    private void displayWaves(int first, int last) {
        wavesTable.clearChildren();
        for (int wave = first; wave <= last; wave++) {
            displayWave(wave);
        }
    }

    public void displayWave(int wave) {
        Table table = wavesTable.table(Tex.button).growX().get();
        table.defaults().left();
        wavesTable.row();
        String waveNumberString = Core.bundle.format("wave", wave);
        table.add(waveNumberString);
        String waveString = "";
        Table waveRepr = new Table();
        int totalSpawned = 0;
        boolean firstValidGroup = true;
        for (SpawnGroup group : Vars.state.rules.spawns) {
            int spawned = group.getSpawned(wave - 1);
            if (spawned == 0) continue;
            int spawns;
            if (group.spawn != -1) {
                spawns = 1;
            } else if (group.type.flying) {
                spawns = Vars.spawner.countFlyerSpawns();
            } else {
                spawns = Vars.spawner.countGroundSpawns();
            }
            int unitsSpawned = spawned * spawns;
            if (unitsSpawned == 0) continue;
            if (firstValidGroup) {
                firstValidGroup = false;
            } else {
                waveString += ' ';
                waveRepr.add().width(Vars.iconMed / 4);
            }
            totalSpawned += unitsSpawned;
            waveString += unitsSpawned;
            waveRepr.add("" + unitsSpawned);
            if (group.effect != null && group.effect != StatusEffects.none) {
                String effect = Fonts.hasUnicodeStr(group.effect.name) ? Fonts.getUnicodeStr(group.effect.name) : ' ' + group.effect.localizedName;
                waveString += effect;
                waveRepr.image(new TextureRegionDrawable(group.effect.uiIcon)).size(Vars.iconMed);
            }
            String unit = Fonts.hasUnicodeStr(group.type.name) ? Fonts.getUnicodeStr(group.type.name) : ' ' + group.type.localizedName;
            waveString += unit;
            waveRepr.image(new TextureRegionDrawable(group.type.uiIcon)).size(Vars.iconMed);
            if (group.items != null) {
                String items = Fonts.hasUnicodeStr(group.items.item.name) ? Fonts.getUnicodeStr(group.items.item.name) : ' ' + group.items.item.localizedName + '(' + group.items.amount + ')';
                waveString += items;
                waveRepr.add("(" + group.items.amount + ')');
                waveRepr.image(new TextureRegionDrawable(group.items.item.uiIcon)).size(Vars.iconMed);
            }
        }
        if (totalSpawned == 0) {
            waveString += Core.bundle.get("empty");
            waveRepr.add(waveString);
        }
        String string = waveNumberString + "[] " + waveString;
        table.add().width(-1).growX();
        table.button(Icon.copySmall, () -> Core.app.setClipboardText(string)).row();
        table.add(waveRepr);
    }
}
