package com.example.fittrackerapp.uielements.usedworkouts

import android.content.Intent
import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.core.net.toUri
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.fittrackerapp.App
import com.example.fittrackerapp.R
import com.example.fittrackerapp.abstractclasses.BaseWorkout
import com.example.fittrackerapp.abstractclasses.repositories.WorkoutsAndExercisesRepository
import com.example.fittrackerapp.entities.Exercise
import com.example.fittrackerapp.entities.ExerciseRepository
import com.example.fittrackerapp.entities.Workout
import com.example.fittrackerapp.ui.theme.DarkerBackground
import com.example.fittrackerapp.ui.theme.DeepTurquoise
import com.example.fittrackerapp.ui.theme.FitTrackerAppTheme
import com.example.fittrackerapp.ui.theme.LightTurquoise
import com.example.fittrackerapp.uielements.FileIcon
import com.example.fittrackerapp.uielements.VideoPlayerFromFile
import com.example.fittrackerapp.uielements.addingtousedworkouts.AddingToUsedWorkoutsActivity
import com.example.fittrackerapp.uielements.creatingworkout.CreatingWorkoutActivity
import com.example.fittrackerapp.uielements.exercise.ExerciseActivity
import com.example.fittrackerapp.uielements.main.MainActivity
import com.example.fittrackerapp.uielements.workout.WorkoutActivity
import java.io.File

class UsedWorkoutsActivity: ComponentActivity() {
    private lateinit var viewModel: UsedWorkoutsViewModel

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            FitTrackerAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(Modifier.padding(innerPadding), ::onBackClick, ::onPlusClick,
                        ::changeWorkoutClick, ::onWorkoutClick)
                }
            }
        }

        val app = application as App

        val workoutsAndExercisesRepository = WorkoutsAndExercisesRepository(app.appDatabase.workoutDao(), app.appDatabase.exerciseDao(), app.appDatabase.workoutDetailDao())

        val exerciseRepository = ExerciseRepository(app.appDatabase.exerciseDao())

        val factory = UsedWorkoutsViewModelFactory(workoutsAndExercisesRepository, exerciseRepository)

        viewModel = ViewModelProvider(this, factory).get(UsedWorkoutsViewModel::class.java)
    }

    fun onPlusClick() {
        val intent = Intent(this, AddingToUsedWorkoutsActivity::class.java)
        startActivity(intent)
    }

    fun changeWorkoutClick(workoutId: Long) {
        val intent = Intent(this, CreatingWorkoutActivity::class.java).apply {
            putExtra("workoutId", workoutId)
        }
        startActivity(intent)
    }

    fun deleteExerciseIcon(context: Context, exercise: Exercise) {
        viewModel.deleteExerciseIcon(context, exercise)
    }

    fun onWorkoutClick(baseWorkout: BaseWorkout) {
        val intent: Intent
        if (baseWorkout is Workout) {
            intent = Intent(this, WorkoutActivity::class.java).apply {
                putExtra("workoutId", baseWorkout.id)
            }
        }
        else {
            intent = Intent(this, ExerciseActivity::class.java).apply {
                putExtra("exerciseId", baseWorkout.id)
            }
        }
        startActivity(intent)
    }

    fun onBackClick() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainScreen(
    modifier: Modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars),
    onBackClick: () -> Unit,
    onPlusClick: () -> Unit,
    changeWorkoutClick: (Long) -> Unit,
    onWorkoutClick: (BaseWorkout) -> Unit,
    viewModel: UsedWorkoutsViewModel = viewModel()
) {
    val isMenuVisible = remember { mutableStateOf(false) }
    val isChangeWindowVisible = remember { mutableStateOf(false) }
    val selectedWorkout = viewModel.selectedWorkout.collectAsState().value
    val favouriteWorkouts = viewModel.favouriteWorkouts.collectAsState().value
    val workouts = viewModel.workoutsList.collectAsState().value
    val menuOffset = remember { mutableStateOf(Offset.Zero) }

    Column(modifier = Modifier.background(DarkerBackground)) {
        TopBar(onBackClick = onBackClick, onPlusClick = onPlusClick)

        WorkoutListContainer {
            FavouriteWorkoutsList(favouriteWorkouts, menuOffset, isMenuVisible, onWorkoutClick)
            Spacer(modifier = Modifier.height(8.dp))
            AllWorkoutsList(workouts, menuOffset, isMenuVisible, onWorkoutClick)
        }
    }

    if (isMenuVisible.value && selectedWorkout != null) {
        ActionMenu(
            menuOffset.value,
            changeWorkoutClick = changeWorkoutClick,
            onDismiss = { isMenuVisible.value = false },
            isChangeWindowVisible
        )
    }
}

