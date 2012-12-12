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

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.core.commons.debug.DebuggableWithTitle;
import org.apache.isis.core.metamodel.spec.ActionType;
import org.apache.isis.core.runtime.sysout.SystemPrinter;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.viewer.dnd.drawing.Location;
import org.apache.isis.viewer.dnd.view.MenuOptions;
import org.apache.isis.viewer.dnd.view.Toolkit;
import org.apache.isis.viewer.dnd.view.UserActionSet;
import org.apache.isis.viewer.dnd.view.View;
import org.apache.isis.viewer.dnd.view.Workspace;
import org.apache.isis.viewer.dnd.view.option.UserActionAbstract;

public class DebugOptions implements MenuOptions {
    private final XViewer viewer;

    public DebugOptions(final XViewer viewer) {
        this.viewer = viewer;
    }

    @Override
    public void menuOptions(final UserActionSet options) {
        final String showExplorationMenu = "Always show exploration menu " + (viewer.showExplorationMenuByDefault ? "off" : "on");
        options.add(new UserActionAbstract(showExplorationMenu, ActionType.DEBUG) {
            @Override
            public void execute(final Workspace workspace, final View view, final Location at) {
                viewer.showExplorationMenuByDefault = !viewer.showExplorationMenuByDefault;
                view.markDamaged();
            }
        });

        final String repaint = "Show painting area  " + (viewer.showRepaintArea ? "off" : "on");
        options.add(new UserActionAbstract(repaint, ActionType.DEBUG) {
            @Override
            public void execute(final Workspace workspace, final View view, final Location at) {
                viewer.showRepaintArea = !viewer.showRepaintArea;
                view.markDamaged();
            }
        });

        final String debug = "Debug graphics " + (Toolkit.debug ? "off" : "on");
        options.add(new UserActionAbstract(debug, ActionType.DEBUG) {
            @Override
            public void execute(final Workspace workspace, final View view, final Location at) {
                Toolkit.debug = !Toolkit.debug;
                view.markDamaged();
            }
        });

        final String action = viewer.isShowingMouseSpy() ? "Hide" : "Show";
        options.add(new UserActionAbstract(action + " mouse spy", ActionType.DEBUG) {
            @Override
            public void execute(final Workspace workspace, final View view, final Location at) {
                viewer.setShowMouseSpy(!viewer.isShowingMouseSpy());
            }
        });

        // I've commented this out because in the new design we should close the
        // ExecutionContext
        // and then re-login.
        // options.add(new AbstractUserAction("Restart object loader/persistor",
        // UserAction.DEBUG) {
        // @Override
        // public void execute(final Workspace workspace, final View view, final
        // Location at) {
        // IsisContext.getObjectPersistor().reset();
        // }
        // });

        options.add(new UserActionAbstract("Diagnostics...", ActionType.DEBUG) {
            @Override
            public void execute(final Workspace workspace, final View view, final Location at) {
                final InfoDebugFrame f = new InfoDebugFrame();
                final DebuggableWithTitle info = new DebuggableWithTitle() {

                    @Override
                    public void debugData(final DebugBuilder debug) {
                        final ByteArrayOutputStream out2 = new ByteArrayOutputStream();
                        final PrintStream out = new PrintStream(out2);
                        new SystemPrinter(out).printDiagnostics();
                        debug.append(out2.toString());
                    }

                    @Override
                    public String debugTitle() {
                        return "Diagnostics";
                    }

                };
                f.setInfo(info);
                f.show(at.getX() + 50, workspace.getBounds().getY() + 6);
            }
        });

        options.add(new UserActionAbstract("Debug system...", ActionType.DEBUG) {
            @Override
            public void execute(final Workspace workspace, final View view, final Location at) {
                final InfoDebugFrame f = new InfoDebugFrame();
                final DebuggableWithTitle[] contextInfo = IsisContext.debugSystem();
                f.setInfo(contextInfo);
                f.show(at.getX() + 50, workspace.getBounds().getY() + 6);
            }
        });

        options.add(new UserActionAbstract("Debug session...", ActionType.DEBUG) {
            @Override
            public void execute(final Workspace workspace, final View view, final Location at) {
                final InfoDebugFrame f = new InfoDebugFrame();
                final DebuggableWithTitle[] contextInfo = IsisContext.debugSession();
                f.setInfo(contextInfo);
                f.show(at.getX() + 50, workspace.getBounds().getY() + 6);
            }
        });

        options.add(new UserActionAbstract("Debug viewer...", ActionType.DEBUG) {
            @Override
            public void execute(final Workspace workspace, final View view, final Location at) {
                final InfoDebugFrame f = new InfoDebugFrame();
                f.setInfo(new DebuggableWithTitle[] { Toolkit.getViewFactory(), viewer.updateNotifier });
                f.show(at.getX() + 50, workspace.getBounds().getY() + 6);
            }
        });

        options.add(new UserActionAbstract("Debug overlay...", ActionType.DEBUG) {
            @Override
            public void execute(final Workspace workspace, final View view, final Location at) {
                final DebugFrame f = new OverlayDebugFrame(viewer);
                f.show(at.getX() + 50, workspace.getBounds().getY() + 6);
            }
        });

    }

}
