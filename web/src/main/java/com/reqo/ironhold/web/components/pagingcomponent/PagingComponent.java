package com.reqo.ironhold.web.components.pagingcomponent;

import com.reqo.ironhold.web.components.pagingcomponent.builder.ElementsBuilder;
import com.reqo.ironhold.web.components.pagingcomponent.button.ButtonPageNavigator;
import com.reqo.ironhold.web.components.pagingcomponent.customizer.adaptator.GlobalCustomizer;
import com.reqo.ironhold.web.components.pagingcomponent.customizer.element.ElementsCustomizer;
import com.reqo.ironhold.web.components.pagingcomponent.customizer.element.impl.BackwardCompatibilityElemensCustomizer;
import com.reqo.ironhold.web.components.pagingcomponent.customizer.element.impl.CssElemensCustomizer;
import com.reqo.ironhold.web.components.pagingcomponent.customizer.style.StyleCustomizer;
import com.reqo.ironhold.web.components.pagingcomponent.customizer.style.impl.BackwardCompatibilityStyleCustomizer;
import com.reqo.ironhold.web.components.pagingcomponent.customizer.style.impl.CssStyleCustomizer;
import com.reqo.ironhold.web.components.pagingcomponent.utilities.FakeList;
import com.reqo.ironhold.web.components.pagingcomponent.utilities.Utils;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This class creates a layout to navigate in different pages of results, the layout contains a button "First", "Last", "Previous", "Next"
 * and as many buttons (called button pages) as the variable "numberOfButtonsPage".
 * Each button has a caption as page number which is updated when navigating.
 */
@SuppressWarnings("serial")
public class PagingComponent<E> extends HorizontalLayout implements Button.ClickListener {

    protected ComponentsManager manager;
    protected List<E> itemsList;  // List with all items.

    // Necessary for the listener  (code copy/pasted from Vaadin's ButtonClickListener, looks heavy but necessary for makeing it work with anonymous Listeners)
    public static final Method METHOD_DISPLAY_PAGE;

    static {
        try {
            METHOD_DISPLAY_PAGE = PagingComponentListener.class.getDeclaredMethod("displayPage", new Class[]{ChangePageEvent.class});
        } catch (final java.lang.NoSuchMethodException e) {
            // This should never happen
            throw new java.lang.RuntimeException("Internal error finding methods in PagingComponent", e);
        }
    }

    /**
     * Deprecated: this constructor is useless and it will be removed in a major version. Use {@link #PagingComponent(int, int, Collection, PagingComponentListener)}
     *
     * @param numberOfItemsPerPage    number of items to display in one page
     * @param itemsCollection         the items to paginate
     * @param pagingComponentListener your {@link PagingComponentListener} that allow you to add the items to display into your layout
     */
    @Deprecated
    public PagingComponent(int numberOfItemsPerPage, Collection<E> itemsCollection, PagingComponentListener<E> pagingComponentListener) {
        this(numberOfItemsPerPage, 9, itemsCollection, new BackwardCompatibilityElemensCustomizer(), null, pagingComponentListener);
    }

    /**
     * @param numberOfItemsPerPage    number of items to display in one page
     * @param numberOfButtonsPage     maximum number of buttons displayed to navigate between pages
     * @param itemsCollection         the items to paginate
     * @param pagingComponentListener your {@link PagingComponentListener} that allow you to add the items to display into your layout
     */
    public PagingComponent(int numberOfItemsPerPage, int numberOfButtonsPage, Collection<E> itemsCollection, PagingComponentListener<E> pagingComponentListener) {
        this(numberOfItemsPerPage, numberOfButtonsPage, itemsCollection, new BackwardCompatibilityElemensCustomizer(), null, pagingComponentListener);
    }

