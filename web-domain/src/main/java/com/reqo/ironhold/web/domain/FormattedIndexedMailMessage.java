package com.reqo.ironhold.web.domain;

import org.apache.commons.io.FileUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import java.io.IOException;
import java.text.SimpleDateFormat;

/**
 * User: ilya
 * Date: 11/30/13
 * Time: 12:24 PM
 */
public class FormattedIndexedMailMessage extends IndexedMailMessage {
    private static final ObjectMapper mapper = new ObjectMapper();

    private static final PeriodFormatter yearsAndMonthsFormatter = new PeriodFormatterBuilder()
            .appendYears().appendSuffix(" year", " years")
            .appendSeparator(" ").appendMonths()
            .appendSuffix(" month", " months").appendLiteral(" ago")
            .toFormatter();

    private static final PeriodFormatter daysFormatter = new PeriodFormatterBuilder()
            .appendDays().appendSuffix(" day", " days").appendLiteral(" ago")
            .toFormatter();

    private static final PeriodFormatter daysAndHoursFormatter = new PeriodFormatterBuilder()
            .appendDays().appendSuffix(" " + "day", " days").appendHours()
            .appendSuffix(" hour", " hours").appendLiteral(" ago")
            .toFormatter();

    private static final PeriodFormatter hoursAndMinsFormatter = new PeriodFormatterBuilder()
            .appendHours().appendSuffix(" " + "" + "hour", " hours")
            .appendMinutes().appendSuffix(" min", " mins")
            .appendLiteral(" ago").toFormatter();


    private static final SimpleDateFormat messageDateFormatter = new SimpleDateFormat(
            "EEE, d MMM yyyy HH:mm:ss z");

    public String getAge() {

        StringBuilder dateString = new StringBuilder();
        Period p = new Period(new DateTime(this.getMessageDate()),
                new DateTime());

        if (p.getYears() > 0) {
            dateString.append(p.toString(yearsAndMonthsFormatter));
        } else if (p.getDays() > 0) {
            dateString.append(p.toString(daysFormatter));
        } else if (p.getHours() > 48) {
            dateString.append(p.toString(daysAndHoursFormatter));
        } else {
            dateString.append(p.toString(hoursAndMinsFormatter));
        }


        return dateString.toString();
    }

    public String getFormattedSize() {
        return FileUtils.byteCountToDisplaySize(this.getSize());
    }

    public String getFormattedMessageDate() {
        return messageDateFormatter.format(this.getMessageDate());
    }


    public static FormattedIndexedMailMessage deserialize(String json) {
        try {
            return mapper.readValue(json, FormattedIndexedMailMessage.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
