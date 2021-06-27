package org.apache.isis.testing.unittestsupport.applib.dom.pojo.holders;

import org.joda.time.LocalTime;

import lombok.Getter;

@Getter
public class JodaLocalTimeHolder extends HolderAbstract<JodaLocalTimeHolder> {
    private LocalTime localTime;
    public void setLocalTime(LocalTime localTime) {
        bump();
        this.localTime = broken ? null : localTime;
    }
}
