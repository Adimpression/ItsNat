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

package org.itsnat.impl.core.resp.shared.otherns;

import org.itsnat.impl.core.clientdoc.ClientDocumentStfulImpl;
import org.itsnat.impl.core.doc.BoundElementDocContainerImpl;
import org.itsnat.impl.core.doc.ItsNatStfulDocumentImpl;
import org.itsnat.impl.core.domimpl.ElementDocContainer;
import org.itsnat.impl.core.path.SimpleElementPathResolver;
import org.itsnat.impl.core.resp.ResponseLoadStfulDocumentValid;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import org.w3c.dom.html.HTMLIFrameElement;

/**
 *
 * @author jmarranz
 */
public class ResponseDelegateSVGLoadDocASVRenesisImpl extends ResponseDelegateSVGLoadDocImpl
{

    /**
     * Creates a new instance of ResponseDelegateOtherNSLoadDocImpl
     */
    public ResponseDelegateSVGLoadDocASVRenesisImpl(ResponseLoadStfulDocumentValid response)
    {
        super(response);
    }

    public void dispatchRequestListeners()
    {
        fixGetSVGDocument();

        fixSetClearTimeout();

        super.dispatchRequestListeners();
    }

    protected void fixSetClearTimeout()
    {
        // setTimeout est� definido en ASV pero s�lo funciona con c�digo como cadena
        // http://blog.codedread.com/archives/2007/01/19/guide-to-deploying-svg-with-html/
        // Este truco "sucio" nos sirve para tener setTimeout como nosotros necesitamos
        // Reemplazamos los m�todos por defecto.

        // Hay que tener en cuenta que itsNatDoc.win es window._window_impl,
        // es v�lido window.setTimeout y window.clearTimeout y tambi�n
        // con window._window_impl.setTimeout/clearTimeout pues est�n definidos tambi�n.
        // El problema es que *de una forma u otra* el objeto contexto con el que se ejecuta
        // el script del setTimeout es desconocido (quiz�s window) pero NO es window._window_impl, esto no afecta a la resoluci�n global de "document"
        // pero afecta a la funci�n que llamemos cuyo objeto global de contexto (no confundir con this que es window._window_impl)
        // no ser� window._window_impl, pues es en este objeto donde hemos definido el objeto global "itsnat".
        // Esto es un problema por ejemplo para instrucciones new itsnat.AJAX() pues "itsnat" debe
        // resolverse como "window._window_impl.itsnat" no como  "window.itsnat" o similar siendo "window"
        // el objeto padre.
        // La soluci�n es forzar el contexto "window._window_impl" en la llamada gracias a "call",
        // como "this" es window._window_impl ser� compatible con Renesis (this es window) en vez de usar window._window_impl,
        // pues Renesis no tiene este problema ni tampoco _window_impl

        // Usamos el documento ItsNat para las variables y colecciones auxiliares para evitar el lio
        // entre window y _window_impl (Renesis no tiene _window_impl)
        // Renesis 1.1.1 imita el funcionamiento de setTimeout en ASV, excepto
        // una cosa, en ASV setTimeout devuelve una cadena, en Renesis devuelve
        // un n�mero entero.

        ClientDocumentStfulImpl clientDoc = getClientDocumentStful();
        if (!clientDoc.isScriptingEnabled()) return;

        StringBuffer code = new StringBuffer();

        code.append("var func = function(func,delay)");
        code.append("{");
        code.append("  if (!this.set_timeout_count) this.set_timeout_count = 0;");
        code.append("  var funcId = \"set_timeout_func_\" + (++this.set_timeout_count);");
        code.append("  var funcWrap = function()");
        code.append("  {");
        code.append("    var thisF = arguments.callee;");
        code.append("    try{ delete thisF.itsNatDoc[thisF.funcId]; }catch(e){}");
        code.append("    try{ delete thisF.itsNatDoc[thisF.handleId]; }catch(e){}");
        code.append("    thisF.func();");
        code.append("  };");
        code.append("  funcWrap.func = func;");
        code.append("  funcWrap.itsNatDoc = this;");
        code.append("  funcWrap.funcId = funcId;");
        code.append("  this[funcId] = funcWrap;");
        code.append("  var handle = this.win.setTimeout(\"var funcWrap = document.getItsNatDoc()['\" + funcId + \"']; funcWrap.call(this);\",delay);");
        code.append("  var handleId = \"set_timeout_hnd_\" + handle;");  // handle es una string en ASV y un n�mero en Renesis
        code.append("  funcWrap.handleId = handleId;");
        code.append("  this[handleId] = funcId;");
        code.append("  return handle;");
        code.append("};");
        code.append("itsNatDoc.setTimeout = func;\n");

        code.append("var func = function(handle)");
        code.append("{");
        code.append("  this.win.clearTimeout(handle);");
        code.append("  var handleId = \"set_timeout_hnd_\" + handle;");  // handle es una string en ASV y un n�mero en Renesis
        code.append("  var funcId = this[handleId];");
        code.append("  try{ delete this[handleId]; }catch(e){}");
        code.append("  try{ delete this[funcId]; }catch(e){}");
        code.append("};");
        code.append("itsNatDoc.clearTimeout = func;\n");

        clientDoc.addCodeToSend(code.toString());
    }

