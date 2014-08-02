var url  = "http://192.168.1.59:3714";

document.addEventListener("deviceready", onDeviceReady, false);
	function onDeviceReady() {
		var iframe = document.getElementById('MyELTIframe');
		iframe.src= url;
		
		var initializeIframe = true;
		
		iframe.addEventListener("load", 
			function(event) {
				if(initializeIframe) {
					window.frames[0].postMessage({'location' : 'device'},url);
					initializeIframe = false;
				}
			}, false);
		//Listens for events via postMessage
		window.addEventListener("message", function(event) {
			if (event.data.operation == "startRecording") {
     			recordAudio();
     		}
			if (event.data.operation == "stopRecording") {
     			stopRecordAudio()
     		}
			if (event.data.operation == "scoreAudio") {
     			scoreAudio(event.data.clientId);
     		}
     		if ((event.data).indexOf("pdf") > -1) {
     			window.open(event.data, '_system', 'location=yes');
     		}
     		if ((event.data).indexOf("Help") > -1) {
     			window.open(event.data, '_self', 'location=yes');
     		}
		});
	}
    
    //Score Audio
	function scoreAudio(clientId) {
		//This function is called if file system fails
	    var fsFail1 = function(error) {
	        alert("failed with error code: " + error.code);
	    };
	    //This function is called if directory system fails
	    var dirFail1 = function(error) {
	        alert("Directory error code: " + error.code);
	    };
	    //This function is called if directory successfully created
		var getDirSuccess1 = function(entry) {
	     	entry.file(gotFile, fail1);
	    };
	    //This function is called if error occurs while fetching file from directory
		var fail1 = function(error) {
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
	    	fileSystem.root.getFile("MyELT1/testAudio.wav", null, getDirSuccess1, dirFail1);
	    };
	    // get file system to copy or move audio file to a specified folder
	    window.requestFileSystem(LocalFileSystem.PERSISTENT, 0, gotFileSystem1, fsFail1);
	    
	}
	//Record button will call this function
	var mediaRec;
	var recTime;
	function recordAudio() {
		//createDirectory();
		audioFile="testAudio.wav";
		mediaRec = new Media(audioFile, onSuccess, onError);
	
		// Record audio
		mediaRec.startRecord();
		recTime = 0;
		recInterval = setInterval(function() {
	    	recTime = recTime + 1;
	    	setAudioPosition(recTime + " sec");
	    	}, 1000);
	}
	function onSuccess() {
		console.log("recordAudio():Audio Success");
	}
	
	// onError Callback 
	function onError(error) {
		alert('code: '    + error.code    + '\n' + 
	     'message: ' + error.message + '\n');
	}

	/*function createDirectory(){
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
	    //function call to create a directory(if it does not exist)
		 var gotFileSystem = function (fileSystem) {
		    	fileSystem.root.getDirectory("MyELT1", { create: true, exclusive: false }, getDirSuccess, dirFail);
		    };
	    // get file system to copy or move audio file to a specified location
	    window.requestFileSystem(LocalFileSystem.PERSISTENT, 0, gotFileSystem, fsFail);
		    
	}*/
	//Stop button will call this function and saves file at a specified location
	function stopRecordAudio() {
		 if (mediaRec) {
		 	clearInterval(recInterval);
		 	//function to stop recording
	        mediaRec.stopRecord();
	    }
	}
	
	function setAudioPosition(position) {
		window.frames[0].document.getElementById('audio_position').innerHTML = position;
	}