    /**
     * @param numberOfItemsPerPage    number of items to display in one page
     * @param numberOfButtonsPage     maximum number of buttons displayed to navigate between pages
     * @param itemsCollection         the items to paginate
     * @param elementsCustomizer      the {@link ElementsCustomizer} to personalize the creation of the different buttons and separators
     * @param pagingComponentListener your {@link PagingComponentListener} that allow you to add the items to display into your layout
     */
    public PagingComponent(int numberOfItemsPerPage, int numberOfButtonsPage, Collection<E> itemsCollection, ElementsCustomizer elementsCustomizer, PagingComponentListener<E> pagingComponentListener) {
        this(numberOfItemsPerPage, numberOfButtonsPage, itemsCollection, elementsCustomizer, new CssStyleCustomizer(), pagingComponentListener);
    }

    /**
     * @param numberOfItemsPerPage    number of items to display in one page
     * @param numberOfButtonsPage     maximum number of buttons displayed to navigate between pages
     * @param itemsCollection         the items to paginate
     * @param styleCustomizer         the {@link StyleCustomizer} to personalize the style of the different buttons and separators
     * @param pagingComponentListener your {@link PagingComponentListener} that allow you to add the items to display into your layout
     */
    public PagingComponent(int numberOfItemsPerPage, int numberOfButtonsPage, Collection<E> itemsCollection, StyleCustomizer styleCustomizer, PagingComponentListener<E> pagingComponentListener) {
        this(numberOfItemsPerPage, numberOfButtonsPage, itemsCollection, new CssElemensCustomizer(), styleCustomizer, pagingComponentListener);
    }

    /**
     * @param numberOfItemsPerPage    number of items to display in one page
     * @param numberOfButtonsPage     maximum number of buttons displayed to navigate between pages
     * @param itemsCollection         the items to paginate
     * @param customizer              the {@link GlobalCustomizer} to personalize the creation and the style of the different buttons and separators
     * @param pagingComponentListener your {@link PagingComponentListener} that allow you to add the items to display into your layout
     */
    public PagingComponent(int numberOfItemsPerPage, int numberOfButtonsPage, Collection<E> itemsCollection, GlobalCustomizer customizer, PagingComponentListener<E> pagingComponentListener) {
        this(numberOfItemsPerPage, numberOfButtonsPage, itemsCollection, customizer, customizer, pagingComponentListener);
    }

    /**
     * @param numberOfItemsPerPage    number of items to display in one page
     * @param numberOfButtonsPage     maximum number of buttons displayed to navigate between pages
     * @param itemsCollection         the items to paginate
     * @param elementsCustomizer      the {@link ElementsCustomizer} to personalize the creation of the different buttons and separators
     * @param styleCustomizer         the {@link StyleCustomizer} to personalize the style of the different buttons and separators
     * @param pagingComponentListener your {@link PagingComponentListener} that allow you to add the items to display into your layout
     */
    protected PagingComponent(int numberOfItemsPerPage, int numberOfButtonsPage, Collection<E> itemsCollection, ElementsCustomizer elementsCustomizer, StyleCustomizer styleCustomizer, PagingComponentListener<E> pagingComponentListener) {
        manager = createComponentsManager(numberOfItemsPerPage, numberOfButtonsPage, itemsCollection, elementsCustomizer, styleCustomizer);

        itemsList = createItemsList(itemsCollection);

        addComponents();
        addListener(pagingComponentListener);

        //throw event for PagingComponentListener and update for the first time RangeDisplayer
        runChangePageEvent(null);
    }

    @Override
    public void buttonClick(ClickEvent event) {
        runChangePageEvent(event);
    }

    public void addListener(PagingComponentListener<E> listener) {
        addListener(ChangePageEvent.class, listener, METHOD_DISPLAY_PAGE);
    }

    public void removeListener(PagingComponentListener<E> listener) {
        removeListener(ChangePageEvent.class, listener, METHOD_DISPLAY_PAGE);
    }

    /**
     * @param numberOfButtonsPage maximum number of buttons displayed to navigate between pages
     */
    public void setNumberOfButtonsPage(int numberOfButtonsPage) {
        manager.setNumberOfButtonsPage(numberOfButtonsPage);
        runChangePageEvent(null);
        addComponents();
    }

