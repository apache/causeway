package org.apache.isis.viewer.wicket.model;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import org.apache.isis.core.webapp.IsisModuleCoreWebapp;

@Configuration
@Import({
        // modules
        IsisModuleCoreWebapp.class,
})
public class IsisModuleWicketModel {
}
