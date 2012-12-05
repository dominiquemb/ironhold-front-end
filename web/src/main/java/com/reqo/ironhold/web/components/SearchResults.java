package com.reqo.ironhold.web.components;

import com.reqo.ironhold.search.IndexFieldEnum;
import com.reqo.ironhold.search.IndexService;
import com.reqo.ironhold.search.MessageSearchBuilder;
import com.reqo.ironhold.storage.IStorageService;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.Reindeer;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.facet.datehistogram.DateHistogramFacet;
import org.elasticsearch.search.facet.terms.TermsFacet;
import org.elasticsearch.search.sort.SortOrder;
import org.vaadin.pagingcomponent.PagingComponent;
import org.vaadin.pagingcomponent.listener.impl.LazyPagingComponentListener;
import org.vaadin.pagingcomponent.utilities.FakeList;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@SuppressWarnings("serial")
public class SearchResults extends HorizontalLayout {

    private final IndexService indexService;
    private final IStorageService storageService;
    private final SimpleDateFormat yearFormat = new SimpleDateFormat("YYYY");
    private String criteria;
    private VerticalLayout leftPane;
    private VerticalLayout rightPane;
    private VerticalLayout messageList;
    private NativeSelect sortFieldSelector;
    private NativeSelect sortOrderSelector;
    private EmailPreviewPanel emailPreview;
    private PagingComponent<SearchHit> pager;
    private VerticalLayout middlePane;
    private Panel yearFacetPanel;
    private Panel fromFacetPanel;
    private Panel fromDomainFacetPanel;
    private Panel toFacetPanel;
    private Panel toDomainFacetPanel;
    private Panel fileExtFacetPanel;
    private SearchResults me;
    private MessageSearchBuilder builder;
    private boolean facetsSetup;
    private Label resultLabel;

    public SearchResults(IndexService indexService, IStorageService storageService) {
        this.indexService = indexService;
        this.storageService = storageService;
        this.me = this;
        this.setSpacing(true);
        this.setMargin(true);
        this.setSizeFull();


    }

    public void setCriteria(String criteria) {
        this.criteria = criteria;
        this.facetsSetup = false;
        builder = indexService.getNewBuilder().withCriteria(criteria);
        reset();

        sortFieldSelector.setVisible(true);
        sortOrderSelector.setVisible(true);
        fromFacetPanel.setVisible(true);
        fromDomainFacetPanel.setVisible(true);
        toFacetPanel.setVisible(true);
        toDomainFacetPanel.setVisible(true);
        yearFacetPanel.setVisible(true);
        fileExtFacetPanel.setVisible(true);
        System.out.println("in setCriteria");
        performSearch();
    }

