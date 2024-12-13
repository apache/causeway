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

import org.apache.causeway.applib.tabular.TabularExporter;
import org.apache.causeway.applib.value.semantics.ValueSemanticsProvider;
import org.apache.causeway.applib.value.semantics.ValueSemanticsResolver;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.commons.internal.functions._Predicates;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.viewer.wicket.model.models.UiAttributeWkt;
import org.apache.causeway.viewer.wicket.ui.ComponentFactory;
import org.apache.causeway.viewer.wicket.ui.app.registry.ComponentFactoryList;
import org.apache.causeway.viewer.wicket.ui.app.registry.ComponentFactoryRegistry;
import org.apache.causeway.viewer.wicket.ui.components.about.AboutPanelFactory;
import org.apache.causeway.viewer.wicket.ui.components.actioninfo.ActionInfoPanelFactory;
import org.apache.causeway.viewer.wicket.ui.components.actionlinks.serviceactions.ServiceActionsPanelFactory;
import org.apache.causeway.viewer.wicket.ui.components.actionlinks.serviceactions.TertiaryMenuPanelFactory;
import org.apache.causeway.viewer.wicket.ui.components.actions.ActionParametersFormPanelFactory;
import org.apache.causeway.viewer.wicket.ui.components.actions.ActionParametersPanelFactory;
import org.apache.causeway.viewer.wicket.ui.components.attributes.AttributeComponentFactoryWithTypeConstraint;
import org.apache.causeway.viewer.wicket.ui.components.attributes.NumericAttributePanel;
import org.apache.causeway.viewer.wicket.ui.components.attributes.blobclob.BlobAttributePanelFactory;
import org.apache.causeway.viewer.wicket.ui.components.attributes.blobclob.ClobAttributePanelFactory;
import org.apache.causeway.viewer.wicket.ui.components.attributes.bool.BooleanAttributePanelFactory;
import org.apache.causeway.viewer.wicket.ui.components.attributes.choices.ChoicesSelect2PanelFactory;
import org.apache.causeway.viewer.wicket.ui.components.attributes.image.JavaAwtImagePanelFactory;
import org.apache.causeway.viewer.wicket.ui.components.attributes.markup.MarkupAttributePanelFactories;
import org.apache.causeway.viewer.wicket.ui.components.attributes.passwd.PasswordAttributePanelFactory;
import org.apache.causeway.viewer.wicket.ui.components.attributes.string.StringAttributePanelFactory;
import org.apache.causeway.viewer.wicket.ui.components.attributes.temporal.TemporalAttributePanel;
import org.apache.causeway.viewer.wicket.ui.components.attributes.value.CompositeValueAttributePanel;
import org.apache.causeway.viewer.wicket.ui.components.attributes.value.ValueAttributePanel;
import org.apache.causeway.viewer.wicket.ui.components.attributes.value.ValueFallbackAttributePanelFactory;
import org.apache.causeway.viewer.wicket.ui.components.bookmarkedpages.BookmarkedPagesPanelFactory;
import org.apache.causeway.viewer.wicket.ui.components.collection.export.CollectionContentsAsExportFactory;
import org.apache.causeway.viewer.wicket.ui.components.collection.parented.ParentedCollectionPanelFactory;
import org.apache.causeway.viewer.wicket.ui.components.collection.present.ajaxtable.CollectionContentsAsAjaxTablePanelFactory;
import org.apache.causeway.viewer.wicket.ui.components.collection.present.multiple.CollectionContentsMultipleViewsPanelFactory;
import org.apache.causeway.viewer.wicket.ui.components.collection.present.summary.CollectionContentsAsSummaryFactory;
import org.apache.causeway.viewer.wicket.ui.components.collection.present.unresolved.CollectionContentsHiddenPanelFactory;
import org.apache.causeway.viewer.wicket.ui.components.collection.standalone.StandaloneCollectionPanelFactory;
import org.apache.causeway.viewer.wicket.ui.components.empty.EmptyCollectionPanelFactory;
import org.apache.causeway.viewer.wicket.ui.components.footer.FooterPanelFactory;
import org.apache.causeway.viewer.wicket.ui.components.header.HeaderPanelFactory;
import org.apache.causeway.viewer.wicket.ui.components.layout.bs.BSGridPanelFactory;
import org.apache.causeway.viewer.wicket.ui.components.object.header.ObjectHeaderPanelFactory;
import org.apache.causeway.viewer.wicket.ui.components.object.icontitle.ObjectIconAndTitlePanelFactory;
import org.apache.causeway.viewer.wicket.ui.components.object.icontitle.ObjectIconTitleAndCopyLinkPanelFactory;
import org.apache.causeway.viewer.wicket.ui.components.property.PropertyEditFormPanelFactory;
import org.apache.causeway.viewer.wicket.ui.components.property.PropertyEditPanelFactory;
import org.apache.causeway.viewer.wicket.ui.components.tree.TreePanelFactories;
import org.apache.causeway.viewer.wicket.ui.components.unknown.UnknownModelPanelFactory;
import org.apache.causeway.viewer.wicket.ui.components.value.StandaloneValuePanelFactory;
import org.apache.causeway.viewer.wicket.ui.components.voidreturn.VoidReturnPanelFactory;
import org.apache.causeway.viewer.wicket.ui.components.welcome.WelcomePanelFactory;
import org.apache.causeway.viewer.wicket.ui.components.widgets.objectsimplelink.ObjectLinkSimplePanelFactory;

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
            @Autowired(required = false) final List<TabularExporter> tabularExporters,
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
        addComponentFactoriesForEntityCollectionContents(factoryList, tabularExporters);
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
        componentFactories.add(new ObjectIconAndTitlePanelFactory());
        componentFactories.add(new ObjectIconTitleAndCopyLinkPanelFactory());
        componentFactories.add(new ObjectHeaderPanelFactory());
        componentFactories.add(new ParentedCollectionPanelFactory());
    }

    protected void addComponentFactoriesForEntityCollectionContents(
            final ComponentFactoryList componentFactories, final List<TabularExporter> tabularExporters) {
        componentFactories.add(new CollectionContentsAsAjaxTablePanelFactory());
        _NullSafe.stream(tabularExporters)
            .map(CollectionContentsAsExportFactory::new)
            .forEach(componentFactories::add);
        componentFactories.add(new CollectionContentsAsSummaryFactory());
    }

    protected void addComponentFactoriesForEmptyCollection(final ComponentFactoryList componentFactories) {
        componentFactories.add(new EmptyCollectionPanelFactory());
    }

    protected void addComponentFactoriesForValue(final ComponentFactoryList componentFactories) {
        componentFactories.add(MarkupAttributePanelFactories.standalone());
        componentFactories.add(TreePanelFactories.standalone());
        componentFactories.add(new StandaloneValuePanelFactory());
    }

    protected void addComponentFactoriesForScalar(
            final ComponentFactoryList componentFactories,
            final ValueSemanticsResolver valueSemanticsResolver) {

        componentFactories.add(TreePanelFactories.parented());
        componentFactories.add(MarkupAttributePanelFactories.parented());

        componentFactories.add(new BooleanAttributePanelFactory());

        componentFactories.add(new StringAttributePanelFactory());

        componentFactories.add(new JavaAwtImagePanelFactory());

        componentFactories.add(new PasswordAttributePanelFactory());

        componentFactories.add(new BlobAttributePanelFactory());
        componentFactories.add(new ClobAttributePanelFactory());

        // install after explicit values, but before fallbacks
        addGenericComponentFactoriesForScalar(componentFactories, valueSemanticsResolver);

        componentFactories.add(new ValueFallbackAttributePanelFactory());

        // or for choices
        componentFactories.add(new ChoicesSelect2PanelFactory());

    }

    protected void addComponentFactoriesForEntityLink(final ComponentFactoryList componentFactories) {
        componentFactories.add(new ObjectLinkSimplePanelFactory());
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
    public static <T extends Serializable> AttributeComponentFactoryWithTypeConstraint
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
    extends AttributeComponentFactoryWithTypeConstraint {

        private final Class<T> valueTypeClass;

        protected ScalarPanelFactoryForTextField(final Class<T> valueTypeClass) {
            super(ValueAttributePanel.class, withPrimitiveVariant(valueTypeClass));
            this.valueTypeClass = valueTypeClass;
        }

        @Override
        public Component createComponent(final String id, final UiAttributeWkt attributeModel) {
            return new ValueAttributePanel<T>(id, attributeModel, valueTypeClass);
        }
    }

    public static class ScalarPanelFactoryForNumberField<T extends Serializable>
    extends AttributeComponentFactoryWithTypeConstraint {

        private final Class<T> valueTypeClass;

        protected ScalarPanelFactoryForNumberField(final Class<T> valueTypeClass) {
            super(NumericAttributePanel.class, withPrimitiveVariant(valueTypeClass));
            this.valueTypeClass = valueTypeClass;
        }

        @Override
        public Component createComponent(final String id, final UiAttributeWkt attributeModel) {
            return new NumericAttributePanel<T>(id, attributeModel, valueTypeClass);
        }
    }

    public static class ScalarPanelFactoryForTemporalPicker<T extends Serializable & Temporal>
    extends AttributeComponentFactoryWithTypeConstraint {

        private final Class<T> valueTypeClass;

        protected ScalarPanelFactoryForTemporalPicker(final Class<T> valueTypeClass) {
            super(TemporalAttributePanel.class,
                    // assuming there is no primitive temporal type
                    valueTypeClass);
            this.valueTypeClass = valueTypeClass;
        }

        @Override
        public Component createComponent(final String id, final UiAttributeWkt attributeModel) {
            return new TemporalAttributePanel<T>(id, attributeModel, valueTypeClass);
        }
    }

    public static class ScalarPanelFactoryForCompositeValue<T extends Serializable>
    extends AttributeComponentFactoryWithTypeConstraint {

        private final Class<T> valueTypeClass;

        protected ScalarPanelFactoryForCompositeValue(final Class<T> valueTypeClass) {
            super(CompositeValueAttributePanel.class,
                    // assuming there is no primitive composite type
                    valueTypeClass);
            this.valueTypeClass = valueTypeClass;
        }

        @Override
        public Component createComponent(final String id, final UiAttributeWkt attributeModel) {
            return new CompositeValueAttributePanel<T>(id, attributeModel, valueTypeClass);
        }
    }

    // -- HELPER

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void addGenericComponentFactoriesForScalar(
            final ComponentFactoryList componentFactories,
            final ValueSemanticsResolver valueSemanticsResolver) {

        // collect those registered up to this point, so we don't override with generic ones at steps below
        var registeredElementTypes =
                componentFactories.stream(AttributeComponentFactoryWithTypeConstraint.class)
                .flatMap(f->f.elementTypes().stream())
                .collect(Collectors.toSet());

        valueSemanticsResolver.streamClassesWithValueSemantics()
            .filter(_Predicates.not(registeredElementTypes::contains))
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
