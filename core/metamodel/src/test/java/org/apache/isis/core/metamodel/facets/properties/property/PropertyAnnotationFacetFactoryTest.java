package org.apache.isis.core.metamodel.facets.properties.property;

import java.lang.reflect.Method;
import java.util.List;
import java.util.regex.Pattern;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.annotation.Optionality;
import org.apache.isis.applib.annotation.PostsPropertyChangedEvent;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.PropertyInteraction;
import org.apache.isis.applib.annotation.When;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.services.eventbus.PropertyChangedEvent;
import org.apache.isis.applib.services.eventbus.PropertyDomainEvent;
import org.apache.isis.applib.services.eventbus.PropertyInteractionEvent;
import org.apache.isis.applib.spec.Specification;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facets.AbstractFacetFactoryJUnit4TestCase;
import org.apache.isis.core.metamodel.facets.FacetFactory;
import org.apache.isis.core.metamodel.facets.all.hide.HiddenFacet;
import org.apache.isis.core.metamodel.facets.members.disabled.DisabledFacet;
import org.apache.isis.core.metamodel.facets.object.regex.RegExFacet;
import org.apache.isis.core.metamodel.facets.propcoll.accessor.PropertyOrCollectionAccessorFacetAbstract;
import org.apache.isis.core.metamodel.facets.propcoll.notpersisted.NotPersistedFacet;
import org.apache.isis.core.metamodel.facets.properties.property.disabled.DisabledFacetForPropertyAnnotation;
import org.apache.isis.core.metamodel.facets.properties.property.hidden.HiddenFacetForPropertyAnnotation;
import org.apache.isis.core.metamodel.facets.properties.property.mandatory.MandatoryFacetForPropertyAnnotation;
import org.apache.isis.core.metamodel.facets.properties.property.maxlength.MaxLengthFacetForPropertyAnnotation;
import org.apache.isis.core.metamodel.facets.properties.property.modify.PropertyClearFacetForDomainEventFromDefault;
import org.apache.isis.core.metamodel.facets.properties.property.modify.PropertyClearFacetForDomainEventFromPropertyAnnotation;
import org.apache.isis.core.metamodel.facets.properties.property.modify.PropertyClearFacetForDomainEventFromPropertyInteractionAnnotation;
import org.apache.isis.core.metamodel.facets.properties.property.modify.PropertyClearFacetForPostsPropertyChangedEventAnnotation;
import org.apache.isis.core.metamodel.facets.properties.property.modify.PropertyDomainEventFacet;
import org.apache.isis.core.metamodel.facets.properties.property.modify.PropertyDomainEventFacetDefault;
import org.apache.isis.core.metamodel.facets.properties.property.modify.PropertyDomainEventFacetForPropertyAnnotation;
import org.apache.isis.core.metamodel.facets.properties.property.modify.PropertyDomainEventFacetForPropertyInteractionAnnotation;
import org.apache.isis.core.metamodel.facets.properties.property.modify.PropertySetterFacetForDomainEventFromDefault;
import org.apache.isis.core.metamodel.facets.properties.property.modify.PropertySetterFacetForDomainEventFromPropertyAnnotation;
import org.apache.isis.core.metamodel.facets.properties.property.modify.PropertySetterFacetForDomainEventFromPropertyInteractionAnnotation;
import org.apache.isis.core.metamodel.facets.properties.property.modify.PropertySetterFacetForPostsPropertyChangedEventAnnotation;
import org.apache.isis.core.metamodel.facets.properties.property.mustsatisfy.MustSatisfySpecificationFacetForPropertyAnnotation;
import org.apache.isis.core.metamodel.facets.properties.property.notpersisted.NotPersistedFacetForPropertyAnnotation;
import org.apache.isis.core.metamodel.facets.properties.property.regex.RegExFacetForPropertyAnnotation;
import org.apache.isis.core.metamodel.facets.properties.update.clear.PropertyClearFacet;
import org.apache.isis.core.metamodel.facets.properties.update.clear.PropertyClearFacetAbstract;
import org.apache.isis.core.metamodel.facets.properties.update.modify.PropertySetterFacet;
import org.apache.isis.core.metamodel.facets.properties.update.modify.PropertySetterFacetAbstract;
import org.apache.isis.core.metamodel.facets.propparam.mandatory.MandatoryFacet;
import org.apache.isis.core.metamodel.facets.propparam.maxlen.MaxLengthFacet;
import org.apache.isis.core.metamodel.facets.propparam.mustsatisfyspec.MustSatisfySpecificationFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

