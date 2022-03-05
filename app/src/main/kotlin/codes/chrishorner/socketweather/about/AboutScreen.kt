package codes.chrishorner.socketweather.about

import android.content.Intent
import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import codes.chrishorner.socketweather.BuildConfig
import codes.chrishorner.socketweather.R
import codes.chrishorner.socketweather.util.InsetAwareTopAppBar
import com.google.accompanist.insets.navigationBarsPadding

object AboutScreen : Screen {
  @Composable
  override fun Content() {
    val navigator = LocalNavigator.currentOrThrow
    AboutUi(onBack = { navigator.pop() })
  }
}

@Composable
private fun AboutUi(onBack: () -> Unit) {

  val scrollState = rememberScrollState()
  val toolbarElevation by animateDpAsState(targetValue = if (scrollState.value > 0) 4.dp else 0.dp)

  Surface(
    color = MaterialTheme.colors.background,
    modifier = Modifier.fillMaxSize()
  ) {
    Scaffold(
      topBar = {
        InsetAwareTopAppBar(
          navigationIcon = {
            IconButton(onClick = onBack) {
              Icon(
                Icons.Rounded.ArrowBack,
                contentDescription = stringResource(R.string.common_backButtonDesc)
              )
            }
          },
          title = { Text(stringResource(R.string.about_title), style = MaterialTheme.typography.h6) },
          backgroundColor = MaterialTheme.colors.background,
          elevation = toolbarElevation,
        )
      },
      content = {
        Column(
          modifier = Modifier
            .verticalScroll(scrollState)
            .padding(vertical = 16.dp)
            .navigationBarsPadding()
        ) {

          Text(
            text = stringResource(R.string.appName),
            style = MaterialTheme.typography.h4,
            modifier = Modifier.padding(horizontal = 16.dp),
          )

          Text(
            text = BuildConfig.VERSION_NAME,
            style = MaterialTheme.typography.subtitle2,
            modifier = Modifier.padding(start = 16.dp, top = 4.dp, end = 16.dp),
          )

          Text(
            text = stringResource(R.string.about_body),
            style = MaterialTheme.typography.body1,
            modifier = Modifier.padding(start = 16.dp, top = 32.dp, end = 16.dp, bottom = 16.dp)
          )

          AboutLink(
            iconRes = R.drawable.ic_sunny_24dp,
            textRes = R.string.about_bomWebsite,
            url = "http://www.bom.gov.au/",
          )

          AboutLink(
            iconRes = R.drawable.ic_github_24dp,
            textRes = R.string.about_source,
            url = "https://github.com/chris-horner/SocketWeather",
          )

          AboutLink(
            iconRes = R.drawable.ic_web_24dp,
            textRes = R.string.about_chrisWebsite,
            url = "https://chrishorner.codes",
          )
        }
      })
  }
}

@Composable
private fun AboutLink(@DrawableRes iconRes: Int, @StringRes textRes: Int, url: String) {

  val context = LocalContext.current

  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier
      .height(64.dp)
      .clickable {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
      }
      .padding(horizontal = 16.dp)
  ) {

    Icon(
      painter = painterResource(iconRes),
      contentDescription = null,
      modifier = Modifier.padding(end = 16.dp),
    )

    Text(
      text = stringResource(textRes),
      style = MaterialTheme.typography.button,
      modifier = Modifier.weight(1f),
    )
  }
}
