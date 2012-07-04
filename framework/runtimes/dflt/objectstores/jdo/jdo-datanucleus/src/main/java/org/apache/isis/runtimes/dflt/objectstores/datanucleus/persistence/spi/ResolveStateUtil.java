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

        if (adapter.getResolveState().isResolving()) { // either RESOLVING or
            // PART_RESOLVING
            adapter.changeState(ResolveState.RESOLVED);

        } else if (adapter.getResolveState().isResolved()) {
            // nothing to do.

        } else if (adapter.getResolveState().isPartlyResolved()) {
            adapter.changeState(ResolveState.RESOLVED);

        } else if (adapter.getResolveState().isUpdating()) {
            adapter.changeState(ResolveState.RESOLVED);
        }
    }


    private static void moveUpToResolving(final ObjectAdapter adapter) {
        // move these on so we can get to part_resolved or resolved.
        if (adapter.isTransient()) {
            adapter.changeState(ResolveState.RESOLVED);
            adapter.changeState(ResolveState.GHOST);
            adapter.changeState(ResolveState.RESOLVING);

        } else if (adapter.getResolveState().isNew()) {
            adapter.changeState(ResolveState.GHOST);
            adapter.changeState(ResolveState.RESOLVING);

        } else if (adapter.getResolveState().isGhost()) {
            adapter.changeState(ResolveState.RESOLVING);
        }
    }

    public static void markAsGhost(final ObjectAdapter adapter) {

        if (adapter.getResolveState().isValue()) {
            // TODO: what should we do here? throw exception?
        }
        if (adapter.getResolveState().isDestroyed()) {
            // TODO: what should we do here? throw exception?
        }

        if (adapter.isTransient()) {
            adapter.changeState(ResolveState.RESOLVED);
            adapter.changeState(ResolveState.GHOST);

        } else if (adapter.getResolveState().isNew()) {
            adapter.changeState(ResolveState.GHOST);

        } else if (adapter.getResolveState().isPartlyResolved()) {
            adapter.changeState(ResolveState.RESOLVING);
            adapter.changeState(ResolveState.RESOLVED);
            adapter.changeState(ResolveState.GHOST);

        } else if (adapter.getResolveState().isResolving()) {
            adapter.changeState(ResolveState.RESOLVED);
            adapter.changeState(ResolveState.GHOST);

        } else if (adapter.getResolveState().isUpdating()) {
            adapter.changeState(ResolveState.RESOLVED);
            adapter.changeState(ResolveState.GHOST);

        } else if (adapter.getResolveState().isResolved()) {
            adapter.changeState(ResolveState.GHOST);

        } else if (adapter.getResolveState().isGhost()) {
            // nothing to do.
        }
    }


    public static void markAsUpdating(final ObjectAdapter adapter) {

        if (adapter.getResolveState() == ResolveState.TRANSIENT) {
            adapter.changeState(ResolveState.RESOLVED);
        }
        if (adapter.getResolveState() == ResolveState.RESOLVED) {
            adapter.changeState(ResolveState.UPDATING);
        }
    }

}


// Copyright (c) Naked Objects Group Ltd.
