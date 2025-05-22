package com.example.fittrackerapp.uielements.exercise

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fittrackerapp.ui.theme.FitTrackerAppTheme
import com.example.fittrackerapp.uielements.CenteredPicker
import com.example.fittrackerapp.uielements.executingexercise.ExecutingExerciseActivity
import com.example.fittrackerapp.uielements.usedworkouts.UsedWorkoutsActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ExerciseActivity: ComponentActivity() {

    private val viewModel: ExerciseViewModel by viewModels()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            FitTrackerAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(modifier = Modifier.padding(innerPadding), ::onStartExerciseClick, ::onBackClick)
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)

    fun onStartExerciseClick() {
        if (viewModel.exerciseId == 0L
            || viewModel.plannedSets.value == 0
            || viewModel.plannedReps.value == 0) {
            return
        }
        val intent = Intent(this, ExecutingExerciseActivity::class.java).apply {
            putExtra("exerciseId", viewModel.exerciseId)
            putExtra("plannedSets", viewModel.plannedSets.value)
            putExtra("plannedReps", viewModel.plannedReps.value)
            putExtra("plannedRestDuration", viewModel.plannedRestDuration.value)
        }
        startActivity(intent)
    }

    fun onBackClick() {
        val intent = Intent(this, UsedWorkoutsActivity::class.java)
        startActivity(intent)
        finish()
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainScreen(modifier: Modifier = Modifier, onStartExerciseClick: () -> Unit, onBackClick: () -> Unit) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        DetailChangeWindow(onBackClick = onBackClick)

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onStartExerciseClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.DarkGray,
                contentColor = Color.White
            ),
        ) {
            Text("Начать", style = MaterialTheme.typography.titleMedium)
        }
    }
}

@SuppressLint("StateFlowValueCalledInComposition")
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DetailChangeWindow(
    viewModel: ExerciseViewModel = viewModel(),
    onBackClick: () -> Unit // Добавь обработку назад
) {
    val selectedSetsNumber = remember { mutableStateOf(viewModel.plannedSets.value) }
    val selectedRepsNumber = remember { mutableStateOf(viewModel.plannedReps.value) }
    val selectedRestDuration = remember { mutableStateOf(viewModel.plannedRestDuration.value) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        // Верхняя панель: кнопка Назад и название упражнения
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onBackClick() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Назад",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            viewModel.exercise.value?.let {
                Text(
                    text = it.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(5f),
                    textAlign = TextAlign.Center
                )
            }
            Spacer(modifier = Modifier.weight(1f)) // для симметрии
        }

        // Заголовки для подходов и повторений
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Text(
                text = "Подходы",
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Повторения",
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Списки подходов и повторений
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            val itemModifier = Modifier.weight(1f).padding(end = 4.dp)

            ListSets(
                modifier = itemModifier,
                setsNum = selectedSetsNumber.value,
                onItemSelected = { selectedSetsNumber.value = it }
            )

            ListNumberReps(
                modifier = itemModifier,
                reps = selectedRepsNumber.value,
                onItemSelected = { selectedRepsNumber.value = it }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Text(
                text = "Отдых",
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Text(
                text = "Минуты",
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Секунды",
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium
            )
        }

        // Таймер выбора времени отдыха
        TimePicker(
            selectedRestDuration = selectedRestDuration.value,
            onTimeSelected = { minutes, seconds ->
                selectedRestDuration.value = minutes * 60 + seconds
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Кнопка сохранения
        Button(
            onClick = {
                viewModel.setPlannedSets(selectedSetsNumber.value)
                viewModel.setPlannedReps(selectedRepsNumber.value)
                viewModel.setPlannedRestDuration(selectedRestDuration.value)
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.DarkGray,
                contentColor = Color.White
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Сохранить")
        }
    }
}

@Composable
fun ListNumberReps(modifier: Modifier, reps: Int, onItemSelected: (Int) -> Unit) {

    val numberList = (0..500).toList()

    CenteredPicker(items = numberList, selectedIndex = reps, onItemSelected = onItemSelected, modifier = modifier)
}

@Composable
fun ListSets(modifier: Modifier, setsNum: Int, onItemSelected: (Int) -> Unit) {
    val setsNumberList = (0..100).toList()

    CenteredPicker(setsNumberList, selectedIndex = setsNum, onItemSelected = { selected ->
        onItemSelected(selected)
    }, modifier = modifier)
}

@Composable
fun TimePicker(selectedRestDuration: Int,
    modifier: Modifier = Modifier,
    onTimeSelected: (minutes: Int, seconds: Int) -> Unit
) {
    var selectedMinute by remember { mutableStateOf(0) }
    var selectedSecond by remember { mutableStateOf(0) }



    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        CenteredPicker(
            items = (0..9).toList(),
            modifier = Modifier.weight(1f),
            selectedIndex = selectedMinute,
            onItemSelected = {
                selectedMinute = it
                onTimeSelected(selectedMinute, selectedSecond)
            }
        )

        Text(
            ":",
            fontSize = 24.sp,
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        CenteredPicker(
            items = (0..59).toList(),
            modifier = Modifier.weight(1f),
            selectedIndex = selectedSecond,
            onItemSelected = {
                selectedSecond = it
                onTimeSelected(selectedMinute, selectedSecond)
            }
        )
    }
}