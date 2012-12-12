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

package org.apache.isis.viewer.html.context;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.commons.exceptions.UnknownTypeException;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.viewer.html.component.Block;
import org.apache.isis.viewer.html.component.Component;

public class ObjectHistory implements Iterable<HistoryEntry> {

    private static final Logger LOG = Logger.getLogger(ObjectHistory.class);
    private static final int MAX = 8;
    private final List<HistoryEntry> history = new ArrayList<HistoryEntry>();

    private void add(final HistoryEntry entry) {
        history.remove(entry);
        history.add(entry);
        LOG.debug("added to history: " + entry);
        if (history.size() > MAX) {
            LOG.debug("purging from history: " + history.get(0));
            history.remove(0);
        }
    }

    public void debug(final DebugBuilder debug) {
        for (int i = history.size() - 1; i >= 0; i--) {
            final HistoryEntry object = history.get(i);
            debug.appendln(object.toString());
        }
    }

    public void listObjects(final Context context, final Block navigation) {
        final Block taskBar = context.getComponentFactory().createBlock("history", null);
        taskBar.add(context.getComponentFactory().createHeading("History"));
        for (int i = history.size() - 1; i >= 0; i--) {
            try {
                final HistoryEntry item = history.get(i);
                Component icon;
                if (item.type == HistoryEntry.OBJECT) {
                    final ObjectAdapter object = context.getMappedObject(item.id);

                    IsisContext.getPersistenceSession().resolveImmediately(object);

                    icon = context.getComponentFactory().createObjectIcon(object, item.id, "item");
                } else if (item.type == HistoryEntry.COLLECTION) {
                    final ObjectAdapter object = context.getMappedCollection(item.id);
                    icon = context.getComponentFactory().createCollectionIcon(object, item.id);
                } else {
                    throw new UnknownTypeException(item);
                }
                taskBar.add(icon);
            } catch (final IsisException e) { // Catch resolveImmediately
                                              // exception when object is
                                              // deleted.

            }
        }
        navigation.add(taskBar);
    }

    public void addObject(final String idString) {
        add(new HistoryEntry(idString, HistoryEntry.OBJECT));
    }

    public void addCollection(final String idString) {
        add(new HistoryEntry(idString, HistoryEntry.COLLECTION));
    }

    public Iterator<HistoryEntry> iterator() {
        return history.iterator();
    }

    public void remove(final String existingId) {
        for (final HistoryEntry entry : history) {
            if (entry.id.equals(existingId)) {
                history.remove(entry);
                break;
            }
        }

    }
}
