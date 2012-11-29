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

package org.itsnat.impl.core.browser;

import java.util.HashMap;
import java.util.Map;
import org.itsnat.impl.core.doc.ItsNatStfulDocumentImpl;
import org.itsnat.impl.core.domutil.DOMUtilHTML;
import org.w3c.dom.html.HTMLElement;

/**
 * Desde la versi�n 6 (incluyendo 7 y 8) y la versi�n m�vil 6
 *
 *  Sobre IE Mobile 6 (desde WinMob 6.1.4)
 *  http://download.microsoft.com/download/8/c/c/8cc611e7-ec6b-49ee-af4a-415559d53262/Internet%20Explorer%20Mobile%206.pdf
 *  http://blog.enterprisemobile.com/2009/03/windows-mobile-6-1-x-upgrades-now-available-2/
 *  http://myitforum.com/cs2/blogs/mnielsen/archive/2009/01/31/windows-mobile-6-x-akus.aspx
 *
 *
 * Ejemplos User Agent Mobile:
 *   En modo "desktop": Mozilla/4.0 (compatible; MSIE_OLD 6.0; Windows NT 5.1)
 *   En modo "mobile":  Mozilla/4.0 (compatible; MSIE_OLD 6.0; Windows CE; IEMobile 8.12; MSIEMobile 6.0)
 *   Por tanto en modo desktop NO hay modo de distinguirlo del navegador de desktop.
 * Ejemplo User Agent Desktop:
 *   Mozilla/4.0 (compatible; MSIE_OLD 6.0; Windows NT 5.1; SV1; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30)
 *
 * @author jmarranz
 */
public class BrowserMSIE6 extends BrowserMSIEOld
{
    private static final Map tagNamesIgnoreZIndex = new HashMap();
    static
    {
        tagNamesIgnoreZIndex.put("select",null);
    }

    protected int version;

    /** Creates a new instance of BrowserMSIE6 */
    public BrowserMSIE6(String userAgent,int version,boolean mobile)
    {
        super(userAgent);

        if (mobile) this.browserSubType = MSIE_MOBILE;
        else this.browserSubType = MSIE_DESKTOP;
        
        this.version = version;
    }

    public boolean isMobile()
    {
        return (browserSubType == MSIE_MOBILE);
    }

    public boolean hasBeforeUnloadSupport(ItsNatStfulDocumentImpl itsNatDoc)
    {
        // Nota: el evento beforeunload se emite **siempre** cuando se pulsa un link
        // aunque tengamos en el href="javascript:;" o "javascript:void(0)" o "javascript:return false;"
        // es inevitable pues lo normal para MSIE_OLD es "abandonar" la p�gina via link
        // Da igual que el link tenga o no el evento click asociado (via AJAX o no AJAX, via attachEvent o onclick)
        // El href no puede evitar de alguna forma el evento beforeunload pues se ejecuta antes del href
        // http://www.jimandkatrin.com/codeblog/2007/08/linkbutton-updatepanel-onbeforenload.html
        // Formas de evitarlo:
        // 1) Poniendo un href="#" con el consabido problema de movimiento de la p�gina (scroll) al ppio
        // 2) A�adiendo un onclick="return false;" que evite el proceso del href (y antes evita el beforeunload)
        // De todas formas este es un problema menor
        return true;
    }

    public boolean isReferrerReferenceStrong()
    {
        // Aunque el back/forward NO est� cacheado en el cliente
        // sin embargo debemos devolver true porque est� el caso de recarga
        // v�a bot�n "Reload", en este caso se ejecuta el beforeunload Y SEGUIDO
        // el unload Y LUEGO la carga de la nueva p�gina. Por tanto el nuevo
        // documento NO es cargado antes de que el anterior se pierda en este caso.
        // Esto no ocurre cuando se "recarga" la misma p�gina a trav�s de un link (beforeunload, nuevo documento, unload del viejo)
        // sin embargo el reload al igual que el back y el forward son botones de "navegaci�n"
        // y cuantos m�s casos soportemos mejor.

        return true;
    }

    public boolean isCachedBackForward()
    {
        return false;
    }

    public boolean isCachedBackForwardExecutedScripts()
    {
        return false;
    }

    public boolean isDOMContentLoadedSupported()
    {
        return false;
    }

    public boolean isFocusOrBlurMethodWrong(String methodName,HTMLElement formElem)
    {
        // Tanto las versiones 6,7 y 8 ignoran la llamada focus() en los casos de input de texto y textarea
        // Si el focus() es ignorado un posterior blur() lo ser�a
        // Un segundo focus() s� es procesado.
        return DOMUtilHTML.isHTMLTextAreaOrInputTextBox(formElem);
    }

    public int getVersion()
    {
        return version;
    }

    public Map getHTMLFormControlsIgnoreZIndex()
    {
        if (version < 7) // Incluso el IE Mobile (WM 6.1.4) tiene el problema de los select no ocultables
            return tagNamesIgnoreZIndex;
        else
            return null;
    }

    public boolean hasHTMLCSSOpacity()
    {
        // Tiene opacidad aunque no sea la propiedad CSS "opacity"
        return true;
    }
}
