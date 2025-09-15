// SPDX-License-Identifier: GPL-3.0
package adminbutton2.util;

import arc.Core;
import arc.Events;
import arc.util.Interval;
import arc.util.Time;
import mindustry.game.EventType;

import adminbutton2.AdminVars;

public class Interaction {
    private Interval interval = new Interval();
    public boolean interacting = false;
    public String interactor = "";
    public float interactionCooldown = (float)Core.settings.getInt("adminbutton2.interaction_queue.interaction_cooldown_milliseconds", 250) / 1000f;

    public boolean willInteract() {
        return interacting == false && interval.get(0, 60f * interactionCooldown);
    }

    public boolean willInteract(String interactor) {
        return (interacting == false || this.interactor.equals(interactor)) && interval.get(0, 60f * interactionCooldown);
    }

    public float timeLeft() {
        return interval.getTime(0);
    }
}
