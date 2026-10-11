package com.example.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import java.util.Locale
import java.util.regex.Pattern

object SyntaxHighlighter {

  private val ColorKeyword = Color(0xFFFFFFFF)
  private val ColorString = Color(0xFFE2E2E2)
  private val ColorNumber = Color(0xFFAAAAAA)
  private val ColorComment = Color(0xFF666666)
  private val ColorProperty = Color(0xFFCCCCCC)
  private val ColorTag = Color(0xFFFFFFFF)
  private val ColorPunctuation = Color(0xFF888888)

  private val PatternJsKeywords = Pattern.compile(
    "\\b(break|case|catch|class|const|continue|debugger|default|delete|do|else|export|extends|finally|for|function|if|import|in|instanceof|new|return|super|switch|this|throw|try|typeof|var|void|while|with|yield|let|static|async|await|of|from|as|interface|type|enum|implements|package|private|protected|public)\\b"
  )

  private val PatternPyKeywords = Pattern.compile(
    "\\b(and|as|assert|async|await|break|class|continue|def|del|elif|else|except|finally|for|from|global|if|import|in|is|lambda|nonlocal|not|or|pass|raise|return|try|while|with|yield)\\b"
  )

  private val PatternSqlKeywords = Pattern.compile(
    "\\b(SELECT|FROM|WHERE|INSERT|INTO|UPDATE|DELETE|JOIN|LEFT|RIGHT|INNER|OUTER|GROUP|BY|ORDER|HAVING|LIMIT|OFFSET|CREATE|TABLE|DROP|ALTER|PRIMARY|KEY|NOT|NULL|DEFAULT|UNION|ALL|AS|AND|OR|IN|LIKE|BETWEEN|IS)\\b",
    Pattern.CASE_INSENSITIVE
  )

  private val PatternBooleans = Pattern.compile(
    "\\b(true|false|null|undefined|None|True|False|nil)\\b"
  )

  private val PatternStrings = Pattern.compile(
    "\"(\\\\.|[^\"\\\\])*\"|'(\\\\.|[^'\\\\])*'|`(\\\\.|[^`\\\\])*`"
  )

  private val PatternNumbers = Pattern.compile(
    "\\b\\d+(\\.\\d+)?([eE][+-]?\\d+)?(px|rem|em|%|vh|vw|s|ms|deg)?\\b"
  )

  private val PatternComments = Pattern.compile(
    "//.*|/\\*[\\s\\S]*?\\*/|#.*|<!--[\\s\\S]*?-->"
  )

  private val PatternHtmlTags = Pattern.compile(
    "</?[a-zA-Z0-9_-]+|/?>"
  )

  private val PatternHtmlAttributes = Pattern.compile(
    "\\b[a-zA-Z0-9_-]+(?=\\=)"
  )

  private val PatternCssProperties = Pattern.compile(
    "\\b[a-zA-Z0-9_-]+(?=\\s*:)"
  )

  fun highlight(text: String, extension: String): AnnotatedString {
    if (text.isEmpty()) return AnnotatedString("")

    val ext = extension.lowercase(Locale.ROOT)
    val builder = AnnotatedString.Builder(text)

    fun applyPattern(pattern: Pattern, spanStyle: SpanStyle) {
      val matcher = pattern.matcher(text)
      while (matcher.find()) {
        builder.addStyle(spanStyle, matcher.start(), matcher.end())
      }
    }

    when (ext) {
      "html", "htm", "xml", "svg" -> {
        applyPattern(PatternHtmlTags, SpanStyle(color = ColorTag, fontWeight = FontWeight.Bold))
        applyPattern(PatternHtmlAttributes, SpanStyle(color = ColorProperty, fontWeight = FontWeight.Medium))
        applyPattern(PatternStrings, SpanStyle(color = ColorString))
        applyPattern(PatternComments, SpanStyle(color = ColorComment, fontStyle = FontStyle.Italic))
      }

      "css", "scss", "sass", "less" -> {
        applyPattern(PatternCssProperties, SpanStyle(color = ColorProperty, fontWeight = FontWeight.Bold))
        applyPattern(PatternNumbers, SpanStyle(color = ColorNumber))
        applyPattern(PatternStrings, SpanStyle(color = ColorString))
        applyPattern(PatternComments, SpanStyle(color = ColorComment, fontStyle = FontStyle.Italic))
      }

      "json" -> {
        applyPattern(PatternStrings, SpanStyle(color = ColorString))
        applyPattern(PatternNumbers, SpanStyle(color = ColorNumber))
        applyPattern(PatternBooleans, SpanStyle(color = ColorKeyword, fontWeight = FontWeight.Bold))
      }

      "py" -> {
        applyPattern(PatternPyKeywords, SpanStyle(color = ColorKeyword, fontWeight = FontWeight.Bold))
        applyPattern(PatternBooleans, SpanStyle(color = ColorKeyword, fontWeight = FontWeight.Bold))
        applyPattern(PatternNumbers, SpanStyle(color = ColorNumber))
        applyPattern(PatternStrings, SpanStyle(color = ColorString))
        applyPattern(PatternComments, SpanStyle(color = ColorComment, fontStyle = FontStyle.Italic))
      }

      "sql" -> {
        applyPattern(PatternSqlKeywords, SpanStyle(color = ColorKeyword, fontWeight = FontWeight.Bold))
        applyPattern(PatternNumbers, SpanStyle(color = ColorNumber))
        applyPattern(PatternStrings, SpanStyle(color = ColorString))
        applyPattern(PatternComments, SpanStyle(color = ColorComment, fontStyle = FontStyle.Italic))
      }

      else -> {
        applyPattern(PatternJsKeywords, SpanStyle(color = ColorKeyword, fontWeight = FontWeight.Bold))
        applyPattern(PatternBooleans, SpanStyle(color = ColorKeyword, fontWeight = FontWeight.Bold))
        applyPattern(PatternNumbers, SpanStyle(color = ColorNumber))
        applyPattern(PatternStrings, SpanStyle(color = ColorString))
        applyPattern(PatternComments, SpanStyle(color = ColorComment, fontStyle = FontStyle.Italic))
      }
    }

    return builder.toAnnotatedString()
  }

  fun createVisualTransformation(extension: String): VisualTransformation {
    return VisualTransformation { text ->
      val highlighted = highlight(text.text, extension)
      TransformedText(highlighted, OffsetMapping.Identity)
    }
  }
}
