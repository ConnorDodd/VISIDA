

app = angular.module('visida_cms', ['restangular', 'ngRoute', 'ngCookies', 'ngFileUpload', 'localytics.directives']);

// implement quantity recognition autofill
// comparison to recommendations
//re-run fiducial markers

app.constant('configSettings', {
    'baseUrl': 'http://localhost:39548/api'
});

app.config(function (RestangularProvider, $routeProvider, configSettings) {

    var Auth = function () {

    }
    $routeProvider
        .when("/", { templateUrl: "views/home/home.html", title: 'Visida' })
        .when("/home", { templateUrl: "views/home/home.html", title: 'Visida' })
        .when("/records", { templateUrl: "views/records/records.html", title: 'Records' })
        .when("/cook", { templateUrl: "views/cook/cook.html", title: 'Cook' })
        .when("/recipe", { templateUrl: "views/cook/recipe.html", title: 'Recipe' })
        //.when("/fiducial",{ templateUrl : "views/fiducial.html" })
        .when("/image", { templateUrl: "views/image/image.html", title: 'Record' })
        .when("/homography", { templateUrl: "views/image/homography.html", title: 'Image Details' })
        .when("/editimage", { templateUrl: "views/image/editimage.html", title: 'Image Details' })
        .when("/upload", { templateUrl: "views/upload/upload.html", title: 'Upload' })
        .when("/recall", { templateUrl: "views/upload/recall.html", title: 'Recall' })
        .when("/reviewdata", { templateUrl: "views/review/review.html", title: 'Review Nutrient Data' })
        .when("/reviewrecipe", { templateUrl: "views/review/recipereview.html", title: 'Review Recipe Data' })
        .when("/reviewgraph", { templateUrl: "views/review/reviewgraph.html", title: 'Explore Intake' })
        .when("/rawupload", { templateUrl: "views/upload/rawupload.html", title: 'Upload' })
        .when("/database", { templateUrl: "views/database/database.html", title: 'Database' })
        .when("/databasemod", { templateUrl: "views/database/databaseEdit.html", title: 'Database Modify' })
        .when("/rda", { templateUrl: "views/database/rda.html", title: 'Recommended Intakes' })
        .when("/admin/users", { templateUrl: "views/admin/users.html", title: 'Users' })
        .when("/admin/user", { templateUrl: "views/admin/user.html", title: 'User' })
        .when("/admin/studies", { templateUrl: "views/admin/studies.html", title: 'Studies' })
        .when("/admin/study", { templateUrl: "views/admin/study.html", title: 'Study' })
        .when("/admin/progress", { templateUrl: "views/admin/progress.html", title: 'Progress' })
        .when("/admin/test", { templateUrl: "views/admin/test.html", title: 'Test' })
        // .when("/recall",   { templateUrl : "views/misc/recallparse.html", title: '24hr Recall' })
        .when("/advice", { templateUrl: "views/advice/advice.html", title: 'Advice' })
        .when("/appusage", { templateUrl: "views/review/appusage.html", title: 'App Usage' })
        .when("/household", { templateUrl: "views/household/household.html", title: 'Household Data' })
        .when("/breastfeeding", { templateUrl: "views/household/breastfeeding.html", title: 'Breastfeeding Occasions' })
        .when("/loginreset", { templateUrl: "views/resetPassword.html", title: 'Reset Password' })
        .when("/login", { templateUrl: "views/login.html", title: 'Login' })
        .otherwise({
            redirectTo: '/'
        });

    RestangularProvider.setBaseUrl(configSettings.baseUrl);
    RestangularProvider.setFullResponse(true);
});

