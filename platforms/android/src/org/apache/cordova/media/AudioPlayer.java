/*
       Licensed to the Apache Software Foundation (ASF) under one
       or more contributor license agreements.  See the NOTICE file
       distributed with this work for additional information
       regarding copyright ownership.  The ASF licenses this file
       to you under the Apache License, Version 2.0 (the
       "License"); you may not use this file except in compliance
       with the License.  You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

       Unless required by applicable law or agreed to in writing,
       software distributed under the License is distributed on an
       "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
       KIND, either express or implied.  See the License for the
       specific language governing permissions and limitations
       under the License.
*/
package org.apache.cordova.media;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;

/**
 * This class implements the audio playback and recording capabilities used by Cordova.
 * It is called by the AudioHandler Cordova class.
 * Only one file can be played or recorded per class instance.
 *
 * Local audio files must reside in one of two places:
 *      android_asset:      file name must start with /android_asset/sound.mp3
 *      sdcard:             file name is just sound.mp3
 */
public class AudioPlayer implements OnCompletionListener, OnPreparedListener, OnErrorListener {

    // AudioPlayer modes
    public enum MODE { NONE, PLAY, RECORD };

    // AudioPlayer states
    public enum STATE { MEDIA_NONE,
                        MEDIA_STARTING,
                        MEDIA_RUNNING,
                        MEDIA_PAUSED,
                        MEDIA_STOPPED,
                        MEDIA_LOADING
                      };

    private static final String LOG_TAG = "AudioPlayer";

    // AudioPlayer message ids
    private static int MEDIA_STATE = 1;
    private static int MEDIA_DURATION = 2;
    private static int MEDIA_POSITION = 3;
    private static int MEDIA_ERROR = 9;

    // Media error codes
    private static int MEDIA_ERR_NONE_ACTIVE    = 0;
    private static int MEDIA_ERR_ABORTED        = 1;
//    private static int MEDIA_ERR_NETWORK        = 2;
//    private static int MEDIA_ERR_DECODE         = 3;
//    private static int MEDIA_ERR_NONE_SUPPORTED = 4;

    private AudioHandler handler;           // The AudioHandler object
    private String id;                      // The id of this player (used to identify Media object in JavaScript)
    private MODE mode = MODE.NONE;          // Playback or Recording mode
    private STATE state = STATE.MEDIA_NONE; // State of recording or playback

    private String audioFile = null;        // File name to play or record to
    private float duration = -1;            // Duration of audio

    private AudioRecord recorder = null;  // Audio recording object
    private String tempFile = null;         // Temporary recording file name

    private MediaPlayer player = null;      // Audio player object
    private boolean prepareOnly = true;     // playback after file prepare flag
    private int seekOnPrepared = 0;     // seek to this location once media is prepared
    
    //Audio settings
    private static final int RECORDER_SAMPLERATE = 22050;
   	private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
   	private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
   	
   	private static final String AUDIO_RECORDER_FOLDER = "media-temp";
   	private static final String AUDIO_RECORDER_TEMP_FILE = "record_temp.raw";
   	private static final int RECORDER_BPP = 16;
   	
   	private int bufferSize = 0;
   	private Thread recordingThread = null;
   	private boolean isRecording = false;

    /**
     * Constructor.
     *
     * @param handler           The audio handler object
     * @param id                The id of this audio player
     */
    public AudioPlayer(AudioHandler handler, String id, String file) {
        this.handler = handler;
        this.id = id;
        this.audioFile = file;
        this.bufferSize = AudioRecord.getMinBufferSize(RECORDER_SAMPLERATE,RECORDER_CHANNELS,RECORDER_AUDIO_ENCODING);
    }

