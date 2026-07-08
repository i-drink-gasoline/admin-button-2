// SPDX-License-Identifier: GPL-3.0
package adminbutton2.ui;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.Pixmap;
import arc.graphics.Texture;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.math.geom.Point2;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.Label;
import arc.scene.ui.layout.Cell;
import arc.scene.ui.layout.Table;
import arc.struct.IntMap;
import arc.struct.IntSeq;
import arc.struct.Seq;
import arc.struct.StringMap;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.game.Schematic;
import mindustry.gen.Icon;
import mindustry.logic.LExecutor;
import mindustry.type.Item;
import mindustry.type.Liquid;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;
import mindustry.ui.FileChooser;
import mindustry.world.Block;
import mindustry.world.blocks.logic.CanvasBlock;
import mindustry.world.blocks.logic.LogicBlock;
import mindustry.world.blocks.logic.LogicDisplay;

import java.nio.ByteBuffer;

import adminbutton2.AdminVars;

public class ImageGeneratorDialog extends BaseDialog {
    private Pixmap imagePixmap;
    private TextureRegion region;
    private Cell imageTableCell;
    private ImageGenerator[] generators = {
        new LogicDisplayImageGenerator(),
        new CanvasImageGenerator(),
        new SorterImageGenerator(),
    };
    private ImageGenerator selectedGenerator = generators[0];
    private boolean saveSchematic = Core.settings.getBool("adminbutton2.imagegenerator.save_schematic", true);

    public ImageGeneratorDialog() {
        super("@adminbutton2.imagegenerator.title");
        addCloseButton();

        Pixmap tmpPixmap;
        if (AdminVars.loadedMod.root.child("icon.png").exists()) {
            tmpPixmap = new Pixmap(AdminVars.loadedMod.root.child("icon.png"));
        } else {
            tmpPixmap = new Pixmap(10, 10);
            tmpPixmap.fill(Color.red);
        }
        setPixmap(tmpPixmap);

        Table imageTable = new Table() {
            @Override
            public void draw() {
                if (region == null) return;
                Draw.color();
                float scale = Math.min(this.width / region.width, this.height / region.height);
                Draw.rect(region, this.x+(this.width/2), this.y+(this.height/2), region.width * scale, region.height * scale);
            }
        };
        imageTableCell = cont.add(imageTable).grow();
        Table rightTable = new Table();
        rightTable.defaults().padBottom(3f);
        cont.add(rightTable);

        rightTable.button("@adminbutton2.imagegenerator.selectimage", () -> {
            FileChooser.open("*").submit(file -> {
                Pixmap pixmap = null;
                try {
                    pixmap = new Pixmap(file);
                } catch (Exception e) {
                    Vars.ui.showException(e);
                }
                if (pixmap == null) return;
                setPixmap(pixmap);
            });
        }).width(200f).row();

        Label generatorButtonLabel = rightTable.button("", () -> {
            BaseDialog selectGeneratorDialog = new BaseDialog("@adminbutton2.imagegenerator.title");
            selectGeneratorDialog.addCloseButton();
            for (ImageGenerator generator : generators) {
                selectGeneratorDialog.cont.button(generator.reprString(), generator.reprIcon(), Vars.iconMed, () -> {
                    selectedGenerator = generator;
                    selectGeneratorDialog.hide();
                }).width(300f);
                selectGeneratorDialog.cont.button(Icon.settings, () -> {
                    selectedGenerator = generator;
                    selectGeneratorDialog.hide();
                    generator.configure();
                }).row();
            }
            selectGeneratorDialog.show();
        }).width(200f).get().getLabel();
        generatorButtonLabel.update(() -> generatorButtonLabel.setText(selectedGenerator.reprString()));
        rightTable.row();

        rightTable.button("@adminbutton2.imagegenerator.generate", () -> {
            Point2 size = selectedGenerator.getSchematicSize();
            if (saveSchematic && size.x > Vars.maxSchematicSize && size.y > Vars.maxSchematicSize) {
                Vars.ui.showConfirm("@adminbutton2.imagegenerator.toolarge", () -> {
                    generateImage(size);
                });
            } else generateImage(size);
        }).width(200f).row();

        rightTable.check("@save", saveSchematic, b -> {
            saveSchematic = b;
            Core.settings.put("adminbutton2.imagegenerator.save_schematic", b);
        });
    }

    private void generateImage(Point2 size) {
        Seq<Schematic.Stile> tiles = new Seq<>();
        selectedGenerator.generate(tiles);
        if (tiles.size == 0) return;
        StringMap tags = new StringMap();
        tags.put("name", "!!!Admin Button 2");
        tags.put("description", "Generated by mod \"Admin Button 2\"");
        Schematic schematic = new Schematic(tiles, tags, size.x, size.y);
        if (saveSchematic && size.x <= Vars.maxSchematicSize && size.y <= Vars.maxSchematicSize) Vars.schematics.add(schematic);
        AdminVars.image.hide();
        AdminVars.admin.hide();
        Vars.control.input.useSchematic(schematic);
    }

