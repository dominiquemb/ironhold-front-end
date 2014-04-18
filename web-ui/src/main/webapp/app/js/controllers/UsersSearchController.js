(function () {
   'use strict';

ironholdApp.controller('UsersSearchController', function ($http, $resource, $window, $rootScope, $scope, $location, $timeout, Restangular, searchResultsService, $state, logInService, usersService) {
    logInService.confirmLoggedIn($state);

    $scope.users = [];
    $scope.pageNum = 0;

    $rootScope.$on('activeTab', function(evt, tab) {
	if (tab === $scope.tabName) {
	    usersService.get({
		    criteria: '*',
		    page: $scope.pageNum,
		    pageSize: $scope.pageSize
	    })
	    .then(function(result) {
			$scope.users = result.payload;
			$scope.initCustomScrollbars('.scrollbar-hidden');
			$scope.initialized();

			$scope.$emit('results', {
			'resultEntries': $scope.users
			});
	    },
	    function(err) {
		$scope.$emit('technicalError', err);
	    });
	}
    });

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

//    $rootScope.$on('selectResultRequest', function(evt, message, inputSearch) {
    $rootScope.$on('selectResultRequest', function() {
        if ($scope.activeTab === $scope.tabName) {
/*
            messagesService.one(message.formattedIndexedMailMessage.messageId).get({criteria: inputSearch}).then(function(result) {
                $scope.$emit('selectResultData', result);
                $scope.$emit('selectMessage', result.payload.messages[0]);
            },
		function(err) {
			$scope.$emit('technicalError', err);
		});
*/
        }
    });

    $rootScope.$on('search', function(evt, args) {
        if ($scope.activeTab === $scope.tabName) {
            $scope.inputSearch = args.inputSearch;
console.log('is this running');
            usersService.get({
		    criteria: args.inputSearch,
		    page: $scope.pageNum,
		    pageSize: $scope.pageSize
            })
            .then(function(result) {
                        $scope.users = result.payload;
                        $scope.initCustomScrollbars('.scrollbar-hidden');
                        $scope.initialized();

                        $scope.$emit('results', {
                        'resultEntries': $scope.users
                        });
            },
            function(err) {
                $scope.$emit('technicalError', err);
            });
        }
    });
});


}());
