package org.nakedobjects.xat.html;

import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.xat.AbstractDocumentor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.Date;


public class HtmlDocumentor extends AbstractDocumentor {
    private boolean isList = false;
    private PrintWriter writer;
    private File dir;

    public HtmlDocumentor(String directory) {
        dir = new File(directory);
        if(!dir.exists()) {
            dir.mkdirs();
        }
        
    }
    
    public void open(String className, String name) {
        try {
            String fileName = className.replace(' ', '_') + "-xat.html";
            if(writer == null) {
	            writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(new File(dir, fileName))));
	            String title = makeTitle(name);
	            writer.println("<html>");
	            writer.println("<head>");
	            writer.println("<style type=\"text/css\">");
	            writer.println("<!--");
	            writer.println("H1 {color: darkblue}");
	            writer.println("P{line-height: 150%}");
	            writer.println("OL LI {line-height: 150%; margin-bottom: 8pt}");
	            writer
	                    .println("EM  {background-color: #cccccc; border-left: solid #cccccc 3px; border-right: solid #cccccc 3px; border-top: solid #cccccc 1px; border-bottom: solid #cccccc 1px ; font-style: normal}");
	            writer
	                    .println("CODE  {background-color: #cccccc; border-top: solid #cccccc 1px; border-bottom: solid black 1px; font-family: sans-serif}");
	            writer.println("IMG  {padding-right: 4px}");
	            writer.println("-->");
	            writer.println("</style>");
	            writer.println("<title>" + title + "</title>");
	            writer.println("</head>");
	            writer.println("<body>");
	            writer.write("<h1>Documentation: " + title + "</h1>");
	            writer.write("<small>Generated " + DateFormat.getDateTimeInstance().format(new Date()) + "</small>");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        writer.println("</body>");
        writer.println("</html>");
        writer.close();
    }

    public void doc(String text) {
        if (isGenerating()) {
            writer.print(text);
        }
    }

    public void docln(String text) {
        if (isGenerating()) {
            doc(text + "\n");
        }
    }

    public void flush() {
        writer.flush();
    }

    public String objectString(Naked object) {
        String title = object.titleString();
        StringBuffer str = new StringBuffer();

        str.append("<b>" + object.getSpecification().getShortName() + "</b> object ");
        str.append("<em>");
        String name = object instanceof NakedObject ? ((NakedObject) object).getIconName() : object.getSpecification().getShortName();
        str.append("<img width=\"16\" height=\"16\" align=\"Center\" src=\"images/" + name + ".gif\">");
        str.append("<font face=\"sans-serif\">" + title + "</font>");
        str.append("</em>");
        return str.toString();
    }

    public String simpleObjectString(Naked object) {
        StringBuffer str = new StringBuffer();

        str.append("<em>");
        String name = object instanceof NakedObject ? ((NakedObject) object).getIconName() : object.getSpecification().getShortName();
        str.append("<img width=\"16\" height=\"16\" align=\"Center\" src=\"images/" + name + ".gif\">");
        str.append("<font face=\"sans-serif\">" + object.titleString() + "</font>");
        str.append("</em>");
        return str.toString();
    }

    public void step(String text) {
        if (isGenerating()) {
            text = text.trim();
            if (!isList) {
                docln("<ol>");
                isList = true;
            }
            docln("<li>" + text + (text.endsWith(".") ? " " : ".  "));
            flush();
        }
    }

    public void subtitle(String text) {
        if (isGenerating()) {
            if (isList) {
                docln("</ol>");
                isList = false;
            }
            if (!text.equals("")) {
                docln("<h3>" + text + "</h3>");
            }
        }
    }

    public void title(String text) {
        if (isGenerating()) {
            docln("<h2>" + text + "</h2>");
        }
    }
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business
 * objects directly to the user. Copyright (C) 2000 - 2003 Naked Objects Group
 * Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address
 * of Naked Objects Group is Kingsway House, 123 Goldworth Road, Woking GU21
 * 1NR, UK).
 */