/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package inexp.xpathex;

import java.util.Iterator;
import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import org.w3c.dom.Document;

/*
 * Based on: http://www.ibm.com/developerworks/library/x-nmspccontext/
 * 
 * More links: http://www.edankert.com/defaultnamespaces.html  
 *  http://xml.apache.org/xalan-j/xpath_apis.html#namespacecontext  
 *  http://plasmasturm.org/log/259/
 */

public class UniversalNamespaceResolver implements NamespaceContext {
    // the delegate
    private Document sourceDocument;

    /**
     * This constructor stores the source document to search the namespaces in
     * it.
     * 
     * @param document
     *            source document
     */
    public UniversalNamespaceResolver(Document document) 
    {
        sourceDocument = document;
    }

    /**
     * The lookup for the namespace uris is delegated to the stored document.
     * 
     * @param prefix
     *            to search for
     * @return uri
     */
    public String getNamespaceURI(String prefix) 
    {
        if (prefix.equals(XMLConstants.DEFAULT_NS_PREFIX)) {
            return sourceDocument.lookupNamespaceURI(null);
        } else {
            return sourceDocument.lookupNamespaceURI(prefix);
        }
    }

    /**
     * This method is not needed in this context, but can be implemented in a
     * similar way.
     */
    public String getPrefix(String namespaceURI) 
    {
        return sourceDocument.lookupPrefix(namespaceURI);
    }

    public Iterator getPrefixes(String namespaceURI) 
    {
        // not implemented yet
        return null;
    }

}
