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
package org.apache.isis.core.runtimeservices.menubars.bootstrap3;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.DomainServiceLayout;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.applib.layout.component.ServiceActionLayoutData;
import org.apache.isis.applib.layout.menubars.bootstrap3.BS3Menu;
import org.apache.isis.applib.layout.menubars.bootstrap3.BS3MenuBar;
import org.apache.isis.applib.layout.menubars.bootstrap3.BS3MenuBars;
import org.apache.isis.applib.layout.menubars.bootstrap3.BS3MenuSection;
import org.apache.isis.applib.services.jaxb.JaxbService;
import org.apache.isis.applib.services.menu.MenuBarsLoaderService;
import org.apache.isis.applib.services.menu.MenuBarsService;
import org.apache.isis.applib.services.message.MessageService;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.commons.internal.collections._Sets;
import org.apache.isis.core.config.environment.IsisSystemEnvironment;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.facets.actions.notinservicemenu.NotInServiceMenuFacet;
import org.apache.isis.core.metamodel.facets.all.named.NamedFacet;
import org.apache.isis.core.metamodel.facets.members.layout.group.LayoutGroupFacet;
import org.apache.isis.core.metamodel.facets.object.domainservice.DomainServiceFacet;
import org.apache.isis.core.metamodel.facets.object.domainservicelayout.DomainServiceLayoutFacet;
import org.apache.isis.core.metamodel.services.grid.GridServiceDefault;
import org.apache.isis.core.metamodel.spec.ActionType;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.MixedIn;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;

import lombok.val;
import lombok.extern.log4j.Log4j2;

@Service
@Named("isis.runtimeservices.MenuBarsServiceBS3")
@Order(OrderPrecedence.MIDPOINT)
@Primary
@Qualifier("BS3")
@Log4j2
public class MenuBarsServiceBS3 implements MenuBarsService {

    public static final String MB3_TNS = "http://isis.apache.org/applib/layout/menubars/bootstrap3";
    public static final String MB3_SCHEMA_LOCATION = "http://isis.apache.org/applib/layout/menubars/bootstrap3/menubars.xsd";

    public static final String COMPONENT_TNS = GridServiceDefault.COMPONENT_TNS;
    public static final String COMPONENT_SCHEMA_LOCATION = GridServiceDefault.COMPONENT_SCHEMA_LOCATION;

    public static final String LINKS_TNS = GridServiceDefault.LINKS_TNS;
    public static final String LINKS_SCHEMA_LOCATION = GridServiceDefault.LINKS_SCHEMA_LOCATION;

    @Inject private MenuBarsLoaderService menuBarsLoaderService;
    @Inject private MessageService messageService;
    @Inject private JaxbService jaxbService;
    @Inject private IsisSystemEnvironment isisSystemEnvironment;
    @Inject private MetaModelContext metaModelContext;

    BS3MenuBars menuBars;

    @Override
    public BS3MenuBars menuBars(final Type type) {

        val fallbackMenuBars = deriveMenuBarsFromMetaModelFacets();

        if(type == Type.FALLBACK) {
            return fallbackMenuBars;
        }

        // else load (and only fallback if nothing could be loaded)...
        if(menuBars == null || menuBarsLoaderService.supportsReloading()) {
            this.menuBars = loadOrElse(fallbackMenuBars);
        }

        return menuBars;
    }

    // -- HELPER

