package org.apache.isis.core.metamodel.facets.actions.action;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.commons.internal.base._Blackhole;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facets.FacetFactory.ProcessMethodContext;
import org.apache.isis.core.metamodel.facets.actions.action.publishing.PublishedActionFacetForActionAnnotation;
import org.apache.isis.core.metamodel.facets.actions.action.publishing.PublishedActionFacetFromConfiguration;
import org.apache.isis.core.metamodel.facets.actions.publish.PublishedActionFacet;
import org.apache.isis.core.metamodel.facets.actions.semantics.ActionSemanticsFacetAbstract;
import org.junit.Test;

public class ActionAnnotationFacetFactoryTest_Publishing extends ActionAnnotationFacetFactoryTest {

    @Test
    public void givenHasTransactionId_thenIgnored() {

        final Method actionMethod = findMethod(SomeTransactionalId.class, "someAction");

        facetFactory.processPublishing(new ProcessMethodContext(SomeTransactionalId.class, null, actionMethod, mockMethodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(PublishedActionFacet.class);
        assertNull(facet);

        expectNoMethodsRemoved();
    }

    @Test
    public void given_noAnnotation_and_configurationSetToIgnoreQueryOnly_andSafeSemantics_thenNone() {

        // given
        allowingPublishingConfigurationToReturn("ignoreQueryOnly");
        final Method actionMethod = findMethod(ActionAnnotationFacetFactoryTest.Customer.class, "someAction");

        facetedMethod.addFacet(new ActionSemanticsFacetAbstract(SemanticsOf.SAFE, facetedMethod) {});

        // when
        facetFactory.processPublishing(new ProcessMethodContext(ActionAnnotationFacetFactoryTest.Customer.class, null,
                actionMethod, mockMethodRemover, facetedMethod));

        // then
        final Facet facet = facetedMethod.getFacet(PublishedActionFacet.class);
        assertNull(facet);
    }

    @Test
    public void given_noAnnotation_and_configurationSetToIgnoreQueryOnly_andNonSafeSemantics_thenAdded() {

        // given
        allowingPublishingConfigurationToReturn("ignoreQueryOnly");
        final Method actionMethod = findMethod(ActionAnnotationFacetFactoryTest.Customer.class, "someAction");

        facetedMethod.addFacet(new ActionSemanticsFacetAbstract(SemanticsOf.IDEMPOTENT, facetedMethod) {});

        // when
        facetFactory.processPublishing(new ProcessMethodContext(ActionAnnotationFacetFactoryTest.Customer.class, null,
                actionMethod, mockMethodRemover, facetedMethod));

        // then
        final Facet facet = facetedMethod.getFacet(PublishedActionFacet.class);
        assertNotNull(facet);
        final PublishedActionFacetFromConfiguration facetImpl = (PublishedActionFacetFromConfiguration) facet;
        _Blackhole.consume(facetImpl);
    }

    @Test(expected=IllegalStateException.class)
    public void given_noAnnotation_and_configurationSetToIgnoreQueryOnly_andNoSemantics_thenException() {

        // given
        allowingPublishingConfigurationToReturn("ignoreQueryOnly");
        final Method actionMethod = findMethod(ActionAnnotationFacetFactoryTest.Customer.class, "someAction");

        // when
        facetFactory.processPublishing(new ProcessMethodContext(ActionAnnotationFacetFactoryTest.Customer.class, null,
                actionMethod, mockMethodRemover, facetedMethod));

    }

    @Test
    public void given_noAnnotation_and_configurationSetToNone_thenNone() {

        // given
        allowingPublishingConfigurationToReturn("none");
        final Method actionMethod = findMethod(ActionAnnotationFacetFactoryTest.Customer.class, "someAction");

        // when
        facetFactory.processPublishing(new ProcessMethodContext(ActionAnnotationFacetFactoryTest.Customer.class, null,
                actionMethod, mockMethodRemover, facetedMethod));

        // then
        final Facet facet = facetedMethod.getFacet(PublishedActionFacet.class);
        assertNull(facet);

        expectNoMethodsRemoved();

    }

    @Test
    public void given_noAnnotation_and_configurationSetToAll_thenFacetAdded() {

        // given
        final Method actionMethod = findMethod(ActionAnnotationFacetFactoryTest.Customer.class, "someAction");

        allowingPublishingConfigurationToReturn("all");

        // when
        facetFactory.processPublishing(new ProcessMethodContext(ActionAnnotationFacetFactoryTest.Customer.class, null,
                actionMethod, mockMethodRemover, facetedMethod));

        // then
        final Facet facet = facetedMethod.getFacet(PublishedActionFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof PublishedActionFacetFromConfiguration);
    }

    @Test
    public void given_asConfigured_and_configurationSetToIgnoreQueryOnly_andSafeSemantics_thenNone() {

        class Customer {
            @Action(publishing = org.apache.isis.applib.annotation.Publishing.AS_CONFIGURED)
            public void someAction() {
            }
        }

        allowingPublishingConfigurationToReturn("ignoreQueryOnly");
        final Method actionMethod = findMethod(Customer.class, "someAction");

        facetedMethod.addFacet(new ActionSemanticsFacetAbstract(SemanticsOf.SAFE, facetedMethod) {});

        facetFactory.processPublishing(new ProcessMethodContext(Customer.class, null, actionMethod, mockMethodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(PublishedActionFacet.class);
        assertNull(facet);

        expectNoMethodsRemoved();
    }

    @Test
    public void given_asConfigured_and_configurationSetToIgnoreQueryOnly_andNonSafeSemantics_thenAdded() {

        // given
        class Customer {
            @Action(
                    publishing = org.apache.isis.applib.annotation.Publishing.AS_CONFIGURED
            )
            public void someAction() {
            }
        }

        allowingPublishingConfigurationToReturn("ignoreQueryOnly");
        final Method actionMethod = findMethod(Customer.class, "someAction");

        facetedMethod.addFacet(new ActionSemanticsFacetAbstract(SemanticsOf.IDEMPOTENT, facetedMethod) {});

        // when
        facetFactory.processPublishing(new ProcessMethodContext(Customer.class, null, actionMethod, mockMethodRemover, facetedMethod));

        // then
        final Facet facet = facetedMethod.getFacet(PublishedActionFacet.class);
        assertNotNull(facet);
        final PublishedActionFacetForActionAnnotation facetImpl = (PublishedActionFacetForActionAnnotation) facet;
        _Blackhole.consume(facetImpl);
        
        expectNoMethodsRemoved();
    }

    @Test(expected=IllegalStateException.class)
    public void given_asConfigured_and_configurationSetToIgnoreQueryOnly_andNoSemantics_thenException() {

        class Customer {
            @Action(publishing = org.apache.isis.applib.annotation.Publishing.AS_CONFIGURED)
            public void someAction() {
            }
        }

        allowingPublishingConfigurationToReturn("ignoreQueryOnly");
        final Method actionMethod = findMethod(Customer.class, "someAction");

        facetFactory.processPublishing(new ProcessMethodContext(Customer.class, null, actionMethod, mockMethodRemover, facetedMethod));
    }

    @Test
    public void given_asConfigured_and_configurationSetToNone_thenNone() {

        class Customer {
            @Action(publishing = org.apache.isis.applib.annotation.Publishing.AS_CONFIGURED)
            public void someAction() {
            }
        }

        allowingPublishingConfigurationToReturn("none");
        final Method actionMethod = findMethod(Customer.class, "someAction");

        facetFactory.processPublishing(new ProcessMethodContext(Customer.class, null, actionMethod, mockMethodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(PublishedActionFacet.class);
        assertNull(facet);

        expectNoMethodsRemoved();

    }

    @Test
    public void given_asConfigured_and_configurationSetToAll_thenFacetAdded() {

        // given
        class Customer {
            @Action(
                    publishing = org.apache.isis.applib.annotation.Publishing.AS_CONFIGURED
            )
            public void someAction() {
            }
        }
        final Method actionMethod = findMethod(Customer.class, "someAction");

        allowingPublishingConfigurationToReturn("all");

        // when
        facetFactory.processPublishing(new ProcessMethodContext(Customer.class, null, actionMethod, mockMethodRemover, facetedMethod));

        // then
        final Facet facet = facetedMethod.getFacet(PublishedActionFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof PublishedActionFacetForActionAnnotation);

        expectNoMethodsRemoved();
    }

    @Test
    public void given_enabled_irrespectiveOfConfiguration_thenFacetAdded() {

        // given
        class Customer {
            @Action(
                    publishing = org.apache.isis.applib.annotation.Publishing.ENABLED
            )
            public void someAction() {
            }
        }
        final Method actionMethod = findMethod(Customer.class, "someAction");

        // even though configuration is disabled
        allowingPublishingConfigurationToReturn("none");

        // when
        facetFactory.processPublishing(new ProcessMethodContext(Customer.class, null, actionMethod, mockMethodRemover, facetedMethod));

        // then
        final Facet facet = facetedMethod.getFacet(PublishedActionFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof PublishedActionFacetForActionAnnotation);
    }

    @Test
    public void given_disabled_irrespectiveOfConfiguration_thenNone() {

        // given
        class Customer {
            @Action(
                    publishing = org.apache.isis.applib.annotation.Publishing.DISABLED
            )
            public void someAction() {
            }
        }
        final Method actionMethod = findMethod(Customer.class, "someAction");

        // even though configuration is disabled
        allowingPublishingConfigurationToReturn("none");

        // when
        facetFactory.processPublishing(new ProcessMethodContext(Customer.class, null, actionMethod, mockMethodRemover, facetedMethod));

        // then
        final Facet facet = facetedMethod.getFacet(PublishedActionFacet.class);
        assertNull(facet);
    }

}