'use strict';

ironholdApp.controller('MessageSearchController', function ($http, $resource, $window, $rootScope, $scope, $location, $timeout, Restangular, searchResultsService, $state, logInService, usersService, messagesService) {
    logInService.confirmLoggedIn($state);

    $scope.msgs;
    $scope.selectedFacets = [];

    searchResultsService.prepForBroadcast("-", "- ");

    if (usersService) {
	    usersService.one("searchHistory").get().then(function(result) {
		$scope.$emit('searchHistoryData', result);
	    });
    }

    $scope.initialized = function() {
	$scope.initialState = false;
    }

    $rootScope.$on('reset', function() {
	$scope.initialState = true;
    });

    $rootScope.$on('selectResultRequest', function(evt, message, inputSearch) {
        if ($scope.activeTab === $scope.tabName) {
		messagesService.one(message.formattedIndexedMailMessage.messageId).get({criteria: inputSearch}).then(function(result) {
		    $scope.$emit('selectResultData', result);
		    $scope.$emit('selectMessage', result.payload.messages[0]);
		});
	}
    });

    $rootScope.$on('searchPreviewRequest', function(evt, inputSearch) {
        if ($scope.activeTab === $scope.tabName) {
		messagesService.one("count").get({criteria: inputSearch}).then(function(result) {
		    $scope.$emit('totalResultsChange', result);
		    $scope.$emit('searchPreviewData', result);
		});
	}
    });

    $rootScope.$on('facetToggled', function(evt, facet, selectedFacets) {
        if ($scope.activeTab === $scope.tabName) {
		$scope.selectedFacets = selectedFacets;
		$scope.$emit('updateSearch', {
			inputSearch: $scope.inputSearch
		});
	}
    });

    $rootScope.$on('updateSearch', function(evt, args) {
        if ($scope.activeTab === $scope.tabName) {
		messagesService.post("", $scope.selectedFacets, {
			criteria: args.inputSearch, 
			page: $scope.currentPage, 
			pageSize: $scope.pageSize,
			sortField: "SCORE",
			sortOrder: "DESC"
			}, 
			{
			"Accept": "application/json", 
			"Content-type" : "application/json"
			})
		.then(function(result) {
		    $scope.$emit('updateFooter', {
			searchTime: result.payload.timeTaken,
			searchMatches: result.payload.matches,
			selectedFacets: $scope.selectedFacets
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
	}
    });

    $scope.formatMessages = function(messages) {
        if ($scope.activeTab === $scope.tabName) {
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
    }
	
    $rootScope.$on('search', function(evt, args) {
	if ($scope.activeTab === $scope.tabName) {
		$scope.inputSearch = args.inputSearch;
		messagesService.get({
			criteria: args.inputSearch, 
			facets: "from,from_domain,to,to_domain,date,msg_type,file_ext", 
			pageSize: $scope.pageSize,
			sortField: "SCORE",
            sortOrder: "DESC"
			})
		    .then(function(result) {
			$scope.$emit('updateFooter', {
				searchTime: result.payload.timeTaken,
				searchMatches: result.payload.matches,
				selectedFacets: []
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
	
			    $scope.initialized();

			    $scope.$emit('results', {
				'matches': result.payload.matches,
				'resultEntries': $scope.msgs
				});
		      });
	}
    });
});
