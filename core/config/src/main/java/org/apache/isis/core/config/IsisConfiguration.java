/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.isis.core.config;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import javax.activation.DataSource;
import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.validation.annotation.Validated;

import org.apache.isis.applib.IsisModuleApplib;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.Introspection.IntrospectionPolicy;
import org.apache.isis.applib.annotation.LabelPosition;
import org.apache.isis.applib.annotation.PromptStyle;
import org.apache.isis.applib.services.i18n.Mode;
import org.apache.isis.applib.services.iactn.Execution;
import org.apache.isis.applib.services.publishing.spi.EntityChangesSubscriber;
import org.apache.isis.applib.services.publishing.spi.EntityPropertyChangeSubscriber;
import org.apache.isis.applib.services.userreg.EmailNotificationService;
import org.apache.isis.applib.services.userreg.UserRegistrationService;
import org.apache.isis.applib.services.userui.UserMenu;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.context._Context;
import org.apache.isis.core.config.IsisConfiguration.Viewer;
import org.apache.isis.core.config.metamodel.facets.DefaultViewConfiguration;
import org.apache.isis.core.config.metamodel.facets.EditingObjectsConfiguration;
import org.apache.isis.core.config.metamodel.facets.PublishingPolicies.ActionPublishingPolicy;
import org.apache.isis.core.config.metamodel.facets.PublishingPolicies.EntityChangePublishingPolicy;
import org.apache.isis.core.config.metamodel.facets.PublishingPolicies.PropertyPublishingPolicy;
import org.apache.isis.core.config.metamodel.services.ApplicationFeaturesInitConfiguration;
import org.apache.isis.core.config.metamodel.specloader.IntrospectionMode;
import org.apache.isis.core.config.viewer.web.DialogMode;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.Value;
import lombok.val;


/**
 * Configuration 'beans' with meta-data (IDE-support).
 *
 * @see <a href="https://docs.spring.io/spring-boot/docs/current/reference/html/configuration-metadata.html">spring.io</a>
 *
 * @since 2.0
 */
@ConfigurationProperties(IsisConfiguration.ROOT_PREFIX)
@Data
@Validated
public class IsisConfiguration {

    public static final String ROOT_PREFIX = "isis";

    private final ConfigurableEnvironment environment;
    public IsisConfiguration(final ConfigurableEnvironment environment) {
        this.environment = environment;
    }

    private final Security security = new Security();
    @Data
    public static class Security {
        private final Shiro shiro = new Shiro();
        @Data
        public static class Shiro {
            /**
             * If the Shiro subject is found to be still authenticated, then will be logged out anyway and then
             * re-authenticated.
             *
             * <p>
             * Applies only to the Restful Objects viewer.
             * </p>
             */
            private boolean autoLogoutIfAlreadyAuthenticated = false;

        }

        private final Spring spring = new Spring();
        @Data
        public static class Spring {
            /**
             * The framework on initialization by default disables any {@code CsrfFilter}(s) it finds
             * with <i>Spring Security</i> registered filters.
             * <p>
             * Setting this option to {@literal true} allows {@code CsrfFilter}(s) to be
             * configured. Yet EXPERIMENTAL.
             *
             * @see org.springframework.security.web.csrf.CsrfFilter
             * @see "https://www.baeldung.com/spring-security-registered-filters"
             */
            private boolean allowCsrfFilters = false;

        }

        private final Keycloak keycloak = new Keycloak();
        @Data
        public static class Keycloak {
            /**
             * The name of the realm for the Apache Isis application, as configured in
             * Keycloak.
             */
            private String realm;

            /**
             * The base URL for the keycloak server.
             *
             * <p>
             *     For example, if running a keycloak using Docker container, such as:
             *     <pre>
             *         docker run -p 9090:8080 \
             *             -e KEYCLOAK_USER=admin \
             *             -e KEYCLOAK_PASSWORD=admin \
             *             quay.io/keycloak/keycloak:14.0.0
             *     </pre>,
             *
             *     then the URL would be "http://localhost:9090/auth".
             * </p>
             */
            private String baseUrl;

            /**
             * Specifies where users will be redirected after authenticating successfully if they
             * have not visited a secured page prior to authenticating or {@code alwaysUse} is
             * true.
             */
            private String loginSuccessUrl = "/wicket";
        }



    }

    private final Applib applib = new Applib();
    @Data
    public static class Applib {

        private final Annotation annotation = new Annotation();
        @Data
        public static class Annotation {

            private final DomainObject domainObject = new DomainObject();

            public interface ConfigPropsForPropertyOrParameterLayout {
                /**
                 * Defines the default position for the label if not specified through an annotation.
                 *
                 * <p>
                 *     If left as {@link LabelPosition#NOT_SPECIFIED} and not overridden, then the position depends
                 *     upon the viewer implementation.
                 * </p>
                 */
                LabelPosition getLabelPosition();
            }

            @Data
            public static class DomainObject {

                /**
                 * TODO[2464] semantic renaming audit/dispatch -> publishing
                 * The default for whether <i>domain entities</i> should be audited or not (meaning that any changes are
                 * sent through to {@link EntityChangesSubscriber}s and
                 * sent through to {@link EntityPropertyChangeSubscriber}.
                 *
                 * <p>
                 * This setting can be overridden on a case-by-case basis using {@link org.apache.isis.applib.annotation.DomainObject#entityChangePublishing()}
                 * </p>
                 *
                 * <p>
                 *     Note: this applies only to domain entities, not view models.
                 * </p>
                 */
                private EntityChangePublishingPolicy entityChangePublishing = EntityChangePublishingPolicy.NONE;

                /**
                 * The default for whether the properties of domain objects can be edited, or whether instead they
                 * can be modified only using actions (or programmatically as a side-effect of actions on other objects).
                 *
                 * <p>
                 * This setting can be overridden on a case-by-case basis using {@link DomainObject#getEditing()  DomainObject#getEditing()}
                 * </p>
                 */
                private EditingObjectsConfiguration editing = EditingObjectsConfiguration.FALSE;

                private final CreatedLifecycleEvent createdLifecycleEvent = new CreatedLifecycleEvent();
                @Data
                public static class CreatedLifecycleEvent {
                    /**
                     * Influences whether an {@link org.apache.isis.applib.events.lifecycle.ObjectCreatedEvent} should
                     * be published (on the internal {@link org.apache.isis.applib.services.eventbus.EventBusService})
                     * whenever a domain object has been created using {@link org.apache.isis.applib.services.factory.FactoryService}.
                     *
                     * <p>
                     *     The algorithm for determining whether (and what type of) an event is sent depends on the value of the
                     *     {@link org.apache.isis.applib.annotation.DomainObject#createdLifecycleEvent() @DomainObject(createdLifecycleEvent=...)} for the
                     *     domain object in question.
                     * </p>
                     *
                     * <ul>
                     *     <li>
                     *         If set to some subtype of
                     *         {@link org.apache.isis.applib.events.lifecycle.ObjectCreatedEvent.Noop ObjectCreatedEvent.Noop},
                     *         then <i>no</i> event is sent.
                     *     </li>
                     *     <li>
                     *         If set to some subtype of
                     *         {@link org.apache.isis.applib.events.lifecycle.ObjectCreatedEvent.Default ObjectCreatedEvent.Default},
                     *         then an event is sent <i>if and only if</i> this configuration setting is set.
                     *     </li>
                     *     <li>
                     *         If set to any other subtype, then an event <i>is</i> sent.
                     *     </li>
                     * </ul>
                     */
                    private boolean postForDefault = true;
                }

                private final LoadedLifecycleEvent loadedLifecycleEvent = new LoadedLifecycleEvent();
                @Data
                public static class LoadedLifecycleEvent {
                    /**
                     * Influences whether an {@link org.apache.isis.applib.events.lifecycle.ObjectLoadedEvent} should
                     * be published (on the internal {@link org.apache.isis.applib.services.eventbus.EventBusService})
                     * whenever a domain <i>entity</i> has been loaded from the persistence store.
                     *
                     * <p>
                     *     The algorithm for determining whether (and what type of) an event is sent depends on the value of the
                     *     {@link org.apache.isis.applib.annotation.DomainObject#loadedLifecycleEvent() @DomainObject(loadedLifecycleEvent=...)} for the
                     *     domain object in question.
                     * </p>
                     *
                     * <ul>
                     *     <li>
                     *         If set to some subtype of
                     *         {@link org.apache.isis.applib.events.lifecycle.ObjectLoadedEvent.Noop ObjectLoadedEvent.Noop},
                     *         then <i>no</i> event is sent.
                     *     </li>
                     *     <li>
                     *         If set to some subtype of
                     *         {@link org.apache.isis.applib.events.lifecycle.ObjectLoadedEvent.Default ObjectCreatedEvent.Default},
                     *         then an event is sent <i>if and only if</i> this configuration setting is set.
                     *     </li>
                     *     <li>
                     *         If set to any other subtype, then an event <i>is</i> sent.
                     *     </li>
                     * </ul>
                     *
                     * <p>
                     *     Note: this applies only to domain entities, not to view models.
                     * </p>
                     */
                    private boolean postForDefault = true;
                }

                private final PersistingLifecycleEvent persistingLifecycleEvent = new PersistingLifecycleEvent();
                @Data
                public static class PersistingLifecycleEvent {
                    /**
                     * Influences whether an {@link org.apache.isis.applib.events.lifecycle.ObjectPersistingEvent} should
                     * be published (on the internal {@link org.apache.isis.applib.services.eventbus.EventBusService})
                     * whenever a domain <i>entity</i> is about to be persisting (for the first time) to the persistence store.
                     *
                     * <p>
                     *     The algorithm for determining whether (and what type of) an event is sent depends on the value of the
                     *     {@link org.apache.isis.applib.annotation.DomainObject#persistingLifecycleEvent() @DomainObject(persistingLifecycleEvent=...)} for the
                     *     domain object in question.
                     * </p>
                     *
                     * <ul>
                     *     <li>
                     *         If set to some subtype of
                     *         {@link org.apache.isis.applib.events.lifecycle.ObjectPersistingEvent.Noop ObjectPersistingEvent.Noop},
                     *         then <i>no</i> event is sent.
                     *     </li>
                     *     <li>
                     *         If set to some subtype of
                     *         {@link org.apache.isis.applib.events.lifecycle.ObjectPersistingEvent.Default ObjectCreatedEvent.Default},
                     *         then an event is sent <i>if and only if</i> this configuration setting is set.
                     *     </li>
                     *     <li>
                     *         If set to any other subtype, then an event <i>is</i> sent.
                     *     </li>
                     * </ul>
                     *
                     * <p>
                     *     Note: this applies only to domain entities, not to view models.
                     * </p>
                     */
                    private boolean postForDefault = true;
                }

                private final PersistedLifecycleEvent persistedLifecycleEvent = new PersistedLifecycleEvent();
                @Data
                public static class PersistedLifecycleEvent {
                    /**
                     * Influences whether an {@link org.apache.isis.applib.events.lifecycle.ObjectPersistedEvent} should
                     * be published (on the internal {@link org.apache.isis.applib.services.eventbus.EventBusService})
                     * whenever a domain <i>entity</i> has been persisted (for the first time) to the persistence store.
                     *
                     * <p>
                     *     The algorithm for determining whether (and what type of) an event is sent depends on the value of the
                     *     {@link org.apache.isis.applib.annotation.DomainObject#persistedLifecycleEvent() @DomainObject(persistedLifecycleEvent=...)} for the
                     *     domain object in question.
                     * </p>
                     *
                     * <ul>
                     *     <li>
                     *         If set to some subtype of
                     *         {@link org.apache.isis.applib.events.lifecycle.ObjectPersistedEvent.Noop ObjectPersistedEvent.Noop},
                     *         then <i>no</i> event is sent.
                     *     </li>
                     *     <li>
                     *         If set to some subtype of
                     *         {@link org.apache.isis.applib.events.lifecycle.ObjectPersistedEvent.Default ObjectCreatedEvent.Default},
                     *         then an event is sent <i>if and only if</i> this configuration setting is set.
                     *     </li>
                     *     <li>
                     *         If set to any other subtype, then an event <i>is</i> sent.
                     *     </li>
                     * </ul>
                     *
                     * <p>
                     *     Note: this applies only to domain entities, not to view models.
                     * </p>
                     */
                    private boolean postForDefault = true;
                }

                private final RemovingLifecycleEvent removingLifecycleEvent = new RemovingLifecycleEvent();
                @Data
                public static class RemovingLifecycleEvent {
                    /**
                     * Influences whether an {@link org.apache.isis.applib.events.lifecycle.ObjectRemovingEvent} should
                     * be published (on the internal {@link org.apache.isis.applib.services.eventbus.EventBusService})
                     * whenever a persistent domain <i>entity</i> is about to be removed (that is, deleted)
                     * from the persistence store.
                     *
                     * <p>
                     *     The algorithm for determining whether (and what type of) an event is sent depends on the value of the
                     *     {@link org.apache.isis.applib.annotation.DomainObject#removingLifecycleEvent() @DomainObject(removingLifecycleEvent=...)} for the
                     *     domain object in question.
                     * </p>
                     *
                     * <ul>
                     *     <li>
                     *         If set to some subtype of
                     *         {@link org.apache.isis.applib.events.lifecycle.ObjectRemovingEvent.Noop ObjectRemovingEvent.Noop},
                     *         then <i>no</i> event is sent.
                     *     </li>
                     *     <li>
                     *         If set to some subtype of
                     *         {@link org.apache.isis.applib.events.lifecycle.ObjectRemovingEvent.Default ObjectCreatedEvent.Default},
                     *         then an event is sent <i>if and only if</i> this configuration setting is set.
                     *     </li>
                     *     <li>
                     *         If set to any other subtype, then an event <i>is</i> sent.
                     *     </li>
                     * </ul>
                     *
                     * <p>
                     *     Note: this applies only to domain entities, not to view models.
                     * </p>
                     *
                     * <p>
                     *     Note: There is no corresponding <code>removed</code> callback, because (for the JDO persistence store at least)
                     *     it is not possible to interact with a domain entity once it has been deleted.
                     * </p>
                     */
                    private boolean postForDefault = true;
                }

