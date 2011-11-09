package org.apache.isis.examples.webshop.dom.catalog.categories;

import org.apache.isis.applib.AbstractDomainObject;
import org.apache.isis.applib.annotation.Bounded;
import org.apache.isis.applib.annotation.Disabled;
import org.apache.isis.applib.annotation.Ignore;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Title;

import com.google.common.base.Objects;

@Bounded
public class ProductCategory extends AbstractDomainObject {

    // {{ Code
    private String code;

    @MemberOrder(sequence = "1.0")
    @Disabled
    public String getCode() {
        return code;
    }

    public void setCode(final String code) {
        this.code = code;
    }


    @Ignore
    public boolean hasCode(String code) {
        return Objects.equal(code, getCode());
    }
    // }}

	// {{ Name (title)
	private String name;

	@MemberOrder(sequence = "2.0")
	@Title
    @Disabled
	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}
	// }}
	
}
