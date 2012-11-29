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

package org.itsnat.impl.comp.inplace;

import org.itsnat.comp.ItsNatComponent;
import org.itsnat.core.event.ItsNatDOMStdEvent;
import org.itsnat.core.event.ItsNatEvent;
import org.itsnat.impl.core.browser.Browser;
import org.itsnat.impl.core.browser.BrowserBlackBerryOld;
import org.itsnat.impl.core.browser.BrowserGecko;
import org.itsnat.impl.core.browser.BrowserGeckoUCWEB;
import org.itsnat.impl.core.browser.opera.BrowserOpera8Mobile;
import org.itsnat.impl.core.browser.opera.BrowserOpera9Mini;
import org.itsnat.impl.core.browser.webkit.BrowserWebKitBolt;
import org.itsnat.impl.core.browser.webkit.BrowserWebKitIPhone;
import org.itsnat.impl.core.clientdoc.ClientDocumentStfulImpl;
import org.itsnat.impl.core.doc.ItsNatStfulDocumentImpl;
import org.itsnat.impl.core.domutil.DOMUtilHTML;
import org.itsnat.impl.core.jsren.JSRenderMethodCallImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.html.HTMLElement;
import org.w3c.dom.html.HTMLLabelElement;
import org.w3c.dom.html.HTMLOptionElement;

/**
 *
 * @author jmarranz
 */
public abstract class ItsNatCellEditorClientImpl
{
    public ItsNatCellEditorClientImpl()
    {
    }

    public static ItsNatCellEditorClientImpl getItsNatHTMLCellEditorClient(Browser browser,ItsNatComponent compEditor)
    {
        if (compEditor.getNode() instanceof HTMLElement)
        {
            if (browser instanceof BrowserOpera9Mini)
                return ItsNatHTMLCellEditorClientOpera9MiniImpl.SINGLETON;
            else if (browser instanceof BrowserWebKitBolt)
                return ItsNatHTMLCellEditorClientWebKitBoltImpl.SINGLETON;
            else if (browser instanceof BrowserGeckoUCWEB)
                return ItsNatHTMLCellEditorClientGeckoUCWEBImpl.SINGLETON;
            else
                return ItsNatCellEditorClientDefaultImpl.SINGLETON;
        }
        else
            return ItsNatCellEditorClientDefaultImpl.SINGLETON;
    }

    public void handleGlobalEvent(Event evt,ItsNatCellEditorImpl parent)
    {
        // No consideramos eventos no est�ndar DOM pues en teor�a no tendr�an porqu�
        // provocar un blur. De hecho el ejemplo del test funcional del Feat. Show lanza un continue
        // event para recoger el valor de retorno del dispatch, si lo consider�ramos para el blur
        // cerrar�amos la edici�n inplace err�neamente antes de que se env�e el blur.
        // Es el caso tambi�n de un timer, no deber�a cerrar el editor.
        if (evt instanceof ItsNatDOMStdEvent)
        {
            ItsNatComponent compEditor = parent.getCellEditorComponent();
            Node nodeEditor = compEditor.getNode();
            EventTarget target = evt.getTarget();
            if (target == nodeEditor) return;

            if ((target instanceof HTMLOptionElement) &&
                (((Node)target).getParentNode() == nodeEditor))
            {
                // En BlackBerry un <option> es el target cuando una opci�n de un select es pulsada (curioso)
                ClientDocumentStfulImpl clientDoc = (ClientDocumentStfulImpl)((ItsNatEvent)evt).getClientDocument();
                Browser browser = clientDoc.getBrowser();
                if (browser instanceof BrowserBlackBerryOld) return;
            }

            parent.stopCellEditing();
        }
    }

    public void handleEvent(Event evt,ItsNatCellEditorImpl parent,ClientDocumentStfulImpl clientDoc)
    {
    }

