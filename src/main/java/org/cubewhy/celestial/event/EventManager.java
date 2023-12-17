/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */

package org.cubewhy.celestial.event;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class EventManager {

    private static final Map<Class<? extends Event>, ArrayList<EventData>> REGISTRY_MAP = new HashMap<>();


    @Getter
    private static final EventManager instance = new EventManager();


    private static void sortListValue(final Class<? extends Event> clazz) {

        final ArrayList<EventData> flexibleArray = new ArrayList<EventData>();

        for (final byte b : EventPriority.valueArray) {
            for (EventData methodData : EventManager.REGISTRY_MAP.get(clazz)) {
                if (methodData.priority == b) {
                    flexibleArray.add(methodData);
                }
            }
        }

        EventManager.REGISTRY_MAP.put(clazz, flexibleArray);

    }

    private static boolean isMethodBad(@NotNull final Method method) {
        return method.getParameterTypes().length != 1 || !method.isAnnotationPresent(EventTarget.class);
    }

    private static boolean isMethodBad(final Method method, final Class<? extends Event> clazz) {
        return isMethodBad(method) || method.getParameterTypes()[0].equals(clazz);
    }

    public static ArrayList<EventData> get(final Class<? extends Event> clazz) {
        return REGISTRY_MAP.get(clazz);
    }

    public static void cleanMap(final boolean removeOnlyEmptyValues) {

        final Iterator<Map.Entry<Class<? extends Event>, ArrayList<EventData>>> iterator = EventManager.REGISTRY_MAP.entrySet().iterator();

        while (iterator.hasNext()) {
            if (!removeOnlyEmptyValues || iterator.next().getValue().isEmpty()) {
                iterator.remove();
            }
        }
    }

    public static void unregister(final Object o, final Class<? extends Event> clazz) {

        if (REGISTRY_MAP.containsKey(clazz)) {
            for (final EventData methodData : REGISTRY_MAP.get(clazz)) {
                if (methodData.source.equals(o)) {
                    REGISTRY_MAP.get(clazz).remove(methodData);
                }
            }
        }

        cleanMap(true);

    }

    public static void unregister(final Object o) {

        for (ArrayList<EventData> flexibleArray : REGISTRY_MAP.values()) {

            for (int i = flexibleArray.size() - 1; i >= 0; i--) {

                if (flexibleArray.get(i).source.equals(o)) {
                    flexibleArray.remove(i);
                }

            }

        }

        cleanMap(true);

    }

    public static void register(@NotNull final Method method, final Object o) {

        final Class<?> clazz = method.getParameterTypes()[0];

        final EventData methodData = new EventData(o, method, method.getAnnotation(EventTarget.class).value());

        if (!methodData.target.isAccessible()) {
            methodData.target.setAccessible(true);
        }

        if (REGISTRY_MAP.containsKey(clazz)) {

            if (!REGISTRY_MAP.get(clazz).contains(methodData)) {
                REGISTRY_MAP.get(clazz).add(methodData);
                sortListValue((Class<? extends Event>) clazz);
            }

        } else {
            REGISTRY_MAP.put((Class<? extends Event>) clazz, new ArrayList<EventData>() {

                {
                    this.add(methodData);
                }

            });
        }

    }

    public static void register(@NotNull final Object o, final Class<? extends Event> clazz) {

        for (final Method method : o.getClass().getMethods()) {
            if (!isMethodBad(method, clazz)) {
                register(method, o);
            }
        }

    }

    public static void register(@NotNull Object o) {
        for (final Method method : o.getClass().getMethods()) {
            if (!isMethodBad(method)) {
                register(method, o);
            }
        }
    }

}
