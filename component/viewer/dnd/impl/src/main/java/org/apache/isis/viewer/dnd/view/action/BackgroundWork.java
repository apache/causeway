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

package org.apache.isis.viewer.dnd.view.action;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.core.commons.exceptions.IsisApplicationException;
import org.apache.isis.viewer.dnd.view.BackgroundTask;
import org.apache.isis.viewer.dnd.view.View;

public final class BackgroundWork {
    private static final Logger LOG = LoggerFactory.getLogger(BackgroundTask.class);

    private static class BackgroundThread extends Thread {
        private final View view;
        private final BackgroundTask task;

        public BackgroundThread(final View view, final BackgroundTask task) {
            super("nof-background");
            this.view = view;
            this.task = task;
            LOG.debug("creating background thread for task " + task);
        }

        @Override
        public void run() {
            try {
                view.getState().setActive();
                view.getFeedbackManager().setBusy(view, task);
                scheduleRepaint(view);

                LOG.debug("running background thread for task " + task);
                task.execute();

            } catch (final Throwable e) {
                if (!(e instanceof IsisApplicationException)) {
                    final String message = "Error while running background task " + task.getName();
                    LOG.error(message, e);
                }
                view.getFeedbackManager().showException(e);

            } finally {
                view.getState().setInactive();
                view.getFeedbackManager().clearBusy(view);
                scheduleRepaint(view);
            }
        }

        private static void scheduleRepaint(final View view) {
            view.markDamaged();
            view.getViewManager().scheduleRepaint();
        }

    }

    public static void runTaskInBackground(final View view, final BackgroundTask task) {
        final Thread t = new BackgroundThread(view, task);
        t.start();
    }

    private BackgroundWork() {
    }

}
