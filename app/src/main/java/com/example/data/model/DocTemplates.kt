package com.example.data.model

import java.util.UUID

object DocTemplates {

    fun getBlankDocument(): WordDocument {
        return WordDocument(
            id = UUID.randomUUID().toString(),
            title = "Document 1",
            blocks = listOf(
                DocumentBlock.Paragraph(
                    text = "",
                    style = BlockStyle.NORMAL,
                    fontSizePt = 11,
                    fontFamily = FontFamilyChoice.CALIBRI
                )
            )
        )
    }

    fun getResumeTemplate(): WordDocument {
        return WordDocument(
            id = UUID.randomUUID().toString(),
            title = "Professional Resume - John Doe",
            headerText = "Curriculum Vitae",
            footerText = "Confidential",
            blocks = listOf(
                DocumentBlock.Paragraph(
                    text = "JOHN DOE",
                    style = BlockStyle.TITLE,
                    alignment = BlockAlignment.CENTER,
                    isBold = true,
                    fontSizePt = 24,
                    textColorHex = 0xFF185ABD
                ),
                DocumentBlock.Paragraph(
                    text = "Senior Software Engineer | tech.lead@example.com | +1 (555) 019-2834 | New York, USA",
                    style = BlockStyle.NORMAL,
                    alignment = BlockAlignment.CENTER,
                    fontSizePt = 10,
                    textColorHex = 0xFF605E5C
                ),
                DocumentBlock.Divider(style = DividerStyle.SOLID),
                DocumentBlock.Paragraph(
                    text = "PROFESSIONAL SUMMARY",
                    style = BlockStyle.HEADING_1,
                    textColorHex = 0xFF185ABD
                ),
                DocumentBlock.Paragraph(
                    text = "Accomplished software engineer with 7+ years of experience in enterprise mobile and cloud architecture. Proven track record in delivering high-impact, performant systems and mentoring engineering talent.",
                    style = BlockStyle.NORMAL,
                    lineSpacingMultiplier = 1.25f
                ),
                DocumentBlock.Paragraph(
                    text = "CORE COMPETENCIES",
                    style = BlockStyle.HEADING_2,
                    textColorHex = 0xFF185ABD
                ),
                DocumentBlock.Paragraph(
                    text = "Kotlin & Jetpack Compose Android Architecture",
                    style = BlockStyle.NORMAL,
                    listType = ListType.BULLET
                ),
                DocumentBlock.Paragraph(
                    text = "Distributed Cloud Backends & Microservices",
                    style = BlockStyle.NORMAL,
                    listType = ListType.BULLET
                ),
                DocumentBlock.Paragraph(
                    text = "Agile Leadership, CI/CD & DevOps Automation",
                    style = BlockStyle.NORMAL,
                    listType = ListType.BULLET
                ),
                DocumentBlock.Paragraph(
                    text = "PROFESSIONAL EXPERIENCE",
                    style = BlockStyle.HEADING_1,
                    textColorHex = 0xFF185ABD
                ),
                DocumentBlock.Table(
                    hasHeaderRow = true,
                    style = TableStyle.BLUE_OFFICE,
                    rows = listOf(
                        listOf("Role & Organization", "Period", "Key Achievement"),
                        listOf("Lead Mobile Engineer @ Apex Inc.", "2023 - Present", "Scaled app to 5M+ active users with 99.9% crash-free rate"),
                        listOf("Senior Software Engineer @ DevCorp", "2020 - 2023", "Pioneered Compose migration cutting bundle size by 35%"),
                        listOf("Software Developer @ StartUp Lab", "2018 - 2020", "Built offline-first sync engine supporting 50k transactions")
                    )
                ),
                DocumentBlock.Paragraph(
                    text = "EDUCATION & CERTIFICATIONS",
                    style = BlockStyle.HEADING_2,
                    textColorHex = 0xFF185ABD
                ),
                DocumentBlock.Paragraph(
                    text = "Bachelor of Science in Computer Science - Columbia University (2018)",
                    style = BlockStyle.NORMAL,
                    listType = ListType.BULLET
                ),
                DocumentBlock.Paragraph(
                    text = "Google Associate Android Developer Certified",
                    style = BlockStyle.NORMAL,
                    listType = ListType.BULLET
                )
            )
        )
    }

