package com.example.ui.screens.admin

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ScrimsRepository
import com.example.model.CsvImportResult
import com.example.ui.theme.*

@Composable
fun BulkScrimCsvDialog(
    repository: ScrimsRepository,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var csvText by remember {
        mutableStateOf(
            "mode,map,entryFee,time\n" +
            "SQUAD,BERMUDA,100,05:00 PM\n" +
            "DUO,PURGATORY,60,06:30 PM\n" +
            "SOLO,KALAHARI,30,08:00 PM\n" +
            "SQUAD,ALPINE,120,09:30 PM"
        )
    }

    var importResult by remember { mutableStateOf<CsvImportResult?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CloudUpload, contentDescription = null, tint = CrimsonPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Bulk Scrim CSV Importer", fontWeight = FontWeight.Bold, color = TextPrimary)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Paste or edit CSV records below to schedule multiple tournament scrims instantly:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Format: mode, map, entryFee, time (e.g. SQUAD, BERMUDA, 100, 06:00 PM)",
                    style = MaterialTheme.typography.labelSmall,
                    color = GoldAccent
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = csvText,
                    onValueChange = { csvText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .testTag("csv_input_field"),
                    textStyle = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace,
                        color = TextPrimary
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CrimsonPrimary,
                        unfocusedBorderColor = BrandBorder,
                        focusedContainerColor = BrandCardElevated,
                        unfocusedContainerColor = BrandCardElevated
                    )
                )

                importResult?.let { result ->
                    Spacer(modifier = Modifier.height(10.dp))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (result.invalidRows.isEmpty()) SuccessGreen.copy(alpha = 0.15f) else WarningOrange.copy(alpha = 0.15f)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "Parsed ${result.totalRowsParsed} rows: ${result.validRows.size} imported successfully, ${result.invalidRows.size} errors.",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val result = repository.importBulkScrims(csvText)
                    importResult = result
                    Toast.makeText(context, "Successfully imported ${result.validRows.size} scrims!", Toast.LENGTH_SHORT).show()
                    if (result.invalidRows.isEmpty()) {
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary),
                modifier = Modifier.testTag("import_csv_confirm_btn")
            ) {
                Text("Validate & Import")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        },
        containerColor = BrandCardDark
    )
}
