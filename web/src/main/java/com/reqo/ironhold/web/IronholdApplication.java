/*
 * Copyright 2009 IT Mill Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.reqo.ironhold.web;

import com.reqo.ironhold.search.IndexService;
import com.reqo.ironhold.web.components.SearchWindow;
import com.vaadin.Application;
import com.vaadin.ui.Window;
import org.apache.log4j.Logger;

/**
 * The Application's "main" class
 */
@SuppressWarnings("serial")
public class IronholdApplication extends Application {
    private Window window;
    private static Logger logger = Logger.getLogger(IronholdApplication.class);
    private static IndexService indexService;

    static {
        try {
            indexService = new IndexService();
        } catch (Exception e) {
            logger.error("Failed to create index node", e);
            throw new RuntimeException(e);
        }

    }

    private Object lock = new Object();

    @Override
    public void init() {

        try {
            setTheme("ironhold");

            window = new SearchWindow("IronHold Search");

            setMainWindow(window);
        } catch (Exception e) {
            logger.warn(e);
        }
    }

    public IndexService getIndexService() {
        return indexService;
    }
}
