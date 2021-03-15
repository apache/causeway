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
package org.apache.isis.commons.internal.debug.xray.sequence;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.isis.commons.internal.base._Refs;
import org.apache.isis.commons.internal.base._Refs.IntReference;
import org.apache.isis.commons.internal.base._Strings;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;

public class SequenceDiagram {

    private final Map<String, String> aliases = new TreeMap<>();

    private final Map<String, Participant> participantsById = new LinkedHashMap<>();
    private final List<Connection> connections = new ArrayList<>();

    private Dimension size;

    public SequenceDiagram alias(String id, String label) {
        aliases.put(id, label);
        return this;
    }

    public void enter(String from, String to, String label) {
        val p0 = participantsById.computeIfAbsent(from, id->new Participant(aliases.getOrDefault(id, id)));
        val p1 = participantsById.computeIfAbsent(to, id->new Participant(aliases.getOrDefault(id, id)));
        connections.add(new Connection(p0, p1, label, false));
    }

    public void exit(String from, String to, String label) {
        val p1 = participantsById.computeIfAbsent(to, id->new Participant(aliases.getOrDefault(id, id)));
        val p0 = participantsById.computeIfAbsent(from, id->new Participant(aliases.getOrDefault(id, id)));
        connections.add(new Connection(p0, p1, label, true));
    }

    public void enter(String from, String to) {
        enter(from, to, null);
    }

    public void exit(String from, String to) {
        exit(from, to, null);
    }

    // -- RENDERING

    private final static Color COLOR_LIGHTER_GREEN = new Color(0xd5, 0xe8, 0xd4);
    private final static Color COLOR_DARKER_GREEN = new Color(0x82, 0xB3, 0x66);
    private final static Color COLOR_DARKER_RED = new Color(0xB2, 0x00, 0x00);

    private final static int CHAR_WIDTH = 6;
    private final static int CHAR_HEIGTH = 20;

    private final static Color PARTICIPANT_BACKGROUND_COLOR = COLOR_LIGHTER_GREEN;
    private final static Color PARTICIPANT_BORDER_COLOR = COLOR_DARKER_GREEN;
    private final static int PARTICIPANT_MARGIN_H = 20;
    private final static int PARTICIPANT_MARGIN_V = 5;
    private final static int PARTICIPANT_PADDING_V = 3;
    private final static int PARTICIPANT_PADDING_H = 8;
    private final static int PARTICIPANT_HEIGHT = 2*PARTICIPANT_PADDING_V + CHAR_HEIGTH;
    private final static int CONNECTION_MARGIN_V = 5;

    private final static BasicStroke STROKE_DEFAULT = new BasicStroke(1.0f);
    private final static BasicStroke STROKE_DASHED = new BasicStroke(1, 
            BasicStroke.CAP_BUTT, 
            BasicStroke.JOIN_ROUND, 
            1.0f, 
            new float[] { 2f, 0f, 2f },
            2f);

    @Getter @RequiredArgsConstructor
    private static class Connection {
        final Participant from;
        final Participant to;
        final String label;
        final boolean dashedLine;

        int y_top;
        int y_bottom;
        int height;

        int x_text;
        int y_text;

        void layout(IntReference y_offset) {
            y_top = y_offset.getValue() + CONNECTION_MARGIN_V;
            height = _Strings.isEmpty(label)
                    ? 10
                    : 40;
            y_bottom = y_top + height;
            x_text = Math.min(from.getX_middle(), to.getX_middle()) + 10;
            y_text = y_bottom - 8;

            y_offset.update(x->y_bottom);
        }
    }

    @Getter @RequiredArgsConstructor
    private static class Participant {
        final String label;
        int x_left;
        int x_middle;
        int x_right;
        int width;

        int y_top;
        int y_bottom;
        int height;

        int x_text;
        int y_text;

        void layout(IntReference x_offset) {
            x_left = x_offset.getValue();
            width = 2 * PARTICIPANT_PADDING_H 
                    + CHAR_WIDTH * label.length();
            x_right = x_left + width;
            x_middle = (x_left + x_right) >> 1;

            height = 2*PARTICIPANT_PADDING_V + CHAR_HEIGTH;
            y_top = PARTICIPANT_MARGIN_V;
            y_bottom = y_top + height;

            x_text = x_left + PARTICIPANT_PADDING_H;
            y_text = PARTICIPANT_MARGIN_V + PARTICIPANT_HEIGHT * 2 / 3;
            x_offset.update(x->x + width + PARTICIPANT_MARGIN_H);
        }
    }

    public Dimension layout() {
        
        val x_offset = _Refs.intRef(PARTICIPANT_MARGIN_H);
        val y_offset = _Refs.intRef(0);
        participantsById.values().stream()
                .peek(p->p.layout(x_offset))
                .forEach(p->y_offset.update(x->Math.max(x, p.getHeight())));

        final int width = x_offset.getValue();

        y_offset.update(x->x + PARTICIPANT_MARGIN_V);

        connections.stream()
        .forEach(c->c.layout(y_offset));

        final int height = y_offset.update(x->x + 2 * PARTICIPANT_MARGIN_V);
        
        return this.size = new Dimension(width, height);
    }
    
    public void render(Graphics2D g) {
        
      //      Font font = new Font("Sans", Font.PLAIN, 12);
      //                ((Graphics2D)g).setFont(font);

      participantsById.values().stream()
      .forEach(p->{

          g.setStroke(STROKE_DEFAULT);

          g.setColor(PARTICIPANT_BACKGROUND_COLOR);
          g.fillRect(p.getX_left(), p.getY_top(), p.getWidth(), p.getHeight());

          g.setColor(PARTICIPANT_BORDER_COLOR);
          g.drawRect(p.getX_left(), p.getY_top(), p.getWidth(), p.getHeight());

          g.setColor(Color.black);
          g.drawString(p.getLabel(), p.getX_text(), p.getY_text());

          g.setColor(PARTICIPANT_BORDER_COLOR);
          g.setStroke(STROKE_DASHED);
          g.drawLine(p.getX_middle(), p.getY_bottom(), p.getX_middle(), size.height - PARTICIPANT_MARGIN_V);
      });

      connections.stream()
      .forEach(c->{

          g.setColor(COLOR_DARKER_RED);

          g.setStroke(c.isDashedLine()
                  ? STROKE_DASHED
                  : STROKE_DEFAULT);

          final int y = c.getY_bottom();
          final int m0 = c.getFrom().getX_middle();
          final int m1 = c.getTo().getX_middle();
          final int dir = m1<m0 ? 1 : -1;

          g.drawLine(m0, y, m1, y);

          // arrow head

          ((Graphics2D)g).setStroke(STROKE_DEFAULT);
          for(int i=0; i<7; ++i) {
              g.drawLine(m1 + i*dir, y, m1 + 8 * dir, y - 3);
              g.drawLine(m1 + i*dir, y, m1 + 8 * dir, y + 3);
          }

          // text

          if(_Strings.isNotEmpty(c.getLabel())) {
              g.setColor(Color.black);
              g.drawString(c.getLabel(), c.getX_text(), c.getY_text());    
          }

      });
        
    }
 

}
