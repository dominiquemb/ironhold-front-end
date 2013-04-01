package com.reqo.ironhold.web.components.pagingcomponent.customizer.element.impl;

import com.reqo.ironhold.web.components.pagingcomponent.button.ButtonPageNavigator;
import com.reqo.ironhold.web.components.pagingcomponent.constant.CaptionConstants;
import com.reqo.ironhold.web.components.pagingcomponent.customizer.element.ElementsCustomizer;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.themes.BaseTheme;

/**
 * Allow the backward compatibility of the captions.
 */
public final class BackwardCompatibilityElemensCustomizer implements ElementsCustomizer {

	@Override
	public Button createButtonFirst() {
		Button button = new Button(CaptionConstants.CAPTION_BUTTON_FIRST);
		button.setStyleName(BaseTheme.BUTTON_LINK);
		return button;
	}

	@Override
	public Button createButtonLast() {
		Button button = new Button(CaptionConstants.CAPTION_BUTTON_LAST);
		button.setStyleName(BaseTheme.BUTTON_LINK);
		return button;
	}

	@Override
	public Button createButtonPrevious() {
		Button button = new Button(CaptionConstants.CAPTION_BUTTON_PREVIOUS);
		button.setStyleName(BaseTheme.BUTTON_LINK);
		return button;
	}

	@Override
	public Button createButtonNext() {
		Button button = new Button(CaptionConstants.CAPTION_BUTTON_NEXT);
		button.setStyleName(BaseTheme.BUTTON_LINK);
		return button;
	}

	@Override
	public ButtonPageNavigator createButtonPage() {
		ButtonPageNavigator button = new ButtonPageNavigator();
		button.setStyleName(BaseTheme.BUTTON_LINK);
		return button;
	}

	@Override
	public Component createFirstSeparator() {
		return new Label(CaptionConstants.CAPTION_SEPARATOR_FIRST);
	}

	@Override
	public Component createLastSeparator() {
		return new Label(CaptionConstants.CAPTION_SEPARATOR_LAST);
	}

}
