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

package org.apache.isis.runtimes.dflt.runtime.transaction.updatenotifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.core.commons.debug.DebuggableWithTitle;
import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.commons.lang.ToString;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ResolveState;

public class UpdateNotifierDefault extends UpdateNotifierAbstract implements DebuggableWithTitle {

    private static final Logger LOG = Logger.getLogger(UpdateNotifierDefault.class);
    private final List<ObjectAdapter> changes = new ArrayList<ObjectAdapter>();
    private final List<ObjectAdapter> disposals = new ArrayList<ObjectAdapter>();

    // //////////////////////////////////////////////////
    // Constructor
    // //////////////////////////////////////////////////

    public UpdateNotifierDefault() {
        // does nothing
    }

    // //////////////////////////////////////////////////
    // Changed Objects
    // //////////////////////////////////////////////////

    @Override
    public synchronized void addChangedObject(final ObjectAdapter adapter) {
        if (!adapter.isResolved() && !adapter.representsTransient()) {
            return;
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("mark as changed " + adapter);
        }
        if (!changes.contains(adapter)) {
            changes.add(adapter);
        }
    }

    @Override
    public List<ObjectAdapter> getChangedObjects() {
        if (changes.size() > 0) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("dirty (changed) objects " + changes);
            }
        }
        final List<ObjectAdapter> changedObjects = new ArrayList<ObjectAdapter>();
        changedObjects.addAll(changes);

        changes.clear();

        return Collections.unmodifiableList(changedObjects);
    }

    // //////////////////////////////////////////////////
    // Disposed Objects
    // //////////////////////////////////////////////////

    @Override
    public void addDisposedObject(final ObjectAdapter adapter) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("mark as disposed " + adapter);
        }
        if (!disposals.contains(adapter)) {
            disposals.add(adapter);
        }
    }

    @Override
    public List<ObjectAdapter> getDisposedObjects() {
        if (disposals.size() > 0) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("dirty (disposed) objects " + disposals);
            }
        }
        final List<ObjectAdapter> disposedObjects = new ArrayList<ObjectAdapter>();
        disposedObjects.addAll(disposals);

        disposals.clear();

        return Collections.unmodifiableList(disposedObjects);
    }

    // //////////////////////////////////////////////////
    // Empty, Clear
    // //////////////////////////////////////////////////

    @Override
    public void ensureEmpty() {
        if (changes.size() > 0) {
            throw new IsisException("Update notifier still has updates");
        }
    }

    @Override
    public void clear() {
        changes.clear();
        disposals.clear();
    }

    // //////////////////////////////////////////////////
    // Debugging
    // //////////////////////////////////////////////////

    @Override
    public void debugData(final DebugBuilder debug) {
        debug.appendln("Changes");
        debugList(debug, changes);

        debug.appendln("Disposals");
        debugList(debug, disposals);
    }

    @Override
    public String debugTitle() {
        return "Simple Update Notifier";
    }

    private void debugList(final DebugBuilder debug, final List<ObjectAdapter> list) {
        debug.indent();
        if (list.size() == 0) {
            debug.appendln("none");
        } else {
            for (final ObjectAdapter adapter : list) {
                debug.appendln(adapter.toString());
            }
        }
        debug.unindent();
    }

    // //////////////////////////////////////////////////
    // toString
    // //////////////////////////////////////////////////

    @Override
    public String toString() {
        return new ToString(this).append("changes", changes).toString();
    }

}
