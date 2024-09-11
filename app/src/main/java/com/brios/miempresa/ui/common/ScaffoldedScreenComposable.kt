package com.brios.miempresa.ui.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.brios.miempresa.R
import com.brios.miempresa.ui.navigation.BottomBar
import com.brios.miempresa.ui.navigation.MiEmpresaScreen
import com.brios.miempresa.ui.navigation.TopBar
import com.brios.miempresa.ui.navigation.TopBarViewModel
import com.brios.miempresa.ui.sign_in.SignInViewModel
import kotlinx.coroutines.launch

@Composable
fun ScaffoldedScreenComposable(
    navController: NavHostController,
    topBarViewModel: TopBarViewModel = hiltViewModel(),
    signInViewModel: SignInViewModel = hiltViewModel(),
    content: @Composable () -> Unit
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val user = signInViewModel.getSignedInUser()
    var showDropdown by remember { mutableStateOf(false) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                // Top logo section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Image(
                        painter = painterResource(id = R.mipmap.miempresa_ic_launcher_round),
                        contentDescription = "App Logo",
                        modifier = Modifier.size(64.dp),
                        contentScale = ContentScale.Crop
                    )
//                    Spacer(modifier = Modifier.height(16.dp))
                }

//                Spacer(modifier = Modifier.weight(1f))

                // Bottom user info section
                user?.let {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(model = user.profilePictureUrl),
                            contentDescription = "Profile Picture",
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = user.username ?: "Unknown User", fontWeight = FontWeight.Bold)
                            Text(text = user.userId, color = Color.Gray)
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
                                        signInViewModel.signOut()
                                        navController.navigate(MiEmpresaScreen.Welcome.name)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopBar(navController, topBarViewModel.topBarTitle)
            },
            content = { paddingValues ->
                Box(
                    modifier = Modifier
                        .padding(paddingValues)
                        .padding(horizontal = 4.dp)
                ) {
                    content()
                }
            },
            bottomBar = {
                BottomBar {
                    navController.navigate(it)
                }
            }
        )
    }
}
