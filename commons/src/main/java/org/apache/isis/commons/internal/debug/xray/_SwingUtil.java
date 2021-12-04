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
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Collections;
import java.util.function.Consumer;

import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import lombok.val;
import lombok.experimental.UtilityClass;

@UtilityClass
final class _SwingUtil {

    JTable newTable(final Object[][] tableData, final String[] columnNames) {
        val table = new JTable(tableData, columnNames) {
            private static final long serialVersionUID = 1L;
            @Override
            public Component prepareRenderer(final TableCellRenderer renderer, final int row, final int column) {
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

    void setTreeExpandedState(final JTree tree, final boolean expanded) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getModel().getRoot();
        setNodeExpandedState(tree, node, expanded);
    }

    void setNodeExpandedState(final JTree tree, final DefaultMutableTreeNode node, final boolean expanded) {
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

    JPanel canvas(final Consumer<Graphics2D> onRender) {

        val canvas = new JPanel() {
            private static final long serialVersionUID = 1L;
            @Override
            public void paintComponent(final Graphics g) {
                onRender.accept((Graphics2D)g);
            }
        };

        return canvas;
    }

    public static Component verticalBox(final Component ...components) {

        val panel = new JPanel(new GridBagLayout());
        val gbc = new GridBagConstraints();
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.insets = new Insets(3, 6, 3, 6);

        for(val component : components) {
            panel.add(component, gbc);
        }

        return panel;
    }

}
