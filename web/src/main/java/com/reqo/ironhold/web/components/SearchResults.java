package com.reqo.ironhold.web.components;

import com.reqo.ironhold.storage.MessageIndexService;
import com.reqo.ironhold.storage.es.IndexFieldEnum;
import com.reqo.ironhold.storage.es.MessageSearchBuilder;
import com.reqo.ironhold.storage.model.user.LoginUser;
import com.reqo.ironhold.web.IronholdApplication;
import com.reqo.ironhold.web.components.pagingcomponent.PagingComponent;
import com.reqo.ironhold.web.components.pagingcomponent.listener.impl.LazyPagingComponentListener;
import com.reqo.ironhold.web.components.pagingcomponent.utilities.FakeList;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.Reindeer;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.facet.terms.TermsFacet;
import org.elasticsearch.search.sort.SortOrder;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@SuppressWarnings({"serial", "unchecked"})
public class SearchResults extends HorizontalLayout {

    protected static final int MAX_RESULTS_TO_BE_FACETED = 30000;

    private final EmailPreviewPanel emailPreview;

    private String criteria;
    private VerticalLayout leftPane;
    private VerticalLayout rightPane;
    private VerticalLayout messageList;
    private NativeSelect sortFieldSelector;
    private NativeSelect sortOrderSelector;
    private PagingComponent<SearchHit> pager;
    private VerticalLayout middlePane;
    private FacetPanel yearFacetPanel;
    private FacetPanel fromFacetPanel;
    private FacetPanel fromDomainFacetPanel;
    private FacetPanel toFacetPanel;
    private FacetPanel toDomainFacetPanel;
    private FacetPanel fileExtFacetPanel;
    private SearchResults me;
    private MessageSearchBuilder builder;
    private boolean facetsSetup;
    private Label resultLabel;
    private String indexPrefix;

    public SearchResults(EmailPreviewPanel emailPreview) {
        this.emailPreview = emailPreview;
        this.setSpacing(true);
        this.setMargin(true);
        this.setSizeFull();
    }

    public void setCriteria(String criteria) throws Exception {

        MessageIndexService messageIndexService = ((IronholdApplication) this.getUI()).getMessageIndexService();
        LoginUser authenticatedUser = (LoginUser) this.getUI().getSession().getAttribute("loginUser");
        this.builder = messageIndexService.getNewBuilder(indexPrefix, authenticatedUser);

        long started = System.currentTimeMillis();
        this.criteria = criteria;
        this.facetsSetup = false;
        builder = messageIndexService.getNewBuilder(indexPrefix, authenticatedUser).withCriteria(criteria);
        reset(authenticatedUser);

        sortFieldSelector.setVisible(true);
        sortOrderSelector.setVisible(true);
        fromFacetPanel.setVisible(true);
        fromDomainFacetPanel.setVisible(true);
        toFacetPanel.setVisible(true);
        toDomainFacetPanel.setVisible(true);
        yearFacetPanel.setVisible(true);
        fileExtFacetPanel.setVisible(true);

        performSearch(messageIndexService, authenticatedUser);
        long finished = System.currentTimeMillis();
        resultLabel.setDescription(String.format(
                "Server rendering took %,d ms", (finished - started)));
    }

