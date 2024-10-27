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
package org.apache.causeway.extensions.docgen.help.topics.welcome;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.stereotype.Component;

import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.layout.component.ActionLayoutData;
import org.apache.causeway.applib.layout.component.CollectionLayoutData;
import org.apache.causeway.applib.layout.component.FieldSet;
import org.apache.causeway.applib.layout.component.PropertyLayoutData;
import org.apache.causeway.applib.layout.component.ServiceActionLayoutData;
import org.apache.causeway.applib.layout.grid.Grid;
import org.apache.causeway.applib.layout.menubars.MenuBars;
import org.apache.causeway.applib.layout.menubars.bootstrap.BSMenuBars;
import org.apache.causeway.applib.services.homepage.HomePageResolverService;
import org.apache.causeway.applib.services.i18n.TranslationContext;
import org.apache.causeway.applib.services.i18n.TranslationService;
import org.apache.causeway.applib.services.menu.MenuBarsService;
import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.core.metamodel.facets.all.described.MemberDescribedFacet;
import org.apache.causeway.core.metamodel.facets.all.i8n.staatic.HasStaticText;
import org.apache.causeway.core.metamodel.facets.object.grid.GridFacet;
import org.apache.causeway.core.metamodel.spec.ActionScope;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;
import org.apache.causeway.extensions.docgen.help.CausewayModuleExtDocgenHelp;
import org.apache.causeway.extensions.docgen.help.applib.HelpPage;
import org.apache.causeway.valuetypes.asciidoc.applib.value.AsciiDoc;

import lombok.RequiredArgsConstructor;

