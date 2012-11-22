package com.reqo.ironhold.storage.model.mixin;

import org.codehaus.jackson.annotate.JsonIgnore;

public abstract class PSTMessageMixin {
	@JsonIgnore
	abstract int getMessageRecipMe();

	@JsonIgnore
	abstract String[] getColorCategories();

	@JsonIgnore
	abstract String getItemsString();
}
