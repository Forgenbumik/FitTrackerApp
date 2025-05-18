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
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fittrackerapp.ui.theme.FitTrackerAppTheme
import com.example.fittrackerapp.uielements.VideoPlayerFromFile
import com.example.fittrackerapp.uielements.allworkouts.AllExercisesActivity
import com.example.fittrackerapp.uielements.creatingworkout.CreatingWorkoutActivity
import java.io.File

class AddingToUsedWorkoutsActivity: ComponentActivity() {

    private val viewModel: AddingToUsedWorkoutsViewModel by viewModels()

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
                        onCreateNewWorkout = { onCreateNewWorkout() },
                        onBack = { finish() }
                    )
                }
            }
        }
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
fun MainScreen(
    modifier: Modifier = Modifier,
    onAddToUsedWorkouts: () -> Unit,
    onCreateNewWorkout: () -> Unit,
    viewModel: AddingToUsedWorkoutsViewModel = viewModel(),
    onBack: () -> Unit
) {
    val isCreatingExercise = remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 🔙 Кнопка назад
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Назад",
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Меню упражнений",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1B9AAA), // Яркий бирюзово-синий
                modifier = Modifier.padding(bottom = 8.dp)
            )

            ActionCard(
                text = "Добавить упражнение из списка",
                color = Color(0xFF1B9AAA),
                onClick = onAddToUsedWorkouts
            )

            ActionCard(
                text = "Создать упражнение",
                color = Color(0xFF007D8A),
                onClick = { isCreatingExercise.value = true }
            )

            ActionCard(
                text = "Добавить сценарий тренировки",
                color = Color(0xFFFFA500),
                onClick = onCreateNewWorkout
            )
        }

        if (isCreatingExercise.value) {
            AddingExerciseDialogWindow(
                modifier = Modifier,
                setIsCreatingExercise = { isCreatingExercise.value = it }
            )
        }
    }
}

@Composable
fun ActionCard(text: String, color: Color, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .shadow(4.dp, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = color),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            Text(
                text = text,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddingExerciseDialogWindow(
    modifier: Modifier = Modifier,
    viewModel: AddingToUsedWorkoutsViewModel = viewModel(),
    setIsCreatingExercise: (Boolean) -> Unit
) {
    val addingExercise = viewModel.addingExercise.collectAsState().value
    val context = LocalContext.current

    val iconLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri: Uri? ->
            uri?.let {
                addingExercise?.let { viewModel.saveExerciseIcon(it, uri, context) }
            }
        }
    )

    val videoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri: Uri? ->
            uri?.let {
                addingExercise?.let { viewModel.saveExerciseVideo(it, uri, context) }
            }
        }
    )

    ModalBottomSheet(
        onDismissRequest = { setIsCreatingExercise(false) },
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Новое упражнение",
                fontSize = 22.sp,
                color = Color(0xFF007D8A), // Бирюзово-синий
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            NameField()
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color.Gray)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Иконка упражнения", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(8.dp))

                    if (addingExercise?.iconPath == null) {
                        OutlinedButton(
                            onClick = { iconLauncher.launch(arrayOf("image/*")) },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF007D8A))
                        ) {
                            Text("Добавить иконку")
                        }
                    } else {
                        val file = File(context.filesDir, addingExercise.iconPath!!)
                        val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier
                                .size(128.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, Color.Gray)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = { viewModel.deleteExerciseIcon(context, addingExercise) },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                        ) {
                            Text("Удалить иконку")
                        }
                    }
                }
            }

            // 🎥 Видео
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color.Gray)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Видео выполнения", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(8.dp))

                    if (addingExercise?.videoPath == null) {
                        OutlinedButton(
                            onClick = { videoLauncher.launch(arrayOf("video/*")) },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF007D8A))
                        ) {
                            Text("Добавить видео")
                        }
                    } else {
                        val file = File(context.filesDir, addingExercise.videoPath!!)
                        VideoPlayerFromFile(file)
                        Spacer(Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = { viewModel.deleteExerciseVideo(context, addingExercise) },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                        ) {
                            Text("Удалить видео")
                        }
                    }
                }
            }

            // ✅ Подтвердить
            Button(
                onClick = {
                    if (viewModel.createNewExercise()) {
                        setIsCreatingExercise(false)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007D8A))
            ) {
                Text("Сохранить", color = Color.White, fontSize = 18.sp)
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