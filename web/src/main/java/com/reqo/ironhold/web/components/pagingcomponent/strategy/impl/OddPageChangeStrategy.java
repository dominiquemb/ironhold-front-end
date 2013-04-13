package com.reqo.ironhold.web.components.pagingcomponent.strategy.impl;

import com.reqo.ironhold.web.components.pagingcomponent.ComponentsManager;
import com.reqo.ironhold.web.components.pagingcomponent.strategy.PageChangeStrategy;

/**
 * Strategy for an odd number of buttons page.
 */
public final class OddPageChangeStrategy extends PageChangeStrategy {

    public OddPageChangeStrategy(ComponentsManager manager) {
        super(manager);
    }

    @Override
    protected int calculatePageNumberWhereStartTheIteration(int currentPage, int buttonPageMargin) {
        return currentPage - buttonPageMargin;
    }

}
