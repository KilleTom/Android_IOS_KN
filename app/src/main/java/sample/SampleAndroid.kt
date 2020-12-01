package sample

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_main.*

actual class Sample {
    actual fun checkMe() = 44
}

actual object Platform {
    actual val name: String = "Android"
}

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Sample().checkMe()
        setContentView(R.layout.activity_main)
        findViewById<TextView>(R.id.main_text).text = hello()
        setContentView(R.layout.activity_main)
        findViewById<TextView>(R.id.main_text).text = hello()

        main_text.setOnClickListener {

            Api.instance.getNewsResult(dataAction = {
                Log.i("KilleTom-Ypz", it.first().toString())
            }, errorAction = {
                it.printStackTrace()
            })
        }
    }
}