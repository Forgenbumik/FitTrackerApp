package com.example.fittrackerapp.uielements.workout

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fittrackerapp.App
import com.example.fittrackerapp.entities.WorkoutDetail
import com.example.fittrackerapp.entities.WorkoutDetailRepository
import com.example.fittrackerapp.entities.WorkoutRepository
import com.example.fittrackerapp.service.WorkoutRecordingService
import com.example.fittrackerapp.ui.theme.FitTrackerAppTheme
import com.example.fittrackerapp.uielements.executingworkout.ExecutingWorkoutActivity
import com.example.fittrackerapp.uielements.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WorkoutActivity: ComponentActivity() {

    private val viewModel: WorkoutViewModel by viewModels()

    var workoutId = 0L
    var workoutName = ""

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FitTrackerAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(modifier = Modifier.padding(innerPadding), workoutName, ::onBackClick, ::onExerciseClick)
                }
            }
        }
        workoutId = intent.getLongExtra("workoutId", -1)
        workoutName = intent.getStringExtra("workoutName") ?: "Тренировка"
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun onExerciseClick(workoutDetail: WorkoutDetail) {
        val serviceIntent = Intent(this, WorkoutRecordingService::class.java).apply {
            putExtra("workoutId", workoutId)
            putExtra("workoutName", workoutName)
            putExtra("detailId", workoutDetail.id)
            putExtra("exerciseName", workoutDetail.exerciseName)
        }
        startForegroundService(serviceIntent)
        val intent = Intent(this, ExecutingWorkoutActivity::class.java).apply {
            putExtra("workoutId", workoutId)
            putExtra("workoutName", workoutName)
            putExtra("detailId", workoutDetail.id)
            putExtra("exerciseName", workoutDetail.exerciseName)
        }
        startActivity(intent)
    }

    fun onBackClick() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    workoutName: String,
    onBackClick: () -> Unit,
    onExerciseClick: (WorkoutDetail) -> Unit,
    viewModel: WorkoutViewModel = viewModel()
) {
    val exercises = viewModel.exercisesList.collectAsState()

    Column(
        modifier = modifier
            .background(Color(0xFF121212)) // Тёмный фон
            .fillMaxSize()
            .padding(WindowInsets.statusBars.asPaddingValues())
    ) {
        TopBar(workoutName, onBackClick)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Упражнения",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
            modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp)
        )
        ExerciseList(exercises.value, onExerciseClick)
    }
}

@Composable
fun TopBar(title: String, onBackClick: () -> Unit) {
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
            text = title,
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ExerciseList(
    exerciseList: List<WorkoutDetail>,
    onExerciseClick: (WorkoutDetail) -> Unit
) {
    Column(
        modifier = Modifier
            .padding(horizontal = 12.dp)
    ) {
        exerciseList.forEach { exercise ->
            ExerciseItem(exercise, onExerciseClick)
            HorizontalDivider(color = Color.Gray.copy(alpha = 0.3f))
        }
    }
}

@Composable
fun ExerciseItem(
    workoutDetail: WorkoutDetail,
    onClick: (WorkoutDetail) -> Unit,
    viewModel: WorkoutViewModel = viewModel()
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
            .background(Color(0xFF2C2C2C), shape = RoundedCornerShape(12.dp)) // Темный фон для элемента
            .padding(12.dp)
    ) {
        Text(
            text = workoutDetail.exerciseName,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White // Белый цвет для текста
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Подходов: ${workoutDetail.setsNumber}, ", color = Color.White)
            Text("Повторений: ${workoutDetail.reps}, ", color = Color.White)
            val restText = if (workoutDetail.isRestManually) {
                "отдых: вручную"
            } else {
                "отдых: ${viewModel.FormatTime(workoutDetail.restDuration)}"
            }
            Text(restText, color = Color.White)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = { onClick(workoutDetail) },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF015965))
        ) {
            Text("Запустить", color = Color.White)
        }
    }
}