    public PageRange<E> getPageRange() {
        return new PageRange<E>(manager.getCurrentPage(), manager.getNumberOfItemsPerPage(), itemsList);
    }

    /**
     * If you want to get something, it is through this object that you will get it.
     *
     * @return the {@link ComponentsManager} of this {@link PagingComponent}
     */
    public ComponentsManager getComponentsManager() {
        return manager;
    }

    /**
     * Deprecated: use {@link ElementsBuilder#getButtonsPage()} that you can get by the {@link ComponentsManager}.
     *
     * @return list of all buttons page that allow to go to the page they indicate.
     */
    @Deprecated
    public List<ButtonPageNavigator> getButtonsPage() {
        return manager.getElementsBuilder().getListButtons();
    }

    /**
     * Deprecated: use {@link ElementsBuilder#getButtonPrevious()} that you can get by the {@link ComponentsManager}.
     *
     * @return the button that allow to go to the previous page.
     */
    @Deprecated
    public Button getButtonPrevious() {
        return manager.getElementsBuilder().getButtonPrevious();
    }

    /**
     * Deprecated: use {@link ElementsBuilder#getButtonNext()} that you can get by the {@link ComponentsManager}.
     *
     * @return the button that allow to go to the next page.
     */
    @Deprecated
    public Button getButtonNext() {
        return manager.getElementsBuilder().getButtonNext();
    }

    /**
     * Deprecated: use {@link ElementsBuilder#getButtonFirst()} that you can get by the {@link ComponentsManager}.
     *
     * @return the button that allow to go to the first page.
     */
    @Deprecated
    public Button getButtonFirst() {
        return manager.getElementsBuilder().getButtonFirst();
    }

    /**
     * Deprecated: use {@link ElementsBuilder#getButtonLast()} that you can get by the {@link ComponentsManager}.
     *
     * @return the button that allow to go to the last page.
     */
    @Deprecated
    public Button getButtonLast() {
        return manager.getElementsBuilder().getButtonLast();
    }

    protected void runChangePageEvent(ClickEvent event) {
        manager.getPageChangeStrategy().reorganizeButtons(event);
        fireEvent(new ChangePageEvent<E>(this, getPageRange()));
    }

    /**
     * Override this method if you want that the {@link PagingComponent} use a particular {@link ComponentsManager}.
     *
     * @param numberOfItemsPerPage number of items to display in one page
     * @param numberOfButtonsPage  maximum number of buttons displayed to navigate between pages
     * @param itemsCollection      the items to paginate
     * @param elementsCustomizer   the {@link ElementsCustomizer} to personalize the creation of the different buttons and separators
     * @param styleCustomizer      the {@link StyleCustomizer} to personalize the style of the different buttons and separators
     * @return a {@link ComponentsManager}
     */
    protected ComponentsManager createComponentsManager(int numberOfItemsPerPage, int numberOfButtonsPage, Collection<E> itemsCollection, ElementsCustomizer elementsCustomizer, StyleCustomizer styleCustomizer) {
        return new ComponentsManager(numberOfItemsPerPage, numberOfButtonsPage, itemsCollection.size(), elementsCustomizer, styleCustomizer, this);
    }

    /**
     * Override this method if you want use a special collection like Apache Commons Collections.
     *
     * @param collection the collection given in parameter to the {@link PagingComponent}
     * @return a list of items to paginate
     */
    protected List<E> createItemsList(Collection<E> collection) {
        return collection instanceof FakeList ? (FakeList<E>) collection : new ArrayList<E>(collection);
    }

    protected void addComponents() {
        setSpacing(true);
        removeAllComponents();
        ElementsBuilder builder = manager.getElementsBuilder();
        Utils.addComponent(this, builder.getButtonFirst());
        Utils.addComponent(this, builder.getButtonPrevious());
        Utils.addComponent(this, builder.getFirstSeparator());
        for (Button button : builder.getListButtons()) {
            addComponent(button);
        }
        Utils.addComponent(this, builder.getLastSeparator());
        Utils.addComponent(this, builder.getButtonNext());
        Utils.addComponent(this, builder.getButtonLast());
    }

