package org.apache.isis.viewer.graphql.viewer;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import org.apache.isis.viewer.graphql.model.IsisModuleIncViewerGraphqlModel;
import org.apache.isis.viewer.graphql.viewer.spring.GraphQlAutoConfiguration;
import org.apache.isis.viewer.graphql.viewer.spring.GraphQlCorsProperties;
import org.apache.isis.viewer.graphql.viewer.spring.GraphQlProperties;
import org.apache.isis.viewer.graphql.viewer.spring.GraphQlWebMvcAutoConfiguration;

@Configuration
@Import({
        // @Service's

        // modules
        IsisModuleIncViewerGraphqlModel.class,

        // autoconfigurations
        GraphQlAutoConfiguration.class,
        GraphQlWebMvcAutoConfiguration.class
})
@EnableConfigurationProperties({
        GraphQlProperties.class, GraphQlCorsProperties.class
})
@ComponentScan
public class IsisModuleIncViewerGraphqlViewer {
}

