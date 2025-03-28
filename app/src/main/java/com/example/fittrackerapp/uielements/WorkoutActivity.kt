package com.example.fittrackerapp.uielements

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fittrackerapp.App
import com.example.fittrackerapp.entities.WorkoutDetail
import com.example.fittrackerapp.entities.WorkoutDetailRepository
import com.example.fittrackerapp.entities.WorkoutRepository
import com.example.fittrackerapp.ui.theme.FitTrackerAppTheme
import com.example.fittrackerapp.viewmodels.WorkoutViewModel
import com.example.fittrackerapp.viewmodels.WorkoutViewModelFactory

class WorkoutActivity: ComponentActivity() {

    private lateinit var viewModel: WorkoutViewModel

    var workoutId = 0L
    var workoutName = ""

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FitTrackerAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(modifier = Modifier.padding(innerPadding), workoutName, viewModel, ::onExerciseClick)
                }
            }
        }

        val app = application as App


        workoutId = intent.getLongExtra("workoutId", -1)
        workoutName = intent.getStringExtra("workoutName") ?: "Тренировка"

        if (workoutId == -1L) {
            Log.e("WorkoutViewModel", "Ошибка: workoutId не передан в Intent!")
        }

        val workoutRepository = WorkoutRepository(app.appDatabase.workoutDao())
        val workoutDetailRepository = WorkoutDetailRepository(app.appDatabase.workoutDetailDao())

        val factory = WorkoutViewModelFactory(workoutId, workoutName, workoutRepository, workoutDetailRepository)

        viewModel = ViewModelProvider(this, factory).get(WorkoutViewModel::class.java)
    }

    fun onExerciseClick(exercise: WorkoutDetail) {
        val intent = Intent(this, ExecutingWorkoutActivity::class.java).apply {
            putExtra("workoutId", workoutId)
            putExtra("workoutName", workoutName)
            putExtra("detailId", exercise.id)
            putExtra("exerciseName", exercise.exerciseName)
        }
        startActivity(intent)
    }
}

@Composable
fun MainScreen(
    modifier: Modifier,
    workoutName: String,
    viewModel: WorkoutViewModel = viewModel(),
    onExerciseClick: (WorkoutDetail) -> Unit
) {
    val exercises = viewModel.exercisesList.collectAsState()
    Column(modifier = modifier
        .windowInsetsPadding(WindowInsets.statusBars)) {
        Text(workoutName)
        Text("Упражнения")
        ExerciseList(exercises.value, onExerciseClick)
    }
}

@Composable
fun ExerciseList(exerciseList: List<WorkoutDetail>,
                 onExerciseClick: (WorkoutDetail) -> Unit) {
    for (exercise in exerciseList) {
        ExerciseItem(exercise, onExerciseClick)
    }
}

@Composable
fun ExerciseItem(workoutDetail: WorkoutDetail, onClick: (WorkoutDetail) -> Unit, viewModel: WorkoutViewModel = viewModel()) {
    Column(
        modifier = Modifier
    ) {
        Text(workoutDetail.exerciseName)
        Row(
            modifier = Modifier,
        ) {
            Text("Подходов: ${workoutDetail.setsNumber}, ")
            Text("повторений: ${workoutDetail.reps}, ")
            if (workoutDetail.isRestManually) {
                Text("отдых: вручную")
            }
            else {
                val textTime = viewModel.FormatTime(workoutDetail.restDuration)
                Text("отдых: $textTime")
            }
            Button(onClick = { onClick(workoutDetail) },
                content = { Text("Запустить") })
        }
    }
}