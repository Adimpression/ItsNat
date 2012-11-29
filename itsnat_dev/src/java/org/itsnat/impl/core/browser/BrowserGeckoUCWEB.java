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
import org.itsnat.impl.core.servlet.ItsNatServletRequestImpl;
import org.itsnat.impl.core.doc.ItsNatStfulDocumentImpl;
import org.itsnat.impl.core.domutil.DOMUtilHTML;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.html.HTMLAnchorElement;
import org.w3c.dom.html.HTMLButtonElement;
import org.w3c.dom.html.HTMLElement;
import org.w3c.dom.html.HTMLImageElement;
import org.w3c.dom.html.HTMLInputElement;
import org.w3c.dom.html.HTMLSelectElement;
import org.w3c.dom.html.HTMLTextAreaElement;

/**
 * Soportado desde UCWEB 6.0
 *
 * Antiguamente se llamaba ucfly y de hecho la web ucfly.com todav�a funciona.
 *
 * El user agent es complicado, hay que tener en cuenta que es el proxy el que lo
 * env�a pero depende del cliente usado.
 *
 * En la versi�n 6.0 Java (128K) el user agent es "Java/1.6.0_11" que es el Java
 * usado en el test con Microemulator y que veremos que coincide con la opci�n "Not Used"
 * de versiones posteriores.

 * En la versi�n 6.3 Java (128K) es posible cambiar el user agent:

    Not used (por defecto): "Java/1.6.0_11"  (por defecto)
    Phone UA: "microemulator" (pues ejecutamos el navegador usando Microemulator)
    OpenWave: "OPENWAVE"
    Opera: "Mozilla/4.0 (compatible; MSIE_OLD 4.0;) Opera"

 * En la versi�n 6.1 (y 6.3) Windows Mobile el user agent es fijo (no se puede cambiar):
 *  "Mozilla/4.0 (compatible; MSIE_OLD 4.01; Windows CE; PPC)"
 *
 * es decir, se identifica como un antiguo Pocket IE (http://blogs.msdn.com/iemobile/archive/2006/08/03/Detecting_IE_Mobile.aspx)
 *
 * De acuerdo a esta web, incluso en Symbian se ve este "user agent" (por la fecha de la noticia ser� la v6.3):
 * http://www.smartphonemag.com/cms/blog/9/review-comparison-another-web-browser-ucweb-it-any-good
 *
 *
 * Estos user agent son obviamente falsos, pues he descubierto que en el servidor
 * hay un Gecko, pues ejecutando el siguiente c�digo JavaScript:

    var msg = "";
    for(var name in navigator)
    try
    {
       msg += name + " - " + navigator[name] + "<br />";
    }
    catch(e)
    {
       msg += name + " - (ERROR)<br />";
    }
    elem.innerHTML = msg;

    Las propiedades que salen en pantalla son pr�cticamente id�nticas a las de un FireFox 3
    salvo algunas cambiadas (las referentes al user agent etc) para intentar simular un MSIE_OLD
    (al parecer se usa Linux de acuerdo con oscpu).
    Usamos el innerHTML de un elemento porque el alert() se ejecuta pero es ignorado (es un navegador proxy)

    M�s razones: el DOM es W3C inclu�dos los eventos y adem�s es un FireFox 3.x
    porque existe el m�todo document.elementFromPoint introducido en FireFox 3.x (Gecko 1.9)
    y que no existe en FireFox 2.x (comprobado), aunque esta propiedad es de MSIE_OLD tambi�n.

    El JavaScript no nos sirve para el user agent o detectar UCWEB en general pues
    en tiempo de carga debemos ya saber si es un UCWEB o no.

    Investigando los headers enviados he descubierto que en "accept" se env�a lo siguiente:
     "...;q=0.8,dn/263..-...737,...
    Lo del q=0.8 es lo de menos (m�s info aqu� http://www.thefutureoftheweb.com/blog/use-accept-language-header)
    lo importante es el c�digo que sigue a dn/ son dos n�meros en hexadecimal separados
    por un "-". Este dn/cod-cod est� presente en el "accept" de todas las versiones de UCWEB que he visto,
    el n�mero cambia pero es el mismo para cada instalaci�n del navegador pues se mantiene al
    re-ejecutar, todo apunta a que "dn" significa "device number".
    Por tanto el header accept es usado para identificar el navegador, es m�s,
    tambi�n se env�a el tama�o de la pantalla por ejemplo con "q=0.6,ss/220x294" (ss = screen size)
    En la versi�n Java 6.0 se incluye tambi�n la palabra UCWEB en el accept, pero no ocurre en todas
    las versiones.

    Conclusi�n: el "dn/" vamos a utilizarlo para detectar UCWEB en vez del user agent,
    por ahora solamente "dn/" si otro navegador utilizara el mismo sistema (rar�simo) recurrir�amos
    a estudiar el patr�n del c�digo.

   Versi�n 7 Windows Mobile: Aqu� por fin se a�ade UCWEB en el user-agent:
     Mozilla/4.0 (compatible; MSIE_OLD 4.01; Windows CE; PPC)/UCWEB7.0.0.41/31/352
     El problema es que el funcionamiento de JavaScript de la v7 Win Mobile ES UN COMPLETO DESASTRE
     POR LO QUE NO ESTA SOPORTADO EN ITSNAT.

   No he encontrado versi�n oficial v7 para Java

 * @author jmarranz
 */
