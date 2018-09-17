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
package org.apache.isis.core.metamodel.adapter.oid;

import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Splitter;

import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.core.metamodel.adapter.oid.Oid.State;
import org.apache.isis.core.metamodel.adapter.version.Version;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;

/**
 * Factory for subtypes of {@link Oid}, based on their oid str.
 *
 * <p>
 * Examples
 * <dl>
 * <dt>CUS:123</dt>
 * <dd>persistent root</dd>
 * <dt>!CUS:123</dt>
 * <dd>transient root</dd>
 * <dt>*CUS:123</dt>
 * <dd>view model root</dd>
 * <dt>CUS:123$items</dt>
 * <dd>collection of persistent root</dd>
 * <dt>!CUS:123$items</dt>
 * <dd>collection of transient root</dd>
 * <dt>CUS:123~NME:2</dt>
 * <dd>aggregated object within persistent root</dd>
 * <dt>!CUS:123~NME:2</dt>
 * <dd>aggregated object within transient root</dd>
 * <dt>CUS:123~NME:2~CTY:LON</dt>
 * <dd>aggregated object within aggregated object within root</dd>
 * <dt>CUS:123~NME:2$items</dt>
 * <dd>collection of an aggregated object within root</dd>
 * <dt>CUS:123~NME:2~CTY:LON$streets</dt>
 * <dd>collection of an aggregated object within aggregated object within root</dd>
 * </dl>
 *
 * <p>
 * Separators:
 * <dl>
 * <dt>!</dt>
 * <dd>precedes root object type, indicates transient</dd>
 * <dt>*</dt>
 * <dd>precedes root object type, indicates transient</dd>
 * <dt>:</dt>
 * <dd>precedes root object identifier</dd>
 * <dt>~</dt>
 * <dd>precedes aggregate oid</dd>
 * <dt>$</dt>
 * <dd>precedes collection name</dd>
 * <dt>^</dt>
 * <dd>precedes version</dd>
 * </dl>
 *
 * <p>
 * Note that # and ; were not chosen as separators to minimize noise when URL encoding OIDs.
 */
public final class OidMarshaller {

    public final static OidMarshaller INSTANCE = new OidMarshaller();

    private OidMarshaller(){}

    // -- public constants
    public static final String VIEWMODEL_INDICATOR =
            Bookmark.ObjectState.VIEW_MODEL.getCode(); // "*"


    // -- private constants
    private static final String TRANSIENT_INDICATOR =
            Bookmark.ObjectState.TRANSIENT.getCode() ; // "!"

    private static final String SEPARATOR = ":";
    private static final String SEPARATOR_NESTING = "~";
    private static final String SEPARATOR_COLLECTION = "$";
    private static final String SEPARATOR_VERSION = "^";

    private static final String WORD = "[^" + SEPARATOR + SEPARATOR_NESTING + SEPARATOR_COLLECTION + "\\" + SEPARATOR_VERSION + "#" + "]+";
    private static final String DIGITS = "\\d+";

    private static final String WORD_GROUP = "(" + WORD + ")";
    private static final String DIGITS_GROUP = "(" + DIGITS + ")";

    private static Pattern OIDSTR_PATTERN =
            Pattern.compile(
                    "^(" +
                            "(" +
                            "([" + TRANSIENT_INDICATOR + VIEWMODEL_INDICATOR + "])?" +
                            WORD_GROUP + SEPARATOR + WORD_GROUP +
                            ")" +
                            "(" +
                            "(" + SEPARATOR_NESTING + WORD + SEPARATOR + WORD + ")*" + // nesting of aggregates
                            ")" +
                            ")" +
                            "(" + "[" + SEPARATOR_COLLECTION + "]" + WORD + ")?"  + // optional collection name
                            "(" +
                            "[\\" + SEPARATOR_VERSION + "]" +
                            DIGITS_GROUP +                    // optional version digit
                            SEPARATOR + WORD_GROUP + "?" +    // optional version user name
                            SEPARATOR + DIGITS_GROUP + "?" +  // optional version UTC time
                            ")?" +
                    "$");


    // -- join, split
    @Programmatic
    public String joinAsOid(String domainType, String instanceId) {
        return domainType + SEPARATOR + instanceId;
    }

    @Programmatic
    public String splitInstanceId(String oidStr) {
        final int indexOfSeperator = oidStr.indexOf(SEPARATOR);
        return indexOfSeperator > 0? oidStr.substring(indexOfSeperator+1): null;
    }



    // -- unmarshal

    @Programmatic
    @SuppressWarnings("unchecked")
    public <T extends Oid> T unmarshal(String oidStr, Class<T> requestedType) {

        final Matcher matcher = OIDSTR_PATTERN.matcher(oidStr);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Could not parse OID '" + oidStr + "'; should match pattern: " + OIDSTR_PATTERN.pattern());
        }

