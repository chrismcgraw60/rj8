/**
 * Interacts with Server Folder services.
 */
var folderService = angular.module('folderServices', []);

folderService.factory('folderManagerService', ['$location',
                                                
	function($location){
		return {
			
			folderFeed : function (params) {
				
				if (!params) {
					throw "params must not be null.";
				}

				/*
				 * 1. Create a websocket request on the well-known folders URL. 
				 */
				var ws = new WebSocket("ws://" + $location.host() + ":" + $location.port() + "/folders");
				
				/*
				 * 2. When the websocket opens, initiate the workflow.
				 */
				ws.onopen = function() {
					
					/*
					 * 3. Wire up callbacks to handle the 2 different types of messages that 
					 * the server will send down the web socket.
					 */
					ws.onmessage = function(message) {
						console.debug(message.data);
						var data = angular.fromJson(message.data);
						params.onFolderEventData(data);
					};
					
					/*
					 * 6. All callbacks are wired up. Initiate the socket request.
					 */
					ws.send("");
				};//ws.onopen
			}//query:
		};
	}]);