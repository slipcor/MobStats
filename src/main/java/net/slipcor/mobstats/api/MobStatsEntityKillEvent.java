package net.slipcor.mobstats.api;

import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class MobStatsEntityKillEvent extends Event implements Cancellable {
    private final Entity killer;
    private final Entity victim;

    boolean cancelled = false;
    private static final HandlerList handlers = new HandlerList();

    MobStatsEntityKillEvent(Entity killer, Entity victim) {
        this.killer = killer;
        this.victim = victim;
    }

    public Entity getKiller() {
        return killer;
    }

    public Entity getVictim() {
        return victim;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean value) {
        cancelled = value;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