                private final UpdatedLifecycleEvent updatedLifecycleEvent = new UpdatedLifecycleEvent();
                @Data
                public static class UpdatedLifecycleEvent {
                    /**
                     * Influences whether an {@link org.apache.isis.applib.events.lifecycle.ObjectUpdatedEvent} should
                     * be published (on the internal {@link org.apache.isis.applib.services.eventbus.EventBusService})
                     * whenever a persistent domain <i>entity</i> has been updated in the persistence store.
                     *
                     * <p>
                     *     The algorithm for determining whether (and what type of) an event is sent depends on the value of the
                     *     {@link org.apache.isis.applib.annotation.DomainObject#updatedLifecycleEvent() @DomainObject(updatedLifecycleEvent=...)} for the
                     *     domain object in question.
                     * </p>
                     *
                     * <ul>
                     *     <li>
                     *         If set to some subtype of
                     *         {@link org.apache.isis.applib.events.lifecycle.ObjectUpdatedEvent.Noop ObjectUpdatedEvent.Noop},
                     *         then <i>no</i> event is sent.
                     *     </li>
                     *     <li>
                     *         If set to some subtype of
                     *         {@link org.apache.isis.applib.events.lifecycle.ObjectUpdatedEvent.Default ObjectCreatedEvent.Default},
                     *         then an event is sent <i>if and only if</i> this configuration setting is set.
                     *     </li>
                     *     <li>
                     *         If set to any other subtype, then an event <i>is</i> sent.
                     *     </li>
                     * </ul>
                     *
                     * <p>
                     *     Note: this applies only to domain entities, not to view models.
                     * </p>
                     */
                    private boolean postForDefault = true;
                }

                private final UpdatingLifecycleEvent updatingLifecycleEvent = new UpdatingLifecycleEvent();
                @Data
                public static class UpdatingLifecycleEvent {
                    /**
                     * Influences whether an {@link org.apache.isis.applib.events.lifecycle.ObjectUpdatingEvent} should
                     * be published (on the internal {@link org.apache.isis.applib.services.eventbus.EventBusService})
                     * whenever a persistent domain <i>entity</i> is about to be updated in the persistence store.
                     *
                     * <p>
                     *     The algorithm for determining whether (and what type of) an event is sent depends on the value of the
                     *     {@link org.apache.isis.applib.annotation.DomainObject#updatingLifecycleEvent() @DomainObject(updatingLifecycleEvent=...)} for the
                     *     domain object in question.
                     * </p>
                     *
                     * <ul>
                     *     <li>
                     *         If set to some subtype of
                     *         {@link org.apache.isis.applib.events.lifecycle.ObjectUpdatingEvent.Noop ObjectUpdatingEvent.Noop},
                     *         then <i>no</i> event is sent.
                     *     </li>
                     *     <li>
                     *         If set to some subtype of
                     *         {@link org.apache.isis.applib.events.lifecycle.ObjectUpdatingEvent.Default ObjectCreatedEvent.Default},
                     *         then an event is sent <i>if and only if</i> this configuration setting is set.
                     *     </li>
                     *     <li>
                     *         If set to any other subtype, then an event <i>is</i> sent.
                     *     </li>
                     * </ul>
                     *
                     * <p>
                     *     Note: this applies only to domain entities, not to view models.
                     * </p>
                     */
                    private boolean postForDefault = true;
                }

            }

            private final DomainObjectLayout domainObjectLayout = new DomainObjectLayout();
            @Data
            public static class DomainObjectLayout {

                /**
                 * Defines the default number of objects that are shown in a &quot;standalone&quot; collection obtained as the
                 * result of invoking an action.
                 *
                 * <p>
                 *     This can be overridden on a case-by-case basis using {@link org.apache.isis.applib.annotation.DomainObjectLayout#paged()}.
                 * </p>
                 */
                private int paged = 25;

                private final CssClassUiEvent cssClassUiEvent = new CssClassUiEvent();
                @Data
                public static class CssClassUiEvent {
                    /**
                     * Influences whether an {@link org.apache.isis.applib.events.ui.CssClassUiEvent} should
                     * be published (on the internal {@link org.apache.isis.applib.services.eventbus.EventBusService})
                     * whenever a domain object is about to be rendered in the UI - thereby allowing subscribers to
                     * optionally {@link org.apache.isis.applib.events.ui.CssClassUiEvent#setCssClass(String)} change)
                     * the CSS classes that are used.
                     *
                     * <p>
                     *     The algorithm for determining whether (and what type of) an event is sent depends on the value of the
                     *     {@link org.apache.isis.applib.annotation.DomainObjectLayout#cssClassUiEvent()}  @DomainObjectLayout(cssClassEvent=...)} for the
                     *     domain object in question.
                     * </p>
                     *
                     * <ul>
                     *     <li>
                     *         If set to some subtype of
                     *         {@link org.apache.isis.applib.events.ui.CssClassUiEvent.Noop CssClassUiEvent.Noop},
                     *         then <i>no</i> event is sent.
                     *     </li>
                     *     <li>
                     *         If set to some subtype of
                     *         {@link org.apache.isis.applib.events.ui.CssClassUiEvent.Default CssClassUiEvent.Default},
                     *         then an event is sent <i>if and only if</i> this configuration setting is set.
                     *     </li>
                     *     <li>
                     *         If set to any other subtype, then an event <i>is</i> sent.
                     *     </li>
                     * </ul>
                     *
                     * <p>
                     *     The default is <tt>false</tt>, because otherwise the mere presence of <tt>@DomainObjectLayout</tt>
                     *     (perhaps for some attribute other than this one) will cause any imperative <code>cssClass()</code>
                     *     method to be ignored.
                     * </p>
                     */
                    private boolean postForDefault = false;
                }

                private final IconUiEvent iconUiEvent = new IconUiEvent();
                @Data
                public static class IconUiEvent {
                    /**
                     * Influences whether an {@link org.apache.isis.applib.events.ui.IconUiEvent} should
                     * be published (on the internal {@link org.apache.isis.applib.services.eventbus.EventBusService})
                     * whenever a domain object is about to be rendered in the UI - thereby allowing subscribers to
                     * optionally {@link org.apache.isis.applib.events.ui.IconUiEvent#setIconName(String)} change)
                     * the icon that is used.
                     *
                     * <p>
                     *     The algorithm for determining whether (and what type of) an event is sent depends on the value of the
                     *     {@link org.apache.isis.applib.annotation.DomainObjectLayout#iconUiEvent()}  @DomainObjectLayout(iconEvent=...)} for the
                     *     domain object in question.
                     * </p>
                     *
                     * <ul>
                     *     <li>
                     *         If set to some subtype of
                     *         {@link org.apache.isis.applib.events.ui.IconUiEvent.Noop IconUiEvent.Noop},
                     *         then <i>no</i> event is sent.
                     *     </li>
                     *     <li>
                     *         If set to some subtype of
                     *         {@link org.apache.isis.applib.events.ui.IconUiEvent.Default IconUiEvent.Default},
                     *         then an event is sent <i>if and only if</i> this configuration setting is set.
                     *     </li>
                     *     <li>
                     *         If set to any other subtype, then an event <i>is</i> sent.
                     *     </li>
                     * </ul>
                     *
                     * <p>
                     *     The default is <tt>false</tt>, because otherwise the mere presence of <tt>@DomainObjectLayout</tt>
                     *     (perhaps for some attribute other than this one) will cause any imperative <code>iconName()</code>
                     *     method to be ignored.
                     * </p>
                     */
                    private boolean postForDefault = false;
                }

                private final LayoutUiEvent layoutUiEvent = new LayoutUiEvent();
                @Data
                public static class LayoutUiEvent {
                    /**
                     * Influences whether an {@link org.apache.isis.applib.events.ui.LayoutUiEvent} should
                     * be published (on the internal {@link org.apache.isis.applib.services.eventbus.EventBusService})
                     * whenever a domain object is about to be rendered in the UI - thereby allowing subscribers to
                     * optionally {@link org.apache.isis.applib.events.ui.LayoutUiEvent#setLayout(String)} change)
                     * the layout that is used.
                     *
                     * <p>
                     *     If a different layout value has been set, then a layout in the form <code>Xxx.layout-zzz.xml</code>
                     *     use used (where <code>zzz</code> is the name of the layout).
                     * </p>
                     *
                     * <p>
                     *     The algorithm for determining whether (and what type of) an event is sent depends on the value of the
                     *     {@link org.apache.isis.applib.annotation.DomainObjectLayout#layoutUiEvent()}  @DomainObjectLayout(layoutEvent=...)} for the
                     *     domain object in question.
                     * </p>
                     *
                     * <ul>
                     *     <li>
                     *         If set to some subtype of
                     *         {@link org.apache.isis.applib.events.ui.LayoutUiEvent.Noop LayoutUiEvent.Noop},
                     *         then <i>no</i> event is sent.
                     *     </li>
                     *     <li>
                     *         If set to some subtype of
                     *         {@link org.apache.isis.applib.events.ui.LayoutUiEvent.Default LayoutUiEvent.Default},
                     *         then an event is sent <i>if and only if</i> this configuration setting is set.
                     *     </li>
                     *     <li>
                     *         If set to any other subtype, then an event <i>is</i> sent.
                     *     </li>
                     * </ul>
                     *
                     * <p>
                     *     The default is <tt>false</tt>, because otherwise the mere presence of <tt>@DomainObjectLayout</tt>
                     *     (perhaps for some attribute other than this one) will cause any imperative <code>layout()</code>
                     *     method to be ignored.
                     * </p>
                     */
                    private boolean postForDefault = false;
                }

                private final TitleUiEvent titleUiEvent = new TitleUiEvent();
                @Data
                public static class TitleUiEvent {
                    /**
                     * Influences whether an {@link org.apache.isis.applib.events.ui.TitleUiEvent} should
                     * be published (on the internal {@link org.apache.isis.applib.services.eventbus.EventBusService})
                     * whenever a domain object is about to be rendered in the UI - thereby allowing subscribers to
                     * optionally {@link org.apache.isis.applib.events.ui.TitleUiEvent#setTitle(String)} change)
                     * the title that is used.
                     *
                     * <p>
                     *     The algorithm for determining whether (and what type of) an event is sent depends on the value of the
                     *     {@link org.apache.isis.applib.annotation.DomainObjectLayout#titleUiEvent()}  @DomainObjectLayout(titleEvent=...)} for the
                     *     domain object in question.
                     * </p>
                     *
                     * <ul>
                     *     <li>
                     *         If set to some subtype of
                     *         {@link org.apache.isis.applib.events.ui.TitleUiEvent.Noop TitleUiEvent.Noop},
                     *         then <i>no</i> event is sent.
                     *     </li>
                     *     <li>
                     *         If set to some subtype of
                     *         {@link org.apache.isis.applib.events.ui.TitleUiEvent.Default TitleUiEvent.Default},
                     *         then an event is sent <i>if and only if</i> this configuration setting is set.
                     *     </li>
                     *     <li>
                     *         If set to any other subtype, then an event <i>is</i> sent.
                     *     </li>
                     * </ul>
                     *
                     * <p>
                     *     The default is <tt>false</tt>, because otherwise the mere presence of <tt>@DomainObjectLayout</tt>
                     *     (perhaps for some attribute other than this one) will cause any imperative <code>title()</code>
                     *     method to be ignored.
                     * </p>
                     */
                    private boolean postForDefault = false;
                }
            }

            private final Action action = new Action();
            @Data
            public static class Action {

                /**
                 * TODO[2464] semantic renaming audit/dispatch -> publishing
                 * The default for whether action invocations should be reified
                 * as a {@link org.apache.isis.applib.services.command.Command},
                 * to be sent to any registered
                 * {@link org.apache.isis.applib.services.publishing.spi.CommandSubscriber}s,
                 * either for auditing or for replayed against a secondary
                 * system, eg for regression testing.
                 *
                 * <p>
                 *  This setting can be overridden on a case-by-case basis using
                 *  {@link org.apache.isis.applib.annotation.Action#commandPublishing()}.
                 * </p>
                 */
                private ActionPublishingPolicy commandPublishing = ActionPublishingPolicy.NONE;

                /**
                 * TODO[2464] semantic renaming audit/dispatch -> publishing
                 * The default for whether action invocations should be sent through to the
                 * {@link org.apache.isis.applib.services.publishing.spi.ExecutionSubscriber} for publishing.
                 *
                 * <p>
                 *     The service's {@link org.apache.isis.applib.services.publishing.spi.ExecutionSubscriber#publish(Execution) publish}
                 *     method is called only once per transaction, with
                 *     {@link Execution} collecting details of
                 *     the identity of the target object, the action invoked, the action arguments and the returned
                 *     object (if any).
                 * </p>
                 *
                 * <p>
                 *  This setting can be overridden on a case-by-case basis using {@link org.apache.isis.applib.annotation.Action#executionDispatch()}.
                 * </p>
                 */
                private ActionPublishingPolicy executionPublishing = ActionPublishingPolicy.NONE;

                private final DomainEvent domainEvent = new DomainEvent();
                @Data
                public static class DomainEvent {
                    /**
                     * Influences whether an {@link org.apache.isis.applib.events.domain.ActionDomainEvent} should
                     * be published (on the internal {@link org.apache.isis.applib.services.eventbus.EventBusService})
                     * whenever an action is being interacted with.
                     *
                     * <p>
                     *     Up to five different events can be fired during an interaction, with the event's
                     *     {@link org.apache.isis.applib.events.domain.ActionDomainEvent#getEventPhase() phase}
                     *     determining which (hide, disable, validate, executing and executed).  Subscribers can
                     *     influence the behaviour at each of these phases.
                     * </p>
                     *
                     * <p>
                     *     The algorithm for determining whether (and what type of) an event is actually sent depends
                     *     on the value of the {@link org.apache.isis.applib.annotation.Action#domainEvent()} for the
                     *     action in question:
                     * </p>
                     *
                     * <ul>
                     *     <li>
                     *         If set to some subtype of
                     *         {@link org.apache.isis.applib.events.domain.ActionDomainEvent.Noop ActionDomainEvent.Noop},
                     *         then <i>no</i> event is sent.
                     *     </li>
                     *     <li>
                     *         If set to some subtype of
                     *         {@link org.apache.isis.applib.events.domain.ActionDomainEvent.Default ActionDomainEvent.Default},
                     *         then an event is sent <i>if and only if</i> this configuration setting is set.
                     *     </li>
                     *     <li>
                     *         If set to any other subtype, then an event <i>is</i> sent.
                     *     </li>
                     * </ul>
                     */
                    private boolean postForDefault = true;
                }




            }

            private final ActionLayout actionLayout = new ActionLayout();
            @Data
            public static class ActionLayout {

                private final CssClass cssClass = new CssClass();
                @Data
                public static class CssClass {
                    /**
                     * Provides a mapping of patterns to CSS classes, where the pattern is used to match against the
                     * name of the action method in order to determine a CSS class to use, for example on the action's
                     * button if rendered by the Wicket viewer.
                     *
                     * <p>
                     *     Providing a default set of patterns encourages a common set of verbs to be used.
                     * </p>
                     *
                     * <p>
                     *     The CSS class for individual actions can be overridden using
                     *     {@link org.apache.isis.applib.annotation.ActionLayout#cssClass()}.
                     * </p>
                     */
                    private String[] patterns = {
                                    "delete.*:btn-danger",
                                    "discard.*:btn-warning",
                                    "remove.*:btn-warning"};

