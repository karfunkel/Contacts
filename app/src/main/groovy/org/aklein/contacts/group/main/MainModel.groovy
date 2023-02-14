package org.aklein.contacts.group.main

import groovy.beans.Bindable
import groovy.swing.model.DefaultTableModel
import groovy.swing.model.ValueHolder
import net.sf.jasperreports.engine.DefaultJasperReportsContext
import net.sf.jasperreports.swing.JRViewerToolbar
import net.sf.jasperreports.view.JRSaveContributor
import net.sf.jasperreports.view.JasperViewer
import net.sf.jasperreports.view.save.JRDocxSaveContributor
import org.aklein.contacts.api.DefaultModel
import org.aklein.contacts.api.Group
import org.jdesktop.swingx.JXFrame
import org.jdesktop.swingx.JXTable
import org.jdesktop.swingx.JXTree

import javax.swing.*
import javax.swing.filechooser.FileFilter
import java.awt.event.ActionEvent
import java.awt.event.WindowAdapter

@Bindable
class MainModel extends DefaultModel<MainDomain, MainView> {
    void exit(ActionEvent evt = null) {
        def frame = view.ref('mainFrame', JXFrame)
        if (exitAndSaveData()) {
            frame.visible = false
            frame.dispose()
            System.exit(0)
        }
    }

    void export(ActionEvent evt = null) {
        JasperViewer jasperViewer = new JasperViewer(
                DefaultJasperReportsContext.getInstance(),
                domain.createJasperPrint(),
                false,
                Group.language,
                null
        )
        JRViewerToolbar toolBar = jasperViewer.viewer.tlbToolBar

        // lastFolder
        String home = System.properties['user.home']
        File lastExportDir = new File(Group.prefs.get('exportFile.lastDir', home))
        if (!lastExportDir?.isDirectory() || !lastExportDir?.exists())
            lastExportDir = new File(home)
        toolBar.lastFolder = lastExportDir

        // lastSaveContributor
        Class<JRSaveContributor> contributorClass = JRDocxSaveContributor
        String contributor = Group.prefs.get('exportFile.lastSaveContributor', contributorClass.canonicalName)
        try {
            contributorClass = Class<JRSaveContributor>.forName(contributor)
        } catch (e) {
        }
        toolBar.lastSaveContributor = toolBar.saveContributors.find { it.getClass() == contributorClass }

        jasperViewer.addWindowListener([windowClosing: {
            Group.prefs.put('exportFile.lastDir', toolBar.lastFolder.absolutePath)
            Group.prefs.put('exportFile.lastSaveContributor', toolBar.lastSaveContributor.getClass().canonicalName)
        }] as WindowAdapter)
        jasperViewer.visible = true
    }

    void exportSample(ActionEvent evt = null) {
        File file = selectFile(i18n.save)
        if (file != null) {
            if (file.exists()) {
                yesNo(
                        i18n.overwriteData,
                        i18n.areYouSure,
                        { domain.exportSample(file) }
                )
            } else
                domain.exportSample(file)
        }
    }

    // TODO: Currently ignored
    void reloadReports(ActionEvent evt = null) {
        if (domain.reportDir != null) {
            domain.loadReports()

            def tree = view.ref('reportTree', JXTree)
            tree.model = getReportTreeModel()
        }
    }

    ReportTreeModel getReportTreeModel() {
        try {
            new ReportTreeModel(domain.reportDir)
        } catch (e) {
            e.printStackTrace()
        }
    }

    void fileOpen(ActionEvent evt = null) {
        File file = selectFile()
        if (file != null) {
            domain.dataFile = file
            reloadData()
            domain.dataSaved = true
        }
    }

    void reloadData(ActionEvent evt = null) {
        if (domain.dataFile) {
            List<Map<String, String>> data = domain.loadData()
            ValueHolder dataHolder = new ValueHolder(data)
            DefaultTableModel tableModel = new DefaultTableModel(dataHolder)

            data[0].each { key, v ->
                tableModel.addClosureColumn(key, { source -> source[key] }, { source, value -> source[key] = value }, String)
            }

            def table = view.ref('dataTable', JXTable)
            table.model = tableModel
        }
    }

    // Save file - return false if user cancelled save
    boolean fileSave(ActionEvent evt = null) {
        if (domain.dataFile == null)
            return fileSaveAs(evt)
        else {
            if (domain.dataFile.exists()) {
                yesNo(
                        i18n.overwriteData,
                        i18n.areYouSure,
                        { domain.saveData() }
                )
            } else
                domain.saveData()
        }
    }

    // Save file - return false if user cancelled save
    boolean fileSaveAs(ActionEvent evt = null) {
        File file = selectFile(i18n.save)
        if (file != null) {
            if (file.exists()) {
                yesNo(
                        i18n.overwriteData,
                        i18n.areYouSure,
                        { domain.saveData(file) }
                )
            } else
                domain.saveData(file)
            return true
        } else
            return false
    }

    File selectFile(buttonName = i18n.open) {
        String home = System.properties['user.home']
        File lastDir = new File(Group.prefs.get('selectFile.lastDir', home))
        if (!lastDir?.isDirectory() || !lastDir?.exists())
            lastDir = new File(home)

        def fc = new JFileChooser(lastDir)
        fc.fileFilter = [accept: domain.adapter.&acceptFilter, getDescription: { i18n[domain.adapter.filterDescriptionKey] }] as FileFilter
        fc.fileSelectionMode = JFileChooser.FILES_ONLY
        if (fc.showDialog(view.ref('mainFrame', JXFrame), buttonName) == JFileChooser.APPROVE_OPTION) {
            Group.prefs.put('selectFile.lastDir', fc.selectedFile.parent)
            return fc.selectedFile
        } else
            return null
    }

    boolean exitAndSaveData() {
        if (domain.dataSaved)
            return true
        return yesNoCancel(
                i18n.closeApp,
                i18n.saveData,
                { fileSave() },
                { true },
                { false }
        )
    }

    def yesNoCancel(String title, String message, def yes = true, def no = false, def cancel = null) {
        switch (JOptionPane.showConfirmDialog(view.ref('mainFrame', JXFrame), message, title, JOptionPane.YES_NO_CANCEL_OPTION)) {
            case JOptionPane.YES_OPTION:
                return yes instanceof Closure ? yes() : yes
            case JOptionPane.NO_OPTION:
                return no instanceof Closure ? no() : no
            default:
                return cancel instanceof Closure ? cancel() : cancel
        }
    }

    def yesNo(String title, String message, def yes = true, def no = false) {
        switch (JOptionPane.showConfirmDialog(view.ref('mainFrame', JXFrame), message, title, JOptionPane.YES_NO_OPTION)) {
            case JOptionPane.YES_OPTION:
                return yes instanceof Closure ? yes() : yes
            case JOptionPane.NO_OPTION:
                return no instanceof Closure ? no() : no
            default:
                return null
        }
    }

    def okCancel(String title, String message, def ok = true, def cancel = false) {
        switch (JOptionPane.showConfirmDialog(view.ref('mainFrame', JXFrame), message, title, JOptionPane.OK_CANCEL_OPTION)) {
            case JOptionPane.YES_OPTION:
                return ok instanceof Closure ? ok() : ok
            case JOptionPane.NO_OPTION:
                return cancel instanceof Closure ? cancel() : cancel
            default:
                return null
        }
    }

    protected executeOrReturn(def value) {
        return value instanceof Closure ? value() : value
    }
}
