package com.example.fittrackerapp

import android.content.Context
import android.util.Log
import androidx.room.Room
import com.example.fittrackerapp.abstractclasses.repositories.CompletedWorkoutsAndExercisesRepository
import com.example.fittrackerapp.abstractclasses.repositories.WorkoutsAndExercisesRepository
import com.example.fittrackerapp.entities.CompletedExerciseDao
import com.example.fittrackerapp.entities.CompletedExerciseRepository
import com.example.fittrackerapp.entities.CompletedWorkoutDao
import com.example.fittrackerapp.entities.CompletedWorkoutRepository
import com.example.fittrackerapp.entities.ExerciseDao
import com.example.fittrackerapp.entities.ExerciseRepository
import com.example.fittrackerapp.entities.LastWorkoutDao
import com.example.fittrackerapp.entities.LastWorkoutRepository
import com.example.fittrackerapp.entities.SetDao
import com.example.fittrackerapp.entities.SetRepository
import com.example.fittrackerapp.entities.WorkoutDao
import com.example.fittrackerapp.entities.WorkoutDetailDao
import com.example.fittrackerapp.entities.WorkoutDetailRepository
import com.example.fittrackerapp.entities.WorkoutRepository
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
    fun provideCompletedExerciseRepository(db: AppDatabase): CompletedExerciseRepository {
        return CompletedExerciseRepository(db.completedExerciseDao())
    }

    @Provides
    @Singleton
    fun provideCompletedWorkoutRepository(db: AppDatabase): CompletedWorkoutRepository {
        return CompletedWorkoutRepository(db.completedWorkoutDao())
    }

    @Provides
    @Singleton
    fun provideExerciseRepository(db: AppDatabase): ExerciseRepository {
        return ExerciseRepository(db.exerciseDao())
    }

    @Provides
    @Singleton
    fun provideLastWorkoutRepository(db: AppDatabase): LastWorkoutRepository {
        return LastWorkoutRepository(db.lastWorkoutDao(), db.workoutDao(), db.exerciseDao())
    }

    @Provides
    @Singleton
    fun provideSetRepository(db: AppDatabase): SetRepository {
        return SetRepository(db.setDao())
    }

    @Provides
    @Singleton
    fun provideWorkoutRepository(db: AppDatabase): WorkoutRepository {
        return WorkoutRepository(db.workoutDao())
    }

    @Provides
    @Singleton
    fun provideWorkoutDetailRepository(db: AppDatabase): WorkoutDetailRepository {
        return WorkoutDetailRepository(db.workoutDetailDao())
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