                    @Getter(lazy = true)
                    private final Map<Pattern, String> patternsAsMap = asMap(getPatterns());

                }

                private final CssClassFa cssClassFa = new CssClassFa();
                @Data
                public static class CssClassFa {
                    /**
                     * Provides a mapping of patterns to font-awesome CSS classes, where the pattern is used to match
                     * against the name of the action method in order to determine a CSS class to use, for example on
                     * the action's menu icon if rendered by the Wicket viewer.
                     *
                     * <p>
                     *     Providing a default set of patterns encourages a common set of verbs to be used.
                     * </p>
                     *
                     * <p>
                     *     The font awesome class for individual actions can be overridden using
                     *     {@link org.apache.isis.applib.annotation.ActionLayout#cssClassFa()}.
                     * </p>
                     */
                    private String[] patterns = {
                            "add.*:fa-plus-square",
                            "all.*:fa-list",
                            "approve.*:fa-thumbs-o-up",
                            "assign.*:fa-hand-o-right",
                            "calculate.*:fa-calculator",
                            "cancel.*:fa-stop",
                            "categorise.*:fa-folder-open-o",
                            "change.*:fa-edit",
                            "clear.*:fa-remove",
                            "copy.*:fa-copy",
                            "create.*:fa-plus",
                            "decline.*:fa-thumbs-o-down",
                            "delete.*:fa-trash",
                            "discard.*:fa-trash-o",
                            "download.*:fa-download",
                            "edit.*:fa-edit",
                            "execute.*:fa-bolt",
                            "export.*:fa-download",
                            "first.*:fa-star",
                            "find.*:fa-search",
                            "install.*:fa-wrench",
                            "list.*:fa-list",
                            "import.*:fa-upload",
                            "lookup.*:fa-search",
                            "maintain.*:fa-edit",
                            "move.*:fa-exchange",
                            "new.*:fa-plus",
                            "next.*:fa-step-forward",
                            "pause.*:fa-pause",
                            "previous.*:fa-step-backward",
                            "refresh.*:fa-sync",
                            "remove.*:fa-minus-square",
                            "renew.*:fa-redo",
                            "reset.*:fa-redo",
                            "resume.*:fa-play",
                            "run.*:fa-bolt",
                            "save.*:fa-floppy-o",
                            "search.*:fa-search",
                            "stop.*:fa-stop",
                            "suspend.*:fa-pause",
                            "switch.*:fa-exchange",
                            "terminate.*:fa-stop",
                            "update.*:fa-edit",
                            "upload.*:fa-upload",
                            "verify.*:fa-check-circle",
                            "view.*:fa-search"};

                    @Getter(lazy = true)
                    private final Map<Pattern, String> patternsAsMap = asMap(getPatterns());

                }
            }

            private final Property property = new Property();
            @Data
            public static class Property {

                /**
                 * TODO[2464] semantic renaming audit/dispatch -> publishing
                 * The default for whether property edits should be reified
                 * as a {@link org.apache.isis.applib.services.command.Command},
                 * to be sent to any registered
                 * {@link org.apache.isis.applib.services.publishing.spi.CommandSubscriber}s,
                 * either for auditing or for replayed against a secondary
                 * system, eg for regression testing.
                 *
                 * <p>
                 *  This setting can be overridden on a case-by-case basis using
                 *  {@link org.apache.isis.applib.annotation.Property#commandDispatch()}.
                 * </p>
                 */
                private PropertyPublishingPolicy commandPublishing = PropertyPublishingPolicy.NONE;

                /**
                 * TODO[2464] semantic renaming audit/dispatch -> publishing
                 * The default for whether property edits should be sent through to the
                 * {@link org.apache.isis.applib.services.publishing.spi.ExecutionSubscriber} for publishing.
                 *
                 * <p>
                 * The service's {@link org.apache.isis.applib.services.publishing.spi.ExecutionSubscriber#publish(Execution) publish}
                 * method is called only once per transaction, with
                 * {@link Execution} collecting details of
                 * the identity of the target object, the property edited, and the new value of the property.
                 * </p>
                 *
                 * <p>
                 * This setting can be overridden on a case-by-case basis using
                 * {@link org.apache.isis.applib.annotation.Property#publishing()}.
                 * </p>
                 */
                private PropertyPublishingPolicy executionPublishing = PropertyPublishingPolicy.NONE;

                private final DomainEvent domainEvent = new DomainEvent();
                @Data
                public static class DomainEvent {
                    /**
                     * Influences whether an {@link org.apache.isis.applib.events.domain.PropertyDomainEvent} should
                     * be published (on the internal {@link org.apache.isis.applib.services.eventbus.EventBusService})
                     * whenever an property is being interacted with.
                     *
                     * <p>
                     *     Up to five different events can be fired during an interaction, with the event's
                     *     {@link org.apache.isis.applib.events.domain.PropertyDomainEvent#getEventPhase() phase}
                     *     determining which (hide, disable, validate, executing and executed).  Subscribers can
                     *     influence the behaviour at each of these phases.
                     * </p>
                     *
                     * <p>
                     *     The algorithm for determining whether (and what type of) an event is actually sent depends
                     *     on the value of the {@link org.apache.isis.applib.annotation.Property#domainEvent()} for the
                     *     property in question:
                     * </p>
                     *
                     * <ul>
                     *     <li>
                     *         If set to some subtype of
                     *         {@link org.apache.isis.applib.events.domain.PropertyDomainEvent.Noop propertyDomainEvent.Noop},
                     *         then <i>no</i> event is sent.
                     *     </li>
                     *     <li>
                     *         If set to some subtype of
                     *         {@link org.apache.isis.applib.events.domain.PropertyDomainEvent.Default propertyDomainEvent.Default},
                     *         then an event is sent <i>if and only if</i> this configuration setting is set.
                     *     </li>
                     *     <li>
                     *         If set to any other subtype, then an event <i>is</i> sent.
                     *     </li>
                     * </ul>
                     */
                    private boolean postForDefault = true;
                }

            }

            private final PropertyLayout propertyLayout = new PropertyLayout();
            @Data
            public static class PropertyLayout implements Applib.Annotation.ConfigPropsForPropertyOrParameterLayout {
                /**
                 * Defines the default position for the label for a domain object property.
                 *
                 * <p>
                 *     Can be overridden on a case-by-case basis using
                 *     {@link org.apache.isis.applib.annotation.ParameterLayout#labelPosition()}.
                 * </p>
                 *
                 * <p>
                 *     If left as {@link LabelPosition#NOT_SPECIFIED} and not overridden, then the position depends
                 *     upon the viewer implementation.
                 * </p>
                 */
                private LabelPosition labelPosition = LabelPosition.NOT_SPECIFIED;
            }

            private final Collection collection = new Collection();
            @Data
            public static class Collection {

                private final DomainEvent domainEvent = new DomainEvent();
                @Data
                public static class DomainEvent {
                    /**
                     * Influences whether an {@link org.apache.isis.applib.events.domain.CollectionDomainEvent} should
                     * be published (on the internal {@link org.apache.isis.applib.services.eventbus.EventBusService})
                     * whenever a collection is being interacted with.
                     *
                     * <p>
                     *     Up to two different events can be fired during an interaction, with the event's
                     *     {@link org.apache.isis.applib.events.domain.CollectionDomainEvent#getEventPhase() phase}
                     *     determining which (hide, disable)Subscribers can influence the behaviour at each of these
                     *     phases.
                     * </p>
                     *
                     * <p>
                     *     The algorithm for determining whether (and what type of) an event is actually sent depends
                     *     on the value of the {@link org.apache.isis.applib.annotation.Collection#domainEvent()} for the
                     *     collection action in question:
                     * </p>
                     *
                     * <ul>
                     *     <li>
                     *         If set to some subtype of
                     *         {@link org.apache.isis.applib.events.domain.CollectionDomainEvent.Noop CollectionDomainEvent.Noop},
                     *         then <i>no</i> event is sent.
                     *     </li>
                     *     <li>
                     *         If set to some subtype of
                     *         {@link org.apache.isis.applib.events.domain.CollectionDomainEvent.Default CollectionDomainEvent.Default},
                     *         then an event is sent <i>if and only if</i> this configuration setting is set.
                     *     </li>
                     *     <li>
                     *         If set to any other subtype, then an event <i>is</i> sent.
                     *     </li>
                     * </ul>
                     */
                    private boolean postForDefault = true;
                }
            }

            private final CollectionLayout collectionLayout = new CollectionLayout();
            @Data
            public static class CollectionLayout {

                /**
                 * Defines the initial view to display collections when rendered.
                 *
                 * <p>
                 *     The value of this can be overridden on a case-by-case basis using
                 *     {@link org.apache.isis.applib.annotation.CollectionLayout#defaultView()}.
                 *     Note that this default configuration property is an enum and so defines only a fixed number of
                 *     values, whereas the annotation returns a string; this is to allow for flexibility that
                 *     individual viewers might support their own additional types.  For example, the Wicket viewer
                 *     supports <codefullcalendar</code> which can render objects that have a date on top of a calendar
                 *     view.
                 * </p>
                 */
                private DefaultViewConfiguration defaultView = DefaultViewConfiguration.TABLE;

                /**
                 * Defines the default number of objects that are shown in a &quot;parented&quot; collection of a
                 * domain object,
                 * result of invoking an action.
                 *
                 * <p>
                 *     This can be overridden on a case-by-case basis using
                 *     {@link org.apache.isis.applib.annotation.CollectionLayout#paged()}.
                 * </p>
                 */
                private int paged = 12;
            }

            private final ViewModel viewModel = new ViewModel();
            @Data
            public static class ViewModel {
                private final Validation validation = new Validation();
                @Data
                public static class Validation {
                    private final SemanticChecking semanticChecking = new SemanticChecking();
                    @Data
                    public static class SemanticChecking {
                        /**
                         * Whether to check for inconsistencies between the usage of
                         * {@link org.apache.isis.applib.annotation.DomainObject},
                         * {@link org.apache.isis.applib.annotation.ViewModel},
                         * {@link org.apache.isis.applib.annotation.DomainObjectLayout} and
                         * {@link org.apache.isis.applib.annotation.ViewModelLayout}.
                          */
                        private boolean enable = false;
                    }
                }
            }

            private final ViewModelLayout viewModelLayout = new ViewModelLayout();
            @Data
            public static class ViewModelLayout {

                private final CssClassUiEvent cssClassUiEvent = new CssClassUiEvent();
                @Data
                public static class CssClassUiEvent {
                    /**
                     * Influences whether an {@link org.apache.isis.applib.events.ui.CssClassUiEvent} should
                     * be published (on the internal {@link org.apache.isis.applib.services.eventbus.EventBusService})
                     * whenever a view model (annotated with
                     * {@link org.apache.isis.applib.annotation.ViewModel @ViewModel}) is about to be rendered in the
                     * UI - thereby allowing subscribers to optionally
                     * {@link org.apache.isis.applib.events.ui.CssClassUiEvent#setCssClass(String)} change) the CSS
                     * classes that are used.
                     *
                     * <p>
                     *     The algorithm for determining whether (and what type of) an event is sent depends on the value of the
                     *     {@link org.apache.isis.applib.annotation.ViewModelLayout#cssClassUiEvent()}  @ViewModelLayout(cssClassEvent=...)} for the
                     *     domain object in question:
                     * </p>
                     *
                     * <ul>
                     *     <li>
                     *         If set to some subtype of
                     *         {@link org.apache.isis.applib.events.ui.CssClassUiEvent.Noop CssClassUiEvent.Noop},
                     *         then <i>no</i> event is sent.
                     *     </li>
                     *     <li>
                     *         If set to some subtype of
                     *         {@link org.apache.isis.applib.events.ui.CssClassUiEvent.Default CssClassUiEvent.Default},
                     *         then an event is sent <i>if and only if</i> this configuration setting is set.
                     *     </li>
                     *     <li>
                     *         If set to any other subtype, then an event <i>is</i> sent.
                     *     </li>
                     * </ul>
                     */
                    private boolean postForDefault =true;
                }

                private final IconUiEvent iconUiEvent = new IconUiEvent();
                @Data
                public static class IconUiEvent {
                    /**
                     * Influences whether an {@link org.apache.isis.applib.events.ui.IconUiEvent} should
                     * be published (on the internal {@link org.apache.isis.applib.services.eventbus.EventBusService})
                     * whenever a view model (annotated with
                     * {@link org.apache.isis.applib.annotation.ViewModel @ViewModel}) is about to be rendered in the
                     * UI - thereby allowing subscribers to optionally
                     * {@link org.apache.isis.applib.events.ui.IconUiEvent#setIconName(String)} change) the icon that
                     * is used.
                     *
                     * <p>
                     *     The algorithm for determining whether (and what type of) an event is sent depends on the value of the
                     *     {@link org.apache.isis.applib.annotation.ViewModelLayout#iconUiEvent()}  @ViewModelLayout(iconEvent=...)} for the
                     *     domain object in question:
                     * </p>
                     *
                     * <ul>
                     *     <li>
                     *         If set to some subtype of
                     *         {@link org.apache.isis.applib.events.ui.IconUiEvent.Noop IconUiEvent.Noop},
                     *         then <i>no</i> event is sent.
                     *     </li>
                     *     <li>
                     *         If set to some subtype of
                     *         {@link org.apache.isis.applib.events.ui.IconUiEvent.Default IconUiEvent.Default},
                     *         then an event is sent <i>if and only if</i> this configuration setting is set.
                     *     </li>
                     *     <li>
                     *         If set to any other subtype, then an event <i>is</i> sent.
                     *     </li>
                     * </ul>
                     */
                    private boolean postForDefault =true;
                }

                private final LayoutUiEvent layoutUiEvent = new LayoutUiEvent();
                @Data
                public static class LayoutUiEvent {
                    /**
                     * Influences whether an {@link org.apache.isis.applib.events.ui.LayoutUiEvent} should
                     * be published (on the internal {@link org.apache.isis.applib.services.eventbus.EventBusService})
                     * whenever a view model (annotated with
                     * {@link org.apache.isis.applib.annotation.ViewModel @ViewModel}) is about to be rendered in the
                     * UI - thereby allowing subscribers to optionally
                     * {@link org.apache.isis.applib.events.ui.LayoutUiEvent#setLayout(String)} change) the layout that is used.
                     *
                     * <p>
                     *     If a different layout value has been set, then a layout in the form <code>Xxx.layout-zzz.xml</code>
                     *     use used (where <code>zzz</code> is the name of the layout).
                     * </p>
                     *
                     * <p>
                     *     The algorithm for determining whether (and what type of) an event is sent depends on the value of the
                     *     {@link org.apache.isis.applib.annotation.ViewModelLayout#layoutUiEvent()}  @ViewModelLayout(layoutEvent=...)} for the
                     *     domain object in question:
                     * </p>
                     *
                     * <ul>
                     *     <li>
                     *         If set to some subtype of
                     *         {@link org.apache.isis.applib.events.ui.LayoutUiEvent.Noop LayoutUiEvent.Noop},
                     *         then <i>no</i> event is sent.
                     *     </li>
                     *     <li>
                     *         If set to some subtype of
                     *         {@link org.apache.isis.applib.events.ui.LayoutUiEvent.Default LayoutUiEvent.Default},
                     *         then an event is sent <i>if and only if</i> this configuration setting is set.
                     *     </li>
                     *     <li>
                     *         If set to any other subtype, then an event <i>is</i> sent.
                     *     </li>
                     * </ul>
                     */
                    private boolean postForDefault =true;
                }

