angular.module('visida_cms').controller('eatController', ['$scope', 'Restangular', '$routeParams', '$window', '$cookies', '$rootScope', '$controller', function($scope, Restangular, $routeParams, $window, $cookies, $rootScope, $controller) {
  $scope.loading = 2;
  $controller('commonCtrl', { $scope: $scope });
  
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

  // $('#daysModal').on('hidden.bs.modal', function () {
  //   $('#searchButton').focus();
  // });

  $scope.buildURL = function(url) {
    if ($scope.searchRecipe) url += '&recipe=' + $scope.searchRecipe;
    if ($scope.searchRecordType) url += '&recordType=' + $scope.searchRecordType;
    if ($scope.selectedStudy) url += '&study=' + $scope.selectedStudy;
    if ($scope.selectedHousehold) url += '&household=' + $scope.selectedHousehold;
    if ($scope.selectedHouseholdMember) url += '&member=' + $scope.selectedHouseholdMember;
    if ($scope.searchIdent) url += '&identified=' + $scope.searchIdent;
    if ($scope.searchPrt) url += '&portioned=' + $scope.searchPrt;
    if ($scope.searchText) url += ('&search=' + $scope.searchText);
    if ($scope.searchCommentText) url += ('&comment=' + $scope.searchCommentText);
    if ($scope.searchOrder) url += ('&order=' + $scope.searchOrder);
    if ($scope.searchVariation) url += ('&gv=' + $scope.searchVariation);
    if ($scope.searchHidden) url += ('&hidden=' + $scope.searchHidden);
    return encodeURI(url);
  };
  $scope.buildLink = function(page) {
    var url = "#!/records?page=" + page + "&results=" + $scope.pageSize;
    return $scope.buildURL(url);
  };
  $scope.buildLinkRecord = function(record) {
    var url = "#!/image?id=" + record.id;
    return $scope.buildURL(url);
  }
  $scope.linkRecord = function(record, evt) {
    if (evt.ctrlKey)
      return $scope.linkRecordCtrl(record);
    $window.location.href = $scope.buildLinkRecord(record);
  };
  $scope.linkRecordCtrl = function(record) {
    $window.open($scope.buildLinkRecord(record), '_blank');
  };
  $scope.linkMouseDown = function(record, evt) {
    evt.preventDefault();
    if (evt.which === 2)
      $scope.linkRecordCtrl(record);
  };
  $scope.getImageSrc = function(img) {
    if (img.imageUrl)
      return img.imageThumbUrl ? img.imageThumbUrl : img.imageUrl;
    if (img.is24HR)
      return 'images/tfhrecall.png';
    if (img.audioUrl)
      return 'images/audioimage.png'
    return 'images/noimage.png'
  };
  $scope.getStatusStyle = function(tag) {
    if (tag < 0)
      return 'text-danger'
    if (tag > 0)
      return 'text-success'
    return 'yellow'
  };
  $scope.getStatusName = function(tag) {
    if (tag < 0)
      return 'Not Started'
    if (tag > 0)
      return 'Completed'
    return 'In Progress'
  };
  $scope.buildFoodString = function(fi) {
    var str = '';
    str += fi.quantityGrams != 0 ? fi.quantityGrams.toFixed(1) + 'g ' : '~ ';
    str += fi.name.substring(0, 35);
    // if (fi.createdByAdmin)
    //   str += ' [A]';
    // else
    //   str += fi.gestaltCount > 0 ? ' [' + fi.gestaltCount + ']' : '';
    return str;
  };
  $scope.styleGestaltDiff = function(fi) {
    if (!$scope.searchVariation || !fi.gestaltDifference || fi.createdByAdmin)
      return 'hidden'
    if (fi.gestaltDifference >= $scope.searchVariation)
      return 'text-danger'
    return '';
  };

  $scope.movePage = function(event) {
    window.scrollTo(0, 0);
  };

  $scope.search = function() {
    $scope.searchRecipe = null;
    var url = '#!/records?page=1&results='+$scope.pageSize;
    url = $scope.buildURL(url);

    var days = null;
    if ($scope.searchDays && !$scope.allDaysChecked) {
      days = [];
      for (var i = 0; i < $scope.searchDays.length; i++)
        if ($scope.searchDays[i].checked)
          days.push($scope.searchDays[i].date);
          //url += ('&date=' + $scope.searchDays[i].date.substring(0, 9));
    }
    $cookies.put('search_days', JSON.stringify(days));

    if ($window.location.hash === url)
      $window.location.reload();
    else
      $window.location = url;
  };

  $scope.nameWindowSize = function(size) {
    if (size < 768) return 'xs';
    if (size < 992) return 'sm';
    if (size < 1200) return 'md';
    return 'lg';
  }

  $scope.clearSearch = function() {
    $scope.pageSize = '10';
    $scope.activePage = 1;
    $scope.searchRecordType = "";
    $scope.searchRecipe = "";
    $scope.selectedStudy = null;
    $scope.selectedHousehold = "";
    $scope.selectedHouseholdMember = "";
    $scope.searchIdent = "";
    $scope.searchPrt = "";
    $scope.searchText = "";
    $scope.searchCommentText = "";
    $scope.searchVariation = null;
    $scope.searchHidden = "";
    $scope.studyChanged(true);
  }

  $scope.hasRouteParams = function() {
    return ($routeParams.results
      || $routeParams.page
      || $routeParams.recordType
      || $routeParams.recipe
      || $routeParams.study
      || $routeParams.household
      || $routeParams.identified
      || $routeParams.portioned
      || $routeParams.search
      || $routeParams.comment
      || $routeParams.gv
      || $routeParams.hidden)
  };

  $scope.toggleSearchOrder = function() {
    if ($scope.searchOrder === 'desc') {
      $scope.searchOrder = null;
      $cookies.put('search_order', null);
    } else {
      $scope.searchOrder = 'desc';
      $cookies.put('search_order', 'desc');
    }
    $scope.search();
  }

  function getCookie(name, def) {
  var value = $cookies.get(name)
  if (value && value !== 'null' && value !== 'undefined' && value !== 'NaN') {
    var type = typeof def;
    if (type === 'boolean')
      return value === 'true';
    if (type === 'number')
      return parseInt(value);
    return value;
  }
  else
    return def !== undefined ? def : "";
}

  $scope.loadRecords = function() {
    var url = 'ImageRecords?PageSize=' + $scope.pageSize + '&PageNumber=' + $scope.activePage;
    url = $scope.buildURL(url);
    if ($scope.searchDaysParam)
      for (var i = 0; i < $scope.searchDaysParam.length; i++)
        url += ('&date=' + $scope.searchDaysParam[i].substring(0, 10))

    Restangular.all(url).getList().then(function(records) {
      $scope.images = records.data;
      $scope.pageData = JSON.parse(records.headers('paging'));
      $rootScope.timeTaken = records.headers('processTime');
      $scope.images.forEach(function(img) {
        img.tagIdent = -1;
        img.tagPrt = -1;
        //if (img.foodItems.length <= 0) {
        if (img.foodItems.length > 0) {
          img.tagIdent = 0;

          for (var fidx = img.foodItems.length - 1; fidx >= 0; fidx--) {
            var fi = img.foodItems[fidx];

            //Calculate gestalt diff
            if (fi.gestaltMinEstimate && fi.gestaltMaxEstimate) {
              fi.gestaltDifference = 
                ((fi.gestaltMaxEstimate - fi.gestaltMinEstimate) / 
                  ((fi.gestaltMaxEstimate + fi.gestaltMinEstimate) / 2)) * 100
            }

            if (fi.quantityGrams > 0)
              img.tagPrt = 0;
          }
        }
        if (img.isCompleted)
          img.tagIdent = 1;
        if ((img.foodItems.length > 0 || img.isCompleted) && img.foodItems.filter(function(item) { return (img.gestaltMax > 0 && (item.gestaltCount < img.gestaltMax && !item.createdByAdmin)) || item.quantityGrams == 0 }).length == 0)
          img.tagPrt = 1;
      });
    }, function errorCallback(response) {
    }).finally(function() { $scope.loading--; });
  };

  $scope.searchCommentTextChanged = function(txt) {
    var a  = txt;
    $scope.searchCommentText = txt;
    return;
  };

  // if (($rootScope.previousPage && $rootScope.previousPage.$$route.originalPath === '/records') || $routeParams.recipe) {
  //   $scope.pageSize = $routeParams.results ? $routeParams.results : '10';
  //   $scope.activePage = $routeParams.page ? parseInt($routeParams.page) : 1;
  //   $scope.searchRecordType = $routeParams.recordType;
  //   $scope.searchRecipe = $routeParams.recipe;
  //   $scope.selectedStudy = $routeParams.study;
  //   $scope.selectedHousehold = $routeParams.household;
  //   $scope.searchIdent = $routeParams.identified;
  //   $scope.searchPrt = $routeParams.portioned;
  //   $scope.searchText = $routeParams.search;
  // } else {
  //   $scope.pageSize = $routeParams.results ? $routeParams.results : 
  //     getCookie('search_page_size', '10');
  //   $scope.activePage = $routeParams.page ? parseInt($routeParams.page) : 
  //     parseInt(getCookie('search_page_number', 1));
  //   $scope.searchRecordType = $routeParams.recordType ? $routeParams.recordType :
  //     getCookie('search_record_type');
  //   $scope.searchRecipe = $routeParams.recipe;
  //   $scope.selectedStudy = $routeParams.study ? $routeParams.study :
  //     getCookie('search_study', null);
  //   $scope.selectedHousehold = $routeParams.household ? $routeParams.household :
  //     getCookie('search_household');
  //   $scope.searchIdent = $routeParams.identified ? $routeParams.identified :
  //     getCookie('search_identified');
  //   $scope.searchPrt = $routeParams.portioned ? $routeParams.portioned :
  //     getCookie('search_portioned');
  // }
  // if ($scope.selectedStudy)
  //   $scope.selectedStudy = parseInt($scope.selectedStudy);

  if ($scope.hasRouteParams()) {
    $scope.pageSize = $routeParams.results ? $routeParams.results : '10';
    $scope.activePage = $routeParams.page ? parseInt($routeParams.page) : 1;
    $scope.searchRecordType = $routeParams.recordType;
    $scope.searchRecipe = $routeParams.recipe;
    $scope.selectedStudy = $routeParams.study;
    $scope.selectedHousehold = $routeParams.household;
    $scope.selectedHouseholdMember = $routeParams.member;
    $scope.searchIdent = $routeParams.identified;
    $scope.searchPrt = $routeParams.portioned;
    $scope.searchText = $routeParams.search;
    $scope.searchCommentText = $routeParams.comment;
    $scope.searchVariation = $routeParams.gv;
    $scope.searchHidden = $routeParams.hidden;
  } else {
    $scope.pageSize = getCookie('search_page_size', 10);
    $scope.activePage = getCookie('search_page_number', 1);
    $scope.searchRecordType = getCookie('search_record_type');
    //$scope.searchRecipe = getCookie('search_recipe');
    $scope.selectedStudy = getCookie('search_study');
    $scope.selectedHousehold = getCookie('search_household');
    $scope.selectedHouseholdMember = getCookie('search_member');
    $scope.searchIdent = getCookie('search_identified');
    $scope.searchPrt = getCookie('search_portioned');
    $scope.searchText = getCookie('search_text');
    $scope.searchCommentText = getCookie('search_comment_text');
    $scope.searchVariation = getCookie('search_variation');
    $scope.searchHidden = getCookie('search_hidden');

    var url = '#!/records?page=' + $scope.activePage + '&results='+$scope.pageSize;
    url = $scope.buildURL(url);
    $window.location = url;
    return;
  }
  var days = getCookie('search_days', null)
  if (days)
    $scope.searchDaysParam = JSON.parse(days);
  if ($scope.selectedStudy)
    $scope.selectedStudy = parseInt($scope.selectedStudy);
  if ($scope.searchVariation)
    $scope.searchVariation = parseInt($scope.searchVariation);
  $scope.searchOrder = getCookie('search_order');

  if ($scope.searchCommentText || $scope.searchText || $scope.searchIdent || $scope.searchPrt || $scope.searchVariation || $scope.searchHidden)
    $scope.showAdvanced = true;

  $cookies.put('search_page_size', $scope.pageSize);
  $cookies.put('search_page_number', $scope.activePage);
  $cookies.put('search_record_type', $scope.searchRecordType);
  // $cookies.put('search_recipe', $scope.searchRecipe);
  $cookies.put('search_study', $scope.selectedStudy);
  $cookies.put('search_household', $scope.selectedHousehold);
  $cookies.put('search_member', $scope.selectedHouseholdMember);
  $cookies.put('search_identified', $scope.searchIdent);
  $cookies.put('search_portioned', $scope.searchPrt);
  $cookies.put('search_text', $scope.searchText);
  $cookies.put('search_comment_text', $scope.searchCommentText);
  $cookies.put('search_variation', $scope.searchVariation);
  $cookies.put('search_hidden', $scope.searchHidden);

  $scope.loadRecords();

  $scope.windowWidth = $scope.nameWindowSize($window.innerWidth);

}]);

app.directive('windowSizeDirective', ['$window', function ($window) {

   return {
      link: link,
      restrict: 'A'           
   };

   function link(scope, element, attrs){

     angular.element($window).on('resize', function(){
        scope.$apply(function() {

         scope.windowWidth = scope.nameWindowSize($window.innerWidth);
        });
     });    
   }    
}]);

app.directive("studySelector", function() {
  return {
    restrict: 'E',
    scope:false,
    templateUrl: 'views/records/studySelectorForm.html'
  }
})
