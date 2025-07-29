angular.module('visida_cms').controller('adminController', ['$scope', 'Restangular', '$window', '$rootScope', function ($scope, Restangular, $window, $rootScope) {
    $scope.roleLoading = $scope.userLoading = true;
    $scope.showUserForm = false;
    $scope.userShowCount = 12;
    $scope.userAppShowCount = 12;
    $scope.userShowTotal = 0;
    $scope.userAppShowTotal = 0;

    $scope.roundPercentage = function (percentage) {
        return Math.round(10 * percentage) / 10;
    };

    Restangular.all('Roles').getList().then(function (roles) {
        $scope.roles = roles.data;
        $scope.roleLoading = false;
    }, function errorCallback() {
        $scope.roleLoading = false;
        $scope.roleError = "Could not load roles.";
    });
    Restangular.all('Users').getList().then(function (result) {
        $scope.users = result.data;
        $rootScope.timeTaken = result.headers('processTime');
        var now = new Date();
        var today = new Date(now.getFullYear(), now.getMonth(), now.getDate());
        var time = today.getTime();
        $scope.appUsers = [];
        for (var i = 0; i < $scope.users.length; i++) {
            var user = $scope.users[i];
            var ll = new Date(user.lastLogin);
            if (ll.getTime() == 0)
                user.displayDate = 'No Login';
            else if (ll < today) {
                // Month index starts at 0, so we need to increment it at one to make it display correctly
                let month = ll.getMonth() + 1;
                user.displayDate = '' + ll.getDate() + '/' + month + '/' + ll.getFullYear();
            }
            else
                user.displayDate = '' + ll.getHours() + ':' + ll.getMinutes() + (ll.getHours() < 12 ? ' AM' : ' PM');

            if (user.role === 'appuser') {
                $scope.appUsers.push(user);
                $scope.users.splice(i, 1);
                i--;
            }
        }

        $scope.userShowTotal = $scope.users.length;
        $scope.userAppShowTotal = $scope.appUsers.length;

        $scope.updateUserSearch("");
        $scope.userLoading = false;
    }, function errorCallback() {
        $scope.userLoading = false;
        $scope.userError = "Could not load users.";
    });

    // Load the studies and their progress in the one api call
    Restangular.all('Studies/Progress').getList().then(function (result) {
        $scope.studies = result.data;
        $scope.studyLoading = false;
        $scope.updateStudySearch("");
        $rootScope.timeTaken = result.headers('processTime');
        for (var i = 0; i < result.data.length; i++) {
            $scope.studies[i].analystsExpanded = false;
            if (result.data[i].households.length > 0) {
                var agg = result.data[i].households.reduce(function (x, y) {
                    return {
                        recordTotal: x.recordTotal + y.recordTotal,
                        recordNotStarted: x.recordNotStarted + y.recordNotStarted,
                        identifyInProgress: x.identifyInProgress + y.identifyInProgress,
                        identifyCompleted: x.identifyCompleted + y.identifyCompleted,
                        portionInProgress: x.portionInProgress + y.portionInProgress,
                        portionCompleted: x.portionCompleted + y.portionCompleted
                    };
                });
                $scope.studies[i].progress = agg;
            }
            else {
                $scope.studies[i].progress = { recordTotal: 0, recordNotStarted: 0 };
            }
        }
    }, function errorCallback() {
        $scope.studyLoading = false;
        $scope.userError = "Could not load studies.";
    });

    //$scope.fctables = Restangular.one('FoodCompositions/GetTableNames').get();
    Restangular.one('FoodCompositionTables').getList().then(function (result) {
        $scope.fctables = result.data;
    });
    $scope.config = Restangular.one('GetSearchConfig').get().$object;
    // Restangular.all('Households').getList().then(function(result) {
    //   $scope.households = result.data;
    // }, function errorCallback() {
    // });

    $scope.updateStudySearch = function (search) {
        var re = buildSearchRegex(search);

        for (var i = 0; i < $scope.studies.length; i++) {
            var study = $scope.studies[i];
            if (study.hidden === undefined)
                study.hidden = false;
            else if (study.name.match(re) ||
                study.households.filter(function (x) { return x.participantId.match(re); }).length > 0 ||
                study.analysts.filter(function (x) { return x.userName.match(re); }).length > 0)// || (study.foodCompositionTable && study.foodCompositionTable.name.match(re)))
                study.hidden = false;
            else
                study.hidden = true;
        }
    };
    $scope.updateUserSearch = function (search) {
        var re = buildSearchRegex(search);

        var count = 0, tCount = 0;
        for (var i = 0; i < $scope.users.length; i++) {
            var user = $scope.users[i];
            if (user.userName.match(re) || (user.email && user.email.match(re))) {
                tCount++;
                user.hidden = false;
            }
            else
                user.hidden = true;
        }
        $scope.userShowTotal = tCount;
    };
    $scope.updateAppUserSearch = function (search) {
        var strs = search.split(' ');
        var searchStr = '^(?=.*' + strs.join(')(?=.*') + ').*$';
        var re = new RegExp(searchStr, 'i');

        var count = 0, tCount = 0;
        for (var i = 0; i < $scope.appUsers.length; i++) {
            var user = $scope.appUsers[i];
            if (user.userName.match(re) || (user.email && user.email.match(re))) {
                if (count >= $scope.userShowCount) {
                    user.hidden = true;
                    tCount++;
                    continue;
                }
                user.hidden = false;
                count++;
            }
            else
                user.hidden = true;
        }
        $scope.userAppShowTotal = tCount;
    };
    $scope.updateAppUserAmount = function () {
        $scope.userShowCount += 6;
        $scope.updateUserSearch($scope.searchApp ? $scope.search : "");
    };
    $scope.createUser = function (email, username, role) {
        $scope.newUserError = "";
        $("#newEmail").css("border-color", "rgb(204, 204, 204)");
        $("#newUsername").css("border-color", "rgb(204, 204, 204)");
        $("#newRole").css("border-color", "rgb(204, 204, 204)");

        var error = false;
        if (!email) {
            $("#newEmail").css("border-color", "#B00020");
            $scope.newUserError += "Email is required\n";
            error = true;
        } else if (! /^\w+([\.-]?\w+)*@\w+([\.-]?\w+)*(\.\w{2,3})+$/.test(email)) {
            $("#newEmail").css("border-color", "#B00020");
            $scope.newUserError += "Email is not valid\n";
            error = true;
        }
        if (!username) {
            $("#newUsername").css("border-color", "#B00020");
            $scope.newUserError += "Username is required\n";
            error = true;
        }
        if (!role) {
            $("#newRole").css("border-color", "#B00020");
            $scope.newUserError += "Role is required\n";
            error = true;
        }
        if (email || username) {
            for (var i = 0; i < $scope.users.length; i++) {
                var user = $scope.users[i];
                if (user.email === email) {
                    $("#newEmail").css("border-color", "#B00020");
                    $scope.newUserError += "Email must be unique\n";
                    error = true;
                    break;
                }
                if (user.userName === username) {
                    $("#newUsernamee").css("border-color", "#B00020");
                    $scope.newUserError += "Username must be unique\n";
                    error = true;
                    break;
                }
            }
            for (var i = 0; i < $scope.appUsers.length; i++) {
                var user = $scope.appUsers[i];
                if (user.email === email) {
                    $("#newEmail").css("border-color", "#B00020");
                    $scope.newUserError += "Email must be unique\n";
                    error = true;
                    break;
                }
                if (user.userName === username) {
                    $("#newUsernamee").css("border-color", "#B00020");
                    $scope.newUserError += "Username must be unique\n";
                    error = true;
                    break;
                }
            }
        }
        if (error)
            return;
        $scope.newUserLoading = true;
        var user = { email: email, userName: username, password: "temporary", role: { role: role } };
        Restangular.oneUrl('Account/Register').post('', user).then(function () {
            $scope.newUserEmail = "";
            $scope.newUserUsername = "";
            $scope.newUserRole = "";
            alert("An invitation has been emailed to " + email);
            $scope.users = Restangular.all('Users').getList().$object;
            $scope.newUserLoading = false;
        }, function (response) {
            $scope.newUserLoading = false;
            $scope.newUserError = "Could not create user.";
        });
    };

    $scope.deleteUser = function (user) {
        if (!confirm("Are you sure you want to delete user?!"))
            return;
        $scope.userError = "";
        $scope.userLoading = true;

        Restangular.oneUrl("Users/" + user).remove().then(function () {
            $scope.users = Restangular.all('Users').getList().$object;
            $scope.userLoading = false;
        }, function (response) {
            $scope.userLoading = false;
            $scope.userError = "Could not delete user.";
        });
    };

    $scope.createStudy = function () {
        var name = prompt("Please enter a name for the study.", "");
        if (name == null || name.length <= 0)
            return;

        $scope.userLoading = true;
        var study = { name: name };
        Restangular.oneUrl('Studies').post('', study).then(function (result) {
            $window.location = "#!/admin/study?id=" + result.data.id;
            $scope.userLoading = false;
        }, function (response) {
            $scope.userLoading = false;
            alert("Could not add study. " + (response.data != null ? response.data.message : "An error has occurred"));
        });
    };

    $scope.updateStudyTable = function (study, tableid) {
        if (!tableid || tableid == null)
            return;
        if (!study.foodCompositionTable)
            study.foodCompositionTable = {};
        study.foodCompositionTable.id = tableid;
        study.households = null;
        study.put();
    };
    $scope.assignStudyAnalyst = function (study, id) {
        var user = $scope.users.filter(function (item) { return item.id == id })[0];
        if (!user)
            return;

        if (study.analysts == null)
            study.analysts = new Array();
        if (study.analysts.filter(function (item) { return item.id == user.id }).length > 0)
            return;

        study.analysts.push({ id: user.id, userName: user.userName });
        //var temphh = study.households = null;
        study.put().then(function (response) {
            $scope.studies = Restangular.all('Studies').getList().$object;
        }, function (error) {
            study.analysts.splice(study.analysts.length - 1, 1);
            $scope.studyError = "Could not update study. " + error.data.message;
        });
    };
    $scope.unassignStudyAnalyst = function (study, id) {
        var user = $scope.users.filter(function (item) { return item.id == id })[0];
        if (!user)
            return;

        var index = study.analysts.indexOf(user);
        study.analysts.splice(index, 1);
        study.put().then(function (response) {
            $scope.studies = Restangular.all('Studies').getList().$object;
        }, function (error) {
            study.analysts.splice(index, 0, user);
            $scope.studyError = "Could not update study. " + error.data.message;
        });
    };
    $scope.lastColIndex = -1;
    $scope.laseDirection = 'desc'
    $scope.sortUserTable = function (colIndex) {
        // Check if this is a second click and we need to sort descending
        let asc = true;
        if (colIndex === $scope.lastColIndex && $scope.lastDirection === 'asc') {
            asc = false;
            $scope.lastDirection = 'des';
        }
        else {
            $scope.lastDirection = 'asc';
        }
        $scope.lastColIndex = colIndex;
        var table, rows, switching, i, x, y, shouldSwitch;
        table = document.getElementById("userTable");
        switching = true;
        /* Make a loop that will continue until
        no switching has been done: */
        while (switching) {
            // Start by saying: no switching is done:
            switching = false;
            // We don't want to look at the first two rows
            rows = table.rows;
            /* Loop through all table rows (except the
            first one, which contains table headers): */
            for (i = 1; i < (rows.length - 1); i++) {
                // Start by saying there should be no switching:
                shouldSwitch = false;
                /* Get the two elements you want to compare,
                one from current row and one from the next: */
                // First row is th tags
                if (colIndex === 0) {
                    x = rows[i].getElementsByTagName("TH")[0].getElementsByTagName("a")[0];
                    if (x === undefined) {
                        x = rows[i].getElementsByTagName("TH")[0].getElementsByTagName("span")[0];
                    }
                    y = rows[i + 1].getElementsByTagName("TH")[0].getElementsByTagName("a")[0];
                    if (y === undefined) {
                        y = rows[i + 1].getElementsByTagName("TH")[0].getElementsByTagName("span")[0];
                    }
                } else {
                    x = rows[i].getElementsByTagName("TD")[colIndex - 1];
                    y = rows[i + 1].getElementsByTagName("TD")[colIndex - 1];
                }
                // Send to a separate function if we are comparing login dates (we have some as dates, some as times and some as "No Login")
                if (colIndex === 3) {
                    if (asc) {
                        shouldSwitch = compareLoginDates(x.innerHTML, y.innerHTML);
                    } else {
                        shouldSwitch = compareLoginDates(y.innerHTML, x.innerHTML);
                    }
                    if (shouldSwitch) {
                        break;
                    }
                } else {
                    // Check if the two rows should switch place:
                    let innerX = x.innerHTML.toUpperCase();
                    let innerY = y.innerHTML.toUpperCase();
                    if (asc && innerX > innerY) {
                        // If so, mark as a switch and break the loop:
                        shouldSwitch = true;
                        break;
                    } else if (!asc && innerX < innerY) {
                        // If so, mark as a switch and break the loop:
                        shouldSwitch = true;
                        break;
                    }
                }
            }
            if (shouldSwitch) {
                /* If a switch has been marked, make the switch
                and mark that a switch has been done: */
                rows[i].parentNode.insertBefore(rows[i + 1], rows[i]);
                switching = true;
            }
        }
    };
    // Helper function for sorting user table
    // Returns true if y should be before x
    compareLoginDates = (x, y) => {
        if (y === "No Login") {
            return false;
        } else if (x === "No Login") {
            return true;
        } else if (!x.includes("/")) {
            if (!y.includes("/")) {
                let arrXTime = x.split(":");
                let xHours = arrXTime[0];
                let xMins = arrXTime[1].split(" ")[0];
                let arrYTime = y.split(":");
                let yHours = arrYTime[0];
                let yMins = arrYTime[1].split(" ")[0];
                return (xHours + 60 * xMins) > (yHours + 60 * yMins);
            }
            return false;
        } else if (!y.includes("/")) {
            return true;
        } else {
            let arrStartDate = x.split("/");
            let date1 = new Date(arrStartDate[2], arrStartDate[1], arrStartDate[0]);
            let arrEndDate = y.split("/");
            let date2 = new Date(arrEndDate[2], arrEndDate[1], arrEndDate[0]);
            return date1 < date2;
        }
    };

    $scope.lastSortType = -1;
    $scope.laseDirection = 'desc'
    // name sortType = 0, date sortType = 1
    $scope.sortStudies = function (sortType) {
        // Check if this is a second click and we need to sort descending
        let asc = true;
        if (sortType === $scope.lastSortType && $scope.lastDirection === 'asc') {
            asc = false;
            $scope.lastDirection = 'des';
        }
        else {
            $scope.lastDirection = 'asc';
        }
        $scope.lastSortType = sortType;

        var studies, switching, i, shouldSwitch;
        switching = true;
        /* Make a loop that will continue until
        no switching has been done: */
        while (switching) {
            // Start by saying: no switching is done:
            switching = false;
            // Get reference to studies
            studies = $scope.studies;
            // Loop through all table rows
            for (i = 0; i < (studies.length - 1); i++) {
                // Start by saying there should be no switching:
                shouldSwitch = false;
                /* Get the two elements you want to compare,
                one from current row and one from the next: */
                let x, y;
                if (sortType === 0) {
                    x = studies[i].name;
                    y = studies[i + 1].name;
                } else if (sortType === 1) {
                    // TODO - handling sort by date - we don't currently have the data to do this
                    x = studies[i].id;
                    y = studies[i + 1].id;
                }
                // Check if the two rows should switch place:
                if (asc && x > y) {
                    // If so, mark as a switch and break the loop:
                    shouldSwitch = true;
                    break;
                } else if (!asc && x < y) {
                    // If so, mark as a switch and break the loop:
                    shouldSwitch = true;
                    break;
                }
            }
            if (shouldSwitch) {
                /* If a switch has been marked, make the switch
                and mark that a switch has been done: */
                // Remove the study
                let tempStudy = studies[i];
                studies.splice(i, 1);
                // Insert study at index after
                studies.splice(i + 1, 0, tempStudy);
                switching = true;
            }
        }
    };
}])

