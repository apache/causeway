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

package org.apache.isis.core.metamodel.adapter;

import static org.apache.isis.core.metamodel.adapter.ResolveState.RepresentsPersistent.DOES_NOT_REPRESENT_PERSISTENT;
import static org.apache.isis.core.metamodel.adapter.ResolveState.RepresentsPersistent.REPRESENTS_PERSISTENT;
import static org.apache.isis.core.metamodel.adapter.ResolveState.RepresentsTransient.DOES_NOT_REPRESENT_TRANSIENT;
import static org.apache.isis.core.metamodel.adapter.ResolveState.RepresentsTransient.REPRESENTS_TRANSIENT;
import static org.apache.isis.core.metamodel.adapter.ResolveState.ResolvableFrom.NOT_RESOLVABLE_FROM;
import static org.apache.isis.core.metamodel.adapter.ResolveState.ResolvableFrom.RESOLVABLE_FROM;
import static org.apache.isis.core.metamodel.adapter.ResolveState.ResolvableInto.NOT_RESOLVABLE_INTO;
import static org.apache.isis.core.metamodel.adapter.ResolveState.ResolvableInto.RESOLVABLE_INTO;
import static org.apache.isis.core.metamodel.adapter.ResolveState.ResolvePotential.COULD_RESOLVE;
import static org.apache.isis.core.metamodel.adapter.ResolveState.ResolvePotential.WILL_NEVER_RESOLVE;
import static org.apache.isis.core.metamodel.adapter.ResolveState.RespondsToChanges.DOES_NOT_RESPOND_TO_CHANGES;
import static org.apache.isis.core.metamodel.adapter.ResolveState.RespondsToChanges.RESPONDS_TO_CHANGES;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;

public final class ResolveState {
    private static final Map<String, ResolveState> statesByName = Maps.newHashMap();

    static enum ResolvableFrom {
        RESOLVABLE_FROM, NOT_RESOLVABLE_FROM
    }

    static enum ResolvableInto {
        RESOLVABLE_INTO, NOT_RESOLVABLE_INTO
    }

    static enum RespondsToChanges {
        RESPONDS_TO_CHANGES, DOES_NOT_RESPOND_TO_CHANGES
    }

    static enum RepresentsTransient {
        REPRESENTS_TRANSIENT, DOES_NOT_REPRESENT_TRANSIENT
    }

    static enum RepresentsPersistent {
        REPRESENTS_PERSISTENT, DOES_NOT_REPRESENT_PERSISTENT
    }

    /**
     * Whether or not an object in this state could cause a resolve (database
     * load) to occur if interacted with.
     */
    static enum ResolvePotential {
        COULD_RESOLVE, WILL_NEVER_RESOLVE
    }

