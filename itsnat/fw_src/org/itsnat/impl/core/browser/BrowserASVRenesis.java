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

import java.util.Map;
import org.itsnat.impl.core.doc.ItsNatStfulDocumentImpl;
import org.w3c.dom.html.HTMLElement;

/**
  Adobe SVG Viewer Plug-in desde la v3 (ASV3) incluida la v6 beta
  Renesis Player y Plug-in desde la v1.1.1 (serie 2)

  Adobe SVG Viewer:

  El plug-in SVG de Adobe est� muy vinculado con el MSIE_OLD
  sando el mismo user agent y los mismos headers (de hecho comparte los cookies)
  y soporta AJAX a trav�s del MSIE_OLD.
  Sin embargo a pesar de que usa el motor JavaScript del
  MSIE_OLD, el ASV3 es un navegador b�sicamente W3C.
  No hay posibilidad de detectar un request del ASV por lo que
  consideramos que es un ASV cuando se invoca un documento SVG desde el MSIE_OLD

  Renesis:

  El caso Renesis es id�ntico, no tenemos forma de detectar en carga
  si es un ASV o Renesis. Por tanto tratamos de soportar ambos a la vez.
  El Renesis tiene problemas importantes en el DOM y a veces no refresca
  ante cambios (quiz�s se podr�a forzar con forceRedraw)

  NO usar el Player "standalone" no integrado con el navegador, tiene un fallo
  que hace que pierda window el m�todo constructor ActiveXObject inexplicablemente. Esto no ocurre en el plugin.

 *
 * @author jmarranz
 */
public class BrowserASVRenesis extends BrowserW3C
{
    /**
     * Creates a new instance of BrowserNetFront
     */
    public BrowserASVRenesis(String userAgent)
    {
        super(userAgent);

        this.browserType = ASV_RENESIS;
    }

    public boolean isMobile()
    {
        return false;
    }

    public boolean hasBeforeUnloadSupport(ItsNatStfulDocumentImpl itsNatDoc)
    {
        return false;
    }

    public boolean isReferrerReferenceStrong()
    {
        // No funciona la navegaci�n desde el SVG por lo que nos
        // da igual los referrers.
        return false;
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

    public boolean isBlurBeforeChangeEvent(HTMLElement formElem)
    {
        return false;
    }

    public boolean isFocusOrBlurMethodWrong(String methodName,HTMLElement formElem)
    {
        return false;
    }

    public Map getHTMLFormControlsIgnoreZIndex()
    {
        // No reconoce XHTML embebido.
        return null;
    }

    public boolean hasHTMLCSSOpacity()
    {
        // S�lo SVG
        return false;
    }

    public boolean isSetTimeoutSupported()
    {
        // window.setTimeout funciona aunque "raro" (el c�digo debe ser una string)
        // se soluciona en tiempo de carga.
        return true;
    }

    public boolean canNativelyRenderOtherNSInXHTMLDoc()
    {
        return false; // Renderiza SVG pero no el propio XHTML.
    }

    public boolean isTextAddedToInsertedHTMLScriptNotExecuted()
    {
        return false;
    }

    public boolean isInsertedSVGScriptNotExecuted()
    {
        // En ASV (ambas v3 y v6) no funciona ni insertado antes ni despu�s
        // En Renesis 1.1.1 es igual.
        return true;
    }

    public boolean isTextAddedToInsertedSVGScriptNotExecuted()
    {
        return true;
    }

    public boolean isClientWindowEventTarget()
    {
        return false;
    }

    public boolean isNeededAbsoluteURL()
    {
        // En ASV no es necesario pero en Renesis las llamadas AJAX
        // necesitan URLs absolutos.
        return true;
    }
}
