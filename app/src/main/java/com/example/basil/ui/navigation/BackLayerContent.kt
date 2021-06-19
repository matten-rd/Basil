package com.example.basil.ui.navigation


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import com.example.basil.R
import com.example.basil.ui.components.BasilSpacer
import org.apache.xpath.operations.Mod

@ExperimentalMaterialApi
@Composable
fun BasilBackLayer(
    scaffoldState: BackdropScaffoldState,
    scope: CoroutineScope,
    navController: NavController
) {
    val options = listOf<String>(
        "FÖRRÄTTER", "HUVUDRÄTTER", "EFTERRÄTTER", "BAKNING"
    )
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxHeight(0.9f)
            .fillMaxWidth(),
        verticalArrangement = Arrangement.Center
    ) {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    scope.launch {
                        scaffoldState.conceal()
                        navController.navigate(Screen.CreateUrl.route)
                    }
                }, horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(painter = painterResource(id = R.drawable.ic_fluent_clipboard_link_24_regular), contentDescription = null, modifier = Modifier.size(40.dp))
                Text(text = "Nytt länkrecept", style = MaterialTheme.typography.h5)
            }
            Spacer(modifier = Modifier.height(40.dp))
            Column(modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    scope.launch {
                        scaffoldState.conceal()
                        navController.navigate(Screen.CreateImage.route)
                    }
                }, horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(painter = painterResource(id = R.drawable.ic_fluent_clipboard_image_24_regular), contentDescription = null, modifier = Modifier.size(40.dp))
                Text(text = "Nytt bildrecept", style = MaterialTheme.typography.h5)
            }

        }
        Divider(
            Modifier
                .fillMaxWidth(0.25f)
                .padding(top = 40.dp, bottom = 20.dp), color = MaterialTheme.colors.primary)
        
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            options.forEachIndexed { index, option ->
                TextButton(onClick = { /*TODO: Handle filtering*/ }) {
                    Text(text = option, style = MaterialTheme.typography.h5, fontSize = 20.sp)
                }
                Spacer(modifier = Modifier.padding(vertical = 12.dp))
            }
        }
    }


}