package org.apache.isis.applib.services.wrapper.control;

import org.apache.isis.applib.services.wrapper.WrapperFactory;
import org.apache.isis.core.commons.collections.ImmutableEnumSet;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

/**
 * Whether interactions with the wrapper are actually passed onto the
 * underlying domain object.
 *
 * @see WrapperFactory#wrap(Object, ImmutableEnumSet)
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
// tag::refguide[]
public enum ExecutionMode {
    // end::refguide[]
    /**
     * Skip all business rules.
     */
    // tag::refguide[]
    SKIP_RULE_VALIDATION,
    // end::refguide[]
    /**
     * Skip execution.
     */
    // tag::refguide[]
    SKIP_EXECUTION,
    // end::refguide[]
    /**
     * Don't fail fast, swallow any exception during validation or execution.
     */
    // tag::refguide[]
    SWALLOW_EXCEPTIONS,
}
// end::refguide[]
