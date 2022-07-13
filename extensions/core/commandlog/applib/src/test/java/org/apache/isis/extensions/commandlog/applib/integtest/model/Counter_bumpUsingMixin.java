package org.apache.isis.extensions.commandlog.applib.integtest.model;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.Publishing;

import lombok.RequiredArgsConstructor;

@Action(commandPublishing = Publishing.ENABLED)
@RequiredArgsConstructor
public class Counter_bumpUsingMixin {

    private final Counter counter;

    public Counter act() {
        return counter.doBump();
    }
}
