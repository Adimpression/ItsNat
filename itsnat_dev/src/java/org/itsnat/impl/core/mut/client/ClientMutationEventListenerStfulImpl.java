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

package org.itsnat.impl.core.mut.client;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.Map;
import org.itsnat.core.ItsNatDOMException;
import org.itsnat.core.html.ItsNatHTMLEmbedElement;
import org.itsnat.impl.core.browser.BrowserMSIE6;
import org.itsnat.impl.core.clientdoc.ClientDocumentStfulImpl;
import org.itsnat.impl.core.clientdoc.ClientDocumentStfulOwnerImpl;
import org.itsnat.impl.core.clientdoc.ClientDocumentAttachedClientImpl;
import org.itsnat.impl.core.doc.BoundElementDocContainerImpl;
import org.itsnat.impl.core.doc.ElementDocContainerWrapperImpl;
import org.itsnat.impl.core.doc.ItsNatStfulDocumentImpl;
import org.itsnat.impl.core.doc.ItsNatHTMLDocumentImpl;
import org.itsnat.impl.core.domimpl.ElementDocContainer;
import org.itsnat.impl.core.domimpl.ItsNatNodeInternal;
import org.itsnat.impl.core.jsren.dom.node.JSRenderAttributeImpl;
import org.itsnat.impl.core.jsren.dom.node.JSRenderCharacterDataImpl;
import org.itsnat.impl.core.jsren.dom.node.JSRenderNodeImpl;
import org.itsnat.impl.core.jsren.dom.node.JSRenderNotAttrOrViewNodeImpl;
import org.itsnat.impl.core.mut.doc.DocMutationEventListenerStfulImpl;
import org.w3c.dom.Attr;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.events.MutationEvent;
import org.w3c.dom.html.HTMLObjectElement;
import org.w3c.dom.html.HTMLParamElement;

/**
 *
 * @author jmarranz
 */
public abstract class ClientMutationEventListenerStfulImpl implements Serializable
{
    protected ClientDocumentStfulImpl clientDoc;

    public ClientMutationEventListenerStfulImpl(ClientDocumentStfulImpl clientDoc)
    {
        this.clientDoc = clientDoc;
    }

    public static ClientMutationEventListenerStfulImpl createClientMutationEventListenerStful(ClientDocumentStfulImpl clientDoc)
    {
        ItsNatStfulDocumentImpl itsNatDoc = clientDoc.getItsNatStfulDocument();
        if (itsNatDoc instanceof ItsNatHTMLDocumentImpl)
            return ClientMutationEventListenerHTMLImpl.createClientMutationEventListenerHTML(clientDoc);
        else
            return new ClientMutationEventListenerStfulDefaultImpl(clientDoc);
    }

    public ClientDocumentStfulImpl getClientDocumentStful()
    {
        return clientDoc;
    }

    public boolean canRenderAndSendMutationJSCode()
    {
        ItsNatStfulDocumentImpl itsNatDoc = clientDoc.getItsNatStfulDocument();
        return (itsNatDoc.canRenderAndSendMutationJSCode() &&
                clientDoc.isSendCodeEnabled());
    }

    public DocMutationEventListenerStfulImpl getDocMutationEventListenerStful()
    {
        return clientDoc.getItsNatStfulDocument().getDocMutationListenerEventStful();
    }

    public boolean isDisconnectedFromClient(Node node)
    {
        return ((ItsNatNodeInternal)node).getDelegateNode().isDisconnectedFromClient();
    }

