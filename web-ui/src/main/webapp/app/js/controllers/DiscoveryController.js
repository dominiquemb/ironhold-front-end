'use strict';

ironholdApp.controller('DiscoveryController', function ($http, $resource, $window, $rootScope, $scope, $location, $timeout, Restangular, searchResultsService, $state, logInService, usersService, messagesService) {
    logInService.confirmLoggedIn($state);

    $scope.msgs;
    $scope.pageSize = 10;
    $scope.selectedFacets = [];

    window.onresize = function(){
	if ($scope.scrollbars) {
		$scope.reinitScrollbars();
		$scope.$apply();
	}
    }

    searchResultsService.prepForBroadcast("-", "- ");

    $scope.$watch(function() {
		if ($('.msgview_middle .jspPane').length > 0) {
			return $('.msgview_middle .jspPane').height();
		}
		else return 0;
	},
	function(newval, oldval) {
		if (newval !== oldval) {
			if ($('.msgview_middle .jspPane').length > 0) {
				$scope.reinitScrollbars();
			}
		}
     });

    $scope.$watch(function() {
	return $('.dashboard').width();
	},
	function(newval, oldval) {
		if (newval >= 1430) {
			$('.msgview').addClass('expandable');
		}
		if (newval < 1430) {
			$('.msgview').removeClass('expandable');
		}
	});

    $rootScope.$on('pageChange', function() {
	$scope.reinitScrollbars();
    });

    $scope.initCustomScrollbars = function(selector) {
	$scope.scrollbars = true;
        $timeout(function() {
                $(selector).jScrollPane({
                        verticalArrowPositions: 'split',
                        horizontalArrowPositions: 'split',
			showArrows: true
                });

		$('.filter-list .jspContainer').mouseenter(function(){
		    $(this).find('.jspVerticalBar, .jspHorizontalBar').animate({opacity:1}, 400);
		});

		$('.filter-list .jspContainer').mouseleave(function(){
		    $(this).find('.jspVerticalBar, .jspHorizontalbar').animate({opacity:0}, 400);
		});
        }, 0);
    }

    if (usersService) {
	    usersService.one("searchHistory").get().then(function(result) {
		$scope.$emit('searchHistoryData', result);
	    });
    }

    $rootScope.$on('modeRequest', function(evt, data) {
	messagesService
		.one(data.messageId)
		.one(data.mode)
		.get(data.criteria)
		.then(function(result) {
			$scope.$emit('modeData', {
				mode: data.mode,
				payload: result.payload
			});
		});
    });

    $rootScope.$on('selectResultRequest', function(evt, message, inputSearch) {
        messagesService.one(message.formattedIndexedMailMessage.messageId).get({criteria: inputSearch}).then(function(result) {
	    $scope.$emit('selectResultData', result);
            $scope.$emit('selectMessage', result.payload.messages[0]);
        });
    });

    $rootScope.$on('searchPreviewRequest', function(evt, inputSearch) {
        messagesService.one("count").get({criteria: inputSearch}).then(function(result) {
            $scope.$emit('totalResultsChange', result);
	    $scope.$emit('searchPreviewData', result);
        });
    });

    $scope.reinitScrollbars = function() {
	angular.forEach($('.scrollbar-hidden'), function(container, key) {
		$('.scrollbar-hidden').eq(key).data('jsp').reinitialise();
	});
    }

    $rootScope.$on('pageChange', function(evt, page) {
	$scope.currentPage = page;
	$scope.$emit('updateSearch', {
		inputSearch: $scope.inputSearch
	});
    });

    $rootScope.$on('facetToggled', function(evt, facet, selectedFacets) {
	$scope.selectedFacets = selectedFacets;
	$scope.$emit('updateSearch', {
		inputSearch: $scope.inputSearch
	});
    });

    $rootScope.$on('updateSearch', function(evt, args) {
        messagesService.post("", $scope.selectedFacets, {
		criteria: args.inputSearch, 
		page: $scope.currentPage, 
		pageSize: $scope.pageSize
		}, 
		{
		"Accept": "application/json", 
		"Content-type" : "application/json"
		})
	.then(function(result) {
	    $scope.$emit('updateFooter', {
		searchTime: result.payload.timeTaken,
		searchMatches: result.payload.matches
	    });
	    $scope.$emit('updateSearchbar', {
		searchTime: result.payload.timeTaken,
		searchMatches: result.payload.matches
	    });
		
                $scope.msgs = $scope.formatMessages(result.payload.messages);
                searchResultsService.prepForBroadcast($scope.searchMatches, $scope.searchTime);
		$scope.$emit('results', {
			'matches': result.payload.matches,
			'resultEntries': $scope.msgs
		});
        });
    });

    $scope.formatMessages = function(messages) {
	angular.forEach(messages, function(message) {
	    message.collapsedBody = true;
	    message.collapsedAttachments = false;
		message.formattedIndexedMailMessage.toLine = '';
		message.formattedIndexedMailMessage.ccLine = '';
		message.formattedIndexedMailMessage.bccLine = '';
	    message.formattedIndexedMailMessage.dayName = new Date(message.formattedIndexedMailMessage.messageDate).getDayName();
	    angular.forEach(message.formattedIndexedMailMessage.to, function(to) {
		message.formattedIndexedMailMessage.toLine += to.name + ' <' + to.address + '>; ';
	    });
	    angular.forEach(message.formattedIndexedMailMessage.cc, function(cc) {
		message.formattedIndexedMailMessage.ccLine += cc.name + ' <' + cc.address + '>; ';
	    });
	    angular.forEach(message.formattedIndexedMailMessage.bcc, function(bcc) {
		message.formattedIndexedMailMessage.bccLine += bcc.name + ' <' + bcc.address + '>; ';
	    });
	});
	return messages;
    }
	
    $rootScope.$on('search', function(evt, args) {
	$scope.inputSearch = args.inputSearch;
        messagesService.get({
		criteria: args.inputSearch, 
		facets: "from,from_domain,to,to_domain,date,msg_type,file_ext", 
		pageSize: $scope.pageSize
	    	})
	    .then(function(result) {
		$scope.$emit('updateFooter', {
			searchTime: result.payload.timeTaken,
			searchMatches: result.payload.matches
		});
	    	$scope.$emit('updateSearchbar', {
			searchTime: result.payload.timeTaken,
			searchMatches: result.payload.matches,
			suggestions: result.payload.suggestions,
			showSearchPreviewResults: true,
			showSuggestions: (result.payload.suggestions[0].options.length > 0) ? true : false
	    	});

                    $scope.msgs = $scope.formatMessages(result.payload.messages);

		    searchResultsService.prepForBroadcast(result.payload.matches, $scope.searchTime);
		    $scope.initCustomScrollbars('.scrollbar-hidden');

		    $scope.$emit('facets', result.payload.facets);

		    $scope.$emit('results', {
			'matches': result.payload.matches,
			'resultEntries': $scope.msgs
			});
              });
    });
});