    private void performSearch(final MessageIndexService messageIndexService, final LoginUser authenticatedUser) {
        final SearchResponse response = messageIndexService.getMatchCount(builder, authenticatedUser);
        if (response == null) {
            resultLabel.setCaption("Invalid search query");
            return;
        }

        if (!facetsSetup) {
            resultLabel.setCaption(String.format("%,d matches.", response
                    .getHits().totalHits()));
        } else {
            resultLabel.setCaption(String.format("%,d filtered matches. ",
                    response.getHits().totalHits()));
        }

        final String originalResultLabelValue = resultLabel.getCaption();

        final List<SearchHit> results = new FakeList<SearchHit>((int) response
                .getHits().totalHits());

        messageList.removeAllComponents();

        if (pager != null) {
            middlePane.removeComponent(pager);
        }

        pager = new PagingComponent<SearchHit>(10, 10, results,
                new LazyPagingComponentListener<SearchHit>(messageList) {

                    @Override
                    protected Collection<SearchHit> getItemsList(
                            int startIndex, int endIndex) throws Exception {
                        builder = messageIndexService.getNewBuilder(indexPrefix, builder, authenticatedUser);

                        if (!facetsSetup) {
                            if (results.size() < MAX_RESULTS_TO_BE_FACETED) {
                                builder.withDateFacet().withFromFacet()
                                        .withFromDomainFacet()
                                        .withToFacet().withToDomainFacet().withFileExtFacet(); // .withToFacet()
                            }
                        }

                        builder.withResultsLimit(startIndex, endIndex
                                - startIndex);

                        SortOrder order = SortOrder.DESC;
                        if ("Ascending".equals((String) sortOrderSelector
                                .getValue())) {
                            order = SortOrder.ASC;
                        }

                        switch ((String) sortFieldSelector.getValue()) {
                            case "Date":
                                builder.withSort(IndexFieldEnum.DATE, order);
                                break;

                            case "Size":
                                builder.withSort(IndexFieldEnum.SIZE, order);
                                break;

                            case "Relevance":
                                builder.withSort(IndexFieldEnum.SCORE, order);
                                break;

                        }

                        SearchResponse response = messageIndexService.search(builder, authenticatedUser);

                        setUpFacets(response, messageIndexService, authenticatedUser);

                        resultLabel.setCaption(String.format(
                                "%s, Search took %,d ms",
                                originalResultLabelValue,
                                response.getTookInMillis()));
                        return Arrays.asList(response.getHits().getHits());
                    }

                    @Override
                    protected Component displayItem(int index, SearchHit item) throws Exception {
                        return new SearchHitPanel(item, emailPreview, criteria, ((IronholdApplication) getUI()));
                    }

                });

        middlePane.addComponent(pager, 1);
    }

