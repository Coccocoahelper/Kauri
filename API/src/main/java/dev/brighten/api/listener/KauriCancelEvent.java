package dev.brighten.api.listener;

import dev.brighten.api.check.CancelType;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
@Setter
public class KauriCancelEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    private boolean cancelled;
    private final Player player;
    private CancelType cancelType;

    public KauriCancelEvent(Player player, CancelType cancelType) {
        this.player = player;
        this.cancelType = cancelType;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}