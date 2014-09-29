(function(){
	
	/* Global Variables. */	
	var mediaObject;
	var recordPlayTime;
	var directoryName = "MyELT";
	var defaultAudioFileName;
	var filePath;
	var counter = 0;
	var timeDur = 0;
	var recordDur = 0;
	
	/******************************************* Helper Functions Starts *********************************************/
	var isIOSDevice = function() {
        if( /iPhone|iPad/i.test(navigator.userAgent)) {
            return true;
        } else {
            return false;
        }
    };
	
	//function call if file system fails
	var fsFail = function(error) {
		var fsError = {"fsFailErrorMessage":"Failure in reading File System",
				  			"fsFailErrorCode":error.code};
		window.frames[0].postMessage(fsError,url);
	};
	//function call if directory fails 
	var dirFail = function(error) {
		var dirError = {"dirFailErrorMessage":"Failure in creating directory",
				  		"dirFailErrorCode":error.code};
		window.frames[0].postMessage(dirError,url);
	};
	//function call if directory successfully created
	var getDirSuccess = function() {
		var dirSuccess = {"dirSuccessMessage":"Directory created Successfully"};
		window.frames[0].postMessage(dirSuccess,url);
	};
	
	var createDirectory = function(){
		//function call to create a directory(if it does not exist)
		var gotFileSystem = function (fileSystem) {
	    	fileSystem.root.getDirectory(directoryName, { create: true, exclusive: false }, getDirSuccess, dirFail);
		};
	    // get file system to copy or move audio file to a specified location
	    window.requestFileSystem(LocalFileSystem.PERSISTENT, 0, gotFileSystem, fsFail);
	}
	
	var onSuccess = function() {
		var mediaObjSuccess = {"mediaObjSuccessMessage":"Media object successfully created"};
		window.frames[0].postMessage(mediaObjSuccess,url);
	}
	
	// onError Callback 
	var onError = function(error) {
		var mediaObjError = {"mediaObjErrorMessage":"Error occurred in creating media object",
				  			 "mediaObjErrorCode":error.code};
		window.frames[0].postMessage(mediaObjError,url);
	}
	
	var showLoader = function(){
		ActivityIndicator.show("Loading...");
	}
	var loadNativeHomePage = function(){
		window.JSInterface.loadNativeHomePage();
	}
	
	
	/******************************************* Helper Functions Ends *********************************************/
	
	
	/******************************************* Audio Specific Starts *********************************************/
	/* Record button will call this function */
	var recordAudio = function(parameters) {
		try{
			createDirectory();
			mediaObject = new Media(filePath, onSuccess, onError);
			// Record audio
			mediaObject.startRecord();
			counter = 0;
			if(parameters.maxDuration != undefined){
				recordDur = setInterval(function() {
					counter = counter + 1;
			        if (counter == parameters.maxDuration) {
			            clearInterval(recordDur);
			            var recordMediaCallbackSuccess  = {
			                    'location' : 'device',
			                    'operation' : 'recordMediaCallback',
			                    'response' : {
			                    	'success' : true
			                    }
			                };
			            window.frames[0].postMessage(recordMediaCallbackSuccess ,url);
			        }
			   }, 1000);
			}
		}catch(err){
			var recordMediaCallbackFailure = {
	                'location' : 'device',
	                'operation' : 'recordMediaCallback',
	                'response' : {
	                	'success' : false,
	                	'errorMessage' :err.message
	                }
	            };
			window.frames[0].postMessage(recordMediaCallbackFailure,url);
		}	
	}
	
	/* Stop button will call this function and saves file at a specified location */
	var stopRecordAudio = function() {
		try{ 
			counter = 0;
			clearInterval(recordDur);
			if (mediaObject) {
			 	//function to stop recording
		        mediaObject.stopRecord();
		        var stopRecordCallbackSuccess = {
	                    'location' : 'device',
	                    'operation' : 'stopRecordCallback',
	                    'response' : {
	                    	'success' : true
	                    }
	                };
	         		
				window.frames[0].postMessage(stopRecordCallbackSuccess,url);
		        
		    }
		}catch(err){
			var stopRecordCallbackFailure = {
	                'location' : 'device',
	                'operation' : 'stopRecordCallback',
	                'response' : {
	                	'success' : false,
	                	'errorMessage' : err.message
	                }
	            };
			window.frames[0].postMessage(stopRecordCallbackFailure,url);
		}
	}
	
	/* Start playback */
	var startPlayback = function() {
		try{
			mediaObject = new Media(filePath, onSuccess, onError);
			mediaObject.play();
	
			/* On playback completion, send a post message to MyELT which further delegates it to activity so as to handle any UI or engine level changes. */
			counter = 0;
			var audioFileDuration = 0;
			timeDur = setInterval(function() {
				audioFileDuration = mediaObject.getDuration();
				counter = counter + 1;
		        if (counter >= audioFileDuration) {
		            clearInterval(timeDur);
		            var startPlaybackCallbackSuccess = {
		                    'location' : 'device',
		                    'operation' : 'startPlaybackCallback',
		                    'response' : {
		                    	'success' : true
		                    }
		                };
		            window.frames[0].postMessage(startPlaybackCallbackSuccess,url);
		        }
		   }, 1000);
		}catch(err){
			var startPlaybackCallbackFailure = {
                    'location' : 'device',
                    'operation' : 'startPlaybackCallback',
                    'response' : {
                    	'success' : false,
                    	'errorMessage' : err.message
                    }
                };
			window.frames[0].postMessage(startPlaybackCallbackFailure,url);
		}
	}
	
	/* Stop playback */
	var stopPlayback = function() {
		try{	
			counter = 0;
			clearInterval(timeDur);
			if (mediaObject) {
				mediaObject.stop();
				var stopPlaybackCallbackSuccess = {
                    'location' : 'device',
                    'operation' : 'stopPlaybackCallback',
                    'response' : {
                    	'success' : true
                    }
                };
				window.frames[0].postMessage(stopPlaybackCallbackSuccess,url);
	        }
		}catch(err){
			var stopPlaybackCallbackFailure = {
                'location' : 'device',
                'operation' : 'stopPlaybackCallback',
                'response' : {
                	'success' : false,
                	'errorMessage' : err.message
                }
            };
			window.frames[0].postMessage(stopPlaybackCallbackFailure,url);
		}	
	}
	
	var scoreAudio = function(activityOptions) {	  
        var gotFileSystem = function (fileSystem) {
            var appDirPath = fileSystem.root.toURL();        
            
            if(isIOSDevice()) {
            	appDirPath = appDirPath.replace("/Documents/", "/tmp/");
            }	  
            
            var fileURL = appDirPath + filePath;     
          
            var uploadSuccess = function(data) {
                var response = JSON.parse(data.response);
                var responseJSON = {
                    'location' : 'device',
                    'operation' : 'scoreSRIMediaCallback',
                    'response' : {
                    	'success' : true
                    }
                };                
                window.frames[0].postMessage(responseJSON,url);
            }

            var uploadFailure = function(error) {
            	var responseJSON = {
                        'location' : 'device',
                        'operation' : 'scoreSRIMediaCallback',
                        'response' : {
                        	'success' : false,
                        	'errorMessage' : 'An unexpected error occured. Please check your internet connectivity and try after restarting application.\n\nInternal Server Error:\n\n'+JSON.stringify(error)
                        }
                    };
        		window.frames[0].postMessage(responseJSON,url);
            }                

            var options = new FileUploadOptions();
            options.fileKey= "file";
            options.fileName= defaultAudioFileName;
            options.mimeType= "audio/wav";
            
            var params = {};
            params.filePath = activityOptions.fileName;

            options.params = params;          

            var ft = new FileTransfer();           
            ft.upload(fileURL, "http://sridemo.comprotechnologies.com:8080/sriUploader/upload", uploadSuccess, uploadFailure, options);
   	
	   };
	        
	   // get file system to copy or move audio file to a specified folder
	   window.requestFileSystem(LocalFileSystem.PERSISTENT, 0, gotFileSystem, fsFail);	   
	}
	
	/******************************************* Audio Specific Ends *********************************************/
	
	/******************************************* Device Ready Specific Starts *********************************************/
	
	var onDeviceReady = function() {
		var iframe = document.getElementById('MyELTIframe');
		iframe.addEventListener("load", 
		function(event) {
		     ActivityIndicator.hide();
		     window.JSInterface.showMyELT();
		}, false);	
						
		//Listens for events via postMessage
		window.addEventListener("message", function(event) {
			
			if (event.data.operation == "recordMedia") {
				defaultAudioFileName = (event.data.options.fileName.split('/'))[2];
				if(isIOSDevice()) {
					filePath = defaultAudioFileName;
				} else {
					filePath = directoryName + "/" + defaultAudioFileName;			
				}
     			recordAudio(event.data.options);
     		}
			if (event.data.operation == "stopRecord") {
     			stopRecordAudio(event.data.options);
     		}
			if (event.data.operation == "scoreSRIMedia") {
     			scoreAudio(event.data.options);
     		}
			if (event.data.operation == "startPlayback") {
     			startPlayback(event.data.options);
     		}
			if (event.data.operation == "stopPlayback") {
				stopPlayback(event.data.options);
     		}
			if (event.data.operation == "showLoader") {
				showLoader();
     		}
			if (event.data.operation == "loadNativeHomePage") {
				loadNativeHomePage();
     		}
			
		});
		
		
	}
	document.addEventListener("deviceready", onDeviceReady, false);
	/******************************************* Device Ready Specific Ends *********************************************/
	
})();
   