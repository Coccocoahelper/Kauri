package cc.funkemunky.anticheat.api.checks;

import cc.funkemunky.anticheat.Kauri;
import cc.funkemunky.anticheat.api.data.PlayerData;
import cc.funkemunky.api.event.system.Listener;
import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
import cc.funkemunky.api.utils.Color;
import cc.funkemunky.api.utils.JsonMessage;
import cc.funkemunky.api.utils.MiscUtils;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

@Getter
@Setter
public abstract class Check implements Listener, org.bukkit.event.Listener {
    private String name;
    private CheckType type;
    private String description;
    private CancelType cancelType;
    private PlayerData data;
    private int maxVL, banWaveThreshold;
    private boolean enabled, executable, cancellable, developer, isBanWave;
    private long lastAlert;
    private List<String> execCommand = new ArrayList<>();
    private Map<String, Object> settings = new HashMap<>();
    private String alertMessage = "";
    private int vl;
    private ProtocolVersion minimum, maximum;

    public Check() {
        alertMessage = CheckSettings.alertMessage;
    }

    protected void flag(String information, boolean cancel, boolean ban) {
        if (Kauri.getInstance().getTps() > CheckSettings.tpsThreshold) {
            Kauri.getInstance().getCheckManager().getAlertsExecutable().execute(() -> {
                if (ban && !data.isLagging()) {
                    vl++;
                }
                Kauri.getInstance().getLoggerManager().addViolation(data.getUuid(), this);
                Kauri.getInstance().getStatsManager().addFlag();
                if (vl > maxVL && executable && ban && !developer && !getData().isBanned()) {
                    getData().setBanned(true);
                    new BukkitRunnable() {
                        public void run() {
                            getData().setBanned(false);
                            execCommand.forEach(cmd -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("%player%", getData().getPlayer().getName()).replace("%check%", getName())));
                            getData().getPacketChecks().values().forEach(checkList -> checkList.forEach(check -> check.vl = 0));
                            getData().getBukkitChecks().values().forEach(checkList -> checkList.forEach(check -> check.vl = 0));
                        }
                    }.runTaskLater(Kauri.getInstance(), 10);
                    if (CheckSettings.broadcastEnabled)
                        Bukkit.broadcastMessage(Color.translate(CheckSettings.broadcastMessage.replace("%player%", getData().getPlayer().getName())));
                    Kauri.getInstance().getLoggerManager().addBan(data.getUuid(), this);
                }

                data.getLastFlag().reset();

                if (cancel && cancellable) data.setCancelType(cancelType);

                JsonMessage message = new JsonMessage();

                if ((!data.getMovementProcessor().isLagging())) {
                    if (System.currentTimeMillis() - lastAlert > CheckSettings.alertsDelay) {
                        if (ban && !developer) {
                            message.addText(Color.translate(alertMessage.replace("%prefix%", CheckSettings.alertPrefix).replace("%check%", getName()).replace("%player%", data.getPlayer().getName()).replace("%vl%", String.valueOf(vl)).replace("%info%", information))).addHoverText(Color.Gray + information);
                            Kauri.getInstance().getDataManager().getDataObjects().keySet().stream().filter(key -> Kauri.getInstance().getDataManager().getDataObjects().get(key).isAlertsEnabled())
                                    .forEach(key -> message.sendToPlayer(Kauri.getInstance().getDataManager().getDataObjects().get(key).getPlayer()));
                            lastAlert = System.currentTimeMillis();

                            if (CheckSettings.printToConsole) {
                                MiscUtils.printToConsole(alertMessage.replace("%check%", (developer ? Color.Red + Color.Italics : "") + getName()).replace("%player%", data.getPlayer().getName()).replace("%vl%", String.valueOf(vl)));
                            }
                        } else {
                            message.addText(Color.translate(alertMessage.replace("%prefix%", CheckSettings.devAlertPrefix).replace("%check%", (developer ? Color.Red + Color.Italics : "") + getName()).replace("%player%", data.getPlayer().getName()).replace("%vl%", "N/A").replaceAll("%info%", information))).addHoverText(Color.Gray + information);

                            Kauri.getInstance().getDataManager().getDataObjects().keySet().stream().filter(key -> {
                                PlayerData data = Kauri.getInstance().getDataManager().getDataObjects().get(key);

                                return data.isAlertsEnabled() && data.isDeveloperAlerts();
                            }).forEach(key -> message.sendToPlayer(Kauri.getInstance().getDataManager().getDataObjects().get(key).getPlayer()));
                        }
                    }
                }

                if (CheckSettings.testMode && !data.isAlertsEnabled()) message.sendToPlayer(data.getPlayer());
            });
        }
    }

    public void loadFromConfig() {
        Map<String, Object> values = new HashMap<>();
        String path = "checks." + name;
        values.put(path + ".maxVL", maxVL);
        values.put(path + ".enabled", enabled);
        values.put(path + ".executable", executable);
        values.put(path + ".cancellable", cancellable);
        values.put(path + ".executableCommands", Collections.singletonList("%global%"));
        values.put(path + ".banWave.enabled", isBanWave);
        values.put(path + ".banWave.threshold", banWaveThreshold);

        values.keySet().stream().filter(key -> Kauri.getInstance().getConfig().get(key) == null).forEach(key -> {
            Kauri.getInstance().getConfig().set(key, values.get(key));
            Kauri.getInstance().saveConfig();
        });

        if (Kauri.getInstance().getConfig().get("checks." + name) != null) {
            maxVL = Kauri.getInstance().getConfig().getInt(path + ".maxVL");
            enabled = Kauri.getInstance().getConfig().getBoolean(path + ".enabled");
            executable = Kauri.getInstance().getConfig().getBoolean(path + ".executable");
            cancellable = Kauri.getInstance().getConfig().getBoolean(path + ".cancellable");
            Kauri.getInstance().getConfig().getStringList(path + ".executableCommands").forEach(cmd -> {
                if (cmd.equals("%global%")) {
                    execCommand.addAll(CheckSettings.executableCommand);
                } else {
                    execCommand.add(cmd);
                }
            });
            isBanWave = Kauri.getInstance().getConfig().getBoolean(path + ".banWave.enabled");
            banWaveThreshold = Kauri.getInstance().getConfig().getInt(path + ".banWave.threshold");
        }
    }

    public void debug(String debugString) {
        Kauri.getInstance().getDataManager().getDataObjects().values().stream()
                .filter(dData -> dData.getDebuggingPlayer() != null && dData.getDebuggingCheck() != null && dData.getDebuggingCheck().getName().equals(name) && dData.getDebuggingPlayer().equals(data.getUuid()))
                .forEach(dData -> dData.getPlayer().sendMessage(Color.translate("&8[&cDebug&8] &7" + debugString)));
    }

    public abstract void onPacket(Object packet, String packetType, long timeStamp);

    public abstract void onBukkitEvent(Event event);
}