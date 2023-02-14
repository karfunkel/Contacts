package com.smartbear.edp.api;

import java.util.EventObject;

/**
 * A Subscriber which can be used as a listener within the EDP framework.
 * User: Renato
 */
public interface EventSubscriber<K extends EventObject> {

    /**
     * Call-back for handling events.
     *
     * @param event
     */
    void handle(K event);

    Class<?> getSourceBaseType();

}
