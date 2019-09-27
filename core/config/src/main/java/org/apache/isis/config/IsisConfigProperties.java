package org.apache.isis.config;

import lombok.Getter;
import lombok.Setter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(ConfigurationConstants.ROOT_PREFIX)
public class IsisConfigProperties {

    @Getter
    private final Reflector reflector = new Reflector();
    public static class Reflector {

        @Getter
        private final ExplicitAnnotations explicitAnnotations = new ExplicitAnnotations();
        public static class ExplicitAnnotations {

            /**
             * Whether or not a public method needs to be annotated with
             * @{@link org.apache.isis.applib.annotation.Action} in order to be picked up as an action in the metamodel.
             */
            @Getter @Setter
            private boolean action = false;
        }
    }

    @Getter
    private final Services services = new Services();
    public static class Services {

        @Getter
        private final Services.Container container = new Services.Container();

        public static class Container {

            /**
             * Normally any queries are automatically preceded by flushing pending executions.
             *
             * <p>
             * This key allows this behaviour to be disabled.
             *
             * <p>
             *     Originally introduced as part of ISIS-1134 (fixing memory leaks in the objectstore)
             *     where it was found that the autoflush behaviour was causing a (now unrepeatable)
             *     data integrity error (see <a href="https://issues.apache.org/jira/browse/ISIS-1134?focusedCommentId=14500638&page=com.atlassian.jira.plugin.system.issuetabpanels:comment-tabpanel#comment-14500638">ISIS-1134 comment</a>, in the isis-module-security.
             *     However, that this could be circumvented by removing the call to flush().
             *     We don't want to break existing apps that might rely on this behaviour, on the
             *     other hand we want to fix the memory leak.  Adding this configuration property
             *     seems the most prudent way forward.
             * </p>
             */
            @Getter @Setter
            private boolean disableAutoFlush = false;

        }
    }

    
}
