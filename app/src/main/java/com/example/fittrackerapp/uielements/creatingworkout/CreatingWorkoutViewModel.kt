package com.example.fittrackerapp.uielements.creatingworkout

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.room.util.copy
import com.example.fittrackerapp.entities.ExerciseRepository
import com.example.fittrackerapp.entities.Workout
import com.example.fittrackerapp.entities.WorkoutDetail

import com.example.fittrackerapp.entities.WorkoutDetailRepository
import com.example.fittrackerapp.entities.WorkoutRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
class CreatingWorkoutViewModel(
    private var workoutId: Long,
    private val workoutRepository: WorkoutRepository,
    private val workoutDetailRepository: WorkoutDetailRepository,
    private val exerciseRepository: ExerciseRepository
): ViewModel() {
    private var workoutName = ""

    private var generatedName = ""

    private var _exercisesList = mutableStateListOf<WorkoutDetail>()
    val exercisesList: SnapshotStateList<WorkoutDetail> get() = _exercisesList

    private val _selectedExercise = MutableStateFlow(WorkoutDetail())
    val selectedExercise: StateFlow<WorkoutDetail> =_selectedExercise

    @RequiresApi(Build.VERSION_CODES.O)
    private val _workout: MutableStateFlow<Workout> = MutableStateFlow(Workout())
    @RequiresApi(Build.VERSION_CODES.O)
    val workout: StateFlow<Workout> = _workout

    private val _isSaveCompleted = MutableStateFlow(false)
    val isSaveCompleted: StateFlow<Boolean> = _isSaveCompleted

    init {
        viewModelScope.launch {
            if (workoutId != -1L) {
                _workout.value = workoutRepository.getById(workoutId)
                _exercisesList.addAll(workoutDetailRepository.getByWorkoutId(workoutId))
            }
        }

        viewModelScope.launch {
            _workout.value.name = generateName()
        }
    }

    fun setWorkoutName(name: String) {
        _workout.value = _workout.value.copy(name = name)
    }

    suspend fun generateName(): String {
        val generatedName = "Сценарий ${workoutRepository.getWorkoutsNames().size + 1}"
        return generatedName
    }

    fun setSelectedExercise(detail: WorkoutDetail) {
        _selectedExercise.value = detail
    }

    fun addExerciseToList(workoutDetail: WorkoutDetail) {
        viewModelScope.launch {
            workoutDetail.exerciseName = exerciseRepository.getExerciseName(workoutDetail.exerciseId)
            _exercisesList.add(workoutDetail)
        }
    }

    fun saveWorkout() {
        if (workoutId == -1L) {
            viewModelScope.launch {
                workoutId = workoutRepository.insert(_workout.value)
                exercisesList.forEach {
                    val detailToAdd = it.copy(workoutId = workoutId)
                    workoutDetailRepository.insert(detailToAdd)
                }
                _isSaveCompleted.value = true
            }
        } else {
            viewModelScope.launch {
                workoutRepository.update(_workout.value)
                exercisesList.forEach {
                    if (it.id == 0L) {
                        val detailToAdd = it.copy(workoutId = workoutId)
                        workoutDetailRepository.insert(detailToAdd)
                    }
                    else {
                        workoutDetailRepository.update(it)
                    }
                }
                _isSaveCompleted.value = true
            }
        }
    }

    fun updateExerciseDetail(updatedDetail: WorkoutDetail) {
        val index = _exercisesList.indexOfFirst { it.exerciseId == updatedDetail.exerciseId }
        if (index != -1) {
            _exercisesList[index] = updatedDetail
        }
    }
}

class CreatingWorkoutViewModelFactory(
    private val workoutId: Long,
    private val workoutRepository: WorkoutRepository,
    private val workoutDetailRepository: WorkoutDetailRepository,
    private val exerciseRepository: ExerciseRepository
) : ViewModelProvider.Factory {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(CreatingWorkoutViewModel::class.java) -> {
                CreatingWorkoutViewModel(workoutId, workoutRepository, workoutDetailRepository, exerciseRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}