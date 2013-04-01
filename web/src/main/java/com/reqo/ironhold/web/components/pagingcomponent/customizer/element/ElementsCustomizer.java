package com.reqo.ironhold.web.components.pagingcomponent.customizer.element;

import com.reqo.ironhold.web.components.pagingcomponent.PagingComponent;
import com.reqo.ironhold.web.components.pagingcomponent.button.ButtonPageNavigator;
import com.reqo.ironhold.web.components.pagingcomponent.customizer.adaptator.GlobalCustomizer;
import com.reqo.ironhold.web.components.pagingcomponent.customizer.style.StyleCustomizer;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;

/**
 * Override this class and give it in parameter to the {@link PagingComponent} to personalize the creation of separators and buttons (first, next, button page, ...).
 * 
 * @see GlobalCustomizer
 * @see StyleCustomizer
 */
public interface ElementsCustomizer {

	/**
	 * Create a button to go to the first page.
	 * 
	 * @return the button created or <code>null</code>
	 */
	public Button createButtonFirst();

	/**
	 * Create a button to go to the last page.
	 * 
	 * @return the button created or <code>null</code>
	 */
	public Button createButtonLast();

	/**
	 * Create a button to go to the previous page.
	 * 
	 * @return the button created or <code>null</code>
	 */
	public Button createButtonPrevious();

	/**
	 * Create a button to go to the next page.
	 * 
	 * @return the button created or <code>null</code>
	 */
	public Button createButtonNext();

	/**
	 * This method is called to create each necessary buttons page to the <code>PagingComponent</code>.
	 * You can also set common styles to the buttons page here but for the styles specific to the state of the button (button with current page or not), 
	 * you have to override {@link StyleCustomizer} or , better, use {@link GlobalCustomizer} to customize both the creation and the style of buttons.
	 * 
	 * @return the button page
	 */
	public ButtonPageNavigator createButtonPage();

	/**
	 * Create an <code>AbstractComponent</code> that allow to separate the button first and previous with the others buttons.
	 * 
	 * @return the <code>AbstractComponent</code> created or <code>null</code>;
	 */
	public Component createFirstSeparator();
	
	/**
	 * Create an <code>AbstractComponent</code> that allow to separate the button next and last with the others buttons.
	 * 
	 * @return the <code>AbstractComponent</code> created or <code>null</code>;
	 */
	public Component createLastSeparator();

}
