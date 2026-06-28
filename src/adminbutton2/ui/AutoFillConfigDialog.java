// SPDX-License-Identifier: GPL-3.0
package adminbutton2.ui;

import arc.Core;
import arc.Events;
import arc.graphics.Color;
import arc.scene.event.Touchable;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.Image;
import arc.scene.ui.ImageButton;
import arc.scene.ui.Label;
import arc.scene.ui.Slider;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Strings;
import arc.util.Timer;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.gen.Icon;
import mindustry.type.Category;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;
import mindustry.world.Block;
import mindustry.world.blocks.payloads.BlockProducer;
import mindustry.world.blocks.units.UnitAssembler;
import mindustry.world.blocks.units.UnitFactory;
import mindustry.world.consumers.Consume;
import mindustry.world.consumers.ConsumeItemDynamic;
import mindustry.world.consumers.ConsumeItemExplode;
import mindustry.world.consumers.ConsumeItemFilter;
import mindustry.world.consumers.ConsumeItems;

import adminbutton2.AdminVars;

public class AutoFillConfigDialog extends BaseDialog {
    Seq<Block> validBlocks = new Seq<>();
    Table blocksTable = new Table();
    Table itemsTable = new Table();
    BaseDialog itemsDialog = new BaseDialog("");
    Block selectedBlock;
    Image selectedBlockImage;
    Seq<Item> selectedBlockItems = new Seq<>();

    public AutoFillConfigDialog() {
        super("@adminbutton2.autofill_config.title");
        addCloseButton();
        Events.run(EventType.WorldLoadEvent.class, () -> {
            rebuildBlocksTable();
            rebuildItemsTable();
        });
        rebuildBlocksTable();
        rebuildItemsTable();
        cont.table(t -> {
            t.defaults().left();
            t.check("@adminbutton2.autofill.enabled", b -> AdminVars.autofill.enabled = !AdminVars.autofill.enabled).checked(b -> AdminVars.autofill.enabled).row();
            t.check("@adminbutton2.autofill.select_buildings", b -> AdminVars.autofill.selectBuildings = !AdminVars.autofill.selectBuildings).checked(b -> AdminVars.autofill.selectBuildings).row();
            t.check("@adminbutton2.autofill.fill_only_selected_buildings", b -> AdminVars.autofill.fillOnlySelectedBuildings = !AdminVars.autofill.fillOnlySelectedBuildings).checked(b -> AdminVars.autofill.fillOnlySelectedBuildings).row();
            t.pane(blocksTable).grow();
        });
        itemsDialog.addCloseButton();
        selectedBlockImage = itemsDialog.cont.image().size((Vars.iconMed + 14f) * 4).get();
        itemsDialog.cont.row();
        itemsDialog.cont.add(itemsTable).row();
        itemsDialog.cont.table(t -> {
            t.button(Icon.left, () -> {
                int index = validBlocks.indexOf(selectedBlock) - 1;
                if (index < 0) index = validBlocks.size - 1;
                showItemDialog(validBlocks.get(index));
            });
            t.button(Icon.right, () -> {
                int index = validBlocks.indexOf(selectedBlock) + 1;
                if (index >= validBlocks.size) index = 0;
                showItemDialog(validBlocks.get(index));
            });
        });
    }

    private Seq<Block> getByCategory(Category c) {
        Seq<Block> blocks = new Seq<>();
        return blocks.selectFrom(Vars.content.blocks(), b -> b.category == c && AdminVars.autofill.validBlock(b));
    }

    private void showItemDialog(Block block) {
        selectedBlock = block;
        getItemsConsumed();
        selectedBlockImage.setDrawable(block.uiIcon);
        itemsDialog.title.setText(block.localizedName);
        if (!itemsDialog.isShown()) itemsDialog.show();
    }