    fun getBusinessProposalTemplate(): WordDocument {
        return WordDocument(
            id = UUID.randomUUID().toString(),
            title = "Strategic Business Proposal",
            headerText = "Apex Consulting | Strategic Proposal",
            footerText = "Page ",
            blocks = listOf(
                DocumentBlock.Paragraph(
                    text = "DIGITAL TRANSFORMATION PROPOSAL",
                    style = BlockStyle.TITLE,
                    alignment = BlockAlignment.LEFT,
                    isBold = true,
                    fontSizePt = 22,
                    textColorHex = 0xFF185ABD
                ),
                DocumentBlock.Paragraph(
                    text = "Prepared for: Horizon Global Enterprises\nPrepared by: Strategy & Solutions Directorate\nDate: October 2026",
                    style = BlockStyle.SUBTITLE,
                    textColorHex = 0xFF605E5C,
                    fontSizePt = 12
                ),
                DocumentBlock.Divider(style = DividerStyle.SOLID),
                DocumentBlock.Callout(
                    text = "This proposal outlines a phased modernization roadmap designed to accelerate process velocity by 40% while reducing infrastructure overhead.",
                    type = CalloutType.TIP,
                    title = "Executive Highlight"
                ),
                DocumentBlock.Paragraph(
                    text = "1. Executive Summary",
                    style = BlockStyle.HEADING_1,
                    textColorHex = 0xFF185ABD
                ),
                DocumentBlock.Paragraph(
                    text = "In an increasingly competitive landscape, modernized internal tooling is essential for sustainable scale. Our end-to-end transformation framework aligns technology investments directly with business outcomes.",
                    style = BlockStyle.NORMAL
                ),
                DocumentBlock.Paragraph(
                    text = "2. Project Scope & Deliverables",
                    style = BlockStyle.HEADING_1,
                    textColorHex = 0xFF185ABD
                ),
                DocumentBlock.Paragraph(
                    text = "Comprehensive workflow audit and gap analysis across 4 departments",
                    style = BlockStyle.NORMAL,
                    listType = ListType.CHECKLIST,
                    isChecked = true
                ),
                DocumentBlock.Paragraph(
                    text = "Cloud-native platform architecture and security hardening",
                    style = BlockStyle.NORMAL,
                    listType = ListType.CHECKLIST,
                    isChecked = true
                ),
                DocumentBlock.Paragraph(
                    text = "Custom mobile & desktop executive analytics dashboard",
                    style = BlockStyle.NORMAL,
                    listType = ListType.CHECKLIST,
                    isChecked = false
                ),
                DocumentBlock.Paragraph(
                    text = "Staff training, change management, and 24/7 hypercare support",
                    style = BlockStyle.NORMAL,
                    listType = ListType.CHECKLIST,
                    isChecked = false
                ),
                DocumentBlock.Paragraph(
                    text = "3. Investment & Timeline",
                    style = BlockStyle.HEADING_1,
                    textColorHex = 0xFF185ABD
                ),
                DocumentBlock.Table(
                    hasHeaderRow = true,
                    style = TableStyle.BLUE_OFFICE,
                    rows = listOf(
                        listOf("Phase", "Duration", "Budget (USD)"),
                        listOf("Phase 1: Discovery & Architecture", "4 Weeks", "$25,000"),
                        listOf("Phase 2: Core Build & Integration", "8 Weeks", "$55,000"),
                        listOf("Phase 3: Rollout & Training", "4 Weeks", "$20,000"),
                        listOf("Total Estimated Engagement", "16 Weeks", "$100,000")
                    )
                ),
                DocumentBlock.Paragraph(
                    text = "Authorization & Sign-off",
                    style = BlockStyle.HEADING_2,
                    textColorHex = 0xFF185ABD
                ),
                DocumentBlock.Paragraph(
                    text = "Authorized Signature: _______________________      Date: _____________",
                    style = BlockStyle.NORMAL
                )
            )
        )
    }

    fun getMeetingMinutesTemplate(): WordDocument {
        return WordDocument(
            id = UUID.randomUUID().toString(),
            title = "Executive Board Meeting Minutes",
            headerText = "Minutes of Meeting",
            blocks = listOf(
                DocumentBlock.Paragraph(
                    text = "WEEKLY LEADERSHIP SYNC",
                    style = BlockStyle.TITLE,
                    alignment = BlockAlignment.CENTER,
                    isBold = true,
                    textColorHex = 0xFF185ABD
                ),
                DocumentBlock.Paragraph(
                    text = "Meeting Date: Friday, October 9, 2026 | Location: Boardroom A & Microsoft Teams",
                    style = BlockStyle.SUBTITLE,
                    alignment = BlockAlignment.CENTER
                ),
                DocumentBlock.Divider(style = DividerStyle.SOLID),
                DocumentBlock.Paragraph(
                    text = "Attendees",
                    style = BlockStyle.HEADING_2,
                    textColorHex = 0xFF185ABD
                ),
                DocumentBlock.Table(
                    hasHeaderRow = true,
                    style = TableStyle.STRIPED,
                    rows = listOf(
                        listOf("Name", "Designation", "Status"),
                        listOf("Sarah Khan", "Chief Executive Officer", "Present"),
                        listOf("David Miller", "Chief Technology Officer", "Present"),
                        listOf("Amina Malik", "VP of Operations", "Present"),
                        listOf("Marcus Vance", "Lead Legal Counsel", "Remote")
                    )
                ),
                DocumentBlock.Paragraph(
                    text = "Agenda & Discussion Points",
                    style = BlockStyle.HEADING_2,
                    textColorHex = 0xFF185ABD
                ),
                DocumentBlock.Paragraph(
                    text = "Q3 Financial Performance review revealed 18% YoY growth in recurring subscriptions.",
                    style = BlockStyle.NORMAL,
                    listType = ListType.NUMBERED
                ),
                DocumentBlock.Paragraph(
                    text = "The migration to the new datacenter facility is scheduled for next weekend with minimal downtime.",
                    style = BlockStyle.NORMAL,
                    listType = ListType.NUMBERED
                ),
                DocumentBlock.Paragraph(
                    text = "Employee satisfaction survey reported an 88% overall positive sentiment index.",
                    style = BlockStyle.NORMAL,
                    listType = ListType.NUMBERED
                ),
                DocumentBlock.Paragraph(
                    text = "Action Items",
                    style = BlockStyle.HEADING_2,
                    textColorHex = 0xFF185ABD
                ),
                DocumentBlock.Paragraph(
                    text = "Finalize budget allocation for Q4 product expansion (Owner: Amina, Due: Oct 15)",
                    style = BlockStyle.NORMAL,
                    listType = ListType.CHECKLIST,
                    isChecked = false
                ),
                DocumentBlock.Paragraph(
                    text = "Publish security audit compliance report (Owner: David, Due: Oct 18)",
                    style = BlockStyle.NORMAL,
                    listType = ListType.CHECKLIST,
                    isChecked = false
                ),
                DocumentBlock.Callout(
                    text = "Next review meeting is scheduled for Friday, October 16, 2026 at 10:00 AM.",
                    type = CalloutType.INFO,
                    title = "Notice"
                )
            )
        )
    }

