package org.apache.isis.testing.unittestsupport.applib.dom.pojo.holders;

import lombok.Getter;

@Getter
public class FloatHolder extends HolderAbstract<FloatHolder> {
    private float someFloat;

    public void setSomeFloat(float someFloat) {
        bump();
        this.someFloat = broken ? 0.0f : someFloat;
    }
}
