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
package org.apache.causeway.core.runtimeservices.menubars.bootstrap;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.annotation.DomainServiceLayout;
import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.layout.component.ServiceActionLayoutData;
import org.apache.causeway.applib.layout.menubars.bootstrap.BSMenu;
import org.apache.causeway.applib.layout.menubars.bootstrap.BSMenuBar;
import org.apache.causeway.applib.layout.menubars.bootstrap.BSMenuBars;
import org.apache.causeway.applib.layout.menubars.bootstrap.BSMenuSection;
import org.apache.causeway.applib.services.jaxb.JaxbService;
import org.apache.causeway.applib.services.menu.MenuBarsLoaderService;
import org.apache.causeway.applib.services.menu.MenuBarsMarshallerService;
import org.apache.causeway.applib.services.menu.MenuBarsService;
import org.apache.causeway.applib.services.message.MessageService;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.base._Lazy;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.internal.collections._Lists;
import org.apache.causeway.commons.internal.collections._Maps;
import org.apache.causeway.commons.internal.collections._Sets;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.facetapi.Facet.Precedence;
import org.apache.causeway.core.metamodel.facetapi.FacetUtil;
import org.apache.causeway.core.metamodel.facets.actions.layout.CssClassFacetForMenuBarXml;
import org.apache.causeway.core.metamodel.facets.actions.layout.FaFacetForMenuBarXml;
import org.apache.causeway.core.metamodel.facets.actions.layout.MemberDescribedFacetForMenuBarXml;
import org.apache.causeway.core.metamodel.facets.actions.layout.MemberNamedFacetForMenuBarXml;
import org.apache.causeway.core.metamodel.facets.all.i8n.staatic.HasStaticText;
import org.apache.causeway.core.metamodel.facets.all.named.MemberNamedFacet;
import org.apache.causeway.core.metamodel.facets.members.layout.group.LayoutGroupFacet;
import org.apache.causeway.core.metamodel.facets.object.domainservicelayout.DomainServiceLayoutFacet;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.services.grid.GridServiceDefault;
import org.apache.causeway.core.metamodel.spec.ActionScope;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.runtimeservices.CausewayModuleCoreRuntimeServices;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

/**
 * Default implementation of {@link MenuBarsService}, supporting DTOs
 * modelled on <a href="https://getbootstrap.com/">Bootstrap</a> CSS/JS design system.
 *
 * @since 2.0 {@index}
 */
