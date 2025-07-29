var PerspT = 
(function(root) {
  if(root.numeric) {
    return;
  }

  else{
    var numeric = {};

    numeric.dim = function dim(x) {
        var y,z;
        if(typeof x === "object") {
            y = x[0];
            if(typeof y === "object") {
                z = y[0];
                if(typeof z === "object") {
                    return numeric._dim(x);
                }
                return [x.length,y.length];
            }
            return [x.length];
        }
        return [];
    };

    numeric._foreach2 = (function _foreach2(x,s,k,f) {
        if(k === s.length-1) { return f(x); }
        var i,n=s[k], ret = Array(n);
        for(i=n-1;i>=0;i--) { ret[i] = _foreach2(x[i],s,k+1,f); }
        return ret;
    });

    numeric.cloneV = function (x) {
      var _n = x.length;
      var i, ret = Array(_n);

      for(i=_n-1;i!==-1;--i) {
        ret[i] = (x[i]);
      }
      return ret;
    };

    numeric.clone = function (x) {
      if(typeof x !== "object") return (x);
      var V = numeric.cloneV;
      var s = numeric.dim(x);
      return numeric._foreach2(x,s,0,V);
    };

    numeric.diag = function diag(d) {
        var i,i1,j,n = d.length, A = Array(n), Ai;
        for(i=n-1;i>=0;i--) {
            Ai = Array(n);
            i1 = i+2;
            for(j=n-1;j>=i1;j-=2) {
                Ai[j] = 0;
                Ai[j-1] = 0;
            }
            if(j>i) { Ai[j] = 0; }
            Ai[i] = d[i];
            for(j=i-1;j>=1;j-=2) {
                Ai[j] = 0;
                Ai[j-1] = 0;
            }
            if(j===0) { Ai[0] = 0; }
            A[i] = Ai;
        }
        return A;
    };

    numeric.rep = function rep(s,v,k) {
        if(typeof k === "undefined") { k=0; }
        var n = s[k], ret = Array(n), i;
        if(k === s.length-1) {
            for(i=n-2;i>=0;i-=2) { ret[i+1] = v; ret[i] = v; }
            if(i===-1) { ret[0] = v; }
            return ret;
        }
        for(i=n-1;i>=0;i--) { ret[i] = numeric.rep(s,v,k+1); }
        return ret;
    };

    numeric.identity = function(n) { return numeric.diag(numeric.rep([n],1)); };

    numeric.inv = function inv(a) {
        var s = numeric.dim(a), abs = Math.abs, m = s[0], n = s[1];
        var A = numeric.clone(a), Ai, Aj;
        var I = numeric.identity(m), Ii, Ij;
        var i,j,k,x;
        for(j=0;j<n;++j) {
            var i0 = -1;
            var v0 = -1;
            for(i=j;i!==m;++i) { k = abs(A[i][j]); if(k>v0) { i0 = i; v0 = k; } }
            Aj = A[i0]; A[i0] = A[j]; A[j] = Aj;
            Ij = I[i0]; I[i0] = I[j]; I[j] = Ij;
            x = Aj[j];
            for(k=j;k!==n;++k)    Aj[k] /= x; 
            for(k=n-1;k!==-1;--k) Ij[k] /= x;
            for(i=m-1;i!==-1;--i) {
                if(i!==j) {
                    Ai = A[i];
                    Ii = I[i];
                    x = Ai[j];
                    for(k=j+1;k!==n;++k)  Ai[k] -= Aj[k]*x;
                    for(k=n-1;k>0;--k) { Ii[k] -= Ij[k]*x; --k; Ii[k] -= Ij[k]*x; }
                    if(k===0) Ii[0] -= Ij[0]*x;
                }
            }
        }
        return I;
    };

    numeric.dotMMsmall = function dotMMsmall(x,y) {
        var i,j,k,p,q,r,ret,foo,bar,woo,i0;
        p = x.length; q = y.length; r = y[0].length;
        ret = Array(p);
        for(i=p-1;i>=0;i--) {
            foo = Array(r);
            bar = x[i];
            for(k=r-1;k>=0;k--) {
                woo = bar[q-1]*y[q-1][k];
                for(j=q-2;j>=1;j-=2) {
                    i0 = j-1;
                    woo += bar[j]*y[j][k] + bar[i0]*y[i0][k];
                }
                if(j===0) { woo += bar[0]*y[0][k]; }
                foo[k] = woo;
            }
            ret[i] = foo;
        }
        return ret;
    };

    numeric.dotMV = function dotMV(x,y) {
        var p = x.length, i;
        var ret = Array(p), dotVV = numeric.dotVV;
        for(i=p-1;i>=0;i--) { ret[i] = dotVV(x[i],y); }
        return ret;
    };

    numeric.dotVV = function dotVV(x,y) {
        var i,n=x.length,i1,ret = x[n-1]*y[n-1];
        for(i=n-2;i>=1;i-=2) {
            i1 = i-1;
            ret += x[i]*y[i] + x[i1]*y[i1];
        }
        if(i===0) { ret += x[0]*y[0]; }
        return ret;
    };

    numeric.transpose = function transpose(x) {
        var i,j,m = x.length,n = x[0].length, ret=Array(n),A0,A1,Bj;
        for(j=0;j<n;j++) ret[j] = Array(m);
        for(i=m-1;i>=1;i-=2) {
            A1 = x[i];
            A0 = x[i-1];
            for(j=n-1;j>=1;--j) {
                Bj = ret[j]; Bj[i] = A1[j]; Bj[i-1] = A0[j];
                --j;
                Bj = ret[j]; Bj[i] = A1[j]; Bj[i-1] = A0[j];
            }
            if(j===0) {
                Bj = ret[0]; Bj[i] = A1[0]; Bj[i-1] = A0[0];
            }
        }
        if(i===0) {
            A0 = x[0];
            for(j=n-1;j>=1;--j) {
                ret[j][0] = A0[j];
                --j;
                ret[j][0] = A0[j];
            }
            if(j===0) { ret[0][0] = A0[0]; }
        }
        return ret;
    };

        this.numeric = numeric;
    root.numeric = numeric;
  }

}(this));