    public void registerEventListeners(ItsNatCellEditorImpl compParent,ClientDocumentStfulImpl clientDoc)
    {
        ItsNatComponent compEditor = compParent.getCellEditorComponent();

        Browser browser = clientDoc.getBrowser();
        ItsNatStfulDocumentImpl itsNatDoc = (ItsNatStfulDocumentImpl)clientDoc.getItsNatDocument();
        Document doc = itsNatDoc.getDocument();
        Element nodeEditor = (Element)compEditor.getNode(); // S�lo admitimos elementos por ahora

        clientDoc.addCodeToSend("var nodeEditor = " + clientDoc.getNodeReference(nodeEditor,true,true) + ";\n");

        StringBuffer codeListener = new StringBuffer();
        codeListener.append( "event.setMustBeSent(false);\n" ); // Sirve para evitar que se env�e el evento click, ya se env�a un evento blur
        codeListener.append( "try{" );
        codeListener.append( "var node = arguments.callee.nodeEditor;\n" );
        codeListener.append( "var target = event.getTarget();\n" );
        if (browser instanceof BrowserGecko)
        {
            // SkyFire v1.0 tiene un error muy tonto en el evento click checkbox,
            // el "target" del evento es siempre el documento en vez del propio elemento,
            // en nuestro caso el click normal de cambio de valor del checkbox provocar�a
            // el env�o de un blur err�neo que har�a perder el cambio (aunque detr�s de SkyFire hay un FireFox 2.x
            // esto no ocurre en el FireFox 2.0 al menos).
            // Probando en SkyFire v1.0 sabemos que el "target" de un click normal
            // en el �rea de la p�gina es el elemento <body> como el m�s alto,
            // nunca el documento salvo en este caso err�neo, por tanto si target
            // es document es que estamos en este caso err�neo.
            if (((BrowserGecko)browser).isSkyFire() &&
                 DOMUtilHTML.isHTMLInputCheckBox(nodeEditor))
                codeListener.append( "if (target == document) return;\n" );
        }
        else if (browser instanceof BrowserOpera8Mobile)
        {
            // En el caso de <label> en Opera Mobile 8.6x forzamos el env�o de
            // un click cuando pulsamos ENTER sobre un <label>, esto nos sirve
            // para activar el edition in place pero cuando ya est� edit�ndose
            // tenemos que evitar que al usar el ENTER en edici�n quite accidentalmente el editor
            // a trav�s de este listener. De todas formas no es muy importante
            // la edici�n in place con <label> no es recomendada y no funciona del todo bien en Opera Mobile 8.x usando cursores.
            // Ver JSRenderItsNatDOMStdEventListenerOpera8MobileImpl para m�s informaci�n.

            if (nodeEditor.getParentNode() instanceof HTMLLabelElement)
                codeListener.append( "if (node.parentNode == target) return;\n" );
        }

        codeListener.append( "if (node == target) return;\n" ); // Es un click dirigido al propio elemento edit�ndose.

        // El m�todo getCallBlurFocusFormControlCode llamar� a blur() o enviar� un evento "blur" manualmente seg�n si
        // blur() es procesado correctamente o ignorado/err�neo, si focus() no lo es lo normal es que el blur() es ignorado salvo que el usuario
        // haya fijado el foco manualmente. En dichos casos (focus() no ejecutado) se env�a un evento "blur" que asegura que el editor se quita
        // aunque el control no haya tenido nunca el foco (ni por focus() ni pulsando el usuario).
        JSRenderMethodCallImpl render = JSRenderMethodCallImpl.getJSRenderMethodCall(nodeEditor);
        codeListener.append(render.getCallBlurFocusFormControlCode(nodeEditor,"node","blur",clientDoc));
        codeListener.append( "}catch(e){}\n" ); // el try/catch es por si el nodo se hubiera eliminado antes y el evento est� pendiente todav�a

        String bindToListener = "nodeEditor = nodeEditor";

        clientDoc.addEventListener((EventTarget)doc,"click", compParent, true,clientDoc.getCommMode(),null, codeListener.toString(),clientDoc.getEventTimeout(),bindToListener);

        if (browser instanceof BrowserWebKitIPhone)
        {
            // En iPhone los eventos de rat�n no llegan al document, <body> o <html>
            // si el elemento pulsado "no es clickable" es decir si no tiene un listener
            // asociado (el propio <body> aunque tenga listener es ignorado si se pulsa en su �rea).
            // Una alternativa es usar los eventos "touch" introducidos en iPhone 2.0 que funcionan
            // casi igual que los eventos de rat�n en un browser de desktop
            // Sin embargo no pueden substituir a los eventos de rat�n porque hay diferencias por ejemplo al pulsar
            // un link no se emiten (quiz�s porque el comportamiento normal de un link es abandonar la p�gina)
            // de otra manera podr�amos usar estos eventos en el cliente y enga�ar al servidor haciendo creer
            // que son mouse events.
            // Por ahora el "touchend" ser� tratado como un evento unknown aunque
            // en futuro podr�amos hacerlo p�blico con interfases propias, env�o desde el servidor etc
            // pues por ejemplo tiene informaci�n del punto pulsado etc
            // (al igual que los dem�s "touchstart", "touchmove" "touchcancel" etc).

            // http://www.sitepen.com/blog/2008/07/10/touching-and-gesturing-on-the-iphone/
            // http://rossboucher.com/2008/08/19/iphone-touch-events-in-javascript/
            // http://developer.yahoo.com/yui/3/event/
            clientDoc.addEventListener((EventTarget)doc,"touchend", compParent, true,clientDoc.getCommMode(),null, codeListener.toString(),clientDoc.getEventTimeout(),bindToListener);
        }
    }

    public void unregisterEventListeners(ItsNatCellEditorImpl parent,ClientDocumentStfulImpl clientDoc)
    {
        Browser browser = clientDoc.getBrowser();
        ItsNatStfulDocumentImpl itsNatDoc = (ItsNatStfulDocumentImpl)clientDoc.getItsNatDocument();
        Document doc = itsNatDoc.getDocument();

        clientDoc.removeEventListener((EventTarget)doc,"click", parent, true);

        if (browser instanceof BrowserWebKitIPhone)
            clientDoc.removeEventListener((EventTarget)doc,"touchend", parent, true);
    }
}
