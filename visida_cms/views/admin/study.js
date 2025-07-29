angular.module('visida_cms').controller('studyController', ['$scope', 'Restangular', '$routeParams', '$window', 'UserService', function ($scope, Restangular, $routeParams, $window, UserService) {
    var id = $routeParams.id;
    $scope.studyId = id;
    $scope.newUser = {};
    $scope.loading = 1;
    $scope.edit = false;

    $scope.toggleEdit = function () {
        $scope.edit = !$scope.edit;
    }

    $scope.save = function () {
        $scope.edit = false;
        $scope.putStudy();
    }

    Restangular.one('Studies', id).get().then(function (response) {
        $scope.filterAnalysts = false;
        $scope.study = response.data;
        $scope.initialCountry = $scope.study.countryCode;
        // Count the participants, leftovers
        $scope.study.identifyCompleted = 0;
        $scope.study.participants = 0;
        $scope.study.leftovers = 0;
        $scope.study.recordTotal = 0;
        $scope.study.foodItemTotal = 0;
        $scope.study.recipeTotal = 0;
        $scope.study.hiddenRecipeTotal = 0;
        $scope.study.identifyAndPortionCompleted = 0;
        $scope.study.hiddenRecordTotal = 0;
        $scope.study.analysts.forEach(analyst => {
            analyst.totalAssignedHouseholds = 0;
            analyst.totalCompletedHouseholds = 0;
            analyst.checked = false;
        });

        /////////////////////////////////////////////////////////////////
        // Merge households with same participantId
        let newHouseholdArray = [];
        $scope.study.households.forEach(household => {
            let foundIndex = -1;
            newHouseholdArray.forEach((newHousehold, newHouseholdIndex) => {
                if (newHousehold.participantId === household.participantId) {
                    foundIndex = newHouseholdIndex;
                }
            });
            // This household needs to be added to the array
            if (foundIndex === -1) {
                newHouseholdArray.push(household);
            }
            // This household is already in the array, merge households
            else {
                newHouseholdArray[foundIndex].identifyCompleted += household.identifyCompleted;
                // TODO - merge these better
                newHouseholdArray[foundIndex].householdMembers.concat(household.householdMembers);
                newHouseholdArray[foundIndex].leftOversTotal += household.leftOversTotal;
                newHouseholdArray[foundIndex].recordTotal += household.recordTotal;
                newHouseholdArray[foundIndex].foodItemTotal += household.foodItemTotal;
                newHouseholdArray[foundIndex].recipeTotal += household.recipeTotal;
                newHouseholdArray[foundIndex].hiddenRecipeTotal += household.hiddenRecipeTotal;
                newHouseholdArray[foundIndex].identifyAndPortionCompleted += household.identifyAndPortionCompleted;
                newHouseholdArray[foundIndex].hiddenRecordTotal += household.hiddenRecordTotal;
                // We shouldn't have to merge household assignations as these should be identical
            }
        });
        $scope.study.households = newHouseholdArray;
        /////////////////////////////////////////////////////////////////

        $scope.study.households.forEach(household => {
            $scope.study.identifyCompleted += household.identifyCompleted;
            $scope.study.participants += household.householdMembers.length;
            $scope.study.leftovers += household.leftOversTotal;
            $scope.study.recordTotal += household.recordTotal;
            $scope.study.foodItemTotal += household.foodItemTotal;
            $scope.study.recipeTotal += household.recipeTotal;
            $scope.study.hiddenRecipeTotal += household.hiddenRecipeTotal;
            $scope.study.identifyAndPortionCompleted += household.identifyAndPortionCompleted;
            $scope.study.hiddenRecordTotal += household.hiddenRecordTotal;
            household.hideCompleted = household.identifyAndPortionCompleted === household.recordTotal;
            household.householdAssignations.forEach(assignation => {
                $scope.study.analysts.forEach(analyst => {
                    if (assignation.userId === analyst.userId) {
                        if (household.identifyAndPortionCompleted === household.recordTotal) {
                            analyst.totalCompletedHouseholds++;
                        } else {
                            analyst.totalAssignedHouseholds++;
                        }
                    }
                })
            })
            household.includeInSummary = true;
        });
        if ($scope.study.countryCode)
            $scope.study.countryString = getTextFromOption("countrySelect", $scope.study.countryCode);
        $scope.tableId = $scope.study.foodCompositionTable ? $scope.study.foodCompositionTable.id : -1;
    }, function (error) {

    }).finally(function () { $scope.loading--; });
    Restangular.one('FoodCompositionTables/Public').getList().then(function (result) {
    	var tables = result.data;
        tables.push({id:0, name:"Custom Database", isPublic: true});
        $scope.fctables = tables;
    });
    Restangular.all('Users').getList().then(function (result) {
        $scope.users = result.data;
    }, function errorCallback() {
    });
    Restangular.one('RDA').get().then(function (result) {
        $scope.rdas = result.data;
    }).finally(function () { });

    $scope.updateTable = function (tableId) {
        var studyString = getTextFromOption("studySelect", "number:" + tableId);
        if (UserService.isCoord) {
            if (!confirm("FCD can only be set once, are you sure " + studyString + " is the correct database?")) {
            	document.getElementById("studySelect").selectedIndex = -1;
                return;
            }
        } else
        	return;
        $scope.study.foodCompositionTable = { id: tableId, name: studyString };
        // $scope.study.put();
    };
    function getTextFromOption(id, code) {
        var options = document.getElementById(id);
        if (!options)
            return "";
        options = options.options;
        for (var i = 0; i < options.length; i++) {
            if (options[i].value === code)
                return options[i].text;
        }
        return "";
    }
    $scope.showHelp = function (index) {
        if (index) {
            $(".panel-collapse").collapse("hide");
            $("#" + index).collapse("show");
        }
        $scope.showHelpDialog = true;
    };
    $scope.putStudy = function () {
        //$scope.study.countryCode = countryCode;
        if (UserService.isCoord) {
            var countryString = getTextFromOption("countrySelect", $scope.study.countryCode);
            if ($scope.initialCountry != $scope.study.countryCode && !confirm("Country can only be set once, are you sure " + countryString + " is the correct country?")) {
                $scope.study.countryCode = $scope.initialCountry;
                return;
            }
            $scope.study.countryString = countryString;
        }
        $scope.study.put();
    };
    $scope.updateTranscribe = function () {
        //$scope.study.transcribe = transcribe;
        $scope.study.put();
    };

    $scope.assignAnalyst = function (assignment) {
        var user = null;
        if (assignment.id > 0)
            user = $scope.users.filter(function (item) { return item.id == assignment.userId })[0];
        else
            user = { userName: "" };
        if (!user || !assignment.accessLevel)
            return;

        if ($scope.study.analysts == null)
            $scope.study.analysts = new Array();
        var body = { id: assignment.userId, accessLevel: assignment.accessLevel, email: assignment.email };

        $scope.loading++;
        Restangular.oneUrl("Studies/" + $scope.studyId + "/Assign").post('', body).then(function (response) {
            //$scope.studies = Restangular.all('Studies').getList().$object;
            if (!assignment.id)
                $scope.study.analysts.push({ id: response.data.id, userId: assignment.userId, userName: response.data.userName, accessLevel: assignment.accessLevel });
            $scope.newUser = {};
            $scope.displayAnalystForm = false;
            $('#analyst-' + assignment.userId).addClass('pulse-success').one('webkitAnimationEnd...', function () { $(this).removeClass('pulse-success') });
        }, function (error) {
            alert("Could not update study. " + error.data.message);
        }).finally(function () { $scope.loading--; });
    };
    $scope.unassignAnalyst = function (newUser) {
        var user = $scope.users.filter(function (item) { return item.id == newUser.userId })[0];
        if (!user)
            return;
        if (!confirm("Are you sure you would like to remove " + user.userName + "'s access to this study?"))
            return;

        if (user.role === 'coordinator') {
            var coordCount = $scope.study.analysts.filter(function (item) { return item.accessLevel === 'Coordinator'; }).length;
            if (coordCount == 1) {
                if (!confirm("There is only one coordinator assigned to this study. If you remove this user you may lose access to this study. Are you sure you want to remove this user?"))
                    return;
            }
        }

        $scope.loading++;
        Restangular.oneUrl("Studies/" + $scope.studyId + "/UnAssign/" + newUser.userId).post().then(function (response) {
            for (var i = 0; i < $scope.study.analysts.length; i++) {
                if ($scope.study.analysts[i].userId === newUser.userId) {
                    $scope.study.analysts.splice(i, 1);
                    break;
                }
            }
        }, function (error) {
            $scope.studyError = "Could not update study. " + error.data.message;
        }).finally(function () { $scope.loading--; });
    };
    $scope.reinviteAnalyst = function (user) {
        Restangular.oneUrl("Studies/" + $scope.studyId + "/Reinvite/" + user.userId).get().then(function (response) {

        }, function (err) {
            alert("There was an error attempting to send an invite email. " + (err.data ? err.data.message : "undefined"));
        });
    };

    $scope.renameStudy = function () {
        var name = prompt("Please enter the desired new name for the study.");
        if (name == null)
            return;

        var oName = $scope.study.name;
        $scope.study.name = name;
        $scope.study.put().then(function (response) {
        }, function (error) {
            $scope.study.name = oName;
            $scope.studyError = "Could not update study. " + error.data.message;
        });
    };
    $scope.deleteStudy = function () {
        if (!confirm("Are you sure you want to delete this study? This will also delete any attached households."))
            return;

        $scope.study.remove().then(function (response) {
            alert("Successfully deleted");
            $window.location = "#!/admin/studies";
        }, function (error) {
            $scope.studyError = "Could not update study. " + error.data.message;
        });
    };
    $scope.lastColIndex = 0;
    $scope.lastDirection = 'asc';
    // Sort the table containing details on households
    $scope.sortTable = function (colIndex) {
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
        table = document.getElementById("householdTable");
        switching = true;
        /* Make a loop that will continue until
        no switching has been done: */
        while (switching) {
            // Start by saying: no switching is done:
            switching = false;
            // We don't want to look at the first two rows
            rows = table.rows;
            /* Loop through all table rows (except the
            first two, which contains table headers): */
            for (i = 2; i < (rows.length - 1); i++) {
                // Start by saying there should be no switching:
                shouldSwitch = false;
                /* Get the two elements you want to compare,
                one from current row and one from the next: */
                x = rows[i].getElementsByTagName("TD")[colIndex];
                y = rows[i + 1].getElementsByTagName("TD")[colIndex];
                // Check if the two rows should switch place:
                let xInt = parseInt(x.innerHTML);
                let yInt = parseInt(y.innerHTML);
                if (asc && xInt > yInt) {
                    // If so, mark as a switch and break the loop:
                    shouldSwitch = true;
                    break;
                } else if (!asc && xInt < yInt) {
                    // If so, mark as a switch and break the loop:
                    shouldSwitch = true;
                    break;
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

    $scope.roundPercentage = function (percentage) {
        return Math.round(10 * percentage) / 10;
    };

    // Handling Household checked flag based on the analyst that has been selected
    $scope.updateChecked = function (analyst) {
        $scope.selectedAnalyst = analyst;
        $scope.study.households.forEach(household => {
            let assigned = false;
            household.householdAssignations.forEach(assignation => {
                if (assignation.userId === analyst.userId) {
                    assigned = true;
                }
            })
            household.checked = assigned;
        })
    };
    $scope.toggleHousehold = function (householdId, checked, participantId) {
        let analyst = $scope.selectedAnalyst;
        let household = $scope.study.households.find(x => x.id === householdId);
        // Check if over max Gestalt (0 Gestalt = unlimited)
        if (household.householdAssignations.length >= $scope.study.gestaltMax && checked && $scope.study.gestaltMax !== 0) {
            if (!confirm("Warning! You are attempting to assign more than " + $scope.study.gestaltMax + " analysts to household: " + household.participantId + ". Do you wish to proceed?")) {
                household.checked = false;
                return;
            }
        }
        // Submit to server
        var body = { householdId, analystId: $scope.selectedAnalyst.userId, assign: checked, participantId };
        Restangular.oneUrl("Studies/" + $scope.studyId + "/AssignHousehold").post('', body).then(function (response) {
            if (checked) {
                household.householdAssignations.push(response.data);
            } else {
                const index = household.householdAssignations.findIndex(x => x.userId === $scope.selectedAnalyst.userId);
                household.householdAssignations.splice(index, 1);
            }
            if (household.identifyAndPortionCompleted === household.recordTotal) {
                if (checked) {
                    analyst.totalCompletedHouseholds++;
                } else {
                    analyst.totalCompletedHouseholds--;
                }
            } else {
                if (checked) {
                    analyst.totalAssignedHouseholds++;
                } else {
                    analyst.totalAssignedHouseholds--;
                }
            }
            // Update the household summary table
            $scope.filterAnalyst(false);
        }, function (error) {
            alert("Could not assign to household. " + error.data.message);
        });
    };

    $scope.selectAllHouseholds = function () {
        // TODO
    };

    $scope.changeFilterHouseholds = function (filter) {
        var re = buildSearchRegex(filter);
        for (var i = 0; i < $scope.study.households.length; i++) {
            $scope.study.households[i].hidden = !$scope.study.households[i].participantId.match(re);
        }
    };

    $scope.filterAnalyst = function (startedFiltering) {
        // Tick the "Show only selected analysts" box if they have begun filtering them again
        if (startedFiltering) {
            $scope.filterAnalysts = true;
        }
        // If we are showing all analysts anyway
        if (!$scope.filterAnalysts) {
            $scope.study.households.forEach(household => {
                household.includeInSummary = true;
            })
            return;
        }
        $scope.study.households.forEach(household => {
            household.includeInSummary = false;
            household.householdAssignations.forEach(hAnalyst => {
                $scope.study.analysts.forEach(sAnalyst => {
                    if (sAnalyst.checked && hAnalyst.userId === sAnalyst.userId) {
                        household.includeInSummary = true;
                    }
                })
            })
        })
    }
}]);