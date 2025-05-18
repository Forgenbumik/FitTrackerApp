package com.example.fittrackerapp.uielements.main

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import com.example.fittrackerapp.ui.theme.FitTrackerAppTheme
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fittrackerapp.App
import com.example.fittrackerapp.R
import com.example.fittrackerapp.abstractclasses.BaseWorkout
import com.example.fittrackerapp.abstractclasses.repositories.WorkoutsAndExercisesRepository
import com.example.fittrackerapp.entities.CompletedExerciseRepository
import com.example.fittrackerapp.entities.Exercise
import com.example.fittrackerapp.entities.LastWorkout
import com.example.fittrackerapp.entities.LastWorkoutRepository
import com.example.fittrackerapp.entities.Workout
import com.example.fittrackerapp.uielements.FileIcon
import com.example.fittrackerapp.uielements.completedworkout.CompletedWorkoutActivity
import com.example.fittrackerapp.uielements.usedworkouts.UsedWorkoutsActivity
import com.example.fittrackerapp.uielements.workout.WorkoutActivity
import com.example.fittrackerapp.uielements.completedexercise.CompletedExerciseActivity
import com.example.fittrackerapp.uielements.completedworkouts.CompletedWorkoutsActivity
import com.example.fittrackerapp.uielements.exercise.ExerciseActivity
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainScreenViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FitTrackerAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(
                        modifier = Modifier.padding(innerPadding),
                        onFavouriteWorkoutClick = { workout -> onFavouriteWorkoutClick(workout) },
                        onLastWorkoutClick = { workout -> onLastWorkoutClick(workout) },
                        onAllWorkoutsClick = { onUsedWorkoutsClick()},
                        onAllCompletedClick = { onAllCompletedClick() }
                    )
                }
            }
        }
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

    fun onAllCompletedClick() {
        val intent = Intent(this, CompletedWorkoutsActivity::class.java)
        startActivity(intent)
    }
}

@Composable
fun MainScreen(
    viewModel: MainScreenViewModel = viewModel(),
    modifier: Modifier,
    onFavouriteWorkoutClick: (BaseWorkout) -> Unit,
    onLastWorkoutClick: (LastWorkout) -> Unit,
    onAllWorkoutsClick: () -> Unit,
    onAllCompletedClick: () -> Unit
) {
    val lastWorkouts = viewModel.lastWorkouts.collectAsState().value

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0D1B2A))
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(16.dp)
    ) {
        Text(
            text = "Избранные тренировки",
            color = Color(0xFF00B4D8),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        FavouriteWorkoutList(onFavouriteWorkoutClick, onAllWorkoutsClick)
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Последние тренировки",
            color = Color(0xFF00B4D8),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        LastWorkoutList(lastWorkouts, onLastWorkoutClick, onAllCompletedClick)
    }
}

@Composable
fun FavouriteWorkoutList(
    onFavouriteWorkoutClick: (BaseWorkout) -> Unit,
    onAllWorkoutsClick: () -> Unit,
    viewModel: MainScreenViewModel = viewModel()
) {
    val workouts = viewModel.favouriteWorkouts.collectAsState().value

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        color = Color(0xFF1B263B), // блок светлее фона
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(workouts) { workout ->
                    FavouriteWorkoutItem(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        workout = workout,
                        onClick = onFavouriteWorkoutClick
                    )
                    HorizontalDivider(color = Color.DarkGray)
                }
            }
            OtherWorkouts(Modifier.align(Alignment.End), onAllWorkoutsClick)
        }
    }
}

@Composable
fun FavouriteWorkoutItem(modifier: Modifier, workout: BaseWorkout, onClick: (BaseWorkout) -> Unit) {
    val context = LocalContext.current
    Row(
        modifier = modifier.clickable { onClick(workout) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (workout is Exercise && workout.iconPath != null) {
            FileIcon(File(context.filesDir, workout.iconPath))
        } else {
            Image(
                painter = painterResource(id = R.drawable.ic_exercise_default),
                contentDescription = "Иконка упражнения",
                modifier = Modifier.size(36.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Text(
            text = workout.name,
            color = Color.White
        )
    }
}

@Composable
fun OtherWorkouts(modifier: Modifier, onClick: () -> Unit) {
    Row(
        modifier = modifier.clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Ещё", color = Color(0xFF00B4D8), fontWeight = FontWeight.Medium)
    }
}

@Composable
fun LastWorkoutList(workoutList: List<LastWorkout>, onClick: (LastWorkout) -> Unit, onAllCompletedClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFF1B263B),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {

            if (workoutList.isEmpty()) {
                Text("Здесь будут ваши последние тренировки")
            }
            else {
                workoutList.forEach { workout ->
                    LastWorkoutItem(workout, onClick)
                    HorizontalDivider(color = Color.DarkGray)
                }
                Row(modifier = Modifier.align(Alignment.End)
                    .clickable {
                        onAllCompletedClick()
                    }) {
                    Text("Ещё", color = Color(0xFF00B4D8), fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
fun LastWorkoutItem(
    workout: LastWorkout,
    onClick: (LastWorkout) -> Unit,
    viewModel: MainScreenViewModel = viewModel()
) {
    var iconPath by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(workout) {
        iconPath = viewModel.getIconPathByCompleted(workout)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(workout) }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (iconPath != null) {
            FileIcon(File(LocalContext.current.filesDir, iconPath!!))
        } else {
            Image(
                painter = painterResource(id = R.drawable.ic_exercise_default),
                contentDescription = "Иконка упражнения",
                modifier = Modifier.size(36.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(
                text = workout.workoutName,
                color = Color.White
            )
            Text(
                text = viewModel.formatTime(workout.duration),
                color = Color(0xFFB0C4DE),
                fontSize = 12.sp
            )
        }
    }
}