package org.apache.isis.testing.unittestsupport.applib.dom.pojo.holders;

import lombok.Getter;

@Getter
public class DoubleWrapperHolder extends HolderAbstract<DoubleWrapperHolder> {
    private Double someDoubleWrapper;

    public void setSomeDoubleWrapper(Double someDoubleWrapper) {
        bump();
        this.someDoubleWrapper = broken ? 0.0d : someDoubleWrapper;
    }
}
