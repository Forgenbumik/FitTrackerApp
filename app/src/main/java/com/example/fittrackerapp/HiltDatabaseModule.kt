package com.example.fittrackerapp

import android.content.Context
import androidx.room.Room
import com.example.fittrackerapp.abstractclasses.repositories.CompletedWorkoutsAndExercisesRepository
import com.example.fittrackerapp.abstractclasses.repositories.WorkoutsAndExercisesRepository
import com.example.fittrackerapp.entities.completedexercise.CompletedExerciseRepository
import com.example.fittrackerapp.entities.completedworkout.CompletedWorkoutRepository
import com.example.fittrackerapp.entities.exercise.ExerciseRepository
import com.example.fittrackerapp.entities.LastWorkoutRepository
import com.example.fittrackerapp.entities.set.SetRepository
import com.example.fittrackerapp.entities.workoutdetail.WorkoutDetailRepository
import com.example.fittrackerapp.entities.workout.WorkoutRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDb(@ApplicationContext context: Context): AppDatabase {

        return Room.databaseBuilder(
            context,
            AppDatabase::class.java, "database.db"
        )
            .fallbackToDestructiveMigration(true)
            .createFromAsset("asset.db")
            .build()
    }

    @Provides
    @Singleton
    fun provideLastWorkoutRepository(db: AppDatabase): LastWorkoutRepository {
        return LastWorkoutRepository(db.lastWorkoutDao(), db.workoutDao(), db.exerciseDao())
    }

    @Provides
    @Singleton
    fun provideCompletedWorkoutsAndExercisesRepository(db: AppDatabase): CompletedWorkoutsAndExercisesRepository {
        return CompletedWorkoutsAndExercisesRepository(db.lastWorkoutDao(),
            db.completedWorkoutDao(),
            db.completedExerciseDao())
    }

    @Provides
    @Singleton
    fun provideWorkoutsAndExercisesRepository(db: AppDatabase): WorkoutsAndExercisesRepository {
        return WorkoutsAndExercisesRepository(db.workoutDao(), db.exerciseDao(), db.workoutDetailDao())
    }
}