package org.thirdplace.handler;

import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thirdplace.bean.HangoutDAO;
import org.thirdplace.provider.HangoutServiceProvider;
import org.thirdplace.util.DateAdditions;
import org.thirdplace.util.HangoutConstant;
import org.xmpp.packet.IQ;
import org.xmpp.packet.JID;
import org.xmpp.packet.PacketError;

import java.text.ParseException;
import java.util.Date;

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

    private HangoutServiceProvider provider = null;

    public IQHangoutDetailHandler()
    {
        this.init();
    }

    public void init()
    {
        System.out.println("IQHangoutDetailHandler init");
        provider = new HangoutServiceProvider();
    }

    public void destory()
    {
        provider = null;
    }


    public IQ handleIQRequest(IQ packet)
    {
        IQ reply = null;
        Element iq = packet.getChildElement();
        String name = iq.getName();

        if (CreateElement.equals(name))
        {
           this.CreateDetail(packet);
        }
        else if (UpdateElement.equals(name))
        {

        }
        else if (QueryElement.equals(name))
        {

        }
        else
        {
            reply = IQ.createResultIQ(packet);
            reply.setError(PacketError.Condition.feature_not_implemented);
            return reply;
        }
        return  null;
    }

    private void CreateDetail(IQ packet)
    {
        provider.createHangout(packet);
    }
}
