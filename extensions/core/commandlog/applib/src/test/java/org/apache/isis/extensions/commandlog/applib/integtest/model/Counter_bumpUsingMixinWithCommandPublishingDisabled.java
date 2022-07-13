package org.apache.isis.extensions.commandlog.applib.integtest.model;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.Publishing;

import lombok.RequiredArgsConstructor;

@Action(commandPublishing = Publishing.DISABLED)
@RequiredArgsConstructor
public class Counter_bumpUsingMixinWithCommandPublishingDisabled {

    private final Counter counter;

    public Counter act() {
        return counter.doBump();
    }
}
