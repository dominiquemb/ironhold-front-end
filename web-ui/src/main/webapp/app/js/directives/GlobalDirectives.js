'use strict';

ironholdApp.directive('pagination', function() {
	return {
		restrict: 'ACE',
		scope: true,
		templateUrl: 'views/Pagination.html',
		controller: 'PaginationController'
	}
});

ironholdApp.directive('searchTabContainer', function() {
	return {
		restrict: 'ACE',
		controller: 'SearchController'
	}
});

ironholdApp.directive('controlbar', function() {
	return {
		restrict: 'ACE',
		scope: true,
		templateUrl: 'views/PreviewTabs/Controlbar.html'
	}
});

ironholdApp.directive('discoveryTabContainer', function() {
	return {
		restrict: 'ACE'
	}
});

ironholdApp.directive('tagsTabContainer', function() {
	return {
		restrict: 'ACE'
	}
});

ironholdApp.directive('foldersTabContainer', function() {
	return {
		restrict: 'ACE'
	}
});

ironholdApp.directive('logsTabContainer', function() {
	return {
		restrict: 'ACE'
	}
});

ironholdApp.directive('usersTabContainer', function() {
	return {
		restrict: 'ACE'
	}
});

ironholdApp.directive('settingsTabContainer', function() {
	return {
		restrict: 'ACE'
	}
});
