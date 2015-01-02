package org.thirdplace.handler;

import org.xmpp.packet.IQ;

/**
 * Created with IntelliJ IDEA.
 * User: Yang
 * Date: 28/12/14
 * Time: 11:00 PM
 * To change this template use File | Settings | File Templates.
 */
public interface IQHangoutHandler
{
    IQ handleIQRequest (IQ packet);
}
