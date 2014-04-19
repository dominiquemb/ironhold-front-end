(function () {
   'use strict';

ironholdApp.controller('MessageSearchController', function ($http, $resource, $window, $rootScope, $scope, $location, $timeout, Restangular, searchResultsService, $state, logInService, usersService, messagesService, downloadService) {
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
	    }
    });

    $scope.onTabActivation = function() {
		    searchResultsService.prepForBroadcast("-", "- ");

		    if (usersService) {
			    usersService.one("searchHistory").get().then(function(result) {
				$scope.$emit('searchHistoryData', result);
			    }, function(err) {
				$scope.$emit('technicalError', err);
			    });
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

    $rootScope.$on('selectResultRequest', function(evt, message, inputSearch) {
        if ($scope.activeTab === $scope.tabName) {
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
    });

    $rootScope.$on('searchPreviewRequest', function(evt, inputSearch) {
        if ($scope.activeTab === $scope.tabName) {
            messagesService.one("count").get({criteria: inputSearch}).then(function(result) {
                $scope.$emit('totalResultsChange', result);
                $scope.$emit('searchPreviewData', result);
            }, function(err) {
		$scope.$emit('technicalError', err);
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
                $scope.$emit('reinitScrollbars');

                $scope.$emit('results', {
                    'matches': result.payload.matches,
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
	
    $rootScope.$on('search', function(evt, args) {
        if ($scope.activeTab === $scope.tabName) {
	    var facets;
	    if (!args.disableFacets) {
		facets = "from,from_domain,to,to_domain,date,msg_type,file_ext";
	    }
            $scope.inputSearch = args.inputSearch;
            messagesService.get({
                criteria: args.inputSearch,
                facets: facets,
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
		    disableFacets: args.disableFacets,
                    matches: result.payload.matches
                    });

                    $scope.initialized();

                    $scope.$emit('results', {
                    'matches': result.payload.matches,
                    'resultEntries': $scope.msgs
                    });
                  },
		function(err) {
			$scope.$emit('technicalError', err);
		});
        }
    });
});


}());
