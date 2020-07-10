package demoapp.dom.extensions.secman.spiimpl;

import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

import org.apache.isis.extensions.secman.api.tenancy.ApplicationTenancyEvaluator;
import org.apache.isis.extensions.secman.api.user.ApplicationUser;

import lombok.Getter;
import lombok.val;

import demoapp.dom.extensions.secman.entities.TenantedJdo;

//tag::class[]
@Service
public class ApplicationTenancyEvaluatorForDemo
                implements ApplicationTenancyEvaluator {                            // <.>

    @Override
    public boolean handles(Class<?> cls) {                                          // <.>
        return TenantedJdo.class.isAssignableFrom(cls);
    }

    @Override
    public String hides(Object domainObject, ApplicationUser applicationUser) {     // <.>
        if(hidePattern == null) {
            return null;
        }
        val tenantedJdo = (TenantedJdo) domainObject;
        val name = tenantedJdo.getName();

        return hidePattern.matcher(name).matches() ? "any non-null value will hide" : null;
    }

    @Override
    public String disables(Object domainObject, ApplicationUser applicationUser) {  // <.>
        if(disablePattern == null) {
            return null;
        }
        val tenantedJdo = (TenantedJdo) domainObject;
        val name = tenantedJdo.getName();

        return disablePattern.matcher(name).matches() ? String.format("disabled, because name matches '%s'", disablePattern) : null;
    }

    @Getter
    private String hideRegex;
    public void setHideRegex(String hideRegex) {
        this.hideRegex = hideRegex;
        this.hidePattern = hideRegex != null ? Pattern.compile(hideRegex) : null;
    }
    private Pattern hidePattern;

    @Getter
    private String disableRegex;
    public void setDisableRegex(String disableRegex) {
        this.disableRegex = disableRegex;
        this.disablePattern = disableRegex != null ? Pattern.compile(disableRegex) : null;
    }
    private Pattern disablePattern;

}
//end::class[]
