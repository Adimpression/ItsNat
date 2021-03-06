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

package org.itsnat.impl.core.event.client.domstd;

import org.itsnat.impl.core.browser.Browser;
import org.itsnat.impl.core.browser.BrowserAdobeSVG;
import org.itsnat.impl.core.browser.BrowserBlackBerryOld;
import org.itsnat.impl.core.browser.BrowserGecko;
import org.itsnat.impl.core.browser.opera.BrowserOpera;
import org.itsnat.impl.core.browser.webkit.BrowserWebKit;
import org.itsnat.impl.core.clientdoc.ClientDocumentImpl;
import org.itsnat.impl.core.event.DOMStdEventTypeInfo;
import org.itsnat.impl.core.event.client.domstd.w3c.BlackBerryOldKeyEventImpl;
import org.itsnat.impl.core.event.client.domstd.w3c.GeckoKeyEventImpl;
import org.itsnat.impl.core.event.client.domstd.w3c.OperaKeyEventImpl;
import org.itsnat.impl.core.event.client.domstd.w3c.W3CEventDefaultImpl;
import org.itsnat.impl.core.event.client.domstd.w3c.W3CEventImpl;
import org.itsnat.impl.core.event.client.domstd.w3c.W3CHTMLEventImpl;
import org.itsnat.impl.core.event.client.domstd.w3c.W3CMouseEventImpl;
import org.itsnat.impl.core.event.client.domstd.w3c.W3CMutationEventAdobeSVGImpl;
import org.itsnat.impl.core.event.client.domstd.w3c.W3CMutationEventDefaultImpl;
import org.itsnat.impl.core.event.client.domstd.w3c.W3CUIEventDefaultImpl;
import org.itsnat.impl.core.event.client.domstd.w3c.WebKitKeyEventImpl;
import org.itsnat.impl.core.listener.domstd.ItsNatDOMStdEventListenerWrapperImpl;
import org.itsnat.impl.core.req.norm.RequestNormalEventImpl;

/**
 *
 * @author jmarranz
 */
public class ClientW3CEventFactory extends ClientItsNatDOMStdEventFactory
{

    /**
     * Creates a new instance of ClientW3CEventFactory
     */
    public ClientW3CEventFactory(RequestNormalEventImpl request)
    {
        super(request);
    }

    public static ClientW3CEventFactory createW3CEventFactory(RequestNormalEventImpl request)
    {
        return new ClientW3CEventFactory(request);
    }

    public ClientItsNatDOMStdEventImpl createClientItsNatDOMStdEvent(int typeCode,ItsNatDOMStdEventListenerWrapperImpl evtListener)
    {
        ClientDocumentImpl clientDoc = request.getClientDocument();

        Browser browser = clientDoc.getBrowser();
        W3CEventImpl event = null;
        switch(typeCode)
        {
            case DOMStdEventTypeInfo.UNKNOWN_EVENT:
                event = new W3CEventDefaultImpl(evtListener,request);
                break;
            case DOMStdEventTypeInfo.UI_EVENT:
                event = new W3CUIEventDefaultImpl(evtListener,request);
                break;
            case DOMStdEventTypeInfo.MOUSE_EVENT:
                event = new W3CMouseEventImpl(evtListener,request);
                break;
            case DOMStdEventTypeInfo.HTML_EVENT:
                event = new W3CHTMLEventImpl(evtListener,request);
                break;
            case DOMStdEventTypeInfo.MUTATION_EVENT:
                if (browser instanceof BrowserAdobeSVG) // ASV v6 (v3 no tiene mutation events)
                    event = new W3CMutationEventAdobeSVGImpl(evtListener,request);
                else
                    event = new W3CMutationEventDefaultImpl(evtListener,request);
                break;
            case DOMStdEventTypeInfo.KEY_EVENT:
                if (browser instanceof BrowserGecko)
                    event = new GeckoKeyEventImpl(evtListener,request);
                else if (browser instanceof BrowserWebKit)
                    event = new WebKitKeyEventImpl(evtListener,request);
                else if (browser instanceof BrowserOpera)
                    event = new OperaKeyEventImpl(evtListener,request);
                else if (browser instanceof BrowserBlackBerryOld)
                    event = new BlackBerryOldKeyEventImpl(evtListener,request);
                else // Desconocido
                    event = new GeckoKeyEventImpl(evtListener,request);
                break;
        }

        return event;
    }

}