angular.module('visida_cms').controller('userController', ['$scope', 'Restangular', '$routeParams', function ($scope, Restangular, $routeParams) {
    var id = $routeParams.id;
    $scope.activeStudy = null;
    $scope.activityToDate = new Date();
    $scope.activityFromDate = new Date();
    $scope.activityFromDate.setDate($scope.activityToDate.getDate() - 6);
    $scope.loading = 1;
    $scope.fullLoad = true;

    Restangular.all('Roles').getList().then(function (roles) {
        $scope.roles = roles.data;
    }, function errorCallback() {
        alert("Could not load roles.");
    });

    Restangular.one('Users', id).get().then(function (response) {
        $scope.user = response.data;

        $scope.tests = $scope.user.tests.slice(0, 5);

        var chrtData = [], chrtLabels = [], chrtColors = [];
        var workSet = { data: [0, 0, 0], backgroundColor: ["#2196F3", "#FF5722", "#9C27B0"], label: "Work Done" };
        var workData = [{ time: 0, items: 0, avg: 0 }, { time: 0, items: 0, avg: 0 }, { time: 0, items: 0, avg: 0 }];
        var colors = ['#e6194B', '#3cb44b', '#ffe119', '#4363d8', '#f58231', '#911eb4', '#42d4f4', '#f032e6', '#bfef45', '#fabebe', '#469990', '#e6beff', '#9A6324', '#fffac8', '#800000', '#aaffc3', '#808000', '#ffd8b1', '#000075'];

        for (var i = 0; i < $scope.user.studies.length; i++) {
            if ($scope.user.studies[i].households.length > 0) {
                var agg = { recordTotal: 0, recordNotStarted: 0, identifyInProgress: 0, identifyCompleted: 0, portionInProgress: 0, portionCompleted: 0 };//, workCompleted: 0, workTotal: 0};
                for (var j = 0; j < $scope.user.studies[i].households.length; j++) {
                    var h = $scope.user.studies[i].households[j];
                    agg.recordTotal += h.recordTotal;
                    agg.recordNotStarted += h.recordNotStarted;
                    agg.identifyInProgress += h.identifyInProgress;
                    agg.identifyCompleted += h.identifyCompleted;
                    agg.portionInProgress += h.portionInProgress;
                    agg.portionCompleted += h.portionCompleted;
                }
                var work = $scope.user.studies[i].workDone.filter(function (p) { return p.createdById == id });
                for (var j = 0; j < work.length; j++) {
                    workSet.data[work[j].type] += work[j].timeTaken;
                    workData[work[j].type].items++;
                }

                agg.workCompleted = work.length;
                agg.workTotal = $scope.user.studies[i].workDone.length;//$scope.user.studies[i].workDone.reduce(function (acc, cur) { return acc + cur.items }, 0);
                $scope.user.studies[i].progress = agg;
                if (agg.workCompleted > 0) {
                    chrtData.push(agg.workCompleted);
                    chrtLabels.push($scope.user.studies[i].name);
                    chrtColors.push(colors[chrtColors.length]);
                }
            }
            else {
                $scope.user.studies[i].progress = { recordTotal: 1, recordNotStarted: 1 };
            }
        }

        workSet.data[0] = (workSet.data[0] / 1000 / 60).toFixed(2);
        workSet.data[1] = (workSet.data[1] / 1000 / 60).toFixed(2);
        workSet.data[2] = (workSet.data[2] / 1000 / 60).toFixed(2);
        $scope.workConfig = {
            type: 'pie',
            data: {
                datasets: [workSet],
                labels: ["Identifying", "Quantifying", "Both Together"]
            },
            options: {
                responsive: true
            }
        };
        var workCtx = document.getElementById('chart-area-work').getContext('2d');
        $scope.workChart = new Chart(workCtx, $scope.workConfig);

        for (var j = 0; j < workData.length; j++) {
            workData[j].time = workSet.data[j];
            workData[j].avg = Math.round((workData[j].time / workData[j].items) * 100) / 100;
        }
        $scope.baseWorkData = workData;
        $scope.workData = workData;

        var config = {
            type: 'pie',
            data: {
                datasets: [{
                    data: chrtData,
                    backgroundColor: chrtColors,
                    label: 'Activity'
                }],
                labels: chrtLabels
            },
            options: {
                responsive: true
            }
        };
        var ctx = document.getElementById('chart-area-studies').getContext('2d');
        $scope.studiesChart = new Chart(ctx, config);
        $scope.studiesChart.options.onClick = function (evt) {
            var points = $scope.studiesChart.getElementsAtEvent(evt);
            var chartData = points[0]['_chart'].config.data;
            var idx = points[0]['_index'];
            var label = chartData.labels[idx];

            var study = $scope.user.studies.filter(function (x) { return x.name === label })[0];
            var dataset = { data: [0, 0, 0], backgroundColor: ["#2196F3", "#FF5722", "#9C27B0"], label: "Work Done" };
            for (var i = 0; i < study.workDone.length; i++) {
                if (study.workDone[i].createdById == id) {
                    dataset.data[study.workDone[i].type] += study.workDone[i].timeTaken;
                }
            }
            dataset.data[0] = (dataset.data[0] / 1000 / 60).toFixed(2);
            dataset.data[1] = (dataset.data[1] / 1000 / 60).toFixed(2);
            dataset.data[2] = (dataset.data[2] / 1000 / 60).toFixed(2);

            workConfig = {
                type: 'pie',
                data: {
                    datasets: [dataset],
                    labels: ["Identifying", "Quantifying", "Both Together"]
                },
                options: {
                    responsive: true
                }
            };
            $scope.workChart.destroy();
            $scope.workChart = null;
            $('#chart-area-work').remove(); // this is my <canvas> element
            $('#work-chart-container').append('<canvas id="chart-area-work"><canvas>');
            var workCtx = document.getElementById('chart-area-work').getContext('2d');
            $scope.workChart = new Chart(workCtx, workConfig);
            $scope.$apply(function () { $scope.activeStudy = label; });

            return;
        }

        $scope.buildLineChart();

        Restangular.one('GetSearchConfig').get().then(function (response) {
            $scope.searchConfig = response.data;

            for (var i = 0; i < $scope.user.testRules.length; i++) {
                $scope.user.testRules[i].study = $scope.searchConfig.studies.filter(function (x) { return x.id === $scope.user.testRules[i].study_Id })[0].name;
            }
        });

        $scope.loading--;

    }, function (error) {
        $scope.loading--;
        alert("Could not load user data.");
    }).finally(function () { $scope.fullLoad = false; });

    $scope.resetWork = function () {
        if (!$scope.activeStudy || !$scope.workChart)
            return;
        $scope.loading++;
        $scope.workChart.destroy();
        $scope.workChart = null;
        $('#chart-area-work').remove(); // this is my <canvas> element
        $('#work-chart-container').append('<canvas id="chart-area-work"><canvas>');

        var workCtx = document.getElementById('chart-area-work').getContext('2d');
        $scope.workChart = new Chart(workCtx, $scope.workConfig);
        $scope.activeStudy = null;
        $scope.loading--;
    };

    $scope.assignRole = function (user, role) {
        if (!user || !role)
            return;
        $scope.userError = "";
        $scope.userLoading = true;
        var assign = { userId: user.toString(), role: role };

        Restangular.oneUrl('Users/AssignRole').post('', assign).then(function () {
            $scope.users = Restangular.all('Users').getList().$object;
            $scope.userLoading = false;
        }, function (response) {
            $scope.userLoading = false;
            $scope.userError = "Could not assign role.";
        });
    };

    $scope.resetPassword = function (id, password) {
        if (!id || !password)
            return;
        $scope.loading++;
        Restangular.oneUrl("Account/ResetPassword/" + id).post('', '"' + password + '"').then(function (response) {
            alert("Password successfully updated.");
            $scope.resettingPassword = false;
        }, function (error) {
            alert("Could not update password. " + error.data.message);
        }).finally(function () { $scope.loading--; });
    };

    $scope.deactivateUser = function (active) {
        $scope.loading++;
        Restangular.oneUrl("Users/" + id + "/ActivateUser?active=" + active).post('').then(function (response) {
            $scope.user.isActive = active;
        }, function (error) {
            alert("Could not deactivate user. " + error.data.message);
        }).finally(function () { $scope.loading--; });
    };

    $scope.showMoreTests = function () {
        $scope.tests = $scope.user.tests.slice(0, $scope.tests.length + 5);
        $('#testList').animate({ scrollTop: $('#testList').prop("scrollHeight") }, 500);
    };
    $scope.assignTest = function (knownId, type) {
        if (!knownId)
            return $('#newTestRecordId').addClass('pulse-fail').one('webkitAnimationEnd...', function () { $(this).removeClass('pulse-fail') });
        if (!type)
            return $('#newTestType').addClass('pulse-fail').one('webkitAnimationEnd...', function () { $(this).removeClass('pulse-fail') });

        $scope.loading++;
        var test = { knownRecordId: knownId, userId: id, testType: type };
        Restangular.oneUrl("Reliability").post('', test).then(function (response) {
            test.id = response.data;
            $scope.user.tests.push(test);
            $scope.loading--;
        }, function (error) {
            alert("Could not create test. " + error.data.message);
            $scope.createTest = true;
            $scope.createRule = null;
            $scope.loading--;
        });
        $scope.createTest = null;
    };
    $scope.deleteTest = function (test, index) {
        $scope.loading++;
        Restangular.oneUrl("Reliability/" + test.id).remove().then(function (response) {
            alert("Successfully deleted");
            $scope.user.tests.splice(index, 1);
            $scope.tests = $scope.user.tests.slice(0, $scope.tests.length);
            $scope.loading--;
        }, function (error) {
            alert("Could not delete. " + error.data.message);
            $scope.loading--;
        });
    };


    $scope.ruleDate = new Date();
    $scope.createTestRule = function (study, type, date, repeat) {
        if (!study)
            return $('#ruleStudy').addClass('pulse-fail').one('webkitAnimationEnd...', function () { $(this).removeClass('pulse-fail') });
        if (!type)
            return $('#ruleType').addClass('pulse-fail').one('webkitAnimationEnd...', function () { $(this).removeClass('pulse-fail') });
        if (!date)
            return $('#ruleDate').addClass('pulse-fail').one('webkitAnimationEnd...', function () { $(this).removeClass('pulse-fail') });

        $scope.loading++;
        var rule = {
            user_Id: id,
            study_Id: study,
            testType: type,
            startDate: date,
            repeatDate: repeat
        };

        Restangular.oneUrl("Reliability/Rules").post('', rule).then(function (response) {
            rule.id = response.data;
            $scope.user.testRules.push(rule);
        }, function (error) {
            alert("Could not create rule. " + error.data.message);
            $scope.createRule = true;
            $scope.createTest = null;
        }).finally(function () { $scope.loading--; });
        $scope.createRule = null;
    };

    $scope.deleteTestRule = function (rule, index) {
        Restangular.oneUrl("Reliability/Rules/" + rule.id).remove().then(function (response) {
            alert("Successfully deleted");
            $scope.user.testRules.splice(index, 1);
        }, function (error) {
            alert("Could not delete. " + error.data.message);
        });
    };

    $scope.buildLineChart = function () {
        var today = new Date();
        var days = ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'];
        var months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'June', 'July', 'Aug', 'Sept', 'Oct', 'Nov', 'Dec'];
        var labels = [];
        var dates = [];
        //var data = [{time:0, items:0},{time:0, items:0},{time:0, items:0},{time:0, items:0},{time:0, items:0},{time:0, items:0},{time:0, items:0}];
        var date = new Date($scope.activityFromDate.getTime());
        while (date <= $scope.activityToDate) {
            var dateNum = date.getDate()
            var dateSuff = "";
            switch (dateNum) {
                case 1: dateSuff = "st"; break;
                case 2: dateSuff = "nd"; break;
                case 3: dateSuff = "rd"; break;
                default: dateSuff = "th"; break;
            }
            labels.push(days[date.getDay()] + ' ' + dateNum + dateSuff + ' ' + months[date.getMonth()]);
            dates.push(date);
            date.setDate(date.getDate() + 1);
        }

        // for (var count = 6; count >= 0; count--) {
        //   var date = new Date();
        //   date.setDate(today.getDate() - (6-count));
        //   var dateNum = date.getDate()
        //   var dateSuff = "";
        //   switch (dateNum) {
        //     case 1: dateSuff = "st"; break;
        //     case 2: dateSuff = "nd"; break;
        //     case 3: dateSuff = "rd"; break;
        //     default: dateSuff = "th"; break;
        //   }
        //   labels[count] = days[date.getDay()] + ' ' + dateNum + dateSuff;
        //   data[count] = {};
        //   data[count].date = date;
        // }

        var timeset = Array.apply(null, Array(labels.length)).map(function () { }), itemset = Array.apply(null, Array(labels.length)).map(function () { });
        timeset.fill(0, 0);
        itemset.fill(0, 0);
        for (var i = 0; i < $scope.user.studies.length; i++) {
            var work = $scope.user.studies[i].workDone.filter(function (p) { return p.createdById == id });
            for (var j = 0; j < work.length; j++) {
                var date = new Date(work[j].createdTime);
                var ms = date.getTime() - $scope.activityFromDate.getTime();
                if (ms > 0) {
                    ms = Math.floor(ms / 8.64e+7);
                    if (ms < labels.length) {
                        timeset[ms] += work[j].timeTaken;
                        itemset[ms]++;
                    }
                }
                // for (var k = 0; k < data.length; k++) {
                //   var cc = data[k].date.getDate();
                //   if (cc === bb) {
                //     timeset[k] += work[k].timeTaken;
                //     itemset[k]++;
                //     break;
                //   }
                // }
            }
        }
        for (var i = 0; i < timeset.length; i++) {
            timeset[i] = timeset[i] / 1000 / 60;
        }

        var config = {
            type: 'line',
            data: {
                labels: labels,
                datasets: [{
                    label: 'Time (mins)',
                    fill: false,
                    backgroundColor: "#2196F3",
                    borderColor: "#2196F3",
                    data: timeset,
                    yAxisID: 'time-axis'
                }, {
                    label: 'Items (#)',
                    fill: false,
                    backgroundColor: "#FF5722",
                    borderColor: "#FF5722",
                    data: itemset,
                    yAxisID: 'item-axis'
                }]
            },
            options: {
                responsive: true,
                title: {
                    display: true,
                    text: 'User activity for ' + labels.length + ' days.'
                },
                tooltips: {
                    mode: 'index',
                    intersect: false,
                },
                hover: {
                    mode: 'nearest',
                    intersect: true
                },
                scales: {
                    xAxes: [{
                        display: true,
                        scaleLabel: {
                            display: true,
                            labelString: 'Date'
                        }
                    }],
                    yAxes: [{
                        display: true,
                        scaleLabel: {
                            display: true,
                            labelString: 'Time'
                        }
                        ,
                        position: 'left',
                        id: 'time-axis'
                    }, {
                        display: true,
                        scaleLabel: {
                            display: true,
                            labelString: 'Items'
                        }
                        ,
                        position: 'right',
                        id: 'item-axis'
                    }]
                }
            }
        };

        if ($scope.activityChart) {
            $scope.activityChart.destroy();
            $scope.activityChart = null;
            $('#chart-area-activity').remove(); // this is my <canvas> element
            $('#activity-chart-container').append('<canvas id="chart-area-activity"><canvas>');
        }
        var ctx = document.getElementById('chart-area-activity').getContext('2d');
        $scope.activityChart = new Chart(ctx, config);
    };
}]);



