angular.module('visida_cms').controller('usageController', ['$scope', 'Restangular', '$routeParams', '$window', function($scope, Restangular, $routeParams, $window) {
	$scope.loading = 1;

	Restangular.one('GetSearchConfig').get()
	.then(function(result) {
		$scope.searchConfig = result.data;
		$scope.searchHouseholds = $scope.searchConfig.households;
	}).finally(function() { $scope.loading--; });

	$scope.studyChanged = function() {
		if (!$scope.selectedStudy) {
			$scope.searchHouseholds = $scope.searchConfig.households;
			return;
		}
		//var sid = parseInt($scope.selectedStudy);
		var study = $scope.searchConfig.studies.filter(function(x) { return x.id === $scope.selectedStudy})[0];
		$scope.searchHouseholds = study.households;
		$scope.selectedHouseholdId = null;
	};

	$scope.householdChanged = function() {
		if (!$scope.selectedStudy) {
			var study = $scope.searchConfig.studies.filter(function(x) { return  x.households.includes($scope.selectedHousehold); })[0];
			$scope.selectedStudy = study.id;
			$scope.searchHouseholds = study.households;
		}
	};

	$scope.search = function() {
		//var study = $scope.searchConfig.studies.filter(function(x) { return  x.households.filter(function(y) { return y.name === $scope.selectedHousehold }); })[0];
		//var hh = study.households.filter(function(x) { return x.participantId === $scope.selectedHousehold })[0];
		$scope.loading++;
		Restangular.one('Households/' + $scope.selectedHousehold + "/UsageLog").get().then(function(result) {
			$scope.logFile = result.data;

			var lines = $scope.logFile.rawData.split("\n");
			$scope.times = [];
			var timeRegex = /([0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2}.[0-9]{3})/ig
			for (var i = 0; i < lines.length; i++) {
				var line = lines[i].trim();
				var timeStr = line.match(timeRegex);
				if (timeStr && timeStr.length > 0)
					$scope.times.push(timeStr[0]);
			}
			var min = new Date(Date.parse("2020-" + $scope.times[0]));
			var max = new Date(Date.parse("2020-" + $scope.times[$scope.times.length-1]));
			var dates = [];

			min.setHours(0);
			min.setMinutes(0);
			min.setSeconds(0);
			min.setMilliseconds(0);
			var dayDiff = (max.getTime() - min.getTime()) /  (1000 * 60 * 60 * 24);
			dayDiff++;
			for (var i = 0; i < dayDiff; i++) {
				var date = new Date(min.getTime());
				date.setDate(date.getDate() + i);
				dates.push(date);
			}
			return;

			var timeFormat = 'MM/DD/YYYY HH:mm';

			function newDateString(days) {
				return moment().add(days, 'd').format(timeFormat);
			}

			var color = Chart.helpers.color;
			var config = {
				type: 'line',
				data: {
					labels: dates,
					datasets: [{
						label: 'My First dataset',
						backgroundColor: color(window.chartColors.red).alpha(0.5).rgbString(),
						borderColor: window.chartColors.red,
						fill: false,
						data: [
							randomScalingFactor(),
							randomScalingFactor(),
							randomScalingFactor(),
							randomScalingFactor(),
							randomScalingFactor(),
							randomScalingFactor(),
							randomScalingFactor()
						],
					}, {
						label: 'My Second dataset',
						backgroundColor: color(window.chartColors.blue).alpha(0.5).rgbString(),
						borderColor: window.chartColors.blue,
						fill: false,
						data: [
							randomScalingFactor(),
							randomScalingFactor(),
							randomScalingFactor(),
							randomScalingFactor(),
							randomScalingFactor(),
							randomScalingFactor(),
							randomScalingFactor()
						],
					}, {
						label: 'Dataset with point data',
						backgroundColor: color(window.chartColors.green).alpha(0.5).rgbString(),
						borderColor: window.chartColors.green,
						fill: false,
						data: [{
							x: newDateString(0),
							y: randomScalingFactor()
						}, {
							x: newDateString(5),
							y: randomScalingFactor()
						}, {
							x: newDateString(7),
							y: randomScalingFactor()
						}, {
							x: newDateString(15),
							y: randomScalingFactor()
						}],
					}]
				},
				options: {
					title: {
						text: 'Chart.js Time Scale'
					},
					scales: {
						xAxes: [{
							type: 'time',
							time: {
								parser: timeFormat,
								// round: 'day'
								tooltipFormat: 'll HH:mm'
							},
							scaleLabel: {
								display: true,
								labelString: 'Date'
							}
						}],
						yAxes: [{
							scaleLabel: {
								display: true,
								labelString: 'value'
							}
						}]
					},
				}
			};

			window.onload = function() {
				var ctx = document.getElementById('canvas').getContext('2d');
				window.myLine = new Chart(ctx, config);

			};

		}).finally(function() { $scope.loading--; });
	};
}]);