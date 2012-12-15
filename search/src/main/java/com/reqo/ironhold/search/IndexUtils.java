package com.reqo.ironhold.search;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.elasticsearch.common.joda.time.DateTime;
import org.elasticsearch.common.joda.time.Period;
import org.elasticsearch.common.joda.time.format.PeriodFormatter;
import org.elasticsearch.common.joda.time.format.PeriodFormatterBuilder;
import org.elasticsearch.search.SearchHit;

import com.pff.PSTMessage;
import com.reqo.ironhold.storage.model.mixin.PSTMessageMixin;

public class IndexUtils {
    private static ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.getSerializationConfig().addMixInAnnotations(PSTMessage.class, PSTMessageMixin.class);
        mapper.enableDefaultTyping();
        mapper.configure(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    private static PeriodFormatter yearsAndMonthsFormatter = new PeriodFormatterBuilder().appendYears().appendSuffix
            (" year", " years").appendSeparator(" and ").appendMonths().appendSuffix(" month",
            " months").appendLiteral(" ago").toFormatter();

    private static PeriodFormatter daysFormatter = new PeriodFormatterBuilder().appendDays().appendSuffix(" day",
            " days").appendLiteral(" ago").toFormatter();

    private static PeriodFormatter daysAndHoursFormatter = new PeriodFormatterBuilder().appendDays().appendSuffix(" "
            + "day", " days").appendHours().appendSuffix(" hour", " hours").appendLiteral(" ago").toFormatter();

    private static PeriodFormatter hoursAndMinsFormatter = new PeriodFormatterBuilder().appendHours().appendSuffix(" " +
            "" + "hour", " hours").appendMinutes().appendSuffix(" min", " mins").appendLiteral(" ago").toFormatter();
    public static final int PREVIEW_RECIPIENT_COUNT = 3;

    public static String getFieldValue(SearchHit hit, IndexFieldEnum field) {
        return getFieldValue(hit, field, true);

    }

    public static String getFieldValue(SearchHit hit, IndexFieldEnum field, boolean preview) {
        final SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z");

        String key = field.getValue();

        if (preview && hit.getHighlightFields().containsKey(key)) {
            return StringUtils.join(hit.getHighlightFields().get(key).getFragments(), " ... ");
        } else if (hit.getFields().containsKey(key)) {
            switch (field) {
                case DATE:
                    try {
                        StringBuilder dateString = new StringBuilder();
                        Date dateValue = mapper.getSerializationConfig().getDateFormat().parse((String) hit.getFields
                                ().get(key).getValue());

                        dateString.append(sdf.format(dateValue));
                        dateString.append(" (");

                        Period p = new Period(new DateTime(dateValue), new DateTime());

                        if (p.getYears() > 0) {
                            dateString.append(p.toString(yearsAndMonthsFormatter));
                        } else if (p.getDays() > PREVIEW_RECIPIENT_COUNT) {
                            dateString.append(p.toString(daysFormatter));
                        } else if (p.getHours() > 48) {
                            dateString.append(p.toString(daysAndHoursFormatter));
                        } else {
                            dateString.append(p.toString(hoursAndMinsFormatter));
                        }

                        dateString.append(")");

                        return dateString.toString();
                    } catch (ParseException e) {
                    }
                    break;
                case SIZE:
                    int size = (Integer) (hit.getFields().get(key).getValue());
                    return FileUtils.byteCountToDisplaySize(size);

                case FROM_NAME:
                    return getRecipientInfo(hit, IndexFieldEnum.FROM_NAME, IndexFieldEnum.FROM_ADDRESS, preview);

                case TO_NAME:
                    return getRecipientInfo(hit, IndexFieldEnum.TO_NAME, IndexFieldEnum.TO_ADDRESS, preview);

                case CC_NAME:
                    return getRecipientInfo(hit, IndexFieldEnum.CC_NAME, IndexFieldEnum.CC_ADDRESS, preview);

                case BODY:
                case SUBJECT:
                    if (!preview) {
                        return (String) hit.getFields().get(key).getValue();
                    } else {
                        return StringUtils.abbreviate((String) hit.getFields().get(key).getValue(), 300);
                    }
            }
        }

        return StringUtils.EMPTY;
    }

    private static String getRecipientInfo(SearchHit hit, IndexFieldEnum key, IndexFieldEnum subKey, boolean preview) {
        Object names = hit.getFields().get(key.getValue()).getValue();
        Object addresses = hit.getFields().get(subKey.getValue()).getValue();

        if (names instanceof String) {

            return String.format("%s %s", names, showAddress((String) names, (String) addresses));
        } else {

            StringBuilder result = new StringBuilder();
            List<String> namesArray = (List<String>) names;
            List<String> addressesArray = (List<String>) addresses;
            int max = preview ? Math.min(PREVIEW_RECIPIENT_COUNT, namesArray.size()) : namesArray.size();
            for (int i = 0; i < max; i++) {
                String leadingComma = i == 0 ? "" : ", ";
                result.append(String.format("%s%s %s", leadingComma, namesArray.get(i),
                        showAddress(namesArray.get(i), addressesArray.get(i))));
            }

            if (preview && namesArray.size() > PREVIEW_RECIPIENT_COUNT) {
                result.append(String.format("<i>+%d more</i>", namesArray.size() - PREVIEW_RECIPIENT_COUNT));
            }

            return result.toString();
        }
    }

    private static String showAddress(String name, String address) {
        if (address.trim().length() == 0) {
            return StringUtils.EMPTY;
        }
        if (name.contains(address)) {
            return StringUtils.EMPTY;
        }

        return String.format("&lt;%s&gt;", address);
    }
}
