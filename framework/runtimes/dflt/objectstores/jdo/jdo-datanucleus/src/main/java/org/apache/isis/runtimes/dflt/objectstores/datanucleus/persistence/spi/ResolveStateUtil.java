package org.apache.isis.runtimes.dflt.objectstores.datanucleus.persistence.spi;

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
        if (adapter.representsTransient()) {
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

        if (adapter.representsTransient()) {
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
