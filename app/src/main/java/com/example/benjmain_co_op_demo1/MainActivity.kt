package com.example.benjmain_co_op_demo1

import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.snackbar.Snackbar
import java.net.URL


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        // - A box where you type/paste the picture web address (urlInput)
        // - A button you press to start (loadBtn)
        // - A big area where the picture will appear (imageView)
        val urlInput: EditText = findViewById(R.id.urlInput)
        val loadBtn: Button = findViewById(R.id.loadBtn)
        val imageView: ImageView = findViewById(R.id.imageView)

        // When the button is tapped:
        loadBtn.setOnClickListener {
            val url = urlInput.text.toString().trim()
            if (url.isEmpty()) {
                // raises error: please type a web address first
                Snackbar.make(findViewById(R.id.main), R.string.error_enter_url, Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            /* We do the internet work in the background so the app stays responsive.
               Think of this as a helper doing the download while the main screen stays smooth. */
            Thread(Runnable {
                val result = runCatching {
                    /* Open a pipe to the picture on the internet.
                       This pipe gives us the raw picture bytes (data). */
                    URL(url).openStream().use { input ->
                        /* Turn those raw bytes into a picture object we can show on screen. */
                        BitmapFactory.decodeStream(input) ?: error("Decode failed")
                    }
                }

                // Now we go back to the main screen to show the result.
                runOnUiThread {
                    result.fold(
                        onSuccess = { bmp ->
                            // Success: place the picture into the ImageView so everyone can see it.
                            imageView.setImageBitmap(bmp)
                            Snackbar.make(findViewById(R.id.main), R.string.msg_image_loaded, Snackbar.LENGTH_SHORT).show()
                        },
                        onFailure = { e ->
                            // If anything failed
                            // show a short, clear message at the bottom.
                            Snackbar.make(
                                findViewById(R.id.main),
                                getString(R.string.error_failed, e.message ?: ""),
                                Snackbar.LENGTH_LONG
                            ).show()
                        }
                    )
                }
            }).start()
        }
    }
}