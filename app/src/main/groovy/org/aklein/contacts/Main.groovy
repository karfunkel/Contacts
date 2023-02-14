package org.aklein.contacts

import groovy.swing.SwingXBuilder
import groovy.swing.factory.LayoutFactory
import groovy.transform.InheritConstructors
import net.miginfocom.swing.MigLayout
import org.aklein.contacts.api.Group
import org.aklein.contacts.api.Registry
import org.aklein.contacts.group.main.MainDomain
import org.aklein.contacts.group.main.MainModel
import org.aklein.contacts.group.main.MainView

@InheritConstructors
class Main extends org.aklein.contacts.api.Main {
    protected void registerBuilders(Registry registry) {
        SwingXBuilder builder = new SwingXBuilder(true)
        builder.registerFactory("migLayout", new LayoutFactory(MigLayout))
        registry.registerBuilder(builder)
    }

    protected void registerGroups(Registry registry) {
        registry.registerGroup('main', MainDomain, MainView, MainModel)
    }

    Group<MainDomain, MainView, MainModel> createMainGroup() {
        return Group.singleton('main')
    }

    protected void beforeStart() {
        Group<MainDomain, MainView, MainModel> main = Group['main']
        main.domain.adapter = adapter
        main.domain.loadReports(reportDir)
        main.domain.dataFile = input
        if(arguments.deleteInput) {
            if(input?.exists())
                input.deleteOnExit()
        }
    }
}