    protected void fixGetSVGDocument()
    {
        BoundElementDocContainerImpl parent = getItsNatStfulDocument().getParentHTMLDocContainer();
        if (parent != null)
        {
            // El documento padre est� sincronizado, no hay problema
            // de hilos
            ElementDocContainer iframe = parent.getElementDocContainer();
            if (iframe instanceof HTMLIFrameElement)
            {
                // En ASV podemos acceder al documento padre via window.top.document (si es el mismo dominio)
                // En Renesis 1.1.1 al acceder al "document" nos da error de acceso denegado,
                // pero afortunadamente podemos acceder al elemento <iframe> padre
                // con window.top.frameElement lo cual no funciona en ASV, aunque
                // typeof window.top.frameElement devuelve "object" sin embargo
                // una comparaci�n con null da true.

                // A�adimos un try/catch para cuando no tenemos permiso de acceso
                // al padre porque el dominio del padre es diferente, dar� un error silencioso
                // y ya est�, obviamente el programador no podr� acceder al hijo ni al padre.

                // Intentamos NO usar la API de ItsNat en el cliente
                // para localizar el elemento contenedor, pues
                // si la carga del SVG es s�ncrona o por lo que sea se carga antes que el documento
                // padre del todo, el script de inicio de ItsNat no se ha ejecutado todav�a en el padre.
                // Hay que recordar que el documento padre es siempre Internet Explorer desktop


                ItsNatStfulDocumentImpl parentDoc = parent.getItsNatStfulDocument();
                ClientDocumentStfulImpl parentClientDoc = parentDoc.getClientDocumentStfulOwner();


                StringBuffer code = new StringBuffer();

                code.append("\n");
                code.append("try{\n");

                code.append("var elem = window.top.frameElement;\n"); // Si es no nulo es el caso de Renesis
                code.append("if (!elem)\n");
                code.append("{\n");
                String path = SimpleElementPathResolver.getPathFromElementJS((Element)iframe);
                code.append(  SimpleElementPathResolver.callGetElementFromPath("elem","window.top.document", path, parentClientDoc));
                code.append( "}\n");
                code.append("var func = function() { return arguments.callee.childDoc; };\n");
                code.append("func.childDoc = document;\n");
                code.append("elem.getSVGDocument = func;\n");

                code.append("}catch(e){}\n");

                addFixDOMCodeToSend(code.toString());
            }
        }
    }

    protected void rewriteClientUIControlProperties(Element elem,boolean revertJSChanges,StringBuffer code)
    {
        // SVG no tiene controles propios.
        // NO llamamos a rewriteClientHTMLUIControlProperties porque
        // ASV3 NO reconoce los elementos XHTML como tales dentro de <foreignObject> (ASV6 algo m�s)
        // Renesis 1.1.1 tampoco soporta <foreignObject>
    }

