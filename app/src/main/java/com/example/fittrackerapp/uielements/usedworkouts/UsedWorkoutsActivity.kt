package com.example.fittrackerapp.uielements.usedworkouts

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import com.example.fittrackerapp.App
import com.example.fittrackerapp.abstractclasses.BaseWorkout
import com.example.fittrackerapp.abstractclasses.repositories.WorkoutsAndExercisesRepository
import com.example.fittrackerapp.entities.FavouriteWorkout
import com.example.fittrackerapp.entities.FavouriteWorkoutRepository
import com.example.fittrackerapp.ui.theme.FitTrackerAppTheme
import com.example.fittrackerapp.uielements.creatingworkout.CreatingWorkoutActivity

class UsedWorkoutsActivity: ComponentActivity() {
    private lateinit var viewModel: UsedWorkoutsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            FitTrackerAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    CompletedExerciseMainScreen(viewModel, Modifier.padding(innerPadding), ::onPlusClick,
                        ::addWorkoutToFavourites, ::deleteWorkoutFromFavourites)
                }
            }
        }

        val app = application as App

        val favouriteWorkoutRepository = FavouriteWorkoutRepository(app.appDatabase.favouriteWorkoutDao())

        val workoutRepository = WorkoutsAndExercisesRepository(app.appDatabase.workoutDao(), app.appDatabase.exerciseDao(), app.appDatabase.favouriteWorkoutDao())

        val factory = UsedWorkoutsModelFactory(favouriteWorkoutRepository, workoutRepository)

        viewModel = ViewModelProvider(this, factory).get(UsedWorkoutsViewModel::class.java)
    }

    fun onPlusClick() {
        val intent = Intent(this, CreatingWorkoutActivity::class.java)
        startActivity(intent)
    }

    fun addWorkoutToFavourites(workout: BaseWorkout) {
        if (!viewModel.addFavouriteWorkout(workout, 1)) {
            Toast.makeText(this, "Превышено допустимое количество избранных тренировок", Toast.LENGTH_SHORT).show()
        }
    }

    fun deleteWorkoutFromFavourites(workout: FavouriteWorkout) {
        viewModel.deleteFavouriteWorkout(workout)
    }
}

@Composable
fun CompletedExerciseMainScreen(viewModel: UsedWorkoutsViewModel, modifier: Modifier = Modifier,
                                onPlusClick: () -> Unit, AddFavouriteClick: (BaseWorkout) -> Unit,
                                DeleteFavouriteClick: (FavouriteWorkout) -> Unit) {
    UpperBar(onPlusClick)
    val favouriteWorkouts = viewModel.favouriteWorkouts.collectAsState().value
    val workouts = viewModel.workoutsList.collectAsState().value

    Column(modifier = modifier) {
        FavouriteWorkoutsList(favouriteWorkouts, DeleteFavouriteClick)
        AllWorkoutsList(workouts, AddFavouriteClick)
    }
}

@Composable
fun UpperBar(onPlusClick: () -> Unit) {
    Row {
        Button(modifier = Modifier,
            onClick = {
                onPlusClick()
            }) {
            Text("+",  modifier = Modifier, fontSize = 16.sp)
        }
    }
}

@Composable
fun FavouriteWorkoutsList(favouriteWorkouts: List<FavouriteWorkout>, DeleteFavouriteClick: (FavouriteWorkout) -> Unit) {
    Text("Избранные сценарии")
    LazyColumn(
        modifier = Modifier.fillMaxWidth()
    ) {
        items(favouriteWorkouts) { workout ->
            Row {
                Text(workout.workoutName)
                IconButton(onClick = { DeleteFavouriteClick(workout) }) {
                    Icon(
                        imageVector = Icons.Default.Delete, // Использует стандартную иконку добавления
                        contentDescription = "Add"
                    )
                }
            }
            HorizontalDivider()
        }
    }
}

@Composable
fun AllWorkoutsList(workouts: List<BaseWorkout>, AddFavouriteClick: (BaseWorkout) -> Unit) {
    Text("Все сценарии")
    Column {
        for (workout in workouts) {
            Row() {
                Text("${workout.name}")
                IconButton(onClick = { AddFavouriteClick(workout) }) {
                    Icon(
                        imageVector = Icons.Default.Add, // Использует стандартную иконку добавления
                        contentDescription = "Add"
                    )
                }
            }
            HorizontalDivider()
        }
    }
}