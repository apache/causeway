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
package org.apache.isis.objectstore.jdo.datanucleus.persistence.spi;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ResolveState;

public final class ResolveStateUtil {

    private ResolveStateUtil() {}


    public static void moveToResolving(final ObjectAdapter adapter) {

        moveUpToResolving(adapter);
    }

    public static void markAsResolved(final ObjectAdapter adapter) {

        moveUpToResolving(adapter);

        // move to ResolveState.RESOLVED;

        if (adapter.isResolving()) { 
            adapter.changeState(ResolveState.RESOLVED);

        } else if (adapter.isResolved()) {
            // nothing to do.

//        } else if (adapter.getResolveState().isPartlyResolved()) {
//            adapter.changeState(ResolveState.RESOLVED);

        } else if (adapter.isUpdating()) {
            adapter.changeState(ResolveState.RESOLVED);
        }
    }


    private static void moveUpToResolving(final ObjectAdapter adapter) {
        // move these on so we can get to part_resolved or resolved.
        if (adapter.isTransient()) {
            adapter.changeState(ResolveState.RESOLVED);
            adapter.changeState(ResolveState.GHOST);
            adapter.changeState(ResolveState.RESOLVING);

        } else if (adapter.isNew()) {
            adapter.changeState(ResolveState.GHOST);
            adapter.changeState(ResolveState.RESOLVING);

        } else if (adapter.isGhost()) {
            adapter.changeState(ResolveState.RESOLVING);
        }
    }

    public static void markAsGhost(final ObjectAdapter adapter) {

        if (adapter.isValue()) {
            // TODO: what should we do here? throw exception?
        }
        if (adapter.isDestroyed()) {
            // TODO: what should we do here? throw exception?
        }

        if (adapter.isTransient()) {
            adapter.changeState(ResolveState.RESOLVED);
            adapter.changeState(ResolveState.GHOST);

        } else if (adapter.isNew()) {
            adapter.changeState(ResolveState.GHOST);

//        } else if (adapter.getResolveState().isPartlyResolved()) {
//            adapter.changeState(ResolveState.RESOLVING);
//            adapter.changeState(ResolveState.RESOLVED);
//            adapter.changeState(ResolveState.GHOST);

        } else if (adapter.isResolving()) {
            adapter.changeState(ResolveState.RESOLVED);
            adapter.changeState(ResolveState.GHOST);

        } else if (adapter.isUpdating()) {
            adapter.changeState(ResolveState.RESOLVED);
            adapter.changeState(ResolveState.GHOST);

        } else if (adapter.isResolved()) {
            adapter.changeState(ResolveState.GHOST);

        } else if (adapter.isGhost()) {
            // nothing to do.
        }
    }


    public static void markAsUpdating(final ObjectAdapter adapter) {

        if (adapter.isTransient()) {
            adapter.changeState(ResolveState.RESOLVED);
        }
        if (adapter.isResolved()) {
            adapter.changeState(ResolveState.UPDATING);
        }
    }

}


// Copyright (c) Naked Objects Group Ltd.
