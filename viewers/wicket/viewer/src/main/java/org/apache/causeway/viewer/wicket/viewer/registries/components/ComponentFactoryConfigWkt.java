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
package org.apache.causeway.viewer.wicket.viewer.registries.components;

import java.io.Serializable;
import java.time.temporal.Temporal;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.wicket.Component;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ClassUtils;

import org.apache.causeway.applib.value.semantics.ValueSemanticsProvider;
import org.apache.causeway.applib.value.semantics.ValueSemanticsResolver;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.commons.internal.functions._Predicates;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.tabular.simple.CollectionContentsExporter;
import org.apache.causeway.viewer.wicket.model.models.ScalarModel;
import org.apache.causeway.viewer.wicket.ui.ComponentFactory;
import org.apache.causeway.viewer.wicket.ui.app.registry.ComponentFactoryList;
import org.apache.causeway.viewer.wicket.ui.app.registry.ComponentFactoryRegistry;
import org.apache.causeway.viewer.wicket.ui.components.about.AboutPanelFactory;
import org.apache.causeway.viewer.wicket.ui.components.actioninfo.ActionInfoPanelFactory;
import org.apache.causeway.viewer.wicket.ui.components.actionlinks.serviceactions.ServiceActionsPanelFactory;
import org.apache.causeway.viewer.wicket.ui.components.actionlinks.serviceactions.TertiaryMenuPanelFactory;
import org.apache.causeway.viewer.wicket.ui.components.actions.ActionParametersFormPanelFactory;
import org.apache.causeway.viewer.wicket.ui.components.actions.ActionParametersPanelFactory;
import org.apache.causeway.viewer.wicket.ui.components.bookmarkedpages.BookmarkedPagesPanelFactory;
import org.apache.causeway.viewer.wicket.ui.components.collectioncontents.ajaxtable.CollectionContentsAsAjaxTablePanelFactory;
import org.apache.causeway.viewer.wicket.ui.components.collectioncontents.export.CollectionContentsAsExportFactory;
import org.apache.causeway.viewer.wicket.ui.components.collectioncontents.multiple.CollectionContentsMultipleViewsPanelFactory;
import org.apache.causeway.viewer.wicket.ui.components.collectioncontents.summary.CollectionContentsAsSummaryFactory;
import org.apache.causeway.viewer.wicket.ui.components.collectioncontents.unresolved.CollectionContentsHiddenPanelFactory;
import org.apache.causeway.viewer.wicket.ui.components.empty.EmptyCollectionPanelFactory;
import org.apache.causeway.viewer.wicket.ui.components.entity.collection.EntityCollectionPanelFactory;
import org.apache.causeway.viewer.wicket.ui.components.entity.header.EntityHeaderPanelFactory;
import org.apache.causeway.viewer.wicket.ui.components.entity.icontitle.EntityIconAndTitlePanelFactory;
import org.apache.causeway.viewer.wicket.ui.components.entity.icontitle.EntityIconTitleAndCopyLinkPanelFactory;
import org.apache.causeway.viewer.wicket.ui.components.footer.FooterPanelFactory;
import org.apache.causeway.viewer.wicket.ui.components.header.HeaderPanelFactory;
import org.apache.causeway.viewer.wicket.ui.components.layout.bs.BSGridPanelFactory;
import org.apache.causeway.viewer.wicket.ui.components.property.PropertyEditFormPanelFactory;
import org.apache.causeway.viewer.wicket.ui.components.property.PropertyEditPanelFactory;
import org.apache.causeway.viewer.wicket.ui.components.scalars.ComponentFactoryScalarTypeConstrainedAbstract;
import org.apache.causeway.viewer.wicket.ui.components.scalars.ScalarPanelTextFieldNumeric;
import org.apache.causeway.viewer.wicket.ui.components.scalars.ScalarPanelTextFieldWithTemporalPicker;
import org.apache.causeway.viewer.wicket.ui.components.scalars.ScalarPanelTextFieldWithValueSemantics;
import org.apache.causeway.viewer.wicket.ui.components.scalars.blobclob.CausewayBlobPanelFactory;
import org.apache.causeway.viewer.wicket.ui.components.scalars.blobclob.CausewayClobPanelFactory;
import org.apache.causeway.viewer.wicket.ui.components.scalars.bool.BooleanPanelFactory;
import org.apache.causeway.viewer.wicket.ui.components.scalars.choices.ChoicesSelect2PanelFactory;
import org.apache.causeway.viewer.wicket.ui.components.scalars.composite.CompositeValuePanel;
import org.apache.causeway.viewer.wicket.ui.components.scalars.image.JavaAwtImagePanelFactory;
import org.apache.causeway.viewer.wicket.ui.components.scalars.markup.MarkupPanelFactories;
import org.apache.causeway.viewer.wicket.ui.components.scalars.passwd.CausewayPasswordPanelFactory;
import org.apache.causeway.viewer.wicket.ui.components.scalars.string.StringPanelFactory;
import org.apache.causeway.viewer.wicket.ui.components.scalars.value.fallback.ValueFallbackPanelFactory;
import org.apache.causeway.viewer.wicket.ui.components.standalonecollection.StandaloneCollectionPanelFactory;
import org.apache.causeway.viewer.wicket.ui.components.tree.TreePanelFactories;
import org.apache.causeway.viewer.wicket.ui.components.unknown.UnknownModelPanelFactory;
import org.apache.causeway.viewer.wicket.ui.components.value.StandaloneValuePanelFactory;
import org.apache.causeway.viewer.wicket.ui.components.voidreturn.VoidReturnPanelFactory;
import org.apache.causeway.viewer.wicket.ui.components.welcome.WelcomePanelFactory;
import org.apache.causeway.viewer.wicket.ui.components.widgets.entitysimplelink.EntityLinkSimplePanelFactory;

