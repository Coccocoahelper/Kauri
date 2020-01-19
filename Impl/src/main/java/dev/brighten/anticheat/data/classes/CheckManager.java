package dev.brighten.anticheat.data.classes;

import cc.funkemunky.api.reflections.types.WrappedClass;
import cc.funkemunky.api.reflections.types.WrappedMethod;
import cc.funkemunky.api.tinyprotocol.api.NMSObject;
import dev.brighten.anticheat.check.api.*;
import dev.brighten.anticheat.data.ObjectData;
import lombok.val;
import org.bukkit.event.Event;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class CheckManager {
    private ObjectData objectData;
    public Map<String, Check> checks = new HashMap<>();
    public Map<Class<?>, List<Map.Entry<String, WrappedMethod>>> checkMethods = new ConcurrentHashMap<>();

    public CheckManager(ObjectData objectData) {
        this.objectData = objectData;
    }

    public boolean runPacket(NMSObject object, long timeStamp) {
        if(!checkMethods.containsKey(object.getClass())) return true;

        val methods = checkMethods.get(object.getClass());
        AtomicBoolean okay = new AtomicBoolean(true);
        methods.parallelStream().filter(entry -> entry.getValue().getMethod().isAnnotationPresent(Packet.class))
                .forEach(entry -> {
                    Check check = checks.get(entry.getKey());

                    if(check.enabled) {
                        if(entry.getValue().getMethod().getParameterCount() > 1) {
                            if(entry.getValue().getMethod().getGenericReturnType().equals(Void.TYPE))
                                entry.getValue().invoke(check, object, timeStamp);
                            else if(okay.get()
                                    && entry.getValue().getMethod().getReturnType().equals(boolean.class)) {
                                boolean cancel = entry.getValue().invoke(check, object, timeStamp);
                                if(!cancel) okay.set(false);
                            }
                        } else {
                            if(entry.getValue().getMethod().getGenericReturnType().equals(Void.TYPE))
                                entry.getValue().invoke(check, object);
                            else if(okay.get()
                                    && entry.getValue().getMethod().getReturnType().equals(boolean.class)) {
                                boolean cancel = entry.getValue().invoke(check, object);
                                if(!cancel) okay.set(false);
                            }
                        }
                    }
                });
        return okay.get();
    }

    public void runEvent(Event event) {
        if(!checkMethods.containsKey(event.getClass())) return;

        val methods = checkMethods.get(event.getClass());

        methods.parallelStream().filter(entry ->
                entry.getValue().getMethod().isAnnotationPresent(dev.brighten.anticheat.check.api.Event.class))
                .forEach(entry -> {
                    Check check = checks.get(entry.getKey());

                    if(check.enabled) {
                        entry.getValue().invoke(check, event);
                    }
                });
    }

    public void addChecks() {
        if(objectData.getPlayer().hasPermission("kauri.bypass") && Config.bypassPermission) return;
        Check.checkClasses.keySet().parallelStream()
                .map(clazz -> {
                    CheckInfo settings = Check.checkClasses.get(clazz);
                    Check check = clazz.getConstructor().newInstance();
                    check.data = objectData.INSTANCE;
                    CheckSettings checkSettings = Check.checkSettings.get(clazz);
                    check.enabled = checkSettings.enabled;
                    check.executable = checkSettings.executable;
                    check.cancellable = checkSettings.cancellable;
                    check.cancelMode = checkSettings.cancelMode;
                    check.developer = settings.developer();
                    check.name = settings.name();
                    check.description = settings.description();
                    check.punishVl = settings.punishVL();
                    check.checkType = settings.checkType();
                    check.maxVersion = settings.maxVersion();
                    check.minVersion = settings.minVersion();
                    return check;
                })
                .sequential()
                .forEach(check -> checks.put(check.name, check));

        checks.keySet().stream().map(name -> checks.get(name)).forEach(check -> {
            WrappedClass checkClass = new WrappedClass(check.getClass());
            
            Arrays.stream(check.getClass().getDeclaredMethods())
                    .parallel()
                    .filter(method -> method.isAnnotationPresent(Packet.class)
                            || method.isAnnotationPresent(dev.brighten.anticheat.check.api.Event.class))
                    .map(method -> new WrappedMethod(checkClass, method))
                    .forEach(method -> {
                        Class<?> parameter = method.getParameters().get(0);
                        List<Map.Entry<String, WrappedMethod>> methods = checkMethods.getOrDefault(
                                parameter,
                                new ArrayList<>());

                        methods.add(new AbstractMap.SimpleEntry<>(
                                check.name, method));
                        checkMethods.put(parameter, methods);
                    });
        });
    }
}