package com.reqo.ironhold.web.components.pagingcomponent.customizer.adaptator.impl;

import com.reqo.ironhold.web.components.pagingcomponent.ComponentsManager;
import com.reqo.ironhold.web.components.pagingcomponent.builder.ElementsBuilder;
import com.reqo.ironhold.web.components.pagingcomponent.button.ButtonPageNavigator;
import com.reqo.ironhold.web.components.pagingcomponent.constant.CaptionConstants;
import com.reqo.ironhold.web.components.pagingcomponent.constant.StyleConstants;
import com.reqo.ironhold.web.components.pagingcomponent.customizer.adaptator.GlobalCustomizer;
import com.reqo.ironhold.web.components.pagingcomponent.utilities.Utils;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.themes.BaseTheme;

/**
 * Allow to style the buttons and separators by CSS.<br><br>
 * The components have the following style name:
 * <ul>
 * <li>button first: button-first</li>
 * <li>button previous: button-previous</li>
 * <li>buttons page: button-page</li>
 * <li>button next: button-next</li>
 * <li>button last: button-last</li>
 * <li>first separator: separator-first</li>
 * <li>second separator: separator-last</li>
 * </ul>
 * When a limit is reached, the relevant components have the CSS class "limit". For example, if the user click on the last page, the button last, next and the last separator will have the style "limit".<br><br>
 * Finally, the style "current" apply at the button page that has the page number selected by the user.
 */
public class CssCustomizerAdaptator implements GlobalCustomizer {

    @Override
    public Button createButtonFirst() {
        Button button = new Button(CaptionConstants.CAPTION_BUTTON_FIRST);
        button.setStyleName(BaseTheme.BUTTON_LINK);
        button.addStyleName(StyleConstants.STYLE_BUTTON_FIRST);
        return button;
    }

    @Override
    public Button createButtonLast() {
        Button button = new Button(CaptionConstants.CAPTION_BUTTON_LAST);
        button.setStyleName(BaseTheme.BUTTON_LINK);
        button.addStyleName(StyleConstants.STYLE_BUTTON_LAST);
        return button;
    }

    @Override
    public Button createButtonPrevious() {
        Button button = new Button(CaptionConstants.CAPTION_BUTTON_PREVIOUS);
        button.setStyleName(BaseTheme.BUTTON_LINK);
        button.addStyleName(StyleConstants.STYLE_BUTTON_PREVIOUS);
        return button;
    }

    @Override
    public Button createButtonNext() {
        Button button = new Button(CaptionConstants.CAPTION_BUTTON_NEXT);
        button.setStyleName(BaseTheme.BUTTON_LINK);
        button.addStyleName(StyleConstants.STYLE_BUTTON_NEXT);
        return button;
    }

    @Override
    public ButtonPageNavigator createButtonPage() {
        ButtonPageNavigator button = new ButtonPageNavigator();
        button.setStyleName(BaseTheme.BUTTON_LINK);
        button.addStyleName(StyleConstants.STYLE_BUTTON_PAGE);
        return button;
    }

    @Override
    public Component createFirstSeparator() {
        AbstractComponent separator = new Label(CaptionConstants.CAPTION_SEPARATOR_FIRST);
        separator.setStyleName(StyleConstants.STYLE_SEPARATOR_FIRST);
        return separator;
    }

    @Override
    public Component createLastSeparator() {
        AbstractComponent separator = new Label(CaptionConstants.CAPTION_SEPARATOR_LAST);
        separator.setStyleName(StyleConstants.STYLE_SEPARATOR_LAST);
        return separator;
    }

    @Override
    public void styleButtonPageNormal(ButtonPageNavigator button, int pageNumber) {
        button.setPage(pageNumber);
        button.removeStyleName(StyleConstants.STYLE_CURRENT);
    }

    @Override
    public void styleButtonPageCurrentPage(ButtonPageNavigator button, int pageNumber) {
        button.setPage(pageNumber);
        button.focus();
        button.addStyleName(StyleConstants.STYLE_CURRENT);
    }

    @Override
    public void styleTheOthersElements(ComponentsManager manager, ElementsBuilder builder) {
        if (manager.isFirstPage()) {
            Utils.addStyleName(builder.getButtonFirst(), StyleConstants.STYLE_LIMIT);
            Utils.addStyleName(builder.getButtonPrevious(), StyleConstants.STYLE_LIMIT);
            Utils.addStyleName(builder.getFirstSeparator(), StyleConstants.STYLE_LIMIT);
        } else {
            Utils.removeStyleName(builder.getButtonFirst(), StyleConstants.STYLE_LIMIT);
            Utils.removeStyleName(builder.getButtonPrevious(), StyleConstants.STYLE_LIMIT);
            Utils.removeStyleName(builder.getFirstSeparator(), StyleConstants.STYLE_LIMIT);
        }

        if (manager.isLastPage()) {
            Utils.addStyleName(builder.getButtonLast(), StyleConstants.STYLE_LIMIT);
            Utils.addStyleName(builder.getButtonNext(), StyleConstants.STYLE_LIMIT);
            Utils.addStyleName(builder.getLastSeparator(), StyleConstants.STYLE_LIMIT);
        } else {
            Utils.removeStyleName(builder.getButtonLast(), StyleConstants.STYLE_LIMIT);
            Utils.removeStyleName(builder.getButtonNext(), StyleConstants.STYLE_LIMIT);
            Utils.removeStyleName(builder.getLastSeparator(), StyleConstants.STYLE_LIMIT);
        }
    }

}
