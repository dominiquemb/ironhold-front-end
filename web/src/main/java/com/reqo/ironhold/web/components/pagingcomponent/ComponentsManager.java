package com.reqo.ironhold.web.components.pagingcomponent;

import com.reqo.ironhold.web.components.pagingcomponent.builder.ElementsBuilder;
import com.reqo.ironhold.web.components.pagingcomponent.customizer.element.ElementsCustomizer;
import com.reqo.ironhold.web.components.pagingcomponent.customizer.style.StyleCustomizer;
import com.reqo.ironhold.web.components.pagingcomponent.customizer.style.impl.BackwardCompatibilityStyleCustomizer;
import com.reqo.ironhold.web.components.pagingcomponent.strategy.PageChangeStrategy;
import com.reqo.ironhold.web.components.pagingcomponent.strategy.impl.EvenPageChangeStrategy;
import com.reqo.ironhold.web.components.pagingcomponent.strategy.impl.OddPageChangeStrategy;

/**
 * It contains all class needed by the {@link PagingComponent}.
 */
public class ComponentsManager {
    protected int numberOfButtonsPage;
    protected int numberTotalOfPages;
    protected int numberTotalOfItems;
    protected int numberOfItemsPerPage;
    protected int currentPage;
    protected int previousPage;

    protected ElementsCustomizer elementsCustomizer;
    protected StyleCustomizer styleCustomizer;
    protected ElementsBuilder builder;
    protected PageChangeStrategy strategy;
    protected PagingComponent<?> pagingComponent;

    /**
     * @param numberOfItemsPerPage number of items to display in one page
     * @param numberOfButtonsPage  maximum number of buttons displayed to navigate between pages
     * @param numberTotalOfItems   number of all items to paginate
     * @param elementsCustomizer   the {@link ElementsCustomizer} to personalize the creation of the different buttons and separators
     * @param styleCustomizer      the {@link StyleCustomizer} to personalize the style of the different buttons and separators
     * @param pagingComponent      the {@link PagingComponent} to manage
     */
    public ComponentsManager(int numberOfItemsPerPage, int numberOfButtonsPage, int numberTotalOfItems, ElementsCustomizer elementsCustomizer, StyleCustomizer styleCustomizer, PagingComponent<?> pagingComponent) {
        this.pagingComponent = pagingComponent;
        this.elementsCustomizer = elementsCustomizer;
        this.styleCustomizer = styleCustomizer != null ? styleCustomizer : new BackwardCompatibilityStyleCustomizer(this);
        this.numberOfItemsPerPage = numberOfItemsPerPage;
        this.numberOfButtonsPage = numberOfButtonsPage;
        this.numberTotalOfItems = numberTotalOfItems;
        numberTotalOfPages = (int) Math.ceil((double) numberTotalOfItems / numberOfItemsPerPage); //Calculate the number of pages needed: if there are 105 results and we want 10 results/page --> we will get 11 pages
        currentPage = 1;
        previousPage = -1;

        builder = new ElementsBuilder(this, elementsCustomizer);
        strategy = createPageChangeStrategy();
    }

    /**
     * @param numberOfButtonsPage maximum number of buttons displayed to navigate between pages
     */
    public void setNumberOfButtonsPage(int numberOfButtonsPage) {
        if (this.numberOfButtonsPage == numberOfButtonsPage) {
            return;
        }

        this.numberOfButtonsPage = numberOfButtonsPage;
        if (previousPage != -1) {
            previousPage = currentPage;
        }
        currentPage = 1;
        builder = new ElementsBuilder(this, elementsCustomizer);
        strategy = createPageChangeStrategy();
    }

    public int getNumberOfButtonsPage() {
        return numberOfButtonsPage;
    }

    public int getNumberTotalOfPages() {
        return numberTotalOfPages;
    }

    public int getNumberTotalOfItems() {
        return numberTotalOfItems;
    }

    public int getNumberOfItemsPerPage() {
        return numberOfItemsPerPage;
    }

    /**
     * Update the previous page number with the old value of the current page number and set this one with the new value given in parameters.
     *
     * @param currentPage
     */
    public void setCurrentPage(int currentPage) {
        previousPage = this.currentPage;
        this.currentPage = currentPage;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public int getPreviousPage() {
        return previousPage;
    }

    public PagingComponent<?> getPagingComponent() {
        return pagingComponent;
    }

    public ElementsCustomizer getElementsCustomizer() {
        return elementsCustomizer;
    }

    public StyleCustomizer getButtonsStyleCustomizer() {
        return styleCustomizer;
    }

    public PageChangeStrategy getPageChangeStrategy() {
        return strategy;
    }

    public ElementsBuilder getElementsBuilder() {
        return builder;
    }

    public boolean isBeingInitialized() {
        return previousPage == -1;
    }

    /**
     * @return true if the selected page is the first page.
     */
    public boolean isFirstPage() {
        return currentPage == 1;
    }

    /**
     * @return true if the selected page is the last page.
     */
    public boolean isLastPage() {
        return currentPage == numberTotalOfPages;
    }

    protected PageChangeStrategy createPageChangeStrategy() {
        return (numberOfButtonsPage & 1) == 0 ? new EvenPageChangeStrategy(this) : new OddPageChangeStrategy(this);
    }
}
