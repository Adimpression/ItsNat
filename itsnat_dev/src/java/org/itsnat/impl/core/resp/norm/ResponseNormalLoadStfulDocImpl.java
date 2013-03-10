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

package org.itsnat.impl.core.resp.norm;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import org.itsnat.core.CommMode;
import org.itsnat.core.ItsNatDOMException;
import org.itsnat.impl.core.CommModeImpl;
import org.itsnat.impl.core.browser.Browser;
import org.itsnat.impl.core.browser.webkit.BrowserWebKit;
import org.itsnat.impl.core.clientdoc.ClientDocumentStfulImpl;
import org.itsnat.impl.core.clientdoc.ClientDocumentStfulOwnerImpl;
import org.itsnat.impl.core.doc.BoundElementDocContainerImpl;
import org.itsnat.impl.core.doc.ItsNatHTMLDocumentImpl;
import org.itsnat.impl.core.doc.ItsNatStfulDocumentImpl;
import org.itsnat.impl.core.domimpl.ElementDocContainer;
import org.itsnat.impl.core.domutil.DOMUtilInternal;
import org.itsnat.impl.core.domutil.NodeConstraints;
import org.itsnat.impl.core.listener.domstd.OnUnloadListenerImpl;
import org.itsnat.impl.core.listener.domstd.RegisterThisDocAsReferrerListenerImpl;
import org.itsnat.impl.core.req.norm.RequestNormalLoadDocImpl;
import org.itsnat.impl.core.resp.*;
import org.itsnat.impl.core.resp.shared.ResponseDelegateStfulLoadDocImpl;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.views.AbstractView;
import org.w3c.dom.views.DocumentView;

/**
 *
 * @author jmarranz
 */
public abstract class ResponseNormalLoadStfulDocImpl extends ResponseNormalLoadDocImpl implements ResponseLoadStfulDocumentValid
{
    protected ResponseDelegateStfulLoadDocImpl responseDelegate;
    protected Map<Node,Object> disconnectedNodesFastLoadMode;

    /**
     * Creates a new instance of ResponseNormalLoadStfulDocImpl
     */
    public ResponseNormalLoadStfulDocImpl(RequestNormalLoadDocImpl request)
    {
        super(request);

        this.responseDelegate = ResponseDelegateStfulLoadDocImpl.createResponseDelegateStfulLoadDoc(this);
    }

    public static ResponseNormalLoadStfulDocImpl createResponseNormalLoadStfulDoc(RequestNormalLoadDocImpl request)
    {
        ItsNatStfulDocumentImpl itsNatDoc = (ItsNatStfulDocumentImpl)request.getItsNatDocument();
        if (itsNatDoc instanceof ItsNatHTMLDocumentImpl)
            return new ResponseNormalLoadHTMLDocImpl(request);
        else
            return new ResponseNormalLoadOtherNSDocImpl(request);
    }

    public ClientDocumentStfulImpl getClientDocumentStful()
    {
        return (ClientDocumentStfulImpl)getClientDocument();
    }

    public ClientDocumentStfulOwnerImpl getClientDocumentStfulOwner()
    {
        return (ClientDocumentStfulOwnerImpl)getClientDocument();
    }

    public ItsNatStfulDocumentImpl getItsNatStfulDocument()
    {
        return (ItsNatStfulDocumentImpl)getRequestNormalLoadDoc().getItsNatDocument();
    }

    public ResponseDelegateStfulLoadDocImpl getResponseDelegateStfulLoadDoc()
    {
        return responseDelegate;
    }

    public void processResponse()
    {
        responseDelegate.processResponse();

        ClientDocumentStfulImpl clientDoc = getClientDocumentStful();
        if (!clientDoc.canReceiveSOMENormalEvents())
        {
            // No hay eventos y por tanto no hay posibilidad de unload
            ItsNatStfulDocumentImpl itsNatDoc = getItsNatStfulDocument();
            itsNatDoc.setInvalid();
        }
    }

    public boolean isSerializeBeforeDispatching()
    {
        ItsNatStfulDocumentImpl itsNatDoc = getItsNatStfulDocument();
        return !itsNatDoc.isFastLoadMode();
    }

