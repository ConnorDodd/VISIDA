

app = angular.module('visida_cms');

app.controller('commentCtrl', ['$scope', 'Restangular', '$cookies', 'ClientConfig', function($scope, Restangular, $cookies, ClientConfig) {


  $scope.configUsers = ClientConfig.loadUsers();
  if ($scope.configUsers == null) {
    Restangular.one('GetSearchUsers').get()
    .then(function(result) {
      $scope.configUsers = result.data;
      ClientConfig.saveUsers($scope.configUsers);
    }).finally(function() {
    });
  }

  // var atRegex = /@[^\s]*/g;
  $scope.validateComment = function(comment) {
    var atMatches = comment.text.match(atRegex);
    if (atMatches) {
      var error = null;
      for (var i = 0; i < atMatches.length; i++) {
        var m = atMatches[i];
        m = m.substring(1);

        var found = false;
        for (var i = 0; i < $scope.configUsers.length; i++) {
          if ($scope.configUsers[i].userName === m) {
            found = true;
            break;
          }
        }
        if (!found) {
          error = '@' + m;
          break;
        }
      }

      if (error !== null) {
        alert(error + ' is not a valid username tag, please select one from the list.');
        return false;
      }
    }

    return true;
  };
  $scope.changeComment = function(index) {
    $scope.commentIndex = index;
  };
  $scope.cancelNewComment = function() { 
    $scope.newComment = null; 
  };
  $scope.commentToggleTaskComplete = function(comment) {
    if (comment.taskCompleted) {
      if (!confirm("Are you sure you want to uncomplete this task?"))
        return;
      comment.taskCompleted = null;
      comment.isTaskCompleted = false;
    } else {
      comment.taskCompleted = new Date();
      comment.isTaskCompleted = true;
    }

    Restangular.one('Comments/' + comment.id).customPUT(comment).then(function(result) {
    }, function(result) {
      alert("Task could not be updated: " + result.message)
    });
  };


  $scope.styleComment = function(comment) {
    if (comment.hidden)
      return 'strikethrough';
    else
      return ';'
    }
  $scope.commentTextChanged = function(comment, prev) {
    if (comment.text.endsWith('@')) {
      comment.isMentioning = true;
      setTimeout(function() { $('#mention-entry').focus(); }, 0);
    } else if (prev.endsWith('@'))
      comment.isMentioning = false;

  };
  $scope.toggleCommentFlag = function(comment) {
    comment.highPriority = !comment.highPriority;
  };
  $scope.toggleTaskFlag = function(comment) {
    comment.isTask = !comment.isTask;
  };
  $scope.toggleCommentMention = function(comment) {
    comment.isMentioning = !comment.isMentioning;
    if (comment.isMentioning)
      setTimeout(function() { $('#mention-entry').focus(); }, 0);
  };
  $scope.commentMentionUpdated = function(comment, entry) {
    for (var i = 0; i < $scope.configUsers.length; i++) {
      if ($scope.configUsers[i].userName === entry) {
        if (comment.text.endsWith('@'))
          comment.text = comment.text.substring(0, comment.text.length-1);
        comment.text += ' @' + entry;
        comment.mentionAutoFill = '';
        comment.isMentioning = false;
        return;
      }
    }
  };
  $scope.deleteComment = function(comment) {
    if (!comment.hidden && !confirm('Are you sure you want to delete this comment?'))
      return;
    comment.hidden = !comment.hidden;

    Restangular.one('Comments/' + comment.id).customDELETE().then(function(result) {
    }, function(result) {
      comment.hidden = !comment.hidden;
      alert("Comment could not be deleted: " + result.message)
    });
  };
}]);



