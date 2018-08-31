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
package org.apache.isis.viewer.wicket.viewer;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.AjaxRequestTarget.IListener;
import org.apache.wicket.request.resource.CssResourceReference;

/**
 * package private mixin for IsisWicketApplication;
 * to move experimental code out of IsisWicketApplication
 */
final class IsisWicketApplication_Experimental {
    
    private final IsisWicketApplication holder;
    
    IsisWicketApplication_Experimental(IsisWicketApplication holder) {
        this.holder = holder;
    }

    // idea here is to avoid XmlPartialPageUpdate spitting out warnings, eg:
    //
    // 13:08:36,642  [XmlPartialPageUpdate qtp1988859660-18 WARN ]  Component '[AjaxLink [Component id = copyLink]]' with markupid: 'copyLink94c' not rendered because it was already removed from page
    //  13:08:36,642  [XmlPartialPageUpdate qtp1988859660-18 WARN ]  Component '[SimpleClipboardModalWindow [Component id = simpleClipboardModalWindow]]' with markupid: 'simpleClipboardModalWindow94e' not rendered because it was already removed from page
    // 13:08:36,643  [XmlPartialPageUpdate qtp1988859660-18 WARN ]  Component '[AjaxFallbackLink [Component id = link]]' with markupid: 'link951' not rendered because it was already removed from page
    // 13:08:36,643  [XmlPartialPageUpdate qtp1988859660-18 WARN ]  Component '[AjaxFallbackLink [Component id = link]]' with markupid: 'link952' not rendered because it was already removed from page
    // 13:08:36,655  [XmlPartialPageUpdate qtp1988859660-18 WARN ]  Component '[AjaxLink [Component id = clearBookmarkLink]]' with markupid: 'clearBookmarkLink953' not rendered because it was already removed from page
    //
    // however, doesn't seem to work (even though the provided map is mutable).
    // must be some other sort of side-effect which causes the enqueued component(s) to be removed from page between
    // this listener firing and XmlPartialPageUpdate actually attempting to render the change components
    //
    boolean addListenerToStripRemovedComponentsFromAjaxTargetResponse() {
        
        return holder.getAjaxRequestTargetListeners().add(new IListener(){

            @Override
            public void onBeforeRespond(Map<String, Component> map, AjaxRequestTarget target) {

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
        return Sets.newLinkedHashSet(
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
    
}