    private void setPixmap(Pixmap pixmap) {
        if (imagePixmap != null) imagePixmap.dispose();
        if (region != null) region.texture.dispose();
        region = new TextureRegion(new Texture(pixmap));
        imagePixmap = pixmap.flipY();
        pixmap.dispose();
    }

    private interface ImageGenerator {
        public String reprString();
        public TextureRegionDrawable reprIcon();
        public void configure();
        public Point2 getSchematicSize();
        public void generate(Seq<Schematic.Stile> tiles);
    }

    private class LogicDisplayImageGenerator implements ImageGenerator {
        BaseDialog config;
        LogicDisplay display;
        LogicBlock processor;

        public LogicDisplayImageGenerator() {
            config = new BaseDialog("@adminbutton2.imagegenerator.title");
            config.addCloseButton();

            Table displays = config.cont.table().get();
            Table processors = config.cont.table().get();

            for (Block b : Vars.content.blocks()) {
                if (b instanceof LogicDisplay) {
                    if (display == null) display = (LogicDisplay) b;
                    displays.button(b.localizedName, new TextureRegionDrawable(b.uiIcon), Styles.togglet, Vars.iconMed, () -> {
                        display = (LogicDisplay) b;
                    }).width(300f).checked(a -> display == b).row();
                } else if (b instanceof LogicBlock && b.size == 1) {
                    if (processor == null) processor = (LogicBlock) b;
                    processors.button(b.localizedName, new TextureRegionDrawable(b.uiIcon), Styles.togglet, Vars.iconMed, () -> {
                        processor = (LogicBlock) b;
                    }).width(300f).checked(a -> processor == b).row();
                }
            }
        }

        public String reprString() { return display.localizedName + " + " + processor.localizedName; };
        public TextureRegionDrawable reprIcon() { return new TextureRegionDrawable(display.uiIcon); };

        public void configure() {
            config.show();
        }

        public Point2 getSchematicSize() {
            return new Point2(display.size + 2, display.size + 2);
        }

        private int px, py;
        private String code = "";
        private int instructions, graphicsBuffer;
        private boolean colorSet;
        public void generate(Seq<Schematic.Stile> tiles) {
            IntMap<IntSeq> colorMap = new IntMap<>();
            code = "";
            instructions = 0; graphicsBuffer = 0;
            px = 0; py = 0;
            int size = display.displaySize;
            tiles.add(new Schematic.Stile(display, Mathf.ceil((float)display.size / 2), Mathf.ceil((float)display.size / 2), null, (byte)0));
            Seq<String> processors = new Seq<>();
            Pixmap scaledPixmap = new Pixmap(size, size);
            scaledPixmap.draw(imagePixmap, 0, 0, imagePixmap.width, imagePixmap.height, 0, 0, size, size, false, true);
            for (int x = 0; x < size; x++) {
                for (int y = 0; y < size; y++) {
                    int c = scaledPixmap.get(x, y);
                    if (!colorMap.containsKey(c)) {
                        colorMap.put(c, new IntSeq());
                    }
                    colorMap.get(c).add(x * display.displaySize + y);
                }
            }
            Color c = new Color();
            colorMap.keys().toSeq().each(key -> {
                colorSet = false;
                IntSeq positions = colorMap.get(key);
                while (!positions.isEmpty()) {
                    int pos = positions.pop();
                    c.set(key);
                    int x = pos / display.displaySize;
                    int y = pos % display.displaySize;
                    if (instructions + 3 > LExecutor.maxInstructions) {
                        code += String.format("drawflush %s1\n", LogicBlock.getLinkName(display));
                        colorSet = false;
                        processors.add(code);
                        code = "";
                        instructions = 0;
                        graphicsBuffer = 0;
                    }
                    if (graphicsBuffer + 2 > LExecutor.maxGraphicsBuffer) {
                        code += String.format("draw color %d %d %d\n", Mathf.round(c.r * 255), Mathf.round(c.g * 255), Mathf.round(c.b * 255));
                        code += String.format("drawflush %s1\n", LogicBlock.getLinkName(display));
                        colorSet = false;
                        instructions += 2;
                        graphicsBuffer = 0;
                    }
                    if (!colorSet) {
                        code += String.format("draw color %d %d %d\n", Mathf.round(c.r * 255), Mathf.round(c.g * 255), Mathf.round(c.b * 255));
                        instructions += 1;
                        graphicsBuffer += 1;
                        colorSet = true;
                    }
                    int height = 1, width = 1;
                    while (y - 1 > 0 && positions.contains(x * display.displaySize + y - 1)) {
                        height += 1;
                        y -= 1;
                        pos = x * display.displaySize + y;
                        positions.removeValue(pos);
                    }
                    whileloop: while (x - 1 > 0) {
                        for (int yPos = y; yPos < y + height; yPos++) {
                            if (!positions.contains((x - 1) * display.displaySize + yPos)) break whileloop;
                        }
                        width += 1;
                        x -= 1;
                        pos = x * display.displaySize + y;
                        positions.removeValue(pos);
                    }
                    code += String.format("draw rect %d %d %d %d\n", x, y, width, height);
                    instructions += 1;
                    graphicsBuffer += 1;
                }
            });
            if (graphicsBuffer != 0) {
                code += String.format("drawflush %s1\n", LogicBlock.getLinkName(display));
                processors.add(code);
            }
            putProcessors(tiles, processors);
            scaledPixmap.dispose();
        }

