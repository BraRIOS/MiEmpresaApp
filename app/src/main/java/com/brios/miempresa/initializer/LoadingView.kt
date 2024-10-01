package com.brios.miempresa.initializer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.brios.miempresa.ui.dimens.AppDimensions

@Composable
fun LoadingView(message:String = "") {
    Column(modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(AppDimensions.mediumPadding, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()
        Text(text = message, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Preview
@Composable
fun LoadingScreenPreview() {
    Surface{ LoadingView() }
}

@Preview
@Composable
fun LoadingScreenPreviewWithMessage() {
    Surface{ LoadingView("Cargando") }
}