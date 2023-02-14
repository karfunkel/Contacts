package org.aklein.contacts.api

import com.smartbear.edp.groovy.GroovyEventManager

interface Domain {}

abstract class AbstractDomain implements Domain {
    Registry registry = Registry.instance
    GroovyEventManager bus = Group.bus
}
