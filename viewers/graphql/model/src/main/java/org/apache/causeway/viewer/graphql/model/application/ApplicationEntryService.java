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
package org.apache.causeway.viewer.graphql.model.application;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;

import jakarta.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.causeway.applib.annotation.Programmatic;
import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.applib.layout.menubars.MenuBars;
import org.apache.causeway.applib.layout.menubars.bootstrap.BSMenuBar;
import org.apache.causeway.applib.layout.menubars.bootstrap.BSMenuBars;
import org.apache.causeway.applib.services.homepage.HomePageResolverService;
import org.apache.causeway.applib.services.layout.LayoutService;
import org.apache.causeway.applib.services.menu.MenuBarsMarshallerService;
import org.apache.causeway.applib.services.menu.MenuBarsService;
import org.apache.causeway.applib.services.marshal.Marshaller;
import org.apache.causeway.applib.value.NamedWithMimeType.CommonMimeType;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.interactions.managed.ManagedAction;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.MmVisibilityUtils;
import org.apache.causeway.core.metamodel.objectmanager.ObjectManager;
import org.apache.causeway.viewer.graphql.model.domain.common.query.ResourcePath;

@Service
public class ApplicationEntryService {

    public static final String MENU_BARS_FORMAT_VERSION =
            "https://causeway.apache.org/applib/layout/menubars/bootstrap3";
    public static final String MENU_BARS_MEDIA_TYPE = "application/xml";
    public static final String PRIVATE_NO_STORE = "private, no-store";

    private static final String MENU_BARS_SCHEMA_LOCATION =
            MENU_BARS_FORMAT_VERSION + " " + MENU_BARS_FORMAT_VERSION + "/menubars.xsd "
            + "https://causeway.apache.org/applib/layout/component "
            + "https://causeway.apache.org/applib/layout/component/component.xsd";
    private static final int MAX_ISSUES = 20;

    private final MenuBarsService menuBarsService;
    private final LayoutService layoutService;
    private final MenuBarsMarshallerService<?> menuBarsMarshallerService;
    private final HomePageResolverService homePageResolverService;
    private final MetaModelContext metaModelContext;
    private final ObjectManager objectManager;
    private final ResourcePath resourcePath;

    @Inject
    public ApplicationEntryService(
            final MenuBarsService menuBarsService,
            final LayoutService layoutService,
            final MenuBarsMarshallerService<?> menuBarsMarshallerService,
            final HomePageResolverService homePageResolverService,
            final MetaModelContext metaModelContext,
            final ObjectManager objectManager,
            final CausewayConfiguration causewayConfiguration) {
        this(
                menuBarsService,
                layoutService,
                menuBarsMarshallerService,
                homePageResolverService,
                metaModelContext,
                objectManager,
                ResourcePath.from(causewayConfiguration));
    }

    ApplicationEntryService(
            final MenuBarsService menuBarsService,
            final LayoutService layoutService,
            final MenuBarsMarshallerService<?> menuBarsMarshallerService,
            final HomePageResolverService homePageResolverService,
            final MetaModelContext metaModelContext,
            final ObjectManager objectManager,
            final ResourcePath resourcePath) {
        this.menuBarsService = menuBarsService;
        this.layoutService = layoutService;
        this.menuBarsMarshallerService = menuBarsMarshallerService;
        this.homePageResolverService = homePageResolverService;
        this.metaModelContext = metaModelContext;
        this.objectManager = objectManager;
        this.resourcePath = resourcePath;
    }

    @Programmatic
    public ApplicationSnapshot applicationSnapshot(final boolean includeMenuBars) {
        var issues = new ArrayList<Issue>();
        MenuBarsMetadata menuBarsMetadata = null;
        if (includeMenuBars) {
            var menuBars = menuBarsResource();
            issues.addAll(menuBars.issues());
            if (menuBars.xml() != null) {
                menuBarsMetadata = new MenuBarsMetadata(
                        resourcePath.application("menu-bars"),
                        MENU_BARS_MEDIA_TYPE,
                        MENU_BARS_FORMAT_VERSION,
                        menuBars.generation(),
                        PRIVATE_NO_STORE);
            }
        }
        var homeResult = home();
        issues.addAll(homeResult.issues());
        return new ApplicationSnapshot(
                menuBarsMetadata,
                homeResult.home(),
                List.copyOf(issues.stream().limit(MAX_ISSUES).toList()));
    }

    @Programmatic
    public MenuBarsResource menuBarsResource() {
        var issues = new ArrayList<Issue>();
        try {
            var exportedXml = layoutService.menuBarsLayout(MenuBarsService.Type.DEFAULT, CommonMimeType.XML);
            var menuBars = menuBarsMarshallerService
                    .unmarshal(exportedXml, CommonMimeType.XML)
                    .getValue()
                    .orElse(null);
            if (!(menuBars instanceof BSMenuBars bsMenuBars)) {
                return new MenuBarsResource(null, null, List.of(issue(
                        "UNSUPPORTED_MENU_FORMAT",
                        "The effective menu-bars model uses an unsupported format.")));
            }
            collectMetadataIssue(bsMenuBars.getMetadataError(), issues);
            bsMenuBars.setMetadataError(null);
            filter(bsMenuBars, issues);
            var schemaLocation = menuBarsService.menuBars().getTnsAndSchemaLocation();
            bsMenuBars.setTnsAndSchemaLocation(schemaLocation != null
                    ? schemaLocation
                    : MENU_BARS_SCHEMA_LOCATION);
            var xml = marshal(bsMenuBars).replace("\r\n", "\n");
            return new MenuBarsResource(xml, sha256(xml), bounded(issues));
        } catch (RuntimeException ex) {
            return new MenuBarsResource(null, null, List.of(issue(
                    "MENU_RESOURCE_UNAVAILABLE",
                    "The effective menu-bars resource is unavailable.")));
        }
    }

