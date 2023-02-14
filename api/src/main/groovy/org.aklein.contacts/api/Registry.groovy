package org.aklein.contacts.api

@Singleton
class Registry {
    static class GroupEntry<D extends Domain, V extends View, M extends Model> {
        String name
        D domain
        Class<D> domainType
        Class<V> viewType
        Class<M> modelType
        Group group
    }

    Map<String, GroupEntry> groupRegistry = [:]

    Map<Class<? extends FactoryBuilderSupport>, FactoryBuilderSupport> builderRegistry = [:]

    void registerGroup(String name, Class<? extends Domain> domain, Class<? extends View> view, Class<? extends Model> model) {
        registerGroup(name, new GroupEntry(name: name, domainType: domain, viewType: view, modelType: model))
    }

    void registerGroup(String name, Domain domain, Class<? extends View> view, Class<? extends Model> model) {
        registerGroup(name, new GroupEntry(name: name, domain: domain, domainType: domain.getClass(), viewType: view, modelType: model))
    }

    void registerGroup(String name, GroupEntry entry) {
        if (groupRegistry.containsKey(name))
            throw new RuntimeException("Group $name is already registered")
        else
            groupRegistry[name] = entry
    }

    def <T extends FactoryBuilderSupport> void registerBuilder(T builder) {
        if (builderRegistry.containsKey(builder.getClass()))
            throw new RuntimeException("Builder of type ${builder.getClass()} is already registered")
        else
            builderRegistry[builder.getClass()] = builder
    }

    GroupEntry group(String name) {
        return groupRegistry[name]
    }

    def <T extends FactoryBuilderSupport> T builder(Class<T> builderClass) {
        return builderRegistry[builderClass]
    }
}

