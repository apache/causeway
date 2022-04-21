package org.apache.isis.viewer.graphql.model;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import org.apache.isis.viewer.graphql.applib.IsisModuleIncViewerGraphqlApplib;

@Configuration
@Import({
        // modules
        IsisModuleIncViewerGraphqlApplib.class
})
public class IsisModuleIncViewerGraphqlModel {
}