    @Override
    public void dispatchRequestListeners()
    {
        // Caso de carga del documento por primera vez, el documento est� reci�n creado

        ItsNatStfulDocumentImpl itsNatDoc = getItsNatStfulDocument();
        Document doc = itsNatDoc.getDocument();
        AbstractView view = ((DocumentView)doc).getDefaultView();
        ClientDocumentStfulImpl clientDoc = getClientDocumentStful();
        Browser browser = clientDoc.getBrowser();

        if (itsNatDoc.isReferrerEnabled())
        {
            EventTarget target;
            String eventType;
            int commMode;
            if (browser.isClientWindowEventTarget())
            {
                target = (EventTarget)view;
                if ( CommModeImpl.isXHRDefaultMode(clientDoc) &&
                     browser.hasBeforeUnloadSupport(itsNatDoc) &&
                     itsNatDoc.isUseXHRSyncOnUnloadEvent() &&
                     (!(browser instanceof BrowserWebKit) ||
                      ((browser instanceof BrowserWebKit) && ((BrowserWebKit)browser).isXHRSyncSupported())) )
                {
                    // Si no se soporta el modo s�ncrono corremos el riesgo de que no se env�e el evento en el proceso de cerrado de la p�gina
                    // lo cual normalmente ocurre en el evento "unload"
                    eventType = "beforeunload";
                    commMode = CommMode.XHR_SYNC; // As� aseguramos que se env�a pues por ejemplo no hay seguridad en modo as�ncrono en MSIE 6 desktop
                }
                else
                {
                    // Intentamos soportar referrers tambi�n aunque de forma menos elegante.
                    // Registramos en el evento load y no cuando se carga el documento
                    // para evitar solapamiento con posibles iframes
                    eventType = "load";
                    commMode = clientDoc.getCommMode();
                }
            }
            else
            {
                target = (EventTarget)doc.getDocumentElement();
                eventType = "SVGLoad";
                commMode = clientDoc.getCommMode();
            }

            clientDoc.addEventListener(target,eventType,RegisterThisDocAsReferrerListenerImpl.SINGLETON,false,commMode);
        }

        // Es necesario usar siempre el modo s�ncrono con unload para asegurar que llega al servidor
        // sobre todo con FireFox, total es destrucci�n
        // En FireFox a veces el unload se env�a pero no llega al servidor en el caso de AJAX as�ncrono,
        // la culpa la tiene quiz�s el enviar por red as�ncronamente algo en el proceso de destrucci�n de la p�gina
        // Curiosamente esto s�lo ocurre cuando se abre un visor remoto Comet y se cierra la p�gina principal.
        // En teor�a "beforeunload" deber�a dar menos problemas que unload en FireFox
        // pero sin embargo tambi�n ocurri� con beforeunload as�ncrono (adem�s beforeunload es cancelable).
        // NOTA: es posible que en versiones recientes est� solucionado esto.
        // De todas formas es �til el modo s�ncrono porque si hubiera alg�n
        // JavaScript pendiente de enviar, pues evita que de error al haberse perdido la p�gina
        // (pues el navegador ha de esperarse, no destruye la p�gina), si fuera asincrono
        // seguir�a destruyendo la p�gina antes de retornar el evento (comprobado en MSIE y FireFox).

        super.dispatchRequestListeners();

        // En W3C en addEventListener el orden de dispatch es el mismo que el orden de inserci�n
        // y en MSIE hemos simulado lo mismo (lo natural es primero el �ltimo)
        // por ello insertamos despu�s de los listeners del usuario tal que
        // nuestro unload "destructor" (invalida/desregistra el documento) sea el �ltimo

        // Si se puede, los eventos de descarga deben enviarse como s�ncronos

        EventTarget target;
        String eventType;
        int commMode;
        int defaultCommMode = clientDoc.getCommMode();
        if (CommModeImpl.isXHRMode(defaultCommMode))
        {
            if (!itsNatDoc.isUseXHRSyncOnUnloadEvent() ||
                ((browser instanceof BrowserWebKit) &&
                 !((BrowserWebKit)browser).canSendXHRSyncUnload())) // Este problema no se ha estudiado para SVGUnLoad pero por si acaso tambi�n lo consideramos
                commMode = CommMode.XHR_ASYNC;
            else
                commMode = CommMode.XHR_SYNC;
        }
        else commMode = defaultCommMode; // Caso SCRIPT o SCRIPT_HOLD, siempre as�ncronos

        if (browser.isClientWindowEventTarget())
        {
            target = (EventTarget)view;
            eventType = "unload";
        }
        else
        {
            // En algunos plugins no se dispara por ejemplo ASV (v3 y v6) o Batik.
            target = (EventTarget)doc.getDocumentElement();
            eventType = "SVGUnload";
        }

        clientDoc.addEventListener(target,eventType,OnUnloadListenerImpl.SINGLETON,false,commMode);
    }