                private final TitleUiEvent titleUiEvent = new TitleUiEvent();
                @Data
                public static class TitleUiEvent {
                    /**
                     * Influences whether an {@link org.apache.isis.applib.events.ui.TitleUiEvent} should
                     * be published (on the internal {@link org.apache.isis.applib.services.eventbus.EventBusService})
                     * whenever a view model (annotated with
                     * {@link org.apache.isis.applib.annotation.ViewModel @ViewModel}) is about to be rendered in the
                     * UI - thereby allowing subscribers to
                     * optionally {@link org.apache.isis.applib.events.ui.TitleUiEvent#setTitle(String)} change)
                     * the title that is used.
                     *
                     * <p>
                     *     The algorithm for determining whether (and what type of) an event is sent depends on the value of the
                     *     {@link org.apache.isis.applib.annotation.ViewModelLayout#titleUiEvent()}  @ViewModelLayout(titleEvent=...)} for the
                     *     domain object in question:
                     * </p>
                     *
                     * <ul>
                     *     <li>
                     *         If set to some subtype of
                     *         {@link org.apache.isis.applib.events.ui.TitleUiEvent.Noop TitleUiEvent.Noop},
                     *         then <i>no</i> event is sent.
                     *     </li>
                     *     <li>
                     *         If set to some subtype of
                     *         {@link org.apache.isis.applib.events.ui.TitleUiEvent.Default TitleUiEvent.Default},
                     *         then an event is sent <i>if and only if</i> this configuration setting is set.
                     *     </li>
                     *     <li>
                     *         If set to any other subtype, then an event <i>is</i> sent.
                     *     </li>
                     * </ul>
                     */
                    private boolean postForDefault =true;
                }
            }

            private final ParameterLayout parameterLayout = new ParameterLayout();
            @Data
            public static class ParameterLayout implements Applib.Annotation.ConfigPropsForPropertyOrParameterLayout {
                /**
                 * Defines the default position for the label for an action parameter.
                 *
                 * <p>
                 *     Can be overridden on a case-by-case basis using
                 *     {@link org.apache.isis.applib.annotation.ParameterLayout#labelPosition()}.
                 * </p>
                 *
                 * <p>
                 *     If left as {@link LabelPosition#NOT_SPECIFIED} and not overridden, then the position depends
                 *     upon the viewer implementation.
                 * </p>
                 */
                private LabelPosition labelPosition = LabelPosition.NOT_SPECIFIED;
            }

        }
    }

    private final Core core = new Core();
    @Data
    public static class Core {

        private final Config config = new Config();
        @Data
        public static class Config {

            public static enum ConfigurationPropertyVisibilityPolicy {
                NEVER_SHOW,
                SHOW_ONLY_IN_PROTOTYPE,
                ALWAYS_SHOW
            }

            /**
             * Configuration values might contain sensitive data, hence per default,
             * configuration properties are only visible with the configuration-page
             * when <i>prototyping</i>.
             *
             * <p>
             * Alternatively this policy can be set to either <b>always</b> show or <b>never</b> show.
             * </p>
             *
             * @see ConfigurationPropertyVisibilityPolicy
             */
            private ConfigurationPropertyVisibilityPolicy configurationPropertyVisibilityPolicy
                = ConfigurationPropertyVisibilityPolicy.SHOW_ONLY_IN_PROTOTYPE;

        }

        private final MetaModel metaModel = new MetaModel();
        @Data
        public static class MetaModel {

            /**
             * Whether domain objects to which the current user does not have visibility access should be rendered
             * within collections or drop-down choices/autocompletes.
             *
             * <p>
             *     One reason this filtering may be necessary is for multi-tenanted applications, whereby an end-user
             *     should only be able to "see" what data that they own.  For efficiency, the application should
             *     only query for objects that the end-user owns.  This configuration property acts as a safety net to
             *     prevent the end-user from viewing domain objects <i>even if</i> those domain objects were rehydrated
             *     from the persistence store.
             * </p>
             */
            private boolean filterVisibility = true;

            private final ProgrammingModel programmingModel = new ProgrammingModel();
            @Data
            public static class ProgrammingModel {

                /**
                 * If set, then any aspects of the programming model (as implemented by <code>FacetFactory</code>s that
                 * have been indicated as deprecated will simply be ignored/excluded from the metamodel.
                 */
                private boolean ignoreDeprecated = false;
            }

            private final Introspector introspector = new Introspector();
            @Data
            public static class Introspector {

                /**
                 * Policy as to how introspection should process
                 * class members and supporting methods.
                 * <p>
                 * Default is to only introspect public class members, while annotating these is optional.
                 */
                private IntrospectionPolicy policy;
                public IntrospectionPolicy getPolicy() {
                    return Optional.ofNullable(policy)
                            .orElse(IntrospectionPolicy.ANNOTATION_OPTIONAL);
                }

                /**
                 * Whether to perform introspection in parallel. Meant to speed up bootstrapping.
                 * <p>
                 *     For now this is <i>experimental</i>. Leave this disabled (the default).
                 * </p>
                 */
                private boolean parallelize = false; //TODO[ISIS-2382] concurrent spec-loading is experimental

                /**
                 * Whether all known types should be fully introspected as part of the bootstrapping, or should only be
                 * partially introspected initially.
                 *
                 * <p>
                 * Leaving this as lazy means that there's a chance that metamodel validation errors will not be
                 * discovered during bootstrap.  That said, metamodel validation is still run incrementally for any
                 * classes introspected lazily after initial bootstrapping (unless {@link #isValidateIncrementally()} is
                 * disabled.
                 * </p>
                 */
                private IntrospectionMode mode = IntrospectionMode.LAZY_UNLESS_PRODUCTION;

                /**
                 * If true, then no new specifications will be allowed to be loaded once introspection has been complete.
                 *
                 * <p>
                 * Only applies if the introspector is configured to perform full introspection up-front (either because of
                 * {@link IntrospectionMode#FULL} or {@link IntrospectionMode#LAZY_UNLESS_PRODUCTION} when in production);
                 * otherwise is ignored.
                 * </p>
                 */
                private boolean lockAfterFullIntrospection = true;

                /**
                 * If true, then metamodel validation is performed after any new specification has been loaded (after the
                 * initial bootstrapping).
                 *
                 * <p>
                 * This does <i>not</i> apply if the introspector is configured to perform full introspection up-front
                 * AND when the metamodel is {@link Core.MetaModel.Introspector#isLockAfterFullIntrospection() locked} after initial bootstrapping
                 * (because in that case the lock check will simply prevent any new specs from being loaded).
                 * But it will apply otherwise.
                 * </p>
                 *
                 * <p>In particular, this setting <i>can</i> still apply even if the {@link Core.MetaModel.Introspector#getMode() introspection mode}
                 * is set to {@link IntrospectionMode#FULL full}, because that in itself does not preclude some code
                 * from attempting to load some previously unknown type.  For example, a fixture script could attempt to
                 * invoke an action on some new type using the
                 * {@link org.apache.isis.applib.services.wrapper.WrapperFactory} - this will cause introspection of that
                 * new type to be performed.
                 * </p>
                 */
                private boolean validateIncrementally = true;

            }

            private final Validator validator = new Validator();
            @Data
            public static class Validator {

                /**
                 * Whether to perform metamodel validation in parallel.
                 */
                private boolean parallelize = true;

                /**
                 * This setting is used to determine whether the use of such deprecated features is
                 * allowed.
                 *
                 * <p>
                 *     If not allowed, then metamodel validation errors will be flagged.
                 * </p>
                 *
                 * <p>
                 *     Note that this settings has no effect if the programming model has been configured to
                 *     {@link ProgrammingModel#isIgnoreDeprecated() ignore deprecated} features (because in this case
                 *     the programming model facets simply won't be included in the introspection process.
                 * </p>
                 */
                private boolean allowDeprecated = true;

                /**
                 * If set, then checks that the supports <code>hideXxx</code> and <code>disableXxx</code> methods for
                 * actions do not have take parameters.
                 *
                 * <p>
                 *     Historically, the programming model allowed these methods to accept the same number of
                 *     parameters as the action method to which they relate, the rationale being for similarity with
                 *     the <code>validateXxx</code> method.  However, since these parameters serve no function, the
                 *     programming model has been simplified so that these supporting methods are discovered if they
                 *     have exactly no parameters.
                 * </p>
                 *
                 * <p>
                 *     Note that this aspect of the programming model relates to the <code>hideXxx</code> and
                 *     <code>disableXxx</code> supporting methods that relate to the entire method.  Do not confuse
                 *     these with the <code>hideNXxx</code> and <code>disableNXxx</code> supporting methods, which
                 *     relate to the N-th parameter, and allow up to N-1 parameters to be passed in (allowing the Nth
                 *     parameter to be dynamically hidden or disabled).
                 * </p>
                 * @deprecated this option is ignored by the framework, behavior is now fixated to noParamsOnly = true
                 */
                @Deprecated(forRemoval = true, since = "2.0.0-M7")
                private boolean noParamsOnly = true;

                /**
                 * Whether to validate that any actions that accept action parameters have either a corresponding
                 * choices or auto-complete for that action parameter, or are associated with a collection of the
                 * appropriate type.
                 */
                private boolean actionCollectionParameterChoices = true;

                /**
                 * Whether to ensure that the logical-type-name of all objects must be specified explicitly, using either
                 * {@link DomainObject#logicalTypeName()} or {@link DomainService#logicalTypeName()}.
                 *
                 * <p>
                 *     It is <i>highly advisable</i> to leave this set as enabled (the default). These logical-type-names
                 *     should also (of course) be unique - among non-abstract types.
                 * </p>
                 */
                private boolean explicitLogicalTypeNames = false;

                private final JaxbViewModel jaxbViewModel = new JaxbViewModel();
                @Data
                public static class JaxbViewModel {
                    /**
                     * If set, then ensures that all JAXB-style view models are concrete classes, not abstract.
                     */
                    private boolean notAbstract = true;
                    /**
                     * If set, then ensures that all JAXB-style view models are either top-level classes or nested
                     * static classes (in other words, checks that they are not anonymous, local nor nested
                     * non-static classes).
                     */
                    private boolean notInnerClass = true;
                    /**
                     * If set, then ensures that all JAXB-style view models have a no-arg constructor.
                     */
                    private boolean noArgConstructor = false;
                    /**
                     * If set, then ensures that for all properties of JAXB-style view models where the property's type
                     * is an entity, then that entity's type has been correctly annotated with
                     * @{@link javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter} (so that the property's value can
                     * be converted into a serializable form).
                     */
                    private boolean referenceTypeAdapter = true;
                    /**
                     * If set, then ensures that for all properties of JAXB-style view models where the property's type
                     * is a date or time, then that property has been correctly annotated with
                     * @{@link javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter} (so that the property's value can
                     * be converted into a serializable form).
                     */
                    private boolean dateTimeTypeAdapter = true;
                }

                private final Jdoql jdoql = new Jdoql();
                @Data
                public static class Jdoql {
                    /**
                     * If set, then ensures that the 'FROM' clause within any JDOQL <code>@Query</code>s annotations
                     * relates to a known entity type, and moreover that that type is compatible with the type on
                     * which the annotation appears: meaning its either a supertype of or the same type as the
                     * annotated type.
                     */
                    private boolean fromClause = true;
                    /**
                     * If set, then ensures that the 'VARIABLES' clause within any JDOQL <code>@Query</code>s relates
                     * to a known entity type.
                     */
                    private boolean variablesClause = true;
                }
            }
        }


        private final Runtime runtime = new Runtime();
        @Data
        public static class Runtime {

            /**
             * If set, then overrides the application's {@link Locale#getDefault()}
             */
            private Optional<String> locale = Optional.empty();

            /**
             * If set, then override's the application's timezone.
             */
            private String timezone;

        }

        private final RuntimeServices runtimeServices = new RuntimeServices();
        @Data
        public static class RuntimeServices {

            private final Email email = new Email();
            @Data
            public static class Email {
                /**
                 * The port to use for sending email.
                 */
                private int port = 587;
                /**
                 * The maximum number of millseconds to wait to obtain a socket connection before timing out.
                 */
                private int socketConnectionTimeout = 2000;
                /**
                 * The maximum number of millseconds to wait to obtain a socket before timing out.
                 */
                private int socketTimeout = 2000;
                /**
                 * If an email fails to send, whether to propagate the exception (meaning that potentially the end-user
                 * might see the exception), or whether instead to just indicate failure through the return value of
                 * the method ({@link org.apache.isis.applib.services.email.EmailService#send(List, List, List, String, String, DataSource...)}
                 * that's being called.
                 */
                private boolean throwExceptionOnFail = true;

                private final Override override = new Override();
                @Data
                public static class Override {
                    /**
                     * Intended for testing purposes only, if set then the requested <code>to:</code> of the email will
                     * be ignored, and instead sent to this email address instead.
                     */
                    @javax.validation.constraints.Email
                    private String to;
                    /**
                     * Intended for testing purposes only, if set then the requested <code>cc:</code> of the email will
                     * be ignored, and instead sent to this email address instead.
                     */
                    @javax.validation.constraints.Email
                    private String cc;
                    /**
                     * Intended for testing purposes only, if set then the requested <code>bcc:</code> of the email will
                     * be ignored, and instead sent to this email address instead.
                     */
                    @javax.validation.constraints.Email
                    private String bcc;
                }

                private final Sender sender = new Sender();
                @Data
                public static class Sender {
                    /**
                     * Specifies the host running the SMTP service.
                     *
                     * <p>
                     *     If not specified, then the value used depends upon the email implementation.  The default
                     *     implementation will use the <code>mail.smtp.host</code> system property.
                     * </p>
                     */
                    private String hostname;
                    /**
                     * Specifies the username to use to connect to the SMTP service.
                     *
                     * <p>
                     *     If not specified, then the sender's {@link #getAddress() email address} will be used instead.
                     * </p>
                     */
                    private String username;
                    /**
                     * Specifies the password (corresponding to the {@link #getUsername() username} to connect to the
                     * SMTP service.
                     *
                     * <p>
                     *     This configuration property is mandatory (for the default implementation of the
                     *     {@link org.apache.isis.applib.services.email.EmailService}, at least).
                     * </p>
                     */
                    private String password;
                    /**
                     * Specifies the email address of the user sending the email.
                     *
                     * <p>
                     *     If the {@link #getUsername() username} is not specified, is also used as the username to
                     *     connect to the SMTP service.
                     * </p>
                     *
                     * <p>
                     *     This configuration property is mandatory (for the default implementation of the
                     *     {@link org.apache.isis.applib.services.email.EmailService}, at least).
                     * </p>
                     */
                    @javax.validation.constraints.Email
                    private String address;
                }

