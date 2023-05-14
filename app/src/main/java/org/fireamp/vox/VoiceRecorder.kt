import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import androidx.core.app.ActivityCompat

class VoiceRecorder(
    private val context: Context,
    private val sampleRate: Int,
    private val minRecordingLevel: Float,
    private val maxSilenceTime: Long,
    private val onRecordingFinished: (FloatArray) -> Unit
) {

    private lateinit var audioRecord: AudioRecord
    private lateinit var recordingThread: Thread

    fun startRecording() {
        //isRecording = true
        val bufferSize = AudioRecord.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_FLOAT)
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e("VoiceRecorder", "Couldn't aquire record permission")
            return
        }
        audioRecord = AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_FLOAT, bufferSize)
        audioRecord.startRecording()

        recordingThread = Thread {
            var isRecording = true
            while (isRecording) {
                val buffer = FloatArray(bufferSize)
                val data = mutableListOf<Float>()
                var recordingStarted = false
                var silentTime = 0L

                while (isRecording && silentTime < maxSilenceTime) {
                    val numSamplesRead =
                        audioRecord.read(buffer, 0, bufferSize, AudioRecord.READ_BLOCKING)

                    // Check if recording should start
                    if (!recordingStarted) {
                        val maxAmplitude = buffer.maxByOrNull { Math.abs(it) } ?: 0f
                        Log.i("VoiceRecorder","Sarah:" + maxAmplitude)
                        if (maxAmplitude >= minRecordingLevel) {
                            recordingStarted = true
                        }
                    }

                    // Record audio if recording has started
                    if (recordingStarted) {
                        data.addAll(buffer.toList())
                        Log.i("VoiceRecorder","Laura:" + Math.abs(buffer.maxByOrNull { Math.abs(it) }
                            ?: 0f) + " Silence: " + (Math.abs(buffer.maxByOrNull { Math.abs(it) }
                            ?: 0f) < minRecordingLevel) + " Silence Samples: " + silentTime)
                        // Check if recording should stop due to

                        if (Math.abs(buffer.maxByOrNull { Math.abs(it) }
                                ?: 0f) < minRecordingLevel) {
                            silentTime += numSamplesRead.toLong()
                        } else {
                            silentTime = 0L
                        }
                    }
                }

                audioRecord.stop()

                if (data.isNotEmpty()) {
                    onRecordingFinished(data.toFloatArray())
                    isRecording = false
                }
            }
            audioRecord.release()
        }

        recordingThread.start()
    }

    fun stopRecording() {
        //isRecording = false
        try {
            recordingThread.join()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }
}
