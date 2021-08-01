package org.apache.isis.extensions.secman.applib.mmm.dom;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.DomainService;

@DomainService(logicalTypeName = "mmm.PressLiftButtonService")
public class PressLiftButtonService {
    @Action
    public void pressLiftButton() {}
}
