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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import org.apache.isis.applib.annotation.LabelPosition;
import org.apache.isis.applib.annotation.PromptStyle;
import org.apache.isis.applib.services.i18n.TranslationService;
import org.apache.isis.core.commons.internal.base._Strings;
import org.apache.isis.core.commons.internal.collections._Maps;
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
            @Data
            public static class DomainObject {

                private AuditObjectsConfiguration auditing = AuditObjectsConfiguration.NONE;

                private EditingObjectsConfiguration editing = EditingObjectsConfiguration.TRUE;

                private PublishObjectsConfiguration publishing = PublishObjectsConfiguration.NONE;

                private final CreatedLifecycleEvent createdLifecycleEvent = new CreatedLifecycleEvent();
                @Data
                public static class CreatedLifecycleEvent {
                    private boolean postForDefault = true;
                }

                private final LoadedLifecycleEvent loadedLifecycleEvent = new LoadedLifecycleEvent();
                @Data
                public static class LoadedLifecycleEvent {
                    private boolean postForDefault = true;
                }

                private final PersistingLifecycleEvent persistingLifecycleEvent = new PersistingLifecycleEvent();
                @Data
                public static class PersistingLifecycleEvent {
                    private boolean postForDefault = true;
                }

                private final PersistedLifecycleEvent persistedLifecycleEvent = new PersistedLifecycleEvent();
                @Data
                public static class PersistedLifecycleEvent {
                    private boolean postForDefault = true;
                }

                private final RemovingLifecycleEvent removingLifecycleEvent = new RemovingLifecycleEvent();
                @Data
                public static class RemovingLifecycleEvent {
                    private boolean postForDefault = true;
                }

                private final UpdatedLifecycleEvent updatedLifecycleEvent = new UpdatedLifecycleEvent();
                @Data
                public static class UpdatedLifecycleEvent {
                    private boolean postForDefault = true;
                }

                private final UpdatingLifecycleEvent updatingLifecycleEvent = new UpdatingLifecycleEvent();
                @Data
                public static class UpdatingLifecycleEvent {
                    private boolean postForDefault = true;
                }

            }

            private final DomainObjectLayout domainObjectLayout = new DomainObjectLayout();
            @Data
            public static class DomainObjectLayout {

                private final CssClassUiEvent cssClassUiEvent = new CssClassUiEvent();
                @Data
                public static class CssClassUiEvent {
                    private boolean postForDefault = true;
                }

                private final IconUiEvent iconUiEvent = new IconUiEvent();
                @Data
                public static class IconUiEvent {
                    private boolean postForDefault = true;
                }

                private final LayoutUiEvent layoutUiEvent = new LayoutUiEvent();
                @Data
                public static class LayoutUiEvent {
                    private boolean postForDefault = true;
                }

                private final TitleUiEvent titleUiEvent = new TitleUiEvent();
                @Data
                public static class TitleUiEvent {
                    private boolean postForDefault = true;
                }

            }

            private final Action action = new Action();
            @Data
            public static class Action {

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
                    private boolean postForDefault = true;
                }

                private PublishActionsConfiguration publishing = PublishActionsConfiguration.NONE;

            }

            private final ActionLayout actionLayout = new ActionLayout();
            @Data
            public static class ActionLayout {

                private final CssClass cssClass = new CssClass();
                @Data
                public static class CssClass {
                    private Map<Pattern, String> patterns = new HashMap<>();
                }

                private final CssClassFa cssClassFa = new CssClassFa();
                @Data
                public static class CssClassFa {
                    private Map<Pattern, String> patterns = new HashMap<>();
                }
            }

            private final Property property = new Property();
            @Data
            public static class Property {

                private CommandPropertiesConfiguration command = CommandPropertiesConfiguration.NONE;

                private PublishPropertiesConfiguration publishing = PublishPropertiesConfiguration.NONE;

                private final DomainEvent domainEvent = new DomainEvent();
                @Data
                public static class DomainEvent {
                    private boolean postForDefault = true;
                }

            }

            private final Collection collection = new Collection();
            @Data
            public static class Collection {

                private final DomainEvent domainEvent = new DomainEvent();
                @Data
                public static class DomainEvent {
                    private boolean postForDefault = true;
                }
            }

            private final CollectionLayout collectionLayout = new CollectionLayout();
            @Data
            public static class CollectionLayout {
                private DefaultViewConfiguration defaultView = DefaultViewConfiguration.HIDDEN;
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
                    private boolean postForDefault =true;
                }

                private final IconUiEvent iconUiEvent = new IconUiEvent();
                @Data
                public static class IconUiEvent {
                    private boolean postForDefault =true;
                }

                private final LayoutUiEvent layoutUiEvent = new LayoutUiEvent();
                @Data
                public static class LayoutUiEvent {
                    private boolean postForDefault =true;
                }

                private final TitleUiEvent titleUiEvent = new TitleUiEvent();
                @Data
                public static class TitleUiEvent {
                    private boolean postForDefault =true;
                }
            }
        }
    }

    private final Core core = new Core();
    @Data
    public static class Core {

        private final MetaModel metaModel = new MetaModel();
        @Data
        public static class MetaModel {

            private boolean filterVisibility = true;

            private final ProgrammingModel programmingModel = new ProgrammingModel();
            @Data
            public static class ProgrammingModel {
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

                private boolean allowDeprecated = true;
                private boolean ensureUniqueObjectTypes = true;
                private boolean checkModuleExtent = true;
                private boolean noParamsOnly = false;
                private boolean actionCollectionParameterChoices = true;

                @Deprecated
                private boolean serviceActionsOnly = true;
                @Deprecated
                private boolean mixinsOnly = true;

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
                    private String to;
                    private String cc;
                    private String bcc;
                }

                private final Sender sender = new Sender();
                @Data
                public static class Sender {
                    private String hostname;
                    private String username;
                    private String password;
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
                     *     See also <tt>additional-spring-configuration-metadata.json</tt> (change casing).
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
                     *     See also <tt>additional-spring-configuration-metadata.json</tt> (change casing).
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
                     *     See also <tt>additional-spring-configuration-metadata.json</tt> (change casing).
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
                     *     See also <tt>additional-spring-configuration-metadata.json</tt> (change casing).
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
                     *     See also <tt>additional-spring-configuration-metadata.json</tt> (change casing).
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
                            private String type = "none";
                        }
                    }
                    private final ObjectProvider objectProvider = new ObjectProvider();
                    @Data
                    public static class ObjectProvider {
                        /**
                         * New feature in DN 3.2.3; enables dependency injection into entities
                         *
                         * <p>
                         *     See also <tt>additional-spring-configuration-metadata.json</tt> (change casing).
                         * </p>
                         */
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
                         *     See also <tt>additional-spring-configuration-metadata.json</tt> (change casing).
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
                         *     See also <tt>additional-spring-configuration-metadata.json</tt> (change casing).
                         * </p>
                         *
                         * @implNote - this config property isn't used by the framework, but is provided as a convenience for IDE autocomplete.
                         */
                        private boolean autoCreateDatabase = false;

                        /**
                         * <p>
                         *     See also <tt>additional-spring-configuration-metadata.json</tt> (change casing).
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
                         *     See also <tt>additional-spring-configuration-metadata.json</tt> (change casing).
                         * </p>
                         *
                         * @implNote - changing this property from its default is used to enable the flyway extension (in combination with {@link Datanucleus.Schema#isAutoCreateAll()}
                         */
                        private String persistenceManagerFactoryClass = "org.datanucleus.api.jdo.JDOPersistenceManagerFactory";

                        private final Option option = new Option();
                        @Data
                        public static class Option {
                            /**
                             * JDBC driver used by DataNucleus Object store to connect.
                             *
                             * <p>
                             *     See also <tt>additional-spring-configuration-metadata.json</tt> (change casing).
                             * </p>
                             *
                             * @implNote - this config property isn't used by the framework, but provided as a convenience for IDE autocomplete (and is mandatory if using JDO Datanucleus).
                             */
                            private String connectionDriverName;
                            /**
                             * URL used by DataNucleus Object store to connect.
                             *
                             * <p>
                             *     See also <tt>additional-spring-configuration-metadata.json</tt> (change casing).
                             * </p>
                             *
                             * @implNote - some extensions (H2Console, MsqlDbManager) peek at this URL to determine if they should be enabled.  Note that it is also mandatory if using JDO Datanucleus.
                             */
                            private String connectionUrl;
                            /**
                             * User account used by DataNucleus Object store to connect.
                             *
                             * <p>
                             *     See also <tt>additional-spring-configuration-metadata.json</tt> (change casing).
                             * </p>
                             *
                             * @implNote - this config property isn't used by the framework, but provided as a convenience for IDE autocomplete (and is mandatory if using JDO Datanucleus).
                             */
                            private String connectionUserName;
                            /**
                             * Password for the user account used by DataNucleus Object store to connect.
                             *
                             * <p>
                             *     See also <tt>additional-spring-configuration-metadata.json</tt> (change casing).
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

            @javax.validation.constraints.Pattern(regexp="^[/].*[/]$")
            private String basePath = "/wicket/";

            private boolean clearOriginalDestination = false;

            /**
             * The pattern used for rendering and parsing dates.
             *
             * <p>
             * Each Date scalar panel will use {@ #getDatePattern()} or {@linkplain #getDateTimePattern()} depending on its
             * date type.  In the case of panels with a date picker, the pattern will be dynamically adjusted so that it can be
             * used by the <a href="https://github.com/Eonasdan/bootstrap-datetimepicker">Bootstrap Datetime Picker</a>
             * component (which uses <a href="http://momentjs.com/docs/#/parsing/string-format/">Moment.js formats</a>, rather
             * than those of regular Java code).
             */
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
            private String dateTimePattern = "dd-MM-yyyy HH:mm";

            private DialogMode dialogMode = DialogMode.SIDEBAR;

            private DialogMode dialogModeForMenu = DialogMode.MODAL;

            private String liveReloadUrl;

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
                 * Label used on the about page. If not specified, then {@link Application#name} 
                 * is used instead.
                 */
                private String about;
                
                /**
                 * Either the location of the image file (relative to the class-path resource root), 
                 * or an absolute URL.
                 * This is rendered on the header panel. An image with a size of 160x40 works well.
                 * If not specified, the application.name is used instead.
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
                
                // since 2.0
                private String menubarsLayoutXml = "menubars.layout.xml"; 
                
                /**
                 * Identifies the application on the sign-in page
                 * (unless a {@link Application#brandLogoSignin} image is configured) and 
                 * on top-left in the header 
                 * (unless a {@link Application#brandLogoHeader} image is configured).
                 */
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
                private String minDate = "1900-01-01T00:00:00.000Z";

                /**
                 * As per http://eonasdan.github.io/bootstrap-datetimepicker/Options/#maxdate, in ISO format (per https://github.com/moment/moment/issues/1407).
                 */
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
                private String initial = "Flatly";

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

    private final Viewers viewers = new Viewers();
    @Data
    public static class Viewers {


        private final Paged paged = new Paged();
        @Data
        public static class Paged {
            private int parented = 12;
            private int standalone = 25;
        }

        private final ParameterLayout parameterLayout = new ParameterLayout();
        @Data
        public static class ParameterLayout implements ConfigPropsForPropertyOrParameterLayout {
            private LabelPosition labelPosition = LabelPosition.NOT_SPECIFIED;
            private LabelPosition label = LabelPosition.NOT_SPECIFIED;
        }
        private final PropertyLayout propertyLayout = new PropertyLayout();
        @Data
        public static class PropertyLayout implements ConfigPropsForPropertyOrParameterLayout {
            private LabelPosition labelPosition = LabelPosition.NOT_SPECIFIED;
            private LabelPosition label = LabelPosition.NOT_SPECIFIED;
        }

        public interface ConfigPropsForPropertyOrParameterLayout {
            public LabelPosition getLabelPosition();

            /**
             * Alias for {@link #getLabelPosition()}
             */
            public LabelPosition getLabel();
        }
    }


    //TODO no meta data yet ... https://docs.spring.io/spring-boot/docs/current/reference/html/appendix-configuration-metadata.html#configuration-metadata-property-attributes
    private String timezone;
    
    //TODO no meta data yet ... https://docs.spring.io/spring-boot/docs/current/reference/html/appendix-configuration-metadata.html#configuration-metadata-property-attributes
    private final Value value = new Value();
    @Data
    public static class Value {
        
        private Map<String, String> format = new HashMap<>();
        
        public enum FormatIdentifier {
            /**
             * Key to indicate how LocalDateTime should be parsed/rendered.
             * <p>
             * eg: {@code isis.value.format.datetime=iso}
             * <p>
             * A pre-determined list of values is available, specifically 'iso_encoding', 'iso' and 'medium' (see
             * <code>org.apache.isis.core.metamodel.facets.value.datetimejdk8local.Jdk8LocalDateTimeValueSemanticsProvider#NAMED_TITLE_FORMATTERS</code>).
             * Alternatively, can also specify a mask, eg <tt>dd-MMM-yyyy</tt>.
             */
            DATETIME,
            /**
             * Key to indicate how LocalDate should be parsed/rendered.
             * <p>
             * eg: {@code isis.value.format.date=iso}
             * <p>
             * A pre-determined list of values is available, specifically 'iso_encoding', 'iso' and 'medium' (see
             * <code>org.apache.isis.core.metamodel.facets.value.datejdk8local.Jdk8LocalDateValueSemanticsProvider.NAMED_TITLE_FORMATTERS</code>).
             * Alternatively,  can also specify a mask, eg <tt>dd-MMM-yyyy</tt>.
             */
            DATE, 
            TIMESTAMP, 
            TIME,
            
            INT, DECIMAL, BYTE, DOUBLE, FLOAT, LONG, SHORT, 
            PERCENTAGE
        }
        
        public String getFormatOrElse(FormatIdentifier formatIdentifier, String defaultFormat) {
            return format.getOrDefault(formatIdentifier.name().toLowerCase(), defaultFormat);
        }
        
        private final Money money = new Money();
        @Data
        public static class Money {
            
            private Optional<String> currency = Optional.empty();

            public String getCurrencyOrElse(String fallback) { 
                return getCurrency().filter(_Strings::isNotEmpty).orElse(fallback);
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


}