    fun getFormalLetterTemplate(): WordDocument {
        return WordDocument(
            id = UUID.randomUUID().toString(),
            title = "Official Formal Letter",
            blocks = listOf(
                DocumentBlock.Paragraph(
                    text = "Acme Technologies Global Ltd.\n450 Innovation Boulevard, Suite 800\nSeattle, Washington 98101",
                    style = BlockStyle.NORMAL,
                    alignment = BlockAlignment.LEFT,
                    lineSpacingMultiplier = 1.2f
                ),
                DocumentBlock.Paragraph(
                    text = "October 9, 2026",
                    style = BlockStyle.NORMAL
                ),
                DocumentBlock.Paragraph(
                    text = "To:\nHuman Resources Department\nGlobal Innovations Corp.\n100 Enterprise Way, San Francisco, CA",
                    style = BlockStyle.NORMAL,
                    lineSpacingMultiplier = 1.2f
                ),
                DocumentBlock.Paragraph(
                    text = "Subject: Formal Expression of Interest - Senior Engineering Position",
                    style = BlockStyle.HEADING_2,
                    textColorHex = 0xFF185ABD,
                    isBold = true
                ),
                DocumentBlock.Paragraph(
                    text = "Dear Hiring Committee,",
                    style = BlockStyle.NORMAL
                ),
                DocumentBlock.Paragraph(
                    text = "I am writing to formally convey my interest in the Senior Engineering role at Global Innovations Corp. Having closely followed your technological advances in cloud and native systems, I am excited by the opportunity to contribute my technical leadership and expertise.",
                    style = BlockStyle.NORMAL,
                    lineSpacingMultiplier = 1.3f
                ),
                DocumentBlock.Paragraph(
                    text = "Throughout my tenure at leading technology enterprises, I have spearheaded cross-functional initiatives that boosted platform reliability, streamlined developer workflows, and delivered intuitive user experiences to millions of worldwide users.",
                    style = BlockStyle.NORMAL,
                    lineSpacingMultiplier = 1.3f
                ),
                DocumentBlock.Paragraph(
                    text = "I welcome the opportunity to discuss how my qualifications align with your organizational roadmap. Thank you for your consideration.",
                    style = BlockStyle.NORMAL,
                    lineSpacingMultiplier = 1.3f
                ),
                DocumentBlock.Paragraph(
                    text = "Sincerely,\n\n___________________________\nAlex Harrison\nPrincipal Systems Architect",
                    style = BlockStyle.NORMAL,
                    lineSpacingMultiplier = 1.2f
                )
            )
        )
    }

    val allTemplates = listOf(
        TemplateItem("Blank Document", "خالی دستاویز", "Start with an empty document", "blank"),
        TemplateItem("Professional Resume", "سی وی / ریزیومے", "Modern resume with skills & experience", "resume"),
        TemplateItem("Business Proposal", "کاروباری پروپوزل", "Strategic project & budget proposal", "proposal"),
        TemplateItem("Meeting Minutes", "میٹنگ کے منٹس", "Meeting agenda, notes & action items", "meeting"),
        TemplateItem("Formal Letter", "باضابطہ خط", "Standard business & correspondence letter", "letter")
    )
}

data class TemplateItem(
    val title: String,
    val urduTitle: String,
    val description: String,
    val templateKey: String
)
