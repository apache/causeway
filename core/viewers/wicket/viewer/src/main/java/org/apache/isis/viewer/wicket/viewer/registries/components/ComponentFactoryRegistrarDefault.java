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

import java.util.List;

import javax.inject.Named;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.viewer.wicket.ui.ComponentFactory;
import org.apache.isis.viewer.wicket.ui.app.registry.ComponentFactoryRegistrar;
import org.apache.isis.viewer.wicket.ui.components.about.AboutPanelFactory;
import org.apache.isis.viewer.wicket.ui.components.actioninfo.ActionInfoPanelFactory;
import org.apache.isis.viewer.wicket.ui.components.actionlink.ActionLinkPanelFactory;
import org.apache.isis.viewer.wicket.ui.components.actionmenu.serviceactions.ServiceActionsPanelFactory;
import org.apache.isis.viewer.wicket.ui.components.actionmenu.serviceactions.TertiaryMenuPanelFactory;
import org.apache.isis.viewer.wicket.ui.components.actions.ActionParametersFormPanelFactory;
import org.apache.isis.viewer.wicket.ui.components.actions.ActionParametersPanelFactory;
import org.apache.isis.viewer.wicket.ui.components.bookmarkedpages.BookmarkedPagesPanelFactory;
import org.apache.isis.viewer.wicket.ui.components.collectioncontents.ajaxtable.CollectionContentsAsAjaxTablePanelFactory;
import org.apache.isis.viewer.wicket.ui.components.collectioncontents.multiple.CollectionContentsMultipleViewsPanelFactory;
import org.apache.isis.viewer.wicket.ui.components.collectioncontents.summary.CollectionContentsAsSummaryFactory;
import org.apache.isis.viewer.wicket.ui.components.collectioncontents.unresolved.CollectionContentsHiddenPanelFactory;
import org.apache.isis.viewer.wicket.ui.components.empty.EmptyCollectionPanelFactory;
import org.apache.isis.viewer.wicket.ui.components.entity.collection.EntityCollectionPanelFactory;
import org.apache.isis.viewer.wicket.ui.components.entity.header.EntityHeaderPanelFactory;
import org.apache.isis.viewer.wicket.ui.components.entity.icontitle.EntityIconAndTitlePanelFactory;
import org.apache.isis.viewer.wicket.ui.components.entity.icontitle.EntityIconTitleAndCopyLinkPanelFactory;
import org.apache.isis.viewer.wicket.ui.components.entity.selector.links.EntityLinksSelectorPanelFactory;
import org.apache.isis.viewer.wicket.ui.components.footer.FooterPanelFactory;
import org.apache.isis.viewer.wicket.ui.components.header.HeaderPanelFactory;
import org.apache.isis.viewer.wicket.ui.components.property.PropertyEditFormPanelFactory;
import org.apache.isis.viewer.wicket.ui.components.property.PropertyEditPanelFactory;
import org.apache.isis.viewer.wicket.ui.components.scalars.isisapplib.IsisBlobPanelFactory;
import org.apache.isis.viewer.wicket.ui.components.scalars.isisapplib.IsisClobPanelFactory;
import org.apache.isis.viewer.wicket.ui.components.scalars.isisapplib.IsisPasswordPanelFactory;
import org.apache.isis.viewer.wicket.ui.components.scalars.jdk8time.Jdk8LocalDatePanelFactory;
import org.apache.isis.viewer.wicket.ui.components.scalars.jdk8time.Jdk8LocalDateTimePanelFactory;
import org.apache.isis.viewer.wicket.ui.components.scalars.jdk8time.Jdk8OffsetDateTimePanelFactory;
import org.apache.isis.viewer.wicket.ui.components.scalars.jdkdates.JavaSqlDatePanelFactory;
import org.apache.isis.viewer.wicket.ui.components.scalars.jdkdates.JavaSqlTimePanelFactory;
import org.apache.isis.viewer.wicket.ui.components.scalars.jdkdates.JavaSqlTimestampPanelFactory;
import org.apache.isis.viewer.wicket.ui.components.scalars.jdkdates.JavaUtilDatePanelFactory;
import org.apache.isis.viewer.wicket.ui.components.scalars.jdkmath.JavaMathBigDecimalPanelFactory;
import org.apache.isis.viewer.wicket.ui.components.scalars.jdkmath.JavaMathBigIntegerPanelFactory;
import org.apache.isis.viewer.wicket.ui.components.scalars.jodatime.JodaDateTimePanelFactory;
import org.apache.isis.viewer.wicket.ui.components.scalars.jodatime.JodaLocalDatePanelFactory;
import org.apache.isis.viewer.wicket.ui.components.scalars.jodatime.JodaLocalDateTimePanelFactory;
import org.apache.isis.viewer.wicket.ui.components.scalars.markup.MarkupPanelFactories;
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
import org.apache.isis.viewer.wicket.ui.components.scalars.uuid.UuidPanelFactory;
import org.apache.isis.viewer.wicket.ui.components.scalars.value.ValuePanelFactory;
import org.apache.isis.viewer.wicket.ui.components.scalars.valuechoices.ValueChoicesSelect2PanelFactory;
import org.apache.isis.viewer.wicket.ui.components.standalonecollection.StandaloneCollectionPanelFactory;
import org.apache.isis.viewer.wicket.ui.components.tree.TreePanelFactories;
import org.apache.isis.viewer.wicket.ui.components.unknown.UnknownModelPanelFactory;
import org.apache.isis.viewer.wicket.ui.components.value.StandaloneValuePanelFactory;
import org.apache.isis.viewer.wicket.ui.components.voidreturn.VoidReturnPanelFactory;
import org.apache.isis.viewer.wicket.ui.components.welcome.WelcomePanelFactory;
import org.apache.isis.viewer.wicket.ui.components.widgets.entitysimplelink.EntityLinkSimplePanelFactory;