    public boolean hasDisconnectedNodesFastLoadMode()
    {
        if (disconnectedNodesFastLoadMode == null) return false;
        return !disconnectedNodesFastLoadMode.isEmpty();
    }

    public Map<Node,Object> getDisconnectedNodesFastLoadMode()
    {
        if (disconnectedNodesFastLoadMode == null)
            this.disconnectedNodesFastLoadMode = new HashMap<Node,Object>();
        return disconnectedNodesFastLoadMode;
    }

    @Override
    public String serializeDocument()
    {
        // Como este m�todo inserta muchos nodos ha de ejecutarse lo antes posible para que otros procesos
        // pre-serializaci�n puedan hacer algo con ellos
        preSerializeDocDisconnectedNodesFastLoadMode();

        LinkedList<BoundElementDocContainerImpl> boundHTMLElemDocContainerList = preSerializeDocProcessBoundElementDocContainer();

        String docMarkup = super.serializeDocument();

        postSerializeDocProcessBoundElementDocContainer(boundHTMLElemDocContainerList);

        postSerializeDocDisconnectedNodesFastLoadMode();

        return docMarkup;
    }

    public void preSerializeDocDisconnectedNodesFastLoadMode()
    {
        ItsNatStfulDocumentImpl itsNatDoc = getItsNatStfulDocument();

        if (itsNatDoc.isFastLoadMode() && hasDisconnectedNodesFastLoadMode())
        {
            // Insertamos temporalmente los nodos hijo eliminados pues el cliente
            // debe recibirlos al serializar
            Map<Node,Object> disconnectedNodesFastLoadMode = getDisconnectedNodesFastLoadMode();
            for(Map.Entry<Node,Object> entry : disconnectedNodesFastLoadMode.entrySet())
            {
                Node parentNode = entry.getKey();
                Object content = entry.getValue();
                if (parentNode.hasChildNodes())
                    throw new RuntimeException("INTERNAL ERROR"); // Por si acaso
                if (content instanceof Node) // Nodo concreto
                {
                    Node childNode = (Node)content;
                    if (itsNatDoc.isDebugMode() && DOMUtilInternal.isNodeBoundToDocumentTree(childNode))
                        throw new ItsNatDOMException("Child nodes removed from a disconnected node cannot be reinserted in a different place on load phase and fast mode",childNode);
                    parentNode.appendChild(childNode);
                }
                else
                {
                    @SuppressWarnings("unchecked")
                    LinkedList<Node> nodeList = (LinkedList<Node>)content;
                    Iterator<Node> itChildNodes = nodeList.iterator();
                    DocumentFragment childNodesFragment = (DocumentFragment)itChildNodes.next(); // Sabemos que el primero es el DocumentFragment que se le dio al usuario
                    while(itChildNodes.hasNext())
                    {
                        Node childNode = itChildNodes.next();
                        if (itsNatDoc.isDebugMode() && DOMUtilInternal.isNodeBoundToDocumentTree(childNode))
                            throw new ItsNatDOMException("Child nodes removed from a disconnected node cannot be reinserted in a different place on load phase and fast mode",childNode);
                        parentNode.appendChild(childNode);
                    }
                    // Al mismo tiempo que los insertamos se eliminaron en teor�a del DocumentFragment que se dio al usuario y que los conten�a,
                    // lo comprobamos
                    if (childNodesFragment.hasChildNodes())
                        throw new ItsNatDOMException("DocumentFragment containing the child nodes removed from a disconnected node cannot be reinserted in a different place on load phase and fast mode",childNodesFragment);
                }
            }
        }
    }

