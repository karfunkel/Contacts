package org.aklein.contacts.adapter

import com.opencsv.CSVWriter
import com.xlson.groovycsv.CsvParser
import com.xlson.groovycsv.PropertyMapper
import groovy.util.logging.Slf4j

@Slf4j
class CSV implements Adapter {
    String getFilterDescriptionKey() {
        return "CSV_filter_description"
    }

    boolean acceptFilter(File file) {
        return file.isDirectory() || file.name.toLowerCase().endsWith('.csv')
    }

    List<Map<String, String>> read(Reader reader, Map<String, Closure<Void>> transforms = [:]) {
        List<Map<String, String>> rows = CsvParser.parseCsv(reader).collect { PropertyMapper row -> row.toMap() }
        rows.each { Map<String, String> row ->
            log.debug "-" * 20
            transforms.each { String name, Closure<Void> transform ->
                if (log.debugEnabled) {
                    log.debug "transform: $name"
                    def old = row.clone()

                    transform(row)

                    def ab = (row - old).collectEntries { k, v -> [("new: $k"): v] }
                    def ba = (old - row).collectEntries { k, v -> [("old: $k"): v] }
                    log.debug(ab + ba)
                    log.debug "-" * 20
                } else
                    transform(row)
            }
        }
    }

    void write(List<Map<String, String>> data, File file) {
        file.withWriter { fileWriter ->
            CSVWriter csvWriter = new CSVWriter(fileWriter)
            String[] headers = data.first().keySet().sort()
            csvWriter.writeNext(headers, true)

            data.each { row ->
                def map = row.sort { it.key }
                String[] array = map.values().toArray(new String[]{})
                csvWriter.writeNext(array, true)
            }
        }
    }
}