    public void renderAndSendMutationCode(MutationEvent mutEvent)
    {
        String type = mutEvent.getType();
        if (type.equals("DOMNodeInserted"))
        {
            // Hay que tener en cuenta que el nodo YA est� insertado en el DOM servidor
            //Element parent = (Element)mutEvent.getRelatedNode();
            Node newNode = (Node)mutEvent.getTarget();

            renderTreeDOMNodeInserted(newNode);
        }
        else if (type.equals("DOMNodeRemoved"))
        {
            // El nodo todav�a no ha sido removido del �rbol DOM servidor

            //Element parent = (Element)mutEvent.getRelatedNode();
            Node removedNode = (Node)mutEvent.getTarget();

            // No damos error en el caso de desconectado pero no generamos el c�digo porque necesitamos
            // eliminar los nodos hijo sin que el cliente se entere, esto s�lo es en fase de
            // desconexi�n pues estando desconectado un nodo no podr�n insertarse hijos nuevos (ni por tanto eliminarse)
            if (!isDisconnectedFromClient(removedNode))
                renderTreeDOMNodeRemoved(removedNode);
        }
        else if (type.equals("DOMAttrModified"))
        {
            Attr attr = (Attr)mutEvent.getRelatedNode();
            Element elem = (Element)mutEvent.getTarget();

            String code = null;
            JSRenderAttributeImpl render = JSRenderAttributeImpl.getJSRenderAttribute(attr,elem,clientDoc);
            int changeType = mutEvent.getAttrChange();
            switch(changeType)
            {
                case MutationEvent.ADDITION:
                case MutationEvent.MODIFICATION:
                    code = render.setAttributeCode(attr,elem,false,clientDoc);
                    break;
                case MutationEvent.REMOVAL:
                    code = render.removeAttributeCode(attr,elem,clientDoc);
                    break;
                // No hay m�s casos
            }
            clientDoc.addCodeToSend(code);
        }
        else if (type.equals("DOMCharacterDataModified"))
        {
            CharacterData charDataNode = (CharacterData)mutEvent.getTarget();
            JSRenderCharacterDataImpl render = (JSRenderCharacterDataImpl)JSRenderNodeImpl.getJSRenderNode(charDataNode,clientDoc);
            String code = render.getCharacterDataModifiedCode(charDataNode,clientDoc);
            clientDoc.addCodeToSend(code);
        }
    }

    public void renderTreeDOMNodeInserted(Node newNode)
    {
        JSRenderNotAttrOrViewNodeImpl render = (JSRenderNotAttrOrViewNodeImpl)JSRenderNodeImpl.getJSRenderNode(newNode,clientDoc);
        Object code = render.getInsertNewNodeCode(newNode,clientDoc); // Puede ser null
        clientDoc.addCodeToSend(code);
    }

    public void renderTreeDOMNodeRemoved(Node removedNode)
    {
        JSRenderNotAttrOrViewNodeImpl render = (JSRenderNotAttrOrViewNodeImpl)JSRenderNodeImpl.getJSRenderNode(removedNode,clientDoc);
        String code = render.getRemoveNodeCode(removedNode,clientDoc);
        clientDoc.addCodeToSend(code);
    }

    public void beforeRenderAndSendMutationCode(MutationEvent mutEvent)
    {
        ItsNatStfulDocumentImpl itsNatDoc = clientDoc.getItsNatStfulDocument();
        String type = mutEvent.getType();

        if (type.equals("DOMNodeInserted") &&
            !itsNatDoc.isLoadingPhaseAndFastLoadMode())
        {
            Node insertedNode = (Node)mutEvent.getTarget();
            processTreeInsertedElementDocContainer(true,insertedNode);
        }
    }

