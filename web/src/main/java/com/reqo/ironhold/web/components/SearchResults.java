package com.reqo.ironhold.web.components;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.elasticsearch.search.SearchHit;
import org.vaadin.pagingcomponent.PagingComponent;
import org.vaadin.pagingcomponent.listener.impl.LazyPagingComponentListener;
import org.vaadin.pagingcomponent.utilities.FakeList;

import com.reqo.ironhold.search.IndexService;
import com.reqo.ironhold.storage.IStorageService;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
public class SearchResults extends VerticalLayout {

	private final IndexService indexService;
	private final IStorageService storageService;
	public SearchResults(IndexService indexService, IStorageService storageService) {
		this.indexService = indexService;
		this.storageService = storageService;
		this.setSpacing(true);
		this.setMargin(true);
	}

	public void performSearch(final String criteria) {
		final long count = indexService.getMatchCount(criteria);
		final List<SearchHit> results = new FakeList<SearchHit>((int) count);

		this.removeAllComponents();

		final HorizontalLayout resultsPane = new HorizontalLayout();
		resultsPane.setSizeFull();
		final VerticalLayout leftPane = new VerticalLayout();
		leftPane.setSizeFull();
		final VerticalLayout rightPane = new VerticalLayout();
		rightPane.setSizeFull();
		final VerticalLayout messageList = new VerticalLayout();
		messageList.setSpacing(true);

		
		final EmailPreview emailPreview = new EmailPreview(storageService);
		rightPane.addComponent(emailPreview);
		
		final PagingComponent<SearchHit> pager = new PagingComponent<SearchHit>(
				10, 10, results, new LazyPagingComponentListener<SearchHit>(
						messageList) {

					@Override
					protected Collection<SearchHit> getItemsList(
							int startIndex, int endIndex) {
						try {
							return Arrays.asList(indexService.search(criteria,
									startIndex, endIndex - startIndex));
						} catch (JsonParseException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (JsonMappingException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						return null;
					}

					@Override
					protected Component displayItem(int index, SearchHit item) {
						return new SearchHitPanel(item, emailPreview);
					}

				});
		leftPane.addComponent(messageList);
		leftPane.addComponent(pager);
;
		resultsPane.addComponent(leftPane);
		resultsPane.addComponent(rightPane);

		this.addComponent(resultsPane);

	}

	public void reset() {
		this.removeAllComponents();

	}

}
