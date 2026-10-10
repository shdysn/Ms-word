package com.example

import com.example.data.local.DocJsonConverter
import com.example.data.model.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ExampleUnitTest {
    @Test
    fun testDocumentStatsCalculation() {
        val doc = WordDocument(
            title = "Sample Test Document",
            blocks = listOf(
                DocumentBlock.Paragraph(text = "Hello world this is a test document with ten words now."),
                DocumentBlock.Callout(text = "Another note with five words.", type = CalloutType.INFO)
            )
        )
        val stats = calculateDocumentStats(doc)
        assertTrue(stats.words >= 15)
        assertTrue(stats.charactersWithSpaces > 0)
        assertTrue(stats.paragraphs >= 2)
    }

    @Test
    fun testJsonSerializationAndDeserialization() {
        val blocks = listOf(
            DocumentBlock.Paragraph(
                text = "Heading title",
                style = BlockStyle.HEADING_1,
                isBold = true,
                fontSizePt = 20
            ),
            DocumentBlock.Table(
                rows = listOf(
                    listOf("Header 1", "Header 2"),
                    listOf("Data 1", "Data 2")
                ),
                style = TableStyle.BLUE_OFFICE
            ),
            DocumentBlock.Callout(
                text = "Important warning notice",
                type = CalloutType.WARNING
            ),
            DocumentBlock.Divider(style = DividerStyle.SOLID),
            DocumentBlock.PageBreak()
        )

        val json = DocJsonConverter.blocksToJson(blocks)
        assertNotNull(json)
        assertTrue(json.contains("Heading title"))
        assertTrue(json.contains("Header 1"))

        val restored = DocJsonConverter.jsonToBlocks(json)
        assertEquals(5, restored.size)
        assertTrue(restored[0] is DocumentBlock.Paragraph)
        assertEquals("Heading title", (restored[0] as DocumentBlock.Paragraph).text)
        assertTrue(restored[1] is DocumentBlock.Table)
        assertEquals(2, (restored[1] as DocumentBlock.Table).rows.size)
        assertTrue(restored[2] is DocumentBlock.Callout)
        assertTrue(restored[3] is DocumentBlock.Divider)
        assertTrue(restored[4] is DocumentBlock.PageBreak)
    }

    @Test
    fun testTemplatesGeneration() {
        val resume = DocTemplates.getResumeTemplate()
        assertNotNull(resume)
        assertTrue(resume.blocks.isNotEmpty())

        val proposal = DocTemplates.getBusinessProposalTemplate()
        assertNotNull(proposal)
        assertTrue(proposal.blocks.isNotEmpty())

        val blank = DocTemplates.getBlankDocument()
        assertNotNull(blank)
        assertEquals(1, blank.blocks.size)
    }
}
