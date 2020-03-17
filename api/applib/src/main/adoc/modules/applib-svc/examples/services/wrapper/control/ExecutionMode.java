package org.apache.isis.applib.services.wrapper.control;

import java.util.function.Consumer;

import org.apache.isis.applib.services.wrapper.WrapperFactory;
import org.apache.isis.applib.services.wrapper.control.AsyncControl;
import org.apache.isis.core.commons.collections.ImmutableEnumSet;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

/**
 * Whether interactions with the wrapper are actually passed onto the
 * underlying domain object.
 *
 * @see WrapperFactory#wrap(Object, org.apache.isis.applib.services.wrapper.control.SyncControl)
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
     * Skip actual execution.
     *
     * <p>
     * This is not supported for {@link WrapperFactory#async(Object, AsyncControl)}; instead just invoke {@link WrapperFactory#wrap(Object, ImmutableEnumSet)}.
     */
    // tag::refguide[]
    SKIP_EXECUTION,
    // end::refguide[]
}
// end::refguide[]
