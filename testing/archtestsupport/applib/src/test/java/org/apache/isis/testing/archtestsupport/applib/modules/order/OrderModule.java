package org.apache.isis.testing.archtestsupport.applib.modules.order;

import org.apache.isis.testing.archtestsupport.applib.modules.customer.CustomerModule;
import org.springframework.context.annotation.Import;

@Import({ CustomerModule.class })
public class OrderModule {
}
