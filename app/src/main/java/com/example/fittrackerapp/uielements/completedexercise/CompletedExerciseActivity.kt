package com.example.fittrackerapp.uielements.completedexercise

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fittrackerapp.App
import com.example.fittrackerapp.entities.CompletedExercise
import com.example.fittrackerapp.entities.CompletedExerciseRepository
import com.example.fittrackerapp.ui.theme.FitTrackerAppTheme
import com.example.fittrackerapp.entities.Set
import com.example.fittrackerapp.entities.SetRepository
import kotlinx.coroutines.launch

class CompletedExerciseActivity: ComponentActivity() {

    private lateinit var viewModel: CompletedExerciseViewModel

    var exerciseName = ""

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FitTrackerAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(modifier = Modifier.padding(innerPadding),
                        exerciseName,
                        formatTime = {secs -> formatTime(secs)},
                        setChangingSet = {set -> setChangingSet(set)})
                }
            }
        }

        val app = application as App

        val completedExerciseId = intent.getLongExtra("completedExerciseId", -1)
        exerciseName = intent.getStringExtra("exerciseName").toString()

        val completedExerciseRepository = CompletedExerciseRepository(app.appDatabase.completedExerciseDao())
        val setRepository = SetRepository(app.appDatabase.setDao())

        val factory = CompletedExerciseViewModelFactory(completedExerciseId, completedExerciseRepository, setRepository)

        viewModel = ViewModelProvider(this, factory).get(CompletedExerciseViewModel::class.java)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun formatTime(secs: Int): String {
        return viewModel.formatTime(secs)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun setChangingSet(set: Set) {
        viewModel.setChangingSet(set)
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainScreen(modifier: Modifier, exerciseName: String, viewModel: CompletedExerciseViewModel = viewModel(), formatTime: (Int) -> String, setChangingSet: (Set) -> Unit) {

    val completedExercise = viewModel.completedExercise.collectAsState().value
    val setList = viewModel.setList

    val changingSet = viewModel.changingSet.collectAsState()

    Column(
        modifier.windowInsetsPadding(WindowInsets.statusBars)
    ) {
        Text(exerciseName)
        ExerciseInformation(completedExercise)
        SetsTable(setList, formatTime, setChangingSet = setChangingSet, changingSet = changingSet)
    }
}

@Composable
fun ExerciseInformation(exercise: CompletedExercise) {
    Text("Подходов: ${exercise.setsNumber}. Всего повторений: ${exercise.totalReps}")
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SetsTable(setList: SnapshotStateList<Set>, formatTime: (Int) -> String, setChangingSet: (Set) -> Unit, changingSet: State<Set?>) {
    Column {
        SetsTableHeaders()
        SetsStrings(setList, formatTime, setChangingSet = setChangingSet, changingSet = changingSet)
    }
}

@Composable
fun SetsTableHeaders() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.LightGray)
            .padding(8.dp)) {

        val modifier = Modifier.weight(1f)
        Text("Подход", modifier = modifier, textAlign = TextAlign.Center)
        Text("Повт.", modifier = modifier, textAlign = TextAlign.Center)
        Text("Вес", modifier = modifier, textAlign = TextAlign.Center)
        Text("Время", modifier = modifier, textAlign = TextAlign.Center)
        Text("Изменить", modifier = modifier, textAlign = TextAlign.Center)
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SetsStrings(setList: SnapshotStateList<Set>, formatTime: (Int) -> String, viewModel: CompletedExerciseViewModel = viewModel(), setChangingSet: (Set) -> Unit, changingSet: State<Set?>) {

    val showSheet = viewModel.isChangingSet.collectAsState().value
    val changingSetValue = changingSet.value

    val setListValue = setList

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        items(setListValue.size) { i ->  // Используем items вместо for
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically // Выравнивание по центру
            ) {
                val modifier = Modifier.weight(1f)
                Text("${i + 1}", modifier = modifier, textAlign = TextAlign.Center)
                Text("${setListValue[i].reps}", modifier = modifier, textAlign = TextAlign.Center)
                Text("${setListValue[i].weight} кг", modifier = modifier, textAlign = TextAlign.Center)
                val setStringSeconds = formatTime(setListValue[i].duration)
                Text(setStringSeconds, modifier = modifier, textAlign = TextAlign.Center)

                Box(modifier = Modifier
                    .aspectRatio(2f)
                    .weight(1f), contentAlignment = Alignment.Center) {
                    Button(onClick = {
                        viewModel.setChangingSet(setListValue[i])
                        viewModel.setIsChangingSet(true)

                    }) {
                        Text("Изменить")
                    }
                }
            }
            HorizontalDivider() // Разделитель между строками
        }
    }
    if (showSheet && changingSetValue != null) {
        SetChangeWindow(changingSetValue, setIsChangingSet = { showSheet-> viewModel.setIsChangingSet(showSheet)})
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SetChangeWindow(set: Set, viewModel: CompletedExerciseViewModel = viewModel(), setIsChangingSet: (Boolean) -> Unit, ) {

    ModalBottomSheet(
        onDismissRequest = { setIsChangingSet(false) }
    ) {
        Column {
            Row {
                Text("Повторения")
                Text("Вес")
            }

            var selectedRepsNumber = set.reps

            var selectedWeight = set.weight
            Column {
                Row {
                    val modifier = Modifier.weight(0.5f)
                    ListNumberReps(modifier, set.reps, onItemSelected = { selectedItem ->
                        selectedRepsNumber = selectedItem
                    })
                    ListWeight(modifier, selectedWeight.toInt(), (selectedWeight - selectedWeight.toInt()).toInt(),
                        onItemSelected = { selectedInteger, selectedDecimal -> selectedWeight = selectedInteger + selectedDecimal.toDouble() / 10 })
                }

                Button(
                    onClick = {
                        viewModel.updateSet(set, selectedRepsNumber, selectedWeight)
                        setIsChangingSet(false)
                        viewModel.setChangingSet(null)
                    }
                ) {
                    Text("ОК")
                }
            }
        }
    }
}

@Composable
fun ListNumberReps(modifier: Modifier, reps: Int, onItemSelected: (Int) -> Unit) {

    val numberList = (0..100).toList()

    CenteredPicker(items = numberList, selectedIndex = reps, onItemSelected = onItemSelected, modifier = modifier)
}

@Composable
fun ListWeight(modifier: Modifier, integerPart: Int, decimalPart: Int, onItemSelected: (Int, Int) -> Unit) {
    val weightList = (0..500).toList()
    val weightDecimals = (0..9).toList()

    var integerWeight = integerPart

    var decimalWeight = decimalPart

    Row(modifier = modifier, horizontalArrangement = Arrangement.Center) {
        CenteredPicker(weightList, selectedIndex = integerPart, onItemSelected = { selectedInteger ->
            integerWeight = selectedInteger
            onItemSelected(integerWeight, decimalWeight)
        }, modifier = modifier)
        Text(".")
        CenteredPicker(weightDecimals, selectedIndex = decimalPart, onItemSelected = { selectedDecimal ->
            decimalWeight = selectedDecimal
            onItemSelected(integerWeight, decimalWeight)
        }, modifier = modifier)
    }


}

@Composable
fun CenteredPicker(
    items: List<Int>,
    modifier: Modifier = Modifier,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val itemHeight = 50.dp
    val visibleItemsCount = 5
    val centerIndex = visibleItemsCount/2

    Box(modifier = modifier.height(itemHeight * visibleItemsCount)) {
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(vertical = itemHeight * centerIndex),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            coroutineScope.launch {
                listState.animateScrollToItem(selectedIndex)
            }

            items(items) { item ->
                val center = listState.firstVisibleItemIndex
                val isSelected = item == center

                CenteredListItem(onItemSelected, item, isSelected, itemHeight)
            }
        }

        // Overlay для выделения центра
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .height(itemHeight)
                .background(Color.LightGray.copy(alpha = 0.2f))
        )
    }
}

@Composable
fun CenteredListItem(onItemSelected: (Int) -> Unit, item: Int, isSelected: Boolean, itemHeight: Dp, ) {
    Text(
        text = "$item",
        fontSize = if (isSelected) 24.sp else 16.sp,
        color = if (isSelected) Color.Black else Color.Gray,
        modifier = Modifier
            .height(itemHeight)
            .fillMaxWidth()
            .wrapContentHeight(),
        textAlign = TextAlign.Center
    )
    if (isSelected) {
        onItemSelected(item)
    }
}