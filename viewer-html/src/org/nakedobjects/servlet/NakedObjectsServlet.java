package org.nakedobjects.servlet;

import org.nakedobjects.NakedObjects;
import org.nakedobjects.application.valueholder.Logical;
import org.nakedobjects.application.valueholder.TextString;
import org.nakedobjects.object.InternalCollection;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedClass;
import org.nakedobjects.object.NakedCollection;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.NakedObjectSpecificationLoader;
import org.nakedobjects.object.NakedValue;
import org.nakedobjects.object.persistence.defaults.SerialOid;
import org.nakedobjects.object.reflect.NakedObjectField;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.Vector;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.mortbay.http.HttpResponse;


public class NakedObjectsServlet extends HttpServlet {
	private static final String ENCODING = "ISO-8859-1";
	
	public String getServletInfo() {
        return "Naked Objects servlet";
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        ServletOutputStream out = response.getOutputStream();
        DataInputStream fileInputStream = null;

        try {
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("text/html");

            HttpSession session = request.getSession();
            if(session.isNew()) {
            	session.setAttribute("user", "temp");
            }
            if(session == null || session.getAttribute("user") == null) {
            	response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Out of session");
            }
 
            
            out.println("<html>");
            out.println("<head>");
            out.println("<style type=\"text/css\">");
            out.println("<!--");
            out.println("P {font-family: arial}");
            out.println("TABLE {border: 0px}");
            out.println("<-->");
            out.println("</style>");
            
            out.println("</head>");
            out.println("<body>");
            
            out.println("<table>");
            // header
            out.println("<tr><td>");
            header(out, session);
            out.println("</td></tr>");
            
            // body
            out.println("<tr><td>");
            out.println("<table>");
            out.println("<tr><td valign=\"TOP\">");
            classes(out, session);
            out.println("<hr>");
            history(out, session);
            out.println("</td><td valign=\"TOP\">");
            body(request, response, out, session);
            out.println("</td></tr>");
            out.println("</table>");
            out.println("</td></tr>");
            
            // footer
            out.println("<tr><td>");
            footer(out, session);
            out.println("</td></tr>");
            
            out.println("</table>");
            
            out.println("</body>");
            out.println("</html>");
            
            out.flush();

        } catch (IOException e) {
            System.err.println("IO Error " + e);
            e.printStackTrace();

            return;
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException ignoree) {
                    ;
                }
            }
        }
    }

    private void header(ServletOutputStream out, HttpSession session) throws IOException {
    	out.println("<table width=\"100%\">");
    	out.println("<tr><td align=\"left\" valign=\"TOP\">");
    	out.println("<img border=0 alt=\"logo\" hspace=4 src=\"/images/logo.png\">");
    	out.println("</td><td align=\"right\" valign=\"BOTTOM\">");
    	out.println("<h1>Naked Objects Browser</h1>");
    	out.println("<p><small>Logged on as </i>" + session.getAttribute("user") + "</i></small></p>");
    	out.println("</td></tr>");
    	out.println("</table>");
    	
    }

	private void footer(ServletOutputStream out, HttpSession session) throws IOException {
		out.println("<hr>");
		out.println("<p><small>Brought to you by Robert Matthews</small></p>");
		out.println("<p><small>Copyright Naked Objects</small></p>");
		out.println("<p><small>Log <a href=\"/logout\">out</a></small></p>");
	}

	private void history(ServletOutputStream out, HttpSession session) throws IOException {
		Vector history = (Vector) session.getAttribute("history");
		for (int i = 0; i < history.size(); i++) {
			String idParam = (String) history.get(i);
			long id = Long.valueOf(idParam).longValue();
			NakedObject object = NakedObjects.getObjectManager().getObject(new SerialOid(id), null);
			out.println("<p><small>" + objectLink(object) + "</small></p>");
		}
	}

	private void body(HttpServletRequest request, HttpServletResponse response, ServletOutputStream out, HttpSession session) throws IOException {
		String action = request.getParameter("action");

		if("object".equals(action)) {
			String idParam = request.getParameter("id");
			if(idParam == null) {
				response.sendError(HttpResponse.__400_Bad_Request);
				return;
			}
			
			Vector history = (Vector) session.getAttribute("history");
			if(history.contains(idParam)) {
				history.remove(idParam);
			} else if(history.size() > 15) {
				history.remove(history.size() - 1);
			}
			history.insertElementAt(idParam, 0);
			
			long id = Long.valueOf(idParam).longValue();
			NakedObject object;
				object = NakedObjects.getObjectManager().getObject(new SerialOid(id), null);
				view(out, object);
			
		} else if("list".equals(action)) {
			String cls = request.getParameter("class");

			NakedObjectSpecification nc = NakedObjects.getSpecificationLoader().loadSpecification(cls);
			out.println("<h2>" +nc.getPluralName() + "</h2>");

			c = nc.explorationActionInstances();
			Enumeration e = c.displayElements();
			while (e.hasMoreElements()) {
				NakedObject instance = (NakedObject) e.nextElement();
				out.println("<p> <a href=\"?action=object&id=" + serialNumber(instance) +
						"\">" + image(instance, 16) + instance.titleString().toString() +
				"</a> ");
			}
			
		} else if("table".equals(action)) {
			String cls = request.getParameter("class");

			NakedObjectSpecification nc = NakedObjectSpecificationLoader.getInstance().loadSpecification(cls);
			FieldSpecification[] fields;
			
			out.println("<h2>" +nc.getPluralName() + "</h2>");

			NakedCollection c = nc.explorationActionInstances();
			Enumeration e = c.elements();
			out.println("<table border=2 cellspacing=1 cellpadding=6><thead align=left>");
			NakedObject firstInstance = (NakedObject) e.nextElement();
			fields = nc.getVisibleFields(firstInstance, session);
			out.println("<th>&nbsp;</th>");
			for (int i = 0; i < fields.length; i++) {
				FieldSpecification field = fields[i];
				out.println("<th>" + field.getName() + "</th>");
			}
			out.println("</thead>");
			
			e = c.displayElements();
			while (e.hasMoreElements()) {
				NakedObject instance = (NakedObject) e.nextElement();
				out.println("<tr align=left>");
				out.println("<th>" + objectLink(instance) + "</th>");
				for (int i = 0; i < fields.length; i++) {
					FieldSpecification field = fields[i];
					NakedObjectSpecification type = field.getType();
					Naked content = field.get(instance);
					out.println("<td>");
					if(content instanceof InternalCollection) {
						out.println(((NakedCollection) content).size());
					} else if (Logical.class.isAssignableFrom(type)) {
						// TODO use tick image
						out.println(content.title().toString());
					} else if (NakedValue.class.isAssignableFrom(type)) {
						out.print(content.title().toString());
					} else {
						if (content != null) {
							out.print(objectLink((NakedObject) content));
						}
					}
					out.println("</td>");
				}
				out.println("</tr>");
			}
			out.println("</table>");
			
		}
	}

	private void classes(ServletOutputStream out, HttpSession session) throws IOException {
		NakedClassList classes = (NakedClassList) session.getAttribute("classes");
		Enumeration e = classes.elements();
		while (e.hasMoreElements()) {
			NakedClass element = (NakedClass) e.nextElement();

			out.println("<p>" + image(element.getShortName(), 32) + "<em>" + element.title() + "</em> <small><a href=\"?action=list&class=" + element.fullName() + "\">list</a> <a href=\"?action=table&class=" + element.fullName() + "\">table</a></small>");
		}
	}
	
	public void init(ServletConfig config) throws ServletException {
        super.init(config);

        config.getServletContext().log("Initializing the instances servlet");
    }

    protected String objectLink(NakedObject object) {
        String param = "action=object&id=" + ((SerialOid) object.getOid()).getSerialNo();

        return "<a href=\"?" + param + "\">" + image(object, 16) +
        object.titleString().toString() + "</a> ";
    }
    
    public void view(ServletOutputStream out, NakedObject object) throws IOException {
    	view(out, object, 0);
    }
	
    public void view(ServletOutputStream out, NakedObject object, int level)
	throws IOException {
    	String tag = "h" + (level + 2);
    	String element = level == 0 ? image(object, 0) + object.titleString() : objectLink(object);
    	if(level < 2) {
	    	out.println("<table cellpadding=3>");
	    	out.println("<tr><th valign=top align=left colspan=\"2\"><" + tag + ">" + element + "</" + tag + "></th></tr>");
	    	
	    	NakedObjectField[] fields;
	    	fields = object.getVisibleFields();
	
	    	// work through fields
	    	for (int i = 0; i < fields.length; i++) {
	    	    NakedObjectField field = fields[i];
	    		NakedObjectSpecification type = field.getSpecification();
	    		Naked content = object.getField(field);
	    		out.println("<tr><th valign=top align=left>" + field.getName() + ":</th><td valign=top>");
	    		if(content instanceof InternalCollection) {
	    			viewContents(out, (NakedCollection) content, object, level);
	    		} else if (Logical.class.isAssignableFrom(type)) {
	    			// TODO use tick image
	    			out.println(content.title().toString());
	    		} else if (NakedValue.class.isAssignableFrom(type)) {
	   				out.print(content.title().toString());
	    		} else {
	    			if (content != null) {
	    				view(out, (NakedObject) content, level + 1);
	    			}
	    		}
	
	    		out.println("</td></tr>");
	    	}
	
	    	out.println("</table>");
    	} else {
    		out.println("<" + tag + ">" + element + "</" + tag + ">");
    	}
    }
    
    public void form(ServletOutputStream out, NakedObject object)
	throws IOException {
    	out.println("<h2>" + image(object, 0) + object.titleString() + "</h2>");
    	out.println("<form  action=\"object\">");
    	out.println("<table width=80% cellpadding=3>");

    	NakedObjectField[] fields;
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
    		} else if (Logical.class.isAssignableFrom(type)) {
    			out.println(element.title().toString());
    		} else if (NakedValue.class.isAssignableFrom(type)) {
    			if (false && (field.getType() == TextString.class)) {
    				out.print(element.title().toString());
    			} else {
    				out.print("<input type=\"text\" name=\"" +
    						field.getName() + "\" value=\"");
    				out.print(element.title().toString());
    				out.print("\">");
    			}
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
    				out.print(objectLink((NakedObject) element));
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
    		sizeAttribute = " height=" + size;
    	} else {
    		file = file + ".gif";
    		sizeAttribute =" height=" + 32;
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
    	out.println("<table width=80% cellpadding=0>");

    	Enumeration e = collection.elements();

    	while (e.hasMoreElements()) {
    		out.println("<tr><td>");
    		NakedObject element = (NakedObject) e.nextElement();
    		out.println(objectLink((NakedObject) element));
    		
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

    public void viewContents(ServletOutputStream out,
    		NakedCollection collection, NakedObject inObject, int level)
	throws IOException {
    	out.println("<table cellpadding=0>");

    	Enumeration e = collection.elements();
    	while (e.hasMoreElements()) {
    		out.println("<tr><td>");
    		NakedObject element = (NakedObject) e.nextElement();
    		view(out, element, level + 1);
    		out.println("</td></tr>");
    	}
    	out.println("</table>");
    }

    protected long serialNumber(NakedObject object) {
    	return ((SerialOid) object.getOid()).getSerialNo();
    }
}