public class BrowserGeckoUCWEB extends BrowserGecko
{
    protected boolean java;

    public BrowserGeckoUCWEB(String userAgent)
    {
        super(userAgent);

        this.browserSubType = UCWEB;
        this.geckoVersion = 1.9f; // Sabemos que es un FireFox 3

        this.java = userAgent.indexOf("Windows CE") == -1;  // Ninguno de los user agent de las versiones de Java contiene "Windows CE"
    }

    public static boolean isUCWEB(String userAgent,ItsNatServletRequestImpl itsNatRequest)
    {
        if (userAgent.indexOf("UCWEB") != -1) // v7 Windows Mobile aunque realmente NO LO SOPORTAMOS
            return true;

        // Hay que tener en cuenta tambi�n que el user agent de UCWEB
        // puede simular un MSIE_OLD 4, un OpenWave y un Opera.
        // El que simule un OpenWave por ahora no nos importa.

        String accept = itsNatRequest.getHeader("accept");
        if (accept == null) return false; // Rar�simo, quiz�s sea un robot.
        return accept.indexOf("dn/") != -1;
    }

    public boolean hasBeforeUnloadSupport(ItsNatStfulDocumentImpl itsNatDoc)
    {
        // En teor�a FireFox soporta el evento "beforeunload" pero hay que darse
        // cuenta que si se cancela este evento se cancela el unload y al parecer
        // UCWEB no est� por la labor.
        return false;
    }

    public boolean isJava()
    {
        return java;
    }

    public boolean isReferrerReferenceStrong()
    {
        return true;
    }

    public boolean isCachedBackForward()
    {
        return true;
    }

    public boolean isCachedBackForwardExecutedScripts()
    {
        return false;
    }

    public boolean isDOMContentLoadedSupported()
    {
        return true;
    }

    public boolean isBlurBeforeChangeEvent(HTMLElement formElem)
    {
        return false;
    }

    public boolean isFocusOrBlurMethodWrong(String methodName, HTMLElement formElem)
    {
        return false;
    }

    public Map getHTMLFormControlsIgnoreZIndex()
    {
        return null;
    }

    public boolean hasHTMLCSSOpacity()
    {
        return true; // Da igual, no se usa en el cliente, en el servidor UCWEB s� se reconoce (es un FireFox) pero en el cliente ni idea.
    }

