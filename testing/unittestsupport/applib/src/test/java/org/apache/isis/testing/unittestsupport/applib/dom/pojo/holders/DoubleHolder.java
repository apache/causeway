package org.apache.isis.testing.unittestsupport.applib.dom.pojo.holders;

import lombok.Getter;

@Getter
public class DoubleHolder extends HolderAbstract<DoubleHolder> {
    private double someDouble;

    public void setSomeDouble(double someDouble) {
        bump();
        this.someDouble = broken ? 0.0d : someDouble;
    }
}
