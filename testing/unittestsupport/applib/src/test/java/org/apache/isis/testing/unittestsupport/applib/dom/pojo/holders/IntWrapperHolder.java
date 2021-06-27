package org.apache.isis.testing.unittestsupport.applib.dom.pojo.holders;

import lombok.Getter;

@Getter
public class IntWrapperHolder extends HolderAbstract<IntWrapperHolder> {
    private Integer someIntWrapper;

    public void setSomeIntWrapper(Integer someIntWrapper) {
        bump();
        this.someIntWrapper = broken ? 0 : someIntWrapper;
    }
}
