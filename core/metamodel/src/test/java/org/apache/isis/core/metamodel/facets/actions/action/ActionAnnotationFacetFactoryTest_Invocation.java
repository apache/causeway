package org.apache.isis.core.metamodel.facets.actions.action;

import static org.apache.isis.core.commons.matchers.IsisMatchers.classEqualTo;
import static org.junit.Assert.assertThat;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.events.domain.ActionDomainEvent;
import org.apache.isis.config.internal._Config;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facets.FacetFactory.ProcessMethodContext;
import org.apache.isis.core.metamodel.facets.actions.action.invocation.ActionDomainEventFacet;
import org.apache.isis.core.metamodel.facets.actions.action.invocation.ActionDomainEventFacetAbstract;
import org.apache.isis.core.metamodel.facets.actions.action.invocation.ActionDomainEventFacetDefault;
import org.apache.isis.core.metamodel.facets.actions.action.invocation.ActionDomainEventFacetForActionAnnotation;
import org.apache.isis.core.metamodel.facets.actions.action.invocation.ActionInvocationFacet;
import org.apache.isis.core.metamodel.facets.actions.action.invocation.ActionInvocationFacetForDomainEventFromActionAnnotation;
import org.apache.isis.core.metamodel.facets.actions.action.invocation.ActionInvocationFacetForDomainEventFromDefault;
import org.junit.Assert;
import org.junit.Test;

public class ActionAnnotationFacetFactoryTest_Invocation extends ActionAnnotationFacetFactoryTest {

    @Test
    public void withPostsActionInvokedEvent() {

        class Customer {

			class SomeActionInvokedDomainEvent extends ActionDomainEvent<Customer> {
				private static final long serialVersionUID = 1L; }

            @Action(domainEvent = SomeActionInvokedDomainEvent.class)
            public void someAction() {
            }
        }

        // given
        final Class<?> cls = Customer.class;
        actionMethod = findMethod(cls, "someAction");

        // expect
        allowingLoadSpecificationRequestsFor(cls, actionMethod.getReturnType());
        expectRemoveMethod(actionMethod);

        _Config.put("isis.reflector.facet.actionAnnotation.domainEvent.postForDefault", true);

        // when
        final ProcessMethodContext processMethodContext = new ProcessMethodContext(
        		cls, null, actionMethod, mockMethodRemover, facetedMethod);
        facetFactory.processInvocation(processMethodContext);

        // then
        final ActionDomainEventFacet domainEventFacet = facetedMethod.getFacet(ActionDomainEventFacet.class);
        Assert.assertNotNull(domainEventFacet);
        Assert.assertTrue(domainEventFacet instanceof ActionDomainEventFacetAbstract);
        final ActionDomainEventFacetAbstract domainEventFacetImpl = (ActionDomainEventFacetAbstract) domainEventFacet;
        assertThat(domainEventFacetImpl.getEventType(), classEqualTo(Customer.SomeActionInvokedDomainEvent.class));

        final Facet invocationFacet = facetedMethod.getFacet(ActionInvocationFacet.class);
        Assert.assertNotNull(invocationFacet);
        Assert.assertTrue(invocationFacet instanceof ActionInvocationFacetForDomainEventFromActionAnnotation);
        final ActionInvocationFacetForDomainEventFromActionAnnotation invocationFacetImpl = (ActionInvocationFacetForDomainEventFromActionAnnotation) invocationFacet;
        assertThat(invocationFacetImpl.getEventType(), classEqualTo(Customer.SomeActionInvokedDomainEvent.class));
    }

    @Test
    public void withActionInteractionEvent() {

        class Customer {

            class SomeActionInvokedDomainEvent extends ActionDomainEvent<Customer> {
				private static final long serialVersionUID = 1L; }

            @Action(domainEvent = SomeActionInvokedDomainEvent.class)
            public void someAction() {
            }
        }

        // given
        final Class<?> cls = Customer.class;
        actionMethod = findMethod(cls, "someAction");

        // expect
        allowingLoadSpecificationRequestsFor(cls, actionMethod.getReturnType());
        expectRemoveMethod(actionMethod);

        // when
        final ProcessMethodContext processMethodContext = new ProcessMethodContext(
        		cls, null, actionMethod, mockMethodRemover, facetedMethod);
        facetFactory.processInvocation(processMethodContext);

        // then
        final Facet domainEventFacet = facetedMethod.getFacet(ActionDomainEventFacet.class);
        Assert.assertNotNull(domainEventFacet);
        Assert.assertTrue(domainEventFacet instanceof ActionDomainEventFacetForActionAnnotation);
        final ActionDomainEventFacetForActionAnnotation domainEventFacetImpl = 
        		(ActionDomainEventFacetForActionAnnotation) domainEventFacet;
        assertThat(domainEventFacetImpl.getEventType(), classEqualTo(Customer.SomeActionInvokedDomainEvent.class));

        final Facet invocationFacet = facetedMethod.getFacet(ActionInvocationFacet.class);
        Assert.assertNotNull(invocationFacet);
        
        Assert.assertTrue(invocationFacet instanceof ActionInvocationFacetForDomainEventFromActionAnnotation);
        final ActionInvocationFacetForDomainEventFromActionAnnotation invocationFacetImpl = 
        		(ActionInvocationFacetForDomainEventFromActionAnnotation) invocationFacet;
        assertThat(invocationFacetImpl.getEventType(), classEqualTo(Customer.SomeActionInvokedDomainEvent.class));
    }

