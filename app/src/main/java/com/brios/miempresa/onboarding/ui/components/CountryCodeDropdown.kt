package com.brios.miempresa.onboarding.ui.components

import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.brios.miempresa.core.ui.theme.AppDimensions
import com.brios.miempresa.core.ui.theme.MiEmpresaTheme
import com.brios.miempresa.core.ui.theme.SlateGray200

data class CountryCode(val emoji: String, val code: String, val name: String)

val defaultCountryCodes =
    listOf(
        CountryCode("🇦🇷", "+54", "Argentina"),
        CountryCode("🇲🇽", "+52", "México"),
        CountryCode("🇨🇴", "+57", "Colombia"),
        CountryCode("🇨🇱", "+56", "Chile"),
        CountryCode("🇧🇷", "+55", "Brasil"),
        CountryCode("🇵🇪", "+51", "Perú"),
        CountryCode("🇺🇾", "+598", "Uruguay"),
        CountryCode("🇪🇨", "+593", "Ecuador"),
        CountryCode("🇺🇸", "+1", "Estados Unidos"),
        CountryCode("🇪🇸", "+34", "España"),
    )

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CountryCodeDropdown(
    selectedCode: String,
    onCodeSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val selected =
        defaultCountryCodes.find { it.code == selectedCode }
            ?: defaultCountryCodes.first()

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier.width(115.dp),
    ) {
        OutlinedTextField(
            value = "${selected.emoji} ${selected.code}",
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium,
            shape = RoundedCornerShape(AppDimensions.inputCornerRadius),
            colors =
                OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = SlateGray200,
                ),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable),
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            defaultCountryCodes.forEach { country ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = "${country.emoji} ${country.code} ${country.name}",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    },
                    onClick = {
                        onCodeSelected(country.code)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun OnboardingScreenPreview() {
    MiEmpresaTheme {
        CountryCodeDropdown(
            selectedCode = "+54",
            onCodeSelected = {},
        )
    }
}
