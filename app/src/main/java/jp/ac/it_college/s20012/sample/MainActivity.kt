package jp.ac.it_college.s20012.sample

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.annotation.UiThread
import androidx.annotation.WorkerThread
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.HandlerCompat
import jp.ac.it_college.s20012.sample.databinding.ActivityMainBinding
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fortuneTelling()

    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun getCurrentDate(): String {
        val today = LocalDate.now()
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")
        return dateFormatter.format(today)
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun fortuneTelling(){
        val handler = HandlerCompat.createAsync(mainLooper)
        val executeService = Executors.newSingleThreadExecutor()

        executeService.submit @WorkerThread {
            var result = ""
            val url = URL("http://api.jugemkey.jp/api/horoscope/free/" + getCurrentDate())
            val con = url.openConnection() as? HttpURLConnection
            con?.let {
                try {
                    it.connectTimeout = 1000
                    it.readTimeout = 1000
                    it.requestMethod = "GET"
                    it.connect()
                    val stream = it.inputStream
                    result = is2String(stream)

                    stream.close()
                } catch (e: SocketTimeoutException) {
                    Log.d("TAG", "通信タイムアウト", e)
                }
                it.disconnect()
            }

            handler.post @UiThread {
                val rootJSON = JSONObject(result)
                val horoscope = rootJSON.getJSONObject("horoscope")
                val getDay = horoscope.getJSONArray(getCurrentDate())
                Log.d("TAG", getDay.toString())
            }

            }
        }
        private fun is2String(stream: InputStream): String {
            val sb = StringBuilder()
            val reader = BufferedReader(InputStreamReader(stream, "UTF-8"))
            var line = reader.readLine()
            while (line != null) {
                sb.append(line)
                line = reader.readLine()
            }
            reader.close()
            return sb.toString()
        }
    }

