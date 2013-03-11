package com.reqo.ironhold.model.mixin;

import com.pff.PSTTimeZone;
import org.codehaus.jackson.annotate.JsonIgnore;

public abstract class PSTAppointmentMixin {
	@JsonIgnore
	abstract PSTTimeZone getRecurrenceTimeZone();
	
	@JsonIgnore
	abstract PSTTimeZone getStartTimeZone();

	@JsonIgnore
	abstract PSTTimeZone getEndTimeZone();
}
