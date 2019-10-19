package org.apache.isis.core.tracing;

import io.opentracing.Scope;

public interface Scope2 extends Scope {

    Span2 span();

    void closeAndFinish();

    boolean isClosed();
}