package org.nakedobjects.servlet;

import org.nakedobjects.NakedObjects;
import org.nakedobjects.application.valueholder.Logical;
import org.nakedobjects.object.InternalCollection;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedCollection;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.NakedValue;
import org.nakedobjects.object.persistence.defaults.SerialOid;
import org.nakedobjects.object.reflect.NakedObjectField;
import org.nakedobjects.object.security.Session;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Enumeration;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;


public abstract class AbstractObjectViewerServlet extends HttpServlet {
    private static final String ENCODING = "ISO-8859-1";

	public void form(ServletOutputStream out, NakedObject object)
        throws IOException {
        out.println("<h2>" + image(object, 0) + object.titleString() + "</h2>");
        out.println("<form  action=\"object\">");
        out.println("<table width=80% border=0 cellpadding=3>");

        NakedObjectField[] fields;
        NakedObjectSpecification oif = object.getSpecification();
        Session session = NakedObjects.getCurrentSession();
        fields = object.getVisibleFields();

        // work through attributes
        for (int i = 0; i < fields.length; i++) {
            NakedObjectField field = fields[i];
            NakedObjectSpecification type = field.getSpecification();
            Naked element = object.getField(field);
            out.println("<tr><th valign=top align=right>" +
                field.getName() + ":</th><td valign=top>");

            getServletContext().log("requesting " + type + " for " + field.getName());
            
            if(element instanceof InternalCollection) {
            	out.print("collection of objecrts");
            	listContents(out, (NakedCollection) element, object);
            } else if (element instanceof Logical) {
                out.println(element.titleString().toString());
            } else if (element instanceof NakedValue) {
                    out.print("<input type=\"text\" name=\"" +
                        field.getName() + "\" value=\"");
                    out.print(element.titleString().toString());
                    out.print("\">");
            } else {
                if (element == null) {
                    out.println("<i>no entry</i>");
                    out.println("<input type=\"submit\" value=\"add\">");
                    out.println("&nbsp;<button name=add-field value=" +
                        URLEncoder.encode(field.getName(), ENCODING) +
                        " type=submit>remove</button>");
//                } else if (element instanceof NakedCollection) {
//                	listContents(out, session, (NakedCollection) element, object);
                } else {
                    out.print(objectLink((NakedObject) element, object));
                    out.println("&nbsp;<button name=remove-field value=" +
                        URLEncoder.encode(field.getName(), ENCODING) +
                        " type=submit>remove</button>");
                }
            }

            out.println("</td></tr>");
        }

        out.println("</table>");
        out.println("</form>");
    }

    protected String image(String name, int size) {
        String file = "/images/" + name;
        String sizeAttribute = "";

        if (size > 0) {
            file = file + ".gif";
            sizeAttribute = " width=" + size + " height=" + size;
        } else {
            file = file + ".gif";
            sizeAttribute = " width=" + 32 + " height=" + 32;
        }

        return "<img" + sizeAttribute +
        " align=absbottom border=0 alt=icon hspace=4 src=\"" + file + "\">";
    }

    protected String image(NakedObject object, int size) {
        return image(object.getSpecification().getShortName(), size);
    }

    public void listContents(ServletOutputStream out,
        NakedCollection collection, NakedObject inObject)
        throws IOException {
        out.println("<table width=80% border=0 cellpadding=0>");

        Enumeration e = collection.elements();

        while (e.hasMoreElements()) {
            out.println("<tr><td>");
            NakedObject element = (NakedObject) e.nextElement();
            out.println(objectLink((NakedObject) element, inObject));
            
            out.println("&nbsp;<button name=remove-element value=" +
                URLEncoder.encode("xxx", ENCODING) + " type=submit>remove</button>");
            out.println("</td></tr>");
        }

        out.println("<button name=add-element value=xxx" +
  //          URLEncoder.encode("" +
  //              ((AggregateOid) collection.getOid()).getParentOid()) +
            " type=submit>add</button>");

        out.println("</table>");
    }

    protected long serialNumber(NakedObject object) {
    	return ((SerialOid) object.getOid()).getSerialNo();
    }

    protected abstract String objectLink(NakedObject object,
        NakedObject returnObject);
}
