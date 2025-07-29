app = angular.module('visida_cms');

app.controller('reviewBaseCtrl', ['$scope', 'Restangular', function($scope, Restangular) {
	Restangular.one('GetSearchConfig').get()
	.then(function(result) {
		$scope.searchConfig = result.data;
		//$scope.searchHouseholds = $scope.searchConfig.households;
		if ($scope.selectedStudy)
			$scope.studyChanged();

	}).finally(function() { $scope.loading--; });

	$scope.studyChanged = function() {
		if (!$scope.selectedStudy) {
			$scope.searchHouseholds = $scope.searchConfig.households;
			return;
		}
		//var sid = parseInt($scope.selectedStudy);
		$scope.study = $scope.searchConfig.studies.filter(function(x) { return x.id === $scope.selectedStudy})[0];
		$scope.searchHouseholds = $scope.study.households;
	};

	$scope.toggleAllHouseholds = function(all) {
		for (var i = 0; i < $scope.searchHouseholds.length; i++) {
			var hh = $scope.searchHouseholds[i];
			if (hh.hidden)
				continue;
			hh.checked = all;
		}
		if (all)
			$scope.hhCount = $scope.searchHouseholds.length;
		else
			$scope.hhCount = 0;
	};

	$scope.changeFilterHouseholds = function(filter) {
		var re = buildSearchRegex(filter);
		var cCount = 0, mCount = 0;
		for (var i = 0; i < $scope.searchHouseholds.length; i++) {
			$scope.searchHouseholds[i].hidden = !$scope.searchHouseholds[i].name.match(re);
			if (!$scope.searchHouseholds[i].hidden) {
				mCount++;
				if ($scope.searchHouseholds[i].checked)
					cCount++;
			}
		}
		$scope.allChecked = cCount == mCount;
	};

	$scope.toggleSort = function(v) {
		if (v === $scope.sortMethod)
			$scope.sortAsc = !$scope.sortAsc;
		else {
			$scope.sortMethod = v;
			$scope.sortAsc = true;
		}
		if ($scope.data)
			$scope.drawTable($scope.data);
	};
	$scope.sortMethod = 'p';
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

	$scope.toggleHousehold = function(val) {
		if (!val) {
			$scope.allChecked = false;
			return;
		}
		var all = true;
		var count = 0;
		for (var i = 0; i < $scope.searchHouseholds.length; i++) {
			if (!$scope.searchHouseholds[i].checked)
				all = false;
			else
				count++;
		}
		$scope.hhCount = count;
		$scope.allChecked = all;
	};
}]);

