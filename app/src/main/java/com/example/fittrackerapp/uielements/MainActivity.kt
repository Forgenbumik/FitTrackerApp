package com.example.fittrackerapp.uielements

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.fittrackerapp.abstractclasses.BaseWorkout
import com.example.fittrackerapp.ui.theme.FitTrackerAppTheme
import com.example.fittrackerapp.viewmodels.MainScreenModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fittrackerapp.App
import com.example.fittrackerapp.abstractclasses.repositories.CompletedWorkoutsAndExercisesRepository
import com.example.fittrackerapp.abstractclasses.repositories.WorkoutsAndExercisesRepository

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: MainScreenModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FitTrackerAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(
                        modifier = Modifier.padding(innerPadding),
                        onWorkoutClick = { workout -> onWorkoutClick(workout) }
                    )
                }
            }
        }

        val app = application as App
        val workoutsAndExercisesRepository = WorkoutsAndExercisesRepository(app.appDatabase.workoutsAndExercisesDao())
        val completedWorkoutsAndExercisesRepository = CompletedWorkoutsAndExercisesRepository(app.appDatabase.completedWorkoutsAndExercisesDao())

        viewModel = MainScreenModel(workoutsAndExercisesRepository, completedWorkoutsAndExercisesRepository)
    }

    fun onWorkoutClick(workout: BaseWorkout) {
        when (workout.type) {
            "Workout" -> {
                val intent = Intent(this, WorkoutActivity::class.java).apply {
                    putExtra("workoutId", workout.id)
                }
                startActivity(intent)
            }
        }
    }
}

@Composable
fun MainScreen(
    viewModel: MainScreenModel = viewModel(),
    modifier: Modifier,
    onWorkoutClick: (BaseWorkout) -> Unit) {

    val workouts = viewModel.favouriteWorkoutsList.collectAsState()
    val lastCompleted = viewModel.favouriteWorkoutsList.collectAsState()

    Text("История тренировок")
    WorkoutList(workouts.value, onWorkoutClick)
}

@Composable
fun WorkoutList(workoutList: List<BaseWorkout>, onClick: (BaseWorkout) -> Unit) {
    for (workout in workoutList) {
        WorkoutItem(workout, onClick)
        HorizontalDivider()
    }
}

@Composable
fun WorkoutItem(workout: BaseWorkout, onClick: (BaseWorkout) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable { onClick(workout) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(workout.name)
    }
}