        final String isTransientOrViewModelStr = getGroup(matcher, 3);
        final State state;
        if("!".equals(isTransientOrViewModelStr)) {
            state = State.TRANSIENT;
        } else if("*".equals(isTransientOrViewModelStr)) {
            state = State.VIEWMODEL;
        } else {
            state = State.PERSISTENT;
        }

        final String rootObjectType = getGroup(matcher, 4);
        final String rootIdentifier = getGroup(matcher, 5);

        final String aggregateOidPart = getGroup(matcher, 6);
        final List<AggregateOidPart> aggregateOidParts = _Lists.newArrayList();
        final Splitter nestingSplitter = Splitter.on(SEPARATOR_NESTING);
        final Splitter partsSplitter = Splitter.on(SEPARATOR);
        if(aggregateOidPart != null) {
            final Iterable<String> tildaSplitIter = nestingSplitter.split(aggregateOidPart);
            for(String str: tildaSplitIter) {
                if(_Strings.isNullOrEmpty(str)) {
                    continue; // leading "~"
                }
                final Iterator<String> colonSplitIter = partsSplitter.split(str).iterator();
                final String objectType = colonSplitIter.next();
                final String localId = colonSplitIter.next();
                aggregateOidParts.add(new AggregateOidPart(objectType, localId));
            }
        }
        final String collectionPart = getGroup(matcher, 8);
        final String collectionName = collectionPart != null ? collectionPart.substring(1) : null;

        final String versionSequence = getGroup(matcher, 10);
        final String versionUser = getGroup(matcher, 11);
        final String versionUtcTimestamp = getGroup(matcher, 12);
        final Version version = Version.create(versionSequence, versionUser, versionUtcTimestamp);

        if(collectionName == null) {
            if(aggregateOidParts.isEmpty()) {
                ensureCorrectType(oidStr, requestedType, RootOid.class);
                return (T) new RootOid(ObjectSpecId.of(rootObjectType), rootIdentifier, state, version);
            } else {
                throw new RuntimeException("Aggregated Oids are no longer supported");
            }
        } else {
            final String oidStrWithoutCollectionName = getGroup(matcher, 1);

            final String parentOidStr = oidStrWithoutCollectionName + marshal(version);

            RootOid parentOid = this.unmarshal(parentOidStr, RootOid.class);
            ensureCorrectType(oidStr, requestedType, ParentedCollectionOid.class);
            return (T)new ParentedCollectionOid(parentOid, collectionName);
        }
    }



    private static class AggregateOidPart {
        AggregateOidPart(String objectType, String localId) {
            this.objectType = objectType;
            this.localId = localId;
        }
        String objectType;
        String localId;
        @Override
        public String toString() {
            return SEPARATOR_NESTING + objectType + SEPARATOR + localId;
        }
    }


    private <T> void ensureCorrectType(String oidStr, Class<T> requestedType, final Class<? extends Oid> actualType) {
        if(!requestedType.isAssignableFrom(actualType)) {
            throw new IllegalArgumentException("OID '" + oidStr + "' does not represent a " +
                    actualType.getSimpleName());
        }
    }

    private String getGroup(final Matcher matcher, final int group) {
        final int groupCount = matcher.groupCount();
        if(group > groupCount) {
            return null;
        }
        final String val = matcher.group(group);
        return _Strings.emptyToNull(val);
    }




    // -- marshal
    @Programmatic
    public final String marshal(RootOid rootOid) {
        return marshalNoVersion(rootOid) + marshal(rootOid.getVersion());
    }

    @Programmatic
    public final String marshalNoVersion(RootOid rootOid) {
        final String transientIndicator = rootOid.isTransient()? TRANSIENT_INDICATOR : "";
        final String viewModelIndicator = rootOid.isViewModel()? VIEWMODEL_INDICATOR : "";
        return transientIndicator + viewModelIndicator + rootOid.getObjectSpecId() + SEPARATOR + rootOid.getIdentifier();
    }

    @Programmatic
    public final String marshal(ParentedCollectionOid collectionOid) {
        return marshalNoVersion(collectionOid) + marshal(collectionOid.getVersion());
    }

    @Programmatic
    public String marshalNoVersion(ParentedCollectionOid collectionOid) {
        return collectionOid.getRootOid().enStringNoVersion() + SEPARATOR_COLLECTION + collectionOid.getName();
    }

    @Programmatic
    public final String marshal(Version version) {
        if(version == null) {
            return "";
        }
        final String versionUser = version.getUser();
        return SEPARATOR_VERSION + version.getSequence() + SEPARATOR + _Strings.nullToEmpty(versionUser) + SEPARATOR + nullToEmpty(version.getUtcTimestamp());
    }

    private static String nullToEmpty(Object obj) {
        return obj == null? "": "" + obj;
    }


}
