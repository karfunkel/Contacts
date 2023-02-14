package com.smartbear.edp.api;

import java.util.EventObject;

/**
 * User: Renato
 */
public abstract class EventSubscriberBase<K extends EventObject> implements EventSubscriber<K> {

    private Class<?> sourceBaseType = Object.class;

    public abstract void handle(K event);

    public Class<?> getSourceBaseType() {
        return sourceBaseType;
    }

    public void setSourceBaseType(Class<?> sourceBaseType) {
        this.sourceBaseType = sourceBaseType;
    }

}
