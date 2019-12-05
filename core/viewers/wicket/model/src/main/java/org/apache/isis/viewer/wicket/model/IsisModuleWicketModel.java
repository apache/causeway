package org.apache.isis.viewer.wicket.model;

import org.apache.isis.webapp.IsisModuleWebapp;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
        // modules
        IsisModuleWebapp.class,
})
public class IsisModuleWicketModel {
}