    protected void rewriteFrameworkScriptElement(Element elem)
    {
        // Esto es debido a que aunque Renesis soporta xlink:href en <script>
        // sin embargo en tiempo de carga el script externo es cargado
        // y ejecutado ASINCRONAMENTE, por lo que no nos vale.
        // Por ello cargamos el script via llamada AJAX s�ncrona.
        // Esto no pasa con ASV que funciona bien pero no podemos distinguir (en tiempo de carga claro).

        String url = elem.getAttribute("xlink:href");

        elem.removeAttribute("xlink:href");
        elem.removeAttribute("xmlns:xlink");

        StringBuffer code = new StringBuffer();
        code.append("var xhr;");
        code.append("try{xhr = new window.XMLHttpRequest();}"); // No se porqu� pero en IE8 no funciona window.ActiveXObject(...) a partir de una actualizaci�n, en los casos de SVG en p�gina completa y en <iframe>
        code.append("catch(e){xhr = new window.ActiveXObject(\"Msxml2.XMLHTTP\");}");   // Microsoft.XMLHTTP  Msxml2.XMLHTTP
        code.append("xhr.open(\"GET\",\"" + url + "\",false);");
        code.append("xhr.setRequestHeader(\"If-Modified-Since\",\"Wed, 15 Nov 1995 00:00:00 GMT\");"); /* el url contiene ya un valor aleatorio pero por si acaso pues he comprobado que sin esto se cachea la request si no cambia el URL (al menos al acceder a archivos JavaScript est�ticos) */
        // xhr.setRequestHeader("Cache-Control","no-cache"); NO hace nada, con la anterior es suficiente
        code.append("xhr.send(null);");
        code.append("window.eval(xhr.responseText);");
        code.append("xhr = null;");

        Text text = elem.getOwnerDocument().createTextNode(code.toString());
        elem.appendChild(text);
    }

    protected void loadFrameworkScriptElementWithURL(Element scriptElem,String jsFileNameList)
    {
        super.loadFrameworkScriptElementWithURL(scriptElem,jsFileNameList);
        rewriteFrameworkScriptElement(scriptElem);
    }

    protected Element createInitScriptElement(final int prevScriptsToRemove)
    {
        Element elem = super.createInitScriptElement(prevScriptsToRemove);
        if (!elem.hasChildNodes()) // Es carga a trav�s de un request con una url especificada
            rewriteFrameworkScriptElement(elem);
        return elem;
    }

    protected String getInitDocumentAndLoadJSCode(final int prevScriptsToRemove)
    {
        // Hacemos un "delayed init" en Adobe SVG Viewer v6 beta.
        // La versi�n 6, por lo menos en IE v7, tiene un problema con el JavaScript
        // en tiempo de carga, se satura enseguida.
        // Lo curioso es que el JavaScript de inicio se ejecuta completamente
        // pero la p�gina no termina de cargar, el evento SVGLoad no es disparado.
        // He comprobado que no tiene nada que ver con el registro de eventos en s� mismo,
        // la propia resoluci�n de paths de nodos es suficiente.

        // Esto se comprueba cuando se registran muchos listeners al cargar (en fast load por ejemplo).
        // La soluci�n es ejecutar las acciones de inicio
        // al procesar el evento SVGLoad (aplicado al <svg> root).

        StringBuffer code = new StringBuffer();

        code.append( "\n" );
        code.append( "var asv6 = (window.navigator.appName.indexOf(\"Adobe\") == 0) && (window.navigator.appVersion.indexOf(\"6.\") == 0); \n" );
        code.append( "var initListener = function (evt)\n" );
        code.append( "{\n" );
        code.append( "  if (asv6)\n" );
        code.append( "  {\n ");
        code.append( "    evt.stopPropagation();\n" ); // De esta manera (recuerda que estamos en fase capture) conseguimos que el inline handler y los listeners del usuario no definidos en el servidor NO se ejecuten ahora, se ejecutar�n cuando enviemos de nuevo el evento SVGLoad manualmente, as� se ejecutar�n una sola vez
        code.append( "    document.documentElement.removeEventListener(\"SVGLoad\",arguments.callee,true);\n" ); // Para evitar que se llame recursivamente, notar que es CAPTURE = true
        code.append( "  }\n ");

        code.append( super.getInitDocumentAndLoadJSCode(prevScriptsToRemove) );

        code.append( "  if (asv6) \n" );
        code.append( "  {\n ");
        code.append( "    var evt = document.createEvent(\"SVGLoad\");\n" );
        code.append( "    document.documentElement.dispatchEvent(evt);\n" );
        code.append( "  }\n ");
        code.append( "};\n" );
        code.append( "if (asv6) document.documentElement.addEventListener(\"SVGLoad\",initListener,true);\n" ); // NOTAR QUE ES CAPTURE, aunque no es muy ortodoxo se ejecutar� (no cumple el W3C pues si es el target del evento no se ejecutan los capture en el target)
        code.append( "else initListener(null);\n" );

        return code.toString();
    }

