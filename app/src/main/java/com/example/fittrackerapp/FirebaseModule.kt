package com.example.fittrackerapp

import com.example.fittrackerapp.entities.completedexercise.FirebaseCompletedExerciseService
import com.example.fittrackerapp.entities.completedworkout.FirebaseCompletedWorkoutService
import com.example.fittrackerapp.entities.exercise.FirebaseExerciseService
import com.example.fittrackerapp.entities.set.FirebaseSetService
import com.example.fittrackerapp.entities.workout.FirebaseWorkoutService
import com.example.fittrackerapp.entities.workoutdetail.FirebaseWorkoutDetailService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    @Provides
    @Singleton
    fun provideFirebaseWorkoutService(
        firestore: FirebaseFirestore,
        @Named("userId") userId: String
    ): FirebaseWorkoutService {
        return FirebaseWorkoutService(firestore, userId)
    }

    @Provides
    @Singleton
    fun provideFirebaseCompletedExerciseService(
        firestore: FirebaseFirestore,
        @Named("userId") userId: String
    ): FirebaseCompletedExerciseService {
        return FirebaseCompletedExerciseService(firestore, userId)
    }

    @Provides
    @Singleton
    fun provideFirebaseExerciseService(
        firestore: FirebaseFirestore,
        @Named("userId") userId: String
        ): FirebaseExerciseService {
        return FirebaseExerciseService(firestore, userId)
    }

    @Provides
    @Singleton
    fun provideFirebaseSetService(
        firestore: FirebaseFirestore,
        @Named("userId") userId: String
    ): FirebaseSetService {
        return FirebaseSetService(firestore, userId)
    }

    @Provides
    @Singleton
    fun provideFirebaseCompletedWorkoutService(
        firestore: FirebaseFirestore,
        @Named("userId") userId: String
    ): FirebaseCompletedWorkoutService {
        return FirebaseCompletedWorkoutService(firestore, userId)
    }

    @Provides
    @Singleton
    fun provideFirebaseWorkoutDetailService(
        firestore: FirebaseFirestore,
        @Named("userId") userId: String
    ): FirebaseWorkoutDetailService {
        return FirebaseWorkoutDetailService(firestore, userId)
    }

    @Provides
    @Singleton
    @Named("userId")
    fun provideUserId(): String {
        return FirebaseAuth.getInstance().currentUser?.uid
            ?: throw IllegalStateException("User must be authenticated")
    }
}