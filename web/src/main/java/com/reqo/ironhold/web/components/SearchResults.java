package com.reqo.ironhold.web.components;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.facet.datehistogram.DateHistogramFacet;
import org.elasticsearch.search.facet.datehistogram.DateHistogramFacet.Entry;
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
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;

@SuppressWarnings("serial")
public class SearchResults extends HorizontalLayout {

	protected static final int MAX_RESULTS_TO_BE_FACETED = 5000;
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

	public SearchResults(IndexService indexService,
			IStorageService storageService) {
		this.indexService = indexService;
		this.storageService = storageService;
		this.me = this;
		this.setSpacing(true);
		this.setMargin(true);
		this.setSizeFull();
		this.builder = indexService.getNewBuilder();
	}

	public void setCriteria(String criteria) {
		long started = System.currentTimeMillis();
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

		performSearch();
		long finished = System.currentTimeMillis();
		resultLabel.setDescription(String.format(
				"Server rendering took %,d ms", (finished - started)));
	}

	private void performSearch() {
		final SearchResponse response = indexService.getMatchCount(builder);
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
							int startIndex, int endIndex) {
						builder = indexService.getNewBuilder(builder);

						if (!facetsSetup) {
							if (results.size() < MAX_RESULTS_TO_BE_FACETED) {
								builder.withDateFacet().withFromFacet()
										.withFromDomainFacet()
										.withToDomainFacet().withFileExtFacet(); // .withToFacet()
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

						SearchResponse response = indexService.search(builder);

						setUpFacets(response);

						resultLabel.setCaption(String.format(
								"%s, Search took %,d ms",
								originalResultLabelValue,
								response.getTookInMillis()));
						return Arrays.asList(response.getHits().getHits());
					}

					@Override
					protected Component displayItem(int index, SearchHit item) {
						return new SearchHitPanel(item, emailPreview, criteria,
								me.getApplication());
					}

				});

		middlePane.addComponent(pager, 1);
	}

	private void setUpFacets(SearchResponse response) {

		if (!facetsSetup) {
			if (builder.isDateFacet()) {
				yearFacetPanel.setVisible(true);
				TermsFacet dateFacet = response.getFacets().facet(
						MessageSearchBuilder.FACET_YEAR);
				yearFacetPanel.removeAllComponents();
				List<TermsFacet.Entry> years = (List<TermsFacet.Entry>) dateFacet.getEntries();
				Collections.sort(years, new Comparator<TermsFacet.Entry>() {

					@Override
					public int compare(TermsFacet.Entry o1, TermsFacet.Entry o2) {
						return o2.getTerm().compareTo(o1.getTerm());
					}

				});

				for (final TermsFacet.Entry entry : years.subList(0,
						Math.min(years.size(), 10))) {
					final HorizontalLayout hl = new HorizontalLayout();
					hl.setWidth("100%");

					CheckBox checkBox = new CheckBox(entry.getTerm());
					checkBox.setImmediate(true);
					checkBox.addListener(new Button.ClickListener() {
						@Override
						public void buttonClick(Button.ClickEvent event) {
							boolean enabled = event.getButton().booleanValue();
							builder = indexService.getNewBuilder(builder);

							if (enabled) {
								builder.withYearFacetValue(entry.getTerm());
							} else {
								builder.withoutYearFacetValue(entry.getTerm());
							}
							performSearch();
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
				for (final TermsFacet.Entry entry : fileExtFacet.getEntries()) {
					final HorizontalLayout hl = new HorizontalLayout();
					hl.setWidth("100%");

					CheckBox checkBox = new CheckBox(StringUtils.abbreviate(
							entry.getTerm(),
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
								builder.withoutFileExtFacetValue(entry
										.getTerm());
							}
							performSearch();
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
						checkBox.addListener(new Button.ClickListener() {
							@Override
							public void buttonClick(Button.ClickEvent event) {
								boolean enabled = event.getButton()
										.booleanValue();
								builder = indexService.getNewBuilder(builder);

								if (enabled) {
									builder.withFromDomainFacetValue(entry
											.getTerm());
								} else {
									builder.withoutFromDomainFacetValue(entry
											.getTerm());
								}
								performSearch();
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
						checkBox.addListener(new Button.ClickListener() {
							@Override
							public void buttonClick(Button.ClickEvent event) {
								boolean enabled = event.getButton()
										.booleanValue();
								builder = indexService.getNewBuilder(builder);

								if (enabled) {
									builder.withToDomainFacetValue(entry
											.getTerm());
								} else {
									builder.withoutToDomainFacetValue(entry
											.getTerm());
								}
								performSearch();
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
