package org.apache.isis.viewer.wicket.ui.components.collectioncontents.selector.links;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;

import org.jmock.auto.Mock;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2.Mode;
import org.apache.isis.viewer.wicket.ui.ComponentFactory;
import org.apache.isis.viewer.wicket.ui.components.collectioncontents.ajaxtable.CollectionContentsAsAjaxTablePanelFactory;

public class CollectionContentsLinksSelectorPanelTest {

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(Mode.INTERFACES_AND_CLASSES);

    @Mock
    private ComponentFactory one;
    
    @Mock
    private ComponentFactory two;

    private ComponentFactory ajaxTableComponentFactory;
    
    @Before
    public void setUp() throws Exception {
        ajaxTableComponentFactory = new CollectionContentsAsAjaxTablePanelFactory();
    }
    
    @Test
    public void testOrderAjaxTableToEnd() {
        
        List<ComponentFactory> componentFactories = 
                Arrays.<ComponentFactory>asList(
                        one,
                        ajaxTableComponentFactory, 
                        two);
        List<ComponentFactory> orderAjaxTableToEnd = CollectionContentsLinksSelectorPanel.orderAjaxTableToEnd(componentFactories);
        assertThat(orderAjaxTableToEnd.get(0), is(one));
        assertThat(orderAjaxTableToEnd.get(1), is(two));
        assertThat(orderAjaxTableToEnd.get(2), is(ajaxTableComponentFactory));
    }

}