import lombok.NonNull;
import lombok.extern.log4j.Log4j2;

/**
 * Registers a hardcoded set of built-in {@link ComponentFactory}s,
 * along with any implementations discovered by Spring.
 */
@Configuration
@Log4j2
public class ComponentFactoryConfigWkt {

    @Bean
    public ComponentFactoryList componentFactoryList(
            @Autowired(required = true) final ValueSemanticsResolver valueSemanticsResolver,
            @Autowired(required = false) final List<CollectionContentsExporter> collectionContentsExporters,
            final List<ComponentFactory> componentFactoriesPluggedIn) {
        var factoryList = new ComponentFactoryList();

        addComponentFactoriesActingAsSelectors(factoryList);

        log.info("adding {} ComponentFactories from plugins: {}",
                _NullSafe.size(componentFactoriesPluggedIn), componentFactoriesPluggedIn);
        _NullSafe.stream(componentFactoriesPluggedIn)
            .forEach(factoryList::add);

        //built-in
        addComponentFactoriesForWelcomeAndAbout(factoryList);
        addComponentFactoriesForApplicationActions(factoryList);
        addComponentFactoriesForEntity(factoryList);
        addComponentFactoriesForActionInfo(factoryList);
        addComponentFactoriesForAction(factoryList);
        addComponentFactoriesForPropertyEdit(factoryList);
        addComponentFactoriesForEntityCollectionContents(factoryList, collectionContentsExporters);
        addComponentFactoriesForEmptyCollection(factoryList);
        addComponentFactoriesForScalar(factoryList, valueSemanticsResolver);
        addComponentFactoriesForEntityLink(factoryList);
        addComponentFactoriesForVoidReturn(factoryList);
        addComponentFactoriesForValue(factoryList);
        addComponentFactoriesForParameters(factoryList);
        addComponentFactoriesForBreadcrumbs(factoryList);
        addComponentFactoriesForPageHeader(factoryList);
        addComponentFactoriesForPageFooter(factoryList);

        addComponentFactoriesForUnknown(factoryList);

        return factoryList;
    }

    @Bean
    public ComponentFactoryRegistry componentFactoryRegistry(
            @Autowired(required = true) final ComponentFactoryList factoryList,
            @Autowired(required = true) final MetaModelContext metaModelContext) {
        return new ComponentFactoryRegistry(factoryList, metaModelContext);
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
        componentFactories.add(new CollectionContentsMultipleViewsPanelFactory());
    }

