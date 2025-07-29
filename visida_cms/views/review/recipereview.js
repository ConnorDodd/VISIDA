angular.module('visida_cms').controller('recipereviewController', ['$scope', 'Restangular', '$routeParams', '$window', '$controller', '$compile', '$timeout', function($scope, Restangular, $routeParams, $window, $controller, $compile, $timeout) {
	$scope.loading = 1;
	$controller('reviewBaseCtrl', { $scope: $scope });

	$scope.search = function() {
		if ($scope.study == null)
			return;
		var url = "Result/Recipe/" +  + $scope.selectedStudy + "?";
		// if ($scope.selectedHousehold) {
		// 	url += 'Household/' + $scope.selectedHousehold;
		// } else if ($scope.selectedStudy) {
		// 	url += 'Study/' + $scope.selectedStudy;
		// } else {
		// 	return;
		// }

		for (var i = 0; i < $scope.searchHouseholds.length; i++) {
			var hh = $scope.searchHouseholds[i];
			if (hh.checked)
				url += "hhid=" + hh.name + "&";
		}

		$scope.loading++;
		Restangular.one(url).get()
		.then(function(result) {
			$scope.data = result.data;
			$scope.fctConfig = $.extend(true, {}, fctConfigStart);
			$scope.updateTable(result.data);
		}, function(error) {
		}).finally(function() { $scope.loading--; });
	};

	$scope.activeRow = null;
	$scope.clickRow = function(recipeId, foodId) {
		if ($scope.activeRow)
			$($scope.activeRow).removeClass("active");
		$scope.activeRow = "#ingredient-row-" + recipeId + '-' + foodId;
		$($scope.activeRow).addClass("active");
	};

	$scope.toggleSort = function(v) {
		if (v === $scope.sortMethod)
			$scope.sortAsc = !$scope.sortAsc;
		else {
			$scope.sortMethod = v;
			$scope.sortAsc = true;
		}
		if ($scope.data) {
			$scope.updateTable($scope.data);
		}
	};
	$scope.sortMethod = 'h';
	$scope.sortAsc = true;
	$scope.classSort = function(v) {
		if (v === $scope.sortMethod) {
			if ($scope.sortAsc)
				return 'glyphicon-menu-down'
			else
				return 'glyphicon-menu-up'
		}
		return 'glyphicon-menu-down arrow-disabled';
	};

	$scope.exportError = null;
	$scope.updateTable = function(data) {

		switch ($scope.sortMethod) {
			case 'n':
				if ($scope.sortAsc)
					$scope.data.sort((a, b) => (a.name > b.name) ? 1 : -1);
				else
					$scope.data.sort((a, b) => (a.name < b.name) ? 1 : -1);
				break;
			case 'h':
				if ($scope.sortAsc)
					$scope.data.sort((a, b) => (a.householdParticipantId > b.householdParticipantId) ? 1 : -1);
				else
					$scope.data.sort((a, b) => (a.householdParticipantId < b.householdParticipantId) ? 1 : -1);
				break;
			case 't':
				if ($scope.sortAsc)
					$scope.data.sort((a, b) => (a.captureTime > b.captureTime) ? 1 : -1);
				else
					$scope.data.sort((a, b) => (a.captureTime < b.captureTime) ? 1 : -1);
				break;
			case 'u':
				if ($scope.sortAsc)
					$scope.data.sort((a, b) => (a.usageCount > b.usageCount) ? 1 : -1);
				else
					$scope.data.sort((a, b) => (a.usageCount < b.usageCount) ? 1 : -1);
				break;
		}
		
		var keys = Object.keys($scope.fctConfig);

		for (var i = 0; i < data.length; i++) {
			var recipe = data[i];
			if (recipe.hidden)
				continue;
			for (var y = 0; y < recipe.ingredients.length; y++) {
				var ing = recipe.ingredients[y];
				for (var z = 0; z < ing.imageRecord.foodItems.length; z++) {
					var fi = ing.imageRecord.foodItems[z];
					if (!fi.foodCompositionDatabaseEntry)
						continue;
					//if (!keys)
						//keys = Object.keys(fi.foodCompositionDatabaseEntry);
					for (var l = keys.length - 1; l >= 0; l--) {
						var key = keys[l];
						if (!$scope.fctConfig.hasOwnProperty(key)) //Exclude inherited variables
							continue;
						if (fi.foodCompositionDatabaseEntry[key] != null) //If at least one variable is not null then show 
							$scope.fctConfig[key].valid = true;
						else
							$scope.fctConfig[key].warning = "There is one or more food composition entries missing this column";
					}
				}
			}
		}

		var count = 0;
		var tableData = [];

		for (var i = 0; i < data.length; i++) {
			var recipe = data[i];
			if (!$scope.showHidden && recipe.hidden)
				continue;
			count++;

			var skips = 1;
			var recipeRow = [];
			recipeRow.push(count);
			recipeRow.push('<a href="#!/recipe?id=' + recipe.id + '">' + recipe.name + '</a>');
			recipeRow.push(recipe.householdParticipantId);
			if ($scope.showDate) {
				skips++;
				recipeRow.push(recipe.captureTime.substring(0, 10));
			}
			if ($scope.showTime) {
				skips++;
				recipeRow.push(recipe.captureTime.substring(11));
			}
			if ($scope.showYF) {
				skips++;
				if (recipe.yieldFactor > 0)
					recipeRow.push(recipe.yieldFactor);
				else
					recipeRow.push('<a target="_blank" href="#!recipe?id=' + recipe.id + '" ><span title="No yield factor has been applied" class="glyphicon glyphicon-warning-sign text-danger"></span></a><span style="display: none;">NONE</span>');
			}

			if ($scope.showEO) {
				skips++;
				if (recipe.usageCount > 0)
					recipeRow.push(recipe.usageCount)
				else
					recipeRow.push('<a target="_blank" href="#!recipe?id=' + recipe.id + '" ><span title="Recipe has not been used" class="glyphicon glyphicon-warning-sign text-danger"></span></a><span style="display: none;">0</span>');
			} 
			if ($scope.showHidden) {
				skips++;
				recipeRow.push(recipe.hidden ? 'Yes' : 'No');
			}
			recipeRow.push(recipe.totalCookedGrams.toFixed(1));
			for (var l = keys.length - 1; l >= 0; l--)
				if ($scope.fctConfig.hasOwnProperty(keys[l]) && $scope.fctConfig[keys[l]].valid && $scope.fctConfig[keys[l]].show)
					recipeRow.push('')

			tableData.push(recipeRow);

			var rawQuantity = 0;
			var raw = {};
			var expErr = {};
			var rowError = false;

			var fiCount = 0;
			for (var y = 0; y < recipe.ingredients.length; y++) {
				var ing = recipe.ingredients[y];
				if (ing.imageRecord.hidden)
					continue;
				for (var z = 0; z < ing.imageRecord.foodItems.length; z++) {
					var fi = ing.imageRecord.foodItems[z];
					fiCount++;
					rawQuantity += fi.quantityGrams;

					var row = []
					var engErr = false;

					for (var l = keys.length - 1; l >= 0; l--) {
						var key = keys[l];
						if (!$scope.fctConfig.hasOwnProperty(key)) //Exclude inherited variables
							continue;

						//If at least one variable is not null then show
						if ($scope.fctConfig[key].valid && $scope.fctConfig[key].show) {
							var isEng = key == 'energykCal' || key == 'energyWoFibre' || key == 'energyWFibre' || key == 'energykJ';
							var amt = (fi.foodCompositionDatabaseEntry[key] ? (fi.foodCompositionDatabaseEntry[key] / 100) * fi.quantityGrams : 0);

							if (raw[key])
								raw[key] += amt;
							else
								raw[key] = amt

							amt = amt.toFixed(1);
							if (fi.foodCompositionDatabaseEntry[key] == null) {
								if ($scope.fctConfig[key].affectsEnergy)
									engErr = true;
								expErr[key] = true;
								row.unshift('*')
							} else if (isEng && engErr) {
								expErr[key] = true;
								row.unshift('<span class="review-nutrient-error">' + amt + '</span>')
							} else {
								row.unshift(amt);
							}
						}
					}

					row.unshift(fi.quantityGrams.toFixed(1));
					for (var s = 0; s < skips; s++)
						row.unshift('');
					row.unshift(fi.foodCompositionDatabaseEntry.name);
					row.unshift(count + '.' + fiCount);

					tableData.push(row);
				}
			}

			var rowR = [count + '.R', 'Total Raw'], rowC = [count + '.C', 'Total Cooked'], rowF = [count + '.F', 'Total Cooked (per 100g)'];
			for (var s = 0; s < skips; s++) {
				rowR.push('');
				rowC.push('');
				rowF.push('');
			}

			rowR.push(rawQuantity.toFixed(2));
			rowC.push(recipe.totalCookedGrams.toFixed(2));
			rowF.push('100');

			for (var l = keys.length - 1; l >= 0; l--) {
				var key = keys[l];
				if (!$scope.fctConfig.hasOwnProperty(key)) //Exclude inherited variables
					continue;
				if ($scope.fctConfig[keys[l]].valid && $scope.fctConfig[keys[l]].show) {
					rowR.push((raw[key] ? raw[key] : 0).toFixed(1));
					rowC.push((recipe.foodComposition[key] / 100 * recipe.totalCookedGrams).toFixed(1));
					rowF.push((recipe.foodComposition[key] ? recipe.foodComposition[key] : 0).toFixed(1));
				}
			}
			tableData.push(rowR);
			tableData.push(rowC);
			tableData.push(rowF);
		}

		var header = [];
		header.push('');
		header.push('<div role="button" ng-click="toggleSort(\'n\')">Name <span class="glyphicon bold ' + $scope.classSort('n') + '"></span></div>');
		header.push('<div role="button" ng-click="toggleSort(\'h\')">Household <span class="glyphicon bold ' + $scope.classSort('h') + '"></span></div>');
		if ($scope.showDate) header.push('Date');
		if ($scope.showTime) header.push('<div role="button" ng-click="toggleSort(\'t\')">Time <span class="glyphicon bold ' + $scope.classSort('t') + '"></span></div>');
		if ($scope.showYF) header.push('Yield Factor');
		if ($scope.showEO) header.push('<div role="button" ng-click="toggleSort(\'u\')">Usage <span class="glyphicon bold ' + $scope.classSort('u') + '"></span></div>');
		if ($scope.showHidden) header.push('Hidden');
		header.push('Weight');
		for (var l = 0; l < keys.length; l++) {
			var key = keys[l];
			if (!$scope.fctConfig.hasOwnProperty(key))
				continue;
			var fc = $scope.fctConfig[key];
			if (fc.valid && fc.show)
				header.push(fc.external);
		}
		tableData.unshift(header);

		var table = document.getElementById('recipe-table');
		frzDisplayTable(table, tableData, 2);

		var th = document.getElementsByClassName("frz-header")[0];
		$compile(th)($scope);

		$scope.showTable = true;
	};

	$scope.export = function() {
		if ($scope.exportError && !confirm("There are items in this export that may require your attention. See items flagged with a red warning symbol. Do you wish to proceed with the export?"))
			return;

		var study = $scope.study.name;
		var hh = $scope.selectedHousehold ? $scope.selectedHousehold :'All';
		var name = 'VisidaRecipes_' + study + '_' + hh + '.xlsx';

		var table = document.getElementById("recipe-table").cloneNode(true);

		var wb = XLSX.utils.book_new();
		var ws1 = XLSX.utils.table_to_sheet(table, { type: 'string', raw:true });
		XLSX.utils.book_append_sheet(wb, ws1, 'Data');

		var recipes = [['', 'Name', 'URL', 'Household', 'Date', 'Time', 'Yield Factor', 'Linked EOs']];
		var urlBase = window.location.href.substring(0, window.location.href.indexOf('#'));
		urlBase += '#!/recipe?id='
		for (var i = 0; i < $scope.data.length; i++) {
			var recipe = $scope.data[i];
			var url = urlBase + recipe.id;
			var row = [i+1, recipe.name, url, recipe.householdParticipantId, recipe.captureTime.substring(0, 10), recipe.captureTime.substring(11), recipe.yieldFactor, recipe.usageCount];
			recipes.push(row);
		}
		var ws2 = XLSX.utils.aoa_to_sheet(recipes);
		XLSX.utils.book_append_sheet(wb, ws2, 'Recipes');

		XLSX.writeFile(wb, name);
		return;
	}
}]);