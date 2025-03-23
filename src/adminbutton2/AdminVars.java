// SPDX-License-Identifier: GPL-3.0
package adminbutton2;

import adminbutton2.ui.AdminDialog;
import adminbutton2.ui.MessageList;

public class AdminVars {
    public static AdminDialog admin;
    public static MessageList messages;

    public static void init() {
        admin = new AdminDialog();
        messages = new MessageList();
    }
}