(function(global, factory) {
  if(typeof exports === 'object' && typeof module !== undefined){
    module.exports = factory();
  }
  else if(typeof define === 'function' && define.amd){
    define(factory);
  }
  else{
    global.PerspT = factory();
  }
}(this, function() {
  'use strict';

    function round(num){
        return Math.round(num*10000000000)/10000000000;
    }

  function getNormalizationCoefficients(srcPts, dstPts, isInverse){
    if(isInverse){
      var tmp = dstPts;
      dstPts = srcPts;
      srcPts = tmp;
    }
    var r1 = [srcPts[0], srcPts[1], 1, 0, 0, 0, -1*dstPts[0]*srcPts[0], -1*dstPts[0]*srcPts[1]];
    var r2 = [0, 0, 0, srcPts[0], srcPts[1], 1, -1*dstPts[1]*srcPts[0], -1*dstPts[1]*srcPts[1]];
    var r3 = [srcPts[2], srcPts[3], 1, 0, 0, 0, -1*dstPts[2]*srcPts[2], -1*dstPts[2]*srcPts[3]];
    var r4 = [0, 0, 0, srcPts[2], srcPts[3], 1, -1*dstPts[3]*srcPts[2], -1*dstPts[3]*srcPts[3]];
    var r5 = [srcPts[4], srcPts[5], 1, 0, 0, 0, -1*dstPts[4]*srcPts[4], -1*dstPts[4]*srcPts[5]];
    var r6 = [0, 0, 0, srcPts[4], srcPts[5], 1, -1*dstPts[5]*srcPts[4], -1*dstPts[5]*srcPts[5]];
    var r7 = [srcPts[6], srcPts[7], 1, 0, 0, 0, -1*dstPts[6]*srcPts[6], -1*dstPts[6]*srcPts[7]];
    var r8 = [0, 0, 0, srcPts[6], srcPts[7], 1, -1*dstPts[7]*srcPts[6], -1*dstPts[7]*srcPts[7]];

    var matA = [r1, r2, r3, r4, r5, r6, r7, r8];
    var matB = dstPts;
        var matC;
  
    try{
        matC = numeric.inv(numeric.dotMMsmall(numeric.transpose(matA), matA));
    }catch(e){
        console.log(e);
        return [1,0,0,0,1,0,0,0];
    }

    var matD = numeric.dotMMsmall(matC, numeric.transpose(matA));
    var matX = numeric.dotMV(matD, matB);
        for(var i = 0; i < matX.length; i++) {
            matX[i] = round(matX[i]);
        }
        matX[8] = 1;

    return matX;
  }

  function PerspT(srcPts, dstPts){
    if( (typeof window !== 'undefined' && window === this) || this === undefined) {
      return new PerspT(srcPts, dstPts);
    }

    this.srcPts = srcPts;
    this.dstPts = dstPts;
    this.coeffs = getNormalizationCoefficients(this.srcPts, this.dstPts, false);
    this.coeffsInv = getNormalizationCoefficients(this.srcPts, this.dstPts, true);

    return this;
  }

  PerspT.prototype = {
    transform: function(x,y) {
      var coordinates = [];
      coordinates[0] = (this.coeffs[0]*x + this.coeffs[1]*y + this.coeffs[2]) / (this.coeffs[6]*x + this.coeffs[7]*y + 1);
      coordinates[1] = (this.coeffs[3]*x + this.coeffs[4]*y + this.coeffs[5]) / (this.coeffs[6]*x + this.coeffs[7]*y + 1);
      return coordinates;
    },

    transformInverse: function(x,y) {
      var coordinates = [];
      coordinates[0] = (this.coeffsInv[0]*x + this.coeffsInv[1]*y + this.coeffsInv[2]) / (this.coeffsInv[6]*x + this.coeffsInv[7]*y + 1);
      coordinates[1] = (this.coeffsInv[3]*x + this.coeffsInv[4]*y + this.coeffsInv[5]) / (this.coeffsInv[6]*x + this.coeffsInv[7]*y + 1);
      return coordinates;
    }
  };

  return PerspT;

}));