    private void performSearch() {
        final SearchResponse response = indexService.getMatchCount(builder);
        if (!facetsSetup) {
            resultLabel.setCaption(String.format("%,d matches. Search took %,d ms", response.getHits().totalHits(),
                    response.getTookInMillis()));
        } else {
            resultLabel.setCaption(String.format("%,d filtered matches. Search took %,d ms",
                    response.getHits().totalHits(), response.getTookInMillis()));
        }
        final List<SearchHit> results = new FakeList<SearchHit>((int) response.getHits().totalHits());

        messageList.removeAllComponents();

        if (pager != null) {
            middlePane.removeComponent(pager);
        }


        pager = new PagingComponent<SearchHit>(10, 10, results, new LazyPagingComponentListener<SearchHit>
                (messageList) {

            @Override
            protected Collection<SearchHit> getItemsList(int startIndex, int endIndex) {
                builder = indexService.getNewBuilder(builder);

                if (!facetsSetup) {
               /*     builder.withDateFacet().withFromFacet().withFromDomainFacet().withToFacet().withToDomainFacet()
                            .withFileExtFacet();*/
                	builder.withFromFacet(); //.withFromDomainFacet().withToFacet().withToDomainFacet();
                }

                builder.withResultsLimit(startIndex, endIndex - startIndex);

                SortOrder order = SortOrder.DESC;
                if ("Ascending".equals((String) sortOrderSelector.getValue())) {
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

                System.out.println("in getItemsList with " + startIndex + ", " + endIndex);
                SearchResponse response = indexService.search(builder);


                setUpFacets(response);


                return Arrays.asList(response.getHits().getHits());
            }

            @Override
            protected Component displayItem(int index, SearchHit item) {
                return new SearchHitPanel(item, emailPreview, criteria, me.getApplication());
            }

        });

        middlePane.addComponent(pager, 1);
    }

    private void setUpFacets(SearchResponse response) {

    	
    	
        if (!facetsSetup) {
          /*  DateHistogramFacet dateFacet = response.getFacets().facet(MessageSearchBuilder.FACET_DATE);
            yearFacetPanel.removeAllComponents();
            for (final DateHistogramFacet.Entry entry : dateFacet.getEntries()) {
                final HorizontalLayout hl = new HorizontalLayout();
                hl.setWidth("100%");

                CheckBox checkBox = new CheckBox(yearFormat.format(entry.getTime()));
                checkBox.setImmediate(true);
                checkBox.addListener(new Button.ClickListener() {
                    @Override
                    public void buttonClick(Button.ClickEvent event) {
                        boolean enabled = event.getButton().booleanValue();
                        builder = indexService.getNewBuilder(builder);

                        if (enabled) {
                            builder.withYearFacetValue(entry.getTime());
                        } else {
                            builder.withoutYearFacetValue(entry.getTime());
                        }
                        performSearch();
                    }
                });

                hl.addComponent(checkBox);

                Label countLabel = new Label(String.format("%,d", entry.getCount()));
                hl.addComponent(countLabel);
                hl.setComponentAlignment(countLabel, Alignment.MIDDLE_RIGHT);

                checkBox.setWidth(null);
                countLabel.setWidth(null);
                hl.setExpandRatio(checkBox, 1.0f);

                yearFacetPanel.addComponent(hl);

            }

            TermsFacet fileExtFacet = response.getFacets().facet(MessageSearchBuilder.FACET_FILENAME);
            fileExtFacetPanel.removeAllComponents();
            for (final TermsFacet.Entry entry : fileExtFacet.getEntries()) {
                final HorizontalLayout hl = new HorizontalLayout();
                hl.setWidth("100%");

                CheckBox checkBox = new CheckBox(StringUtils.abbreviate(entry.getTerm(),
                        20 - Integer.toString(entry.getCount()).length()));
                checkBox.setImmediate(true);
                checkBox.addListener(new Button.ClickListener() {
                    @Override
                    public void buttonClick(Button.ClickEvent event) {
                        boolean enabled = event.getButton().booleanValue();
                        builder = indexService.getNewBuilder(builder);

                        if (enabled) {
                            builder.withFileExtFacetValue(entry.getTerm());
                        } else {
                            builder.withoutFileExtFacetValue(entry.getTerm());
                        }
                        performSearch();
                    }
                });

                hl.addComponent(checkBox);

                Label countLabel = new Label(String.format("%,d", entry.getCount()));
                hl.addComponent(countLabel);
                hl.setComponentAlignment(countLabel, Alignment.MIDDLE_RIGHT);

                checkBox.setWidth(null);
                countLabel.setWidth(null);
                hl.setExpandRatio(checkBox, 1.0f);

                fileExtFacetPanel.addComponent(hl);
            }
*/
            TermsFacet fromFacet = response.getFacets().facet(MessageSearchBuilder.FACET_FROM_NAME);
            fromFacetPanel.removeAllComponents();
            for (final TermsFacet.Entry entry : fromFacet.getEntries()) {
                final HorizontalLayout hl = new HorizontalLayout();
                hl.setWidth("100%");

                CheckBox checkBox = new CheckBox(StringUtils.abbreviate(entry.getTerm(),
                        20 - Integer.toString(entry.getCount()).length()));
                checkBox.setImmediate(true);
                checkBox.addListener(new Button.ClickListener() {
                    @Override
                    public void buttonClick(Button.ClickEvent event) {
                        boolean enabled = event.getButton().booleanValue();
                        builder = indexService.getNewBuilder(builder);

                        if (enabled) {
                            builder.withFromFacetValue(entry.getTerm());
                        } else {
                            builder.withoutFromFacetValue(entry.getTerm());
                        }
                        performSearch();
                    }
                });

                hl.addComponent(checkBox);

                Label countLabel = new Label(String.format("%,d", entry.getCount()));
                hl.addComponent(countLabel);
                hl.setComponentAlignment(countLabel, Alignment.MIDDLE_RIGHT);

                checkBox.setWidth(null);
                countLabel.setWidth(null);
                hl.setExpandRatio(checkBox, 1.0f);

                fromFacetPanel.addComponent(hl);
            }
/*
            TermsFacet fromDomainFacet = response.getFacets().facet(MessageSearchBuilder.FACET_FROM_DOMAIN);
            fromDomainFacetPanel.removeAllComponents();
            for (final TermsFacet.Entry entry : fromDomainFacet.getEntries()) {
                if (entry.getTerm().contains(".")) {
                    final HorizontalLayout hl = new HorizontalLayout();
                    hl.setWidth("100%");

                    CheckBox checkBox = new CheckBox(StringUtils.abbreviate(entry.getTerm(),
                            20 - Integer.toString(entry.getCount()).length()));
                    checkBox.setImmediate(true);
                    checkBox.addListener(new Button.ClickListener() {
                        @Override
                        public void buttonClick(Button.ClickEvent event) {
                            boolean enabled = event.getButton().booleanValue();
                            builder = indexService.getNewBuilder(builder);

                            if (enabled) {
                                builder.withFromDomainFacetValue(entry.getTerm());
                            } else {
                                builder.withoutFromDomainFacetValue(entry.getTerm());
                            }
                            performSearch();
                        }
                    });
                    hl.addComponent(checkBox);

                    Label countLabel = new Label(String.format("%,d", entry.getCount()));
                    hl.addComponent(countLabel);
                    hl.setComponentAlignment(countLabel, Alignment.MIDDLE_RIGHT);

                    checkBox.setWidth(null);
                    countLabel.setWidth(null);
                    hl.setExpandRatio(checkBox, 1.0f);

                    fromDomainFacetPanel.addComponent(hl);
                }
            }

            TermsFacet toFacet = response.getFacets().facet(MessageSearchBuilder.FACET_TO_NAME);
            toFacetPanel.removeAllComponents();
            for (final TermsFacet.Entry entry : toFacet.getEntries()) {
                final HorizontalLayout hl = new HorizontalLayout();
                hl.setWidth("100%");

                CheckBox checkBox = new CheckBox(StringUtils.abbreviate(entry.getTerm(),
                        20 - Integer.toString(entry.getCount()).length()));
                checkBox.setImmediate(true);
                checkBox.addListener(new Button.ClickListener() {
                    @Override
                    public void buttonClick(Button.ClickEvent event) {
                        boolean enabled = event.getButton().booleanValue();
                        builder = indexService.getNewBuilder(builder);

                        if (enabled) {
                            builder.withToFacetValue(entry.getTerm());
                        } else {
                            builder.withoutToFacetValue(entry.getTerm());
                        }
                        performSearch();
                    }
                });
                hl.addComponent(checkBox);

                Label countLabel = new Label(String.format("%,d", entry.getCount()));
                hl.addComponent(countLabel);
                hl.setComponentAlignment(countLabel, Alignment.MIDDLE_RIGHT);

                checkBox.setWidth(null);
                countLabel.setWidth(null);
                hl.setExpandRatio(checkBox, 1.0f);

                toFacetPanel.addComponent(hl);
            }

            TermsFacet toDomainFacet = response.getFacets().facet(MessageSearchBuilder.FACET_TO_DOMAIN);
            toDomainFacetPanel.removeAllComponents();
            for (final TermsFacet.Entry entry : toDomainFacet.getEntries()) {
                if (entry.getTerm().contains(".")) {
                    final HorizontalLayout hl = new HorizontalLayout();
                    hl.setWidth("100%");

                    CheckBox checkBox = new CheckBox(StringUtils.abbreviate(entry.getTerm(),
                            20 - Integer.toString(entry.getCount()).length()));
                    checkBox.setImmediate(true);
                    checkBox.addListener(new Button.ClickListener() {
                        @Override
                        public void buttonClick(Button.ClickEvent event) {
                            boolean enabled = event.getButton().booleanValue();
                            builder = indexService.getNewBuilder(builder);

                            if (enabled) {
                                builder.withToDomainFacetValue(entry.getTerm());
                            } else {
                                builder.withoutToDomainFacetValue(entry.getTerm());
                            }
                            performSearch();
                        }
                    });
                    hl.addComponent(checkBox);

                    Label countLabel = new Label(String.format("%,d", entry.getCount()));
                    hl.addComponent(countLabel);
                    hl.setComponentAlignment(countLabel, Alignment.MIDDLE_RIGHT);

                    checkBox.setWidth(null);
                    countLabel.setWidth(null);
                    hl.setExpandRatio(checkBox, 1.0f);

                    toDomainFacetPanel.addComponent(hl);
                }
            }*/
        }
        facetsSetup = true;
    }

    public void reset() {
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

        emailPreview = new EmailPreviewPanel(storageService, indexService);
        rightPane.addComponent(emailPreview);

        sortFieldSelector = new NativeSelect("Sort By");
        sortFieldSelector.addItem("Relevance");
        sortFieldSelector.addItem("Date");
        sortFieldSelector.addItem("Size");
        sortFieldSelector.setValue("Relevance");
        sortFieldSelector.setNullSelectionAllowed(false);
        sortFieldSelector.setImmediate(true);
        sortFieldSelector.addListener(new ValueChangeListener() {

            @Override
            public void valueChange(ValueChangeEvent event) {
                builder = indexService.getNewBuilder(builder);
                System.out.println("in sort field listener");
                performSearch();
            }
        });


        sortOrderSelector = new NativeSelect("Order");
        sortOrderSelector.addItem("Ascending");
        sortOrderSelector.addItem("Descending");
        sortOrderSelector.setValue("Descending");
        sortOrderSelector.setNullSelectionAllowed(false);
        sortOrderSelector.setImmediate(true);
        sortOrderSelector.addListener(new ValueChangeListener() {

            @Override
            public void valueChange(ValueChangeEvent event) {
                builder = indexService.getNewBuilder(builder);
                System.out.println("in sort order listener");
                performSearch();
            }
        });


        HorizontalLayout sortOptionsBar = new HorizontalLayout();
        sortOptionsBar.setSpacing(true);
        sortOptionsBar.addComponent(sortOrderSelector);
        sortOptionsBar.addComponent(sortFieldSelector);

        fromFacetPanel = new Panel("From by name:");
        fromFacetPanel.setWidth("200px");
        fromFacetPanel.setScrollable(true);

        fromDomainFacetPanel = new Panel("From by domain:");
        fromDomainFacetPanel.setWidth("200px");
        fromDomainFacetPanel.setScrollable(true);

        toFacetPanel = new Panel("To by name:");
        toFacetPanel.setWidth("200px");
        toFacetPanel.setScrollable(true);

        toDomainFacetPanel = new Panel("To by domain:");
        toDomainFacetPanel.setWidth("200px");
        toDomainFacetPanel.setScrollable(true);

        yearFacetPanel = new Panel("Year:");
        yearFacetPanel.setWidth("200px");
        yearFacetPanel.setScrollable(true);

        fileExtFacetPanel = new Panel("Attachment file type:");
        fileExtFacetPanel.setWidth("200px");
        fileExtFacetPanel.setScrollable(true);

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

}