function getColorForPerc(perc, reverse) {
    if (typeof perc === "string")
        perc = parseInt(perc);
    if (reverse)
        perc = 100 - perc;
    var max = 210, min = 40;
    var r = min, g = min, b = min;

    if (perc > 50) {
        r = max;
        g = max - max * (((perc - 50) * 2) / 100);
    } else {
        g = max;
        r = min + max * ((perc * 2) / 100);
    }

    return "rgb(" + r + "," + g + "," + b + ")";
}

angular.module('visida_cms').controller('testController', ['$scope', 'Restangular', '$routeParams', function ($scope, Restangular, $routeParams) {
    var id = $routeParams.id;
    $scope.loading = 1;

    Restangular.one('Reliability', id).get().then(function (response) {
        $scope.test = response.data;

        $scope.metrics = { timeTaken: 0 };
        for (var i = 0; i < $scope.test.timings.length; i++) {
            $scope.metrics.timeTaken += $scope.test.timings[i].timeTaken;
        }
        $scope.metrics.timeTaken = ($scope.metrics.timeTaken / 60000).toFixed(2);

        $scope.metrics.diffGram = 0;
        $scope.metrics.diffPerc = 0;
        $scope.metrics.diffCount = 0;
        for (var i = 0; i < $scope.test.imageRecord.foodItems.length; i++) {
            var item = $scope.test.imageRecord.foodItems[i];
            var known = $scope.test.knownRecord.foodItems.filter(function (x) {
                return x.foodCompositionId === item.foodCompositionId
            })[0];
            if (known) {
                if (known.quantityGrams > 0) {
                    item.diffGram = known.quantityGrams - item.quantityGrams;
                    item.diffSign = item.diffGram > 0 ? '-' : '+';
                    item.diffGram = Math.abs(item.diffGram);
                    item.diffPerc = ((item.diffGram / known.quantityGrams) * 100).toFixed(2);
                    item.diffCol = getColorForPerc(item.diffPerc);
                    $scope.metrics.diffGram += item.diffGram;
                    $scope.metrics.diffPerc += (item.diffGram / known.quantityGrams);
                    $scope.metrics.diffCount++;
                }
            } else {
                for (var j = 0; j < $scope.test.scores.length; j++) {
                    var score = $scope.test.scores[j];
                    if (item.id === score.foodItemId) {
                        if (score.type === 0)
                            item.identScore = score;
                        else if (score.type === 1)
                            item.quantScore = score;
                    }
                }
                if (!item.identScore)
                    item.identScore = { type: 0, foodItemId: item.id };
                if (!item.quantScore && $scope.test.testType > 0)
                    item.quantScore = { type: 1, foodItemId: item.id };
            }
        }
        $scope.metrics.diffPerc = (($scope.metrics.diffPerc / $scope.metrics.diffCount) * 100).toFixed(2);
        $scope.metrics.diffCol = getColorForPerc($scope.metrics.diffPerc);

        var knownNut = {}, testNut = {}, comparison = {};
        $scope.fctConfig = $.extend(true, {}, fctConfigStart);

        for (var i = 0; i < $scope.test.knownRecord.foodItems.length; i++) {
            var foodItem = $scope.test.knownRecord.foodItems[i];
            var keys = Object.keys(foodItem.foodCompositionDatabaseEntry);
            for (var l = keys.length - 1; l >= 0; l--) {
                if (!$scope.fctConfig.hasOwnProperty(keys[l]) || !$scope.fctConfig[keys[l]].show) //Exclude inherited variables
                    continue;

                if (foodItem.foodCompositionDatabaseEntry[keys[l]] == null) {
                    $scope.fctConfig[keys[l]].show = false;
                    continue;
                }

                if (knownNut[keys[l]])
                    knownNut[keys[l]] += foodItem.foodCompositionDatabaseEntry[keys[l]] * (foodItem.quantityGrams / 100);
                else
                    knownNut[keys[l]] = foodItem.foodCompositionDatabaseEntry[keys[l]] * (foodItem.quantityGrams / 100);
            }
        }

        for (var i = 0; i < $scope.test.imageRecord.foodItems.length; i++) {
            var foodItem = $scope.test.imageRecord.foodItems[i];
            var keys = Object.keys(foodItem.foodCompositionDatabaseEntry);
            for (var l = keys.length - 1; l >= 0; l--) {
                if (!$scope.fctConfig.hasOwnProperty(keys[l]) || !$scope.fctConfig[keys[l]].show) //Exclude inherited variables
                    continue;

                if (foodItem.foodCompositionDatabaseEntry[keys[l]] == null) {
                    $scope.fctConfig[keys[l]].show = false;
                    continue;
                }

                if (testNut[keys[l]])
                    testNut[keys[l]] += foodItem.foodCompositionDatabaseEntry[keys[l]] * (foodItem.quantityGrams / 100);
                else
                    testNut[keys[l]] = foodItem.foodCompositionDatabaseEntry[keys[l]] * (foodItem.quantityGrams / 100);
            }
        }

        var keys = Object.keys($scope.fctConfig);
        for (var l = 0; l < keys.length; l++) {
            var diff = Math.abs(knownNut[keys[l]] - testNut[keys[l]]);
            comparison[keys[l]] = {};
            comparison[keys[l]].grams = diff;
            comparison[keys[l]].perc = ((diff / knownNut[keys[l]]) * 100);
            comparison[keys[l]].color = getColorForPerc(comparison[keys[l]].perc);
        }

        $scope.comparison = comparison;
        $scope.knownNut = knownNut;
        $scope.testNut = testNut;
    }, function (error) {
        alert("Failed to load.");
    }).finally(function () { $scope.loading--; });

    $scope.updateScore = function (score, accuracy) {
        score.accuracy = accuracy;
        Restangular.oneUrl('Reliability/' + $scope.test.id + '/Score').post('', score).then(function (response) {
            if (!score.id)
                score.id = response.data;
        }, function (response) {
            alert("Could not save test score");
        });
    };
    $scope.perColor = function (perc, reverse) {
        return getColorForPerc(perc, reverse);
    };
}]);