/*
 * TestCheckBoxBase.java
 *
 * Created on 20 de noviembre de 2006, 22:14
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package test.comp;

import org.itsnat.core.html.ItsNatHTMLDocument;
import javax.swing.ButtonModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.w3c.dom.html.HTMLDocument;
import test.shared.TestBaseHTMLDocument;

/**
 *
 * @author jmarranz
 */
public abstract class TestCheckBoxBase extends TestBaseHTMLDocument implements ChangeListener
{
    protected boolean selected = false;

    /**
     * Creates a new instance of TestCheckBoxBase
     */
    public TestCheckBoxBase(ItsNatHTMLDocument itsNatDoc)
    {
        super(itsNatDoc);
    }

    public void stateChanged(ChangeEvent e)
    {
        // Este m�todo es ejecutado ante otros tipos de cambios
        // no s�lo por setSelected, por ello lo detectamos detectando
        // el cambio de selecci�n
        ButtonModel model = (ButtonModel)e.getSource();

        String fact = "";
        if (model.isSelected() && !selected)
        {
            selected = true;
            fact = "selected ";
        }
        else if (!model.isSelected() && selected)
        {
            selected = false;
            fact = "deselected ";
        }
        if (!fact.equals(""))
        {
            outText("OK " + fact + " "); // Para que se vea
        }
    }
}
