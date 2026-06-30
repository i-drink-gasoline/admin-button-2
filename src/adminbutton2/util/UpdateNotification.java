// SPDX-License-Identifier: GPL-3.0
package adminbutton2.util;

import arc.Core;
import arc.Events;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.Image;
import arc.util.Http;
import arc.util.Strings;
import arc.util.Time;
import arc.util.serialization.Jval;
import mindustry.Vars;
import mindustry.core.Version;
import mindustry.game.EventType;
import mindustry.gen.Icon;
import mindustry.ui.Fonts;
import mindustry.ui.dialogs.BaseDialog;

import adminbutton2.AdminVars;

public class UpdateNotification {
    String repo = AdminVars.loadedMod.getRepo();
    boolean shownReload = false;

    public UpdateNotification() {
        if (!(Core.settings.getBool("adminbutton2.imagegenerator.check_for_updates", true))) return;
        if (repo == null) return;
        Events.run(EventType.ClientLoadEvent.class, () -> query(repo));
    }

    private void query(String repo) {
        Http.get(Vars.ghApi + "/repos/" + repo + "/releases/latest", r -> {
            try {
                Jval json = Jval.read(r.getResultAsString());
                Jval.JsonArray assets = json.get("assets").asArray();
                boolean hasJAR = false;
                for (Jval asset : assets) {
                    if (asset.getString("name").equals("adminbutton2.jar")) {
                        hasJAR = true;
                        break;
                    }
                }
                if (!hasJAR) return;
                String currentVersionString = AdminVars.loadedMod.meta.version;
                float currentVersion = Strings.parseFloat(currentVersionString, -1f);
                if (currentVersion == -1f) return;
                String latestVersionString = json.getString("tag_name");
                float latestVersion = Strings.parseFloat(latestVersionString, -1f);
                if (currentVersion >= latestVersion) return;
                Http.get("https://raw.githubusercontent.com/" + repo + "/refs/tags/" + latestVersionString + "/mod.json", r2 -> {
                    try {
                        Jval modJson = Jval.read(r2.getResultAsString());
                        if (!Version.isAtLeast(modJson.getString("minGameVersion"))) return;
                        showUpdateDialog(latestVersionString, currentVersionString);
                    } catch (Exception e) {}
                });
            } catch (Exception e) {}
        });
    }

    private void showUpdateDialog(String latestVersionString, String currentVersionString) {
        BaseDialog dialog = new BaseDialog("@adminbutton2.update_dialog.title");
        dialog.addCloseListener();
        dialog.cont.defaults().pad(10f);
        Image adminImage = dialog.cont.image(new TextureRegionDrawable(Fonts.getLargeIcon("admin"))).size(130f).get();
        adminImage.update(() -> adminImage.color.fromHsv(Time.time % 360, 1f, 1f));
        dialog.cont.row();
        dialog.cont.add(Core.bundle.format("adminbutton2.update_dialog.update_available", currentVersionString, latestVersionString)).row();
        dialog.cont.table(t -> {
            t.defaults().size(130f).pad(5f);
            t.buttonRow("@adminbutton2.update_dialog.title.disable_notifications", Icon.eyeOff, () -> {
                dialog.hide();
                Core.settings.put("adminbutton2.imagegenerator.check_for_updates", false);
            });
            t.buttonRow("@adminbutton2.update_dialog.title.later", Icon.planeOutline, () -> {
                dialog.hide();
            });
            t.buttonRow("@adminbutton2.update_dialog.title.update", Icon.download, () -> {
                dialog.hide();
                Vars.ui.mods.githubImportMod(repo, true, "tags/" + latestVersionString);
                Events.run(EventType.Trigger.update, () -> {
                    if (!shownReload && Vars.mods.requiresReload()) {
                        Vars.mods.reload();
                        shownReload = true;
                    }
                });
            });
        });
        dialog.show();
    }
}
