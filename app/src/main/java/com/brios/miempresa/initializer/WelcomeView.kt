package com.brios.miempresa.initializer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.brios.miempresa.R
import com.brios.miempresa.ui.dimens.AppDimensions

@Composable
fun WelcomeView(username: String, isFirstTime: Boolean, onCompanyNameEntered: (String) -> Unit) {
    var companyName by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(AppDimensions.mediumPadding),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isFirstTime) {
            Text(
                text = stringResource(R.string.greeting_user, username),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(AppDimensions.mediumPadding))
            Text(
                text = stringResource(R.string.create_your_company),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }else
            Text(
                "${stringResource(id = R.string.create_your_company)}, $username",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
        Spacer(modifier = Modifier.height(AppDimensions.mediumPadding))
        OutlinedTextField(
            value = companyName,
            onValueChange = { companyName = it },
            label = { Text(stringResource(id = R.string.company_name_label)) }
        )
        Spacer(modifier = Modifier.height(AppDimensions.mediumPadding))
        Button(onClick = { onCompanyNameEntered(companyName) }) {
            Text(stringResource(id = R.string.create))
        }
    }
}

@Preview
@Composable
fun WelcomeScreenPreview() {
    Surface {
        WelcomeView("John Doe", true) {}
    }

}

@Preview
@Composable
fun WelcomeScreenPreview2() {
    Surface {
        WelcomeView("John Doe", false) {}
    }
}