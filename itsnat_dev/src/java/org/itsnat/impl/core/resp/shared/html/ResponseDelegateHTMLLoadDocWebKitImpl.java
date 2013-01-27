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

import org.itsnat.impl.core.browser.Browser;
import org.itsnat.impl.core.browser.webkit.BrowserWebKit;
import org.itsnat.impl.core.browser.webkit.BrowserWebKitAndroid;
import org.itsnat.impl.core.browser.webkit.BrowserWebKitS40;
import org.itsnat.impl.core.clientdoc.ClientDocumentStfulImpl;
import org.itsnat.impl.core.resp.ResponseLoadStfulDocumentValid;

/**
 *
 * @author jmarranz
 */
public abstract class ResponseDelegateHTMLLoadDocWebKitImpl extends ResponseDelegateHTMLLoadDocW3CImpl
{
    public ResponseDelegateHTMLLoadDocWebKitImpl(ResponseLoadStfulDocumentValid response)
    {
        super(response);
    }

    public static ResponseDelegateHTMLLoadDocWebKitImpl createResponseDelegateLoadHTMLDocWebKit(ResponseLoadStfulDocumentValid responseParent)
    {
        BrowserWebKit browser = (BrowserWebKit)responseParent.getClientDocument().getBrowser();
        if (browser instanceof BrowserWebKitAndroid)
            return new ResponseDelegateHTMLLoadDocWebKitAndroidImpl(responseParent);
        else
            return new ResponseDelegateHTMLLoadDocWebKitDefaultImpl(responseParent);
    }

    public String serializeDocument()
    {
        BrowserWebKit browser = (BrowserWebKit)getClientDocumentStful().getBrowser();
        if (browser.isFilteredCommentsInMarkup())
        {
            // Los comentarios son filtrados en la primera versi�n del S60WebKit (incluido en el S60 3rd Feature Pack 1), 
            // y BlackBerry.
            // Esto no ocurre en el S60WebKit FP 2 pero la soluci�n adoptada en teor�a funciona aunque no se filtren los comentarios
            // Ver notas en ResponseNormalLoadHTMLDocBlackBerryImpl sobre la estrategia

            ResponseDelegateHTMLLoadDocFixFilteredCommentsImpl filter = new ResponseDelegateHTMLLoadDocFixFilteredCommentsImpl(this);

            filter.preSerializeDocument();

            String docMarkup = super.serializeDocument();

            docMarkup = filter.postSerializeDocument(docMarkup);

            return docMarkup;
        }
        else
            return super.serializeDocument();
    }

    public void dispatchRequestListeners()
    {
        ClientDocumentStfulImpl clientDoc = getClientDocumentStful();
        BrowserWebKit browser = (BrowserWebKit)clientDoc.getBrowser();
        if (browser.isChangeNotFiredHTMLSelectWithSizeOrMultiple(null))
            fixDOMHTMLSelectWithSizeOrMultipleElementsChangeNotFired();

        super.dispatchRequestListeners();
    }

    public void fixDOMHTMLSelectWithSizeOrMultipleElementsChangeNotFired()
    {
        // Solucionamos el error del S40WebKit, iPhone 2.0 y el Android v1 r2 (y anteriores) de que en los <select>
        // con atributos multiple o size no lanzan el evento change
        // Detectamos blur para lanzar el change antes de ser procesado.
        // http://lists.apple.com/archives/safari-iphone-web-dev/2008/Jul/msg00025.html
        // http://groups.google.com/group/iphonewebdev/browse_thread/thread/33288230c30cbd53

        ClientDocumentStfulImpl clientDoc = getClientDocumentStful();
        if (!clientDoc.isScriptingEnabled())
            return;

        StringBuffer code = new StringBuffer();

        code.append("var func = function (evt)");
        code.append("{");
        code.append("  if (\"select\" != evt.target.localName) return;");
        code.append("  if (!evt.target.hasAttribute(\"multiple\")&&!evt.target.hasAttribute(\"size\")) return;");
        code.append("  var evtTmp = document.createEvent(\"UIEvents\");");
        code.append("  evtTmp.initUIEvent(\"change\",true,true,window,0);");
        code.append("  evt.target.dispatchEvent(evtTmp);");
        code.append("};");

        String type;
        Browser browser = clientDoc.getBrowser();
        if (browser instanceof BrowserWebKitS40) type = "click";
        else type = "blur"; // En Android el click es ignorado pues hay un editor especial, idem en iPhone adem�s hasta el blur el estado no es correcto

        code.append("document.addEventListener(\"" + type + "\",func,true);"); // Notar que es capture para asegurar que se ejecuta el primero de todo

        addFixDOMCodeToSend(code.toString());
    }
}

