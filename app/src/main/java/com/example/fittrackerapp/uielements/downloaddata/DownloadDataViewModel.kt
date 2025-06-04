package com.example.fittrackerapp.uielements.downloaddata

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fittrackerapp.entities.completedexercise.CompletedExercise
import com.example.fittrackerapp.entities.completedexercise.CompletedExerciseDao
import com.example.fittrackerapp.entities.completedexercise.FirebaseCompletedExerciseService
import com.example.fittrackerapp.entities.completedworkout.CompletedWorkout
import com.example.fittrackerapp.entities.completedworkout.CompletedWorkoutDao
import com.example.fittrackerapp.entities.completedworkout.FirebaseCompletedWorkoutService
import com.example.fittrackerapp.entities.exercise.ExerciseDao
import com.example.fittrackerapp.entities.exercise.FirebaseExerciseService
import com.example.fittrackerapp.entities.set.FirebaseSetService
import com.example.fittrackerapp.entities.set.SetDao
import com.example.fittrackerapp.entities.workout.FirebaseWorkoutService
import com.example.fittrackerapp.entities.workout.WorkoutDao
import com.example.fittrackerapp.entities.workoutdetail.FirebaseWorkoutDetailService
import com.example.fittrackerapp.entities.workoutdetail.WorkoutDetailDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.fittrackerapp.entities.set.Set
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await
import java.io.File

@HiltViewModel
class DownloadDataViewModel @Inject constructor(
    private val workoutService: FirebaseWorkoutService,
    private val exerciseService: FirebaseExerciseService,
    private val completedWorkoutService: FirebaseCompletedWorkoutService,
    private val completedExerciseService: FirebaseCompletedExerciseService,
    private val setService: FirebaseSetService,
    private val workoutDetailService: FirebaseWorkoutDetailService,
    private val workoutDao: WorkoutDao,
    private val exerciseDao: ExerciseDao,
    private val completedWorkoutDao: CompletedWorkoutDao,
    private val completedExerciseDao: CompletedExerciseDao,
    private val setDao: SetDao,
    private val workoutDetailDao: WorkoutDetailDao
    ): ViewModel() {

    private val _isDataDownloaded = MutableStateFlow(false)
    val isDataDownloaded: StateFlow<Boolean> = _isDataDownloaded

    init {
        viewModelScope.launch {
            downloadData()
        }
    }

    suspend fun downloadData() {

        val workoutList = workoutService.getWorkoutsForUserOrDefault()
        workoutDao.insertAll(workoutList)

        val exerciseList = exerciseService.getExercisesForUserOrDefault()

        exerciseList.forEach {
            if (it.iconPath != "") {

            }
        }

        exerciseDao.insertAll(exerciseList)

        val completedWorkoutList = completedWorkoutService.getAll().toObjects(CompletedWorkout::class.java)
        completedWorkoutDao.insertAll(completedWorkoutList)

        val completedExerciseList = completedExerciseService.getAll().toObjects(CompletedExercise::class.java)
        completedExerciseDao.insertAll(completedExerciseList)

        val setList = setService.getAll().toObjects(Set::class.java)
        setDao.insertAll(setList)

        val workoutDetailList = workoutDetailService.getAll().toObjects(com.example.fittrackerapp.entities.workoutdetail.WorkoutDetail::class.java)
        workoutDetailDao.insertAll(workoutDetailList)

        _isDataDownloaded.value = true
    }

    suspend fun downloadExerciseMediaIfNeeded(context: Context, iconPath: String, videoPath: String) {
        iconPath.let {
            val file = getLocalFile(context, it)
            if (!file.exists()) {
                downloadAndSaveFile(it, file)
            }
        }

        videoPath?.let {
            val file = getLocalFile(context, it)
            if (!file.exists()) {
                downloadAndSaveFile(it, file)
            }
        }
    }

    fun getLocalFile(context: Context, relativePath: String): File {
        val subfolder = if (relativePath.startsWith("images/")) "exercise_icons"
        else if (relativePath.startsWith("videos/")) "exercise_videos"
        else "other"

        val filename = relativePath.substringAfterLast("/")
        val dir = File(context.filesDir, subfolder)
        if (!dir.exists()) dir.mkdirs()

        return File(dir, filename)
    }

    suspend fun downloadAndSaveFile(
        relativePath: String,
        targetFile: File
    ): Boolean {
        return try {
            val ref = Firebase.storage.getReference(relativePath)
            ref.getFile(targetFile).await()
            true
        } catch (e: Exception) {
            Log.e("FirebaseDownload", "Ошибка при загрузке $relativePath", e)
            false
        }
    }

}