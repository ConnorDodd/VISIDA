angular.module('visida_cms').controller('uploadController', ['$scope', 'Restangular', 'Upload', 'configSettings', function($scope, Restangular, Upload, configSettings) {

  $scope.loading = 1;
  $scope.hasData = false;

  Restangular.all('Studies').getList().then(function(result) {
    $scope.studies = result.data;
  }, function errorCallback() {
    $scope.studies = new Array();
    $scope.studies.push({id: -1, name: "ERROR loading studies."});
  }).finally(function() { $scope.loading--; });

  $scope.filesChosen = function(files) {
    $scope.hhData = null;
    $scope.uploading = false;
    $scope.progress = 0;
    var hasZip = files.filter(function(x) {return x.name.endsWith(".zip");}).length > 0;
    if (hasZip) {
      alert("Cannot upload ZIP files. If the ZIP contains household data, please unpack it first and then upload.");
      $scope.files = null;
      return;
    }

    var data = files.filter(function(x) { return x.name.endsWith(".json"); });
    if (data.length > 1) {
      alert("Cannot upload more than one household data at once. Please only include one database.json file at a time.");
      $scope.files = null;
      return;
    }
    data = data[0];
    if (data) {
      $scope.hasData = true;
      var reader = new FileReader();
      reader.readAsText(data, "UTF-8");
      $scope.loading++;
      reader.onload = function() {
        $scope.hhData = JSON.parse(reader.result);
        var usage = {days:[], files: 1, images: 0, audio: 0};
        for (var i = $scope.hhData.householdMembers.length - 1; i >= 0; i--) {
          var hm = $scope.hhData.householdMembers[i];
          for (var j = hm.foodRecords.length - 1; j >= 0; j--) {
            var fr = hm.foodRecords[j];
            var date = new Date(Date.parse(fr.date));
            date.setHours(0); date.setMinutes(0); date.setSeconds(0); date.setMilliseconds(0);
            var day = usage.days.filter(function(x) {return x.date.getTime() == date.getTime() })[0];
            if (!day){
              day = {date: date, eatingOccasions: 0, foodItems: 0, files: 0, images: 0, audio: 0};
              usage.days.push(day);
            }
            for (var k = fr.eatingOccasions.length - 1; k >= 0; k--) {
              var eo = fr.eatingOccasions[k];
              day.eatingOccasions++;
              for (var l = eo.foodItems.length - 1; l >= 0; l--) {
                var fi = eo.foodItems[l];
                day.foodItems++;
                if (fi.imageUrl) { day.files++; day.images++; usage.files++; usage.images++; }
                if (fi.audioUrls) { day.files++; day.audio++; usage.files++; usage.audio++; }
              }
            }
          }
        }
        for (var i = $scope.hhData.householdRecipes.length - 1; i >= 0; i--) {
          var hr = $scope.hhData.householdRecipes[i];
          if (hr.finalImageUrl) { day.files++; day.images++; usage.files++; usage.images++; }
          if (hr.recipeNameAudioUrl) { day.files++; day.audio++; usage.files++; usage.audio++; }
          for (var j = hr.ingredients.length - 1; j >= 0; j--) {
            var hri = hr.ingredients[j];
            if (hri.imageUrl) { day.files++; day.images++; usage.files++; usage.images++; }
            if (hri.audioUrl) { day.files++; day.audio++; usage.files++; usage.audio++; }
          }
        }

        usage.days.sort(function (a, b) {
          return a.date.getTime() - b.date.getTime();
        });
        $scope.usage = usage;
        //$scope.hhDataText = JSON.stringify($scope.hhData, null, 2);
        $scope.loading--;
        $scope.$apply();
      };
    }

    $scope.uploadFiles = function(files) {
      $scope.uploading = true;
      $scope.failures = 0;
      for (var i = 0; i < files.length; i++) {
        if (files[i].success > 0) {
          files.splice(i, 1);
          i--;
        } else {
          files[i].success = 0;
        }
      }

      $scope.progress = 1;
      var dataFile = files.filter(function(x) { return x.name.endsWith(".json"); })[0];
      if (dataFile && $scope.hhData) {
        $scope.hhData.studyId = $scope.studyId;

        Restangular.oneUrl('Households').post('', $scope.hhData).then(
          function(result) {
            dataFile.success = 1;
            $scope.up(0);
          }, function(result) {
            $scope.uploading = false;
            $scope.progress = null;
            alert("Household data failed to upload. Error: " + (result.data ? result.data.message : result.message));
        });
      } else {
        $scope.up(0);
      }
    };

    $scope.up = function(index) {
      if (index >= $scope.files.length) {

        return;
      }
      var file = $scope.files[index];
      if (file.type === 'application/json') {
        $scope.progress = Math.ceil((100 / $scope.files.length) * (index + 1));
        $scope.up(index + 1);
        return;
      }
      file = Upload.rename(file, file.name);
      var url = configSettings.baseUrl + '/Upload';
      if (file.type.startsWith('image'))
        url += '/PostImageRecord';
      else if (file.type.startsWith('audio'))
        url += '/PostAudioRecord';
      else
        url += '/PostLogFile';
      url += '?overwrite=' + ($scope.overwrite ? 'true' : 'false');

      Upload.upload({
        url: url,
        data: {file: file}
      }).then(function(result) {
        $scope.progress = Math.ceil((100 / $scope.files.length) * (index + 1));
        file.success = 1;
        $scope.up(index + 1);
      }, function(result) {
        $scope.progress = Math.ceil((100 / $scope.files.length) * (index + 1));
        file.success = -1;
        file.message = result.data;
        $scope.failures++;
        $scope.up(index + 1);
      });
    };

    $scope.clearFiles = function() {
      $scope.progress = 0;
      $scope.files = null;
      $scope.hhData = null;
      $scope.uploading = false;
      $scope.failures = 0;
    };
  };
}]);

