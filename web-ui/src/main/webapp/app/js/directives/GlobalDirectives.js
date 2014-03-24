'use strict';

ironholdApp.directive('pagination', function() {
	return {
		restrict: 'ACE',
		scope: true,
		templateUrl: 'views/Pagination.html',
		controller: 'PaginationController'
	}
});

ironholdApp.directive('body', function() {
	return {
		restrict: "ACE",
		controller: 'BodyController'
	}
});

ironholdApp.directive('errors', function() {
	return {
		restrict: 'ACE',
		controller: 'ErrorsController'
	}
});

ironholdApp.directive('sortingPanel', function() {
	return {
		restrict: 'ACE',
		templateUrl: 'views/SortingPanel.html',
	}
});

ironholdApp.directive('footer', function() {
	return {
		restrict: 'ACE',
		scope: true,
		templateUrl: 'views/Footer.html',
		controller: 'FooterController'
	}
});

ironholdApp.directive('searchTabContainer', function() {
	return {
		restrict: 'ACE',
		scope: true,
		controller: 'SearchController'
	}
});

ironholdApp.directive('messageSearchTab', function() {
	return {
		restrict: 'ACE',
		scope: true,
		controller: 'MessageSearchController'
	}
});

ironholdApp.directive('controlbar', function() {
	return {
		restrict: 'ACE',
		templateUrl: 'views/PreviewTabs/Controlbar.html'
	}
});

ironholdApp.directive('discoveryTabContainer', function() {
	return {
		restrict: 'ACE',
		scope: true,
		controller: 'DiscoveryController'
	}
});

ironholdApp.directive('foldersTabContainer', function() {
	return {
		restrict: 'ACE',
		scope: true,
		controller: 'FoldersController'
	}
});

ironholdApp.directive('logsTabContainer', function() {
	return {
		restrict: 'ACE',
		scope: true,
		controller: 'LogsController'
	}
});

ironholdApp.directive('usersTabContainer', function() {
	return {
		restrict: 'ACE',
		scope: true,
		controller: 'UsersController'
	}
});

ironholdApp.directive('settingsTabContainer', function() {
	return {
		restrict: 'ACE',
		scope: true,
		controller: 'SettingsController'
	}
});

ironholdApp.directive('bindHtmlUnsafe', function( $compile ) {
    return function( $scope, $element, $attrs ) {

        var compile = function( newHTML ) { // Create re-useable compile function
            newHTML = $compile(newHTML)($scope); // Compile html
            $element.html('').append(newHTML); // Clear and append it
        };

        var htmlName = $attrs.bindHtmlUnsafe; // Get the name of the variable 
                                              // Where the HTML is stored

        $scope.$watch(htmlName, function( newHTML ) { // Watch for changes to 
                                                      // the HTML
            if(!newHTML) return;
            compile(newHTML);   // Compile it
        });

    };
});
