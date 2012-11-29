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

package org.itsnat.impl.core.clientdoc;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import org.itsnat.core.ItsNatException;
import org.itsnat.impl.core.doc.ItsNatStfulDocumentImpl;
import org.itsnat.impl.core.domimpl.AbstractViewImpl;
import org.itsnat.impl.core.domutil.DOMUtilInternal;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Recuerda los nodos cacheados usando un id �nico que no puede ser reutilizado
 * aunque el nodo se quite.
 * Los nodos cacheados deben pertenecer al �rbol DOM, es decir est�n dentro del Document. *
 *
 * No usamos la colecci�n con WeakReferences porque cuando el nodo (sub�rbol) se quita del �rbol
 * es detectado autom�ticamente con un mutation listener que elimina dicho nodo
 * de la cach� (si estuviera) as� como todos los hijos del sub�rbol y por tanto
 * deja de referenciarse y puede recogerse el nodo por el garbage collector.
 *
 * @author jmarranz
 */
public class NodeCacheRegistryImpl implements Serializable
{
    protected ClientDocumentStfulImpl clientDoc;
    protected Map mapByNode = new HashMap();
    protected Map mapById = new HashMap();

    /**
     * Creates a new instance of NodeCacheRegistryImpl
     */
    public NodeCacheRegistryImpl(ClientDocumentStfulImpl clientDoc)
    {
        this.clientDoc = clientDoc;
    }

    public ClientDocumentStfulImpl getClientDocumentStful()
    {
        return clientDoc;
    }

    public ItsNatStfulDocumentImpl getItsNatStfulDocument()
    {
        return clientDoc.getItsNatStfulDocument();
    }

    public Iterator iterator()
    {
        return mapByNode.entrySet().iterator();
    }

    private static boolean isNodeTypeCacheable(Node node)
    {
        // Cacheamos cualquier nodo excepto el propio Document, la vista, DocumentType
        // pues es absurdo cachearlos pues son "singleton",
        // y nodos de texto, pues los nodos de texto no admiten asociar una propiedad (el id)
        // en el MSIE y adem�s f�cilmente son filtrados por los browsers etc

        int type = node.getNodeType();
        if (type == Node.ELEMENT_NODE)
            return true; // El caso t�pico, para acelerar
        else if (type == Node.TEXT_NODE)
            return false; // Otro caso t�pico, para acelerar
        else if (type == Node.DOCUMENT_NODE)
            return false;
        else if (type == AbstractViewImpl.ABSTRACT_VIEW)
            return false;
        else if (type == Node.DOCUMENT_TYPE_NODE)
            return false;

        return true; // Comentarios etc
    }

    public static boolean isCacheableNode(Node node,Document doc)
    {
        if (!isNodeTypeCacheable(node))
            return false; // No se puede cachear

        if (!DOMUtilInternal.isNodeInside(node,doc))
            return false; // S�lo cacheamos nodos vinculados al documento.

        return true;
    }

    public String removeNode(Node node)
    {
        if (!isNodeTypeCacheable(node))
            return null; // Nos ahorramos tiempo en buscar

        String id = (String)mapByNode.remove(node);
        if (id == null)
            return null;

        mapById.remove(id);

        return id;
    }

    public String getId(Node node)
    {
        if (node == null) return null;

        if (!isNodeTypeCacheable(node))
            return null; // Nos ahorramos tiempo de b�squeda

        return (String)mapByNode.get(node);
    }

    public Node getNodeById(String id)
    {
        return (Node)mapById.get(id);
    }

    public String generateUniqueId()
    {
        ItsNatStfulDocumentImpl itsNatDoc = getItsNatStfulDocument();
        return generateUniqueId(itsNatDoc);
    }

