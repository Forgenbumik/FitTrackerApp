package com.example.fittrackerapp.uielements.allworkouts

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
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
import com.example.fittrackerapp.abstractclasses.BaseWorkout
import com.example.fittrackerapp.abstractclasses.repositories.WorkoutsAndExercisesRepository
import com.example.fittrackerapp.entities.Workout

class AllExercisesActivity: ComponentActivity() {
    private lateinit var viewModel: AllExercisesViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            FitTrackerAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(modifier = Modifier.padding(innerPadding), onExerciseClick = { exercise -> onExerciseClick(exercise)})
                }
            }
        }

        val app = application as App

        val reason = intent.getStringExtra("reason")

        val exerciseRepository = ExerciseRepository(app.appDatabase.exerciseDao())

        val workoutsAndExercisesRepository = WorkoutsAndExercisesRepository(app.appDatabase.workoutDao(), app.appDatabase.exerciseDao(), app.appDatabase.workoutDetailDao())

        val factory = AllExercisesViewModelFactory(reason, exerciseRepository, workoutsAndExercisesRepository)

        viewModel = ViewModelProvider(this, factory).get(AllExercisesViewModel::class.java)
    }

    fun onExerciseClick(baseWorkout: BaseWorkout) {
        val resultIntent = Intent()
        resultIntent.putExtra("exerciseId", baseWorkout.id)
        if (baseWorkout is Workout) {
            resultIntent.putExtra("typeId", 1)
        }
        else {
            resultIntent.putExtra("typeId", 2)
        }
        setResult(RESULT_OK, resultIntent)
        finish()
    }
}

@Composable
fun MainScreen(modifier: Modifier, onExerciseClick: (BaseWorkout) -> Unit, viewModel: AllExercisesViewModel = viewModel()) {
    val exercisesList = viewModel.exercisesList.collectAsState()
    Column(
        modifier.windowInsetsPadding(WindowInsets.statusBars)
    ) {
        ExercisesList(exercisesList, onExerciseClick = onExerciseClick)
    }
}

@Composable
fun ExercisesList(exercises: State<List<BaseWorkout>>, onExerciseClick: (BaseWorkout) -> Unit) {

    val exercisesValue = exercises.value

    LazyColumn {
        items(exercisesValue) { exercise ->
            WorkoutItem(exercise, onExerciseClick)
        }
    }
}

@Composable
fun WorkoutItem(baseWorkout: BaseWorkout, onExerciseClick: (BaseWorkout) -> Unit) {
    Column(modifier = Modifier.clickable { onExerciseClick(baseWorkout) }) {
        Text(baseWorkout.name)
        HorizontalDivider()
    }
}