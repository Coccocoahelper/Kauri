package cc.funkemunky.anticheat.api.checks;

import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface CheckInfo {
    String name() default "";
    String description() default "Blocks cheats";
    CheckType type() default CheckType.MOVEMENT;
    CancelType cancelType() default CancelType.MOTION;
    int maxVL() default 100;
    boolean enabled() default true;
    boolean executable() default true;
    boolean cancellable() default true;
    boolean developer() default false;
    ProtocolVersion maxVersion() default ProtocolVersion.V1_13_2;

}