package dev.brighten.anticheat.commands;

import cc.funkemunky.api.commands.ancmd.Command;
import cc.funkemunky.api.commands.ancmd.CommandAdapter;
import cc.funkemunky.api.utils.Init;
import dev.brighten.anticheat.Kauri;
import dev.brighten.anticheat.data.ObjectData;

@Init(commands =  true)
public class AlertsCommand {

    @Command(name = "kauri.alerts", description = "toggle off cheat alerts.", aliases = {"alerts"},
            display = "alerts", playerOnly = true, permission = "kauri.command.alerts")
    public void onCommand(CommandAdapter cmd) {
        ObjectData data = Kauri.INSTANCE.dataManager.getData(cmd.getPlayer());

        if(data != null) {
            data.alerts = !data.alerts;

            if(data.alerts) {
                Kauri.INSTANCE.dataManager.hasAlerts.add(data);
                cmd.getPlayer().sendMessage(Kauri.INSTANCE.msgHandler.getLanguage().msg("alerts-on",
                        "&aYou are now viewing cheat alerts."));
            } else {
                Kauri.INSTANCE.dataManager.hasAlerts.remove(data);
                cmd.getPlayer().sendMessage(Kauri.INSTANCE.msgHandler.getLanguage().msg("alerts-none",
                        "&cYou are no longer viewing cheat alerts."));
            }
        } else cmd.getSender().sendMessage(Kauri.INSTANCE.msgHandler.getLanguage().msg("data-error",
                "&cThere was an error trying to find your data."));
    }

}