angular.module('visida_cms').controller('editImageController', ['$scope', '$rootScope', 'Restangular', '$routeParams', '$window', 'UserService', function($scope, $rootScope, Restangular, $routeParams, $window, UserService) {
  $scope.searchHousehold = $routeParams.household;
  $scope.searchRecipe = $routeParams.recipe;
  $scope.searchStudy = $routeParams.study;
  $scope.searchIdent = $routeParams.identified;
  $scope.searchPrt = $routeParams.portioned;

  $scope.dirtyCorners = false;

  var FIDUCIAL_WIDTH = 85.6;
  var FIDUCIAL_HEIGHT = 51.1;

  var url = 'ImageRecords/' + $routeParams.id + '?';
  if ($scope.searchHousehold) url += ('&Household=' + $scope.searchHousehold);
  if ($scope.searchRecipe) url += ('&Recipe=' + $scope.searchRecipe);
  if ($scope.searchStudy) url += ('&Study=' + $scope.searchStudy);
  if ($scope.searchIdent) url += ('&identified=' + $scope.searchIdent); 
  if ($scope.searchPrt) url += ('&portioned=' + $scope.searchPrt);
  Restangular.one(url).get().then(function(record) {
    $scope.record = record.data;

    if (!$scope.record.homography) {
      $scope.record.homography = {
        points: [{x:0, y:0},{x:0, y:0},{x:0, y:0},{x:0, y:0}]
      };
    }

    var svg = $("#svg")[0];
    svg.onmousedown = $scope.startRuler;
    svg.onmousemove = $scope.moveRuler;
    svg.onmouseup = $scope.endRuler;

    return;

  }).finally(function () { $scope.loaded++; });

  $scope.getHG = function(point) {
    var img = $("#image")[0];
    return {x:Math.round(point.x * img.width),y:Math.round(point.y*img.height)};
  };

  $scope.getHGL = function($index) {
    var img = $("#image")[0];
    var point = $scope.record.homography.points[$index];

    if ($scope.editRotation > 0) {
      var x = point.x * img.width - img.width / 2;
      var y = point.y*img.height - img.height / 2;
      var rad = $scope.editRotation * Math.PI / 180
      return {x:Math.round((x * Math.cos(rad)) - (y * Math.sin(rad)) + img.width / 2),y:Math.round((y * Math.cos(rad)) + (x * Math.sin(rad)) + img.height / 2)};
    }
    return {x:Math.round(point.x * img.width),y:Math.round(point.y*img.height)};
  };

  $scope.pointToImageSpace = function(point){
    var img = $("#image")[0];
    return {x: point.x * img.width, y: point.y * img.height};
  };

  $scope.euclideanDistance = function(p1, p2){
    xs = (p1.x - p2.x) * (p1.x - p2.x);
    ys = (p1.y - p2.y) * (p1.y - p2.y);

    return Math.sqrt(xs + ys);
  };

  $scope.calcSVG = function() {
    var img = $("#image")[0];
    var svg = $("#svg")[0];

    svg.setAttribute('width', img.width);
    svg.setAttribute('height', img.height);

    var prev = {x: $scope.record.homography.points[3].x * img.width, y: $scope.record.homography.points[3].y * img.height };
    for (var i = 0; i < $scope.record.homography.points.length; i++) {
        var point = $scope.record.homography.points[i];
        var x = Math.round(point.x * img.width);
        var y = Math.round(point.y * img.height);

        var line = $("#line-" + i)[0];
        line.setAttribute('x1', prev.x);
        line.setAttribute('y1', prev.y);
        line.setAttribute('x2', x);
        line.setAttribute('y2', y);
        
        var dot = $("#circle-" + i)[0];
        dot.setAttribute('cx', x);
        dot.setAttribute('cy', y);

        prev.x = x;
        prev.y = y;
    }
  };

  $scope.drawCorner = function(element, x, y, index) {
    element.setAttribute('cx', x);
    element.setAttribute('cy', y);

    //var prev = $("#line-" + (index-1 > 0 ? index-1 : 3))[0];
    var prev = $("#line-" + (index+1 > 3 ? 0 : index + 1))[0];
    prev.setAttribute('x1', x);
    prev.setAttribute('y1', y);

    var line = $("#line-" + index)[0];
    line.setAttribute('x2', x);
    line.setAttribute('y2', y);
  }

  $scope.updateHomography = function(index, x, y) {
    var img = $("#image")[0];

    $scope.record.homography.points[index].x = x / img.width;
    $scope.record.homography.points[index].y = y / img.height;
    $scope.recalculateHomography();
  }

  $scope.recalculateHomography = function(){
    //Get the four corners
    br = $scope.pointToImageSpace($scope.record.homography.points[0]);
    bl = $scope.pointToImageSpace($scope.record.homography.points[1]);
    tl = $scope.pointToImageSpace($scope.record.homography.points[2]);
    tr = $scope.pointToImageSpace($scope.record.homography.points[3]);

    //Calculate pixels per mm
    distPixels = $scope.euclideanDistance(bl, br);
    pixPerMm = distPixels / FIDUCIAL_WIDTH;
    mmPerPix = FIDUCIAL_WIDTH / distPixels;
 
    //Create virtual square starting form bottom left going right.
    BL_real = bl;
    BR_real = {x: bl.x + (FIDUCIAL_WIDTH * pixPerMm), y: bl.y};
    TL_real = {x: bl.x, y: bl.y - (FIDUCIAL_HEIGHT * pixPerMm)};
    TR_real = {x: bl.x + (FIDUCIAL_WIDTH * pixPerMm), y: bl.y - (FIDUCIAL_HEIGHT * pixPerMm)};

    //Create homography
    var srcCorners = [br.x, br.y, bl.x, bl.y, tl.x, tl.y, tr.x, tr.y];
    var dstCorners = [BR_real.x, BR_real.y, BL_real.x, BL_real.y, TL_real.x, TL_real.y, TR_real.x, TR_real.y];
    $scope.perspT = PerspT(srcCorners, dstCorners);
    //var acc = $scope.calculateDistanceBetweenPoints(bl, br);
  };

  $scope.calculateDistanceBetweenPoints = function(p1, p2){
    var srcPt1 = [p1.x, p1.y];
    var dstPt1 = $scope.perspT.transform(srcPt1[0], srcPt1[1]);
    p1 = {x: dstPt1[0], y: dstPt1[1]}
    var srcPt2 = [p2.x, p2.y];
    var dstPt2 = $scope.perspT.transform(srcPt2[0], srcPt2[1]);
    p2 = {x: dstPt2[0], y: dstPt2[1]}
    return mmPerPix * $scope.euclideanDistance(p1, p2);
  };

  $scope.startRuler = function(evt) {
    $scope.pointS = {x: evt.layerX, y: evt.layerY};
    var ruler = $("#ruler")[0];
    ruler.style.display = "none";
    ruler.setAttribute('x1', evt.layerX);
    ruler.setAttribute('y1', evt.layerY);

    var text = $("#ruler-text")[0];
    text.textContent = "";

    // var ticks = $("#ruler-ticks")[0];
    // ticks.style.display = "none";
    // ticks.setAttribute('x1', evt.layerX);
    // ticks.setAttribute('y1', evt.layerY);

    if(!$scope.perspT){
      $scope.recalculateHomography();
    }
    // ruler.x1 = evt.layerX;
    // ruler.y1 = evt.layerY;
  };

  $scope.moveRuler = function(evt, t) {
    if (!$scope.pointS)
        return;
    $scope.pointE = {x: evt.layerX, y: evt.layerY};
    var ruler = $("#ruler")[0];
    ruler.style.display = "inline";
    ruler.setAttribute('x2', evt.layerX);
    ruler.setAttribute('y2', evt.layerY);

    // var ticks = $("#ruler-ticks")[0];
    // ticks.style.display = "inline";
    // ticks.setAttribute('x2', evt.layerX);
    // ticks.setAttribute('y2', evt.layerY);

    var dist = $scope.calculateDistanceBetweenPoints($scope.pointS, $scope.pointE);
    var text = $("#ruler-text")[0];
    text.setAttribute('x', evt.layerX+5);
    text.setAttribute('y', evt.layerY);
    //text.setAttribute('textContent', dist.toFixed(2) + "mm");
    text.textContent = dist.toFixed(2) + "mm";

    var rect = $("#ruler-rect")[0];
    rect.setAttribute('x', evt.layerX);
    rect.setAttribute('y', evt.layerY-17);

    $("#ruler-output")[0].value = dist.toFixed(2);
  };

  $scope.endRuler = function(evt) {
    $scope.pointS = $scope.pointE = null;
  };

  $scope.makeDirtyCorners = function() { $scope.dirtyCorners = true; };

  $scope.editRotation = 0;
  $scope.checkFiducial = function(rot) {
    var update = {id: $scope.record.id};
    Restangular.oneUrl('EatImageRecords/CheckFiducial?rotation=' + $scope.editRotation).post('', update)
    .then(function(result) {
      return;
    });
  };

  $scope.saveCorners = function() {
    Restangular.allUrl('ImageRecords/' + $scope.record.id + "/Homography").customPUT($scope.record.homography.points).then(function(result) {
      $scope.dirtyCorners = false;
    }, function(error) {
      alert("Could not save corners. " + error.data.message);
    });
  };
  $scope.resetCorners = function() {
    $scope.record.homography.points[0].x = 0.2738;
    $scope.record.homography.points[0].y = 0.7148;

    $scope.record.homography.points[1].x = 0.6819;
    $scope.record.homography.points[1].y = 0.7135;

    $scope.record.homography.points[2].x = 0.7288;
    $scope.record.homography.points[2].y = 0.9141;

    $scope.record.homography.points[3].x = 0.2432;
    $scope.record.homography.points[3].y = 0.9208;

    $scope.calcSVG();
    $scope.makeDirtyCorners();
  };
}]);

