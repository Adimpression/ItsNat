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

import java.util.Map;
import org.itsnat.impl.core.browser.opera.BrowserOpera8Mobile;
import org.itsnat.impl.core.clientdoc.ClientDocumentStfulImpl;
import org.itsnat.impl.core.event.EventListenerInternal;
import org.itsnat.impl.core.jsren.dom.node.html.JSRenderHTMLPropertyImpl;
import org.itsnat.impl.core.jsren.dom.node.html.w3c.JSRenderHTMLElementOpera8MobileImpl;
import org.w3c.dom.Node;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.MutationEvent;
import org.w3c.dom.html.HTMLCollection;
import org.w3c.dom.html.HTMLOptionElement;
import org.w3c.dom.html.HTMLSelectElement;

/**
 *
 * @author jmarranz
 */
public class ClientMutationEventListenerHTMLOpera8MobileImpl extends ClientMutationEventListenerHTMLImpl
{
    public ClientMutationEventListenerHTMLOpera8MobileImpl(ClientDocumentStfulImpl clientDoc)
    {
        super(clientDoc);
    }

    public void postRenderAndSendMutationCode(MutationEvent mutEvent,Map context)
    {
        super.postRenderAndSendMutationCode(mutEvent,context);

        ClientDocumentStfulImpl clientDoc = getClientDocumentStful();
        BrowserOpera8Mobile browser = (BrowserOpera8Mobile)clientDoc.getBrowser();

        String type = mutEvent.getType();
        if ( browser.isHTMLSelectSelectedPropBuggy() &&
              (type.equals("DOMNodeInserted")||
               type.equals("DOMNodeRemoved")) )
        {
            Node node = (Node)mutEvent.getTarget(); // node es el nuevo o a eliminar
            if (node instanceof HTMLOptionElement)
            {
                HTMLSelectElement select = (HTMLSelectElement)node.getParentNode();
                fixHTMLSelectSelectedProperties(select);
            }
        }

        if (browser.isInputCheckBoxSurplusFocusBlur() &&
            type.equals("DOMNodeInserted"))
        {
            Node newNode = (Node)mutEvent.getTarget();
            JSRenderHTMLElementOpera8MobileImpl.fixTreeHTMLInputCheckBoxFocusBlur(newNode,clientDoc);
        }
    }

    private void fixHTMLSelectSelectedProperties(final HTMLSelectElement select)
    {
        // Este c�digo soluciona un problema muy raro de la v8.60 tanto WinCE
        // como UIQ.
        // Cuando se carga la p�gina conteniendo un <select> con alg�n atributo "selected"
        // definido, dicho atributo es como si quedara fijado (de hecho el valor
        // del atributo cambia a true) tal que si insertamos nuevos elementos,
        // aunque se desplacen los option la selecci�n antigua permanece, seleccionando
        // el option que ahora ocupa el lugar del option que estaba seleccionado inicialmente.
        // Esto no ocurre cuando el <select> es insertado din�micamente tras la carga.
        // El problema es muy raro y no lo entiendo del todo, la simple reinserci�n del <select>
        // en tiempo de carga no es suficiente, ni con un clone, pues es la combinaci�n entre la presencia
        // de un atributo selected y el proceso de carga lo que fastidia todo o quiz�s
        // s�lo la presencia del atributo selected pues cuando insertamos din�micamente
        // no se define el atributo o bien sencillamente que hay un bug m�s complicado que todo esto.
        // pues el cambio de la propiedad "selected" tambi�n influye en todo esto.
        // Emitir c�digo "redefinidor" de las propiedades despu�s de la inserci�n (c�digo JS seguido)
        // no es suficiente, pues el cambio err�neo de la selecci�n se hace cuando renderiza
        // los cambios, por ello la soluci�n es emitir un continue event, como se emite
        // as�ncronamente el select ya est� renderizado aunque sea mal.

        // El <select> combo puro (no "size" ni "multiple") sigue estando loco
        // en Opera Mobile 8.60 UIQ (3.0) si se inserta un option como parte de un change
        // sin embargo aunque el seleccionado que queda es diferente al seleccionado manualmente
        // existe sincronizaci�n entre servidor y cliente (se puede ver con un visor control remoto).

        EventListener listener = new EventListenerInternal()
        {
            public void handleEvent(Event evt)
            {
                StringBuffer code = new StringBuffer();

                code.append( "var elem = " + clientDoc.getNodeReference(select,true,true) + ";\n" );

                // El HTMLCollection no es muy eficiente iterando pero hay que tener en cuenta los <optgroup>
                HTMLCollection options = select.getOptions();
                int len = options.getLength();
                for(int i = 0; i < len; i++)
                {
                    String opref = "elem.options[" + i + "]";
                    HTMLOptionElement option = (HTMLOptionElement)options.item(i);
                    code.append( JSRenderHTMLPropertyImpl.renderUIControlProperty(option,opref,"selected",clientDoc) );
                }

                clientDoc.addCodeToSend(code.toString());
            }
        };
        clientDoc.addContinueEventListener(null, listener);
    }


}
