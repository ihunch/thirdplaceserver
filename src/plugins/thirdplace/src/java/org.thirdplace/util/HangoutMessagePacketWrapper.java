package org.thirdplace.util;

import org.dom4j.Element;
import org.thirdplace.HangoutComponent;
import org.thirdplace.bean.HangoutDAO;
import org.thirdplace.provider.HangoutServiceProvider;
import org.xmpp.packet.JID;
import org.xmpp.packet.Message;
import org.xmpp.packet.Packet;

/**
 * Created with IntelliJ IDEA.
 * User: Yang
 * Date: 3/01/15
 * Time: 11:59 PM
 * To change this template use File | Settings | File Templates.
 */
public class HangoutMessagePacketWrapper
{
    private Packet root = null;
    private Element element = null;
    private Element hangout = null;


    private void init(String nameSpace)
    {
        root = new Message();
        element = root.getElement();
        hangout = element.addElement("hangout", HangoutComponent.HANGOUT_MESSAGE);
    }

    public HangoutMessagePacketWrapper(String namespace)
    {
        this.init(namespace);
    }

    public void setTo(JID jid)
    {
       root.setTo(jid);
    }

    public void setFrom(JID jid)
    {
        root.setFrom(jid);
    }

    public void setID(String ID)
    {
        root.setID(ID);
    }

    public Packet getMessage() {
        return root;
    }

    public void addHangoutDetailContent(HangoutDAO hangoutDAO)
    {
        Element startdate = hangout.addElement(HangoutServiceProvider.HANGOUT_STARTDATE_ELEMENT);

        Element enddate =  hangout.addElement(HangoutServiceProvider.HANGOUT_ENDDATE_ELEMENT);

        Element description = hangout.addElement(HangoutServiceProvider.HANGOUTDESCRIPTION_ELEMET);
        description.setText(hangoutDAO.getDescription());
        Element timedes = hangout.addElement(HangoutServiceProvider.HANGOUT_TIMEDESCRIPTION_ELEMENT);

        Element message = hangout.addElement(HangoutServiceProvider.HANGOUT_MESSAGE_ELEMENT);

        Element location = hangout.addElement(HangoutServiceProvider.HANGOUTLOCATION_ELEMENT);

    }
}
