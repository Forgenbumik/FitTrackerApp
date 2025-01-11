package com.example.fittrackerapp.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.example.fittrackerapp.abstractclasses.BaseWorkout

@Entity(
    tableName = "exercises",
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["category_id"],
            onUpdate = ForeignKey.CASCADE
        )
    ]
)
data class Exercise(
    @PrimaryKey override val id: Int,
    @ColumnInfo override val type: String,
    @ColumnInfo override val name: String,
    @ColumnInfo(name = "category_id") override val categoryId: Int
): BaseWorkout()