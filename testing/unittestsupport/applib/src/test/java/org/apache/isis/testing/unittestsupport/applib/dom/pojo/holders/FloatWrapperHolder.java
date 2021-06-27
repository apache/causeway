package org.apache.isis.testing.unittestsupport.applib.dom.pojo.holders;

import lombok.Getter;

@Getter
public class FloatWrapperHolder extends HolderAbstract<FloatWrapperHolder> {
    private Float someFloatWrapper;

    public void setSomeFloatWrapper(Float someFloatWrapper) {
        bump();
        this.someFloatWrapper = broken ? 0.0f : someFloatWrapper;
    }
}
