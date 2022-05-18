package it.polito.MAD.group06.models.skill

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Skill::class], version = 1)
abstract class SkillDatabase : RoomDatabase() {
    abstract fun skillDao(): SkillDAO

    companion object {
        @Volatile
        private var INSTANCE: SkillDatabase? = null

        /**
         * There's a single instance of the database and
         * this method checks whether it's already been instantiated and,
         * eventually, returns the reference to the unique object shared among
         * all the callers.
         */
        fun getDatabase(context: Context): SkillDatabase =
            (
                    INSTANCE ?: synchronized(this) {
                        val i = INSTANCE ?: Room.databaseBuilder(
                            context.applicationContext,
                            SkillDatabase::class.java,
                            "skillDB"
                        ).build()
                        INSTANCE = i
                        INSTANCE
                    }
                    )!!

    }
}