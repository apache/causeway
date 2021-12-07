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
package org.apache.isis.viewer.wicket.viewer.wicketapp;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.SystemMapper;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.AjaxRequestTarget.IListener;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.IAjaxCallListener;
import org.apache.wicket.core.request.handler.ListenerRequestHandler;
import org.apache.wicket.core.request.mapper.PageInstanceMapper;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.IRequestMapper;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import org.apache.isis.commons.internal._Constants;
import org.apache.isis.commons.internal.base._Refs;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.commons.internal.collections._Sets;
import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.core.metamodel.interactions.managed.nonscalar.DataTableModel;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.runtime.context.IsisAppCommonContext;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.ui.components.collection.CollectionPanel;
import org.apache.isis.viewer.wicket.ui.components.scalars.reference.ReferencePanel;
import org.apache.isis.viewer.wicket.ui.pages.entity.EntityPage;

import lombok.NonNull;
import lombok.val;

/**
 * package private mixin for IsisWicketApplication;
 * to move experimental code out of IsisWicketApplication
 */
final class IsisWicketApplication_experimental {

    private final IsisWicketApplication holder;

    IsisWicketApplication_experimental(final IsisWicketApplication holder) {
        this.holder = holder;
    }

    void enableCsrfTokensForAjaxRequests(final @NonNull IsisConfiguration configuration) {
        if(!configuration.getSecurity().getSpring().isAllowCsrfFilters()) {
            return;
        }

        holder.getAjaxRequestTargetListeners().add(new IListener() {

            @Override
            public void updateAjaxAttributes(
                    final AbstractDefaultAjaxBehavior behavior,
                    final AjaxRequestAttributes attributes) {

                attributes.getAjaxCallListeners().add(new IAjaxCallListener() {
                    @Override
                    public CharSequence getBeforeSendHandler(final Component component) {

                        val csrfToken = csrfToken().orElse(null);

                        //debug
                        //System.err.printf("csrfToken %s%n", csrfToken);

                        return _Strings.isNotEmpty(csrfToken)
                                ? "function(attrs, xhr, settings){"
                                    + "xhr.setRequestHeader(\"X-CSRF-TOKEN\", \"" + csrfToken + "\");"
                                    + "}"
                                : null;

                    }
                });

            }

        });
    }

    private static Optional<String> csrfToken() {
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        return Optional.ofNullable(attr.getRequest().getSession(false)) // don't allow create
                .map(session->session.getAttribute("org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository.CSRF_TOKEN"))
                .map(csrfToken->{
                    try {
                        return (String)csrfToken.getClass().getMethod("getToken", _Constants.emptyClasses).invoke(csrfToken);
                    } catch (Exception e) {
                        return null;
                    }
                });
    }


    /*
     idea here is to avoid XmlPartialPageUpdate spitting out warnings, eg:

     13:08:36,642  [XmlPartialPageUpdate qtp1988859660-18 WARN ]  Component '[AjaxLink [Component id = copyLink]]' with markupid: 'copyLink94c' not rendered because it was already removed from page
     13:08:36,642  [XmlPartialPageUpdate qtp1988859660-18 WARN ]  Component '[SimpleClipboardModalWindow [Component id = simpleClipboardModalWindow]]' with markupid: 'simpleClipboardModalWindow94e' not rendered because it was already removed from page
     13:08:36,643  [XmlPartialPageUpdate qtp1988859660-18 WARN ]  Component '[AjaxFallbackLink [Component id = link]]' with markupid: 'link951' not rendered because it was already removed from page
     13:08:36,643  [XmlPartialPageUpdate qtp1988859660-18 WARN ]  Component '[AjaxFallbackLink [Component id = link]]' with markupid: 'link952' not rendered because it was already removed from page
     13:08:36,655  [XmlPartialPageUpdate qtp1988859660-18 WARN ]  Component '[AjaxLink [Component id = clearBookmarkLink]]' with markupid: 'clearBookmarkLink953' not rendered because it was already removed from page

     however, doesn't seem to work (even though the provided map is mutable).
     must be some other sort of side-effect which causes the enqueued component(s) to be removed from page between
     this listener firing and XmlPartialPageUpdate actually attempting to render the change components
     */
    boolean addListenerToStripRemovedComponentsFromAjaxTargetResponse() {

        return holder.getAjaxRequestTargetListeners().add(new IListener(){

            @Override
            public void onBeforeRespond(final Map<String, Component> map, final AjaxRequestTarget target) {

                System.out.println("=====================================");
                System.out.println("=== on before respond");
                System.out.println("map="+map);
                System.out.println("=====================================");
                System.out.println("=== removals");
                map.entrySet().removeIf(entry->{
                    final Component component = entry.getValue();
                    final Page page = component.findParent(Page.class);
                    if(page==null) {
                        System.out.println("id: "+entry.getKey()+": page="+page);
                    }
                    return page==null;
                });

                System.out.println("=====================================");

            }
        });
    }

