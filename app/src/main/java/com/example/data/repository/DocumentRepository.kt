package com.example.data.repository

import com.example.data.local.DocumentDao
import com.example.data.local.DocumentEntity
import com.example.data.model.DocTemplates
import com.example.data.model.WordDocument
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class DocumentRepository(private val dao: DocumentDao) {

    val allDocuments: Flow<List<WordDocument>> = dao.getAllDocuments().map { list ->
        list.map { it.toDomain() }
    }

    suspend fun initializeDefaultDocumentsIfEmpty() {
        if (dao.getCount() == 0) {
            val proposal = DocTemplates.getBusinessProposalTemplate()
            val resume = DocTemplates.getResumeTemplate()
            val minutes = DocTemplates.getMeetingMinutesTemplate()
            dao.insertOrUpdate(DocumentEntity.fromDomain(proposal))
            dao.insertOrUpdate(DocumentEntity.fromDomain(resume))
            dao.insertOrUpdate(DocumentEntity.fromDomain(minutes))
        }
    }

    suspend fun getDocument(id: String): WordDocument? {
        return dao.getDocumentById(id)?.toDomain()
    }

    suspend fun saveDocument(doc: WordDocument) {
        val updated = doc.copy(updatedAt = System.currentTimeMillis())
        dao.insertOrUpdate(DocumentEntity.fromDomain(updated))
    }

    suspend fun deleteDocument(id: String) {
        dao.deleteById(id)
    }

    suspend fun togglePin(id: String, currentPinned: Boolean) {
        dao.updatePinned(id, !currentPinned)
    }

    suspend fun duplicateDocument(doc: WordDocument): WordDocument {
        val duplicated = doc.copy(
            id = UUID.randomUUID().toString(),
            title = "${doc.title} (Copy)",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        dao.insertOrUpdate(DocumentEntity.fromDomain(duplicated))
        return duplicated
    }
}
