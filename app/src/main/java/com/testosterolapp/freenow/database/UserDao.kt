package com.testosterolapp.freenow.database

import androidx.room.*
import com.testosterolapp.freenow.data.User
import com.testosterolapp.freenow.data.Vehicle

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(user: User)

    @Update
    suspend fun update(user: User)

    @Query("Select * from user")
    fun getUser(): User?

    @Query("SELECT * FROM user")
    fun getAll(): List<User?>?

}