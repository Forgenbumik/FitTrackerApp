package com.example.fittrackerapp.uielements.addingtousedworkouts

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fittrackerapp.App
import com.example.fittrackerapp.entities.ExerciseRepository
import com.example.fittrackerapp.ui.theme.FitTrackerAppTheme
import com.example.fittrackerapp.uielements.allworkouts.AllExercisesActivity
import com.example.fittrackerapp.uielements.creatingworkout.CreatingWorkoutActivity
import com.example.fittrackerapp.uielements.creatingworkout.CreatingWorkoutViewModel
import com.example.fittrackerapp.uielements.usedworkouts.UsedWorkoutsViewModel

class AddingToUsedWorkoutsActivity: ComponentActivity() {

    lateinit var viewModel: AddingToUsedWorkoutsViewModel

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            FitTrackerAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AddingToUsedWorkoutsMainScreen(
                        modifier = Modifier.padding(innerPadding),
                        onAddToUsedWorkouts = { onAddToUsedWorkouts() },
                        onCreateNewWorkout = { onCreateNewWorkout() }
                    )
                }
            }
        }

        val app = application as App

        val exerciseRepository = ExerciseRepository(app.appDatabase.exerciseDao())

        val factory = AddingToUsedWorkoutsModelFactory(exerciseRepository)

        viewModel = ViewModelProvider(this, factory).get(AddingToUsedWorkoutsViewModel::class.java)
    }

    fun onAddToUsedWorkouts() {
        val intent = Intent(this, AllExercisesActivity::class.java)
        startActivity(intent)
    }

    fun onCreateNewWorkout() {
        val intent = Intent(this, CreatingWorkoutActivity::class.java)
        startActivity(intent)
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AddingToUsedWorkoutsMainScreen(modifier: Modifier, onAddToUsedWorkouts: () -> Unit, onCreateNewWorkout: () -> Unit, viewModel: AddingToUsedWorkoutsViewModel = viewModel()) {
    val isCreatingExercise = viewModel.isCreatingExercise.collectAsState().value

    Column {
        Text(modifier = Modifier
            .padding(horizontal = 6.dp, vertical = 12.dp)
            .clickable {
                onAddToUsedWorkouts()
        }, text = "Добавить упражнение из списка")
        Text(modifier = Modifier
            .padding(horizontal = 6.dp, vertical = 12.dp)
            .clickable {
                viewModel.setIsCreatingExercise(true)
            }, text = "Создать упражнение")
        Text(modifier = Modifier
            .padding(horizontal = 6.dp, vertical = 12.dp)
            .clickable {
                onCreateNewWorkout()
            }, text = "Создать новый сценарий")
    }

    if (isCreatingExercise) {
        AddingExerciseDialogWindow(modifier = Modifier, setIsCreatingExercise = {showSheet -> viewModel.setIsCreatingExercise(showSheet)})
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddingExerciseDialogWindow(modifier: Modifier, viewModel: AddingToUsedWorkoutsViewModel = viewModel(), setIsCreatingExercise: (Boolean) -> Unit) {

    ModalBottomSheet(onDismissRequest = { setIsCreatingExercise(false) },
        )
    {
        NameField()
        Button(onClick = {
            if (viewModel.addExercise()) {
                setIsCreatingExercise(false)
            }
        }) {
            Text(text = "ОК")
        }
    }
}

@Composable
fun NameField(viewModel: AddingToUsedWorkoutsViewModel = viewModel()) {
    val workoutName = viewModel.getExerciseName()
    TextField(
        value = workoutName,
        onValueChange = { name ->
            viewModel.setExerciseName(name)
        },
        label = { Text("Название упражнения") },
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    )
}