    public boolean isHTMLTextControlPassive(Node node)
    {
        // Elementos que no emiten por acci�n del usuario eventos
        // y que tendremos que emitirlos nosotros manualmente
        return DOMUtilHTML.isHTMLTextAreaOrInputTextBox(node);
    }

    public boolean isHTMLAnchorOnClickNotExec()
    {
        // En Windows Mobile se detecta la pulsaci�n pero el onclick NO se ejecuta.
        return !java; // S� en las versiones Java
    }

    public boolean isOnlyOnClickExecuted(Element elem)
    {
        // Caso Windows Mobile (6.1 y 6.3 al menos):
        // Hay elementos en donde s�lo el handler inline (onclick) es ejecutado
        // cuando el usuario pulsa el elemento, los listeners via addEventListener no son despachados.
        // En los <input type=text|password|file> y <textarea> ni siquiera el onclick es ejecutado,
        // pero la soluci�n a �stos elementos no es a trav�s del onclick.
        // En los dem�s tipos de <input> y en <select> y <button>, el click funciona bien.

        // Ejemplo de elementos comprobados que ejecutan el onclick (no form):
        // <span>,<td>. Seguramente hay m�s.

        // Hay elementos tal y como <div> y <p> en donde el onclick es ignorado tambi�n,
        // en esos casos esta soluci�n se aplicar� pero no har� nada, la soluci�n es a�adir un <span> (que s� funciona)
        // o un <a> como hijo.
        // En un anchor (<a>) es tambi�n ignorado el onclick pero afortunadamente podemos usar href="javascript:..."
        // para ejecutar el evento click de forma similar a como se utiliza con el onclick
        // por tanto excluimos los anchor del "only onclick executed"
        // pues se da otra soluci�n, detect�ndose este caso con isHTMLAnchorOnClickNotExec().
        // Para los dem�s elementos en donde tampoco funciona el onclick suponemos que si, aunque
        // no sea verdad y la soluci�n no haga nada.
        // Hay m�s informaci�n en el c�digo que gestiona todo esto.

        // Caso Java:
        // La situaci�n es peor en los elementos no form y no link, pues el
        // onclick es totalmente ignorado tambi�n en <td> y en <span>
        // El elemento <a> por ejemplo s�lo ejecuta el onclick (addEventListener ignorado)
        // en ese caso la soluci�n ser� a trav�s del href
        // El elemento <img> sin embargo ejecuta el onclick y el addEventListener
        // en ese caso la versi�n Java NO tiene el problema de "OnlyOnClickExecuted"
        // pues si se ejecuta el onclick tambi�n se ejecutan los listeners addEventListener

        if ((elem instanceof HTMLInputElement)||(elem instanceof HTMLTextAreaElement)||
            (elem instanceof HTMLSelectElement)||(elem instanceof HTMLButtonElement))
             return false;

        if ((elem instanceof HTMLAnchorElement) && isHTMLAnchorOnClickNotExec())
            return false; // Se soluciona de otra forma (a trav�s del href)

        if (java)
        {
            // Elementos en los que adem�s del onclick curiosamente funciona
            // tambi�n el addEventListener:
            if (elem instanceof HTMLImageElement)
                return false;
        }

        return true; // Todos los dem�s
    }

    public boolean isSetTimeoutSupported()
    {
        /* El soporte de window.setTimeout es muy raro, para empezar
         * s�lo admite hasta 60000 miliseg, por encima de este valor
         * la llamada es ignorada. El problema es que se ejecuta
         * el handler antes de devolver la p�gina con los cambios,
         * es decir no es as�ncrono y desde luego NO espera el tiempo
         * indicado en la llamada. En el caso de Win Mobile parece
         * que no espera nada y en el caso Java parece que espera
         * unos segundos (se nota que espera) antes de ejecutar el handler.
         * En definitiva, como no es as�ncrono no nos vale.
         */
        return false;
    }
}
