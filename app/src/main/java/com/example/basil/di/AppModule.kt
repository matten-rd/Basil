package com.example.basil.di

import android.app.Application
import androidx.room.Room
import com.example.basil.data.BasilDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(
        app: Application
    ) = Room.databaseBuilder(app, BasilDatabase::class.java, "basil_database")
            .fallbackToDestructiveMigration()
            .build()


    @Provides
    fun provideRecipeDao(db: BasilDatabase) = db.recipeDao()
}