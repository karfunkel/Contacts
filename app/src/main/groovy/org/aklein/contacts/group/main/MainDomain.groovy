package org.aklein.contacts.group.main


import groovy.beans.Bindable
import net.sf.jasperreports.compilers.JRGroovyCompiler
import net.sf.jasperreports.engine.DefaultJasperReportsContext
import net.sf.jasperreports.engine.JasperFillManager
import net.sf.jasperreports.engine.JasperPrint
import net.sf.jasperreports.engine.JasperReport
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource
import net.sf.jasperreports.engine.design.JasperDesign
import net.sf.jasperreports.engine.xml.JRXmlLoader
import org.aklein.contacts.adapter.Adapter
import org.aklein.contacts.api.AbstractDomain

import javax.xml.parsers.SAXParserFactory

// Separate data and methods to circumvent the fact that setting
// a property of the same class, the value will be set directly
// and the method with the event logic will not be called.
class MainDomainData extends AbstractDomain {
    @Bindable
    File reportDir
    @Bindable
    File dataFile
    @Bindable
    Adapter adapter
    @Bindable
    Report selectedReport
    @Bindable
    List<Map<String, String>> data = []
    @Bindable
    boolean dataSaved = false

    void setDataFile(File file) {
        if(!file){
            setDataSaved(true)
        } else if (file.exists() && this.dataFile != file) {
            this.dataFile = file
            // use setter to trigger Binding-Event
            setDataSaved(false)
        }
    }
}

class MainDomain extends MainDomainData {
    void loadReports(File file = reportDir) {
        if (file?.exists() && this.reportDir != file) {
            reportDir = file
        }
    }

    List<Map<String, String>> loadData(File file = dataFile) {
        dataFile = file
        if (selectedReport)
            this.data = file.withReader { return selectedReport.loadData(it, adapter) }
        return this.data
    }

    void saveData(File file) {
        file.withOutputStream { outStream ->
            this.dataFile.withInputStream { inStream ->
                outStream << inStream
            }
        }
        this.dataSaved = true
    }

    void exportSample(File file) {
        adapter.write(data, file)
        this.dataSaved = true
    }

    JasperPrint createJasperPrint() {
        if (selectedReport) {
            JasperReport report = createJasperReport(selectedReport)
            JRMapCollectionDataSource jrDataSource = new JRMapCollectionDataSource(data)
            return JasperFillManager.fillReport(report, [:], jrDataSource)
        }
    }

    JasperReport createJasperReport(Report report) {
        if (!report)
            return null
        JasperDesign design = JRXmlLoader.load(new File(report.file.absoluteFile.parentFile, report.report))
        JRGroovyCompiler compiler = new JRGroovyCompiler(DefaultJasperReportsContext.instance)
        return compiler.compileReport(design)
    }
}
