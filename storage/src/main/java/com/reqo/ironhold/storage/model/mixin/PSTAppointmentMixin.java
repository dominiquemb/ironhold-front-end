package com.reqo.ironhold.storage.model.mixin;

import org.codehaus.jackson.annotate.JsonIgnore;

import com.pff.PSTTimeZone;

public abstract class PSTAppointmentMixin {
	@JsonIgnore
	abstract PSTTimeZone getRecurrenceTimeZone();

}