    private void setUpFacets(SearchResponse response, final MessageIndexService messageIndexService, final LoginUser authenticatedUser) {

        if (!facetsSetup) {
            if (builder.isDateFacet()) {
                yearFacetPanel.setVisible(true);
                TermsFacet dateFacet = response.getFacets().facet(
                        MessageSearchBuilder.FACET_YEAR);
                yearFacetPanel.removeAllComponents();
                List<TermsFacet.Entry> years = (List<TermsFacet.Entry>) dateFacet.getEntries();

                for (final TermsFacet.Entry entry : years.subList(0,
                        Math.min(years.size(), 10))) {
                    final HorizontalLayout hl = new HorizontalLayout();
                    hl.setWidth("100%");

                    CheckBox checkBox = new CheckBox(entry.getTerm());
                    checkBox.setImmediate(true);
                    checkBox.addValueChangeListener(new ValueChangeListener() {
                        @Override
                        public void valueChange(ValueChangeEvent event) {
                            boolean enabled = (Boolean) event.getProperty().getValue();
                            try {
                                builder = messageIndexService.getNewBuilder(indexPrefix, builder, authenticatedUser);

                                if (enabled) {
                                    builder.withYearFacetValue(entry.getTerm());
                                } else {
                                    builder.withoutYearFacetValue(entry.getTerm());
                                }
                                performSearch(messageIndexService, authenticatedUser);
                            } catch (Exception e) {
                                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                            }

                        }
                    });

                    hl.addComponent(checkBox);

                    Label countLabel = new Label(String.format("%,d",
                            entry.getCount()));
                    hl.addComponent(countLabel);
                    hl.setComponentAlignment(countLabel, Alignment.MIDDLE_RIGHT);

                    checkBox.setWidth(null);
                    countLabel.setWidth(null);
                    hl.setExpandRatio(checkBox, 1.0f);

                    yearFacetPanel.addComponent(hl);

                }
            } else {
                yearFacetPanel.setVisible(false);
            }

            if (builder.isFileExtFacet()) {
                fileExtFacetPanel.setVisible(true);
                TermsFacet fileExtFacet = response.getFacets().facet(
                        MessageSearchBuilder.FACET_FILEEXT);
                fileExtFacetPanel.removeAllComponents();
                int visibleFacets = 0;
                for (final TermsFacet.Entry entry : fileExtFacet.getEntries()) {
                    if (entry.getTerm().trim().length() != 0) {
                        final HorizontalLayout hl = new HorizontalLayout();
                        hl.setWidth("100%");

                        CheckBox checkBox = new CheckBox(StringUtils.abbreviate(
                                entry.getTerm(),
                                20 - Integer.toString(entry.getCount()).length()));
                        checkBox.setImmediate(true);
                        checkBox.addValueChangeListener(new ValueChangeListener() {
                            @Override
                            public void valueChange(ValueChangeEvent event) {
                                boolean enabled = (Boolean) event.getProperty().getValue();
                                try {
                                    builder = messageIndexService.getNewBuilder(indexPrefix, builder, authenticatedUser);

                                    if (enabled) {
                                        builder.withFileExtFacetValue(entry.getTerm());
                                    } else {
                                        builder.withoutFileExtFacetValue(entry
                                                .getTerm());
                                    }
                                    performSearch(messageIndexService, authenticatedUser);
                                } catch (Exception e) {
                                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                                }

                            }
                        });

                        hl.addComponent(checkBox);

                        Label countLabel = new Label(String.format("%,d",
                                entry.getCount()));
                        hl.addComponent(countLabel);
                        hl.setComponentAlignment(countLabel, Alignment.MIDDLE_RIGHT);

                        checkBox.setWidth(null);
                        countLabel.setWidth(null);
                        hl.setExpandRatio(checkBox, 1.0f);

                        fileExtFacetPanel.addComponent(hl);
                        visibleFacets++;
                    }
                }

                if (visibleFacets == 0) {
                    fileExtFacetPanel.setVisible(false);
                }
            } else {
                fileExtFacetPanel.setVisible(false);
            }

            if (builder.isFromFacet()) {
                fromFacetPanel.setVisible(true);
                TermsFacet fromFacet = response.getFacets().facet(
                        MessageSearchBuilder.FACET_FROM_NAME);
                fromFacetPanel.removeAllComponents();
                for (final TermsFacet.Entry entry : fromFacet.getEntries()) {
                    final HorizontalLayout hl = new HorizontalLayout();
                    hl.setWidth("100%");

                    CheckBox checkBox = new CheckBox(StringUtils.abbreviate(
                            entry.getTerm(),
                            20 - Integer.toString(entry.getCount()).length()));
                    checkBox.setImmediate(true);
                    checkBox.addValueChangeListener(new ValueChangeListener() {
                        @Override
                        public void valueChange(ValueChangeEvent event) {
                            boolean enabled = (Boolean) event.getProperty().getValue();
                            try {
                                builder = messageIndexService.getNewBuilder(indexPrefix, builder, authenticatedUser);

                                if (enabled) {
                                    builder.withFromFacetValue(entry.getTerm());
                                } else {
                                    builder.withoutFromFacetValue(entry.getTerm());
                                }
                                performSearch(messageIndexService, authenticatedUser);
                            } catch (Exception e) {
                                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                            }

                        }
                    });

                    hl.addComponent(checkBox);

                    Label countLabel = new Label(String.format("%,d",
                            entry.getCount()));
                    hl.addComponent(countLabel);
                    hl.setComponentAlignment(countLabel, Alignment.MIDDLE_RIGHT);

                    checkBox.setWidth(null);
                    countLabel.setWidth(null);
                    hl.setExpandRatio(checkBox, 1.0f);

                    fromFacetPanel.addComponent(hl);
                }
            } else {
                fromFacetPanel.setVisible(false);
            }

            if (builder.isFromDomainFacet()) {
                fromDomainFacetPanel.setVisible(true);
                TermsFacet fromDomainFacet = response.getFacets().facet(
                        MessageSearchBuilder.FACET_FROM_DOMAIN);
                fromDomainFacetPanel.removeAllComponents();
                for (final TermsFacet.Entry entry : fromDomainFacet
                        .getEntries()) {
                    if (entry.getTerm().contains(".")) {
                        final HorizontalLayout hl = new HorizontalLayout();
                        hl.setWidth("100%");

                        CheckBox checkBox = new CheckBox(
                                StringUtils.abbreviate(entry.getTerm(),
                                        20 - Integer.toString(entry.getCount())
                                                .length()));
                        checkBox.setImmediate(true);
                        checkBox.addValueChangeListener(new ValueChangeListener() {
                            @Override
                            public void valueChange(ValueChangeEvent event) {
                                boolean enabled = (Boolean) event.getProperty().getValue();
                                try {
                                    builder = messageIndexService.getNewBuilder(indexPrefix, builder, authenticatedUser);

                                    if (enabled) {
                                        builder.withFromDomainFacetValue(entry
                                                .getTerm());
                                    } else {
                                        builder.withoutFromDomainFacetValue(entry
                                                .getTerm());
                                    }
                                    performSearch(messageIndexService, authenticatedUser);
                                } catch (Exception e) {
                                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                                }

                            }
                        });
                        hl.addComponent(checkBox);

                        Label countLabel = new Label(String.format("%,d",
                                entry.getCount()));
                        hl.addComponent(countLabel);
                        hl.setComponentAlignment(countLabel,
                                Alignment.MIDDLE_RIGHT);

                        checkBox.setWidth(null);
                        countLabel.setWidth(null);
                        hl.setExpandRatio(checkBox, 1.0f);

                        fromDomainFacetPanel.addComponent(hl);
                    }
                }
            } else {
                fromDomainFacetPanel.setVisible(false);
            }

            if (builder.isToFacet()) {
                toFacetPanel.setVisible(true);
                TermsFacet toFacet = response.getFacets().facet(
                        MessageSearchBuilder.FACET_TO_NAME);
                toFacetPanel.removeAllComponents();
                for (final TermsFacet.Entry entry : toFacet.getEntries()) {
                    final HorizontalLayout hl = new HorizontalLayout();
                    hl.setWidth("100%");

                    CheckBox checkBox = new CheckBox(StringUtils.abbreviate(
                            entry.getTerm(),
                            20 - Integer.toString(entry.getCount()).length()));
                    checkBox.setImmediate(true);
                    checkBox.addValueChangeListener(new ValueChangeListener() {
                        @Override
                        public void valueChange(ValueChangeEvent event) {
                            boolean enabled = (Boolean) event.getProperty().getValue();
                            try {
                                builder = messageIndexService.getNewBuilder(indexPrefix, builder, authenticatedUser);

                                if (enabled) {
                                    builder.withToFacetValue(entry.getTerm());
                                } else {
                                    builder.withoutToFacetValue(entry.getTerm());
                                }
                                performSearch(messageIndexService, authenticatedUser);
                            } catch (Exception e) {
                                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                            }

                        }
                    });
                    hl.addComponent(checkBox);

                    Label countLabel = new Label(String.format("%,d",
                            entry.getCount()));
                    hl.addComponent(countLabel);
                    hl.setComponentAlignment(countLabel, Alignment.MIDDLE_RIGHT);

                    checkBox.setWidth(null);
                    countLabel.setWidth(null);
                    hl.setExpandRatio(checkBox, 1.0f);

                    toFacetPanel.addComponent(hl);
                }
            } else {
                toFacetPanel.setVisible(false);
            }

            if (builder.isToDomainFacet()) {
                toDomainFacetPanel.setVisible(true);
                TermsFacet toDomainFacet = response.getFacets().facet(
                        MessageSearchBuilder.FACET_TO_DOMAIN);
                toDomainFacetPanel.removeAllComponents();
                for (final TermsFacet.Entry entry : toDomainFacet.getEntries()) {
                    if (entry.getTerm().contains(".")) {
                        final HorizontalLayout hl = new HorizontalLayout();
                        hl.setWidth("100%");

                        CheckBox checkBox = new CheckBox(
                                StringUtils.abbreviate(entry.getTerm(),
                                        20 - Integer.toString(entry.getCount())
                                                .length()));
                        checkBox.setImmediate(true);
                        checkBox.addValueChangeListener(new ValueChangeListener() {
                            @Override
                            public void valueChange(ValueChangeEvent event) {
                                boolean enabled = (Boolean) event.getProperty().getValue();
                                try {
                                    builder = messageIndexService.getNewBuilder(indexPrefix, builder, authenticatedUser);

                                    if (enabled) {
                                        builder.withToDomainFacetValue(entry
                                                .getTerm());
                                    } else {
                                        builder.withoutToDomainFacetValue(entry
                                                .getTerm());
                                    }
                                    performSearch(messageIndexService, authenticatedUser);
                                } catch (Exception e) {
                                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                                }

                            }
                        });
                        hl.addComponent(checkBox);

                        Label countLabel = new Label(String.format("%,d",
                                entry.getCount()));
                        hl.addComponent(countLabel);
                        hl.setComponentAlignment(countLabel,
                                Alignment.MIDDLE_RIGHT);

                        checkBox.setWidth(null);
                        countLabel.setWidth(null);
                        hl.setExpandRatio(checkBox, 1.0f);

                        toDomainFacetPanel.addComponent(hl);
                    }
                }
            } else {
                toDomainFacetPanel.setVisible(false);
            }
        }
        facetsSetup = true;
    }


