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

package org.apache.isis.viewer.wicket.viewer.registries.components;

import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

import org.apache.isis.viewer.wicket.ui.ComponentFactory;
import org.apache.isis.viewer.wicket.ui.actions.params.ActionParametersFormPanelFactory;
import org.apache.isis.viewer.wicket.ui.app.registry.ComponentFactoryList;
import org.apache.isis.viewer.wicket.ui.components.actions.ActionInfoPanelFactory;
import org.apache.isis.viewer.wicket.ui.components.actions.ActionPanelFactory;
import org.apache.isis.viewer.wicket.ui.components.appactions.cssmenu.AppActionsCssMenuFactory;
import org.apache.isis.viewer.wicket.ui.components.collection.CollectionPanelFactory;
import org.apache.isis.viewer.wicket.ui.components.collectioncontents.ajaxtable.CollectionContentsAsAjaxTableFactory;
import org.apache.isis.viewer.wicket.ui.components.collectioncontents.icons.CollectionContentsAsIconsFactory;
import org.apache.isis.viewer.wicket.ui.components.collectioncontents.selector.CollectionContentsSelectorFactory;
import org.apache.isis.viewer.wicket.ui.components.collectioncontents.unresolved.CollectionContentsAsUnresolvedFactory;
import org.apache.isis.viewer.wicket.ui.components.empty.EmptyCollectionPanelFactory;
import org.apache.isis.viewer.wicket.ui.components.entity.blocks.propcoll.EntityCollectionsReadOnlyFormPanelFactory;
import org.apache.isis.viewer.wicket.ui.components.entity.blocks.propcoll.EntityCollectionsPanelFactory;
import org.apache.isis.viewer.wicket.ui.components.entity.blocks.propcoll.EntityPropertiesAndCollectionsPanelFactory;
import org.apache.isis.viewer.wicket.ui.components.entity.blocks.propcoll.EntityPropertiesPanelFactory;
import org.apache.isis.viewer.wicket.ui.components.entity.blocks.summary.EntitySummaryPanelFactory;
import org.apache.isis.viewer.wicket.ui.components.entity.combined.EntityCombinedPanelFactory;
import org.apache.isis.viewer.wicket.ui.components.entity.selector.EntitySelectorFactory;
import org.apache.isis.viewer.wicket.ui.components.entity.tabbed.EntityTabbedPanelFactory;
import org.apache.isis.viewer.wicket.ui.components.scalars.jdkdates.JavaSqlDatePanelFactory;
import org.apache.isis.viewer.wicket.ui.components.scalars.jdkdates.JavaSqlTimePanelFactory;
import org.apache.isis.viewer.wicket.ui.components.scalars.jdkdates.JavaUtilDatePanelFactory;
import org.apache.isis.viewer.wicket.ui.components.scalars.jdkmath.JavaMathBigDecimalPanelFactory;
import org.apache.isis.viewer.wicket.ui.components.scalars.jdkmath.JavaMathBigIntegerPanelFactory;
import org.apache.isis.viewer.wicket.ui.components.scalars.jodatime.JodaLocalDatePanelFactory;
import org.apache.isis.viewer.wicket.ui.components.scalars.noapplib.IsisColorPanelFactory;
import org.apache.isis.viewer.wicket.ui.components.scalars.noapplib.IsisDatePanelFactory;
import org.apache.isis.viewer.wicket.ui.components.scalars.noapplib.IsisDateTimePanelFactory;
import org.apache.isis.viewer.wicket.ui.components.scalars.noapplib.IsisMoneyPanelFactory;
import org.apache.isis.viewer.wicket.ui.components.scalars.noapplib.IsisPasswordPanelFactory;
import org.apache.isis.viewer.wicket.ui.components.scalars.noapplib.IsisPercentagePanelFactory;
import org.apache.isis.viewer.wicket.ui.components.scalars.noapplib.IsisTimePanelFactory;
import org.apache.isis.viewer.wicket.ui.components.scalars.noapplib.IsisTimeStampPanelFactory;
import org.apache.isis.viewer.wicket.ui.components.scalars.primitive.BooleanPanelFactory;
import org.apache.isis.viewer.wicket.ui.components.scalars.primitive.BytePanelFactory;
import org.apache.isis.viewer.wicket.ui.components.scalars.primitive.CharacterPanelFactory;
import org.apache.isis.viewer.wicket.ui.components.scalars.primitive.DoublePanelFactory;
import org.apache.isis.viewer.wicket.ui.components.scalars.primitive.FloatPanelFactory;
import org.apache.isis.viewer.wicket.ui.components.scalars.primitive.IntegerPanelFactory;
import org.apache.isis.viewer.wicket.ui.components.scalars.primitive.LongPanelFactory;
import org.apache.isis.viewer.wicket.ui.components.scalars.primitive.ShortPanelFactory;
import org.apache.isis.viewer.wicket.ui.components.scalars.reference.ReferencePanelFactory;
import org.apache.isis.viewer.wicket.ui.components.scalars.string.StringPanelFactory;
import org.apache.isis.viewer.wicket.ui.components.scalars.value.ValuePanelFactory;
import org.apache.isis.viewer.wicket.ui.components.scalars.wizardpagedesc.WizardPageDescriptionPanelFactory;
import org.apache.isis.viewer.wicket.ui.components.unknown.UnknownModelPanelFactory;
import org.apache.isis.viewer.wicket.ui.components.value.StandaloneValuePanelFactory;
import org.apache.isis.viewer.wicket.ui.components.voidreturn.VoidReturnPanelFactory;
import org.apache.isis.viewer.wicket.ui.components.welcome.WelcomePanelFactory;
import org.apache.isis.viewer.wicket.ui.components.widgets.entitylink.EntityLinkFactory;

