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

package org.itsnat.impl.core.domimpl;

import org.itsnat.impl.core.domimpl.deleg.DelegateNotDocumentImpl;
import org.itsnat.impl.core.domimpl.deleg.DelegateNodeImpl;
import org.apache.batik.dom.GenericDocumentFragment;
import org.itsnat.core.ItsNatDocument;
import org.itsnat.impl.core.domimpl.deleg.DelegateNotDocWithoutParentNodeImpl;
import org.w3c.dom.Node;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventException;
import org.w3c.dom.events.EventListener;

/**
 *
 * @author jmarranz
 */
public class DocumentFragmentImpl extends GenericDocumentFragment implements ItsNatNodeWithChildInternal
{
    protected DelegateNotDocumentImpl delegate;

    public DocumentFragmentImpl()
    {
        getDelegateNode();
    }

    public DocumentFragmentImpl(DocumentImpl owner)
    {
        super(owner);
        getDelegateNode();
    }

    protected Node newNode()
    {
        return new DocumentFragmentImpl();
    }

    public ItsNatDocument getItsNatDocument()
    {
        return getDelegateNode().getItsNatDocument();
    }

    public DelegateNodeImpl getDelegateNode()
    {
        if (delegate == null) this.delegate = new DelegateNotDocWithoutParentNodeImpl(this);
        return delegate;
    }

    public void addEventListenerInternal(String type, EventListener listener, boolean useCapture)
    {
        super.addEventListener(type,listener,useCapture);
    }

    public void removeEventListenerInternal(String type, EventListener listener, boolean useCapture)
    {
        super.removeEventListener(type,listener,useCapture);
    }

    public boolean isInternalMode()
    {
        DelegateNodeImpl delegate = getDelegateNode();
        return delegate.isInternalMode();
    }

    public void setInternalMode(boolean mode)
    {
        DelegateNodeImpl delegate = getDelegateNode();
        delegate.setInternalMode(mode);
    }

    public void fireDOMNodeInsertedIntoDocumentEvent()
    {
        DelegateNodeImpl delegate = getDelegateNode();
        delegate.setMutationEventInternal(true);
        try
        {
            super.fireDOMNodeInsertedIntoDocumentEvent();
        }
        finally
        {
            delegate.setMutationEventInternal(false);
        }
    }

    public void fireDOMNodeRemovedFromDocumentEvent()
    {
        DelegateNodeImpl delegate = getDelegateNode();
        delegate.setMutationEventInternal(true);
        try
        {
            super.fireDOMNodeRemovedFromDocumentEvent();
        }
        finally
        {
            delegate.setMutationEventInternal(false);
        }
    }

    public void fireDOMCharacterDataModifiedEvent(String oldv,String newv)
    {
        DelegateNodeImpl delegate = getDelegateNode();
        delegate.setMutationEventInternal(true);
        try
        {
            super.fireDOMCharacterDataModifiedEvent(oldv,newv);
        }
        finally
        {
            delegate.setMutationEventInternal(false);
        }
    }

    public void fireDOMSubtreeModifiedEvent()
    {
        DelegateNodeImpl delegate = getDelegateNode();
        delegate.setMutationEventInternal(true);
        try
        {
            super.fireDOMSubtreeModifiedEvent();
        }
        finally
        {
            delegate.setMutationEventInternal(false);
        }
    }

    public void fireDOMNodeInsertedEvent(Node node)
    {
        DelegateNodeImpl delegate = getDelegateNode();
        delegate.setMutationEventInternal(true);
        try
        {
            super.fireDOMNodeInsertedEvent(node);
        }
        finally
        {
            delegate.setMutationEventInternal(false);
        }
    }

    public void fireDOMNodeRemovedEvent(Node node)
    {
        DelegateNodeImpl delegate = getDelegateNode();
        delegate.setMutationEventInternal(true);
        try
        {
            super.fireDOMNodeRemovedEvent(node);
        }
        finally
        {
            delegate.setMutationEventInternal(false);
        }
    }


    // M�todos de EventTarget

    public boolean dispatchEvent(Event evt) throws EventException
    {
        DelegateNodeImpl delegate = getDelegateNode();
        if (delegate.isDispatchEventInternal(evt))
            return super.dispatchEvent(evt);
        else
            return delegate.dispatchEventRemote(evt);
    }

    public void addEventListener(String type, EventListener listener, boolean useCapture)
    {
        DelegateNodeImpl delegate = getDelegateNode();
        if (delegate.isAddRemoveEventListenerInternal())
            addEventListenerInternal(type,listener,useCapture);
        else
            delegate.addEventListenerRemote(type,listener,useCapture);
    }

    public void removeEventListener(String type, EventListener listener, boolean useCapture)
    {
        DelegateNodeImpl delegate = getDelegateNode();
        if (delegate.isAddRemoveEventListenerInternal())
            removeEventListenerInternal(type,listener,useCapture);
        else
            delegate.removeEventListenerRemote(type,listener,useCapture);
    }

    // ItsNatUserData

    public boolean containsUserValueName(String name)
    {
        DelegateNodeImpl delegate = getDelegateNode();
        return delegate.containsUserValueName(name);
    }

    public Object getUserValue(String name)
    {
        DelegateNodeImpl delegate = getDelegateNode();
        return delegate.getUserValue(name);
    }

    public Object setUserValue(String name, Object value)
    {
        DelegateNodeImpl delegate = getDelegateNode();
        return delegate.setUserValue(name,value);
    }

    public Object removeUserValue(String name)
    {
        DelegateNodeImpl delegate = getDelegateNode();
        return delegate.removeUserValue(name);
    }

    public String[] getUserValueNames()
    {
        DelegateNodeImpl delegate = getDelegateNode();
        return delegate.getUserValueNames();
    }
}