    protected void addComponentFactoriesForPageHeader(final ComponentFactoryList componentFactories) {
        componentFactories.add(new HeaderPanelFactory());
    }

    protected void addComponentFactoriesForPageFooter(final ComponentFactoryList componentFactories) {
        componentFactories.add(new FooterPanelFactory());
    }

    protected void addComponentFactoriesForWelcomeAndAbout(final ComponentFactoryList componentFactories) {
        componentFactories.add(new WelcomePanelFactory());
        componentFactories.add(new AboutPanelFactory());
    }

    protected void addComponentFactoriesForEntity(final ComponentFactoryList componentFactories) {
        // top level
        componentFactories.add(new BSGridPanelFactory());

        // lower-level
        componentFactories.add(new EntityIconAndTitlePanelFactory());
        componentFactories.add(new EntityIconTitleAndCopyLinkPanelFactory());
        componentFactories.add(new EntityHeaderPanelFactory());
        componentFactories.add(new EntityCollectionPanelFactory());
    }

    protected void addComponentFactoriesForEntityCollectionContents(
            final ComponentFactoryList componentFactories,
            final List<CollectionContentsExporter> collectionContentsExporters) {
        componentFactories.add(new CollectionContentsAsAjaxTablePanelFactory());
        _NullSafe.stream(collectionContentsExporters)
            .map(CollectionContentsAsExportFactory::new)
            .forEach(componentFactories::add);
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

    protected void addComponentFactoriesForScalar(
            final ComponentFactoryList componentFactories,
            final ValueSemanticsResolver valueSemanticsResolver) {

        componentFactories.add(TreePanelFactories.parented());
        componentFactories.add(MarkupPanelFactories.parented());

        componentFactories.add(new BooleanPanelFactory());

        componentFactories.add(new StringPanelFactory());

        componentFactories.add(new JavaAwtImagePanelFactory());

        componentFactories.add(new CausewayPasswordPanelFactory());

        componentFactories.add(new CausewayBlobPanelFactory());
        componentFactories.add(new CausewayClobPanelFactory());

        // install after explicit values, but before fallbacks
        addGenericComponentFactoriesForScalar(componentFactories, valueSemanticsResolver);

        componentFactories.add(new ValueFallbackPanelFactory());

        // or for choices
        componentFactories.add(new ChoicesSelect2PanelFactory());

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

    protected void addComponentFactoriesForApplicationActions(final ComponentFactoryList componentFactories) {
        componentFactories.add(new ServiceActionsPanelFactory());
        componentFactories.add(new TertiaryMenuPanelFactory());
    }

    protected void addComponentFactoriesForBreadcrumbs(final ComponentFactoryList componentFactories) {
        componentFactories.add(new BookmarkedPagesPanelFactory());
    }

    protected void addComponentFactoriesForUnknown(final ComponentFactoryList componentFactories) {
        componentFactories.add(new UnknownModelPanelFactory());
    }

    // -- UTILTIY

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static <T extends Serializable> ComponentFactoryScalarTypeConstrainedAbstract
    createForValueSemantics(final ValueSemanticsProvider<T> valueSemantics) {

        if(valueSemantics.isNumberType()) {
            return new ScalarPanelFactoryForNumberField<T>(valueSemantics.getCorrespondingClass());
        }

        if(valueSemantics.isTemporalType()) {
            return _Casts.uncheckedCast(new ScalarPanelFactoryForTemporalPicker(valueSemantics.getCorrespondingClass()));
        }

        if(valueSemantics.isCompositeType()) {
            return new ScalarPanelFactoryForCompositeValue<T>(valueSemantics.getCorrespondingClass());
        }

        return new ScalarPanelFactoryForTextField<T>(valueSemantics.getCorrespondingClass());
    }

    public static class ScalarPanelFactoryForTextField<T extends Serializable>
    extends ComponentFactoryScalarTypeConstrainedAbstract {

        private final Class<T> valueTypeClass;

        protected ScalarPanelFactoryForTextField(final Class<T> valueTypeClass) {
            super(ScalarPanelTextFieldWithValueSemantics.class, withPrimitiveVariant(valueTypeClass));
            this.valueTypeClass = valueTypeClass;
        }

        @Override
        public Component createComponent(final String id, final ScalarModel scalarModel) {
            return new ScalarPanelTextFieldWithValueSemantics<T>(id, scalarModel, valueTypeClass);
        }
    }

    public static class ScalarPanelFactoryForNumberField<T extends Serializable>
    extends ComponentFactoryScalarTypeConstrainedAbstract {

        private final Class<T> valueTypeClass;

        protected ScalarPanelFactoryForNumberField(final Class<T> valueTypeClass) {
            super(ScalarPanelTextFieldNumeric.class, withPrimitiveVariant(valueTypeClass));
            this.valueTypeClass = valueTypeClass;
        }

        @Override
        public Component createComponent(final String id, final ScalarModel scalarModel) {
            return new ScalarPanelTextFieldNumeric<T>(id, scalarModel, valueTypeClass);
        }
    }

    public static class ScalarPanelFactoryForTemporalPicker<T extends Serializable & Temporal>
    extends ComponentFactoryScalarTypeConstrainedAbstract {

        private final Class<T> valueTypeClass;

        protected ScalarPanelFactoryForTemporalPicker(final Class<T> valueTypeClass) {
            super(ScalarPanelTextFieldWithTemporalPicker.class,
                    // assuming there is no primitive temporal type
                    valueTypeClass);
            this.valueTypeClass = valueTypeClass;
        }

        @Override
        public Component createComponent(final String id, final ScalarModel scalarModel) {
            return new ScalarPanelTextFieldWithTemporalPicker<T>(id, scalarModel, valueTypeClass);
        }
    }

    public static class ScalarPanelFactoryForCompositeValue<T extends Serializable>
    extends ComponentFactoryScalarTypeConstrainedAbstract {

        private final Class<T> valueTypeClass;

        protected ScalarPanelFactoryForCompositeValue(final Class<T> valueTypeClass) {
            super(CompositeValuePanel.class,
                    // assuming there is no primitive composite type
                    valueTypeClass);
            this.valueTypeClass = valueTypeClass;
        }

        @Override
        public Component createComponent(final String id, final ScalarModel scalarModel) {
            return new CompositeValuePanel<T>(id, scalarModel, valueTypeClass);
        }
    }

    // -- HELPER

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void addGenericComponentFactoriesForScalar(
            final ComponentFactoryList componentFactories,
            final ValueSemanticsResolver valueSemanticsResolver) {

        // collect those registered up to this point, so we don't override with generic ones at steps below
        var registeredScalarTypes =
                componentFactories.stream(ComponentFactoryScalarTypeConstrainedAbstract.class)
                .flatMap(f->f.getScalarTypes().stream())
                .collect(Collectors.toSet());

        valueSemanticsResolver.streamClassesWithValueSemantics()
            .filter(_Predicates.not(registeredScalarTypes::contains))
            .flatMap(valueSemanticsResolver::streamValueSemantics)
            //.peek(valueSemantics->System.err.printf("%s -> %s%n", valueSemantics, valueSemantics.getCorrespondingClass().getName()))
            .map(valueSemantics->createForValueSemantics((ValueSemanticsProvider)valueSemantics))
            .forEach(componentFactories::add);
    }

    private static Can<Class<?>> withPrimitiveVariant(final @NonNull Class<?> valueTypeClass) {
        var valueTypeClasses = Can.<Class<?>>ofSingleton(valueTypeClass);

        // if the valueType is a wrapper type, also append its unboxed variant
        if(ClassUtils.isPrimitiveWrapper(valueTypeClass)) {
            var unboxed = org.apache.causeway.core.metamodel.commons.ClassUtil
                    .unboxPrimitiveIfNecessary(valueTypeClass);
            valueTypeClasses = valueTypeClasses.add(unboxed);
        }

        return valueTypeClasses;
    }

}