    /////////////////////////////**these methods are used to control the style of buttons**
    /////////////////////////////**these methods are used to control the style of buttons**
    /////////////////////////////**these methods are used to control the style of buttons**
    /////////////////////////////**these methods are used to control the style of buttons**
    /////////////////////////////**these methods are used to control the style of buttons**
    /////////////////////////////**these methods are used to control the style of buttons**
    /////////////////////////////**these methods are used to control the style of buttons**

    /**
     * Sets the same style to all buttons. First, Previous, 1, 2 ... Next, Last<br><br>
     * <p/>
     * NOTE : Don't use it if you have created the <code>PagingComponent</code> with a "<code>Customizer</code>" in parameter because this one does this job.
     */
    @Deprecated
    public void setStyleNameForAllButtons(String style) {
        getBackwardCompatibilityStyleCustomizer().setStyleNameForAllButtons(style);
    }

    /**
     * Sets the style to buttons Previous and Next<br><br>
     * <p/>
     * NOTE : Don't use it if you have created the <code>PagingComponent</code> with a "<code>Customizer</code>" in parameter because this one does this job.
     */
    @Deprecated
    public void setStyleNameButtonsPreviousAndNext(String style) {
        getBackwardCompatibilityStyleCustomizer().setStyleNameButtonsPreviousAndNext(style);
    }

    /**
     * Sets the style to buttons First and Last<br><br>
     * <p/>
     * NOTE : Don't use it if you have created the <code>PagingComponent</code> with a "<code>Customizer</code>" in parameter because this one does this job.
     */
    @Deprecated
    public void setStyleNameButtonsFirstAndLast(String style) {
        getBackwardCompatibilityStyleCustomizer().setStyleNameButtonsFirstAndLast(style);
    }

    /**
     * Sets the style to a button of current page<br><br>
     * <p/>
     * NOTE : Don't use it if you have created the <code>PagingComponent</code> with a "<code>Customizer</code>" in parameter because this one does this job.
     */
    @Deprecated
    public void setStyleNameCurrentButtonState(String style) {
        getBackwardCompatibilityStyleCustomizer().setStyleNameCurrentButtonState(style);
    }

    /**
     * Sets the style to the buttons that don't correspond to the current page<br><br>
     * <p/>
     * NOTE : Don't use it if you have created the <code>PagingComponent</code> with a "<code>Customizer</code>" in parameter because this one does this job.
     */
    @Deprecated
    public void setStyleNameNormalButtonsState(String style) {
        getBackwardCompatibilityStyleCustomizer().setStyleNameNormalButtonsState(style);
    }

    /**
     * Adds an existing style to all buttons<br><br>
     * <p/>
     * NOTE : Don't use it if you have created the <code>PagingComponent</code> with a "<code>Customizer</code>" in parameter because this one does this job.
     */
    @Deprecated
    public void addStyleNameForAllButtons(String style) {
        getBackwardCompatibilityStyleCustomizer().addStyleNameForAllButtons(style);
    }

    /**
     * Adds an existing style to buttons Previous and Next<br><br>
     * <p/>
     * NOTE : Don't use it if you have created the <code>PagingComponent</code> with a "<code>Customizer</code>" in parameter because this one does this job.
     */
    @Deprecated
    public void addStyleNameButtonsPreviousAndNext(String style) {
        getBackwardCompatibilityStyleCustomizer().addStyleNameButtonsPreviousAndNext(style);
    }

    /**
     * Adds an existing style to buttons First and Last<br><br>
     * <p/>
     * NOTE : Don't use it if you have created the <code>PagingComponent</code> with a "<code>Customizer</code>" in parameter because this one does this job.
     */
    @Deprecated
    public void addStyleNameButtonsFirstAndLast(String style) {
        getBackwardCompatibilityStyleCustomizer().addStyleNameButtonsFirstAndLast(style);
    }

