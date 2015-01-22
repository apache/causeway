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
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.isis.applib.annotation.MemberGroupLayout.ColumnSpans;
import org.apache.isis.applib.annotation.Render;
import org.apache.isis.applib.annotation.Render.Type;
import org.apache.isis.applib.annotation.When;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.filter.Filter;
import org.apache.isis.core.commons.lang.ClassExtensions;
import org.apache.isis.core.metamodel.facets.all.describedas.DescribedAsFacet;
import org.apache.isis.core.metamodel.facets.all.hide.HiddenFacet;
import org.apache.isis.core.metamodel.facets.all.named.NamedFacet;
import org.apache.isis.core.metamodel.facets.members.cssclass.CssClassFacet;
import org.apache.isis.core.metamodel.facets.members.disabled.DisabledFacet;
import org.apache.isis.core.metamodel.facets.members.disabled.DisabledFacetAbstractImpl;
import org.apache.isis.core.metamodel.facets.members.order.MemberOrderFacet;
import org.apache.isis.core.metamodel.facets.members.render.RenderFacet;
import org.apache.isis.core.metamodel.facets.object.membergroups.MemberGroupLayoutFacet;
import org.apache.isis.core.metamodel.facets.object.paged.PagedFacet;
import org.apache.isis.core.metamodel.facets.objpropparam.typicallen.TypicalLengthFacet;
import org.apache.isis.core.metamodel.facets.propparam.multiline.MultiLineFacet;
import org.apache.isis.core.metamodel.layout.memberorderfacet.MemberOrderFacetComparator;
import org.apache.isis.core.metamodel.layoutmetadata.ActionLayoutFacetRepr;
import org.apache.isis.core.metamodel.layoutmetadata.ActionRepr;
import org.apache.isis.core.metamodel.layoutmetadata.CollectionLayoutFacetRepr;
import org.apache.isis.core.metamodel.layoutmetadata.ColumnRepr;
import org.apache.isis.core.metamodel.layoutmetadata.CssClassFaFacetRepr;
import org.apache.isis.core.metamodel.layoutmetadata.CssClassFacetRepr;
import org.apache.isis.core.metamodel.layoutmetadata.DescribedAsFacetRepr;
import org.apache.isis.core.metamodel.layoutmetadata.DisabledFacetRepr;
import org.apache.isis.core.metamodel.layoutmetadata.HiddenFacetRepr;
import org.apache.isis.core.metamodel.layoutmetadata.LayoutMetadata;
import org.apache.isis.core.metamodel.layoutmetadata.LayoutMetadataReader;
import org.apache.isis.core.metamodel.layoutmetadata.MemberGroupRepr;
import org.apache.isis.core.metamodel.layoutmetadata.MemberRepr;
import org.apache.isis.core.metamodel.layoutmetadata.MultiLineFacetRepr;
import org.apache.isis.core.metamodel.layoutmetadata.NamedFacetRepr;
import org.apache.isis.core.metamodel.layoutmetadata.PagedFacetRepr;
import org.apache.isis.core.metamodel.layoutmetadata.PropertyLayoutFacetRepr;
import org.apache.isis.core.metamodel.layoutmetadata.RenderFacetRepr;
import org.apache.isis.core.metamodel.layoutmetadata.TypicalLengthFacetRepr;
import org.apache.isis.core.metamodel.spec.ActionType;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.ObjectSpecifications;
import org.apache.isis.core.metamodel.spec.ObjectSpecifications.MemberGroupLayoutHint;
import org.apache.isis.core.metamodel.spec.feature.Contributed;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;

public class LayoutMetadataReaderFromJson implements LayoutMetadataReader {

