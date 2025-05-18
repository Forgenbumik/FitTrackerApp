package com.example.fittrackerapp.uielements.completedworkout

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.fittrackerapp.ui.theme.FitTrackerAppTheme
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fittrackerapp.R
import com.example.fittrackerapp.entities.CompletedExercise
import com.example.fittrackerapp.uielements.FileIcon
import com.example.fittrackerapp.uielements.completedexercise.CompletedExerciseActivity
import java.io.File
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.size
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.ui.text.font.FontWeight
import com.example.fittrackerapp.uielements.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CompletedWorkoutActivity: ComponentActivity()  {
    private val viewModel: CompletedWorkoutViewModel by viewModels()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FitTrackerAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(modifier = Modifier.padding(innerPadding),
                               onExerciseClick = { exercise, exerciseName -> onExerciseClick(exercise, exerciseName) },
                               formatTime = ::formatTime, onBackClick = ::onBackPressed)
                }
            }
        }
    }

    fun onExerciseClick(completedExercise: CompletedExercise, exerciseName: String) {
        val intent = Intent(this, CompletedExerciseActivity::class.java).apply {
            putExtra("completedExerciseId", completedExercise.id)
            putExtra("exerciseName", exerciseName)
        }
        startActivity(intent)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun formatTime(secs: Int): String {
        return viewModel.formatTime(secs)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    formatTime: (Int) -> String,
    onExerciseClick: (CompletedExercise, String) -> Unit,
    viewModel: CompletedWorkoutViewModel = viewModel(),
    onBackClick: () -> Unit
) {
    val completedExercises = viewModel.completedExercises.collectAsState().value

    val completedWorkout = viewModel.completedWorkout.collectAsState().value

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            IconButton(onClick = { onBackClick() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Назад",
                    tint = Color(0xFF1B9AAA)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            WorkoutInformation()
            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF1A1A1A)) // Темнее основного фона
                    .padding(12.dp)
            ) {
                ExercisesList(completedExercises, onExerciseClick)
            }
            completedWorkout.notes?.let { NameField(it) }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun WorkoutInformation(viewModel: CompletedWorkoutViewModel = viewModel()) {
    val completedWorkout = viewModel.completedWorkout.collectAsState().value
    val completedExercises = viewModel.completedExercises.collectAsState().value

    val formattedTime = viewModel.formatTime(completedWorkout.duration)
    val totalExercises = completedExercises.size

    val totalSets = remember { mutableStateOf(0) }
    val totalReps = remember { mutableStateOf(0) }

    LaunchedEffect(completedExercises) {
        totalSets.value = completedExercises.sumOf {
            viewModel.getExerciseSetsNumber(it.id)
        }

        totalReps.value = completedExercises.sumOf {
            viewModel.getExerciseTotalReps(it.id)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF015965))
            .padding(12.dp)
    ) {
        Text("Общее время: $formattedTime", fontSize = 16.sp, color = Color.White)
        Text("Всего упражнений: $totalExercises", fontSize = 16.sp, color = Color.White)
        Text("Всего подходов: ${totalSets.value}", fontSize = 16.sp, color = Color.White)
        Text("Всего повторений: ${totalReps.value}", fontSize = 16.sp, color = Color.White)
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ExercisesList(
    completedExercises: List<CompletedExercise>,
    onExerciseClick: (CompletedExercise, String) -> Unit
) {
    LazyColumn {
        items(completedExercises) { exercise ->
            ExerciseItem(exercise, onExerciseClick)
            HorizontalDivider(color = Color.DarkGray, thickness = 1.dp)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("UnrememberedMutableState")
@Composable
fun ExerciseItem(
    completedExercise: CompletedExercise,
    onExerciseClick: (CompletedExercise, String) -> Unit,
    viewModel: CompletedWorkoutViewModel = viewModel()
) {
    val context = LocalContext.current
    val exerciseName = remember { mutableStateOf("") }
    val exerciseSetsNumber = remember { mutableStateOf(0) }
    val exerciseTotalReps = remember { mutableStateOf(0) }
    var exerciseIconPath by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(completedExercise.exerciseId) {
        exerciseName.value = viewModel.getExerciseName(completedExercise.exerciseId)
        exerciseSetsNumber.value = viewModel.getExerciseSetsNumber(completedExercise.id)
        exerciseTotalReps.value = viewModel.getExerciseTotalReps(completedExercise.id)
        exerciseIconPath = viewModel.getIconPathByCompleted(completedExercise)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth().background(Color(0xFF015965))
            .padding(vertical = 8.dp)
            .clickable { onExerciseClick(completedExercise, exerciseName.value) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (exerciseIconPath != null) {
            val file = File(context.filesDir, exerciseIconPath)
            FileIcon(file)
            Spacer(modifier = Modifier.width(12.dp))
        }
        else {
            Image(
                painter = painterResource(id = R.drawable.ic_exercise_default), // Здесь используем идентификатор ресурса
                contentDescription = "Иконка упражнения",
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(exerciseName.value, fontSize = 18.sp, color = Color.White, fontWeight = FontWeight.Bold)
            Text("Длительность: ${viewModel.formatTime(completedExercise.duration)}", color = Color.LightGray)
            Text("${exerciseSetsNumber.value} подходов, ${exerciseTotalReps.value} повторений", color = Color.LightGray)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NameField(notes: String, viewModel: CompletedWorkoutViewModel = viewModel()) {
    TextField(
        value = notes,
        onValueChange = { viewModel.setWorkoutNotes(it) },
        label = { Text("Название тренировки") },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color(0xFF1E1E2E),
            unfocusedContainerColor = Color(0xFF2A2A3A),
            focusedLabelColor = Color(0xFF1B9AAA),
            unfocusedLabelColor = Color.Gray,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            cursorColor = Color(0xFF1B9AAA)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    )
}