                private final Tls tls = new Tls();
                @Data
                public static class Tls {
                    /**
                     * Whether TLS encryption should be started (that is, <code>STARTTLS</code>).
                     */
                    private boolean enabled = true;
                }
            }

            private final ApplicationFeatures applicationFeatures = new ApplicationFeatures();
            @Data
            public static class ApplicationFeatures {
                /**
                 * Whether the {@link org.apache.isis.applib.services.appfeat.ApplicationFeatureRepository} (or the
                 * default implementation of that service, at least) should compute the set of
                 * <code>ApplicationFeature</code> that describe the metamodel
                 * {@link ApplicationFeaturesInitConfiguration#EAGERLY eagerly}, or lazily.
                 */
                ApplicationFeaturesInitConfiguration init = ApplicationFeaturesInitConfiguration.NOT_SPECIFIED;
            }

            private final RepositoryService repositoryService = new RepositoryService();
            @Data
            public static class RepositoryService {
                /**
                 * Normally any queries are automatically preceded by flushing pending executions.
                 *
                 * <p>
                 * This key allows this behaviour to be disabled.
                 *
                 * <p>
                 *     NOTE: this key is redundant for JPA/EclipseLink, which supports its own auto-flush using
                 *     <a href="https://www.eclipse.org/eclipselink/documentation/2.7/jpa/extensions/persistenceproperties_ref.htm#BABDHEEB">eclipselink.persistence-context.flush-mode</a>
                 * </p>
                 */
                private boolean disableAutoFlush = false;

            }

            private final ExceptionRecognizer exceptionRecognizer = new ExceptionRecognizer();
            @Data
            public static class ExceptionRecognizer {

                private final Dae dae = new Dae();
                @Data
                public static class Dae {
                    /**
                     * Whether the {@link org.apache.isis.applib.services.exceprecog.ExceptionRecognizer}
                     * implementation for Spring's DataAccessException - which attempts to sanitize any exceptions
                     * arising from object stores - should be disabled (meaning that exceptions will potentially
                     * propagate as more serious to the end user).
                     */
                    private boolean disable = false;
                }
            }

            private final Translation translation = new Translation();
            @Data
            public static class Translation {

                private final Po po = new Po();

                /**
                 * Specifies the relative resource path to look for translation files.
                 * <p>
                 * If {@code null} uses {@code servletContext.getResource("/WEB-INF/")}.
                 * <p>
                 * Replaces the former Servlet context parameter 'isis.config.dir';
                 */
                private String resourceLocation = null;

                @Data
                public static class Po {
                    /**
                     * Specifies the initial mode for obtaining/discovering translations.
                     *
                     * <p>
                     *     There are three modes:
                     *     <ul>
                     *         <li>
                     *              <p>
                     *                  The default mode of {@link Mode#WRITE write} is appropriate for
                     *                  integration testing or prototyping, meaning that the service records any requests made of it
                     *                  but just returns the string unaltered.  This is a good way to discover new strings that
                     *                  require translation.
                     *              </p>
                     *         </li>
                     *         <li>
                     *              <p>
                     *                  The {@link Mode#READ read} mode is appropriate for production; the
                     *                  service looks up translations that have previously been captured.
                     *              </p>
                     *         </li>
                     *         <li>
                     *             <p>
                     *                 The {@link Mode#DISABLED disabled} performs no translation
                     *                 and simply returns the original string unchanged.  Unlike the write mode, it
                     *                 does <i>not</i> keep track of translation requests.
                     *             </p>
                     *         </li>
                     *     </ul>
                     * </p>
                     */
                    Mode mode = Mode.WRITE;
                }
            }
        }
    }


    private final Persistence persistence = new Persistence();
    @Data
    public static class Persistence {

        private final Schema schema = new Schema();
        @Data
        public static class Schema {

            /**
             * List of additional schemas to be auto-created.
             * <p>
             * Explicitly creates given list of schemas by using the specified
             * {@link #getCreateSchemaSqlTemplate()} to generate the actual SQL
             * statement against the configured data-source.
             * <p>
             * This configuration mechanism does not consider any schema-auto-creation
             * configuration (if any), that independently is provided the standard JPA way.
             */
            private final List<String> autoCreateSchemas = new ArrayList<>();

            /**
             * Does lookup additional "mapping-files" in META-INF/orm-<i>name</i>.xml
             * (equivalent to "mapping-file" entries in persistence.xml) and adds these
             * to those that are already configured the <i>Spring Data</i> way (if any).
             * @implNote not implemented for JDO
             */
            private final List<String> additionalOrmFiles = new ArrayList<>();

            /**
             * Vendor specific SQL syntax to create a DB schema.
             * <p>
             * This template is passed through {@link String#format(String, schemaName)} to
             * make the actual SQL statement thats to be used against the configured data-source.
             * <p>
             * Default template is {@literal CREATE SCHEMA IF NOT EXISTS %S} with the schema name
             * converted to upper-case.
             * <p>
             * For MYSQL/MARIADB use escape like {@code `%S`}
             */
            private String createSchemaSqlTemplate = "CREATE SCHEMA IF NOT EXISTS %S";

        }
    }


    private final Viewer viewer = new Viewer();
    @Data
    public static class Viewer {
        private final Restfulobjects restfulobjects = new Restfulobjects();
        @Data
        public static class Restfulobjects {

            @Getter
            private final Authentication authentication = new Authentication();
            @Data
            public static class Authentication {
                /**
                 * Defaults to <code>org.apache.isis.viewer.restfulobjects.viewer.webmodule.auth.AuthenticationStrategyBasicAuth</code>.
                 */
                private Optional<String> strategyClassName = Optional.empty();
            }

            /**
             * Whether to enable the <code>x-ro-follow-links</code> support, to minimize round trips.
             *
             * <p>
             *     The RO viewer provides the capability for the client to set the optional
             *     <code>x-ro-follow-links</code> query parameter, as described in section 34.4 of the RO spec v1.0.
             *     If used, the resultant representation includes the result of following the associated link, but
             *     through a server-side "join", somewhat akin to GraphQL.
             * </p>
             *
             * <p>
             *     By default this functionality is disabled, this configuration property enables the feature.
             *     If enabled, then the representations returned are non-standard with respect to the RO Spec v1.0.
             * </p>
             */
            private boolean honorUiHints = false;

            /**
             * When rendering domain objects, if set the representation returned is stripped back to a minimal set,
             * excluding links to actions and collections and with a simplified representation of an object's
             * properties.
             *
             * <p>
             *     This is disabled by default.  If enabled, then the representations returned are non-standard with
             *     respect to the RO Spec v1.0.
             * </p>
             */
            private boolean objectPropertyValuesOnly = false;

            /**
             * If set, then any unrecognised <code>Accept</code> headers will result in an HTTP <i>Not Acceptable</i>
             * response code (406).
             */
            private boolean strictAcceptChecking = false;

            /**
             * If set, then the representations returned will omit any links to the formal domain-type representations.
             */
            private boolean suppressDescribedByLinks = false;

            /**
             * If set, then - should there be an interaction with an action, property or collection that is disabled -
             * then this will prevent the <code>disabledReason</code> reason from being added to the returned
             * representation.
             *
             * <p>
             *     This is disabled by default.  If enabled, then the representations returned are non-standard with
             *     respect to the RO Spec v1.0.
             * </p>
             */
            private boolean suppressMemberDisabledReason = false;

            /**
             * If set, then the <code>x-isis-format</code> key (under <code>extensions</code>) for properties will be
             * suppressed.
             *
             * <p>
             *     This is disabled by default.  If enabled, then the representations returned are non-standard with
             *     respect to the RO Spec v1.0.
             * </p>
             */
            private boolean suppressMemberExtensions = false;

            /**
             * If set, then the <code>id</code> key for all members will be suppressed.
             *
             * <p>
             *     This is disabled by default.  If enabled, then the representations returned are non-standard with
             *     respect to the RO Spec v1.0.
             * </p>
             */
            private boolean suppressMemberId = false;

            /**
             * If set, then the detail link (in other words <code>links[rel='details' ...]</code>) for all members
             * will be suppressed.
             *
             * <p>
             *     This is disabled by default.  If enabled, then the representations returned are non-standard with
             *     respect to the RO Spec v1.0.
             * </p>
             */
            private boolean suppressMemberLinks = false;

            /**
             * If set, then the update link (in other words <code>links[rel='update'... ]</code> to perform a bulk
             * update of an object) will be suppressed.
             *
             * <p>
             *     This is disabled by default.  If enabled, then the representations returned are non-standard with
             *     respect to the RO Spec v1.0.
             * </p>
             */
            private boolean suppressUpdateLink = false;

            /**
             * If left unset (the default), then the RO viewer will use the {@link javax.ws.rs.core.UriInfo}
             * (injected using {@link javax.ws.rs.core.Context}) to figure out the base Uri (used to render
             * <code>href</code>s).
             *
             * <p>
             * This will be correct much of the time, but will almost certainly be wrong if there is a reverse proxy.
             * </p>
             *
             * <p>
             * If set, eg <code>https://dev.myapp.com/</code>, then this value will be used instead.
             * </p>
             */
            @javax.validation.constraints.Pattern(regexp="^http[s]?://[^:]+?(:\\d+)?/([^/]+/)*+$")
            private Optional<String> baseUri = Optional.empty();
        }

        private final Wicket wicket = new Wicket();
        @Data
        public static class Wicket {

            /**
             * Specifies the subclass of
             * <code>org.apache.isis.viewer.wicket.viewer.wicketapp.IsisWicketApplication</code> that is used to
             * bootstrap Wicket.
             *
             * <p>
             *     There is usually very little reason to change this from its default.
             * </p>
             */
            private String app = "org.apache.isis.viewer.wicket.viewer.wicketapp.IsisWicketApplication";

            /**
             * Whether the Ajax debug should be shown, by default this is disabled.
             */
            private boolean ajaxDebugMode = false;

            /**
             * Which major JQuery version the <i>Wicket Viewer</i> should use, default is 3.
             * @see org.apache.wicket.resource.JQueryResourceReference
             */
            private int jQueryVersion = 3;

            /**
             * The base path at which the Wicket viewer is mounted.
             */
            @javax.validation.constraints.Pattern(regexp="^[/](.*[/]|)$") @NotNull @NotEmpty
            private String basePath = "/wicket/";

            /**
             * If the end user uses a deep link to access the Wicket viewer, but is not authenticated, then this
             * configuration property determines whether to continue through to that original destination once
             * authenticated, or simply to go to the home page.
             *
             * <p>
             *     The default behaviour is to honour the original destination requested.
             * </p>
             */
            private boolean clearOriginalDestination = false;

            /**
             * The pattern used for rendering and parsing dates.
             *
             * <p>
             * Each Date scalar panel will use {@link #getDatePattern()} or {@link #getDateTimePattern()} depending on its
             * date type.  In the case of panels with a date picker, the pattern will be dynamically adjusted so that it can be
             * used by the <a href="https://github.com/Eonasdan/bootstrap-datetimepicker">Bootstrap Datetime Picker</a>
             * component (which uses <a href="http://momentjs.com/docs/#/parsing/string-format/">Moment.js formats</a>, rather
             * than those of regular Java code).
             */
            @NotNull @NotEmpty
            private String datePattern = "dd-MM-yyyy";

            /**
             * The pattern used for rendering and parsing date/times.
             *
             * <p>
             * Each Date scalar panel will use {@link #getDatePattern()} or {@link #getDateTimePattern()}
             * depending on its date type.  In the case of panels with a date time picker, the pattern will be
             * dynamically adjusted so that it can be
             * used by the <a href="https://github.com/Eonasdan/bootstrap-datetimepicker">Bootstrap Datetime Picker</a>
             * component (which uses <a href="http://momentjs.com/docs/#/parsing/string-format/">Moment.js formats</a>, rather
             * than those of regular Java code).
             * </p>
             */
            @NotNull @NotEmpty
            private String dateTimePattern = "dd-MM-yyyy HH:mm";

            /**
             * Whether the dialog mode rendered when invoking actions on domain objects should be to use
             * the sidebar (the default) or to use a modal dialog.
             *
             * <p>
             *     This can be overridden on a case-by-case basis using {@link ActionLayout#promptStyle()}.
             * </p>
             */
            private DialogMode dialogMode = DialogMode.SIDEBAR;

            /**
             * Whether the dialog mode rendered when invoking actions on domain services (that is, menus) should be to
             * use a modal dialog (the default) or to use the sidebar panel.
             *
             * <p>
             *     This can be overridden on a case-by-case basis using {@link ActionLayout#promptStyle()}.
             * </p>
             */
            private DialogMode dialogModeForMenu = DialogMode.MODAL;

            /**
             * If specified, then is rendered on each page to enable live reload.
             *
             * <p>
             *     Configuring live reload also requires an appropriate plugin to the web browser (eg see
             *     <a href="http://livereload.com/">livereload.com</a> and a mechanism to trigger changes, eg by
             *     watching <code>Xxx.layout.xml</code> files.
             * </p>
             */
            private Optional<String> liveReloadUrl = Optional.empty();

            /**
             * The maximum number of characters to use to render the title of a domain object (alongside the icon)
             * in any table, if not otherwise overridden by either {@link #getMaxTitleLengthInParentedTables()}
             * or {@link #getMaxTitleLengthInStandaloneTables()}.
             *
             * <p>
             *     If truncated, then the remainder of the title will be replaced with ellipses (...).
             * </p>
             */
            private int maxTitleLengthInTables = 12;

            private Integer maxTitleLengthInParentedTables;

            /**
             * The maximum number of characters to use to render the title of a domain object (alongside the icon) in a
             * parented table.
             *
             * <p>
             *     If truncated, then the remainder of the title will be replaced with ellipses (...).
             * </p>
             *
             * <p>
             *     If not specified, then the value of {@link #getMaxTitleLengthInTables()} is used.
             * </p>
             */
            public int getMaxTitleLengthInParentedTables() {
                return maxTitleLengthInParentedTables != null ? maxTitleLengthInParentedTables : getMaxTitleLengthInTables();
            }

            public void setMaxTitleLengthInParentedTables(final int val) {
                maxTitleLengthInParentedTables = val;
            }

            private Integer maxTitleLengthInStandaloneTables;

