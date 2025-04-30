package com.example.fittrackerapp.uielements.completedexercise

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fittrackerapp.App
import com.example.fittrackerapp.entities.CompletedExercise
import com.example.fittrackerapp.entities.CompletedExerciseRepository
import com.example.fittrackerapp.ui.theme.FitTrackerAppTheme
import com.example.fittrackerapp.entities.Set
import com.example.fittrackerapp.entities.SetRepository
import com.example.fittrackerapp.uielements.executingworkout.SetsTable

class CompletedExerciseActivity: ComponentActivity() {

    private lateinit var viewModel: CompletedExerciseViewModel

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FitTrackerAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    CompletedExerciseMainScreen(modifier = Modifier.padding(innerPadding),
                        formatTime = {secs -> formatTime(secs)},
                        setChangingSet = {set -> setChangingSet(set)})
                }
            }
        }

        val app = application as App

        val completedExerciseId = intent.getLongExtra("completedExerciseId", -1)
        val exerciseName = intent.getStringExtra("exerciseName")

        val completedExerciseRepository = CompletedExerciseRepository(app.appDatabase.completedExerciseDao())
        val setRepository = SetRepository(app.appDatabase.setDao())

        val factory = CompletedExerciseViewModelFactory(completedExerciseId, completedExerciseRepository, setRepository)

        viewModel = ViewModelProvider(this, factory).get(CompletedExerciseViewModel::class.java)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun formatTime(secs: Int): String {
        return viewModel.formatTime(secs)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun setChangingSet(set: Set) {
        viewModel.setChangingSet(set)
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CompletedExerciseMainScreen(modifier: Modifier, viewModel: CompletedExerciseViewModel = viewModel(), formatTime: (Int) -> String, setChangingSet: (Set) -> Unit) {

    val completedExercise = viewModel.completedExercise.collectAsState().value
    val setList = viewModel.setList


    val changingSet = viewModel.changingSet.collectAsState()

    ExerciseInformation(completedExercise)
    SetsTable(setList, formatTime, setChangingSet = setChangingSet, changingSet = changingSet)
}

@Composable
fun ExerciseInformation(exercise: CompletedExercise) {
    Text("Подходов: ${exercise.setsNumber}. Всего повторений: ${exercise.totalReps}")
}