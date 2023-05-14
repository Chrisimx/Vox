package org.fireamp.vox

import VoiceRecorder
import android.Manifest
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.runBlocking
import org.fireamp.vox.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var REQUEST_CODE = 1002
    private lateinit var voiceRecorder:VoiceRecorder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        this.requestPermissions( arrayOf(Manifest.permission.RECORD_AUDIO), REQUEST_CODE)
        voiceRecorder = VoiceRecorder(this,16000, 0.014f,32000) {
            Log.w(
                "VoiceRecorder",
                "onCreate: recording finished"
            )

            var whisper = Whisper.createContextFromInputStream(resources.openRawResource(R.raw.ggmlmodelbase))
            Log.w(
                "VoiceRecorder",
                "Model loaded"
            )
            runBlocking {

                Log.w(
                    "VoiceRecorder",
                    "Transcription: " +  whisper.transcribeData(it)
                )
            }
        }
        voiceRecorder.startRecording()
    }
}