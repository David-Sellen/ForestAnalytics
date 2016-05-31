// Leaflet interactive map
var map = L.map('mapid').setView([63.3435503, 15.3123929], 4);
L.tileLayer('http://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
	maxZoom: 19,
	attribution: '&copy; <a href="http://www.openstreetmap.org/copyright">OpenStreetMap</a>'
}).addTo(map);

map.zoomControl.setPosition('topright');

setTimeout(function(){map.invalidateSize();}, 100);

// Angular appliaction
var app = angular.module('firespark', ['ngStorage', 'ngSanitize']);

app.controller('AppController', function($scope, $http) {
    $scope.totalStems = 0;
    $scope.worksites = [];
    $scope.selectedData = {'stems' : 0, 'worksites' : 0 };
    $scope.selected = [];
    $scope.allSelected = false;

    function selectWorksite(item){
        var index = $scope.selected.indexOf(item.id);
        if(index !== -1) {
            item.active = false;
            $scope.selected.splice(index, 1);
            $scope.selectedData['stems'] -= item.stems;
            $scope.selectedData['worksites'] -= 1;
            item.marker.setStyle({
                fillColor: 'GREEN'
            });
        } else {
            item.active = true;
            $scope.selected.push(item.id);
            $scope.selectedData.stems += item.stems;
            $scope.selectedData.worksites += 1;
            item.marker.setStyle({
                fillColor: 'RED'
            });
        }
    }

    $scope.selectAll = function selectAll(){
        $scope.allSelected ? $scope.allSelected = false : $scope.allSelected = true;
        for(i=0; i<$scope.worksites.length; i++){
            if($scope.allSelected){
                if(!$scope.worksites[i].active ){
                    selectWorksite($scope.worksites[i]);
                }
            } else {
                if($scope.worksites[i].active ){
                    selectWorksite($scope.worksites[i]);
                }
            }
        }
    }

    $http.get('/worksites/listAll').success(function(data) {
        $scope.worksites = data.map(JSON.parse);

        angular.forEach($scope.worksites, function(item){
            if(!item.id || item.id.length === 0 || !item.id.trim()){
                item.id = "No name"
            }
            item.active = false;
            $scope.totalStems = $scope.totalStems + item.stems;
            item.marker = L.circleMarker([item.latitude, item.longitude], {
                fill: true,
                fillColor: 'GREEN',
                fillOpacity: 0.6,
                weight: 0,
                clickable: true
            });
            item.marker.setRadius(8);

            item.marker.on('mouseover', function(e){
                //Create a popup and open it.
                e.target.bindPopup("Worksite: " + item.id + " </br> Trees: " + item.stems,
                {closeButton: false, offset: L.point(0, -10)}).openPopup();
            });

            item.marker.on("mouseout", function (e) {
                // Destroying the popup
                 e.target.closePopup();
            });
            item.marker.on('click', function onClick(e) {
                selectWorksite(item);
                $scope.$apply();
            });
            item.marker.addTo(map);
        });
    });

    $scope.select = function(item){selectWorksite(item)};
})

// Form for creating a dataset
.controller('formController', function($scope, $http, $sessionStorage){
    $scope.field = {};
    $scope.field.features = [];

    // Add features to extract from raw data
    $scope.addFeature = function addFeature(feature){
        var index = $scope.field.features.indexOf(feature);
        if(index == -1){
            $scope.field.features.push(feature);
        } else {
            $scope.field.features.splice(index, 1);
        }
    };

    // Send query from form data to create a data set
    $scope.submitQuery=function submitQuery(){
        if(!$scope.field.group) return;
        var dataQuery = $scope.field;
        dataQuery.name.concat("_", $scope.selectedData.stems);
        dataQuery.worksites = $scope.selected;

        $http.post('/dataset/create', dataQuery).success(function(data) {
            if(data){
            //TODO set this to loadedDataset and make loadedDataset a global parameter! LocalStorage!
                var dataset = {};
                dataset.name = data.name;
                dataset.instances = data.instances;
                dataset.attributes = data.attributes;
                $sessionStorage.dataset = dataset;

                console.log("Successfully created the dataset!");
            } else {
                console.log("Failed to created the dataset!");
            }
        }).error(function(data) {
            console.log("Error when creating the dataset!");
        });
    };
});

// Form for creating a dataset
app.controller('datasetCtrl', function($scope, $http, $sessionStorage, $sce){
    $scope.loadedDataset = {};
    $scope.loadedDataset = $sessionStorage.dataset;
    $scope.datasets = {};
    $scope.selectedDataset = "";
    $scope.mlResults = [];

    // Get a list of all stored datasetes
    $http.get('/dataset/list').success(function(data) {
            $scope.datasets = data;
        });

    //Select current dataset
    $scope.select=function(item){
        if($scope.selectedDataset == item){
            $scope.selectedDataset = '';
        } else {
            $scope.selectedDataset = item;
        }
    }

    //Load a dataset into backend/computer cluster RAM.
    $scope.loadDataset=function(name){
        if(name != undefined){
            $http.post('/dataset/load/' + name, {})
            .success(function(data) {
                var dataset = {};
                dataset.name = data.name;
                dataset.instances = data.instances;
                dataset.attributes = data.attributes;
                $sessionStorage.dataset = dataset;
                $scope.loadedDataset = dataset;
            })
            .error(function(data) {
                console.log("Error when loading the dataset!");
            });
        }
    }

	  //Send ML parameters to run on the loadedDataset
    $scope.submitQuery=function(){
        console.log("Sending ML query!");
        if(!$scope.field.algorithm) return;
            var query = {};
            query.algorithm = $scope.field.algorithm;
            query.splitRatio = $scope.field.splitRatio/100;
            query.seed = $scope.field.seed;
            query.label = $scope.field.label;
            query.parameters = {};
            query.parameters.useTrain = $scope.field.useTrain;
            query.parameters.k = $scope.field.k;
            query.parameters.iterations = $scope.field.iterations;
            query.parameters.centroidSeed = $scope.field.centroidSeed;

            $http.post('/ml/run', query)
            .success(function(data) {
                if(data){
                    $scope.mlResults = data;
                    console.log("Successfull ML!");
                } else {
                    console.log("Failed ML!");
                }
            }).error(function(data) {
                console.log("Error on ML request!");
        });
	}

	$scope.charts = [];

	$scope.printChart = function printChart(cluster){
	   // alert(cluster.id);
	    var clusterData = [];

	    // Fill data
	    for(var key in cluster.instancesWithLabel) {
	        clusterData.push([ key, cluster.instancesWithLabel[key] ]);
	        console.log(key + ", " + cluster.instancesWithLabel[key]);
	    }

	   clusterData.sort(function(a, b) {
            return a[0] > b[0] ? 1 : -1;
        });

        $scope.charts.push(c3.generate({
                data: {
                    columns: clusterData,
                    type : 'pie',
                },
                bindto: '#chart'+cluster.id,
                tooltip:{
                    format:{
                        value:function(x){
                            return x;
                        }
                    }
                }
            })
        );
	}
});

