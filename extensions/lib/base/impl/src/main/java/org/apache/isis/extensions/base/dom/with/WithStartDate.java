package org.apache.isis.extensions.base.dom.with;

import org.apache.isis.applib.annotation.Editing;
import org.apache.isis.applib.annotation.Property;
import org.joda.time.LocalDate;

public interface WithStartDate {

    @Property(editing = Editing.DISABLED)
    public LocalDate getStartDate();
    public void setStartDate(LocalDate startDate);

}
