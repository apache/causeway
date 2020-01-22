package org.apache.isis.extensions.fullcalendar.ui.component;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import org.apache.isis.extensions.fullcalendar.applib.IsisModuleExtFullCalendarApplib;

@Configuration
@Import({
        IsisModuleExtFullCalendarApplib.class
})
public class IsisModuleExtFullCalendarUi {
}