    private Set<CssResourceReference> cssResourceReferencesForAllComponents() {
        // TODO mgrigorov: ISIS-537 temporary disabled to not mess up with Bootstrap styles
        //        Collection<ComponentFactory> componentFactories = getComponentFactoryRegistry().listComponentFactories();
        return _Sets.newLinkedHashSet(
                //                Iterables.concat(
                //                        Iterables.transform(
                //                                componentFactories,
                //                                getCssResourceReferences))
                );
    }

    void buildCssBundle() {
        // get the css for all components built by component factories
        final Set<CssResourceReference> references = cssResourceReferencesForAllComponents();

        // some additional special cases.
        holder.addSpecialCasesToCssBundle(references);

        // create the bundle
        holder.getResourceBundles().addCssBundle(
                IsisWicketApplication.class, "isis-wicket-viewer-bundle.css",
                references.toArray(new CssResourceReference[]{}));
    }

    // -- ENTITY PAGE EXPERIMENTS

    static void setRootRequestMapper(final IsisWicketApplication app) {
        app.setRootRequestMapper(new SystemMapper(app) {
            @Override
            protected IRequestMapper newPageInstanceMapper() {
                return new PageInstanceMapper() {
                    @Override
                    public IRequestHandler mapRequest(final Request request) {
                        var handler = super.mapRequest(request);
                        return reattachingHandler(app.getCommonContext(), handler);
                    }
                };
            }
        });

    }

    private static String keyFor(final Component component) {
        val ref = _Refs.stringRef(component.getPath());
        ref.cutAtIndexOfAndDrop(":theme:");
        return ref.getValue();
    }

