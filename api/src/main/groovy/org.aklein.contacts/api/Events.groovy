package org.aklein.contacts.api

import com.smartbear.edp.groovy.GroovyEventObject
import groovy.transform.InheritConstructors

@InheritConstructors
class BusReady extends GroovyEventObject {}

@InheritConstructors
class BuildersRegistered extends GroovyEventObject {}

@InheritConstructors
class InitReady extends GroovyEventObject {}

@InheritConstructors
class GroupsRegistered extends GroovyEventObject {}

@InheritConstructors
class MainGroupCreated extends GroovyEventObject {}

@InheritConstructors
class CreateGroupStart extends GroovyEventObject {}

@InheritConstructors
class CreateGroupEnd extends GroovyEventObject {}

@InheritConstructors
class Start extends GroovyEventObject {}

@InheritConstructors
class End extends GroovyEventObject {}

@InheritConstructors
class ErrorOccurred extends GroovyEventObject {}
