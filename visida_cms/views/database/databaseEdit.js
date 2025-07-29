angular.module('visida_cms').controller('databaseEditController', ['$scope', 'Restangular', '$timeout', '$compile', '$controller', function($scope, Restangular, $timeout, $compile, $controller) {
  $scope.loading = 1;
  $controller('databaseCommon', { $scope: $scope });
  //$scope.fctConfig = $.extend(true, {}, fctConfigStart);

  Restangular.one('FoodCompositionTables').get().then(function(result) {
    $scope.fctables = result.data;
  }).finally(function() { $scope.loading--; });

  $scope.createDatabase = function() {
  	var name = prompt("Please enter a name for the new database.")
    if (!name || name.length <= 0)
      return;

  	var table = {name: name};
  	$scope.loading++;
    Restangular.oneUrl('FoodCompositionTables').post('', table).then(function(response) {
      $scope.fctables.push({name: name, id: response.data.id});
      alert("Table " + name + " was created successfully.");
    }, function(response) {
      alert("Could not create table. " + response.data.message);
    }).finally(function() { $scope.loading--; });
  }

  $scope.parse = function(file) {
    $scope.uploadMessage = {};
    var fctConfig = JSON.parse(JSON.stringify(allfctConfig));

    var reader = new FileReader();
    reader.readAsArrayBuffer (file);
    reader.onload = function(evt) {
      var arraybuffer = reader.result;
		/* convert data to binary string */
		var data = new Uint8Array(arraybuffer);
		var arr = new Array();
		for (var i = 0; i != data.length; ++i) arr[i] = String.fromCharCode(data[i]);
		var bstr = arr.join("");

		/* Call XLSX */
		var workbook = XLSX.read(bstr, {
		type: "binary"
		});

		/* DO SOMETHING WITH workbook HERE */
		var first_sheet_name = workbook.SheetNames[0];
		/* Get worksheet */
		var worksheet = workbook.Sheets[first_sheet_name];
		var json = XLSX.utils.sheet_to_json(worksheet);
		$scope.data = $scope.parseJsonData(json);
		$scope.$apply(function(){
			$scope.pageItems($scope.data);
		});
    };
  };

  $scope.parseJsonData = function(json) {
  	if (json.length == 0)
  		return [];
  	var data = [], headers = [];
  	var config = $.extend(true, {}, fctConfigStart);

  	// var hKeys = Object.keys(json[0]);
  	// for (var i = 0; i < hKeys.length; i++) {
  	// 	var match = config.filter(function(x) { x.external === hKeys[i]});
  	// 	headers.push(match.internal);
  	// }

  	for (var r = 0; r < json.length; r++) {
  		var jRow = json[r];
  		var row = {};
  		var keys = Object.keys(jRow);
  		var configKeys = Object.keys(config);
  		for (var k = 0; k < keys.length; k++) {
  			if (keys[k] === 'Id') {
  				row.originId = jRow[keys[k]];
  				continue;
  			} else if (keys[k] === 'Name') {
  				row.name = jRow[keys[k]];
  				continue;
  			} else if (keys[k] === 'Local Name') {
  				row.alternateName = jRow[keys[k]];
  				continue;
  			}
  			for (var ck = 0; ck < configKeys.length; ck++) {
  				if (config[configKeys[ck]].external === keys[k]) {
  					row[config[configKeys[ck]].internal] = jRow[keys[k]]
  					break;
  				}
  			}
  		}
  		data.push(row);
  	}
  	return data;
  };

  $scope.upload = function() {
    if (!$scope.tableId || !$scope.data || !confirm("Are you sure you want to upload " + $scope.data.length + " food items?"))
      return;
    $scope.loading++;

    $scope.overwrite = true;

    var request = {
      overwrite: true,
      commitMessage: $scope.file.name,
      foodCompositions: $scope.data,
      tableId: $scope.tableId
    };
    Restangular.oneUrl('FoodCompositions/PostFoodItems').post('', request).then(
    function(result) {
      alert("Success! " + result.data + " new food items recorded.");
    }, function(result) {
      alert("Could not upload food items. \n" + result.data.message);
    }).finally(function() { $scope.loading--; });
  };

}]);