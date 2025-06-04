package com.example.fittrackerapp

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.fittrackerapp.entities.completedexercise.CompletedExercise
import com.example.fittrackerapp.entities.completedexercise.CompletedExerciseDao
import com.example.fittrackerapp.entities.completedworkout.CompletedWorkout
import com.example.fittrackerapp.entities.completedworkout.CompletedWorkoutDao
import com.example.fittrackerapp.entities.exercise.Exercise
import com.example.fittrackerapp.entities.exercise.ExerciseDao
import com.example.fittrackerapp.entities.set.Set
import com.example.fittrackerapp.entities.set.SetDao
import com.example.fittrackerapp.entities.workout.Workout
import com.example.fittrackerapp.entities.workout.WorkoutDao
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID
import javax.inject.Inject

@HiltAndroidTest
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class ComplexDaoTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var db: AppDatabase
    @Inject
    lateinit var workoutDao: WorkoutDao
    @Inject
    lateinit var exerciseDao: ExerciseDao
    @Inject
    lateinit var completedWorkoutDao: CompletedWorkoutDao
    @Inject
    lateinit var completedExerciseDao: CompletedExerciseDao
    @Inject
    lateinit var setDao: SetDao

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        completedExerciseDao = db.completedExerciseDao()
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun complexInsertAndDeleteItems() = runBlocking {

        val workoutId = UUID.randomUUID().toString()
        checkInsertWorkout(workoutId)

        val exercise1Id = UUID.randomUUID().toString()
        checkInsertExercise(exercise1Id, "Классическая тяга")

        val exercise2Id = UUID.randomUUID().toString()
        checkInsertExercise(exercise2Id, "Присед")

        val completedWorkoutId = UUID.randomUUID().toString()
        checkInsertCompletedWorkout(completedWorkoutId, workoutId)

        val completedExercise1Id = UUID.randomUUID().toString()
        checkInsertCompletedExercise(completedExercise1Id, exercise1Id)

        val set1Id = UUID.randomUUID().toString()
        checkInsertSet1(set1Id, completedExercise1Id)

        val set2Id = UUID.randomUUID().toString()
        checkInsertSet1(set2Id, completedExercise1Id)

        val set3Id = UUID.randomUUID().toString()
        checkInsertSet1(set3Id, completedExercise1Id)

        val setsNumber1 = completedExerciseDao.getSetsNumber(completedExercise1Id)
        assertEquals(3, setsNumber1)

        val totalReps1 = completedExerciseDao.getTotalReps(completedExercise1Id)
        assertEquals(30, totalReps1)

        val completedExercise2Id = UUID.randomUUID().toString()
        checkInsertCompletedExercise(completedExercise2Id, exercise2Id)

        val set4Id = UUID.randomUUID().toString()
        checkInsertSet2(set4Id, completedExercise2Id)

        val set5Id = UUID.randomUUID().toString()
        checkInsertSet2(set5Id, completedExercise2Id)

        val set6Id = UUID.randomUUID().toString()
        checkInsertSet2(set6Id, completedExercise2Id)

        val setsNumber2 = completedExerciseDao.getSetsNumber(completedExercise2Id)
        assertEquals(3, setsNumber2)

        val totalReps2 = completedExerciseDao.getTotalReps(completedExercise2Id)
        assertEquals(24, totalReps2)
    }

    private fun checkInsertWorkout(id: String) = runBlocking {
        var workout = Workout(id = id, "Ноги")
        workoutDao.insert(workout)
        workout = workoutDao.getById(id)

        assertEquals(id, workout.id)
    }

    private fun checkInsertExercise(id: String, name: String) = runBlocking {
        var exercise = Exercise(id = id, name = name)
        exerciseDao.insert(exercise)
        exercise = exerciseDao.getById(id)

        assertEquals(id, exercise.id)
    }

    private fun checkInsertCompletedWorkout(id: String, workoutId: String) = runBlocking {
        var completedWorkout = CompletedWorkout(id = id, workoutId = workoutId)
        completedWorkoutDao.insert(completedWorkout)
        completedWorkout = completedWorkoutDao.getById(id)

        assertEquals(id, completedWorkout.id)
    }

    private suspend fun checkInsertCompletedExercise(completedExerciseId: String, exerciseId: String) {
        var completedExercise = CompletedExercise(id = completedExerciseId, exerciseId = exerciseId)
        completedExerciseDao.insert(completedExercise)
        completedExercise = completedExerciseDao.getById(completedExerciseId)

        assertEquals(completedExerciseId, completedExercise.id)
    }

    private suspend fun checkInsertSet1(setId: String, completedExerciseId: String) {
        var set = Set(id = setId, completedExerciseId = completedExerciseId, reps = 10, setNumber = 1)
        setDao.insert(set)
        set = setDao.getById(setId)

        assertEquals(setId, set.id)
    }

    private suspend fun checkInsertSet2(setId: String, completedExerciseId: String) {
        var set = Set(id = setId, completedExerciseId = completedExerciseId, reps = 8, setNumber = 1)
        setDao.insert(set)
        set = setDao.getById(setId)

        assertEquals(setId, set.id)
    }
}