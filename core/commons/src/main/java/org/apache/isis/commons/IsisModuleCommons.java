package org.apache.isis.commons;

import org.apache.isis.commons.internal.environment.IsisSystemEnvironment;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
    // @Service's
    IsisSystemEnvironment.class
})
public class IsisModuleCommons {
}
