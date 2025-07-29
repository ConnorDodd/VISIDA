
angular.module('visida_cms').controller('adviceController', ['$scope', 'Restangular', '$routeParams', '$window', function($scope, Restangular, $routeParams, $window) {
  $scope.search = $routeParams.search ? $routeParams.search : '';
  $scope.searchTable = $routeParams.table ? parseInt($routeParams.table) : '';
  $scope.activePage = $routeParams.page ? parseInt($routeParams.page) : 1;
  $scope.newAdvice = {suggestions:[]};

  var url = "Advice?PageSize=25&PageNumber=" + $scope.activePage;
  if ($scope.search)
    url += "&search=" + $scope.search;
  if ($scope.searchTable)
    url += "&table=" + $scope.searchTable;
  $scope.loading = 2;

  Restangular.oneUrl(url).get().then(function(response) {
    $scope.loading--;
    $scope.pageData = JSON.parse(response.headers('paging'));
    $scope.records = response.data;
  }, function(error) {
    $scope.loading--;
  });

  Restangular.one('FoodCompositionTables').getList().then(function(result) {
    $scope.fctables = result.data;
  }, function(error) {

  }).finally(function() { $scope.loading--; });

  $scope.searchFunction = function() {
    //if ($scope.search === $routeParams.search)
      //$window.location.reload();
    var url = '#!/advice?page=1';
    if ($scope.search) url += '&search='+$scope.search;
    if ($scope.searchTable) url += '&table='+$scope.searchTable;
    $window.location = url;
  };

  $scope.changeTable = function(tableId) {
    $scope.loading++;
    Restangular.oneUrl('FoodCompositionTables/' + tableId + '/Names').get().then(function(record) {
      $scope.foodCompositions = record.data;
      $scope.loading--;
    }, function (error) {
      $scope.loading--;
    });
  };

  $scope.createAdvice = function(advice) {
    //var advice = {description: desc, issueDescription: issue, solutionDescription: sol};
    if (!advice.tableId) return $scope.createError = "Food composition table must be selected.";
    if (!advice.description) return $scope.createError = "Description is required.";
    if (!advice.issueDescription) return $scope.createError = "Issue is required.";
    if (!advice.solutionDescription) return $scope.createError = "Assumption is required.";

    $scope.loading++;
    Restangular.oneUrl("Advice").post('', advice).then(function(response) {
      alert("Added successfully");
      //$scope.newDescription = $scope.newIssue = $scope.newSolution = "";
    }, function(error) {
      alert("Could not create advice. " + error.data.message);
      $scope.showCreateDialog = true;
    }).finally(function() { $scope.loading--; });
    $scope.showCreateDialog = false;
  }

  $scope.selectFoodComposition = function(sugg) {
    $scope.newAdvice.suggestions.push(sugg);
    $scope.newAdviceSuggestion = null;
  };
  $scope.removeNewFoodComposition = function(index) {
    $scope.newAdvice.suggestions.splice(index, 1);
  }

  $scope.pageNumArr = null;
  $scope.getPageNumber = function() {
    if ($scope.pageNumArr)
      return $scope.pageNumArr;
    if ($scope.pageData) {
      if ($scope.pageData.totalPages > 25) {
        var start = $scope.activePage - 12;
        var end = $scope.activePage + 12;//Math.min($scope.pageData.totalPages, $scope.activePage + 12);
        if (start < 1) {
          var trim = Math.abs(12 - $scope.activePage);
          start = 1;
          end = Math.min($scope.pageData.totalPages, end + trim + 1);
        }
        if (end > $scope.pageData.totalPages) {
          var trim = end - $scope.pageData.totalPages;
          end = $scope.pageData.totalPages;
          start = Math.max(1, start - trim);
        }
        var arr = [];
        for (var i = start; i <= end; i++) {
          arr.push(i);
        }
        $scope.pageNumArr = arr;
        return arr;
      }
      else {
        var arr = [];
        for (var i = 1; i <= $scope.pageData.totalPages; i++) {
          arr.push(i);
        }
        $scope.pageNumArr = arr;
        return arr;
      }
    }
    else
      return [1];
  };
}]);