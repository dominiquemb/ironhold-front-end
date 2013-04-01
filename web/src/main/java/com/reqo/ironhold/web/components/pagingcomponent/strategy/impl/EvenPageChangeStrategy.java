package com.reqo.ironhold.web.components.pagingcomponent.strategy.impl;

import com.reqo.ironhold.web.components.pagingcomponent.ComponentsManager;
import com.reqo.ironhold.web.components.pagingcomponent.strategy.PageChangeStrategy;

/**
 * Strategy for an even number of buttons page.
 */
public final class EvenPageChangeStrategy extends PageChangeStrategy {
	
	public EvenPageChangeStrategy(ComponentsManager manager) {
		super(manager);
	}

	@Override
	protected int calculatePageNumberWhereStartTheIteration(int currentPage, int buttonPageMargin) {
		return currentPage > manager.getPreviousPage() ? currentPage - buttonPageMargin + 1: currentPage - buttonPageMargin;
	}

}
