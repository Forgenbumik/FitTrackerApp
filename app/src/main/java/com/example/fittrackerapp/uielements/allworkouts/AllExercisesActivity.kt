package com.example.fittrackerapp.uielements.allworkouts

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fittrackerapp.entities.exercise.Exercise
import com.example.fittrackerapp.ui.theme.FitTrackerAppTheme
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fittrackerapp.abstractclasses.BaseWorkout
import com.example.fittrackerapp.entities.workout.Workout
import com.example.fittrackerapp.uielements.FileIcon
import java.io.File
import androidx.compose.foundation.layout.size
import androidx.compose.ui.res.painterResource
import com.example.fittrackerapp.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AllExercisesActivity: ComponentActivity() {
    private val viewModel: AllExercisesViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            FitTrackerAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(modifier = Modifier.padding(innerPadding), onExerciseClick = { exercise -> onExerciseClick(exercise)}, onBackClick = { finish() })
                }
            }
        }
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
fun MainScreen(
    modifier: Modifier = Modifier,
    onExerciseClick: (BaseWorkout) -> Unit,
    viewModel: AllExercisesViewModel = viewModel(),
    onBackClick: () -> Unit
) {
    val exercisesList = viewModel.exercisesList.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212)) // Основной темный фон
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Кнопка "Назад"
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .size(40.dp)
                    .padding(start = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Назад",
                    tint = Color(0xFF1B9AAA)
                )
            }

            // Заголовок
            Text(
                text = "Список упражнений",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1B9AAA),
                modifier = Modifier.padding(vertical = 12.dp)
            )

            ExercisesList(exercisesList, onExerciseClick = onExerciseClick)
        }
    }
}

@Composable
fun ExercisesList(
    exercises: State<List<BaseWorkout>>,
    onExerciseClick: (BaseWorkout) -> Unit
) {
    val exercisesValue = exercises.value

    Box(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF015965))
    ) {
        LazyColumn(
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            items(exercisesValue) { exercise ->
                WorkoutItem(exercise, onExerciseClick)
            }
        }
    }
}
@Composable
fun WorkoutItem(
    baseWorkout: BaseWorkout,
    onExerciseClick: (BaseWorkout) -> Unit
) {
    val context = LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onExerciseClick(baseWorkout) }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (baseWorkout is Exercise && baseWorkout.iconPath != null) {
            val file = File(context.filesDir, baseWorkout.iconPath)
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
        Text(
            text = baseWorkout.name,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White
        )
    }
    HorizontalDivider(
        color = Color.Black.copy(alpha = 0.5f),
        thickness = 1.dp,
        modifier = Modifier.padding(horizontal = 12.dp)
    )
}