import static org.apache.isis.core.commons.matchers.IsisMatchers.classEqualTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class PropertyAnnotationFacetFactoryTest extends AbstractFacetFactoryJUnit4TestCase {

    PropertyAnnotationFacetFactory facetFactory;
    Method propertyMethod;

    @Mock
    ObjectSpecification mockTypeSpec;
    @Mock
    ObjectSpecification mockReturnTypeSpec;

    void expectRemoveMethod(final Method actionMethod) {
        context.checking(new Expectations() {{
            oneOf(mockMethodRemover).removeMethod(actionMethod);
        }});
    }

    void allowingLoadSpecificationRequestsFor(final Class<?> cls, final Class<?> returnType) {
        context.checking(new Expectations() {{
            allowing(mockSpecificationLoaderSpi).loadSpecification(cls);
            will(returnValue(mockTypeSpec));

            allowing(mockSpecificationLoaderSpi).loadSpecification(returnType);
            will(returnValue(mockReturnTypeSpec));
        }});
    }

    @Before
    public void setUp() throws Exception {
        facetFactory = new PropertyAnnotationFacetFactory();
        facetFactory.setSpecificationLookup(mockSpecificationLoaderSpi);
    }

    @After
    public void tearDown() throws Exception {
        facetFactory = null;
    }

    public static class Modify extends PropertyAnnotationFacetFactoryTest {

        private void addGetterFacet(final FacetHolder holder) {
            FacetUtil.addFacet(new PropertyOrCollectionAccessorFacetAbstract(holder) {
                @Override
                public Object getProperty(final ObjectAdapter inObject) {
                    return null;
                }
            });
        }

        private void addSetterFacet(final FacetHolder holder) {
            FacetUtil.addFacet(new PropertySetterFacetAbstract(holder) {
                @Override
                public void setProperty(final ObjectAdapter inObject, final ObjectAdapter value) {
                }
            });
        }

        private void addClearFacet(final FacetHolder holder) {
            FacetUtil.addFacet(new PropertyClearFacetAbstract(holder) {
                @Override
                public void clearProperty(final ObjectAdapter inObject) {
                }
            });
        }


        @Test
        public void withDeprecatedPostsPropertyChangedEvent_andGetterFacet_andSetterFacet() {

            class Customer {
                class NamedChanged extends PropertyChangedEvent<Customer, String> {
                    public NamedChanged(final Customer source, final String oldValue, final String newValue) {
                        super(source, oldValue, newValue);
                    }
                }
                @PostsPropertyChangedEvent(Customer.NamedChanged.class)
                public String getName() {
                    return null;
                }
                public void setName(final String name) {
                }
            }

            // given
            final Class<?> cls = Customer.class;
            propertyMethod = findMethod(Customer.class, "getName");

            addGetterFacet(facetedMethod);
            addSetterFacet(facetedMethod);
            addClearFacet(facetedMethod);

            // expect
            allowingLoadSpecificationRequestsFor(cls, propertyMethod.getReturnType());

            // when
            final FacetFactory.ProcessMethodContext processMethodContext = new FacetFactory.ProcessMethodContext(cls, null, null, propertyMethod, mockMethodRemover, facetedMethod);
            facetFactory.processModify(processMethodContext);

            // then
            final Facet domainEventFacet = facetedMethod.getFacet(PropertyDomainEventFacet.class);
            Assert.assertNotNull(domainEventFacet);
            Assert.assertTrue(domainEventFacet instanceof PropertyDomainEventFacetDefault);
            final PropertyDomainEventFacetDefault domainEventFacetDefault = (PropertyDomainEventFacetDefault) domainEventFacet;
            assertThat(domainEventFacetDefault.getEventType(), classEqualTo(PropertyDomainEvent.Default.class)); // this is discarded at runtime, see PropertySetterFacetForPostsPropertyChangedEventAnnotation#verify(...)

            // then
            final Facet setterFacet = facetedMethod.getFacet(PropertySetterFacet.class);
            Assert.assertNotNull(setterFacet);
            Assert.assertTrue(setterFacet instanceof PropertySetterFacetForPostsPropertyChangedEventAnnotation);
            final PropertySetterFacetForPostsPropertyChangedEventAnnotation setterFacetImpl = (PropertySetterFacetForPostsPropertyChangedEventAnnotation) setterFacet;
            assertThat(setterFacetImpl.value(), classEqualTo(Customer.NamedChanged.class));

            // then
            final Facet clearFacet = facetedMethod.getFacet(PropertyClearFacet.class);
            Assert.assertNotNull(clearFacet);
            Assert.assertTrue(clearFacet instanceof PropertyClearFacetForPostsPropertyChangedEventAnnotation);
            final PropertyClearFacetForPostsPropertyChangedEventAnnotation clearFacetImpl = (PropertyClearFacetForPostsPropertyChangedEventAnnotation) clearFacet;
            assertThat(clearFacetImpl.value(), classEqualTo(Customer.NamedChanged.class));
        }


        @Test
        public void withPropertyInteractionEvent() {

            class Customer {
                class NamedChanged extends PropertyInteractionEvent<Customer, String> {
                    public NamedChanged(final Customer source, final Identifier identifier, final String oldValue, final String newValue) {
                        super(source, identifier, oldValue, newValue);
                    }
                }
                @PropertyInteraction(Customer.NamedChanged.class)
                public String getName() {
                    return null;
                }
                public void setName(final String name) {
                }
            }

            // given
            final Class<?> cls = Customer.class;
            propertyMethod = findMethod(Customer.class, "getName");

            addGetterFacet(facetedMethod);
            addSetterFacet(facetedMethod);
            addClearFacet(facetedMethod);

            // expect
            allowingLoadSpecificationRequestsFor(cls, propertyMethod.getReturnType());

            // when
            final FacetFactory.ProcessMethodContext processMethodContext = new FacetFactory.ProcessMethodContext(cls, null, null, propertyMethod, mockMethodRemover, facetedMethod);
            facetFactory.processModify(processMethodContext);

            // then
            final Facet domainEventFacet = facetedMethod.getFacet(PropertyDomainEventFacet.class);
            Assert.assertNotNull(domainEventFacet);
            Assert.assertTrue(domainEventFacet instanceof PropertyDomainEventFacetForPropertyInteractionAnnotation);
            final PropertyDomainEventFacetForPropertyInteractionAnnotation domainEventFacetImpl = (PropertyDomainEventFacetForPropertyInteractionAnnotation) domainEventFacet;
            assertThat(domainEventFacetImpl.value(), classEqualTo(Customer.NamedChanged.class));

            // then
            final Facet setterFacet = facetedMethod.getFacet(PropertySetterFacet.class);
            Assert.assertNotNull(setterFacet);
            Assert.assertTrue(setterFacet instanceof PropertySetterFacetForDomainEventFromPropertyInteractionAnnotation);
            final PropertySetterFacetForDomainEventFromPropertyInteractionAnnotation setterFacetImpl = (PropertySetterFacetForDomainEventFromPropertyInteractionAnnotation) setterFacet;
            assertThat(setterFacetImpl.value(), classEqualTo(Customer.NamedChanged.class));

            // then
            final Facet clearFacet = facetedMethod.getFacet(PropertyClearFacet.class);
            Assert.assertNotNull(clearFacet);
            Assert.assertTrue(clearFacet instanceof PropertyClearFacetForDomainEventFromPropertyInteractionAnnotation);
            final PropertyClearFacetForDomainEventFromPropertyInteractionAnnotation clearFacetImpl = (PropertyClearFacetForDomainEventFromPropertyInteractionAnnotation) clearFacet;
            assertThat(clearFacetImpl.value(), classEqualTo(Customer.NamedChanged.class));
        }

        @Test
        public void withPropertyDomainEvent() {

            class Customer {
                class NamedChanged extends PropertyDomainEvent<Customer, String> {
                    public NamedChanged(final Customer source, final Identifier identifier, final String oldValue, final String newValue) {
                        super(source, identifier, oldValue, newValue);
                    }
                }
                @Property(domainEvent=Customer.NamedChanged.class)
                public String getName() {
                    return null;
                }
                public void setName(final String name) {
                }
            }

            // given
            final Class<?> cls = Customer.class;
            propertyMethod = findMethod(Customer.class, "getName");

            addGetterFacet(facetedMethod);
            addSetterFacet(facetedMethod);
            addClearFacet(facetedMethod);

            // expect
            allowingLoadSpecificationRequestsFor(cls, propertyMethod.getReturnType());

            // when
            final FacetFactory.ProcessMethodContext processMethodContext = new FacetFactory.ProcessMethodContext(cls, null, null, propertyMethod, mockMethodRemover, facetedMethod);
            facetFactory.processModify(processMethodContext);

            // then
            final Facet domainEventFacet = facetedMethod.getFacet(PropertyDomainEventFacet.class);
            Assert.assertNotNull(domainEventFacet);
            Assert.assertTrue(domainEventFacet instanceof PropertyDomainEventFacetForPropertyAnnotation);
            final PropertyDomainEventFacetForPropertyAnnotation domainEventFacetImpl = (PropertyDomainEventFacetForPropertyAnnotation) domainEventFacet;
            assertThat(domainEventFacetImpl.value(), classEqualTo(Customer.NamedChanged.class));

            // then
            final Facet setterFacet = facetedMethod.getFacet(PropertySetterFacet.class);
            Assert.assertNotNull(setterFacet);
            Assert.assertTrue(setterFacet instanceof PropertySetterFacetForDomainEventFromPropertyAnnotation);
            final PropertySetterFacetForDomainEventFromPropertyAnnotation setterFacetImpl = (PropertySetterFacetForDomainEventFromPropertyAnnotation) setterFacet;
            assertThat(setterFacetImpl.value(), classEqualTo(Customer.NamedChanged.class));

            // then
            final Facet clearFacet = facetedMethod.getFacet(PropertyClearFacet.class);
            Assert.assertNotNull(clearFacet);
            Assert.assertTrue(clearFacet instanceof PropertyClearFacetForDomainEventFromPropertyAnnotation);
            final PropertyClearFacetForDomainEventFromPropertyAnnotation clearFacetImpl = (PropertyClearFacetForDomainEventFromPropertyAnnotation) clearFacet;
            assertThat(clearFacetImpl.value(), classEqualTo(Customer.NamedChanged.class));
        }

        @Test
        public void withDefaultEvent() {

            class Customer {
                public String getName() {
                    return null;
                }
                public void setName(final String name) {
                }
            }

            // given
            final Class<?> cls = Customer.class;
            propertyMethod = findMethod(Customer.class, "getName");

            addGetterFacet(facetedMethod);
            addSetterFacet(facetedMethod);
            addClearFacet(facetedMethod);

            // expect
            allowingLoadSpecificationRequestsFor(cls, propertyMethod.getReturnType());

            // when
            final FacetFactory.ProcessMethodContext processMethodContext = new FacetFactory.ProcessMethodContext(cls, null, null, propertyMethod, mockMethodRemover, facetedMethod);
            facetFactory.processModify(processMethodContext);

            // then
            final Facet domainEventFacet = facetedMethod.getFacet(PropertyDomainEventFacet.class);
            Assert.assertNotNull(domainEventFacet);
            Assert.assertTrue(domainEventFacet instanceof PropertyDomainEventFacetDefault);
            final PropertyDomainEventFacetDefault domainEventFacetImpl = (PropertyDomainEventFacetDefault) domainEventFacet;
            assertThat(domainEventFacetImpl.value(), classEqualTo(PropertyDomainEvent.Default.class));

            // then
            final Facet setterFacet = facetedMethod.getFacet(PropertySetterFacet.class);
            Assert.assertNotNull(setterFacet);
            Assert.assertTrue(setterFacet instanceof PropertySetterFacetForDomainEventFromDefault);
            final PropertySetterFacetForDomainEventFromDefault setterFacetImpl = (PropertySetterFacetForDomainEventFromDefault) setterFacet;
            assertThat(setterFacetImpl.value(), classEqualTo(PropertyDomainEvent.Default.class));

            // then
            final Facet clearFacet = facetedMethod.getFacet(PropertyClearFacet.class);
            Assert.assertNotNull(clearFacet);
            Assert.assertTrue(clearFacet instanceof PropertyClearFacetForDomainEventFromDefault);
            final PropertyClearFacetForDomainEventFromDefault clearFacetImpl = (PropertyClearFacetForDomainEventFromDefault) clearFacet;
            assertThat(clearFacetImpl.value(), classEqualTo(PropertyDomainEvent.Default.class));
        }
    }

    public static class Hidden extends PropertyAnnotationFacetFactoryTest {

        @Test
        public void withAnnotation() {

            class Customer {
                @Property(hidden = Where.REFERENCES_PARENT)
                public String getName() {
                    return null;
                }
                public void setName(final String name) {
                }
            }

            // given
            final Class<?> cls = Customer.class;
            propertyMethod = findMethod(Customer.class, "getName");

            // when
            final FacetFactory.ProcessMethodContext processMethodContext = new FacetFactory.ProcessMethodContext(cls, null, null, propertyMethod, mockMethodRemover, facetedMethod);
            facetFactory.processHidden(processMethodContext);

            // then
            final HiddenFacet hiddenFacet = facetedMethod.getFacet(HiddenFacet.class);
            Assert.assertNotNull(hiddenFacet);
            Assert.assertTrue(hiddenFacet instanceof HiddenFacetForPropertyAnnotation);
            final HiddenFacetForPropertyAnnotation hiddenFacetImpl = (HiddenFacetForPropertyAnnotation) hiddenFacet;
            assertThat(hiddenFacetImpl.where(), is(Where.REFERENCES_PARENT));
            assertThat(hiddenFacetImpl.when(), is(When.ALWAYS));

            final Facet hiddenFacetForProp = facetedMethod.getFacet(HiddenFacetForPropertyAnnotation.class);
            Assert.assertNotNull(hiddenFacetForProp);
            Assert.assertTrue(hiddenFacet == hiddenFacetForProp);
        }

    }

    public static class Editing extends PropertyAnnotationFacetFactoryTest {

        @Test
        public void withAnnotation() {

            class Customer {
                @Property(
                        editing = org.apache.isis.applib.annotation.Editing.DISABLED,
                        editingDisabledReason = "you cannot edit the name property"
                )
                public String getName() {
                    return null;
                }
                public void setName(final String name) {
                }
            }

            // given
            final Class<?> cls = Customer.class;
            propertyMethod = findMethod(Customer.class, "getName");

            // when
            final FacetFactory.ProcessMethodContext processMethodContext = new FacetFactory.ProcessMethodContext(cls, null, null, propertyMethod, mockMethodRemover, facetedMethod);
            facetFactory.processEditing(processMethodContext);

            // then
            final DisabledFacet disabledFacet = facetedMethod.getFacet(DisabledFacet.class);
            Assert.assertNotNull(disabledFacet);
            Assert.assertTrue(disabledFacet instanceof DisabledFacetForPropertyAnnotation);
            final DisabledFacetForPropertyAnnotation disabledFacetImpl = (DisabledFacetForPropertyAnnotation) disabledFacet;
            assertThat(disabledFacet.where(), is(Where.EVERYWHERE));
            assertThat(disabledFacet.when(), is(When.ALWAYS));
            assertThat(disabledFacetImpl.getReason(), is("you cannot edit the name property"));
        }
    }

    public static class MaxLength extends PropertyAnnotationFacetFactoryTest {

        @Test
        public void withAnnotation() {

            class Customer {
                @Property(
                        maxLength = 30
                )
                public String getName() {
                    return null;
                }
                public void setName(final String name) {
                }
            }

            // given
            final Class<?> cls = Customer.class;
            propertyMethod = findMethod(Customer.class, "getName");

            // when
            final FacetFactory.ProcessMethodContext processMethodContext = new FacetFactory.ProcessMethodContext(cls, null, null, propertyMethod, mockMethodRemover, facetedMethod);
            facetFactory.processMaxLength(processMethodContext);

            // then
            final MaxLengthFacet maxLengthFacet = facetedMethod.getFacet(MaxLengthFacet.class);
            Assert.assertNotNull(maxLengthFacet);
            Assert.assertTrue(maxLengthFacet instanceof MaxLengthFacetForPropertyAnnotation);
            assertThat(maxLengthFacet.value(), is(30));
        }
    }

    public static class MustSatisfy extends PropertyAnnotationFacetFactoryTest {

        public static class NotTooHot implements Specification {
            @Override
            public String satisfies(final Object obj) {
                return null;
            }
        }

        public static class NotTooCold implements Specification {
            @Override
            public String satisfies(final Object obj) {
                return null;
            }
        }


        @Test
        public void withAnnotation() {

            class Customer {
                @Property(
                        mustSatisfy = {NotTooHot.class, NotTooCold.class}
                )
                public String getName() {
                    return null;
                }
                public void setName(final String name) {
                }
            }

            // given
            final Class<?> cls = Customer.class;
            propertyMethod = findMethod(Customer.class, "getName");

            // when
            final FacetFactory.ProcessMethodContext processMethodContext = new FacetFactory.ProcessMethodContext(cls, null, null, propertyMethod, mockMethodRemover, facetedMethod);
            facetFactory.processMustSatisfy(processMethodContext);

            // then
            final MustSatisfySpecificationFacet mustSatisfySpecificationFacet = facetedMethod.getFacet(MustSatisfySpecificationFacet.class);
            Assert.assertNotNull(mustSatisfySpecificationFacet);
            Assert.assertTrue(mustSatisfySpecificationFacet instanceof MustSatisfySpecificationFacetForPropertyAnnotation);
            final MustSatisfySpecificationFacetForPropertyAnnotation mustSatisfySpecificationFacetImpl = (MustSatisfySpecificationFacetForPropertyAnnotation) mustSatisfySpecificationFacet;
            final List<Specification> specifications = mustSatisfySpecificationFacetImpl.getSpecifications();
            assertThat(specifications.size(), is(2));

            assertTrue(specifications.get(0) instanceof NotTooHot);
            assertTrue(specifications.get(1) instanceof NotTooCold);
        }

    }

    public static class NotPersisted extends PropertyAnnotationFacetFactoryTest {

        @Test
        public void withAnnotation() {

            class Customer {
                @Property(
                        notPersisted = true
                )
                public String getName() {
                    return null;
                }
                public void setName(final String name) {
                }
            }

            // given
            final Class<?> cls = Customer.class;
            propertyMethod = findMethod(Customer.class, "getName");

            // when
            final FacetFactory.ProcessMethodContext processMethodContext = new FacetFactory.ProcessMethodContext(cls, null, null, propertyMethod, mockMethodRemover, facetedMethod);
            facetFactory.processNotPersisted(processMethodContext);

            // then
            final NotPersistedFacet notPersistedFacet = facetedMethod.getFacet(NotPersistedFacet.class);
            Assert.assertNotNull(notPersistedFacet);
            Assert.assertTrue(notPersistedFacet instanceof NotPersistedFacetForPropertyAnnotation);
        }

    }

    public static class Mandatory extends PropertyAnnotationFacetFactoryTest {

        @Test
        public void whenOptionalityIsTrue() {

            class Customer {
                @Property(
                        optional = Optionality.TRUE
                )
                public String getName() {
                    return null;
                }
                public void setName(final String name) {
                }
            }

            // given
            final Class<?> cls = Customer.class;
            propertyMethod = findMethod(Customer.class, "getName");

            // when
            final FacetFactory.ProcessMethodContext processMethodContext = new FacetFactory.ProcessMethodContext(cls, null, null, propertyMethod, mockMethodRemover, facetedMethod);
            facetFactory.processOptional(processMethodContext);

            // then
            final MandatoryFacet mandatoryFacet = facetedMethod.getFacet(MandatoryFacet.class);
            Assert.assertNotNull(mandatoryFacet);
            Assert.assertTrue(mandatoryFacet instanceof MandatoryFacetForPropertyAnnotation.Optional);
        }

        @Test
        public void whenOptionalityIsFalse() {

            class Customer {
                @Property(
                        optional = Optionality.FALSE
                )
                public String getName() {
                    return null;
                }
                public void setName(final String name) {
                }
            }

            // given
            final Class<?> cls = Customer.class;
            propertyMethod = findMethod(Customer.class, "getName");

            // when
            final FacetFactory.ProcessMethodContext processMethodContext = new FacetFactory.ProcessMethodContext(cls, null, null, propertyMethod, mockMethodRemover, facetedMethod);
            facetFactory.processOptional(processMethodContext);

            // then
            final MandatoryFacet mandatoryFacet = facetedMethod.getFacet(MandatoryFacet.class);
            Assert.assertNotNull(mandatoryFacet);
            Assert.assertTrue(mandatoryFacet instanceof MandatoryFacetForPropertyAnnotation.Required);
        }

        @Test
        public void whenOptionalityIsDefault() {

            class Customer {
                @Property(
                        optional = Optionality.DEFAULT
                )
                public String getName() {
                    return null;
                }
                public void setName(final String name) {
                }
            }

            // given
            final Class<?> cls = Customer.class;
            propertyMethod = findMethod(Customer.class, "getName");

            // when
            final FacetFactory.ProcessMethodContext processMethodContext = new FacetFactory.ProcessMethodContext(cls, null, null, propertyMethod, mockMethodRemover, facetedMethod);
            facetFactory.processOptional(processMethodContext);

            // then
            final MandatoryFacet mandatoryFacet = facetedMethod.getFacet(MandatoryFacet.class);
            Assert.assertNull(mandatoryFacet);
        }

        @Test
        public void whenNone() {

            class Customer {
                @Property(
                )
                public String getName() {
                    return null;
                }
                public void setName(final String name) {
                }
            }

            // given
            final Class<?> cls = Customer.class;
            propertyMethod = findMethod(Customer.class, "getName");

            // when
            final FacetFactory.ProcessMethodContext processMethodContext = new FacetFactory.ProcessMethodContext(cls, null, null, propertyMethod, mockMethodRemover, facetedMethod);
            facetFactory.processOptional(processMethodContext);

            // then
            final MandatoryFacet mandatoryFacet = facetedMethod.getFacet(MandatoryFacet.class);
            Assert.assertNull(mandatoryFacet);
        }

    }
    public static class RegEx extends PropertyAnnotationFacetFactoryTest {

        @Test
        public void whenHasAnnotation() {

            class Customer {
                @Property(
                        regexPattern = "[123].*",
                        regexPatternFlags = Pattern.CASE_INSENSITIVE | Pattern.MULTILINE
                )
                public String getName() {
                    return null;
                }
                public void setName(final String name) {
                }
            }

            // given
            final Class<?> cls = Customer.class;
            propertyMethod = findMethod(Customer.class, "getName");

            // when
            final FacetFactory.ProcessMethodContext processMethodContext = new FacetFactory.ProcessMethodContext(cls, null, null, propertyMethod, mockMethodRemover, facetedMethod);
            facetFactory.processRegEx(processMethodContext);

            // then
            final RegExFacet regExFacet = facetedMethod.getFacet(RegExFacet.class);
            Assert.assertNotNull(regExFacet);
            Assert.assertTrue(regExFacet instanceof RegExFacetForPropertyAnnotation);
            assertThat(regExFacet.caseSensitive(), is(true));
            assertThat(regExFacet.validation(), is("[123].*"));
        }

        @Test
        public void whenNone() {

            class Customer {
                @Property(
                )
                public String getName() {
                    return null;
                }
                public void setName(final String name) {
                }
            }

            // given
            final Class<?> cls = Customer.class;
            propertyMethod = findMethod(Customer.class, "getName");

            // when
            final FacetFactory.ProcessMethodContext processMethodContext = new FacetFactory.ProcessMethodContext(cls, null, null, propertyMethod, mockMethodRemover, facetedMethod);
            facetFactory.processRegEx(processMethodContext);

            // then
            final RegExFacet regExFacet = facetedMethod.getFacet(RegExFacet.class);
            Assert.assertNull(regExFacet);
        }

        @Test
        public void whenEmptyString() {

            class Customer {
                @Property(
                        regexPattern = ""
                )
                public String getName() {
                    return null;
                }
                public void setName(final String name) {
                }
            }

            // given
            final Class<?> cls = Customer.class;
            propertyMethod = findMethod(Customer.class, "getName");

            // when
            final FacetFactory.ProcessMethodContext processMethodContext = new FacetFactory.ProcessMethodContext(cls, null, null, propertyMethod, mockMethodRemover, facetedMethod);
            facetFactory.processRegEx(processMethodContext);

            // then
            final RegExFacet regExFacet = facetedMethod.getFacet(RegExFacet.class);
            Assert.assertNull(regExFacet);
        }

         @Test
        public void whenNotAnnotatedOnStringProperty() {

            class Customer {
                @Property(
                        regexPattern = "[abc].*"
                )
                public int getName() {
                    return 0;
                }
                public void setName(final int name) {
                }
            }

            // given
            final Class<?> cls = Customer.class;
            propertyMethod = findMethod(Customer.class, "getName");

            // when
            final FacetFactory.ProcessMethodContext processMethodContext = new FacetFactory.ProcessMethodContext(cls, null, null, propertyMethod, mockMethodRemover, facetedMethod);
            facetFactory.processRegEx(processMethodContext);

            // then
            final RegExFacet regExFacet = facetedMethod.getFacet(RegExFacet.class);
            Assert.assertNull(regExFacet);
        }

    }

}