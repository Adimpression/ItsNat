/*
 * This file is not part of the ItsNat framework.
 *
 * Original source code use and closed source derivatives are authorized
 * to third parties with no restriction or fee.
 * The original source code is owned by the author.
 *
 * This program is distributed AS IS in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * (C) Innowhere Software a service of Jose Maria Arranz Santamaria, Spanish citizen.
 */

package manual.stateless;

import org.itsnat.core.ItsNatServletRequest;
import org.itsnat.core.ItsNatServletResponse;
import org.itsnat.core.event.ItsNatServletRequestListener;
import org.itsnat.core.html.ItsNatHTMLDocument;

/**
 *
 * @author jmarranz
 */
public class StatelessExampleForProcessingEventDocLoadListener implements ItsNatServletRequestListener
{
    public StatelessExampleForProcessingEventDocLoadListener()
    {
    }
   
    public void processRequest(ItsNatServletRequest request, ItsNatServletResponse response)
    {
        new StatelessExampleForProcessingEventDocument((ItsNatHTMLDocument)request.getItsNatDocument(),request,response);
    }
}
