package com.itamarstern.cropview

import android.graphics.Rect
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.itamarstern.cropview.databinding.ActivityMainBinding

const val TAG = "CropViewTag"

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onResume() {
        super.onResume()

        binding.button.setOnClickListener {
            with(binding.cropView){
                setCropWidthDp(200)
                setCropHeightDp(200)
                Log.d(TAG, "${getCropRect()}")
            }
        }

        binding.cropView.setOnSizeChangeListener(object : OnSizeChangeListener{
            override fun onSizeChange(rect: Rect, width: Int, height: Int) {
                Log.d(TAG, "rect: $rect | width: $width | height: $height")
            }
        })
    }
}