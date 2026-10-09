package ru.yourname.events.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ru.yourname.events.data.model.Event

@Composable
fun ZodiacBlock(event: Event) {
    val western = event.westernZodiac()
    val eastern = event.easternZodiac()
    val element = event.chineseElement()

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        HorizontalDivider()
        Text("Астрология", style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ZodiacCell(western.symbol, western.label, western.element)
            if (eastern != null && element != null) {
                ZodiacCell(eastern.symbol, eastern.label, "Год ${event.year}")
                ZodiacCell(element.symbol, element.label, "Стихия")
            }
        }
        HorizontalDivider()
    }
}

@Composable
private fun ZodiacCell(symbol: String, title: String, subtitle: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(4.dp)
    ) {
        Text(symbol, style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(4.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
        Text(subtitle, style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
