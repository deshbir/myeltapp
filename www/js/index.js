(function(){
	
	/* Global Variables. */
	var url  = "http://192.168.1.59:3714/ilrn/global/extlogin.do";
	var mediaObject;
	var recordPlayTime;
	var directoryName = "MyELT";
	var defaultAudioFileName ="audioFile.wav";
	var filePath;
	var counter = 0;
	var timeDur = 0;
	
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
	   alert("failed with error code: " + error.code);
	};
	//function call if directory fails 
	var dirFail = function(error) {
	   alert("Directory error code: " + error.code);
	};
	//function call if directory successfully created
	var getDirSuccess = function() {
		console.log("Directory created Successfully");
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
		console.log("recordAudio():Audio Success");
	}
	
	// onError Callback 
	var onError = function(error) {
		alert('code: '    + error.code    + '\n' + 
	     'message: ' + error.message + '\n');
	}

	var setAudioPosition = function(position) {
		window.frames[0].document.getElementById('audio_position').innerHTML = position;
	}
	
	/******************************************* Helper Functions Ends *********************************************/
	
	
	/******************************************* Audio Specific Starts *********************************************/
	/* Record button will call this function */
	var recordAudio = function() {
		createDirectory();
		mediaObject = new Media(filePath, onSuccess, onError);
		// Record audio
		mediaObject.startRecord();
	}
	
	/* Stop button will call this function and saves file at a specified location */
	var stopRecordAudio = function() {
		 if (mediaObject) {
		 	//function to stop recording
	        mediaObject.stopRecord();
	    }
	}
	
	/* Start playback */
	var startPlayback = function() {
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
	            window.frames[0].postMessage({'location' : 'device','operation' : 'playbackCompleted'},url);
	        }
	   }, 1000);
	}
	
	/* Stop playback */
	var stopPlayback = function() {
		counter = 0;
		clearInterval(timeDur);
		if (mediaObject) {
			mediaObject.stop();
        }
	}
	
	/* Score Audio */
	var scoreAudio = function(clientId) {	   
	    //This function is called if file is successfully found
	    function getDirSuccess(fileObj){                        
            
            var fileURL;
            
            if(isIOSDevice()) {
			    fileURL = fileObj.nativeURL;
                fileURL = fileURL.replace("/Documents/" + defaultAudioFileName , "/tmp/" + defaultAudioFileName);
            } else {
                fileURL = fileObj.fullPath;	
            }
            
	    	var uploadSuccess = function(data) {               
                var response = JSON.parse(data.response);                
                var responseJSON = {
                    'location' : 'device',
                    'operation' : 'scoreResults',
                    'filePath' : response.filePath
                };                
                window.frames[0].postMessage(responseJSON,url);
            }

            var uploadFailure = function(error) {
                alert("An error has occurred, Response= " + JSON.stringify(error));               
            }                

            var options = new FileUploadOptions();
            options.fileKey= "file";
            options.fileName= defaultAudioFileName;
            options.mimeType= "audio/wav";
            
            var params = {};
            params.fileExtension = "wav";
            params.clientID = clientId;

            options.params = params;          

            var ft = new FileTransfer();           
            ft.upload(fileURL, "http://sridemo.comprotechnologies.com:8080/sriUploader/upload", uploadSuccess, uploadFailure, options);
	    };
        
	    var gotFileSystem = function (fileSystem) {
	    	fileSystem.root.getFile(filePath, null, getDirSuccess, dirFail);
	    };
        
	    // get file system to copy or move audio file to a specified folder
	    window.requestFileSystem(LocalFileSystem.PERSISTENT, 0, gotFileSystem, fsFail);
	    
	}
	/******************************************* Audio Specific Ends *********************************************/
	
	/******************************************* Device Ready Specific Starts *********************************************/
	
	var onDeviceReady = function() {
		var iframe = document.getElementById('MyELTIframe');
        var iframeURL = url + "?u=" + userName + "&p=" + password;        
		iframe.src= iframeURL;	
		
		iframe.addEventListener("load", 
			function(event) {
			     window.frames[0].postMessage({'location' : 'device'},url);				
			}, false);
		//Listens for events via postMessage
		window.addEventListener("message", function(event) {
			if (event.data.operation == "startRecording") {
     			recordAudio();
     		}
			if (event.data.operation == "stopRecording") {
     			stopRecordAudio();
     		}
			if (event.data.operation == "scoreAudio") {
     			scoreAudio(event.data.clientId);
     		}
			if (event.data.operation == "startPlayback") {
     			startPlayback();
     		}
			if (event.data.operation == "stopPlayback") {
				stopPlayback();
     		}
		});
		
		if(isIOSDevice()) {
			filePath = defaultAudioFileName;
		} else {
			filePath = directoryName + "/" + defaultAudioFileName;			
		}
	}
	document.addEventListener("deviceready", onDeviceReady, false);
	/******************************************* Device Ready Specific Ends *********************************************/
	
})();
	
    
     NSMutableDictionary *recordSettings = [[NSMutableDictionary alloc] init];
            [recordSettings setValue:[NSNumber numberWithFloat:22050.0] forKey:AVSampleRateKey];
            
            // create a new recorder for each start record
            audioFile.recorder = [[CDVAudioRecorder alloc] initWithURL:audioFile.resourceURL settings:recordSettings error:&error];