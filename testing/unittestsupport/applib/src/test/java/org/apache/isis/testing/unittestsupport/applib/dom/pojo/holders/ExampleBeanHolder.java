package org.apache.isis.testing.unittestsupport.applib.dom.pojo.holders;

import lombok.Getter;

@Getter
public class ExampleBeanHolder extends HolderAbstract<ExampleBeanHolder> {
    private ExampleBean exampleBean;
    public void setExampleBean(ExampleBean exampleBean) {
        bump();
        this.exampleBean = broken ? null : exampleBean;
    }
}
