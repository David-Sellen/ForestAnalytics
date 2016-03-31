var app = angular.module('firespark', []);

var map = L.map('mapid').setView([63.3435503, 15.3123929], 4);
L.tileLayer('http://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
	maxZoom: 19,
	attribution: '&copy; <a href="http://www.openstreetmap.org/copyright">OpenStreetMap</a>'
}).addTo(map);

setTimeout(function(){map.invalidateSize();}, 100);

app.controller('AppController', function($scope, $http) {
    $scope.totalStems = 0;
    $scope.worksites = [];
    $http.get('/worksites/listAll').success(function(data) {
        $scope.worksites = data.map(JSON.parse);

        angular.forEach($scope.worksites, function(item){
                $scope.totalStems = $scope.totalStems + item.stems
                
                L.circle([51.508, -0.11], 500, {
                    color: 'red',
                    fillColor: '#f03',
                    fillOpacity: 0.5
                }).addTo(mymap);

                map.addCircles(item.longitude, item.latitude, popup=ct$type, weight = 3, radius=40,
                                 color="#ffa500", stroke = TRUE, fillOpacity = 0.8)

        });
    });

    $scope.selectedData = {'stems' : 0, 'worksites' : 0 };
    $scope.selected = [];
    $scope.select = function(item){
        var index = $scope.selected.indexOf(item.id);
        if(index !== -1) {
          $scope.selected.splice(index, 1);
          $scope.selectedData['stems'] -= item.stems;
          $scope.selectedData['worksites'] -= 1;
        } else {
            $scope.selected.push(item.id);
            $scope.selectedData['stems'] += item.stems;
            $scope.selectedData['worksites'] += 1;
        }
    }
})

// Form for creating a dataset
.controller('formController', function($scope, $http){
    $scope.submitQuery=function(){

        if(!$scope.field.group) return;

        /*alert($scope.field.group);
        if($scope.field.group == "treeLevel"){
            alert("Show tree data");
        } else {
            alert("Show location data");
        }*/

        var data = $scope.field;

        console.log("Data:" + data);

        $http.post('/dataset/create', data).success(function(data) {
            if(data){
                alert("Success!");
            } else {
                alert("Failed!");
            }
        }).error(function(data) {
            alert("Error!");
        });
    };
});



// Worksites list item activation
/*
$('.worksites').on('click', '.list-group-item', function(event) {
  if($(this).hasClass('active')){
    $(this).removeClass('active');
  } else {
    $(this).addClass('active');
  }
});*/

