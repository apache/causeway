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
package org.apache.isis.core.commons.debug;

import java.io.PrintWriter;
import java.io.StringWriter;

public abstract class DebugHtmlStringAbstract implements DebugBuilder {

    private final boolean createPage;
    private int tableLevel;
    private boolean isOdd;
    private boolean titleShown;
    private boolean endLine;

    public DebugHtmlStringAbstract(final boolean createPage) {
        this.createPage = createPage;
    }

    @Override
    public void concat(final DebugBuilder debug) {
        appendHtml(debug.toString());
    }

    @Override
    public void append(final int number, final int width) {
        appendHtml(number + "");
    }

    @Override
    public void append(final Object object) {
        if (object instanceof DebuggableWithTitle) {
            DebuggableWithTitle d = (DebuggableWithTitle) object;
            appendTitle(d.debugTitle());
            d.debugData(this);
        } else {
            appendHtml(object.toString());
        }
    }

    @Override
    public void append(final Object object, final int width) {
        appendHtml(object.toString());
    }

    @Override
    public void appendln() {
        if (tableLevel > 0) {
            endLine = true;
        } else {
            appendHtml("<p></p>");
        }
    }

    @Override
    public void blankLine() {
        if (tableLevel > 0) {
            appendHtml(row() + "<td class=\"error\" colspan=\"2\" >blank line</td></tr>");
        } else {
            appendHtml("<p>blank line</p>");
        }
    }

    @Override
    public void appendln(final String label, final boolean value) {
        appendln(label, String.valueOf(value));
    }

    @Override
    public void appendln(final String label, final double value) {
        appendln(label, String.valueOf(value));
    }

    @Override
    public void appendln(final String label, final long value) {
        appendln(label, String.valueOf(value));
    }

    @Override
    public void appendAsHexln(final String label, final long value) {
        appendln(label, Long.toHexString(value));
    }

    @Override
    public void appendPreformatted(final String label, final String object) {
        final String value = object == null ? "null" : object.toString();
        appendln(label, "<pre>" + value + "</pre>");
    };

    @Override
    public void appendln(final String label, final Object object) {
        final String value = object == null ? "null" : object.toString();
        appendln(label, value);
    }

    @Override
    public void appendln(final String label, final Object[] object) {
        if (object.length == 0) {
            appendln(label, "empty array");
        } else {
            appendln(label, object[0]);
            for (int i = 1; i < object.length; i++) {
                appendHtml(row() + "<td></td><td>" + object[i] + "</td></tr>");
            }
        }
    }

    @Override
    public void startSection(final String title) {
        startTableIfNeeded(true);
        appendTitle(title);
    }

    @Override
    public void endSection() {
        endTableIfStarted();
    }

    @Override
    public void indent() {
        if (tableLevel > 0) {
            appendHtml(row() + "<td>indented</td><td>");
            startTableIfNeeded(true);
        }
    }

    @Override
    public void unindent() {
        if (tableLevel > 0) {
            endTableIfStarted();
            appendHtml("</td>");
        }
    }

    protected void header() {
        if (createPage) {
            appendHtml("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">");
            appendHtml("<html>");
            appendHtml("<head>");
            appendHtml("<title>Debug Details</title>");
            appendHtml("<style type=\"text/css\">");
            appendHtml("body { margin: 15px; }\n" + ".links { background: #ddd; font-size: 80%; padding-bottom:5px; }\n" + ".links > p { display: inline-block; }\n" + "td {vertical-align: top; margin-left: 15px;}\n" + "td.error {color: red; font-style: italic; }\n" + "td.code {white-space: pre; font-family: monospace;}\n"
                    + "th.title {text-align: left; padding: 0.3em 1em; font-style: italic; background: #AED1FF; }\n" + "td.label {width: 16em; text-align: right; padding-right: 1.5em; padding-top: 0.2em; font-size: 80%; font-weight: bold; }\n"
                    + "span.facet-type { font-weight: bold; padding-right: 10px; }\n");
            appendHtml("</style>");
            appendHtml("</head>");
            appendHtml("<body>");
        }
    }

    protected abstract void appendHtml(String html);

    protected void footer() {
        endTableIfStarted();
        if (createPage) {
            appendHtml("</body>");
            appendHtml("</html>");
        }
    }

    @Override
    public void appendException(final Throwable e) {
        appendTitle("Exception");
        final String message = e.getMessage();
        if (message != null) {
            appendHtml(row() + "<td class=\"error\" colspan=\"2\" >" + message + "</td></tr>");
        }
        causingException(e);
        appendHtml(row() + "<td class=\"code\" colspan=\"2\" ><pre>");
        final StringWriter stringWriter = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(stringWriter);
        e.printStackTrace(printWriter);
        appendHtml(stringWriter.toString());
        appendHtml("</pre></td></tr>");

    }

    private void causingException(final Throwable throwable) {
        final Throwable cause = throwable.getCause();
        if (cause != null && cause != throwable) {
            appendHtml(row() + "<td colspan=\"2\" >" + cause.getMessage() + "</td></tr>");
            causingException(cause);
        }
    }

    @Override
    public void appendTitle(final String title) {
        if (tableLevel > 0) {
            String className = titleShown ? "subtitle" : "title";
            appendHtml(row() + "<th class=\""+ className + "\" colspan=\"2\" >" + title + "</th></tr>");
            titleShown = true;
        } else {
            appendHtml("<h2>" + title + "</h2>");
        }
    }

    private void appendln(final String name, final String value) {
        startTableIfNeeded(false);
        appendHtml(row() + "<td class=\"label\">" + name + "</td><td>" + value + "</td></tr>");
    }

    private String row() {
        final String line = (isOdd ? "odd" : "even") + (endLine ? " end-line" : ""); 
        isOdd = !isOdd;
        endLine = false;
        return "<tr class=\"" + line + "\">";
    }

    private void startTableIfNeeded(final boolean b) {
        if (tableLevel == 0 || b) {
            appendHtml("<table class=\"debug\" summary=\"Debug details\" >");
            tableLevel++;
            titleShown = false;
        }
    }

    private void endTableIfStarted() {
        if (tableLevel > 0) {
            appendHtml("</table>");
            tableLevel--;
        }
    }

    @Override
    public void appendPreformatted(final String text) {
        appendln("<pre>" + text + "</pre>");
    }

    @Override
    public void appendln(final String text) {
        if (tableLevel > 0) {
            appendHtml(row() + "<td colspan=\"2\">" + text + "</td></tr>");
        } else {
            appendHtml("<p>" + text + "</p>");
        }
    }

    @Override
    public void close() {
        endTableIfStarted();
        doClose();
    }

    protected abstract void doClose();

}
