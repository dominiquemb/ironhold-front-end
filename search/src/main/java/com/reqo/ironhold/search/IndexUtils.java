package com.reqo.ironhold.search;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

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
		mapper.getSerializationConfig().addMixInAnnotations(PSTMessage.class,
				PSTMessageMixin.class);
		mapper.enableDefaultTyping();
		mapper.configure(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS,
				false);
	}

	private static PeriodFormatter yearsAndMonthsFormatter = new PeriodFormatterBuilder()
			.appendYears().appendSuffix(" year", " years")
			.appendSeparator(" and ").appendMonths()
			.appendSuffix(" month", " months").appendLiteral(" ago")
			.toFormatter();

	private static PeriodFormatter daysFormatter = new PeriodFormatterBuilder()
			.appendDays().appendSuffix(" day", " days").appendLiteral(" ago")
			.toFormatter();

	private static PeriodFormatter daysAndHoursFormatter = new PeriodFormatterBuilder()
			.appendDays().appendSuffix(" day", " days").appendHours()
			.appendSuffix(" hour", " hours").appendLiteral(" ago")
			.toFormatter();

	private static PeriodFormatter hoursAndMinsFormatter = new PeriodFormatterBuilder()
			.appendHours().appendSuffix(" hour", " hours").appendMinutes()
			.appendSuffix(" min", " mins").appendLiteral(" ago").toFormatter();

	public static String getFieldValue(SearchHit hit, IndexFieldEnum field) {
		return getFieldValue(hit, field, true);

	}

	public static String getFieldValue(SearchHit hit, IndexFieldEnum field,
			boolean preview) {
		final SimpleDateFormat sdf = new SimpleDateFormat(
				"EEE, d MMM yyyy HH:mm:ss z");

		String key = StringUtils.EMPTY;
		String subKey = StringUtils.EMPTY;
		switch (field) {
		case SUBJECT:
			key = "pstMessage.subject";
			break;
		case BODY:
			key = "pstMessage.body";
			break;
		case ATTACHMENT:
			key = "pstMessage.attachments.body";
			break;
		case DATE:
			key = "pstMessage.messageDeliveryTime";
			break;
		case SIZE:
			key = "pstMessage.messageSize";
			break;
		case FROM:
			key = "pstMessage.sentRepresentingName";
			subKey = "pstMessage.sentRepresentingEmailAddress";
			break;
		case TO:
			key = "pstMessage.displayTo";
			break;
		case CC:
			key = "pstMessage.displayCc";
			break;
		default:
			break;
		}

		if (preview && hit.getHighlightFields().containsKey(key)) {
			return StringUtils.join(hit.getHighlightFields().get(key)
					.getFragments(), " ... ");
		} else if (hit.getFields().containsKey(key)) {
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
					} else if (p.getDays() > 3) {
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
			case FROM:
				StringBuilder fromString = new StringBuilder();
				String name = hit.getFields().get(key).getValue();
				String address = hit.getFields().get(subKey).getValue();
				fromString.append(name);
				if (!name.equals(address)) {
					fromString.append(" &lt;");
					fromString.append(address);
					fromString.append("&gt;");
				}

				return fromString.toString();
			case TO:
			case CC:
			case BCC:
				
				String stringValue = hit.getFields().get(key).getValue();
				String[] tokens = stringValue.split(";");
				if (preview && tokens.length > 2) {
					return tokens[0].trim() + "; " + tokens[1].trim() + "; <i>(+"
							+ (tokens.length - 2) + " more)</i>";
				} else {
					return stringValue;
				}
			default:
				if (!preview) {
					return (String) hit.getFields().get(key).getValue();
				} else {
					return StringUtils.abbreviate(
							(String) hit.getFields().get(key).getValue(), 300);
				}
			}
		}

		return StringUtils.EMPTY;
	}
}
