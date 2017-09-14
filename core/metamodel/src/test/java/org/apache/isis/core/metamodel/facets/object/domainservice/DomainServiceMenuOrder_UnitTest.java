package org.apache.isis.core.metamodel.facets.object.domainservice;

import org.junit.Test;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.DomainServiceLayout;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class DomainServiceMenuOrder_UnitTest {

    @DomainService(menuOrder = "100")
    public static class ServiceWithDomainService100 {
    }

    @DomainServiceLayout(menuOrder = "100")
    public static class ServiceWithDomainServiceLayout100 {
    }

    @DomainService(menuOrder = "100")
    @DomainServiceLayout(menuOrder = "101")
    public static class ServiceWithDomainService100AndDomainServiceLayout101 {
    }

    @DomainService(menuOrder = "101")
    @DomainServiceLayout(menuOrder = "100")
    public static class ServiceWithDomainService101AndDomainServiceLayout100 {
    }

    @DomainService()
    @DomainServiceLayout()
    public static class ServiceWithDomainServiceAndDomainServiceLayout {
    }

    @DomainService()
    public static class ServiceWithDomainService {
    }

    @DomainServiceLayout()
    public static class ServiceWithDomainServiceLayout {
    }

    @Test
    public void orderOf() throws Exception {
        assertOrder(ServiceWithDomainService.class, Integer.MAX_VALUE - 100);
        assertOrder(ServiceWithDomainServiceLayout.class, Integer.MAX_VALUE - 100);
        assertOrder(ServiceWithDomainServiceAndDomainServiceLayout.class, Integer.MAX_VALUE - 100);

        assertOrder(ServiceWithDomainService100.class, 100);
        assertOrder(ServiceWithDomainServiceLayout100.class, 100);

        assertOrder(ServiceWithDomainService100AndDomainServiceLayout101.class, 100);
        assertOrder(ServiceWithDomainService101AndDomainServiceLayout100.class, 100);
    }

    private static void assertOrder(final Class<?> cls, final int expected) {
        String menuOrder = DomainServiceMenuOrder.orderOf(cls);
        assertThat(menuOrder, is(equalTo("" + expected)));
    }



}