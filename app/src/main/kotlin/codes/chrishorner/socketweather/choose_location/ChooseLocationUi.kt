package codes.chrishorner.socketweather.choose_location

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.AmbientContentAlpha
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Providers
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import codes.chrishorner.socketweather.R.string
import codes.chrishorner.socketweather.styles.SocketWeatherTheme
import dev.chrisbanes.accompanist.insets.ProvideWindowInsets
import dev.chrisbanes.accompanist.insets.systemBarsPadding

@Preview(showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_NO, device = Devices.NEXUS_5)
@Composable
fun ChooseLocationPreview() {
  SocketWeatherTheme {
    ProvideWindowInsets {
      ChooseLocationScreen()
    }
  }
}

@Composable
fun ChooseLocationScreen() {
  Surface(color = MaterialTheme.colors.background) {
    Column(modifier = Modifier.systemBarsPadding()) {
      IconButton(onClick = { /*TODO*/ }) {
        Providers(AmbientContentAlpha provides ContentAlpha.medium) {
          Icon(Icons.Rounded.Close)
        }
      }
      Spacer(modifier = Modifier.weight(1f))
      Text(
          text = stringResource(string.chooseLocation_title),
          style = MaterialTheme.typography.h4,
          modifier = Modifier.padding(horizontal = 32.dp)
      )
      Spacer(modifier = Modifier.size(16.dp))
      OutlinedTextField(
          value = "",
          label = { Text(text = stringResource(string.chooseLocation_searchHint)) },
          onValueChange = { /*TODO*/ },
          leadingIcon = { Icon(Icons.Rounded.Search) },
          modifier = Modifier
              .padding(horizontal = 32.dp)
              .fillMaxWidth()
      )
      Spacer(modifier = Modifier.weight(2f))
    }
  }
}
