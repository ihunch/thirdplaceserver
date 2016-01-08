package org.thirdplace.util;

import org.dom4j.Element;
import org.thirdplace.HangoutComponent;
import org.thirdplace.bean.HangoutDAO;
import org.thirdplace.bean.HangoutLocationDAO;
import org.thirdplace.bean.HangoutMessageDAO;
import org.thirdplace.bean.HangoutTimeDAO;
import org.thirdplace.provider.HangoutServiceProvider;
import org.xmpp.packet.JID;
import org.xmpp.packet.Message;
import org.xmpp.packet.Packet;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

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
    private int First = 0;

    private void init(String nameSpace)
    {
        root = new Message();
        element = root.getElement();
        hangout = element.addElement("hangout", nameSpace);
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
        HangoutTimeDAO time = hangoutDAO.getTimeDAOList().get(First);
        HangoutMessageDAO messageDAO = hangoutDAO.getMessageDAOList().get(First);
        Element hangoutid = hangout.addElement(HangoutServiceProvider.HANGOUTID_ELEMET);
        Element hangoutcreateuser = hangout.addElement(HangoutServiceProvider.HANGOUT_CREATEUSER);
        hangoutcreateuser.setText(hangoutDAO.getCreateUser().toBareJID());
        hangoutid.setText(String.valueOf(hangoutDAO.getHangoutid()));
        DateFormat df = new SimpleDateFormat(HangoutConstant.Hangout_DATEFORMAT);
        Element startdate = hangout.addElement(HangoutServiceProvider.HANGOUT_STARTDATE_ELEMENT);
        startdate.setText( df.format(time.getStartdate()));
        Element enddate =  hangout.addElement(HangoutServiceProvider.HANGOUT_ENDDATE_ELEMENT);
        enddate.setText( df.format(time.getEnddate()));
        Element description = hangout.addElement(HangoutServiceProvider.HANGOUTDESCRIPTION_ELEMET);
        description.setText(hangoutDAO.getDescription());
        Element timedes = hangout.addElement(HangoutServiceProvider.HANGOUT_TIMEDESCRIPTION_ELEMENT);
        timedes.setText(time.getTimeDescription());
        if (time.isTimeConfirmed())
        {
            Element timeconfirm = hangout.addElement(HangoutServiceProvider.HANGOUT_TIMECONFIRM_ELEMENT);
            timeconfirm.setText("true");
        }
        Element message = hangout.addElement(HangoutServiceProvider.HANGOUT_MESSAGE_ELEMENT);
        message.setText(messageDAO.getContent());
        Element messageid = hangout.addElement(HangoutServiceProvider.HANGOUT_MESSAGEID_ELEMENT);
        messageid.setText(Long.toString(messageDAO.getMessageid()));
        if (hangoutDAO.getLocationDAOList() != null && hangoutDAO.getLocationDAOList().size() > 0) {
            HangoutLocationDAO locationDAO = hangoutDAO.getLocationDAOList().get(First);

            Element location = hangout.addElement(HangoutServiceProvider.HANGOUTLOCATION_ELEMENT);
            location.setText(String.valueOf(locationDAO.getFoursquare_locationid()));
            if (locationDAO.isLocationconfirm()) {
                Element locationconfirm = hangout.addElement(HangoutServiceProvider.HANGOUT_LOCATIONCONFIRM_ELEMENT);
                locationconfirm.setText("true");
            }
        }
        Element perferredlocation = hangout.addElement(HangoutServiceProvider.HANGOUT_PERFERREDLOCATION_ELEMENT);
        perferredlocation.setText(hangoutDAO.getPreferredlocation());
    }

    public void addSenderGoingStatus(String status)
    {
        Element goingstatus = hangout.addElement(HangoutServiceProvider.HANGOUT_GOINGSTATUS_ELEMENT);
        goingstatus.setText(status);
    }

    public void addHangoutCloseInformation(HangoutDAO hangoutDAO)
    {
        Element close = hangout.addElement("close");
        close.setText(String.valueOf(hangoutDAO.getHangoutid()));
    }
}
