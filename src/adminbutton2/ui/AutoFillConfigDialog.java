// SPDX-License-Identifier: GPL-3.0
package adminbutton2.ui;

import arc.Core;
import arc.scene.event.Touchable;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.ImageButton;
import arc.scene.ui.Label;
import arc.scene.ui.Slider;
import arc.struct.Seq;
import arc.util.Strings;
import arc.util.Timer;
import mindustry.Vars;
import mindustry.type.Category;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;
import mindustry.world.Block;

import adminbutton2.AdminVars;

public class AutoFillConfigDialog extends BaseDialog {
    public AutoFillConfigDialog() {
        super("@adminbutton2.autofill_config.title");
        addCloseButton();
        cont.table(t -> {
            t.defaults().left();
            t.check("@adminbutton2.autofill.enabled", b -> AdminVars.autofill.enabled = !AdminVars.autofill.enabled).checked(b -> AdminVars.autofill.enabled).row();
            t.check("@adminbutton2.autofill.select_buildings", b -> AdminVars.autofill.selectBuildings = !AdminVars.autofill.selectBuildings).checked(b -> AdminVars.autofill.selectBuildings).row();
            t.check("@adminbutton2.autofill.fill_only_selected_buildings", b -> AdminVars.autofill.fillOnlySelectedBuildings = !AdminVars.autofill.fillOnlySelectedBuildings).checked(b -> AdminVars.autofill.fillOnlySelectedBuildings).row();
            t.pane(t2 -> {
                for (Category c : Category.all) {
                    Seq<Block> blocks = getByCategory(c);
                    if (blocks.isEmpty()) continue;
                    t2.table(t3 -> {
                        t3.image().height(3f).growX();
                        t3.image(Vars.ui.getIcon(c.name())).size(42);
                        t3.image().height(3f).growX();
                    }).growX().row();
                    t2.table(t3 -> {
                        for (int i = 0; i < blocks.size; i++) {
                            Block block = blocks.get(i);
                            if (i % 4 == 0) t3.row();
                            ImageButton button = t3.button(new TextureRegionDrawable(block.uiIcon), Styles.selecti, () -> {
                                boolean bool = AdminVars.autofill.fillMap[block.id];
                                Core.settings.put("adminbutton2.autofill.fill." + block.name, !bool);
                                AdminVars.autofill.fillMap[block.id] = !bool;
                            }).size(Vars.iconMed + 14f).pad(2f)
                            .checked(b -> AdminVars.autofill.fillMap[block.id]).get();
                            button.resizeImage(Vars.iconMed);
                        }
                    }).left();
                    t2.row();
                }
            }).growX();
        });
    }

    private Seq<Block> getByCategory(Category c) {
        Seq<Block> blocks = new Seq<>();
        return blocks.selectFrom(Vars.content.blocks(), b -> b.category == c && AdminVars.autofill.validBlock(b));
    }
}
