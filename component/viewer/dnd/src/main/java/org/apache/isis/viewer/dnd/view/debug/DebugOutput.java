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

package org.apache.isis.viewer.dnd.view.debug;

import java.awt.FileDialog;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.PrintJob;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.StringTokenizer;

import org.apache.isis.core.commons.debug.DebugString;
import org.apache.isis.core.commons.debug.DebuggableWithTitle;
import org.apache.isis.core.commons.exceptions.IsisException;

public class DebugOutput {
    private static final DateFormat FORMAT = new SimpleDateFormat("yyyyMMdd-HHmmss-SSS");
    private static final Font TEXT_FONT = new Font("SansSerif", Font.PLAIN, 10);
    private static final Font TITLE_FONT = new Font("SansSerif", Font.BOLD, 12);

    public static void print(final String title, final String text) {
        final Frame parent = new Frame();
        final PrintJob job = Toolkit.getDefaultToolkit().getPrintJob(parent, "Print " + title, new Properties());

        if (job != null) {
            final Graphics graphic = job.getGraphics();
            // Dimension pageSize = job.getPageDimension();

            if (graphic != null) {
                graphic.translate(10, 10);
                final int x = 50;
                int y = 50;

                graphic.setFont(TITLE_FONT);

                final int height = graphic.getFontMetrics().getAscent();
                final int width = graphic.getFontMetrics().stringWidth(title);
                graphic.drawRect(x - 10, y - 10 - height, width + 20, height + 20);

                graphic.drawString(title, x, y);

                y += graphic.getFontMetrics().getHeight();
                y += 20;

                graphic.setFont(TEXT_FONT);
                final StringTokenizer tk = new StringTokenizer(text, "\n\r");
                while (tk.hasMoreTokens()) {
                    final String line = tk.nextToken();
                    graphic.drawString(line, x, y);
                    y += graphic.getFontMetrics().getHeight();
                }

                graphic.dispose();
            }

            job.end();
        }
        parent.dispose();
    }

    /*
     * 
     * 
     * Frame frame = new Frame(); PrintJob job =
     * Toolkit.getDefaultToolkit().getPrintJob(frame, "Print object", null);
     * 
     * if (job != null) { Graphics pg = job.getGraphics(); Dimension pageSize =
     * job.getPageDimension();
     * 
     * if (pg != null) { pg.translate(LEFT, HEIGHT); pg.drawRect(0, 0,
     * pageSize.width - LEFT - 1, pageSize.height - HEIGHT - 1); view.print(new
     * PrintCanvas(pg, view)); pg.dispose(); }
     * 
     * job.end(); } frame.dispose();
     */

    public static void saveToClipboard(final String text) {
        final Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
        final StringSelection ss = new StringSelection(text);
        cb.setContents(ss, ss);
    }

    public static void saveToFile(final DebuggableWithTitle object) {
        final String dateStamp = FORMAT.format(new Date());
        final String fileName = object.getClass().getName() + "-" + dateStamp + ".txt";
        final DebugString text = new DebugString();
        object.debugData(text);
        final String title = object.debugTitle();

        saveToFile(new File(fileName), title, text.toString());
    }

    public static void saveToFile(final File file, final String title, final String text) {
        try {
            final PrintWriter writer = new PrintWriter(new FileWriter(file));
            writer.println(title);
            writer.println();
            writer.println(text.toString());
            writer.close();
        } catch (final IOException e) {
            throw new IsisException(e);
        }
    }

    public static void saveToFile(final String saveDialogTitle, final String title, final String text) {
        final Frame parent = new Frame();

        final FileDialog dialog = new FileDialog(parent, saveDialogTitle, FileDialog.SAVE);
        dialog.setVisible(true);
        final String file = dialog.getFile();
        final String dir = dialog.getDirectory();

        parent.dispose();

        saveToFile(new File(dir, file), title, text);
    }
}
