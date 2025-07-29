
app = angular.module('visida_cms');

app.controller('commonCtrl', ['$scope', 'Restangular', '$cookies', 'ClientConfig', function($scope, Restangular, $cookies, ClientConfig) {
	$scope.getCookie = function(name, def) {
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

	$scope.setDays = function(households, reset) {
		$scope.allDaysChecked = true;
		var dates = [], days = [];
		for (var i = 0; i < households.length; i++) {
			// if (!households[i].days)
			// 	continue;
			for (var j = 0; j < $scope.searchConfig.households.length; j++) {
				var hh = $scope.searchConfig.households[j];
				if (hh.name === households[i].name) {
					for (var l = 0; l < hh.days.length; l++) {
						if (!dates.includes(hh.days[l]))
							dates.push(hh.days[l]);
					}
					break;
				}
			}
		}
		dates.sort();
		for (var i = 0; i < dates.length; i++) {
			if (reset || !$scope.searchDaysParam) {
				days.push({date: dates[i], checked: true});
			} else {
				var found = false;
				for (var j = 0; j < $scope.searchDaysParam.length; j++) {
					if ($scope.searchDaysParam[j] === dates[i]) {
						found = true;
						break;
					}
				}
				if (!found)
					$scope.allDaysChecked = false;
				days.push({date: dates[i], checked: found});
			}
		}
		return days;
	};

	$scope.setParticipants = function(households) {
		var participants = [];
		for (var i = 0; i < households.length; i++) {
			for (var l = 0; l < $scope.searchConfig.households.length; l++) {
				if ($scope.searchConfig.households[l].name === households[i].name) {
					var hh = $scope.searchConfig.households[l];
					if (!hh.householdMembers)
						continue;
					for (var j = 0; j < hh.householdMembers.length; j++) {
						var hm = hh.householdMembers[j];
						if (!participants.includes(hm))
							participants.push(hm);
					}
					break;
				}
			}
		}
		return participants;
	};

	$scope.toggleAllDays = function(all) {
		for (var i = 0; i < $scope.searchDays.length; i++)
			$scope.searchDays[i].checked = all;
		if (all)
			$scope.selectedDaysCount = $scope.searchDays.length;
		else
			$scope.selectedDaysCount = 0;
	};

	$scope.toggleDay = function(day) {
		if (!day.checked) {
			$scope.allDaysChecked = false;
			$scope.selectedDaysCount--;
			return;
		}
		var all = true;
		var count = 0;
		for (var i = 0; i < $scope.searchDays.length; i++) {
			if (!$scope.searchDays[i].checked)
				all = false;
			else
				count++;
		}
		$scope.selectedDaysCount = count;
		$scope.allDaysChecked = all;
	};

	$scope.studyChanged = function(resetDates, skipDates, skipParticipants) {
		if (!$scope.selectedStudy) {
			$scope.searchHouseholds = $scope.searchConfig.households;
			$scope.searchParticipants = $scope.setParticipants($scope.searchHouseholds);
			$scope.searchDays = $scope.setDays($scope.searchHouseholds, resetDates);
			return;
		}
		//var sid = parseInt($scope.selectedStudy);
		$scope.study = $scope.searchConfig.studies.filter(function(x) { return x.id === $scope.selectedStudy})[0];
		$scope.searchHouseholds = $scope.study.households;
		if (!skipDates)
			$scope.searchDays = $scope.setDays($scope.searchHouseholds, resetDates);
		if (!skipParticipants)
			$scope.searchParticipants = $scope.setParticipants($scope.searchHouseholds);
	};

	$scope.householdChanged = function(resetDates) {
		if (!$scope.selectedStudy) {
			for (var i = 0; i < $scope.searchConfig.studies.length; i++) {
				var hh = $scope.searchConfig.studies[i].households.filter(function(x) { return x.name === $scope.selectedHousehold; })[0];
				if (hh) {
					$scope.selectedHouseholdId = hh.id;
					$scope.selectedStudy = $scope.searchConfig.studies[i].id;
					$scope.searchParticipants = $scope.setParticipants([{name: hh.name}]);
					$scope.searchDays = $scope.setDays([hh], resetDates);
					$scope.studyChanged(resetDates, true, true);
					break;
				}
			}
		}
		else {
			var hh = $scope.study.households.filter(function(x) { return x.name === $scope.selectedHousehold })[0];
  			if (hh) {
  				$scope.selectedHouseholdId = hh.id;
  				$scope.searchParticipants = $scope.setParticipants([{name: hh.name}]);
  				$scope.searchDays = $scope.setDays([hh], resetDates);
  			} else {
  				$scope.selectedHouseholdId = null;
  				$scope.selectedHousehold = "";
  				$scope.selectedHouseholdMember = "";
  				$scope.studyChanged();
  			}
		}
	};

	$scope.householdMemberChanged = function() {
		if (!$scope.selectedHouseholdMember)
			return;
		if (!$scope.selectedHousehold) {
			hhLoop:
			for (var i = 0; i < $scope.searchConfig.households.length; i++) {
				var hh = $scope.searchConfig.households[i];
				for (var j = 0; j < hh.householdMembers.length; j++) {
					if (hh.householdMembers[j] === $scope.selectedHouseholdMember) {
						$scope.selectedHousehold = hh.name;
						$scope.householdChanged(true);
						return;
					}
				}
			}
		}
	};

	$scope.initializeSearches = function(config) {
		$scope.searchConfig = config;

		$scope.searchHouseholds = $scope.searchConfig.households;
		$scope.searchParticipants = $scope.setParticipants($scope.searchHouseholds);
		$scope.searchDays = $scope.setDays($scope.searchHouseholds);
		if ($scope.selectedStudy)
			$scope.studyChanged();
		if ($scope.selectedHousehold)
			$scope.householdChanged();

		$scope.loading--;
		//var loadConfigComplete = new Event('loadConfig', { bubbles: true, cancelable: false });
		window.dispatchEvent(loadConfigComplete);
	};
	const loadConfigComplete = new CustomEvent('loadConfig');

	//const loadConfigComplete = new Event('loadConfig', { bubbles: true, cancelable: false });


	angular.element(document).ready(function () {
		var config = ClientConfig.loadConfig();
		if (!config) {
			Restangular.one('GetSearchConfig').get()
			.then(function(result) {
				ClientConfig.saveConfig(result.data);

				$scope.initializeSearches(result.data);
			}).finally(function() {
			});
		} else {
			$scope.initializeSearches(config);
		}
	    console.log('page loading completed');
	});
}]);

app.controller('householdController', ['$scope', 'Restangular', '$routeParams', '$window', '$controller', '$cookies', '$timeout', function($scope, Restangular, $routeParams, $window, $controller, $cookies, $timeout) {
	$scope.loading = 1;
	$controller('commonCtrl', { $scope: $scope });
	//$scope.colors = ["#e74c3c","#1abc9c","#9b59b6","#e67e22","#f1c40f","#3498db","#2ecc71"];
	$scope.colors = ["#7b1916","#573350","#000000","#9a8f54","#f1c40f","#3498db","#2ecc71"];

	if ($routeParams.household) {
		$scope.selectedStudy = $routeParams.study;
		$scope.selectedHousehold = $routeParams.household;
	} else {
		$scope.selectedStudy = $scope.getCookie('household_study', null);
		$scope.selectedHousehold = $scope.getCookie('household_household', null);
	}
	if ($scope.selectedStudy)
		$scope.selectedStudy = parseInt($scope.selectedStudy)

	if ($routeParams.date)
		$scope.searchDaysParam = [$routeParams.date + 'T00:00:00'];
	if ($routeParams.member) {
		$scope.showRecipes = false;
		$scope.preselectedMember = $routeParams.member;
	}

	$scope.scrollToLinked = function() {
		$timeout(function() {
			if ($routeParams.record || $routeParams.recipe) {
				var elementId = $routeParams.record ? 'record-' + $routeParams.record : 'recipe-' + $routeParams.recipe;
				var element = document.getElementById(elementId);
				if (element)
					element.scrollIntoView({ behavior: 'smooth', block: 'center', inline: 'center' });
			}
		}, 1000);
	}

	$scope.onConfigLoaded = function(e) {
		$scope.search();
		window.removeEventListener('loadConfig', $scope.onConfigLoaded, false);
	};



	window.addEventListener('loadConfig', $scope.onConfigLoaded, false);

	$scope.search = function() {
		if ($scope.selectedStudy && $scope.selectedHousehold) {
			$scope.allImageIcons = null;
			$scope.loading++;
			Restangular.one('/Households/' + $scope.selectedStudy + '/ByName/' + $scope.selectedHousehold).get()
			.then(function(result) {
				$scope.household = result.data;
				$scope.reassignDays();
				$scope.household.householdMembers.sort(function(x, y) { return x.participantId.localeCompare(y.participantId); });
				for (var i = 0; i < $scope.household.householdMembers.length; i++) {
					if (i <= $scope.colors.length)
						$scope.household.householdMembers[i].color = $scope.colors[i];
					else
						$scope.household.householdMembers[i].color = '#000000'

					if ($scope.preselectedMember && $scope.preselectedMember !== $scope.household.householdMembers[i].participantId)
						$scope.household.householdMembers[i].hide = true;
				}
				$scope.parseData();
			}).finally(function() { $scope.loading--; });
		}
	};

	$scope.searchClicked = function() {
		if (!$scope.selectedHousehold)
			return;
		
		var route = window.location.href;//.substring(0, window.location.href.indexOf('?'));//window.location.protocol + '//' + window.location.host + window.location.pathname + window.location.hash;
		if (route.indexOf('?') > 0)
			route = route.substring(0, route.indexOf('?'));
		history.pushState({}, 'Qwerty', route + '?household=' + $scope.selectedHousehold + '&study=' + $scope.selectedStudy);
		$cookies.put('household_study', $scope.selectedStudy);
		$cookies.put('household_household', $scope.selectedHousehold);
	};

	$scope.cookMethodToString = function(method) {
		switch (method) {
			case 0:
				return 'Uncooked';
			case 1:
				return 'Boil/Poach/Steam';
			case 2:
				return 'Grill';
			case 3:
			return 'Oven';
			case 4:
			return 'Fry';
			default:
			return "None";
		}
	}

	$scope.parseData = function() {
		var hh = $scope.household;
		var days = [];
		for (var i = 0; i < hh.householdMembers.length; i++) {
			var hm = hh.householdMembers[i];

			hm.years = Math.floor(hm.age);
			hm.months = 0;
			if (hm.age % 1 != 0) {
				hm.months = Math.round((hm.age % 1) * 12);
			}

			for (var fridx = 0; fridx < hm.foodRecords.length; fridx++) {
				var fr = hm.foodRecords[fridx];
				var day = days.filter(function(x) { return x.dateStr === fr.date; })[0];
				if (!day) {
					day = {
						dateStr: fr.date,
						date: new Date(fr.date),
						//eatOccasions: [],
						eatRecords: [],
						recordings: [],
						comments: []
					};
					days.push(day);
				}

				//Loop through eating occasions
				for (var eoidx = 0; eoidx < fr.eatOccasions.length; eoidx++) {
					var eo = fr.eatOccasions[eoidx];
					if (eo.isBreastfeeding)
						return;
					// var st = new Date(eo.timeStart), et = new Date(eo.timeEnd);
					// var timeLowerBound = st.getTime() - 900000, timeUpperBound = st.getTime() + 900000;
					// var eatingOccasion = day.eatOccasions.filter(function(x) { var t = x.timeStart.getTime(); return t > timeLowerBound && t < timeUpperBound })[0];
					// if (!eatingOccasion) {
					// 	eatingOccasion = {
					// 		finalized: eo.finalized,
					// 		timeStart: st,
					// 		timeEnd: et,
					// 		eatRecords: [],
					// 		day: day
					// 	}
					// 	day.eatOccasions.push(eatingOccasion);
					// }

					//Loop through eat records, and their internal image record
					for (var eridx = 0; eridx < eo.eatRecords.length; eridx++) {
						var er = eo.eatRecords[eridx];
						//if (er.imageRecord.hidden)
							//ontinue;
						var eatRecord = day.eatRecords.filter(function(x) { return er.imageRecord.id === x.id; })[0];
						if (!eatRecord) {
							eatRecord = {
								id: er.imageRecord.id,
								isEatRecord: true,
								day: day,
								finalized: er.finalized,
								finalizeTime: er.finalizeTime,
								captureTime: er.imageRecord.captureTime,
								participants: [{
									isFemale: hm.isFemale,
									isMother: hm.isMother,
									lifeStage: hm.lifeStage,
									participantId: hm.participantId,
									age: hm.age,
									years: hm.years,
									months: hm.months,
									color: hm.color,
									id: hm.id
									//glyph: hm.age < 18 ? 'tag-child' : (hm.isFemale ? 'tag-aFemale' : 'tag-aMale')
								}],
								guestInfo: er.imageRecord.guestInfo,
								imageUrl: er.imageRecord.imageUrl,
								imageThumbUrl: er.imageRecord.imageThumbUrl,
								textDescription: er.imageRecord.textDescription,
								recordType: er.imageRecord.recordType,
								imageRecord: er.imageRecord,
								leftovers: er.leftovers
							}
							for (var cidx = 0; cidx < er.imageRecord.comments.length; cidx++)
								day.comments.push(er.imageRecord.comments[cidx]);
							if (er.leftovers)
								for (var cidx = 0; cidx < er.leftovers.comments.length; cidx++)
									day.comments.push(er.leftovers.comments[cidx]);

							eatRecord.imageRecord.day = day;
							eatRecord.imageRecord.displayObj = eatRecord;
							if (eatRecord.leftovers){
								eatRecord.leftovers.day = day;
								eatRecord.leftovers.displayObj = eatRecord;
							}

							if (er.leftovers) {
								eatRecord.hasLeftovers = true;
								eatRecord.leftoverImageUrl = er.leftovers.imageThumbUrl ? er.leftovers.imageThumbUrl : er.leftovers.imageUrl;
								eatRecord.leftoverDescription = er.leftovers.textDescription;
							}
							//eatingOccasion.eatRecords.push(eatRecord);
							day.eatRecords.push(eatRecord);
							day.recordings.push(eatRecord);
						} else {
							eatRecord.participants.push({
								isFemale: hm.isFemale,
								isMother: hm.isMother,
								lifeStage: hm.lifeStage,
								participantId: hm.participantId,
								age: hm.age,
								years: hm.years,
								months: hm.months,
								color: hm.color,
								id: hm.id
							})
						}
						//Use this if you need guests only instead of total participants
						// if (eatRecord.guestInfo) {
						// 	if (hm.age < 18)
						// 		eatRecord.guestInfo.childGuests -= 1;
						// 	else if (hm.isFemale)
						// 		eatRecord.guestInfo.adultFemaleGuests -= 1;
						// 	else
						// 		eatRecord.guestInfo.adultMaleGuests -= 1;
						// }
					}
				}
			}
		}

		for (var i = 0; i < hh.householdRecipes.length; i++) {
			var recipe = hh.householdRecipes[i];
			var dstr = recipe.captureTime.substring(0, 11) + "00:00:00";
			var date = new Date(dstr);
			var day = days.filter(function(x) { return x.dateStr === dstr; })[0];
			if (!day) {
				day = {
					dateStr: dstr,
					date: date,
					//eatOccasions: [],
					eatRecords: [],
					recordings: [],
					comments: []
				};
				days.push(day);
			}
			recipe.isRecipe = true;
			recipe.day = day;
			recipe.foodItems = [];
			day.recordings.push(recipe);
			for (var cidx = 0; cidx < recipe.comments.length; cidx++)
				day.comments.push(recipe.comments[cidx]);

			for (var j = 0; j < recipe.ingredients.length; j++) {
				recipe.ingredients[j].imageRecord.day = day;
				recipe.ingredients[j].imageRecord.ingredient = recipe.ingredients[j];
				for (var cidx = 0; cidx < recipe.ingredients[j].imageRecord.comments.length; cidx++)
					day.comments.push(recipe.ingredients[j].imageRecord.comments[cidx]);
				for (var fidx = 0; fidx < recipe.ingredients[j].imageRecord.foodItems.length; fidx++)
					recipe.foodItems.push(recipe.ingredients[j].imageRecord.foodItems[fidx]);
			}
		}

		for (var i = 0; i < days.length; i++) {
			var day = days[i];
			day.recordings.sort(function(a, b) {
				if (a.captureTime < b.captureTime)
					return -1;
				else if (a.captureTime > b.captureTime)
					return 1;
				else
					return 0;
			});

			day.comments.sort(function(a, b) {
				if (a.createdTime < b.createdTime)
					return -1;
				else if (a.createdTime > b.createdTime)
					return 1;
				else
					return 0;
			});

			for (var cidx = 0; cidx < day.comments.length; cidx++) {
				var c = day.comments[cidx];
				if (c.replyTo > 0) {
					day.comments.splice(cidx, 1);
					cidx--;

					for (var ridx = 0; ridx < day.comments.length; ridx++) {
						if (c.replyTo === day.comments[ridx].id) {
							day.comments[ridx].replies.push(c);
							break;
						}
					}
				} else
					c.replies = [];
			}
		}

		for (var i = 0; i < hh.conversionHistories.length; i++) {
			var h = hh.conversionHistories[i];

			hLoop:
			for (var j = 0; j < days.length; j++) {
				for (var k = 0; k < days[j].recordings.length; k++) {
					var record = days[j].recordings[k];
					if ((record.isEatRecord && h.recordId && h.recordId === record.id) ||
						(record.isRecipe && h.recipeId && h.recipeId === record.id)) {
						if (!record.conversionHistories)
							record.conversionHistories = [];
						record.conversionHistories.push(h);
						break hLoop;
					} else if (record.leftovers && h.recordId && record.leftovers.id === h.recordId) {
						if (!record.leftovers.conversionHistories)
							record.leftovers.conversionHistories = [];
						record.leftovers.conversionHistories.push(h);
						break hLoop;
					} else if (record.ingredients && h.recordId) {
						for (var l = 0; l < record.ingredients.length; l++) {
							var ing = record.ingredients[l];
							if (ing.id === h.recordId) {
								if (!ing.conversionHistories)
									ing.conversionHistories = [];
								ing.conversionHistories.push(h);
								break hLoop;
							}
						}
					}
				}
			}
		}

		days.sort(function(a, b) {
		    return a.date.getTime() - b.date.getTime();
		});

		$scope.days = days;
	};

	$scope.selectRecord = function(record) {
		$scope.convertDestination = null;
		$scope.selectedRecord = record;
		if (record && record.recordType === 'EatRecord') {
			var editParticipants = [];
			for (var i = 0; i < $scope.household.householdMembers.length; i++) {
				var hm = $scope.household.householdMembers[i];
				var nhm = {checked: false, name: hm.participantId, default: false};
				for (var j = 0; j < record.displayObj.participants.length; j++) {
					if (record.displayObj.participants[j].participantId === hm.participantId) {
						nhm.checked = nhm.default = true;
						break;
					}
				}
				editParticipants.push(nhm);
			}
			record.editParticipants = editParticipants;
		}
	};
	$scope.toggleRecordParticipant = function(record, p) {
		if (p.checked) {
			Restangular.oneUrl("/ImageRecords/" + record.id + "/AddParticipantByName/" + p.name).get().then(function(result) {
				for (var i = 0; i < $scope.household.householdMembers.length; i++) {
					var hm = $scope.household.householdMembers[i];
					if (hm.participantId === p.name) {
						record.displayObj.participants.push({
							isFemale: hm.isFemale,
							isMother: hm.isMother,
							lifeStage: hm.lifeStage,
							participantId: hm.participantId,
							age: hm.age,
							years: hm.years,
							months: hm.months,
							color: hm.color,
							id: hm.id
						});
						record.displayObj.guestInfo = record.guestInfo = result.data;
						break;
					}
				}
		    }, function(result) {
		      alert('Failed to update: ' + result.data.message);
		    });
		} else {
			var pCount = 0;
			for (var ip = 0; ip < record.editParticipants.length; ip++) {
				if (record.editParticipants[ip].checked)
					pCount++;
			}
			if (pCount == 0) {
				p.checked = true;
				alert("Could not remove participant, there must be at least one assigned participant.");
				return;
			}

			Restangular.oneUrl("/ImageRecords/" + record.id + "/DeleteParticipantByName/" + p.name).customDELETE().then(function(result) {
				for (var i = 0; i < record.displayObj.participants.length; i++) {
					if (record.displayObj.participants[i].participantId === p.name) {
						record.displayObj.participants.splice(i, 1);
						record.displayObj.guestInfo = record.guestInfo = result.data;
						return;
					}
				}
		    }, function(result) {
		      alert('Failed to update: ' + result.data.message);
		    });
		}
	};
	$scope.printConversionMessage = function(history) {
		switch (history.conversionType) {
			case 'IRCreate':
				return 'Created new Eat Record';
			case 'CRCreate':
				return 'Created new Recipe';
			case 'CICreate':
				return 'Created new Ingredient';
			case 'CRAdd':
				return 'Added new ingredient to recipe';
			case 'ChangeImage':
				return 'Changed image';
			case 'IRFromCI':
				return 'Converted to Eat Record';
			case 'CIFromIR':
				return 'Converted to Ingredient';
			case 'CRFromIR':
				return 'Converted to Recipe';
			case 'IRFromCR':
				return 'Converted to Eat Record';
			case 'CIFromCR':
				return 'Converted to Ingredient';
			case 'ERLFromIR':
				return 'Converted to Leftovers';
			default:
				return 'Error loading history'
		}
	};
	$scope.showConversionHistories = function(histories, evt) {
		$scope.conversionHistories = histories;

		if (evt) {
			$('#historyModal').modal('show');	
			evt.stopPropagation();
		}
	};
	$scope.closeHistoryModal = function(href) {
		// $("#historyModal").modal("hide");
		setTimeout(function	() {
			$window.location.href = href;
		}, 200);
		//Actually losing my mind, need to set a delay or it won't clear the modal before the page changes and then you're stuck with it
		//Could fix by doing properly https://embed.plnkr.co/plunk/vmPRBY
	};
	$scope.selectRecipe = function(recipe) {
		$scope.selectedRecipe = recipe;
	};
	$scope.selectParticipant = function(participant) {
		$scope.selectedParticipant = participant;
	};
	$scope.toggleParticipant = function(participant) {
		participant.hide = !participant.hide;
	};

	$scope.styleCommentIndicator = function(record) {
		var style = {};
		if (record.comments) {
			var all = true;
			for (var i = 0; i < record.comments.length; i++) {
				if (!record.comments[i].hidden)
					all = false;
			}
			if (all)
				style['color'] = '#ababab';
		} 
		return style;
	};
	$scope.toggleAllComments = function(day) {
		day.allCommentsChecked = !day.allCommentsChecked;

		for (var i = 0; i < day.comments.length; i++) {
			day.comments[i].checked = day.allCommentsChecked;
		}
	};
	$scope.checkComment = function(check, comment, day) {
		comment.checked = check;

		if (!comment.checked) {
			day.allCommentsChecked = false;
			return;
		}
		var all = true;
		for (var i = 0; i < day.comments.length; i++) {
			if (!day.comments[i].checked) {
				all = false;
				break;
			}
		}
		day.allCommentsChecked = all;
	};
	$scope.selectComments = function(record, checked) {
		//record.allCommentsChecked = !record.allCommentsChecked;
		if (!record.day.showComments)
			record.day.showComments = true;
		if (checked === undefined)
			checked = true;
		var all = checked;
		for (var i = 0; i < record.comments.length; i++) {
			for (var j = 0; j < record.day.comments.length; j++) {
				if (record.comments[i].id === record.day.comments[j].id) {
					if (record.day.comments[j].hidden)
						continue;
					if (!record.day.comments[j].checked && checked)
						all = false;
					$scope.checkComment(checked, record.day.comments[j], record.day);
					// record.day.comments[j].checked = record.allCommentsChecked;
					break;
				}
			}
		}
		if (all)
			$scope.selectComments(record, false);
	};
	$scope.addComment = function(record) {
		if (!record.newComment)
			return;
		var comment = {authorName: "Me", text: record.newComment, flag: "normal", createdTime: formatDateString(new Date())}
		record.comments.push(comment);
		record.newComment = undefined;
		record.day.comments.push(comment);
		Restangular.one('ImageRecords/' + record.id + '/Comments').post('', comment).then(function(result) {
			comment.id = result.data;
		}, function(result) {
			alert("Comment could not be uploaded: " + result.message)
		});
	};
	$scope.addCommentRecipe = function(recipe) {
		if (!recipe.newComment)
			return;
		var comment = {authorName: "Me", text: recipe.newComment, flag: "normal"}
		recipe.comments.push(comment);
		recipe.newComment = undefined;
		Restangular.one('CookRecipes/' + recipe.id + '/Comments').post('', comment).then(function(result) {
			comment.id = result.data;
		}, function(result) {
			alert("Comment could not be uploaded: " + result.message)
		});
	};
	$scope.deleteComment = function(comment) {
		if (!confirm('Are you sure you want to hide this comment?'))
			return;
		comment.hidden = true;
		comment.checked = false;

		Restangular.one('Comments/' + comment.id).customDELETE().then(function(result) {
		}, function(result) {
			comment.hidden = false;
			alert("Comment could not be deleted: " + result.message)
		});
	};

	$scope.showRecipes = true;
	$scope.toggleRecipes = function() { $scope.showRecipes = !$scope.showRecipes; };

	$scope.shouldShowRecord = function(record) {
		if (record.isRecipe) {
			if ($scope.showRecipes)
				return true;
			return false;
		}
		var showing = false;
		if (record.id === 3830)
			showing = false;
		for (var i = 0; i < record.participants.length; i++) {
			for (var j = 0; j < $scope.household.householdMembers.length; j++) {
				if ($scope.household.householdMembers[j].id === record.participants[i].id) {
					if (!$scope.household.householdMembers[j].hide)
						showing = true;//return true;
					break;
				}
			}
		}
		return showing;
	};
	$scope.styleRecord = function(record) {
		var style = {};
		if (record.isRecipe) {
			style['padding-bottom'] = '33px';
			style['background-color'] = '#cbd6be';
		}
		else
			style['padding-bottom'] = '0px';
		return style;
	};
	$scope.styleComment = function(record) {
		if (record && record.comments)
			for (var i = 0; i < record.comments.length; i++) {
				var cm = record.comments[i];
				for (var j = 0; j < record.day.comments.length; j++) {
					if (cm.id === record.day.comments[j].id) {
						if (record.day.comments[j].checked)
							return {outline: '3px solid #e74c3c'};
						break;
					}
				}
			}
		return {};
	}
	$scope.shouldShowDay = function(day) {
		for (var i = 0; i < $scope.searchedDays.length; i++) {
			if ($scope.searchedDays[i].date === day.dateStr) {
				if ($scope.searchedDays[i].checked)
					return true;
				else
					return false;
			}
		}
		return false;
	};
	$scope.reassignDays = function() {
		if ($scope.selectedHousehold !== $scope.household.participantId)
			return;
		$scope.searchedDays = $.extend(true, [], $scope.searchDays);
	};
	$scope.getToolTip = function(record) {
		var tt = '';
		if (record.isRecipe)
			return 'Recipe\n\nTotal prepared weight:\n' + record.totalCookedGrams.toFixed(1) + 'g';
		else if (record.recordType === 'EatRecord') {
			// tt = 'Eat Record\n\n';
			tt = ((record.guestInfo && record.guestInfo.totalHeads > 1) ? 'Shared Plate' : 'Own Plate');
		} else if (record.recordType === 'Ingredient')
			tt = "Ingredient\n" + $scope.cookMethodToString(record.ingredient.cookMethod);
		else
			tt = record.recordType;

		if (record.foodItems) {
			tt += '\n\n';
			if (record.foodItems.length == 0)
				tt += 'No items';
			for (var i = record.foodItems.length - 1; i >= 0; i--) {
				var fi = record.foodItems[i];
				if (!fi.name)
					continue;
				var rIdx = fi.name.regexIndexOf(/[^a-z ]/i);
				if (rIdx <= 0)
					rIdx = fi.name.length;
				tt += fi.quantityGrams.toFixed(1) + 'g ' + fi.name.substring(0, rIdx) + '\n';
			}
		}
		return tt;
	};

	$scope.toggleHidden = function(record) {
		if (!record.hidden && !confirm('Hidden records will not appear for analysts or in review pages. Are you sure you want to hide this record?'))
			return;
		record.hidden = !record.hidden;

		Restangular.allUrl('ImageRecords/' + record.id).customPOST(JSON.stringify(record.hidden)).then(function() {
			return;
		}, function(error) {
			alert("Could not hide record: " + error.data.message);
			return;
		});
	};
	$scope.toggleHiddenRecipe = function(recipe) {
		if (!recipe.hidden && !confirm('Hidden recipes will not appear for analysts, as a food item or in review pages. Are you sure you want to hide this recipe?'))
			return;
		recipe.hidden = !recipe.hidden;

		Restangular.allUrl('CookRecipes/' + recipe.id).customPOST(JSON.stringify(recipe.hidden)).then(function() {
			return;
		}, function(error) {
			alert("Could not hide recipe: " + error.data.message);
			return;
		});
	};
	$scope.saveParticipant = function(participant) {
		$scope.loading++;
		var age = participant.years;
		if (participant.years <= 2 && participant.months > 0)
			age += (participant.months / 12);
		var request = {id: participant.id, age: age, isFemale: participant.isFemale, participantId: participant.participantId, isBreastfed: participant.isBreastfed, lifeStage: participant.lifeStage, 
			weight: participant.weight, height: participant.height, pregnancyTrimester: participant.pregnancyTrimester };
		Restangular.allUrl('HouseholdMembers/' + participant.id).customPUT(request).then(function() {
			return;
		}, function(error) {
			alert("Error: " + error.data.message);
			return;
		}).finally(function() { $scope.loading--; });
		$scope.selectedParticipant = null;
	};

	$scope.findRecordByImage = function(url) {
		for (var i = 0; i < $scope.days.length; i++) {
			for (var j = $scope.days[i].recordings.length - 1; j >= 0; j--) {
				var r = $scope.days[i].recordings[j];
				
				if (r.isRecipe) {
					if (r.imageUrl === url)
						return r;

					for (var iidx = r.ingredients.length - 1; iidx >= 0; iidx--) {
						var ing = r.ingredients[iidx];
						if (ing.imageRecord.imageUrl === url)
							return ing.imageRecord;
					}
				} else {
					if (r.imageRecord && r.imageRecord.imageUrl === url)
						return r.imageRecord;
					if (r.leftovers && r.leftovers.imageUrl === url)
						return r.leftovers;
				}
			}
		}
		return null;
	}

	$scope.findRecordById = function(id, isRecipe) {
		for (var i = 0; i < $scope.days.length; i++) {
			for (var j = $scope.days[i].recordings.length - 1; j >= 0; j--) {
				var r = $scope.days[i].recordings[j];
				
				if (isRecipe) {
					if (r.isRecipe && r.id === id)
						return r;
				} else {
					if (r.isRecipe) {
						for (var iidx = r.ingredients.length - 1; iidx >= 0; iidx--) {
							var ing = r.ingredients[iidx];
							if (ing.imageRecord.id === id)
								return ing.imageRecord;
						}
					} else {
						if (r.imageRecord && r.imageRecord.id === id)
							return r.imageRecord;
						if (r.leftovers && r.leftovers.id === id)
							return r.leftovers;
					}
				}
			}
		}
		return null;
	}

	$scope.allImageIcons;
	$scope.changeImage = function(record) {
		// if (!$scope.allImageIcons) {
		// 	var icons = [];
		// 	for (var i = 0; i < $scope.household.allImages.length; i++) {
		// 		icons.push({
		// 			selected: $scope.household.allImages[i].item1 === record.imageUrl,
		// 			imageUrl: $scope.household.allImages[i].item1,
		// 			data: $scope.household.allImages[i],
		// 			id: record.id,
		// 			isRecipe: record.isRecipe
		// 		});
		// 	}
		// 	$scope.allImageIcons = icons;
		// }
		if (!$scope.allImageIcons) {
			var icons = [];
			icons.push({
				selected: !record.imageUrl,
				imageUrl: null,
				data: {item1: '', item2: ''},
				id: null,
				textDescription: 'No Image'
			});
			for (var d = $scope.days.length - 1; d >= 0; d--) {
				$scope.days[d]
				for (var i = $scope.days[d].recordings.length - 1; i >= 0; i--) {
					var r = $scope.days[d].recordings[i];
					icons.push({
						selected: record.id === r.id,
						imageUrl: r.imageThumbUrl ? r.imageThumbUrl : r.imageUrl,
						data: {item1: r.imageThumbUrl, item2: r.imageUrl},
						id: r.id,
						isRecipe: r.isRecipe,
						textDescription: r.textDescription
					});

					if (r.isRecipe) {
						for (var g = r.ingredients.length - 1; g >= 0; g--) {
							var ing = r.ingredients[g].imageRecord;
							icons.push({
								selected: record.id === ing.id,
								imageUrl: ing.imageThumbUrl ? ing.imageThumbUrl :ing.imageUrl,
								data: {item1: ing.imageThumbUrl, item2: ing.imageUrl},
								id: ing.id,
								isRecipe: false,
								textDescription: ing.textDescription
							});
						}
					}
				}
			}

			for (var i = 0; i < $scope.household.allImages.length; i++) {
				icons.push({
					selected: $scope.household.allImages[i].item1 === record.imageUrl,
					imageUrl: $scope.household.allImages[i].item1,
					data: $scope.household.allImages[i],
					id: record.id,
					isRecipe: record.isRecipe
				});
			}
			$scope.allImageIcons = icons;
		}
		$scope.iconSelected = function(result) {
			//var ir = $scope.findRecordByImage(result.data.item2);
			var ir = $scope.findRecordById(result.id, result.isRecipe);
			$scope.displaySelectOptions = false;
			if (!result)
				return;
			if (!confirm("Are you sure you want to change the image for this record?"))
				return;
			var alsoCopyAudio = ir && confirm("Do you want to also copy the audio and text description accompanying this image?");

			record.imageUrl = result.data.item1;
			var req = {sourceId: record.id, newImageUrl: result.data.item2};
			if (ir && alsoCopyAudio) {
				req.textDescription = ir.textDescription;
				req.newAudioUrl = ir.audioUrl;
				req.transcript = ir.nTranscript;
			}
			$scope.loading++;
			if (record.isRecipe) {
				record.imageThumbUrl = result.data.item1;
				Restangular.one("/Conversion/ChangeRecipeImage").post('', req).then(function() {
				}, function(err) {
					alert("Could not change image.");
				}).finally(function() { $scope.loading--; });
			} else {
				record.displayObj.imageUrl = result.data.item1;
				Restangular.one("/Conversion/ChangeImage").post('', req).then(function() {
				}, function(err) {
					alert("Could not change image.");
				}).finally(function() { $scope.loading--; });
			}
		};
		$scope.alsoCopyAudio = false;
		$scope.displaySelectOptions = $scope.allImageIcons;
	};

	function CustomSelect() {

		// this.ok = function();
	}
	$scope.iconSelected = null;
	$scope.chooseEatRecord = function(record) {
		var icons = [];
		var day = record.day;
		for (var i = 0; i < day.eatRecords.length; i++) {
			var er = day.eatRecords[i];
			if (er.id != record.id) {
				icons.push({
					captureTime: er.captureTime,
					imageUrl: er.imageUrl,
					textDescription: er.textDescription,
					data: er.imageRecord,
					participants: er.participants,
					guestInfo: er.guestInfo
				});
			}
		}
		$scope.iconSelected = function(result) {
			$scope.displaySelectOptions = false;
			if (!result)
				return;
			$scope.destinationRecord = result;
			return result;
		};
		$scope.alsoCopyAudio = undefined;
		$scope.displaySelectOptions = icons;
	};

	$scope.convertToLeftovers = function(source, destination, swtch) {
		if (!source || !destination)
			return;
		if (!confirm("This will overwrite any existing leftovers. Are you sure you would like to convert this record to leftovers for the selected eat record?"))
			return;

		var req = {sourceId: source.id, destinationId: destination.data.id, switch: swtch};
		$scope.destinationSwitch = false;
		$scope.clearConversionSettings();
		$scope.loading++;
		Restangular.one("/Conversion/ConvertRecordToLeftovers").post('', req).then(function() {
			$scope.search();
		}, function(result) {
			alert("Could not convert.");
		}).finally(function() { $scope.loading--; });
	};

	$scope.convertToIngredient = function(source, destination) {
		if (!source || !destination)
			return;

		if (source.isLeftovers === false && source.displayObj.leftovers) {
			if (!confirm("This record has leftovers. If this record is converted, the leftovers will be deleted. Are you sure you would like to convert this record to an ingredient for the selected recipe?"))
				return;
		}
		else if (!confirm("Are you sure you would like to convert this record to an ingredient for the selected recipe?"))
			return;

		var req = {sourceId: source.id, destinationId: parseInt(destination)};
		$scope.clearConversionSettings();
		$scope.loading++;
		Restangular.one("/Conversion/ConvertRecordToIngredient").post('', req).then(function() {
			$scope.search();
		}, function(result) {
			alert("Could not convert.");
		}).finally(function() { $scope.loading--; });
	};

	$scope.updateParticipantCheckbox = function(participant) {
		if (!$scope.destinationParticipants)
			$scope.destinationParticipants = [];
		if ($scope.destinationParticipants.includes(participant.id))
			$scope.destinationParticipants.splice($scope.destinationParticipants.indexOf(participant.id), 1);
		else
			$scope.destinationParticipants.push(participant.id);

		if ($scope.destinationParticipants.length == 0)
			$scope.destinationParticipants = null;
	};

	$scope.clearConversionSettings = function() {
		$scope.destinationParticipants = null;
		$scope.destinationRecord = null;
		$scope.destinationRecipe = null;
		$scope.selectedRecord = null;
		$scope.selectedRecipe = null;
		$scope.newEatRecord = null;
		$scope.newRecipe = null;
		$scope.newIngredient = null;
		for (var i = 0; i < $scope.household.householdMembers.length; i++) {
			$scope.household.householdMembers[i].checked = false;
		}
	};

	$scope.countParticipants = function(participants) {
		var totals = [0, 0, 0];
		for (var i = 0; i < $scope.household.householdMembers.length; i++) {
			var hm = $scope.household.householdMembers[i];
			if (participants.includes(hm.id)) {
				if (hm.age < 18)
					totals[2]++;
				else if (hm.isFemale)
					totals[0]++;
				else
					totals[1]++;
			}
		}
		return totals;
	};
	$scope.convertToEatRecord = function(source, participants) {
		if (!source || !participants)
			return;
		if (!confirm("Are you sure you would like to convert this record to a new eat record?"))
			return;

		var totals = $scope.countParticipants(participants);
		var req = {sourceId: source.id, newParticipants: participants, participantTotals: totals};
		$scope.clearConversionSettings();
		$scope.loading++;
		Restangular.one("/Conversion/ConvertRecordToRecord").post('', req).then(function() {
			$scope.search();
		}, function(result) {
			alert("Could not convert.");
		}).finally(function() { $scope.loading--; });
	}

	$scope.convertToRecipe = function(source) {
		if (!source)
			return;
		if (!confirm("Are you sure you would like to convert this record to a new recipe?"))
			return;

		var req = {sourceId: source.id, textDescription: source.textDescription};
		$scope.clearConversionSettings();
		$scope.loading++;
		Restangular.one("/Conversion/ConvertRecordToRecipe").post('', req).then(function() {
			$scope.search();
		}, function(result) {
			alert("Could not convert.");
		}).finally(function() { $scope.loading--; });
	};


	$scope.convertRecipe = function(source, participants) {
		if (!source || !participants || !confirm("Are you sure you would like to convert this recipe to a new record?"))
			return;

		var totals = $scope.countParticipants(participants);
		var req = {sourceId: source.id, newParticipants: participants, participantTotals: totals};
		$scope.clearConversionSettings();
		$scope.loading++;
		Restangular.one("/Conversion/ConvertRecipeToRecord").post('', req).then(function() {
			$scope.search();
		}, function(result) {
			alert("Could not convert.");
		}).finally(function() { $scope.loading--; });
	}

	$scope.convertRecipeToIngredient = function(source, destination) {
		if (!source || !destination || !confirm("Any attached ingredients will be lost. Are you sure you would like to convert this recipe to an ingredient?"))
			return;

		var req = {sourceId: source.id, destinationId: parseInt(destination)};
		$scope.clearConversionSettings();
		$scope.loading++;
		Restangular.one("/Conversion/ConvertRecipeToIngredient").post('', req).then(function() {
			$scope.search();
		}, function(result) {
			alert("Could not convert.");
		}).finally(function() { $scope.loading--; });
	}

	$scope.convertRecipeToLeftovers = function(source, destination) {
		if (!source || !destination || !confirm("Are you sure you would like to convert this recipe to a new record?"))
			return;

		var req = {sourceId: source.id, destinationId: destination.id};
		$scope.clearConversionSettings();
		$scope.loading++;
		Restangular.one("/Conversion/ConvertRecipeToLeftovers").post('', req).then(function() {
			$scope.search();
		}, function(result) {
			alert("Could not convert.");
		}).finally(function() { $scope.loading--; });
	}

	$scope.startNewRecord = function(day) {
		$scope.newRecord = day;
	}
	$scope.addEatRecord = function(day) {
		if (day) {
			$scope.newEatRecord = {textDescription: ''};
			$scope.newEatRecord.day = day;
		}
		else
			$scope.newEatRecord = null;
		$scope.newRecord = null;
	}
	$scope.addIngredient = function(day, recipe) {
		if (day) {
			$scope.newIngredient = {textDescription: ''};
			$scope.newIngredient.day = day;
			$scope.newIngredient.recipeId = recipe.id;
			var date = recipe.captureTime.substring(0, 17) + '00';
			$scope.newIngredient.captureTime = new Date(date);
		}
		else
			$scope.newIngredient = null;
	}
	$scope.addRecipe = function(day) {
		if (day) {
			$scope.newRecipe = {textDescription: '', name: ''};
			$scope.newRecipe.day = day;
		}
		else
			$scope.newRecipe = null;
		$scope.newRecord = null;
	}

	$scope.createEatRecord = function(newRecord, participants) {
		var captureTime = newRecord.captureTime;
		captureTime.setFullYear(newRecord.day.date.getFullYear());
		captureTime.setMonth(newRecord.day.date.getMonth());
		captureTime.setDate(newRecord.day.date.getDate());

		var totals = $scope.countParticipants(participants);
		var req = {captureTime: formatDateString(captureTime), householdId: $scope.selectedHouseholdId, newParticipants: participants, participantTotals: totals, textDescription: newRecord.textDescription};
		$scope.clearConversionSettings();

		Restangular.one("/Conversion/CreateEatRecord").post('', req).then(function() {
			$scope.search();
		}, function(result) {
			alert("Could not convert.");
		}).finally(function() { $scope.loading--; });
	}
	$scope.createRecipe = function(newRecipe) {
		var captureTime = newRecipe.captureTime;
		captureTime.setFullYear(newRecipe.day.date.getFullYear());
		captureTime.setMonth(newRecipe.day.date.getMonth());
		captureTime.setDate(newRecipe.day.date.getDate());

		var req = {captureTime: formatDateString(captureTime), householdId: $scope.selectedHouseholdId, textDescription: newRecipe.textDescription, name: newRecipe.name};
		$scope.clearConversionSettings();

		Restangular.one("/Conversion/CreateRecipe").post('', req).then(function() {
			$scope.search();
		}, function(result) {
			alert("Could not convert.");
		}).finally(function() { $scope.loading--; });
	}
	$scope.createIngredient = function(newIngredient) {
		var captureTime = newIngredient.captureTime;
		captureTime.setFullYear(newIngredient.day.date.getFullYear());
		captureTime.setMonth(newIngredient.day.date.getMonth());
		captureTime.setDate(newIngredient.day.date.getDate());

		var req = {captureTime: formatDateString(captureTime), householdId: $scope.selectedHouseholdId, destinationId: newIngredient.recipeId, textDescription: newIngredient.textDescription};
		$scope.clearConversionSettings();

		Restangular.one("/Conversion/CreateIngredient").post('', req).then(function() {
			$scope.search();
		}, function(result) {
			alert("Could not convert.");
		}).finally(function() { $scope.loading--; });
	}
}]);


app.controller('breastfeedingController', ['$scope', 'Restangular', '$routeParams', '$window', '$controller', function($scope, Restangular, $routeParams, $window, $controller) {
	$scope.loading = 1;
	$controller('commonCtrl', { $scope: $scope });

	$scope.formatData = function() {
		if (!$scope.allOccasions || !$scope.members)
			return;
		var list = [];
		for (var i = 0; i < $scope.members.length; i++) {
			var member = $scope.members[i];
			var data = {
				id: member.id,
				age: member.age,
				participantId: member.participantId,
				isFemale: member.isFemale,
				occasions: []
			}
			for (var j = 0; j < $scope.allOccasions.length; j++) {
				var occ = $scope.allOccasions[j];
				if (occ.householdMemberId === member.id) {
					data.occasions.push(occ);
				}
			}
			if (data.occasions.length > 0) {
				data.occasions.sort(function(a, b) { return a.localTimeStart > a.localTimeEnd });
				list.push(data);
			}
		}

		$scope.members = list;
		if ($scope.members.length > 0) {
			$scope.member = $scope.members[0];
			$scope.selectedMemberId = $scope.members[0].id;
		}
	};

	$scope.changeSelectedMember = function(id) {
		for (var i = 0; i < $scope.members.length; i++) {
			if ($scope.members[i].id === id) {
				$scope.member = $scope.members[i];
				return;
			};
		}
	};

	$scope.search = function() {
		if (!$scope.selectedHousehold)
			return;

		$scope.allOccasions = $scope.members = $scope.member = null;
		$scope.loading = 2;
		Restangular.one('Households/' + $scope.selectedHousehold + '/Breastfeeding').get()
		.then(function(result) {
			var occasions = result.data;
			for (var i = 0; i < occasions.length; i++) {
				var occ = occasions[i];
				occ.localTimeStart = Date.parse(occ.timeStart);
				occ.localTimeEnd = Date.parse(occ.timeEnd);
				var dur = (occ.localTimeEnd - occ.localTimeStart) / 1000;
				occ.duration = (dur / 60) + "m " + (dur % 60) + "s";
				occ.localTimeStart = new Date(occ.localTimeStart);
				occ.localTimeEnd = new Date(occ.localTimeEnd);
			}
			$scope.allOccasions = occasions;
			$scope.formatData();

			Restangular.one('Households/' + $scope.selectedHouseholdId + '/Members').get()
			.then(function(result) {
				$scope.members = result.data;
				$scope.formatData();
			}).finally(function() { $scope.loading--; });

		}).finally(function() { $scope.loading--; });
	};

	$scope.displayOccasion = function(occ) {
		occ.startTimeStr = formatTime(occ.localTimeStart);
		occ.endTimeStr = formatTime(occ.localTimeEnd);
		$scope.selectedOccasion = occ;
	};
	
	$scope.saveOccasion = function(occ) {
		var start = parseTime(occ.startTimeStr, occ.localTimeStart);
		if (!start)
			return alert('Start time was not in correct format "HH:mm"');
		occ.localTimeStart = start;
		occ.timeStart = offsetTime(start);

		var end = parseTime(occ.endTimeStr, occ.localTimeEnd);
		if (!end)
			return alert('End time was not in correct format "HH:mm"');
		occ.localTimeEnd = end;
		occ.timeEnd = offsetTime(end);

		Restangular.allUrl("/Households/Breastfeeding/" + occ.id).customPUT(occ).then(function() {
		}, function(result) {
			alert(result.data ? result.data : "Could not save occasion.");
			selectedOccasion = occ;
		});

		$scope.selectedOccasion = null;
	};
	$scope.deleteOccasion = function(occ) {
		if (!confirm("Are you sure you want to delete this breastfeeding occasion?"))
			return;
		Restangular.oneUrl("/Households/Breastfeeding/" + occ.id).customDELETE().then(function(result) {
	      for (var i = 0; i < $scope.occasions.length; i++) {
	      	if ($scope.occasions[i].id === occ.id) {
	      		$scope.occasions = $scope.occasions.splice(i, 1);
	      		break;
	      	}
	      }
	    }, function(result) {
	      alert(result.data ? result.data : "An error occurred trying to delete this occasion.");
	    });
	};
}]);

var timeRegex = /^[0-9]{2}:[0-9]{2}$/;
function parseTime(str, date) {
	if (!str.match(timeRegex))
		return null;
	var hh = parseInt(str.substring(0, 2));
	var mm = parseInt(str.substring(3));
	if (hh > 23)
		return null;
	if (mm > 59)
		return null;

	var ret = new Date(date.getTime());
	ret.setHours(hh);
	ret.setMinutes(mm);
	return ret;
};

function offsetTime(date) {
	var offset = date.getTimezoneOffset() * 60000;
	return new Date(date.getTime() - offset);
};

function formatTime(time) {
	var str = time.getHours() + ":" + time.getMinutes();
	if (str.indexOf(":") == 1) str = "0" + str;
	if (str.length < 5)
		str = str.slice(0, 3) + "0" + str.slice(3);
	return str;
};

app.directive("breastfeedingDialog", function() {
  return {
    restrict: 'E',
    scope:false,
    templateUrl: 'views/household/breastfeedingDialog.html'
  }
})

app.directive("householdDataDialog", function() {
  return {
    restrict: 'E',
    scope:false,
    templateUrl: 'views/household/householdDataDialog.html'
  }
})

app.directive("imageSelectDialog", function() {
  return {
    restrict: 'E',
    scope:false,
    templateUrl: 'views/household/imageSelectDialog.html'
  }
})