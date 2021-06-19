package com.example.basil.ui.navigation


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import com.example.basil.R

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
            modifier = Modifier.fillMaxWidth().clickable {
                scope.launch {
                    scaffoldState.conceal()
                    navController.navigate(Screen.CreateUrl.route)
                }
            }
        ) {
            Icon(painter = painterResource(id = R.drawable.ic_fluent_document_add_48_regular), contentDescription = null)
            Text(text = "NYTT RECEPT", style = MaterialTheme.typography.h5)
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
                    Text(text = option) // TODO: Fix the styling of the text
                }
                Spacer(modifier = Modifier.padding(vertical = 20.dp))
            }
        }
    }


}