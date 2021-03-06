(function () {
   'use strict';

ironholdApp.controller('MessageSearchController', function ($http, $resource, $window, $rootScope, $scope, $location, $timeout, Restangular, searchResultsService, $state, logInService, usersService, searchHistoryService, messagesService, downloadService) {
    logInService.confirmLoggedIn($state);

    $scope.msgs = [];
    $scope.selectedFacets = [];
    $scope.sortOrder = 'ASC';
    $scope.sortField = 'SCORE'; 
    $scope.sortFields = {
        'SCORE': 'relevance',
        'DATE': 'date',
        'SIZE': 'size'
    };

    $rootScope.$on('activeTab', function(evt, tab) {
	    if (tab === $scope.tabName) {
		    $scope.onTabActivation();
		    $state.go('loggedin.main.text');
		    $scope.$emit('mode', 'text', false);
                    $scope.initCustomScrollbars('.scrollbar-hidden');
	    }
    });

    $scope.onTabActivation = function() {
		    searchResultsService.prepForBroadcast("-", "- ");

	if (usersService) {
		    var result = searchHistoryService.getSearchHistory();
		    if (result) {
			    if (result.pending) {
					result.pending.then(function(data) {
						$scope.$emit('searchHistoryData', data);
					});
				}
			    else if (result.cached) {
				$scope.$emit('searchHistoryData', result);
			    }
		    } else {
			$scope.$emit('technicalError', 'There is no search history available');
		    }
	}
    };

    $scope.onTabActivation();

    $scope.getSortOrder = function() {
        if ($scope.activeTab === $scope.tabName) {
                return $scope.sortOrder.toLowerCase();
        }
    };

    $rootScope.$on('downloadAttachment', function(evt, info) {
        if ($scope.activeTab === $scope.tabName) {
                var msgDate = new Date(info.message.formattedIndexedMailMessage.messageDate);

                 downloadService
                    .one(msgDate.getFullYear())
                    .one(msgDate.getMonth() + 1)
                    .one(msgDate.getDate())
                    .one(info.message.formattedIndexedMailMessage.messageId)
                    .one(info.attachment.fileName)
                    .post()
                    .then(function(result) {
                        var dataUrl = '${rest-api.proto}://${rest-api.host}:${rest-api.port}/${rest-api.prefix}/download/attachment/' + result;

                        var link = document.createElement('a');

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
                        else if (link.click) {
                            link.click();
                        }
                    });

        }
    });

    $scope.getSortField = function() {
        if ($scope.activeTab === $scope.tabName) {
        	return $scope.sortFields[$scope.sortField];
	    }
    };

    $rootScope.$on('downloadMessage', function(evt, message) { 
        if ($scope.activeTab === $scope.tabName) { 
		var formattedMsg = message.formattedIndexedMailMessage,
		msgDate = new Date(formattedMsg.messageDate);

                downloadService
                        .one(msgDate.getFullYear())
                        .one(msgDate.getMonth() + 1)
                        .one(msgDate.getDate())
                        .one(formattedMsg.messageId)
                        .post()
                        .then(function(result) {
            			var dataUrl = '${rest-api.proto}://${rest-api.host}:${rest-api.port}/${rest-api.prefix}/download/full/' + result;

                        var link = document.createElement('a');

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
                        else if (link.click) {
                            link.click();
                        }
        });
    }});

    $scope.changeSortOrder = function(order) {
        if ($scope.activeTab === $scope.tabName) {
            $scope.sortOrder = order;
            $scope.$emit('triggerSearch');
        }
    };

    $scope.changeSortField = function(field) {
        if ($scope.activeTab === $scope.tabName) {
            $scope.sortField = field;
            $scope.$emit('triggerSearch');
        }
    };

    $scope.initialized = function() {
        if ($scope.activeTab === $scope.tabName) {
            $scope.initialState = false;
        }
    };

    $rootScope.$on('reset', function() {
        if ($scope.activeTab === $scope.tabName) {
            $scope.initialState = true;
        }
    });

    $rootScope.$on('selectResultRequest', function(evt, message, inputSearch, advanced) {
        if ($scope.activeTab === $scope.tabName) {
	    if (advanced) {
		    messagesService
			.one('advanced')
			.one(message.formattedIndexedMailMessage.messageId)
			.get({
				criteria: inputSearch,
				pageSize: $scope.pageSize,
				page: $scope.page,
				sortField: $scope.sortField,
				sortOrder: $scope.sortOrder,
				startDate: advanced.startDate,
				endDate: advanced.endDate,
				sender: advanced.sender,
				recipient: advanced.recipient,
				subject: advanced.subject,
				body: advanced.body,
				messageType: advanced.messageType,
				attachment: advanced.attachment
			})
			.then(function(result) {
				$scope.$emit('selectResultData', result.payload.messages[0]);
				$scope.$emit('selectMessage', result.payload.messages[0]);
		    },
			function(err) {
				$scope.$emit('technicalError', err);
			});
	    } else {
		    messagesService
			.one(message.formattedIndexedMailMessage.messageId)
			.get({criteria: inputSearch})
			.then(function(result) {
				$scope.$emit('selectResultData', result.payload.messages[0]);
				$scope.$emit('selectMessage', result.payload.messages[0]);
		    },
			function(err) {
				$scope.$emit('technicalError', err);
			});
	    }
        }
    });

    $rootScope.$on('searchPreviewRequest', function(evt, args) {
        if ($scope.activeTab === $scope.tabName) {
		if (args.advanced) {
			messagesService
				.one('count')
				.one('advanced')
				.get({
					criteria: args.inputSearch,
					pageSize: $scope.pageSize,
					page: $scope.page,
					sortField: $scope.sortField,
					sortOrder: $scope.sortOrder,
					startDate: args.advanced.startDate,
					endDate: args.advanced.endDate,
					sender: args.advanced.sender,
					recipient: args.advanced.recipient,
					subject: args.advanced.subject,
					body: args.advanced.body,
					messageType: args.advanced.messageType,
					attachment: args.advanced.attachment
				})
				.then(function(result) {
					$scope.$emit('totalResultsChange', result);
					$scope.$emit('searchPreviewData', result, args);
				},
				function(err) {
					$scope.$emit('technicalError', err);
				});
	    } else {
		    messagesService
			.one("count")
			.get({criteria: args.inputSearch})
			.then(function(result) {
				$scope.$emit('totalResultsChange', result);
				$scope.$emit('searchPreviewData', result, args);
	                }, function(err) {
				$scope.$emit('technicalError', err);
			});
	    }
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
                $scope.$emit('reinitScrollbars');

                $scope.$emit('results', {
                    'matches': result.payload.matches,
		    'advanced': args.advanced,
                    'resultEntries': $scope.msgs
                });
            },
	    function(err) {
			$scope.$emit('technicalError', err);
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
    };

    $scope.processResults = function(args, result) {
			var showSuggestions = false;
			if (result.payload.suggestions[0]) {
				if (result.payload.suggestions[0].options) {
					if (result.payload.suggestions[0].options.length > 0) {
						showSuggestions = true;
					}
				}
			}

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
				showSuggestions: showSuggestions
			});

			$scope.msgs = $scope.formatMessages(result.payload.messages);
			searchResultsService.prepForBroadcast(result.payload.matches, $scope.searchTime);
			$scope.initCustomScrollbars('.scrollbar-hidden');

		
			if (!args.advanced || !args.disableFacets) {
			    $scope.$emit('facets', {
			    facets: result.payload.facets,
			    disableFacets: args.disableFacets,
			    matches: result.payload.matches
			    });
			}

			$scope.initialized();

			    $scope.$emit('results', {
			    'matches': result.payload.matches,
			    'advanced': args.advanced,
			    'resultEntries': $scope.msgs
			    });
    };
	
    $rootScope.$on('search', function(evt, args) {
        if ($scope.activeTab === $scope.tabName) {
            $scope.inputSearch = args.inputSearch;

	    if (args.advanced) {
		messagesService
			.one('advanced')
			.get({
				criteria: args.inputSearch,
				pageSize: $scope.pageSize,
				page: $scope.page,
				sortField: $scope.sortField,
				sortOrder: $scope.sortOrder,
				startDate: args.advanced.startDate,
				endDate: args.advanced.endDate,
				sender: args.advanced.sender,
				recipient: args.advanced.recipient,
				subject: args.advanced.subject,
				body: args.advanced.body,
				messageType: args.advanced.messageType,
				attachment: args.advanced.attachment
			})
			.then(function(result) {
				$scope.processResults(args, result);
			});
	    }
	    else {
		    var facets;
		    if (!args.disableFacets) {
			facets = "from,from_domain,to,to_domain,date,msg_type,file_ext";
		    }
		    messagesService.get({
			criteria: args.inputSearch,
			facets: facets,
			pageSize: $scope.pageSize,
			sortField: $scope.sortField,
				sortOrder: $scope.sortOrder
			})
			.then(function(result) {
				$scope.processResults(args, result);
			  },
			function(err) {
				$scope.$emit('technicalError', err);
			});
		}
        }
    });
});


}());
