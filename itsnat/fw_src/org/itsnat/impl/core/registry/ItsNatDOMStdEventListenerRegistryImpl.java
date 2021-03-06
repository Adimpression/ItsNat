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

package org.itsnat.impl.core.registry;

import org.itsnat.impl.core.listener.domstd.*;
import org.itsnat.core.ItsNatException;
import org.itsnat.core.event.NodeMutationTransport;
import org.itsnat.core.event.ParamTransport;
import org.itsnat.impl.core.clientdoc.ClientDocumentStfulImpl;
import org.itsnat.impl.core.doc.ItsNatStfulDocumentImpl;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;

/**
 *
 * @author jmarranz
 */
public class ItsNatDOMStdEventListenerRegistryImpl extends ItsNatDOMEventListenerRegistryByTargetTooImpl
{

    /**
     * Creates a new instance of ItsNatDOMStdEventListenerRegistryImpl
     */
    public ItsNatDOMStdEventListenerRegistryImpl(ItsNatStfulDocumentImpl itsNatDoc,ClientDocumentStfulImpl clientDoc)
    {
        super(itsNatDoc,clientDoc);
    }

    public boolean isValidEventTarget(EventTarget target,boolean throwErr)
    {
        if (target == null)
            if (throwErr)
                throw new ItsNatException("Null event target is not allowed");
            else
                return false;

        return super.isValidEventTarget(target,throwErr);
    }

    public void addItsNatDOMStdEventListener(EventTarget target,String type,EventListener listener,boolean useCapture,int commMode,ParamTransport[] extraParams,String preSendCode,long eventTimeout,String bindToListener)
    {
        if (!canAddItsNatDOMEventListener(target,type,listener,useCapture))
            return; // Ya registrado (u otra raz�n)

        ItsNatDOMStdEventListenerWrapperImpl listenerWrapper = new ItsNatDOMStdEventListenerWrapperImpl(itsNatDoc,clientDoc,target,type,listener,useCapture,commMode,extraParams,preSendCode,eventTimeout,bindToListener);

        addItsNatDOMEventListener(listenerWrapper);
    }

    public ItsNatDOMStdEventListenerWrapperImpl removeItsNatDOMStdEventListener(EventTarget target,String type,EventListener listener,boolean useCapture,boolean updateClient)
    {
        if (!isValidEventTarget(target,false)) return null; // No pudo registrarse, nos ahorramos una b�squeda in�til

        return (ItsNatDOMStdEventListenerWrapperImpl)removeItsNatDOMEventListener(target,type,listener,useCapture,updateClient);
    }

    public int removeAllItsNatDOMStdEventListeners(EventTarget target,boolean updateClient)
    {
        return removeAllItsNatDOMEventListeners(target,updateClient);
    }

    public ItsNatDOMStdEventListenerWrapperImpl getItsNatDOMStdEventListenerById(String listenerId)
    {
        return (ItsNatDOMStdEventListenerWrapperImpl)getItsNatDOMEventListenerById(listenerId);
    }

    public void addMutationEventListener(EventTarget nodeTarget,EventListener mutationListener,boolean useCapture,int commMode,long eventTimeout)
    {
        addMutationEventListener(nodeTarget,mutationListener,useCapture,commMode,null,eventTimeout,null);
    }

    public void addMutationEventListener(EventTarget target,EventListener listener,boolean useCapture,int commMode,String preSendCode,long eventTimeout,String bindToListener)
    {
        ParamTransport[] params = new ParamTransport[]{ new NodeMutationTransport() };
        addItsNatDOMStdEventListener(target,"DOMAttrModified",listener,useCapture,commMode,params,preSendCode,eventTimeout,bindToListener);
        addItsNatDOMStdEventListener(target,"DOMNodeInserted",listener,useCapture,commMode,params,preSendCode,eventTimeout,bindToListener);
        addItsNatDOMStdEventListener(target,"DOMNodeRemoved",listener,useCapture,commMode,params,preSendCode,eventTimeout,bindToListener);
        addItsNatDOMStdEventListener(target,"DOMCharacterDataModified",listener,useCapture,commMode,params,preSendCode,eventTimeout,bindToListener);
    }

    public void removeMutationEventListener(EventTarget target,EventListener listener,boolean useCapture,boolean updateClient)
    {
        removeItsNatDOMStdEventListener(target,"DOMAttrModified",listener,useCapture,updateClient);
        removeItsNatDOMStdEventListener(target,"DOMNodeInserted",listener,useCapture,updateClient);
        removeItsNatDOMStdEventListener(target,"DOMNodeRemoved",listener,useCapture,updateClient);
        removeItsNatDOMStdEventListener(target,"DOMCharacterDataModified",listener,useCapture,updateClient);
    }
}
