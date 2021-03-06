/*
 * TestHTMLLabelEditors.java
 *
 * Created on 26 de octubre de 2006, 16:59
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package test.comp.html;

import org.itsnat.comp.label.ItsNatHTMLLabel;
import org.itsnat.core.html.ItsNatHTMLDocument;
import test.comp.TestLabelEditorsBase;

/**
 *
 * @author jmarranz
 */
public class TestHTMLLabelEditors extends TestLabelEditorsBase
{
    /**
     * Creates a new instance of TestHTMLLabelEditors
     */
    public TestHTMLLabelEditors(ItsNatHTMLDocument itsNatDoc)
    {
        super(itsNatDoc);

        labels = new ItsNatHTMLLabel[6];

        load( new String[]
               {"testItemEditorLabelId1","testItemEditorLabelId2","testItemEditorLabelId3",
                "testItemEditorLabelId4","testItemEditorLabelId5","testItemEditorLabelId6"}
            );
    }


}
