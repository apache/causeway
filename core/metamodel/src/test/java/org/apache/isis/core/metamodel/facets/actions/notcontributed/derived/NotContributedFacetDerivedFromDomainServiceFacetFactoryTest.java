package org.apache.isis.core.metamodel.facets.actions.notcontributed.derived;

import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Test;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facets.AbstractFacetFactoryJUnit4TestCase;
import org.apache.isis.core.metamodel.facets.FacetFactory;
import org.apache.isis.core.metamodel.facets.FacetedMethod;
import org.apache.isis.core.metamodel.facets.actions.notcontributed.NotContributedFacet;
import org.apache.isis.core.metamodel.facets.object.domainservice.DomainServiceFacet;
import org.apache.isis.core.metamodel.facets.object.domainservice.DomainServiceFacetAbstract;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

public class NotContributedFacetDerivedFromDomainServiceFacetFactoryTest extends AbstractFacetFactoryJUnit4TestCase {

    private NotContributedFacetDerivedFromDomainServiceFacetFactory facetFactory;

    @Before
    public void setUp() throws Exception {
        facetFactory = new NotContributedFacetDerivedFromDomainServiceFacetFactory();
        facetFactory.setSpecificationLookup(mockSpecificationLoaderSpi);
    }

    @Test
    public void whenMenu() throws Exception {

        // given
        @DomainService(nature = NatureOfService.VIEW_MENU_ONLY)
        class CustomerService {

            public String name() {
                return "Joe";
            }
        }

        context.checking(new Expectations() {{
            allowing(mockSpecificationLoaderSpi).loadSpecification(CustomerService.class);
            will(returnValue(mockObjSpec));

            allowing(mockObjSpec).getFacet(DomainServiceFacet.class);
            will(returnValue(new DomainServiceFacetAbstract(mockObjSpec, null, NatureOfService.VIEW_MENU_ONLY) {
            }));
        }});

        expectNoMethodsRemoved();

        facetedMethod = FacetedMethod.createForAction(CustomerService.class, "name");

        // when
        facetFactory.process(new FacetFactory.ProcessMethodContext(CustomerService.class, null, null, facetedMethod.getMethod(), mockMethodRemover, facetedMethod));

        // then
        final Facet facet = facetedMethod.getFacet(NotContributedFacet.class);
        assertThat(facet, is(not(nullValue())));
        assertThat(facet instanceof NotContributedFacetDerivedFromDomainServiceFacet, is(true));
        final NotContributedFacetDerivedFromDomainServiceFacet facetDerivedFromDomainServiceFacet = (NotContributedFacetDerivedFromDomainServiceFacet) facet;
        assertThat(facetDerivedFromDomainServiceFacet.getNatureOfService(), equalTo(NatureOfService.VIEW_MENU_ONLY));
    }

    @Test
    public void whenDomain() throws Exception {

        // given
        @DomainService(nature = NatureOfService.DOMAIN)
        class CustomerService {

            public String name() {
                return "Joe";
            }
        }

        context.checking(new Expectations() {{
            allowing(mockSpecificationLoaderSpi).loadSpecification(CustomerService.class);
            will(returnValue(mockObjSpec));

            allowing(mockObjSpec).getFacet(DomainServiceFacet.class);
            will(returnValue(new DomainServiceFacetAbstract(mockObjSpec, null, NatureOfService.DOMAIN) {
            }));
        }});

        expectNoMethodsRemoved();

        facetedMethod = FacetedMethod.createForAction(CustomerService.class, "name");

        // when
        facetFactory.process(new FacetFactory.ProcessMethodContext(CustomerService.class, null, null, facetedMethod.getMethod(), mockMethodRemover, facetedMethod));

        // then
        final Facet facet = facetedMethod.getFacet(NotContributedFacet.class);
        assertThat(facet, is(not(nullValue())));
        assertThat(facet instanceof NotContributedFacetDerivedFromDomainServiceFacet, is(true));
        final NotContributedFacetDerivedFromDomainServiceFacet facetDerivedFromDomainServiceFacet = (NotContributedFacetDerivedFromDomainServiceFacet) facet;
        assertThat(facetDerivedFromDomainServiceFacet.getNatureOfService(), equalTo(NatureOfService.DOMAIN));
    }

    @Test
    public void whenView() throws Exception {

        // given
        @DomainService(nature = NatureOfService.VIEW)
        class CustomerService {

            public String name() {
                return "Joe";
            }
        }

        context.checking(new Expectations() {{
            allowing(mockSpecificationLoaderSpi).loadSpecification(CustomerService.class);
            will(returnValue(mockObjSpec));

            allowing(mockObjSpec).getFacet(DomainServiceFacet.class);
            will(returnValue(new DomainServiceFacetAbstract(mockObjSpec, null, NatureOfService.VIEW) {
            }));
        }});

        expectNoMethodsRemoved();

        facetedMethod = FacetedMethod.createForAction(CustomerService.class, "name");

        // when
        facetFactory.process(new FacetFactory.ProcessMethodContext(CustomerService.class, null, null, facetedMethod.getMethod(), mockMethodRemover, facetedMethod));

        // then
        final Facet facet = facetedMethod.getFacet(NotContributedFacet.class);
        assertThat(facet, is(nullValue()));
    }

    @Test
    public void whenContributions() throws Exception {

        // given
        @DomainService(nature = NatureOfService.VIEW_CONTRIBUTIONS_ONLY)
        class CustomerService {

            public String name() {
                return "Joe";
            }
        }

        context.checking(new Expectations() {{
            allowing(mockSpecificationLoaderSpi).loadSpecification(CustomerService.class);
            will(returnValue(mockObjSpec));

            allowing(mockObjSpec).getFacet(DomainServiceFacet.class);
            will(returnValue(new DomainServiceFacetAbstract(mockObjSpec, null, NatureOfService.VIEW_CONTRIBUTIONS_ONLY) {
            }));
        }});

        expectNoMethodsRemoved();

        facetedMethod = FacetedMethod.createForAction(CustomerService.class, "name");

        // when
        facetFactory.process(new FacetFactory.ProcessMethodContext(CustomerService.class, null, null, facetedMethod.getMethod(), mockMethodRemover, facetedMethod));

        // then
        final Facet facet = facetedMethod.getFacet(NotContributedFacet.class);
        assertThat(facet, is(nullValue()));
    }

    @Test
    public void whenNone() throws Exception {

        // given
        class CustomerService {

            public String name() {
                return "Joe";
            }
        }

        context.checking(new Expectations() {{
            allowing(mockSpecificationLoaderSpi).loadSpecification(CustomerService.class);
            will(returnValue(mockObjSpec));

            allowing(mockObjSpec).getFacet(DomainServiceFacet.class);
            will(returnValue(null));
        }});

        expectNoMethodsRemoved();

        facetedMethod = FacetedMethod.createForAction(CustomerService.class, "name");

        // when
        facetFactory.process(new FacetFactory.ProcessMethodContext(CustomerService.class, null, null, facetedMethod.getMethod(), mockMethodRemover, facetedMethod));

        // then
        final Facet facet = facetedMethod.getFacet(NotContributedFacet.class);
        assertThat(facet, is(nullValue()));
    }

}