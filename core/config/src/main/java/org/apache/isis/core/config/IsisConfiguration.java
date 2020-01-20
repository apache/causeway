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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.validation.annotation.Validated;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.Collection;
import org.apache.isis.applib.annotation.CollectionLayout;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.DomainObjectLayout;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.LabelPosition;
import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.applib.annotation.PromptStyle;
import org.apache.isis.applib.events.domain.ActionDomainEvent;
import org.apache.isis.applib.services.command.CommandWithDto;
import org.apache.isis.applib.services.i18n.TranslationService;
import org.apache.isis.applib.services.iactn.Interaction;
import org.apache.isis.applib.services.publish.PublishedObjects;
import org.apache.isis.core.commons.internal.base._Strings;
import org.apache.isis.core.config.metamodel.facets.AuditObjectsConfiguration;
import org.apache.isis.core.config.metamodel.facets.CommandActionsConfiguration;
import org.apache.isis.core.config.metamodel.facets.CommandPropertiesConfiguration;
import org.apache.isis.core.config.metamodel.facets.DefaultViewConfiguration;
import org.apache.isis.core.config.metamodel.facets.EditingObjectsConfiguration;
import org.apache.isis.core.config.metamodel.facets.PublishActionsConfiguration;
import org.apache.isis.core.config.metamodel.facets.PublishObjectsConfiguration;
import org.apache.isis.core.config.metamodel.facets.PublishPropertiesConfiguration;
import org.apache.isis.core.config.metamodel.services.ApplicationFeaturesInitConfiguration;
import org.apache.isis.core.config.metamodel.specloader.IntrospectionMode;
import org.apache.isis.core.config.viewer.wicket.DialogMode;