    /**
     * When first instantiated by <tt>PojoAdapterFactory</tt>.
     */
    public static final ResolveState NEW = new ResolveState("New", "N~~~", null, NOT_RESOLVABLE_FROM, NOT_RESOLVABLE_INTO, DOES_NOT_RESPOND_TO_CHANGES, DOES_NOT_REPRESENT_TRANSIENT, DOES_NOT_REPRESENT_PERSISTENT, WILL_NEVER_RESOLVE);
    public static final ResolveState TRANSIENT = new ResolveState("Transient", "T~~~", null, NOT_RESOLVABLE_FROM, NOT_RESOLVABLE_INTO, DOES_NOT_RESPOND_TO_CHANGES, REPRESENTS_TRANSIENT, DOES_NOT_REPRESENT_PERSISTENT, WILL_NEVER_RESOLVE);
    public static final ResolveState GHOST = new ResolveState("Ghost", "PG~~", null, RESOLVABLE_FROM, NOT_RESOLVABLE_INTO, RESPONDS_TO_CHANGES, DOES_NOT_REPRESENT_TRANSIENT, REPRESENTS_PERSISTENT, COULD_RESOLVE);
    public static final ResolveState RESOLVED = new ResolveState("Resolved", "PR~~", null, RESOLVABLE_FROM, NOT_RESOLVABLE_INTO, RESPONDS_TO_CHANGES, DOES_NOT_REPRESENT_TRANSIENT, REPRESENTS_PERSISTENT, WILL_NEVER_RESOLVE);
    public static final ResolveState RESOLVING = new ResolveState("Resolving", "P~R~", RESOLVED, NOT_RESOLVABLE_FROM, RESOLVABLE_INTO, DOES_NOT_RESPOND_TO_CHANGES, DOES_NOT_REPRESENT_TRANSIENT, REPRESENTS_PERSISTENT, COULD_RESOLVE);
    /**
     * 20120709: used only in <tt>Memento</tt>, when recreating a transient
     * object.
     */
    public static final ResolveState SERIALIZING_TRANSIENT = new ResolveState("Serializing Transient", "T~~S", TRANSIENT, NOT_RESOLVABLE_FROM, NOT_RESOLVABLE_INTO, DOES_NOT_RESPOND_TO_CHANGES, REPRESENTS_TRANSIENT, DOES_NOT_REPRESENT_PERSISTENT, WILL_NEVER_RESOLVE);
    /**
     * 20120709: used only in <tt>DefaultPersistAlgorithm</tt>
     */
    public static final ResolveState SERIALIZING_RESOLVED = new ResolveState("Serializing Resolved", "PR~S", RESOLVED, NOT_RESOLVABLE_FROM, NOT_RESOLVABLE_INTO, DOES_NOT_RESPOND_TO_CHANGES, DOES_NOT_REPRESENT_TRANSIENT, REPRESENTS_PERSISTENT, WILL_NEVER_RESOLVE);
    public static final ResolveState UPDATING = new ResolveState("Updating", "PU~~", RESOLVED, NOT_RESOLVABLE_FROM, RESOLVABLE_INTO, DOES_NOT_RESPOND_TO_CHANGES, DOES_NOT_REPRESENT_TRANSIENT, REPRESENTS_PERSISTENT, COULD_RESOLVE);
    public static final ResolveState DESTROYED = new ResolveState("Destroyed", "D~~~", null, NOT_RESOLVABLE_FROM, NOT_RESOLVABLE_INTO, RESPONDS_TO_CHANGES, DOES_NOT_REPRESENT_TRANSIENT, DOES_NOT_REPRESENT_PERSISTENT, WILL_NEVER_RESOLVE);
    public static final ResolveState VALUE = new ResolveState("Value", "V~~~", null, NOT_RESOLVABLE_FROM, NOT_RESOLVABLE_INTO, RESPONDS_TO_CHANGES, DOES_NOT_REPRESENT_TRANSIENT, DOES_NOT_REPRESENT_PERSISTENT, WILL_NEVER_RESOLVE);

    // no longer seem to be used

    // public static final ResolveState PART_RESOLVED = new
    // ResolveState("Part Resolved", "Pr~~", null, RESOLVABLE_FROM,
    // NOT_RESOLVABLE_INTO, RESPONDS_TO_CHANGES, DOES_NOT_REPRESENT_TRANSIENT,
    // REPRESENTS_PERSISTENT, DOES_NOT_REPRESENT_RESOLVING, COULD_RESOLVE);
    // public static final ResolveState RESOLVING_PART = new
    // ResolveState("Resolving Part", "P~r~", PART_RESOLVED,
    // NOT_RESOLVABLE_FROM, RESOLVABLE_INTO, DOES_NOT_RESPOND_TO_CHANGES,
    // DOES_NOT_REPRESENT_TRANSIENT, REPRESENTS_PERSISTENT,
    // REPRESENTS_RESOLVING, COULD_RESOLVE);

    // no longer appear to be needed following the removal of remoting support.

    // public static final ResolveState SERIALIZING_GHOST = new
    // ResolveState("Serializing Ghost", "PG~S", GHOST, NOT_RESOLVABLE_FROM,
    // NOT_RESOLVABLE_INTO, DOES_NOT_RESPOND_TO_CHANGES,
    // DOES_NOT_REPRESENT_TRANSIENT, REPRESENTS_PERSISTENT,
    // DOES_NOT_REPRESENT_RESOLVING, COULD_RESOLVE);
    // public static final ResolveState SERIALIZING_PART_RESOLVED = new
    // ResolveState("Serializing Part Resolved", "Pr~S", PART_RESOLVED,
    // NOT_RESOLVABLE_FROM, NOT_RESOLVABLE_INTO, DOES_NOT_RESPOND_TO_CHANGES,
    // DOES_NOT_REPRESENT_TRANSIENT, REPRESENTS_PERSISTENT,
    // DOES_NOT_REPRESENT_RESOLVING,
    // COULD_RESOLVE);

