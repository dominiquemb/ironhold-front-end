package com.reqo.ironhold.web.components.pagingcomponent.customizer.style.impl;

import com.reqo.ironhold.web.components.pagingcomponent.ComponentsManager;
import com.reqo.ironhold.web.components.pagingcomponent.PagingComponent;
import com.reqo.ironhold.web.components.pagingcomponent.builder.ElementsBuilder;
import com.reqo.ironhold.web.components.pagingcomponent.button.ButtonPageNavigator;
import com.reqo.ironhold.web.components.pagingcomponent.customizer.style.StyleCustomizer;
import com.reqo.ironhold.web.components.pagingcomponent.strategy.PageChangeStrategy;
import com.reqo.ironhold.web.components.pagingcomponent.utilities.Utils;
import com.vaadin.ui.Button;
import com.vaadin.ui.themes.BaseTheme;

import java.util.ArrayList;
import java.util.List;

/**
 * Allow the backward compatibility with the style methods of {@link PagingComponent} :
 * <ul>
 * <li>{@link PagingComponent#setStyleNameForAllButtons(String)}</li>
 * <li>{@link PagingComponent#addStyleNameForAllButtons(String)}</li>
 * <li>etc.</li>
 * </ul>
 */
public final class BackwardCompatibilityStyleCustomizer implements StyleCustomizer {

    private ComponentsManager manager;
    private List<String> stylesButtonNormal, stylesButtonCurrent;

    public BackwardCompatibilityStyleCustomizer(ComponentsManager manager) {
        this.manager = manager;
        stylesButtonNormal = new ArrayList<String>();
        stylesButtonNormal.add(BaseTheme.BUTTON_LINK);
        stylesButtonCurrent = new ArrayList<String>();
        stylesButtonCurrent.add(BaseTheme.BUTTON_LINK);
    }

    @Override
    public void styleButtonPageNormal(ButtonPageNavigator button, int pageNumber) {
        for (int i = 0; i < stylesButtonNormal.size(); i++) {
            if (i == 0) {
                button.setStyleName(stylesButtonNormal.get(i));
            } else {
                button.addStyleName(stylesButtonNormal.get(i));
            }
        }
        button.setPage(pageNumber);
    }

    @Override
    public void styleButtonPageCurrentPage(ButtonPageNavigator button, int pageNumber) {
        for (int i = 0; i < stylesButtonCurrent.size(); i++) {
            if (i == 0) {
                button.setStyleName(stylesButtonCurrent.get(i));
            } else {
                button.addStyleName(stylesButtonCurrent.get(i));
            }
        }
        button.setPage(pageNumber);
        button.focus();
    }

    @Override
    public void styleTheOthersElements(ComponentsManager manager, ElementsBuilder builder) {
        // do nothing
    }

    /**
     * Sets the same style to all buttons. First, Previous, 1, 2 ... Next, Last
     */
    public void setStyleNameForAllButtons(String style) {
        if (Utils.isEmpty(style)) {
            return;
        }
        setStyleNameButtonsPreviousAndNext(style);
        setStyleNameButtonsFirstAndLast(style);
        stylesButtonNormal.clear();
        stylesButtonCurrent.clear();
        stylesButtonNormal.add(style);
        stylesButtonCurrent.add(style);
        refreshStyleOfButtonsPage();
    }

    /**
     * Sets the style to buttons Previous and Next
     */
    public void setStyleNameButtonsPreviousAndNext(String style) {
        if (Utils.isEmpty(style)) {
            return;
        }

        ElementsBuilder builder = manager.getElementsBuilder();
        Button buttonPrevious = builder.getButtonPrevious();
        Button buttonNext = builder.getButtonNext();

        if (buttonPrevious == null || buttonNext == null) {
            return;
        }

        buttonPrevious.setStyleName(style);
        buttonNext.setStyleName(style);
    }

    /**
     * Sets the style to buttons First and Last
     */
    public void setStyleNameButtonsFirstAndLast(String style) {
        if (Utils.isEmpty(style)) {
            return;
        }

        ElementsBuilder builder = manager.getElementsBuilder();
        Button buttonFirst = builder.getButtonFirst();
        Button ButtonLast = builder.getButtonLast();

        if (buttonFirst == null || ButtonLast == null) {
            return;
        }

        buttonFirst.setStyleName(style);
        ButtonLast.setStyleName(style);
    }

    /**
     * Sets the style to a button of current page
     */
    public void setStyleNameCurrentButtonState(String style) {
        if (Utils.isEmpty(style)) {
            return;
        }
        stylesButtonCurrent.clear();
        stylesButtonCurrent.add(style);
        refreshStyleOfButtonsPage();
    }

    /**
     * Sets the style to the buttons that don't correspond to the current page
     */
    public void setStyleNameNormalButtonsState(String style) {
        if (Utils.isEmpty(style)) {
            return;
        }
        stylesButtonNormal.clear();
        stylesButtonNormal.add(style);
        refreshStyleOfButtonsPage();
    }

    /**
     * Adds an existing style to all buttons
     */
    public void addStyleNameForAllButtons(String style) {
        if (Utils.isEmpty(style)) {
            return;
        }
        addStyleNameButtonsPreviousAndNext(style);
        addStyleNameButtonsFirstAndLast(style);
        stylesButtonNormal.add(style);
        stylesButtonCurrent.add(style);
        refreshStyleOfButtonsPage();
    }

    /**
     * Adds an existing style to buttons Previous and Next
     */
    public void addStyleNameButtonsPreviousAndNext(String style) {
        if (Utils.isEmpty(style)) {
            return;
        }

        ElementsBuilder builder = manager.getElementsBuilder();
        Button buttonPrevious = builder.getButtonPrevious();
        Button buttonNext = builder.getButtonNext();

        if (buttonPrevious == null || buttonNext == null) {
            return;
        }

        buttonPrevious.addStyleName(style);
        buttonNext.addStyleName(style);
    }

    /**
     * Adds an existing style to buttons First and Last
     */
    public void addStyleNameButtonsFirstAndLast(String style) {
        if (Utils.isEmpty(style)) {
            return;
        }

        ElementsBuilder builder = manager.getElementsBuilder();
        Button buttonFirst = builder.getButtonFirst();
        Button ButtonLast = builder.getButtonLast();

        if (buttonFirst == null || ButtonLast == null) {
            return;
        }

        buttonFirst.addStyleName(style);
        ButtonLast.addStyleName(style);
    }

    /**
     * Adds an existing style to a button of current page
     */
    public void addStyleNameCurrentButtonState(String style) {
        if (Utils.isEmpty(style)) {
            return;
        }
        stylesButtonCurrent.add(style);
        refreshStyleOfButtonsPage();
    }

    /**
     * Adds an existing style to the buttons that don't correspond to the current page
     */
    public void addStyleNameNormalButtonsState(String style) {
        if (Utils.isEmpty(style)) {
            return;
        }
        stylesButtonNormal.add(style);
        refreshStyleOfButtonsPage();
    }

    private void refreshStyleOfButtonsPage() {
        PageChangeStrategy strategy = manager.getPageChangeStrategy();
        StyleCustomizer styleCustomizer = manager.getButtonsStyleCustomizer();
        for (ButtonPageNavigator button : manager.getElementsBuilder().getListButtons()) {
            strategy.chooseStyle(styleCustomizer, button, manager.getCurrentPage(), button.getPage());
        }
    }

}