app.run(function run($rootScope, $window, Restangular, $cookies) {
    //
    $rootScope.logActivity = function () {
        alert("Root Activity");
    };

    //document.body.addEventListener('click', function() {alert('Hello WOrk');}, true);
    $rootScope.$on('$viewContentLoaded', function () {
        var doc = document.getElementsByClassName("content");
        if (doc.length > 0) {
            doc = doc[0];
            doc.addEventListener('click', function () {
                // $rootScope.timeOut = new Date(new Date().getTime() + (5 * 60000));
                $cookies.put('time-out', new Date(new Date().getTime() + (40 * 60000)));
            }, true);
        }
        var counter = setInterval(function () {
            var now = new Date().getTime();
            var timeoutCookie = $cookies.get('time-out');
            // var distance = $rootScope.timeOut.getTime() - now;
            var distance = new Date(timeoutCookie).getTime() - now;
            var minutes = (distance % (1000 * 60 * 60)) / (1000 * 60);

            if (minutes < 0) {
                $cookies.put('auth_token', null);
                $cookies.put('auth_expire', null);
                $window.location.href = "/#!/logout";
            } else if (minutes < 5) {
                minutes = "0" + Math.floor(minutes);
                var seconds = Math.floor((distance % (1000 * 60)) / 1000);
                if (seconds < 10)
                    seconds = "0" + seconds;
                if (minutes === "01" && seconds === 30)
                    alert("Your session will log out soon if no interaction is detected.");
                document.getElementById("timer-text").innerText = minutes + ":" + seconds;
                document.getElementById("timer-div").style.display = "inline-block";
            }
            else
                document.getElementById("timer-div").style.display = "none";
        }, 1000);
    });

    $rootScope.$on("$routeChangeSuccess", function (event, current, previous) {
        //Change page title, based on Route information
        $rootScope.title = current.$$route.title;
        $rootScope.previousPage = previous;
        // $rootScope.timeOut = new Date(new Date().getTime() + (5 * 60000));
        var idx = current.$$route.originalPath.indexOf('login');
        if (idx > 0)
            $cookies.put('time-out', new Date(new Date().getTime() + (9999 * 60000)));
        else
            $cookies.put('time-out', new Date(new Date().getTime() + (40 * 60000)));
    });

    $rootScope.$on("$locationChangeStart", function (event, next, current) {
        var token = $cookies.get('auth_token');
        Restangular.setDefaultHeaders({ 'Authorization': 'Bearer ' + token });
        var date = new Date($cookies.get('auth_expire'));
        // var isAdmin = $cookies.get('auth_expire');
        var expired = date < new Date();

        if (expired) {
            $cookies.put('auth_token', null);
            $cookies.put('auth_expire', null);
            token = null;
        }

    $rootScope.showHeader = (token && token != "null" && !expired); //set whether to show header
    if (next.indexOf("login") < 1 && next.indexOf("logout") < 1 && (!token || token == "null")) {
      $cookies.put('next-page-login', next);
      var host = $window.location.host;
      var landingUrl = "http://" + host + "/#!/login";
      event.preventDefault();
      $window.location.href = landingUrl;
    }
  });
});


app.factory("UserService", function () {
    return {
        username: null,
        isAdmin: false,
        isCoord: false,
        id: null,
        role: '',
        email: ''
    }
    // username: $cookies.get('auth_username'),
    //   isAdmin: $cookies.get('auth_is_admin')
    //   id: $cookies.get('auth_id')
});

app.factory("ClientConfig", ['Restangular', function (Restangular) {
    var config = null;
    var users = null
    var service = { counter: 0 };

    service.loadConfig = function () {
        if (config) {
            this.counter++;
            return JSON.parse(config);
        }
        return null;
    };

    service.loadUsers = function () {
      return users;
    }

    service.saveUsers = function(data) {
      users = data;
    }

    service.saveConfig = function (cfg) {
        config = JSON.stringify(cfg);
    }

    service.clearConfig = function() {
      config = null;
      users = null;
    }

    return service;
}]);



