package com.reqo.ironhold.web.components;

import com.reqo.ironhold.storage.IMimeMailMessageStorageService;
import com.reqo.ironhold.storage.es.IndexFieldEnum;
import com.reqo.ironhold.web.IronholdApplication;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.elasticsearch.search.SearchHit;

@SuppressWarnings("serial")
public class SourceView extends AbstractEmailView {

    private static Logger logger = Logger.getLogger(SourceView.class);

    private final SourceView me;
    private SearchHitPanel currentHitPanel;
    private final VerticalLayout layout;

    public SourceView() {
        this.setSizeFull();
        this.me = this;
        layout = new VerticalLayout();
        layout.setMargin(true);
        this.setContent(layout);
    }

    public synchronized void show(SearchHitPanel newHitPanel, final SearchHit item, String criteria) throws Exception {
        String client = (String) getSession().getAttribute("client");

        layout.removeAllComponents();

        addEmailToolBar(layout, client, item);

        final Label messageId = new Label("<b>MessageId: " + StringEscapeUtils.escapeHtml4(item.getId()) + "</b>");
        messageId.setContentMode(ContentMode.HTML);
        layout.addComponent(messageId);

        IMimeMailMessageStorageService mimeMailMessageStorageService = ((IronholdApplication) this.getUI()).getMimeMailMessageStorageService();
        final String mailMessage = mimeMailMessageStorageService.get(client, (String) item.getFields().get(IndexFieldEnum.YEAR.getValue()).getValue(), (String) item.getFields().get(IndexFieldEnum.MONTH_DAY.getValue()).getValue(), item.getId());

        final Label body = new Label(StringEscapeUtils.escapeHtml4(mailMessage).replaceAll("\r?\n", "<br/>"));

        body.setContentMode(ContentMode.HTML);
        layout.addComponent(body);

    }


}
