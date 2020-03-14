package org.apache.isis.applib.services.wrapper.control;

import org.apache.isis.core.commons.collections.ImmutableEnumSet;

import static org.apache.isis.core.commons.collections.ImmutableEnumSet.noneOf;
import static org.apache.isis.core.commons.collections.ImmutableEnumSet.of;

import lombok.experimental.UtilityClass;

// tag::refguide[]
@UtilityClass
public class ExecutionModes {
    // end::refguide[]
    /**
     * Validate all business rules and then execute. May throw exceptions in order to fail fast.
     */
    // tag::refguide[]
    public static final ImmutableEnumSet<ExecutionMode> EXECUTE =
                            noneOf(ExecutionMode.class);
    // end::refguide[]
    /**
     * Skip all business rules and then execute, does throw an exception if execution fails.
     */
    // tag::refguide[]
    public static final ImmutableEnumSet<ExecutionMode> SKIP_RULES =
                            of(ExecutionMode.SKIP_RULE_VALIDATION);
    // end::refguide[]
    /**
     * Validate all business rules but do not execute, throw an exception if validation
     * fails.
     */
    // tag::refguide[]
    public static final ImmutableEnumSet<ExecutionMode> NO_EXECUTE =
                            of(ExecutionMode.SKIP_EXECUTION);
    // end::refguide[]
    /**
     * Validate all business rules and then execute, but don't throw an exception if validation
     * or execution fails.
     */
    // tag::refguide[]
    public static final ImmutableEnumSet<ExecutionMode> TRY =
                            of(ExecutionMode.SWALLOW_EXCEPTIONS);
    // end::refguide[]
    /**
     * Skips all steps.
     * @since 2.0
     */
    // tag::refguide[]
    public static final ImmutableEnumSet<ExecutionMode> NOOP =
                            of(ExecutionMode.SKIP_RULE_VALIDATION,
                               ExecutionMode.SKIP_EXECUTION);
}
// end::refguide[]
