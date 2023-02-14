package org.aklein.contacts.group.main

import groovy.swing.SwingXBuilder
import groovy.transform.BaseScript
import org.aklein.contacts.api.Registry
import org.aklein.contacts.api.View

@BaseScript
View<MainModel> script

SwingXBuilder swing = Registry.instance.builder(SwingXBuilder)

swing.edt {
    menuBar(id: 'menuBar') {
        menu(text: i18n.menu_file_text, mnemonic: i18n.menu_file_mnemonic) {
            menuItem(openAction, icon:null)
            menuItem(reloadDataAction, icon: null)
            separator()
            menuItem(saveAction, icon:null)
            //menuItem(saveAsAction, icon:null)
            separator()
            menuItem(exitAction, icon: null)
        }

        menu(text: i18n.menu_report_text, mnemonic: i18n.menu_report_mnemonic) {
            //menuItem(reloadReportsAction, icon: null)
            menuItem(exportSampleAction, icon: null)
            separator()
            menuItem(exportAction, icon: null)
        }
    }
}
