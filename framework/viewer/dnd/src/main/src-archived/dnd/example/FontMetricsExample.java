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


package org.apache.isis.viewer.dnd.example;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.font.LineMetrics;


public class FontMetricsExample extends Frame {

    public static void main(final String[] args) {
        final FontMetricsExample frame = new FontMetricsExample();
        frame.setSize(400, 400);
        frame.show();
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(final WindowEvent e) {
                frame.dispose();
            }
        });
        
        String fonts[] = Toolkit.getDefaultToolkit().getFontList();
        for (int i = 0; i < fonts.length; i++) {
            System.out.println(fonts[i].toString());
        }
    }

    final int left = 40 + getInsets().left;
    final int top = 60 + getInsets().top;
    final int width = 300;

    public void show() {
        super.show();
 //       left = 10 + getInsets().left;
  //      top = 10 + getInsets().top;
    }

    public void paint(final Graphics g) {
        System.out.println("\nfont size: height = leading + ascent + descent\n   max_ascent + max_descent");
        int sizes[] = new int[] { 10, 12, 16, 24, 26, 48, 60 };
        int y = top;
        for (int i = 0; i < sizes.length; i++) {
            y += drawText(g, y, sizes[i]);
        }
    }

    private int drawText(final Graphics g, final int top, final int size) {
        Font font = new Font("sansserif", 0, size);
        g.setFont(font);
        FontMetrics fm = g.getFontMetrics();

        int lineHeight = fm.getHeight();
        int baselineOffset = fm.getLeading() + fm.getAscent();

        g.setColor(Color.LIGHT_GRAY);
        g.drawRect(left - 10, top, width + 20, lineHeight - 1);

        g.setColor(Color.LIGHT_GRAY);
        g.drawLine(left - 10, top + baselineOffset, left + 10 + width, top + baselineOffset);

        g.setColor(Color.BLUE);
        g.drawLine(left, top + fm.getLeading(), left + width, top + fm.getLeading());

        g.setColor(Color.GREEN);
        g.drawLine(left, top + baselineOffset + fm.getDescent(), left + width, top + baselineOffset + fm.getDescent());
        
        g.setColor(Color.RED);
        g.drawLine(left, top + baselineOffset - fm.getAscent() - 1, left + width, top + baselineOffset - fm.getAscent() -1);
        
        g.setColor(Color.ORANGE);
        g.drawLine(left, top + fm.getDescent(), left + width, top +   fm.getDescent());

        
        g.setColor(Color.BLACK);
        g.drawString("Xy Æ \u00c3", left, top + baselineOffset);

        System.out.println(font.getFontName() + "  " + size + ": " + fm.getHeight() + " = " + fm.getLeading() + " + " + fm.getAscent() + " + "
                + fm.getDescent());
        System.out.println("    " + fm.getMaxAscent() + " + " + fm.getMaxDescent());

        LineMetrics lm = fm.getLineMetrics("test", g);
        
        System.out.println("    " + lm.getLeading() + "    " + lm.getAscent() + " + " + lm.getDescent());

        return fm.getHeight() + 14;
    }
}
