package org.apache.isis.core.metamodel.facets.actions.prototype;

import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.junit.Rule;
import org.junit.Test;
import org.apache.isis.core.metamodel.deployment.DeploymentCategory;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.interactions.VisibilityContext;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2;

import static org.junit.Assert.assertEquals;

public class PrototypeFacetAbstractTest {

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(JUnitRuleMockery2.Mode.INTERFACES_AND_CLASSES);

    @Mock
    private VisibilityContext mockVisibilityContext;
    @Mock
    private FacetHolder mockFacetHolder;

    @Test
    public void allCombinations() throws Exception {
        givenWhenThen(DeploymentCategory.EXPLORING, null);
        givenWhenThen(DeploymentCategory.PROTOTYPING, null);
        givenWhenThen(DeploymentCategory.PRODUCTION, "Prototyping action not visible in production mode");
    }

    protected void givenWhenThen(final DeploymentCategory deploymentCategory, final String expected) {
        // given
        final PrototypeFacetAbstract facet = new PrototypeFacetAbstract(mockFacetHolder){};

        // expect
        context.checking(new Expectations() {{
            oneOf(mockVisibilityContext).getDeploymentCategory();
            will(returnValue(deploymentCategory));
        }});

        // when
        final String reason = facet.hides(mockVisibilityContext);

        // then
        assertEquals(expected, reason);
    }
}