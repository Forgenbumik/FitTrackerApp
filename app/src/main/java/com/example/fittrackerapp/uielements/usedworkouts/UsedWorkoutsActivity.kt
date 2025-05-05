package com.example.fittrackerapp.uielements.usedworkouts

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fittrackerapp.App
import com.example.fittrackerapp.abstractclasses.BaseWorkout
import com.example.fittrackerapp.abstractclasses.repositories.WorkoutsAndExercisesRepository
import com.example.fittrackerapp.entities.Exercise
import com.example.fittrackerapp.entities.Workout
import com.example.fittrackerapp.ui.theme.FitTrackerAppTheme
import com.example.fittrackerapp.uielements.addingtousedworkouts.AddingToUsedWorkoutsActivity
import com.example.fittrackerapp.uielements.creatingworkout.CreatingWorkoutActivity

class UsedWorkoutsActivity: ComponentActivity() {
    private lateinit var viewModel: UsedWorkoutsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            FitTrackerAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(viewModel, Modifier.padding(innerPadding), ::onPlusClick,
                        ::addWorkoutToFavourites,  ::setSelectedWorkout, ::changeWorkoutClick)
                }
            }
        }

        val app = application as App

        val workoutRepository = WorkoutsAndExercisesRepository(app.appDatabase.workoutDao(), app.appDatabase.exerciseDao())

        val factory = UsedWorkoutsViewModelFactory(workoutRepository)

        viewModel = ViewModelProvider(this, factory).get(UsedWorkoutsViewModel::class.java)
    }

    fun onPlusClick() {
        val intent = Intent(this, AddingToUsedWorkoutsActivity::class.java)
        startActivity(intent)
    }

    fun addWorkoutToFavourites(workout: BaseWorkout) {
        if (!viewModel.addFavouriteWorkout(workout)) {
            Toast.makeText(this, "Превышено допустимое количество избранных тренировок", Toast.LENGTH_SHORT).show()
        }
    }

    fun setSelectedWorkout(baseWorkout: BaseWorkout) {
        viewModel.setSelectedWorkout(baseWorkout)
    }

    fun changeWorkoutClick(workoutId: Long) {
        val intent = Intent(this, CreatingWorkoutActivity::class.java).apply {
            putExtra("workoutId", workoutId)
        }
        startActivity(intent)
    }
}

@Composable
fun MainScreen(viewModel: UsedWorkoutsViewModel, modifier: Modifier = Modifier,
               onPlusClick: () -> Unit,
               AddFavouriteClick: (BaseWorkout) -> Unit,
               setSelectedWorkout: (BaseWorkout) -> Unit,
               changeWorkoutClick: (Long) -> Unit
               ) {

    val showMenu = remember { mutableStateOf(false)}

    UpperBar(onPlusClick)
    val favouriteWorkouts = viewModel.favouriteWorkouts.collectAsState().value
    val workouts = viewModel.workoutsList.collectAsState().value

    Column(modifier = modifier) {
        FavouriteWorkoutsList(favouriteWorkouts, showMenu, setSelectedWorkout, changeWorkoutClick)
        AllWorkoutsList(workouts, setSelectedWorkout, changeWorkoutClick, showMenu)
    }
}

@Composable
fun UpperBar(onPlusClick: () -> Unit) {
    Row {
        Button(modifier = Modifier,
            onClick = {
                onPlusClick()
            }) {
            Text("+",  modifier = Modifier, fontSize = 16.sp)
        }
    }
}

@Composable
fun FavouriteWorkoutsList(
    favouriteWorkouts: List<BaseWorkout>,
    showMenu: MutableState<Boolean>,
    setSelectedWorkout: (BaseWorkout) -> Unit,
    changeWorkoutClick: (Long) -> Unit
) {
    Text("Избранные сценарии")
    LazyColumn(
        modifier = Modifier.fillMaxWidth()
    ) {
        items(favouriteWorkouts) { workout ->
            Row {
                Text(workout.name)
                IconButton(onClick = { showMenu.value = !showMenu.value }) {
                    Icon(imageVector = Icons.Default.MoreVert, contentDescription = "More options")
                }
            }
            HorizontalDivider()
        }
    }
}

@Composable
fun AllWorkoutsList(
    workouts: List<BaseWorkout>,
    setSelectedWorkout: (BaseWorkout) -> Unit,
    changeWorkoutClick: (Long) -> Unit,
    showMenu: MutableState<Boolean>) {

    Text("Все сценарии")
    LazyColumn {
        items(workouts) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp), // Добавление отступов
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("${it.name}")
                IconButton(onClick = {
                    showMenu.value = !showMenu.value
                    setSelectedWorkout(it)
                }) {
                    Icon(imageVector = Icons.Default.MoreVert, contentDescription = "More options")
                }
            }
            HorizontalDivider()
        }
    }
    if (showMenu.value) {
        ActionMenu(changeWorkoutClick, onDismiss = { showMenu.value = false })
    }
}

@Composable
fun ActionMenu(changeWorkoutClick: (Long) -> Unit,
               onDismiss: () -> Unit, viewModel: UsedWorkoutsViewModel = viewModel()) {
    val selectedWorkout = viewModel.selectedWorkout.collectAsState().value

    var showChangeWindow by remember { mutableStateOf(false) }

    DropdownMenu(
        expanded = true, // Меню открывается для текущего элемента
        onDismissRequest = { onDismiss() } // Закрытие меню при клике вне
    ) {
        DropdownMenuItem(onClick = {
            if (selectedWorkout != null) {
                if (selectedWorkout.isFavourite)
                    viewModel.removeFavouriteWorkout(selectedWorkout)
                else {
                    viewModel.addFavouriteWorkout(selectedWorkout)
                }
            }
        },
            text = {
                if (selectedWorkout != null) {
                    if (selectedWorkout.isFavourite)
                        Text("Удалить из избранного")
                    else
                        Text("Добавить в избранное")
                }
            })
        DropdownMenuItem(onClick = {
            showChangeWindow = true
        },
            text = { Text("Изменить") })
        DropdownMenuItem(onClick = {
            if (selectedWorkout != null) {
                viewModel.removeWorkoutFromUsed(selectedWorkout)
            }
        },
            text = { Text("Скрыть") })
        DropdownMenuItem(onClick = {
            if (selectedWorkout != null) {
                viewModel.deleteWorkout(selectedWorkout)
            }
        },
            text = { Text("Удалить") })
    }
    if (showChangeWindow) {
        if (selectedWorkout is Workout) {
            changeWorkoutClick(selectedWorkout.id)
        }
        else {
            ExerciseChangeWindow(selectedWorkout as Exercise)
        }
    }
}

@Composable
fun ExerciseChangeWindow(exercise: Exercise) {

}