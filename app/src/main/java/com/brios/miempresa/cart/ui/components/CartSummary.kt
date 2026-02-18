package com.brios.miempresa.cart.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.brios.miempresa.R
import com.brios.miempresa.core.ui.theme.AppDimensions
import com.brios.miempresa.core.ui.theme.SlateGray200
import com.brios.miempresa.core.ui.theme.WhatsAppGreen
import java.text.NumberFormat
import java.util.Locale

@Composable
fun CartSummary(
    modifier: Modifier = Modifier,
    totalPrice: Double,
    showConsultTotal: Boolean = false,
    onCheckout: () -> Unit,
    enabled: Boolean = true,
) {
    val currencyFormatter = remember { NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-AR")) }

    Surface(
        modifier = modifier.fillMaxWidth().navigationBarsPadding(),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shadowElevation = 8.dp,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(AppDimensions.mediumPadding),
            verticalArrangement = Arrangement.spacedBy(AppDimensions.smallPadding),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.order_total),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text =
                        if (showConsultTotal) {
                            stringResource(R.string.cart_total_consult)
                        } else {
                            currencyFormatter.format(totalPrice)
                        },
                    style = MaterialTheme.typography.headlineSmall,
                    color = if (showConsultTotal) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.ExtraBold,
                )
            }

            OutlinedButton(
                onClick = onCheckout,
                enabled = enabled,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(AppDimensions.mediumCornerRadius),
                border = BorderStroke(AppDimensions.smallBorderWidth, SlateGray200),
                colors =
                    ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White,
                        contentColor = WhatsAppGreen,
                    ),
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.whatsapp_glyph_green),
                    contentDescription = null,
                    modifier = Modifier.size(AppDimensions.defaultSmallIconSize),
                    tint = Color.Unspecified,
                )
                Spacer(modifier = Modifier.width(AppDimensions.smallPadding))
                Text(
                    text = stringResource(R.string.cart_checkout_whatsapp),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}
