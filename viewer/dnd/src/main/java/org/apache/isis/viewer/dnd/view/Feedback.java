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

package org.apache.isis.viewer.dnd.view;

public interface Feedback {

    void showException(final Throwable e);

    void showArrowCursor();

    void showCrosshairCursor();

    void showDefaultCursor();

    void showTextCursor();

    void showHandCursor();

    void showMoveCursor();

    void showResizeDownCursor();

    void showResizeDownLeftCursor();

    void showResizeDownRightCursor();

    void showResizeLeftCursor();

    void showResizeRightCursor();

    void showResizeUpCursor();

    void showResizeUpLeftCursor();

    void showResizeUpRightCursor();

    void setBusy(final View view, BackgroundTask task);

    void clearBusy(final View view);

    boolean isBusy(View view);

    String getStatusBarOutput();

    void showMessagesAndWarnings();

    void setViewDetail(String string);

    void setAction(String actionText);

    void addMessage(String string);

    void setError(String string);

    void clearAction();

    void clearError();

    void showBusyState(View view);

}