/**
 * Default implementation of {@link ComponentFactoryRegistrar} that registers a
 * hardcoded set of built-in {@link ComponentFactory}s, along with any
 * implementations discovered by the IoC container.
 */
@Service
@Named("isisWicketViewer.ComponentFactoryRegistrarDefault")
@Order(OrderPrecedence.MIDPOINT)
@Primary
@Qualifier("Default")
public class ComponentFactoryRegistrarDefault implements ComponentFactoryRegistrar {
    
    @Autowired(required = false) private List<ComponentFactory> componentFactoriesPluggedIn;
    
    @Override
    public void addComponentFactories(final ComponentFactoryList componentFactories) {

        addComponentFactoriesActingAsSelectors(componentFactories);

        addComponentFactoriesFromPlugins(componentFactories);

        addBuiltInComponentFactories(componentFactories);
    }

    /**
     * Any {@link ComponentFactory}s that act as selectors of other factories
     * should be registered here; they will be loaded first, to ensure that they
     * are found first.
     */
    protected void addComponentFactoriesActingAsSelectors(final ComponentFactoryList componentFactories) {
        addLinksSelectorFactories(componentFactories);
        componentFactories.add(new CollectionContentsHiddenPanelFactory()); // to prevent eager loading
    }

    protected void addLinksSelectorFactories(final ComponentFactoryList componentFactories) {
        componentFactories.add(new EntityLinksSelectorPanelFactory());
        componentFactories.add(new CollectionContentsMultipleViewsPanelFactory());
    }

    protected void addComponentFactoriesFromPlugins(final ComponentFactoryList componentFactories) {
        
        System.out.println("!!!! " + componentFactoriesPluggedIn);
        
        _NullSafe.stream(componentFactoriesPluggedIn)
            .forEach(componentFactories::add);
    }

    private void addBuiltInComponentFactories(final ComponentFactoryList componentFactories) {
        addComponentFactoriesForWelcomeAndAbout(componentFactories);
        addComponentFactoriesForApplicationActions(componentFactories);
        addComponentFactoriesForEntity(componentFactories);
        addComponentFactoriesForActionInfo(componentFactories);
        addComponentFactoriesForAction(componentFactories);
        addComponentFactoriesForActionLink(componentFactories);
        addComponentFactoriesForPropertyEdit(componentFactories);
        addComponentFactoriesForEntityCollectionContents(componentFactories);
        addComponentFactoriesForEmptyCollection(componentFactories);
        addComponentFactoriesForScalar(componentFactories);
        addComponentFactoriesForEntityLink(componentFactories);
        addComponentFactoriesForVoidReturn(componentFactories);
        addComponentFactoriesForValue(componentFactories);
        addComponentFactoriesForParameters(componentFactories);
        addComponentFactoriesForBreadcrumbs(componentFactories);
        addComponentFactoriesForPageHeader(componentFactories);
        addComponentFactoriesForPageFooter(componentFactories);

        addComponentFactoriesForUnknown(componentFactories);
    }

    protected void addComponentFactoriesForPageHeader(ComponentFactoryList componentFactories) {
        componentFactories.add(new HeaderPanelFactory());
    }

    protected void addComponentFactoriesForPageFooter(ComponentFactoryList componentFactories) {
        componentFactories.add(new FooterPanelFactory());
    }

    protected void addComponentFactoriesForWelcomeAndAbout(final ComponentFactoryList componentFactories) {
        componentFactories.add(new WelcomePanelFactory());
        componentFactories.add(new AboutPanelFactory());
    }

    protected void addComponentFactoriesForEntity(final ComponentFactoryList componentFactories) {

        // lower-level
        componentFactories.add(new EntityIconAndTitlePanelFactory());
        componentFactories.add(new EntityIconTitleAndCopyLinkPanelFactory());
        componentFactories.add(new EntityHeaderPanelFactory());
        componentFactories.add(new EntityCollectionPanelFactory());
    }

