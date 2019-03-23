package cc.funkemunky.anticheat.impl.checks.combat.autoclicker;

import cc.funkemunky.anticheat.api.checks.CancelType;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.MiscUtils;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.anticheat.api.utils.Setting;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import lombok.val;
import org.bukkit.event.Event;

@Packets(packets = {Packet.Client.ARM_ANIMATION})
@cc.funkemunky.api.utils.Init
@CheckInfo(name = "Autoclicker (Type A)", description = "A unique fast click check that detects jumps in CPS much faster.", type = CheckType.AUTOCLICKER, cancelType = CancelType.INTERACT)
public class AutoclickerA extends Check {

    @Setting(name = "maxCPS")
    private int maxCPS = 20;

    @Setting(name = "banCPS")
    private int banCPS = 30;

    @Setting(name = "verboseThreshold")
    private int verboseThreshold = 6;

    @Setting(name = "verboseDeduct")
    private double deduct = 0.25;

    private long lastTimeStamp;
    private double vl;

    public AutoclickerA() {

    }


    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        if (MiscUtils.shouldReturnArmAnimation(getData())) return;

        val elapsed = timeStamp - lastTimeStamp;

        if (elapsed < 2) return;
        val cps = 1000D / elapsed;

        if (cps > maxCPS && !getData().isLagging()) {
            if (vl++ > verboseThreshold) {
                flag(cps + ">-" + maxCPS, false, cps > banCPS);
            }
        } else {
            vl -= vl > 0 ? deduct : 0;
        }

        debug("VL: " + vl + " CPS: " + cps);

        lastTimeStamp = timeStamp;
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}