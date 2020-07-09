package org.apache.isis.testdomain.model.interaction;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.extensions.modelannotation.applib.annotation.Model;

import lombok.RequiredArgsConstructor;

@Action
@RequiredArgsConstructor
public class InteractionDemo_noArgEnabled {

    private final InteractionDemo holder;
    
    @Model
    public Integer act() {
        return 99;
    }
    
}