            /**
             * The maximum number of characters to use to render the title of a domain object (alongside the icon)
             * in a standalone table, ie the result of invoking an action.
             *
             * <p>
             *     If truncated, then the remainder of the title will be replaced with ellipses (...).
             * </p>
             *
             * <p>
             *     If not specified, then the value of {@link #getMaxTitleLengthInTables()} is used.
             * </p>
             */
            public int getMaxTitleLengthInStandaloneTables() {
                return maxTitleLengthInStandaloneTables != null ? maxTitleLengthInStandaloneTables : getMaxTitleLengthInTables();
            }
            /**
             * The maximum length that a title of an object will be shown when rendered in a standalone table;
             * will be truncated beyond this (with ellipses to indicate the truncation).
             */
            public void setMaxTitleLengthInStandaloneTables(final int val) {
                maxTitleLengthInStandaloneTables = val;
            }

            /**
             * Whether to use a modal dialog for property edits and for actions associated with properties.
             *
             * <p>
             * This can be overridden on a case-by-case basis using <code>@PropertyLayout#promptStyle</code> and
             * <code>@ActionLayout#promptStyle</code>.
             * </p>
             *
             * <p>
             * This behaviour is disabled by default; the viewer will use an inline prompt in these cases, making for a smoother
             * user experience. If enabled then this reinstates the pre-1.15.0 behaviour of using a dialog prompt in all cases.
             * </p>
             */
            private PromptStyle promptStyle = PromptStyle.INLINE;

            /**
             * Whether to redirect to a new page, even if the object being shown (after an action invocation or a property edit)
             * is the same as the previous page.
             *
             * <p>
             * This behaviour is disabled by default; the viewer will update the existing page if it can, making for a
             * smoother user experience. If enabled then this reinstates the pre-1.15.0 behaviour of redirecting in all cases.
             * </p>
             */
            private boolean redirectEvenIfSameObject = false;

            /**
             * In Firefox and more recent versions of Chrome 54+, cannot copy out of disabled fields; instead we use the
             * readonly attribute (https://www.w3.org/TR/2014/REC-html5-20141028/forms.html#the-readonly-attribute)
             *
             * <p>
             * This behaviour is enabled by default but can be disabled using this flag
             * </p>
             */
            private boolean replaceDisabledTagWithReadonlyTag = true;

            /**
             * Whether to disable a form submit button after it has been clicked, to prevent users causing an error if they
             * do a double click.
             *
             * This behaviour is enabled by default, but can be disabled using this flag.
             */
            private boolean preventDoubleClickForFormSubmit = true;

            /**
             * Whether to disable a no-arg action button after it has been clicked, to prevent users causing an error if they
             * do a double click.
             *
             * <p>
             * This behaviour is enabled by default, but can be disabled using this flag.
             * </p>
             */
            private boolean preventDoubleClickForNoArgAction = true;

            /**
             * Whether to show the footer menu bar.
             *
             * <p>
             *     This is enabled by default.
             * </p>
             */
            private boolean showFooter = true;

            /**
             * Whether Wicket tags should be stripped from the markup.
             *
             * <p>
             *      By default this is enabled, in other words Wicket tags are stripped.  Please be aware that if
             *      tags are <i>not</i> stripped, then this may break CSS rules on some browsers.
             * </p>
             */
            private boolean stripWicketTags = true;

            /**
             * Whether to suppress the sign-up link on the sign-in page.
             *
             * <p>
             *     Although this is disabled by default (in other words the sign-up link is not suppressed), note that
             *     in addition the application must provide an implementation of the
             *     {@link UserRegistrationService} as well as a
             *     configured {@link EmailNotificationService} (same conditions
             *     as for the {@link #isSuppressPasswordReset()} password reset link).
             * </p>
             */
            private boolean suppressSignUp = false;


            /**
             * Whether to suppress the password reset link on the sign-in page.
             *
             * <p>
             *     Although this is disabled by default (in other words the 'reset password' link is not suppressed),
             *     note that in addition the application must provide an implementation of the
             *     {@link UserRegistrationService} as well as a
             *     configured {@link EmailNotificationService} (same conditions
             *     as for the {@link #isSuppressSignUp()} sign-up link).
             * </p>
             */
            private boolean suppressPasswordReset = false;

            /**
             * Whether to show an indicator for a form submit button that it has been clicked.
             *
             * <p>
             * This behaviour is enabled by default.
             * </p>
             */
            private boolean useIndicatorForFormSubmit = true;

            /**
             * Whether to show an indicator for a no-arg action button that it has been clicked.
             *
             * <p>
             * This behaviour is enabled by default.
             * </p>
             */
            private boolean useIndicatorForNoArgAction = true;

            /**
             * Whether the Wicket source plugin should be enabled; if so, the markup includes links to the Wicket source.
             *
             * <p>
             *     This behaviour is disabled by default.  Please be aware that enabloing it can substantially impact
             *     performance.
             * </p>
             */
            private boolean wicketSourcePlugin = false;

            //TODO no meta data yet ... https://docs.spring.io/spring-boot/docs/current/reference/html/appendix-configuration-metadata.html#configuration-metadata-property-attributes
            private final Application application = new Application();
            @Data
            public static class Application {

                /**
                 * Label used on the about page.
                 */
                private String about;

                /**
                 * Either the location of the image file (relative to the class-path resource root),
                 * or an absolute URL.
                 *
                 * <p>
                 * This is rendered on the header panel. An image with a size of 160x40 works well.
                 * If not specified, the application.name is used instead.
                 * </p>
                 */
                @javax.validation.constraints.Pattern(regexp="^[^/].*$")
                private Optional<String> brandLogoHeader = Optional.empty();

                /**
                 * Either the location of the image file (relative to the class-path resource root),
                 * or an absolute URL.
                 *
                 * <p>
                 * This is rendered on the sign-in page. An image with a size of 400x40 works well.
                 * If not specified, the {@link Application#getName() application name} is used instead.
                 * </p>
                 */
                @javax.validation.constraints.Pattern(regexp="^[^/].*$")
                private Optional<String> brandLogoSignin = Optional.empty();

                /**
                 * URL of file to read any custom CSS, relative to <code>static</code> package on the class path.
                 *
                 * <p>
                 *     A typical value is <code>css/application.css</code>.  This will result in this file being read
                 *     from the <code>static/css</code> directory (because static resources such as CSS are mounted by
                 *     Spring by default under <code>static</code> package).
                 * </p>
                 */
                @javax.validation.constraints.Pattern(regexp="^[^/].*$")
                private Optional<String> css = Optional.empty();

                /**
                 * Specifies the URL to use of the favIcon.
                 *
                 * <p>
                 *     This is expected to be a local resource.
                 * </p>
                 */
                @javax.validation.constraints.Pattern(regexp="^[^/].*$")
                private Optional<String> faviconUrl = Optional.empty();

                /**
                 */
                /**
                 * URL of file to read any custom JavaScript, relative to <code>static</code> package on the class path.
                 *
                 * <p>
                 *     A typical value is <code>js/application.js</code>.  This will result in this file being read
                 *     from the <code>static/js</code> directory (because static resources such as CSS are mounted by
                 *     Spring by default under <code>static</code> package).
                 * </p>
                 */
                @javax.validation.constraints.Pattern(regexp="^[^/].*$")
                private Optional<String> js = Optional.empty();

                /**
                 * Specifies the file name containing the menubars.
                 *
                 * <p>
                 *     This is expected to be a local resource.
                 * </p>
                 */
                @NotNull @NotEmpty
                private String menubarsLayoutXml = "menubars.layout.xml";

                /**
                 * Identifies the application on the sign-in page
                 * (unless a {@link Application#brandLogoSignin sign-in} image is configured) and
                 * on top-left in the header
                 * (unless a {@link Application#brandLogoHeader header} image is configured).
                 */
                @NotNull @NotEmpty
                private String name = "Apache Isis ™";

                /**
                 * The version of the application, eg 1.0, 1.1, etc.
                 *
                 * <p>
                 * If present, then this will be shown in the footer on every page as well as on the
                 * about page.
                 * </p>
                 */
                private String version;
            }

            private final BookmarkedPages bookmarkedPages = new BookmarkedPages();
            @Data
            public static class BookmarkedPages {

                /**
                 * Whether the panel providing linsk to previously visited object should be accessible from the top-left of the header.
                 */
                private boolean showChooser = true;

                /**
                 * Specifies the maximum number of bookmarks to show.
                 *
                 * <p>
                 *     These are aged out on an MRU-LRU basis.
                 * </p>
                 */
                private int maxSize = 15;

                /**
                 * Whether the drop-down list of previously visited objects should be shown in the footer.
                 */
                private boolean showDropDownOnFooter = true;

            }

            private final Breadcrumbs breadcrumbs = new Breadcrumbs();
            @Data
            public static class Breadcrumbs {
                /**
                 * Whether to enable the 'where am i' feature, in other words the breadcrumbs.
                 */
                private boolean enabled = true;
                /**
                 *
                 */
                private int maxParentChainLength = 64;
            }

            /**
             * List of organisations or individuals to give credit to, shown as links and icons in the footer.
             * A maximum of 3 credits can be specified.
             *
             * <p>
             * IntelliJ unfortunately does not provide IDE completion for lists of classes; YMMV.
             * </p>
             *
             * <p>
             * @implNote - For further discussion, see for example
             * <a href="https://stackoverflow.com/questions/41417933/spring-configuration-properties-metadata-json-for-nested-list-of-objects">this stackoverflow question</a>
             * and <a href="https://github.com/spring-projects/spring-boot/wiki/IDE-binding-features#simple-pojo">this wiki page</a>.
             * </p>
             */
            private List<Credit> credit = new ArrayList<>();

            @Data
            public static class Credit {
                /**
                 * URL of an organisation or individual to give credit to, appearing as a link in the footer.
                 *
                 * <p>
                 *     For the credit to appear, the {@link #getUrl() url} must be provided along with either
                 *     {@link #getName() name} and/or {@link #getImage() image}.
                 * </p>
                 */
                @javax.validation.constraints.Pattern(regexp="^http[s]?://[^:]+?(:\\d+)?.*$")
                private String url;
                /**
                 * URL of an organisation or individual to give credit to, appearing as text in the footer.
                 *
                 * <p>
                 *     For the credit to appear, the {@link #getUrl() url} must be provided along with either
                 *     {@link #getName() name} and/or {@link #getImage() image}.
                 * </p>
                 */
                private String name;
                /**
                 * Name of an image resource of an organisation or individual, appearing as an icon in the footer.
                 *
                 * <p>
                 *     For the credit to appear, the {@link #getUrl() url} must be provided along with either
                 *     {@link #getName() name} and/or {@link #getImage() image}.
                 * </p>
                 */
                @javax.validation.constraints.Pattern(regexp="^[^/].*$")
                private String image;

                /**
                 * Whether enough information has been defined for the credit to be appear.
                 */
                public boolean isDefined() { return (name != null || image != null) && url != null; }
            }

            private final DatePicker datePicker = new DatePicker();
            @Data
            public static class DatePicker {

                /**
                 * Defines the first date available in the date picker.
                 *
                 * <p>
                 * As per http://eonasdan.github.io/bootstrap-datetimepicker/Options/#maxdate, in ISO format (per https://github.com/moment/moment/issues/1407).
                 * </p>
                 */
                @NotEmpty @NotNull
                private String minDate = "1900-01-01T00:00:00.000Z";

                /**
                 * Defines the first date available in the date picker.
                 * <p>
                 * As per http://eonasdan.github.io/bootstrap-datetimepicker/Options/#maxdate, in ISO format (per https://github.com/moment/moment/issues/1407).
                 * </p>
                 */
                @NotEmpty @NotNull
                private String maxDate = "2100-01-01T00:00:00.000Z";
            }

            private final DevelopmentUtilities developmentUtilities = new DevelopmentUtilities();
            @Data
            public static class DevelopmentUtilities {

                /**
                 * Determines whether debug bar and other stuff influenced by
                 * <code>DebugSettings#isDevelopmentUtilitiesEnabled()</code> is enabled or not.
                 *
                 * <p>
                 *     By default, depends on the mode (prototyping = enabled, server = disabled).  This property acts as an override.
                 * </p>
                 */
                private boolean enable = false;
            }

            private final RememberMe rememberMe = new RememberMe();
            @Data
            public static class RememberMe {
                /**
                 * Whether the sign-in page should have a &quot;remember me&quot; link (the default), or if it should
                 * be suppressed.
                 *
                 * <p>
                 *     If &quot;remember me&quot; is available and checked, then the viewer will allow users to login
                 *     based on encrypted credentials stored in a cookie.  An {@link #getEncryptionKey() encryption key}
                 *     can optionally be specified.
                 * </p>
                 */
                private boolean suppress = false;

                /**
                 * If the &quot;remember me&quot; feature is available, specifies the key to hold the encrypted
                 * credentials in the cookie.
                 */
                private String cookieKey = "isisWicketRememberMe";
                /**
                 * If the &quot;remember me&quot; feature is available, optionally specifies an encryption key
                 * (a complex string acting as salt to the encryption algorithm) for computing the encrypted
                 * credentials.
                 *
                 * <p>
                 *     If not set, then (in production mode) the Wicket viewer will compute a random key each time it
                 *     is started.  This will mean that any credentials stored between sessions will become invalid.
                 * </p>
                 *
                 * <p>
                 *     Conversely, if set then (in production mode) then the same salt will be used each time the app
                 *     is started, meaning that cached credentials can continue to be used across restarts.
                 * </p>
                 *
                 * <p>
                 *     In prototype mode this setting is effectively ignored, because the same key will always be
                 *     provided (either as set, or a fixed literal otherwise).
                 * </p>
                 */
                private Optional<String> encryptionKey = Optional.empty();
            }

            private final Themes themes = new Themes();
            @Data
            public static class Themes {

                /**
                 * A comma separated list of enabled theme names, as defined by https://bootswatch.com.
                 */
                private List<String> enabled = listOf("Cosmo","Flatly","Darkly","Sandstone","United");

                /**
                 * The initial theme to use.
                 *
                 * <p>
                 *     Expected to be in the list of {@link #getEnabled()} themes.
                 * </p>
                 */
                @NotEmpty @NotNull
                private String initial = "Flatly";

                /**
                 * Specifies an implementation of <code>org.apache.isis.viewer.wicket.ui.components.widgets.themepicker.IsisWicketThemeSupport</code>
                 *
                 */
                @NotEmpty @NotNull
                private String provider = "org.apache.isis.viewer.wicket.ui.components.widgets.themepicker.IsisWicketThemeSupportDefault";

                /**
                 * Whether the theme chooser widget should be available in the footer.
                 */
                private boolean showChooser = false;
            }

            private final Welcome welcome = new Welcome();
            @Data
            public static class Welcome {