    /**
     * These cannot be passed into the constructor because cannot reference an
     * instance until it has been declared.
     */
    public static Map<ResolveState, ResolveState[]> changeToStatesByState = new HashMap<ResolveState, ResolveState[]>() {
        private static final long serialVersionUID = 1L;
        {
            // previously also RESOLVING_PART and SERIALIZING_GHOST
            put(GHOST, new ResolveState[] { DESTROYED, RESOLVING, UPDATING });
            put(NEW, new ResolveState[] { TRANSIENT, GHOST, VALUE });
            put(TRANSIENT, new ResolveState[] { RESOLVED, SERIALIZING_TRANSIENT });
            put(RESOLVING, new ResolveState[] { RESOLVED });
            put(RESOLVED, new ResolveState[] { GHOST, SERIALIZING_RESOLVED, UPDATING, DESTROYED });
            put(SERIALIZING_RESOLVED, new ResolveState[] { RESOLVED });
            put(SERIALIZING_TRANSIENT, new ResolveState[] { TRANSIENT });
            put(UPDATING, new ResolveState[] { RESOLVED });
            put(DESTROYED, new ResolveState[] {});
            put(VALUE, new ResolveState[] {});

            // put(PART_RESOLVED, new ResolveState[] { RESOLVING , UPDATING,
            // DESTROYED , RESOLVING_PART, SERIALIZING_PART_RESOLVED });
            // put(RESOLVING_PART, new ResolveState[] { PART_RESOLVED, RESOLVED
            // });
            // put(SERIALIZING_GHOST, new ResolveState[] { GHOST });
            // put(SERIALIZING_PART_RESOLVED, new ResolveState[] { PART_RESOLVED
            // });

        }
    };

    private final String code;
    private final ResolveState endState;
    private final String name;
    private final ResolvableFrom resolvableFrom;
    private final ResolvableInto resolvableInto;
    private final RespondsToChanges respondsToChanges;
    private final RepresentsTransient representsTransient;
    private final RepresentsPersistent representsPersistent;
    private final ResolvePotential resolvePotential;
    private HashSet<ResolveState> changeToStates;

    private ResolveState(final String name, final String code, final ResolveState endState, final ResolvableFrom resolvableFrom, final ResolvableInto resolvableInto, final RespondsToChanges respondsToChanges, final RepresentsTransient representsTransient,
            final RepresentsPersistent representsPersistent, final ResolvePotential resolvePotential) {
        this.name = name;
        this.code = code;
        this.endState = endState;
        this.resolvableFrom = resolvableFrom;
        this.resolvableInto = resolvableInto;
        this.respondsToChanges = respondsToChanges;
        this.representsTransient = representsTransient;
        this.representsPersistent = representsPersistent;
        this.resolvePotential = resolvePotential;
        statesByName.put(name, this);
    }

    /**
     * Four character representation of the state.
     * 
     * <p>
     * The format is <tt>XYZW</tt> where:
     * <ul>
     * <li><tt>X</tt> is transient state:
     * <ul>
     * <li>N</li> for <b>N</b>ew
     * <li>T</li> for <b>T</b>ransient
     * <li>P</li> for <b>P</b>ersistent
     * <li>D</li> for <b>D</b>estroyed
     * <li>A</li> for st<b>A</b>ndalone
     * </ul>
     * </li>
     * <li><tt>Y</tt> (for persistent only) is the resolve state:
     * <ul>
     * <li>G</li> for <b>G</b>host
     * <li>R</li> for <b>R</b>esolved
     * <li>r</li> for Part <b>r</b>esolved
     * <li>~</li> if not persistent
     * </ul>
     * </li>
     * <li><tt>Z</tt> (for persistent only) is the resolving state:
     * <ul>
     * <li>R</li> for <b>R</b>esolving
     * <li>r</li> for Part <b>r</b>esolving
     * <li>~</li> if not persistent
     * </ul>
     * </li>
     * <li><tt>W</tt> (for non-standalone, not resolving, not updating, not
     * destroyed) is the serialization state:
     * <ul>
     * <li>~</li> not serializing
     * <li>S</li> is serializing
     * </ul>
     * </li>
     * </ul>
     */
    public String code() {
        return code;
    }

