package org.apache.isis.testing.archtestsupport.applib.modules.customer.dom;

import java.util.List;

import javax.inject.Inject;

import org.apache.isis.testing.archtestsupport.applib.modules.base.api.BaseJpa;
import org.apache.isis.testing.archtestsupport.applib.modules.customer.api.Customer;
import org.apache.isis.testing.archtestsupport.applib.modules.customer.spi.CustomerDeletionSpi;

public class CustomerJpa extends BaseJpa implements Customer {

    @Inject
    List<CustomerDeletionSpi> customerDeletionSpiList;

    //List<OrderJpa> orders;
}
