package org.aklein.contacts.adapter

interface Adapter {
    List<Map<String, String>> read(Reader reader)

    List<Map<String, String>> read(Reader reader, Map<String, Closure<Void>> transforms)

    void write(List<Map<String, String>> data, File file)

    String getFilterDescriptionKey()

    boolean acceptFilter(File file)
}
