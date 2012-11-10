package com.reqo.ironhold.web.components;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.facet.datehistogram.DateHistogramFacet;
import org.elasticsearch.search.facet.terms.TermsFacet;
import org.elasticsearch.search.sort.SortOrder;
import org.vaadin.pagingcomponent.PagingComponent;
import org.vaadin.pagingcomponent.listener.impl.LazyPagingComponentListener;
import org.vaadin.pagingcomponent.utilities.FakeList;

import com.reqo.ironhold.search.IndexFieldEnum;
import com.reqo.ironhold.search.IndexService;
import com.reqo.ironhold.search.MessageSearchBuilder;
import com.reqo.ironhold.storage.IStorageService;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

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
	private EmailPreview emailPreview;
	private PagingComponent<SearchHit> pager;
	private VerticalLayout middlePane;
	private Panel dateHistogram;
	private Panel fromFacetPanel;

	public SearchResults(IndexService indexService,
			IStorageService storageService) {
		this.indexService = indexService;
		this.storageService = storageService;
		this.setSpacing(true);
		this.setMargin(true);
		this.setSizeFull();

	}

	public void setCriteria(String criteria) {
		this.criteria = criteria;

		reset();

		sortFieldSelector.setVisible(true);
		sortOrderSelector.setVisible(true);
		fromFacetPanel.setVisible(true);
		dateHistogram.setVisible(true);
		performSearch();
	}

	private void performSearch() {
		final long count = indexService.getMatchCount(criteria);
		final List<SearchHit> results = new FakeList<SearchHit>((int) count);

		messageList.removeAllComponents();

		if (pager != null) {
			middlePane.removeComponent(pager);
		}
		pager = new PagingComponent<SearchHit>(10, 10, results,
				new LazyPagingComponentListener<SearchHit>(messageList) {

					@Override
					protected Collection<SearchHit> getItemsList(
							int startIndex, int endIndex) {
						MessageSearchBuilder builder = indexService
								.getNewBuilder()
								.withCriteria(criteria)
								.withResultsLimit(startIndex,
										endIndex - startIndex).withDateFacet().withFromFacet();

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
						
						SearchResponse response = indexService.search(builder);

						DateHistogramFacet dateFacet = response.getFacets().facet("date");
						dateHistogram.removeAllComponents();
						for (DateHistogramFacet.Entry entry : dateFacet.getEntries()) {
								
							CheckBox checkBox = new CheckBox(yearFormat.format(entry.getTime()) + "  [" + entry.getCount() + "]");
							checkBox.setImmediate(true);
							dateHistogram.addComponent(checkBox);
						}
						
						TermsFacet fromFacet = response.getFacets().facet("from");
						fromFacetPanel.removeAllComponents();
						for (TermsFacet.Entry entry : fromFacet.getEntries()) {
							
							CheckBox checkBox = new CheckBox(entry.getTerm() + "  [" + entry.getCount() + "]");
							checkBox.setImmediate(true);
							fromFacetPanel.addComponent(checkBox);
						}
						
						return Arrays.asList(response.getHits().getHits());
					}

					@Override
					protected Component displayItem(int index, SearchHit item) {
						return new SearchHitPanel(item, emailPreview, criteria);
					}

				});

		middlePane.addComponent(pager);
	}

	public void reset() {
		this.removeAllComponents();

		leftPane = new VerticalLayout();
		leftPane.setSizeFull();
		middlePane = new VerticalLayout();
		middlePane.setSizeFull();
		rightPane = new VerticalLayout();
		rightPane.setSizeFull();
		messageList = new VerticalLayout();
		messageList.setSpacing(true);

		emailPreview = new EmailPreview(storageService, indexService);
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
				performSearch();
			}
		});
		
		
		HorizontalLayout sortOptionsBar = new HorizontalLayout();
		sortOptionsBar.setSpacing(true);
		sortOptionsBar.addComponent(sortOrderSelector);
		sortOptionsBar.addComponent(sortFieldSelector);

		fromFacetPanel = new Panel("From:");
		fromFacetPanel.setWidth("200px");
		
		dateHistogram = new Panel("Year:");
		dateHistogram.setWidth("200px");
		
		leftPane.setSpacing(true);
		leftPane.addComponent(sortOptionsBar);
		leftPane.addComponent(fromFacetPanel);
		leftPane.addComponent(dateHistogram);
		middlePane.addComponent(messageList);

		sortFieldSelector.setVisible(false);
		sortOrderSelector.setVisible(false);
		dateHistogram.setVisible(false);
		fromFacetPanel.setVisible(false);
		
		this.addComponent(leftPane);
		this.addComponent(middlePane);
		this.addComponent(rightPane);
		this.setExpandRatio(leftPane, 1);
		this.setExpandRatio(middlePane, 3);
		this.setExpandRatio(rightPane, 3);

	}

}