    private void rebuildBlocksTable() {
        blocksTable.clear();
        for (Category c : Category.all) {
            Seq<Block> blocks = getByCategory(c);
            validBlocks.add(blocks);
            if (blocks.isEmpty()) continue;
            blocksTable.table(t -> {
                t.image().height(3f).growX();
                t.image(Vars.ui.getIcon(c.name())).size(42);
                t.image().height(3f).growX();
            }).growX().row();
            blocksTable.table(t -> {
                for (int i = 0; i < blocks.size; i++) {
                    Block block = blocks.get(i);
                    if (i % 4 == 0) t.row();
                    t.button(new TextureRegionDrawable(block.uiIcon), Styles.selecti, () -> {
                        boolean bool = AdminVars.autofill.fillMap[block.id];
                        Core.settings.put("adminbutton2.autofill.fill." + block.name, !bool);
                        AdminVars.autofill.fillMap[block.id] = !bool;
                    }).size(Vars.iconMed + 14f).pad(2f)
                    .checked(b -> AdminVars.autofill.fillMap[block.id]).get().resizeImage(Vars.iconMed);
                    t.button(Icon.settings, Styles.cleari, () -> {
                        showItemDialog(block);
                    });
                }
            }).left();
            blocksTable.row();
        }
    }

    private void rebuildItemsTable() {
        itemsTable.clear();
        Seq<Item> items = Vars.content.items();
        for (int j = 0; j < items.size; j++) {
            Item item = items.get(j);
            if (j % 4 == 0) itemsTable.row();
            ImageButton button = new ImageButton(new TextureRegionDrawable(item.uiIcon), Styles.selecti);
            Runnable runnable = () -> {
                if (!selectedBlockItems.contains(item)) return;
                boolean bool = AdminVars.autofill.fillWithMap[selectedBlock.id][item.id];
                arc.util.Log.info("adminbutton2.autofill.fill." + selectedBlock.name + ".with." + item.name);
                Core.settings.put("adminbutton2.autofill.fill." + selectedBlock.name + ".with." + item.name, !bool);
                AdminVars.autofill.fillWithMap[selectedBlock.id][item.id] = !bool;
            };
            button.resizeImage(Vars.iconMed);
            button.clicked(runnable);
            Image cross = new Image(Icon.cancel);
            cross.touchable = Touchable.disabled;
            cross.setColor(Color.scarlet);
            itemsTable.stack(button, cross).size(Vars.iconMed + 14f).pad(2f);
            button.update(() -> {
                boolean touchable = selectedBlockItems.contains(item);
                boolean enabled = touchable && AdminVars.autofill.fillWithMap[selectedBlock.id][item.id];
                button.setChecked(enabled);
                button.touchable = touchable ? Touchable.enabled : Touchable.disabled;
                button.getImage().color.set(touchable ? Color.white : Color.gray);
                cross.visible = !touchable;
            });
        }
    }

    private void getItemsConsumed() {
        Block block = selectedBlock;
        selectedBlockItems.clear();
        for (Consume c : block.consumers) {
            if (c instanceof ConsumeItems) {
                for (ItemStack i : ((ConsumeItems)c).items) {
                    selectedBlockItems.add(i.item);
                }
            } else if (c instanceof ConsumeItemDynamic) {
                if (block instanceof UnitFactory) {
                    for (UnitFactory.UnitPlan plan : ((UnitFactory)block).plans) {
                        for (ItemStack i : plan.requirements) {
                            selectedBlockItems.add(i.item);
                        }
                    }
                } else if (block instanceof UnitAssembler) {
                    for (UnitAssembler.AssemblerUnitPlan plan : ((UnitAssembler)block).plans) {
                        if (plan.itemReq == null) continue;
                        for (ItemStack i : plan.itemReq) {
                            selectedBlockItems.add(i.item);
                        }
                    }
                } else if (block instanceof BlockProducer) {
                    for (Block b : Vars.content.blocks()) {
                        for (ItemStack i : b.requirements) {
                            selectedBlockItems.addUnique(i.item);
                        }
                    }
                }
            } else if (c instanceof ConsumeItemFilter) {
                loop: for (Item i : Vars.content.items()) {
                    if (((ConsumeItemFilter)c).filter.get(i)) {
                        for (Consume c2 : block.consumers) {
                            if (c2 instanceof ConsumeItemExplode) {
                                if (((ConsumeItemExplode)c2).filter.get(i)) continue loop;
                            }
                        }
                        selectedBlockItems.add(i);
                    }
                }
            }
        }
    }
}
