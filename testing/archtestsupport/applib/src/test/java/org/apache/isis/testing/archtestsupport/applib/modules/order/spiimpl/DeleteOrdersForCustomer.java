package org.apache.isis.testing.archtestsupport.applib.modules.order.spiimpl;

import org.apache.isis.testing.archtestsupport.applib.modules.customer.api.Customer;
import org.apache.isis.testing.archtestsupport.applib.modules.customer.spi.CustomerDeletionSpi;

public class DeleteOrdersForCustomer implements CustomerDeletionSpi {
    @Override public void onDelete(final Customer cust) {

    }
}
