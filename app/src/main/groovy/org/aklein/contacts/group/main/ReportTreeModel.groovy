package org.aklein.contacts.group.main

import javax.swing.event.TreeModelListener
import javax.swing.tree.TreeModel
import javax.swing.tree.TreePath

class ReportTreeModel implements TreeModel {
    File reportDir
    Report root

    ReportTreeModel(File reportDir) {
        this.reportDir = reportDir
        root = new Report(reportDir)
    }

    @Override
    Object getRoot() {
        return root
    }

    @Override
    Object getChild(Object parent, int index) {
        if (parent instanceof Report)
            parent.children[index]
        else
            return null
    }

    @Override
    int getChildCount(Object parent) {
        if (parent instanceof Report)
            parent.children.size()
        else
            return 0
    }

    @Override
    boolean isLeaf(Object node) {
        if (node instanceof Report)
            return node.leaf
        else
            return true
    }

    @Override
    int getIndexOfChild(Object parent, Object child) {
        if (parent instanceof Report && child instanceof Report)
            return parent.children.indexOf(child)
        else
            return -1
    }

    @Override
    void valueForPathChanged(TreePath path, Object newValue) {}

    @Override
    void addTreeModelListener(TreeModelListener l) {}

    @Override
    void removeTreeModelListener(TreeModelListener l) {}
}

