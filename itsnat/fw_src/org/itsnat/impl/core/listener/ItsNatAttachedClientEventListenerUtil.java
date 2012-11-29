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

package org.itsnat.impl.core.listener;

import java.util.LinkedList;
import org.itsnat.core.event.ItsNatAttachedClientEvent;
import org.itsnat.core.event.ItsNatAttachedClientEventListener;
import org.itsnat.impl.core.servlet.ItsNatServletImpl;
import org.itsnat.impl.core.doc.ItsNatStfulDocumentImpl;
import org.itsnat.impl.core.event.ItsNatEventImpl;
import org.itsnat.impl.core.event.ItsNatEventListenerChainImpl;

/**
 *
 * @author jmarranz
 */
public class ItsNatAttachedClientEventListenerUtil
{
    private static void handleEventListeners(ItsNatAttachedClientEvent event,ItsNatEventListenerChainImpl chain)
    {
        if (chain.isStopped()) return; // Por si acaso
        LinkedList listeners = chain.getListeners();
        while(!listeners.isEmpty())
        {
            ItsNatAttachedClientEventListener listener = (ItsNatAttachedClientEventListener)listeners.removeFirst();
            listener.handleEvent(event);
            if (chain.isStopped()) break;
        }
    }

    public static boolean handleEventListeners(final ItsNatAttachedClientEvent evt,final LinkedList listeners)
    {
        if (listeners.isEmpty()) return false;

        // Hay alg�n listener
        ItsNatEventListenerChainImpl chain = new ItsNatEventListenerChainImpl(listeners)
        {
           public void continueChain()
           {
               super.continueChain();
               handleEventListeners(evt,this);
           }
        };

        ItsNatEventImpl itsNatEvt = (ItsNatEventImpl)evt;
        itsNatEvt.setItsNatEventListenerChain(chain);

        try
        {
            handleEventListeners(evt,chain);
        }
        finally
        {
            itsNatEvt.unsetEventListenerChain();  // El chain s�lo tiene validez en este contexto
        }

        return true;
    }

    private static void addGlobalEventListeners(ItsNatAttachedClientEvent evt,LinkedList listeners)
    {
        ItsNatEventImpl itsNatEvt = (ItsNatEventImpl)evt;
        ItsNatServletImpl servlet = itsNatEvt.getItsNatServletRequestImpl().getItsNatServletImpl();

        servlet.getItsNatAttachedClientEventListenerList(listeners);

        ItsNatStfulDocumentImpl itsNatDoc = itsNatEvt.getItsNatStfulDocument();

        itsNatDoc.getItsNatDocumentTemplateImpl().getItsNatAttachedClientEventListenerList(listeners);

        itsNatDoc.getItsNatAttachedClientEventListenerList(listeners);
    }

    public static boolean handleEventIncludingGlobalListeners(ItsNatAttachedClientEvent evt)
    {
        LinkedList listeners = new LinkedList();
        addGlobalEventListeners(evt,listeners);

        return handleEventListeners(evt,listeners);
    }
}
