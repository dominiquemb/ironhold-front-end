package com.reqo.ironhold.web.components;

import com.reqo.ironhold.storage.IMimeMailMessageStorageService;
import com.reqo.ironhold.web.IronholdApplication;
import com.reqo.ironhold.web.domain.IndexedMailMessage;
import com.reqo.ironhold.web.domain.LoginUser;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

/**
 * User: ilya
 * Date: 4/16/13
 * Time: 8:56 AM
 */
public abstract class AbstractEmailView extends Panel {
    public AbstractEmailView() {
    }

    protected void addEmailToolBar(VerticalLayout layout, String client, IndexedMailMessage item) throws Exception {
        IMimeMailMessageStorageService mimeMailMessageStorageService = ((IronholdApplication) this.getUI()).getMimeMailMessageStorageService();
        final LoginUser loginUser = (LoginUser) getSession().getAttribute("loginUser");

        final String mailMessage = mimeMailMessageStorageService.get(client, item.getYear(), item.getMonthDay(), item.getMessageId());
        layout.addComponent(new EmailToolBar(loginUser, item.getMessageId(), mailMessage));
    }

}
