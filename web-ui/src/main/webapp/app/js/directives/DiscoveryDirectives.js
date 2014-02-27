'use strict';

ironholdApp.directive('pagination', function() {
	return {
		restrict: 'ACE',
		scope: true,
		templateUrl: 'views/pagination.html',
		controller: 'PaginationController'
	}
});

ironholdApp.directive('discoveryTabContainer', function() {
	return {
		restrict: 'ACE',
		templateUrl: 'views/Discovery.html',
		controller: 'DiscoveryController'
	}
});

ironholdApp.directive('tagsTabContainer', function() {
	return {
		restrict: 'ACE',
		templateUrl: 'views/Tags.html'
	}
});

ironholdApp.directive('foldersTabContainer', function() {
	return {
		restrict: 'ACE',
		templateUrl: 'views/Folders.html'
	}
});

ironholdApp.directive('logsTabContainer', function() {
	return {
		restrict: 'ACE',
		templateUrl: 'views/Logs.html'
	}
});

ironholdApp.directive('usersTabContainer', function() {
	return {
		restrict: 'ACE',
		templateUrl: 'views/Users.html'
	}
});

ironholdApp.directive('settingsTabContainer', function() {
	return {
		restrict: 'ACE',
		templateUrl: 'views/Settings.html'
	}
});
