package org.apache.isis.testing.archtestsupport.applib.modules.customer.spi;

import org.apache.isis.testing.archtestsupport.applib.modules.customer.api.Customer;

public interface CustomerDeletionSpi {

    public void onDelete(Customer cust);

    // should break architecture
    // OrderJpa order();
}
