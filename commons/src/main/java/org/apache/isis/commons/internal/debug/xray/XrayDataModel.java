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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.apache.isis.commons.internal.base._Refs;
import org.apache.isis.commons.internal.debug.xray.XrayModel.HasIdAndLabel;
import org.apache.isis.commons.internal.debug.xray.sequence.SequenceDiagram;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;

public abstract class XrayDataModel extends HasIdAndLabel {

    public abstract void render(JScrollPane detailPanel);
    @Override
    public abstract String getId();
    @Override
    public abstract String getLabel();
    public abstract String getIconResource();

    // -- PREDEFINED DATA MODELS

    @Getter
    @EqualsAndHashCode(callSuper = false)
    @RequiredArgsConstructor
    public static class KeyValue extends XrayDataModel {

        @EqualsAndHashCode.Exclude
        private final Map<String, String> data = new TreeMap<>();

        private final String id;
        private final String label;

        @EqualsAndHashCode.Exclude
        private final String iconResource = "/xray/key-value.png";

        @Override
        public void render(final JScrollPane panel) {
            String[] columnNames = {"Key", "Value"};
            Object[][] tableData = new Object[data.size()][columnNames.length];

            val rowIndex = _Refs.intRef(0);

            data.forEach((k, v)->{
                val row = tableData[rowIndex.getValue()];
                rowIndex.incAndGet();
                row[0] = k;
                row[1] = v;
            });

            val table = _SwingUtil.newTable(tableData, columnNames);
            table.setFillsViewportHeight(true);

            panel.setViewportView(table);
        }
    }

    @Getter
    @EqualsAndHashCode(callSuper = false)
    @RequiredArgsConstructor
    public static class Sequence extends XrayDataModel {

        @EqualsAndHashCode.Exclude
        private final SequenceDiagram data = new SequenceDiagram();

        private final String id;
        private final String label;

        @EqualsAndHashCode.Exclude
        private final String iconResource = "/xray/sequence.png";

        private final static Color COLOR_SILVER = new Color(0xf5, 0xf5, 0xf5);
        private final static Color BACKGROUND_COLOR = COLOR_SILVER;
        private final static Color BORDER_COLOR = Color.GRAY;

        public Sequence(final String label) {
            this(UUID.randomUUID().toString(), label);
        }

        @Override
        public void render(final JScrollPane panel) {

            val dim = data.layout((Graphics2D)panel.getGraphics());

            val canvas = new JPanel() {
                private static final long serialVersionUID = 1L;

                @Override
                public void paintComponent(final Graphics _g) {

                    val g = (Graphics2D)_g;

                    g.setColor(BACKGROUND_COLOR);
                    g.fillRect(0, 0, getWidth(), getHeight());

                    data.render(g);
                  }
            };

            if(BORDER_COLOR!=null) {
                canvas.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
            }
            canvas.setPreferredSize(dim);

            panel.setViewportView(canvas);

        }


    }

}
