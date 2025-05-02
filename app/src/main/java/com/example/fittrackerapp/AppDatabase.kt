package com.example.fittrackerapp

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.fittrackerapp.entities.*

@Database(
    entities = [
        Exercise::class,
        Workout::class,
        WorkoutDetail::class,
        CompletedWorkout::class,
        CompletedExercise::class,
        LastWorkout::class,
        com.example.fittrackerapp.entities.Set::class,
        Type::class
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
    abstract fun lastWorkoutDao(): LastWorkoutDao
}