app.controller('cookController', ['$scope', 'Restangular', '$routeParams', '$window', '$rootScope', '$cookies', '$controller', function($scope, Restangular, $routeParams, $window, $rootScope, $cookies, $controller) {
  $scope.loading = 2;
  $controller('commonCtrl', { $scope: $scope });

  $scope.search = function() {
    var url = '#!/cook?page=1&results='+$scope.pageSize;
    url = $scope.buildURL(url);

    var days = null;
    if ($scope.searchDays && !$scope.allDaysChecked) {
      days = [];
      for (var i = 0; i < $scope.searchDays.length; i++)
        if ($scope.searchDays[i].checked)
          days.push($scope.searchDays[i].date);
          //url += ('&date=' + $scope.searchDays[i].date.substring(0, 9));
    }
    $cookies.put('cook_days', JSON.stringify(days));

    if (window.location.hash === url)
      $window.location.reload();
    else
      $window.location = url;
  };

  $scope.buildURL = function(url) {
    if ($scope.selectedHousehold) url += ('&household=' + $scope.selectedHousehold);
    if ($scope.selectedStudy) url += ('&study=' + $scope.selectedStudy);
    if ($scope.searchText) url += ('&search=' + $scope.searchText);
    if ($scope.searchCommentText) url += ('&comment=' + $scope.searchCommentText);
    return encodeURI(url);
  };
  $scope.buildLink = function(page) {
    var url = "#!/cook?page=" + page;
    if ($scope.pageSize) url += ('&results=' + $scope.pageSize);
    return $scope.buildURL(url);
  };
  $scope.buildRecipeLink = function(recipe) {
    var url = "#!/recipe?id=" + recipe.id;
    return $scope.buildURL(url);
  }
  $scope.recipeLink = function(recipe, evt) {
    if (evt.ctrlKey)
      return $scope.recipeLinkCtrl(recipe);
    $window.location.href = $scope.buildRecipeLink(recipe);
  };
  $scope.recipeLinkCtrl = function(recipe) {
    $window.open($scope.buildRecipeLink(recipe), '_blank');
  };
  $scope.recipeMouseDown = function(recipe, evt) {
    if (evt.which === 2) {
      evt.preventDefault();
      $scope.recipeLinkCtrl(recipe);
    }
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

  $scope.loadRecords = function() {
    var url = 'CookRecipes?PageSize=' + $scope.pageSize + '&PageNumber=' + $scope.activePage;
    url = $scope.buildURL(url);
    if ($scope.searchDaysParam)
      for (var i = 0; i < $scope.searchDaysParam.length; i++)
        url += ('&date=' + $scope.searchDaysParam[i].substring(0, 10))

    Restangular.all(url).getList().then(function(records) {
      $scope.recipes = records.data;
      //TODO parse ident tag
      for (var i = 0; i < $scope.recipes.length; i++) {
        var recipe = $scope.recipes[i];
        var total = 0, completed = 0, totalItems = 0, portioned = 0;
        for (var j = 0; j < recipe.ingredients.length; j++) {
          var ir = recipe.ingredients[j].imageRecord;
          total++;
          if (ir.isCompleted)
            completed++;
          totalItems += ir.foodItems.length;
          var a = ir.foodItems.filter(function(x) { return x.quantityGrams > 0; });
          // if (a.length === ir.foodItems.length)
          portioned += a.length;

        }
        recipe.tagIdent = 0;
        recipe.tagPrt = 0;
        if (total === 0)
          recipe.tagIdent = -1;
        if (portioned === 0)
          recipe.tagPrt = -1;
        if (total > 0) {
          if (total === completed)
            recipe.tagIdent = 1;
          if (totalItems === portioned)
            recipe.tagPrt = 1;
        }
      }
      $scope.pageData = JSON.parse(records.headers('paging'));
      $rootScope.timeTaken = records.headers('processTime');
    }, function errorCallback(response) {
    }).finally(function() { $scope.loading--; });
  };

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

  $scope.hasRouteParams = function() {
    return ($routeParams.results
      || $routeParams.page
      || $routeParams.recordType
      || $routeParams.study
      || $routeParams.household
      || $routeParams.search
      || $routeParams.comment)
  };

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

  if ($scope.hasRouteParams()) {
    $scope.pageSize = $routeParams.results ? $routeParams.results : '10';
    $scope.activePage = $routeParams.page ? parseInt($routeParams.page) : 1;
    $scope.selectedStudy = $routeParams.study;
    $scope.selectedHousehold = $routeParams.household;
    $scope.searchText = $routeParams.search;
    $scope.searchCommentText = $routeParams.comment;
  } else {
    $scope.pageSize = getCookie('cook_page_size', 10);
    $scope.activePage = getCookie('cook_page_number', 1);
    $scope.selectedStudy = getCookie('cook_study');
    $scope.selectedHousehold = getCookie('cook_household');
    $scope.searchText = getCookie('cook_text');
    $scope.searchCommentText = getCookie('cook_comment_text');

    var url = '#!/cook?page=' + $scope.activePage + '&results=' + $scope.pageSize;
    url = $scope.buildURL(url);
    $window.location = url;
    return;
  }
  var days = getCookie('cook_days', null)
  if (days)
    $scope.searchDaysParam = JSON.parse(days);
  if ($scope.selectedStudy)
    $scope.selectedStudy = parseInt($scope.selectedStudy);

  $cookies.put('cook_page_size', $scope.pageSize);
  $cookies.put('cook_page_number', $scope.activePage);
  $cookies.put('cook_study', $scope.selectedStudy);
  $cookies.put('cook_household', $scope.selectedHousehold);
  $cookies.put('cook_text', $scope.searchText);
  $cookies.put('cook_comment_text', $scope.searchCommentText);

  // $scope.pageSize = $routeParams.results ? $routeParams.results : '10';
  // $scope.activePage = $routeParams.page ? parseInt($routeParams.page) : 1;
  // $scope.selectedHousehold = $routeParams.household;
  // $scope.selectedStudy = $routeParams.study;

  $scope.loadRecords();

  // Restangular.one('GetSearchConfig').get().then(function(response) {
  //   $scope.searchConfig = response.data;
  //   $scope.updateHouseholds();    
  // });
}]);



app.controller('recipeController', ['$scope', 'Restangular', '$routeParams', '$window', 'UserService', '$timeout', '$controller',
  function($scope, Restangular, $routeParams, $window, UserService, $timeout, $controller) {
  
  if (!$routeParams || !$routeParams.id)
    $window.location = "#!cook";
  $scope.mainMethodSelect = "0";
  $scope.commentIndex = 0;
  $scope.loading = true;

  $controller('commentCtrl', { $scope: $scope });

  $scope.selectedStudy = $routeParams.study;
  $scope.selectedHousehold = $routeParams.household;
  $scope.searchText = $routeParams.search;
  $scope.searchCommentText = $routeParams.comment;

  var url = 'CookRecipes/' + $routeParams.id + '?';
  if ($scope.selectedStudy) url += ('&Study=' + $scope.selectedStudy);
  if ($scope.selectedHousehold) url += ('&Household=' + $scope.selectedHousehold);
  if ($scope.searchText) url += ('&search=' + $scope.searchText);
  if ($scope.searchCommentText) url += ('&comment=' + $scope.searchCommentText);
  url = encodeURI(url);

  Restangular.one(url).get().then(function(record) {
    $scope.recipe = record.data;
    $scope.pageData = JSON.parse(record.headers('pages'));
    $scope.editName = !$scope.recipe.name;
    $scope.recipe.cookedModifier = $scope.recipe.totalCookedGrams / 100;

    if (UserService.isAdmin || $scope.recipe.assignation.accessLevel === 'Coordinator')
      $scope.canAdmin = true;

    var wCount = 0, fCount = 0, oCount = 0;
    for (var i = 0; i < $scope.recipe.ingredients.length; i++) {
      var ingredient = $scope.recipe.ingredients[i];
      if (ingredient.cookMethod === 1) wCount++;
      else if (ingredient.cookMethod === 2) fCount++;
      else if (ingredient.cookMethod === 3) oCount++;

      ingredient.cookSelect = ingredient.cookMethod + "";
      for (var j = 0; j < ingredient.imageRecord.foodItems.length; j++) {
        if (!ingredient.imageRecord.foodItems[j].foodCompositionDatabaseEntry)
          continue;

        if (ingredient.imageRecord.foodItems[j].foodCompositionDatabaseEntry.yieldWater)
          ingredient.yieldWater = true;
        if (ingredient.imageRecord.foodItems[j].foodCompositionDatabaseEntry.yieldStoveTop)
          ingredient.yieldStoveTop = true;
        if (ingredient.imageRecord.foodItems[j].foodCompositionDatabaseEntry.yieldOven)
          ingredient.yieldOven = true;
      }
    }
    $scope.displayTable();

    if ($scope.recipe.comments.length == 0)
      $scope.newComment = {authorName: "Me", text: "", flag: "normal"};
    else {
      for (var i = 0; i < $scope.recipe.comments.length; i++) {
        var comment = $scope.recipe.comments[i];
        comment.isTaskCompleted = comment.taskCompleted != null;
        if (comment.replyTo > 0) {
          $scope.recipe.comments.splice(i, 1);
          i--;
          for (var j = 0; j < $scope.recipe.comments.length; j++) {
            if ($scope.recipe.comments[j].id == comment.replyTo) {
              $scope.recipe.comments[j].replies.push(comment);
              break;
            }
          }
        } else
          comment.replies = [];
      }
      $scope.commentIndex = $scope.recipe.comments.length - 1;
    }

    $scope.genericFactors = [
      {cookMethod: -1, name: 'Not Selected', factor: 1},
      {cookMethod: 0, name: 'Uncooked', factor: 1},
      {cookMethod: 4, name: 'Stir-fry, generic', factor: 0.78},
      {cookMethod: 1, name: 'Fish, boiled', factor: 0.77},
      {cookMethod: 4, name: 'Fish, fried', factor: 0.8},
      {cookMethod: 1, name: 'Soup, without noodles', factor: 0.86},
      {cookMethod: 1, name: 'Soup, with noodles', factor: 1.1}
    ];
    $scope.recipe.matchingFactors = $scope.genericFactors.concat($scope.recipe.matchingFactors);
    $scope.recipe.matchingFactors.push({cookMethod: -1, name: 'Enter custom values', factor: 1})
    // for (var i = 0; i < $scope.recipe.matchingFactors.length; i++) {
    //   $scope.recipe.matchingFactors[i]
    // }


    $(document).ready(function(){
      var evt = new Event();
      $scope.mag = new Magnifier(evt);
      $scope.mag.attach({
        thumb: '#image',
        largeWrapper: 'preview'
      });
    });
  }, function errorCallback(error) {
    return;
  }).finally(function() { $scope.loading = false; });

  $scope.buildLink = function(id) {
    if (!$scope.recipe)
      return;
    var url = "#!recipe?id=" + ((id && id > 0) ? id : $scope.recipe.id);
    if ($scope.selectedHousehold) url += ('&household=' + $scope.selectedHousehold);
    if ($scope.selectedStudy) url += ('&study=' + $scope.selectedStudy);
    if ($scope.searchText) url += ('&search=' + $scope.searchText);
    if ($scope.searchCommentText) url += ('&comment=' + $scope.searchCommentText);
    return encodeURI(url);
  };

  $scope.cookMethodToString = function(method) {
    switch (method) {
      case 0:
        return '';//'[Uncooked]';
      case 1:
        return '[Boil/Poach/Steam]';
      case 2:
        return '[Grill]';
      case 3:
        return '[Oven]';
      case 4:
        return '[Fry]';
      default:
        return "";
    }
  }

  $scope.getToolTip = function(record, index) {
    var tt = 'Ingredient ' + (index + 1) + '\n';
    if (record.cookMethod)
      tt += $scope.cookMethodToString(record.cookMethod) + '\n';
    tt += '\n'

    for (var i = 0; i < record.imageRecord.foodItems.length; i++) {
      var fi = record.imageRecord.foodItems[i];
      if (!fi.name)
        fi.name = 'Unidentified'

      var rIdx = fi.name.regexIndexOf(/[^a-z ]/i);
      if (rIdx <= 0)
        rIdx = fi.name.length;
      tt += fi.quantityGrams.toFixed(1) + 'g ' + fi.name.substring(0, rIdx) + '\n';
    }
    return tt;
  };

  $scope.movePage = function(event) {
    if ($scope.dirty && !confirm("There is unsaved data. Are you sure you want to leave this page?")) {
      event.stopPropagation();
      event.preventDefault();
      return;
    }
    var replies = $scope.recipe.comments.filter(function(x) { return x.replyText; });
    if ((replies.length > 0 || ($scope.newComment && $scope.newComment.text)) && !confirm("There is an unsaved comment. Are you sure you want to leave this page?")) {
      event.stopPropagation();
      event.preventDefault();
    }
  };

  $scope.displayTable = function() {
    $scope.comparison = {quantityGrams: 0, density: "-"};
    $scope.fctConfig = $.extend(true, {}, fctConfigStart);

    var keys = Object.keys($scope.recipe.foodComposition);
    var ingCount = 1;
    for (var i = 0; i < $scope.recipe.ingredients.length; i++) {
      var ingredient = $scope.recipe.ingredients[i];

      if (ingredient.imageRecord.hidden) {
        ingredient.displayIndex = "Hidden";
        continue;
      }

      ingredient.displayIndex = ingCount++;

      ingredient.nutrientTotal = { quantityGrams: 0, density: "-"};
      if (!ingredient.imageRecord.isCompleted) {
        ingredient.error = "This image record has not been marked as completed";
        ingredient.errorId = ingredient.imageRecord.id;
      }
      for (var j = 0; j < ingredient.imageRecord.foodItems.length; j++) {
        var foodItem = ingredient.imageRecord.foodItems[j];
        foodItem.nutrientTotal = {};
        if (!foodItem.quantityGrams) {
          foodItem.error = "This food item has not been quantified"
          foodItem.errorId = ingredient.imageRecord.id;
        }
        ingredient.nutrientTotal.quantityGrams += foodItem.quantityGrams;
        $scope.comparison.quantityGrams += foodItem.quantityGrams;

        if (!foodItem.foodCompositionDatabaseEntry)
          continue;
        
        for (var l = keys.length - 1; l >= 0; l--) {
          var key = keys[l];
          if (!$scope.fctConfig.hasOwnProperty(key)) //Exclude inherited variables
            continue;

          if (foodItem.foodCompositionDatabaseEntry[key] != null) {
            $scope.fctConfig[key].valid = true;
            
            var calculated = foodItem.foodCompositionDatabaseEntry[key] * (foodItem.quantityGrams / 100);

            //apply retention factor
            if (foodItem.retentionFactor) {
              var rfVal = foodItem.retentionFactor[key];
              if (rfVal)
                calculated  = calculated * rfVal;
            }

            if (key == 'density') {
              foodItem.nutrientTotal[key] =  foodItem.foodCompositionDatabaseEntry[key];
              //ingredient.nutrientTotal[key] = (ingredient.nutrientTotal[key] || 0) + foodItem.foodCompositionDatabaseEntry[key];
              //$scope.comparison[key] = ($scope.comparison[key] || 0) + foodItem.foodCompositionDatabaseEntry[key]; //probably wrong
            } else {
              if (key == 'niacin')
                var a = 1;
              foodItem.nutrientTotal[key] = calculated;
              ingredient.nutrientTotal[key] = (ingredient.nutrientTotal[key] || 0) + calculated;
              $scope.comparison[key] = ($scope.comparison[key] || 0) + (foodItem.foodCompositionDatabaseEntry[key] * (foodItem.quantityGrams / 100));
            }
            
          }
        }
      }
      //ingredient.nutrientTotal.density /= ingredient.nutrientTotal.densityCount;
    }
    //$scope.comparison.density /= $scope.comparison.densityCount;
  };

  $scope.saveName = function() {
    //validate
    if (!$scope.recipe.name.length > 0) {
      alert("Cannot save recipe. Must have a name.")
      return;
    }
    if ($scope.recipe.isSource) {
      for (var i = 0; i < $scope.recipe.matchingFactors.length; i++) {
        if ($scope.recipe.matchingFactors[i].name === $scope.recipe.name) {
          alert("Cannot save recipe. A source recipe name cannot be the same as any other source recipe name in the drop-down.");
          return;
        }
      }
    }

    $scope.recipe.put().then(function(record) {
      $scope.recipe.foodComposition = record.data.foodComposition;
      //if (record.data.matchingFactors && record.data.matchingFactors.length > 0)
        //$scope.recipe.matchingFactors = record.data.matchingFactors;
      // $scope.displayTable();
      $scope.editName = false;
      $scope.dirty = false;
      $scope.displayTable();
      return;
    }, function(error) {
      alert(error.data.message);
    });
  };

  $scope.initYieldFactor = function() {
    $timeout(function() {
      var elt = document.getElementById("yield-factor-select");
      if ($scope.recipe.yieldFactorSource) {
        for (var i = 0; i < $scope.recipe.matchingFactors.length; i++) {
          if ($scope.recipe.matchingFactors[i].name === $scope.recipe.yieldFactorSource) {
            // && $scope.recipe.matchingFactors[i].factor === $scope.recipe.yieldFactor) {
            elt.selectedIndex = i+1; //one for indexing
            if ($scope.recipe.yieldFactorSource === 'Enter custom values')
              $scope.recipe.isCustomYF = true;
            return;
          }
        }
      }
      elt.selectedIndex = 1;
    }, 0);
  };
  $scope.changeYieldFactor = function() {
    $scope.dirty = true;
    var elt = document.getElementById("yield-factor-select");

    if (elt && elt.selectedIndex >= 0) {
      var yf = $scope.recipe.matchingFactors[elt.selectedIndex]; //Remove 1 for added "not selected"
      $scope.recipe.yieldFactor = yf.factor;

      if (yf.name === 'Enter custom values') {
        $scope.recipe.isCustomYF = true;
        $scope.recipe.yieldFactorSource = 'Enter custom values';
        $scope.recipe.foodComposition.density = null;
      } else {
        $scope.recipe.isCustomYF = false;
        $scope.recipe.isSource = false;
        $scope.recipe.yieldFactorSource = yf.name;
        $scope.recipe.foodComposition.density = yf.density;
        // $scope.recipe.foodGroup = yf.foodGroup.substring(0, 2);

        if (yf.cookMethod || yf.name === 'Uncooked') {
          for (var i = 0; i < $scope.recipe.ingredients.length; i++) {
            $scope.recipe.ingredients[i].cookMethod = yf.cookMethod;
            $scope.recipe.ingredients[i].cookSelect = yf.cookMethod + "";
          }
        }
      }
    }
  }

  $scope.makeDirty = function() { $scope.dirty = true; }
  $scope.updateYield = function(ingredient) {
    ingredient.cookMethod = parseInt(ingredient.cookSelect);
    $scope.dirty = true;
  }

  $scope.toggleHidden = function(tog) {
    if (tog && $scope.recipe.usages.length > 0 && !confirm("There are already records using this recipe. These should be changed to a different recipe or food item if you hide this one. Are you sure you want to hide?"))
      return;
    $scope.recipe.hidden = tog;
    $scope.recipe.put().then(function(record) {
      $scope.recipe.hidden = tog;
    }, function error(res) {
      $scope.recipe.hidden = !tog;
    });
  };
  $scope.startEditTime = function() {
    var time = new Date($scope.recipe.captureTime);
    time.setSeconds(0);
    $scope.editTimeTime = time;
    $scope.editTimeDate = new Date($scope.recipe.captureTime);
    $scope.displayEditTime = true;
  };
  $scope.saveEditTime = function(time, date) {
    var tStr = formatDateString(time);
    var dStr = formatDateString(date);
    var ns = dStr.substring(0, 10) + tStr.substring(10);

    $scope.recipe.captureTime = ns;
    $scope.displayEditTime = false;

    $scope.recipe.put().then(function(record) {
    }, function error(res) {
      alert("There was an error updating the time.")
    });
  };

  $scope.addComment = function(comment) {
    if (!comment) {
      $scope.newComment = {authorName: "Me", text: "", flag: "Normal"};
      return;
    }
    if (!$scope.validateComment(comment))
      return;
    if (!comment.text || comment.text.length == 0)
      return;
    comment.flag = 'Normal'
    if (comment.isTask)
      comment.flag = 'Task'

    $scope.recipe.comments.push(comment);
    $scope.commentIndex = $scope.recipe.comments.length-1;
    $scope.newComment = null;
    Restangular.one('CookRecipes/' + $scope.recipe.id + '/Comments').post('', comment).then(function(result) {
      comment.id = result.data;
    }, function(result) {
      alert("Comment could not be uploaded: " + result.message)
    });
  };
  $scope.addCommentReply = function(comment, text) {
    if (!comment || !text) {
      return;
    }
    comment.replyText = "";
    var reply = {authorName: "Me", text: text, flag: "Reply", replyTo: comment.id};
    comment.replies.push(reply);
    Restangular.one('CookRecipes/' + $scope.recipe.id + '/Comments').post('', reply).then(function(result) {
      reply.id = result.data;
    }, function(result) {
      alert("Comment could not be uploaded: " + result.message)
    });
  };
}]);