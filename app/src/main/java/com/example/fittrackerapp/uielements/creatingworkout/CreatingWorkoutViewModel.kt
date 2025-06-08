package com.example.fittrackerapp.uielements.creatingworkout

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fittrackerapp.entities.exercise.ExerciseRepository
import com.example.fittrackerapp.entities.workout.Workout
import com.example.fittrackerapp.entities.workoutdetail.WorkoutDetail

import com.example.fittrackerapp.entities.workoutdetail.WorkoutDetailRepository
import com.example.fittrackerapp.entities.workout.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
@RequiresApi(Build.VERSION_CODES.O)
class CreatingWorkoutViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val workoutRepository: WorkoutRepository,
    private val workoutDetailRepository: WorkoutDetailRepository,
    private val exerciseRepository: ExerciseRepository
): ViewModel() {

    private var workoutId: String? get() = savedStateHandle["workoutId"]
        set(value) {
            savedStateHandle["workoutId"] = value
        }

    private var _workoutDetailsList = mutableStateListOf<WorkoutDetail>()
    val workoutDetailsList: SnapshotStateList<WorkoutDetail> get() = _workoutDetailsList

    private val _selectedExercise = MutableStateFlow(WorkoutDetail())
    val selectedExercise: StateFlow<WorkoutDetail> =_selectedExercise

    @RequiresApi(Build.VERSION_CODES.O)
    private val _workout: MutableStateFlow<Workout?> = MutableStateFlow(null)
    @RequiresApi(Build.VERSION_CODES.O)
    val workout: StateFlow<Workout?> = _workout

    private val _isSaveCompleted = MutableStateFlow(false)
    val isSaveCompleted: StateFlow<Boolean> = _isSaveCompleted

    init {

        val id = UUID.randomUUID().toString()
        _workout.value = Workout(id = id)

        viewModelScope.launch {
            if (workoutId != "" && workoutId != null) {
                _workout.value = workoutId?.let { workoutRepository.getById(it) }
                workoutDetailRepository.getByWorkoutIdFlow(workoutId!!).collect {
                    _workoutDetailsList.addAll(it)
                }

            }
        }

        viewModelScope.launch {
            _workout.value?.name = generateName()
        }
    }

    fun setWorkoutName(name: String) {
        _workout.value = _workout.value?.copy(name = name)
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
            _workoutDetailsList.add(workoutDetail)
        }
    }

    fun saveWorkout() {
        if (workoutId == "") {
            viewModelScope.launch {
                workoutDetailsList.toList().forEach {
                    val detailToAdd = _workout.value?.id.let { id -> it.copy(workoutId = id!!) }
                    workoutDetailRepository.insert(detailToAdd)
                }
                _isSaveCompleted.value = true
            }
        } else {
            viewModelScope.launch {
                _workout.value?.let { workoutRepository.update(it) }
                workoutDetailsList.forEach {
                    if (it.id == "") {
                        val detailToAdd = _workout.value?.id.let { id -> it.copy(workoutId = id!!) }
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
        val index = _workoutDetailsList.indexOfFirst { it.exerciseId == updatedDetail.exerciseId }
        if (index != -1) {
            _workoutDetailsList[index] = updatedDetail
        }
    }

    suspend fun getExerciseName(exerciseId: String): String {
        return exerciseRepository.getExerciseName(exerciseId)
    }

    fun deleteWorkoutDetail(workoutDetail: WorkoutDetail) {
        viewModelScope.launch {
            workoutDetailRepository.delete(workoutDetail)
        }
    }
}