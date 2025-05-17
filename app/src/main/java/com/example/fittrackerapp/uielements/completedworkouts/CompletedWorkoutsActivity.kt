package com.example.fittrackerapp.uielements.completedworkouts

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.example.fittrackerapp.App
import com.example.fittrackerapp.entities.ExerciseRepository
import com.example.fittrackerapp.entities.WorkoutDetailRepository
import com.example.fittrackerapp.entities.WorkoutRepository
import com.example.fittrackerapp.ui.theme.FitTrackerAppTheme
import com.example.fittrackerapp.uielements.creatingworkout.CreatingWorkoutViewModelFactory
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fittrackerapp.abstractclasses.BaseCompletedWorkout
import com.example.fittrackerapp.abstractclasses.repositories.CompletedWorkoutsAndExercisesRepository
import com.example.fittrackerapp.entities.CompletedWorkoutRepository
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fittrackerapp.entities.CompletedExercise
import com.example.fittrackerapp.entities.CompletedExerciseRepository
import com.example.fittrackerapp.entities.CompletedWorkout
import com.example.fittrackerapp.ui.theme.DarkBlue
import com.example.fittrackerapp.ui.theme.LightTeal
import com.example.fittrackerapp.ui.theme.Teal
import com.example.fittrackerapp.uielements.completedexercise.CompletedExerciseActivity
import com.example.fittrackerapp.uielements.completedworkout.CompletedWorkoutActivity
import com.example.fittrackerapp.uielements.main.MainActivity

class CompletedWorkoutsActivity: ComponentActivity() {

    private lateinit var viewModel: CompletedWorkoutsViewModel

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            FitTrackerAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(Modifier.padding(innerPadding),
                        onCompletedWorkoutClick = { baseCompletedWorkout -> onCompletedWorkoutClick(baseCompletedWorkout)}, onBackClick ={onBackClick()})
                }
            }
        }

        val app = application as App

        val completedWorkoutRepository = CompletedWorkoutRepository(app.appDatabase.completedWorkoutDao())
        val completedWorkoutsAndExercisesRepository = CompletedWorkoutsAndExercisesRepository(app.appDatabase.lastWorkoutDao(), app.appDatabase.completedWorkoutDao(), app.appDatabase.completedExerciseDao())
        val completedExerciseRepository = CompletedExerciseRepository(app.appDatabase.completedExerciseDao())

        val factory = CompletedWorkoutsViewModelFactory(completedWorkoutRepository, completedWorkoutsAndExercisesRepository, completedExerciseRepository)

        viewModel = ViewModelProvider(this, factory).get(CompletedWorkoutsViewModel::class.java)
    }

    fun onCompletedWorkoutClick(baseCompletedWorkout: BaseCompletedWorkout) {
        if (baseCompletedWorkout is CompletedWorkout) {
            val intent = Intent(this, CompletedWorkoutActivity::class.java)
            intent.putExtra("completedWorkoutId", baseCompletedWorkout.id)
            startActivity(intent)
        }
        else if (baseCompletedWorkout is CompletedExercise) {
            val intent = Intent(this, CompletedExerciseActivity::class.java)
            intent.putExtra("completedExerciseId", baseCompletedWorkout.id)
            startActivity(intent)
        }
    }

    fun onBackClick() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}

@Composable
fun TopBar(onBackClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1F1F1F)) // Тёмно-серый
            .padding(horizontal = 12.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Назад",
                tint = Color.White // Белый цвет для иконки
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Завершённые тренировки",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    viewModel: CompletedWorkoutsViewModel = viewModel(),
    onBackClick: () -> Unit,
    onCompletedWorkoutClick: (BaseCompletedWorkout) -> Unit
) {
    val completedWorkouts = viewModel.completedWorkouts.collectAsState().value

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBlue)
            .padding(16.dp)
    ) {
        TopBar(onBackClick)

        Spacer(modifier = Modifier.height(16.dp))

        // Список тренировок
        CompletedWorkoutsList(
            completedWorkouts = completedWorkouts,
            onCompletedWorkoutClick = onCompletedWorkoutClick,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun CompletedWorkoutsList(
    modifier: Modifier = Modifier,
    completedWorkouts: List<BaseCompletedWorkout>,
    onCompletedWorkoutClick: (BaseCompletedWorkout) -> Unit
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(completedWorkouts) { completedWorkout ->
            CompletedWorkoutItem(
                completedWorkout = completedWorkout,
                onCompletedWorkoutClick = onCompletedWorkoutClick
            )
        }
    }
}

@Composable
fun CompletedWorkoutItem(
    modifier: Modifier = Modifier,
    completedWorkout: BaseCompletedWorkout,
    viewModel: CompletedWorkoutsViewModel = viewModel(),
    onCompletedWorkoutClick: (BaseCompletedWorkout) -> Unit
) {


    val workoutName = remember { mutableStateOf("") }

    LaunchedEffect(completedWorkout) {
        workoutName.value = viewModel.getWorkoutName(completedWorkout)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onCompletedWorkoutClick(completedWorkout) },
        colors = CardDefaults.cardColors(containerColor = Teal),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = workoutName.value,
                color = Color.White,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}