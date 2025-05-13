package com.example.fittrackerapp.uielements.exercise

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.example.fittrackerapp.App
import com.example.fittrackerapp.entities.ExerciseRepository
import com.example.fittrackerapp.ui.theme.FitTrackerAppTheme

class ExerciseActivity: ComponentActivity() {

    lateinit var viewModel: ExerciseViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            FitTrackerAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }

        val exerciseId = intent.getLongExtra("exerciseId", 0)

        val app = application as App

        val exerciseRepository = ExerciseRepository(app.appDatabase.exerciseDao())

        val factory = ExerciseViewModelFactory(exerciseId, exerciseRepository)

        viewModel = ViewModelProvider(this, factory).get(ExerciseViewModel::class.java)
    }
}

@Composable
fun MainScreen(modifier: Modifier) {

}