'use strict';

ironholdApp.controller('MessageSearchController', function ($http, $resource, $window, $rootScope, $scope, $location, $timeout, Restangular, searchResultsService, $state, logInService, usersService, messagesService) {
    logInService.confirmLoggedIn($state);

    $scope.msgs;
    $scope.selectedFacets = [];
    $scope.sortOrder = 'ASC';
    $scope.sortField = 'SCORE'; 
    $scope.sortFields = {
        'SCORE': 'relevance',
        'DATE': 'date',
        'SIZE': 'size'
    };

    if ($scope.activeTab === $scope.tabName) {
	    searchResultsService.prepForBroadcast("-", "- ");

	    if (usersService) {
		    usersService.one("searchHistory").get().then(function(result) {
			$scope.$emit('searchHistoryData', result);
		    });
	    }
    }


    $scope.getSortOrder = function() {
        if ($scope.activeTab === $scope.tabName) {
        	return $scope.sortOrder.toLowerCase();
	}
    }

    $rootScope.$on('downloadAttachment', function(evt, info) {
        if ($scope.activeTab === $scope.tabName) {
                var msgDate = new Date(info.message.formattedIndexedMailMessage.messageDate);
                        messagesService
                                .one(msgDate.getFullYear())
                                .one(msgDate.getMonth() + 1)
                                .one(msgDate.getDate())
                                .one(info.message.formattedIndexedMailMessage.messageId)
				.one("download")
                                .one(info.attachment.fileName)
                        .get()
                        .then(function(result) {
                                var dataUrl = 'content-disposition:attachment;filename="' + info.attachment.fileName + '"; content-length:' + info.attachment.size + '; content-type:application/' + info.attachment.fileExt + ',' + encodeURI(result),
                                link = document.createElement('a');

                                angular.element(link)
                                        .attr('href', dataUrl)
                                        .attr('download', info.attachment.fileName);

                                // Firefox
                                if (document.createEvent) {
                                    var event = document.createEvent("MouseEvents");
                                    event.initEvent("click", true, true);
                                    link.dispatchEvent(event);
                                }
                                // IE
                                else if (el.click) {
                                    link.click();
                                }
                        });
	}
    });

    $scope.getSortField = function() {
        if ($scope.activeTab === $scope.tabName) {
        	return $scope.sortFields[$scope.sortField];
	}
    }

    $rootScope.$on('downloadMessage', function(evt, message) { 
        if ($scope.activeTab === $scope.tabName) { 
		var formattedMsg = message.formattedIndexedMailMessage,
		msgDate = new Date(formattedMsg.messageDate);

                messagesService
                        .one(msgDate.getFullYear())
                        .one(msgDate.getMonth() + 1)
                        .one(msgDate.getDate())
                        .one(formattedMsg.messageId)
                        .one('download')
                        .get()
                        .then(function(result) {
            			var dataUrl = 'data:text/plain;utf-9,' + encodeURI(result),
				link = document.createElement('a');
			
				angular.element(link)
					.attr('href', dataUrl)
					.attr('download', formattedMsg.messageId + '.eml');

				// Firefox
				if (document.createEvent) {
				    var event = document.createEvent("MouseEvents");
				    event.initEvent("click", true, true);
				    link.dispatchEvent(event);
				}
				// IE
				else if (el.click) {
				    link.click();
				}
                });
        }       
    }); 

    $scope.changeSortOrder = function(order) {
        if ($scope.activeTab === $scope.tabName) {
		$scope.sortOrder = order;
		$scope.$emit('triggerSearch');
	}
    }

    $scope.changeSortField = function(field) {
        if ($scope.activeTab === $scope.tabName) {
		$scope.sortField = field;
		$scope.$emit('triggerSearch');
	}
    }

    $scope.initialized = function() {
        if ($scope.activeTab === $scope.tabName) {
		$scope.initialState = false;
	}
    }

    $rootScope.$on('reset', function() {
        if ($scope.activeTab === $scope.tabName) {
		$scope.initialState = true;
	}
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
			sortField: $scope.sortField,
			sortOrder: $scope.sortOrder
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
console.log(messagesService);
		messagesService.get({
			criteria: args.inputSearch, 
			facets: "from,from_domain,to,to_domain,date,msg_type,file_ext", 
			pageSize: $scope.pageSize,
			sortField: $scope.sortField,
            		sortOrder: $scope.sortOrder
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

			    $scope.$emit('facets', {
				facets: result.payload.facets,
				matches: result.payload.matches
				});
	
			    $scope.initialized();

			    $scope.$emit('results', {
				'matches': result.payload.matches,
				'resultEntries': $scope.msgs
				});
		      });
	}
    });
});
