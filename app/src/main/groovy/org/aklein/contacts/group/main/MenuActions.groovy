package org.aklein.contacts.group.main

import groovy.swing.SwingXBuilder
import groovy.transform.BaseScript
import org.aklein.contacts.api.View

@BaseScript
View<MainModel> script

SwingXBuilder swing = registry.builder(SwingXBuilder)

swing.edt {
    action(id: 'reloadDataAction',
            name: i18n.reloadDataAction_name,
            closure: model.&reloadData,
            mnemonic: i18n.reloadDataAction_mnemonic,
            accelerator: shortcut(i18n.reloadDataAction_accelerator),
            enabled: bind{ model.domain.dataFile }
    )

    action(id: 'reloadReportsAction', // TODO: Currently ignored
            name: i18n.reloadReportsAction_name,
            closure: model.&reloadReports,
            mnemonic: i18n.reloadReportsAction_mnemonic,
    )

    action(id: 'exportAction',
            name: i18n.exportAction_name,
            closure: model.&export,
            mnemonic: i18n.exportAction_mnemonic,
            accelerator: shortcut(i18n.exportAction_accelerator),
            enabled: bind { model.domain.data }
    )

    action(id: 'exportSampleAction',
            name: i18n.exportSampleAction_name,
            closure: model.&exportSample,
            mnemonic: i18n.exportSampleAction_mnemonic,
            enabled: bind{ model.domain.data }
    )

    action(id: 'openAction',
            name: i18n.openAction_name,
            closure: model.&fileOpen,
            mnemonic: i18n.openAction_mnemonic,
            accelerator: shortcut(i18n.openAction_accelerator),
            shortDescription: i18n.openAction_shortDescription
    )

    action(id: 'saveAction',
            name: i18n.saveAction_name,
            //closure: model.&fileSave,
            closure: model.&fileSaveAs,
            mnemonic: i18n.saveAction_mnemonic,
            accelerator: shortcut(i18n.saveAction_accelerator),
            shortDescription: i18n.saveAction_shortDescription,
            enabled: bind{ model.domain.dataFile }
    )

    action(id: 'saveAsAction',
            name: i18n.saveAsAction_name,
            closure: model.&fileSaveAs,
            mnemonic: i18n.saveAsAction_mnemonic,
    )

    action(id: 'exitAction',
            name: i18n.exitAction_name,
            closure: model.&exit,
            mnemonic: i18n.exitAction_mnemonic,
            accelerator: shortcut(i18n.exitAction_accelerator)
    )
}
