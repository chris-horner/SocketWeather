package codes.chrishorner.socketweather.about

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import codes.chrishorner.socketweather.BuildConfig
import codes.chrishorner.socketweather.Navigator
import codes.chrishorner.socketweather.Presenter
import codes.chrishorner.socketweather.R
import codes.chrishorner.socketweather.Screen
import codes.chrishorner.socketweather.about.AboutPresenter.BackPressEvent
import kotlinx.parcelize.Parcelize

@Parcelize
object AboutScreen : Screen<BackPressEvent, Unit> {
  override fun onCreatePresenter(context: Context, navigator: Navigator): Presenter<BackPressEvent, Unit> {
    return AboutPresenter(navigator)
  }

  @Composable
  override fun Content(state: Unit, onEvent: (BackPressEvent) -> Unit) {
    AboutUi(onBack = { onEvent(BackPressEvent) })
  }
}

@Composable
private fun AboutUi(onBack: () -> Unit) {

  val scrollState = rememberScrollState()
  val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

  Scaffold(
    modifier = Modifier
      .fillMaxSize()
      .nestedScroll(scrollBehavior.nestedScrollConnection),
    topBar = {
      TopAppBar(
        navigationIcon = {
          IconButton(onClick = onBack) {
            Icon(
              Icons.Rounded.ArrowBack,
              contentDescription = stringResource(R.string.common_backButtonDesc)
            )
          }
        },
        title = { Text(stringResource(R.string.about_title), style = MaterialTheme.typography.titleLarge) },
        scrollBehavior = scrollBehavior,
      )
    }
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .verticalScroll(scrollState)
        .padding(innerPadding)
        .padding(vertical = 16.dp)
        .navigationBarsPadding()
    ) {

      Text(
        text = stringResource(R.string.appName),
        style = MaterialTheme.typography.headlineMedium,
        modifier = Modifier.padding(horizontal = 16.dp),
      )

      Text(
        text = BuildConfig.VERSION_NAME,
        style = MaterialTheme.typography.titleSmall,
        modifier = Modifier.padding(start = 16.dp, top = 4.dp, end = 16.dp),
      )

      Text(
        text = stringResource(R.string.about_body),
        style = MaterialTheme.typography.bodyLarge,
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
      style = MaterialTheme.typography.labelLarge,
      modifier = Modifier.weight(1f),
    )
  }
}
