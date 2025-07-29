
String.prototype.replaceAll = function(search, replacement) {
    var target = this;
    return target.replace(new RegExp(search, 'g'), replacement);
};
function isNumeric(n) {
  return !isNaN(parseFloat(n)) && isFinite(n);
}

var allfctConfig = [
      ["Origin","OriginId","^.*\\bid\\b.*$"],
      ["Name","Name","^(?=.*\\bname\\b|.*\\bdescription\\b)((?!alternate).)*$"],
      ["Alternate Name","AlternateName","^(?=.*\\bname\\b)(?=.*\\balternate\\b).*$"],
      ["Energy - by calculation (kcal)","EnergykCal","^(?=.*\\benergy\\b)(?=.*\\bkcal\\b).*$"],
      ["Energy, without dietary fibre (kJ)","EnergyWoFibre","^(?=.*\\benergy\\b)(?=.*\\bkJ\\b)(?=.*\\bwithout\\b).*$"],
      ["Energy, with dietary fibre (kJ)","EnergyWFibre","^(?=.*\\benergy\\b)(?=.*\\bkJ\\b)(?=.*\\bwith\\b).*$"],
      ["Energy - by calculation (kJ)","EnergykJ","^(?=.*\\benergy\\b)(?=.*\\bkJ\\b)((?!with).)*$"],
      ["Density (g/mL)","Density","^(?=.*\\bdensity\\b).*$"],
      ["Moisture (g)","Moisture","^(?=.*\\bmoisture\\b).*$"],
      ["Protein, total (g)","Protein","^(?=.*\\bprotein\\b).*$"],
      ["Fat, total (g)","Fat","^(?=.*\\bfat\\b)((?!saturated).)*$"],
      ["Total saturated fat (g)","SaturatedFat","^(?=.*\\bfat\\b)(?=.*\\bsaturated\\b).*$"],
      ["Total monounsaturated fat (g)","MonounsaturatedFat","^(?=.*\\bfat\\b)(?=.*\\bmonounsaturated\\b).*$"],
      ["Total polyunsaturated fat (g)","PolyunsaturatedFat","^(?=.*\\bfat\\b)(?=.*\\bpolyunsaturated\\b).*$"],
      ["Linoleic acid (g)","LinoleicAcic","^(?=.*\\blinoleic\\b).*$"],
      ["Alpha-linolenic acid (g)","AlphaLinolenicAcid","^(?=.*alpha)(?=.*linolenic).*$"],
      ["Available carbohydrates, without sugar alcohol (g)","CarbohydratesWoSa","^(?=.*carbohydrates)(?=.*without sugar alcohol).*$"],
      ["Available carbohydrates, with sugar alcohols (g)","Carbohydrates","^(?=.*carbohydrates).*$"],
      ["Starch (g)","Starch","^(?=.*\\bstarch\\b).*$"],
      ["Total sugars (g)","SugarTotal","^(?=.*sugar)(?=.*total).*$"],
      ["Added sugars (g)","SugarAdded","^(?=.*sugar)(?=.*added).*$"],
      ["Free sugars (g)","SugarFree","^(?=.*sugar)(?=.*free).*$"],
      ["Dietary fibre (g)","Fibre","^(?=.*\\bdietary\\b)(?=.*\\bfibre\\b)((?!energy).)*$"],
      ["Alcohol (g)","Alcohol","^(?=.*\\balcohol\\b).*$"],
      ["Ash (g)","Ash","^(?=.*\\bash\\b).*$"],
      ["Preformed vitamin A (retinol) (µg)","Retinol","^(?=.*\\bretinol\\b).*$"],
      ["Beta-carotene (µg)","BetaCarotene","^(?=.*carotene)((?!vitamin).)*$"],
      ["Provitamin A (b-carotene equivalents) (µg)","ProvitaminA","^(?=.*provitamin a)((?!retinol).)*$"],
      ["Vitamin A retinol equivalents (µg)","VitaminA","^(?=.*\\bvitamin a\\b)((?!carotene).)*$"],
      ["Thiamin (B1) (mg)","Thiamin","^(?=.*thiamin).*$"],
      ["Riboflavin (B2) (mg)","Riboflavin","^(?=.*riboflavin).*$"],
      ["Niacin (B3) (mg)","Niacin","^(?=.*niacin)((?!.*equivalent).)*$"],
      ["Niacin derived equivalents (mg)","NiacinDE","^(?=.*\\bniacin\\b)(?=.*equivalent).*$"],
      ["Vitamin B6 (mg)","VitaminB6","^(?=.*\\bvitamin\\b)(?=.*\\bb6\\b).*$"],
      ["Vitamin B12 (µg)","VitaminB12","^(?=.*\\bvitamin\\b)(?=.*\\bb12\\b).*$"],
      ["Vitamin C (mg)","VitaminC","^(?=.*\\bvitamin\\b)(?=.*\\bc\\b).*$"],
      ["Alpha-tocopherol (mg)","AlphaTocopherol","^(?=.*alpha)(?=.*tocopherol).*$"],
      ["Vitamin E (mg)","VitaminE","^(?=.*\\bvitamin\\b)(?=.*\\be\\b).*$"],
      ["Folate, natural (µg)","Folate","^(?=.*folate)(?=.*\\bnatural\\b).*$"],
      ["Folic acid (µg)","FolicAcid","^(?=.*\\bfolic\\b)(?=.*\\bacid\\b).*$"],
      ["Total Folates (µg)","FolatesTotal","^(?=.*folate)(?=.*\\btotal\\b).*$"],
      ["Dietary folate equivalents (µg)","FolateDietary","^(?=.*folate)(?=.*\\bdietary\\b).*$"],
      ["Calcium (Ca) (mg)","Calcium","^(?=.*\\bcalcium\\b).*$"],
      ["Iodine (I) (µg)","Iodine","^(?=.*\\biodine\\b).*$"],
      ["Phosphorus (P) (mg)","Phosphorus","^(?=.*\\bphosphorus\\b).*$"],
      ["Sodium (Na) (mg)","Sodium","^(?=.*\\bsodium\\b).*$"],
      ["Potassium (K) (mg)","Potassium","^(?=.*\\bpotassium\\b).*$"],
      ["Iron (Fe) (mg)","Iron","^(?=.*\\biron\\b).*$"],
      ["Magnesium (Mg) (mg)","Magnesium","^(?=.*\\bmagnesium\\b).*$"],
      ["Selenium (Se) (µg)","Selenium","^(?=.*\\bselenium\\b).*$"],
      ["Copper, (Cu) (mg)","Copper","^(?=.*\\bcopper\\b).*$"],
      ["Zinc (Zn) (mg)","Zinc","^(?=.*\\bzinc\\b).*$"],
      ["Caffeine (mg)","Caffeine","^(?=.*\\bcaffeine\\b).*$"],
      ["Tryptophan (mg)","Tryptophan","^(?=.*\\btryptophan\\b).*$"],
      ["C20:5w3 Eicosapentaenoic (mg)","Eicosapentaenoic","^(?=.*\\beicosapentaenoic\\b).*$"],
      ["C22:5w3 Docosapentaenoic (mg)","Docosapentaenoic","^(?=.*\\bdocosapentaenoic\\b).*$"],
      ["C22:6w3 Docosahexaenoic (mg)","Docosahexaenoic","^(?=.*\\bdocosahexaenoic\\b).*$"],
      ["Total long chain omega 3 fatty acids (mg)","Omega3FattyAcid","^(?=.*\\bomega.*3\\b).*$"],
      ["Total trans fatty acids (mg)","TransFattyAcid","^(?=.*\\btrans\\b)(?=.*\\bfatty\\b)(?=.*acid).*$"],
      ["Cholesterol (mg)","Cholesterol","^(?=.*\\bcholesterol\\b).*$"],
      ["Grain (cereal) foods","ADG_10","^10$"],
      ["Wholegrain (WG) or higher fibre (HF) cereals/grains","ADG_101","^101$"],
      ["WG/HF Breads","ADG_1011","^1011$"],
      ["WG/HF Grains (excluding oats)","ADG_1012","^1012$"],
      ["WG/HF Oats","ADG_1013","^1013$"],
      ["WG/HF Breakfast cereal flakes","ADG_1014","^1014$"],
      ["WG/HF Savoury crackers/crispbreads","ADG_1015","^1015$"],
      ["WG/HF Crumpets","ADG_1016","^1016$"],
      ["WG/HF English muffins and scones","ADG_1017","^1017$"],
      ["WG/HF Flour","ADG_1018","^1018$"],
      ["Refined (Ref) or lower fibre (LF) cereals/grains","ADG_102","^102$"],
      ["Ref/LF Breads","ADG_1021","^1021$"],
      ["Ref/LF Grains (excluding oats)","ADG_1022","^1022$"],
      ["Ref/LF Oats","ADG_1023","^1023$"],
      ["Ref/LF Breakfast cereal flakes","ADG_1024","^1024$"],
      ["Ref/LF Savoury crackers/crispbreads","ADG_1025","^1025$"],
      ["Ref/LF Crumpets","ADG_1026","^1026$"],
      ["Ref/LF English muffins and scones","ADG_1027","^1027$"],
      ["Ref/LF Flour","ADG_1028","^1028$"],
      ["Vegetables and legumes/beans","ADG_20","^20$"],
      ["Green and brassica vegetables","ADG_201","^201$"],
      ["Orange vegetables","ADG_202","^202$"],
      ["Starchy vegetables","ADG_203","^203$"],
      ["Legumes as a vegetable","ADG_204","^204$"],
      ["Other vegetables","ADG_205","^205$"],
      ["Whole vegetables","ADG_2051","^2051$"],
      ["Vegetable Juice","ADG_2052","^2052$"],
      ["Fruit","ADG_30","^30$"],
      ["Fresh/canned fruit ","ADG_301","^301$"],
      ["Dried fruit","ADG_302","^302$"],
      ["Fruit juice","ADG_303","^303$"],
      ["Milk, yoghurt, cheese and/or alternatives","ADG_40","^40$"],
      ["Higher fat (HF) dairy foods (>10% fat)","ADG_401","^401$"],
      ["HF Cheese","ADG_4011","^4011$"],
      ["HF Milk powder only","ADG_4012","^4012$"],
      ["Medium fat (MF) dairy foods (4-10% fat)","ADG_402","^402$"],
      ["MF Milk","ADG_4021","^4021$"],
      ["MF Evaporated milk","ADG_4022","^4022$"],
      ["MF Condensed milk","ADG_4023","^4023$"],
      ["MF Cheese, hard & soft","ADG_4024","^4024$"],
      ["MF Cheese, fresh","ADG_4025","^4025$"],
      ["MF Yoghurt, dairy based","ADG_4026","^4026$"],
      ["MF Milk alternative beverage, calcium enriched","ADG_4027","^4027$"],
      ["MF Dairy-based snack foods","ADG_4028","^4028$"],
      ["Lower fat (LF) dairy foods (<4% fat)","ADG_403","^403$"],
      ["LF Milk","ADG_4031","^4031$"],
      ["LF Evaporated milk","ADG_4032","^4032$"],
      ["LF Condensed milk","ADG_4033","^4033$"],
      ["LF Cheese, hard & soft","ADG_4034","^4034$"],
      ["LF Cheese, fresh","ADG_4035","^4035$"],
      ["LF Yoghurt, dairy based","ADG_4036","^4036$"],
      ["LF Milk alternative beverage, calcium enriched","ADG_4037","^4037$"],
      ["LF Dairy-based snack foods","ADG_4038","^4038$"],
      ["LF Milk powder only","ADG_4039","^4039$"],
      ["Meats, poultry, fish, eggs, tofu, nuts and seeds and legumes/beans/tofu","ADG_50","^50$"],
      ["Red meat, lean (<10% fat)","ADG_501","^501$"],
      ["Unprocessed meat (<10% fat)","ADG_5011","^5011$"],
      ["Processed meat (<10% fat)","ADG_5012","^5012$"],
      ["Red meat, non-lean (≥10% fat)","ADG_502","^502$"],
      ["Unprocessed meat (≥10% fat)","ADG_5021","^5021$"],
      ["Processed meat (≥10% fat)","ADG_5022","^5022$"],
      ["Poultry, lean (<10% fat)","ADG_503","^503$"],
      ["Unprocessed poultry (<10% fat)","ADG_5031","^5031$"],
      ["Processed poultry (<10% fat)","ADG_5032","^5032$"],
      ["Poultry, non-lean (≥10% fat)","ADG_504","^504$"],
      ["Unprocessed poultry (≥10% fat)","ADG_5041","^5041$"],
      ["Processed poultry (≥10% fat)","ADG_5042","^5042$"],
      ["Fish and seafood","ADG_505","^505$"],
      ["Eggs","ADG_506","^506$"],
      ["Legumes as meat alternative","ADG_507","^507$"],
      ["Legumes/beans","ADG_5071","^5071$"],
      ["Tofu","ADG_5072","^5072$"],
      ["Nuts and seeds","ADG_508","^508$"],
      ["Water","ADG_60","^60$"],
      ["Unsaturated spreads and oils","ADG_70","^70$"],
      ["Unsaturated spreads ","ADG_701","^701$"],
      ["Unsaturated oils","ADG_702","^702$"],
      ["Nuts","ADG_703","^703$"]
    ];

