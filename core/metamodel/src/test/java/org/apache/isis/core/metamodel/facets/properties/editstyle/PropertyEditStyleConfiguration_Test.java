package org.apache.isis.core.metamodel.facets.properties.editstyle;

import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.applib.annotation.PropertyEditStyle;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.metamodel.facets.properties.property.editStyle.PropertyEditStyleConfiguration;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2;

import static org.hamcrest.CoreMatchers.is;

public class PropertyEditStyleConfiguration_Test {

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(JUnitRuleMockery2.Mode.INTERFACES_AND_CLASSES);

    @Mock
    IsisConfiguration mockIsisConfiguration;

    @Test
    public void when_none() throws Exception {
        context.checking(new Expectations() {{
            oneOf(mockIsisConfiguration).getString("isis.properties.editStyle");
            will(returnValue(null));
        }});
        PropertyEditStyle editStyle = PropertyEditStyleConfiguration.parse(mockIsisConfiguration);
        Assert.assertThat(editStyle, is(PropertyEditStyle.DIALOG));
    }

    @Test
    public void when_inline() throws Exception {
        context.checking(new Expectations() {{
            oneOf(mockIsisConfiguration).getString("isis.properties.editStyle");
            will(returnValue("inline"));
        }});
        PropertyEditStyle editStyle = PropertyEditStyleConfiguration.parse(mockIsisConfiguration);
        Assert.assertThat(editStyle, is(PropertyEditStyle.INLINE));
    }

    @Test
    public void when_inline_mixed_case_and_superfluous_characters() throws Exception {
        context.checking(new Expectations() {{
            oneOf(mockIsisConfiguration).getString("isis.properties.editStyle");
            will(returnValue(" inLIne "));
        }});
        PropertyEditStyle editStyle = PropertyEditStyleConfiguration.parse(mockIsisConfiguration);
        Assert.assertThat(editStyle, is(PropertyEditStyle.INLINE));
    }

    @Test
    public void when_dialog() throws Exception {
        context.checking(new Expectations() {{
            oneOf(mockIsisConfiguration).getString("isis.properties.editStyle");
            will(returnValue("dialog"));
        }});
        PropertyEditStyle editStyle = PropertyEditStyleConfiguration.parse(mockIsisConfiguration);
        Assert.assertThat(editStyle, is(PropertyEditStyle.DIALOG));
    }

    @Test
    public void when_invalid() throws Exception {
        context.checking(new Expectations() {{
            oneOf(mockIsisConfiguration).getString("isis.properties.editStyle");
            will(returnValue("garbage"));
        }});
        PropertyEditStyle editStyle = PropertyEditStyleConfiguration.parse(mockIsisConfiguration);
        Assert.assertThat(editStyle, is(PropertyEditStyle.DIALOG));
    }

}