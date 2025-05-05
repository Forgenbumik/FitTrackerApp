package com.example.fittrackerapp.uielements.main

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import com.example.fittrackerapp.ui.theme.FitTrackerAppTheme
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fittrackerapp.App
import com.example.fittrackerapp.abstractclasses.BaseWorkout
import com.example.fittrackerapp.abstractclasses.repositories.WorkoutsAndExercisesRepository
import com.example.fittrackerapp.entities.LastWorkout
import com.example.fittrackerapp.entities.LastWorkoutRepository
import com.example.fittrackerapp.entities.Workout
import com.example.fittrackerapp.uielements.completedworkout.CompletedWorkoutActivity
import com.example.fittrackerapp.uielements.usedworkouts.UsedWorkoutsActivity
import com.example.fittrackerapp.uielements.workout.WorkoutActivity
import com.example.fittrackerapp.uielements.completedexercise.CompletedExerciseActivity
import com.example.fittrackerapp.uielements.exercise.ExerciseActivity

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: MainScreenViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FitTrackerAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    CompletedExerciseMainScreen(
                        modifier = Modifier.padding(innerPadding),
                        onFavouriteWorkoutClick = { workout -> onFavouriteWorkoutClick(workout) },
                        onLastWorkoutClick = { workout -> onLastWorkoutClick(workout) },
                        onAllWorkoutsClick = { onUsedWorkoutsClick() }
                    )
                }
            }
        }

        val app = application as App

        val factory = MainScreenModelFactory(
            WorkoutsAndExercisesRepository(
                app.appDatabase.workoutDao(),
                app.appDatabase.exerciseDao()),
            LastWorkoutRepository(
                app.appDatabase.lastWorkoutDao(),
                app.appDatabase.workoutDao(),
                app.appDatabase.exerciseDao()
            )
        )

        viewModel = ViewModelProvider(this, factory).get(MainScreenViewModel::class.java)
    }

    fun onFavouriteWorkoutClick(workout: BaseWorkout) {
        when (workout is Workout) {
            true -> {
                val intent = Intent(this, WorkoutActivity::class.java).apply {
                    putExtra("workoutId", workout.id)
                    putExtra("workoutName", workout.name)
                }
                startActivity(intent)
            }

            false -> {
                val intent = Intent(this, ExerciseActivity::class.java).apply {
                    putExtra("exerciseId", workout.id)
                    putExtra("exerciseName", workout.name)
                }
                startActivity(intent)
            }
        }
    }

    fun onLastWorkoutClick(workout: LastWorkout) {
        when (workout.typeId) {
            1 -> {
                val intent = Intent(this, CompletedWorkoutActivity::class.java).apply {
                    putExtra("completedWorkoutId", workout.completedWorkoutId)
                }
                startActivity(intent)
            }
            2 -> {
                val intent = Intent(this, CompletedExerciseActivity::class.java).apply {
                    putExtra("exerciseId", workout.completedWorkoutId)
                }
                startActivity(intent)
            }
        }
    }

    fun onUsedWorkoutsClick() {
        val intent = Intent(this, UsedWorkoutsActivity::class.java)
        startActivity(intent)
    }
}

@Composable
fun CompletedExerciseMainScreen(
    viewModel: MainScreenViewModel = viewModel(),
    modifier: Modifier,
    onFavouriteWorkoutClick: (BaseWorkout) -> Unit,
    onLastWorkoutClick: (LastWorkout) -> Unit,
    onAllWorkoutsClick: () -> Unit
) {
    val last_workouts = viewModel.lastWorkouts.collectAsState().value

    Column(
        modifier = modifier.fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        Text("Избранные тренировки")
        FavouriteWorkoutList(onFavouriteWorkoutClick, onAllWorkoutsClick)
        LastWorkoutList(last_workouts, onLastWorkoutClick)
    }
}

@Composable
fun FavouriteWorkoutList(
    onFavouriteWorkoutClick: (BaseWorkout) -> Unit,
    onAllWorkoutsClick: () -> Unit,
    viewModel: MainScreenViewModel = viewModel()
) {

    val workouts = viewModel.favouriteWorkouts.collectAsState().value

    val modifier = Modifier
        .padding(16.dp)
    LazyColumn(modifier = Modifier.fillMaxWidth()) {
        items(workouts) { workout ->
            FavouriteWorkoutItem(modifier = Modifier
                .fillMaxWidth().padding(8.dp),
                workout, onFavouriteWorkoutClick)
            HorizontalDivider()
        }
    }
    OtherWorkouts(modifier, onAllWorkoutsClick)
}

@Composable
fun FavouriteWorkoutItem(modifier: Modifier, workout: BaseWorkout, onClick: (BaseWorkout) -> Unit) {
    Row(
        modifier = modifier
            .clickable { onClick(workout) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(workout.name)
    }
}

@Composable
fun OtherWorkouts(
    modifier: Modifier,
    onClick: () -> Unit
    )
    {
        Row(
            modifier = modifier
                .clickable { onClick() },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Ещё")
        }
}

@Composable
fun LastWorkoutList(workoutList: List<LastWorkout>, onClick: (LastWorkout) -> Unit) {
    for (workout in workoutList) {
        LastWorkoutItem(workout, onClick)
        HorizontalDivider()
    }
}

@Composable
fun LastWorkoutItem(workout: LastWorkout, onClick: (LastWorkout) -> Unit, viewModel: MainScreenViewModel = viewModel()) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable { onClick(workout) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(workout.workoutName)
        Text(viewModel.formatTime(workout.duration))
    }
}