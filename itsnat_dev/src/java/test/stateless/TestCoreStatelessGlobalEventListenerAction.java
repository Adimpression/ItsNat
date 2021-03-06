/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package test.stateless;

import org.itsnat.core.ClientDocument;
import org.itsnat.core.event.ItsNatEventStateless;

/**
 *
 * @author jmarranz
 */
public class TestCoreStatelessGlobalEventListenerAction 
{
    public static void handleEvent(ItsNatEventStateless itsNatEvt)
    {
        if (itsNatEvt.getItsNatDocument() == null)
        {
            ClientDocument clientDoc = itsNatEvt.getClientDocument();                
            //ServletRequest request = itsNatEvt.getItsNatServletRequest().getServletRequest();
            String docName = (String)itsNatEvt.getExtraParam("itsnat_doc_name");
            if (docName != null)
                clientDoc.addCodeToSend("alert('Stateless event OK with not found itsnat_doc_name: " + docName + " and title " + itsNatEvt.getExtraParam("title") + "');"); 
            else
                clientDoc.addCodeToSend("alert('Custom stateless event OK and title " + itsNatEvt.getExtraParam("title") + "');");                             
        }
    }
}
