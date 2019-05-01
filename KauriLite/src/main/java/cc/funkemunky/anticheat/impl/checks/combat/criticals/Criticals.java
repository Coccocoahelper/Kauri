package cc.funkemunky.anticheat.impl.checks.combat.criticals;

import cc.funkemunky.anticheat.api.checks.CancelType;
import cc.funkemunky.anticheat.api.checks.Check;
import cc.funkemunky.anticheat.api.checks.CheckInfo;
import cc.funkemunky.anticheat.api.checks.CheckType;
import cc.funkemunky.anticheat.api.utils.Packets;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.utils.Init;
import lombok.val;
import org.bukkit.event.Event;

@CheckInfo(name = "Criticals", type = CheckType.COMBAT, cancelType = CancelType.COMBAT, maxVL = 50)
@Init
@Packets(packets = {Packet.Client.POSITION, Packet.Client.POSITION_LOOK, Packet.Client.LEGACY_POSITION_LOOK, Packet.Client.LEGACY_POSITION})
public class Criticals extends Check {

    @Override
    public void onPacket(Object packet, String packetType, long timeStamp) {
        val move = getData().getMovementProcessor();

        if(getData().getLastAttack().hasNotPassed(1) && move.getGroundTicks() > 2) {
            if(move.getDeltaY() < 0 && !move.isHalfBlocksAround() && !move.isBlocksOnTop()) {
                flag(move.getDeltaY() + "<-0", true, true);
            }
            debug("deltaY=" + move.getDeltaY() + " ticks=" + move.getGroundTicks());
        }
    }

    @Override
    public void onBukkitEvent(Event event) {

    }
}