    public void afterRenderAndSendMutationCode(MutationEvent mutEvent)
    {
        ItsNatStfulDocumentImpl itsNatDoc = clientDoc.getItsNatStfulDocument();
        String type = mutEvent.getType();

        if (clientDoc.isNodeCacheEnabled() &&
            ( !itsNatDoc.isLoadingPhaseAndFastLoadMode() || itsNatDoc.isDebugMode()) &&
            type.equals("DOMNodeRemoved"))
        {
            // Esto lo hacemos s� o s� pues tenemos que eliminar del cach�
            // en el servidor los nodos removidos, aunque el cliente no se entere (send code disabled)
            // al menos evitamos memory leaks en el servidor (y evitamos fallos al clonar
            // la cach� inicialmente para un cliente control remoto)
            // Ha de llamarse lo �ltimo porque para eliminar el nodo en el cliente es m�s r�pido
            // usar la cach� y aunque el nodo haya sido eliminado ya en el cliente
            // (porque ya se ha enviado el c�digo de eliminaci�n) el registro de la cach�
            // permanece, pues aunque el elemento cliente eliminado y el id no vuelven a reutilizarse
            // evitamos que la memoria crezca indefinidamente en cliente y servidor.

            // En carga y fast load en teor�a no es necesario porque si es as� es que estamos programando mal
            // pues si estamos eliminando un nodo que fue cacheado como no se genera c�digo JavaScript
            // de las mutaciones significa que en el cliente cuando se vaya a cachear el nodo dicho nodo NO estar�
            // en el �rbol. Si fue cacheado es que se uso para algo o se cambi� algo, porque la simple inserci�n
            // no cachea. Por eso nos interesa hacerlo en este caso cuando tenemos el modo debug con la �nica
            // finalidad de detectar errores en tiempo de desarrollo no por otra rz�n.

            Node removedNode = (Node)mutEvent.getTarget();
            removeTreeFromNodeCache(removedNode);
        }

        if (type.equals("DOMNodeInserted") &&
            !itsNatDoc.isLoadingPhaseAndFastLoadMode())
        {
            Node insertedNode = (Node)mutEvent.getTarget();
            processTreeInsertedElementDocContainer(false,insertedNode);
        }
    }

    public abstract Map preRenderAndSendMutationCode(MutationEvent mutEvent);

    public void postRenderAndSendMutationCode(MutationEvent mutEvent,Map context)
    {
        String type = mutEvent.getType();

        if (type.equals("DOMAttrModified"))
        {
            Element elem = (Element)mutEvent.getTarget();
            Attr attr = (Attr)mutEvent.getRelatedNode();

            boolean mustCallSetSrc = false;
            ElementDocContainer elemDocCont = ElementDocContainerWrapperImpl.getElementDocContainerIfURLAttr(attr,elem);
            if (elemDocCont != null)
            {
                if (elemDocCont.getElementDocContainerWrapper().isJavaApplet())
                {
                    mustCallSetSrc = true;
                }
                else if ((clientDoc.getBrowser() instanceof BrowserMSIE6) &&
                         ((elemDocCont instanceof HTMLObjectElement) ||
                          (elemDocCont instanceof ItsNatHTMLEmbedElement))) // Posiblemente ASV o Renesis
                {
                    mustCallSetSrc = true;
                }
            }

            if (mustCallSetSrc)
                callSetSrcInPlugin(mutEvent,elem,elemDocCont);
        }
    }

