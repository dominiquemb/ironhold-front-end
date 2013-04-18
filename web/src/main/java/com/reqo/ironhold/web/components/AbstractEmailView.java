package com.reqo.ironhold.web.components;

import com.reqo.ironhold.storage.IMimeMailMessageStorageService;
import com.reqo.ironhold.storage.es.IndexFieldEnum;
import com.reqo.ironhold.storage.model.user.LoginUser;
import com.reqo.ironhold.web.IronholdApplication;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import org.elasticsearch.search.SearchHit;

/**
 * User: ilya
 * Date: 4/16/13
 * Time: 8:56 AM
 */
public abstract class AbstractEmailView extends Panel {

    protected void addEmailToolBar(VerticalLayout layout, String client, SearchHit item) throws Exception {
        IMimeMailMessageStorageService mimeMailMessageStorageService = ((IronholdApplication) this.getUI()).getMimeMailMessageStorageService();
        final LoginUser loginUser = (LoginUser) getSession().getAttribute("loginUser");

        final String mailMessage = mimeMailMessageStorageService.get(client, (String) item.getFields().get(IndexFieldEnum.YEAR.getValue()).getValue(), (String) item.getFields().get(IndexFieldEnum.MONTH_DAY.getValue()).getValue(), item.getId());
        layout.addComponent(new EmailToolBar(loginUser, item.getId(), mailMessage));
    }

}
