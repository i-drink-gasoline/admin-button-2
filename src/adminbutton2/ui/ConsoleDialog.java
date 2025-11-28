// SPDX-License-Identifier: GPL-3.0
package adminbutton2.ui;

import arc.scene.ui.ScrollPane;
import arc.scene.ui.TextArea;
import arc.scene.ui.layout.Table;
import arc.util.Log;
import arc.util.Time;
import mindustry.Vars;
import mindustry.gen.Icon;
import mindustry.ui.dialogs.BaseDialog;

import adminbutton2.AdminVars;

public class ConsoleDialog extends BaseDialog {
    private Table table = new Table();
    private ScrollPane pane;
    private TextArea area;

    static {
        Log.LogHandler previousHandler = Log.logger;
        String[] prefixes = { "[green][D][]", "[blue][I][]", "[yellow][W][]", "[red][E][]" };
        Log.logger = (level, text) -> {
            if (previousHandler != null) previousHandler.log(level, text);
            String prefix = level.ordinal() <= prefixes.length ? prefixes[level.ordinal()] : "[?]" ;
            AdminVars.console.addMessage(prefix + " " + text);
        };
    }

    public ConsoleDialog() {
        super("@adminbutton2.consoledialog.title");
        addCloseButton();
        table.defaults().growX();
        pane = cont.pane(table).grow().fill().get();
        cont.row();
        area = cont.area("", s -> {}).growX().height(120f).get();
        cont.button(Icon.play, () -> {
            String message = area.getText();
            addMessage("[lightgray]> " + message.replace("[", "[["));
            addMessage(Vars.mods.getScripts().runConsole(message).replace("[", "[["));
        }).size(120f);
    }

    private void scroll() {
        pane.setScrollY(Float.MAX_VALUE);
    }

    public void addMessage(String text) {
        table.add(text).row();
        Time.run(5, this::scroll);
    }
}
