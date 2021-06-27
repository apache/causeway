package org.apache.isis.testing.unittestsupport.applib.dom.pojo.holders;

import java.time.LocalTime;

import lombok.Getter;

@Getter
public class JavaLocalTimeHolder extends HolderAbstract<JavaLocalTimeHolder> {
    private LocalTime localTime;
    public void setLocalTime(LocalTime localTime) {
        bump();
        this.localTime = broken ? null : localTime;
    }
}
