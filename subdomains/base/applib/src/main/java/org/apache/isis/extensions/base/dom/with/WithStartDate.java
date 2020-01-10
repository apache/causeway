package org.apache.isis.extensions.base.dom.with;

import org.joda.time.LocalDate;

import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.Property;

public interface WithStartDate {

    @Property(editing = Editing.DISABLED)
    public LocalDate getStartDate();
    public void setStartDate(LocalDate startDate);

}
