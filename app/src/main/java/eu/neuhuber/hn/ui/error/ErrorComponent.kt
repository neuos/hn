package eu.neuhuber.hn.ui.error

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import eu.neuhuber.hn.ui.theme.HnPreviews
import eu.neuhuber.hn.ui.theme.HnTheme

@Composable
fun ErrorComponent(message: String, modifier: Modifier = Modifier, retry: (() -> Unit)? = null) {
    Card(
        modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                message,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            if (retry != null) {
                Spacer(modifier = Modifier.size(16.dp))
                Button(
                    onClick = retry,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onErrorContainer)
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = "Retry")
                    Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                    Text("Retry")
                }
            }

        }
    }
}

@HnPreviews
@Composable
fun ErrorComponentPreview() {
    HnTheme {
        ErrorComponent("Something bad happened.") {}
    }
}