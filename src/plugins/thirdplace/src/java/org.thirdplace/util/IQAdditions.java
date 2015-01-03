package org.thirdplace.util;

import org.dom4j.Element;
import org.xmpp.packet.IQ;

/**
 * Created with IntelliJ IDEA.
 * User: Yang
 * Date: 3/01/15
 * Time: 9:04 PM
 * To change this template use File | Settings | File Templates.
 */
public class IQAdditions
{
    public static IQ createResultIQ(IQ packet, Element childElement)
    {
       IQ returniq = IQ.createResultIQ(packet);
       returniq.getElement().add(childElement);
       return  returniq;
    }
}