    /**
     * Adds an existing style to a button of current page<br><br>
     * <p/>
     * NOTE : Don't use it if you have created the <code>PagingComponent</code> with a "<code>Customizer</code>" in parameter because this one does this job.
     */
    @Deprecated
    public void addStyleNameCurrentButtonState(String style) {
        getBackwardCompatibilityStyleCustomizer().addStyleNameCurrentButtonState(style);
    }

    /**
     * Adds an existing style to the buttons that don't correspond to the current page<br><br>
     * <p/>
     * NOTE : Don't use it if you have created the <code>PagingComponent</code> with a "<code>Customizer</code>" in parameter because this one does this job.
     */
    @Deprecated
    public void addStyleNameNormalButtonsState(String style) {
        getBackwardCompatibilityStyleCustomizer().addStyleNameNormalButtonsState(style);
    }

    /**
     * @return {@link BackwardCompatibilityStyleCustomizer}
     * @throws RuntimeException it is thrown when a {@link BackwardCompatibilityStyleCustomizer} isn't found
     */
    protected BackwardCompatibilityStyleCustomizer getBackwardCompatibilityStyleCustomizer() throws RuntimeException {
        StyleCustomizer styleCustomizer = manager.getButtonsStyleCustomizer();

        if (!(styleCustomizer instanceof BackwardCompatibilityStyleCustomizer)) {
            throw new RuntimeException("Error in PagingComponent:\n" +
                    "You can't use the style methods of the PagingComponent when this one is created with a \"Customizer\" in parameter.\n" +
                    "If you want customize together the creation and the style of buttons, give a GlobalCustomizer as parameter to the PagingComponent constructor.\n");
        }

        return (BackwardCompatibilityStyleCustomizer) styleCustomizer;
    }

    ////////////////////////////////////////////// INNER CLASS /////////////////////////////////////
    ////////////////////////////////////////////// INNER CLASS /////////////////////////////////////
    ////////////////////////////////////////////// INNER CLASS /////////////////////////////////////
    ////////////////////////////////////////////// INNER CLASS /////////////////////////////////////
    ////////////////////////////////////////////// INNER CLASS /////////////////////////////////////
    ////////////////////////////////////////////// INNER CLASS /////////////////////////////////////
    ////////////////////////////////////////////// INNER CLASS /////////////////////////////////////

    /**
     * Retrieves the list of items which will be displayed and the index range.
     */
    public static class PageRange<I> {
        protected int indexPageStart, indexPageEnd;
        protected List<I> itemsList;

        public PageRange(int currentPage, int itemsPerPage, List<I> itemsList) {
            indexPageEnd = currentPage * itemsPerPage;
            indexPageStart = indexPageEnd - itemsPerPage;
            if (indexPageEnd > itemsList.size()) {
                indexPageEnd = itemsList.size();
            }
            this.itemsList = itemsList.subList(indexPageStart, indexPageEnd);
        }

        /**
         * Gets items which will be displayed (in the current page)
         */
        public List<I> getItemsList() {
            return itemsList;
        }

        public int getIndexPageStart() {
            return indexPageStart;
        }

        public int getIndexPageEnd() {
            return indexPageEnd;
        }
    }

    /**
     * Listens when changing a page. It allow to retrieve the items to display and add these into the layout of your choice.
     */
    public static interface PagingComponentListener<I> extends Serializable {

        /**
         * Allow to retrieve the items to display by the event and add these into the layout of your choice.
         *
         * @param event
         */
        public void displayPage(ChangePageEvent<I> event);
    }

    /**
     * It's fired when changing a page
     */
    public static class ChangePageEvent<I> extends Event {
        protected PageRange<I> pageRange;

        public ChangePageEvent(Component source, PageRange<I> pageRange) {
            super(source);
            this.pageRange = pageRange;
        }

        /**
         * @return a {@link PageRange} which contains the items to display and the index range
         */
        public PageRange<I> getPageRange() {
            return pageRange;
        }
    }
}