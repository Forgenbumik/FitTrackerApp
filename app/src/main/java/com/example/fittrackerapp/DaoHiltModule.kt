package com.example.fittrackerapp

import android.content.Context
import androidx.room.Room
import com.example.fittrackerapp.entities.LastWorkoutDao
import com.example.fittrackerapp.entities.completedexercise.CompletedExerciseDao
import com.example.fittrackerapp.entities.completedworkout.CompletedWorkoutDao
import com.example.fittrackerapp.entities.exercise.ExerciseDao
import com.example.fittrackerapp.entities.set.SetDao
import com.example.fittrackerapp.entities.workout.WorkoutDao
import com.example.fittrackerapp.entities.workoutdetail.WorkoutDetailDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object DaoHiltModule {

    @Provides
    @Singleton
    fun provideCompletedExerciseDao(db: AppDatabase): CompletedExerciseDao = db.completedExerciseDao()

    @Provides
    @Singleton
    fun provideCompletedWorkoutDao(db: AppDatabase): CompletedWorkoutDao = db.completedWorkoutDao()

    @Provides
    @Singleton
    fun provideExerciseDao(db: AppDatabase): ExerciseDao = db.exerciseDao()

    @Provides
    @Singleton
    fun provideWorkoutDao(db: AppDatabase): WorkoutDao = db.workoutDao()

    @Provides
    @Singleton
    fun provideSetDao(db: AppDatabase): SetDao = db.setDao()

    @Provides
    @Singleton
    fun provideLastWorkoutDao(db: AppDatabase): LastWorkoutDao = db.lastWorkoutDao()

    @Provides
    @Singleton
    fun provideWorkoutDetailDao(db: AppDatabase): WorkoutDetailDao = db.workoutDetailDao()
}