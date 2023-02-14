package org.aklein.contacts.api

class I18n {
    final ResourceBundle bundle

    I18n(ResourceBundle bundle) {
        this.bundle = bundle
    }

    String propertyMissing(String name) {
        return bundle.getString(name)
    }
}