app = angular.module('visida_cms');

app.controller('databaseCommon', ['$scope', 'Restangular', '$timeout', '$compile', 'UserService', function($scope, Restangular, $timeout, $compile, UserService) {
  $scope.pageItems = function(items) {
    $scope.pages = [];
    $scope.pageNames = [];
    var size = items.length;
    var pageCount = 1, pageSize = size;
    while (pageSize > 500) {
      pageSize = size / pageCount++;
    }
    pageSize = Math.ceil(pageSize);
    //var pageNames = new Array();
    for (var i = 0; i < pageCount-1; i++) {
      var page = items.slice(i * pageSize, i * pageSize + pageSize);
      $scope.pages.push(page);
      //var name = (page[0].name.split(/[\s,]/))[0] + " - " + (page[page.length-1].name.split(/[\s,]/))[0];
      //$scope.pageNames.push(name);
    };
    if ($scope.pages.length <= 0) {
      var page = items;
      $scope.pages.push(page);
      // var name = (page[0].name.split(/[\s,]/))[0] + " - " + (page[page.length-1].name.split(/[\s,]/))[0];
      // $scope.pageNames.push(name);
    }

    $scope.displayItems($scope.pages[0]);
    $scope.currentPageIndex = 0;
  };

  $scope.changePage = function(i) {
    if (i < 0 || i >= $scope.pages.length)
      return;
    $scope.currentPageIndex = i;
    var page = $scope.pages[i];
    $scope.displayItems(page);
  };

  $scope.searchItems = function(search) {
    if (search.length == 0) {
      $scope.pageItems($scope.allItems);
      return;
    }

    var strs = search.split(' ');
    var searchStr = '^(?=.*' + strs.join(')(?=.*') + ').*$';
    var re = new RegExp(searchStr, 'i');
    var items = $scope.allItems.filter(function(item) {
      return item.name.match(re) || (item.alternateName != null && item.alternateName.match(re));
    });
    $scope.pageItems(items);
  }

  $scope.displayItems = function(items) {
    var data = $scope.createDataArray(items);

    var table = document.getElementById('recipe-table');
    // $scope.tableData = data;
    frzDisplayTable(table, data, 3);

    var fbf = document.getElementsByClassName("frz-body-fixed")[0];
    $compile(fbf)($scope);
  }

  $scope.createDataArray = function(items) {
    $scope.fctConfig = $.extend(true, {}, fctConfigStart);

    var keys = Object.keys($scope.fctConfig);
    var chkSize = Math.min(100,items.length);
    var altName = false;
    for (var i = 0; i < chkSize; i++) {
      if (items[i].alternateName)
        altName = true;
      for (var j = 0; j < keys.length; j++) {
        if (!$scope.fctConfig.hasOwnProperty(keys[j]))
          continue;
        if (items[i][keys[j]]) {
          $scope.fctConfig[keys[j]].valid = true;
        }
      }
    }

    var data = [];
    var header = ['', 'Id', 'Name'];
    if (altName)
      header.push('Local Name');
    for (var j = 0; j < keys.length; j++) {
      if (!$scope.fctConfig.hasOwnProperty(keys[j]) || !$scope.fctConfig[keys[j]].valid)
        continue;
      header.push($scope.fctConfig[keys[j]].external)
    }
    data.push(header);

    for (var i = 0; i < items.length; i++) {
      var item = items[i];
      var buttonCell = '<span></span>';
      if (UserService.isAdmin || UserService.isCoord)
        buttonCell = '<span class="glyphicon glyphicon-edit table-glyph" role="button" ng-click="editRow(' + i + ')" ></span>'
      var row = [buttonCell, item.originId, item.name];
      if (altName)
        row.push(item.alternateName);
      for (var j = 0; j < keys.length; j++) {
        if (!$scope.fctConfig.hasOwnProperty(keys[j]) || !$scope.fctConfig[keys[j]].valid)
          continue;
        row.push(item[keys[j]]);
      }
      data.push(row);
    }

    return data;
  };
}]);

