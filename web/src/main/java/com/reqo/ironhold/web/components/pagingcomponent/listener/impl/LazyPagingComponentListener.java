package com.reqo.ironhold.web.components.pagingcomponent.listener.impl;

import com.reqo.ironhold.web.components.pagingcomponent.PagingComponent.ChangePageEvent;
import com.reqo.ironhold.web.components.pagingcomponent.PagingComponent.PageRange;
import com.reqo.ironhold.web.components.pagingcomponent.PagingComponent.PagingComponentListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;

import java.util.Collection;

/**
 * Allow to do lazy loading and display the items more easily than the {@link PagingComponentListener} at each page change.
 */
public abstract class LazyPagingComponentListener<I> implements PagingComponentListener<I> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -508849271522788638L;

	private ComponentContainer componentContainer;

	/**
	 * Allow to do lazy loading and display the items more easily than the {@link PagingComponentListener} at each page change.<br><br>
	 * You have to override the methods:
	 * <ul>
	 * 	<li>{@link #getItemsList(int, int)} to fetch the list of items to display.</li>
	 * 	<li>{@link #displayItem(int, Object)} to display an item in the componentContainer provided.</li>
	 * </ul>
	 * 
	 * @param componentContainer all items to display will be added in this layout
	 */
	public LazyPagingComponentListener(ComponentContainer componentContainer) {
		this.componentContainer = componentContainer;
	}

	@Override
	public void displayPage(ChangePageEvent<I> event) {
		componentContainer.removeAllComponents();

		PageRange<I> pageRange = event.getPageRange();
		int index = pageRange.getIndexPageStart();
		for (I item : getItemsList(pageRange.getIndexPageStart(), pageRange.getIndexPageEnd())) {
			componentContainer.addComponent(displayItem(++index, item));
		}
	}

	/**
	 * This method is called to fetch an sublist of items to display thanks to a range.
	 * The smallest number that can have an index in a range is zero.
	 * 
	 * @param startIndex the index number of the first item to display (this number is inclusive)
	 * @param endIndex the index number of the last item to display (this number is exclusive)
	 * @return a list of items to display.
	 */
	protected abstract Collection<I> getItemsList(int startIndex, int endIndex);

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