                /**
                 * Text to be displayed on the application’s home page, used as a fallback if
                 * welcome.file is not specified. If a @HomePage action exists, then that will take
                 * precedence.
                 */
                private String text;
            }

            private final MessagePopups messagePopups = new MessagePopups();
            @Data
            public static class MessagePopups {

                /**
                 * How long the info popup should display before disappearing.
                 *
                 * <p>
                 *     A value of 0 means do not disappear automatically.
                 * </p>
                 */
                Duration infoDelay = Duration.ofMillis(3500);

                /**
                 * How long the warning popup should display before disappearing.
                 *
                 * <p>
                 *     A value of 0 (the default) means do not disappear automatically.
                 * </p>
                 */
                Duration warningDelay = Duration.ofMillis(0);

                /**
                 * How long the error popup should display before disappearing.
                 *
                 * <p>
                 *     A value of 0 (the default) means do not disappear automatically.
                 * </p>
                 */
                Duration errorDelay = Duration.ofMillis(0);

                /**
                 * How far in from the edge the popup should display
                 */
                int offset = 100;

                private final Placement placement = new Placement();
                @Data
                public static class Placement {

                    public static enum Vertical {
                        TOP, BOTTOM
                    }

                    public static enum Horizontal {
                        LEFT, RIGHT
                    }

                    /**
                     * Whether to display popups at the top or the bottom of the page.
                     *
                     * <p>
                     * The default is to show them at the top.
                     * </p>
                     */
                    Vertical vertical = Vertical.TOP;

                    /**
                     * Whether to display popups aligned ot the left or right of the page.
                     *
                     * <p>
                     * The default is to show them aligned to the right
                     * </p>
                     */
                    Horizontal horizontal = Horizontal.RIGHT;
                }
            }

        }
    }

    private final ValueTypes valueTypes = new ValueTypes();
    @Data
    public static class ValueTypes {

        private final Primitives primitives = new Primitives();
        @Data
        public static class Primitives {

            // capitalized to avoid clash with keyword
            private final Int Int = new Int();
            @Data
            public static class Int {
                /**
                 * Configures the number format understood by <code>IntValueSemanticsProviderAbstract</code>.
                 *
                 * @deprecated
                 */
                @Deprecated
                private String format;
            }
        }

        private final JavaLang javaLang = new JavaLang();
        @Data
        public static class JavaLang {

            // capitalized to avoid clash with keyword
            private final Byte Byte = new Byte();
            @Data
            public static class Byte {
                /**
                 * Configures the number format understood by <code>ByteValueSemanticsProviderAbstract</code>.
                 *
                 * @deprecated
                 */
                @Deprecated
                private String format;
            }

            // capitalized to avoid clash with keyword
            private final Double Double = new Double();
            @Data
            public static class Double {
                /**
                 * Configures the number format understood by <code>DoubleValueSemanticsProviderAbstract</code>.
                 *
                 * @deprecated
                 */
                @Deprecated
                private String format;
            }

            // capitalized to avoid clash with keyword
            private final Float Float = new Float();
            @Data
            public static class Float {
                /**
                 * Configures the number format understood by <code>FloatValueSemanticsProviderAbstract</code>.
                 *
                 * @deprecated
                 */
                @Deprecated
                private String format;
            }

            // capitalized to avoid clash with keyword
            private final Long Long = new Long();
            @Data
            public static class Long {
                /**
                 * Configures the number format understood by <code>LongValueSemanticsProviderAbstract</code>.
                 *
                 * @deprecated
                 */
                @Deprecated
                private String format;
            }

            // capitalized to avoid clash with keyword
            private final Short Short = new Short();
            @Data
            public static class Short {
                /**
                 * Configures the number format understood by <code>ShortValueSemanticsProviderAbstract</code>.
                 *
                 * @deprecated
                 */
                @Deprecated
                private String format;
            }
        }

        private final JavaMath javaMath = new JavaMath();
        @Data
        public static class JavaMath {
            private final BigInteger bigInteger = new BigInteger();
            @Data
            public static class BigInteger {
                /**
                 * Configures the number format understood by <code>BigIntegerValueSemanticsProvider</code>.
                 *
                 * @deprecated
                 */
                @Deprecated
                private String format;
            }

            private final BigDecimal bigDecimal = new BigDecimal();
            @Data
            public static class BigDecimal {
                /**
                 * Configures the number format understood by <code>BigDecimalValueSemanticsProvider</code>.
                 *
                 * @deprecated
                 */
                @Deprecated
                private String format;
            }
        }

        private final JavaTime javaTime = new JavaTime();
        @Data
        public static class JavaTime {
            private final LocalDateTime localDateTime = new LocalDateTime();
            @Data
            public static class LocalDateTime {
                /**
                 * Configures the formats understood by <code>LocalDateTimeValueSemanticsProvider</code>.
                 *
                 * @deprecated
                 */
                @Deprecated
                private String format = "medium";
            }

            private final OffsetDateTime offsetDateTime = new OffsetDateTime();
            @Data
            public static class OffsetDateTime {
                /**
                 * Configures the formats understood by <code>OffsetDateTimeValueSemanticsProvider</code>.
                 *
                 * @deprecated
                 */
                @Deprecated
                private String format = "medium";
            }

            private final OffsetTime offsetTime = new OffsetTime();
            @Data
            public static class OffsetTime {
                /**
                 * Configures the formats understood by <code>OffsetTimeValueSemanticsProvider</code>.
                 *
                 * @deprecated
                 */
                @Deprecated
                private String format = "medium";
            }

            private final LocalDate localDate = new LocalDate();
            @Data
            public static class LocalDate {
                /**
                 * Configures the formats understood by <code>LocalDateValueSemanticsProvider</code>.
                 *
                 * @deprecated
                 */
                @Deprecated
                private String format = "medium";
            }

            private final LocalTime localTime = new LocalTime();
            @Data
            public static class LocalTime {
                /**
                 * Configures the formats understood by <code>LocalTimeValueSemanticsProvider</code>.
                 *
                 * @deprecated
                 */
                @Deprecated
                private String format = "medium";
            }

            private final ZonedDateTime zonedDateTime = new ZonedDateTime();
            @Data
            public static class ZonedDateTime {
                /**
                 * Configures the formats understood by <code>ZonedDateTimeValueSemanticsProvider</code>.
                 *
                 * @deprecated
                 */
                @Deprecated
                private String format = "medium";
            }
        }

        private final JavaUtil javaUtil = new JavaUtil();
        @Data
        public static class JavaUtil {

            private final Date date = new Date();
            @Data
            public static class Date {
                /**
                 * Configures the formats understood by <code>JavaUtilDateValueSemanticsProvider</code>.
                 *
                 * @deprecated
                 */
                @Deprecated
                private String format = "medium";
            }

        }

        private final JavaSql javaSql = new JavaSql();
        @Data
        public static class JavaSql {
            private final Date date = new Date();
            @Data
            public static class Date {
                /**
                 * Configures the formats understood by <code>JavaSqlDateValueSemanticsProvider</code>.
                 *
                 * @deprecated
                 */
                @Deprecated
                private String format = "medium";
            }
            private final Time time = new Time();
            @Data
            public static class Time {
                /**
                 * Configures the formats understood by <code>JavaSqlTimeValueSemanticsProvider</code>.
                 *
                 * @deprecated
                 */
                @Deprecated
                private String format = "short";
            }

            private final Timestamp timestamp = new Timestamp();
            @Data
            public static class Timestamp {
                /**
                 * Configures the formats understood by <code>JavaSqlTimeStampValueSemanticsProvider</code>.
                 *
                 * @deprecated
                 */
                @Deprecated
                private String format = "short";
            }

        }

        private final Joda joda = new Joda();
        @Data
        public static class Joda {
            private final LocalDateTime localDateTime = new LocalDateTime();
            @Data
            public static class LocalDateTime {
                /**
                 * Configures the formats understood by <code>JodaLocalDateTimeValueSemanticsProvider</code>.
                 *
                 * @deprecated
                 */
                @Deprecated
                private String format = "medium";
            }

            private final LocalDate localDate = new LocalDate();
            @Data
            public static class LocalDate {
                /**
                 * Configures the formats understood by <code>JodaLocalDateValueSemanticsProvider</code>.
                 *
                 * @deprecated
                 */
                @Deprecated
                private String format = "medium";
            }

            private final DateTime dateTime = new DateTime();
            @Data
            public static class DateTime {
                /**
                 * Configures the formats understood by <code>JodaDateTimeValueSemanticsProvider</code>.
                 *
                 * @deprecated
                 */
                @Deprecated
                private String format = "medium";
            }
        }
    }

    private final Testing testing = new Testing();
    @Data
    public static class Testing {
        private final Fixtures fixtures = new Fixtures();
        @Data
        public static class Fixtures {
            /**
             * Indicates the fixture script class to run initially.
             *
             * <p>
             *     Intended for use when prototyping against an in-memory database (but will run in production mode
             *     as well if required).
             * </p>
             */
            @AssignableFrom("org.apache.isis.testing.fixtures.applib.fixturescripts.FixtureScript")
            private Class<?> initialScript = null;

            private final FixtureScriptsSpecification fixtureScriptsSpecification = new FixtureScriptsSpecification();
            @Data
            public static class FixtureScriptsSpecification {
                /**
                 * Specifies the base package from which to search for fixture scripts.
                 *
                 * <p>
                 *     Either this or {@link #getPackagePrefix() packagePrefix} must be specified.  This property is
                 *     used by preference.
                 * </p>
                 *
                 * @see #getPackagePrefix()
                 */
                private Class<?> contextClass = null;
                /**
                 * Specifies the base package from which to search for fixture scripts.
                 *
                 * <p>
                 *     Either this or {@link #getContextClass()} must be specified; {@link #getContextClass()} is
                 *     used by preference.
                 * </p>
                 *
                 * @see #getContextClass()
                 */
                private String packagePrefix = null;


                /**
                 * How to handle objects that are to be
                 * {@link FixtureScripts#newFixtureResult(FixtureScript, String, Object, boolean) added}
                 * into a {@link FixtureResult} but which are not yet persisted.
                 */
                public enum NonPersistedObjectsStrategy {
                    PERSIST,
                    IGNORE
                }

                /**
                 * How to handle fixture scripts that are submitted to be executed more than once.
                 *
                 * <p>
                 *     Note that this is a {@link FixtureScripts#getMultipleExecutionStrategy() global setting} of the
                 *     {@link FixtureScripts} service; there isn't (currently) any way to mix-and-match fixture scripts that are
                 *     written with differing semantics in mind.  Ideally it should be the responsibility of the fixture script
                 *     itself to determine whether it should be run.  As a partial solution to this, the
                 *
                 * </p>
                 */
                public enum MultipleExecutionStrategy {
                    /**
                     * Any given fixture script (or more precisely, any fixture script instance for a particular fixture script
                     * class) can only be run once.
                     *
                     * <p>
                     *     This strategy represents the original design of fixture scripts service.  Specifically, it allows an
                     *     arbitrary graph of fixture scripts (eg A -> B -> C, A -> B -> D, A -> C -> D) to be created each
                     *     specifying its dependencies, and without having to worry or co-ordinate whether those prerequisite
                     *     fixture scripts have already been run.
                     * </p>
                     * <p>
                     *     The most obvious example is a global teardown script; every fixture script can require this to be
                     *     called, but it will only be run once.
                     * </p>
                     * <p>
                     *     Note that this strategy treats fixture scripts as combining both the 'how' (which business action(s) to
                     *     call) and the also the 'what' (what the arguments are to those actions).
                     * </p>
                     */
                    EXECUTE_ONCE_BY_CLASS,
                    /**
                     * Any given fixture script can only be run once, where the check to determine if a fixture script has already
                     * been run is performed using value semantics.
                     *
                     * <p>
                     *     This strategy is a half-way house between the {@link #EXECUTE_ONCE_BY_VALUE} and {@link #EXECUTE}
                     *     strategies, where we want to prevent a fixture from running more than once, where by "fixture" we mean
                     *     the 'what' - the data to be loaded up; the 'how' is unimportant.
                     * </p>
                     *
                     * <p>
                     *     This strategy was introduced in order to better support the <tt>ExcelFixture</tt> fixture script
                     *     (provided by the (non-ASF) Isis Addons'
                     *     <a href="https://github.com/isisaddons/isis-module-excel">Excel module</a>.  The <tt>ExcelFixture</tt>
                     *     takes an Excel spreadsheet as the 'what' and loads up each row.  So the 'how' is re-usable (therefore
                     *     the {@link #EXECUTE_ONCE_BY_CLASS} doesn't apply) on the other hand we don't want the 'what' to be
                     *     loaded more than once (so the {@link #EXECUTE} strategy doesn't apply either).  The solution is for
                     *     <tt>ExcelFixture</tt> to have value semantics (a digest of the spreadsheet argument).
                     * </p>
                     */
                    EXECUTE_ONCE_BY_VALUE,
                    /**
                     * Allow fixture scripts to run as requested.
                     *
                     * <p>
                     *     This strategy is conceptually the simplest; all fixtures are run as requested.  However, it is then
                     *     the responsibility of the programmer to ensure that fixtures do not interfere with each other.  For
                     *     example, if fixture A calls fixture B which calls teardown, and fixture A also calls fixture C that
                     *     itself calls teardown, then fixture B's setup will get removed.
                     * </p>
                     * <p>
                     *     The workaround to the teardown issue is of course to call the teardown fixture only once in the test
                     *     itself; however even then this strategy cannot cope with arbitrary graphs of fixtures.  The solution
                     *     is for the fixture list to be flat, one level high.
                     * </p>
                     */
                    EXECUTE;
                }


                /**
                 * Indicates whether, if a fixture script (or more precisely any other fixture scripts of the same
                 * class) is encountered more than once in a graph of dependencies, it should be executed again or
                 * skipped.
                 *
                 * <p>
                 *     The default is to fixture scripts are executed only once per class.
                 * </p>
                 *
                 * <p>
                 * Note that this policy can be overridden on a fixture-by-fixture basis if the fixture implements
                 * {@link FixtureScriptWithExecutionStrategy}.
                 * </p>
                 */
                private MultipleExecutionStrategy multipleExecutionStrategy = MultipleExecutionStrategy.EXECUTE_ONCE_BY_CLASS;

                /**
                 * Indicates whether objects that are returned as a fixture result should be automatically persisted
                 * if required (the default) or not.
                 */
                private NonPersistedObjectsStrategy nonPersistedObjectsStrategy = NonPersistedObjectsStrategy.PERSIST;

                @AssignableFrom("org.apache.isis.testing.fixtures.applib.fixturescripts.FixtureScript")
                private Class<?> recreate = null;

                @AssignableFrom("org.apache.isis.testing.fixtures.applib.fixturescripts.FixtureScript")
                private Class<?> runScriptDefault = null;

            }

        }
    }

    private final Legacy legacy = new Legacy();
    @Data
    public static class Legacy {

