package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.*

@Entity(tableName = "documents")
data class DocumentEntity(
    @PrimaryKey val id: String,
    val title: String,
    val blocksJson: String,
    val orientation: String,
    val pageSize: String,
    val margins: String,
    val pageTone: String,
    val headerText: String,
    val footerText: String,
    val showPageNumbers: Boolean,
    val isPinned: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
    val author: String
) {
    fun toDomain(): WordDocument {
        return WordDocument(
            id = id,
            title = title,
            blocks = DocJsonConverter.jsonToBlocks(blocksJson),
            orientation = runCatching { PageOrientation.valueOf(orientation) }.getOrDefault(PageOrientation.PORTRAIT),
            pageSize = runCatching { DocPageSize.valueOf(pageSize) }.getOrDefault(DocPageSize.A4),
            margins = runCatching { PageMargins.valueOf(margins) }.getOrDefault(PageMargins.NORMAL),
            pageTone = runCatching { PageTone.valueOf(pageTone) }.getOrDefault(PageTone.WHITE),
            headerText = headerText,
            footerText = footerText,
            showPageNumbers = showPageNumbers,
            isPinned = isPinned,
            createdAt = createdAt,
            updatedAt = updatedAt,
            author = author
        )
    }

    companion object {
        fun fromDomain(doc: WordDocument): DocumentEntity {
            return DocumentEntity(
                id = doc.id,
                title = doc.title,
                blocksJson = DocJsonConverter.blocksToJson(doc.blocks),
                orientation = doc.orientation.name,
                pageSize = doc.pageSize.name,
                margins = doc.margins.name,
                pageTone = doc.pageTone.name,
                headerText = doc.headerText,
                footerText = doc.footerText,
                showPageNumbers = doc.showPageNumbers,
                isPinned = doc.isPinned,
                createdAt = doc.createdAt,
                updatedAt = doc.updatedAt,
                author = doc.author
            )
        }
    }
}
