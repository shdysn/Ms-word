package com.example.data.local

import com.example.data.model.*
import org.json.JSONArray
import org.json.JSONObject

object DocJsonConverter {

    fun blocksToJson(blocks: List<DocumentBlock>): String {
        val array = JSONArray()
        for (block in blocks) {
            val obj = JSONObject()
            when (block) {
                is DocumentBlock.Paragraph -> {
                    obj.put("type", "paragraph")
                    obj.put("id", block.id)
                    obj.put("text", block.text)
                    obj.put("style", block.style.name)
                    obj.put("alignment", block.alignment.name)
                    obj.put("lineSpacing", block.lineSpacingMultiplier.toDouble())
                    obj.put("isBold", block.isBold)
                    obj.put("isItalic", block.isItalic)
                    obj.put("isUnderline", block.isUnderline)
                    obj.put("isStrikethrough", block.isStrikethrough)
                    obj.put("isSubscript", block.isSubscript)
                    obj.put("isSuperscript", block.isSuperscript)
                    obj.put("textColorHex", block.textColorHex)
                    obj.put("highlightColorHex", block.highlightColorHex)
                    obj.put("fontSizePt", block.fontSizePt)
                    obj.put("fontFamily", block.fontFamily.name)
                    obj.put("indentLevel", block.indentLevel)
                    obj.put("listType", block.listType.name)
                    obj.put("isChecked", block.isChecked)
                }
                is DocumentBlock.Table -> {
                    obj.put("type", "table")
                    obj.put("id", block.id)
                    obj.put("hasHeaderRow", block.hasHeaderRow)
                    obj.put("style", block.style.name)
                    obj.put("caption", block.caption)
                    val rowsArray = JSONArray()
                    for (row in block.rows) {
                        val rowArray = JSONArray()
                        for (cell in row) {
                            rowArray.put(cell)
                        }
                        rowsArray.put(rowArray)
                    }
                    obj.put("rows", rowsArray)
                }
                is DocumentBlock.Callout -> {
                    obj.put("type", "callout")
                    obj.put("id", block.id)
                    obj.put("text", block.text)
                    obj.put("calloutType", block.type.name)
                    obj.put("title", block.title)
                }
                is DocumentBlock.Divider -> {
                    obj.put("type", "divider")
                    obj.put("id", block.id)
                    obj.put("style", block.style.name)
                }
                is DocumentBlock.PageBreak -> {
                    obj.put("type", "page_break")
                    obj.put("id", block.id)
                }
            }
            array.put(obj)
        }
        return array.toString()
    }

    fun jsonToBlocks(jsonString: String): List<DocumentBlock> {
        val list = mutableListOf<DocumentBlock>()
        if (jsonString.isBlank()) return listOf(DocumentBlock.Paragraph())
        try {
            val array = JSONArray(jsonString)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                when (obj.optString("type")) {
                    "paragraph" -> {
                        list.add(
                            DocumentBlock.Paragraph(
                                id = obj.optString("id"),
                                text = obj.optString("text"),
                                style = runCatching { BlockStyle.valueOf(obj.optString("style", "NORMAL")) }.getOrDefault(BlockStyle.NORMAL),
                                alignment = runCatching { BlockAlignment.valueOf(obj.optString("alignment", "LEFT")) }.getOrDefault(BlockAlignment.LEFT),
                                lineSpacingMultiplier = obj.optDouble("lineSpacing", 1.15).toFloat(),
                                isBold = obj.optBoolean("isBold", false),
                                isItalic = obj.optBoolean("isItalic", false),
                                isUnderline = obj.optBoolean("isUnderline", false),
                                isStrikethrough = obj.optBoolean("isStrikethrough", false),
                                isSubscript = obj.optBoolean("isSubscript", false),
                                isSuperscript = obj.optBoolean("isSuperscript", false),
                                textColorHex = obj.optLong("textColorHex", 0xFF201F1E),
                                highlightColorHex = obj.optLong("highlightColorHex", 0x00000000),
                                fontSizePt = obj.optInt("fontSizePt", 11),
                                fontFamily = runCatching { FontFamilyChoice.valueOf(obj.optString("fontFamily", "CALIBRI")) }.getOrDefault(FontFamilyChoice.CALIBRI),
                                indentLevel = obj.optInt("indentLevel", 0),
                                listType = runCatching { ListType.valueOf(obj.optString("listType", "NONE")) }.getOrDefault(ListType.NONE),
                                isChecked = obj.optBoolean("isChecked", false)
                            )
                        )
                    }
                    "table" -> {
                        val rowsList = mutableListOf<List<String>>()
                        val rowsArray = obj.optJSONArray("rows")
                        if (rowsArray != null) {
                            for (r in 0 until rowsArray.length()) {
                                val rowArr = rowsArray.getJSONArray(r)
                                val row = mutableListOf<String>()
                                for (c in 0 until rowArr.length()) {
                                    row.add(rowArr.optString(c, ""))
                                }
                                rowsList.add(row)
                            }
                        }
                        if (rowsList.isEmpty()) {
                            rowsList.add(listOf("Col 1", "Col 2"))
                            rowsList.add(listOf("Val 1", "Val 2"))
                        }
                        list.add(
                            DocumentBlock.Table(
                                id = obj.optString("id"),
                                rows = rowsList,
                                hasHeaderRow = obj.optBoolean("hasHeaderRow", true),
                                style = runCatching { TableStyle.valueOf(obj.optString("style", "BLUE_OFFICE")) }.getOrDefault(TableStyle.BLUE_OFFICE),
                                caption = obj.optString("caption", "")
                            )
                        )
                    }
                    "callout" -> {
                        list.add(
                            DocumentBlock.Callout(
                                id = obj.optString("id"),
                                text = obj.optString("text", ""),
                                type = runCatching { CalloutType.valueOf(obj.optString("calloutType", "INFO")) }.getOrDefault(CalloutType.INFO),
                                title = obj.optString("title", "Note")
                            )
                        )
                    }
                    "divider" -> {
                        list.add(
                            DocumentBlock.Divider(
                                id = obj.optString("id"),
                                style = runCatching { DividerStyle.valueOf(obj.optString("style", "SOLID")) }.getOrDefault(DividerStyle.SOLID)
                            )
                        )
                    }
                    "page_break" -> {
                        list.add(
                            DocumentBlock.PageBreak(id = obj.optString("id"))
                        )
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            list.clear()
            list.add(DocumentBlock.Paragraph(text = "Document recovered."))
        }
        if (list.isEmpty()) {
            list.add(DocumentBlock.Paragraph())
        }
        return list
    }
}
