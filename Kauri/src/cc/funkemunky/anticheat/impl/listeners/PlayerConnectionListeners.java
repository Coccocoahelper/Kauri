package cc.funkemunky.anticheat.impl.listeners;

import cc.funkemunky.anticheat.Kauri;
import cc.funkemunky.api.Atlas;
import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
import cc.funkemunky.api.tinyprotocol.api.TinyProtocolHandler;
import org.bukkit.Achievement;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

@cc.funkemunky.api.utils.Init
public class PlayerConnectionListeners implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Atlas.getInstance().getThreadPool().execute(() -> {
            Kauri.getInstance().getDataManager().addData(event.getPlayer().getUniqueId());
            if (Kauri.getInstance().getStatsManager().isPlayerBanned(event.getPlayer().getUniqueId())) {
                Kauri.getInstance().getLoggerManager().removeBan(event.getPlayer().getUniqueId());
            }
        });

        if (ProtocolVersion.getGameVersion().isBelow(ProtocolVersion.V1_9)) event.getPlayer().removeAchievement(Achievement.OPEN_INVENTORY);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Kauri.getInstance().getDataManager().removeData(event.getPlayer().getUniqueId());
    }
}
