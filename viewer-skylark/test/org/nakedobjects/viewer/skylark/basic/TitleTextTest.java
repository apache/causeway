package org.nakedobjects.viewer.skylark.basic;

import org.nakedobjects.viewer.skylark.Color;
import org.nakedobjects.viewer.skylark.DummyCanvas;
import org.nakedobjects.viewer.skylark.DummyText;
import org.nakedobjects.viewer.skylark.Text;
import org.nakedobjects.viewer.skylark.View;
import org.nakedobjects.viewer.skylark.core.DummyView;

import junit.framework.TestCase;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import test.org.nakedobjects.object.TestSystem;


public class TitleTextTest extends TestCase {
    private TitleText titleText;
    private String title;
    private TestSystem system;
    private TestCanvas canvas;
    private DummyView view;

    public static void main(String[] args) {
        junit.textui.TestRunner.run(TitleTextTest.class);
    }

    protected void setUp() throws Exception {
        Logger.getRootLogger().setLevel(Level.OFF);

        system = new TestSystem();
        system.init();
        
        view = new DummyView();
        Text style = new DummyText();
        titleText = new TitleText(view, style) {
            protected String title() {
                return title;
            }
        };
        canvas = new TestCanvas();
    }

    protected void tearDown() throws Exception {
        system.shutdown();
    }
    
    // TODO these tests won't work on server that doesn't have graphics - eg a Linux box without X
/*
    public void XXtestDrawCanvas() {
        title = "abcde";
        
        TestCanvas canvas = new TestCanvas() ;
           
        
        titleText.draw(canvas, 10, 20);
    }

    public void testDrawCanvasDefaultColor() {
        titleText.draw(canvas, 10, 20);
        assertEquals(Style.BLACK, canvas.color);
    }
    
    public void testDrawCanvasCanDrop() {
        view.getState().setCanDrop();
        
        titleText.draw(canvas, 10, 20);
        assertEquals(Style.VALID, canvas.color);
    }

    public void testDrawCanvasCantDrop() {
        view.getState().setCantDrop();
        
        titleText.draw(canvas, 10, 20);
        assertEquals(Style.INVALID, canvas.color);
    }
    
    public void testDrawCanvasIdentifier() {
        view.getState().setObjectIdentified();
        
        titleText.draw(canvas, 10, 20);
        assertEquals(Style.PRIMARY1, canvas.color);
    }
    
    
    public void testDrawingLocation() {
        titleText.draw(canvas, 10, 20);
        assertEquals(10 + View.HPADDING, canvas.x);
        assertEquals(20, canvas.y);
    }
    
    public void testDrawingText() {
        title = "test string";
        titleText.draw(canvas, 10, 20);
        assertEquals("test string", canvas.text);
    }
    
    
    public void testDrawingTextTruncated() {
        /*
         * Word boundaries at 4, 11, 16, 21, 24 & 34
         * /
        title = "test string that will be truncated";
    
        titleText.draw(canvas, 10, 20, 340);
        assertEquals("test string that will be truncated", canvas.text);

        titleText.draw(canvas, 10, 20, 339);
        assertEquals("test string that will be...", canvas.text);

        titleText.draw(canvas, 10, 20, 210 + 30);
        assertEquals("test string that will...", canvas.text);
        
        titleText.draw(canvas, 10, 20, 199 + 30);
        assertEquals("test string that...", canvas.text);
        
        titleText.draw(canvas, 10, 20, 140);
        assertEquals("test string...", canvas.text);

        titleText.draw(canvas, 10, 20, 139);
        assertEquals("test...", canvas.text);
                
        titleText.draw(canvas, 10, 20, 70);
        assertEquals("test...", canvas.text);
     
        titleText.draw(canvas, 10, 20, 60);
        assertEquals("tes...", canvas.text);
    }
    

    
    public void testDrawingTextTruncatedBeforeCommasEtc() {
        title = "test string, that? is truncated";
        
        titleText.draw(canvas, 10, 20, 210);
        assertEquals("test string, that...", canvas.text);
   
        titleText.draw(canvas, 10, 20, 199);
        assertEquals("test string...", canvas.text);
    }
    */
    

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

    public void drawText(String text, int x, int y, Color color, Text style) {
       this.text = text; 
       this.x = x;
       this.y = y;
       this.color = color;     
    }
    
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business objects directly to the
 * user. Copyright (C) 2000 - 2005 Naked Objects Group Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 * 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address of Naked Objects
 * Group is Kingsway House, 123 Goldworth Road, Woking GU21 1NR, UK).
 */