package org.apache.isis.subdomains.docx.applib.traverse;

import java.util.List;

import org.docx4j.TraversalUtil;
import org.docx4j.TraversalUtil.Callback;
import org.docx4j.XmlUtils;

public abstract class CallbackAbstract implements Callback {
    public void walkJAXBElements(Object parent) {
        List<Object> children = getChildren(parent);
        if (children == null) {
            return;
        }
        for (Object o : children) {
            // if wrapped in javax.xml.bind.JAXBElement, get its value
            o = XmlUtils.unwrap(o);
            apply(o);
            if (shouldTraverse(o)) {
                walkJAXBElements(o);
            }
        }
    }

    public List<Object> getChildren(Object o) {
        return TraversalUtil.getChildrenImpl(o);
    }

    public boolean shouldTraverse(Object o) {
        return true;
    }
}