angular.module('visida_cms').controller('recallController', ['$scope', 'Restangular', 'Upload', 'configSettings', '$window', function($scope, Restangular, Upload, configSettings, $window) {
  $scope.loading = 1;
  $scope.hasData = false;

  Restangular.all('Studies').getList().then(function(result) {
    $scope.studies = result.data;
  }, function errorCallback() {
    $scope.studies = new Array();
    $scope.studies.push({id: -1, name: "ERROR loading studies."});
  }).finally(function() { $scope.loading--; });

  $scope.parse = function(file) {
    if (!file) return;
    var reader = new FileReader();
    if (reader.readAsBinaryString) {
        reader.onload = function (e) {
            $scope.processExcel(e.target.result);
        };
        reader.readAsBinaryString(file);
    } else
      alert("This browser is not capable of parsing xlsx records.");
  };

  /**
   * Adds time to a date. Modelled after MySQL DATE_ADD function.
   * Example: dateAdd(new Date(), 'minute', 30)  //returns 30 minutes from now.
   * https://stackoverflow.com/a/1214753/18511
   * 
   * @param date  Date to start with
   * @param interval  One of: year, quarter, month, week, day, hour, minute, second
   * @param units  Number of units of the given interval to add.
   */
  function dateAdd(date, interval, units) {
    if(!(date instanceof Date))
      return undefined;
    var ret = new Date(date); //don't change original date
    var checkRollover = function() { if(ret.getDate() != date.getDate()) ret.setDate(0);};
    switch(String(interval).toLowerCase()) {
      case 'year'   :  ret.setFullYear(ret.getFullYear() + units); checkRollover();  break;
      case 'quarter':  ret.setMonth(ret.getMonth() + 3*units); checkRollover();  break;
      case 'month'  :  ret.setMonth(ret.getMonth() + units); checkRollover();  break;
      case 'week'   :  ret.setDate(ret.getDate() + 7*units);  break;
      case 'day'    :  ret.setDate(ret.getDate() + units);  break;
      case 'hour'   :  ret.setTime(ret.getTime() + units*3600000);  break;
      case 'minute' :  ret.setTime(ret.getTime() + units*60000);  break;
      case 'second' :  ret.setTime(ret.getTime() + units*1000);  break;
      default       :  ret = undefined;  break;
    }
    return ret;
  }

  var baseDate = new Date("1899/12/30");//new Date(1899, 12, 30);
  var test = new Date(baseDate.getTime());
  var baseTime = baseDate.getTime();
  function parseAccessDate(add) {
    // var days = Math.trunc(add);
    var time = add * 24 * 60 * 60;// * 1000;
    //time += baseTime;
    //time -= 3600000; //Have to take an hour away for some reason
    //return new Date(time);
    var ret = dateAdd(baseDate, 'day', add);
    return ret;
  };
  function parseAccessTime(add) {
    if (!add)
      return 0;
    var str = add + '';
    var idx = str.indexOf('.');
    if (idx < 0)
      return 0;
    var time = parseFloat('0.' + str.substring(idx+1));
    return time * 24 * 60 * 60;// * 1000;
  };
  $scope.showHousehold = function(hh) {
    var s = hh.show;
    for (var i = 0; i < $scope.households.length; i++) {
      $scope.households[i].show = false;
    }
    hh.show = !s;
  };

  $scope.upload = function(hh) {
    if (!confirm("24HR data will not be able to be re-uploaded to fix any mistakes. Please ensure your data is well formatted before uploading. Are you sure you want to continue?"))
      return;
    $scope.loading++;
    Restangular.oneUrl('Recall/' + $scope.studyId).post('', $scope.households).then(
    function(result) {
      $scope.households = null;
      $window.location.href = "#!/records?study=" + $scope.studyId;
    }, function(result) {
      $scope.uploadMessage = { success: false, txt: "Could not upload households. " + result.data.message };
    }).finally(function() { $scope.loading--; });
  };

  $scope.processExcel = function(data) {
    //Read the Excel File data.
    var workbook = XLSX.read(data, {
      type: 'binary'
    });

    //Fetch the name of First Sheet.
    var firstSheet = workbook.SheetNames[0];
    //Read all rows from First Sheet into an JSON array.
    var excelRows = XLSX.utils.sheet_to_row_object_array(workbook.Sheets[firstSheet]);

    if (excelRows.length <= 0 || !(excelRows[0]["24RID"])) {
      alert("This 24hr recall data is in an incorrect format.");
      return;
    }

    var newRecipeId = 0;
    var households = [];
    for (var i = 0; i < excelRows.length; i++) {
      var row = excelRows[i];
      var hh = households.filter(function(x) { return x.participantId === row.HHID; })[0];
      if (!hh) {
        hh = {
          participantId: row.HHID,
          householdMembers: [],
          householdRecipes: []
        };
        households.push(hh);
      }

      var hm = hh.householdMembers.filter(function(x) { return x.participantHouseholdMemberId === row.ParticipantID})[0];
      if (!hm) {
        hm = {
          participantHouseholdId: row.HHID,
          participantHouseholdMemberId: row.ParticipantID,
          age: row.Age,
          isFemale: row.Gender === 'Female',
          foodRecords: []
        };
        hh.householdMembers.push(hm);
      }

      var date = parseAccessDate(row.Date24RCollectedfor);
      var fr = hm.foodRecords.filter(function(x) { return x.dateObj.getTime() === date.getTime()})[0];
      if (!fr) {
        fr = {
          date: formatDateString(date),
          dateObj: date,
          eatingOccasions: []
        };
        fr.eatingOccasions.push({
          startTime: formatDateString(date), 
          endTime: formatDateString(new Date(date.getTime() + 8.64e+6)),
          finalized: true,
          foodItems: [],
          recipeIds: []
        });
        hm.foodRecords.push(fr);
      };

      var eo = fr.eatingOccasions[0];

      for (var j = 1; j < 19; j++) {
        var item = "Item" + j + "-";
        var name = row[item + 'QuickListName'];
        if (!name)
          continue;
        var a = row[item + 'TimeConsumed'];
        var b = parseAccessTime(a);
        var time = date;
        if (a)
          time = dateAdd(date, 'second', b);
        var timeStr = formatDateString(time);
        if (name === 'Breastfeeding') {
          var beo = {
            finalized: true,
            foodItems: [],
            isBreastfeedOccasion: true,
            startTime: timeStr,
            endTime: timeStr
          }
          fr.eatingOccasions.push(beo);
          continue;
        }
        var description = "24HRDATA\n";
        description += name + '\n';
        description += timeStr + "\n";
        description += row[item + "Description"] + '\n';
        description += row[item + "QuantityConsumed"] + '\n';
        var cm = row[item + "Cooking&PrepMethods"];
        if (cm)
          cm = cm.replace(/,(?=[^ ])/g, ", ");
        description += cm + '\n';
        var ri = row[item + "RecipeInfo"];
        if (ri)
        ri = ri.replace(/,(?=[^ ])/g, ", ");
        description += ri + '\n';
        description += row[item + "CombinedWithOrAdditions"] + '\n';
        description += row[item + "LeftoverORSeconds"] + '\n';
        description += row[item + "Comments"] + '\n';

        var fi = {
          description: description,
          annotatedStatus: 2,
          captureTime: timeStr,
          finalizeTime: timeStr
        };
        eo.foodItems.push(fi);

        if (/\d/.test(row[item + "RecipeInfo"])) {
          hh.householdRecipes.push({
            recipeId: ++newRecipeId,
            recipeNameText: ri + "\n\n" + cm,
            captureTime: timeStr,
            recipeName: name,
            ingredients: [{
              description: row[item + "RecipeInfo"],
              captureTime: timeStr
            }]
          });
          eo.recipeIds.push(newRecipeId);
        }

      }
    }

    $scope.households = households;

    return;
  };
}]);