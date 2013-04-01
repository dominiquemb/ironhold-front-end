package com.reqo.ironhold.web.components.pagingcomponent.button;


import com.reqo.ironhold.web.components.pagingcomponent.ComponentsManager;
import com.vaadin.ui.Button;

/**
 * Button used for buttons of pages. These one allow to go to the different pages.
 */
public class ButtonPageNavigator extends Button {

    /**
     *
     */
    private static final long serialVersionUID = 5409886013515830245L;
    private int page;
    private ComponentsManager manager;

    public ButtonPageNavigator() {
        setImmediate(true);
    }

    /**
     * @param page
     */
    public ButtonPageNavigator(int page) {
        setImmediate(true);
        setPage(page);
        CheckActualPageAndSetStyle();
    }

    public ButtonPageNavigator(int page, ClickListener listener) {
        this(page);
        addClickListener(listener);
    }

    @Deprecated
    public void setCaptionCkeckActualPage(int page) {
        setPage(page);
        CheckActualPageAndSetStyle();
    }

    @Deprecated
    public void CheckActualPageAndSetStyle() {
        manager.getPageChangeStrategy().chooseStyle(manager.getButtonsStyleCustomizer(), this, manager.getCurrentPage(), page);
    }

    @Deprecated
    public void setCaptionCurrent() {
        manager.getButtonsStyleCustomizer().styleButtonPageCurrentPage(this, page);
    }

    @Deprecated
    public void setCaptionNormal() {
        manager.getButtonsStyleCustomizer().styleButtonPageNormal(this, page);
    }

    /**
     * Sets the caption.
     * Don't use this method directly, use {@link #setPage(int)} or {@link #setPage(int, String)} to set the caption and the page number.
     */
    @Override
    public void setCaption(String caption) {
        super.setCaption(caption);
    }

    /**
     * Set the page number and set the caption with the page number value.
     *
     * @param page the page number
     */
    public void setPage(int page) {
        this.page = page;
        setCaption(Integer.toString(page));
    }

    /**
     * @param page    the page number
     * @param caption the caption
     */
    public void setPage(int page, String caption) {
        this.page = page;
        setCaption(caption);
    }

    public int getPage() {
        return page;
    }

    public ComponentsManager getManager() {
        return manager;
    }

    public void setManager(ComponentsManager manager) {
        this.manager = manager;
    }
}
