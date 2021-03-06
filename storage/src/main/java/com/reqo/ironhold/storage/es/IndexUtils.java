package com.reqo.ironhold.storage.es;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.elasticsearch.common.joda.time.DateTime;
import org.elasticsearch.common.joda.time.Period;
import org.elasticsearch.common.joda.time.format.PeriodFormatter;
import org.elasticsearch.common.joda.time.format.PeriodFormatterBuilder;
import org.elasticsearch.search.SearchHit;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@SuppressWarnings("unchecked")
public class IndexUtils {
    private static ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.configure(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS,
                false);
    }

    private static PeriodFormatter yearsAndMonthsFormatter = new PeriodFormatterBuilder()
            .appendYears().appendSuffix(" year", " years")
            .appendSeparator(" ").appendMonths()
            .appendSuffix(" month", " months").appendLiteral(" ago")
            .toFormatter();

    private static PeriodFormatter daysFormatter = new PeriodFormatterBuilder()
            .appendDays().appendSuffix(" day", " days").appendLiteral(" ago")
            .toFormatter();

    private static PeriodFormatter daysAndHoursFormatter = new PeriodFormatterBuilder()
            .appendDays().appendSuffix(" " + "day", " days").appendHours()
            .appendSuffix(" hour", " hours").appendLiteral(" ago")
            .toFormatter();

    private static PeriodFormatter hoursAndMinsFormatter = new PeriodFormatterBuilder()
            .appendHours().appendSuffix(" " + "" + "hour", " hours")
            .appendMinutes().appendSuffix(" min", " mins")
            .appendLiteral(" ago").toFormatter();
    public static final int PREVIEW_RECIPIENT_COUNT = 4;

    public static String getFieldValue(SearchHit hit, IndexFieldEnum field) {
        return getFieldValue(hit, field, null, true);
    }

    public static String getFieldValue(SearchHit hit, IndexFieldEnum field, IndexFieldEnum subField) {
        return getFieldValue(hit, field, subField, true);

    }

    public static String getFieldValue(SearchHit hit, IndexFieldEnum field, IndexFieldEnum subField,
                                       boolean preview) {
        final SimpleDateFormat sdf = new SimpleDateFormat(
                "EEE, d MMM yyyy HH:mm:ss z");

        String key = field.getValue();
        String subKey = subField != null ? subField.getValue() : StringUtils.EMPTY;
        if (preview && hit.getHighlightFields().containsKey(key)) {
            return StringUtils.join(hit.getHighlightFields().get(key)
                    .getFragments(), " ... ");
        } else if (hit.getFields().containsKey(key) || hit.getFields().containsKey(subKey)) {
            switch (field) {
                case DATE:
                    try {
                        StringBuilder dateString = new StringBuilder();
                        Date dateValue = mapper
                                .getSerializationConfig()
                                .getDateFormat()
                                .parse((String) hit.getFields().get(key).getValue());

                        dateString.append(sdf.format(dateValue));
                        dateString.append(" (");

                        Period p = new Period(new DateTime(dateValue),
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

                        dateString.append(")");

                        return dateString.toString();
                    } catch (ParseException e) {
                    }
                    break;
                case IMPORTANCE:
                    return hit.getFields().get(key).getValue();
                case SIZE:
                    int size = (Integer) (hit.getFields().get(key).getValue());
                    return FileUtils.byteCountToDisplaySize(size);

                case FROM_NAME:
                case TO_NAME:
                case CC_NAME:
                case BCC_NAME:
                    return getRecipientInfo(hit, field, subField, preview);
                case BODY:
                    if (!preview) {
                        if (hit.getHighlightFields().get(key) != null) {
                            return hit.getHighlightFields().get(key).getFragments()[0]
                                    .string();
                        } else {
                            return (String) hit.getFields().get(key).getValue();
                        }
                    } else {
                        return StringUtils.abbreviate(
                                (String) hit.getFields().get(key).getValue(), 300)
                                + "...";
                    }
                case SUBJECT:
                    if (hit.getHighlightFields().get(key) != null) {
                        return hit.getHighlightFields().get(key).getFragments()[0]
                                .string();
                    } else {
                        return (String) hit.getFields().get(key).getValue();
                    }
            }

        }

        return StringUtils.EMPTY;
    }

    private static String getRecipientInfo(SearchHit hit, IndexFieldEnum key,
                                           IndexFieldEnum subKey, boolean preview) {


        Object name = null;
        if (hit.getFields().containsKey(key.getValue())) {
            name = hit.getFields().get(key.getValue()).getValue();
        }

        Object address = null;
        if (hit.getFields().containsKey(subKey.getValue())) {
            address = hit.getFields().get(subKey.getValue()).getValue();
        }
        if (name instanceof String || address instanceof String) {

            if (name == null || name.equals("unknown")) {
                name = StringUtils.EMPTY;
            }
                                           /*
            return String.format("%s %s", name,
                    showAddress((String) name, (String) address));*/
            return String.format("%s ", name);

        } else {

            StringBuilder result = new StringBuilder();
            List<String> namesArray = (List<String>) name;
            List<String> addressesArray = (List<String>) address;
            int max = preview ? Math.min(PREVIEW_RECIPIENT_COUNT,
                    namesArray.size()) : namesArray.size();
            for (int i = 0; i < max; i++) {
                String leadingComma = i == 0 ? "" : ", ";
                name = namesArray.get(i);

                if (name == null || name.equals("unknown")) {
                    name = StringUtils.EMPTY;
                }
/*                result.append(String.format("%s%s %s", leadingComma,
                        name,
                        showAddress((String) name, addressesArray.get(i))));*/
                result.append(String.format("%s%s ", leadingComma,
                        name));

            }

            if (preview && namesArray.size() > PREVIEW_RECIPIENT_COUNT) {
                result.append(String.format("<i>+%d more</i>",
                        namesArray.size() - PREVIEW_RECIPIENT_COUNT));
            }

            return result.toString();
        }
    }

    private static String showAddress(String name, String address) {
        if (address.trim().length() == 0) {
            return StringUtils.EMPTY;
        }
        if (name != null && name.contains(address)) {
            return StringUtils.EMPTY;
        }

        return String.format("&lt;%s&gt;", address);
    }
}
