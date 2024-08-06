package com.example.geminiapitest

import android.graphics.BitmapFactory
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel


@Composable
fun PiratesScreen(
   piratesViewModel: PiratesViewModel = viewModel()
) {
    val placeholderResult = stringResource(R.string.results_placeholder)
    var error by rememberSaveable { mutableStateOf(placeholderResult) }
    var adventure by rememberSaveable { mutableStateOf(placeholderResult) }
    var actions by rememberSaveable { mutableStateOf(emptyList<String>()) }
    val uiState by piratesViewModel.uiState.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = "#Pirates Test",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(16.dp)
        )
        when(uiState){
            is UiState.Initial -> piratesViewModel.start()
            is UiState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }
            is UiState.Error -> {
                error = (uiState as UiState.Error).errorMessage
                Text(
                    text = (uiState as UiState.Error).errorMessage,
                    textAlign = TextAlign.Start,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(16.dp)
                )
            }
            is UiState.Success -> {
                adventure = (uiState as UiState.Success).outputText
                actions = (uiState as UiState.Success).actions
                PiratesAdventure(
                    modifier = Modifier.padding(16.dp),
                    adventure = adventure,
                    actions = actions
                ) { actionIndex ->
                    piratesViewModel.sendAction(actionIndex)
                }
            }
        }
    }
}

@Composable
fun PiratesAdventure(
    modifier: Modifier,
    adventure:String,
    actions:List<String>,
    onAction:(Int)->Unit
){
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        val scrollState = rememberScrollState()
        Text(
            text = adventure,
            textAlign = TextAlign.Start,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .padding(16.dp)
                .weight(1f)
                .verticalScroll(scrollState)
        )
        actions.forEachIndexed { index, action ->
            Button(
                onClick = { onAction(index) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = action)
            }
        }
    }
}

@Preview
@Composable
fun PiratesPreview() {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = "#Pirates Test",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(16.dp)
        )
        PiratesAdventure(
            Modifier.padding(16.dp),
            adventure = "The salty spray whipped across the deck of the \"Black Kraken\", a weathered galleon carving" +
                    " through the storm-tossed seas. Captain Blackheart, a grizzled veteran with a missing eye and a " +
                    "voice like thunder, bellowed orders, his scarred face etched with a grim determination.  " +
                    "His first mate, Shadow, perched in the crow's nest, scanning the horizon for any sign of trouble." +
                    " Below, the rest of the crew – the burly Ironfist, the agile thieves Whisper and Swift – worked with practiced efficiency, " +
                    "their eyes gleaming with the thrill of danger and the promise of treasure. This was no ordinary voyage;" +
                    " this was an adventure that promised to test their mettle and change their lives forever. \n" +
                    "\n" +
                    "His first mate, Shadow, perched in the crow's nest, scanning the horizon for any sign of trouble." +
                    " Below, the rest of the crew – the burly Ironfist, the agile thieves Whisper and Swift – worked with practiced efficiency, " +
                    "their eyes gleaming with the thrill of danger and the promise of treasure. This was no ordinary voyage;" +
                    " this was an adventure that promised to test their mettle and change their lives forever. \n" +
                    "\n" +
                    "His first mate, Shadow, perched in the crow's nest, scanning the horizon for any sign of trouble." +
                    " Below, the rest of the crew – the burly Ironfist, the agile thieves Whisper and Swift – worked with practiced efficiency, " +
                    "their eyes gleaming with the thrill of danger and the promise of treasure. This was no ordinary voyage;" +
                    " this was an adventure that promised to test their mettle and change their lives forever. \n" +
                    "\n" +
                    "Shadow, his keen eyes scanning the churning waves, spotted a break in the storm clouds revealing a distant," +
                    " jagged island. \"Captain!\" he shouted, \"Land ahoy! Bearing 220!\" Blackheart, his missing eye seeming " +
                    "to pierce the darkness, abarked orders. Ironfist steered the ship, his hands a blur on the helm, while " +
                    "Whisper and Swift, their agility honed by years of seafaring, adjusted sails, catching the wind and guiding " +
                    "the \"Black Kraken\" towards the island, a potential haven in the tempest."+
                    "\n" +
                    "Shadow, his keen eyes scanning the churning waves, spotted a break in the storm clouds revealing a distant," +
                    " jagged island. \"Captain!\" he shouted, \"Land ahoy! Bearing 220!\" Blackheart, his missing eye seeming" +
                    " to pierce the darkness, abarked orders. Ironfist steered the ship, his hands a blur on the helm, while" +
                    " Whisper and Swift, their agility honed by years of seafaring, adjusted sails, catching the wind and guiding " +
                    "the \"Black Kraken\" towards the island, a potential haven in the tempest."
            ,
            actions = listOf("Examine the island through a spyglass", "Ask Shadow for more details about the island", "Prepare for a possible encounter with danger")
            , onAction = {}
        )
    }
}