        private void putProcessors(Seq<Schematic.Stile> tiles, Seq<String> processors) {
            Point2 pos = new Point2(1, 0);
            Point2 dir = new Point2(1, 0);
            Point2 displayPos = new Point2(tiles.first().x, tiles.first().y);
            int lineLength = display.size + 1;
            int line = 0;
            int rotations = 0;
            for (String code : processors) {
                Seq<LogicBlock.LogicLink> links = new Seq<>();
                links.add(new LogicBlock.LogicLink(displayPos.x - pos.x, displayPos.y - pos.y, "", true));
                tiles.add(new Schematic.Stile(processor, pos.x, pos.y, LogicBlock.compress(code, links), (byte)0));
                line += 1;
                if (line == lineLength) {
                    dir.rotate(1);
                    line = 0;
                    if (rotations == 2) {
                        rotations = 0;
                        lineLength += 1;
                    }
                    rotations += 1;
                }
                pos.add(dir);
            }
        }
    }

    private class CanvasImageGenerator implements ImageGenerator {
        BaseDialog config;
        CanvasBlock canvas;

        public CanvasImageGenerator() {
            config = new BaseDialog("@adminbutton2.imagegenerator.title");
            config.addCloseButton();

            for (Block b : Vars.content.blocks()) {
                if (b instanceof CanvasBlock) {
                    if (canvas == null) canvas = (CanvasBlock) b;
                    config.cont.button(b.localizedName, new TextureRegionDrawable(b.uiIcon), Styles.togglet, Vars.iconMed, () -> {
                        canvas = (CanvasBlock) b;
                    }).width(300f).checked(a -> canvas == b).row();
                }
            }
        }

        public String reprString() { return canvas.localizedName; }

        public TextureRegionDrawable reprIcon() { return new TextureRegionDrawable(canvas.uiIcon); }

        public void configure() {
            config.show();
        }

        public Point2 getSchematicSize() {
            return new Point2(Mathf.ceil((float)imagePixmap.width / canvas.canvasSize) * canvas.size, Mathf.ceil((float)imagePixmap.height / canvas.canvasSize) * canvas.size);
        }

        public void generate(Seq<Schematic.Stile> tiles) {
            Point2 size = getSchematicSize();
            CanvasBlock.CanvasBuild build = (CanvasBlock.CanvasBuild) canvas.newBuilding();
            Pixmap correctedPixmap = new Pixmap(imagePixmap.width, imagePixmap.height);
            Color tmpColor = new Color();
            for (int x = 0; x < imagePixmap.width; x++) {
                for (int y = 0; y < imagePixmap.height; y++) {
                    Color c = new Color(imagePixmap.get(x, y));
                    int closestColor = 0;
                    int closestColorDifference = Integer.MAX_VALUE;
                    int r = Mathf.round(c.r * 255);
                    int g = Mathf.round(c.g * 255);
                    int b = Mathf.round(c.b * 255);
                    for (int thisColor : canvas.palette) {
                        tmpColor.set(thisColor);
                        int dr = Mathf.round(tmpColor.r * 255) - r;
                        int dg = Mathf.round(tmpColor.g * 255) - g;
                        int db = Mathf.round(tmpColor.b * 255) - b;
                        int difference = dr*dr + dg*dg + db*db;
                        if (difference < closestColorDifference) {
                            closestColor = thisColor;
                            closestColorDifference = difference;
                        }
                    }
                    correctedPixmap.set(x, y, closestColor);
                }
            }
            for (int x = 0; x < size.x / canvas.size; x++) {
                for (int y = 0; y < size.y / canvas.size; y++) {
                    Pixmap cropped = correctedPixmap.crop(x * canvas.canvasSize, y * canvas.canvasSize, canvas.canvasSize, canvas.canvasSize);
                    Pixmap croppedFlipped = cropped.flipY();
                    cropped.dispose();
                    byte[] bytes = build.packPixmap(croppedFlipped);
                    cropped.dispose();
                    tiles.add(new Schematic.Stile(canvas,
                                                  (short) canvas.size * x + (canvas.size - 1) / 2,
                                                  (short) canvas.size * y + (canvas.size - 1) / 2,
                                                  (Object) bytes, (byte) 0));
                }
            }
            correctedPixmap.dispose();
        }
    }