app.controller('databaseController', ['$scope', 'Restangular', '$timeout', '$compile', '$controller', function($scope, Restangular, $timeout, $compile, $controller) {
  $scope.loading = 1;
  $controller('databaseCommon', { $scope: $scope });
  $scope.fctConfig = $.extend(true, {}, fctConfigStart);

  Restangular.one('FoodCompositionTables').get().then(function(result) {
    $scope.fctables = result.data;
  }).finally(function() { $scope.loading--; });

  $scope.load = function() {
    if (!$scope.selectedDb)
      return $scope.dbMessage = { success: false, txt: "No table selected." };
    $scope.loading++;
    
    Restangular.one('FoodCompositionTables', $scope.selectedDb.id).get().then(function(items) {
      $scope.allItems = items.data.foodCompositions;
      $scope.pageItems($scope.allItems);
      // $scope.displayItems($scope.allItems);
    }, function errorCallback(heck) {
      $scope.dbMessage = { success: false, txt: "Could not load food items." };
    }).finally(function() { $scope.loading--; });
  };

  $scope.changeDatabase = function(tableId) {
    $scope.selectedDb = tableId;
    $scope.load();
  };

  $scope.exportTable = function() {
    var s = document.getElementById("table-select");
    var tableName = s.options[s.selectedIndex].text;
    var date = new Date();
    var name = tableName + '_' +  date.getDate() + '-' + date.getMonth() + '-' + date.getFullYear() + '.xlsx';

    var data = $scope.createDataArray($scope.allItems);
    for (var i = 0; i < data.length; i++) {
      data[i].splice(0, 1);
    }

    var wb = XLSX.utils.book_new();
    var ws1 = XLSX.utils.aoa_to_sheet(data, { type: 'string', raw:true });
    XLSX.utils.book_append_sheet(wb, ws1, 'Data');

    XLSX.writeFile(wb, name);
    return;
  };

  $scope.editingRowIndex = null;
  $scope.editingRowOrig = null;
  $scope.editRow = function(index) {
    if ($scope.editingRowIndex != null) {
      alert('Can only edit one row at a time');
      return;
    }
    $scope.editingRowIndex = index;
    var page = $scope.pages[$scope.currentPageIndex];
    var item = page[index];

    var fixed = document.getElementsByClassName("frz-body-fixed")[0].children[0];
    var buttonCell = fixed.rows[index].cells[0];
    buttonCell.innerHTML = '<span class="glyphicon glyphicon-remove table-glyph" role="button" ng-click="cancelEditRow()"></span>' + 
      '<span class="glyphicon glyphicon-floppy-disk table-glyph" role="button" ng-click="saveEditRow()" style="padding-left: 5px;"></span>';
    $compile(buttonCell)($scope);

    var free = document.getElementsByClassName("frz-body-free")[0].children[0];
    var orig = [];
    var row = free.rows[index];
    var headers = document.getElementsByClassName("frz-header-free")[0].children[0].rows[0];
    for (var c = 0; c < row.cells.length; c++) {
      var cell = row.cells[c];
      var name = headers.cells[c].innerText;

      cell.classList.add("table-input-cell")
      var input = document.createElement("INPUT");
      input.setAttribute("type", "text");
      input.setAttribute("class", "table-input");
      input.setAttribute("name", name);
      input.value = orig[c] = cell.innerText;
      cell.innerText = null;
      cell.appendChild(input);
    }

    $scope.editingRowOrig = orig;
  };

  $scope.cancelEditRow = function() {
    var fixed = document.getElementsByClassName("frz-body-fixed")[0].children[0];
    var buttonCell = fixed.rows[$scope.editingRowIndex].cells[0];
    buttonCell.innerHTML = '<span class="glyphicon glyphicon-edit table-glyph" role="button" ng-click="editRow(' + $scope.editingRowIndex + ')"></span>';
    $compile(buttonCell)($scope);

    var free = document.getElementsByClassName("frz-body-free")[0].children[0];
    var row = free.rows[$scope.editingRowIndex];
    for (var c = 0; c < row.cells.length; c++) {
      var cell = row.cells[c];
      cell.classList.remove("table-input-cell")
      cell.innerHTML = null;
      cell.innerText = $scope.editingRowOrig[c];
    }

    $scope.editingRowIndex = null;
  };

  $scope.saveEditRow = function() {
    var page = $scope.pages[$scope.currentPageIndex];
    var orig = page[$scope.editingRowIndex];

    var free = document.getElementsByClassName("frz-body-free")[0].children[0];
    var row = free.rows[$scope.editingRowIndex];
    var item = $.extend(true, {}, orig);
    var configKeys = Object.keys(fctConfigStart);
    for (var c = 0; c < row.cells.length; c++) {
      var input = row.cells[c].children[0];
      var value = input.value;
      if (value === 'null' || value.length == 0)
        value = null;

      if (input.name === 'Local Name') {
        item.alternateName = value;
        continue;
      } //else
      for (var k = 0; k < configKeys.length; k++) {
        var key = configKeys[k];
        if (input.name === fctConfigStart[key].external) {
          item[fctConfigStart[key].internal] = value;
          break;
        }
      }
    }

    var fixed = document.getElementsByClassName("frz-body-fixed")[0].children[0];
    var buttonCell = fixed.rows[$scope.editingRowIndex].cells[0];
    buttonCell.innerHTML = '<span class="glyphicon glyphicon-refresh loading-run table-glyph"></span>';

    Restangular.oneUrl('FoodCompositions/PutFoodComposition').customPUT(item).then(
    function(result) {
      page[$scope.editingRowIndex] = item;
      $scope.cancelEditRow();
    }, function(result) {
      buttonCell.innerHTML = '<span class="glyphicon glyphicon-remove table-glyph" role="button" ng-click="cancelEditRow()"></span>' + 
        '<span class="glyphicon glyphicon-floppy-disk table-glyph" role="button" ng-click="saveEditRow()" style="padding-left: 5px;"></span>';
      $compile(buttonCell)($scope);
      alert("Could not upload food item. \n" + result.data.message);
    }).finally(function() { });
  };
}]);

