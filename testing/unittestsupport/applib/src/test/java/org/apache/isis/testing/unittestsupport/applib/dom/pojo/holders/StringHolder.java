package org.apache.isis.testing.unittestsupport.applib.dom.pojo.holders;

import lombok.Getter;

@Getter
public class StringHolder extends HolderAbstract<StringHolder> {
    private String someString;
    public void setSomeString(String someString) {
        bump();
        this.someString = broken ? null : someString;
    }
}
