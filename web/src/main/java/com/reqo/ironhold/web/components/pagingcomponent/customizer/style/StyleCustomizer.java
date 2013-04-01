package com.reqo.ironhold.web.components.pagingcomponent.customizer.style;

import com.reqo.ironhold.web.components.pagingcomponent.ComponentsManager;
import com.reqo.ironhold.web.components.pagingcomponent.PagingComponent;
import com.reqo.ironhold.web.components.pagingcomponent.builder.ElementsBuilder;
import com.reqo.ironhold.web.components.pagingcomponent.button.ButtonPageNavigator;
import com.reqo.ironhold.web.components.pagingcomponent.customizer.adaptator.GlobalCustomizer;
import com.reqo.ironhold.web.components.pagingcomponent.customizer.element.ElementsCustomizer;

/**
 * Override this class and give it in parameter to the {@link PagingComponent} to personalize the style and to set the caption with the page number to the buttons page.
 * You can also set style of the others buttons (first, previous, next, last) and separators. For example, you can disable/hide these elements when the first or last page is selected.
 *
 * @see GlobalCustomizer
 * @see ElementsCustomizer
 */
public interface StyleCustomizer {

    /**
     * Allow to set style and caption of button page given in parameter.
     * The parameter <code>pageNumber</code> is not the current page number thus the button should not be highlighted.
     * To set the caption use {@link ButtonPageNavigator#setPage(int)} or {@link ButtonPageNavigator#setPage(int, String)}.
     *
     * @param button     the button page to modify
     * @param pageNumber the page number to set to the button
     */
    public void styleButtonPageNormal(ButtonPageNavigator button, int pageNumber);

    /**
     * Allow to set style and caption of button page given in parameter.
     * The parameter <code>pageNumber</code> is the current page number thus the button should be highlighted.
     * To set the caption use {@link ButtonPageNavigator#setPage(int)} or {@link ButtonPageNavigator#setPage(int, String)}.<br><br>
     * <p/>
     * WARNING: don't forget to call the <code>focus()</code> method on the button.
     *
     * @param button     the button page to modify
     * @param pageNumber the page number to set to the button
     */
    public void styleButtonPageCurrentPage(ButtonPageNavigator button, int pageNumber);

    /**
     * Set style of the two separators and the buttons others than the buttons page.
     * You can access to these elements with help of {@link ElementsBuilder} given in parameter.
     * For example, if you want to disable/hide/... the button first, previous and the first separator when the first page is selected, you can override this method and set the styles for each of these elements.
     * Call the {@link ComponentsManager#isFirstPage()} method to check if the first page is selected or {@link ComponentsManager#isLastPage()} for the last page.
     *
     * @param manager
     */
    public void styleTheOthersElements(ComponentsManager manager, ElementsBuilder builder);

}