app.controller('reviewController', ['$scope', 'Restangular', '$routeParams', '$window', '$controller', '$compile', function($scope, Restangular, $routeParams, $window, $controller, $compile) {
	$scope.loading = 1;
	$controller('reviewBaseCtrl', { $scope: $scope });

	$scope.search = function() {
		if ($scope.study == null)
			return;
		var url = "Result/Intake/" + $scope.selectedStudy + "?";
		for (var i = 0; i < $scope.searchHouseholds.length; i++) {
			var hh = $scope.searchHouseholds[i];
			if (hh.checked)
				url += "hhid=" + hh.name + "&";
		}
		lastSortMethod = lastSortAsc = null;
		$scope.loading++;
		url += "groupDay=" + (($scope.groupBy === 'day') ? 'true' : 'false');
		Restangular.one(url).get()
		.then(function(result) {
			$scope.data = result.data;
			$scope.updateTable(result.data);
		}, function(error) {
		}).finally(function() { $scope.loading--; });
	};

	$scope.updateTable = function(data) {
		$scope.fctConfig = $.extend(true, {}, fctConfigStart);//Object.assign({}, $scope.fctConfigStart);
		var keys = Object.keys($scope.fctConfig);

		var html = '';
		var count = 0;
		var idC = 1;

		var rows = [];
		for (var hhidx = 0; hhidx < data.length; hhidx++) {
			var hh = data[hhidx];

			for (var hmidx = 0; hmidx < hh.householdMembers.length; hmidx++) {
				var hm = hh.householdMembers[hmidx];

				for (var eridx = 0; eridx < hm.eatRecords.length; eridx++) {
					var er = hm.eatRecords[eridx];
					var imageRecord = er.imageRecord;
 					var leftovers = er.leftovers;
					if (leftovers && leftovers.hidden)
						leftovers = null;

					if (er.hidden || !imageRecord || (imageRecord.hidden && !$scope.showHidden))
						continue; //Skip if admin has hidden
					var date = imageRecord.captureTime.substring(0, 10);

					var row = null;
					if ($scope.groupBy === 'day') 
						row = rows.filter(function(x) { return x.date === date && x.household === hh.participantId && x.member === hm.participantId })[0];
					if (!row && $scope.groupBy !== 'foodItem') {
						row = {
							id: idC++,
							date: date,
							household: hh.participantId,
							member: hm.participantId,
							quantityGrams: 0,
							captureTime: imageRecord.captureTime,
							time: imageRecord.captureTime.substring(11, 16),
							imageRecordId: imageRecord.id,
							errors: [],
							comments: [],
							shared: er.imageRecord.participantCount > 1,// er.imageRecord.guestInfo && er.imageRecord.guestInfo.totalHeads > 1
							hidden: imageRecord.hidden
						};
						// var shared = rows.filter(function(x) { return x.imageRecordId === row.imageRecordId; });
						// if (shared.length > 0) {
						// 	for (var sl = 0; sl < shared.length; sl++)
						// 		shared[sl].shared = true;
						// 	row.shared = true;
						// }
						rows.push(row);
					}

					//Leave an warning message if the record is not checked as completed
					if (!imageRecord.isCompleted && row) {
						row.errors.push({desc: "Record is not completed", id: imageRecord.id});
						// row.error = "One or more record is not completed";
						// row.errorId = imageRecord.id;
					}

					for (var fidx = 0; fidx < imageRecord.foodItems.length; fidx++) {
						var foodItem = imageRecord.foodItems[fidx];

						//If there are any duplicate items, sum their quantity and remove the duplicates
						for (var fidx2 = fidx+1; fidx2 < imageRecord.foodItems.length; fidx2++) {
							var fi2 = imageRecord.foodItems[fidx2];
							if (fi2.foodCompositionId == foodItem.foodCompositionId) {
								foodItem.quantityGrams += fi2.quantityGrams;
								imageRecord.foodItems.splice(fidx2, 1);
								fidx2--;
							}
						}
						if ($scope.groupBy === 'foodItem') {
							row = {
								id: idC++,
								date: date,
								household: hh.participantId,
								member: hm.participantId,
								quantityGrams: 0,
								foodItemName: foodItem.foodCompositionDatabaseEntry ? foodItem.foodCompositionDatabaseEntry.name : 'Unidentified',
								captureTime: imageRecord.captureTime,
								time: imageRecord.captureTime.substring(11, 16),
								imageRecordId: imageRecord.id,
								foodItemId: foodItem.id,
								errors: [],
								comments: [],
								shared: er.imageRecord.guestInfo && er.imageRecord.guestInfo.totalHeads > 1,
								hidden: imageRecord.hidden
							}
							var sharedFi = rows.filter(function(x) { return x.foodItemId === row.foodItemId; });
							if (sharedFi.length > 0) {
								for (var sl = 0; sl < sharedFi.length; sl++)
									sharedFi[sl].shared = true;
								row.shared = true;
							}
							rows.push(row);
						}

						if (!foodItem.foodCompositionDatabaseEntry) {
							if (row) {// && !row.error) {
								row.errors.push({desc: "Record has incomplete identification: " + foodItem.name, id: imageRecord.id});
								// row.error = "Record has incomplete identification";
								// row.errorId = imageRecord.id;
							}
							continue;
						}

						for (var cidx = 0; cidx < imageRecord.comments.length; cidx++)
							row.comments.push(imageRecord.comments[cidx]);
						if (leftovers)
							for (var cidx = 0; cidx < leftovers.comments.length; cidx++)
								row.comments.push(leftovers.comments[cidx]);

						if (!keys)
							keys = Object.keys(foodItem.foodCompositionDatabaseEntry); //all properties of the foodcomposition
						// var pCount = imageRecord.participantCount ? imageRecord.participantCount : 1;
						// if (imageRecord.guestInfo) {
						// 	pCount += imageRecord.guestInfo.totalHeads;
						// } //Old way of doing it, now guestInfo includes participants
						
						//var pCount = imageRecord.guestInfo ? imageRecord.guestInfo.totalHeads : 1;
						var pCount = imageRecord.participantCount ? imageRecord.participantCount : 1;
						if (imageRecord.guestInfo && imageRecord.guestInfo.totalHeads > 0)
							pCount = imageRecord.guestInfo.totalHeads;

						//if (foodItem.quantityGrams != null && foodItem.quantityGrams > 0) {
						row.quantityGrams += foodItem.quantityGrams / pCount;

						//Check each variable to make sure it is valid
						for (var l = keys.length - 1; l >= 0; l--) {
							var key = keys[l];
							if (!$scope.fctConfig.hasOwnProperty(key)) //Exclude inherited variables
								continue;
							if (foodItem.foodCompositionDatabaseEntry[key] != null) //If at least one variable is not null then show 
								$scope.fctConfig[key].valid = true;
							if (foodItem.foodCompositionDatabaseEntry[key] == null) { //If at least one column variable is missing then show a warning
								{
									$scope.fctConfig[key].warning = "There is one or more food composition entries missing this column";
									row[key+"error"] = true;
								}
								// $scope.fctConfig[keys[l]].show = false;
							}
							//Set the nutrient value to that of the food composition * the quantity scaled by 100
							// if (!row[key])
							// 	row[key] = 0;
							// var baseValue = foodItem.foodCompositionDatabaseEntry[key]
							// if (!baseValue) baseValue = 0;
							// row[key] += (baseValue * (foodItem.quantityGrams / 100)) / pCount;
							var amt = foodItem.foodCompositionDatabaseEntry[key];
							if (amt)
								amt = ((amt / 100) * foodItem.quantityGrams) / pCount;
							if (row[key])
								row[key] += amt;
							else
								row[key] = amt;
						}
						//} else 
						if (!foodItem.quantityGrams) {// && !row.error) {
							row.errors.push({desc: "Record has incomplete quantification: " + foodItem.name, id: imageRecord.id});
							// row.error = "Record has incomplete quantification";
							// row.errorId = imageRecord.id;
						}

						if (leftovers && !leftovers.hidden) {
							var leftFoodItemMatches = leftovers.foodItems.filter(function(x) { return x.foodCompositionId === foodItem.foodCompositionId; } );
							if (leftFoodItemMatches.length > 0) {
								var leftFoodItem = leftFoodItemMatches[0];

								//Loop through all other matches and sum the quantity
								for (var lidx2 = 1; lidx2 < leftFoodItemMatches.length; lidx2++) {
									var li2 = leftFoodItemMatches[lidx2]
									leftFoodItem.quantityGrams += li2.quantityGrams;
								}

								if (leftFoodItem.quantityGrams > 0) {
									var diff = foodItem.quantityGrams - leftFoodItem.quantityGrams;
									if (diff < 0)
										row.errors.push({desc: "Leftovers amount exceeds initial amount: " + leftFoodItem.name, id: leftovers.id});

									row.quantityGrams -= leftFoodItem.quantityGrams / pCount;

									for (var l = keys.length - 1; l >= 0; l--) {
										if (!$scope.fctConfig.hasOwnProperty(keys[l]))
											continue;
										var sub = (leftFoodItem.foodCompositionDatabaseEntry[keys[l]] * (leftFoodItem.quantityGrams / 100)) / pCount;
										//if (pCount > 1)
											//sub = sub / pCount;
										row[keys[l]] = (row[keys[l]] ? row[keys[l]] : 0) - sub;
									}
								} else {// if (!row.error) {
									row.errors.push({desc: "Leftovers has incomplete quantification: " + leftFoodItem.name, id: leftovers.id});
									// row.error = "Leftovers has incomplete quantification";
									// row.errorId = leftovers.id;
								}
							} else
								row.errors.push({desc: "Leftovers missing item from record: " + foodItem.name, id: leftovers.id});
						}
					}

					if (leftovers) {
						for (var lidx = leftovers.foodItems.length - 1; lidx >= 0; lidx--) {
							var fi = leftovers.foodItems[lidx];
							var oFoodItem = imageRecord.foodItems.find(function(item) { return item.name === fi.name; });
							if (!oFoodItem && row)
								row.errors.push({desc: "Leftovers includes item not in record: " + fi.name, id: leftovers.id});
						}
					}
				}
			}
		}

		$scope.allRows = rows;
		$scope.drawTable();
	};

	var lastSortMethod, lastSortAsc;
	$scope.drawTable = function() {
		var rows = $scope.allRows;
		var keys = Object.keys($scope.fctConfig);

		if ($scope.groupBy === 'day') {
			$scope.showShared = false; 
			$scope.showTime = false;
		}

		if ($scope.sortMethod != lastSortMethod || $scope.sortAsc != lastSortAsc) {
			lastSortMethod = $scope.sortMethod;
			lastSortAsc = $scope.sortAsc;
			
			switch ($scope.sortMethod) {
				case 'h':
					if ($scope.sortAsc)
						rows.sort((a, b) => (a.household > b.household) ? 1 : -1);
					else
						rows.sort((a, b) => (a.household < b.household) ? 1 : -1);
					break;
				case 'p':
					if ($scope.sortAsc)
						rows.sort((a, b) => (a.member > b.member) ? 1 : -1);
					else
						rows.sort((a, b) => (a.member < b.member) ? 1 : -1);
					break;
				case 'd':
					// if ($scope.sortAsc)
					// 	rows.sort((a, b) => 
					// 		{
					// 			if (a.date === b.date)
					// 				return (a.captureTime.getTime() > b.captureTime.getTime()) ? 1 : -1;
					// 			else
					// 				return (a.date > b.date) ? 1 : -1;
					// 		});
					// else
					// 	rows.sort((a, b) => 
					// 		{
					// 			if (a.date === b.date)
					// 				return (a.captureTime.getTime() < b.captureTime.getTime()) ? 1 : -1;
					// 			else
					// 				return (a.date < b.date) ? 1 : -1;
					// 		});
					if ($scope.sortAsc)
						rows.sort((a, b) => (a.captureTime > b.captureTime) ? 1 : -1);
					else
						rows.sort((a, b) => (a.captureTime < b.captureTime) ? 1 : -1);
					break;
				case 't':
					if ($scope.sortAsc)
						rows.sort((a, b) => (a.time > b.time) ? 1 : -1);
					else
						rows.sort((a, b) => (a.time < b.time) ? 1 : -1);
					break;
				case 'w':
					if ($scope.sortAsc)
						rows.sort((a, b) => (a.quantityGrams > b.quantityGrams) ? 1 : -1);
					else
						rows.sort((a, b) => (a.quantityGrams < b.quantityGrams) ? 1 : -1);
					break;
			}
		}

		var html = '<tbody>'
		for (var i = 0; i < rows.length; i++) {
			var row = rows[i];
			row.quantityGramsF = row.quantityGrams.toFixed(2);
			var tr = '</tr>';
			var engErr = false;
			for (var l = keys.length - 1; l >= 0; l--) {
				var key = keys[l];
				if (!$scope.fctConfig.hasOwnProperty(key)) //Exclude inherited variables
					continue;

				if ($scope.fctConfig[key].valid && $scope.fctConfig[key].show) {
					var isEng = key == 'energykCal' || key == 'energyWoFibre' || key == 'energyWFibre' || key == 'energykJ';
					if (row[key] == null) {
						if ($scope.fctConfig[key].affectsEnergy)
							engErr = true;
						tr = '<td class="review-nutrient-error">*</td>' + tr;
					} else if (row[key+"error"] || (isEng && engErr)) {
						if (!engErr && $scope.fctConfig[key].affectsEnergy)
							engErr = true;
						tr = '<td class="review-nutrient-error">' + row[key].toFixed(1) + '*</td>' + tr;
					} else {
						tr = '<td>' + row[key].toFixed(1) + '</td>' + tr;
					}
				}
			}
			if (row.quantityGrams < 0) 
				tr = '<td class="review-nutrient-error">' + row.quantityGramsF + '</td>' + tr;
			else
				tr = '<td>' + row.quantityGramsF + '</td>' + tr;
			//Shared
			if ($scope.showShared)
				tr = '<td>' + (row.shared ? 'Shared' : 'Own') + '</td>' + tr
			if ($scope.groupBy === 'foodItem')
				tr = '<td style="min-width: 300px;">' + row.foodItemName + '</td>' + tr;
			if ($scope.showTime)
				tr = '<td><a target="_blank" href="#!image?id=' + row.imageRecordId + '">' + row.time + '</a></td>' + tr; //captureTime.substring(11)
			if ($scope.showDate)
				tr = '<td>' + row.date + '</td>' + tr;//
			if ($scope.showPId)
				tr = '<td>' + row.member + '</td>' + tr;
			if ($scope.showHid)
				tr = '<td>' + row.household + '</td>' + tr;

			//Show error/hidden icons
			tr = '</td>' + tr;
			if (row.hidden)
				tr = '<span title="Hidden" class="glyphicon glyphicon-eye-close" style="color: #ababab"></span>' + tr;
			if (row.errors.length > 0)
				tr = '<span title="Error, click for more details" class="glyphicon glyphicon-warning-sign text-danger btn-glyph" ng-click="loadError(' + row.id + ')" data-toggle="modal" data-target="#errorModal" role="button"></span>' + tr;
			tr = '<td>' + tr;

			//Show index #
			tr = '<tr><td style="font-weight: bold;">' + (i + 1) + '</td>' + tr;
			html += tr;
		}
		html += '</tbody>';
		var table = document.getElementById('intake-result-table');
		table.innerHTML = html;
		$compile(table)($scope);

		$scope.showName = $scope.groupBy === 'foodItem';;
	}

	$scope.loadError = function(id) {
		for (var i = $scope.allRows.length - 1; i >= 0; i--) {
			if ($scope.allRows[i].id === id) {
				$scope.errorRow = $scope.allRows[i];
				return;
			}
		}
	};

	$scope.ColCheckChange = function(adG, checked) {
		for (var col in $scope.fctConfig) {
			if (!$scope.fctConfig.hasOwnProperty(col)) continue;

			if ($scope.fctConfig[col].internal.startsWith('adG') == adG)
				$scope.fctConfig[col].show = checked;
		}
	};

	$scope.export = function() {
		var study = $scope.study.name;
		var hh = $scope.selectedHousehold ? $scope.selectedHousehold :'All';
		var type = $scope.groupBy === 'day' ? 'Days' : ($scope.groupBy === 'record' ? 'Records' : 'Items');
		var name = 'VisidaExport_' + study + '_' + hh + '_' + type + '.xlsx';
		//var wb = XLSX.utils.table_to_book(document.getElementById("resultTable"));
		//wb.SheetNames[0] = $scope.study.name;

		var table = document.getElementById("intake-result-table").cloneNode(true);
		for (var i = 0; i < table.rows.length; i++) {
			var row = table.rows[i];
			row.deleteCell(1);
		}
		var trHead = document.getElementById("intake-result-head-tr").cloneNode(true);
		trHead.deleteCell(1);
		var header = table.createTHead();
		header.appendChild(trHead);

		var wb = XLSX.utils.book_new();
		var ws1 = XLSX.utils.table_to_sheet(table, { type: 'string', raw:true });
		XLSX.utils.book_append_sheet(wb, ws1, 'Data');

		var participants = [];
		for (var i = 0; i < $scope.data.length; i++) {
			var hh = $scope.data[i];
			for (var j = 0; j < hh.householdMembers.length; j++) {
				var hm = hh.householdMembers[j];
				var found = false;
				for (var k = 0; k < participants.length; k++) {
					if (participants[k].participantId === hm.participantId) {
						found = true;
						break;
					}
				}
				if (!found) {
					participants.push(hm);
					hm.householdId = hh.participantId;
				}
			}
		}
		var participantData = [['Household', 'Participant', 'Age', 'Gender', 'Lifestage', 'Breastfeeding Status']];
		for (var i = 0; i < participants.length; i++) {
			var hm = participants[i];
			participantData.push([
				hm.householdId,
				hm.participantId,
				hm.age,
				hm.isFemale ? 'Female' : 'Male',
				hm.lifeStage,
				hm.isBreastfed ? 'Yes' : 'No'
			]);
		}
		var ws4 = XLSX.utils.aoa_to_sheet(participantData);
		XLSX.utils.book_append_sheet(wb, ws4, 'Participants');

		var comments = [['', 'Comments']];
		for (var i = 0; i < $scope.allRows.length; i++) {
			var row = $scope.allRows[i];
			var cstr = '';
			for (var cidx = 0; cidx < row.comments.length; cidx++) {
				if (row.comments[cidx].hidden)
					continue;
				cstr += row.comments[cidx].authorName + ': ' + row.comments[cidx].text + ';\r\n';
			}
			comments.push([i+1, cstr]);
		}
		var ws2 = XLSX.utils.aoa_to_sheet(comments);
		XLSX.utils.book_append_sheet(wb, ws2, 'Comments');

		var errors = [['', 'Errors', 'Hidden']];
		for (var i = 0; i < $scope.allRows.length; i++) {
			var row = $scope.allRows[i];
			var estr = '';
			for (var eidx = 0; eidx < row.errors.length; eidx++) {
				estr += row.errors[eidx].desc + '\r\n';
			}
			var hiddenStr = row.hidden ? 'TRUE' : '';
			errors.push([i+1, estr, hiddenStr]);
		}
		var ws3 = XLSX.utils.aoa_to_sheet(errors);
		XLSX.utils.book_append_sheet(wb, ws3, 'Errors');

		XLSX.writeFile(wb, name);
		return;

		var data = new Array();
		var iHeaders = new Array();
		var eHeaders = new Array();
		if ($scope.showDate) { iHeaders.push("date"); eHeaders.push("Date"); }
		if ($scope.showPId) { iHeaders.push("householdMemberParticipantId"); eHeaders.push("Participant Id"); }
		if ($scope.showMeals) { iHeaders.push("meals"); eHeaders.push("# Meals"); }
		if ($scope.showName) { iHeaders.push("name"); eHeaders.push("Food Item Name"); }

		iHeaders.push("quantityGrams"); eHeaders.push("Quantity (g)");

		var keys = Object.keys($scope.fctConfig);
		for (var i = 0; i < keys.length; i++) {
			if (!$scope.fctConfig.hasOwnProperty(keys[i]))
			continue;
		var col = $scope.fctConfig[keys[i]]
			if (col.valid && col.show) {
				iHeaders.push(col.internal);
				eHeaders.push(col.external);
			}
		}
		data.push(eHeaders);
		for (var i = 0; i < $scope.tableRows.length; i++) {
			var row = $scope.tableRows[i];
			var rowData = new Array()
			for (var j = 0; j < iHeaders.length; j++) {
				if (row.hasOwnProperty(iHeaders[j]))
					rowData.push(row[iHeaders[j]]);
				else
					rowData.push("");
			}
			data.push(rowData);
		}
		var csv = $.csv.fromArrays(data);

		var file = new Blob([csv], {
		    type : 'application/csv'
		});
		//trick to download store a file having its URL
		var fileURL = URL.createObjectURL(file);
		var a         = document.createElement('a');
		a.href        = fileURL; 
		a.target      = '_blank';
		a.download    = "export.csv";
		document.body.appendChild(a);
		a.click();
	};

	// function setModalMaxHeight(element) {
	// this.$element     = $(element);  
	// this.$content     = this.$element.find('.modal-content');
	// var borderWidth   = this.$content.outerHeight() - this.$content.innerHeight();
	// var dialogMargin  = $(window).width() < 768 ? 20 : 60;
	// var contentHeight = $(window).height() - (dialogMargin + borderWidth);
	// var headerHeight  = this.$element.find('.modal-header').outerHeight() || 0;
	// var footerHeight  = this.$element.find('.modal-footer').outerHeight() || 0;
	// var maxHeight     = contentHeight - (headerHeight + footerHeight);

	// this.$content.css({
	//   'overflow': 'hidden'
	// });

	// this.$element
	// .find('.modal-body').css({
	//   'max-height': maxHeight,
	//   'overflow-y': 'auto'
	// });
	// };

	// $('.modal').on('show.bs.modal', function() {
	// $(this).show();
	// setModalMaxHeight(this);
	// });

	// $(window).resize(function() {
	// if ($('.modal.in').length != 0) {
	// setModalMaxHeight($('.modal.in'));
	// }
	// });

}]);