    private BS3MenuBars loadOrElse(BS3MenuBars fallbackMenuBars) {

        val menuBars = Optional.ofNullable(menuBarsLoaderService.menuBars())
                .map(this::addTnsAndSchemaLocation)
                .orElse(fallbackMenuBars);

        val unreferencedActionsMenu = validateAndGetUnreferencedActionMenu(menuBars);
        if (unreferencedActionsMenu == null) {
            // just use fallback
            return fallbackMenuBars;
        }

        // add in any missing actions from the fallback
        val referencedActionsByObjectTypeAndId = menuBars.getAllServiceActionsByObjectTypeAndId();

        fallbackMenuBars.visit(BS3MenuBars.VisitorAdapter.visitingMenuSections(menuSection->{

            // created only if required to collect unreferenced actions
            // for this menuSection into a new section within the designated
            // unreferencedActionsMenu
            BS3MenuSection section = null;

            for (val serviceActionLayout : menuSection.getServiceActions()) {
                val logicalTypeNameAndId = serviceActionLayout.getLogicalTypeNameAndId();
                val isReferencedAction = referencedActionsByObjectTypeAndId.containsKey(logicalTypeNameAndId);

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

    private BS3MenuBars addTnsAndSchemaLocation(BS3MenuBars menuBars) {
        menuBars.setTnsAndSchemaLocation(tnsAndSchemaLocation());
        return menuBars;
    }

    private static BS3MenuSection addSectionToMenu(BS3Menu menu) {
        val section = new BS3MenuSection();
        menu.getSections().add(section);
        return section;
    }

    private static void bindActionToSection(
            ServiceActionLayoutData serviceAction,
            BS3MenuSection section) {

        // detach from fallback, attach to this section
        serviceAction.setOwner(section);
        section.getServiceActions().add(serviceAction);
    }

    private BS3Menu validateAndGetUnreferencedActionMenu(final BS3MenuBars menuBars) {

        if (menuBars == null) {
            return null;
        }

        val menusWithUnreferencedActionsFlagSet = _Lists.<BS3Menu>newArrayList();
        menuBars.visit(BS3MenuBars.VisitorAdapter.visitingMenus(menu->{
            if(Boolean.TRUE.equals(menu.isUnreferencedActions())) {
                menusWithUnreferencedActionsFlagSet.add(menu);
            }
        }));

        val size = menusWithUnreferencedActionsFlagSet.size();
        if (size == 1) {
            return menusWithUnreferencedActionsFlagSet.get(0);
        }

        menuBars.setMetadataError(
                "Exactly one menu must have 'unreferencedActions' flag set; found " + size + " such menus");
        if(isisSystemEnvironment.isPrototyping()) {
            messageService.warnUser("Menubars metadata errors; check the error log");
        }
        log.error("Menubar layout metadata errors:\n\n{}\n\n", jaxbService.toXml(menuBars));

        return null;
    }

    private BS3MenuBars deriveMenuBarsFromMetaModelFacets() {
        final BS3MenuBars menuBars = new BS3MenuBars();

        final List<ManagedObject> visibleServiceAdapters = metaModelContext.streamServiceAdapters()
                .filter(this::isVisibleAdapterForMenu)
                .collect(Collectors.toList());

        append(visibleServiceAdapters, menuBars.getPrimary(), DomainServiceLayout.MenuBar.PRIMARY);
        append(visibleServiceAdapters, menuBars.getSecondary(), DomainServiceLayout.MenuBar.SECONDARY);
        append(visibleServiceAdapters, menuBars.getTertiary(), DomainServiceLayout.MenuBar.TERTIARY);

        menuBars.setTnsAndSchemaLocation(tnsAndSchemaLocation());

        final BS3Menu otherMenu = new BS3Menu();
        otherMenu.setNamed("Other");
        otherMenu.setUnreferencedActions(true);
        menuBars.getPrimary().getMenus().add(otherMenu);

        return menuBars;
    }

    private boolean isVisibleAdapterForMenu(ManagedObject objectAdapter) {
        val spec = objectAdapter.getSpecification();
        if (spec.isHidden()) {
            // however, this isn't the same as HiddenObjectFacet, so doesn't filter out
            // services that have an imperative hidden() method.
            return false;
        }
        return DomainServiceFacet.getNatureOfService(spec)
                .filter(NatureOfService::isView)
                .isPresent();
    }


    private void append(
            final List<ManagedObject> serviceAdapters,
            final BS3MenuBar menuBar,
            final DomainServiceLayout.MenuBar menuBarPos) {

        val serviceActions = _Lists.<ServiceAndAction>newArrayList();

        // cf ServiceActionsModel & ServiceActionUtil#buildMenu in Wicket viewer
        _NullSafe.stream(serviceAdapters)
        .filter(with(menuBarPos))
        .forEach(serviceAdapter->{

            streamServiceActions(serviceAdapter, ActionType.USER).forEach(serviceActions::add);
            streamServiceActions(serviceAdapter, ActionType.PROTOTYPE).forEach(serviceActions::add);

        });

        final Set<String> serviceNamesInOrder = serviceNamesInOrder(serviceAdapters, serviceActions);
        final Map<String, List<ServiceAndAction>> serviceActionsByName = groupByServiceName(serviceActions);

        // prune any service names that have no service actions
        serviceNamesInOrder.retainAll(serviceActionsByName.keySet());

        List<BS3Menu> menus = buildMenuItems(serviceNamesInOrder, serviceActionsByName);
        menuBar.getMenus().addAll(menus);
    }

    private static List<BS3Menu> buildMenuItems(
            final Set<String> serviceNamesInOrder,
            final Map<String, List<ServiceAndAction>> serviceActionsByName) {

        final List<BS3Menu> menus = _Lists.newArrayList();
        for (String serviceName : serviceNamesInOrder) {

            BS3Menu menu = new BS3Menu(serviceName);
            menus.add(menu);

            BS3MenuSection menuSection = new BS3MenuSection();
            final List<ServiceAndAction> serviceActionsForName = serviceActionsByName.get(serviceName);
            for (ServiceAndAction serviceAndAction : serviceActionsForName) {

                if(serviceAndAction.isPrependSeparator() && !menuSection.getServiceActions().isEmpty()) {
                    menu.getSections().add(menuSection);
                    menuSection = new BS3MenuSection();
                }

                ObjectAction objectAction = serviceAndAction.getObjectAction();
                final String logicalTypeName = serviceAndAction.getServiceAdapter().getSpecification().getLogicalTypeName();
                ServiceActionLayoutData action = new ServiceActionLayoutData(logicalTypeName, objectAction.getId());
                action.setNamed(objectAction.getName());
                menuSection.getServiceActions().add(action);
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
            final List<ManagedObject> serviceAdapters,
            final List<ServiceAndAction> serviceActions) {
        final Set<String> serviceNameOrder = _Sets.newLinkedHashSet();

        // first, order as defined in isis.properties
        for (ManagedObject serviceAdapter : serviceAdapters) {
            final ObjectSpecification serviceSpec = serviceAdapter.getSpecification();
            String serviceName = serviceSpec.getFacet(NamedFacet.class).translated();
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
        val serviceActionsByName = _Maps.<String, List<ServiceAndAction>>newTreeMap();

        // map available services
        ManagedObject lastServiceAdapter = null;

        for (ServiceAndAction serviceAction : serviceActions) {
            List<ServiceAndAction> serviceActionsForName =
                    serviceActionsByName.get(serviceAction.getServiceName());

            val serviceAdapter = serviceAction.getServiceAdapter();

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
            final ActionType actionType) {
        final ObjectSpecification serviceSpec = serviceAdapter.getSpecification();

        // skip if annotated to not be included in repository menu using @DomainService
        final DomainServiceFacet domainServiceFacet = serviceSpec.getFacet(DomainServiceFacet.class);
        if (domainServiceFacet != null) {
            final NatureOfService natureOfService = domainServiceFacet.getNatureOfService();
            if (!natureOfService.isView()) {
                return Stream.empty();
            }
        }

        final Stream<ObjectAction> objectActions = serviceSpec.streamDeclaredActions(actionType, MixedIn.INCLUDED);

        return objectActions
                // skip if annotated to not be included in repository menu using legacy mechanism
                .filter(objectAction->objectAction.getFacet(NotInServiceMenuFacet.class) == null)
                .map(objectAction->{
                    val layoutGroupFacet = objectAction.getFacet(LayoutGroupFacet.class);
                    String serviceName = layoutGroupFacet != null ? layoutGroupFacet.getGroupId(): null;
                    if(_Strings.isNullOrEmpty(serviceName)){
                        serviceName = serviceSpec.getFacet(NamedFacet.class).translated();
                    }
                    return new ServiceAndAction(serviceName, serviceAdapter, objectAction);
                });

    }

    private static Predicate<ManagedObject> with(final DomainServiceLayout.MenuBar menuBar) {
        return (ManagedObject input) -> {
            final DomainServiceLayoutFacet facet =
                    input.getSpecification().getFacet(DomainServiceLayoutFacet.class);
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

