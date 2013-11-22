/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.core.metamodel.layoutmetadata.json;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.isis.applib.annotation.MemberGroupLayout.ColumnSpans;
import org.apache.isis.applib.annotation.Render;
import org.apache.isis.applib.annotation.Render.Type;
import org.apache.isis.applib.filter.Filter;
import org.apache.isis.applib.filter.Filters;
import org.apache.isis.core.commons.lang.ClassExtensions;
import org.apache.isis.core.metamodel.facets.members.order.MemberOrderFacet;
import org.apache.isis.core.metamodel.facets.object.membergroups.MemberGroupLayoutFacet;
import org.apache.isis.core.metamodel.layout.memberorderfacet.MemberOrderFacetComparator;
import org.apache.isis.core.metamodel.layoutmetadata.ActionRepr;
import org.apache.isis.core.metamodel.layoutmetadata.ColumnRepr;
import org.apache.isis.core.metamodel.layoutmetadata.LayoutMetadata;
import org.apache.isis.core.metamodel.layoutmetadata.LayoutMetadataReader;
import org.apache.isis.core.metamodel.layoutmetadata.MemberGroupRepr;
import org.apache.isis.core.metamodel.layoutmetadata.MemberRepr;
import org.apache.isis.core.metamodel.layoutmetadata.PagedFacetRepr;
import org.apache.isis.core.metamodel.layoutmetadata.RenderFacetRepr;
import org.apache.isis.core.metamodel.spec.ActionType;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.ObjectSpecifications;
import org.apache.isis.core.metamodel.spec.ObjectSpecifications.MemberGroupLayoutHint;
import org.apache.isis.core.metamodel.spec.feature.Contributed;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActions;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;

public class LayoutMetadataReaderFromJson implements LayoutMetadataReader {

    public Properties asProperties(Class<?> domainClass) {
        LayoutMetadata metadata;
        try {
            metadata = readMetadata(domainClass);
        } catch (Exception e) {
            throw new ReaderException("Failed to locate/parse " + domainClass.getName() + ".layout.json file (" + e.getMessage() + ")", e);
        }
        if(metadata.getColumns() == null || metadata.getColumns().size() != 4) {
            throw new ReaderException("JSON metadata must have precisely 4 columns (prop/prop/prop/coll)");
        }

        final Properties props = new Properties();
        
        setMemberGroupLayoutColumnSpans(metadata, props);
        setMemberGroupLayoutColumnLists(metadata, 0, "left", props);
        setMemberGroupLayoutColumnLists(metadata, 1, "middle", props);
        setMemberGroupLayoutColumnLists(metadata, 2, "right", props);
        
        int[] memberSeq = {0};
        setProperties(metadata, props, memberSeq);
        setCollections(metadata, props, memberSeq);
        setFreestandingActions(metadata, props);

        return props;
    }

    private static void setMemberGroupLayoutColumnSpans(LayoutMetadata metadata, final Properties props) {
        final List<ColumnRepr> columns = metadata.getColumns();
        final String columnSpansStr = Joiner.on(",").join(Iterables.transform(columns, new Function<ColumnRepr,Integer>(){
            @Override
            public Integer apply(ColumnRepr input) {
                return input.span;
            }}));
        props.setProperty("class.memberGroupLayout.columnSpans", columnSpansStr);
    }

    private static void setMemberGroupLayoutColumnLists(LayoutMetadata metadata, int colIdx, String propkey, Properties props) {
        final ColumnRepr column = metadata.getColumns().get(colIdx);
        final Map<String, MemberGroupRepr> memberGroups = column.memberGroups;
        final String val = memberGroups != null ? Joiner.on(",").join(memberGroups.keySet()) : "";
        props.setProperty("class.memberGroupLayout." + propkey, val);
    }

    private static void setProperties(LayoutMetadata metadata, Properties props, int[] memberSeq) {
        final List<ColumnRepr> columns = metadata.getColumns();
        for (final ColumnRepr columnRepr : columns) {
            final Map<String, MemberGroupRepr> memberGroups = columnRepr.memberGroups;
            
            if(memberGroups == null) {
                continue;
            }
            
            for (final String memberGroupName : memberGroups.keySet()) {
                final MemberGroupRepr memberGroup = memberGroups.get(memberGroupName);
                final Map<String, MemberRepr> members = memberGroup.members;
                
                if(members == null) {
                    continue;
                }
                setMembersAndAssociatedActions(props, memberGroupName, members, memberSeq);
            }
        }
    }

    private static void setCollections(LayoutMetadata metadata, Properties props, int[] memberSeq) {
        final ColumnRepr columnRepr = metadata.getColumns().get(3);
        final Map<String, MemberRepr> collections = columnRepr.collections;
        setMembersAndAssociatedActions(props, null, collections, memberSeq);
    }

