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
package org.apache.isis.commons.internal.debug.xray.graphics;

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

import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.base._Refs;
import org.apache.isis.commons.internal.base._Refs.IntReference;
import org.apache.isis.commons.internal.debug.xray.graphics._Graphics.TextBlock;
import org.apache.isis.commons.internal.primitives._Ints;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

public class SequenceDiagram {

    private final Map<String, String> aliases = new TreeMap<>();

    private final Map<String, Participant> participantsById = new LinkedHashMap<>();
    private final List<Connection> connections = new ArrayList<>();
    private final List<Lifeline> lifelines = new ArrayList<>();

    private Dimension size;

    public SequenceDiagram alias(final String id, final String label) {
        aliases.put(id, label);
        return this;
    }

    public void enter(final @NonNull String from, final @NonNull String to, final String label) {
        val p0 = participant(from);
        val p1 = participant(to);
        connections.add(newConnection(p0, p1, label, false));
    }

    public void exit(final @NonNull String from, final @NonNull String to, final String label) {
        val p1 = participant(to);
        val p0 = participant(from);
        connections.add(newConnection(p0, p1, label, true));
    }

    public void enter(final String from, final String to) {
        enter(from, to, null);
    }

    public void exit(final String from, final String to) {
        exit(from, to, null);
    }

    public void activate(final String participantId) {
        val participant = participant(participantId);
        val latestConnection = latestConnection();
        lifelines.add(new Lifeline(participant, latestConnection));
    }

    public void deactivate(final String participantId) {
        val participant = participant(participantId);
        val latestConnection = latestConnection();
        Can.ofCollection(lifelines).reverse().stream()
        .filter(lifeline->lifeline.getParticipant().equals(participant))
        .findFirst()
        .ifPresent(lifeline->lifeline.endAt = latestConnection);
    }

    // -- STYLE OVERRIDE

    private Color connectionArrowColor;
    private Color connectionLabelColor;

    public void setConnectionArrowColor(final Color connectionArrowColor) {
        this.connectionArrowColor = connectionArrowColor;
    }

    public void setConnectionLabelColor(final Color connectionLabelColor) {
        this.connectionLabelColor = connectionLabelColor;
    }

    // -- HELPER

    private Connection newConnection(
            final Participant from,
            final Participant to,
            final String label,
            final boolean dashedLine) {
        return new Connection(
                connections.size(),
                from,
                to,
                label,
                dashedLine,
                getConnectionArrowColor(),
                getConnectionLabelColor());
    }

    private Participant participant(final String participantId) {
        return participantsById
                .computeIfAbsent(participantId, id->new Participant(aliases.getOrDefault(id, id)));
    }

    private Connection latestConnection() {
        return Can.ofCollection(connections).getLast().orElse(null);
    }

    private Color getConnectionArrowColor() {
        return connectionArrowColor!=null
                ? connectionArrowColor
                : CONNECTION_ARROW_COLOR;
    }