    @Test
    public void withActionDomainEvent() {

        class Customer {

            class SomeActionInvokedDomainEvent extends ActionDomainEvent<Customer> {
            	private static final long serialVersionUID = 1L; }

            @Action(domainEvent= SomeActionInvokedDomainEvent.class)
            public void someAction() {
            }
        }

        // given
        final Class<?> cls = Customer.class;
        actionMethod = findMethod(cls, "someAction");

        // expect
        allowingLoadSpecificationRequestsFor(cls, actionMethod.getReturnType());
        expectRemoveMethod(actionMethod);

        // when
        final ProcessMethodContext processMethodContext = new ProcessMethodContext(
        		cls, null, actionMethod, mockMethodRemover, facetedMethod);
        facetFactory.processInvocation(processMethodContext);

        // then
        final Facet domainEventFacet = facetedMethod.getFacet(ActionDomainEventFacet.class);
        Assert.assertNotNull(domainEventFacet);
        Assert.assertTrue(domainEventFacet instanceof ActionDomainEventFacetForActionAnnotation);
        final ActionDomainEventFacetForActionAnnotation domainEventFacetImpl = (ActionDomainEventFacetForActionAnnotation) domainEventFacet;
        assertThat(domainEventFacetImpl.getEventType(), classEqualTo(Customer.SomeActionInvokedDomainEvent.class));

        final Facet invocationFacet = facetedMethod.getFacet(ActionInvocationFacet.class);
        Assert.assertNotNull(invocationFacet);
        Assert.assertTrue(invocationFacet instanceof ActionInvocationFacetForDomainEventFromActionAnnotation);
        final ActionInvocationFacetForDomainEventFromActionAnnotation invocationFacetImpl = (ActionInvocationFacetForDomainEventFromActionAnnotation) invocationFacet;
        assertThat(invocationFacetImpl.getEventType(), classEqualTo(Customer.SomeActionInvokedDomainEvent.class));
    }

    @Test
    public void withDefaultEvent() {

        class Customer {
            @SuppressWarnings("unused")
            public void someAction() {
            }
        }

        // given
        final Class<?> cls = Customer.class;
        actionMethod = findMethod(cls, "someAction");

        // expect
        allowingLoadSpecificationRequestsFor(cls, actionMethod.getReturnType());
        expectRemoveMethod(actionMethod);

        _Config.put("isis.reflector.facet.actionAnnotation.domainEvent.postForDefault", true);

        // when
        final ProcessMethodContext processMethodContext = new ProcessMethodContext(
        		cls, null, actionMethod, mockMethodRemover, facetedMethod);
        facetFactory.processInvocation(processMethodContext);

        // then
        final Facet domainEventFacet = facetedMethod.getFacet(ActionDomainEventFacet.class);
        Assert.assertNotNull(domainEventFacet);
        Assert.assertTrue(domainEventFacet instanceof ActionDomainEventFacetDefault);
        final ActionDomainEventFacetDefault domainEventFacetImpl = (ActionDomainEventFacetDefault) domainEventFacet;
        assertThat(domainEventFacetImpl.getEventType(), classEqualTo(ActionDomainEvent.Default.class));

        final Facet invocationFacet = facetedMethod.getFacet(ActionInvocationFacet.class);
        Assert.assertNotNull(invocationFacet);
        Assert.assertTrue(invocationFacet instanceof ActionInvocationFacetForDomainEventFromDefault);
        final ActionInvocationFacetForDomainEventFromDefault invocationFacetImpl = (ActionInvocationFacetForDomainEventFromDefault) invocationFacet;
        assertThat(invocationFacetImpl.getEventType(), classEqualTo(ActionDomainEvent.Default.class));
    }
}