    protected String getWindowReference()
    {
        // El Adobe SVG Viewer (todas las versiones soportadas) tiene
        // un comportamiento MUY EXTRA�O con el uso de window cuando
        // es usado a trav�s de <object> o <embed> (al menos dentro del mismo dominio),
        // en estos casos la integraci�n entre el engine JavaScript del plugin y el
        // del padre es muy alta.
        // Para ello hay que diferenciar entre el objeto contexto global y el objeto contexto "this"
        // Es decir, probando esto en ASV:
        // <script>alert(window == this);</script>
        // devuelve false siendo "window" la propiedad window que el propio window
        // objeto global suele tener.

        // En el caso de un script dentro de un elemento <script> en ASV, el contexto global es
        // es un extra�o window que aunque no es exactamente el window del padre est� INTIMAMENTE vinculado
        // por ejemplo <script>alert(location)</script> devuelve el location del padre y si lo
        // cambiamos cambiamos la direcci�n del padre.
        // Hemos de distinguir 3 windows en un contexto ASV dentro de un <script>:
        //  El objeto global: suponemos que es el mismo que obtenemos con "window" (pues window.window es s� mismo)
        //  El objeto window._window_impl: este objeto curiosamente COINCIDE con "this"
        //  El window del parent: window.top
        // Aunque window y window.top no son el mismo objeto hay una extra�a
        // compartici�n de propiedades, por ejemplo:
        // alert(window.location == window.top.location); muestra "true"

        // El caso es que si pasamos "window" en iniciaci�n para crear un
        // window.itsnat = new Object() ... por alguna extra�a raz�n pasamos
        // el window del padre o se realiza una extra�a sincronizaci�n con el padre,
        // reescribiendo por tanto el objeto itsnat definido en el el padre.
        // El problema es MUY SERIO pues el objeto "itsnat" del padre acaba definido y vinculado
        // a scripts ***cargados en el plugin**, es decir API W3C, tal que cuando llamamos a un m�todo desde el padre
        // pero cargado en el plugin hijo, por ejemplo "new itsnat.ALGO()", el m�todo ALGO est� en el plugin y
        // autom�ticamente el objeto global contexto pasa a ser el extra�o window de ASV
        // por lo que "window" (window.window es s� mismo) y "document" son los del plugin
        // NO LOS DEL PADRE. Una paradoja de este problema es cuando desde un script del padre
        // llamamos ("sin darnos cuenta") a un m�todo definido en el plugin que por ejemplo
        // hace un removeChild para quitarlo del DOM padre, la eliminaci�n falla pues el m�todo
        // que usamos se destruye con el padre, el motor JavaScript no sabe como continuar.

        // En resumen, la soluci�n es usar window._window_impl como verdadero window del plugin,
        // asociando "itsnat" tal que en window._window_impl.itsnat pongamos la API de ItsNat,
        // as� evitamos los problemas del falso window vinculado al padre de ASV.
        // Supone detectar cual es el objeto global que se est� usando, por ejemplo lo habitual es
        // que sea "window" por lo que antes de llamar a un m�todo que hace dentro referencias
        // tipo new itsnat.ALGO() hemos de usar func.call(window._window_impl) pues
        // "itsnat" es visible con window._window_impl como global no como "window".
        // Esto ocurre en el c�digo ejecutado por setTimeout donde el global es window,
        // en ese caso incluso podemos usar "this" pues es window._window_impl NO window (menudo lio)

        // Como Renesis NO tiene este problema (ni _window_impl) tenemos que detectarlo.

        return "window._window_impl ? window._window_impl : window";
    }
}
