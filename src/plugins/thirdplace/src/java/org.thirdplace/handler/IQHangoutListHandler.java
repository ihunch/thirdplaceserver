package org.thirdplace.handler;

import com.notnoop.apns.ApnsService;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.jivesoftware.openfire.OfflineMessageStore;
import org.jivesoftware.openfire.PacketRouter;
import org.jivesoftware.openfire.PresenceManager;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.user.UserManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thirdplace.HangoutComponent;
import org.thirdplace.bean.*;
import org.thirdplace.provider.HangoutServiceProvider;
import org.thirdplace.util.HangoutConstant;
import org.thirdplace.util.IQAdditions;
import org.xmpp.packet.IQ;
import org.xmpp.packet.JID;
import org.xmpp.packet.PacketError;

import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Yang
 * Date: 28/12/14
 * Time: 10:17 PM
 * To change this template use File | Settings | File Templates.
 */
public class IQHangoutListHandler implements IQHangoutHandler
{
    private final String QueryElement = "query";
    private final String ResultElment = "result";
    private HangoutServiceProvider provider = null;
    private static final Logger Log = LoggerFactory.getLogger(IQHangoutListHandler.class);
    private int First = 0;
    private ApnsService service = null;
    public IQHangoutListHandler()
    {
       this.init();
    }

    public IQ handleIQRequest(IQ packet) {
        IQ reply = null;
        try {
            Element iq = packet.getChildElement();
            String name = iq.getName();
            if (QueryElement.equals(name)) {
                String jidstr = packet.getElement().attributeValue("from");
                JID fromjid = new JID(jidstr);
                Document document = DocumentHelper.createDocument();
                Element result = document.addElement(ResultElment, HangoutComponent.HANGOUT_LIST);
                List<HangoutDAO> list = provider.selectListOfHangout(fromjid);
                for (HangoutDAO hangoutDAO : list) {
                    Element hangout = result.addElement("hangout");
                    Element id = hangout.addElement("id");
                    id.setText(String.valueOf(hangoutDAO.getHangoutid()));
                    if (hangoutDAO.getLocationDAOList().size() > 0) {
                        HangoutLocationDAO locationDAO = hangoutDAO.getLocationDAOList().get(First);
                        Element location = hangout.addElement("location");
                        location.setText(String.valueOf(locationDAO.getFoursquare_locationid()));
                        System.out.println(String.valueOf(locationDAO.getFoursquare_locationid()));
                    }
                    if (hangoutDAO.getTimeDAOList().size() > 0) {
                        HangoutTimeDAO timeDAO = hangoutDAO.getTimeDAOList().get(First);
                        Element time = hangout.addElement("time");
                        time.setText(timeDAO.getTimeDescription());
                    }
                    if (hangoutDAO.getMessageDAOList().size() > 0) {
                        HangoutMessageDAO messageDAO = hangoutDAO.getMessageDAOList().get(First);
                        Element message = hangout.addElement("message");
                        System.out.println(messageDAO.getContent());
                        message.setText(messageDAO.getContent());
                    }
                    if (hangoutDAO.getUserDAOList().size() > 0) {
                        List<HangoutUserDAO> users = hangoutDAO.getUserDAOList();
                        Element usersElement = hangout.addElement("users");
                        for (HangoutUserDAO user : users) {
                            Element userElement = usersElement.addElement("user");
                            userElement.addAttribute("jid",user.getJid().toBareJID());
                            userElement.addAttribute("goingstatus",user.getGoingstatus());
                        }
                    }
                }
                reply = IQAdditions.createResultIQ(packet, result);
                return reply;
            } else {
                reply = IQ.createResultIQ(packet);
                reply.setError(PacketError.Condition.feature_not_implemented);
                return reply;
            }
        } catch (Exception e) {
            System.out.println(e.toString());
            reply = IQ.createResultIQ(packet);
            reply.setError(PacketError.Condition.internal_server_error);
            return reply;
        }
    }


    public void init()
    {
        provider = new HangoutServiceProvider();
    }

    public void destory() {
       provider = null;
    }

    public void setApnsServce(ApnsService service){
            this.service = service;
    }
}

