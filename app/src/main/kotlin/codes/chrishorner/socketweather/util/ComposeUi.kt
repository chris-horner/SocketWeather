package codes.chrishorner.socketweather.util

import android.content.Intent
import android.content.Intent.ACTION_VIEW
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.core.net.toUri

data class TextLink(
  val text: String,
  val url: String,
)

@Composable
fun TextWithLinks(
  text: String,
  links: List<TextLink>,
  modifier: Modifier = Modifier,
  style: TextStyle = LocalTextStyle.current,
) {
  val sortedLinks = links.sortedBy { link -> text.indexOf(link.text) }

  val annotatedString = buildAnnotatedString {
    var currentIndex = 0

    for (link in sortedLinks) {
      val linkIndex = text.indexOf(link.text)
      require(linkIndex != -1) {
        "Link text '${link.text}' not found in text."
      }

      if (currentIndex != linkIndex) {
        append(text.substring(currentIndex, linkIndex))
      }

      pushStringAnnotation("url", annotation = link.url)
      withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary, textDecoration = TextDecoration.Underline)) {
        append(link.text)
      }
      pop()

      currentIndex = linkIndex + link.text.length
    }

    if (currentIndex < text.length) {
      append(text.substring(currentIndex, text.length))
    }
  }

  val context = LocalContext.current

  ClickableText(
    text = annotatedString,
    style = style,
    modifier = modifier,
    onClick = { offset ->
      annotatedString.getStringAnnotations(offset, offset).firstOrNull()?.let { annotation ->
        context.startActivity(Intent(ACTION_VIEW, annotation.item.toUri()))
      }
    },
  )
}
