app.directive("commentDialog", function () {
    return {
        restrict: 'E',
        scope: false,
        templateUrl: 'views/home/commentDialog.html'
    };
});

app.controller('homeController', ['$scope', 'Upload', 'configSettings', 'Restangular', 'UserService', '$cookies', function ($scope, Upload, configSettings, Restangular, UserService, $cookies) {
    $scope.getUrl = configSettings.baseUrl + '/Upload/GetAPK';
    $scope.admin = UserService.isAdmin;
    $scope.isCoord = UserService.isCoord;
    $scope.userName = UserService.username;
    $scope.loading = 2;
    $scope.userDisplayCount = 8;
    $scope.pageSize = 10;

    Restangular.one('/User/' + UserService.id + '/Progress').get().then(function (result) {
        $scope.progress = result.data;
    }, function (result) {
    }).finally(function () { $scope.loading--; });

    function getCookie(name, def) {
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


    Restangular.one('/User/Feed').get().then(function (result) {
        $scope.feedData = result.data;
        var ll = result.headers('lastLogin');
        $scope.lastRefresh = new Date(ll).getTime();

        var today = new Date();
        $scope.tasks = [];
        $scope.updateCommentSearch($scope.commentSearchText, 'a', $scope.activePage);

        var mentionTag = '@' + UserService.username;
        for (var i = 0; i < $scope.comments.length; i++) {
            var comment = $scope.comments[i];

            if (comment.text.includes(mentionTag))
                comment.isMention = true;

            var ctime = new Date(comment.createdTime).getTime()
            if (ctime > $scope.lastRefresh)
                comment.unseen = true;

            if (comment.flag === 'Task' && !comment.taskCompleted && $scope.tasks.length < 5)
                $scope.tasks.push(comment);
        }

        var onlineTime = new Date(today.getTime() - 60000 * 10);
        $scope.onlineCount = 0;
        if ($scope.feedData.users) {
            for (var i = 0; i < $scope.feedData.users.length; i++) {
                if ($scope.feedData.users[i].userName === UserService.username) {
                    //$scope.feedData.users.splice(i, 1);
                    //continue;
                    $scope.feedData.users[i].userName = 'You';
                }
                var time = new Date($scope.feedData.users[i].lastLogin);
                if (time > onlineTime) {
                    $scope.feedData.users[i].online = true;
                    $scope.onlineCount++;
                } else if (time.getDate() == today.getDate()
                    && time.getMonth() == today.getMonth()
                    && time.getFullYear() == today.getFullYear()) {
                    $scope.feedData.users[i].loginTime = time.getHours() + ":" + time.getMinutes();
                } else
                    $scope.feedData.users[i].loginTime = time.getDate() + "/" + time.getMonth() + "/" + time.getFullYear();
            }
        }

        $scope.messageCount = 0;
        if ($scope.feedData.messages) {
            for (var i = 0; i < $scope.feedData.messages.length; i++) {
                var message = $scope.feedData.messages[i];
                message.time = new Date(message.createdTime);
                if (message.time > onlineTime) {
                    message.seen = true;
                    $scope.messageCount++;
                }
                switch (message.type) {
                    case 0:
                        message.href = "#!/admin/test?id=" + message.refId;
                        break;
                    case 1:
                        message.href = "#!/image?id=" + message.refId;
                        break;
                    default:
                        message.href = "";
                }
            }
        }

    }, function (result) {

    }).finally(function () { $scope.loading--; });

    // function handleUnloadEvent(event) {
    //      event.returnValue = "Your warning text";
    //   };

    //   $scope.addUnloadEvent = function(){
    //   if (window.addEventListener) {
    //          window.addEventListener("beforeunload", handleUnloadEvent);
    //      } else {
    //          //For IE browsers
    //          window.attachEvent("onbeforeunload", handleUnloadEvent);
    //      }
    //   };
    //   $scope.addUnloadEvent();

    $scope.activePage = 0;
    if (getCookie("home_comments_open", false)) {
        $scope.showCommentDialog = true;
        $scope.commentSearchText = getCookie('home_comments_search', '');
        $scope.onlyShowPriority = getCookie('home_comments_priority', false);
        $scope.onlyShowUnseen = getCookie('home_comments_seen', false);
        $scope.activePage = getCookie('home_comments_page', 0);
    }

    //If the comment dialog is open when the page changes, save the settings for next time
    $scope.$on('$locationChangeStart', function (event, next, current) {
        if ($scope.showCommentDialog) {
            $cookies.put('home_comments_open', true);
            $cookies.put('home_comments_search', $scope.commentSearchText);
            $cookies.put('home_comments_priority', $scope.onlyShowPriority);
            $cookies.put('home_comments_seen', $scope.onlyShowUnseen);
            $cookies.put('home_comments_page', $scope.activePage);

        } else {
            $cookies.put('home_comments_open', false);
        }
    });

    $scope.uploadAPK = function (apkFile) {
        var file = Upload.rename(apkFile[0], apkFile[0].name);
        var url = configSettings.baseUrl + '/Upload/PostAPK';
        Upload.upload({
            url: url,
            data: { file: file }
        }).then(function (result) {
            alert("Yea that worked");
        }, function (result) {
            alert("Yea nah that wasn't so good ask connor");
        });
    };

    $scope.toggleAdmin = function () {
        $scope.admin = !$scope.admin;
    };

    $scope.updateCommentSearch = function (search, filter, savedPage) {
        var comments = [];
        if (!search) search = "";
        var regex = buildSearchRegex(search);

        switch (filter) {
            case 'a':
                comments = $scope.feedData.comments;
                break;
            case 'hp':
                comments = $scope.feedData.comments.filter(function(x) { return x.highPriority; })
                break;
            case 'm':
                comments = $scope.feedData.comments.filter(function(x) { return x.isMention; })
                break;
            case 'r':
                comments = $scope.feedData.comments.filter(function(x) { return x.flag === 'Reply'; })
                break;
            case 't':
                comments = $scope.feedData.comments.filter(function(x) { return x.flag === 'Task'; })
                break;
            default:
                comments = [];
                break;
        }



        // for (var i = 0; i < $scope.feedData.comments.length; i++) {
        //     var com = $scope.feedData.comments[i];

            

        //     if (com.text.match(regex))
        //         comments.push(com);
        //     else if (com.authorName && com.authorName.match(regex))
        //         comments.push(com);
        //     else
        //         continue;
        // }
        $scope.comments = comments;
        $scope.totalPages = Math.ceil($scope.comments.length / $scope.pageSize);
        $scope.setPageNumber();
        $scope.changePage(savedPage ? savedPage : 0);
        // $scope.displayComments = comments;
    };

    $scope.styleComment = function(comment) {
        if (comment.flag === 'Task' && comment.taskCompleted != null)
            return 'strikethrough';
        else
            return '';
    };

    $scope.changePage = function (page) {
        if ($scope.totalPages == 0)
            $scope.displayComments = [];
        if (page < 0 || page >= $scope.totalPages)
            return;
        $scope.activePage = page;

        var skip = page * $scope.pageSize;
        var end = skip + $scope.pageSize;
        var displayComments = [];
        for (var i = skip; i < $scope.comments.length && i < end; i++) {
            displayComments.push($scope.comments[i]);
        }
        $scope.displayComments = displayComments;
        $scope.setPageNumber();
    };

    $scope.pageNumArr = null;
    var maxPageCount = 11;
    $scope.setPageNumber = function () {
        if ($scope.feedData && $scope.feedData.comments) {
            if ($scope.totalPages > maxPageCount) {
                var split = 5;
                var start = $scope.activePage - split;
                var end = $scope.activePage + split;//Math.min($scope.pageData.totalPages, $scope.activePage + 12);
                if (start < 1) {
                    var trim = Math.abs(split - $scope.activePage);
                    start = 1;
                    end = Math.min($scope.totalPages, end + trim + 1);
                }
                if (end > $scope.totalPages) {
                    var trim = end - $scope.totalPages;
                    end = $scope.totalPages;
                    start = Math.max(1, start - trim);
                }
                var arr = [];
                for (var i = start; i <= end; i++) {
                    arr.push(i);
                }
                $scope.pageNumArr = arr;
                return arr;
            }
            else {
                var arr = [];
                for (var i = 1; i <= $scope.totalPages; i++) {
                    arr.push(i);
                }
                $scope.pageNumArr = arr;
                return arr;
            }
        }
        else
            return [1];
    };

    $scope.showMoreUsers = function () {
        $scope.userDisplayCount += 5;
    }

    $scope.mailSupport = function () {
        console.log('runs')
        let mailString = 'mailto:support@visida.newcastle.edu.au?subject=Report%20an%20Issue&body=';
        mailString += 'Username:%20' + $scope.userName + '%0D%0A';
        mailString += 'Role:%20' + ($scope.admin ? 'admin' : $scope.isCoord ? 'Study Coordinator' : 'Analyst') + '%0D%0A';
        mailString += 'What%20page%20were%20you%20on%20when%20you%20experienced%20the%20issue%3F' + '%0D%0A';
        mailString += document.getElementById('page').value + '%0D%0A';
        mailString += 'Describe%20the%20issue' + '%0D%0A';
        mailString += document.getElementById('issue').value + '%0D%0A';
        mailString += 'Potential%20solution' + '%0D%0A';
        mailString += document.getElementById('expect').value + '%0D%0A';
        window.location.href = mailString;
    }
}]);

// function buildSearchRegex(search) {
//   if (!search)
//     return /[\s\S]*/i;
//   search = search.replace(/[()!@$?^.*]/gi, '')
//   var strs = search.split(' ');
//   var searchStr = '^(?=.*' + strs.join(')(?=.*') + ').*$';
//   var re = new RegExp(searchStr, 'i');

//   return re;
// };