        private final ValueTypes valueTypes = new ValueTypes();
        @Data
        public static class ValueTypes {
            private final Percentage percentage = new Percentage();
            @Data
            public static class Percentage {
                /**
                 * Configures the formats understood by <code>PercentageValueSemanticsProvider</code>.
                 *
                 * @deprecated
                 */
                @Deprecated
                private String format;
            }

            private final Money money = new Money();
            @Data
            public static class Money {
                /**
                 * Configures the default currency code used by <code>MoneyValueSemanticsProvider</code>.
                 *
                 * @deprecated
                 */
                @Deprecated
                private Optional<String> currency = Optional.empty();
            }
        }
    }


    private final Extensions extensions = new Extensions();
    @Data
    public static class Extensions {

        private final Cors cors = new Cors();
        @Data
        public static class Cors {

            /**
             * TODO missing java-doc
             */
            private boolean allowCredentials = false;

            /**
             * Which origins are allowed to make CORS requests.
             *
             * <p>
             *     The default is the wildcard (&quot;*&quot;) but this can be made more restrictive if necessary.
             * </p>
             *
             * <p>
             *     For more information, check the usage of the <code>cors.allowed.origins</code> init parameter
             *     for <a href="https://github.com/eBay/cors-filter">EBay CORSFilter</a>.
             * </p>
             */
            private List<String> allowedOrigins = listOf("*");

            /**
             * Which HTTP headers are allowed in a CORS request.
             *
             * <p>
             *     For more information, check the usage of the <code>cors.allowed.headers</code> init parameter
             *     for <a href="https://github.com/eBay/cors-filter">EBay CORSFilter</a>.
             * </p>
             */
            private List<String> allowedHeaders = listOf(
                    "Content-Type",
                    "Accept",
                    "Origin",
                    "Access-Control-Request-Method",
                    "Access-Control-Request-Headers",
                    "Authorization",
                    "Cache-Control",
                    "If-Modified-Since",
                    "Pragma");

            /**
             * Which HTTP methods are permitted in a CORS request.
             *
             * <p>
             *     For more information, check the usage of the <code>cors.allowed.methods</code> init parameter
             *     for <a href="https://github.com/eBay/cors-filter">EBay CORSFilter</a>.
             * </p>
             */
            private List<String> allowedMethods = listOf("GET","PUT","DELETE","POST","OPTIONS");

            /**
             * Which HTTP headers are exposed in a CORS request.
             *
             * <p>
             *     For more information, check the usage of the <code>cors.exposed.headers</code> init parameter
             *     for <a href="https://github.com/eBay/cors-filter">EBay CORSFilter</a>.
             * </p>
             */
            private List<String> exposedHeaders = listOf("Authorization");


        }

        private final Quartz quartz = new Quartz();
        @Data
        public static class Quartz {
        }

        private final CommandReplay commandReplay = new CommandReplay();
        @Data
        public static class CommandReplay {

            private final PrimaryAccess primaryAccess = new PrimaryAccess();
            @Data
            public static class PrimaryAccess {
                @javax.validation.constraints.Pattern(regexp="^http[s]?://[^:]+?(:\\d+)?.*([^/]+/)$")
                private Optional<String> baseUrlRestful;
                private Optional<String> user;
                private Optional<String> password;
                @javax.validation.constraints.Pattern(regexp="^http[s]?://[^:]+?(:\\d+)?.*([^/]+/)$")
                private Optional<String> baseUrlWicket;
            }

            private final SecondaryAccess secondaryAccess = new SecondaryAccess();
            @Data
            public static class SecondaryAccess {
                @javax.validation.constraints.Pattern(regexp="^http[s]?://[^:]+?(:\\d+)?.*([^/]+/)$")
                private Optional<String> baseUrlWicket;
            }

            private Integer batchSize = 10;

            private final QuartzSession quartzSession = new QuartzSession();
            @Data
            public static class QuartzSession {
                /**
                 * The user that runs the replay session secondary.
                 */
                private String user = "isisModuleExtCommandReplaySecondaryUser";
                private List<String> roles = listOf("isisModuleExtCommandReplaySecondaryRole");
            }

            private final QuartzReplicateAndReplayJob quartzReplicateAndReplayJob = new QuartzReplicateAndReplayJob();
            @Data
            public static class QuartzReplicateAndReplayJob {
                /**
                 * Number of milliseconds before starting the job.
                 */
                private long startDelay = 15000;
                /**
                 * Number of milliseconds before running again.
                 */
                private long repeatInterval = 10000;
            }

            private final Analyser analyser = new Analyser();
            @Data
            public static class Analyser {
                private final Result result = new Result();
                @Data
                public static class Result {
                    private boolean enabled = true;
                }
                private final Exception exception = new Exception();
                @Data
                public static class Exception {
                    private boolean enabled = true;
                }

            }
        }

        private final Secman secman = new Secman();
        @Data
        public static class Secman {

            private final Seed seed = new Seed();
            public static class Seed {

                public static final String ADMIN_USER_NAME_DEFAULT = "secman-admin";
                public static final String ADMIN_PASSWORD_DEFAULT = "pass";
                public static final String ADMIN_ROLE_NAME_DEFAULT = "isis-ext-secman-admin";
                public static final List<String> ADMIN_STICKY_NAMESPACE_PERMISSIONS_DEFAULT =
                        Collections.unmodifiableList(listOf(
                                IsisModuleApplib.NAMESPACE,
                                IsisModuleApplib.NAMESPACE_SUDO,
                                IsisModuleApplib.NAMESPACE_CONF,
                                IsisModuleApplib.NAMESPACE_FEAT,
                                "isis.security",
                                "isis.ext.h2Console",
                                "isis.ext.secman"
                        ));
                public static final List<String> ADMIN_ADDITIONAL_NAMESPACE_PERMISSIONS =
                        Collections.unmodifiableList(listOf());
                public static final String REGULAR_USER_ROLE_NAME_DEFAULT = "isis-ext-secman-user";
                public static final boolean AUTO_UNLOCK_IF_DELEGATED_AND_AUTHENTICATED_DEFAULT = false;

                @Getter
                private final Admin admin = new Admin();
                @Data
                public static class Admin {

                    /**
                     * The name of the security super user.
                     *
                     * <p>
                     * This user is automatically made a member of the
                     * {@link #getRoleName() admin role}, from which it is granted
                     * permissions to administer other users.
                     * </p>
                     *
                     * <p>
                     * The password for this user is set in {@link Admin#getPassword()}.
                     * </p>
                     *
                     * @see #getPassword()
                     * @see #getRoleName()
                     */
                    private String userName = ADMIN_USER_NAME_DEFAULT;


                    // sonar-ignore-on (detects potential security risk, which we are aware of)
                    /**
                     * The corresponding password for {@link #getUserName() admin user}.
                     *
                     * @see #getUserName()
                     */
                    private String password = ADMIN_PASSWORD_DEFAULT;
                    // sonar-ignore-off

                    /**
                     * The name of security admin role.
                     *
                     * <p>
                     * Users with this role (in particular, the default
                     * {@link Admin#getUserName() admin user} are granted access to a set of
                     * namespaces ({@link NamespacePermissions#getSticky()} and
                     * {@link NamespacePermissions#getAdditional()}) which are intended to
                     * be sufficient to allow users with this admin role to be able to
                     * administer the security module itself, for example to manage users and
                     * roles.
                     * </p>
                     *
                     * @see Admin#getUserName()
                     * @see NamespacePermissions#getSticky()
                     * @see NamespacePermissions#getAdditional()
                     */
                    private String roleName = ADMIN_ROLE_NAME_DEFAULT;

                    private final NamespacePermissions namespacePermissions = new NamespacePermissions();
                    @Data
                    public static class NamespacePermissions {

                        /**
                         * The set of namespaces to which the {@link Admin#getRoleName() admin role}
                         * is granted.
                         *
                         * <p>
                         * These namespaces are intended to be sufficient to allow users with
                         * this admin role to be able to administer the security module itself,
                         * for example to manage users and roles.  The security user is not
                         * necessarily able to use the main business logic within the domain
                         * application itself, though.
                         * </p>
                         *
                         * <p>
                         * These roles cannot be removed via user interface
                         * </p>
                         *
                         * <p>
                         * WARNING: normally these should not be overridden.  Instead, specify
                         * additional namespaces using
                         * {@link NamespacePermissions#getAdditional()}.
                         * </p>
                         *
                         * @see NamespacePermissions#getAdditional()
                         */
                        private List<String> sticky = ADMIN_STICKY_NAMESPACE_PERMISSIONS_DEFAULT;


                        /**
                         * An (optional) additional set of namespaces that the
                         * {@link Admin#getRoleName() admin role} is granted.
                         *
                         * <p>
                         * These are in addition to the main
                         * {@link NamespacePermissions#getSticky() namespaces} granted.
                         * </p>
                         *
                         * @see NamespacePermissions#getSticky()
                         */
                        @Getter @Setter
                        private List<String> additional = ADMIN_ADDITIONAL_NAMESPACE_PERMISSIONS;

                    }

                }

                @Getter
                private final RegularUser regularUser = new RegularUser();
                @Data
                public static class RegularUser {

                    /**
                     * The role name for regular users of the application, granting them access
                     * to basic security features.
                     *
                     * <p>
                     *     The exact set of permissions is hard-wired in the
                     *     <code>IsisExtSecmanRegularUserRoleAndPermissions</code> fixture.
                     * </p>
                     */
                    private String roleName = REGULAR_USER_ROLE_NAME_DEFAULT;

                }

            }

            private final DelegatedUsers delegatedUsers = new DelegatedUsers();

            @Data
            public static class DelegatedUsers {

                public enum AutoCreatePolicy {
                    AUTO_CREATE_AS_LOCKED,
                    AUTO_CREATE_AS_UNLOCKED,
                    // NO_AUTO_CREATE
                }

                /**
                 * Whether delegated users should be autocreated as locked (the default) or unlocked.
                 *
                 * <p>
                 * BE AWARE THAT if any users are auto-created as unlocked, then the set of roles that
                 * they are given should be highly restricted !!!
                 * </p>
                 */
                @Getter @Setter
                private AutoCreatePolicy autoCreatePolicy = AutoCreatePolicy.AUTO_CREATE_AS_LOCKED;

            }

            public enum PermissionsEvaluationPolicy {
                ALLOW_BEATS_VETO,
                VETO_BEATS_ALLOW
            }

            /**
             * If there are conflicting (allow vs veto) permissions at the same scope, then this policy determines
             * whether to prefer to allow the permission or to veto it.
             *
             * <p>
             *     This is only used if a {@link org.apache.isis.extensions.secman.applib.permission.spi.PermissionsEvaluationService} has not been declared explicitly.
             * </p>
             */
            private PermissionsEvaluationPolicy permissionsEvaluationPolicy = PermissionsEvaluationPolicy.ALLOW_BEATS_VETO;

            private final UserRegistration userRegistration = new UserRegistration();
            @Data
            public static class UserRegistration {
                /**
                 * The set of roles that users registering with the app are granted
                 * automatically.
                 *
                 * <p>
                 *     If using the wicket viewer, also requires
                 *     {@link Viewer.Wicket#isSuppressSignUp() isis.viewer.wicket.suppress-signup} to be set
                 *     <code>false</code>, along with any other of its other prereqs.
                 * </p>
                 */
                private final List<String> initialRoleNames = new ArrayList<>();
            }

            public enum UserMenuMeActionPolicy {
                HIDE,
                DISABLE,
                ENABLE
            }

            /**
             * Whether the presence of SecMan should result in the automatic suppression of the {@link org.apache.isis.applib.services.userui.UserMenu}'s
             * {@link UserMenu#me() me} action.
             *
             * <p>
             *     This is normally what is required as SecMan's <code>ApplicationUser</code> is a more comprehensive
             *     representation of the current user.  If the default {@link UserMenu#me() me} action is not
             *     suppressed, then the end-user will see two actions with the name &quot;me&quot; in the tertiary menu.
             * </p>
             */
            private UserMenuMeActionPolicy userMenuMeActionPolicy = UserMenuMeActionPolicy.HIDE;
        }
    }

    private static List<String> listOf(final String ...values) {
        return new ArrayList<>(Arrays.asList(values));
    }

    @Value
    static class PatternToString {
        private final Pattern pattern;
        private final String string;
    }
    private static Map<Pattern, String> asMap(final String... mappings) {
        return new LinkedHashMap<>(_NullSafe.stream(mappings).map(mapping -> {
            final String[] parts = mapping.split(":");
            if (parts.length != 2) {
                return null;
            }
            try {
                return new PatternToString(Pattern.compile(parts[0], Pattern.CASE_INSENSITIVE), parts[1]);
            } catch(Exception ex) {
                return null;
            }
        }).filter(Objects::nonNull)
        .collect(Collectors.toMap(PatternToString::getPattern, PatternToString::getString)));
    }


    @Target({ FIELD, METHOD, PARAMETER, ANNOTATION_TYPE })
    @Retention(RUNTIME)
    @Constraint(validatedBy = AssignableFromValidator.class)
    @Documented
    public @interface AssignableFrom {

        String value();

        String message()
                default "{org.apache.isis.core.config.IsisConfiguration.AssignableFrom.message}";

        Class<?>[] groups() default { };

        Class<? extends Payload>[] payload() default { };
    }


    public static class AssignableFromValidator implements ConstraintValidator<AssignableFrom, Class<?>> {

        private Class<?> superType;

        @Override
        public void initialize(final AssignableFrom assignableFrom) {
            val className = assignableFrom.value();
            try {
                superType = _Context.loadClass(className);
            } catch (ClassNotFoundException e) {
                superType = null;
            }
        }

        @Override
        public boolean isValid(
                final Class<?> candidateClass,
                final ConstraintValidatorContext constraintContext) {
            if (superType == null || candidateClass == null) {
                return true;
            }
            return superType.isAssignableFrom(candidateClass);
        }
    }

    @Target({ FIELD, METHOD, PARAMETER, ANNOTATION_TYPE })
    @Retention(RUNTIME)
    @Constraint(validatedBy = OneOfValidator.class)
    @Documented
    public @interface OneOf {

        String[] value();

        String message()
                default "{org.apache.isis.core.config.IsisConfiguration.OneOf.message}";

        Class<?>[] groups() default { };

        Class<? extends Payload>[] payload() default { };
    }


    public static class OneOfValidator implements ConstraintValidator<OneOf, String> {

        private List<String> allowed;

        @Override
        public void initialize(final OneOf assignableFrom) {
            val value = assignableFrom.value();
            allowed = value != null? Collections.unmodifiableList(Arrays.asList(value)): Collections.emptyList();
        }

        @Override
        public boolean isValid(
                final String candidateValue,
                final ConstraintValidatorContext constraintContext) {
            return allowed.contains(candidateValue);
        }
    }

}