import lombok.Data;
import lombok.Value;


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
    @Autowired private ConfigurableEnvironment environment;
    
    @Inject @Named("isis-settings") private Map<String, String> isisSettings;
    public Map<String, String> getAsMap() { return Collections.unmodifiableMap(isisSettings); }
    
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
                 * The default for whether <i>domain entities</i> should be audited or not (meaning that any changes are
                 * sent through to the {@link org.apache.isis.applib.services.audit.AuditerService}.
                 *
                 * <p>
                 * This setting can be overridden on a case-by-case basis using {@link org.apache.isis.applib.annotation.DomainObject#auditing()} DomainObject#getAuditing()}
                 * </p>
                 *
                 * <p>
                 *     Note: this applies only to domain entities, not view models.
                 * </p>
                 */
                private AuditObjectsConfiguration auditing = AuditObjectsConfiguration.NONE;

                /**
                 * The default for whether the properties of domain objects can be edited, or whether instead they
                 * can be modified only using actions (or programmatically as a side-effect of actions on other objects).
                 *
                 * <p>
                 * This setting can be overridden on a case-by-case basis using {@link DomainObject#getEditing()  DomainObject#getEditing()}
                 * </p>
                 */
                private EditingObjectsConfiguration editing = EditingObjectsConfiguration.TRUE;

                /**
                 * The default for whether the identities of changed objects should be sent through to the
                 * {@link org.apache.isis.applib.services.publish.PublisherService} for publishing.
                 *
                 * <p>
                 *     The service's {@link org.apache.isis.applib.services.publish.PublisherService#publish(PublishedObjects) publish}
                 *     method is called only once per transaction, with {@link PublishedObjects} collecting details of
                 *     all changed domain objects.
                 * </p>
                 *
                 * <p>
                 *  This setting can be overridden on a case-by-case basis using {@link org.apache.isis.applib.annotation.DomainObject#publishing()}.
                 * </p>
                 */
                private PublishObjectsConfiguration publishing = PublishObjectsConfiguration.NONE;

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
                     */
                    private boolean postForDefault = true;
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
                     */
                    private boolean postForDefault = true;
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
                     */
                    private boolean postForDefault = true;
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
                     */
                    private boolean postForDefault = true;
                }
            }

            private final Action action = new Action();
            @Data
            public static class Action {

                /**
                 * The default for whether action invocations should be reified as a
                 * {@link org.apache.isis.applib.services.command.Command} using the
                 * {@link org.apache.isis.applib.services.command.spi.CommandService}, possibly so that the actual
                 * execution of the action can be deferred until later (background execution) or replayed against a
                 * copy of the system.
                 *
                 * <p>
                 *     In particular, the {@link org.apache.isis.applib.services.command.CommandWithDto} implementation
                 *     of {@link org.apache.isis.applib.services.command.Command} represents the action invocation
                 *     memento (obtained using {@link CommandWithDto#asDto()}) as a
                 *     {@link org.apache.isis.schema.cmd.v2.CommandDto}.
                 * </p>
                 *
                 * <p>
                 *  This setting can be overridden on a case-by-case basis using
                 *  {@link org.apache.isis.applib.annotation.Action#command()}.
                 * </p>
                 */
                private CommandActionsConfiguration command = CommandActionsConfiguration.NONE;

                /**
                 * Whether or not a public method needs to be annotated with
                 * @{@link org.apache.isis.applib.annotation.Action} in order to be picked up as an action in the
                 * metamodel.
                 */
                private boolean explicit = false;

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

                /**
                 * The default for whether action invocations should be sent through to the
                 * {@link org.apache.isis.applib.services.publish.PublisherService} for publishing.
                 *
                 * <p>
                 *     The service's {@link org.apache.isis.applib.services.publish.PublisherService#publish(Interaction.Execution) publish}
                 *     method is called only once per transaction, with
                 *     {@link org.apache.isis.applib.services.iactn.Interaction.Execution} collecting details of
                 *     the identity of the target object, the action invoked, the action arguments and the returned
                 *     object (if any).
                 * </p>
                 *
                 * <p>
                 *  This setting can be overridden on a case-by-case basis using {@link org.apache.isis.applib.annotation.Action#publishing()}.
                 * </p>
                 */
                private PublishActionsConfiguration publishing = PublishActionsConfiguration.NONE;

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
                    private Map<Pattern, String> patterns = asMap(
                                    "delete.*:btn-danger",
                                    "discard.*:btn-warning",
                                    "remove.*:btn-warning"
                    );
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
                    private Map<Pattern, String> patterns = asMap(
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
                            "edit.*:fa-pencil-square-o",
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
                            "refresh.*:fa-refresh",
                            "remove.*:fa-minus-square",
                            "renew.*:fa-repeat",
                            "reset.*:fa-repeat",
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
                            "view.*:fa-search");
                }
            }

            private final Property property = new Property();
            @Data
            public static class Property {

                /**
                 * The default for whether property edits should be reified as a
                 * {@link org.apache.isis.applib.services.command.Command} using the
                 * {@link org.apache.isis.applib.services.command.spi.CommandService}, possibly so that the actual
                 * execution of the property edit can be deferred until later (background execution) or replayed
                 * against a copy of the system.
                 *
                 * <p>
                 *     In particular, the {@link org.apache.isis.applib.services.command.CommandWithDto} implementation
                 *     of {@link org.apache.isis.applib.services.command.Command} represents the action invocation
                 *     memento (obtained using {@link CommandWithDto#asDto()}) as a
                 *     {@link org.apache.isis.schema.cmd.v2.CommandDto}.
                 * </p>
                 *
                 * <p>
                 *  This setting can be overridden on a case-by-case basis using
                 *  {@link org.apache.isis.applib.annotation.Action#command()}.
                 * </p>
                 */
                private CommandPropertiesConfiguration command = CommandPropertiesConfiguration.NONE;

                /**
                 * The default for whether property edits should be sent through to the
                 * {@link org.apache.isis.applib.services.publish.PublisherService} for publishing.
                 *
                 * <p>
                 *     The service's {@link org.apache.isis.applib.services.publish.PublisherService#publish(Interaction.Execution) publish}
                 *     method is called only once per transaction, with
                 *     {@link org.apache.isis.applib.services.iactn.Interaction.Execution} collecting details of
                 *     the identity of the target object, the property edited, and the new value of the property.
                 * </p>
                 *
                 * <p>
                 *  This setting can be overridden on a case-by-case basis using {
                 *  @link org.apache.isis.applib.annotation.Property#publishing()}.
                 * </p>
                 */
                private PublishPropertiesConfiguration publishing = PublishPropertiesConfiguration.NONE;

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
                private DefaultViewConfiguration defaultView = DefaultViewConfiguration.HIDDEN;

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
                 * Whether to perform introspection in parallel.
                 */
                private boolean parallelize = true;

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
                 * Whether to ensure that the object type of all objects (which can be set either explicitly using
                 * {@link DomainObject#objectType()} or {@link DomainService#objectType()}, or can be inferred
                 * implicitly using a variety of mechanisms) must be unique with respect to all other object types.
                 *
                 * <p>
                 *     It is <i>highly advisable</i> to leave this set as enabled (the default), and to also use
                 *     explicit types (see {@link #isExplicitObjectType()}.
                 * </p>
                 */
                private boolean ensureUniqueObjectTypes = true;

                // TODO: to remove
                private boolean checkModuleExtent = true;
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
                 */
                private boolean noParamsOnly = false;

                /**
                 * Whether to validate that any actions that accept action parameters have either a corresponding
                 * choices or auto-complete for that action parameter, or are associated with a collection of the
                 * appropriate type.
                 */
                private boolean actionCollectionParameterChoices = true;

                /**
                 * If set, checks that any domain services have only actions associated with them, not properties
                 * or collections.
                 *
                 * @deprecated - in that in the future the programming model will simply not search for properties or collections of domain services.
                 */
                @Deprecated
                private boolean serviceActionsOnly = true;

                /**
                 * If set, then domain services actions are not contributed to domain objects.
                 *
                 * @deprecated - in that in the future the programming model will simply not support contributed actions from domain services.
                 */
                @Deprecated
                private boolean mixinsOnly = true;

                /**
                 * Whether to ensure that the object type of all objects must be specified explicitly, using either
                 * {@link DomainObject#objectType()} or {@link DomainService#objectType()}.
                 *
                 * <p>
                 *     It is <i>highly advisable</i> to leave this set as enabled (the default).  These object types
                 *     should also (of course) be unique - that can be checked by setting the
                 *     {@link #isEnsureUniqueObjectTypes()} config property.
                 * </p>
                 */
                private boolean explicitObjectType = false;

                private final JaxbViewModel jaxbViewModel = new JaxbViewModel();
                @Data
                public static class JaxbViewModel {
                    private boolean notAbstract = true;
                    private boolean notInnerClass = true;
                    private boolean noArgConstructor = false;
                    private boolean referenceTypeAdapter = true;
                    private boolean dateTimeTypeAdapter = true;
                }

                private final Jdoql jdoql = new Jdoql();
                @Data
                public static class Jdoql {
                    private boolean fromClause = true;
                    private boolean variablesClause = true;
                }
            }
        }


        private final Runtime runtime = new Runtime();
        @Data
        public static class Runtime {

            /**
             * Set to override {@link Locale#getDefault()}
             */
            private Optional<String> locale = Optional.empty();

            //TODO no meta data yet ... https://docs.spring.io/spring-boot/docs/current/reference/html/appendix-configuration-metadata.html#configuration-metadata-property-attributes
            private String timezone;

        }

        private final RuntimeServices runtimeServices = new RuntimeServices();
        @Data
        public static class RuntimeServices {

            private final Email email = new Email();
            @Data
            public static class Email {
                private int port = 587;
                private int socketConnectionTimeout = 2000;
                private int socketTimeout = 2000;
                private boolean throwExceptionOnFail = true;

                private final Override override = new Override();
                @Data
                public static class Override {
                    @javax.validation.constraints.Email
                    private String to;
                    @javax.validation.constraints.Email
                    private String cc;
                    @javax.validation.constraints.Email
                    private String bcc;
                }

                private final Sender sender = new Sender();
                @Data
                public static class Sender {
                    private String hostname;
                    private String username;
                    private String password;
                    @javax.validation.constraints.Email
                    private String address;
                }

                private final Tls tls = new Tls();
                @Data
                public static class Tls {
                    private boolean enabled = true;
                }
            }

            private final ApplicationFeatures applicationFeatures = new ApplicationFeatures();
            @Data
            public static class ApplicationFeatures {
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
                 *     Originally introduced as part of ISIS-1134 (fixing memory leaks in the objectstore)
                 *     where it was found that the autoflush behaviour was causing a (now unrepeatable)
                 *     data integrity error (see <a href="https://issues.apache.org/jira/browse/ISIS-1134?focusedCommentId=14500638&page=com.atlassian.jira.plugin.system.issuetabpanels:comment-tabpanel#comment-14500638">ISIS-1134 comment</a>, in the isis-module-security.
                 *     However, that this could be circumvented by removing the call to flush().
                 *     We don't want to break existing apps that might rely on this behaviour, on the
                 *     other hand we want to fix the memory leak.  Adding this configuration property
                 *     seems the most prudent way forward.
                 * </p>
                 */
                private boolean disableAutoFlush = false;

            }

            private final ExceptionRecognizer exceptionRecognizer = new ExceptionRecognizer();
            @Data
            public static class ExceptionRecognizer {

                private final Jdo jdo = new Jdo();
                @Data
                public static class Jdo {
                    private boolean disable = false;
                }
            }

            private final Translation translation = new Translation();
            @Data
            public static class Translation {

                private final Po po = new Po();

                @Data
                public static class Po {
                    TranslationService.Mode mode = TranslationService.Mode.WRITE;
                }
            }

        }
    }


    private final Persistence persistence = new Persistence();
    @Data
    public static class Persistence {
        private final JdoDatanucleus jdoDatanucleus = new JdoDatanucleus();
        @Data
        public static class JdoDatanucleus {
            private String classMetadataLoadedListener = "org.apache.isis.persistence.jdo.datanucleus5.datanucleus.CreateSchemaObjectFromClassMetadata";

            private final Impl impl = new Impl();
            @Data
            public static class Impl {
                private final Datanucleus datanucleus = new Datanucleus();
                @Data
                public static class Datanucleus {


                    /**
                     * 	The JNDI name for a connection factory for transactional connections.
                     *
                     * 	<p>
                     * 	    For RBDMS, it must be a JNDI name that points to a javax.sql.DataSource object.
                     * 	</p>
                     *
                     * <p>
                     *     See also <tt>additional-spring-configuration-metadata.json</tt> (PascalCasing instead of kebab-casing).
                     * </p>
                     *
                     * @implNote - this config property isn't used by the framework, but is provided as a convenience for IDE autocomplete.
                     */
                    private String connectionFactoryName;

                    /**
                     * 	The JNDI name for a connection factory for non-transactional connections.
                     *
                     * 	<p>
                     * 	    For RBDMS, it must be a JNDI name that points to a javax.sql.DataSource object.
                     * 	</p>
                     *
                     * <p>
                     *     See also <tt>additional-spring-configuration-metadata.json</tt> (PascalCasing instead of kebab-casing).
                     * </p>
                     *
                     * @implNote - this config property isn't used by the framework, but is provided as a convenience for IDE autocomplete.
                     */
                    private String connectionFactory2Name;


                    /**
                     * Name of a class that implements <tt>org.datanucleus.store.connection.DecryptionProvider</tt>
                     * and should only be specified if the password is encrypted in the persistence properties.
                     *
                     * <p>
                     *     See also <tt>additional-spring-configuration-metadata.json</tt> (camelCasing instead of kebab-casing).
                     * </p>
                     *
                     * @implNote - this config property isn't used by the framework, but is provided as a convenience for IDE autocomplete.
                     */
                    private String connectionPasswordDecrypter;


                    /**
                     * 	Used when we have specified the persistence-unit name for a PMF/EMF and where we want the
                     * 	datastore "tables" for all classes of that persistence-unit loading up into the StoreManager.
                     *
                     * <p>
                     *     Defaults to true, which is the opposite of DataNucleus' own default.
                     *     (The reason that DN defaults to false is because some databases are slow so such an
                     *     operation would slow down the startup process).
                     * </p>
                     *
                     * <p>
                     *     See also <tt>additional-spring-configuration-metadata.json</tt> (camelCasing instead of kebab-casing).
                     * </p>
                     *
                     * @implNote - this config property isn't used by the framework, but is provided as a convenience for IDE autocomplete.
                     */
                    private boolean persistenceUnitLoadClasses = true;

                    public enum TransactionTypeEnum {
                        RESOURCE_LOCAL,
                        JTA
                    }

                    /**
                     * Type of transaction to use.
                     *
                     * <p>
                     * If running under JavaSE the default is RESOURCE_LOCAL, and if running under JavaEE the default is JTA.
                     * </p>
                     *
                     * <p>
                     *     See also <tt>additional-spring-configuration-metadata.json</tt> (camelCasing instead of kebab-casing).
                     * </p>
                     *
                     * @implNote - this config property isn't used by the framework, but is provided as a convenience for IDE autocomplete.
                     */
                    private TransactionTypeEnum transactionType;

                    private final Cache cache = new Cache();
                    @Data
                    public static class Cache {
                        private final Level2 level2 = new Level2();
                        @Data
                        public static class Level2 {
                            /**
                             * Name of the type of Level 2 Cache to use.
                             *
                             * <p>
                             * Can be used to interface with external caching products.
                             * Use "none" to turn off L2 caching.
                             * </p>
                             *
                             * <p>
                             * See also Cache docs for JDO, and for JPA
                             * </p>
                             *
                             * @implNote - this config property isn't used by the framework, but is provided as a convenience for IDE autocomplete.
                             */
                            @NotNull @NotEmpty
                            private String type = "none";
                        }
                    }
                    private final ObjectProvider objectProvider = new ObjectProvider();
                    @Data
                    public static class ObjectProvider {
                        /**
                         * Enables dependency injection into entities
                         *
                         * <p>
                         *     See also <tt>additional-spring-configuration-metadata.json</tt> (camelCasing instead of kebab-casing).
                         * </p>
                         */
                        @NotNull @NotEmpty
                        private String className = "org.apache.isis.persistence.jdo.datanucleus5.datanucleus.JDOStateManagerForIsis";
                    }
                    private final Schema schema = new Schema();
                    @Data
                    public static class Schema {
                        /**
                         * Whether DN should automatically create the database schema on bootstrapping.
                         *
                         * <p>
                         *     This should be set to <tt>true</tt> when running against an in-memory database, but
                         *     set to <tt>false</tt> when running against a persistent database (use something like
                         *     flyway instead to manage schema evolution).
                         * </p>
                         *
                         * <p>
                         *     See also <tt>additional-spring-configuration-metadata.json</tt> (camelCasing instead of kebab-casing).
                         * </p>
                         *
                         *
                         * @implNote - this config property isn't used by the core framework, but is used by one the flyway extension.
                         */
                        private boolean autoCreateAll = false;

                        /**
                         * Previously we defaulted this property to "true", but that could cause the target database
                         * to be modified
                         *
                         * <p>
                         *     See also <tt>additional-spring-configuration-metadata.json</tt> (camelCasing instead of kebab-casing).
                         * </p>
                         *
                         * @implNote - this config property isn't used by the framework, but is provided as a convenience for IDE autocomplete.
                         */
                        private boolean autoCreateDatabase = false;

                        /**
                         * <p>
                         *     See also <tt>additional-spring-configuration-metadata.json</tt> (camelCasing instead of kebab-casing).
                         * </p>
                         *
                         * @implNote - this config property isn't used by the framework, but is provided as a convenience for IDE autocomplete.
                         */
                        private boolean validateAll = true;
                    }
                }
                private final Javax javax = new Javax();
                @Data
                public static class Javax {
                    private final Jdo jdo = new Jdo();
                    @Data
                    public static class Jdo {

                        /**
                         * <p>
                         *     See also <tt>additional-spring-configuration-metadata.json</tt> (camelCasing instead of kebab-casing).
                         * </p>
                         *
                         * @implNote - changing this property from its default is used to enable the flyway extension (in combination with {@link Datanucleus.Schema#isAutoCreateAll()}
                         */
                        @NotNull @NotEmpty
                        private String persistenceManagerFactoryClass = "org.datanucleus.api.jdo.JDOPersistenceManagerFactory";

                        private final Option option = new Option();
                        @Data
                        public static class Option {
                            /**
                             * JDBC driver used by DataNucleus Object store to connect.
                             *
                             * <p>
                             *     See also <tt>additional-spring-configuration-metadata.json</tt> (PascalCasing instead of kebab-casing).
                             * </p>
                             *
                             * @implNote - this config property isn't used by the framework, but provided as a convenience for IDE autocomplete (and is mandatory if using JDO Datanucleus).
                             */
                            private String connectionDriverName;
                            /**
                             * URL used by DataNucleus Object store to connect.
                             *
                             * <p>
                             *     See also <tt>additional-spring-configuration-metadata.json</tt> (PascalCasing instead of kebab-casing).
                             * </p>
                             *
                             * @implNote - some extensions (H2Console, MsqlDbManager) peek at this URL to determine if they should be enabled.  Note that it is also mandatory if using JDO Datanucleus.
                             */
                            private String connectionUrl;
                            /**
                             * User account used by DataNucleus Object store to connect.
                             *
                             * <p>
                             *     See also <tt>additional-spring-configuration-metadata.json</tt> (PascalCasing instead of kebab-casing).
                             * </p>
                             *
                             * @implNote - this config property isn't used by the framework, but provided as a convenience for IDE autocomplete (and is mandatory if using JDO Datanucleus).
                             */
                            private String connectionUserName;
                            /**
                             * Password for the user account used by DataNucleus Object store to connect.
                             *
                             * <p>
                             *     See also <tt>additional-spring-configuration-metadata.json</tt> (PascalCasing instead of kebab-casing).
                             * </p>
                             *
                             * @implNote - this config property isn't used by the framework, but provided as a convenience for IDE autocomplete.  It is not necessarily mandatory, some databases accept an empty password.
                             */
                            private String connectionPassword;
                        }
                    }
                }
            }
        }
    }



    private final Viewer viewer = new Viewer();
    @Data
    public static class Viewer {
        private final Restfulobjects restfulobjects = new Restfulobjects();
        @Data
        public static class Restfulobjects {
            private boolean honorUiHints = false;
            private boolean objectPropertyValuesOnly = false;
            private boolean strictAcceptChecking = false;
            private boolean suppressDescribedByLinks = false;
            private boolean suppressMemberDisabledReason = false;
            private boolean suppressMemberExtensions = false;
            private boolean suppressMemberId = false;
            private boolean suppressMemberLinks = false;
            private boolean suppressUpdateLink = false;

            /**
             * If left unset (the default), then the RO viewer will use the {@link javax.ws.rs.core.UriInfo}
             * injected using  @link javax.ws.rs.core.Context}) to figure out the base Uri (used to render
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
            @javax.validation.constraints.Pattern(regexp="^http[s]?://[^:]+?(:\\d+)?/([^/]+/)*$")
            private Optional<String> baseUri = Optional.empty();
            private final Gsoc2013 gsoc2013 = new Gsoc2013();
            @Data
            public static class Gsoc2013 {
                private boolean legacyParamDetails = false;
            }
        }

        private final Wicket wicket = new Wicket();
        @Data
        public static class Wicket {

            private String app = "org.apache.isis.viewer.wicket.viewer.wicketapp.IsisWicketApplication";

            /**
             * Whether the Ajax debug should be shown.
             */
            private boolean ajaxDebugMode = false;

            @javax.validation.constraints.Pattern(regexp="^[/].*[/]$") @NotNull @NotEmpty
            private String basePath = "/wicket/";

            private boolean clearOriginalDestination = false;

            /**
             * The pattern used for rendering and parsing dates.
             *
             * <p>
             * Each Date scalar panel will use {#getDatePattern()} or {@linkplain #getDateTimePattern()} depending on its
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
             * Each Date scalar panel will use {@link Wicket#getDatePattern()} or {@link Wicket#getDateTimePattern()} depending on its
             * date type.  In the case of panels with a date time picker, the pattern will be dynamically adjusted so that it can be
             * used by the <a href="https://github.com/Eonasdan/bootstrap-datetimepicker">Bootstrap Datetime Picker</a>
             * component (which uses <a href="http://momentjs.com/docs/#/parsing/string-format/">Moment.js formats</a>, rather
             * than those of regular Java code).
             */
            @NotNull @NotEmpty
            private String dateTimePattern = "dd-MM-yyyy HH:mm";

            private DialogMode dialogMode = DialogMode.SIDEBAR;

            private DialogMode dialogModeForMenu = DialogMode.MODAL;

            private Optional<String> liveReloadUrl = Optional.empty();

            private int maxTitleLengthInTables = 12;

            private Integer maxTitleLengthInParentedTables;
            public int getMaxTitleLengthInParentedTables() {
                return maxTitleLengthInParentedTables != null ? maxTitleLengthInParentedTables : getMaxTitleLengthInTables();
            }
            /**
             * The maximum length that a title of an object will be shown when rendered in a parented table;
             * will be truncated beyond this (with ellipses to indicate the truncation).
             */
            public void setMaxTitleLengthInParentedTables(final int val) {
                maxTitleLengthInParentedTables = val;
            }

            private Integer maxTitleLengthInStandaloneTables;
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
             * This can be overridden on a case-by-case basis using <code>@PropertyLayout#promptStyle</code> and
             * <code>@ActionLayout#promptStyle</code>.
             *
             * This behaviour is disabled by default; the viewer will use an inline prompt in these cases, making for a smoother
             * user experience. If enabled then this reinstates the pre-1.15.0 behaviour of using a dialog prompt in all cases.
             */
            private PromptStyle promptStyle = PromptStyle.INLINE;

            /**
             * Whether to redirect to a new page, even if the object being shown (after an action invocation or a property edit)
             * is the same as the previous page.
             *
             * This behaviour is disabled by default; the viewer will update the existing page if it can, making for a
             * smoother user experience. If enabled then this reinstates the pre-1.15.0 behaviour of redirecting in all cases.
             */
            private boolean redirectEvenIfSameObject = false;

            /**
             * in Firefox and more recent versions of Chrome 54+, cannot copy out of disabled fields; instead we use the
             * readonly attribute (https://www.w3.org/TR/2014/REC-html5-20141028/forms.html#the-readonly-attribute)
             * This behaviour is enabled by default but can be disabled using this flag
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
             * This behaviour is enabled by default, but can be disabled using this flag.
             */
            private boolean preventDoubleClickForNoArgAction = true;

            private boolean showFooter = true;

            /**
             * Whether Wicket tags should be stripped from the markup.
             *
             * <p>
             * Be aware that if Wicket tags are <i>not</i> stripped, then this may break CSS rules on some browsers.
             * </p>
             */
            private boolean stripWicketTags = true;

            private boolean suppressSignUp = false;

            private boolean suppressPasswordReset = false;

            /**
             * The pattern used for rendering and parsing timestamps.
             */
            @NotNull @NotEmpty
            private String timestampPattern = "yyyy-MM-dd HH:mm:ss.SSS";

            /**
             * Whether to show an indicator for a form submit button that it has been clicked.
             *
             * This behaviour is enabled by default, but can be disabled using this flag.
             */
            private boolean useIndicatorForFormSubmit = true;
            /**
             * Whether to show an indicator for a no-arg action button that it has been clicked.
             *
             * This behaviour is enabled by default, but can be disabled using this flag.
             */
            private boolean useIndicatorForNoArgAction = true;

            /**
             * Whether the Wicket source plugin should be enabled; if so, the markup includes links to the Wicket source.
             *
             * <p>
             *     Be aware that this can substantially impact performance.
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
                private String brandLogoHeader;
                
                /**
                 * Either the location of the image file (relative to the class-path resource root), 
                 * or an absolute URL. 
                 * This is rendered on the sign-in page. An image with a size of 400x40 works well. 
                 * If not specified, the {@link Application#name} is used instead.
                 */
                private String brandLogoSignin;
                
                /**
                 * URL of file to read any custom CSS, relative to relative to the class-path resource 
                 * root.
                 */
                private String css;
                
                // since 2.0
                private String faviconContentType;
                
                // since 2.0
                private String faviconUrl;
                
                /**
                 * URL of file to read any custom Javascript, relative to the class-path resource root.
                 */
                private String js;

                /**
                 *
                 */
                @NotNull @NotEmpty
                private String menubarsLayoutXml = "menubars.layout.xml";

                /**
                 * Identifies the application on the sign-in page
                 * (unless a {@link Application#brandLogoSignin} image is configured) and
                 * on top-left in the header
                 * (unless a {@link Application#brandLogoHeader} image is configured).
                 */
                @NotNull @NotEmpty
                private String name = "Apache Isis ™";
                
                /**
                 * The version of the application, eg 1.0, 1.1, etc.
                 * If present, then this will be shown in the footer on every page as well as on the 
                 * about page.
                 */
                private String version;
            }
            
            private final BookmarkedPages bookmarkedPages = new BookmarkedPages();
            @Data
            public static class BookmarkedPages {
                /**
                 * Determines whether the bookmarks should be available in the header.
                 */
                private boolean showChooser = true;

                private int maxSize = 15;
            }

            private final Breadcrumbs breadcrumbs = new Breadcrumbs();
            @Data
            public static class Breadcrumbs {
                /**
                 * Determines whether the breadcrumbs should be available in the footer.
                 */
                private boolean showChooser = true;
            }


            /**
             * IntelliJ unfortunately does not provide IDE completion for lists of classes; YMMV.
             *
             * For further discussion, see for example this stackoverflow question:
             * https://stackoverflow.com/questions/41417933/spring-configuration-properties-metadata-json-for-nested-list-of-objects
             * and this wiki page: https://github.com/spring-projects/spring-boot/wiki/IDE-binding-features#simple-pojo and
             * 
             */
            private List<Credit> credit = new ArrayList<>();
            
            @Data
            public static class Credit {
                private String url;
                private String name;
                private String image;
                
                public boolean isDefined() { return (name != null || image != null) && url != null; }
            }
            
            private final DatePicker datePicker = new DatePicker();
            @Data
            public static class DatePicker {

                /**
                 * As per http://eonasdan.github.io/bootstrap-datetimepicker/Options/#maxdate, in ISO format (per https://github.com/moment/moment/issues/1407).
                 */
                @NotEmpty @NotNull
                private String minDate = "1900-01-01T00:00:00.000Z";

                /**
                 * As per http://eonasdan.github.io/bootstrap-datetimepicker/Options/#maxdate, in ISO format (per https://github.com/moment/moment/issues/1407).
                 */
                @NotEmpty @NotNull
                private String maxDate = "2100-01-01T00:00:00.000Z";
            }

            private final DevelopmentUtilities developmentUtilities = new DevelopmentUtilities();
            @Data
            public static class DevelopmentUtilities {

                /**
                 * Determines whether debug bar and other stuff influenced by <tt>org.apache.wicket.settings.DebugSettings#isDevelopmentUtilitiesEnabled()</tt> is enabled or not.
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
                private boolean suppress = false;
                private String cookieKey = "isisWicketRememberMe";
                private String encryptionKey;
            }

            private final Themes themes = new Themes();
            @Data
            public static class Themes {

                /**
                 * A comma separated list of enabled theme names, as defined by https://bootswatch.com.
                 */
                private List<String> enabled = new ArrayList<>();

                /**
                 * The initial theme to use.
                 *
                 * <p>
                 *     Expected to be in the list of {@link #getEnabled()} themes.
                 * </p>
                 */
                @NotEmpty @NotNull
                private String initial = "Flatly";

                @NotEmpty @NotNull
                private String provider = "org.apache.isis.viewer.wicket.ui.components.widgets.themepicker.IsisWicketThemeSupportDefault";

                /**
                 * Whether the theme chooser should be available in the footer.
                 */
                private boolean showChooser = false;
            }

            //TODO no meta data yet ... https://docs.spring.io/spring-boot/docs/current/reference/html/appendix-configuration-metadata.html#configuration-metadata-property-attributes
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
            
            
            private final WhereAmI whereAmI = new WhereAmI();
            @Data
            public static class WhereAmI {
                private boolean enabled = true;
                private int maxParentChainLength = 64;
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
                private String format;
            }

            // capitalized to avoid clash with keyword
            private final Double Double = new Double();
            @Data
            public static class Double {
                private String format;
            }

            // capitalized to avoid clash with keyword
            private final Float Float = new Float();
            @Data
            public static class Float {
                private String format;
            }

            // capitalized to avoid clash with keyword
            private final Long Long = new Long();
            @Data
            public static class Long {
                private String format;
            }

            // capitalized to avoid clash with keyword
            private final Short Short = new Short();
            @Data
            public static class Short {
                private String format;
            }
        }

        private final JavaMath javaMath = new JavaMath();
        @Data
        public static class JavaMath {
            private final BigInteger bigInteger = new BigInteger();
            @Data
            public static class BigInteger {
                private String format;
            }

            private final BigDecimal bigDecimal = new BigDecimal();
            @Data
            public static class BigDecimal {
                private String format;
            }
        }

        private final JavaTime javaTime = new JavaTime();
        @Data
        public static class JavaTime {
            private final LocalDateTime localDateTime = new LocalDateTime();
            @Data
            public static class LocalDateTime {
                private String format = "medium";
            }

            private final OffsetDateTime offsetDateTime = new OffsetDateTime();
            @Data
            public static class OffsetDateTime {
                private String format = "medium";
            }

            private final OffsetTime offsetTime = new OffsetTime();
            @Data
            public static class OffsetTime {
                private String format = "medium";
            }

            private final LocalDate localDate = new LocalDate();
            @Data
            public static class LocalDate {
                // lower case
                private String format = "medium";
            }

            private final LocalTime localTime = new LocalTime();
            @Data
            public static class LocalTime {
                private String format = "medium";
            }

            private final ZonedDateTime zonedDateTime = new ZonedDateTime();
            @Data
            public static class ZonedDateTime {
                private String format = "medium";
            }
        }

        private final JavaUtil javaUtil = new JavaUtil();
        @Data
        public static class JavaUtil {

            private final Date date = new Date();
            @Data
            public static class Date {
                // lower case
                private String format = "medium";
            }

        }

        private final JavaSql javaSql = new JavaSql();
        @Data
        public static class JavaSql {
            private final Date date = new Date();
            @Data
            public static class Date {
                // lower case
                private String format = "medium";
            }
            private final Time time = new Time();
            @Data
            public static class Time {
                // lower case
                private String format = "short";
            }

            private final Timestamp timestamp = new Timestamp();
            @Data
            public static class Timestamp {
                // lower case
                private String format = "short";
            }

        }

        private final Joda joda = new Joda();
        @Data
        public static class Joda {
            private final LocalDateTime localDateTime = new LocalDateTime();
            @Data
            public static class LocalDateTime {
                // lower case
                private String format = "medium";
            }

            private final LocalDate localDate = new LocalDate();
            @Data
            public static class LocalDate {
                // lower case
                private String format = "medium";
            }

            private final DateTime dateTime = new DateTime();
            @Data
            public static class DateTime {
                // lower case
                private String format = "medium";
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
                private String format;
            }

            private final Money money = new Money();
            @Data
            public static class Money {
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
            private List<String> allowedOrigins = listOf("*");
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
            private List<String> allowedMethods = listOf("GET","PUT","DELETE","POST","OPTIONS");
            private List<String> exposedHeaders = listOf("Authorization");
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
    private static Map<Pattern, String> asMap(String... mappings) {
        return new LinkedHashMap<>(Arrays.stream(mappings).map(mapping -> {
            final String[] parts = mapping.split(":");
            if (parts.length != 2) {
                return null;
            }
            try {
                return new PatternToString(Pattern.compile(parts[0]), parts[1]);
            } catch(Exception ex) {
                return null;
            }
        }).filter(Objects::nonNull)
        .collect(Collectors.toMap(PatternToString::getPattern, PatternToString::getString)));
    }


}