    private HomeResult home() {
        try {
            var pojo = homePageResolverService.getHomePage();
            if (pojo == null) {
                return new HomeResult(null, List.of());
            }
            var managedObject = objectManager.adapt(pojo);
            if (!isSupportedVisibleHome(managedObject)) {
                return new HomeResult(null, List.of(issue(
                        "HOME_UNAVAILABLE",
                        "The configured home entry is unavailable.")));
            }
            return new HomeResult(
                    new HomeSnapshot("OBJECT", managedObject.objSpec().logicalTypeName(), managedObject.getPojo()),
                    List.of());
        } catch (RuntimeException ex) {
            return new HomeResult(null, List.of(issue(
                    "HOME_UNAVAILABLE",
                    "The configured home entry is unavailable.")));
        }
    }

    private static boolean isSupportedVisibleHome(final ManagedObject managedObject) {
        if (managedObject == null || !managedObject.objSpec().isEntityOrViewModel()) {
            return false;
        }
        return MmVisibilityUtils.isVisible(managedObject, InteractionInitiatedBy.FRAMEWORK)
                && MmVisibilityUtils.isVisible(managedObject, InteractionInitiatedBy.USER);
    }

    private void filter(final BSMenuBars menuBars, final List<Issue> issues) {
        filter(menuBars.getPrimary(), issues);
        filter(menuBars.getSecondary(), issues);
        filter(menuBars.getTertiary(), issues);
    }

    private void filter(final BSMenuBar menuBar, final List<Issue> issues) {
        for (var menuIterator = menuBar.getMenus().iterator(); menuIterator.hasNext();) {
            var menu = menuIterator.next();
            for (var sectionIterator = menu.getSections().iterator(); sectionIterator.hasNext();) {
                var section = sectionIterator.next();
                section.getServiceActions().removeIf(action -> {
                    collectMetadataIssue(action.getMetadataError(), issues);
                    action.setMetadataError(null);
                    action.setLink(null);
                    var managedAction = managedAction(action.getLogicalTypeName(), action.getId());
                    if (managedAction.isEmpty()) {
                        addIssue(issues, issue(
                                "INVALID_ACTION_REFERENCE",
                                "A menu action reference could not be resolved."));
                        return true;
                    }
                    if (managedAction.get().checkVisibility().isPresent()) {
                        return true;
                    }
                    localize(action, managedAction.get());
                    return false;
                });
                if (section.getServiceActions().isEmpty()) {
                    sectionIterator.remove();
                }
            }
            if (menu.getSections().isEmpty()) {
                menuIterator.remove();
            }
        }
    }

    private Optional<ManagedAction> managedAction(
            final String serviceLogicalTypeName,
            final String actionId) {
        if (serviceLogicalTypeName == null || actionId == null) {
            return Optional.empty();
        }
        var serviceAdapter = metaModelContext.lookupServiceAdapterById(serviceLogicalTypeName);
        return serviceAdapter != null
                ? ManagedAction.lookupAction(serviceAdapter, actionId, Where.EVERYWHERE)
                : Optional.empty();
    }

    private static void localize(
            final org.apache.causeway.applib.layout.component.ServiceActionLayoutData action,
            final ManagedAction managedAction) {
        if (action.getNamed() == null || action.getNamed().isBlank()) {
            action.setNamed(managedAction.getFriendlyName());
        }
        if (action.getDescribedAs() == null || action.getDescribedAs().isBlank()) {
            managedAction.getDescription().ifPresent(action::setDescribedAs);
        }
    }

    private static void collectMetadataIssue(final String metadataError, final List<Issue> issues) {
        if (metadataError != null && !metadataError.isBlank()) {
            addIssue(issues, issue(
                    "INVALID_MENU_METADATA",
                    "The effective menu-bars model contains invalid metadata."));
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private String marshal(final MenuBars menuBars) {
        var marshaller = (Marshaller) menuBarsMarshallerService;
        return marshaller.marshal(menuBars, CommonMimeType.XML);
    }

    private static List<Issue> bounded(final List<Issue> issues) {
        return List.copyOf(issues.stream().limit(MAX_ISSUES).toList());
    }

    private static void addIssue(final List<Issue> issues, final Issue issue) {
        if (issues.size() < MAX_ISSUES) {
            issues.add(issue);
        }
    }

    private static Issue issue(final String code, final String message) {
        return new Issue(code, message);
    }

    private static String sha256(final String value) {
        try {
            var digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is unavailable", ex);
        }
    }

    public record ApplicationSnapshot(
            MenuBarsMetadata menuBars,
            HomeSnapshot home,
            List<Issue> issues) {
    }

    public record MenuBarsMetadata(
            String href,
            String mediaType,
            String formatVersion,
            String generation,
            String cacheControl) {
    }

    public record HomeSnapshot(
            String kind,
            String logicalTypeName,
            Object object) {
    }

    public record Issue(String code, String message) {
    }

    public record MenuBarsResource(
            String xml,
            String generation,
            List<Issue> issues) {
    }

    private record HomeResult(HomeSnapshot home, List<Issue> issues) {
    }
}
