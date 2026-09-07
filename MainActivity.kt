package com.docueasy.app

import android.app.Activity
import android.os.Bundle
import android.os.Environment
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.widget.*
import java.io.File
import java.io.FileOutputStream

class MainActivity : Activity() {
    private val fields = mutableListOf<Pair<String, EditText>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val scroll = ScrollView(this)
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 28, 32, 28)
        }
        scroll.addView(box)

        box.addView(TextView(this).apply {
            text = "DocuEasy\nResume Maker"
            textSize = 28f
            typeface = Typeface.DEFAULT_BOLD
            setPadding(0, 0, 0, 24)
        })

        addField(box, "Full Name")
        addField(box, "Mobile Number")
        addField(box, "Email")
        addField(box, "Address")
        addField(box, "Career Objective", 3)
        addField(box, "Education", 3)
        addField(box, "Work Experience", 3)
        addField(box, "Skills", 3)
        addField(box, "Languages", 2)

        box.addView(Button(this).apply {
            text = "Generate Resume PDF"
            setOnClickListener { generatePdf() }
        })
        setContentView(scroll)
    }

    private fun addField(box: LinearLayout, label: String, lines: Int = 1) {
        val e = EditText(this).apply {
            hint = label
            minLines = lines
            gravity = android.view.Gravity.TOP
            textSize = 16f
        }
        box.addView(e, LinearLayout.LayoutParams(-1, -2).apply { setMargins(0,0,0,12) })
        fields.add(label to e)
    }

    private fun value(label: String) = fields.firstOrNull { it.first == label }?.second?.text?.toString().orEmpty()

    private fun generatePdf() {
        val pdf = PdfDocument()
        val page = pdf.startPage(PdfDocument.PageInfo.Builder(595, 842, 1).create())
        val canvas = page.canvas
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        var y = 48f

        fun line(s: String, size: Float = 11f, bold: Boolean = false) {
            if (s.isBlank()) return
            paint.textSize = size
            paint.typeface = if (bold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
            canvas.drawText(s.take(85), 42f, y, paint)
            y += size + 9
        }
        fun section(title: String, body: String) {
            if (body.isBlank()) return
            y += 7
            line(title, 15f, true)
            body.lines().filter { it.isNotBlank() }.forEach { line("• $it") }
        }

        line(value("Full Name").ifBlank { "Your Name" }, 24f, true)
        line(value("Mobile Number"))
        line(value("Email"))
        line(value("Address"))
        section("CAREER OBJECTIVE", value("Career Objective"))
        section("EDUCATION", value("Education"))
        section("WORK EXPERIENCE", value("Work Experience"))
        section("SKILLS", value("Skills"))
        section("LANGUAGES", value("Languages"))

        pdf.finishPage(page)
        val dir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) ?: filesDir
        val file = File(dir, "DocuEasy_Resume.pdf")
        FileOutputStream(file).use { pdf.writeTo(it) }
        pdf.close()
        Toast.makeText(this, "PDF saved in app Documents", Toast.LENGTH_LONG).show()
    }
}
