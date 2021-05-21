/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.isis.commons.internal.debug.xray;

import java.awt.Component;
import java.util.Collections;

import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import lombok.val;

final class _SwingUtil {

    static JTable newTable(Object[][] tableData, String[] columnNames) {
        val table = new JTable(tableData, columnNames) {
            private static final long serialVersionUID = 1L;
            @Override
               public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                   Component component = super.prepareRenderer(renderer, row, column);
                   int rendererWidth = component.getPreferredSize().width;
                   TableColumn tableColumn = getColumnModel().getColumn(column);
                   tableColumn.setPreferredWidth(Math.max(rendererWidth + getIntercellSpacing().width, tableColumn.getPreferredWidth()));
                   return component;
                }
            };
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        return table;
    }

    static void setTreeExpandedState(JTree tree, boolean expanded) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getModel().getRoot();
        setNodeExpandedState(tree, node, expanded);
    }

    @SuppressWarnings("unchecked")
    static void setNodeExpandedState(JTree tree, DefaultMutableTreeNode node, boolean expanded) {
        for (Object treeNode : Collections.list(node.children())) {
            setNodeExpandedState(tree, (DefaultMutableTreeNode) treeNode, expanded);
        }
        if (!expanded && node.isRoot()) {
            return;
        }
        val path = new TreePath(node.getPath());
        if (expanded) {
            tree.expandPath(path);
        } else {
            tree.collapsePath(path);
        }
    }

}
