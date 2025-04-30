package com.example.fittrackerapp.uielements.allworkouts

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fittrackerapp.App
import com.example.fittrackerapp.entities.Exercise
import com.example.fittrackerapp.entities.ExerciseRepository
import com.example.fittrackerapp.ui.theme.FitTrackerAppTheme
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.State

class AllExercisesActivity: ComponentActivity() {
    private lateinit var viewModel: AllExercisesViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            FitTrackerAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AllWorkoutsMainScreen(modifier = Modifier.padding(innerPadding), onExerciseClick = { exercise -> onExerciseClick(exercise)})
                }
            }
        }

        val app = application as App

        val exerciseRepository = ExerciseRepository(app.appDatabase.exerciseDao())

        val factory = AllExercisesViewModelFactory(exerciseRepository)

        viewModel = ViewModelProvider(this, factory).get(AllExercisesViewModel::class.java)
    }

    fun onExerciseClick(exercise: Exercise) {
        viewModel.addExercisetoUsed(exercise)
        finish()
    }

    fun onСonfirm() {

    }
}

@Composable
fun AllWorkoutsMainScreen(modifier: Modifier, onExerciseClick: (Exercise) -> Unit, viewModel: AllExercisesViewModel = viewModel()) {
    val exercisesList = viewModel.exercisesList.collectAsState()
    ExercisesList(exercisesList, onExerciseClick = onExerciseClick)
}

@Composable
fun ExercisesList(exercises: State<List<Exercise>>, onExerciseClick: (Exercise) -> Unit) {

    val exercisesValue = exercises.value

    LazyColumn {
        items(exercisesValue) { exercise ->
            ExerciseItem(exercise, onExerciseClick)
        }
    }
}

@Composable
fun ExerciseItem(exercise: Exercise, onExerciseClick: (Exercise) -> Unit) {
    Column(modifier = Modifier.clickable { onExerciseClick(exercise) }) {
        Text(exercise.name)
        HorizontalDivider()
    }
}