app.controller('headController', ['$rootScope', '$scope', '$cookies', '$window', 'UserService', 'Restangular', 'ClientConfig', function ($rootScope, $scope, $cookies, $window, UserService, Restangular, ClientConfig) {
    // UserService.isAdmin = $cookies.get('auth_is_admin') === "true";
    // UserService.isCoord = $cookies.get('auth_is_coord') === "true";
    var role = $cookies.get('auth_role');
    UserService.isAdmin = (role === 'admin' || role === 'coordinator')
    UserService.isCoord = role === 'coordinator';
    UserService.role = role;
    UserService.username = $cookies.get('auth_username');
    UserService.id = $cookies.get('auth_id');
    $scope.admin = UserService.isAdmin;
    $scope.isCoord = UserService.isCoord;
    $scope.userName = UserService.username;

  $scope.getUser = function() {
    return UserService;
  }
  $scope.logout = function() {
    var token = $cookies.get('auth_token');
    $cookies.put('auth_token', null);
    $cookies.put('auth_expire', 0);
    $cookies.put('auth_role', null);
    $cookies.put('auth_username', null);
    $cookies.put('auth_id', null);


    $cookies.put('search_config', null);

    $cookies.put('search_page_size', null);
    $cookies.put('search_page_number', null);
    $cookies.put('search_record_type', null);
    $cookies.put('search_recipe', null);
    $cookies.put('search_study', null);
    $cookies.put('search_household', null);
    $cookies.put('search_member', null);
    $cookies.put('search_identified', null);
    $cookies.put('search_portioned', null);
    $cookies.put('search_text', null);
    $cookies.put('search_comment_text', null);
    $cookies.put('search_days', null);
    $cookies.put('search_gv', null);
    $cookies.put('search_hidden', null);

    $cookies.put('cook_page_size', null);
    $cookies.put('cook_page_number', null);
    $cookies.put('cook_study', null);
    $cookies.put('cook_household', null);
    $cookies.put('cook_days', null);

    $cookies.put('home_comments_open', true);
    $cookies.put('home_comments_search', null);
    $cookies.put('home_comments_priority', null);
    $cookies.put('home_comments_seen', null);
    $cookies.put('home_comments_page', null);

    $cookies.put('household_study', null);
    $cookies.put('household_household', null);

    UserService.username = null;
    UserService.isAdmin = false;
    UserService.isCoord = false;
    UserService.id = null;

    ClientConfig.clearConfig();

    $window.location.href = "/#!/logout";
  };
  $scope.resetTimer = function() {
    // $rootScope.timeOut = new Date(new Date().getTime() + (20 * 60000));
    $cookies.put('time-out', new Date(new Date().getTime() + (25 * 60000)));
  };

  $scope.reportIssue = () => {
        let emailMessage = {
            username: $scope.userName,
            role: $scope.role,
            page: $scope.page,
            issue: $scope.issue,
            expect: $scope.expect
        };
        // Submit the email request
        Restangular.oneUrl('ReportIssue').post("", emailMessage).then((response) => {
        });
        // Hide the modal
        $('#reportIssueModal').modal('hide');
    }
}]);

// Define the controller

/*app.controller('fiducialController', ['$scope', 'Restangular', function($scope, Restangular) {
  $scope.loading = true;
  Restangular.all('EatImageRecords').getList().then(function(records) {
    $scope.records = records.data;
    $scope.loading = false;
  }, function errorCallback() {
    $scope.loading = false;
  });
}]);*/

