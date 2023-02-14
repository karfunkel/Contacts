package org.aklein.contacts.api

import com.smartbear.edp.groovy.GroovyEventManager
import groovy.swing.SwingBuilder
import groovy.util.logging.Slf4j
import org.aklein.contacts.adapter.Adapter
import picocli.CommandLine

import javax.swing.*
import java.util.prefs.Preferences

@Slf4j
abstract class Main {
    File input
    Adapter adapter
    File reportDir
    Locale language
    Preferences prefs
    CommandLine commandLine
    Map<String, Object> arguments

    GroovyEventManager bus

    Main(Map<String, Object> arguments = [:], File input, Adapter adapter, File reportDir, Locale language, Preferences prefs, CommandLine commandLine) {
        this.arguments = arguments
        this.input = input
        this.adapter = adapter
        this.reportDir = reportDir
        this.language = language
        this.prefs = prefs
        this.commandLine = commandLine

        def args = [
                input      : input?.absolutePath,
                adapter    : adapter?.getClass()?.canonicalName,
                reportDir  : reportDir?.absolutePath,
                language   : language,
                preferences: prefs?.absolutePath()
        ]

        log.info "Contacts starting with options: " + args.collect { "$it.key = $it.value" }.join(', ')

        Group.arguments = this.arguments
        Group.prefs = this.prefs
        Group.language = this.language
        Locale.default = this.language
        JComponent.defaultLocale = this.language

        ResourceBundle uiBundle = ResourceBundle.getBundle("${this.getClass().packageName}.UIManager", this.language, this.getClass().classLoader)
        uiBundle.keySet().each { String key ->
            UIManager.put(key, uiBundle.getString(key))
        }

        log.debug "Creating bus ..."
        this.bus = createBus()
        Group.bus = this.bus
        log.debug "Creating bus ... finished"

        log.debug "Subscribing events ..."
        subscribe(this.bus)
        bus.post(BusReady, this)
        log.debug "Subscribing events ... finished"

        log.debug "Registering builder ..."
        registerBuilders(Registry.instance)
        bus.post(BuildersRegistered, this)
        log.debug "Registering builder ... finished"

        log.debug "Init Main ..."
        init()
        bus.post(InitReady, this)
        log.debug "Init Main ... finished"

        log.debug "Registering groups ..."
        registerGroups(Registry.instance)
        bus.post(GroupsRegistered, this)
        log.debug "Registering groups ... finished"

        log.debug "Creating main-group ..."
        Group mainGroup = createMainGroup()
        bus.post(MainGroupCreated, this, group: mainGroup)
        log.debug "Creating main-group ... finished"

        log.debug "Before start configuration..."
        beforeStart()
        bus.post(Start, this)
        log.debug "Before start configuration... finished"

        log.debug "Ready to rumble"
        mainGroup.view.run()
    }

    protected GroovyEventManager createBus() {
        return new GroovyEventManager()
    }

    protected void registerBuilders(Registry registry) {
        registry.registerBuilder(new SwingBuilder(true))
    }

    protected void subscribe(GroovyEventManager bus) {}

    protected void init() {}

    protected void beforeStart() {}

    abstract protected void registerGroups(Registry registry)

    abstract Group createMainGroup()
}
