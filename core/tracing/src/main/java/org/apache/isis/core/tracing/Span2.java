package org.apache.isis.core.tracing;

import io.opentracing.Span;

public interface Span2 extends Span {

    String START_TAG = "_in";
    String FINISH_TAG = "_out";

    /**
     * Indicates whether a nested scope/span has overridden the operation of a higher level (usually root) scope/span.
     * 
     * If so, this tag is set and the operation cannot be specified again.
     * 
     * @see TraceScopeManager#rootOperation(String).
     */
    String ROOT_OPERATION_TAG = "isis.root.operation";

    boolean isFinished();

    Scope2 scope();

    boolean hasTag(String tag);
}
