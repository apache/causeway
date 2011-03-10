package org.apache.isis.viewer.scimpi.dispatcher.view.debug;

import java.util.List;

import org.apache.isis.applib.Identifier;
import org.apache.isis.viewer.scimpi.dispatcher.AbstractElementProcessor;
import org.apache.isis.viewer.scimpi.dispatcher.processor.Request;

public class PrintAuthorizationClause extends AbstractElementProcessor {

    public String getName() {
        return "print-authorization-clause";
    }

    public void process(Request request) {
        Identifier identifier = (Identifier) request.getContext().getVariable("_security_identifier");
        List<String> roles =  (List<String>) request.getContext().getVariable("_security_roles");
        StringBuffer roleList = new StringBuffer();
        for (String role : roles) {
            if (roleList.length() > 0) {
                roleList.append("|");
            }
            roleList.append(role);
        }
        
        request.appendHtml("<pre>" );
        request.appendHtml(identifier.toClassIdentityString() + ":" + roleList + "\n");
        request.appendHtml(identifier.toString() + ":" + roleList);
        request.appendHtml( "</pre>");
    }

}



