package org.aklein.contacts.api

import com.smartbear.edp.groovy.GroovyEventManager
import groovy.swing.SwingBuilder

interface Model<D extends Domain, V extends View> {
    V getView()

    void setView(V view)

    D getDomain()

    void setDomain(D domain)

    Group<D, V, ? extends Model> getGroup()

    void setGroup(Group<D, V, ? extends Model<D, V>> group)

    I18n getI18n()

    void setI18n(I18n bundle)
}

class DefaultModel<D extends Domain, V extends View> implements Model<D, V> {
    V view
    D domain
    Group<D, V, ? extends Model<D, V>> group
    I18n i18n
    Registry registry = Registry.instance
    GroovyEventManager bus = Group.bus

    void delegateToDomain(List<String> properties, boolean bidirectional = true) {
        delegateToDomain(properties.collectEntries { [(it): it] })
    }

    void delegateToDomain(Map<String, String> properties, boolean bidirectional = true) {
        properties.each { String localProperty, String targetProperty ->
            registry.builder(SwingBuilder).bind(source: this, sourceProperty: localProperty, target: domain, targetProperty: targetProperty, mutual: bidirectional)
        }
    }
}
