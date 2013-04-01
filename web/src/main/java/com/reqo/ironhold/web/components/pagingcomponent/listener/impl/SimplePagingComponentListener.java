package com.reqo.ironhold.web.components.pagingcomponent.listener.impl;

import com.reqo.ironhold.web.components.pagingcomponent.PagingComponent.ChangePageEvent;
import com.reqo.ironhold.web.components.pagingcomponent.PagingComponent.PagingComponentListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;

/**
 * Allow to display the items more easily than the {@link PagingComponentListener} at each page change.
 */ 
public abstract class SimplePagingComponentListener<I> implements PagingComponentListener<I> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5375546221107716565L;

	private ComponentContainer componentContainer;
	
	
	/**
	 * Allow to display the items more easily than the {@link PagingComponentListener} at each page change.
	 * You only have to override the method {@link #displayItem(int, Object)} and the job is done.
	 * 
	 * @param componentContainer all items to display will be added in this layout
	 */
	public SimplePagingComponentListener(ComponentContainer componentContainer) {
		this.componentContainer = componentContainer;
	}
	
	@Override
	public void displayPage(ChangePageEvent<I> event) {
		componentContainer.removeAllComponents();
		
		int index = event.getPageRange().getIndexPageStart();
		for (I item : event.getPageRange().getItemsList()) {
			componentContainer.addComponent(displayItem(++index, item));
		}
	}
	
	/**
	 * This method is called each time that an item must be displayed.
	 * The smallest number that can have an index is <code>1</code>.
	 * 
	 * @param index the index number of the item
	 * @param item the item to display
	 * @return a {@link Component} to display the item in the layout.
	 */
	protected abstract Component displayItem(int index, I item);
	
}
