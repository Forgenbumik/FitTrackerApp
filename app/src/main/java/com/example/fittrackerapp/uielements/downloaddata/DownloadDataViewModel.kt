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

    suspend fun downloadData(context: Context) {

        val workoutList = workoutService.getWorkoutsForUserOrDefault()
        Log.d("DownloadData", "Workouts: ${workoutList.size}")
        workoutDao.insertAll(workoutList)
        Log.d("DownloadData", "Inserted workouts into Room: ${workoutList.size}")

        val exerciseList = exerciseService.getExercisesForUserOrDefault()
        Log.d("DownloadData", "Workouts: ${exerciseList.size}")

        exerciseList.forEach {
            downloadExerciseMediaIfNeeded(context, it.iconPath, it.videoPath)
        }

        exerciseDao.insertAll(exerciseList)
        Log.d("DownloadData", "Inserted workouts into Room: ${exerciseList.size}")

        val completedWorkoutList = completedWorkoutService.getAll().toObjects(CompletedWorkout::class.java)
        Log.d("DownloadData", "Workouts: ${completedWorkoutList.size}")
        completedWorkoutDao.insertAll(completedWorkoutList)
        Log.d("DownloadData", "Inserted workouts into Room: ${completedWorkoutList.size}")

        val completedExerciseList = completedExerciseService.getAll().toObjects(CompletedExercise::class.java)
        Log.d("DownloadData", "Workouts: ${completedExerciseList.size}")
        completedExerciseDao.insertAll(completedExerciseList)
        Log.d("DownloadData", "Inserted workouts into Room: ${completedExerciseList.size}")

        val setList = setService.getAll().toObjects(Set::class.java)
        Log.d("DownloadData", "Workouts: ${setList.size}")
        setDao.insertAll(setList)
        Log.d("DownloadData", "Inserted workouts into Room: ${setList.size}")

        val workoutDetailList = workoutDetailService.getAll().toObjects(com.example.fittrackerapp.entities.workoutdetail.WorkoutDetail::class.java)
        Log.d("DownloadData", "Workouts: ${workoutDetailList.size}")
        workoutDetailDao.insertAll(workoutDetailList)
        Log.d("DownloadData", "Inserted workouts into Room: ${workoutDetailList.size}")
        _isDataDownloaded.value = true
    }

    private suspend fun downloadExerciseMediaIfNeeded(context: Context, iconPath: String?, videoPath: String?) {
        iconPath?.let {
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

    private fun getLocalFile(context: Context, relativePath: String): File {
        val subfolder = if (relativePath.startsWith("images/")) "exercise_images"
        else if (relativePath.startsWith("videos/")) "exercise_videos"
        else "other"

        val filename = relativePath.substringAfterLast("/")
        val dir = File(context.filesDir, subfolder)
        if (!dir.exists()) dir.mkdirs()

        return File(dir, filename)
    }

    private suspend fun downloadAndSaveFile(
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