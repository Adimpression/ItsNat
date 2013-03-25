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

package org.itsnat.impl.core.event.client.domext;

import org.itsnat.core.event.ItsNatEventStateless;
import org.itsnat.impl.core.listener.domext.ItsNatEventStatelessListenerWrapperImpl;
import org.itsnat.impl.core.req.norm.RequestNormalEventImpl;

/**
 *
 * @author jmarranz
 */
public class ClientItsNatEventStatelessImpl extends ClientItsNatDOMExtEventImpl implements ItsNatEventStateless
{
    /** Creates a new instance of ContinueEventImpl */
    public ClientItsNatEventStatelessImpl(ItsNatEventStatelessListenerWrapperImpl listenerWrapper,RequestNormalEventImpl request)
    {
        super(listenerWrapper,request);
    }

    public ItsNatEventStatelessListenerWrapperImpl getEventStatelessListenerWrapper()
    {
        return (ItsNatEventStatelessListenerWrapperImpl)listenerWrapper;
    }

}