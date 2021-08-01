package org.apache.isis.extensions.secman.applib.mmm;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import org.apache.isis.extensions.secman.applib.mmm.dom.ChaseCatsService;
import org.apache.isis.extensions.secman.applib.mmm.dom.EatCheeseService;
import org.apache.isis.extensions.secman.applib.mmm.dom.PressLiftButtonService;
import org.apache.isis.extensions.secman.applib.mmm.dom.RideLiftService;

@Configuration
@Import({
        PressLiftButtonService.class,
        RideLiftService.class,
        ChaseCatsService.class,
        EatCheeseService.class,
})
public class MmmModule {
}
