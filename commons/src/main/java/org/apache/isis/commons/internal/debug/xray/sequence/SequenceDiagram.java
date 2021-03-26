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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import org.apache.isis.commons.internal.base._Refs;
import org.apache.isis.commons.internal.base._Refs.IntReference;
import org.apache.isis.commons.internal.debug.xray.sequence._Graphics.TextBlock;

import lombok.Getter;
import lombok.NonNull;
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

    public void enter(final @NonNull String from, final @NonNull String to, String label) {
        val p0 = participantsById.computeIfAbsent(from, id->new Participant(aliases.getOrDefault(id, id)));
        val p1 = participantsById.computeIfAbsent(to, id->new Participant(aliases.getOrDefault(id, id)));
        connections.add(new Connection(p0, p1, label, false));
    }

    public void exit(final @NonNull String from, final @NonNull String to, String label) {
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

    private final static Color PARTICIPANT_BACKGROUND_COLOR = _Graphics.COLOR_LIGHTER_GREEN;
    private final static Color PARTICIPANT_BORDER_COLOR = _Graphics.COLOR_DARKER_GREEN;
    private final static int PARTICIPANT_MARGIN_H = 20;
    private final static int PARTICIPANT_MARGIN_V = 5;
    private final static int PARTICIPANT_PADDING_H = 8;
    private final static int PARTICIPANT_PADDING_V = 3;
    private final static int PARTICIPANT_LINEGAP = 0;
    private final static int PARTICIPANT_MAX_CHAR_PER_LINE = 26;
    private final static Optional<Font> PARTICIPANT_FONT = _Graphics.lookupFont("Verdana", 12.f);
    
    private final static int CONNECTION_MARGIN_V = 12;
    private final static int CONNECTION_LABEL_PADDING_H = 8;
    private final static int CONNECTION_LABEL_PADDING_V = 3;
    private final static int CONNECTION_LABEL_LINEGAP = 0;
    private final static Optional<Font> CONNECTION_FONT = _Graphics.lookupFont("Courier New", 11.f);

    @Getter @RequiredArgsConstructor
    private static class Connection {
        final Participant from;
        final Participant to;
        final String label;
        final boolean dashedLine;

        TextBlock textBlock;

        int y_top;
        int y_bottom;
        int height;

        void layout(Graphics2D g, IntReference y_offset) {
            y_top = y_offset.getValue() + CONNECTION_MARGIN_V;

            textBlock = new TextBlock(label, 
                    Math.min(from.getX_middle(), to.getX_middle()), 
                    y_top);

            val dim = textBlock.layout(g.getFontMetrics(), 
                    CONNECTION_LABEL_PADDING_H, 
                    CONNECTION_LABEL_PADDING_V, 
                    CONNECTION_LABEL_LINEGAP,
                    Integer.MAX_VALUE);

            height = dim.height;
            y_bottom = y_top + height;

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

        TextBlock textBlock;

        void layout(Graphics2D g, IntReference x_offset) {

            x_left = x_offset.getValue();
            y_top = PARTICIPANT_MARGIN_V;

            textBlock = new TextBlock(label, x_left, y_top);

            val dim = textBlock.layout(g.getFontMetrics(), 
                    PARTICIPANT_PADDING_H, 
                    PARTICIPANT_PADDING_V, 
                    PARTICIPANT_LINEGAP,
                    PARTICIPANT_MAX_CHAR_PER_LINE);

            width = dim.width;
            x_right = x_left + width;
            x_middle = (x_left + x_right) >> 1;

            height = dim.height;
            y_bottom = y_top + height;

            x_offset.update(x->x + width + PARTICIPANT_MARGIN_H);
        }
    }

    public Dimension layout(Graphics2D g) {

        PARTICIPANT_FONT.ifPresent(g::setFont);
        
        val x_offset = _Refs.intRef(PARTICIPANT_MARGIN_H);
        val y_offset = _Refs.intRef(0);
        participantsById.values().stream()
        .peek(p->p.layout(g, x_offset))
        .forEach(p->y_offset.update(x->Math.max(x, p.getHeight())));

        final int width = x_offset.getValue();

        y_offset.update(x->x + PARTICIPANT_MARGIN_V);

        CONNECTION_FONT.ifPresent(g::setFont);
        
        connections.stream()
        .forEach(c->c.layout(g, y_offset));

        final int height = y_offset.update(x->x + 2*PARTICIPANT_MARGIN_V);

        return this.size = new Dimension(width, height);
    }

    public void render(Graphics2D g) {

        _Graphics.enableTextAntialiasing(g);
        
        PARTICIPANT_FONT.ifPresent(g::setFont);

        participantsById.values().stream()
        .forEach(p->{

            // participant box

            g.setStroke(_Graphics.STROKE_DEFAULT);

            g.setColor(PARTICIPANT_BACKGROUND_COLOR);
            g.fillRect(p.getX_left(), p.getY_top(), p.getWidth(), p.getHeight());

            g.setColor(PARTICIPANT_BORDER_COLOR);
            g.drawRect(p.getX_left(), p.getY_top(), p.getWidth(), p.getHeight());

            // participant box label

            g.setColor(Color.black);
            p.getTextBlock().render(g);

            // participant vertical time line

            g.setColor(PARTICIPANT_BORDER_COLOR);
            g.setStroke(_Graphics.STROKE_DASHED);
            g.drawLine(p.getX_middle(), p.getY_bottom(), p.getX_middle(), size.height - PARTICIPANT_MARGIN_V);
        });

        CONNECTION_FONT.ifPresent(g::setFont);
        
        connections.stream()
        .forEach(c->{

            // connection arrow

            g.setColor(_Graphics.COLOR_DARKER_RED);

            g.setStroke(c.isDashedLine()
                    ? _Graphics.STROKE_DASHED
                            : _Graphics.STROKE_DEFAULT);

            _Graphics.arrowHorizontal(g, 
                    c.getFrom().getX_middle(), 
                    c.getTo().getX_middle(), 
                    c.getY_bottom());

            // connection label

            g.setColor(Color.black);
            c.getTextBlock().render(g);

        });

    }


}
