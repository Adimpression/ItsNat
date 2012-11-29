/*
  ItsNat Java Web Application Framework
  Copyright (C) 2007-2011 Jose Maria Arranz Santamaria, Spanish citizen

  This software is free software; you can redistribute it and/or modify it
  under the terms of the GNU Lesser General Public License as
  published by the Free Software Foundation; either version 3 of
  the License, or (at your option) any later version.
  This software is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  Lesser General Public License for more details. You should have received
  a copy of the GNU Lesser General Public License along with this program.
  If not, see <http://www.gnu.org/licenses/>.
*/

package org.itsnat.impl.core.resp.shared.html;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import org.itsnat.impl.core.browser.BrowserMSIEPocket;
import org.itsnat.impl.core.clientdoc.ClientDocumentStfulImpl;
import org.itsnat.impl.core.doc.ItsNatStfulDocumentImpl;
import org.itsnat.impl.core.domutil.DOMUtilInternal;
import org.itsnat.impl.core.jsren.dom.node.html.msie.JSRenderHTMLAttributeMSIEPocketImpl;
import org.itsnat.impl.core.domutil.NamespaceUtil;
import org.itsnat.impl.core.event.EventListenerInternal;
import org.itsnat.impl.core.resp.ResponseLoadStfulDocumentValid;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.views.AbstractView;
import org.w3c.dom.views.DocumentView;

/**
 *
 * @author jmarranz
 */
public class ResponseDelegateHTMLLoadDocMSIEPocketImpl extends ResponseDelegateHTMLLoadDocMSIEOldImpl
{
    public ResponseDelegateHTMLLoadDocMSIEPocketImpl(ResponseLoadStfulDocumentValid responseParent)
    {
        super(responseParent);
    }

    public String getJavaScriptDocumentName()
    {
        return "MSIEPocketHTMLDocument";
    }

    public void dispatchRequestListeners()
    {
        super.dispatchRequestListeners();

        fixBackButton(); // Debe a�adirse lo m�s �ltimo posible pues registra un listener "unload" que debe ejecutarse el �ltimo
    }

    public void fixBackButton()
    {
        ClientDocumentStfulImpl clientDoc = getClientDocumentStful();
        if (!clientDoc.isScriptingEnabled())
            return;

        // IE Mobile no recibe clicks en BODY, ni en window ni en document etc
        // Si se vuelve via back o similares es posible que la p�gina a la que se vuelve
        // haya ya ejecutado el evento unload, en ese caso lo que hay que hacer es recargar la p�gina
        // en cuanto el usuario toque algo.

        Document doc = clientDoc.getItsNatStfulDocument().getDocument();
        AbstractView view = ((DocumentView)doc).getDefaultView();
        EventListener listener = new EventListenerInternal()
        {
            public void handleEvent(Event evt) {}
        };
        StringBuffer preSendCode = new StringBuffer();
        preSendCode.append("event.setMustBeSent(false);"); // Evita enviar el evento unload
        preSendCode.append("var func = function()");
        preSendCode.append("  { itsNatDoc.disabledEvents = true; window.location.reload(true); return false; };"); // El return false evita que se env�e el evento y el itsNatDoc.disabledEvents = true evita otros eventos
        preSendCode.append("itsNatDoc.addGlobalEventListener(func);");
        clientDoc.addEventListener((EventTarget)view, "unload", listener, false,preSendCode.toString());
    }

    public String serializeDocument()
    {
        // Hacemos esto para aumentar radicalmente el rendimiento en Pocket IE
        // pues necesitamos definir atributos onXXX con el contenido especial para poder registrar event listeners
        // y la emulaci�n del setAttribute es costos�sima en tiempo de proceso
        // en IE Mobile.
        // Como hay pocos eventos posibles en IE Mobile, definimos todos los atributos onXXX
        // en tiempo de carga y as� nos ahorramos el coste de llamar al setAttribute

        // En control remoto hay que hacerlo tambi�n pues los handlers se a�aden para
        // serializar y luego se quitan del �rbol DOM servidor.

        // En este caso la estrategia es la siguiente: el DOM del documento
        // tiene nodos cacheados,
        // dichos nodos cacheados pueden tener nodos que necesiten
        // a�adirse listeners para que funcionen
        // en el cliente, sin embargo esos nodos NO nos interesan pues
        // no se reflejan en el DOM del servidor por tanto no ser�n accedidos ni habr� c�lculo
        // de paths ni path-ids de los mismos ni vincularemos listeners en
        // en el servidor

        ItsNatStfulDocumentImpl itsNatDoc = getItsNatStfulDocument();
        Document doc = itsNatDoc.getDocument();

        LinkedList attributes = new LinkedList();
        addEventListenerAttrs(doc,attributes);

        String docMarkup = super.serializeDocument();

        if (!attributes.isEmpty())
            removeAttributes(attributes);

        return docMarkup;
    }

    protected void addEventListenerAttrs(Document doc,LinkedList attributes)
    {
        // Sabemos que en este contexto los mutation events est�n desactivados

        ClientDocumentStfulImpl clientDoc = getClientDocumentStful();
        BrowserMSIEPocket browser = (BrowserMSIEPocket)clientDoc.getBrowser();
        Map eventTypes = browser.getEventTypesByTagName();

        for(Iterator it = eventTypes.entrySet().iterator(); it.hasNext(); )
        {
            Map.Entry entry = (Map.Entry)it.next();
            String localName = (String)entry.getKey();
            LinkedList types = (LinkedList)entry.getValue();
            addEventListenerAttrs(localName,types,attributes,doc);
        }
    }

    protected static void addEventListenerAttrs(String localName,LinkedList types,LinkedList attributes,Document doc)
    {
        // El comportamiento del atributo onload y onunload en <body> es diferente al de los
        // dem�s elementos, en este caso la propiedad correspondiente funciona y sobreescribe
        // el comportamiento del atributo cuando se ejecuta el "evento" load/unload
        // (aunque no cambia el valor del atributo en eso NO es igual a MSIE desktop < 8).
        // Las propiedades window.onload/unload se corresponden con las de <body>

        // En teor�a podemos asociar a la propiedad onload y unload de BODY la funci�n que queramos
        // y dicha funci�n se ejecutar�a en carga y descarga, esto no es as� en los dem�s
        // elementos de ah� la reescritura de los atributos.
        // De todas formas para unificar el tratamiento de listeners consideramos el <body>
        // como los dem�s

        LinkedList elems = DOMUtilInternal.getChildElementListWithTagNameNS(doc,NamespaceUtil.XHTML_NAMESPACE,localName,true);
        if (elems != null)
        {
            for(Iterator it = elems.iterator(); it.hasNext(); )
            {
                Element elem = (Element)it.next();
                JSRenderHTMLAttributeMSIEPocketImpl.addEventListenerAttrs(elem,types,attributes,doc);
            }
        }
    }

    public static void removeAttributes(LinkedList attributes)
    {
        JSRenderHTMLAttributeMSIEPocketImpl.removeAttributes(attributes);
    }

}

