package com.example.fittrackerapp.uielements.addingtousedworkouts

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.fittrackerapp.App
import com.example.fittrackerapp.entities.ExerciseRepository
import com.example.fittrackerapp.entities.WorkoutDetail
import com.example.fittrackerapp.ui.theme.FitTrackerAppTheme
import com.example.fittrackerapp.uielements.ClickableRow
import com.example.fittrackerapp.uielements.VideoPlayerFromFile
import com.example.fittrackerapp.uielements.allworkouts.AllExercisesActivity
import com.example.fittrackerapp.uielements.creatingworkout.CreatingWorkoutActivity
import java.io.File

class AddingToUsedWorkoutsActivity: ComponentActivity() {

    lateinit var viewModel: AddingToUsedWorkoutsViewModel

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            FitTrackerAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(
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

    @RequiresApi(Build.VERSION_CODES.O)
    val addExerciseLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val exerciseId = result.data?.getLongExtra("exerciseId", -1)
            if (exerciseId != null) {
                viewModel.addSelectedExercise(exerciseId)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun onAddToUsedWorkouts() {

        val intent = Intent(this, AllExercisesActivity::class.java).apply {
            putExtra("reason", "exerciseAdding")
        }
        addExerciseLauncher.launch(intent)
    }

    fun onCreateNewWorkout() {
        val intent = Intent(this, CreatingWorkoutActivity::class.java)
        startActivity(intent)
    }


}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainScreen(modifier: Modifier, onAddToUsedWorkouts: () -> Unit, onCreateNewWorkout: () -> Unit, viewModel: AddingToUsedWorkoutsViewModel = viewModel()) {
    val isCreatingExercise = remember { mutableStateOf(false) }

    Column(
        Modifier.windowInsetsPadding(WindowInsets.statusBars)
    ) {
        ClickableRow("Добавить упражнение из списка", onAddToUsedWorkouts)
        ClickableRow("Создать упражнение", onClick = { isCreatingExercise.value = true })
        ClickableRow("Добавить сценарий тренировки", onCreateNewWorkout)
    }

    if (isCreatingExercise.value) {
        AddingExerciseDialogWindow(modifier = Modifier, setIsCreatingExercise = {showSheet -> isCreatingExercise.value = showSheet})
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddingExerciseDialogWindow(modifier: Modifier, viewModel: AddingToUsedWorkoutsViewModel = viewModel(), setIsCreatingExercise: (Boolean) -> Unit) {

    val addingExercise = viewModel.addingExercise.collectAsState().value

    val context = LocalContext.current

    val iconLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri: Uri? ->
            uri?.let {
                if (addingExercise != null) {
                    viewModel.saveExerciseIcon(addingExercise, uri, context)
                }
            }
        }
    )

    val videoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri: Uri? ->
            uri?.let {
                if (addingExercise != null) {
                    viewModel.saveExerciseVideo(addingExercise, uri, context)
                }
            }
        }
    )

    ModalBottomSheet(onDismissRequest = { setIsCreatingExercise(false) },
        )
    {
        Column {
            NameField()
            if (addingExercise?.iconPath == null) {
                Button(
                    onClick = {
                        iconLauncher.launch(arrayOf("image/*"))
                    }
                ) {
                    Text("Добавить иконку")
                }
            }
            else {
                val file = addingExercise?.iconPath?.let { File(context.filesDir, it) }
                val bitmap = BitmapFactory.decodeFile(file?.absolutePath)

                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.size(128.dp).aspectRatio(2f)
                )
                Button(
                    onClick = {
                        if (addingExercise != null) {
                            viewModel.deleteExerciseIcon(context, addingExercise)
                        }
                    }
                ) {
                    Text("Удалить иконку")
                }
            }
            if (addingExercise?.videoPath == null) {
                Button(
                    onClick = {
                        videoLauncher.launch(arrayOf("video/*"))
                    }
                ) {
                    Text("Добавить видео выполнения")
                }
            }
            else {
                val file = addingExercise.videoPath?.let { File(context.filesDir, it) }
                if (file != null) {
                    VideoPlayerFromFile(file)
                }
                Button(
                    onClick = {
                        viewModel.deleteExerciseVideo(context, addingExercise)
                    }
                ) {
                    Text("Удалить видео")
                }
            }


            Button(onClick = {
                if (viewModel.createNewExercise()) {
                    setIsCreatingExercise(false)
                }
            }) {
                Text(text = "ОК")
            }
        }

    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NameField(viewModel: AddingToUsedWorkoutsViewModel = viewModel()) {
    val exercise = viewModel.addingExercise.collectAsState()
    exercise.value?.let {
        TextField(
        value = it.name,
        onValueChange = { name ->
            viewModel.setExerciseName(name)
        },
        label = { Text("Название упражнения") },
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    )
    }
}