    protected void addComponentFactoriesForEntityCollectionContents(final ComponentFactoryList componentFactories) {
        componentFactories.add(new CollectionContentsAsAjaxTablePanelFactory());

        // // work-in-progress
        // componentFactories.add(new CollectionContentsAsIconsPanelFactory());

        componentFactories.add(new CollectionContentsAsSummaryFactory());
    }


    protected void addComponentFactoriesForEmptyCollection(final ComponentFactoryList componentFactories) {
        componentFactories.add(new EmptyCollectionPanelFactory());
    }

    protected void addComponentFactoriesForValue(final ComponentFactoryList componentFactories) {
        componentFactories.add(MarkupPanelFactories.standalone());
        componentFactories.add(TreePanelFactories.standalone());
        componentFactories.add(new StandaloneValuePanelFactory());
    }

    protected void addComponentFactoriesForScalar(final ComponentFactoryList componentFactories) {

        componentFactories.add(TreePanelFactories.parented());

        componentFactories.add(new ReferencePanelFactory());
        componentFactories.add(MarkupPanelFactories.parented());

        componentFactories.add(new BooleanPanelFactory());
        componentFactories.add(new BytePanelFactory());
        componentFactories.add(new ShortPanelFactory());
        componentFactories.add(new IntegerPanelFactory());
        componentFactories.add(new LongPanelFactory());
        componentFactories.add(new CharacterPanelFactory());
        componentFactories.add(new FloatPanelFactory());
        componentFactories.add(new DoublePanelFactory());

        componentFactories.add(new StringPanelFactory());

        // work-in-progress
        // componentFactories.add(new JavaAwtImagePanelFactory());
        componentFactories.add(new JavaUtilDatePanelFactory());
        componentFactories.add(new JavaSqlTimestampPanelFactory());
        componentFactories.add(new JavaSqlDatePanelFactory());
        componentFactories.add(new JavaSqlTimePanelFactory());

        componentFactories.add(new IsisPasswordPanelFactory());

        componentFactories.add(new IsisBlobPanelFactory());
        componentFactories.add(new IsisClobPanelFactory());

        componentFactories.add(new JavaMathBigIntegerPanelFactory());
        componentFactories.add(new JavaMathBigDecimalPanelFactory());
        componentFactories.add(new UuidPanelFactory());

        componentFactories.add(new JodaLocalDatePanelFactory());
        componentFactories.add(new JodaLocalDateTimePanelFactory());
        componentFactories.add(new JodaDateTimePanelFactory());

        componentFactories.add(new Jdk8OffsetDateTimePanelFactory());
        componentFactories.add(new Jdk8LocalDatePanelFactory());
        componentFactories.add(new Jdk8LocalDateTimePanelFactory());

        componentFactories.add(new ValuePanelFactory());

        // or for choices
        componentFactories.add(new ValueChoicesSelect2PanelFactory());

    }

    protected void addComponentFactoriesForEntityLink(final ComponentFactoryList componentFactories) {
        componentFactories.add(new EntityLinkSimplePanelFactory());
    }

    protected void addComponentFactoriesForVoidReturn(final ComponentFactoryList componentFactories) {
        componentFactories.add(new VoidReturnPanelFactory());
    }

    protected void addComponentFactoriesForActionInfo(final ComponentFactoryList componentFactories) {
        componentFactories.add(new ActionInfoPanelFactory());
    }

    protected void addComponentFactoriesForParameters(final ComponentFactoryList componentFactories) {
        componentFactories.add(new ActionParametersFormPanelFactory());
    }

    protected void addComponentFactoriesForAction(final ComponentFactoryList componentFactories) {
        componentFactories.add(new ActionParametersPanelFactory());
        componentFactories.add(new StandaloneCollectionPanelFactory());
    }

    protected void addComponentFactoriesForPropertyEdit(final ComponentFactoryList componentFactories) {
        componentFactories.add(new PropertyEditPanelFactory());
        componentFactories.add(new PropertyEditFormPanelFactory());
    }

    protected void addComponentFactoriesForActionLink(final ComponentFactoryList componentFactories) {
        componentFactories.add(new ActionLinkPanelFactory());
    }

    protected void addComponentFactoriesForApplicationActions(final ComponentFactoryList componentFactories) {
        componentFactories.add(new ServiceActionsPanelFactory());
        componentFactories.add(new TertiaryMenuPanelFactory());
    }

    protected void addComponentFactoriesForBreadcrumbs(ComponentFactoryList componentFactories) {
        componentFactories.add(new BookmarkedPagesPanelFactory());
    }

    protected void addComponentFactoriesForUnknown(final ComponentFactoryList componentFactories) {
        componentFactories.add(new UnknownModelPanelFactory());
    }

}