    private static void setMembersAndAssociatedActions(Properties props, final String memberGroupName, final Map<String, MemberRepr> members, int[] memberSeq) {
        for(final String memberName: members.keySet()) {
            props.setProperty("member." + memberName + ".memberOrder.sequence", ""+ ++memberSeq[0]);
            if(memberGroupName != null) {
                props.setProperty("member." + memberName + ".memberOrder.name", memberGroupName);
            }
            
            final MemberRepr memberRepr = members.get(memberName);

            final PagedFacetRepr paged = memberRepr.paged;
            if(paged != null) {
                props.setProperty("member." + memberName + ".paged.value", ""+paged.value);
            }

            final RenderFacetRepr render = memberRepr.render;
            if(render != null) {
                // same default as in Render.Type.value()
                final Type renderType = render.value!=null?render.value: Render.Type.EAGERLY;
                props.setProperty("member." + memberName + ".render.value", renderType.toString());
            }
            
            final Map<String, ActionRepr> actions = memberRepr.actions;
            if(actions != null) {
                int actSeq = 0;
                for(final String actionName: actions.keySet()) {
                    String nameKey = "action." + actionName + ".memberOrder.name";
                    String sequenceKey = "action." + actionName + ".memberOrder.sequence";
                    props.setProperty(nameKey, memberName);
                    props.setProperty(sequenceKey, ""+ ++actSeq);
                }
            }
        }
    }

    private static void setFreestandingActions(LayoutMetadata metadata, Properties props) {
        if(metadata.getActions() == null) {
            return;
        }
        int seq=0;
        for (final String actionName : metadata.getActions().keySet()) {
            props.setProperty("member." + actionName + ".memberOrder.sequence", ""+ ++seq);
        }
    }

    public LayoutMetadata asLayoutMetadata(Class<?> domainClass) throws ReaderException {
        try {
            return readMetadata(domainClass);
        } catch (IOException e) {
            throw new ReaderException(e);
        } catch (RuntimeException e) {
            throw new ReaderException(e);
        }
    }

    // //////////////////////////////////////

    private LayoutMetadata readMetadata(Class<?> domainClass) throws IOException {
        final String content = ClassExtensions.resourceContent(domainClass, ".layout.json");
        return readMetadata(content);
    }

    LayoutMetadata readMetadata(final String content) {
        final Gson gson = new GsonBuilder().create();
        return gson.fromJson(content, LayoutMetadata.class);
    }

    // //////////////////////////////////////

    private final static MemberOrderFacetComparator memberOrderFacetComparator = new MemberOrderFacetComparator(false);

    /**
     * not API
     */
    public String asJson(ObjectSpecification objectSpec) {
        final LayoutMetadata metadata = new LayoutMetadata();
        metadata.setColumns(Lists.<ColumnRepr>newArrayList());
        
        final MemberGroupLayoutFacet mglf = objectSpec.getFacet(MemberGroupLayoutFacet.class);
        final ColumnSpans columnSpans = mglf.getColumnSpans();
        
        Set<String> actionIdsForAssociations = Sets.newTreeSet();
        
        ColumnRepr columnRepr;
        
        columnRepr = addColumnWithSpan(metadata, columnSpans.getLeft());
        updateColumnMemberGroups(objectSpec, MemberGroupLayoutHint.LEFT, columnRepr, actionIdsForAssociations);
        
        columnRepr = addColumnWithSpan(metadata, columnSpans.getMiddle());
        updateColumnMemberGroups(objectSpec, MemberGroupLayoutHint.MIDDLE, columnRepr, actionIdsForAssociations);
        
        columnRepr = addColumnWithSpan(metadata, columnSpans.getRight());
        updateColumnMemberGroups(objectSpec, MemberGroupLayoutHint.RIGHT, columnRepr, actionIdsForAssociations);
        
        columnRepr = addColumnWithSpan(metadata, columnSpans.getCollections());
        updateCollectionColumnRepr(objectSpec, columnRepr, actionIdsForAssociations);

        addActions(objectSpec, metadata, actionIdsForAssociations);
        
        final Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(metadata);
    }

