package com.example.fittrackerapp.uielements.completedworkouts

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
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
import com.example.fittrackerapp.ui.theme.FitTrackerAppTheme
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fittrackerapp.abstractclasses.BaseCompletedWorkout
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fittrackerapp.entities.completedexercise.CompletedExercise
import com.example.fittrackerapp.entities.completedworkout.CompletedWorkout
import com.example.fittrackerapp.ui.theme.DarkBlue
import com.example.fittrackerapp.ui.theme.Teal
import com.example.fittrackerapp.uielements.completedexercise.CompletedExerciseActivity
import com.example.fittrackerapp.uielements.completedworkout.CompletedWorkoutActivity
import com.example.fittrackerapp.uielements.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CompletedWorkoutsActivity: ComponentActivity() {

    private val viewModel: CompletedWorkoutsViewModel by viewModels()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            FitTrackerAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(Modifier.padding(innerPadding),
                        onCompletedWorkoutClick = { baseCompletedWorkout, workoutName -> onCompletedWorkoutClick(baseCompletedWorkout, workoutName)}, onBackClick ={onBackClick()})
                }
            }
        }
    }

    fun onCompletedWorkoutClick(baseCompletedWorkout: BaseCompletedWorkout, workoutName: String) {
        if (baseCompletedWorkout is CompletedWorkout) {
            val intent = Intent(this, CompletedWorkoutActivity::class.java)
            intent.putExtra("completedWorkoutId", baseCompletedWorkout.id)
            intent.putExtra("workoutName", workoutName)
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

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    viewModel: CompletedWorkoutsViewModel = viewModel(),
    onBackClick: () -> Unit,
    onCompletedWorkoutClick: (BaseCompletedWorkout, String) -> Unit
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

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CompletedWorkoutsList(
    modifier: Modifier = Modifier,
    completedWorkouts: List<BaseCompletedWorkout>,
    onCompletedWorkoutClick: (BaseCompletedWorkout, String) -> Unit
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

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CompletedWorkoutItem(
    modifier: Modifier = Modifier,
    completedWorkout: BaseCompletedWorkout,
    viewModel: CompletedWorkoutsViewModel = viewModel(),
    onCompletedWorkoutClick: (BaseCompletedWorkout, String) -> Unit
) {
    val workoutName = remember { mutableStateOf("") }

    LaunchedEffect(completedWorkout) {
        workoutName.value = viewModel.getWorkoutName(completedWorkout)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onCompletedWorkoutClick(completedWorkout, workoutName.value) }
            .padding(vertical = 6.dp, horizontal = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Teal),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = workoutName.value,
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = completedWorkout.beginTime.toLocalDate().toString(),
                    color = Color.White.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = viewModel.formatTime(completedWorkout.duration),
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            IconButton(
                onClick = { viewModel.deleteCompletedWorkout(completedWorkout) }
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Удалить",
                    tint = Color.White
                )
            }
        }
    }
}