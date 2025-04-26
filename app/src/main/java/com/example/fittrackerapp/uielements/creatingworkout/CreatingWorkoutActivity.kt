package com.example.fittrackerapp.uielements.creatingworkout

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import com.example.fittrackerapp.App
import com.example.fittrackerapp.entities.ExerciseRepository
import com.example.fittrackerapp.entities.WorkoutDetailRepository
import com.example.fittrackerapp.entities.WorkoutRepository
import com.example.fittrackerapp.ui.theme.FitTrackerAppTheme
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fittrackerapp.entities.Exercise

class CreatingWorkoutActivity: ComponentActivity() {
    private lateinit var viewModel: CreatingWorkoutViewModel



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            FitTrackerAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    CompletedExerciseMainScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }

        val app = application as App

        val workoutRepository = WorkoutRepository(app.appDatabase.workoutDao())
        val workoutDetailRepository = WorkoutDetailRepository(app.appDatabase.workoutDetailDao())
        val exerciseRepository = ExerciseRepository(app.appDatabase.exerciseDao())

        val factory = CreatingWorkoutViewModelFactory(workoutRepository, workoutDetailRepository, exerciseRepository)

        viewModel = ViewModelProvider(this, factory).get(CreatingWorkoutViewModel::class.java)
    }
}

@Composable
fun CompletedExerciseMainScreen(modifier: Modifier, viewModel: CreatingWorkoutViewModel = viewModel()) {
    val exercises = viewModel.exercisesList.collectAsState().value
    nameField()
    exercisesList(exercises)
}

@Composable
fun nameField(viewModel: CreatingWorkoutViewModel = viewModel()) {
    val workoutName = viewModel.getWorkoutName()
    TextField(
        value = workoutName,
        onValueChange = { name ->
            viewModel.setWorkoutName(name)
        },
        label = { Text(viewModel.getGeneratedName()) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    )
}

@Composable
fun exercisesList(exercises: List<Exercise>) {
    @Composable
    fun SimpleDropdownMenu() {
        var expanded by remember { mutableStateOf(false) }
        val options = listOf("Option 1", "Option 2", "Option 3")

        Box {
            Button(onClick = { expanded = true }) {
                Text("Show Menu")
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            println("Selected: $option")
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}