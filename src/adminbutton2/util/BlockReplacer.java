// SPDX-License-Identifier: GPL-3.0
package adminbutton2.util;

import arc.Core;
import arc.struct.ObjectMap;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.ctype.ContentType;
import mindustry.ctype.MappableContent;
import mindustry.game.Schematics;
import mindustry.logic.GlobalVars;
import mindustry.world.Block;

import adminbutton2.blocks.distribution.*;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class BlockReplacer {
    public void replaceBlocks() {
        if (!Core.settings.getBool("adminbutton2.settings.block_replacer.enabled", false)) return;
        Class[] replaceClasses = {
            AB2Junction.class,
            AB2ItemBridge.class,
            AB2BufferedItemBridge.class,
        };
        Vars.content.getContentMap()[ContentType.block.ordinal()].each(b -> {
            for (Class c : replaceClasses) {
                if (b.getClass().getSuperclass() == c.getSuperclass()) {
                    try {
                        Block newBlock = (Block)c.newInstance();
                        replace((Block)b, newBlock);
                        if (c == AB2ItemBridge.class || c == AB2BufferedItemBridge.class) {
                            newBlock.allowDiagonal = false;
                        }
                    } catch (Exception e) {}
                }
            }
        });
        Vars.schematics = new Schematics();
        Vars.schematics.load();
        Vars.logicVars = new GlobalVars();
        Vars.logicVars.init();
    }

    @SuppressWarnings("unchecked")
    private void replace(Block source, Block target) {
        Class sourceClass = source.getClass();
        Class targetClass = target.getClass();
        ObjectMap<String, MappableContent>[] contentNameMap;
        try {
            Field contentNameMapf = Vars.content.getClass().getDeclaredField("contentNameMap");
            contentNameMapf.setAccessible(true);
            contentNameMap = (ObjectMap<String, MappableContent>[]) contentNameMapf.get(Vars.content);
        } catch (Exception e) {
            return;
        }
        while (true) {
            sourceClass = sourceClass.getSuperclass();
            if (sourceClass == null) break;
            targetClass = targetClass.getSuperclass();
            if (targetClass == null) break;
            Field[] sourceFields = sourceClass.getDeclaredFields();
            Field[] targetFields = targetClass.getDeclaredFields();
            for (Field targetF : targetFields) {
                try {
                    Field sourceF = sourceClass.getDeclaredField(targetF.getName());
                    targetF.setAccessible(true);
                    sourceF.setAccessible(true);
                    if (Modifier.isStatic(targetF.getModifiers())) continue;
                    if (targetF.getName().equals("buildType")) continue;
                    if (sourceF.getType().equals(targetF.getType())) {
                        targetF.set(target, sourceF.get(source));
                    }
                } catch (Exception e) {
                    continue;
                }
            }
        }
        Vars.content.getContentMap()[ContentType.block.ordinal()].replace(source, target);
        Vars.content.getContentMap()[ContentType.block.ordinal()].each(b -> {
            Class tclass = b.getClass();
            while (true) {
                tclass = tclass.getSuperclass();
                if (tclass == null) break;
                Field[] fields = tclass.getDeclaredFields();
                for (Field field : fields) {
                    try {
                        if (Modifier.isStatic(field.getModifiers())) continue;
                        if (field.get(b) == source) {
                            field.set(b, target);
                        }
                    } catch (Exception e) {
                        continue;
                    }
                }
            }
        });
        contentNameMap[ContentType.block.ordinal()].put(target.name, target);
        for (Field field : Blocks.class.getDeclaredFields()) {
            Blocks b = new Blocks();
            try {
                if (field.get(b) == source) {
                    field.set(b, target);
                    break;
                }
            } catch (Exception e) {
                break;
            }
        }
    }
}