    private Color getConnectionLabelColor() {
        return connectionLabelColor!=null
                ? connectionLabelColor
                : CONNECTION_LABEL_COLOR;
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

    private final static Color LIFELINE_BACKGROUND_COLOR = Color.WHITE;
    private final static int LIFELINE_WIDTH = 8;

    private final static Color CONNECTION_ARROW_COLOR = _Graphics.COLOR_DARKER_RED;
    private final static Color CONNECTION_LABEL_COLOR = Color.BLACK;
    private final static int CONNECTION_MARGIN_V = 12;
    private final static int CONNECTION_LABEL_PADDING_H = 8;
    private final static int CONNECTION_LABEL_PADDING_V = 3;
    private final static int CONNECTION_LABEL_LINEGAP = 0;
    private final static Optional<Font> CONNECTION_FONT = _Graphics.lookupFont("Courier New", 11.f);

    @Getter @RequiredArgsConstructor
    private static class Connection {
        final int index;
        final Participant from;
        final Participant to;
        final String label;
        final boolean dashedLine;

        final Color arrowColor;
        final Color labelColor;

        TextBlock textBlock;

        int x_left;
        int x_from;
        int x_to;

        int y_top;
        int y_bottom;
        int height;

        void layout(final Graphics2D g, final IntReference y_offset, final List<Lifeline> lifelines) {

            x_from = from.getX_middle();
            x_to = to.getX_middle();

            val fromConnectsLifeline = lifelines.stream()
                    .filter(ll->ll.getParticipant().equals(from))
                    .anyMatch(ll->ll.overlaps(this));

            val toConnectsLifeline = lifelines.stream()
                    .filter(ll->ll.getParticipant().equals(to))
                    .anyMatch(ll->ll.overlaps(this));

            final int dir = from.getX_middle() < to.getX_middle()
                    ? 1
                    : -1;
            if(fromConnectsLifeline) {
                x_from+= dir * LIFELINE_WIDTH / 2;
            }
            if(toConnectsLifeline) {
                x_to-= dir * LIFELINE_WIDTH / 2;
            }

            x_left = Math.min(x_from, x_to);

            y_top = y_offset.getValue() + CONNECTION_MARGIN_V;

            textBlock = new TextBlock(label,
                    x_left,
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

        void layout(final Graphics2D g, final IntReference x_offset) {

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

    @Getter @RequiredArgsConstructor
    private static class Lifeline {
        final @NonNull Participant participant;
        final Connection startAt;
        Connection endAt;

        int x_left;
        int x_right;
        int width;

        int y_top;
        int y_bottom;
        int height;

        void layout(final Graphics2D g, final int min_y, final int max_y) {

            width = LIFELINE_WIDTH;
            x_left = participant.getX_middle() - LIFELINE_WIDTH / 2;
            x_right = x_left + width;

            y_top = startAt !=null
                    ? startAt.y_bottom
                    : min_y;
            y_bottom = endAt !=null
                    ? endAt.y_bottom
                    : max_y;

            height = y_bottom - y_top;
        }

        public boolean overlaps(final Connection connection) {
            val lowerBound = _Ints.Bound.inclusive(startAt != null
                    ? startAt.index
                    : -1);
            val upperBound = _Ints.Bound.inclusive(endAt !=null
                    ? endAt.index
                    : Integer.MAX_VALUE);
            return _Ints.Range.of(lowerBound, upperBound).contains(connection.index);
        }
    }


    public Dimension layout(final Graphics2D g) {

        PARTICIPANT_FONT.ifPresent(g::setFont);

        val x_offset = _Refs.intRef(PARTICIPANT_MARGIN_H);
        val y_offset = _Refs.intRef(0);

        participantsById.values().stream()
        .peek(p->p.layout(g, x_offset))
        .forEach(p->y_offset.update(x->Math.max(x, p.getHeight())));

        final int width = x_offset.getValue();

        y_offset.update(x->x + PARTICIPANT_MARGIN_V);

        final int y_offset_first_con = y_offset.getValue();

        CONNECTION_FONT.ifPresent(g::setFont);

        connections.stream()
        .forEach(c->c.layout(g, y_offset, lifelines));

        final int y_offset_last_con = y_offset.getValue();

        final int height = y_offset.update(x->x + 2*PARTICIPANT_MARGIN_V);
        this.size = new Dimension(width, height);

        lifelines.stream()
        .forEach(ll->ll.layout(g, y_offset_first_con - 3, y_offset_last_con + 3));

        return this.size;
    }

    public void render(final Graphics2D g) {

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


        g.setStroke(_Graphics.STROKE_DEFAULT);

        lifelines.stream()
        .forEach(ll->{

            // lifeline box

            g.setColor(LIFELINE_BACKGROUND_COLOR);
            g.fillRect(ll.getX_left(), ll.getY_top(), ll.getWidth(), ll.getHeight());

            g.setColor(PARTICIPANT_BORDER_COLOR);
            g.drawRect(ll.getX_left(), ll.getY_top(), ll.getWidth(), ll.getHeight());
        });

        CONNECTION_FONT.ifPresent(g::setFont);

        connections.stream()
        .forEach(c->{

            // connection arrow

            g.setColor(c.getArrowColor());

            g.setStroke(c.isDashedLine()
                    ? _Graphics.STROKE_DASHED
                    : _Graphics.STROKE_DEFAULT);

            _Graphics.arrowHorizontal(g,
                    c.getX_from(),
                    c.getX_to(),
                    c.getY_bottom());

            // connection label

            g.setColor(c.getLabelColor());
            c.getTextBlock().render(g);

        });

    }

}
