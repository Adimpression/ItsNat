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

package org.itsnat.impl.core.req;

import org.itsnat.impl.core.servlet.ItsNatServletRequestImpl;
import org.itsnat.impl.core.doc.ItsNatStfulDocumentImpl;

/**
 *
 * @author jmarranz
 */
public class RequestEventStatelessImpl extends RequestImpl
{
    /**
     * Creates a new instance of RequestNormalLoadDocImpl
     */
    public RequestEventStatelessImpl(ItsNatServletRequestImpl itsNatRequest)
    {
        super(itsNatRequest);
    }

    public static RequestEventStatelessImpl createRequestEventStateless(ItsNatServletRequestImpl itsNatRequest)
    {
        return new RequestEventStatelessImpl(itsNatRequest);
    }

    public ItsNatStfulDocumentImpl getItsNatStfulDocumentReferrer()
    {
        // Aunque sea la carga de una p�gina, no hay listeners que pudieran aprovechar el referrer
        return null;
    }

    public void processRequest()
    {
        HACER;
        /*
        ItsNatServletRequestImpl request = getItsNatServletRequest();
        ItsNatServletResponseImpl itsNatResponse = request.getItsNatServletResponseImpl();
        try
        {
            ServletResponse response = itsNatResponse.getServletResponse();
            response.setContentType("text/plain");

            ItsNatImpl itsNat = request.getItsNatServletImpl().getItsNatImpl();
            OutputStream out = response.getOutputStream();
            Properties prop = new Properties();
            prop.setProperty("product","ItsNat");
            prop.setProperty("version",itsNat.getVersion());
            prop.store(out,null);
        }
        catch(IOException ex)
        {
            throw new ItsNatException(ex);
        }
        * */
    }

    protected boolean isMustNotifyEndOfRequestToSession()
    {
        // As� nos ahorramos una serializaci�n in�til, estamos en stateless
        return false;
    }
}