    public ResolveState getEndState() {
        return endState;
    }

    /**
     * Returns <tt>true</tt> when an object is persistent (except for
     * {@link #VALUE} adapters).
     * 
     * <p>
     * Always returns <tt>false</tt> for {@link #VALUE}.
     */
    public boolean representsPersistent() {
        return this.representsPersistent == RepresentsPersistent.REPRESENTS_PERSISTENT;
    }

    /**
     * Returns <tt>true</tt> when an object has not yet been made persistent
     * (except for {@link #VALUE} adapters)..
     * 
     * <p>
     * Always returns <tt>false</tt> for {@link #VALUE}.
     */
    public boolean representsTransient() {
        return this.representsTransient == REPRESENTS_TRANSIENT;
    }

    /**
     * As per {@link #isValidToChangeTo(ResolveState)}, but will additionally
     * throw a {@link ResolveException} if the requested state can never be
     * transitioned into, and will return <tt>false</tt> if the current state
     * can never be transitioned from.
     */
    public boolean canChangeTo(final ResolveState newState) {
        if (newState.resolvableInto == NOT_RESOLVABLE_INTO) {
            throw new ResolveException("new state must be resolvable into");
        }
        if (this.resolvableFrom == RESOLVABLE_FROM) {
            return isValidToChangeTo(newState);
        }
        return false;
    }

    /**
     * Returns false while object is having its field set up.
     */
    public boolean respondToChangesInPersistentObjects() {
        return respondsToChanges == RESPONDS_TO_CHANGES;
    }

    public boolean isGhost() {
        return this == GHOST;
    }

    public boolean isValue() {
        return this == VALUE;
    }

    public boolean isNew() {
        return this == NEW;
    }

    public boolean isUpdating() {
        return this == UPDATING;
    }

    public boolean isDestroyed() {
        return this == DESTROYED;
    }

    public boolean isResolved() {
        return this == RESOLVED;
    }

    public boolean isTransient() {
        return this == TRANSIENT;
    }

    /**
     * Return true if the state reflects some kind of loading.
     */
    public boolean isResolving() {
        return this == RESOLVING;
    }

    /**
     * Returns <tt>true</tt> if an object in this state could trigger some sort
     * of database loading.
     * 
     * <p>
     * Used to prevent calls to <tt>title()</tt> etc on objects that are not
     * resolved.
     * 
     * @see ResolvePotential
     */
    public boolean couldResolve() {
        return this.resolvePotential == COULD_RESOLVE;
    }

    /**
     * Determines if the resolved state can be changed from this state to the
     * specified state. Returns true if the change is valid.
     */
    public boolean isValidToChangeTo(final ResolveState nextState) {
        cacheChangeToStatesIfNecessary();
        return this.changeToStates.contains(nextState);
    }

    private void cacheChangeToStatesIfNecessary() {
        if (this.changeToStates == null) {
            final List<ResolveState> nextStates = Arrays.asList(changeToStatesByState.get(this));
            changeToStates = new HashSet<ResolveState>(nextStates);
        }
    }

    public String name() {
        return name;
    }

    private transient String cachedToString;

    @Override
    public String toString() {
        if (cachedToString == null) {
            final StringBuffer str = new StringBuffer();
            str.append("ResolveState [name=");
            str.append(name);
            str.append(",code=");
            str.append(code);
            if (endState != null) {
                str.append(",endstate=");
                str.append(endState.name());
            }
            str.append("]");
            cachedToString = str.toString();
        }
        return cachedToString;
    }

}
