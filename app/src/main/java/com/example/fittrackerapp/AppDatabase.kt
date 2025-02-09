package com.example.fittrackerapp

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.fittrackerapp.abstractclasses.daointerfaces.CompletedWorkoutsAndExercisesDao
import com.example.fittrackerapp.abstractclasses.daointerfaces.WorkoutsAndExercisesDao
import com.example.fittrackerapp.abstractclasses.repositories.CompletedWorkoutsAndExercisesRepository
import com.example.fittrackerapp.abstractclasses.repositories.WorkoutsAndExercisesRepository
import com.example.fittrackerapp.entities.*
import com.example.fittrackerapp.entities.Set
import com.example.fittrackerapp.entities.daoInterfaces.CompletedExerciseDao
import com.example.fittrackerapp.entities.daoInterfaces.CompletedWorkoutDao
import com.example.fittrackerapp.entities.daoInterfaces.ExerciseDao
import com.example.fittrackerapp.entities.daoInterfaces.SetDao
import com.example.fittrackerapp.entities.daoInterfaces.WorkoutDao
import com.example.fittrackerapp.entities.daoInterfaces.WorkoutDetailDao

@Database(
    entities = [
        Exercise::class,
        Workout::class,
        WorkoutDetail::class,
        CompletedWorkout::class,
        CompletedExercise::class,
        Set::class
    ], version = 1
)
@TypeConverters(DateTimeConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun workoutDetailDao(): WorkoutDetailDao
    abstract fun setDao(): SetDao
    abstract fun completedExerciseDao(): CompletedExerciseDao
    abstract fun completedWorkoutDao(): CompletedWorkoutDao
    abstract fun workoutsAndExercisesDao(): WorkoutsAndExercisesDao
    abstract fun completedWorkoutsAndExercisesDao(): CompletedWorkoutsAndExercisesDao
}

object Dependencies {

    private lateinit var applicationContext: Context

    fun init(context: Context) {
        applicationContext = context
    }

    private val appDatabase: AppDatabase by lazy {
        Room.databaseBuilder(applicationContext, AppDatabase::class.java, "database.db")
            .createFromAsset("databases/database.db")
            .build()
    }
    val workoutsAndExercisesRepository: WorkoutsAndExercisesRepository by lazy { WorkoutsAndExercisesRepository(appDatabase.workoutsAndExercisesDao()) }

    val completedWorkoutsAndExercisesRepository: CompletedWorkoutsAndExercisesRepository by lazy {CompletedWorkoutsAndExercisesRepository(appDatabase.completedWorkoutsAndExercisesDao())}
}