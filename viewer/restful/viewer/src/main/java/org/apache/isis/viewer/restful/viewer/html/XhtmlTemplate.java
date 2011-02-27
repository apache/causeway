package org.apache.isis.viewer.restful.viewer.html;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;

import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Serializer;

import org.apache.isis.viewer.restful.viewer.Constants;


/**
 * TODO: add in support for base URI in generated XML docs?
 */
public class XhtmlTemplate {

    private final Element html;
	private Element head;
	private Element body;

    public XhtmlTemplate(final String titleStr, final HttpServletRequest servletRequest, final String... javaScriptFiles) {
    	this(titleStr, servletRequest.getSession().getServletContext(), javaScriptFiles);
    }

    private XhtmlTemplate(final String titleStr, final ServletContext servletContext, final String... javaScriptFiles) {
        this.html = new Element("html");
        addHeadAndTitle(titleStr);
        for (final String javaScriptFile : javaScriptFiles) {
            final Element script = new Element("script");
            script.addAttribute(new Attribute("type", "text/javascript"));
            script.addAttribute(new Attribute("src", "/" + javaScriptFile));
            script.appendChild(""); // force the </script> to be separate.
            head.appendChild(script);
        }
        addBody();
    }

    private void addHeadAndTitle(final String titleStr) {
        head = new Element("head");
        html.appendChild(head);
        final Element title = new Element("title");
        title.appendChild(titleStr);
        head.appendChild(title);
    }

    /**
     * Adds a &lt;body id=&quot;body&quot;> element.
     * 
     * <p>
     * The <tt>id</tt> attribute is so that Javascript can use
     * <tt>document.getElementById(&quot;body&quot;);</tt>
     * 
     */
    private void addBody() {
        body = new Element("body");
        body.addAttribute(new Attribute("id", "body"));
        html.appendChild(body);
    }

    public Element getBody() {
        return body;
    }

    public XhtmlTemplate appendToBody(final Element... elements) {
        for (final Element element : elements) {
            body.appendChild(element);
        }
        return this;
    }
    
    public Element appendToDiv(final Element div, final Element... elements){
    	for (final Element element: elements){
    		div.appendChild(element);
    	}
    	return div;
    }
    
	public Document getDocument() {
		Document document = new Document(html);
		return document;
	}

	/**
	 * 
	 * TODO: would rather be using the serializer, to ensure charset encoding is correct? 
	 * @return
	 */
	public String toXML() {
		// return xmlUsingSerializer();
		return xmlUsingToXmlMethod();
	}

	private String xmlUsingToXmlMethod() {
		return getDocument().toXML();
	}

	/**
	 * Not playing ball...
	 */
	@SuppressWarnings("unused")
	private String xmlUsingSerializer() {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			Serializer serializer = new Serializer(baos, Constants.URL_ENCODING_CHAR_SET);
			
			serializer.setPreserveBaseURI(true);
			
			//no need for pretty printing
			//serializer.setIndent(4);
			//serializer.setMaxLength(64);
			
			serializer.write(getDocument());
			serializer.flush();
			return new String(baos.toByteArray(), Constants.URL_ENCODING_CHAR_SET);
		} catch (IOException e) {
			throw new WebApplicationException(e);
		}
	}

}