    private void callSetSrcInPlugin(MutationEvent mutEvent,Element elem,ElementDocContainer elemDocCont)
    {
        // Hay varios casos:
        // 1) Applet y elem es un HTMLParamElement hijo de <applet> o <object> y el atributo es el "value" del <param name="src" value="url" />
        // Luego estamos cambiando el atributo "value" de este <param> de un <applet>
        // El cambio del src no cambia para nada el documento cargado <applet> pues
        // este src es un invento de ItsNat que s�lo es �til en el applet Batik modificado para ItsNat.
        // Este applet tiene un m�todo setSrc(url), ese m�todo s� que es capaz de cambiar el
        // documento cargado por el applet Batik.
        // En ItsNat s�lo reconocemos este applet especial Batik, en vez de tratar
        // averiguar si es el applet Batik ItsNat, lo cual es dif�cil porque la configuraci�n de atributos del
        // applet tiene cierta libertad (salvo que la acotemos) y as� permitimos posibles cambios
        // en el empaquetamiento del applet, llamamos a setSrc(url) con un try/catch
        // y ya est� y as� podemos soportar "autom�ticamente" otros futuros <applet> que
        // tengan un comportamiento similar tambi�n con setSrc y applets que "casualmente" (mucha casualidad) tienen este <param> especial
        // pero que no tienen el m�todo setSrc(url).

        // 2) Applet en <embed>, en este caso el cambio del atributo src
        // o bien no funciona como es el caso de MSIE o bien da problemas
        // como es el caso de FireFox (extra�o cacheado), Chrome y Safari (el antiguo documento parece que no se quita visualmente)
        // por lo que adem�s llamamos a setSrc que soluciona todo.

        // 3) ASV o Renesis o Savarese Ssrc en <object> o <embed> (s�lo MSIE carga ActiveX)
        //    el cambio del atributo/propiedad src no es suficiente,
        //    ASV define setSrc(url), Renesis loadFromURI y Ssrc Navigate.
        //    El loadFromURI de Renesis 1.1.1 ha sido obtenido provocando un error en MSIE v8
        //    lanzando el depurador, aunque parezca asociado a "contentDocument" est� definido a nivel
        //    del objeto ActiveX.

        StringBuffer code = new StringBuffer();

        String refJS = clientDoc.getNodeReference(elem, true, true);

        code.append("var elem = " + refJS + ";");

        int changeType = mutEvent.getAttrChange();
        switch(changeType)
        {
            case MutationEvent.ADDITION:
            case MutationEvent.MODIFICATION:
                code.append("var value = itsNatDoc.getAttribute(elem,\"" + elemDocCont.getElementDocContainerWrapper().getURLAttrName() + "\");"); // El valor del atributo ya se defini� antes en la fase de renderizado del atributo
                break;
            case MutationEvent.REMOVAL: // Raro pero por si acaso
                code.append("var value = \"\";");
                break;
            // No hay m�s casos
        }

        if (elem instanceof HTMLParamElement) // <applet> y <object>, en caso contrario es <embed>
            code.append("var elem = itsNatDoc.getParentNode(elem);"); // elem ahora es el <applet>

        code.append("try{");
        if (elemDocCont.getElementDocContainerWrapper().isJavaApplet())
            code.append("if (typeof elem.setSrc != \"undefined\") elem.setSrc(value);"); // S�lo soportamos el Applet Batik ItsNat que tiene este m�todo, si fuera otro applet (muy raro llegar hasta aqu�) capturamos el error y no pasa nada
        else // ASV, Renesis o Savarese Ssrc.
        {
            code.append("if (typeof elem.setSrc != \"undefined\") elem.setSrc(value);"); // ASV
            code.append("else if (typeof elem.loadFromURI != \"undefined\") elem.loadFromURI(value);"); // Renesis
            code.append("else if (typeof elem.Navigate != \"undefined\")"); // Savarese Ssrc
            code.append("  if (value == elem.LocationURL) elem.Refresh(); else elem.Navigate(value);"); // La llamada a Refresh() asegura que la request se realiza (ignora el cach�) http://msdn.microsoft.com/en-us/library/aa752098%28VS.85%29.aspx
        }
        code.append("}catch(e){}\n"); // Por si acaso, probablemente es un plugin o applet desconocido por ItsNat pero que nos ha confundido

        clientDoc.addCodeToSend(code.toString());
    }

