package org.thirdplace.handler;

import org.dom4j.*;
import org.jivesoftware.openfire.OfflineMessageStore;
import org.jivesoftware.openfire.PacketRouter;
import org.jivesoftware.openfire.PresenceManager;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.user.User;
import org.jivesoftware.openfire.user.UserManager;
import org.jivesoftware.openfire.user.UserNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thirdplace.HangoutComponent;
import org.thirdplace.bean.HangoutDAO;
import org.thirdplace.bean.HangoutUserDAO;
import org.thirdplace.provider.HangoutServiceProvider;
import org.thirdplace.util.HangoutMessagePacketWrapper;
import org.thirdplace.util.IQAdditions;
import org.xmpp.packet.IQ;
import org.xmpp.packet.Message;
import org.xmpp.packet.PacketError;

/**
 * Created with IntelliJ IDEA.
 * User: Yang
 * Date: 28/12/14
 * Time: 10:15 PM
 * To change this template use File | Settings | File Templates.
 */
public class IQHangoutDetailHandler implements IQHangoutHandler
{
    private static final Logger Log = LoggerFactory.getLogger(IQHangoutDetailHandler.class);

    private final String CreateElement = "create";
    private final String UpdateElement = "update";
    private final String QueryElement = "query";
    private UserManager userManager;
    private HangoutServiceProvider provider = null;
    private PacketRouter router;
    private OfflineMessageStore offlineMessageStore;
    private PresenceManager presenceManager;

    public IQHangoutDetailHandler()
    {
        this.init();
    }

    public void init()
    {
        provider = new HangoutServiceProvider();
        userManager = UserManager.getInstance();
        router = XMPPServer.getInstance().getPacketRouter();
        offlineMessageStore =  OfflineMessageStore.getInstance();
        presenceManager = XMPPServer.getInstance().getPresenceManager();
    }

    public void destory()
    {
        provider = null;
        router = null;
        offlineMessageStore= null;
        presenceManager=null;
    }


    public IQ handleIQRequest(IQ packet)
    {
        IQ reply = null;
        Element iq = packet.getChildElement();
        String name = iq.getName();

        if (CreateElement.equals(name))
        {
           HangoutDAO hangout = this.CreateDetail(packet);
           Document document = DocumentHelper.createDocument();
           Element create = document.addElement(CreateElement,HangoutComponent.HANGOUT_DETAIL);
           create.setText(String.valueOf(hangout.getHangoutid()));
           reply = IQAdditions.createResultIQ(packet,create);
           for (HangoutUserDAO user : hangout.getUserDAOList())
           {
                if (userManager.isRegisteredUser(user.getUsername()))
                {
                    User toUser = null;
                    try {
                        toUser = userManager.getUser(user.getUsername());
                        HangoutMessagePacketWrapper messagePacketHandler = new HangoutMessagePacketWrapper(HangoutComponent.HANGOUT_MESSAGE);
                        messagePacketHandler.addHangoutDetailContent(hangout);
                        messagePacketHandler.setFrom(hangout.getCreateUser());
                        messagePacketHandler.setTo(user.getJid());
                        messagePacketHandler.setID(packet.getID());
                        if (presenceManager.isAvailable(toUser)) {
                            router.route(messagePacketHandler.getMessage());
                        } else {
                            offlineMessageStore.addMessage((Message) messagePacketHandler.getMessage());
                        }
                    } catch (UserNotFoundException e) {
                        Log.error(e.toString());
                    }
                }
               else
                {
                    //Not Implement at this stage. for SMS FOR EMAIL
                }
           }
           //Board
           return reply;
        }
        else if (UpdateElement.equals(name))
        {
            return  null;
        }
        else if (QueryElement.equals(name))
        {
            return  null;
        }
        else
        {
            reply = IQ.createResultIQ(packet);
            reply.setError(PacketError.Condition.feature_not_implemented);
            return reply;
        }

    }

    private HangoutDAO CreateDetail(IQ packet)
    {
        return provider.createHangout(packet);
    }
}