/**
 * Default implementation of {@link ComponentFactoryList} that registers a
 * hardcoded set of built-in {@link ComponentFactory}s, along with any
 * implementations loaded using {@link ServiceLoader} (ie from
 * <tt>META-INF/services</tt>).
 */
public class ComponentFactoryListDefault implements ComponentFactoryList {

    @Override
    public void addComponentFactories(final List<ComponentFactory> componentFactories) {

        addComponentFactoriesActingAsSelectors(componentFactories);

        addComponentFactoriesUsingServiceLoader(componentFactories);

        addBuiltInComponentFactories(componentFactories);
    }

    /**
     * Any {@link ComponentFactory}s that act as selectors of other factories
     * should be registered here; they will be loaded first, to ensure that they
     * are found first.
     */
    protected void addComponentFactoriesActingAsSelectors(final List<ComponentFactory> componentFactories) {
        
    	componentFactories.add(new EntitySelectorFactory());
    	
        componentFactories.add(new CollectionContentsSelectorFactory());
        componentFactories.add(new CollectionContentsAsUnresolvedFactory()); // make
                                                                             // first
    }

    protected void addComponentFactoriesUsingServiceLoader(final List<ComponentFactory> componentFactories) {
        final ServiceLoader<ComponentFactory> serviceLoader = ServiceLoader.load(ComponentFactory.class);

        for (final ComponentFactory componentFactory : serviceLoader) {
            componentFactories.add(componentFactory);
        }
    }

    private void addBuiltInComponentFactories(final List<ComponentFactory> componentFactories) {
        addComponentFactoriesForSpecial(componentFactories);
        addComponentFactoriesForWelcome(componentFactories);
        addComponentFactoriesForApplicationActions(componentFactories);
        addComponentFactoriesForEntity(componentFactories);
        addComponentFactoriesForActionInfo(componentFactories);
        addComponentFactoriesForAction(componentFactories);
        addComponentFactoriesForEntityCollection(componentFactories);
        addComponentFactoriesForEntityCollectionContents(componentFactories);
        addComponentFactoriesForEmptyCollection(componentFactories);
        addComponentFactoriesForScalar(componentFactories);
        addComponentFactoriesForEntityLink(componentFactories);
        addComponentFactoriesForVoidReturn(componentFactories);
        addComponentFactoriesForValue(componentFactories);
        addComponentFactoriesForParameters(componentFactories);
        
        addComponentFactoriesForUnknown(componentFactories);

    }

    protected void addComponentFactoriesForSpecial(final List<ComponentFactory> componentFactories) {
        componentFactories.add(new WizardPageDescriptionPanelFactory());
    }

    protected void addComponentFactoriesForWelcome(final List<ComponentFactory> componentFactories) {
        componentFactories.add(new WelcomePanelFactory());
    }

    protected void addComponentFactoriesForEntity(final List<ComponentFactory> componentFactories) {

        // top-level
        componentFactories.add(new EntityCombinedPanelFactory());
//        componentFactories.add(new EntityTabbedPanelFactory());
        
        // lower-level
        componentFactories.add(new EntitySummaryPanelFactory());
        componentFactories.add(new EntityPropertiesPanelFactory());
        componentFactories.add(new EntityCollectionsPanelFactory());
        componentFactories.add(new EntityCollectionsReadOnlyFormPanelFactory());
        componentFactories.add(new EntityPropertiesAndCollectionsPanelFactory());
    }