    public static String generateUniqueId(ItsNatStfulDocumentImpl itsNatDoc)
    {
        // El id debe ser �nico y vinculado al nodo un�voca e inequ�vocamente
        // (no deber�a existir el mismo id para otro nodo) y no deber�a
        // reutilizarse un id que ya fue usado por otro nodo aunque ya no exista
        // Hay casos en donde varios NodeCacheRegistryImpl pueden compartir el mismo id para el mismo nodo
        return itsNatDoc.getUniqueIdGenerator().generateId("cn");  // cn = cached node
    }

    public String addNode(Node node)
    {
        // SE SUPONE QUE EL NODO NO ESTA CACHEADO

        if (node == null) throw new ItsNatException("Null node is not supported",clientDoc);

        if (!isCacheableNode(node,getItsNatStfulDocument().getDocument()))
            return null; // No se puede cachear

        /* Evitamos cachear cuando el cliente
         * no puede recibir c�digo de respuesta.
         * Ya existir� otra oportunidad de cachear el nodo, la cache no es imprescindible simplemente acelera.
         */
        if (!clientDoc.isSendCodeEnabled())
            return null;

        // Debe generarse el id por el documento pues algunos ids pueden compartirse entre cach�s de un mismo documento como es este caso
        String id = generateUniqueId();

        addNode(node,id);

        return id;
    }

    public void addNode(Node node,String id)
    {
        // Ha de existir la seguridad de que es cacheable y
        // SE SUPONE QUE EL NODO NO ESTA CACHEADO, si lo est� dar� error

        if (node == null) throw new ItsNatException("Null node is not supported",clientDoc);

        String idOld = (String)mapByNode.put(node,id);
        Node nodeOld = (Node)mapById.put(id,node);

        if (idOld != null) throw new ItsNatException("INTERNAL ERROR");
        if (nodeOld != null) throw new ItsNatException("INTERNAL ERROR");
    }

    public boolean isEmpty()
    {
        boolean res = mapById.isEmpty();
        if (res != mapByNode.isEmpty())
            throw new ItsNatException("INTERNAL ERROR");
        return res;
    }

    public ArrayList getOrderedByHeight()
    {
        /* Este m�todo es usado por el control remoto, se debe a que
         * los nodos no est�n ordenados de ninguna forma en la cach�
         * y al replicar la cach� en el browser remoto necesitamos
         * enviar todos los nodos con su id y calculando su path, usando paths absolutos
         * no hay problema pero es un proceso muy lento, si utilizamos paths
         * relativos no sabemos si el padre que hemos encontrado en la cach�
         * lo hemos enviado antes al browser y est� ya cacheado all�.
         * Por ello una t�cnica es ordenar los nodos por alturas tal que
         * si se env�an primero los m�s altos los m�s bajos encontrar�n ya
         * en el browser el padre cacheado (si existe) pues este es m�s alto.
         * Entre nodos de la misma altura no hay problema de orden pues ninguno
         * es padre del otro, no hay dependencias a la hora de calcular el path.
         * El ArrayList devuelto procesar pero no memorizar pues contiene los Map.Entry
         * de este cach�.
         * Puede haber alturas en donde no haya ning�n nodo (lo normal).
         */

        ArrayList cacheCopy = new ArrayList();
        for(Iterator it = iterator(); it.hasNext(); )
        {
            Map.Entry entry = (Map.Entry)it.next();
            Node node = (Node)entry.getKey();
            int h = getNodeDeep(node);
            // Aseguramos que cacheCopy contiene la posici�n h
            if (cacheCopy.size() <= h)
            {
                int currSize = cacheCopy.size();
                for(int i = 1; i <= h - currSize + 1; i++)
                    cacheCopy.add(null);
            }
            LinkedList sameH = (LinkedList)cacheCopy.get(h);
            if (sameH == null)
            {
                sameH = new LinkedList();
                cacheCopy.set(h,sameH);
            }
            sameH.add(entry);
        }
        return cacheCopy;
    }

    private static int getNodeDeep(Node node)
    {
        int i = 0;
        while(node != null)
        {
            i++;
            node = node.getParentNode();
        }
        return i;
    }
}