app.directive('imageonload', function() {
    return {
        restrict: 'A',
        link: function(scope, element, attrs) {
            element.bind('load', function() {
                //call the function that was passed
                scope.$apply(attrs.imageonload);
            });
        }
    };
});

app.directive('corner', ['$timeout', function($timeout) {
    return {
        restrict: 'A',
        scope: true,
        link: function(scope, element, attrs) {
            $timeout(function() {
            element = element[0];
            var index = parseInt(element.id.substring(element.id.lastIndexOf('-') + 1));

            element.onmousedown = function(e) {
                var svg = $("#svg")[0];
                e.stopPropagation();
                scope.makeDirtyCorners();

                var text = $("#corner-text")[0];
                var tc = "";
                switch (index) {
                  case 0: tc = "Top Left"; break;
                  case 1: tc = "Top Right"; break;
                  case 2: tc = "Bottom Right"; break;
                  case 3: tc = "Bottom Left"; break;
                  default: break;
                }
                text.textContent = tc;
                text.setAttribute('font-weight', 'bold');
                text.setAttribute('x', e.layerX);
                text.setAttribute('y', e.layerY);

                svg.onmousemove = function(e) {
                    e = e || window.event;
                    e.preventDefault();

                    text.setAttribute('x', e.layerX);
                    text.setAttribute('y', e.layerY);
                    scope.drawCorner(element, e.layerX, e.layerY, index);
                };

                svg.onmouseup = function(e) {
                    svg.onmousemove = scope.moveRuler;
                    svg.onmouseup = scope.endRuler;
                    svg.onmouseleave = null;

                    text.textContent = "";
                    scope.updateHomography(index, e.layerX, e.layerY);
                };

                svg.onmouseleave = function(e) {
                    svg.onmousemove = scope.moveRuler;
                    svg.onmouseup = scope.endRuler;
                    svg.onmouseleave = null;

                    text.textContent = "";
                    var img = $("#image")[0];
                    scope.drawCorner(element, scope.record.homography.points[index].x * img.width, scope.record.homography.points[index].y * img.height, index);
                };
            }
        });
        }
    }
}]);