    private void removeTreeFromNodeCache(Node node)
    {
        // Quitamos los nodos del trozo de �rbol del cach� (si hay) porque cuando insertamos
        // un nodo al documento en el cliente enviamos siempre la "regeneraci�n"
        // via DOM del nodo porque no detectamos cambios en nodos que no est�n
        // vinculados al Document, sin embargo el nodo en el servidor que se inserta puede ser
        // un nodo que previamente se quitara del �rbol (y conservamos) y que si no quitamos
        // de la cach� aqu� estar�a en la cache posiblemente, pero en el cliente el nodo ser� uno nuevo,
        // as� desde el punto de vista  de la cach� el nodo insertado es nuevo.

        LinkedList idList = null;
        if (clientDoc.isSendCodeEnabled())
            idList = new LinkedList();

        removeTreeFromNodeCache(node,idList);

        if ((idList != null)&& !idList.isEmpty())
        {
            clientDoc.addCodeToSend(JSRenderNodeImpl.removeNodeFromCache(idList));   // El c�digo generado es compatible con todos los navegadores

            /*
             Un nodo se quita de la cach� cuando se elimina del documento DOM
             (salvo alg�n en un caso especial de "preventive caching"),
             si esto se ejecuta en tiempo de carga y fastLoad = true significa
             que el nodo que se cachear� (y descachear�) en el cliente no
             estar� presente en el DOM de carga.
             Esto es debido a que el programador ha utilizado el nodo de alguna
             manera (por ejemplo usando ScriptUtil.getNodeReference()) pues
             en inserci�n no hay cacheado.
             En el caso de error NO evitamos que se env�e el c�digo JavaScript al cliente
             pues nos interesa que de error (aunque en este caso no ocurre) pues se da el problema
             de la captura de excepciones en el proceso de los mutation events del Batik DOM (afortunadamente el error se ve en la consola)
             */
            ItsNatStfulDocumentImpl itsNatDoc = clientDoc.getItsNatStfulDocument();
            if (itsNatDoc.isLoadingPhaseAndFastLoadMode())
                throw new ItsNatDOMException("A cached DOM node is being removed on load time in fast load mode. Avoid any access to this node or avoid removing in load time (use a load event instead) or disable fast load mode.",node);
        }
    }

    private void removeTreeFromNodeCache(Node node,LinkedList idList)
    {
        String id = clientDoc.removeNodeFromCache(node);
        if ((id != null)&&(idList != null))
            idList.add(id);

        Node child = node.getFirstChild();
        while (child != null)
        {
            removeTreeFromNodeCache(child,idList);
            child = child.getNextSibling();
        }
    }

    protected void processTreeInsertedElementDocContainer(boolean beforeRender,Node node)
    {
        if (node instanceof ElementDocContainer)
        {
            // En el caso de isLoading y FastLoadMode no es necesario
            // porque lo que interesa es el markup finalmente enviado al cliente
            // pues es el cliente el que va a cargar el iframe/object/embed, los
            // estados intermedios del DOM no nos interesan. En este caso
            // el proceso ya se hace al final, al serializar.
            ElementDocContainer elem = (ElementDocContainer)node;
            BoundElementDocContainerImpl bindInfo = elem.getElementDocContainerWrapper().getBoundElementDocContainer();

            if (bindInfo != null)
            {
                DocMutationEventListenerStfulImpl docMut = getDocMutationEventListenerStful();
                boolean modeOld = docMut.isEnabled();
                docMut.setEnabled(false);
                if (beforeRender)
                {
                    // Un iframe/object/embed/applet binding ha sido registrado, modificamos el atributo que define el URL
                    // para que al renderizar se env�e el nuevo
                    if (clientDoc instanceof ClientDocumentStfulOwnerImpl)
                        bindInfo.setURLForClientOwner((ClientDocumentStfulOwnerImpl)clientDoc);
                    else // attached
                        bindInfo.setURLForClientAttached((ClientDocumentAttachedClientImpl)clientDoc);
                }
                else // after
                {
                    // Como fue cambiado el URL, hay que restaurar
                    bindInfo.restoreOriginalURL(clientDoc);
                }
                docMut.setEnabled(modeOld);
            }
        }

        Node child = node.getFirstChild();
        while (child != null)
        {
            processTreeInsertedElementDocContainer(beforeRender,child);
            child = child.getNextSibling();
        }
    }

    public void removeAllChild(Node node)
    {
        if (canRenderAndSendMutationJSCode()) // Por si acaso est� desactivado el enviar c�digo en este cliente
        {
            JSRenderNotAttrOrViewNodeImpl render = (JSRenderNotAttrOrViewNodeImpl)JSRenderNodeImpl.getJSRenderNode(node,clientDoc);
            String code = render.getRemoveAllChildCode(node,clientDoc);
            clientDoc.addCodeToSend(code);
        }
    }
}