    public void reset(final LoginUser authenticatedUser) {
        final MessageIndexService messageIndexService = ((IronholdApplication) this.getUI()).getMessageIndexService();

        this.removeAllComponents();

        leftPane = new VerticalLayout();
        leftPane.setSizeFull();
        middlePane = new VerticalLayout();
        middlePane.setSizeFull();
        middlePane.setSpacing(true);
        rightPane = new VerticalLayout();
        rightPane.setSizeFull();
        messageList = new VerticalLayout();
        messageList.setSpacing(true);

        resultLabel = new Label();
        resultLabel.setStyleName(Reindeer.LABEL_SMALL);

        emailPreview.setIndexPrefix(indexPrefix);
        rightPane.addComponent(emailPreview);

        sortFieldSelector = new NativeSelect("Sort By");
        sortFieldSelector.addItem("Relevance");
        sortFieldSelector.addItem("Date");
        sortFieldSelector.addItem("Size");
        sortFieldSelector.setValue("Relevance");
        sortFieldSelector.setNullSelectionAllowed(false);
        sortFieldSelector.setImmediate(true);
        sortFieldSelector.addValueChangeListener(new ValueChangeListener() {

            @Override
            public void valueChange(ValueChangeEvent event) {
                try {
                    builder = messageIndexService.getNewBuilder(indexPrefix, builder, authenticatedUser);
                    performSearch(messageIndexService, authenticatedUser);
                } catch (Exception e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        });

        sortOrderSelector = new NativeSelect("Order");
        sortOrderSelector.addItem("Ascending");
        sortOrderSelector.addItem("Descending");
        sortOrderSelector.setValue("Descending");
        sortOrderSelector.setNullSelectionAllowed(false);
        sortOrderSelector.setImmediate(true);
        sortOrderSelector.addValueChangeListener(new ValueChangeListener() {

            @Override
            public void valueChange(ValueChangeEvent event) {
                try {
                    builder = messageIndexService.getNewBuilder(indexPrefix, builder, authenticatedUser);
                    performSearch(messageIndexService, authenticatedUser);
                } catch (Exception e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        });

        HorizontalLayout sortOptionsBar = new HorizontalLayout();
        sortOptionsBar.setSpacing(true);
        sortOptionsBar.addComponent(sortOrderSelector);
        sortOptionsBar.addComponent(sortFieldSelector);

        fromFacetPanel = new FacetPanel("From by name:");
        fromFacetPanel.setWidth("200px");

        fromDomainFacetPanel = new FacetPanel("From by domain:");
        fromDomainFacetPanel.setWidth("200px");

        toFacetPanel = new FacetPanel("To by name:");
        toFacetPanel.setWidth("200px");

        toDomainFacetPanel = new FacetPanel("To by domain:");
        toDomainFacetPanel.setWidth("200px");

        yearFacetPanel = new FacetPanel("Year:");
        yearFacetPanel.setWidth("200px");

        fileExtFacetPanel = new FacetPanel("Attachment file type:");
        fileExtFacetPanel.setWidth("200px");

        leftPane.setSpacing(true);
        leftPane.addComponent(sortOptionsBar);
        leftPane.addComponent(fromFacetPanel);
        leftPane.addComponent(fromDomainFacetPanel);
        leftPane.addComponent(toFacetPanel);
        leftPane.addComponent(toDomainFacetPanel);
        leftPane.addComponent(yearFacetPanel);
        leftPane.addComponent(fileExtFacetPanel);

        leftPane.setExpandRatio(fromFacetPanel, 1);
        leftPane.setExpandRatio(fromDomainFacetPanel, 1);
        leftPane.setExpandRatio(toFacetPanel, 1);
        leftPane.setExpandRatio(toDomainFacetPanel, 1);
        leftPane.setExpandRatio(yearFacetPanel, 1);
        leftPane.setExpandRatio(fileExtFacetPanel, 1);

        leftPane.setWidth("200px");
        middlePane.addComponent(resultLabel);
        middlePane.addComponent(messageList);

        sortFieldSelector.setVisible(false);
        sortOrderSelector.setVisible(false);
        yearFacetPanel.setVisible(false);
        fromFacetPanel.setVisible(false);
        fromDomainFacetPanel.setVisible(false);
        toFacetPanel.setVisible(false);
        toDomainFacetPanel.setVisible(false);
        fileExtFacetPanel.setVisible(false);

        this.addComponent(leftPane);
        this.addComponent(middlePane);
        this.addComponent(rightPane);
        this.setExpandRatio(middlePane, 1);
        this.setExpandRatio(rightPane, 1);

    }

    public void setIndexPrefix(String indexPrefix) {
        this.indexPrefix = indexPrefix;
    }

    public String getIndexPrefix() {
        return indexPrefix;
    }
}