app.controller('loginController', ['$scope', '$cookies', '$http', '$window', 'UserService', 'configSettings', '$routeParams', 'Restangular', function($scope, $cookies, $http, $window, UserService, configSettings, $routeParams, Restangular) {
  var token = $cookies.get('auth_token');
  var date = $cookies.get('auth_expire');
  var expired = (!date || date === 'null' || new Date() >= new Date(date));
  if (token && token != "null" && !expired)
    $window.location.href = "/#!/home";

  $scope.login = function() {
    var data = {"grant_type": "password", "username": $scope.username, "password": $scope.password}
    if ($scope.username == "" || $scope.password == "") {
      $scope.error = 'username and password are required';
      return;
    }
    $scope.loading = true;
    var response = $http({
      method: 'POST',
      url: configSettings.baseUrl.replace("/api", "/token"),
      data: $.param(data),
      headers: {'Content-Type': 'application/x-www-form-urlencoded'}
    }).then(function(response) {
        //First function handles success
        if (response.status == 200) {

          $cookies.put('auth_token', response.data.access_token);
          var date = new Date();
          // var isAdmin = (response.data.role === 'admin' || response.data.role === 'coordinator'),
          //   isCoord = response.data.role
          // var isAdmin = (response.data.admin === "true" || response.data.coordinator === "true"),
          //   isCoord = response.data.coordinator === "true";
          date.setSeconds(date.getSeconds() + response.data.expires_in);
          $cookies.put('auth_expire', date);
          $cookies.put('auth_role', response.data.role);
          // $cookies.put('auth_is_coord', isCoord);
          $cookies.put('auth_username', $scope.username);
          $cookies.put('auth_id', response.data.id);

          UserService.username = response.data.username;
          UserService.email = response.data.email;
          UserService.isAdmin = (response.data.role === 'admin' || response.data.role === 'coordinator');
          UserService.isCoord = (response.data.role === 'coordinator');
          UserService.role = response.data.role;
          UserService.id = response.data.id;

          var next = $cookies.get('next-page-login', null);
          if (next)
            $window.location = next;
          else
            $window.location.href = "/#!/home";

          $cookies.put('next-page-login', null);
        }
        $scope.loading = false;
    }, function(response) {
        //Second function handles error
        $scope.loading = false;
        if (response.status == 400) 
          $scope.error = response.data.error_description;//"incorrect login";
        else
          $scope.error = "We can't log you in at this time, please try again";
    });
  }

  var passwordRegex = new RegExp('^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#\$%\^&\*])(?=.{8,}).*', '');
  $scope.newPasswordUpdated = function() {
    if (!$scope.password.match(passwordRegex)) {
      $("#userPassword").css("border-color", "#B00020");
      $scope.error = "Password not strong enough";
      return;
    }
    $("#userPassword").css("border-color", "rgb(204, 204, 204)");

    if ($scope.confirmPassword && $scope.confirmPassword.length > 0 && $scope.password !== $scope.confirmPassword) {
      $("#userConfirmPassword").css("border-color", "#B00020");
      $scope.error = "Password and confirmation must match";
      return;
    }
    $("#userConfirmPassword").css("border-color", "rgb(204, 204, 204)");

    $scope.error = "";
  }

  $scope.RequestResetPassword = function () {
    if (!$scope.username || $scope.username.length == 0)
        $scope.username = prompt("Please enter username to reset password.");
    if (!$scope.username)
        return;
    $scope.loading = true;
    var url = window.location.href;
    url = url.substring(0, url.indexOf("/#!"));
    Restangular.one('/Account/ResetPassword?username=' + $scope.username + '&hostUrl=' + url).get().then(function (result) {
        alert("Reset password email has been sent. Please check your email address: " + result.data + ". This reset request will expire in 1 hour.");
    }, function (result) {
        if (result.status === 400)
            alert(result.data.message);
        else
            alert("Could not send reset email. Please contact a system administrator.")
    }).finally(function () { $scope.loading = false; });
  }

  $scope.resetPassword = function () {
    var key = $routeParams.key;
    if (!key || key.length == 0) {
      $window.location.href = "/#!/login";
      return;
    }
    //Reset to default colors and error
    $("#userPassword").css("border-color", "rgb(204, 204, 204)");
    $("#userConfirmPassword").css("border-color", "rgb(204, 204, 204)");
    $scope.error = "";

    if (!$scope.password) {
      $("#userPassword").css("border-color", "#B00020");
      $scope.error = "Password is required";
      return;
    }
    if (!$scope.password.match(passwordRegex)) {
      $("#userPassword").css("border-color", "#B00020");
      $scope.error = "Password not strong enough";
      return;
    }
    if (!$scope.confirmPassword) {
      $("#userConfirmPassword").css("border-color", "#B00020");
      $scope.error = "Password confirmation is required";
      return;
    }
    if ($scope.password !== $scope.confirmPassword) {
      $("#userConfirmPassword").css("border-color", "#B00020");
      $scope.error = "Password and confirmation must match";
      return;
    }
    $scope.loading = true;
    var request = { password: $scope.password, key: key };
    //var formencoded = $.param(request);
    Restangular.one('/Account/ResetPasswordByKey').post('', request).then(function (result) {
        alert("Password has been reset.");
        $window.location.href = "/#!/login";
    }, function (result) {
        if (result.status === 400)
            alert("Could not reset password. " + (result.data ? result.data.message : "Please contact a system administrator at VISIDA@newcastle.edu.au."));
        else
            alert("Could not reset password. Please contact a system administrator at VISIDA@newcastle.edu.au.")
    }).finally(function () { $scope.loading = false; });
  }
}]);

function formatDateString(date) {
    // /2020-02-05T13:57:11
    var str = "" + date.getFullYear() + "-" + pdc(date.getMonth() + 1) + "-" + pdc(date.getDate()) + "T" + pdc(date.getHours()) + ":" + pdc(date.getMinutes()) + ":" + pdc(date.getSeconds());
    return str;
};
function pdc(comp) {
    var r = "" + comp;
    if (r.length < 2)
        return "0" + r;
    return r;
};
function buildSearchRegex(search) {
    if (!search)
        return /[\s\S]*/i;
    search = search.replace(/[()!@$?^.*/\\]/gi, '')
    var strs = search.split(' ');
    var searchStr = '^(?=.*' + strs.join(')(?=.*') + ').*$';
    var re = new RegExp(searchStr, 'i');
    return re;
}

String.prototype.regexIndexOf = function (regex, startpos) {
    var indexOf = this.substring(startpos || 0).search(regex);
    return (indexOf >= 0) ? (indexOf + (startpos || 0)) : indexOf;
}