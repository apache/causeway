package org.apache.isis.viewer.wicket.model;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import org.apache.isis.core.webapp.IsisModuleWebapp;

@Configuration
@Import({
        // modules
        IsisModuleWebapp.class,
})
public class IsisModuleWicketModel {
}
