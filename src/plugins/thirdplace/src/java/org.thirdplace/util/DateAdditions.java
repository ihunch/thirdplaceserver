package org.thirdplace.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created with IntelliJ IDEA.
 * User: Yang
 * Date: 30/12/14
 * Time: 10:36 PM
 * To change this template use File | Settings | File Templates.
 */
public class DateAdditions
{
    public static Date stringToDate(String date, String format) throws ParseException
    {
        DateFormat dateFormat = new SimpleDateFormat(format);
        return dateFormat.parse(date);
    }
}