@Service
@Named(CausewayModuleCoreRuntimeServices.NAMESPACE + ".MenuBarsServiceBootstrap")
@Priority(PriorityPrecedence.MIDPOINT)
@Qualifier("BS")
@RequiredArgsConstructor(onConstructor_ = {@Inject})
@Slf4j
public class MenuBarsServiceBootstrap
implements MenuBarsService {

    public static final String MB3_TNS = "https://causeway.apache.org/applib/layout/menubars/bootstrap3";
    public static final String MB3_SCHEMA_LOCATION = "https://causeway.apache.org/applib/layout/menubars/bootstrap3/menubars.xsd";

    public static final String COMPONENT_TNS = GridServiceDefault.COMPONENT_TNS;
    public static final String COMPONENT_SCHEMA_LOCATION = GridServiceDefault.COMPONENT_SCHEMA_LOCATION;

    public static final String LINKS_TNS = GridServiceDefault.LINKS_TNS;
    public static final String LINKS_SCHEMA_LOCATION = GridServiceDefault.LINKS_SCHEMA_LOCATION;

    private final MenuBarsLoaderService loader;

    @Getter(onMethod_={@Override}) @Accessors(fluent = true)
    private final MenuBarsMarshallerService<BSMenuBars> marshaller;
    private final MessageService messageService;
    private final JaxbService jaxbService;
    private final MetaModelContext metaModelContext;

    private final _Lazy<BSMenuBars> menuBarsFromAnnotationsOnly =
            _Lazy.threadSafe(this::menuBarsFromAnnotationsOnly);

    BSMenuBars menuBars;

    @Override
    public BSMenuBars menuBars(final Type type) {
        var menuBarsFromAnnotationsOnly = this.menuBarsFromAnnotationsOnly.get();
        if(type == Type.ANNOTATED) {
            return menuBarsFromAnnotationsOnly;
        }
        return menuBarsDefault();
    }

    // -- HELPER

    private BSMenuBars menuBarsDefault() {
        var menuBarsFromAnnotationsOnly = this.menuBarsFromAnnotationsOnly.get();
        // load (and only fallback if nothing could be loaded)...
        if(menuBars == null || loader.supportsReloading()) {
            this.menuBars = loadOrElse(menuBarsFromAnnotationsOnly);
        }
        return menuBars;
    }

    private BSMenuBars loadOrElse(
            final BSMenuBars menuBarsFromAnnotationsOnly) {

        var menuBars = loader.menuBars(marshaller)
                .map(this::updateFacetsFromActionLayoutXml)
                .map(this::addTnsAndSchemaLocation)
                .orElse(menuBarsFromAnnotationsOnly);

        var unreferencedActionsMenu = validateAndGetUnreferencedActionMenu(menuBars);
        if (unreferencedActionsMenu == null) {
            // just use fallback
            return menuBarsFromAnnotationsOnly;
        }

        // add in any missing actions from the fallback
        var referencedActionsByObjectTypeAndId = menuBars.getAllServiceActionsByObjectTypeAndId();

        menuBarsFromAnnotationsOnly.visit(BSMenuBars.VisitorAdapter.visitingMenuSections(menuSection->{

            // created only if required to collect unreferenced actions
            // for this menuSection into a new section within the designated
            // unreferencedActionsMenu
            BSMenuSection section = null;

            for (var serviceActionLayout : menuSection.getServiceActions()) {
                var logicalTypeNameAndId = serviceActionLayout.getLogicalTypeNameAndId();
                var isReferencedAction = referencedActionsByObjectTypeAndId.containsKey(logicalTypeNameAndId);

                if (isReferencedAction) {
                    continue; // check next
                }

                if(section == null) {
                    section = addSectionToMenu(unreferencedActionsMenu);
                }

                bindActionToSection(serviceActionLayout, section);
            }

        }));

        return menuBars;
    }

    private BSMenuBars updateFacetsFromActionLayoutXml(final BSMenuBars menuBarsFromXml) {
        final Map<String, ServiceActionLayoutData> serviceActionLayoutDataByActionId = _Maps.newHashMap();
        menuBarsFromXml.visit(serviceActionLayoutData->
            serviceActionLayoutDataByActionId.put(
                    serviceActionLayoutData.getLogicalTypeNameAndId(),
                    serviceActionLayoutData));

        metaModelContext.streamServiceAdapters()
        .filter(this::isVisibleAdapterForMenu)
        .forEach(serviceAdapter->{

            var serviceSpec = serviceAdapter.objSpec();

            serviceSpec.streamAnyActions(MixedIn.INCLUDED)
            .forEach(objectAction->{

                var serviceActionIdentifier = objectAction.getFeatureIdentifier();

                var actionId = serviceActionIdentifier.logicalTypeName()
                        + "#" + serviceActionIdentifier.memberLogicalName();

                var layoutData = serviceActionLayoutDataByActionId.get(actionId);

                FacetUtil.updateFacetIfPresent(
                        MemberNamedFacetForMenuBarXml.create(layoutData, objectAction));

                FacetUtil.updateFacetIfPresent(
                        MemberDescribedFacetForMenuBarXml.create(layoutData, objectAction));

                FacetUtil.updateFacetIfPresent(
                        CssClassFacetForMenuBarXml.create(layoutData, objectAction));

                FacetUtil.updateFacetIfPresent(
                        FaFacetForMenuBarXml.create(layoutData, objectAction));

            });

        });

        return menuBarsFromXml;
    }

    private BSMenuBars addTnsAndSchemaLocation(final BSMenuBars menuBars) {
        menuBars.setTnsAndSchemaLocation(tnsAndSchemaLocation());
        return menuBars;
    }

    private static BSMenuSection addSectionToMenu(final BSMenu menu) {
        var section = new BSMenuSection();
        menu.getSections().add(section);
        return section;
    }

    private static void bindActionToSection(
            final ServiceActionLayoutData serviceAction,
            final BSMenuSection section) {

        // detach from fallback, attach to this section
        serviceAction.setOwner(section);
        section.getServiceActions().add(serviceAction);
    }

    private BSMenu validateAndGetUnreferencedActionMenu(final BSMenuBars menuBars) {

        if (menuBars == null) {
            return null;
        }

        var menusWithUnreferencedActionsFlagSet = _Lists.<BSMenu>newArrayList();
        menuBars.visit(BSMenuBars.VisitorAdapter.visitingMenus(menu->{
            if(Boolean.TRUE.equals(menu.isUnreferencedActions())) {
                menusWithUnreferencedActionsFlagSet.add(menu);
            }
        }));

        var size = menusWithUnreferencedActionsFlagSet.size();
        if (size == 1) {
            return menusWithUnreferencedActionsFlagSet.get(0);
        }

        menuBars.setMetadataError(
                "Exactly one menu must have 'unreferencedActions' flag set; found " + size + " such menus");
        if(metaModelContext.getSystemEnvironment().isPrototyping()) {
            messageService.warnUser("Menubars metadata errors; check the error log");
        }
        log.error("Menubar layout metadata errors:\n\n{}\n\n", jaxbService.toXml(menuBars));

        return null;
    }

    private BSMenuBars menuBarsFromAnnotationsOnly() {
        final BSMenuBars menuBars = new BSMenuBars();

        var visibleServiceAdapters = metaModelContext.streamServiceAdapters()
                .filter(this::isVisibleAdapterForMenu)
                .collect(Can.toCan());

        appendFromAnnotationsOnly(visibleServiceAdapters, menuBars.getPrimary(), DomainServiceLayout.MenuBar.PRIMARY);
        appendFromAnnotationsOnly(visibleServiceAdapters, menuBars.getSecondary(), DomainServiceLayout.MenuBar.SECONDARY);
        appendFromAnnotationsOnly(visibleServiceAdapters, menuBars.getTertiary(), DomainServiceLayout.MenuBar.TERTIARY);

        menuBars.setTnsAndSchemaLocation(tnsAndSchemaLocation());

        final BSMenu otherMenu = new BSMenu();
        otherMenu.setNamed("Other");
        otherMenu.setUnreferencedActions(true);
        menuBars.getPrimary().getMenus().add(otherMenu);

        return menuBars;
    }

    private boolean isVisibleAdapterForMenu(final ManagedObject objectAdapter) {
        var spec = objectAdapter.objSpec();
        if (spec.isHidden()) {
            // however, this isn't the same as HiddenObjectFacet, so doesn't filter out
            // services that have an imperative hidden() method.
            return false;
        }
        return true;
    }

    private void appendFromAnnotationsOnly(
            final Can<ManagedObject> serviceAdapters,
            final BSMenuBar menuBar,
            final DomainServiceLayout.MenuBar menuBarPos) {

        var serviceActions = _Lists.<ServiceAndAction>newArrayList();

        // cf ServiceActionsModel & ServiceActionUtil#buildMenu in Wicket viewer
        serviceAdapters.stream()
        .filter(with(menuBarPos))
        .forEach(serviceAdapter->{

            streamServiceActions(serviceAdapter, ActionScope.PRODUCTION).forEach(serviceActions::add);
            streamServiceActions(serviceAdapter, ActionScope.PROTOTYPE).forEach(serviceActions::add);

        });

        final Set<String> serviceNamesInOrder = serviceNamesInOrder(serviceAdapters, serviceActions);
        final Map<String, List<ServiceAndAction>> serviceActionsByName = groupByServiceName(serviceActions);

        // prune any service names that have no service actions
        serviceNamesInOrder.retainAll(serviceActionsByName.keySet());

        List<BSMenu> menus = buildMenuItemsFromAnnotationsOnly(serviceNamesInOrder, serviceActionsByName);
        menuBar.getMenus().addAll(menus);
    }

    private static List<BSMenu> buildMenuItemsFromAnnotationsOnly(
            final Set<String> serviceNamesInOrder,
            final Map<String, List<ServiceAndAction>> serviceActionsByName) {

        final List<BSMenu> menus = _Lists.newArrayList();
        for (String serviceName : serviceNamesInOrder) {

            BSMenu menu = new BSMenu(serviceName);
            menus.add(menu);

            BSMenuSection menuSection = new BSMenuSection();
            final List<ServiceAndAction> serviceActionsForName = serviceActionsByName.get(serviceName);
            for (ServiceAndAction serviceAndAction : serviceActionsForName) {

                if(serviceAndAction.isPrependSeparator() && !menuSection.getServiceActions().isEmpty()) {
                    menu.getSections().add(menuSection);
                    menuSection = new BSMenuSection();
                }

                var objectAction = serviceAndAction.getObjectAction();
                //var service = serviceAndAction.getServiceAdapter();
                var logicalTypeName = serviceAndAction.getServiceAdapter().objSpec().logicalTypeName();
                var actionLayoutData = new ServiceActionLayoutData(logicalTypeName, objectAction.getId());

                var named = objectAction
                .getFacetRanking(MemberNamedFacet.class)
                // assuming layout from annotations never installs higher than Precedence.DEFAULT
                .flatMap(facetRanking->facetRanking.getWinnerNonEventLowerOrEqualTo(MemberNamedFacet.class, Precedence.DEFAULT))
                .map(MemberNamedFacet::getSpecialization)
                .flatMap(specialization->specialization.left())
                .map(HasStaticText::translated)
                //we have a facet-post-processor to ensure following code path is unreachable,
                //however, we keep it in avoidance of fatal failure
                .orElseGet(()->objectAction.getFeatureIdentifier().getMemberNaturalName());

                actionLayoutData.setNamed(named); //that's for imperative naming ... objectAction.getFriendlyName(service.asProvider()));
                menuSection.getServiceActions().add(actionLayoutData);
            }
            if(!menuSection.getServiceActions().isEmpty()) {
                menu.getSections().add(menuSection);
            }
        }
        return menus;
    }

    /**
     * The unique service names, as they appear in order of the provided List of {@link ServiceAndAction}s.
     *
     * straight copy from Wicket UI
     */
    private static Set<String> serviceNamesInOrder(
            final Can<ManagedObject> serviceAdapters,
            final List<ServiceAndAction> serviceActions) {
        final Set<String> serviceNameOrder = _Sets.newLinkedHashSet();

        // first, order as defined in causeway.properties
        for (ManagedObject serviceAdapter : serviceAdapters) {
            var serviceSpec = serviceAdapter.objSpec();
            // assuming services always provide singular NounForm
            String serviceName = serviceSpec.getSingularName();
            serviceNameOrder.add(serviceName);
        }
        // then, any other services (eg due to misspellings, at the end)
        for (ServiceAndAction serviceAction : serviceActions) {
            if(!serviceNameOrder.contains(serviceAction.getServiceName())) {
                serviceNameOrder.add(serviceAction.getServiceName());
            }
        }
        return serviceNameOrder;
    }

    /**
     * Group the provided {@link ServiceAndAction}s by their service name.
     *
     * straight copy from Wicket UI
     */
    private static Map<String, List<ServiceAndAction>> groupByServiceName(
            final List<ServiceAndAction> serviceActions) {
        var serviceActionsByName = _Maps.<String, List<ServiceAndAction>>newTreeMap();

        // map available services
        ManagedObject lastServiceAdapter = null;

        for (ServiceAndAction serviceAction : serviceActions) {
            List<ServiceAndAction> serviceActionsForName =
                    serviceActionsByName.get(serviceAction.getServiceName());

            var serviceAdapter = serviceAction.getServiceAdapter();

            if(serviceActionsForName == null) {
                serviceActionsForName = _Lists.newArrayList();
                serviceActionsByName.put(serviceAction.getServiceName(), serviceActionsForName);
            } else {
                // capture whether this action is from a different service; if so, add a separator before it
                serviceAction.setPrependSeparator(lastServiceAdapter != serviceAdapter);
            }
            serviceActionsForName.add(serviceAction);
            lastServiceAdapter = serviceAdapter;
        }

        return serviceActionsByName;
    }

    private Stream<ServiceAndAction> streamServiceActions(
            final ManagedObject serviceAdapter,
            final ActionScope actionType) {

        final ObjectSpecification serviceSpec = serviceAdapter.objSpec();
        final Stream<ObjectAction> objectActions = serviceSpec.streamDeclaredActions(actionType, MixedIn.INCLUDED);

        return objectActions
                .map(objectAction->{
                    var layoutGroupFacet = objectAction.getFacet(LayoutGroupFacet.class);
                    String serviceName = layoutGroupFacet != null
                            ? layoutGroupFacet.getGroupId()
                            : null;
                    if(_Strings.isNullOrEmpty(serviceName)){
                        // assuming services always provide singular NounForm
                        serviceName = serviceSpec.getSingularName();
                    }
                    return new ServiceAndAction(serviceName, serviceAdapter, objectAction);
                });
    }

    private static Predicate<ManagedObject> with(final DomainServiceLayout.MenuBar menuBar) {
        return (final ManagedObject input) -> {
            final DomainServiceLayoutFacet facet =
                    input.objSpec().getFacet(DomainServiceLayoutFacet.class);
            return facet != null && facet.getMenuBar() == menuBar;
        };
    }

    private String tnsAndSchemaLocation() {

        return Stream.of(
                MB3_TNS,
                MB3_SCHEMA_LOCATION,

                COMPONENT_TNS,
                COMPONENT_SCHEMA_LOCATION,

                LINKS_TNS,
                LINKS_SCHEMA_LOCATION)
                .collect(Collectors.joining(" "));
    }

}
