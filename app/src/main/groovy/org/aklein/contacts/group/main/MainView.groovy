package org.aklein.contacts.group.main

import groovy.swing.SwingXBuilder
import groovy.transform.BaseScript
import org.aklein.contacts.api.View

import javax.swing.*
import javax.swing.event.TreeSelectionEvent
import javax.swing.tree.TreeSelectionModel

import static javax.swing.JFrame.DO_NOTHING_ON_CLOSE

@BaseScript
View<MainModel> script

SwingXBuilder swing = registry.builder(SwingXBuilder)

boolean debugLayout = false

Closure migDebug = { return debugLayout ? ', debug' : '' }

swing.lookAndFeel 'javax.swing.plaf.nimbus.NimbusLookAndFeel'

subView(MenuActions, swing)

swing.edt {
    frame(id: 'mainFrame',
            title: i18n.title,
            size: [1024, 768],
            locationByPlatform: true,
            defaultCloseOperation: DO_NOTHING_ON_CLOSE,
            show: true,
            windowClosing: { model.exit() }
    ) {
        subView(MenuBar, swing)
        migLayout(layoutConstraints: 'fill' + migDebug(), rowConstraints: '10[25!]5[fill]5[25!]10', columnConstraints: '10[300!]5[fill]10')
        scrollPane(constraints: 'cell 0 0 1 3, grow') {
            tree(
                    id: 'reportTree',
                    model: model.reportTreeModel,
                    showsRootHandles: false,
            )
            reportTree.selectionModel.selectionMode = TreeSelectionModel.SINGLE_TREE_SELECTION
            reportTree.addTreeSelectionListener { TreeSelectionEvent evt ->
                Report report = evt.path.lastPathComponent
                model.domain.selectedReport = report
                model.reloadData()
            }
        }
        label(
                constraints: 'cell 1 0, grow',
                text: bind(source: model.domain, sourceProperty: 'dataFile', converter: {
                    it?.absolutePath ?: i18n.unknownFile
                })
        )
        scrollPane(constraints: 'cell 1 1, grow', horizontalScrollBarPolicy: ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS) {
            table(id: 'dataTable', editable: false, autoResizeMode: JTable.AUTO_RESIZE_OFF) {
                tableModel(list: []) {}
            }
        }
        panel(constraints: 'cell 1 2, grow') {
            migLayout(
                    layoutConstraints: 'gap 10, ins 0, rtl' + migDebug(),
                    rowConstraints: '[fill]',
                    columnConstraints: '[sg, fill]',
            )
            button(
                    text: i18n.export,
                    actionPerformed: model.&export,
                    enabled: bind { model.domain.data }
            )
        }
    }
}
