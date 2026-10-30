package ir.kharjyar.data.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ir.kharjyar.data.local.KharjYarDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideKharjYarDatabase(
        @ApplicationContext context: Context
    ): KharjYarDatabase {
        return Room.databaseBuilder(
            context,
            KharjYarDatabase::class.java,
            KharjYarDatabase.DATABASE_NAME
        )
            .addMigrations(KharjYarDatabase.MIGRATION_1_2)
            .fallbackToDestructiveMigration()
            .build()
    }
}