    /**
     * Destroy player and stop audio playing or recording.
     */
    public void destroy() {
        // Stop any play or record
        if (this.player != null) {
            if ((this.state == STATE.MEDIA_RUNNING) || (this.state == STATE.MEDIA_PAUSED)) {
                this.player.stop();
                this.setState(STATE.MEDIA_STOPPED);
            }
            this.player.release();
            this.player = null;
        }
        if (this.recorder != null) {
            this.stopRecording();
            this.recorder.release();
            this.recorder = null;
        }
    }

    /**
     * Start recording the specified file.
     *
     * @param file              The name of the file
     */
    public void startRecording(String file) {
        switch (this.mode) {
        case PLAY:
            Log.d(LOG_TAG, "AudioPlayer Error: Can't record in play mode.");
            this.handler.webView.sendJavascript("cordova.require('org.apache.cordova.media.Media').onStatus('" + this.id + "', "+MEDIA_ERROR+", { \"code\":"+MEDIA_ERR_ABORTED+"});");
            break;
        case NONE:
            this.audioFile = file;
            try {
	            recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
						RECORDER_SAMPLERATE, RECORDER_CHANNELS,RECORDER_AUDIO_ENCODING, bufferSize);
	            
	            recorder.startRecording();				
				isRecording = true;				
				
				recordingThread = new Thread(new Runnable() {					
					@Override
					public void run() {
						writeAudioDataToFile();
					}
				},"AudioRecorder Thread");
				
				recordingThread.start();
				
				this.setState(STATE.MEDIA_RUNNING);
	            return;
            }   
            catch (IllegalStateException e) {
            	e.printStackTrace();
            }
            this.handler.webView.sendJavascript("cordova.require('org.apache.cordova.media.Media').onStatus('" + this.id + "', "+MEDIA_ERROR+", { \"code\":"+MEDIA_ERR_ABORTED+"});");
            break;
        case RECORD:
            Log.d(LOG_TAG, "AudioPlayer Error: Already recording.");
            this.handler.webView.sendJavascript("cordova.require('org.apache.cordova.media.Media').onStatus('" + this.id + "', "+MEDIA_ERROR+", { \"code\":"+MEDIA_ERR_ABORTED+"});");
        }
    }
        
    private void writeAudioDataToFile(){
		byte data[] = new byte[bufferSize];
		String filename = getTempFilename();
		FileOutputStream os = null;
		
		try {
			os = new FileOutputStream(filename);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		int read = 0;
		
		if(null != os){
			while(isRecording){
				read = recorder.read(data, 0, bufferSize);
				if(AudioRecord.ERROR_INVALID_OPERATION != read){
					try {
						os.write(data);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			
			try {
				os.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
    
    private String getTempFilename(){
		String filepath = Environment.getExternalStorageDirectory().getPath();
		File file = new File(filepath,AUDIO_RECORDER_FOLDER);
		
		if(!file.exists()){
			file.mkdirs();
		}
		
		File tempFile = new File(filepath,AUDIO_RECORDER_TEMP_FILE);
		
		if(tempFile.exists())
			tempFile.delete();
		
		return (file.getAbsolutePath() + "/" + AUDIO_RECORDER_TEMP_FILE);
	}
    
    private String getFilename(){
		String filepath = Environment.getExternalStorageDirectory().getPath() +  "/" + this.audioFile;
		String fileDir = filepath.substring(0, filepath.lastIndexOf("/"));
		File file = new File(fileDir);
		
		if(!file.exists()){
			file.mkdirs();
		}
		
		return (filepath);
	}
    
    private void deleteTempFile() {
		File file = new File(getTempFilename());
		file.delete();
	}
    
    private void copyWaveFile(String inFilename,String outFilename){
		FileInputStream in = null;
		FileOutputStream out = null;
		long totalAudioLen = 0;
		long totalDataLen = totalAudioLen + 36;
		long longSampleRate = RECORDER_SAMPLERATE;
		int channels = 1;
		long byteRate = RECORDER_BPP * RECORDER_SAMPLERATE * channels/8;
		
		byte[] data = new byte[bufferSize];
                
		try {
			in = new FileInputStream(inFilename);
			out = new FileOutputStream(outFilename);
			totalAudioLen = in.getChannel().size();
			totalDataLen = totalAudioLen + 36;
			
			WriteWaveFileHeader(out, totalAudioLen, totalDataLen,
					longSampleRate, channels, byteRate);
			
			while(in.read(data) != -1){
				out.write(data);
			}
			
			in.close();
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
    
    private void WriteWaveFileHeader(
			FileOutputStream out, long totalAudioLen,
			long totalDataLen, long longSampleRate, int channels,
			long byteRate) throws IOException {
		
		byte[] header = new byte[44];
		
		header[0] = 'R';  // RIFF/WAVE header
		header[1] = 'I';
		header[2] = 'F';
		header[3] = 'F';
		header[4] = (byte) (totalDataLen & 0xff);
		header[5] = (byte) ((totalDataLen >> 8) & 0xff);
		header[6] = (byte) ((totalDataLen >> 16) & 0xff);
		header[7] = (byte) ((totalDataLen >> 24) & 0xff);
		header[8] = 'W';
		header[9] = 'A';
		header[10] = 'V';
		header[11] = 'E';
		header[12] = 'f';  // 'fmt ' chunk
		header[13] = 'm';
		header[14] = 't';
		header[15] = ' ';
		header[16] = 16;  // 4 bytes: size of 'fmt ' chunk
		header[17] = 0;
		header[18] = 0;
		header[19] = 0;
		header[20] = 1;  // format = 1
		header[21] = 0;
		header[22] = (byte) channels;
		header[23] = 0;
		header[24] = (byte) (longSampleRate & 0xff);
		header[25] = (byte) ((longSampleRate >> 8) & 0xff);
		header[26] = (byte) ((longSampleRate >> 16) & 0xff);
		header[27] = (byte) ((longSampleRate >> 24) & 0xff);
		header[28] = (byte) (byteRate & 0xff);
		header[29] = (byte) ((byteRate >> 8) & 0xff);
		header[30] = (byte) ((byteRate >> 16) & 0xff);
		header[31] = (byte) ((byteRate >> 24) & 0xff);
		header[32] = (byte) (2 * 16 / 8);  // block align
		header[33] = 0;
		header[34] = RECORDER_BPP;  // bits per sample
		header[35] = 0;
		header[36] = 'd';
		header[37] = 'a';
		header[38] = 't';
		header[39] = 'a';
		header[40] = (byte) (totalAudioLen & 0xff);
		header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
		header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
		header[43] = (byte) ((totalAudioLen >> 24) & 0xff);

		out.write(header, 0, 44);
	}



    /**
     * Save temporary recorded file to specified name
     *
     * @param file
     */
    public void moveFile(String file) {
        /* this is a hack to save the file as the specified name */
        File f = new File(this.tempFile);

        if (!file.startsWith("/")) {
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                file = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + file;
            } else {
                file = "/data/data/" + handler.cordova.getActivity().getPackageName() + "/cache/" + file;
            }
        }

        String logMsg = "renaming " + this.tempFile + " to " + file;
        Log.d(LOG_TAG, logMsg);
        if (!f.renameTo(new File(file))) Log.e(LOG_TAG, "FAILED " + logMsg);
    }

    /**
     * Stop recording and save to the file specified when recording started.
     */
    public void stopRecording() {
        if (this.recorder != null) {
            try{
                if (this.state == STATE.MEDIA_RUNNING) {
                	isRecording = false;
                	recorder.stop();
        			recorder.release();
        			
        			recorder = null;
        			recordingThread = null;
        			
                    this.setState(STATE.MEDIA_STOPPED);
                }
                copyWaveFile(getTempFilename(), getFilename());
                deleteTempFile();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //==========================================================================
    // Playback
    //==========================================================================

    /**
     * Start or resume playing audio file.
     *
     * @param file              The name of the audio file.
     */
    public void startPlaying(String file) {
        if (this.readyPlayer(file) && this.player != null) {
            this.player.start();
            this.setState(STATE.MEDIA_RUNNING);
            this.seekOnPrepared = 0; //insures this is always reset
        } else {
            this.prepareOnly = false;
        }
    }

    /**
     * Seek or jump to a new time in the track.
     */
    public void seekToPlaying(int milliseconds) {
        if (this.readyPlayer(this.audioFile)) {
            this.player.seekTo(milliseconds);
            Log.d(LOG_TAG, "Send a onStatus update for the new seek");
            this.handler.webView.sendJavascript("cordova.require('org.apache.cordova.media.Media').onStatus('" + this.id + "', " + MEDIA_POSITION + ", " + milliseconds / 1000.0f + ");");
        }
        else {
            this.seekOnPrepared = milliseconds;
        }
    }

    /**
     * Pause playing.
     */
    public void pausePlaying() {

        // If playing, then pause
        if (this.state == STATE.MEDIA_RUNNING && this.player != null) {
            this.player.pause();
            this.setState(STATE.MEDIA_PAUSED);
        }
        else {
            Log.d(LOG_TAG, "AudioPlayer Error: pausePlaying() called during invalid state: " + this.state.ordinal());
            this.handler.webView.sendJavascript("cordova.require('org.apache.cordova.media.Media').onStatus('" + this.id + "', " + MEDIA_ERROR + ", { \"code\":" + MEDIA_ERR_NONE_ACTIVE + "});");
        }
    }

    /**
     * Stop playing the audio file.
     */
    public void stopPlaying() {
        if ((this.state == STATE.MEDIA_RUNNING) || (this.state == STATE.MEDIA_PAUSED)) {
            this.player.pause();
            this.player.seekTo(0);
            Log.d(LOG_TAG, "stopPlaying is calling stopped");
            this.setState(STATE.MEDIA_STOPPED);
        }
        else {
            Log.d(LOG_TAG, "AudioPlayer Error: stopPlaying() called during invalid state: " + this.state.ordinal());
            this.handler.webView.sendJavascript("cordova.require('org.apache.cordova.media.Media').onStatus('" + this.id + "', " + MEDIA_ERROR + ", { \"code\":" + MEDIA_ERR_NONE_ACTIVE + "});");
        }
    }

    /**
     * Callback to be invoked when playback of a media source has completed.
     *
     * @param player           The MediaPlayer that reached the end of the file
     */
    public void onCompletion(MediaPlayer player) {
        Log.d(LOG_TAG, "on completion is calling stopped");
        this.setState(STATE.MEDIA_STOPPED);
    }

    /**
     * Get current position of playback.
     *
     * @return                  position in msec or -1 if not playing
     */
    public long getCurrentPosition() {
        if ((this.state == STATE.MEDIA_RUNNING) || (this.state == STATE.MEDIA_PAUSED)) {
            int curPos = this.player.getCurrentPosition();
            this.handler.webView.sendJavascript("cordova.require('org.apache.cordova.media.Media').onStatus('" + this.id + "', " + MEDIA_POSITION + ", " + curPos / 1000.0f + ");");
            return curPos;
        }
        else {
            return -1;
        }
    }

    /**
     * Determine if playback file is streaming or local.
     * It is streaming if file name starts with "http://"
     *
     * @param file              The file name
     * @return                  T=streaming, F=local
     */
    public boolean isStreaming(String file) {
        if (file.contains("http://") || file.contains("https://")) {
            return true;
        }
        else {
            return false;
        }
    }

    /**
      * Get the duration of the audio file.
      *
      * @param file             The name of the audio file.
      * @return                 The duration in msec.
      *                             -1=can't be determined
      *                             -2=not allowed
      */
    public float getDuration(String file) {

        // Can't get duration of recording
        if (this.recorder != null) {
            return (-2); // not allowed
        }

        // If audio file already loaded and started, then return duration
        if (this.player != null) {
            return this.duration;
        }

        // If no player yet, then create one
        else {
            this.prepareOnly = true;
            this.startPlaying(file);

            // This will only return value for local, since streaming
            // file hasn't been read yet.
            return this.duration;
        }
    }

    /**
     * Callback to be invoked when the media source is ready for playback.
     *
     * @param player           The MediaPlayer that is ready for playback
     */
    public void onPrepared(MediaPlayer player) {
        // Listen for playback completion
        this.player.setOnCompletionListener(this);
        // seek to any location received while not prepared
        this.seekToPlaying(this.seekOnPrepared);
        // If start playing after prepared
        if (!this.prepareOnly) {
            this.player.start();
            this.setState(STATE.MEDIA_RUNNING);
            this.seekOnPrepared = 0; //reset only when played
        } else {
            this.setState(STATE.MEDIA_STARTING);
        }
        // Save off duration
        this.duration = getDurationInSeconds();
        // reset prepare only flag
        this.prepareOnly = true;

        // Send status notification to JavaScript
        this.handler.webView.sendJavascript("cordova.require('org.apache.cordova.media.Media').onStatus('" + this.id + "', " + MEDIA_DURATION + "," + this.duration + ");");
    }

    /**
     * By default Android returns the length of audio in mills but we want seconds
     *
     * @return length of clip in seconds
     */
    private float getDurationInSeconds() {
        return (this.player.getDuration() / 1000.0f);
    }

    /**
     * Callback to be invoked when there has been an error during an asynchronous operation
     *  (other errors will throw exceptions at method call time).
     *
     * @param player           the MediaPlayer the error pertains to
     * @param arg1              the type of error that has occurred: (MEDIA_ERROR_UNKNOWN, MEDIA_ERROR_SERVER_DIED)
     * @param arg2              an extra code, specific to the error.
     */
    public boolean onError(MediaPlayer player, int arg1, int arg2) {
        Log.d(LOG_TAG, "AudioPlayer.onError(" + arg1 + ", " + arg2 + ")");

        // TODO: Not sure if this needs to be sent?
        this.player.stop();
        this.player.release();

        // Send error notification to JavaScript
        this.handler.webView.sendJavascript("cordova.require('org.apache.cordova.media.Media').onStatus('" + this.id + "', { \"code\":" + arg1 + "});");
        return false;
    }

    /**
     * Set the state and send it to JavaScript.
     *
     * @param state
     */
    private void setState(STATE state) {
        if (this.state != state) {
            this.handler.webView.sendJavascript("cordova.require('org.apache.cordova.media.Media').onStatus('" + this.id + "', " + MEDIA_STATE + ", " + state.ordinal() + ");");
        }
        this.state = state;
    }

    /**
     * Set the mode and send it to JavaScript.
     *
     * @param state
     */
    private void setMode(MODE mode) {
        if (this.mode != mode) {
            //mode is not part of the expected behavior, so no notification
            //this.handler.webView.sendJavascript("cordova.require('org.apache.cordova.media.Media').onStatus('" + this.id + "', " + MEDIA_STATE + ", " + mode + ");");
        }
        this.mode = mode;
    }

    /**
     * Get the audio state.
     *
     * @return int
     */
    public int getState() {
        return this.state.ordinal();
    }

    /**
     * Set the volume for audio player
     *
     * @param volume
     */
    public void setVolume(float volume) {
        this.player.setVolume(volume, volume);
    }

    /**
     * attempts to put the player in play mode
     * @return true if in playmode, false otherwise
     */
    private boolean playMode() {
        switch(this.mode) {
        case NONE:
            this.setMode(MODE.PLAY);
            break;
        case PLAY:
            break;
        case RECORD:
            Log.d(LOG_TAG, "AudioPlayer Error: Can't play in record mode.");
            this.handler.webView.sendJavascript("cordova.require('org.apache.cordova.media.Media').onStatus('" + this.id + "', " + MEDIA_ERROR + ", { \"code\":" + MEDIA_ERR_ABORTED + "});");
            return false; //player is not ready
        }
        return true;
    }

    /**
     * attempts to initialize the media player for playback
     * @param file the file to play
     * @return false if player not ready, reports if in wrong mode or state
     */
    private boolean readyPlayer(String file) {
        if (playMode()) {
            switch (this.state) {
                case MEDIA_NONE:
                    if (this.player == null) {
                        this.player = new MediaPlayer();
                    }
                    try {
                        this.loadAudioFile(file);
                    } catch (Exception e) {
                        this.handler.webView.sendJavascript("cordova.require('org.apache.cordova.media.Media').onStatus('" + this.id + "', "+MEDIA_ERROR+", { \"code\":"+MEDIA_ERR_ABORTED+"});");
                    }
                    return false;
                case MEDIA_LOADING:
                    //cordova js is not aware of MEDIA_LOADING, so we send MEDIA_STARTING instead
                    Log.d(LOG_TAG, "AudioPlayer Loading: startPlaying() called during media preparation: " + STATE.MEDIA_STARTING.ordinal());
                    this.prepareOnly = false;
                    return false;
                case MEDIA_STARTING:
                case MEDIA_RUNNING:
                case MEDIA_PAUSED:
                    return true;
                case MEDIA_STOPPED:
                    //if we are readying the same file
                    if (this.audioFile.compareTo(file) == 0) {
                        //reset the audio file
                        player.seekTo(0);
                        player.pause();
                        return true;
                    } else {
                        //reset the player
                        this.player.reset();
                        try {
                            this.loadAudioFile(file);
                        } catch (Exception e) {
                            this.handler.webView.sendJavascript("cordova.require('org.apache.cordova.media.Media').onStatus('" + this.id + "', " + MEDIA_ERROR + ", { \"code\":" + MEDIA_ERR_ABORTED + "});");
                        }
                        //if we had to prepare= the file, we won't be in the correct state for playback
                        return false;
                    }
                default:
                    Log.d(LOG_TAG, "AudioPlayer Error: startPlaying() called during invalid state: " + this.state);
                    this.handler.webView.sendJavascript("cordova.require('org.apache.cordova.media.Media').onStatus('" + this.id + "', " + MEDIA_ERROR + ", { \"code\":" + MEDIA_ERR_ABORTED + "});");
            }
        }
        return false;
    }

    /**
     * load audio file
     * @throws IOException
     * @throws IllegalStateException
     * @throws SecurityException
     * @throws IllegalArgumentException
     */
    private void loadAudioFile(String file) throws IllegalArgumentException, SecurityException, IllegalStateException, IOException {
        if (this.isStreaming(file)) {
            this.player.setDataSource(file);
            this.player.setAudioStreamType(AudioManager.STREAM_MUSIC);
            //if it's a streaming file, play mode is implied
            this.setMode(MODE.PLAY);
            this.setState(STATE.MEDIA_STARTING);
            this.player.setOnPreparedListener(this);
            this.player.prepareAsync();
        }
        else {
            if (file.startsWith("/android_asset/")) {
                String f = file.substring(15);
                android.content.res.AssetFileDescriptor fd = this.handler.cordova.getActivity().getAssets().openFd(f);
                this.player.setDataSource(fd.getFileDescriptor(), fd.getStartOffset(), fd.getLength());
            }
            else {
                File fp = new File(file);
                if (fp.exists()) {
                    FileInputStream fileInputStream = new FileInputStream(file);
                    this.player.setDataSource(fileInputStream.getFD());
                    fileInputStream.close();
                }
                else {
                    this.player.setDataSource(Environment.getExternalStorageDirectory().getPath() + "/" + file);
                }
            }
                this.setState(STATE.MEDIA_STARTING);
                this.player.setOnPreparedListener(this);
                this.player.prepare();

                // Get duration
                this.duration = getDurationInSeconds();
            }
    }
}
