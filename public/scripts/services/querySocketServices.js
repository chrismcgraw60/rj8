/**
 * This service still has to really take shape but the idea is that it provides the following:
 * - Hides the low level websocket API and workflow.
 * - Encapsulates the query protocol that we have in place where we have a 'column' metadata message 
 *   followed by n 'row' data messages. 
 * 
 * It is absolutely not production ready as it sends SQL over the wire and the server does nothing
 * to validate it (ie block update statements). Not sure if we should provide named URLs for the various
 * custom queries such as those that populate the dashboard summary graph.
 * 
 * Its probably OK (and fast to develop) if we send SQL from the client, so long as the server protects 
 * itself adequately.
 * 
 * The response data has the following form:
 * 
 * metadata response: Array of object literals, each object containing a name and type property. These correspond
 * to the name and type of a column / computed field (AS) in an SQL ResultSet.
 * 
 * rowData response : Each SQL ResultSet row is transmitted one at a time over the web socket by the server
 * and handed to the callback. The row is represented as a JSON array of values. The position of each value
 * in the array corresponds exactly to the array position of the metadata type. So if the metadata has a the 
 * following object at metadata[5] - {name: ERRORS, type BIGINT; }, then the value for every row data array at
 * position [5] will be the number of Errors. This keeps redundant information out of the response.
 * 
 */
var querySocket = angular.module('querySocketServices', []);

querySocket.factory('adhocQuerySocketService', ['$location',
                                                
	function($location){
		return {
			/**
			 * Runs a given query against the server's adhoc query service. The subsequent server interaction
			 * is relayed to the caller via 4 callback function parameters:
			 * 
			 * - sql : The SQL statement to be run.
			 * - socketOpenedHandler: called after the server web socket has been opened.
			 * - socketClosedHandler: called after the server web socket has been closed.
			 * - metadataHandler: called when the response metadata has been sent by the server. See metadata response above.
			 * - rowDataHandler: called when the each result data row is sent by the server. See rowData response above.
			 *  
			 *  returns: Nothng.
			 */
			query : function (sql, socketOpenedHandler, socketClosedHandler, metadataHandler, rowDataHandler) {
				
				/*
				 * 1. Create a websocket request on the well-known query URL. 
				 */
				var ws = new WebSocket("ws://" + $location.host() + ":" + $location.port() + "/query");
				
				/*
				 * 2. When the websocket opens, initiate the workflow.
				 */
				ws.onopen = function() {
					
					/*
					 * 3. Invoke callback to notify that web socket is open.
					 */
					if (socketOpenedHandler) {
						socketOpenedHandler();
					}
				
					/*
					 * 4. Wire up callback to be invoked when this web socket is closed.
					 *    For this particular service, the server will close the web socket
					 *    once it has sent all of its row data for the submitted query.
					 */
					ws.onclose = function() {
						if (socketClosedHandler) {
							socketClosedHandler();
						}
					}
					
					/*
					 * 5. Wire up callbacks to handle the 2 different types of messages that 
					 * the server would send down the web socket.
					 */
					ws.onmessage = function(message) {
						var data = angular.fromJson(message.data);
						
						if (data.metadata && metadataHandler) {
							/*
							 * Handle metadata message.
							 */
							metadataHandler(data.metadata);
						}
						else if (data.row && rowDataHandler) {
							/*
							 * Handle row data message.
							 */
							rowDataHandler(data.row)
						}
						else {
							/*
							 * Unknown message, throw an exception.
							 */
							throw "Unexpected Server Data from Query Result :" + data;
						}
					};
					
					/*
					 * 6. All callbacks are wired up. Send the SQL message to the server.
					 */
					ws.send(sql);
				};
			}
		};
	}]);