    protected void addComponentFactoriesForEntityCollectionContents(final List<ComponentFactory> componentFactories) {
        componentFactories.add(new CollectionContentsAsAjaxTableFactory());
        // componentFactories.add(new CollectionContentsAsSimpleTableFactory());
        // // work-in-progress
        componentFactories.add(new CollectionContentsAsIconsFactory());
    }

    protected void addComponentFactoriesForEntityCollection(final List<ComponentFactory> componentFactories) {
        componentFactories.add(new CollectionPanelFactory());
    }

    protected void addComponentFactoriesForEmptyCollection(final List<ComponentFactory> componentFactories) {
        componentFactories.add(new EmptyCollectionPanelFactory());
    }

    protected void addComponentFactoriesForValue(final List<ComponentFactory> componentFactories) {
        componentFactories.add(new StandaloneValuePanelFactory());
    }

    protected void addComponentFactoriesForScalar(final List<ComponentFactory> componentFactories) {

        componentFactories.add(new ReferencePanelFactory());

        componentFactories.add(new BooleanPanelFactory());
        componentFactories.add(new BytePanelFactory());
        componentFactories.add(new ShortPanelFactory());
        componentFactories.add(new IntegerPanelFactory());
        componentFactories.add(new LongPanelFactory());
        componentFactories.add(new CharacterPanelFactory());
        componentFactories.add(new FloatPanelFactory());
        componentFactories.add(new DoublePanelFactory());

        componentFactories.add(new StringPanelFactory());

        // componentFactories.add(new JavaAwtImagePanelFactory()); //
        // work-in-progress
        componentFactories.add(new JavaUtilDatePanelFactory());
        componentFactories.add(new JavaSqlDatePanelFactory());
        componentFactories.add(new JavaSqlTimePanelFactory());

        componentFactories.add(new IsisMoneyPanelFactory());
        componentFactories.add(new IsisDatePanelFactory());
        componentFactories.add(new IsisDateTimePanelFactory());
        componentFactories.add(new IsisTimePanelFactory());
        componentFactories.add(new IsisTimeStampPanelFactory());
        componentFactories.add(new IsisColorPanelFactory());
        componentFactories.add(new IsisPercentagePanelFactory());
        componentFactories.add(new IsisPasswordPanelFactory());

        componentFactories.add(new JavaMathBigIntegerPanelFactory());
        componentFactories.add(new JavaMathBigDecimalPanelFactory());

        componentFactories.add(new JodaLocalDatePanelFactory());

        componentFactories.add(new ValuePanelFactory());
    }

    protected void addComponentFactoriesForEntityLink(final List<ComponentFactory> componentFactories) {
        componentFactories.add(new EntityLinkFactory());
    }

    protected void addComponentFactoriesForVoidReturn(final List<ComponentFactory> componentFactories) {
        componentFactories.add(new VoidReturnPanelFactory());
    }

    protected void addComponentFactoriesForActionInfo(final List<ComponentFactory> componentFactories) {
        componentFactories.add(new ActionInfoPanelFactory());
    }

    protected void addComponentFactoriesForParameters(final List<ComponentFactory> componentFactories) {
        componentFactories.add(new ActionParametersFormPanelFactory());
    }

    protected void addComponentFactoriesForAction(final List<ComponentFactory> componentFactories) {
        componentFactories.add(new ActionPanelFactory());
    }

    protected void addComponentFactoriesForApplicationActions(final List<ComponentFactory> componentFactories) {
        componentFactories.add(new AppActionsCssMenuFactory());
    }


    protected void addComponentFactoriesForUnknown(final List<ComponentFactory> componentFactories) {
        componentFactories.add(new UnknownModelPanelFactory());
    }


    /**
     * Allows the subclass to remove any built-in factories.
     */
    protected void removeComponentFactories(final List<ComponentFactory> componentFactories, final Class<? extends ComponentFactory>... classes) {
        for (final Iterator<ComponentFactory> iterator = componentFactories.iterator(); iterator.hasNext();) {
            final ComponentFactory componentFactory = iterator.next();
            if (isAssignableToAny(componentFactory, classes)) {
                iterator.remove();
            }
        }
    }

    private boolean isAssignableToAny(final ComponentFactory componentFactory, final Class<? extends ComponentFactory>... classes) {
        final Class<? extends ComponentFactory> componentFactoryCls = componentFactory.getClass();
        for (final Class<? extends ComponentFactory> cls : classes) {
            if (cls.isAssignableFrom(componentFactoryCls)) {
                return true;
            }
        }
        return false;
    }
}