    public void postSerializeDocDisconnectedNodesFastLoadMode()
    {
        ItsNatStfulDocumentImpl itsNatDoc = getItsNatStfulDocument();
        if (itsNatDoc.isFastLoadMode() && hasDisconnectedNodesFastLoadMode())
        {
            // Eliminamos el contenido de los nodos nuevo para dejarlos en el servidor como el programador lo hizo
            // cuando desconect�
            Map<Node,Object> disconnectedNodesFastLoadMode = getDisconnectedNodesFastLoadMode();
            for(Map.Entry<Node,Object> entry : disconnectedNodesFastLoadMode.entrySet())
            {
                Node parentNode = entry.getKey();
                Object content = entry.getValue();

                if (content instanceof Node) // Nodo concreto
                {
                    // S�lo esperamos un nodo
                    Node childNode = (Node)content;
                    if (childNode != DOMUtilInternal.extractChildren(parentNode))
                        throw new RuntimeException("INTERNAL ERROR"); // Para que quede claro
                }
                else
                {
                    @SuppressWarnings("unchecked")
                    LinkedList<Node> nodeList = (LinkedList<Node>)content;
                    Iterator<Node> itChildNodes = nodeList.iterator();
                    DocumentFragment childNodesFragment = (DocumentFragment)itChildNodes.next(); // Sabemos que el primero es el DocumentFragment que se le dio al usuario
                    DocumentFragment childNodesFragmentAux = (DocumentFragment)DOMUtilInternal.extractChildren(parentNode);
                    // Copiamos uno en otro para restaurar el DocumentFragment del usuario
                    while(childNodesFragmentAux.getFirstChild() != null)
                    {
                        childNodesFragment.appendChild(childNodesFragmentAux.getFirstChild());
                    }
                }
            }

            this.disconnectedNodesFastLoadMode = null; // Ya no los necesitamos m�s, liberamos cuanto antes memoria
        }
    }

    public LinkedList<BoundElementDocContainerImpl> preSerializeDocProcessBoundElementDocContainer()
    {
        LinkedList<BoundElementDocContainerImpl> boundHTMLElemDocContainerList = null;

        ItsNatStfulDocumentImpl itsNatDoc = getItsNatStfulDocument();
        Document doc = itsNatDoc.getDocument();

        // Elementos que implementan ElementDocContainer: <object>, <iframe> y <embed>
        NodeConstraints rules = new NodeConstraints()
        {
            public boolean match(Node node, Object context)
            {
                return (node instanceof ElementDocContainer); // <iframe> y <object>
            }
        };
        LinkedList<Node> elemList = DOMUtilInternal.getChildNodeListMatching(doc,rules,true,null);
        if (elemList != null)
        {
            ClientDocumentStfulOwnerImpl cliendDoc = getClientDocumentStfulOwner();
            for(Iterator<Node> it = elemList.iterator(); it.hasNext(); )
            {
                ElementDocContainer elem = (ElementDocContainer)it.next();
                BoundElementDocContainerImpl bindInfo = BoundElementDocContainerImpl.register(elem, itsNatDoc);
                if (bindInfo == null)
                    continue; // No tiene el formato de URL relativa esperado o los par�metros est�n malformados

                bindInfo.setURLForClientOwner(cliendDoc);

                if (boundHTMLElemDocContainerList == null) boundHTMLElemDocContainerList = new LinkedList<BoundElementDocContainerImpl>();
                boundHTMLElemDocContainerList.add(bindInfo);
            }
        }

        return boundHTMLElemDocContainerList;
    }

    public void postSerializeDocProcessBoundElementDocContainer(LinkedList<BoundElementDocContainerImpl> boundHTMLElemDocContainerList)
    {
        // Restauramos los URLs originales ("src" en iframe o "data" en object)
        if (boundHTMLElemDocContainerList != null)
        {
            ClientDocumentStfulOwnerImpl cliendDoc = getClientDocumentStfulOwner();
            for(BoundElementDocContainerImpl bindInfo : boundHTMLElemDocContainerList)
            {
                bindInfo.restoreOriginalURL(cliendDoc);
            }
        }
    }


    public void preSerializeDocumentStful()
    {
        // Nada que hacer
    }

    public boolean isOnlyReturnMarkupOfScripts()
    {
        if (getParentResponseAttachedServerLoadDoc() != null)
            return true;  // Porque el markup ya est� en el cliente no es necesario enviarlo de nuevo
        return false;
    }

    public boolean isNeededAbsoluteURL()
    {
        if (getParentResponseAttachedServerLoadDoc() != null)
            return true;  // Porque los requests se enviar�n posiblemente a un servidor diferente al que carg� la p�gina inicial
        return false;
    }

    public boolean isInlineLoadFrameworkScripts()
    {
        if (getParentResponseAttachedServerLoadDoc() != null)
            return true;  // Porque as� por una parte evitamos un request (lo menos importante) y evitamos que a trav�s de un URL el archivo se cargue despu�s del c�digo inicial en navegadores tal y como MSIE 6 en donde la carga de <script src=""> introducidos via document.write() sigue siendo as�ncrona, al estar el c�digo ahora dentro del <script>c�digo</script> es inevitable su ejecuci�n inmediata
        return false;
    }
}