angular.module('visida_cms').controller('databaseModController', ['$scope', 'Restangular', 'Upload', 'configSettings', function($scope, Restangular, Upload, configSettings) {
  //$scope.newItem = {};
  $scope.searchtimer = "";
  $scope.selectedMeasureId = null;
  $scope.newMeasures = [];

  Restangular.one('FoodCompositionTables').get().then(function(result) {
    $scope.fctables = result.data;
  });
  Restangular.one('ExampleMeasures').get().then(function(result) {
    $scope.foodItemList = result.data;
  });

  $scope.addMeasure = function() {
    var name = prompt("Please enter identifiers for the measure in order of importance. Separate identifiers with a space (e.g. \"rice white cooked\")." ,"");
    if (name == null || name.length <= 0)
      return;

    $scope.measureLoading = true;
    var measure = {name: name};
    Restangular.oneUrl('ExampleMeasures').post('', measure).then(function(result) {
      $scope.foodItemList.push({id: result.data.id, name: name, measures: []});
      $scope.selectedMeasureId = result.data.id;
      $scope.changeMeasure($scope.selectedMeasureId);
      $scope.measureLoading = false;
    }, function(response) {
      $scope.measureLoading = false;
      alert("Could not add measure. " + (response.data != null ? response.data.message : "An error has occurred"));
    });
  };
  $scope.changeMeasure = function(id) {
    if (!id)
      return;
    var iid = parseInt(id);
    //$scope.measureLoading = true;
    //id = $scope.selectedMeasureId;
    // for (var i = 0; i < $scope.newMeasures.length; i++) { $scope.newMeasures[i].foodTypeId = id; }
    // Restangular.one('ExampleMeasures/' + id).get().then(function(result) {
    //   $scope.exampleMeasure = result.data;
    //   $scope.measureLoading = false;
    // });
    for (var i = 0; i < $scope.foodItemList.length; i++) {
      if ($scope.foodItemList[i].id === iid) {
        $scope.exampleMeasure = $scope.foodItemList[i];
        break;
      }
    }
  };
  $scope.addNewMeasures = function(images) {
    for (var i = 0; i < images.length; i++) {
      var measure = {
        image: images[i],
        foodTypeId: $scope.selectedMeasureId
      };
      $scope.newMeasures.push(measure);
    }
  };
  $scope.addMeasureItem = function(measure, index) {
    $scope.measureLoading = true;
    var url = configSettings.baseUrl + '/ExampleMeasureItem/PostMeasureImage';
    Upload.upload({
      url: url,
      data: {file: measure.image}
    }).then(function(result) {
      measure.imageUrl = result.data;
      measure.image = null;
      Restangular.oneUrl('ExampleMeasureItems').post('', measure).then(function(result) {
        //$scope.foodItemList.push({id: result.data.id, name: name});
        $scope.exampleMeasure.measures.push(measure);
        $scope.newMeasures.splice(index, 1);
        $scope.measureLoading = false;
      }, function(response) {
        $scope.measureLoading = false;
        alert("Could not add measure. " + (response.data != null ? response.data.message : "An error has occurred"));
      });
    }, function(result) {
      alert("Failed to upload image!");
      $scope.measureLoading = false;
    });
  };
  $scope.removeNewMeasure = function(index) {
    $scope.newMeasures.splice(index, 1);
  };
  $scope.deleteMeasure = function(measure, index) {
    Restangular.oneUrl("ExampleMeasureItem/" + measure.id).remove().then(function() {
      $scope.exampleMeasure.measures.splice(index, 1);
    }, function(response) {
      alert("Item could not be deleted. " + response.data.message);
    });
  };

  $scope.setParsedEntries = function(entries) {
    $scope.entries = entries;
    $scope.loaded = true;
    $scope.$apply();
  };
  $scope.parse = function(file) {
    $scope.uploadMessage = {};
    var fctConfig = JSON.parse(JSON.stringify(allfctConfig));

    var reader = new FileReader();
    reader.readAsText(file, "UTF-8");
    reader.onload = function(evt) {
      var csv = reader.result;
      //Clean data
      csv = csv.replaceAll('-', '');
      if (csv.indexOf("\n") < 0)
        return;
      //Get the top row, using quote buffers
      var head = "", count = 0, index = 0;
      var testA = new Array();
      do {
        if (index + 1 >= csv.length)
          return;
        index = csv.indexOf("\r", index + 1)
        head = csv.slice(0, index);
        count = (head.match(new RegExp('"', "g")) || []).length
      } while (count % 2 != 0)
      //Format head 
      var headers = $.csv.toArray(head);
      /*for (var i = 0; i < fctConfig.length; i++) {
        for (var j = 0; j < headers.length; j++) {
          var re = new RegExp(fctConfig[i][2], 'gi');
          var reMatch = re.test(headers[j]);
          if (reMatch) {
            testA.push([headers[j],fctConfig[i][1]]);
            //fctConfig[i][2] = "";
            head = head.replace(headers[j], fctConfig[i][1]);
            headers[j] = "";
            break;
          }
        }
      }*/
      for (var i = 0; i < headers.length; i++) {
        var matched = false;
        for (var j = 0; j < fctConfig.length; j++) {
          var re = new RegExp(fctConfig[j][2], 'gi');
          var reMatch = re.test(headers[i]);
          if (reMatch) {
            testA.push([headers[i],fctConfig[j][1]]);
            head = head.replace(headers[i], fctConfig[j][1]);
            fctConfig.splice(j, 1);
            j--;
            //headers[j] = "";
            matched = true;
            break;
          }
        }
        if (!matched)
          head = head.replace(headers[i], '');
      }
      head = head.replaceAll(' ', '').replaceAll('"', '').replaceAll('\n', '');
      csv = head + csv.substring(index);
      $scope.headers = $.csv.toArray(head).filter(function(item) { return item != ""; });
      var obj = $.csv.toObjects(csv);
      for (var i = obj.length - 1; i >= 0; i--) {
        if (obj[i].Name == undefined || obj[i].Name == "")
          obj.splice(i, 1);
        else if (obj[i][""]) {
          var prop = "";
          var row = obj[i];
          delete row[prop];
        }
      }
      $scope.allEntries = obj;
      $scope.setParsedEntries(obj.slice(0, 200));
    };
  };

  $scope.parseMeasures = function(file) {
    $scope.measuresLoading = true;
    var reader = new FileReader();
    reader.readAsText(file, "UTF-8");
    reader.onload = function(evt) {
      var csv = reader.result;
      if (csv.indexOf("\n") < 0)
        return;
      //Get the top row, using quote buffers
      // var head = "", count = 0, index = 0;
      // do {
      //   if (index + 1 >= csv.length)
      //     return;
      //   index = csv.indexOf("\r", index + 1)
      //   head = csv.slice(0, index);
      //   count = (head.match(new RegExp('"', "g")) || []).length
      // } while (count % 2 != 0)
      // //Format head 
      // var headers = $.csv.toArray(head);
      // csv = csv.substring(index);
      var rows = $.csv.toObjects(csv);

      var measures = [];
      for (var i = 0; i < rows.length; i++) {
        var row = rows[i];
        var description = row["Measure description 1"];
        if (row["Measure description 2"]) description += ", " + row["Measure description 2"];
        if (row["Measure description 3"]) description += ", " + row["Measure description 3"];
        if (row["Measure description 4"]) description += ", " + row["Measure description 4"];
        
        if (!description)
          continue;

        var meas = {};
        if (measures.length > 1 && measures[measures.length-1].originId === row["Food ID"]) {
          meas = measures[measures.length-1];
        } else {
          meas.originId = row["Food ID"];
          meas.name = row["Food Name"];
          measures.push(meas);
        }
        if (description.includes('density'))
          meas.density = row["Weight in grams"];
        else {
          if (meas.measures) {
            meas.measures += "|" + description + ":" + row["Weight in grams"] + "";
          }
          else
            meas.measures = "" + description + ":" + row["Weight in grams"] + "";
        }
      }

      $scope.measures = measures;
      $scope.displayMeasures = measures.slice(0, 400);
      $scope.measuresLoading = false;
      $scope.$apply();
    } 
  }
  $scope.upload = function() {
    $scope.uploadMessage = {};
    if (!$scope.tableId) {
      $scope.uploadMessage = { success: false, txt: "Must select a table to upload to." };
      return;
    }
    if (!confirm("Are you sure you want to upload " + $scope.allEntries.length + " food items?"))
      return;
    $scope.loaded = false;
    var request = {
      overwrite: $scope.overwrite,
      commitMessage: $scope.file.name,
      foodCompositions: $scope.allEntries,
      tableId: $scope.tableId
    };
    Restangular.oneUrl('FoodCompositions/PostFoodItems').post('', request).then(
    function(result) {
      $scope.loaded = true;
      $scope.file = null;
      $scope.entries = new Array();
      $scope.allEntries = null;
      $scope.uploadMessage = { success: true, txt: "Success! " + result.data + " new food items recorded." };
    }, function(result) {
      $scope.loaded = true;
      $scope.uploadMessage = { success: false, txt: "Could not upload food items." };
    });
  };

  $scope.uploadMeasures = function() {
    $scope.measureMessage = {};
    if (!$scope.tableIdMeasures) {
      $scope.measureMessage = { success: false, txt: "Must select a table to upload to." };
      return;
    }
    if (!confirm("Are you sure you want to update " + $scope.measures.length + " food items?"))
      return;
    $scope.measuresLoading = true;
    var request = {
      overwrite: 'true',
      commitMessage: $scope.measureFile.name,
      foodCompositions: $scope.measures,
      tableId: $scope.tableIdMeasures
    };
    Restangular.oneUrl('FoodCompositions/PostFoodItems').post('', request).then(
    function(result) {
      $scope.measuresLoading = false;
      $scope.measureFile = null;
      $scope.measureMessage = { success: true, txt: "Success! " + measures.length + " rows updated." };
      $scope.measures = new Array();
      $scope.displayMeasures = null;
      $scope.$apply();
    }, function(result) {
      $scope.measuresLoading = false;
      $scope.measureMessage = { success: false, txt: "Could not upload food items." };
    });
  }

  $scope.createTable = function(name) {
    $scope.tableMessage = {};
    if (!name || name.length <= 0)
      return;

    var table = {name: name};
    $scope.tableMessage = {success: true, txt: "Creating table...."};
    Restangular.oneUrl('FoodCompositionTables').post('', table).then(function(response) {
      $scope.fctables.push({name: name, id: response.data.id});
      $scope.tableMessage = {success: true, txt: "Table " + name + " was created successfully."};
    }, function(response) {
      $scope.tableMessage = {success: false, txt: "Could not create table. " + response.data.message};
    });
  };
}]);

