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


package org.apache.isis.viewer.dnd.viewer.basic;

import org.apache.isis.runtimes.dflt.runtime.context.IsisContext;
import org.apache.isis.runtimes.dflt.runtime.testsystem.ProxyJunit3TestCase;
import org.apache.isis.runtimes.dflt.runtime.testsystem.TestProxyConfiguration;
import org.apache.isis.viewer.dnd.DummyCanvas;
import org.apache.isis.viewer.dnd.DummyView;
import org.apache.isis.viewer.dnd.TestToolkit;
import org.apache.isis.viewer.dnd.drawing.Color;
import org.apache.isis.viewer.dnd.drawing.ColorsAndFonts;
import org.apache.isis.viewer.dnd.drawing.Text;
import org.apache.isis.viewer.dnd.view.Toolkit;
import org.apache.isis.viewer.dnd.view.text.TitleText;
import org.apache.isis.viewer.dnd.viewer.drawing.DummyText;


public class TitleTextTest extends ProxyJunit3TestCase {
    private TitleText titleText;
    private String title;
    private TestCanvas canvas;
    private DummyView view;

    public static void main(final String[] args) {
        junit.textui.TestRunner.run(TitleTextTest.class);
    }

    @Override
    protected void setUp() throws Exception {
        IsisContext.setConfiguration(new TestProxyConfiguration());
        TestToolkit.createInstance();

        view = new DummyView();
        final Text style = new DummyText();
        titleText = new TitleText(view, style, Toolkit.getColor(ColorsAndFonts.COLOR_BLACK)) {
            @Override
            protected String title() {
                return title;
            }
        };
        canvas = new TestCanvas();
    }

    // TODO these tests won't work on server that doesn't have graphics - eg a Linux box without X
/*
    public void XXtestDrawCanvas() {
        title = "abcde";
        titleText.draw(canvas, 10, 20);
    }

    public void testDrawCanvasDefaultColor() {
        titleText.draw(canvas, 10, 20);
        assertEquals(Toolkit.getColor(ColorsAndFonts.COLOR_BLACK), canvas.color);
    }

    public void testDrawCanvasCanDrop() {
        view.getState().setCanDrop();

        titleText.draw(canvas, 10, 20);
        assertEquals(Toolkit.getColor(ColorsAndFonts.COLOR_VALID), canvas.color);
    }

    public void testDrawCanvasCantDrop() {
        view.getState().setCantDrop();

        titleText.draw(canvas, 10, 20);
        assertEquals(ColorsAndFonts.COLOR_INVALID, canvas.color);
    }

    public void testDrawCanvasIdentifier() {
        view.getState().setContentIdentified();

        titleText.draw(canvas, 10, 20);
        assertEquals(Toolkit.getColor("primary1"), canvas.color);
    }
*/
    public void testDrawingLocation() {
        titleText.draw(canvas, 10, 20);
        assertEquals(10, canvas.x);
        assertEquals(20, canvas.y);
    }

    public void testDrawingText() {
        title = "test string";
        titleText.draw(canvas, 10, 20);
        assertEquals("test string", canvas.text);
    }

    public void testGetSize() {
        title = "abcde";

        assertEquals(10 * 5, titleText.getSize().getWidth());
        assertEquals(8, titleText.getSize().getHeight());
    }

}

class TestCanvas extends DummyCanvas {
    String text;
    int x;
    int y;
    Color color;

    @Override
    public void drawText(final String text, final int x, final int y, final Color color, final Text style) {
        this.text = text;
        this.x = x;
        this.y = y;
        this.color = color;
    }

}
