package org.aklein.contacts.api

import com.smartbear.edp.groovy.GroovyEventManager

import java.util.prefs.Preferences

class Group<D extends Domain, V extends View, M extends Model> {
    final String name
    final D domain
    final V view
    final M model
    final I18n i18n

    static Locale language = Locale.default
    static def bus = new GroovyEventManager()
    static Preferences prefs
    static Map<String, Object> arguments

    static Group<? extends Domain, ? extends View, ? extends Model> singleton(String name) {
        createOrGet(name, true)
    }

    static Group<? extends Domain, ? extends View, ? extends Model> instance(String name) {
        createOrGet(name, false)
    }

    static Group<? extends Domain, ? extends View, ? extends Model> getAt(String name) {
        Registry.GroupEntry entry = getGroupEntry(name)
        return entry.group
    }

    static Group<? extends Domain, ? extends View, ? extends Model> createOrGet(String name, boolean singleton = true) {
        if (singleton)
            return getAt(name) ?: create(name, true)
        else
            return create(name, false)
    }

    static protected getGroupEntry(String name) {
        Registry.GroupEntry entry = Registry.instance.group(name)
        if (!entry) {
            String message = "Group $name is not registered"
            bus.post(ErrorOccurred, this, location: 'getGroupEntry', name: name, error: message) // TODO: Generalisieren ?
            throw new LifecycleException(message)
        }
        return entry
    }

    protected static <D extends Domain, V extends View, M extends Model> Group<D, V, M> create(String name, boolean singleton) {
        Registry.GroupEntry entry = Registry.instance.group(name)
        bus.post(CreateGroupStart, this, name: name, entry: entry, singleton: singleton)
        Group<D, V, M> group = new Group<D, V, M>(
                entry.name,
                entry.domain ?: entry.domainType.newInstance(),
                entry.viewType.newInstance(),
                entry.modelType.newInstance()
        )
        if (singleton)
            entry.group = group
        bus.post(CreateGroupEnd, this, name: name, entry: entry, group: group, singleton: singleton)
        return group
    }

    protected Group(String name, D domain, V view, M model) {
        this.name = name
        this.domain = domain
        this.view = view
        this.model = model
        this.i18n = new I18n(ResourceBundle.getBundle("${view.getClass().packageName}.$name", Group.language, view.getClass().classLoader))

        model.view = view
        model.domain = domain
        model.group = this
        model.i18n = this.i18n

        // User setters to because of Script-Binding
        view.setModel(model)
        view.setGroup(this)
        view.setI18n(i18n)
    }
}
