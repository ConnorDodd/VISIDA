
angular.module('visida_cms').controller('eatImageController', ['$scope', '$rootScope', 'Restangular', '$routeParams', '$window', 'UserService', '$controller',
  function($scope, $rootScope, Restangular, $routeParams, $window, UserService, $controller) {
  
  if (!$routeParams || !$routeParams.id)
    $window.location = "#!eat";
  $scope.textIndex = 0;
  $scope.loading = 1;
  $scope.showParticipantDialog = false;
  $scope.newParticipant = {};
  //var canvas = document.getElementById('imgCanvas');
  //var ctx = canvas.getContext('2d');
  $scope.colors = ["#e74c3c","#2ecc71","#9b59b6","#e67e22","#1abc9c","#f1c40f","#3498db"];
  $scope.commentIndex = 0;
  $scope.foodItemList = [];
  $scope.startTime = new Date();

  $controller('commentCtrl', { $scope: $scope });

  $scope.searchRecordType = $routeParams.recordType;
  $scope.searchHousehold = $routeParams.household;
  $scope.searchMember = $routeParams.member;
  $scope.searchRecipe = $routeParams.recipe;
  $scope.searchStudy = $routeParams.study;
  $scope.searchIdent = $routeParams.identified;
  $scope.searchPrt = $routeParams.portioned;
  $scope.searchText = $routeParams.search;
  $scope.searchCommentText = $routeParams.comment;
  $scope.searchOrder = $routeParams.order;
  $scope.searchHidden = $routeParams.hidden;
  $scope.gestaltVariation = parseFloat($routeParams.gv);

  $scope.displayHistoryIdentify = true;
  $scope.displayHistoryQuantify = true;
  $scope.displayHistoryDelete = true;

  var url = 'ImageRecords/' + $routeParams.id + '?';
  if ($scope.searchRecordType) url += '&recordType=' + $scope.searchRecordType;
  if ($scope.searchHousehold) url += ('&Household=' + $scope.searchHousehold);
  if ($scope.searchMember) url += ('&Member=' + $scope.searchMember);
  if ($scope.searchRecipe) url += ('&Recipe=' + $scope.searchRecipe);
  if ($scope.searchStudy) url += ('&Study=' + $scope.searchStudy);
  if ($scope.searchIdent) url += ('&identified=' + $scope.searchIdent); 
  if ($scope.searchPrt) url += ('&portioned=' + $scope.searchPrt);
  if ($scope.searchText) url += ('&search=' + $scope.searchText);
  if ($scope.searchCommentText) url += ('&comment=' + $scope.searchCommentText);
  if ($scope.searchOrder) url += ('&order=' + $scope.searchOrder);
  if ($scope.searchHidden) url += ('&hidden=' + $scope.searchHidden);
  if ($scope.gestaltVariation) url += ('&gv=' + $scope.gestaltVariation);
  url = encodeURI(url);
  if ($scope.searchDaysParam)
    for (var i = 0; i < $scope.searchDaysParam.length; i++)
      url += ('&date=' + $scope.searchDaysParam[i].substring(0, 10))
  
  Restangular.one(url).get().then(function(record) {
    $scope.record = record.data;
    $rootScope.title = $scope.record.recordType + ' - ' + $scope.record.id;
    $rootScope.timeTaken = record.headers('processTime');
    $scope.updateLabels();
    var access = $scope.record.assignation ? $scope.record.assignation.accessLevel : null;
    $scope.canIdentify = true, $scope.canQuantify = true, $scope.canComplete = true, $scope.canAdmin = false;
    if (UserService.isAdmin || access === 'Coordinator')
      $scope.canAdmin = true;
    else {
      $scope.canIdentify = (access === 'Identify' || access === 'Both');
      $scope.canQuantify = (access === 'Quantify' || access === 'Both');
      $scope.canComplete = $scope.canIdentify || $scope.record.recordType === 'Test';
    }
    $scope.newTranslationText = $scope.record.translation;
    $scope.newManualTranscript = $scope.record.manualTranscript;
    $scope.newTextDescription = $scope.record.textDescription;

    $scope.pageData = JSON.parse(record.headers('pages'));
    if ($scope.record.householdRecipeNames) {
      var toAdd = $scope.record.householdRecipeNames.map(function(elem) {
        return {
          id: elem.id,
          recipeId: elem.recipeId,
          name: elem.name + (elem.alternateName ? " (" + elem.alternateName + ")" : '')
        }
      });
      $scope.foodItemList = $scope.foodItemList.concat(toAdd);
    }

    if ($scope.record.is24HR) {
      var lines = $scope.record.textDescription.split('\n');
      $scope.record.recall = {};
      $scope.record.recall.name = lines[1];
      $scope.record.recall.time = new Date(lines[2]);
      $scope.record.recall.description = lines[3];
      $scope.record.recall.amount = lines[4];
      $scope.record.recall.prep = lines[5];
      $scope.record.recall.recipe = lines[6];
      $scope.record.recall.combined = lines[7];
      $scope.record.recall.leftovers = lines[8];
      $scope.record.recall.comments = lines[9];

      for (var i = 0; i < $scope.record.suggestions.length; i++) {
        var sugg = $scope.record.suggestions[i];
        var name = sugg.name.substring(0, sugg.name.search(/\d/)-1);
        if (name === $scope.record.recall.name) {
          $scope.record.recall.recipeLink = sugg.name;
          $scope.record.recall.recipeLinkId = sugg.imageRecordId;
          break;
        }
      }

      $scope.record.textDescription = $scope.record.recall.recipe; //$scope.record.recall.name + " " + 
    }

    if (!$scope.record.textDescription && $scope.record.transcript)
      $scope.record.textDescription = $scope.record.transcript;

    if ($scope.record.transcriptionGroups && $scope.record.textDescription) {

      $scope.displayTranscriptionGroups($scope.record.transcriptionGroups, $scope.record.textDescription);
      // if ($scope.adviceSearch.length > 0) {
      //   $scope.adviceSearchStr = $scope.adviceSearch = $scope.adviceSearch.substring(0, $scope.adviceSearch.length-2)
      //   if ($scope.adviceSearch.length > 20)
      //     $scope.adviceSearchStr = $scope.adviceSearch.substring(0, 20) + "...";
      // }
    }
    if ($scope.record.nTranscriptionGroups && $scope.record.nTranscript) {
      $scope.displayNTranscriptionGroups($scope.record.nTranscriptionGroups, $scope.record.nTranscript);
    }


    // if ($scope.record.foodItems.length <= 0)
    //   $scope.addItem();
    if ($scope.record.comments.length == 0)
      $scope.newComment = {authorName: "Me", text: "", flag: "normal"};
    else {
      for (var i = 0; i < $scope.record.comments.length; i++) {
        var comment = $scope.record.comments[i];
        if (comment.replyTo > 0) {
          $scope.record.comments.splice(i, 1);
          i--;
          for (var j = 0; j < $scope.record.comments.length; j++) {
            if ($scope.record.comments[j].id == comment.replyTo) {
              $scope.record.comments[j].replies.push(comment);
              break;
            }
          }
        } else
          comment.replies = [];
      }
      $scope.commentIndex = $scope.record.comments.length - 1;
    }

    $scope.adviceSearch = $scope.adviceSearchStr = "";
    for (var i = 0; i < $scope.record.foodItems.length; i++) {
      var foodItem = $scope.record.foodItems[i];
      if (foodItem.name) {
        $scope.adviceSearch += foodItem.name;
        var split = foodItem.name.indexOf(",");
        if (split <= 0)
          split = foodItem.name.length-1;
        $scope.adviceSearchStr += foodItem.name.substring(0, split);
      }
      for (var j = 0; j < $scope.record.suggestions.length; j++) {
        if ($scope.record.suggestions[j].name === $scope.record.foodItems[i].name)
          $scope.record.suggestions.splice(j, 1);
      }
      foodItem.gestaltDifference = ((foodItem.gestaltMaxEstimate - foodItem.gestaltMinEstimate) / ((foodItem.gestaltMaxEstimate + foodItem.gestaltMinEstimate) / 2)) * 100
    }

    if ($scope.record.foodItems.length == 0 && $scope.record.suggestions.length > 0)
      $scope.displaySuggestions = true;

    if ($scope.record.tableId) {
      Restangular.one('ExampleMeasures/' + $scope.record.tableId).get().then(function(result) {
        $scope.exampleMeasures = result.data;
        for (var i = 0; i < $scope.exampleMeasures.length; i++)
          if ($scope.exampleMeasures[i].name === 'rice white cooked bowl') {
            for (var j = 0; j < $scope.exampleMeasures.length; j++)
              if ($scope.exampleMeasures[j].name === 'rice white cooked plate') {
                var t = $scope.exampleMeasures[i];
                $scope.exampleMeasures[i] = $scope.exampleMeasures[j];
                $scope.exampleMeasures[j] = t;
                break;
              }
            break;
          }
        for (var i = 0; i < $scope.record.foodItems.length; i++)
          $scope.matchExampleMeasure($scope.record.foodItems[i]);
        return;
      }, function(error) {

      });

      Restangular.one('FoodCompositionTables/' + $scope.record.tableId + '/Measures').get().then(function(record) {
        $scope.measuresList = record.data.measures;
        $scope.standardMeasures = record.data.standards;
        var toAdd = record.data.measures.map(function(elem) {
          return {
            id: elem.id,
            name: elem.name + (elem.alternateName ? " (" + elem.alternateName + ")" : '')
          }
        });
        $scope.foodItemList = $scope.foodItemList.concat(toAdd);
        for (var i = 0; i < $scope.record.foodItems.length; i++) {
          $scope.changeFoodItem(i);
        }

        if ($scope.adviceSearch) {
          Restangular.oneUrl("Advice?PageSize=25&PageNumber=1&table=" + $scope.record.tableId + "&search=" + $scope.adviceSearch).get().then(function(response) {
            $scope.advice = response.data;
            for (var i = $scope.advice.length - 1; i >= 0; i--) {
              $scope.advice[i].links = [];
              for (var j = $scope.advice[i].suggestions.length - 1; j >= 0; j--) {
                var name = $scope.advice[i].suggestions[j];
                var fc = $scope.measuresList.filter(function(food) { return food.name === name })[0];
                if (fc) {
                  $scope.advice[i].links.push({id: fc.id, name: name});
                }
              }
            }
          });
        }
        $(document).ready(function(){
          if ($scope.record.imageUrl) {
            var evt = new Event();
            $scope.mag = new Magnifier(evt);
            $scope.mag.attach({
              thumb: '#image',
              // largeWrapper: 'preview'
              mode: 'inside',
              zoom: 2,
              zoomable: true
            });
          }
          // $('#image').magnifier({magnify:5, display:$('#hoverimage'), region:{h:140, w:140}});
        });

      }, function (error) {
        return error;
      });
    }

    if ($scope.record.recordType === 'EatRecord') {
      Restangular.one('Households/' + $scope.record.householdId + '/Members').get().then(function(result) {
        $scope.householdMembers = result.data;
        for (var i = 0; i < $scope.householdMembers.length; i++) {
          for (var j = 0; j < $scope.record.participants.length; j++) {
            if ($scope.record.participants[j].id === $scope.householdMembers[i].id) {
              $scope.householdMembers.splice(i, 1);
              i--;
              break;
            }
          }
        }
      }, function(result) {
      });
    }

    return;
  }, function errorCallback(error) {
    if (error.status === 401)
      $window.location = "#!eat";
    return;
  }).finally(function() { $scope.loading--; });

  window.onkeydown = function(e) {
    if (e.keyCode === 27) {
      $scope.$apply(function() {
        $scope.showHelpDialog = false;
        $scope.showParticipantDialog = false;
        $scope.editGuests = false;
        $scope.displayMeasures = false;
        $scope.displayImage = false;
      });
    }
  };

  $scope.displayTranscriptionGroups = function(groups, text) {
    // var links = [];
    //   var desc = text.toLowerCase();
    //   for (var i = 0; i < groups.length; i++) {
    //     //$scope.adviceSearch += $scope.record.transcriptionGroups[i].key + ", ";
    //     var index = desc.indexOf(groups[i].key.toLowerCase());
    //     if (index > 0)
    //       links.push({key: desc.substring(0, index)});
    //     links.push(groups[i]);
    //     desc = desc.substring(index + groups[i].key.length, desc.length);
    //     desc = desc.trim();
    //   }
    //   if (desc.length > 0)
    //     links.push({key: desc});
    //   groups = links;

    var desc = $scope.record.textDescription;
    var lower = desc.toLowerCase();
    var temp = [];
    for (var i = 0; i < $scope.record.transcriptionGroups.length; i++) {
      var group = $scope.record.transcriptionGroups[i];
      if (group.nodes.length == 0)
        continue;
      var index = lower.indexOf(group.key);
      if (index >= 0)
      {
        var key = desc.substring(index, index + group.key.length);
        temp.push({
          index: index,
          key: key,
          group: group
        });
        var placeholder = "";
        for (var k = 0; k < group.key.length; k++)
          placeholder +="#";
        var firstPart = desc.substring(0, index), 
          secondPart = desc.substring(index + group.key.length);
        desc = firstPart + placeholder + secondPart;
      }
    }
    var building = "";
    var links = [];
    for (var i = 0; i < desc.length; i++) {
      if (desc[i] === "#") {
        var match = temp.filter(function(x) { return x.index == i; });
        if (match.length > 0) {
          match = match[0];
          if (building.length > 0) 
            links.push({key: building});
          links.push({
            key: match.key,
            nodes: match.group.nodes,
            quantity: match.group.quantity,
            measure: match.group.measure
          });
        }
        building = "";
      } else {
        building += desc[i];
      }
    }
    if (building.length > 0)
      links.push({key: building});
    $scope.record.transcriptionGroups = links;
  };

  $scope.displayNTranscriptionGroups = function(groups, text) {
    // var links = [];
    // var desc = $scope.record.nTranscript;
    // for (var i = 0; i < $scope.record.nTranscriptionGroups.length; i++) {
    //   var index = desc.indexOf($scope.record.nTranscriptionGroups[i].key);
    //   if (index > 0)
    //     links.push({key: desc.substring(0, index)});
    //   links.push($scope.record.nTranscriptionGroups[i]);
    //   desc = desc.substring(index + $scope.record.nTranscriptionGroups[i].key.length, desc.length);
    //   desc = desc.trim();
    // }
    // if (desc.length > 0)
    //   links.push({key: desc});
    // $scope.record.nTranscriptionGroups = links;

    var desc = $scope.record.nTranscript;
    var temp = [];
    for (var i = 0; i < $scope.record.nTranscriptionGroups.length; i++) {
      var group = $scope.record.nTranscriptionGroups[i];
      var index = desc.indexOf(group.key);
      if (index >= 0)
      {
        temp.push({
          index: index,
          key: group.key,
          group: group
        });
        var placeholder = "";
        for (var k = 0; k < group.key.length; k++)
          placeholder +="#";
        var firstPart = desc.substring(0, index), 
          secondPart = desc.substring(index + group.key.length);
        desc = firstPart + placeholder + secondPart;
      }
    }
    var building = "";
    var links = [];
    for (var i = 0; i < desc.length; i++) {
      if (desc[i] === "#") {
        var match = temp.filter(function(x) { return x.index == i; });
        if (match.length > 0) {
          match = match[0];
          if (building.length > 0) 
            links.push({key: building});
          links.push(match.group);
        }
        building = "";
      } else {
        building += desc[i];
      }
    }
    if (building.length > 0)
      links.push({key: building});
    $scope.record.nTranscriptionGroups = links;
  };

  $scope.buildLink = function(id) {
    if (!$scope.record)
      return;
    var url = "#!image?id=" + ((id && id > 0) ? id : $scope.record.id);
    if ($scope.searchRecordType) url += '&recordType=' + $scope.searchRecordType;
    if ($scope.searchHousehold) url += ('&household=' + $scope.searchHousehold);
    if ($scope.searchStudy) url += ('&study=' + $scope.searchStudy);
    if ($scope.searchMember) url += ('&member=' + $scope.searchMember);
    if ($scope.searchRecipe) url += ('&recipe=' + $scope.searchRecipe);
    if ($scope.searchIdent) url += ('&identified=' + $scope.searchIdent); 
    if ($scope.searchPrt) url += ('&portioned=' + $scope.searchPrt);
    if ($scope.searchText) url += ('&search=' + $scope.searchText);
    if ($scope.searchCommentText) url += ('&comment=' + $scope.searchCommentText);
    if ($scope.searchOrder) url += ('&order=' + $scope.searchOrder);
    if ($scope.searchHidden) url += ('&hidden=' + $scope.searchHidden);
    if ($scope.gestaltVariation) url += ('&gv=' + $scope.gestaltVariation);
    return encodeURI(url);
  };
  
  $scope.tempArr = function(size) { return new Array(size); }
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

    $scope.record.comments.push(comment);
    $scope.commentIndex = $scope.record.comments.length-1;
    $scope.newComment = null;
    Restangular.one('ImageRecords/' + $scope.record.id + '/Comments').post('', comment).then(function(result) {
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
    Restangular.one('ImageRecords/' + $scope.record.id + '/Comments').post('', reply).then(function(result) {
      reply.id = result.data;
    }, function(result) {
      alert("Comment could not be uploaded: " + result.message)
    });
  };

  $scope.styleMeasure = function(item) {
    if (!item.createdByAdmin && $scope.gestaltVariation > 0 && item.gestaltDifference > $scope.gestaltVariation)
      return 'gestalt-variation';
    if (item.gestaltLock)
      return'gestalt-locked';
  };


  //Restangular.one('FoodCompositions/GetFoodCompositionDensities').get().then(function(record) {
  //  $scope.foodItemList = record.data;
  //});

  $scope.updateLabels = function() {
    //Set progress identifiers
    if ($scope.record.foodItems.length <= 0) {
      $scope.record.tagIdent = 'Not Started';
      $scope.record.tagPrt = 'Not Started';
    } else {
      $scope.record.tagIdent = 'In Progress';
      $scope.record.tagPrt = 'In Progress';
    }
    if ($scope.record.isCompleted)
      $scope.record.tagIdent = 'Completed';
    if (($scope.record.foodItems.length > 0 || $scope.record.isCompleted) && $scope.record.foodItems.filter(function(item) { return !item.quantityGrams || item.quantityGrams == 0 }).length == 0)
      $scope.record.tagPrt = 'Completed';

  };

  $scope.addParticipant = function(p) {
    Restangular.oneUrl("/ImageRecords/" + $scope.record.id + "/AddParticipant/" + p.id).get().then(function(result) {
      if (p.original)
        p.hidden = false;
      else {
        var newP = result.data.item1;
        $scope.record.participants.push(newP);
      }
      for (var i = 0; i < $scope.householdMembers.length; i++) {
        if ($scope.householdMembers[i].id == p.id) {
          $scope.householdMembers.splice(i, 1);
          break;
        }
      }
      $scope.record.guestInfo = result.data.item2;
    }, function(result) {
      alert(result.data)
    });
  }
  $scope.removeParticipant = function(p, index) {
    var pCount = 0;
    for (var i = 0; i < $scope.record.participants.length; i++) {
      var pCheck = $scope.record.participants[i];
      if (!pCheck.hidden)
        pCount++;
    }
    if (pCount == 1) {
      alert("Could not remove participant, there must be at least one assigned participant.");
      return;
    }

    Restangular.oneUrl("/ImageRecords/" + $scope.record.id + "/DeleteParticipant/" + p.id).customDELETE().then(function(result) {
      if (p.original)
        p.hidden = true;
      else
        $scope.record.participants.splice(index, 1);
      $scope.householdMembers.push(p);
      $scope.record.guestInfo = result.data;
    }, function(result) {
      alert(result.data.message)
    });
  };
  $scope.parseAge = function(age) {
    var str = Math.floor(age) + 'yr ';
    if (age % 1 != 0)
      str += Math.round((age % 1) * 12) + 'mo';
    return str;
  };
  $scope.toggleEditGuests = function() {
    if (!$scope.editGuests)
      $scope.editGuests = true;
    else
      $scope.editGuests = false;
  };
  $scope.toggleParticipantDialog = function() {
      $scope.showParticipantDialog = !$scope.showParticipantDialog;
  };

  $scope.complete = function() {
    for (var i = 0; i < $scope.record.foodItems.length; i++) {
      if ($scope.record.foodItems[i].unsaved) {
        alert("Cannot complete with unsaved food items");
        return;
      }
    }

    $scope.record.isCompleted = true;
    $scope.record.put().then(function(result) {
      $scope.updateLabels();
    }, function(error) {
      $scope.record.isCompleted = false;
      return;
    })
  };
  $scope.uncomplete = function() {
    $scope.record.isCompleted = false;
    $scope.record.put().then(function(result) {
      $scope.updateLabels();
    }, function(error) {
      $scope.record.isCompleted = true;
      return;
    });
  };
  $scope.saveRecord = function() {
    $scope.record.put().then(function(result) {
      return true;
    }, function(error) {
      return false;
    })
  };
  $scope.saveDescription = function() { $scope.record.textDescription = $scope.newTextDescription; $scope.saveRecord(); };
  $scope.saveManualTranscript = function() { $scope.record.manualTranscript = $scope.newManualTranscript; $scope.saveRecord(); };
  $scope.saveTranslation = function(text) {
    $scope.record.translation = text;
    $scope.saveRecord();
  };

  $scope.editNTranscript = function() {
    $scope.newNTranscriptText = $scope.record.nTranscript;
    $scope.record.nTranscript = null;
  };
  $scope.updateNTranscript = function(text) {
    if (text) {
      $scope.transcriptLoading = true;
      $scope.record.nTranscript = text;
      $scope.record.nTranscriptionGroups = null;

      Restangular.one('ImageRecords/' + $scope.record.id + '/Transcript').post('', '"' + text + '"').then(function(result) {
        $scope.record.nTranscriptionGroups = result.data;
        $scope.displayNTranscriptionGroups($scope.record.nTranscriptionGroups, $scope.record.nTranscript);
      }, function(result) {
      }).finally(function() { $scope.transcriptLoading = false; });
    }
  };

  $scope.editDescription = function() {
    $scope.newDescriptionText = $scope.record.textDescription;
    $scope.record.textDescription = null;
  };
  $scope.updateDescription = function(text) {
    //if (text) {
      $scope.textDescriptionLoading = true;
      $scope.record.textDescription = text;
      $scope.record.transcriptionGroups = null;
      //$scope.saveRecord();
    //}

    Restangular.one('ImageRecords/' + $scope.record.id + '/TextDescription').post('', '"' + text + '"').then(function(result) {
      $scope.record.transcriptionGroups = result.data;
      $scope.displayTranscriptionGroups($scope.record.transcriptionGroups, $scope.record.textDescription);
    }, function(result) {
    }).finally(function() { $scope.textDescriptionLoading = false; });
  };

  $scope.setChosen = function(index) {
    var select = $("#food-item-" + index);
    select.chosen({width: "100%",search_contains: true});

    return true;
  }
  $scope.addItem = function(id, quantity, qType) {
    if ($scope.record.isCompleted && !$scope.canAdmin) {
      alert("Record is tagged as completed, cannot add item. Please leave a flagged comment for an admin to review if you believe this is in error.")
      return;
    }
    if (!id) {
      var item = {imageRecordId: $scope.record.id, unsaved: true, dirty: true, createStart: new Date()};
      $scope.record.foodItems.push(item);
    }
    else {
      var fc = $scope.foodItemList.filter(function(food) { return food.id === id; })[0];
      var mes = $scope.measuresList.filter(function(food) { return food.id === id; })[0];
      // if (!fc || fc.length == 0)
      //   fc = $scope.
      if (fc) {
        var item = {imageRecordId: $scope.record.id, unsaved: true, dirty: true, foodCompositionId: fc.id, createStart: new Date()};
        if (quantity) {
          if (qType === 'g') {
            item.quantityGrams = quantity;
            item.measureType = 'Raw gram input (g)';
            item.measureEntry = item.measureCount = quantity;
          }
          else if (qType === 'mL') {
            if (mes.density) {
              item.quantityGrams = quantity * mes.density;
              item.measureType = 'Volume input (mL)';
              item.measureEntry = item.measureCount = quantity;
              item.measureMult = mes.density
            }
          }
        }
        $scope.record.foodItems.push(item);
        $scope.changeFoodItem($scope.record.foodItems.length-1);
      }
    }

    setTimeout(function() {
      var id = "#food-item-" + ($scope.record.foodItems.length - 1)
      var s = $(id);
      s.trigger('chosen:open');
    }, 500);

    //$scope.record.isCompleted = false;
    $scope.updateLabels();
  };
  $scope.addSuggestion = function(index) {
    var sugg = $scope.record.suggestions[index];
    if (sugg) {
      // var item = {
      //   imageRecordId: $scope.record.id,
      //   unsaved: true,
      //   dirty: true,
      //   createStart: new Date(),
      //   foodCompositionId: sugg.foodCompositionId
      // };
      // $scope.record.foodItems.push(item);

      $scope.addItem(sugg.foodCompositionId, 0);

      $scope.record.suggestions.splice(index, 1);
    }
  };
  $scope.toggleDisplayImage = function(url) {
    $scope.displayImage = url;
  };
  $scope.toggleDisplayHistory = function(foodItem) {
    if (!foodItem) {
      $scope.history = $scope.record.updates;
      $scope.historyFoodItem = null;
    } else {
      $scope.history = $scope.record.updates.filter(function(x) { return x.foodItemId === foodItem.id; });
      $scope.historyFoodItem = foodItem;
    }
    $scope.displayHistory = !$scope.displayHistory;
  };
  $scope.getFoodCompositionFromId = function(id) {
    if (!$scope.measuresList)
      return;
    for (var i = $scope.measuresList.length - 1; i >= 0; i--) {
      if ($scope.measuresList[i].id === id)
        return $scope.measuresList[i].name;
    }
    return "unknown item";
  };
  $scope.getColorForFoodItem = function(id) {
    for (var i = $scope.record.foodItems.length - 1; i >= 0; i--) {
      if ($scope.record.foodItems[i].id === id)
        return $scope.colors[i];
    }
    return "#000000"
  };
  $scope.undeleteItem = function(foodCompositionId, quantityGrams, toolMeasure, toolSource) {
    if (!confirm("Would you like to re-add this item: " + quantityGrams.toFixed(2) + "g " + $scope.getFoodCompositionFromId(foodCompositionId)))
      return;
    $scope.displayHistory = false;
    var item = {imageRecordId: $scope.record.id, unsaved: true, dirty: true, foodCompositionId: foodCompositionId, createStart: new Date(), quantityGrams: quantityGrams, measureCount: toolMeasure, measureType: toolSource};

    $scope.record.foodItems.push(item);
    $scope.changeFoodItem($scope.record.foodItems.length-1);
  };
  $scope.deleteItem = function(index) {
    $scope.message = null;
    var item = $scope.record.foodItems[index];
    if (item.id > 0) {
      if (!confirm("Are you sure you want to delete this item?"))
        return;
      Restangular.allUrl('FoodItems/' + item.id).customDELETE().then(function() {
        $scope.record.foodItems.splice(index, 1);
      }, function(result) { 
        $scope.message = {success:false,txt:"Could not delete item."};
      });
    } else {
      $scope.record.foodItems.splice(index, 1);
    }
  };
  $scope.dirtyItem = function(item) {
    item.dirty = true;
  };
  $scope.styleSaveAll = function() {
    if (!$scope.record || !$scope.record.foodItems)
      return;
    var unsaved = $scope.record.foodItems.filter(function(x) { return x.dirty; });
    var color = unsaved.length > 0 ? "#295173" : "#ababab";
    return {color: color};
  };

  $scope.movePage = function(event) {
    var unsaved = $scope.record.foodItems.filter(function(x) { return x.dirty; });
    if (unsaved.length > 0 && !confirm("There are unsaved food items. Are you sure you want to leave this page?")) {
      event.stopPropagation();
      event.preventDefault();
      return;
    }
    var replies = $scope.record.comments.filter(function(x) { return x.replyText; });
    if ((replies.length > 0 || ($scope.newComment && $scope.newComment.text)) && !confirm("There is an unsaved comment. Are you sure you want to leave this page?")) {
      event.stopPropagation();
      event.preventDefault();
      return;
    }

    window.scrollTo(0, 0);
  };

  function lcs(string1, string2) {
    var options = {s1: string1, s2: string2, longest: '', s1idx: -1, s2idx: -1};
    lcs_run(options);
    return options;
  }
  function lcs_run(options, index = 0, length = 1) {
    //Find the longest common substring
    if (index + length > options.s1.length)
      return;
    var str = options.s1.substring(index, index + length);
    var matchIndex = options.s2.indexOf(str);
    if (matchIndex >= 0) {
      if (str.length > options.longest.length) {
        options.longest = str;
        options.s1idx = index;
        options.s2idx = matchIndex;
      }
      lcs_run(options, index, length + 1);
    } else
      lcs_run (options, index + Math.max((length - 1), 1), 1);

    return options;
  }

  var charRegex = /[(),./"']/;
  //var charRegex = /[^A-Za-z ]/;
  function ratcliffMatch(string1, string2) {
    // Basic implementation of Ratcliff/Obershelp string matching algorithm. Splits a string on the longest matching string, then recursively does the same to the right and left segments.
    var options = {matches: [], score: 0}
    string1 = string1.toLowerCase().replaceAll(charRegex, '');
    string2 = string2.toLowerCase().replaceAll(charRegex, '');

    ratcliffMatch_run(string1, string2, options);

    return options;
  }

  var ratCliffMinimumLength = 2;
  function ratcliffMatch_run(string1, string2, options, modifier = 1) {
    // Basic implementation of Ratcliff/Obershelp string matching algorithm. Splits a string on the longest matching string, then recursively does the same to the right and left segments.
    if (string1.length == 0 || string2.length == 0)
      return options;
    var match = lcs(string1, string2);
    if (match.s1idx < 0)
      return options;

    if (match.longest.length <= ratCliffMinimumLength)
      return options;
    options.matches.push(match);
    options.score += match.longest.trim().length * modifier;

    var s1h1 = string1.substring(0, match.s1idx), s1h2 = string1.substring(match.s1idx + match.longest.length, string1.length),
      s2h1 = string2.substring(0, match.s2idx), s2h2 = string2.substring(match.s2idx + match.longest.length, string2.length);

    modifier -= 0.1;
    ratcliffMatch_run(s1h1, s2h1, options, modifier);
    ratcliffMatch_run(s1h2, s2h2, options, modifier);

    return options;
  }

  
  $scope.matchExampleMeasure = function(item) {
    if (!item.measure || !$scope.exampleMeasures)
      return;

    var name = item.measure.name;
    var match = {score: -1}, matchIndex = -1;
    for (var i = 0; i < $scope.exampleMeasures.length; i++) {
      var testMatch = ratcliffMatch($scope.exampleMeasures[i].name, name);
      if (testMatch.score > match.score) {
        matchIndex = i;
        match = testMatch;
      }
    }
    if (matchIndex >= 0) {
      item.exampleMeasures = $scope.exampleMeasures[matchIndex];
      item.exampleMeasureId = $scope.exampleMeasures[matchIndex].id;
    }
  }

  $scope.saveItem = function(index) {
    $scope.message = null;
    var item = $scope.record.foodItems[index];

    item.createEnd = new Date();
    if (!item.createStart) {
      item.createStart = $scope.startTime;
      $scope.startTime = new Date();
    }
    if (!item.quantityGrams)
      item.quantityGrams = 0;
    if (item.measureEntry && item.measureMult) {
      var elt = document.getElementById("measureSelect-" + index);
      if (elt && elt.selectedIndex != -1) {
        item.measureType = elt.options[elt.selectedIndex].text;
        item.measureCount = item.measureEntry;
      }
    }

    item.dirty = false;
    if (item.id > 0) {
      Restangular.allUrl('FoodItems/' + item.id).customPUT(item).then(function() {
        $('#food-item-tr-' + index).addClass('pulse-success').one('webkitAnimationEnd...', function(){$(this).removeClass('pulse-success')});
        item.unsaved = false;
        $scope.updateLabels();
      }, function(result) {
        item.dirty = true;
        $('#food-item-tr-' + index).addClass('pulse-fail').one('webkitAnimationEnd...', function(){$(this).removeClass('pulse-fail')});
        $scope.message = {success:false,txt:"Could not save item."};
      });
    } else if (item.foodCompositionId || item.tagXPercent) {
      Restangular.one('FoodItems').post('', item).then(function(result) {
        $('#food-item-tr-' + index).addClass('pulse-success').one('webkitAnimationEnd...', function(){$(this).removeClass('pulse-success')});
        item.unsaved = false;
        item.id = result.data.id;
        $scope.updateLabels();
      }, function(result) {
        item.dirty = true;
        $('#food-item-tr-' + index).addClass('pulse-fail').one('webkitAnimationEnd...', function(){$(this).removeClass('pulse-fail')});
        $scope.message = {success:false,txt:"Could not save item."};
      });
    }
     else {
      alert("Food item must be identified through name or tag to save.");
     }
  };
  $scope.saveAllItems = function() {
    for (var i = 0; i < $scope.record.foodItems.length; i++) {
      if ($scope.record.foodItems[i].dirty)
        $scope.saveItem(i);
    }
  };
  $scope.unsaveItem = function(index) {
    var item = $scope.record.foodItems[index];
    item.unsaved = true;
    // elem = $("#food-item-" + (index + 1));
    // elem.chosen({width: "100%",search_contains: true});
  };

  $scope.flagFoodItem = function(item) {
    if (!confirm("This feature will flag this food item as incorrect for review by a coordinator. If you are experiencing difficulty identifying the image or would like to leave a more detailed message, use the help (?) button. Are you sure you would like to flag this food item?"))
      return;
    Restangular.oneUrl("/FoodItems/" + item.id + "/Flag").get().then(function(result) {

    }, function(result) {
      alert(result.data.message)
    });
  };

  $scope.startDisplayMeasures = function(item, index) {
    if (item) {
      $scope.displayMeasures = item;
      $scope.exampleMeasureId = item.exampleMeasureId;
      $scope.displayMeasureIndex = 0;
      $scope.displayMeasureItemIndex = index;

      //$scope.changeExampleMeasure(item.exampleMeasureId, item);
    }
  };
  $scope.endDisplayMeasures = function(quantity) {
    if ($scope.displayMeasures && quantity && $scope.canQuantify) {
      $scope.displayMeasures.quantityGrams = quantity;
      $scope.displayMeasures.dirty = true;

      $scope.displayMeasures.measureType = 'Raw gram input (g)';
      $scope.displayMeasures.measureEntry = quantity;

      var elt = document.getElementById("measureSelect-" + $scope.displayMeasureItemIndex);
      $scope.changeMeasure($scope.displayMeasures, elt);
      $scope.changeMeasureType($scope.displayMeasures, 0);
    }
    $scope.displayMeasures = null;
  };
  $scope.changeExampleMeasure = function(id, item) {
    if (!id)
      return;
    var example = $scope.exampleMeasures.filter(function(x) { return x.id == id; })[0];
    item.exampleMeasures = example;
  };
  $scope.styleMeasureImage = function() {
    var h = document.getElementById("image");
    if (!h)
      return { height: 460};
    return {height: h.height};
  };
  $scope.changeMeasureIndex = function(index) {
    $scope.displayMeasureIndex = index;
  };

  $scope.clearTag = function(item) {
    delete item.tagXPercent;
    delete item.tagYPercent;
  };
  $scope.styleTag = function(item) {
    var image = $("#image")[0];
    if (!image)
      return {display:'none'};
    var ox = image.offsetLeft, oy = image.offsetTop;
    var parElem = image.offsetParent;
    while (parElem) {
      ox += parElem.offsetLeft
      oy += parElem.offsetTop;
      parElem = parElem.offsetParent;
    }
    var x = ox + (image.width * item.tagXPercent);
    var y = oy + (image.height * item.tagYPercent);
    var display = (item.tagXPercent && item.tagXPercent) > 0 ? 'block' : 'none';
    return {left: x, top: y, display: display};
  };
  $scope.styleGraph = function(item) {
    var image = $("#image")[0];
    return {left:image.offsetLeft+15,top:image.offsetTop,width:image.width,height:image.innerHeight};
  };

  $scope.saveText = function() {
    var text = {eatImageRecordId: $scope.record.id, description: $scope.newText};
    Restangular.oneUrl('EatImageRecords/EatImageText').post('', text)
    .then(function(result) {
      $scope.newText = null;
      $scope.record.eatImageTexts.push(text);
    });
    /*, function(result) {
      if (!confirm("Your message failed to upload. Would you like to try again?"))
        $scope.newText = null;
    });*/
    
  };
  function parseMeasures(str) {
    var measures = [];
    if (!str)
      return measures;
    var itms = str.split('|');
    for (var i = 0; i < itms.length; i++) {
      var itm = itms[i].split(':');
      if (itm.length == 2) {
        measures.push({name: itm[0], value: itm[1]});
      }
    }
    return measures;
  }

  $scope.changeFoodItem = function(index) {
    var item = $scope.record.foodItems[index];

    if (item.dirty === undefined || item.dirty === null)
      item.dirty = false;
    else
      item.dirty = true;

    item.recipeId = null;
    item.standardMeasures = $scope.standardMeasures.filter(() => true);
    var mes = $scope.measuresList.filter(function(food) { return food.id === item.foodCompositionId; })[0];
    if (mes) {
      item.measure = mes;
      item.measure.measureList = parseMeasures(mes.measures);
      for (var i = item.measure.measureList.length - 1; i >= 0; i--) {
        var m = item.measure.measureList[i];
        for (var j = item.standardMeasures.length - 1; j >= 0; j--) {
          if (item.standardMeasures[j].name === m.name) {
            item.standardMeasures.splice(j, 1);
            break;
          }
        }
      }
    } else {
      mes = $scope.record.householdRecipeNames.filter(function(x) { return x.id === item.foodCompositionId; })[0];
      if (mes) {
        item.measure = mes;
        item.recipeId = mes.recipeId;
      }
    }

    $scope.matchExampleMeasure(item);

    setTimeout(function() {
      var elt = document.getElementById("measureSelect-" + index);
      if (item.gestaltLock) {
        elt.options.length = 0;
        var opt = document.createElement('option');
        opt.appendChild(document.createTextNode('Gestalt max reached'));
        opt.value = 1;
        elt.appendChild(opt);
        elt.selectedIndex = 0;
      }
      if (item.measureType && item.measureType === 'Gestalt estimation (g)') {
        var opt = document.createElement('option');
        opt.appendChild(document.createTextNode('Gestalt estimation (g)'));
        opt.value = 1;
        elt.appendChild(opt);
      }
      
      $scope.changeMeasure(item, elt);

      $scope.$apply();
    }, 500);
  };

  $scope.changeMeasure = function(item, elt) {
    if (item.measureType && item.measureCount) {
        var found = false;
        for (var i = 0; i < elt.length; i++) {
          if (elt[i].text === item.measureType) {
            found = true;
            elt.selectedIndex = i;
            if (!item.measureEntry)
              item.measureEntry = item.measureCount;
            item.measureMult = elt[i].value;
            item.measureEntry = +item.measureEntry.toFixed(2);
            item.quantityGrams = item.measureEntry * item.measureMult;
            item.quantityGrams = +item.quantityGrams.toFixed(2);
            break;
          }
        }
        if (!found) {
          // elt.selectedIndex = 1;
          item.measureMult = '1';
          item.measureType = 'Raw gram input (g)';
          item.measureCount = item.quantityGrams;
          item.measureEntry = item.quantityGrams.toFixed(2);
          found = false;
        }
      }
  }

  $scope.changeMeasureType = function(item, index) {
    item.dirty = true;
    // if (item.measureMult == 0) {
    //   item.quantityGrams = 0;
    //   item.measureEntry = 0;
    //   item.measureCount = 0;
    //   item.measureType = 'Use wiremesh app';
    //   return;
    // }
    if (item.measureMult) {
      var elt = document.getElementById("measureSelect-" + index);
      if (elt && elt.selectedIndex != -1) {
        item.measureType = elt.options[elt.selectedIndex].text;
        item.measureCount = item.measureEntry;
      }
    }
    if (item.quantityGrams > 0 && item.measureMult) {
      item.measureEntry = +(item.quantityGrams / parseFloat(item.measureMult)).toFixed(2);
    }
  };
  $scope.changeQuantity = function(item) {
    item.dirty = true;
    item.measureCount = item.measureEntry;
    if (item.measureEntry > 0) {
      var b = (item.measureEntry * parseFloat(item.measureMult));
      var q = +b.toFixed(2);
      item.quantityGrams = q;
    } else {
      item.quantityGrams = 0;
    }
  };
  $scope.changeRawGrams = function(item) {
    item.dirty = true;
    if (item.quantityGrams > 0) {
      var b = (item.quantityGrams / parseFloat(item.measureMult));
      var q = +b.toFixed(2);
      item.measureEntry = q;
    } else {
      item.measureEntry = 0;
    }
  };
  

  $scope.showHelp = function(item) {
    $scope.helpContextItem = item;
    $scope.showHelpDialog = true;
  };
  $scope.addHelpComment = function(text, detailPrompt) {
    if ($scope.helpContextItem) {
      if (!$scope.helpContextItem.id) {
        alert('Item must be saved before asking for assistance');
        return;
      }
      text += $scope.helpContextItem.id;
    }
    $scope.addAdminRequest(text, detailPrompt);
  };
  $scope.addAdminRequest = function(text, detailPrompt) {
    if (detailPrompt){
      var details = prompt(detailPrompt);
      if (!details)
        return;
      text += "\n" + details;
    }
    $scope.addComment({ text: text, highPriority: true, flag: 'normal', authorName: 'Me' });
    $scope.showHelpDialog = false;
  };
  $scope.writeAdminRequest = function() {
    $scope.newComment = {authorName: 'Me', text: '', highPriority: true, flag: 'normal'};
    $scope.showHelpDialog = false;
    $('#commentInput').addClass('pulse-success').one('webkitAnimationEnd...', function(){$(this).removeClass('pulse-success')});
  };

  $scope.startEditTime = function() {
    var time = new Date($scope.record.captureTime);
    time.setSeconds(0);
    $scope.editTimeTime = time;
    $scope.editTimeDate = new Date($scope.record.captureTime);
    $scope.displayEditTime = true;
  };
  $scope.saveEditTime = function(time, date) {
    var tStr = formatDateString(time);
    var dStr = formatDateString(date);
    var ns = dStr.substring(0, 10) + tStr.substring(10);

    $scope.record.captureTime = ns;
    $scope.displayEditTime = false;
    $scope.saveRecord();
    // Restangular.oneUrl('EatImageRecords/EatImageText').post('', text)
    // .then(function(result) {
    //   $scope.newText = null;
    //   $scope.record.eatImageTexts.push(text);
    // });
  };

  $scope.onResize = function(event) {
    return event.target.innerWidth;
  };

  $scope.$watch('width', function(old, newv){
    //console.log(old, newv); //USE THIS TO DO SOMETHING WHEN THE SCREEN SIZE CHANGES
  })

  $('#guestForm').submit(function(e) {
      e.preventDefault();
      // Coding
      $('#guestModal').modal('toggle'); //or  $('#IDModal').modal('hide');
  });
}]);

function between(chk, first, second, inclusive) {
  inclusive = inclusive || false;
  if (inclusive)
    return (chk && chk >= first && chk <= second);
  else
    return (chk && chk > first && chk < second);

}

angular.module('visida_cms').directive('resize', ['$window', function ($window) {
  return {
    link: link,
    restrict: 'A'
  };

  function link(scope, element, attrs){
    scope.width = $window.innerWidth;
    function onResize(){
        // uncomment for only fire when $window.innerWidth change   
        // if (scope.width !== $window.innerWidth)
        {
            scope.width = $window.innerWidth;
            scope.$digest();
        }
    };

    function cleanUp() {
        angular.element($window).off('resize', onResize);
    }

    angular.element($window).on('resize', onResize);
    scope.$on('$destroy', cleanUp);
  }
}]);

app.directive('tag', ['$timeout', function ($timeout) {
    return function (scope, element, attrs) {
      $timeout(function() {
        var trigger = document.getElementById(element[0].id);
        //if (e)
          //scope.dragElement(trigger);
        if (trigger) {// && scope.imageUrl) {
          trigger.onmousedown = function(e) {
            scope.mag.setPause(true);
            var elmnt;
            if (e.currentTarget.id.includes('item'))
              elmnt = document.getElementById(e.currentTarget.id);
            else
              elmnt = document.getElementById(e.currentTarget.id + "-item");
            var index = parseInt(e.currentTarget.id.substring(0, 1));
            var x, y;
            elmnt.style.left = e.pageX + "px";
            elmnt.style.top = e.pageY + "px";
            elmnt.style.display = 'block';

            document.onmouseup = function() {
              document.onmouseup = null;
              document.onmousemove = null;
              scope.mag.setPause(false);

              var image = $("#image")[0];
              var ox = image.offsetLeft, oy = image.offsetTop;
              var parElem = image.offsetParent;
              while (parElem) {
                ox += parElem.offsetLeft
                oy += parElem.offsetTop;
                parElem = parElem.offsetParent;
              }
              if (between(x, ox, ox + image.width) && between(y, oy, oy + image.height)) {
                var tx = (x - ox) / image.width;
                var ty = (y - oy) / image.height;

                var item = scope.record.foodItems[index];
                item.tagXPercent = tx;
                item.tagYPercent = ty;
                item.dirty = true;
                scope.$apply();
              } else {
                var item = scope.record.foodItems[index];
                item.dirty = true;
                elmnt.style.display = 'none';
                delete item.tagXPercent;
                delete item.tagYPercent;
                scope.$apply();
              }
            };

            document.onmousemove = function(e) {
              e = e || window.event;
              e.preventDefault();
              x = e.pageX;
              y = e.pageY;
              elmnt.style.left = x + "px";
              elmnt.style.top = y + "px";
            };
          };

          window.onresize = function() {
            
          }
        }
        return;
      });
    };
}]);

app.directive('imageonload', function() {
    return {
        restrict: 'A',
        link: function(scope, element, attrs) {
            element.bind('load', function() {
                scope.$apply();
            });
        }
    };
});

app.directive("helpDialog", function() {
  return {
    restrict: 'E',
    scope:false,
    templateUrl: 'views/image/helpDialog.html'
  }
})

var urlRegex = /https:\/\/visida.newcastle.edu.au\/#!\/[^ ]*/gi,
  idRegex = /(?<=\?id=)[0-9]*/g,
  tagRegex = /id: [0-9]*/g,
  atRegex = /@[^\s]*/g;


app.directive('myPostRepeatDirective', ['$compile', function($compile) {
  return function(scope, element, attrs) {
    window.setTimeout(function(){
      var comment = null;
      if (scope.record)
        comment = scope.record.comments[scope.$index];
      else
        comment = scope.recipe.comments[scope.$index];
      var elem = document.getElementById("comment-text-" + scope.$index);
      var html = '';

      if (comment.flag === 'Task') {
        comment.isTaskCompleted = comment.taskCompleted != null;
        html += '<span class="comment-task-header"><span class="glyphicon glyphicon-edit" style="font-weight: lighter; padding-right:5px;"></span> Task: ';
        if (comment.highPriority)
          html += '<span class="glyphicon glyphicon-warning-sign text-danger" style="font-weight: lighter; padding-left:5px;"></span>';
        html += '</span><br/>';

        html += '<span ng-class="styleComment(comment)">'
        html += comment.text;
        html += '</span><br/>';

        html += '<span class="subscript">created by ' + comment.authorName + '</span>';
      } else {
        html += '<span class="bold">' + comment.authorName + ': </span>';
        if (comment.highPriority)
          html += '<span class="glyphicon glyphicon-warning-sign text-danger" style="font-weight: lighter; padding-left:5px;"></span>';

        html += '<span ng-class="styleComment(comment)">'
        html += comment.text;
        html += '</span>';
      }

      if (scope.record) {
        var tagMatches = comment.text.match(tagRegex);
        if (tagMatches) {
          for (var i = 0; i < tagMatches.length; i++) {
            var t = tagMatches[i];
            var id = parseInt(t.substring(t.indexOf(' ')));

            for (var j = 0; j < scope.record.foodItems.length; j++) {
              if (id === scope.record.foodItems[j].id) {
                var color = j;
                var span = '<span class="glyphicon glyphicon-tag" style="color: ' + scope.colors[color] + '"> </span>';
                html = html.replace(t, span);
                break;
              }
            }
          }
        }
      }

      var urlMatches = comment.text.match(urlRegex);
      if (urlMatches) {
        for (var i = 0; i < urlMatches.length; i++) {
          var m = urlMatches[i];
          var rMatch = m.match(idRegex);
          var rid = rMatch ? ' ' + rMatch[0] : '';

          var span = '<a href="' + m + '">record' + rid + '</a>';
          html = html.replace(m, span);
        }
      }

      var atMatches = comment.text.match(atRegex);
      if (atMatches) {
        for (var i = 0; i < atMatches.length; i++) {
          var m = atMatches[i];

          var span = '<span style="color: #e74c3c; font-weight: bolder;">' + m + '</span>';
          html = html.replace(m, span);
        }
      }

      elem.innerHTML = html;
      $compile(elem)(scope);
    }, 0);
  };
}]);