angular.module('visida_cms').controller('graphController', ['$scope', 'Restangular', '$routeParams', '$window', '$controller', '$compile', function($scope, Restangular, $routeParams, $window, $controller, $compile) {
	$scope.loading = 2;
	$controller('commonCtrl', { $scope: $scope });

	// Restangular.one('GetSearchConfig').get()
	// .then(function(result) {
	// 	$scope.searchConfig = result.data;
	// 	$scope.searchHouseholds = $scope.searchConfig.households;
	// }).finally(function() { $scope.loading--; });

	Restangular.one('FoodCompositions/FoodGroups').get()
	.then(function(result) {
		$scope.foodGroups = result.data;
	}).finally(function() { $scope.loading--; });

	// $scope.studyChanged = function() {
	// 	if (!$scope.selectedStudy) {
	// 		$scope.searchHouseholds = $scope.searchConfig.households;
	// 		return;
	// 	}
	// 	//var sid = parseInt($scope.selectedStudy);
	// 	var study = $scope.searchConfig.studies.filter(function(x) { return x.id === $scope.selectedStudy})[0];
	// 	$scope.searchHouseholds = study.households;
	// 	$scope.selectedHouseholdId = null;

	// 	$scope.loading++;
	// 	Restangular.one('Studies/' + $scope.selectedStudy).get().then(function(result) {
	//   		$scope.study = result.data;
	//   		$scope.searchMembers = [];
	//   		if ($scope.selectedHousehold) {
	//   			var hh = $scope.study.households.filter(function(x) { return x.participantId === $scope.selectedHousehold })[0];
	//   			if (hh) {
	//   				$scope.selectedHouseholdId = hh.id;
	//   				$scope.searchMembers = hh.householdMembers;
	//   			} else {
	//   				$scope.selectedHouseholdId = null;
	//   				$scope.selectedHousehold = null;
	//   			}
	//   		}
	//   		if ($scope.searchMembers.length < 1) {
	//   			var members = [];
	// 				for (var i = 0; i < $scope.study.households.length; i++)
	// 					members = members.concat($scope.study.households[i].householdMembers);
	// 				$scope.searchMembers = members;
	//   		}
	//   	}).finally(function() { $scope.loading--; });
	// };

	// $scope.householdChanged = function() {
	// 	if (!$scope.selectedHousehold) {
	// 		var members = [];
	// 		for (var i = 0; i < $scope.study.households.length; i++)
	// 			members = members.concat($scope.study.households[i].householdMembers);
	// 		$scope.searchMembers = members;
	// 		return;
	// 	}
	// 	if (!$scope.selectedStudy) {
	// 		for (var i = 0; i < $scope.searchConfig.studies.length; i++) {
	// 			if ($scope.searchConfig.studies[i].households.includes($scope.selectedHousehold)) {
	// 				$scope.selectedStudy = $scope.searchConfig.studies[i].id;
	// 				$scope.studyChanged();
	// 				break;
	// 			}
	// 		}
	// 	}
	// 	else {
	// 		var hh = $scope.study.households.filter(function(x) { return x.participantId === $scope.selectedHousehold })[0];
	// 			if (hh) {
	// 				$scope.selectedHouseholdId = hh.id;
	// 				$scope.searchMembers = hh.householdMembers;
	// 			} else {
	// 				$scope.selectedHouseholdId = null;
	// 				$scope.selectedHousehold = "";
	// 			}
	// 	}
	// };

	// $scope.memberChanged = function(member) {
	// 	if ($scope.study) {
	// 		if (!$scope.selectedHousehold) {
	//   			var hh = $scope.study.households.filter(function(x) { return x.householdMembers.filter(function(y) { return y.id === member; }).length > 0 })[0];
	//   			if (hh) {
	//   				$scope.selectedHousehold = hh.participantId;
	//   				$scope.searchMembers = hh.householdMembers;
	//   			}
	// 		}
	// 	}
	// };

	$scope.search = function() {
		if ($scope.selectedStudy == null)
			return;
		$scope.loading+=2;
		$scope.studyDetails = null;
		$scope.data = null;

		$scope.clearAllCharts();

		Restangular.one('Studies/' + $scope.selectedStudy).get().then(function(result) {
			$scope.studyDetails = result.data;
			$scope.updateTable();
		}).finally(function() { $scope.loading--; });

	  	var url = "Result/Intake/" + $scope.selectedStudy + "?hhid=" + $scope.selectedHousehold;
		Restangular.one(url).get()
		.then(function(result) {
			$scope.data = result.data;
			$scope.updateTable();
		}).finally(function() { $scope.loading--; });
	};

	$scope.selectedRowId = null;
	$scope.selectedRow = null;

	$scope.findMember = function(memberId) {
		if (!memberId)
			return null;
		// for (var i = 0; i < $scope.studyDetails.households.length; i++)
		// 	for (var j = 0; j < $scope.studyDetails.households[i].householdMembers.length; j++)
		// 		if ($scope.studyDetails.households[i].householdMembers[j].participantId === memberId)
		// 			return $scope.studyDetails.households[i].householdMembers[j];
		for (var i = 0; i < $scope.members.length; i++)
			if ($scope.members[i].participantId === memberId)
				return $scope.members[i];
		return null;
	};


	$scope.findRow = function(rowId) {
		if (!rowId)
			return null;
		return $scope.rows.filter(function(x) { return x.rowId === rowId; })[0];
	};

	function getColorForPerc(perc, alpha) {
		if (typeof perc === "string")
			perc = parseInt(perc);
		var max = 210, min = 40;
		var r = min, g = min, b = min;
		var dist = Math.abs(100 - perc);

		if (perc > 100) {
    		b = max;
		} else if (dist > 50) {
			r = max;
			g = max - max * (((dist - 50) * 2) / 100);
		} else {
			g = max;
			r = min + max * ((dist * 2) / 100);
		}

		if (alpha)
			return "rgba("+r+","+g+","+b+", " + alpha + ")";
		else
			return "rgb("+r+","+g+","+b+")";
	};

	var colorList = ["#F7816E","#F97E87","#F2809F","#E287B6","#CA91C8","#AA9CD4","#86A5D8","#5EADD4","#34B3C8","#12B7B5","#25B99D","#47B884","#67B66B","#85B255","#A1AC44","#BCA53D","#D39B41"];
	function getColorForIndex(index, total) {
		var skip = Math.floor(colorList.length / total);
		return colorList[index * skip];
	}
	function getColorsFromBaseHue(baseColor, total) {
		var h, s, l;
		if (baseColor.startsWith('hsl')) {
			var result = /^hsl\(([\d.]*), ([\d]*)%, ([\d]*)%\)$/i.exec(baseColor);

			h = Math.round(parseFloat(result[1]))
			s = parseInt(result[2])
			l = parseInt(result[3])
		} else {
		    var result = /^#?([a-f\d]{2})([a-f\d]{2})([a-f\d]{2})$/i.exec(baseColor);

		    var r = parseInt(result[1], 16);
		    var g = parseInt(result[2], 16);
		    var b = parseInt(result[3], 16);

		    r /= 255, g /= 255, b /= 255;
		    var max = Math.max(r, g, b), min = Math.min(r, g, b);
		    l = (max + min) / 2;

		    if(max == min){
		        h = s = 0; // achromatic
		    } else {
		        var d = max - min;
		        s = l > 0.5 ? d / (2 - max - min) : d / (max + min);
		        switch(max) {
		            case r: h = (g - b) / d + (g < b ? 6 : 0); break;
		            case g: h = (b - r) / d + 2; break;
		            case b: h = (r - g) / d + 4; break;
		        }
		        h /= 6;
		    }

			s = Math.round(100 * s);
			l = Math.round(100 * l);
			h = Math.round(360*h);
		}

	    var spacing = 15, hueStart = h - ((spacing * total) / 2);
	    var colors = [];
	    for (var i = 0; i < total; i++) {
	    	var hue = hueStart + (spacing * i);
	    	if (hue < 0)
	    		hue = 360 + hue;

	    	colors.push('hsl(' + hue + ', ' + s + '%, ' + l + '%)');
	    }
	    return colors;
	}

	Chart.Tooltip.positioners.exact = function(elements, eventPosition) {
	    /** @type {Chart.Tooltip} */
	    var tooltip = this;

	    /* ... */

	    return {
	        x: tooltip._eventPosition.x,
	        y: tooltip._eventPosition.y
	    };
	};

	$scope.clearAllCharts = function() {
		if ($scope.rdaChart) {
			$scope.rdaChart.destroy();
			$scope.rdaChart = null;
			$('#chart-area-rda').remove(); // this is my <canvas> element
			$('#rda-chart-container').append('<canvas id="chart-area-rda"><canvas>');
		}
		if ($scope.leftChart) {
			$scope.leftChart.destroy();
			$scope.leftChart = null;
			$('#chart-area-left').remove(); // this is my <canvas> element
			$('#left-chart-container').append('<canvas id="chart-area-left"><canvas>');
		}
		while ($scope.nutrientGraphs.length > 0) {
			$scope.nutrientSelector[$scope.nutrientGraphs[0].key] = false;
			if ($scope.nutrientGraphs[0].chart)
				$scope.nutrientGraphs[0].chart.destroy();
			$scope.nutrientGraphs.splice(0, 1)
		}
	};

	$scope.graphRow = function(rowId, memberId) {
		$scope.rdaError = "";
		var row = $scope.findRow(rowId);

		//Cleanup old charts
		$scope.clearAllCharts();

		var member = $scope.findMember(memberId);
		if (!member || !$scope.studyDetails.rdaModel || $scope.studyDetails.rdaModel.rdAs.length <= 0)
			return;
		$scope.selectedDateRowLabelLine1 = (row.isAverage ? 'Average' : row.date) + ': ' + member.participantId
		$scope.selectedDateRowLabelLine2 = member.age + 'Yrs ' + (member.isFemale ? 'Female' : 'Male');
		if (member.age < 5)
			$scope.selectedDateRowLabelLine2 += '  Is Breastfed: ' + (member.isBreastfed ? 'Yes' : 'No');
		if (member.lifeStage && member.lifeStage !== 'None')
		 	$scope.selectedDateRowLabelLine2 += '  Lifestage: ' + member.lifeStage;

		if (row.isAverage)
			$scope.selectedAvg = memberId;
		else
			$scope.selectedAvg = null;

		var data = [];
		var labels = [];
		$scope.selectedRda = member.rda;
		var rda = member.rda;
		if (!rda) {
			$scope.rdaError = "Member does not match any RDA row.";
			return;
		}

		var rdaKeys = Object.keys(rda);
		for (var i = 0; i < rdaKeys.length; i++) {
			var k = rdaKeys[i];
			if (!$scope.fctConfig.hasOwnProperty(k) || !$scope.fctConfig[k].valid || !$scope.fctConfig[k].show || !rda[k])
				continue;
			var r1 = row[k], r2 = rda[k];
			//var perc = Math.round(((rda[k] - row[k]) / rda[k]) * 100);
			var perc = Math.round((r1 / r2) * 100);

			// var perc = ((r1 - r2) / ((r1+r2)/2)) * 100;
			//perc = Math.round(perc);
			//perc = Math.abs(perc);

			labels.push($scope.fctConfig[k].external);
			data.push(perc);
		}

		var colors = [], borders = [];
		for (var i = 0; i < data.length; i++) {
			colors.push(getColorForPerc(data[i], '0.4'));
			borders.push(getColorForPerc(data[i], '0.8'));
		}

		var date = new Date(row.date);
		var barChartData = {
			labels: labels,
			datasets: [{
				label: memberId + ': ' + (row.date ? (date.getDate() + '/' + date.getMonth() + '/' + date.getFullYear()) : 'average'),
				backgroundColor: colors,//color(window.chartColors.red).alpha(0.5).rgbString(),
				borderColor: borders,
				borderWidth: 1,
				// barThickness: 20,
				data: data,
				hoverBackgroundColor: colors,
				hoverBorderColor: borders,
				xAxisID: 'name-axis',
				yAxisID: 'left-axis'
			}]

		};

		var ctx = document.getElementById('chart-area-rda').getContext('2d');
		$scope.rdaChart = new Chart(ctx, {
			type: 'horizontalBar',
			data: barChartData,
			options: {
				// responsive: true,
				maintainAspectRatio: false,
				legend: {
					display: false,
					position: 'top',
				},
				title: {
					display: true,
					text: ['Nutrient comparison to RDI (%)',
						$scope.selectedDateRowLabelLine1,
						$scope.selectedDateRowLabelLine2
					]
				},
				scales: {
					xAxes: [{
						barThickness: 60,
						id: 'name-axis',
						maxRotation: 90,
						minRotation: 90,
						ticks: {
							//autoSkip: false
							max: 125,
							min: 0,
							stepSize: 25
						}
					}],
					yAxes: [{
						id: 'left-axis',
						scaleLabel: {
							labelString: "% of RDA"
						}
					}]
				},
				tooltips: {
					position: 'exact',
					custom: function(tooltip) {
						if (!tooltip) return;
						tooltip.displayColors = false;
					},
					callbacks: {
						label: function(tooltipItem, data) {
							var label = data.datasets[tooltipItem.datasetIndex].label || '';
							var str = [];
							if (label) {
								var key;
								var keys = Object.keys($scope.fctConfig);
								for (var i = 0; i < keys.length; i++) {
									if ($scope.fctConfig[keys[i]].external === tooltipItem.yLabel) {
										key = keys[i];
										break;
									}
								}
								var idx = tooltipItem.yLabel.lastIndexOf('(');
								var measure = '';
								if (idx > 0)
									measure = tooltipItem.yLabel.substring(idx + 1, tooltipItem.yLabel.length - 1);

								str.push('');
								str.push('Intake: ' + row[key].toFixed(2) + ' ' + measure + '/day');
								str.push('RDI: ' + $scope.selectedRda[key] + ' ' + measure + '/day');
								str.push('%RDI: ' + data.datasets[tooltipItem.datasetIndex].data[tooltipItem.index]);
							}

							return str;
						}
					}
				}
			}
		});
		$scope.rdaChart.options.onClick = $scope.nutrientClickedGraphHandler; //Assigns click event for clicking on a nutrient
		$scope.graphFoodGroup(rowId);
	};

	$scope.getFilterLabel = function(filter) {
		if (filter === 'recipes')
			return 'Household Recipes'
		var fg = $scope.foodGroups.filter(function(x) { return x.foodGroupId === filter})[0];
		return fg.description;
	}

	$scope.ColCheckChange = function(adG, checked) {
		for (var col in $scope.fctConfig) {
			if (!$scope.fctConfig.hasOwnProperty(col)) continue;

			if ($scope.fctConfig[col].internal.startsWith('adG') == adG)
				$scope.fctConfig[col].show = checked;
		}
	};

	$scope.nutrientGraphs = [];
	$scope.nutrientSelector = {};

	$scope.nutrientClickedGraphHandler = function(evt, chrt) {
		var points = $scope.rdaChart.getElementsAtEvent(evt);
		var chartData = points[0]['_chart'].config.data;
		var idx = points[0]['_index'];
		var label = chartData.labels[idx];
		var key;

		var keys = Object.keys($scope.fctConfig);
		for (var i = 0; i < keys.length; i++) {
		 	if ($scope.fctConfig[keys[i]].external === label) {
		 		key = keys[i];
		 		$scope.nutrientSelector[key] = true;
		 		$scope.nutrientClickedCheckboxHandler(key);
		 		$scope.$apply();
		 		return;
		 	}
	 	}
	};

	$scope.nutrientClickedCheckboxHandler = function(key) {
		if ($scope.nutrientSelector[key] === false) {
			for (var i = 0; i < $scope.nutrientGraphs.length; i++) {
				if ($scope.nutrientGraphs[i].key === key) {
					$scope.nutrientGraphs[i].chart.destroy();
					$scope.nutrientGraphs.splice(i, 1)
					for (var i = 0; i < $scope.nutrientGraphs.length; i++)
						$scope.drawNutrientGraph(i);
					break;
				}
			}
			return;
		}

		if ($scope.nutrientGraphs.length == 4) {
			$scope.nutrientSelector[$scope.nutrientGraphs[0].key] = false;
		}

		$scope.nutrientClicked(null, null, key);
	};

	$scope.nutrientClicked = function(filter, index, key) {
		var zoom = 2, label;

		if (!key) {
			key = $scope.nutrientGraphs[index].key;

			if (filter.length == 3) zoom = 5;
			else if (filter.length == 2) zoom = 3;
		}
		label = $scope.fctConfig[key].external;

		var totals = [], labels = [];
		if (filter === 'recipes') {
			for (var hhidx = 0; hhidx < $scope.data.length; hhidx++) {
				var hh = $scope.data[hhidx];
				for (var hmidx = 0; hmidx < hh.householdMembers.length; hmidx++) {
					hm = hh.householdMembers[hmidx];
					if (hm.participantId !== $scope.selectedRow.memberId)
						continue;

					for (var eridx = 0; eridx < hm.eatRecords.length; eridx++) {
						var er = hm.eatRecords[eridx];
						if (er.date !== $scope.selectedRow.date && !$scope.selectedRow.isAverage)
							continue;

						var ir = er.imageRecord;
						for (var k = 0; k < ir.foodItems.length; k++) {
							var fi = ir.foodItems[k];

							if (fi.quantityGrams <= 0 || !fi.foodCompositionDatabaseEntry || fi.foodCompositionDatabaseEntry.table_Id !== null || !fi.foodCompositionDatabaseEntry[key])
								continue;

							var l;
							for (l = 0; l < labels.length; l++) {
								if (labels[l] === fi.foodCompositionDatabaseEntry.name) {
									totals[l] += (fi.foodCompositionDatabaseEntry[key] / 100) * fi.quantityGrams;
									break;
								}
							}
							if (l == labels.length) {
								labels.push(fi.foodCompositionDatabaseEntry.name);
								totals.push((fi.foodCompositionDatabaseEntry[key] / 100) * fi.quantityGrams);
							}
						}
					}
				}
			}

			for (var i = 0; i < labels.length; i++)
				totals[i] = Math.round(totals[i] * 100) / 100;
		} else {
			//Loop through all data that matches the selected row date and member
			for (var hhidx = 0; hhidx < $scope.data.length; hhidx++) {
				var hh = $scope.data[hhidx];
				for (var hmidx = 0; hmidx < hh.householdMembers.length; hmidx++) {
					hm = hh.householdMembers[hmidx];
					if (hm.participantId !== $scope.selectedRow.memberId)
						continue;

					for (var eridx = 0; eridx < hm.eatRecords.length; eridx++) {
						var er = hm.eatRecords[eridx];
						if (er.date !== $scope.selectedRow.date && !$scope.selectedRow.isAverage)
							continue;

						//Basically trims the food group to the zoom level, like 2012762 will get trimmed to 20, meat or zoomed in to 201 meat, chicken
						var ir = er.imageRecord;
						for (var k = 0; k < ir.foodItems.length; k++) {
							var fi = ir.foodItems[k];
							if (fi.quantityGrams <= 0 || !fi.foodCompositionDatabaseEntry || !fi.foodCompositionDatabaseEntry[key])
								continue;
							var l;
							var fgStr = (fi.foodCompositionDatabaseEntry.foodGroupId + '').substring(0, zoom);
							//If the filter has been clicked, we're just showing ones where the first zoom digits match
							if (filter && !fgStr.startsWith(filter))
								continue;
							//If the label for that food group is already set, add the weight of this item to it.
							for (l = 0; l < labels.length; l++) {
								if (labels[l] === fgStr) {
									totals[l] += (fi.foodCompositionDatabaseEntry[key] / 100) * fi.quantityGrams;
									break;
								}
							}
							//If it wasn't found, add it in
							if (l == labels.length) {
								labels.push(fgStr);
								totals.push((fi.foodCompositionDatabaseEntry[key] / 100) * fi.quantityGrams);
							}
						}
					}
				}
			}

			//Labels need to be converted from id to readable e.g. 201 to meat
			for (var i = 0; i < labels.length; i++) {
				totals[i] = Math.round(totals[i] * 100) / 100;

				var fg = $scope.foodGroups.filter(function(x) { return x.foodGroupId === labels[i]})[0];
				if (fg)
					labels[i] = fg.description;
				else if (labels[i] === '0')
					labels[i] = 'Household Recipes';
				else
					labels[i] = 'Unknown';
			}
		}

		var colors = [];
		var colors = [];
		if (filter) {
			var graph = $scope.nutrientGraphs[index];
			colors = getColorsFromBaseHue(graph.filterColors[graph.filterColors.length - 1], totals.length);
		} else {
			for (var i = 0; i < totals.length; i++) 
				colors.push(getColorForIndex(i, totals.length));
		}
		var dataset = {
			data: totals,
			label: "Work Done",
			backgroundColor: colors,
			hoverBackgroundColor: colors,
			hoverBorderColor: '#FFF'
		};
		var config = {
			type: 'pie',
			data: {
				datasets: [dataset],
				labels: labels
			},
			options: {
				maintainAspectRatio: false,
				// responsive: true,
				title: {
					display: true,
					text: ['Sources of ' + label,
						$scope.selectedDateRowLabelLine1,
						$scope.selectedDateRowLabelLine2
					],
					position: 'top'
				},
				legend: {
					position: 'bottom'
				},
				tooltips: {
					callbacks: {
						label: function(tooltipItem, data) {

							var label = data.labels[tooltipItem.index];
							var sum = 0;
							var value = data.datasets[tooltipItem.datasetIndex].data[tooltipItem.index];
							data.datasets[tooltipItem.datasetIndex].data.map(x => {
								sum += x;
							});
							var perc = (value / sum) * 100;

							var title = this._chart.config.options.title.text[0];
							var idx = title.lastIndexOf('(');
							var measure = '';
							if (idx > 0)
								measure = title.substring(idx + 1, title.length - 1);

							label += ': ' + perc.toFixed(1) + '%  ' + value.toFixed(1) + measure;
							return label;
						}
					}
				}
			}
		};


		if (index !== null && index !== undefined) {
			var graph = $scope.nutrientGraphs[index];
			graph.config = config;
			graph.zoom = zoom;
			// graph.filter = filter;
			if (filter) {
				graph.filterLabel = $scope.getFilterLabel(filter);
			}
			$scope.drawNutrientGraph(index)
		} else {
			var graph = {
				config: config,
				chart: null,
				key: key,
				zoom: zoom,
				filterColors: []
				// filter: filter
			}
			if ($scope.nutrientGraphs.length < 4) {
				$scope.nutrientGraphs.push(graph);
				$scope.drawNutrientGraph($scope.nutrientGraphs.length - 1)
			} else {
				if ($scope.nutrientGraphs[0].chart)
					$scope.nutrientGraphs[0].chart.destroy();
				$scope.nutrientGraphs.splice(0, 1);
				$scope.nutrientGraphs.push(graph);
				for (var i = 0; i < 4; i++)
					$scope.drawNutrientGraph(i);
			}
		}
	}

	$scope.drawNutrientGraph = function(index) {
		var chartName = 'chart-area-nutrient-' + index, containerName = 'nutrient-chart-container-' + index;

		if ($scope.nutrientGraphs[index].chart) {
			$scope.nutrientGraphs[index].chart.destroy();
			// $('#' + chartName).remove();// this is my <canvas> element
			// $('#' + containerName).append('<canvas id="' + chartName + '"><canvas>');
		}
		var workCtx = document.getElementById(chartName).getContext('2d');
		var chart = new Chart(workCtx, $scope.nutrientGraphs[index].config);
		chart.options.onClick = $scope.zoomNutrient;
		$scope.nutrientGraphs[index].chart = chart;
	}

	$scope.graphFoodGroup = function(rowId, filter) {
		//change row color
		if (!filter) {
			$scope.currentFilterColors = [];
			$scope.zoomLevel = 2;
		}
		else
			$scope.filterLabel = $scope.getFilterLabel(filter);
		var row = $scope.findRow(rowId);
		if ($scope.selectedRowId)
			var e = $($scope.selectedRowId).removeClass("active");
		$scope.selectedRowId = "#table-row-" + row.rowId;
		$($scope.selectedRowId).addClass("active");
		$scope.selectedRow = row;

		$scope.rowCount = 0;
		var data = [];
		var labels = [];
		if (filter === 'recipes') {
			for (var hhidx = 0; hhidx < $scope.data.length; hhidx++) {
				var hh = $scope.data[hhidx];
				for (var hmidx = 0; hmidx < hh.householdMembers.length; hmidx++) {
					hm = hh.householdMembers[hmidx];
					if (hm.participantId !== $scope.selectedRow.memberId)
						continue;

					for (var eridx = 0; eridx < hm.eatRecords.length; eridx++) {
						var er = hm.eatRecords[eridx];
						if (er.date !== $scope.selectedRow.date && !$scope.selectedRow.isAverage)
							continue;

						var ir = er.imageRecord;
						for (var k = 0; k < ir.foodItems.length; k++) {
							var fi = ir.foodItems[k];

							if (fi.quantityGrams <= 0 || !fi.foodCompositionDatabaseEntry || fi.foodCompositionDatabaseEntry.table_Id !== null)
								continue;

							var l;
							for (l = 0; l < labels.length; l++) {
								if (labels[l] === fi.foodCompositionDatabaseEntry.name) {
									data[l] += fi.quantityGrams;
									break;
								}
							}
							if (l == labels.length) {
								labels.push(fi.foodCompositionDatabaseEntry.name);
								data.push(fi.quantityGrams);
							}
						}
					}
				}
			}

			for (var i = 0; i < labels.length; i++)
				data[i] = Math.round(data[i] * 100) / 100;
		} else {
			for (var i = 0; i < row.foodGroups.length; i++) {
				var fg = row.foodGroups[i];

				var j = 0;
				var ls = fg.foodGroup + '';
				if (filter && !ls.startsWith(filter))
					continue;
				if (fg.grams <= 0)
					continue;
				for (j = 0; j < labels.length; j++) {
					//if (labels[j] === ls.substring(0, filter ? filter.length + 1 : 2)) {
					if (labels[j] === ls.substring(0, $scope.zoomLevel)) {	
						data[j] += fg.grams;
						break;
					}
				}
				if (j === labels.length) {
					var ls = fg.foodGroup + '';
					//labels.push(ls.substring(0, filter ? filter.length + 1 : 2));
					//if ($scope.zoomLevel == 0)
					labels.push(ls.substring(0, $scope.zoomLevel));
					data.push(fg.grams);
				}
			}

			for (var i = 0; i < labels.length; i++) {
				data[i] = Math.round(data[i] * 100) / 100;

				if (labels[i] === '0') {
					labels[i] = 'Unknown';
					continue;
				} else if (labels[i] === '-1') {
					labels[i] = 'Household Recipes';
					continue;
				}
				var fg = $scope.foodGroups.filter(function(x) { return x.foodGroupId === labels[i]})[0];
				if (fg)
					labels[i] = fg.description;
			}
		}
		var colors = [];
		if (filter)
			colors = getColorsFromBaseHue($scope.currentFilterColors[$scope.currentFilterColors.length - 1], data.length);
		else {
			for (var i = 0; i < data.length; i++) 
				colors.push(getColorForIndex(i, data.length));
		}
		var dataset = {data: data, label: "Work Done", backgroundColor:colors, hoverBackgroundColor: colors, hoverBorderColor: '#FFF'};
		$scope.workConfig = {
			type: 'pie',
			data: {
				datasets: [dataset],
				labels: labels
			},
			options: {
				responsive: true,
				title: {
					display: true,
					text: ['Food groups consumed (g)',
						$scope.selectedDateRowLabelLine1,
						$scope.selectedDateRowLabelLine2
					],
					position: 'top'
				},
				legend: {
					position: 'bottom'
				},
				tooltips: {
					callbacks: {
						label: function(tooltipItem, data) {

							var label = data.labels[tooltipItem.index];
							var sum = 0;
							var value = data.datasets[tooltipItem.datasetIndex].data[tooltipItem.index];
							data.datasets[tooltipItem.datasetIndex].data.map(x => {
								sum += x;
							});
							var perc = (value / sum) * 100;

							var title = this._chart.config.options.title.text[0];
							var idx = title.lastIndexOf('(');
							var measure = '';
							if (idx > 0)
								measure = title.substring(idx + 1, title.length - 1);

							label += ': ' + perc.toFixed(1) + '%  ' + value.toFixed(1) + measure;
							return label;
						}
					}
				}
			}
		};
		// var workCtx = document.getElementById('chart-area-activity').getContext('2d');
		// $scope.workChart = new Chart(workCtx, $scope.workConfig);

		if ($scope.leftChart) {
			$scope.leftChart.destroy();
			$scope.leftChart = null;
			$('#chart-area-left').remove(); // this is my <canvas> element
			$('#left-chart-container').append('<canvas id="chart-area-left"><canvas>');
		}
		//actually on the right rip
		var workCtx = document.getElementById('chart-area-left').getContext('2d');
		$scope.leftChart = new Chart(workCtx, $scope.workConfig);
		$scope.leftChart.options.onClick = $scope.zoomFoodGroup;
	};

	$scope.zoomLevel = 2;
	$scope.currentFilter = "";
	$scope.currentFilterColors = [];
	$scope.zoomFoodGroup = function(evt) {
		var points = $scope.leftChart.getElementsAtEvent(evt);
		var chartData = points[0]['_chart'].config.data;
		var idx = points[0]['_index'];
		var label = chartData.labels[idx];
		var color = chartData.datasets[0].backgroundColor[idx];

		if (label === 'Household Recipes') {
			$scope.currentFilter = 'recipes';
			$scope.zoomLevel = 999;
		} else {
			var fg = $scope.foodGroups.filter(function(x) { return x.description === label})[0];
			if (!fg)
				return;
			$scope.currentFilter = fg.foodGroupId;

			if ($scope.zoomLevel == 2)
				$scope.zoomLevel = 3;
			else if ($scope.zoomLevel == 3)
				$scope.zoomLevel = 5;
			else if ($scope.zoomLevel == 5)
			 	return;
		}

		$scope.currentFilterColors.push(color);
		$scope.graphFoodGroup($scope.selectedRow.rowId, $scope.currentFilter);
		$scope.$apply();
	};

	$scope.unzoomFoodGroup = function() {
		if ($scope.currentFilter === 'recipes') {
			$scope.currentFilter = '';
			$scope.zoomLevel = 2;
		}
		else if ($scope.zoomLevel == 5) {
			$scope.zoomLevel = 3;
			$scope.currentFilter = $scope.currentFilter.substring(0, 2);
		}
		else if ($scope.zoomLevel == 3) {
			$scope.zoomLevel = 2;
			$scope.currentFilter = "";
		}

		$scope.currentFilterColors.splice($scope.currentFilterColors.length-1, 1);
		$scope.graphFoodGroup($scope.selectedRow.rowId, $scope.currentFilter);
	};

	$scope.zoomNutrient = function(evt) {
		var id = evt.currentTarget.id;
		var index = parseInt(id.substring(20));
		var graph = $scope.nutrientGraphs[index];

		var points = $scope.nutrientGraphs[index].chart.getElementsAtEvent(evt);
		var chartData = points[0]['_chart'].config.data;
		var idx = points[0]['_index'];
		var label = chartData.labels[idx];
		var color = chartData.datasets[0].backgroundColor[idx];

		var filter;
		if (label === 'Household Recipes') {
			graph.filter = filter = 'recipes';
			graph.zoom = 999;
		} else {
			var fgid = $scope.foodGroups.filter(function(x) { return x.description === label})[0].foodGroupId;
			if (fgid.length > 3)
				fgid = fgid.substring(0, 3);
			graph.filter = filter = fgid;

			var zoom = fgid.length + 1;
			if (zoom > 3) zoom = 5;
			if (zoom === graph.zoom)
				return;
		}

		graph.filterColors.push(color);
		$scope.nutrientClicked(filter, index);
		$scope.$apply();
	};
	$scope.unzoomNutrient = function(index) {
		var graph = $scope.nutrientGraphs[index];
		if (graph.filter === 'recipes') {
			graph.filter = '';
			graph.zoom = 2;
		} else {
			var zoom = graph.filter.length - 1;
			if (zoom < 2) zoom = 0;
			graph.filter = graph.filter.substring(0, zoom);
		}
		graph.filterColors.splice(graph.filterColors.length-1, 1);
		$scope.nutrientClicked(graph.filter, index);
	}

	$scope.updateTable = function() {
		if (!$scope.data || !$scope.studyDetails)
			return; //Wait until both are loaded, both will try and call
		$scope.fctConfig = $.extend(true, {}, fctConfigStart);
		// $scope.fctConfig = $.extend(true, {}, $scope.fctConfig);
		var members = [], rows = [];
		var rowCount = 0;
		$scope.tAverage = {count: 0, foodGroups: [], rowId: ++rowCount, isAverage: true };
		//rows.push($scope.tAverage);
		var keys = Object.keys($scope.fctConfig);

		for (var i = 0; i < $scope.data.length; i++) {
			var hh = $scope.data[i];
			for (var hmidx = 0; hmidx < hh.householdMembers.length; hmidx++) {
				var hm = hh.householdMembers[hmidx];
				var member  = members.filter(function(x) { return x.participantId === hm.participantId})[0];
				if (!member) {
					member = {participantId: hm.participantId, show: true, rows: [], average: {memberId: hm.participantId, foodGroups: [], rowId: ++rowCount, isAverage: true, quantityGrams: 0},
						age: hm.age, isBreastfed: hm.isBreastfed, lifeStage: hm.lifeStage, isFemale: hm.isFemale};
					rows.push(member.average);
					members.push(member);

					var rda = null;
					if ($scope.studyDetails.rdaModel) {
						for (var rdaIdx = 0; rdaIdx < $scope.studyDetails.rdaModel.rdAs.length; rdaIdx++) {
							var tr = $scope.studyDetails.rdaModel.rdAs[rdaIdx];
							if ((tr.gender === 'Child' || tr.isFemale === member.isFemale) && member.age < tr.ageUpperBound) {
								if (!rda || tr.ageUpperBound < rda.ageUpperBound)
									rda = tr;
							}
						}
					}
					member.rda = rda;
				}

				for (var eridx = 0; eridx < hm.eatRecords.length; eridx++) {
					var er = hm.eatRecords[eridx];
					er.date = er.imageRecord.captureTime.substring(0, 10);
					var row = member.rows.filter(function(x) { return x.date === er.date})[0];
					if (!row) {
						row = {memberId: member.participantId, member: member, date: er.date, records: 0, foodGroups: [], rowId: ++rowCount, showAverage: true, quantityGrams: 0, errors: []};
						member.rows.push(row);
						rows.push(row);
						$scope.tAverage.count++; //Since it's a new date for a member add a count to total days
					}

					row.records++;

					var record = er.imageRecord;
					var leftovers = er.leftovers;

					if (!record.isCompleted)
						row.errors.push({desc: "Record is not completed", id: record.id});
					for (var k = 0; k < record.foodItems.length; k++) {
						var foodItem = record.foodItems[k];
						if (!foodItem.foodCompositionDatabaseEntry) { //Item is not identified with a food item
							row.errors.push({desc: "Record has incomplete identification: " + foodItem.name, id: record.id});
							continue;
						}
						if (!foodItem.quantityGrams) {
							row.errors.push({desc: "Record has incomplete quantification: " + foodItem.name, id: record.id});
							// continue;
						}

						var pCount = record.participantCount ? record.participantCount : 1;
						if (record.guestInfo && record.guestInfo.totalHeads > 0)
							pCount = record.guestInfo.totalHeads;
						
						var quantityGrams = foodItem.quantityGrams / pCount;
						row.quantityGrams += quantityGrams;

						var fg = {grams: quantityGrams, foodGroup: 0};
						if (foodItem.foodCompositionDatabaseEntry && foodItem.foodCompositionDatabaseEntry.foodGroupId)
							fg.foodGroup = foodItem.foodCompositionDatabaseEntry.foodGroupId;
						else if (foodItem.foodCompositionDatabaseEntry && !foodItem.foodCompositionDatabaseEntry.table_Id)
							fg.foodGroup = -1;
						row.foodGroups.push(fg);
						member.average.foodGroups.push(fg);
						$scope.tAverage.foodGroups.push(fg);

					  	for (var l = 0; l < keys.length; l++) {
					  		var key = keys[l];
					  		if (!$scope.fctConfig.hasOwnProperty(key))
								continue;
							if (foodItem.foodCompositionDatabaseEntry[key])
								$scope.fctConfig[key].valid = true;

							var amt = foodItem.foodCompositionDatabaseEntry[key] * (quantityGrams / 100);
							row[key] = row[key] ? row[key] + amt : amt;
							$scope.tAverage[key] = $scope.tAverage[key] ? $scope.tAverage[key] + amt : amt;
						}

						if (!leftovers) //There is no leftovers, move to next record
							continue;

						if (!leftovers.isCompleted)
							row.errors.push({desc: "Leftovers is not completed", id: record.id});

						var leftFoodItem = leftovers.foodItems.find(function(item) { return item.name === foodItem.name; });
						if (leftFoodItem) {
							if (!leftFoodItem.quantityGrams > 0) {
								var diff = foodItem.quantityGrams - leftFoodItem.quantityGrams;
								if (diff < 0)
									row.errors.push({desc: "Leftovers amount exceeds initial amount: " + leftFoodItem.name, id: leftovers.id});

								row.quantityGrams -= leftFoodItem.quantityGrams / pCount;

								for (var l = keys.length - 1; l >= 0; l--) {
									if (!$scope.fctConfig.hasOwnProperty(keys[l]))
										continue;
									var sub = (leftFoodItem.foodCompositionDatabaseEntry[keys[l]] * (leftFoodItem.quantityGrams / 100)) / pCount;
									row[keys[l]] = (row[keys[l]] ? row[keys[l]] : 0) - sub;
								}
							} else {// if (!row.error) {
								row.errors.push({desc: "Leftovers has incomplete quantification: " + leftFoodItem.name, id: leftovers.id});
							}
						} else
							row.errors.push({desc: "Leftovers missing item from record: " + foodItem.name, id: leftovers.id});

					}
					if (leftovers) {
						for (var lidx = leftovers.foodItems.length - 1; lidx >= 0; lidx--) {
							var fi = leftovers.foodItems[lidx];
							var oFoodItem = record.foodItems.find(function(item) { return item.name === fi.name; });
							if (!oFoodItem)
								row.errors.push({desc: "Leftovers includes item not in record: " + fi.name, id: leftovers.id});
						}
					}
					// if (leftovers) {
					// 	for (var k = 0; k < leftovers.foodItems.length; k++) {
					// 		var fi = leftovers.foodItems[k];
					// 		if (!fi.quantityGrams || !fi.foodCompositionDatabaseEntry)
					// 			continue;
					// 	  	for (var l = 0; l < keys.length; l++) {
					// 	  		var key = keys[l];
					// 	  		if (!$scope.fctConfig.hasOwnProperty(key))
					// 				continue;

					// 			var amt = fi.foodCompositionDatabaseEntry[key] * (fi.quantityGrams / 100);
					// 			row[key] -= amt;
					// 			//member.average[key] -= amt;
					// 			$scope.tAverage[key] -= amt;
					// 		}
					// 	}
					// }
				}
			}
		}

		for (var i = 0; i < members.length; i++) {
			var member = members[i];
			$scope.calculateAverage(member.rows, member.average);
			member.rows.sort(function(x, y) { return x.date.localeCompare(y.date); });
		}

		$scope.calculateAverage(rows, $scope.tAverage);

		$scope.rows = rows;
		$scope.members = members;
		$scope.fctConfig.density.valid = $scope.fctConfig.moisture.valid = $scope.fctConfig.ash.valid = false;

		$scope.drawTable();
	};

	$scope.calculateAverage = function(rows, avg) {
		var keys = Object.keys($scope.fctConfig);
		for (var k = 0; k < keys.length; k++) {
			var key = keys[k];
			if (!$scope.fctConfig.hasOwnProperty(key))
					continue;
			avg[key] = 0;
		}
		avg.quantityGrams = 0;

		var count = 0;
		for (var i = 0; i < rows.length; i++) {
			var row = rows[i];

			if (row.isAverage || !row.showAverage)
				continue;

			count++;
			avg.quantityGrams += row.quantityGrams;
			for (var k = 0; k < keys.length; k++) {
				var key = keys[k];
				if (!row.hasOwnProperty(key))
						continue;
				avg[key] += row[key];
			}
		}

		avg.quantityGrams /= count;
		for (var k = 0; k < keys.length; k++) {
			var key = keys[k];
			if (!$scope.fctConfig.hasOwnProperty(key))
					continue;
			avg[key] /= count;
		}
	}

	$scope.averageToggleChanged = function(rowId) {
		var row = $scope.findRow(rowId);
		row.showAverage = !row.showAverage;

		$scope.calculateAverage(row.member.rows, row.member.average)
		$scope.calculateAverage($scope.rows, $scope.tAverage);

		$scope.drawTable();
		if ($scope.selectedRow.isAverage && row.memberId === $scope.selectedRow.memberId)
			$scope.graphRow($scope.selectedRow.rowId, $scope.selectedRow.memberId);
	}

	$scope.showMemberToggled = function(memberId) {
		for (var i = 0; i < $scope.members.length; i++) {
			if ($scope.members[i].participantId === memberId) {
				$scope.members[i].show = !$scope.members[i].show;
				break;
			}
		}

		$scope.drawTable();
	};

	$scope.loadError = function(id) {
		if (!id) {
			$scope.errorRow = null;
			return;
		}
		for (var i = $scope.rows.length - 1; i >= 0; i--) {
			if ($scope.rows[i].rowId === id) {
				$scope.errorRow = $scope.rows[i];
				return;
			}
		}
	};

	function toFixed(num, places) {
		if (!num)
			return (0).toFixed(places);
		else
			return num.toFixed(places);
	}

	$scope.drawTable = function() {
		var keys = Object.keys($scope.fctConfig);
		var html = '';
		for (var i = 0; i < $scope.members.length; i++) {
			var member = $scope.members[i];
			var memberHtml = '<tbody><tr><td colspan="100%">'
				+ '<span class="glyphicon ' + (member.show ? 'glyphicon-minus': 'glyphicon-plus') + ' btn-glyph" role="button" ng-click="showMemberToggled(\'' + member.participantId + '\')"></span>'
				+ '<span style="padding-left: 5px; padding-right: 45px; font-weight: bold;">' + member.participantId + '</span>'

			// <input type="checkbox" '  + (member.show ? 'checked ' : '') + 'ng-click="showMemberToggled(\'' + member.participantId + '\')"/>'

			memberHtml += '<span>' + (member.isFemale ? 'Female' : 'Male') + ' </span>';
			memberHtml += '<span style="padding-right: 10px;">' + member.age + ' yrs old </span>';
			if (member.age < 5)
				memberHtml += '<span>Is Breastfed: ' + (member.isBreastfed ? 'Yes' : 'No') + '</span>';
			if (member.lifeStage !== 'None')
				memberHtml += '<span>Lifestage: ' + member.lifeStage + '</span>';
			if (member.rda)
				memberHtml += '<span style="font-style: italic; padding-left: 60px;">Matching RDA: ' + member.rda.description + '</span>';
			memberHtml += '</td></tr>';

			if (member.show) {
				var rdaHtml = null;
				if (member.rda) {
					rdaHtml = '<tr><td style="width: 20px; min-width:20px;"></td><td>Recommended</td><td></td>';
					for (var l = 0; l < keys.length; l++) {
						var key = keys[l];
						if (!$scope.fctConfig.hasOwnProperty(key)) //Exclude inherited variables
							continue;
						if ($scope.fctConfig[key].valid && $scope.fctConfig[key].show) {
							if (member.rda[key] === null)
								rdaHtml += '<td>N/A</td>';
							else
								rdaHtml += '<td>' + toFixed(member.rda[key], 2) + '</td>';
						}
					}
				} else {
					rdaHtml = '<tr><td colspan="100%">Member does not match any RDA row.</td></tr>'
				}
				memberHtml += rdaHtml;

				for (var j = 0; j < member.rows.length; j++) {
					var row = member.rows[j];
					var rowHtml = '<tr id="table-row-' + row.rowId + '" role="button" ng-click="graphRow(' + row.rowId + ', \'' + member.participantId + '\')">';
					
					rowHtml += '<td style="width: 20px; min-width:20px;"><div ng-click="$event.stopPropagation();"><input type="checkbox" ' + (row.showAverage ? 'checked ' : '')  + 'ng-click="averageToggleChanged(' + row.rowId + ')"/></div></td>';
					rowHtml += '<td ng-click="$event.stopPropagation();"><a target="_blank" href="#!/household?household=' + $scope.selectedHousehold + '&study=' + $scope.selectedStudy + '&date=' + row.date + '&member=' + member.participantId + '">' + row.date + '</a>';
					if (row.errors.length > 0) {
						rowHtml += '<span title="Error, click for more details" class="pull-right glyphicon glyphicon-warning-sign text-danger btn-glyph" ng-click="loadError(' + row.rowId + ')" data-toggle="modal" data-target="#errorModal" role="button"></span></td>';
					} else 
						rowHtml += '</td>';
					rowHtml += '<td>' + toFixed(row.quantityGrams, 1) + '</td>';

					for (var l = 0; l < keys.length; l++) {
						var key = keys[l];
						if (!$scope.fctConfig.hasOwnProperty(key)) //Exclude inherited variables
							continue;
						if ($scope.fctConfig[key].valid && $scope.fctConfig[key].show)
							rowHtml += '<td>' + toFixed(row[key], 2) + '</td>';
					}
					rowHtml += '</tr>';
					memberHtml += rowHtml;
				}

				memberHtml += '<tr id="table-row-' + member.average.rowId + '" role="button" ng-click="graphRow(' + member.average.rowId + ', \'' + member.participantId + '\')" style="font-weight: bolder;"><td style="width: 20px; min-width:20px;"></td>';
				memberHtml += '<td style="font-style: italic;">Average</td>';
				// memberHtml += '<td></td>'; //Error
				memberHtml += '<td>' + member.average.quantityGrams.toFixed(1) + '</td>';

				for (var l = 0; l < keys.length; l++) {
					var key = keys[l];
					if (!$scope.fctConfig.hasOwnProperty(key)) //Exclude inherited variables
						continue;
					if ($scope.fctConfig[key].valid && $scope.fctConfig[key].show)
						memberHtml += '<td>' + member.average[key].toFixed(2) + '</td>';
				}
			}
			memberHtml += '</tr></tbody>';
			html += memberHtml;
		}

		// html += '<tr id="table-row-' + $scope.tAverage.rowId + '"  style="background-color: #bcbcbc; font-weight: bold;"><th style="width: 20px; min-width:20px;"></th><td style="font-weight: bold;">Total Avg</td>';
		// for (var l = 0; l < keys.length; l++) {
		// 	var key = keys[l];
		// 	if (!$scope.fctConfig.hasOwnProperty(key)) //Exclude inherited variables
		// 		continue;
		// 	if ($scope.fctConfig[key].valid && $scope.fctConfig[key].show)
		// 		html += '<td>' + $scope.tAverage[key].toFixed(2) + '</td>';
		// }
		// html += '</tr>';

		var table = document.getElementById('graph-result-table');
		table.innerHTML = html;
		$compile(table)($scope);

		return;
	};

	}]);

	fctConfigStart = {
	// name:{ external:"Name",internal:"Name",valid:false, show:true },
	// alternateName:{ external:"Alternate Name",internal:"AlternateName",valid:false, show:true },
		energykCal:{ external:"Energy (kcal)",internal:"energykCal",valid:false, show:true, affectsEnergy:true }, //"Energy - by calculation (kcal)
		energyWoFibre:{ external:"Energy, without dietary fibre (kJ)",internal:"energyWoFibre",valid:false, show:true, affectsEnergy:true },
		energyWFibre:{ external:"Energy, with dietary fibre (kJ)",internal:"energyWFibre",valid:false, show:true, affectsEnergy:true },
		energykJ:{ external:"Energy (kJ)",internal:"energykJ",valid:false, show:true, affectsEnergy:true }, //Energy - by calculation (kJ)
		density:{ external:"Density (g/mL)",internal:"density",valid:false, show:true, affectsEnergy:false },
		moisture:{ external:"Moisture (g)",internal:"moisture",valid:false, show:true, affectsEnergy:false },
		protein:{ external:"Protein, total (g)",internal:"protein",valid:false, show:true, affectsEnergy:true },
		fat:{ external:"Fat, total (g)",internal:"fat",valid:false, show:true, affectsEnergy:true },
		saturatedFat:{ external:"Total saturated fat (g)",internal:"saturatedFat",valid:false, show:true, affectsEnergy:true },
		monounsaturatedFat:{ external:"Total monounsaturated fat (g)",internal:"monounsaturatedFat",valid:false, show:true, affectsEnergy:true },
		polyunsaturatedFat:{ external:"Total polyunsaturated fat (g)",internal:"polyunsaturatedFat",valid:false, show:true, affectsEnergy:true },
		linoleicAcic:{ external:"Linoleic acid (g)",internal:"linoleicAcic",valid:false, show:true, affectsEnergy:false },
		alphaLinolenicAcid:{ external:"Alpha-linolenic acid (g)",internal:"alphaLinolenicAcid",valid:false, show:true, affectsEnergy:false },
		carbohydratesWoSa:{ external:"Available carbohydrates, without sugar alcohol (g)",internal:"carbohydratesWoSa",valid:false, show:true, affectsEnergy:true },
		carbohydrates:{ external:"Available carbohydrates, with sugar alcohols (g)",internal:"carbohydrates",valid:false, show:true, affectsEnergy:true },
		starch:{ external:"Starch (g)",internal:"starch",valid:false, show:true, affectsEnergy:false },
		sugarTotal:{ external:"Total sugars (g)",internal:"sugarTotal",valid:false, show:true, affectsEnergy:true },
		sugarAdded:{ external:"Added sugars (g)",internal:"sugarAdded",valid:false, show:true, affectsEnergy:true },
		sugarFree:{ external:"Free sugars (g)",internal:"sugarFree",valid:false, show:true, affectsEnergy:true },
		fibre:{ external:"Dietary fibre (g)",internal:"fibre",valid:false, show:true, affectsEnergy:true },
		alcohol:{ external:"Alcohol (g)",internal:"alcohol",valid:false, show:true, affectsEnergy:true },
		ash:{ external:"Ash (g)",internal:"ash",valid:false, show:true, affectsEnergy:false },
		retinol:{ external:"Preformed vitamin A (retinol) (\u00B5g)",internal:"retinol",valid:false, show:true, affectsEnergy:false },
		betaCarotene:{ external:"Beta-carotene (\u00B5g)",internal:"betaCarotene",valid:false, show:true, affectsEnergy:false },
		provitaminA:{ external:"Provitamin A (b-carotene equivalents) (\u00B5g)",internal:"provitaminA",valid:false, show:true, affectsEnergy:false },
		vitaminA:{ external:"Vitamin A retinol equivalents (\u00B5g)",internal:"vitaminA",valid:false, show:true, affectsEnergy:false },
		thiamin:{ external:"Thiamin (B1) (mg)",internal:"thiamin",valid:false, show:true, affectsEnergy:false },
		riboflavin:{ external:"Riboflavin (B2) (mg)",internal:"riboflavin",valid:false, show:true, affectsEnergy:false },
		niacin:{ external:"Niacin (B3) (mg)",internal:"niacin",valid:false, show:true, affectsEnergy:false },
		niacinDE:{ external:"Niacin derived equivalents (mg)",internal:"niacinDE",valid:false, show:true, affectsEnergy:false },
		vitaminB6:{ external:"Vitamin B6 (mg)",internal:"vitaminB6",valid:false, show:true, affectsEnergy:false },
		vitaminB12:{ external:"Vitamin B12 (\u00B5g)",internal:"vitaminB12",valid:false, show:true, affectsEnergy:false },
		vitaminC:{ external:"Vitamin C (mg)",internal:"vitaminC",valid:false, show:true, affectsEnergy:false },
		alphaTocopherol:{ external:"Alpha-tocopherol (mg)",internal:"alphaTocopherol",valid:false, show:true, affectsEnergy:false },
		vitaminE:{ external:"Vitamin E (mg)",internal:"vitaminE",valid:false, show:true, affectsEnergy:false },
		folate:{ external:"Folate, natural (\u00B5g)",internal:"folate",valid:false, show:true, affectsEnergy:false },
		folicAcid:{ external:"Folic acid (\u00B5g)",internal:"folicAcid",valid:false, show:true, affectsEnergy:false },
		folatesTotal:{ external:"Total Folates (\u00B5g)",internal:"folatesTotal",valid:false, show:true, affectsEnergy:false },
		folateDietary:{ external:"Dietary folate equivalents (\u00B5g)",internal:"folateDietary",valid:false, show:true, affectsEnergy:false },
		calcium:{ external:"Calcium (Ca) (mg)",internal:"calcium",valid:false, show:true, affectsEnergy:false },
		iodine:{ external:"Iodine (I) (\u00B5g)",internal:"iodine",valid:false, show:true, affectsEnergy:false },
		phosphorus:{ external:"Phosphorus (P) (mg)",internal:"phosphorus",valid:false, show:true, affectsEnergy:false },
		sodium:{ external:"Sodium (Na) (mg)",internal:"sodium",valid:false, show:true, affectsEnergy:false },
		potassium:{ external:"Potassium (K) (mg)",internal:"potassium",valid:false, show:true, affectsEnergy:false },
		iron:{ external:"Iron (Fe) (mg)",internal:"iron",valid:false, show:true, affectsEnergy:false },
		magnesium:{ external:"Magnesium (Mg) (mg)",internal:"magnesium",valid:false, show:true, affectsEnergy:false },
		selenium:{ external:"Selenium (Se) (\u00B5g)",internal:"selenium",valid:false, show:true, affectsEnergy:false },
		copper:{ external:"Copper, (Cu) (mg)",internal:"copper",valid:false, show:true, affectsEnergy:false },
		zinc:{ external:"Zinc (Zn) (mg)",internal:"zinc",valid:false, show:true, affectsEnergy:false },
		caffeine:{ external:"Caffeine (mg)",internal:"caffeine",valid:false, show:true, affectsEnergy:false },
		tryptophan:{ external:"Tryptophan (mg)",internal:"tryptophan",valid:false, show:true, affectsEnergy:false },
		eicosapentaenoic:{ external:"C20:5w3 Eicosapentaenoic (mg)",internal:"eicosapentaenoic",valid:false, show:true, affectsEnergy:false },
		docosapentaenoic:{ external:"C22:5w3 Docosapentaenoic (mg)",internal:"docosapentaenoic",valid:false, show:true, affectsEnergy:false },
		docosahexaenoic:{ external:"C22:6w3 Docosahexaenoic (mg)",internal:"docosahexaenoic",valid:false, show:true, affectsEnergy:false },
		omega3FattyAcid:{ external:"Total long chain omega 3 fatty acids (mg)",internal:"omega3FattyAcid",valid:false, show:true, affectsEnergy:false },
		transFattyAcid:{ external:"Total trans fatty acids (mg)",internal:"transFattyAcid",valid:false, show:true, affectsEnergy:false },
		cholesterol:{ external:"Cholesterol (mg)",internal:"cholesterol",valid:false, show:true, affectsEnergy:false },
		adG_10:{ external:"Grain (cereal) foods",internal:"adG_10",valid:false, show:true, affectsEnergy:false },
		adG_101:{ external:"Wholegrain (WG) or higher fibre (HF) cereals/grains",internal:"adG_101",valid:false, show:true, affectsEnergy:false },
		adG_1011:{ external:"WG/HF Breads",internal:"adG_1011",valid:false, show:true, affectsEnergy:false },
		adG_1012:{ external:"WG/HF Grains (excluding oats)",internal:"adG_1012",valid:false, show:true, affectsEnergy:false },
		adG_1013:{ external:"WG/HF Oats",internal:"adG_1013",valid:false, show:true, affectsEnergy:false },
		adG_1014:{ external:"WG/HF Breakfast cereal flakes",internal:"adG_1014",valid:false, show:true, affectsEnergy:false },
		adG_1015:{ external:"WG/HF Savoury crackers/crispbreads",internal:"adG_1015",valid:false, show:true, affectsEnergy:false },
		adG_1016:{ external:"WG/HF Crumpets",internal:"adG_1016",valid:false, show:true, affectsEnergy:false },
		adG_1017:{ external:"WG/HF English muffins and scones",internal:"adG_1017",valid:false, show:true, affectsEnergy:false },
		adG_1018:{ external:"WG/HF Flour",internal:"adG_1018",valid:false, show:true, affectsEnergy:false },
		adG_102:{ external:"Refined (Ref) or lower fibre (LF) cereals/grains",internal:"adG_102",valid:false, show:true, affectsEnergy:false },
		adG_1021:{ external:"Ref/LF Breads",internal:"adG_1021",valid:false, show:true, affectsEnergy:false },
		adG_1022:{ external:"Ref/LF Grains (excluding oats)",internal:"adG_1022",valid:false, show:true, affectsEnergy:false },
		adG_1023:{ external:"Ref/LF Oats",internal:"adG_1023",valid:false, show:true, affectsEnergy:false },
		adG_1024:{ external:"Ref/LF Breakfast cereal flakes",internal:"adG_1024",valid:false, show:true, affectsEnergy:false },
		adG_1025:{ external:"Ref/LF Savoury crackers/crispbreads",internal:"adG_1025",valid:false, show:true, affectsEnergy:false },
		adG_1026:{ external:"Ref/LF Crumpets",internal:"adG_1026",valid:false, show:true, affectsEnergy:false },
		adG_1027:{ external:"Ref/LF English muffins and scones",internal:"adG_1027",valid:false, show:true, affectsEnergy:false },
		adG_1028:{ external:"Ref/LF Flour",internal:"adG_1028",valid:false, show:true, affectsEnergy:false },
		adG_20:{ external:"Vegetables and legumes/beans",internal:"adG_20",valid:false, show:true, affectsEnergy:false },
		adG_201:{ external:"Green and brassica vegetables",internal:"adG_201",valid:false, show:true, affectsEnergy:false },
		adG_202:{ external:"Orange vegetables",internal:"adG_202",valid:false, show:true, affectsEnergy:false },
		adG_203:{ external:"Starchy vegetables",internal:"adG_203",valid:false, show:true, affectsEnergy:false },
		adG_204:{ external:"Legumes as a vegetable",internal:"adG_204",valid:false, show:true, affectsEnergy:false },
		adG_205:{ external:"Other vegetables",internal:"adG_205",valid:false, show:true, affectsEnergy:false },
		adG_2051:{ external:"Whole vegetables",internal:"adG_2051",valid:false, show:true, affectsEnergy:false },
		adG_2052:{ external:"Vegetable Juice",internal:"adG_2052",valid:false, show:true, affectsEnergy:false },
		adG_30:{ external:"Fruit",internal:"adG_30",valid:false, show:true, affectsEnergy:false },
		adG_301:{ external:"Fresh/canned fruit ",internal:"adG_301",valid:false, show:true, affectsEnergy:false },
		adG_302:{ external:"Dried fruit",internal:"adG_302",valid:false, show:true, affectsEnergy:false },
		adG_303:{ external:"Fruit juice",internal:"adG_303",valid:false, show:true, affectsEnergy:false },
		adG_40:{ external:"Milk, yoghurt, cheese and/or alternatives",internal:"adG_40",valid:false, show:true, affectsEnergy:false },
		adG_401:{ external:"Higher fat (HF) dairy foods (>10% fat)",internal:"adG_401",valid:false, show:true, affectsEnergy:false },
		adG_4011:{ external:"HF Cheese",internal:"adG_4011",valid:false, show:true, affectsEnergy:false },
		adG_4012:{ external:"HF Milk powder only",internal:"adG_4012",valid:false, show:true, affectsEnergy:false },
		adG_402:{ external:"Medium fat (MF) dairy foods (4-10% fat)",internal:"adG_402",valid:false, show:true, affectsEnergy:false },
		adG_4021:{ external:"MF Milk",internal:"adG_4021",valid:false, show:true, affectsEnergy:false },
		adG_4022:{ external:"MF Evaporated milk",internal:"adG_4022",valid:false, show:true, affectsEnergy:false },
		adG_4023:{ external:"MF Condensed milk",internal:"adG_4023",valid:false, show:true, affectsEnergy:false },
		adG_4024:{ external:"MF Cheese, hard & soft",internal:"adG_4024",valid:false, show:true, affectsEnergy:false },
		adG_4025:{ external:"MF Cheese, fresh",internal:"adG_4025",valid:false, show:true, affectsEnergy:false },
		adG_4026:{ external:"MF Yoghurt, dairy based",internal:"adG_4026",valid:false, show:true, affectsEnergy:false },
		adG_4027:{ external:"MF Milk alternative beverage, calcium enriched",internal:"adG_4027",valid:false, show:true, affectsEnergy:false },
		adG_4028:{ external:"MF Dairy-based snack foods",internal:"adG_4028",valid:false, show:true, affectsEnergy:false },
		adG_403:{ external:"Lower fat (LF) dairy foods (<4% fat)",internal:"adG_403",valid:false, show:true, affectsEnergy:false },
		adG_4031:{ external:"LF Milk",internal:"adG_4031",valid:false, show:true, affectsEnergy:false },
		adG_4032:{ external:"LF Evaporated milk",internal:"adG_4032",valid:false, show:true, affectsEnergy:false },
		adG_4033:{ external:"LF Condensed milk",internal:"adG_4033",valid:false, show:true, affectsEnergy:false },
		adG_4034:{ external:"LF Cheese, hard & soft",internal:"adG_4034",valid:false, show:true, affectsEnergy:false },
		adG_4035:{ external:"LF Cheese, fresh",internal:"adG_4035",valid:false, show:true, affectsEnergy:false },
		adG_4036:{ external:"LF Yoghurt, dairy based",internal:"adG_4036",valid:false, show:true, affectsEnergy:false },
		adG_4037:{ external:"LF Milk alternative beverage, calcium enriched",internal:"adG_4037",valid:false, show:true, affectsEnergy:false },
		adG_4038:{ external:"LF Dairy-based snack foods",internal:"adG_4038",valid:false, show:true, affectsEnergy:false },
		adG_4039:{ external:"LF Milk powder only",internal:"adG_4039",valid:false, show:true, affectsEnergy:false },
		adG_50:{ external:"Meats, poultry, fish, eggs, tofu, nuts and seeds and legumes/beans/tofu",internal:"adG_50",valid:false, show:true, affectsEnergy:false },
		adG_501:{ external:"Red meat, lean (<10% fat)",internal:"adG_501",valid:false, show:true, affectsEnergy:false },
		adG_5011:{ external:"Unprocessed meat (<10% fat)",internal:"adG_5011",valid:false, show:true, affectsEnergy:false },
		adG_5012:{ external:"Processed meat (<10% fat)",internal:"adG_5012",valid:false, show:true, affectsEnergy:false },
		adG_502:{ external:"Red meat, non-lean (≥10% fat)",internal:"adG_502",valid:false, show:true, affectsEnergy:false },
		adG_5021:{ external:"Unprocessed meat (≥10% fat)",internal:"adG_5021",valid:false, show:true, affectsEnergy:false },
		adG_5022:{ external:"Processed meat (≥10% fat)",internal:"adG_5022",valid:false, show:true, affectsEnergy:false },
		adG_503:{ external:"Poultry, lean (<10% fat)",internal:"adG_503",valid:false, show:true, affectsEnergy:false },
		adG_5031:{ external:"Unprocessed poultry (<10% fat)",internal:"adG_5031",valid:false, show:true, affectsEnergy:false },
		adG_5032:{ external:"Processed poultry (<10% fat)",internal:"adG_5032",valid:false, show:true, affectsEnergy:false },
		adG_504:{ external:"Poultry, non-lean (≥10% fat)",internal:"adG_504",valid:false, show:true, affectsEnergy:false },
		adG_5041:{ external:"Unprocessed poultry (≥10% fat)",internal:"adG_5041",valid:false, show:true, affectsEnergy:false },
		adG_5042:{ external:"Processed poultry (≥10% fat)",internal:"adG_5042",valid:false, show:true, affectsEnergy:false },
		adG_505:{ external:"Fish and seafood",internal:"adG_505",valid:false, show:true, affectsEnergy:false },
		adG_506:{ external:"Eggs",internal:"adG_506",valid:false, show:true, affectsEnergy:false },
		adG_507:{ external:"Legumes as meat alternative",internal:"adG_507",valid:false, show:true, affectsEnergy:false },
		adG_5071:{ external:"Legumes/beans",internal:"adG_5071",valid:false, show:true, affectsEnergy:false },
		adG_5072:{ external:"Tofu",internal:"adG_5072",valid:false, show:true, affectsEnergy:false },
		adG_508:{ external:"Nuts and seeds",internal:"adG_508",valid:false, show:true, affectsEnergy:false },
		adG_60:{ external:"Water",internal:"adG_60",valid:false, show:true, affectsEnergy:false },
		adG_70:{ external:"Unsaturated spreads and oils",internal:"adG_70",valid:false, show:true, affectsEnergy:false },
		adG_701:{ external:"Unsaturated spreads ",internal:"adG_701",valid:false, show:true, affectsEnergy:false },
		adG_702:{ external:"Unsaturated oils",internal:"adG_702",valid:false, show:true, affectsEnergy:false },
		adG_703:{ external:"Nuts",internal:"adG_703",valid:false, show:true, affectsEnergy:false },
	};