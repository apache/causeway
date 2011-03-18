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


package org.apache.isis.viewer.scimpi.dispatcher.debug;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.isis.core.commons.debug.DebugString;
import org.apache.isis.viewer.scimpi.dispatcher.ScimpiException;


public class DebugView {
    private final PrintWriter writer;
    private final DebugString debug;

    public DebugView(PrintWriter writer, DebugString debug) {
        this.writer = writer;
        this.debug = debug;
    }
    
    public void header() {
        appendln("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">");
        appendln("<html>");
        appendln("<head>");
        appendln("<title>Debug Details</title>");
        appendln("<style type=\"text/css\">");
        appendln("body { margin: 15px; }\n" +
                "links { font-size: 80%; padding-bottom:5px; }\n" +  
                "td {vertical-align: top; margin-left: 15px;}\n" +  
                "td.error {color: red; font-style: italic; }\n" +  
                "td.code {white-space: pre; font-family: monospace;}\n" +  
                "th.title {text-align: left; padding: 0.3em 1em; font-style: italic; background: #AED1FF; }\n" +  
                "td.label {width: 14em; text-align: right; padding-right: 1.5em; padding-top: 0.2em; font-size: 80%; font-weight: bold; }\n" +  
                "span.facet-type { font-weight: bold; padding-right: 10px; }\n");
        appendln("</style>");
        appendln("</head>");
        appendln("<body>");
    }


    public void startTable() {
        writer.flush();
        writer.println("<table class=\"debug\" width=\"100%\" summary=\"Debug details\" >");
    }

    public void exception(Throwable e) {
        divider("Exception");
        String message = e instanceof  ScimpiException ? ((ScimpiException) e).getHtmlMessage() : e.getMessage();
        if (message != null) {
            writer.println("<tr><td class=\"error\" colspan=\"2\" >" + message + "<td></tr>");
        }
        causingException(e);
        writer.println("<tr><td class=\"code\" colspan=\"2\" >");
        e.printStackTrace(writer);
        writer.println("<td></tr>");
        
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        e.printStackTrace(printWriter);
        debug.appendln(stringWriter.toString());
    }

    private void causingException(Throwable throwable) {
        Throwable cause = throwable.getCause();
        if (cause != null && cause != throwable) {
            writer.println("<tr><td colspan=\"2\" >" + cause.getMessage() + "<td></tr>");
            debug.appendln(cause.getMessage());
            causingException(cause);
        }
    }

    public void divider(String title) {
        writer.println("<tr><th class=\"title\" colspan=\"2\" >" + title + "</th></tr>");
        debug.appendTitle(title);
    }

    public void endTable() {
        writer.println("</table>");
    }

    public void appendRow(String name, Object object) {
        String value = object == null ? "null" : object.toString();
        writer.println("<tr><td class=\"label\">" + name + "</td><td>" + value + "<td></tr>");
        debug.appendln(name, value);
    }

    public void appendRow(Object object) {
        writer.println("<tr><td colspan=\"2\">" + object.toString() + "<td></tr>"); 
        debug.appendln(object.toString());
    } 

    public void appendDebugTrace(String text) { 
        writer.println("<tr><td colspan=\"2\"><pre>" + text + "</pre><td></tr>"); 
    }

    public void appendln(Object object) {
        writer.println(object.toString());
    }

    public void footer() {
        appendln("</body>");
        appendln("</html>");
    }
}

