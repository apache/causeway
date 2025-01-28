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
package org.apache.causeway.commons.internal.debug.xray;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics2D;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import javax.swing.BorderFactory;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.apache.causeway.commons.functional.IndexedConsumer;
import org.apache.causeway.commons.internal.base._Refs;
import org.apache.causeway.commons.internal.base._Temporals;
import org.apache.causeway.commons.internal.debug.xray.XrayModel.HasIdAndLabel;
import org.apache.causeway.commons.internal.debug.xray.XrayModel.Stickiness;
import org.apache.causeway.commons.internal.debug.xray.graphics.SequenceDiagram;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.jspecify.annotations.NonNull;
import lombok.RequiredArgsConstructor;

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
        private final @NonNull Stickiness stickiness;

        @EqualsAndHashCode.Exclude
        private final String iconResource = "/xray/key-value.png";

        @Override
        public void render(final JScrollPane panel) {
            String[] columnNames = {"Key", "Value"};
            Object[][] tableData = new Object[data.size()][columnNames.length];

            var rowIndex = _Refs.intRef(0);

            data.forEach((k, v)->{
                var row = tableData[rowIndex.getValue()];
                rowIndex.incAndGet();
                row[0] = k;
                row[1] = v;
            });

            var table = _SwingUtil.newTable(tableData, columnNames);
            table.setFillsViewportHeight(true);

            panel.setViewportView(table);
        }
    }

    @Getter
    @EqualsAndHashCode(callSuper = false)
    @RequiredArgsConstructor
    public static class LogEntry extends XrayDataModel {

        @EqualsAndHashCode.Exclude
        private final List<StackTraceElement> data = new ArrayList<>();

        private final String id;
        private final LocalDateTime timestamp;
        @EqualsAndHashCode.Exclude
        private final String iconResource;
        private final String label;
        private final String logMessage;
        private final @NonNull Stickiness stickiness;

        @Override
        public void render(final JScrollPane panel) {

            var layout = new BorderLayout();
            var panel2 = new JPanel(layout);
            layout.setHgap(10);
            layout.setVgap(10);

            // log message label

            var logMessagePane = new JEditorPane();
            logMessagePane.setEditable(false);
            logMessagePane.setText(logMessage);

            var timestampLabel = new JLabel(timestamp.format(_Temporals.DEFAULT_LOCAL_DATETIME_FORMATTER_WITH_MILLIS));

            panel2.add(
                    _SwingUtil.verticalBox(
                            timestampLabel,
                            logMessagePane),
                    BorderLayout.NORTH);

            // table rendering

            String[] columnNames = {"", "StackTraceElement"};
            Object[][] tableData = new Object[data.size()][columnNames.length];

            var rowIndex = _Refs.intRef(0);

            data.forEach(IndexedConsumer.offset(1, (index, se)->{
                var row = tableData[rowIndex.getValue()];
                rowIndex.incAndGet();
                row[0] = index;
                row[1] = se.toString();
            }));

            var table = _SwingUtil.newTable(tableData, columnNames);
            table.setFillsViewportHeight(true);

            panel2.add(table, BorderLayout.CENTER);

            panel.setViewportView(panel2);
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
        private final @NonNull Stickiness stickiness;

        @EqualsAndHashCode.Exclude
        private final String iconResource = "/xray/sequence.png";

        private final static Color COLOR_SILVER = new Color(0xf5, 0xf5, 0xf5);
        private final static Color BACKGROUND_COLOR = COLOR_SILVER;
        private final static Color BORDER_COLOR = Color.GRAY;

        public Sequence(final String label) {
            this(UUID.randomUUID().toString(), label, Stickiness.CAN_DELETE_NODE);
        }

        public Sequence(final String id, final String label) {
            this(id, label, Stickiness.CAN_DELETE_NODE);
        }

        @Override
        public void render(final JScrollPane panel) {

            var canvas = _SwingUtil.canvas(g->{
                g.setColor(BACKGROUND_COLOR);
                g.fill(g.getClip());
                data.render(g);
            });

            var dim = data.layout((Graphics2D)panel.getGraphics());

            if(BORDER_COLOR!=null) {
                canvas.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
            }
            canvas.setPreferredSize(dim);

            panel.setViewportView(canvas);

        }
    }

}
