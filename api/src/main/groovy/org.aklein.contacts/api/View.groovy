package org.aklein.contacts.api

import com.smartbear.edp.groovy.GroovyEventManager

abstract class View<M extends Model> extends Script {
    M model
    Group<? extends Domain, ? extends View, M> group
    I18n i18n
    List<View> parentViews = []

    Registry registry = Registry.instance
    GroovyEventManager bus = Group.bus

    def <T extends View> Object subView(Class<T> viewClass, FactoryBuilderSupport builder) {
        parentViews.push(this)
        builder.parent = this

        T view = viewClass.newInstance()

        // User setters to because of Script-Binding
        view.setModel(this.model)
        view.setGroup(this.group)
        view.setI18n(this.i18n)
        view.setParentViews(this.parentViews)

        def result = builder.build(view)

        parentViews.pop()
        builder.parent = parentViews ? parentViews.first() : null

        return result
    }

    def <T> T ref(String name, Class<T> type = null) {
        // Search in view Script-Binding, Script-Scope and Builders in registration order
        List<GroovyObject> sources = [] + this + registry.builderRegistry.values()
        for (GroovyObject obj : sources) {
            try {
                def result = obj.getProperty(name)
                if (typedValue(result, type) != null)
                    return result
            } catch (MissingPropertyException e) {
            }
        }
        return null
    }

    protected <T> T typedValue(def value, Class<T> type) {
        if (!type || type.isInstance(value))
            return value
        return null
    }
}

