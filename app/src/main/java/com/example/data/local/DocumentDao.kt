package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentDao {

    @Query("SELECT * FROM documents ORDER BY isPinned DESC, updatedAt DESC")
    fun getAllDocuments(): Flow<List<DocumentEntity>>

    @Query("SELECT * FROM documents WHERE id = :id LIMIT 1")
    suspend fun getDocumentById(id: String): DocumentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(document: DocumentEntity)

    @Delete
    suspend fun delete(document: DocumentEntity)

    @Query("DELETE FROM documents WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("UPDATE documents SET isPinned = :isPinned WHERE id = :id")
    suspend fun updatePinned(id: String, isPinned: Boolean)

    @Query("SELECT COUNT(*) FROM documents")
    suspend fun getCount(): Int
}
