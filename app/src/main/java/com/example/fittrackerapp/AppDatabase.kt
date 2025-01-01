package com.example.fittrackerapp

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.fittrackerapp.daoInterfaces.*
import com.example.fittrackerapp.entities.*

@Database(
    entities = [
        Exercise::class,
        Category::class,
        Workout::class,
        WorkoutDetail::class,
        CompletedWorkout::class,
        CompletedExercise::class,
        Set::class,
        CompletedWorkoutDetail::class,
        CompletedExerciseDetail::class
    ], version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun exerciseDao(): ExerciseDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun workoutDetailDao(): WorkoutDetailDao
    abstract fun setDao(): SetDao
    abstract fun completedExerciseDao(): CompletedExerciseDao
    abstract fun completedExerciseDetailDao(): CompletedExerciseDetailDao
    abstract fun completedWorkoutDao(): CompletedWorkoutDao
    abstract fun completedWorkoutDetailDao(): CompletedWorkoutDetailDao
}

object Dependencies {

    private lateinit var applicationContext: Context

    fun init(context: Context) {
        applicationContext = context
    }

    private val appDatabase: AppDatabase by lazy {
        Room.databaseBuilder(applicationContext, AppDatabase::class.java, "database.db")
            .createFromAsset("databases/training_journal.db")
            .build()
    }
}