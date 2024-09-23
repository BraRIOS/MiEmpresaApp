package com.brios.miempresa.navigation

import android.app.Activity
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.brios.miempresa.R
import com.brios.miempresa.welcome.SignInViewModel
import kotlinx.coroutines.launch

@Composable
fun DrawerComposable(
    navController: NavHostController,
    signInViewModel: SignInViewModel = hiltViewModel(),
    content: @Composable () -> Unit
){
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val user = signInViewModel.getSignedInUser()
    var showDropdown by remember { mutableStateOf(false) }
    val context = LocalContext.current as Activity

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                // Top logo section
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.miempresa_logo_glyph),
                        contentDescription = "App Logo",
                        modifier = Modifier.size(64.dp),
                        contentScale = ContentScale.Crop
                    )
                    user?.let {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {

                            Image(
                                painter = rememberAsyncImagePainter(model = user.profilePictureUrl),
                                contentDescription = "Profile Picture",
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                            )

                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(text = user.username ?: "Unknown User", fontWeight = FontWeight.Bold)
                                user.email?.let { it1 -> Text(text = it1, color = Color.Gray) }
                            }
                            Box {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "Options",
                                    modifier = Modifier
                                        .clickable { showDropdown = !showDropdown }
                                )
                                DropdownMenu(
                                    expanded = showDropdown,
                                    onDismissRequest = { showDropdown = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Ver perfil") },
                                        onClick = {
                                            showDropdown = false
                                            scope.launch {
                                                drawerState.close()
                                            }
//                                        navController.navigate("profile")
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Cerrar sesión") },
                                        onClick = {
                                            showDropdown = false
                                            scope.launch {
                                                drawerState.close()
                                            }
                                            signInViewModel.signOut(context)
                                            navController.navigate(MiEmpresaScreen.Welcome.name) {
                                                popUpTo(0)
                                                launchSingleTop = true
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    ){ content() }
}