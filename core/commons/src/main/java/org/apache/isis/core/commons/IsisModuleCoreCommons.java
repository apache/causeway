package org.apache.isis.core.commons;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import org.apache.isis.core.commons.internal.environment.IsisSystemEnvironment;

@Configuration
@Import({
    // @Service's
    IsisSystemEnvironment.class
})
public class IsisModuleCoreCommons {
}
