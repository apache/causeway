package org.apache.isis.core.metamodel.adapter.oid;

import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import org.apache.isis.core.commons.lang.CastUtils;
import org.apache.isis.core.metamodel.adapter.oid.Oid.State;
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
 * <dt>:</dt>
 * <dd>precedes root object identifier</dd>
 * <dt>~</dt>
 * <dd>precedes aggregate oid</dd>
 * <dt>$</dt>
 * <dd>precedes collection name</dd>
 * </dl>
 * <p>
 * Note that # and ; were not chosen as separators to minimize noise when URL encoding OIDs.
 */
public class OidMarshaller {

    private static Pattern OIDSTR_PATTERN = 
            Pattern.compile("^((([!])?([^:@#$]+):([^:@#$]+))((~[^:@#$]+:[^:@#$]+)*))([$]([^:@#$]+))?$");

    ////////////////////////////////////////////////////////////////
    // unmarshall
    ////////////////////////////////////////////////////////////////

    public <T extends Oid> T unmarshal(String oidStr, Class<T> requestedType) {
        
        final Matcher matcher = OIDSTR_PATTERN.matcher(oidStr);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Could not parse OID '" + oidStr + "'; should match pattern: " + OIDSTR_PATTERN.pattern());
        }

        final int groupCount = matcher.groupCount();

        final String isTransientStr = getGroup(matcher, 3);
        boolean isTransient = "!".equals(isTransientStr);
        
        final String oidStrWithoutCollectionName = getGroup(matcher, 1);
        final String rootOidStr = getGroup(matcher, 2);
        
        final String rootObjectType = getGroup(matcher, 4);
        final String rootIdentifier = getGroup(matcher, 5);
        
        final String aggregateOidPart = getGroup(matcher, 6);
        final List<AggregateOidPart> aggregateOidParts = Lists.newArrayList();
        final Splitter tildaSplitter = Splitter.on("~");
        final Splitter colonSplitter = Splitter.on(":");
        if(aggregateOidPart != null) {
            final Iterable<String> tildaSplitIter = tildaSplitter.split(aggregateOidPart);
            for(String str: tildaSplitIter) {
                if(Strings.isNullOrEmpty(str)) {
                    continue; // leading "~"
                }
                final Iterator<String> colonSplitIter = colonSplitter.split(str).iterator();
                final String objectType = colonSplitIter.next();
                final String localId = colonSplitIter.next();
                aggregateOidParts.add(new AggregateOidPart(objectType, localId));
            }
        }
        final String collectionName = getGroup(matcher, groupCount); // last one
        
        if(collectionName == null) {
            if(aggregateOidParts.isEmpty()) {
                ensureCorrectType(oidStr, requestedType, RootOidDefault.class); 
                return (T)new RootOidDefault(ObjectSpecId.of(rootObjectType), rootIdentifier, State.valueOf(isTransient));
            } else {
                ensureCorrectType(oidStr, requestedType, AggregatedOid.class);
                final AggregateOidPart lastPart = aggregateOidParts.remove(aggregateOidParts.size()-1);
                final TypedOid parentOid = parentOidFor(rootOidStr, aggregateOidParts);
                return (T)new AggregatedOid(ObjectSpecId.of(lastPart.objectType), parentOid, lastPart.localId);
            }
        } else {
            TypedOid parentOid = this.unmarshal(oidStrWithoutCollectionName, TypedOid.class);
            ensureCorrectType(oidStr, requestedType, CollectionOid.class);
            return (T)new CollectionOid(parentOid, collectionName);
        }
    }

    private static class AggregateOidPart {
        AggregateOidPart(String objectType, String localId) {
            this.objectType = objectType;
            this.localId = localId;
        }
        String objectType;
        String localId;
        public String toString() {
            return "~" + objectType + ":" + localId;
        }
    }
    

    private TypedOid parentOidFor(final String rootOidStr, final List<AggregateOidPart> aggregateOidParts) {
        final StringBuilder buf = new StringBuilder(rootOidStr);
        for(AggregateOidPart part: aggregateOidParts) {
            buf.append(part.toString());
        }
        return unmarshal(buf.toString(), TypedOid.class);
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
        return Strings.emptyToNull(val);
    }

    
    ////////////////////////////////////////////////////////////////
    // marshall
    ////////////////////////////////////////////////////////////////
    
    public String marshal(RootOid rootOid) {
        return (rootOid.isTransient()? "!" : "") + rootOid.getObjectSpecId() + ":" + rootOid.getIdentifier();
    }

    public String marshal(CollectionOid collectionOid) {
        return collectionOid.getParentOid().enString() + "$" + collectionOid.getName();
    }

    public String marshal(AggregatedOid aggregatedOid) {
        return aggregatedOid.getParentOid().enString() + "~" + aggregatedOid.getObjectSpecId() + ":" + aggregatedOid.getLocalId();
    }


}
