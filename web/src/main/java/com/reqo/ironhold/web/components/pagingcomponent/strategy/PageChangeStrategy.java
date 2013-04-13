package com.reqo.ironhold.web.components.pagingcomponent.strategy;


import com.reqo.ironhold.web.components.pagingcomponent.ComponentsManager;
import com.reqo.ironhold.web.components.pagingcomponent.builder.ElementsBuilder;
import com.reqo.ironhold.web.components.pagingcomponent.button.ButtonPageNavigator;
import com.reqo.ironhold.web.components.pagingcomponent.customizer.style.StyleCustomizer;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;

import java.util.List;

/**
 * Define a strategy to find the page number that the buttons page have to display and to center the button page containing the current page number if it's possible.
 * It allow also to choose and set the styles to the buttons and the separators thanks to {@link StyleCustomizer}.
 */
public abstract class PageChangeStrategy {

    protected int buttonPageMargin;
    protected ComponentsManager manager;

    public PageChangeStrategy(ComponentsManager manager) {
        this.manager = manager;
        buttonPageMargin = manager.getNumberOfButtonsPage() >> 1; // divide by 2
    }

    /**
     * Reorganizes the caption of buttons each time we change a page.
     */
    public void reorganizeButtons(ClickEvent event) {
        int numberTotalOfPages = manager.getNumberTotalOfPages();

        if (event == null) {
            refreshStyleOfAllComponents(numberTotalOfPages);
            return;
        }

        int currentPage = manager.getCurrentPage();
        Button buttonPressed = event.getButton();
        ElementsBuilder builder = manager.getElementsBuilder();

        if (buttonPressed == builder.getButtonFirst()) {
            if (currentPage != 1) {
                manager.setCurrentPage(1);
                refreshStyleOfAllComponents(numberTotalOfPages);
            }

        } else if (buttonPressed == builder.getButtonLast()) {
            if (currentPage != numberTotalOfPages) {
                manager.setCurrentPage(numberTotalOfPages);
                refreshStyleOfAllComponents(numberTotalOfPages);
            }

        } else if (buttonPressed == builder.getButtonPrevious()) { // Management of button previous
            manager.setCurrentPage(currentPage != 1 ? --currentPage : numberTotalOfPages);
            refreshStyleOfAllComponents(numberTotalOfPages);

        } else if (buttonPressed == builder.getButtonNext()) { // Management of button back
            manager.setCurrentPage(currentPage != numberTotalOfPages ? ++currentPage : 1);
            refreshStyleOfAllComponents(numberTotalOfPages);

        } else { // Management of buttons page
            ButtonPageNavigator button = (ButtonPageNavigator) buttonPressed;
            if (currentPage != button.getPage()) {
                manager.setCurrentPage(button.getPage());
                refreshStyleOfAllComponents(numberTotalOfPages);
            }
        }
    }

    /**
     * Decide which of the methods of {@link StyleCustomizer} must be called on the button given in parameter according that this one display the current page number or not.
     *
     * @param styleCustomizer
     * @param button                 the button to style
     * @param currentPageNumber      the page number selected by the user.
     * @param pageNumberForTheButton the page number that the button should display.
     */
    public void chooseStyle(StyleCustomizer styleCustomizer, ButtonPageNavigator button, int currentPageNumber, int pageNumberForTheButton) {
        if (currentPageNumber == pageNumberForTheButton) {
            styleCustomizer.styleButtonPageCurrentPage(button, pageNumberForTheButton);
        } else {
            styleCustomizer.styleButtonPageNormal(button, pageNumberForTheButton);
        }
    }

    /**
     * Set style of others buttons (first, previous, next, last).
     */
    protected void refreshStyleOfAllComponents(int numberTotalOfPages) {
        ElementsBuilder builder = manager.getElementsBuilder();
        StyleCustomizer styleCustomizer = manager.getButtonsStyleCustomizer();
        styleCustomizer.styleTheOthersElements(manager, builder);
        setStyleAndPageNumberToButtonsPage(builder, styleCustomizer, numberTotalOfPages);
    }

    /**
     * Calculate the page number and set the styles for each buttons page.
     */
    protected void setStyleAndPageNumberToButtonsPage(ElementsBuilder builder, StyleCustomizer styleCustomizer, int numberTotalOfPages) {
        int currentPage = manager.getCurrentPage();
        int numberOfButtonsPage = manager.getNumberOfButtonsPage();
        List<ButtonPageNavigator> listButtons = builder.getListButtons();

        if (manager.isBeingInitialized()) { // if the PagagingComponent is being initialized. We need also to initialize the style of all buttons page.
            for (int i = 0, limit = listButtons.size(); i < limit; i++) {
                chooseStyle(styleCustomizer, listButtons.get(i), currentPage, i + 1);
            }
        } else if (numberTotalOfPages > numberOfButtonsPage) { // if we have more pages of results then the number of buttons -> we need to reorganize every button
            int pageNumberWhereStartTheIteration;
            if (currentPage <= buttonPageMargin) { // current page is in the lower margin
                pageNumberWhereStartTheIteration = 1;
            } else if (currentPage >= numberTotalOfPages - buttonPageMargin) { // current page is in the higher margin
                pageNumberWhereStartTheIteration = numberTotalOfPages - numberOfButtonsPage + 1;
            } else { // current page is not in the margins and we must scroll the number of the button page
                pageNumberWhereStartTheIteration = calculatePageNumberWhereStartTheIteration(currentPage, buttonPageMargin);
            }

            for (int i = 0; i < numberOfButtonsPage; pageNumberWhereStartTheIteration++, i++) {
                chooseStyle(styleCustomizer, listButtons.get(i), currentPage, pageNumberWhereStartTheIteration);
            }
        } else { // if we have less pages of results then the number of buttons -> we only need to highlight the button of the current page and remove highlighting at the button of the previous page
            int previousPage = manager.getPreviousPage();
            styleCustomizer.styleButtonPageNormal(listButtons.get(previousPage - 1), previousPage);
            styleCustomizer.styleButtonPageCurrentPage(listButtons.get(currentPage - 1), currentPage);
        }
    }

    protected abstract int calculatePageNumberWhereStartTheIteration(int currentPage, int buttonPageMargin);

}