        public Properties asProperties(final Class<?> domainClass) {
        final LayoutMetadata metadata;
        try {
            metadata = readMetadata(domainClass);
        } catch (final Exception e) {
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
        
        final int[] memberSeq = {0};
        setProperties(metadata, props, memberSeq);
        setCollections(metadata, props, memberSeq);
        setFreestandingActions(metadata, props);

        return props;
    }

    private static void setMemberGroupLayoutColumnSpans(final LayoutMetadata metadata, final Properties props) {
        final List<ColumnRepr> columns = metadata.getColumns();
        final String columnSpansStr = Joiner.on(",").join(Iterables.transform(columns, new Function<ColumnRepr,Integer>(){
            @Override
            public Integer apply(final ColumnRepr input) {
                return input.span;
            }}));
        props.setProperty("class.memberGroupLayout.columnSpans", columnSpansStr);
    }

    private static void setMemberGroupLayoutColumnLists(final LayoutMetadata metadata, final int colIdx, final String propkey, final Properties props) {
        final ColumnRepr column = metadata.getColumns().get(colIdx);
        final Map<String, MemberGroupRepr> memberGroups = column.memberGroups;
        final String val = memberGroups != null ? Joiner.on(",").join(memberGroups.keySet()) : "";
        props.setProperty("class.memberGroupLayout." + propkey, val);
    }

    private static void setProperties(final LayoutMetadata metadata, final Properties props, final int[] memberSeq) {
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

    private static void setCollections(final LayoutMetadata metadata, final Properties props, final int[] memberSeq) {
        final ColumnRepr columnRepr = metadata.getColumns().get(3);
        final Map<String, MemberRepr> collections = columnRepr.collections;
        setMembersAndAssociatedActions(props, null, collections, memberSeq);
    }

    private static void setMembersAndAssociatedActions(final Properties props, final String memberGroupName, final Map<String, MemberRepr> members, final int[] memberSeq) {
        for(final String memberName: members.keySet()) {
            props.setProperty("member." + memberName + ".memberOrder.sequence", ""+ ++memberSeq[0]);
            if(memberGroupName != null) {
                props.setProperty("member." + memberName + ".memberOrder.name", memberGroupName);
            }
            
            final MemberRepr memberRepr = members.get(memberName);

            // actions

            final Map<String, ActionRepr> actions = memberRepr.actions;
            if(actions != null) {
                int actSeq = 0;
                for(final String actionName: actions.keySet()) {
                    final ActionRepr actionRepr = actions.get(actionName);
                    final String nameKey = "action." + actionName + ".memberOrder.name";
                    props.setProperty(nameKey, memberName);
                    setRemainingActionProperties(props, "action", actionName, actionRepr, ++actSeq);
                }
            }

            // propertyLayout

            final PropertyLayoutFacetRepr propertyLayout = memberRepr.propertyLayout;
            if(propertyLayout!= null) {
                if(propertyLayout.cssClass != null) {
                    props.setProperty("member." + memberName + ".propertyLayout.cssClass", propertyLayout.cssClass);
                }
                if(propertyLayout.describedAs != null) {
                    props.setProperty("member." + memberName + ".propertyLayout.describedAs", propertyLayout.describedAs);
                }
                if(propertyLayout.hidden != null) {
                    props.setProperty("member." + memberName + ".propertyLayout.hidden", ""+propertyLayout.hidden);
                }
                if(propertyLayout.labelPosition != null) {
                    props.setProperty("member." + memberName + ".propertyLayout.labelPosition", ""+propertyLayout.labelPosition);
                }
                if(propertyLayout.multiLine > 1) {
                    props.setProperty("member." + memberName + ".propertyLayout.multiLine", "" + propertyLayout.multiLine);
                }
                if(propertyLayout.named != null) {
                    props.setProperty("member." + memberName + ".propertyLayout.named", propertyLayout.named);
                }
                //
                {
                    props.setProperty("member." + memberName + ".propertyLayout.namedEscaped", ""+propertyLayout.namedEscaped);
                }
                //
                {
                    props.setProperty("member." + memberName + ".propertyLayout.renderedAsDayBefore", "" + propertyLayout.renderedAsDayBefore);
                }
                if(propertyLayout.typicalLength > 0) {
                    props.setProperty("member." + memberName + ".propertyLayout.typicalLength", "" + propertyLayout.typicalLength);
                }
            }

            // collectionLayout
            final CollectionLayoutFacetRepr collectionLayout = memberRepr.collectionLayout;
            if(collectionLayout!= null) {
                if(collectionLayout.cssClass != null) {
                    props.setProperty("member." + memberName + ".collectionLayout.cssClass", collectionLayout.cssClass);
                }
                if(collectionLayout.describedAs != null) {
                    props.setProperty("member." + memberName + ".collectionLayout.describedAs", collectionLayout.describedAs);
                }
                if(collectionLayout.hidden != null) {
                    props.setProperty("member." + memberName + ".collectionLayout.hidden", ""+collectionLayout.hidden);
                }
                if(collectionLayout.named != null) {
                    props.setProperty("member." + memberName + ".collectionLayout.named", collectionLayout.named);
                }
                //
                {
                    props.setProperty("member." + memberName + ".collectionLayout.namedEscaped", ""+collectionLayout.namedEscaped);
                }
                if(collectionLayout.paged > 0) {
                    props.setProperty("member." + memberName + ".collectionLayout.paged", "" + collectionLayout.paged);
                }
                if(collectionLayout.render != null) {
                    props.setProperty("member." + memberName + ".collectionLayout.render", ""+collectionLayout.render);
                }
                if(collectionLayout.sortedBy != null) {
                    props.setProperty("member." + memberName + ".collectionLayout.sortedBy", collectionLayout.sortedBy);
                }
            }


            // deprecated - properties & collections

            final CssClassFacetRepr cssClass = memberRepr.cssClass;
            if(cssClass!= null) {
                props.setProperty("member." + memberName + ".cssClass.value", cssClass.value);
            }
            final DescribedAsFacetRepr describedAs = memberRepr.describedAs;
            if(describedAs!= null) {
                props.setProperty("member." + memberName + ".describedAs.value", describedAs.value);
            }

            final DisabledFacetRepr disabled = memberRepr.disabled;
            if(disabled != null) {
                // same default as in Disabled.when()
                final When disabledWhen = disabled.when!=null?disabled.when: When.ALWAYS;
                props.setProperty("member." + memberName + ".disabled.when", disabledWhen.toString());
                // same default as in Disabled.where()
                final Where disabledWhere = disabled.where!=null?disabled.where: Where.ANYWHERE;
                props.setProperty("member." + memberName + ".disabled.where", disabledWhere.toString());
                // same default as in Disabled.reason()
                final String disabledReason = disabled.reason!=null?disabled.reason: "";
                props.setProperty("member." + memberName + ".disabled.reason", disabledReason);
            }
            final HiddenFacetRepr hidden = memberRepr.hidden;
            if(hidden != null) {
                // same default as in Hidden.when()
                final When hiddenWhen = hidden.when!=null?hidden.when: When.ALWAYS;
                props.setProperty("member." + memberName + ".hidden.when", hiddenWhen.toString());
                // same default as in Hidden.where()
                final Where hiddenWhere = hidden.where!=null?hidden.where: Where.ANYWHERE;
                props.setProperty("member." + memberName + ".hidden.where", hiddenWhere.toString());
            }

            final NamedFacetRepr named = memberRepr.named;
            if(named != null) {
                props.setProperty("member." + memberName + ".named.value", named.value);
            }

            // deprecated - properties

            final MultiLineFacetRepr multiLine = memberRepr.multiLine;
            if(multiLine!= null) {
                props.setProperty("member." + memberName + ".multiLine.numberOfLines", ""+multiLine.numberOfLines);
            }
            final TypicalLengthFacetRepr typicalLength = memberRepr.typicalLength;
            if(typicalLength!= null) {
                props.setProperty("member." + memberName + ".typicalLength.value", ""+typicalLength.value);
            }

            // deprecated - collections

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
            
        }
    }

    private static void setFreestandingActions(final LayoutMetadata metadata, final Properties props) {
        if(metadata.getActions() == null) {
            return;
        }
        int xeq=0;
        final Map<String, ActionRepr> actions = metadata.getActions();
        for (final String actionName : actions.keySet()) {
            final ActionRepr actionRepr = actions.get(actionName);
            setRemainingActionProperties(props, "member", actionName, actionRepr, ++xeq);
        }
    }

    private static void setRemainingActionProperties(
            final Properties props,
            final String prefix,
            final String actionNameOrig,
            final ActionRepr actionRepr,
            final int seq) {

        final String actionName = actionNameOrig + ("action".equals(prefix)?"":"()");
        props.setProperty(prefix + "." + actionName + ".memberOrder.sequence", ""+ seq);

        final ActionLayoutFacetRepr actionLayout = actionRepr.actionLayout;
        if(actionLayout != null) {
            if(actionLayout.bookmarking != null) {
                props.setProperty(prefix + "." + actionName + ".actionLayout.bookmarking", ""+actionLayout.bookmarking);
            }
            if(actionLayout.cssClass != null) {
                props.setProperty(prefix + "." + actionName + ".actionLayout.cssClass", actionLayout.cssClass);
            }
            if(actionLayout.cssClassFa != null) {
                props.setProperty(prefix + "." + actionName + ".actionLayout.cssClassFa", actionLayout.cssClassFa);
            }
            if(actionLayout.cssClassFaPosition != null) {
                props.setProperty(prefix + "." + actionName + ".actionLayout.cssClassFaPosition", actionLayout.cssClassFaPosition);
            }
            if(actionLayout.describedAs != null) {
                props.setProperty(prefix + "." + actionName + ".actionLayout.describedAs", actionLayout.describedAs);
            }
            if(actionLayout.hidden != null) {
                props.setProperty(prefix + "." + actionName + ".actionLayout.hidden", ""+actionLayout.hidden);
            }
            if(actionLayout.named != null) {
                props.setProperty(prefix + "." + actionName + ".actionLayout.named", actionLayout.named);
            }
            //
            {
                props.setProperty(prefix + "." + actionName + ".actionLayout.namedEscaped", ""+actionLayout.namedEscaped);
            }
            if(actionLayout.position != null) {
                props.setProperty(prefix + "." + actionName + ".actionLayout.position", ""+actionLayout.position);
            }
        }


        // deprecated

        final CssClassFacetRepr cssClass = actionRepr.cssClass;
        if(cssClass!= null) {
            props.setProperty(prefix +"." + actionName + ".cssClass.value", cssClass.value);
        }
        final CssClassFaFacetRepr cssClassFa = actionRepr.cssClassFa;
        if(cssClassFa != null) {
            props.setProperty(prefix +"." + actionName + ".cssClassFa.value", cssClassFa.value);
            props.setProperty(prefix +"." + actionName + ".cssClassFa.position", cssClassFa.position);
        }
        final DescribedAsFacetRepr describedAs = actionRepr.describedAs;
        if(describedAs!= null) {
            props.setProperty(prefix +"." + actionName + ".describedAs.value", describedAs.value);
        }
        final NamedFacetRepr actionNamed = actionRepr.named;
        if(actionNamed != null) {
            props.setProperty(prefix +"." + actionName + ".named.value", actionNamed.value);
        }
    }

    public LayoutMetadata asLayoutMetadata(final Class<?> domainClass) throws ReaderException {
        try {
            return readMetadata(domainClass);
        } catch (IOException | RuntimeException e) {
            throw new ReaderException(e);
        }
    }

    // //////////////////////////////////////

    private LayoutMetadata readMetadata(final Class<?> domainClass) throws IOException {
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
    public String asJson(final ObjectSpecification objectSpec) {
        final LayoutMetadata metadata = new LayoutMetadata();
        metadata.setColumns(Lists.<ColumnRepr>newArrayList());
        
        final MemberGroupLayoutFacet mglf = objectSpec.getFacet(MemberGroupLayoutFacet.class);
        final ColumnSpans columnSpans = mglf.getColumnSpans();
        
        final Set<String> actionIdsForAssociations = Sets.newTreeSet();
        
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

    private static void updateColumnMemberGroups(final ObjectSpecification objectSpec, final MemberGroupLayoutHint hint, final ColumnRepr columnRepr, final Set<String> actionIdsForAssociations) {
        final List<ObjectAssociation> objectAssociations = propertiesOf(objectSpec);
        final Map<String, List<ObjectAssociation>> associationsByGroup = ObjectAssociation.Util.groupByMemberOrderName(objectAssociations);
        
        final List<String> groupNames = ObjectSpecifications.orderByMemberGroups(objectSpec, associationsByGroup.keySet(), hint);
        
        columnRepr.memberGroups = Maps.newLinkedHashMap();
        for (final String groupName : groupNames) {
            final MemberGroupRepr memberGroupRepr = new MemberGroupRepr();
            columnRepr.memberGroups.put(groupName, memberGroupRepr);
            final List<ObjectAssociation> associationsInGroup = associationsByGroup.get(groupName);
            memberGroupRepr.members = Maps.newLinkedHashMap();
            if(associationsInGroup == null) {
                continue;
            }
            for (final ObjectAssociation assoc : associationsInGroup) {
                final MemberRepr memberRepr = newMemberRepr(objectSpec, assoc, actionIdsForAssociations);
                memberGroupRepr.members.put(assoc.getId(), memberRepr);
            }
        }
    }
    private static void addActions(final ObjectSpecification objectSpec, final LayoutMetadata metadata, final Set<String> actionIdsForAssociations) {
        final Map<String, ActionRepr> actions = Maps.newLinkedHashMap();
        final List<ObjectAction> actionsOf = actionsOf(objectSpec, actionIdsForAssociations);
        for(final ObjectAction action: actionsOf) {
            actions.put(action.getId(), newActionRepr(objectSpec, action));
        }
        metadata.setActions(actions);
    }

    private static ActionRepr newActionRepr(final ObjectSpecification objectSpec, final ObjectAction action) {
        final ActionRepr actionRepr = new ActionRepr();
        
        final CssClassFacet cssClassFacet = action.getFacet(CssClassFacet.class);
        if(cssClassFacet != null && !cssClassFacet.isNoop()) {
            final CssClassFacetRepr cssClassFacetRepr = new CssClassFacetRepr();
            cssClassFacetRepr.value = cssClassFacet.cssClass(null);
            actionRepr.cssClass = cssClassFacetRepr;
        }
        final DescribedAsFacet describedAsFacet = action.getFacet(DescribedAsFacet.class);
        if(describedAsFacet != null && !describedAsFacet.isNoop() && !Strings.isNullOrEmpty(describedAsFacet.value())) {
            final DescribedAsFacetRepr describedAsFacetRepr = new DescribedAsFacetRepr();
            describedAsFacetRepr.value = describedAsFacet.value();
            actionRepr.describedAs = describedAsFacetRepr;
        }
        final NamedFacet namedFacet = action.getFacet(NamedFacet.class);
        if(namedFacet != null && !namedFacet.isNoop()) {
            final NamedFacetRepr namedFacetRepr = new NamedFacetRepr();
            namedFacetRepr.value = namedFacet.value();
            actionRepr.named = namedFacetRepr;
        }
        
        return actionRepr;
    }

    private static void updateCollectionColumnRepr(final ObjectSpecification objectSpec, final ColumnRepr columnRepr, final Set<String> actionIdsOfAssociations) {
        final List<ObjectAssociation> objectAssociations = collectionsOf(objectSpec);
        columnRepr.collections = Maps.newLinkedHashMap();
        for(final ObjectAssociation assoc: objectAssociations) {
            final MemberRepr memberRepr = newMemberRepr(objectSpec, assoc, actionIdsOfAssociations);
            columnRepr.collections.put(assoc.getId(), memberRepr);
        }
    }


    private static MemberRepr newMemberRepr(final ObjectSpecification objectSpec, final ObjectAssociation assoc, final Set<String> actionIdsForAssociations) {
        final MemberRepr memberRepr = new MemberRepr();

        final CssClassFacet cssClassFacet = assoc.getFacet(CssClassFacet.class);
        if(cssClassFacet != null && !cssClassFacet.isNoop()) {
            final CssClassFacetRepr cssClassFacetRepr = new CssClassFacetRepr();
            cssClassFacetRepr.value = cssClassFacet.cssClass(null);
            memberRepr.cssClass = cssClassFacetRepr;
        }
        final DescribedAsFacet describedAsFacet = assoc.getFacet(DescribedAsFacet.class);
        if(describedAsFacet != null && !describedAsFacet.isNoop() && !Strings.isNullOrEmpty(describedAsFacet.value())) {
            final DescribedAsFacetRepr describedAsFacetRepr = new DescribedAsFacetRepr();
            describedAsFacetRepr.value = describedAsFacet.value();
            memberRepr.describedAs = describedAsFacetRepr;
        }
        final NamedFacet namedFacet = assoc.getFacet(NamedFacet.class);
        if(namedFacet != null && !namedFacet.isNoop()) {
            final NamedFacetRepr namedFacetRepr = new NamedFacetRepr();
            namedFacetRepr.value = namedFacet.value();
            memberRepr.named = namedFacetRepr;
        }
        final DisabledFacet disabledFacet = assoc.getFacet(DisabledFacet.class);
        if(disabledFacet != null && !disabledFacet.isNoop()) {
            final DisabledFacetRepr disabledFacetRepr = new DisabledFacetRepr();
            if(disabledFacet instanceof DisabledFacetAbstractImpl) {
                final DisabledFacetAbstractImpl disabledFacetImpl = (DisabledFacetAbstractImpl) disabledFacet;
                disabledFacetRepr.reason = Strings.emptyToNull(disabledFacetImpl.getReason());
            }
            disabledFacetRepr.when = whenAlwaysToNull(disabledFacet.when());
            disabledFacetRepr.where = whereAnywhereToNull(disabledFacet.where());
            memberRepr.disabled = disabledFacetRepr;
        }
        // relies on the fact that HiddenFacetAbstract is multi-typed
        final HiddenFacet hiddenFacet = assoc.getFacet(HiddenFacet.class);
        if(hiddenFacet != null && !hiddenFacet.isNoop()) {
            final HiddenFacetRepr hiddenFacetRepr = new HiddenFacetRepr();
            hiddenFacetRepr.when = whenAlwaysToNull(hiddenFacet.when());
            hiddenFacetRepr.where = whereAnywhereToNull(hiddenFacet.where());
            memberRepr.hidden = hiddenFacetRepr;
        }
        final MultiLineFacet multiLineFacet = assoc.getFacet(MultiLineFacet.class);
        if(multiLineFacet != null && !multiLineFacet.isNoop()) {
            final MultiLineFacetRepr multiLineFacetRepr = new MultiLineFacetRepr();
            multiLineFacetRepr.numberOfLines = multiLineFacet.numberOfLines();
            memberRepr.multiLine = multiLineFacetRepr;
        }
        final PagedFacet pagedFacet = assoc.getFacet(PagedFacet.class);
        if(pagedFacet != null && !pagedFacet.isNoop()) {
            final PagedFacetRepr pagedFacetRepr = new PagedFacetRepr();
            pagedFacetRepr.value = pagedFacet.value();
            memberRepr.paged = pagedFacetRepr;
        }
        final RenderFacet renderFacet = assoc.getFacet(RenderFacet.class);
        if(renderFacet != null && !renderFacet.isNoop()) {
            final RenderFacetRepr renderFacetRepr = new RenderFacetRepr();
            renderFacetRepr.value = renderFacet.value();
            memberRepr.render = renderFacetRepr;
        }
        final TypicalLengthFacet typicalLengthFacet = assoc.getFacet(TypicalLengthFacet.class);
        if(typicalLengthFacet != null && !typicalLengthFacet.isNoop()) {
            final TypicalLengthFacetRepr typicalLengthFacetRepr = new TypicalLengthFacetRepr();
            typicalLengthFacetRepr.value = typicalLengthFacet.value();
            memberRepr.typicalLength = typicalLengthFacetRepr;
        }

        final List<ObjectAction> actions = objectSpec.getObjectActions(
                ActionType.USER, Contributed.INCLUDED, ObjectAction.Filters.memberOrderOf(assoc));
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

    private static Where whereAnywhereToNull(final Where where) {
        return where != Where.ANYWHERE? where: null;
    }

    private static When whenAlwaysToNull(final When when) {
        return when != When.ALWAYS? when: null;
    }

    private static void sortByMemberOrderFacet(final List<ObjectAction> actions) {
        Collections.sort(actions, new Comparator<ObjectAction>() {

            @Override
            public int compare(final ObjectAction o1, final ObjectAction o2) {
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

    
    private static List<ObjectAssociation> propertiesOf(final ObjectSpecification objSpec) {
        return objSpec.getAssociations(Contributed.EXCLUDED, ObjectAssociation.Filters.PROPERTIES);
    }
    private static List<ObjectAssociation> collectionsOf(final ObjectSpecification objSpec) {
        return objSpec.getAssociations(Contributed.EXCLUDED, ObjectAssociation.Filters.COLLECTIONS);
    }
    private static List<ObjectAction> actionsOf(final ObjectSpecification objSpec, final Set<String> excludedActionIds) {
        return objSpec.getObjectActions(ActionType.ALL, Contributed.INCLUDED, excluding(excludedActionIds));
    }

    @SuppressWarnings({ "deprecation" })
    private static Filter<ObjectAction> excluding(final Set<String> excludedActionIds) {
        return new Filter<ObjectAction>(){
                    @Override
                    public boolean accept(final ObjectAction t) {
                        return !excludedActionIds.contains(t.getId());
                    }
                };
    }

    @Override
    public String toString() {
        return getClass().getName();
    }

}
