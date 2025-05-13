package com.example.fittrackerapp.uielements.usedworkouts

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.fittrackerapp.abstractclasses.BaseWorkout
import com.example.fittrackerapp.abstractclasses.repositories.WorkoutsAndExercisesRepository
import com.example.fittrackerapp.entities.Exercise
import com.example.fittrackerapp.entities.ExerciseRepository
import com.example.fittrackerapp.entities.Workout
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class UsedWorkoutsViewModel(
    private val workoutsAndExercisesRepository: WorkoutsAndExercisesRepository,
    private val exerciseRepository: ExerciseRepository
): ViewModel() {

    private val _favouriteWorkouts = MutableStateFlow<List<BaseWorkout>>(emptyList())
    val favouriteWorkouts: StateFlow<List<BaseWorkout>> = _favouriteWorkouts

    private val _workoutsList = MutableStateFlow<List<BaseWorkout>>(emptyList())
    val workoutsList: StateFlow<List<BaseWorkout>> = _workoutsList

    private val _selectedWorkout = MutableStateFlow<BaseWorkout?>(null)
    val selectedWorkout: StateFlow<BaseWorkout?> = _selectedWorkout

    init {
        viewModelScope.launch {
            workoutsAndExercisesRepository.getFavouritesFlow().collect {
                _favouriteWorkouts.value = it
            }
        }
        viewModelScope.launch {
            workoutsAndExercisesRepository.getUsedExceptFavouritesFlow().collect {
                _workoutsList.value = it.sortedByDescending { bw -> bw.lastUsedDate }
            }
        }
    }

    fun addFavouriteWorkout(workout: BaseWorkout): Boolean {

        if (_favouriteWorkouts.value.size < 10 && isUnique(workout)) {
            if (workout is Workout) {
                val workoutToAdd = workout.copy(isFavourite = true)
                viewModelScope.launch {
                    workoutsAndExercisesRepository.addFavourite(workoutToAdd)
                }
            }
            else if (workout is Exercise) {
                val exerciseToAdd = workout.copy(isFavourite = true)
                viewModelScope.launch {
                    workoutsAndExercisesRepository.addFavourite(exerciseToAdd)
                }
            }
            return true
        }

        return false
    }

    fun removeFavouriteWorkout(workout: BaseWorkout) {
        if (workout is Workout) {
            val workoutToAdd = workout.copy(isFavourite = false)
            viewModelScope.launch {
                workoutsAndExercisesRepository.addFavourite(workoutToAdd)
            }
        }
        else if (workout is Exercise) {
            val exerciseToAdd = workout.copy(isFavourite = false)
            viewModelScope.launch {
                workoutsAndExercisesRepository.addFavourite(exerciseToAdd)
            }
        }
    }

    fun isUnique(workout: BaseWorkout): Boolean {
        return _favouriteWorkouts.value.none {
            it.id == workout.id
                    && it.javaClass.simpleName == workout.javaClass.simpleName }
    }

    fun removeWorkoutFromUsed(workout: BaseWorkout) {
        viewModelScope.launch {
            workoutsAndExercisesRepository.removeFromUsed(workout)
        }
    }

    fun deleteWorkout(workout: BaseWorkout) {
        viewModelScope.launch {
            workoutsAndExercisesRepository.delete(workout)
        }
    }

    fun setSelectedWorkout(baseWorkout: BaseWorkout) {
        _selectedWorkout.value = baseWorkout
    }

    fun setExerciseName(name: String) {
        _selectedWorkout.value?.name ?: name
        viewModelScope.launch {
            exerciseRepository.update(_selectedWorkout.value as Exercise)
        }
    }

    fun saveExerciseIcon(exercise: Exercise, uri: Uri, context: Context) {
        try {
            val subDir = File(context.filesDir, "exercise_images")
            if (!subDir.exists()) subDir.mkdirs()

            val inputStream = context.contentResolver.openInputStream(uri)

            if (inputStream == null) {
                Toast.makeText(context, "Не удалось открыть файл. Попробуйте снова.", Toast.LENGTH_SHORT).show()
                return
            }

            val originalBitmap = BitmapFactory.decodeStream(inputStream)

            // 2. Масштабируем до нужного размера
            val resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, 48, 48, true)

            // 3. Сохраняем его в файл
            val fileName = "image_${exercise.name}.jpg"
            val file = File(subDir, fileName)

            FileOutputStream(file).use { output ->
                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, output)
            }

            val relativePath = "exercise_images/$fileName"

            val exerciseToChange = exercise.copy(iconPath = relativePath)
            viewModelScope.launch {
                exerciseRepository.update(exerciseToChange)
                _selectedWorkout.value = exerciseToChange
                _workoutsList.value = workoutsAndExercisesRepository.getUsedExceptFavourites()
                Toast.makeText(context, "Иконка сохранена.", Toast.LENGTH_SHORT).show()
            }

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Произошла ошибка", Toast.LENGTH_SHORT).show()
        }
    }

    fun saveExerciseVideo(exercise: Exercise, uri: Uri, context: Context) {
        try {
            val subDir = File(context.filesDir, "exercise_videos")
            if (!subDir.exists()) subDir.mkdirs()

            val inputStream = context.contentResolver.openInputStream(uri)

            if (inputStream == null) {
                Toast.makeText(context, "Не удалось открыть файл. Попробуйте снова.", Toast.LENGTH_SHORT).show()
                return
            }

            val fileName = "video_${exercise.name}.jpg"
            val file = File(subDir, fileName)

            inputStream.use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }

            val relativePath = "exercise_videos/$fileName"

            val exerciseToChange = exercise.copy(videoPath = relativePath)
            viewModelScope.launch {
                exerciseRepository.update(exerciseToChange)
                _selectedWorkout.value = exerciseToChange
                _workoutsList.value = workoutsAndExercisesRepository.getUsedExceptFavourites()
                Toast.makeText(context, "Видеоролик сохранён.", Toast.LENGTH_SHORT).show()
            }

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Произошла ошибка", Toast.LENGTH_SHORT).show()
        }
    }

    fun deleteExerciseIcon(context: Context, exercise: Exercise) {
        val file = File(context.filesDir, exercise.iconPath)
        if (file.exists()) {
            file.delete()
            val exerciseToChange = exercise.copy(iconPath = null)
            viewModelScope.launch {
                exerciseRepository.update(exerciseToChange)
                _selectedWorkout.value = exerciseToChange
                _workoutsList.value = workoutsAndExercisesRepository.getUsedExceptFavourites()
            }
            Toast.makeText(context, "Изображение удалено", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Произошла ошибка", Toast.LENGTH_SHORT).show()
        }
    }

    fun deleteExerciseVideo(context: Context, exercise: Exercise) {
        val file = File(context.filesDir, exercise.videoPath)
        if (file.exists()) {
            file.delete()
            val exerciseToChange = exercise.copy(videoPath = null)
            viewModelScope.launch {
                exerciseRepository.update(exerciseToChange)
                _selectedWorkout.value = exerciseToChange
            }
            Toast.makeText(context, "Изображение удалено", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Произошла ошибка", Toast.LENGTH_SHORT).show()
        }
    }
}

class UsedWorkoutsViewModelFactory(
    private val workoutsAndExercisesRepository: WorkoutsAndExercisesRepository,
    private val exerciseRepository: ExerciseRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UsedWorkoutsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UsedWorkoutsViewModel(workoutsAndExercisesRepository, exerciseRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}