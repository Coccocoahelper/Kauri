package dev.brighten.anticheat.check.impl.combat.autoclicker;

import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInArmAnimationPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInBlockDigPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInFlyingPacket;
import dev.brighten.anticheat.check.api.*;
import dev.brighten.api.check.CheckType;

@CheckInfo(name = "Autoclicker (G)", description = "Stolen and modified FFX check (Autoclicker 6).", checkType = CheckType.AUTOCLICKER,
        developer = true)
@Cancellable(cancelType = CancelType.INTERACT)
public class AutoclickerG extends Check {

    private int clicks;
    private int outliers;
    private int flyingCount;
    private boolean release;

    @Packet
    void check(WrappedInFlyingPacket packet) {
        ++this.flyingCount;
    }

    @Packet
    void check(WrappedInBlockDigPacket packet) {
        if (packet.getAction() == WrappedInBlockDigPacket.EnumPlayerDigType.RELEASE_USE_ITEM) {
            this.release = true;
        }
    }

    @Packet
    void check(WrappedInArmAnimationPacket packet) {
        if (!data.playerInfo.breakingBlock && data.playerInfo.lastBlockPlace.hasPassed(4)) {
            if (this.flyingCount < 10) {
                if (this.release) {
                    this.release = false;
                    this.flyingCount = 0;
                    return;
                }
                if (this.flyingCount > 3) {
                    ++this.outliers;
                } else if (this.flyingCount == 0) {
                    return;
                }
                if (++this.clicks == 40) {
                    if (this.outliers == 0) {
                        if ((vl ++) >= 4.0) {
                            flag("o=%1", outliers);
                        }
                    } else if(vl > 0) {
                        vl --;
                    }
                    debug("outliers=" + outliers + " vl=" + vl);
                    this.outliers = 0;
                    this.clicks = 0;
                }
            }
            this.flyingCount = 0;
        }
    }
}