    private static IRequestHandler reattachingHandler(
            final IsisAppCommonContext commonContext, final IRequestHandler handler) {
        if(handler!=null) {
            return handler;
        }
        if(handler instanceof ListenerRequestHandler) {
            val lirqh = (ListenerRequestHandler)handler;
            val page = lirqh.getPage();
            if(!(page instanceof EntityPage)) {
                return lirqh;
            }

            val entityPage = (EntityPage) page;
            val entityModel = (EntityModel)entityPage.getUiHintContainerIfAny();
            val spec = entityModel.getObject().getSpecification();

            if(!spec.getCorrespondingClass()
            .getSimpleName().contains("InventoryJaxb")) {
                return lirqh;
            };

            val pageParams = entityPage.getPageParameters();
            val pageWithAttachedEntities = EntityPage.ofPageParameters(commonContext, pageParams);
            pageWithAttachedEntities.internalInitialize();

            final Map<String, ManagedObject> entityByPath = _Maps.newHashMap();
            final Map<String, DataTableModel> dataTableByPath = _Maps.newHashMap();

            System.err.println("===================================================");

            pageWithAttachedEntities.visitChildren(new IVisitor<Component, Void>() {

                @Override
                public void component(final Component component, final IVisit<Void> visit) {

                    //System.err.printf("+ %s%n", component);

                    if(component.getClass().equals(ReferencePanel.class)) {
                        val scalarModel = ((ReferencePanel)component).getModel();
                        val value = scalarModel.getObject();
                        entityByPath.put(keyFor(component), value);

                        System.err.printf("AAA-value %s%n", value.getPojo());
                        System.err.printf("AAA-value@ %s%n", keyFor(component));
                        return;
                    }
                    if(component.getClass().equals(CollectionPanel.class)) {
                        val collectionModel = ((CollectionPanel)component).getModel();
                        val dataTableModel = collectionModel.getDataTableModel();
                        dataTableByPath.put(keyFor(component), dataTableModel);
                        System.err.printf("AAA-coll %s%n", dataTableModel);
                        System.err.printf("AAA-coll@ %s%n", keyFor(component));
                    }

//                    if(component.getClass().equals(EntityCollectionPanel.class)) {
//                        val entityModel = ((EntityCollectionPanel)component).getModel();
//                        val dataTableModel = EntityCollectionModelParented
//                                .forParentObjectModel(entityModel)
//                                .getDataTableModel();
//                        dataTableByPath.put(keyFor(component), dataTableModel);
//
//                        System.err.printf("AAA-coll %s%n", dataTableModel);
//                        System.err.printf("AAA-coll@ %s%n", keyFor(component));
//                    }

                }
            });


            //TODO re-attach all entities in the object graph

            if(entityModel.getCommonContext().getInteractionProvider().isInInteraction()) {

                System.err.println("===================================================");

                entityPage.visitChildren((component, visit) -> {
                    //System.err.printf("+ %s%n", component);
                    if(component.getClass().getSimpleName().equals("ReferencePanel")) {
                        var scalarModel = ((ReferencePanel)component).getModel();
                        scalarModel.setObject(entityByPath.get(keyFor(component)));
                        var value = scalarModel.getObject();
                        //getObjectManager().attachObject(value);
                        System.err.printf("BBB-value@ %s%n", keyFor(component));
                        System.err.printf("BBB-value %s%n", value.getPojo());
                        return;
                    }

                    if(component.getClass().equals(CollectionPanel.class)) {
                        val collectionModel = ((CollectionPanel)component).getModel();
                        val dataTableModel = dataTableByPath.get(keyFor(component));

                        //XXX removed collectionModel.setDataTableModel(dataTableModel);

                        System.err.printf("BBB-coll@ %s%n", keyFor(component));
                        System.err.printf("BBB-coll %s%n", collectionModel.getDataTableModel());

                        dataTableModel.getDataElements().getValue().forEach(mo->{
                            System.err.printf("element %s%n", mo.getBookmark());
                        });

                    }

                });
            }

//            if(false) {
//                Url url = request.getUrl();
//                PageComponentInfo info = getPageComponentInfo(url);
////                            Integer renderCount = info.getComponentInfo() != null ? info.getComponentInfo()
////                                    .getRenderCount() : null;
//                ComponentInfo componentInfo = info.getComponentInfo();
////                            PageAndComponentProvider provider = new PageAndComponentProvider(
////                                    info.getPageInfo().getPageId(), renderCount,
////                                    componentInfo.getComponentPath());
////                            provider.setPageSource(getContext());
//
//                val pageParams = entityPage.getPageParameters();
//
//                PageAndComponentProvider provider2 = new PageAndComponentProvider(
//                        EntityPage.class, pageParams,
//                        componentInfo.getComponentPath());
//
//                System.err.printf("***ComponentPath %s%n", componentInfo.getComponentPath());
//
//                provider2.setPageSource(getContext());
//
//                return new ListenerRequestHandler(provider2, componentInfo.getBehaviorId()) {
//                    @Override
//                    public IRequestableComponent getComponent() {
//                        dumpComponentTree((EntityPage)getPage(), _Predicates.alwaysTrue());
//                        return super.getComponent();
//                    }
//                };
//            }

            return lirqh;

        }
        return handler;
    }




}
