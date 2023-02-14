package org.aklein.contacts.group.main

import org.aklein.contacts.adapter.Adapter

class Report {
    Report parent = null
    List<Report> children = []
    boolean leaf = true

    File file
    String name
    String report
    Map<String, Closure<Void>> transforms
    Closure<List<Map<String, String>>> sort

    Report(File file, Report parent = null) {
        this.file = file
        this.parent = parent
        if (file.directory) {
            this.leaf = false
            this.name = name ?: file.name
            this.file.listFiles().findAll { it.directory || it.name.toLowerCase().endsWith('.report') }.each { File child ->
                children << new Report(child, this)
            }
        } else {
            this.leaf = true
            ConfigSlurper slurper = new ConfigSlurper()
            ConfigObject data = slurper.parse(file.toURI().toURL())
            this.name = data.name
            this.report = data.report
            this.transforms = data.transform
            this.sort = data.sort
        }
    }

    String toString() {
        return this.name
    }

    List<Map<String, String>> loadData(Reader reader, Adapter apapter) {
        if (!this.isLeaf())
            return []

        List<Map<String, String>> rows = apapter.read(reader, transforms)
        def result = this.sort(rows)
        return result
    }
}
