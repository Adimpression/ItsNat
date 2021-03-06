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

package org.itsnat.impl.comp.button.normal;

import org.itsnat.impl.comp.button.ItsNatHTMLInputButtonBaseImpl;
import org.itsnat.comp.button.normal.ItsNatHTMLInputButtonNormal;
import org.itsnat.comp.ItsNatComponentUI;
import javax.swing.ButtonModel;
import javax.swing.DefaultButtonModel;
import org.itsnat.core.NameValue;
import org.itsnat.impl.comp.button.ItsNatButtonSharedImpl;
import org.itsnat.impl.comp.mgr.ItsNatStfulDocComponentManagerImpl;
import org.w3c.dom.html.HTMLInputElement;

/**
 *
 * @author jmarranz
 */
public abstract class ItsNatHTMLInputButtonNormalImpl extends ItsNatHTMLInputButtonBaseImpl implements ItsNatHTMLInputButtonNormal,ItsNatButtonNormalInternal
{
    /**
     * Creates a new instance of ItsNatHTMLInputButtonDefaultImpl
     */
    public ItsNatHTMLInputButtonNormalImpl(HTMLInputElement element,NameValue[] artifacts,ItsNatStfulDocComponentManagerImpl componentMgr)
    {
        super(element,artifacts,componentMgr);
    }

    public ItsNatButtonSharedImpl createItsNatButtonShared()
    {
        return new ItsNatButtonNormalSharedImpl(this);
    }

    public ItsNatComponentUI createDefaultItsNatComponentUI()
    {
        return new ItsNatButtonNormalBasedUIImpl(this);
    }

    public Object createDefaultModelInternal()
    {
        return createDefaultButtonModel();
    }

    public ButtonModel createDefaultButtonModel()
    {
        return new DefaultButtonModel();
    }
}