@Composable
fun TopBar(onPlusClick: () -> Unit, onBackClick: () -> Unit, ) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkerBackground)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onBackClick) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад", tint = Color.White)
        }

        Text(
            text = "Мои тренировки",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        IconButton(onClick = onPlusClick) {
            Icon(Icons.Default.Add, contentDescription = "Добавить", tint = Color.White)
        }
    }
}
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun FavouriteWorkoutsList(
    favouriteWorkouts: List<BaseWorkout>,
    menuOffset: MutableState<Offset>,
    isMenuVisible: MutableState<Boolean>,
    onWorkoutClick: (BaseWorkout) -> Unit
) {

    val context = LocalContext.current

    Column {
        Text("Избранные сценарии")
        LazyColumn(
            modifier = Modifier.fillMaxWidth()
        ) {
            items(favouriteWorkouts) { workout ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp).clickable {
                            onWorkoutClick(workout)
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (workout is Exercise && workout.iconPath != null) {
                        FileIcon(File(context.filesDir, workout.iconPath))
                    }
                    else {
                        Image(
                            painter = painterResource(id = R.drawable.ic_exercise_default), // Здесь используем идентификатор ресурса
                            contentDescription = "Иконка упражнения",
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Text(workout.name)
                    ButtonMore(menuOffset, isMenuVisible, workout)
                }
                HorizontalDivider()
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AllWorkoutsList(
    workouts: List<BaseWorkout>,
    menuOffset: MutableState<Offset>,
    isMenuVisible: MutableState<Boolean>,
    onWorkoutClick: (BaseWorkout) -> Unit
) {
    val context = LocalContext.current

    Text("Больше упражнений")

    LazyColumn {
        items(workouts) { workout ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp).clickable {
                        onWorkoutClick(workout)
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (workout is Exercise && workout.iconPath != null) {
                    val file = File(context.filesDir, workout.iconPath)
                    FileIcon(file)
                }
                else {
                    Image(
                        painter = painterResource(id = R.drawable.ic_exercise_default), // Здесь используем идентификатор ресурса
                        contentDescription = "Иконка упражнения",
                        modifier = Modifier.size(36.dp)
                    )
                }
                Text(workout.name, modifier = Modifier.weight(1f))
                ButtonMore(menuOffset, isMenuVisible, workout)
            }
            HorizontalDivider()
        }
    }
}

@Composable
fun WorkoutListContainer(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 4.dp,
        color = LightTurquoise
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            content()
        }
    }
}

@Composable
fun ButtonMore(
    menuOffset: MutableState<Offset>,
    isMenuVisible: MutableState<Boolean>,
    workout: BaseWorkout,
    viewModel: UsedWorkoutsViewModel = viewModel(),
) {

    Box(
        modifier = Modifier
            .onGloballyPositioned { coordinates ->
                val position = coordinates.localToRoot(Offset.Zero)
                menuOffset.value = position
            }
    ) {
        IconButton(onClick = {
            viewModel.setSelectedWorkout(workout)
            isMenuVisible.value = true
        }) {
            Icon(Icons.Default.MoreVert, contentDescription = "More options")
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ActionMenu(
    menuOffset: Offset,
    changeWorkoutClick: (Long) -> Unit,
    onDismiss: () -> Unit,
    isChangeWindowVisible: MutableState<Boolean>,
    viewModel: UsedWorkoutsViewModel = viewModel(),
) {
    val selectedWorkout = viewModel.selectedWorkout.collectAsState().value

    Popup(
        offset = IntOffset(menuOffset.x.toInt(), menuOffset.y.toInt()),
        onDismissRequest = onDismiss,
        properties = PopupProperties(focusable = true)
    ) {
        Surface(
            modifier = Modifier.wrapContentSize(),
            tonalElevation = 4.dp,
            shape = MaterialTheme.shapes.medium
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                DropdownMenuItem(
                    text = {
                        Text(
                            if (selectedWorkout?.isFavourite == true)
                                "Удалить из избранного"
                            else
                                "Добавить в избранное"
                        )
                    },
                    onClick = {
                        selectedWorkout?.let {
                            if (it.isFavourite)
                                viewModel.removeFavouriteWorkout(it)
                            else
                                viewModel.addFavouriteWorkout(it)
                        }
                        onDismiss()
                    }
                )

                DropdownMenuItem(
                    text = { Text("Изменить") },
                    onClick = {
                        isChangeWindowVisible.value = true
                    }
                )

                DropdownMenuItem(
                    text = { Text("Скрыть") },
                    onClick = {
                        selectedWorkout?.let { viewModel.removeWorkoutFromUsed(it) }
                        onDismiss()
                    }
                )

                DropdownMenuItem(
                    text = { Text("Удалить") },
                    onClick = {
                        selectedWorkout?.let { viewModel.deleteWorkout(it) }
                        onDismiss()
                    }
                )
            }
        }
    }

    if (isChangeWindowVisible.value) {

        if (selectedWorkout is Workout) {
            changeWorkoutClick(selectedWorkout.id)
        } else {
            ExerciseChangeWindow(
                setIsChangingExercise = { isChangeWindowVisible.value = it }
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseChangeWindow(modifier: Modifier = Modifier, setIsChangingExercise: (Boolean) -> Unit, viewModel: UsedWorkoutsViewModel = viewModel()) {

    val selected = viewModel.selectedWorkout.collectAsState().value as? Exercise


    if (selected !is Exercise) {
        Log.e("ExerciseChangeWindow", "Selected workout is not an Exercise")
        return
    }

    val context = LocalContext.current

    val iconLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri: Uri? ->
            uri?.let {
                viewModel.saveExerciseIcon(selected, uri, context)
            }
        }
    )

    val videoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri: Uri? ->
            uri?.let {
                viewModel.saveExerciseVideo(selected, uri, context)
            }
        }
    )

    ModalBottomSheet(
        onDismissRequest = { setIsChangingExercise(false) },
    )
    {
        Column {
            NameField(selected.name)
            if (selected.iconPath == null) {
                Button(
                    onClick = {
                        iconLauncher.launch(arrayOf("image/*"))
                    }
                ) {
                    Text("Добавить иконку")
                }
            } else {
                selected.iconPath?.let { path ->
                    val file = File(context.filesDir, path)
                    if (file.exists()) {
                        val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier
                                .size(64.dp) // круглый аватар
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Button(
                            onClick = {
                                viewModel.deleteExerciseIcon(context, selected)
                            }
                        ) {
                            Text("Удалить иконку")
                        }
                    } else {
                        Log.e("ExerciseChangeWindow", "Файл иконки не найден: ${file.absolutePath}")
                    }
                }
            }
            if (selected.videoPath == null) {
                Button(
                    onClick = {
                        videoLauncher.launch(arrayOf("video/*"))
                    }
                ) {
                    Text("Добавить видео выполнения")
                }
            }
            else {
                val file = File(context.filesDir, selected.videoPath)
                VideoPlayerFromFile(file)
                Button(
                    onClick = {
                        viewModel.deleteExerciseVideo(context, selected)
                    }
                ) {
                    Text("Удалить видео")
                }
            }
            Button(onClick = {
                setIsChangingExercise(false)
            }) {
                Text(text = "ОК")
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NameField(name: String, viewModel: UsedWorkoutsViewModel = viewModel()) {
    TextField(
        value = name,
        onValueChange = { name ->
            viewModel.setExerciseName(name)
        },
        label = { Text("Название упражнения") },
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    )
}