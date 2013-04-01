package com.reqo.ironhold.web.components.pagingcomponent.utilities;

import com.google.gwt.thirdparty.guava.common.collect.Iterators;
import com.reqo.ironhold.web.components.pagingcomponent.PagingComponent;
import com.reqo.ironhold.web.components.pagingcomponent.PagingComponent.PagingComponentListener;

import java.io.Serializable;
import java.util.AbstractList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * This list is always empty and it is impossible to add objects.
 * Its only purpose is to allow the lazy loading for the {@link PagingComponent}.
 */
public class FakeList<E> extends AbstractList<E> implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5885776796967911119L;
	
	protected int size;
	
	/**
	 * Create a list without having to load the items, making it possible the lazy loading.<br>
	 * You can load these items when displaying by overriding the {@link PagingComponentListener#displayPage(com.reqo.ironhold.web.components.pagingcomponent.PagingComponent.ChangePageEvent)} method.
	 * 
	 * @param size the number total of items to paginate. Typically, this value will be the result of your SQL query count
	 */
	public FakeList(int size) {
		this.size = size;
	}
	
	/**
	 * return always true
	 */
	@Override
	public boolean isEmpty() {
		return true;
	}
	
	/**
	 * return always an empty iterator
	 */
	@Override
	public Iterator<E> iterator() {
		return Iterators.emptyIterator();
	}
	
	/**
	 * return always a empty list
	 */
	@Override
	public List<E> subList(int fromIndex, int toIndex) {
		return Collections.emptyList();
	}

	/**
	 * return throw an {@link IndexOutOfBoundsException};
	 */
	@Override
	public E get(int index) {
		throw new IndexOutOfBoundsException("It is a FakeList to do lazy loading for the PagingComponent and it never contains elements");
	}

	@Override
	public int size() {
		return size;
	}

}
