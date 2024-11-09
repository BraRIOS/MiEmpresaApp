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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.brios.miempresa.R
import com.brios.miempresa.data.Company
import com.brios.miempresa.domain.UserData
import com.brios.miempresa.signin.SignInViewModel
import com.brios.miempresa.ui.dimens.AppDimensions
import kotlinx.coroutines.launch

@Composable
fun DrawerComposable(
    navController: NavHostController,
    drawerState: DrawerState,
    signInViewModel: SignInViewModel = hiltViewModel(),
    content: @Composable () -> Unit
){
    val user = signInViewModel.getSignedInUser()
    val selectedCompany by signInViewModel.getSelectedCompany().observeAsState()
    val context = LocalContext.current as Activity
    DrawerContent(drawerState, user, selectedCompany, context, signInViewModel, navController, content)
}

@Composable
private fun DrawerContent(
    drawerState: DrawerState,
    user: UserData?,
    selectedCompany:  Company?,
    context: Activity?,
    signInViewModel: SignInViewModel?,
    navController: NavHostController,
    content: @Composable () -> Unit
) {
    val scope = rememberCoroutineScope()
    var showDropdown by remember { mutableStateOf(false) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(AppDimensions.mediumPadding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.miempresa_logo_glyph),
                        contentDescription = stringResource(R.string.app_logo),
                        modifier = Modifier.size(AppDimensions.Drawer.appLogoSize),
                        contentScale = ContentScale.Crop
                    )
                    Column(
                        modifier = Modifier.wrapContentHeight(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        HorizontalDivider()
                        selectedCompany?.let {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(AppDimensions.mediumPadding),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(
                                    modifier = Modifier.wrapContentHeight(),
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = it.name, style = MaterialTheme.typography.bodyLarge,
                                    )
                                    Text(
                                        text = stringResource(R.string.current_company),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Icon(
                                    modifier = Modifier
                                        .size(AppDimensions.mediumIconSize)
                                        .clickable {
                                            scope.launch {
                                                drawerState.close()
                                            }
                                            navController.navigate(MiEmpresaScreen.Initializer.name+"/ShowCompanyList")
                                        },
                                    imageVector = Icons.AutoMirrored.Filled.CompareArrows,
                                    contentDescription = stringResource(R.string.switch_company),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        HorizontalDivider()
                        user?.let {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(AppDimensions.mediumPadding),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(AppDimensions.mediumPadding)
                            ) {

                                Image(
                                    painter = rememberAsyncImagePainter(model = user.profilePictureUrl),
                                    contentDescription = stringResource(R.string.profile_picture),
                                    modifier = Modifier
                                        .size(AppDimensions.largeIconSize)
                                        .clip(CircleShape)
                                )

                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = user.username ?: stringResource(id = R.string.user),
                                        fontWeight = FontWeight.Bold
                                    )
                                    user.email?.let { it1 ->
                                        Text(
                                            text = it1,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                                Box {
                                    Icon(
                                        imageVector = Icons.Default.MoreVert,
                                        contentDescription = stringResource(R.string.options),
                                        modifier = Modifier
                                            .clickable { showDropdown = !showDropdown }
                                    )
                                    DropdownMenu(
                                        expanded = showDropdown,
                                        onDismissRequest = { showDropdown = false }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text(stringResource(R.string.view_profile)) },
                                            onClick = {
                                                showDropdown = false
                                                scope.launch {
                                                    drawerState.close()
                                                }
//                                        navController.navigate("profile")
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text(stringResource(R.string.log_out)) },
                                            onClick = {
                                                showDropdown = false
                                                scope.launch {
                                                    drawerState.close()
                                                    signInViewModel!!.signOut(context!!).also {
                                                        navController.navigate(MiEmpresaScreen.SignIn.name) {
                                                            popUpTo(0)
                                                        }
                                                    }
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
        }
    ) { content() }
}

@Preview
@Composable
fun DrawerContentPreview(){
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    DrawerContent(
        drawerState = drawerState,
        UserData("Test", "Test username", "test@test.com", "url"),
        Company("Test", "Test Company", true),
        null,
        null,
        NavHostController(LocalContext.current)
    ){
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ){
                Button(
                    onClick = { scope.launch { drawerState.open() } }) {
                    Text(text = "Open Drawer")
                }
            }
        }

    }

}