    private static void updateColumnMemberGroups(ObjectSpecification objectSpec, final MemberGroupLayoutHint hint, ColumnRepr columnRepr, Set<String> actionIdsForAssociations) {
        final List<ObjectAssociation> objectAssociations = propertiesOf(objectSpec);
        final Map<String, List<ObjectAssociation>> associationsByGroup = ObjectAssociation.Util.groupByMemberOrderName(objectAssociations);
        
        final List<String> groupNames = ObjectSpecifications.orderByMemberGroups(objectSpec, associationsByGroup.keySet(), hint);
        
        columnRepr.memberGroups = Maps.newLinkedHashMap();
        for (String groupName : groupNames) {
            final MemberGroupRepr memberGroupRepr = new MemberGroupRepr();
            columnRepr.memberGroups.put(groupName, memberGroupRepr);
            final List<ObjectAssociation> associationsInGroup = associationsByGroup.get(groupName);
            memberGroupRepr.members = Maps.newLinkedHashMap();
            if(associationsInGroup == null) {
                continue;
            }
            for (ObjectAssociation assoc : associationsInGroup) {
                final MemberRepr memberRepr = newMemberRepr(objectSpec, assoc, actionIdsForAssociations);
                memberGroupRepr.members.put(assoc.getId(), memberRepr);
            }
        }
    }
    private static void addActions(ObjectSpecification objectSpec, final LayoutMetadata metadata, Set<String> actionIdsForAssociations) {
        Map<String, ActionRepr> actions = Maps.newLinkedHashMap();
        final List<ObjectAction> actionsOf = actionsOf(objectSpec, actionIdsForAssociations);
        for(ObjectAction action: actionsOf) {
            actions.put(action.getId(), newActionRepr(objectSpec, action));
        }
        metadata.setActions(actions);
    }

    private static ActionRepr newActionRepr(ObjectSpecification objectSpec, ObjectAction action) {
        return new ActionRepr();
    }

    private static void updateCollectionColumnRepr(ObjectSpecification objectSpec, ColumnRepr columnRepr, Set<String> actionIdsOfAssociations) {
        final List<ObjectAssociation> objectAssociations = collectionsOf(objectSpec);
        columnRepr.collections = Maps.newLinkedHashMap();
        for(ObjectAssociation assoc: objectAssociations) {
            final MemberRepr memberRepr = newMemberRepr(objectSpec, assoc, actionIdsOfAssociations);
            columnRepr.collections.put(assoc.getId(), memberRepr);
        }
    }


    private static MemberRepr newMemberRepr(ObjectSpecification objectSpec, ObjectAssociation assoc, Set<String> actionIdsForAssociations) {
        final MemberRepr memberRepr = new MemberRepr();
        
        final List<ObjectAction> actions = objectSpec.getObjectActions(
                ActionType.USER, Contributed.INCLUDED, ObjectActions.memberOrderOf(assoc));
        if(!actions.isEmpty()) {
            memberRepr.actions = Maps.newLinkedHashMap();
            
            sortByMemberOrderFacet(actions);
            
            for (final ObjectAction action : actions) {
                final String actionId = action.getId();
                memberRepr.actions.put(actionId, new ActionRepr());
                actionIdsForAssociations.add(actionId);
            }
        }
        return memberRepr;
    }

    private static void sortByMemberOrderFacet(final List<ObjectAction> actions) {
        Collections.sort(actions, new Comparator<ObjectAction>() {

            @Override
            public int compare(ObjectAction o1, ObjectAction o2) {
                final MemberOrderFacet m1 = o1.getFacet(MemberOrderFacet.class);
                final MemberOrderFacet m2 = o2.getFacet(MemberOrderFacet.class);
                return memberOrderFacetComparator.compare(m1, m2);
            }});
    }

    private static ColumnRepr addColumnWithSpan(final LayoutMetadata metadata, final int span) {
        final ColumnRepr col = new ColumnRepr();
        metadata.getColumns().add(col);
        col.span = span;
        return col;
    }

    
    @SuppressWarnings({ "unchecked", "deprecation" })
    private static List<ObjectAssociation> propertiesOf(final ObjectSpecification objSpec) {
        return objSpec.getAssociations(Contributed.EXCLUDED, 
                Filters.and(ObjectAssociation.Filters.PROPERTIES, 
                            ObjectAssociation.Filters.VISIBLE_AT_LEAST_SOMETIMES));
    }
    @SuppressWarnings({ "unchecked", "deprecation" })
    private static List<ObjectAssociation> collectionsOf(final ObjectSpecification objSpec) {
        return objSpec.getAssociations(Contributed.EXCLUDED, 
                Filters.and(ObjectAssociation.Filters.COLLECTIONS, 
                            ObjectAssociation.Filters.VISIBLE_AT_LEAST_SOMETIMES));
    }
    private static List<ObjectAction> actionsOf(final ObjectSpecification objSpec, final Set<String> excludedActionIds) {
        return objSpec.getObjectActions(ActionType.ALL, Contributed.INCLUDED, staticallyVisibleExcluding(excludedActionIds));
    }

    @SuppressWarnings({ "unchecked", "deprecation" })
    private static Filter<ObjectAction> staticallyVisibleExcluding(final Set<String> excludedActionIds) {
        return Filters.and(
                ObjectAction.Filters.VISIBLE_AT_LEAST_SOMETIMES, 
                new Filter<ObjectAction>(){
                    @Override
                    public boolean accept(ObjectAction t) {
                        return !excludedActionIds.contains(t.getId());
                    }
                });
    }

    @Override
    public String toString() {
        return getClass().getName();
    }

}
