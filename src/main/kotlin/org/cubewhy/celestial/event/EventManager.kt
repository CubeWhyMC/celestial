/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */
package org.cubewhy.celestial.event

import java.lang.reflect.Method

object EventManager {
    private val REGISTRY_MAP: MutableMap<Class<out Event?>, ArrayList<EventData>?> = HashMap()


    private fun sortListValue(clazz: Class<out Event?>) {
        val flexibleArray = ArrayList<EventData>()

        for (b in EventPriority.valueArray) {
            for (methodData in REGISTRY_MAP[clazz]!!) {
                if (methodData.priority == b) {
                    flexibleArray.add(methodData)
                }
            }
        }

        REGISTRY_MAP[clazz] = flexibleArray
    }

    private fun isMethodBad(method: Method): Boolean {
        return method.parameterTypes.size != 1 || !method.isAnnotationPresent(EventTarget::class.java)
    }

    private fun isMethodBad(method: Method, clazz: Class<out Event?>): Boolean {
        return isMethodBad(method) || method.parameterTypes[0] == clazz
    }

    fun get(clazz: Class<out Event?>): ArrayList<EventData>? {
        return REGISTRY_MAP[clazz]
    }

    fun cleanMap(removeOnlyEmptyValues: Boolean) {
        val iterator: MutableIterator<Map.Entry<Class<out Event?>, ArrayList<EventData>?>> =
            REGISTRY_MAP.entries.iterator()

        while (iterator.hasNext()) {
            if (!removeOnlyEmptyValues || iterator.next().value!!.isEmpty()) {
                iterator.remove()
            }
        }
    }

    fun unregister(o: Any, clazz: Class<out Event?>) {
        if (REGISTRY_MAP.containsKey(clazz)) {
            for (methodData in REGISTRY_MAP[clazz]!!) {
                if (methodData.source == o) {
                    REGISTRY_MAP[clazz]!!.remove(methodData)
                }
            }
        }

        cleanMap(true)
    }

    fun unregister(o: Any) {
        for (flexibleArray in REGISTRY_MAP.values) {
            for (i in flexibleArray!!.indices.reversed()) {
                if (flexibleArray[i].source == o) {
                    flexibleArray.removeAt(i)
                }
            }
        }

        cleanMap(true)
    }

    fun register(method: Method, o: Any?) {
        val clazz = method.parameterTypes[0]

        val methodData = EventData(
            o!!, method, method.getAnnotation(
                EventTarget::class.java
            ).value
        )

        if (!methodData.target.isAccessible) {
            methodData.target.isAccessible = true
        }

        if (REGISTRY_MAP.containsKey(clazz)) {
            if (!REGISTRY_MAP[clazz]!!.contains(methodData)) {
                REGISTRY_MAP[clazz]!!.add(methodData)
                sortListValue(clazz as Class<out Event?>)
            }
        } else {
            REGISTRY_MAP[clazz as Class<out Event?>] = object : ArrayList<EventData>() {
                init {
                    this.add(methodData)
                }
            }
        }
    }

    fun register(o: Any, clazz: Class<out Event?>) {
        for (method in o.javaClass.methods) {
            if (!isMethodBad(method, clazz)) {
                register(method, o)
            }
        }
    }

    @JvmStatic
    fun register(o: Any) {
        for (method in o.javaClass.methods) {
            if (!isMethodBad(method)) {
                register(method, o)
            }
        }
    }
}