@Component
@Named(CausewayModuleExtDocgenHelp.NAMESPACE + ".WelcomeHelpPage")
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class WelcomeHelpPage implements HelpPage {

    private final SpecificationLoader specificationLoader;
    private final MenuBarsService menuBarsService;
    private final HomePageResolverService homePageResolverService;
    private final TranslationService translationService;

    @Override
    public String getTitle() {
        return "Welcome";
    }

    @Override
    public AsciiDoc getContent() {
        // uses a HTML passthrough block (https://docs.asciidoctor.org/asciidoc/latest/pass/pass-block/)
        return AsciiDoc.valueOf(
                "== Welcome\n\n"
                + "++++\n"
                + getDocumentationAsHtml()
                + "++++\n");
    }

    // -- HELPER

    //TODO generate ascii-doc syntax instead; also eventually split the page into smaller sub-pages
    private String getDocumentationAsHtml() {
        final StringBuilder html = new StringBuilder();
        Object homePage = homePageResolverService.getHomePage();
        if (homePage != null) {
            specificationLoader.specForType(homePage.getClass()).ifPresent(homeSpec -> {
                html.append(String.format("<i>%s</i>\n", homeSpec.getDescription()));
                html.append("</br><i>").append(translationService.translate(TranslationContext.empty(), "To know more about full terminology"));
                html.append(" <a href='#allObjectSpecs'>").append(translationService.translate(TranslationContext.empty(), "click here")).append("</a></i>\n");
                html.append("<hr></hr>");
                html.append("<h3>").append(translationService.translate(TranslationContext.empty(), "Application Home Page")).append("</h3>");
                html.append(documentationForObjectType(homeSpec));
                html.append("<hr></hr>");
            });
        }
        html.append("<h3>").append(translationService.translate(TranslationContext.empty(), "Menu Actions")).append("</h3>");

        Map<String, ObjectSpecification> domainObjects = new HashMap<>();

        MenuBars menuBars = menuBarsService.menuBars(MenuBarsService.Type.DEFAULT);
        html.append("<ol>");
        menuBars.visit(BSMenuBars.VisitorAdapter.visitingMenus(menu -> {
            String menuName = menu.getNamed();
            if (_Strings.isNotEmpty(menuName)) {

                html.append(String.format("<li><h4>%s</h4></li>\n", menuName));

                html.append("<ol>");
                _NullSafe.stream(menu.getSections())
                        .forEach(menuSection -> {
                            String sectionName = menuSection.getNamed();
                            if (_Strings.isNotEmpty(sectionName)) {
                                html.append(String.format("<li><h5>%s</h5></li>\n", sectionName));

                                html.append("<ul>");
                                _NullSafe.stream(menuSection.getServiceActions())
                                        .map(this::lookupAction)
                                        .flatMap(Optional::stream)
                                        .forEach(menuAction -> {
                                            html.append(String.format("<li>%s: ", menuAction.getCanonicalFriendlyName()));
                                            menuAction.getCanonicalDescription()
                                                    .ifPresent(describedAs -> {
                                                        html.append(String.format("<b>%s</b>.", describedAs));
                                                    });
                                            ObjectSpecification actionReturnType = menuAction.getReturnType();
                                            ObjectSpecification actionElementType = menuAction.getElementType();

                                            if (actionElementType.getCorrespondingClass() == void.class) {
                                                html.append("<i></i>"); //WARNING : NOTHING
                                            } else if (actionReturnType.isPlural()) {
                                                domainObjects.put(actionElementType.getLogicalTypeName(), actionElementType);
                                                html.append(String.format(" <i> %s: <a href='#%s'>%s</a>\n</i>"
                                                        , translationService.translate(TranslationContext.empty(), "See"), actionElementType.getLogicalTypeName(), actionElementType.getSingularName()));
                                            } else {
                                                domainObjects.put(actionReturnType.getLogicalTypeName(), actionReturnType);
                                                html.append(String.format(" <i> %s: <a href='#%s'>%s</a>\n</i>"
                                                        , translationService.translate(TranslationContext.empty(), "See"), actionElementType.getLogicalTypeName(), actionElementType.getSingularName()));
                                            }
                                            html.append("</li>");
                                        });
                                html.append("</ul>");
                            }
                        });
                html.append("</ol>");
            }
        }));
        html.append("</ol>");

        html.append("<hr></hr>");
        html.append("<h2 id='allObjectSpecs'>").append(translationService.translate(TranslationContext.empty(), "Terminology")).append("</h2>");
        html.append("<ol>");
        for (ObjectSpecification objectSpec : domainObjects.values()) {
            if (objectSpec != null) {
                html.append(String.format("<li id='%s'><h3>%s</h3></li>\n",
                        objectSpec.getLogicalTypeName(), objectSpec.getSingularName()));
                html.append(objectSpec.getDescription());
                html.append(".");
                html.append(String.format("%s\n", documentationForObjectType(objectSpec)));
            }
        }
        html.append("</ol>");
        return html.toString();
    }

    private StringBuffer documentationForObjectType(final ObjectSpecification objectSpec) {
        StringBuffer html = new StringBuffer();

        Grid grid = toGrid(objectSpec.getCorrespondingClass());
        html.append("<ul>");
        {
            html.append("<ul>");
            {
                for (ActionLayoutData layout : grid.getAllActionsById().values()) {
                    objectSpec.getAction(layout.getId(), ActionScope.PRODUCTION_ONLY)
                            .ifPresent(member -> {
                                if (!"clearHints".equals(member.getId()) && !member.isAlwaysHidden()) {
                                    String describedAs = member.getCanonicalDescription()
                                            .map(desc -> String.format("%s", desc))
                                            .orElse("");
                                    html.append(String.format("<li><i class='%s'></i><b>%s</b>: %s.</li>\n",
                                            _Strings.isNotEmpty(layout.getCssClassFa()) ? layout.getCssClassFa() : "fa fa-faw fa-location-arrow",
                                            member.getCanonicalFriendlyName(),
                                            describedAs));
                                }
                            });
                }
            }
            html.append("</ul>");
            html.append("<ul>");
            {
                for (CollectionLayoutData layout : grid.getAllCollectionsById().values()) {
                    objectSpec.getCollection(layout.getId())
                            .ifPresent(member -> {
                                if (!member.isAlwaysHidden()) {
                                    String description = null;
                                    if (member.containsFacet(MemberDescribedFacet.class)) {
                                        description = member.getFacet(MemberDescribedFacet.class).getSpecialization()
                                                .left().map(HasStaticText::text).orElse(null);
                                    }
                                    if (_Strings.isNotEmpty(description)) {
                                        description = layout.getDescribedAs();
                                    }
                                    html.append(String.format("<li><i class='%s'></i><b>%s</b>: %s.</li>\n",
                                            "fa fa-fw fa-list",
                                            member.getCanonicalFriendlyName(),
                                            description != null ? description : ""));
                                    // FIXME[CAUSEWAY-2883] also visit associated actions
                                    // ... collection.streamAssociatedActions()
                                    html.append("<ul>");
                                    member.streamAssociatedActions().forEach(action -> {
                                        html.append(String.format("<li><i class='%s'></i> <b>%s</b>: %s.</li>\n",
                                                "fa fa-faw fa-location-arrow",
                                                action.getCanonicalFriendlyName(),
                                                action.getCanonicalDescription().orElse("")));
                                    });
                                    html.append("</ul>");
                                }
                            });
                }
            }
            html.append("</ul>");
            html.append("<ul>");
            {
                grid.visit(new Grid.VisitorAdapter() {
                    @Override
                    public void visit(final FieldSet fieldSet) {
                        if (_NullSafe.isEmpty(fieldSet.getProperties())) {
                            return;
                        } else {
                            html.append(String.format("<li>%s</li>\n", fieldSet.getName()));
                            html.append("<ul>");
                            for (PropertyLayoutData layout : fieldSet.getProperties()) {
                                objectSpec.getProperty(layout.getId())
                                        .ifPresent(member -> {
                                            if (!member.isAlwaysHidden()) {
                                                String describedAs = member.getCanonicalDescription()
                                                        .map(desc -> String.format("%s", desc))
                                                        .orElse("");
                                                html.append(String.format("<li><b>%s</b>: %s.",
                                                        member.getCanonicalFriendlyName(),
                                                        describedAs));
                                                if (member.getElementType().getLogicalType().getCorrespondingClass()
                                                        .isAnnotationPresent(DomainObject.class)) {
                                                    html.append(String.format(" <i> See: <a href='#%s'>%s</a></i>",
                                                            member.getElementType().getLogicalTypeName(),
                                                            member.getElementType().getSingularName()));
                                                } else {
                                                    //none
                                                }
                                                html.append("</li>\n");
                                            }
                                        });
                            }
                            html.append("</ul>");
                        }
                    }
                });
            }
            html.append("</ul>");
        }
        html.append("</ul>");
        return html;
    }

    private Optional<ObjectAction> lookupAction(final ServiceActionLayoutData actionLayout) {
        return specificationLoader
                .specForLogicalTypeName(actionLayout.getLogicalTypeName())
                .map(typeSpec -> typeSpec.getAction(actionLayout.getId(), ActionScope.PRODUCTION_ONLY).orElse(null));
    }

    private Grid toGrid(final Class<?> domainClass) {
        return specificationLoader.specForType(domainClass)
                .flatMap(spec -> spec.lookupFacet(GridFacet.class))
                .map(gridFacet -> gridFacet.getGrid(null))
                .orElse(null);
    }

}
