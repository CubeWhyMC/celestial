/*
 * Celestial Launcher <me@lunarclient.top>
 * License under GPLv3
 * Do NOT remove this note if you want to copy this file.
 */

package org.cubewhy.celestial.event;

import lombok.extern.slf4j.Slf4j;
import org.cubewhy.celestial.utils.TextUtils;

import java.util.ArrayList;

@Slf4j
public class Event {
    /**
     * Call a event
     *
     * @return Event
     */
    public Event call() {
        final ArrayList<EventData> dataList = EventManager.get(this.getClass());

        if (dataList != null) {
            for (EventData data : dataList) {
                try {
                    data.target.invoke(data.source, this);
                } catch (Exception e) {
                    log.error(TextUtils.dumpTrace(e));
                }
            }
        }
        return this;
    }
}
