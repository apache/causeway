package org.apache.isis.core.metamodel.facets.value.datetimejodalocal;

import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.core.commons.config.IsisConfigurationDefault;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.services.ServicesInjector;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class JodaLocalDateTimeValueSemanticsProviderTest {

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(JUnitRuleMockery2.Mode.INTERFACES_AND_CLASSES);

    @Mock
    FacetHolder mockFacetHolder;

    @Mock
    ServicesInjector mockServicesInjector;

    @Mock
    IsisConfigurationDefault mockConfiguration;

    JodaLocalDateTimeValueSemanticsProvider provider;

    @Before
    public void setUp() throws Exception {
        context.checking(new Expectations() {{

            ignoring(mockFacetHolder);

            allowing(mockServicesInjector).getConfigurationServiceInternal();
            will(returnValue(mockConfiguration));

            allowing(mockConfiguration).getString("isis.value.format.datetime","medium");
            will(returnValue("iso_encoding"));
        }});

        provider = new JodaLocalDateTimeValueSemanticsProvider(mockFacetHolder, mockServicesInjector);

    }

    @Test
    public void roundtrip() throws Exception {

        final LocalDateTime t0 = LocalDateTime.now();

        final String encoded = provider.doEncode(t0);
        final LocalDateTime t1 = provider.doRestore(encoded);

        assertThat(t0, is(equalTo(t1)));
    }

}