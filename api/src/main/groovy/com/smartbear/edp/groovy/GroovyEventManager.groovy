package com.smartbear.edp.groovy

import com.google.common.eventbus.EventBus
import com.google.common.eventbus.Subscribe
import com.smartbear.edp.api.EventManager
import com.smartbear.edp.api.EventSubscriber
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.FromString
import groovy.transform.stc.SimpleType

import java.lang.ref.WeakReference

/**
 *
 * User: Renato
 * Adjusted by Sascha Klein
 */
class GroovyEventManager implements EventManager {
    private EventBus bus = new EventBus()
    protected Map<Class<? extends EventObject>, List> handlers = [:]
    final String name = GroovyEventManager.name

    GroovyEventManager() {
        bus.register this
    }

    @Subscribe
    void onEvent(EventObject event) {
        def deadRefs = []
        def handlersList = handlers[event.getClass()]
        handlersList?.each { ref ->
            def subscriber = ref instanceof WeakReference ? ref.get() : ref
            if (subscriber) {
                if (subscriber.sourceBaseType.isAssignableFrom(event.source.getClass()))
                    subscriber.handle event
            } else {
                deadRefs << ref
            }
        }
        handlersList?.removeAll(deadRefs)
    }

    @Override
    void post(EventObject event) {
        bus.post(event)
    }

    void post(Map<String, Object> data = [:], Class<? extends GroovyEventObject> type, def source) {
        post(type.newInstance(data, source))
    }

    @Override
    def <K extends EventObject> void subscribe(EventSubscriber<K> subscriber, Class<K> eventType) {
        handlers.get(eventType, []) << subscriber
    }

    def <K extends EventObject> void subscribe(Class<K> eventType, @ClosureParams(value = SimpleType, options = "java.util.EventObject") Closure subscriber) {
        subscribe(eventType, null, null, subscriber)
    }

    def <K extends EventObject> void subscribe(Class<K> eventType, Object id, @ClosureParams(value = SimpleType, options = "java.util.EventObject") Closure subscriber) {
        subscribe(eventType, null, id, subscriber)
    }

    def <K extends EventObject> void subscribe(Class<K> eventType, Class sourceBaseType, @ClosureParams(value = SimpleType, options = "java.util.EventObject") Closure subscriber) {
        subscribe(eventType, sourceBaseType, null, subscriber)
    }

    def <K extends EventObject> void subscribe(Class<K> eventType, Class sourceBaseType, Object id, @ClosureParams(value = SimpleType, options = "java.util.EventObject") Closure subscriber) {
        EventSubscriber<K> ges = new GroovyEventSubscriber<>(id, sourceBaseType, subscriber)
        handlers.get(eventType, []) << ges
    }

    @Override
    def <K extends EventObject> void subscribeWeakly(EventSubscriber<K> subscriber, Class<K> eventType) {
        handlers.get(eventType, []) << new WeakReference(subscriber)
    }

    def <K extends EventObject> void subscribeWeakly(Class<K> eventType, @ClosureParams(value = SimpleType, options = "java.util.EventObject") Closure subscriber) {
        EventSubscriber<K> ges = new GroovyEventSubscriber<>(id, sourceBaseType, subscriber)
        subscribeWeakly(eventType, null, null, subscriber)
    }

    def <K extends EventObject> void subscribeWeakly(Class<K> eventType, Object id, @ClosureParams(value = SimpleType, options = "java.util.EventObject") Closure subscriber) {
        EventSubscriber<K> ges = new GroovyEventSubscriber<>(id, sourceBaseType, subscriber)
        subscribeWeakly(eventType, null, id, subscriber)
    }

    def <K extends EventObject> void subscribeWeakly(Class<K> eventType, Class sourceBaseType, @ClosureParams(value = SimpleType, options = "java.util.EventObject") Closure subscriber) {
        EventSubscriber<K> ges = new GroovyEventSubscriber<>(id, sourceBaseType, subscriber)
        subscribeWeakly(eventType, sourceBaseType, null, subscriber)
    }

    def <K extends EventObject> void subscribeWeakly(Class<K> eventType, Class sourceBaseType, Object id, @ClosureParams(value = SimpleType, options = "java.util.EventObject") Closure subscriber) {
        EventSubscriber<K> ges = new GroovyEventSubscriber<>(id, sourceBaseType, subscriber)
        handlers.get(eventType, []) << new WeakReference(ges)
    }

    @Override
    def <K extends EventObject> void unSubscribe(EventSubscriber<K> subscriber) {
        unSubscribe { subscriber.is(it) }
    }

    def <K extends EventObject> void unSubscribe(EventSubscriber<K>[] subscribers) {
        unSubscribe { subscribers.contains(it) }
    }

    def <K extends EventObject> void unSubscribe(Class<?> subscriberType) {
        unSubscribe { subscriberType.isInstance(it) }
    }

    def <K extends EventObject> void unSubscribe(Class<?> subscriberType, @ClosureParams(value = FromString, options = "com.smartbear.edp.api.EventSubscriber<? extends EventObject>") Closure<Boolean> filter) {
        unSubscribe { subscriberType.isInstance(it) && filter(it) }
    }

    def <K extends EventObject> void unSubscribe(Object id) {
        unSubscribe {
            if (!it instanceof GroovyEventSubscriber) return false
            switch (it.id) {
                case id: return true
                default: return false
            }
        }
    }

    def <K extends EventObject> void unSubscribe(@ClosureParams(value = FromString, options = "com.smartbear.edp.api.EventSubscriber<? extends EventObject>") Closure<Boolean> filter) {
        handlers.values().each { List list ->
            list.removeAll {
                EventSubscriber sub = it instanceof WeakReference ? it.get() : it
                return (sub == null) ? true : filter(sub)
            }
        }
    }
}

class GroovyEventObject extends EventObject {
    Map<String, Object> data = [:]

    GroovyEventObject(Map<String, Object> data = [:], Object source) {
        super(source)
        this.data = data
    }
}

class GroovyEventSubscriber<K extends EventObject> implements EventSubscriber<K> {
    protected Class<?> sourceBaseType = Object
    protected Closure handler
    protected Object id

    GroovyEventSubscriber(String id, Class<?> sourceBaseType, @ClosureParams(value = SimpleType, options = "java.util.EventObject") Closure<Void> handler) {
        this.id = id
        this.sourceBaseType = sourceBaseType
        this.handler = handler
    }

    @Override
    void handle(K event) {
        handler(event)
    }

    @Override
    Class<?> getSourceBaseType() {
        return sourceBaseType
    }

    Object getId() {
        return id
    }
}
