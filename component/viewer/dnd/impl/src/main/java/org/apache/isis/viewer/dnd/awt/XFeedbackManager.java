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

package org.apache.isis.viewer.dnd.awt;

import java.awt.Cursor;
import java.util.List;
import java.util.Vector;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.transaction.MessageBroker;
import org.apache.isis.viewer.dnd.view.BackgroundTask;
import org.apache.isis.viewer.dnd.view.Content;
import org.apache.isis.viewer.dnd.view.Feedback;
import org.apache.isis.viewer.dnd.view.ObjectContent;
import org.apache.isis.viewer.dnd.view.View;
import org.apache.isis.viewer.dnd.view.message.ExceptionMessageContent;
import org.apache.isis.viewer.dnd.view.message.TextMessageContent;

public class XFeedbackManager implements Feedback {
    private final XViewer viewer;
    private final Vector<ObjectAdapter> busy = new Vector<ObjectAdapter>();
    private String messages;
    private String view;
    private String action;
    private String error;
    private String message;
    private Cursor cursor;

    public XFeedbackManager(final XViewer viewer) {
        this.viewer = viewer;
    }

    @Override
    public String getStatusBarOutput() {
        final StringBuffer text = new StringBuffer();
        append(text, view);
        append(text, action);
        append(text, error);
        append(text, message);
        append(text, messages);
        return text.toString();

        // for debug
        // return "[view: " + view + "] [action: " + action + "] [error: " +
        // error + "] [message: " + message
        // + "] [messages:" + messages + "]";
    }

    private void append(final StringBuffer text, final String entry) {
        if (entry != null && !entry.equals("")) {
            if (text.length() > 0) {
                text.append(";  ");
            }
            text.append(entry);
        }
    }

    // REVIEW why can only objects be set to busy? Specifically the service icon
    // do not show as bust when a
    // long standing option is being set up when a menu is being created.
    @Override
    public void setBusy(final View view, final BackgroundTask task) {
        final Content content = view.getContent();
        if (content != null && content.isObject()) {
            final ObjectAdapter object = ((ObjectContent) content).getObject();
            busy.addElement(object);
        }
        showBusyState(view);

        message = "BUSY";
        // Don't force repaint here, else an infinite loop forms as the layout
    }

    @Override
    public void clearBusy(final View view) {
        if (view.getContent().isObject()) {
            final ObjectAdapter object = ((ObjectContent) view.getContent()).getObject();
            busy.removeElement(object);
            // showDefaultCursor();
        }
        showBusyState(view);

        if (busy.size() == 0) {
            message = "";
            viewer.forcePaintOfStatusBar();
        }
    }

    @Override
    public boolean isBusy(final View view) {
        if (view != null) {
            final Content content = view.getContent();
            if (content != null && content.isObject()) {
                final ObjectAdapter object = ((ObjectContent) content).getObject();
                if (busy.contains(object)) {
                    return true;
                }
            }
            final View parent = view.getParent();
            return parent != null && isBusy(parent);
        }
        return false;
    }

    @Override
    public void showBusyState(final View view) {
        Cursor cursor;
        if (isBusy(view)) {
            cursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
        } else {
            cursor = this.cursor;
        }
        viewer.setCursor(cursor);
    }

    @Override
    public void setViewDetail(final String text) {
        view = text;
        viewer.forcePaintOfStatusBar();
    }

    @Override
    public void addMessage(final String text) {
        message = text;
        viewer.forcePaintOfStatusBar();
    }

    @Override
    public void clearAction() {
        action = null;
        viewer.forcePaintOfStatusBar();
    }

    @Override
    public void setAction(final String text) {
        action = text;
        viewer.forcePaintOfStatusBar();
    }

    @Override
    public void setError(final String text) {
        error = text;
        viewer.forcePaintOfStatusBar();
    }

    @Override
    public void clearError() {
        error = null;
        viewer.forcePaintOfStatusBar();
    }

    @Override
    public void showMessagesAndWarnings() {
        this.messages = getMessageBroker().getMessagesCombined();

        // TODO this is common across viewers so should be in common code.
        final List<String> warnings = getMessageBroker().getWarnings();
        for (final String warning : warnings) {
            final TextMessageContent content = new TextMessageContent("Warning", warning);
            viewer.showDialog(content);
        }
    }

    private MessageBroker getMessageBroker() {
        return IsisContext.getMessageBroker();
    }

    @Override
    public void showException(final Throwable e) {
        final ExceptionMessageContent content = new ExceptionMessageContent(e);
        viewer.showDialog(content);
    }

    @Override
    public void showArrowCursor() {
        setCursor(Cursor.DEFAULT_CURSOR);
    }

    @Override
    public void showCrosshairCursor() {
        setCursor(Cursor.CROSSHAIR_CURSOR);
    }

    @Override
    public void showDefaultCursor() {
        setCursor(Cursor.DEFAULT_CURSOR);
    }

    @Override
    public void showHandCursor() {
        setCursor(Cursor.HAND_CURSOR);
    }

    @Override
    public void showMoveCursor() {
        setCursor(Cursor.MOVE_CURSOR);
    }

    @Override
    public void showResizeDownCursor() {
        setCursor(Cursor.S_RESIZE_CURSOR);
    }

    @Override
    public void showResizeDownLeftCursor() {
        setCursor(Cursor.SW_RESIZE_CURSOR);
    }

    @Override
    public void showResizeDownRightCursor() {
        setCursor(Cursor.SE_RESIZE_CURSOR);
    }

    @Override
    public void showResizeLeftCursor() {
        setCursor(Cursor.W_RESIZE_CURSOR);
    }

    @Override
    public void showResizeRightCursor() {
        setCursor(Cursor.E_RESIZE_CURSOR);
    }

    @Override
    public void showResizeUpCursor() {
        setCursor(Cursor.N_RESIZE_CURSOR);
    }

    @Override
    public void showResizeUpLeftCursor() {
        setCursor(Cursor.NW_RESIZE_CURSOR);
    }

    @Override
    public void showResizeUpRightCursor() {
        setCursor(Cursor.NE_RESIZE_CURSOR);
    }

    @Override
    public void showTextCursor() {
        setCursor(Cursor.TEXT_CURSOR);
    }

    private void setCursor(final int cursorStyle) {
        cursor = Cursor.getPredefinedCursor(cursorStyle);
        viewer.setCursor(cursor);
    }
}