angular.module('visida_cms').controller('rdaController', ['$scope', 'Restangular', function($scope, Restangular) {
  $scope.loading = 1;

  Restangular.one('RDA').get().then(function(result) {
    $scope.rdas = result.data;
  }).finally(function() { $scope.loading--; });

  $scope.createModel = function() {
    var name = prompt("Please enter a name for the model." ,"");
    if (name == null || name.length <= 0)
      return;

    $scope.loading++;
    var model = {name: name};
    Restangular.oneUrl('RDA').post('', model).then(function(result) {
      $scope.rdas.push(result.data);
    }, function(response) {
      alert("Could not add model. " + (response.data != null ? response.data.message : "An error has occurred"));
    }).finally(function() { $scope.loading--; });
  };

  $scope.loadModel = function(id) {
    var model = $scope.rdas.filter(function(x) { return x.id === id; })[0];
    if (!model)
      return;
    $scope.model = model;
    $scope.fctConfig = $.extend(true, {}, fctConfigStart);
    $scope.modelDescription = model.description;
    // if (!model.fieldData) {
    //   $scope.data = [];
    // } else
      $scope.data = model.rdAs;//JSON.parse(model.fieldData);
    if ($scope.data.length == 0)
      $scope.data.push({rowId: "row-0", childbearing: "none"});
    var keys = Object.keys($scope.fctConfig);

    for (var i = 0; i < keys.length; i++) {
      if (!$scope.fctConfig.hasOwnProperty(keys[i]))
        return;
      for (var j = 0; j < $scope.data.length; j++) {
        var row = $scope.data[j];
        if (row.hasOwnProperty(keys[i]) && row[keys[i]])
          $scope.fctConfig[keys[i]].valid = true;
      }
    }
  };

  $scope.updateColumnCheck = function(col, evt) {
    if (col.valid) {
      for (var i = 0; i < $scope.data.length; i++) {
        $scope.data[i][col.internal] = 0;
      }
    } else {
      var p = confirm("Removing this column will delete any entered data. Would you like to continue?");
      if (p) {
        col.valid = false;
        for (var i = 0; i < $scope.data.length; i++) {
          delete $scope.data[i][col.internal];
        }
      } else
        col.valid = true;
    }
  };

  $scope.saveModel = function() {
    $scope.loading++;
    $scope.model.rdAs = $scope.data;
    Restangular.allUrl('RDA/' + $scope.model.id).customPUT($scope.model).then(function() {
      return;
    }, function(error) {
      alert("Error: " + error.data.message);
      return;
    }).finally(function() { $scope.loading--; });
  };

  $scope.addRow = function() {
    var last = $scope.data[$scope.data.length-1];
    var row = {rowId: "row-" + $scope.data.length, description: "", gender: last.gender, ageLowerBound: last.ageUpperBound, ageUpperBound: "", childbearing: "0", weight: ""};

    $scope.data.push(row);
  };

  $scope.removeRow = function(index) {
    $scope.data.splice(index, 1);
  };

  $scope.checkPaste = function(index, desc) {
    if (!desc.indexOf("\t") > 0)
      return;

    var line = desc;
    var breakIndex = line.indexOf('\n');
    if (breakIndex > 0)
      line = line.substring(0, line.indexOf('\n'));

    var vals = line.split('\t');
    var cols = ['description', 'gender', 'ageLowerBound', 'ageUpperBound', 'childbearing', 'weight'];
    var keys = Object.keys($scope.fctConfig);
    for (var k = 0; k < keys.length; k++) {
      if ($scope.fctConfig[keys[k]].valid)
        cols.push($scope.fctConfig[keys[k]].internal);
    }
    var row = $scope.data[index];

    for (var i = 0; i < cols.length && i < vals.length; i++) {
      var c = cols[i];
      if (c === 'gender') {
        if (vals[i].toLowerCase().startsWith('f')) vals[i] = 'Female';
        else if (vals[i].toLowerCase().startsWith('m')) vals[i] = 'Male';
        else vals[i] = 'Child';
      } else if (c === 'childbearing') {
        var p = vals[i].indexOf('P') >= 0, b = vals[i].indexOf('B') >= 0;
        if (p && b) vals[i] = 'PregnantAndBreastfeeding';
        else if (p) vals[i] = 'Pregnant';
        else if (b) vals[i] = 'Breastfeeding';
        else vals[i] = 'None';
      } else if (c !== 'description') {
        vals[i] = parseFloat(vals[i]);
      }
      row[c] = vals[i];
    }
    
    // var row = $("#row-" + index)
    // if (!row) {
    //   return;
    // }
    // var childs = row.find("td");//.childNodes;
    // childs.splice(0, 1);
    // for (var i = 0; i < childs.length && i < vals.length; i++) {
    //   var c = childs[i];
    //   var input = c.children[0];
    //   if (input.nodeName === 'INPUT' || input.nodeName === 'TEXTAREA') {
    //     input.value = vals[i];
    //   } else {
    //     if (input.id === 'gender') {
    //       if (vals[i].toLowerCase().startsWith('f')) input.value = 'female';
    //       else if (vals[i].toLowerCase().startsWith('m')) input.value = 'male';
    //       else input.value = 'child';
    //     } else if (input.id === 'childbearing') {
    //       var p = vals[i].indexOf('p') > 0, b = vals[i].indexOf('b') > 0;
    //       if (p && b) input.value = 'pregnantAndBreastfeeding';
    //       else if (p) input.value = 'pregnant';
    //       else if (b) input.value = 'breastfeeding';
    //       else input.value = 'none';
    //     }
    //   }
    // }

    if (breakIndex > 0) {
      desc = desc.substring(breakIndex+1);
      if (index + 1 >= $scope.data.length)
        $scope.data.push({rowId: "row-" + $scope.data.length});

      $scope.checkPaste(index + 1, desc);
    }
  };
}]);