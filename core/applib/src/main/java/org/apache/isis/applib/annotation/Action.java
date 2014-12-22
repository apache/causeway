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

package org.apache.isis.applib.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.services.eventbus.ActionInteractionEvent;
import org.apache.isis.applib.services.publish.EventPayload;
import org.apache.isis.applib.util.Enums;

/**
 * Domain semantics for domain object collection.
 */
@Inherited
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface Action {


    /**
     * Indicates that an invocation of the action should be posted to the
     * {@link org.apache.isis.applib.services.eventbus.EventBusService event bus} using a custom (subclass of)
     * {@link org.apache.isis.applib.services.eventbus.ActionInteractionEvent}.
     *
     * <p>For example:
     * </p>
     *
     * <pre>
     * public static class StartDateChangedEvent extends ActionInvokedEvent {}
     *
     * &#64;PostsActionInvokedEvent(StartDateChangedEvent.class)
     * public void changeStartDate(final Date startDate) { ...}
     * </pre>
     *
     * <p>
     * Only domain services should be registered as subscribers; only domain services are guaranteed to be instantiated and
     * resident in memory.  The typical implementation of a domain service subscriber is to identify the impacted entities,
     * load them using a repository, and then to delegate to the event to them.
     * </p>
     *
     * <p>
     * This subclass must provide a no-arg constructor; the fields are set reflectively.
     * </p>
     */
    Class<? extends ActionInteractionEvent<?>> interaction() default ActionInteractionEvent.Default.class;


    // //////////////////////////////////////

    /**
     * Indicates when the action is not visible to the user.
     *
     * <p>
     * For a repository action, is equivalent to {@link NotContributed} and also
     * {@link NotInServiceMenu}.
     * </p>
     *
     * <p>
     * If a repository action is contributed as a property, then the {@link Hidden#where() where}
     * attribute can be used to optionally hide the contributed property in a table (using
     * {@link Where#ALL_TABLES}, {@link Where#STANDALONE_TABLES}, {@link Where#PARENTED_TABLES}
     * as appropriate).
     * </p>
     */
    Where hidden() default Where.NOWHERE;


    // //////////////////////////////////////


    /**
     * Indicates when an action is not invokable by the user.
     */
    Where disabled() default Where.NOWHERE;


    /**
     * If {@link #disabled()} (in any {@link org.apache.isis.applib.annotation.Where} context), then the reason to provide to the user as to why the
     * collection cannot be edited.
     * @return
     */
    String disabledReason();


    // //////////////////////////////////////

    enum Semantics {

        /**
         * Safe, with no side-effects.
         *
         * <p>
         * In other words, a query-only action.  By definition, is also idempotent.
         */
        SAFE,
        /**
         * Post-conditions are always the same, irrespective as to how many times called.
         *
         * <p>
         * An example might be <tt>placeOrder()</tt>, that is a no-op if the order has already been placed.
         */
        IDEMPOTENT,
        /**
         * Neither safe nor idempotent; every invocation is likely to change the state of the object.
         *
         * <p>
         * An example is increasing the quantity of a line item in an Order by 1.
         */
        NON_IDEMPOTENT;

        public String getFriendlyName() {
            return Enums.getFriendlyNameOf(this);
        }

        public String getCamelCaseName() {
            return Enums.enumToCamelCase(this);
        }

        /**
         * {@link #SAFE} is idempotent in nature, as well as, obviously, {@link #IDEMPOTENT}.
         */
        public boolean isIdempotentInNature() {
            return this == SAFE || this == IDEMPOTENT;
        }

        public boolean isSafe() {
            return this == SAFE;
        }

        @Deprecated
        public static ActionSemantics.Of from(final Semantics semantics) {
            if(semantics == SAFE) return ActionSemantics.Of.SAFE;
            if(semantics == IDEMPOTENT) return ActionSemantics.Of.IDEMPOTENT;
            if(semantics == NON_IDEMPOTENT) return ActionSemantics.Of.NON_IDEMPOTENT;
            // shouldn't happen
            throw new IllegalArgumentException("Unrecognized of: " + semantics);
        }

        @Deprecated
        public static Semantics from(final ActionSemantics.Of semantics) {
            if(semantics == ActionSemantics.Of.SAFE) return SAFE;
            if(semantics == ActionSemantics.Of.IDEMPOTENT) return IDEMPOTENT;
            if(semantics == ActionSemantics.Of.NON_IDEMPOTENT) return NON_IDEMPOTENT;
            // shouldn't happen
            throw new IllegalArgumentException("Unrecognized semantics: " + semantics);
        }
    }


    /**
     * The action semantics, either {@link org.apache.isis.applib.annotation.Action.Semantics#SAFE safe} (query-only),
     * {@link org.apache.isis.applib.annotation.Action.Semantics#IDEMPOTENT idempotent} or
     * {@link org.apache.isis.applib.annotation.Action.Semantics#NON_IDEMPOTENT non-idempotent}.
     */
    Semantics semantics() default Semantics.NON_IDEMPOTENT;


    // //////////////////////////////////////


    enum BulkAppliesTo {
        BULK_AND_REGULAR,
        BULK_ONLY;

        @Deprecated
        public static Bulk.AppliesTo from(final BulkAppliesTo bulkAppliesTo) {
            if(bulkAppliesTo == BULK_AND_REGULAR) return Bulk.AppliesTo.BULK_AND_REGULAR;
            if(bulkAppliesTo == BULK_ONLY) return Bulk.AppliesTo.BULK_ONLY;
            // shouldn't happen
            throw new IllegalArgumentException("Unrecognized appliesTo: " + bulkAppliesTo);
        }

        @Deprecated
        public static BulkAppliesTo from(final Bulk.AppliesTo appliesTo) {
            if(appliesTo == Bulk.AppliesTo.BULK_AND_REGULAR) return BULK_AND_REGULAR;
            if(appliesTo == Bulk.AppliesTo.BULK_ONLY) return BULK_ONLY;
            // shouldn't happen
            throw new IllegalArgumentException("Unrecognized appliesTo: " + appliesTo);
        }
    }

    /**
     * Indicates the (entity) action should be used only against many objects in a collection.
     *
     * <p>
     * Bulk actions have a number of constraints:
     * <ul>
     * <li>It must take no arguments
     * <li>It cannot be hidden (any annotations or supporting methods to that effect will be
     *     ignored).
     * <li>It cannot be disabled (any annotations or supporting methods to that effect will be
     *     ignored).
     * </ul>
     *
     * <p>
     * Has no meaning if annotated on an action of a domain service.
     */
    BulkAppliesTo bulk() default BulkAppliesTo.BULK_AND_REGULAR;


    // //////////////////////////////////////

    enum CommandExecuteIn {
        /**
         * Execute synchronously in the &quot;foreground&quot;, wait for the results.
         */
        FOREGROUND,
        /**
         * Execute &quot;asynchronously&quot; through the {@link org.apache.isis.applib.services.background.BackgroundCommandService}, returning (if possible) the
         * persisted {@link org.apache.isis.applib.services.command.Command command} object as a placeholder to the
         * result.
         */
        BACKGROUND;

        @Deprecated
        public static CommandExecuteIn from(final Command.ExecuteIn executeIn) {
            if(executeIn == Command.ExecuteIn.FOREGROUND) return FOREGROUND;
            if(executeIn == Command.ExecuteIn.BACKGROUND) return BACKGROUND;
            // shouldn't happen
            throw new IllegalArgumentException("Unrecognized : executeIn" + executeIn);
        }

        @Deprecated
        public static Command.ExecuteIn from(final CommandExecuteIn commandExecuteIn) {
            if(commandExecuteIn == FOREGROUND) return Command.ExecuteIn.FOREGROUND;
            if(commandExecuteIn == BACKGROUND) return Command.ExecuteIn.BACKGROUND;
            // shouldn't happen
            throw new IllegalArgumentException("Unrecognized : executeIn" + commandExecuteIn);
        }

    }

    enum CommandPersistence {
        /**
         * (If the configured {@link org.apache.isis.applib.services.command.spi.CommandService} supports it), indicates that the
         * {@link org.apache.isis.applib.services.command.Command Command} object should be persisted.
         */
        PERSISTED,
        /**
         * (If the configured {@link org.apache.isis.applib.services.command.spi.CommandService} supports it), indicates that the
         * {@link org.apache.isis.applib.services.command.Command Command} object should only be persisted if
         * another service, such as the {@link org.apache.isis.applib.services.background.BackgroundCommandService}, hints that it should.
         */
        IF_HINTED,
        /**
         * (Even if the configured {@link org.apache.isis.applib.services.command.spi.CommandService} supports it), indicates that the
         * {@link org.apache.isis.applib.services.command.Command Command} object should <i>not</i> be persisted (even if
         * another service, such as the {@link org.apache.isis.applib.services.background.BackgroundCommandService}, hints that it should).
         */
        NOT_PERSISTED;

        @Deprecated
        public static CommandPersistence from(final Command.Persistence persistence) {
            if(persistence == Command.Persistence.PERSISTED) return PERSISTED;
            if(persistence == Command.Persistence.IF_HINTED) return IF_HINTED;
            // shouldn't happen
            throw new IllegalArgumentException("Unrecognized : persistence" + persistence);
        }

        @Deprecated
        public static Command.Persistence from(final CommandPersistence commandPersistence) {
            if(commandPersistence == PERSISTED) return Command.Persistence.PERSISTED;
            if(commandPersistence == IF_HINTED) return Command.Persistence.IF_HINTED;
            // shouldn't happen
            throw new IllegalArgumentException("Unrecognized : persistence" + commandPersistence);
        }
    }


    boolean command() default false;

    /**
     * How the {@link org.apache.isis.applib.services.command.Command Command} object provided by the
     * {@link org.apache.isis.applib.services.command.CommandContext CommandContext} domain service should be persisted.
     */
    CommandPersistence commandPersistence() default CommandPersistence.PERSISTED;

    /**
     * How the command/action should be executed.
     *
     * <p>
     * If the corresponding {@link org.apache.isis.applib.services.command.Command Command} object is persisted,
     * then its {@link org.apache.isis.applib.services.command.Command#getExecuteIn() invocationType} property
     * will be set to this value.
     * </p>
     */
    CommandExecuteIn commandExecuteIn() default CommandExecuteIn.FOREGROUND;

    /**
     * If set to <tt>true</tt>, acts as an override to <i>disable</i> command semantics for the action when it is
     * otherwise (eg through configuration) configured as the default.
     */
    boolean commandDisabled() default false;


    // //////////////////////////////////////

    interface PublishingPayloadFactory {

        @Programmatic
        public EventPayload payloadFor(Identifier actionIdentifier, Object target, List<Object> arguments, Object result);

        /**
         * Adapter to subclass if have an existing {@link org.apache.isis.applib.annotation.PublishedObject.PayloadFactory}.
         */
        @Deprecated
        public abstract class Adapter implements PublishingPayloadFactory {

            private final PublishedAction.PayloadFactory payloadFactory;

            public Adapter(final PublishedAction.PayloadFactory payloadFactory) {
                this.payloadFactory = payloadFactory;
            }

            @Override
            public EventPayload payloadFor(Identifier actionIdentifier, Object target, List<Object> arguments, Object result) {
                return payloadFactory.payloadFor(actionIdentifier, target, arguments, result);
            }
        }
    }


    // TODO: factor out PayloadFactory.Default so similar to interaction
    Class<? extends PublishingPayloadFactory> publishingPayloadFactory() default PublishingPayloadFactory.class;



}