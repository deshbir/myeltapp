(function(){
	
	/* Global Variables. */
	var url  = "http://myelt3.comprotechnologies.com/ilrn/global/extlogin.do";
	var mediaObject;
	var recordPlayTime;
	var directoryName = "MyELT";
	var defaultAduioFileName ="audioFile.wav";
	var filePath;
	
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
		/*recordPlayTime = 0;
		recInterval = setInterval(function() {
	    	recordPlayTime = recordPlayTime + 1;
	    	setAudioPosition(recordPlayTime + " sec");
	    	}, 1000);*/
	}
	
	/* Stop button will call this function and saves file at a specified location */
	var stopRecordAudio = function() {
		 if (mediaObject) {
		 	//clearInterval(recInterval);
		 	//function to stop recording
	        mediaObject.stopRecord();
	    }
	}
	
	/* Start playback */
	var startPlayback = function() {
		mediaObject = new Media(filePath, onSuccess, onError);
		mediaObject.play();
	}
	
	/* Stop playback */
	var stopPlayback = function() {
		if (mediaObject) {
			mediaObject.stop();
        }
	}
	
	/* Score Audio */
	var scoreAudio = function(clientId) {
	    //This function is called if directory successfully created
		var getDirSuccess1 = function(entry) {
	     	entry.file(gotFile, fail);
	    };
	    //This function is called if error occurs while fetching file from directory
		var fail = function(error) {
	        alert("An error has occurred: Code = " = error.code);
	    };
	    //This function is called if file is successfully found
	    function gotFile(file){
	    	var reader = new FileReader();
	    	reader.onloadend = function (evt) {
				var byteArray = new Uint8Array(evt.target.result);
	    		compressedAudio = Speex.encodeFile(byteArray);
				jQuery.ajax({
				    url : "http://sridemo.comprotechnologies.com:8080/sriUploader/upload?clientID=" + clientId,
				    type: "POST",
	   				contentType: false,
				    data: btoa(compressedAudio),
				    processData:false
				}).done(function(data){
						var response = {
		         			'location' : 'device',
		         			'operation' : 'scoreResults',
		         			'filePath' : data.filePath
		         		};
						window.frames[0].postMessage(response,url);
					}).fail(function(){
					alert("FilePath not recieved ");
				});
		    };
		    reader.readAsArrayBuffer(file);
	    };
	    var gotFileSystem1 = function (fileSystem) {
	    	fileSystem.root.getFile(filePath, null, getDirSuccess1, dirFail);
	    };
	    // get file system to copy or move audio file to a specified folder
	    window.requestFileSystem(LocalFileSystem.PERSISTENT, 0, gotFileSystem1, fsFail);
	    
	}
	/******************************************* Audio Specific Ends *********************************************/
	
	/******************************************* Device Ready Specific Starts *********************************************/
	
	var onDeviceReady = function() {
		var iframe = document.getElementById('MyELTIframe');
        var iframeURL = url + "?u=" + userName + "&p=" + password;
        //  alert(iframeURL);
		iframe.src= iframeURL;		
		//var initializeIframe = true;
		//iframe.src= url;
		
		//var initializeIframe = true;
		
		iframe.addEventListener("load", 
			function(event) {
			window.frames[0].postMessage({'location' : 'device'},url);
				/*if(initializeIframe) {
					window.frames[0].postMessage({'location' : 'device'},url);
					initializeIframe = false;
				}*/
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
			filePath = "documents://" + defaultAduioFileName;
		} else {
			filePath = directoryName + "/" + defaultAduioFileName;
			
		}
	}
	document.addEventListener("deviceready", onDeviceReady, false);
	/******************************************* Device Ready Specific Ends *********************************************/
	
})();
	