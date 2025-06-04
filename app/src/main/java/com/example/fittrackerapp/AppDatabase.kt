package com.example.fittrackerapp

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.fittrackerapp.entities.*
import com.example.fittrackerapp.entities.set.Set
import com.example.fittrackerapp.entities.completedexercise.CompletedExercise
import com.example.fittrackerapp.entities.completedexercise.CompletedExerciseDao
import com.example.fittrackerapp.entities.completedworkout.CompletedWorkout
import com.example.fittrackerapp.entities.completedworkout.CompletedWorkoutDao
import com.example.fittrackerapp.entities.exercise.Exercise
import com.example.fittrackerapp.entities.exercise.ExerciseDao
import com.example.fittrackerapp.entities.set.SetDao
import com.example.fittrackerapp.entities.workout.Workout
import com.example.fittrackerapp.entities.workout.WorkoutDao
import com.example.fittrackerapp.entities.workoutdetail.WorkoutDetail
import com.example.fittrackerapp.entities.workoutdetail.WorkoutDetailDao

@Database(
    entities = [
        Exercise::class,
        Workout::class,
        WorkoutDetail::class,
        CompletedWorkout::class,
        CompletedExercise::class,
        LastWorkout::class,
        Set::class,
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