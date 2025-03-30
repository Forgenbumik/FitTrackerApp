package com.example.fittrackerapp.uielements

import android.content.Intent
import androidx.activity.ComponentActivity

class ResultsActivity: ComponentActivity() {

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP // Закрывает все промежуточные активити
        startActivity(intent)
        finish() // Завершаем текущую Activity
    }

}