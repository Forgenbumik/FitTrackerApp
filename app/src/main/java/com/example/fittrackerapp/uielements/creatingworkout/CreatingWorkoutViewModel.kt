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

    private var workoutId: String get() = savedStateHandle["workoutId"] ?: ""
        set(value) {
            savedStateHandle["workoutId"] = value
        }

    private var _workoutDetailsList = mutableStateListOf<WorkoutDetail>()
    val workoutDetailsList: SnapshotStateList<WorkoutDetail> get() = _workoutDetailsList

    private val _selectedExercise = MutableStateFlow(WorkoutDetail())
    val selectedExercise: StateFlow<WorkoutDetail> =_selectedExercise

    @RequiresApi(Build.VERSION_CODES.O)
    private val _workout: MutableStateFlow<Workout> = MutableStateFlow(Workout())
    @RequiresApi(Build.VERSION_CODES.O)
    val workout: StateFlow<Workout?> = _workout

    private val _isSaveCompleted = MutableStateFlow(false)
    val isSaveCompleted: StateFlow<Boolean> = _isSaveCompleted

    init {
        if (workoutId != "") {
            viewModelScope.launch {
                _workout.value = workoutRepository.getById(workoutId)
                workoutDetailRepository.getByWorkoutIdFlow(workoutId).collect {
                    _workoutDetailsList.addAll(it)
                }
            }
        }

        viewModelScope.launch {
            _workout.value?.name = generateName()
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
            _workoutDetailsList.add(workoutDetail)
        }
    }

    fun saveWorkout() {
        viewModelScope.launch {
            if (workoutId == "") {
                workoutId = UUID.randomUUID().toString()
                _workout.value = _workout.value!!.copy(id = workoutId)
                workoutRepository.insert(_workout.value!!)
            }
            else {
                workoutRepository.update(_workout.value!!)
            }

            workoutDetailsList.toList().forEach {
                if (it.id == "") {

                    val workoutDetailToAdd = UUID.randomUUID().toString()

                    val detailToAdd = it.copy(id = workoutDetailToAdd, workoutId = workoutId)
                    workoutDetailRepository.insert(detailToAdd)
                }
                else {
                    workoutDetailRepository.update(it)
                }
            }
            _isSaveCompleted.value = true
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