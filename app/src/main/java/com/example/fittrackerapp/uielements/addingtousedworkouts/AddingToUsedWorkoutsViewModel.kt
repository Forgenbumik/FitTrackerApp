package com.example.fittrackerapp.uielements.addingtousedworkouts

import android.content.Context
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fittrackerapp.entities.exercise.Exercise
import com.example.fittrackerapp.entities.exercise.ExerciseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@HiltViewModel
@RequiresApi(Build.VERSION_CODES.O)
class AddingToUsedWorkoutsViewModel @Inject constructor(
    private val exerciseRepository: ExerciseRepository
): ViewModel() {

    var _exerciseNames: List<String> = emptyList()

    @RequiresApi(Build.VERSION_CODES.O)
    private val _addingExercise = MutableStateFlow<Exercise?>(Exercise())
    @RequiresApi(Build.VERSION_CODES.O)
    val addingExercise: StateFlow<Exercise?> = _addingExercise

    init {
        viewModelScope.launch {
            exerciseRepository.getAllExerciseNamesFlow().collect {
                _exerciseNames = it
                generateName()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun createNewExercise(): Boolean {
        if (_exerciseNames.none { it.equals(_addingExercise.value?.name, ignoreCase = true) }) {
            viewModelScope.launch {
                if (addingExercise.value?.id == "") {
                    _addingExercise.value?.let { exerciseRepository.insert(it) }
                }
                else {
                    addingExercise.value?.let { exerciseRepository.update(it) }
                }
            }
            _addingExercise.value = null
            return true
        }
        return false
    }

    init {
        viewModelScope.launch {
            generateName()
        }
    }

    fun addSelectedExercise(exerciseId: String) {
        viewModelScope.launch {
            exerciseRepository.updateExerciseUsingById(exerciseId, true)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun setExerciseName(name: String) {
        _addingExercise.value = _addingExercise.value?.copy(name = name)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun generateName() {
        _addingExercise.value?.name = "Упражнение ${_exerciseNames.size + 1}"
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

            val fileName = "image_${exercise.name}.jpg"
            val file = File(subDir, fileName)

            inputStream.use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }

            val relativePath = "exercise_images/$fileName"

            val exerciseToChange = exercise.copy(iconPath = relativePath)
            viewModelScope.launch {
                exerciseRepository.update(exerciseToChange)
            }
            Toast.makeText(context, "Иконка сохранена.", Toast.LENGTH_SHORT).show()


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
            }

            Toast.makeText(context, "Видеоролик сохранён.", Toast.LENGTH_SHORT).show()

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Произошла ошибка", Toast.LENGTH_SHORT).show()
        }
    }

    fun deleteExerciseIcon(context: Context, exercise: Exercise) {

        val file = exercise.iconPath?.let { File(context.filesDir, it) }
        if (file != null) {
            if (file.exists()) {
                file.delete()
                val exerciseToChange = exercise.copy(iconPath = "")
                viewModelScope.launch {
                    exerciseRepository.update(exerciseToChange)
                }
                Toast.makeText(context, "Изображение удалено", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Произошла ошибка", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun deleteExerciseVideo(context: Context, exercise: Exercise) {

        val file = exercise.videoPath?.let { File(context.filesDir, it) }
        if (file != null) {
            if (file.exists()) {
                file.delete()
                val exerciseToChange = exercise.copy(videoPath = "")
                viewModelScope.launch {
                    exerciseRepository.update(exerciseToChange)
                }
                Toast.makeText(context, "Видео удалено", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Произошла ошибка", Toast.LENGTH_SHORT).show()
            }
        }
    }
}