    private class SorterImageGenerator implements ImageGenerator {
        BaseDialog config = new BaseDialog("@adminbutton2.imagegenerator.title");
        Block block;
        float scale = 1;

        public SorterImageGenerator() {
            config = new BaseDialog("@adminbutton2.imagegenerator.title");
            config.addCloseButton();

            config.cont.slider(0.01f, 1f, 0.01f, scale, v -> scale = v).row();
            Label scaleLabel = config.cont.add("").get();
            scaleLabel.update(() -> {
                Point2 size = getSchematicSize();
                scaleLabel.setText(Core.bundle.format("adminbutton2.imagegenerator.size", size.x, size.y));
            });
            config.cont.row();
            for (Block b : Vars.content.blocks()) {
                if ((Blocks.sorter.subclass.isInstance(b) ||
                     Blocks.unloader.subclass.isInstance(b) ||
                     Blocks.itemSource.subclass.isInstance(b) ||
                     Blocks.ductRouter.subclass.isInstance(b) ||
                     Blocks.ductUnloader.subclass.isInstance(b) ||
                     Blocks.unitCargoUnloadPoint.subclass.isInstance(b) ||
                     Blocks.itemSource.subclass.isInstance(b) ||
                     Blocks.liquidSource.subclass.isInstance(b) ||
                     Blocks.illuminator.subclass.isInstance(b)
                    ) && b.size == 1) {
                    if (block == null) block = b;
                    config.cont.button(b.localizedName, new TextureRegionDrawable(b.uiIcon), Styles.togglet, Vars.iconMed, () -> {
                        block = b;
                    }).width(300f).checked(a -> block == b).row();
                }
            }
        }

        public String reprString() { return block.localizedName; }
        public TextureRegionDrawable reprIcon() { return new TextureRegionDrawable(block.uiIcon); }

        public void configure() {
            config.show();
        }

        public Point2 getSchematicSize() {
            int width = (int) (imagePixmap.width * scale);
            int height = (int) (imagePixmap.height * scale);
            return new Point2(width, height);
        }

        public void generate(Seq<Schematic.Stile> tiles) {
            Point2 size = getSchematicSize();
            Pixmap scaledPixmap = new Pixmap(size.x, size.y);
            scaledPixmap.draw(imagePixmap, 0, 0, imagePixmap.width, imagePixmap.height, 0, 0, size.x, size.y, false, true);
            if (Blocks.illuminator.subclass.isInstance(block)) {
                for (int x = 0; x < size.x; x++) {
                    for (int y = 0; y < size.y; y++) {
                        Color c = new Color(scaledPixmap.get(x, y));
                        tiles.add(new Schematic.Stile(block, (short) x, (short) y, (Object) c.rgba8888(), (byte) 0));
                    }
                }
            } else if (Blocks.liquidSource.subclass.isInstance(block)) {
                Seq<Liquid> liquids = Vars.content.liquids();
                for (int x = 0; x < size.x; x++) {
                    for (int y = 0; y < size.y; y++) {
                        Color c = new Color(scaledPixmap.get(x, y));
                        int r = Mathf.round(c.r * 255);
                        int g = Mathf.round(c.g * 255);
                        int b = Mathf.round(c.b * 255);
                        Liquid liquid = liquids.min(it -> {
                            int dr = Mathf.round(it.color.r * 255) - r;
                            int dg = Mathf.round(it.color.g * 255) - g;
                            int db = Mathf.round(it.color.b * 255) - b;
                            return dr*dr + dg*dg + db*db;
                        });
                        tiles.add(new Schematic.Stile(block, (short) x, (short) y, (Object) liquid, (byte) 0));
                    }
                }
            } else {
                Seq<Item> items = Vars.content.items();
                for (int x = 0; x < size.x; x++) {
                    for (int y = 0; y < size.y; y++) {
                        Color c = new Color(scaledPixmap.get(x, y));
                        int r = Mathf.round(c.r * 255);
                        int g = Mathf.round(c.g * 255);
                        int b = Mathf.round(c.b * 255);
                        Item item = items.min(it -> {
                            int dr = Mathf.round(it.color.r * 255) - r;
                            int dg = Mathf.round(it.color.g * 255) - g;
                            int db = Mathf.round(it.color.b * 255) - b;
                            return dr*dr + dg*dg + db*db;
                        });
                        tiles.add(new Schematic.Stile(block, (short) x, (short) y, (Object) item, (byte) 0));
                    }
                }
            }
            scaledPixmap.dispose();
        }
    }
}
