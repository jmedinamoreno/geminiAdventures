package com.example.geminiapitest

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class PiratesViewModel : ViewModel() {
    private val _uiState: MutableStateFlow<UiState> =
        MutableStateFlow(UiState.Initial)
    val uiState: StateFlow<UiState> =
        _uiState.asStateFlow()

    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = BuildConfig.apiKey,
    )

    private var adventure:String=""
    private var actions:List<String> = emptyList()
    private val crewSize = 5

    val isDebug = false

    fun start(){
        _uiState.value = UiState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if(isDebug){
                    adventure = "The salty spray whipped across the deck of the \"Black Kraken\", a weathered galleon carving through the storm-tossed seas. Captain Blackheart, a grizzled veteran with a missing eye and a voice like thunder, bellowed orders, his scarred face etched with a grim determination.  His first mate, Shadow, perched in the crow's nest, scanning the horizon for any sign of trouble. Below, the rest of the crew – the burly Ironfist, the agile thieves Whisper and Swift – worked with practiced efficiency, their eyes gleaming with the thrill of danger and the promise of treasure. This was no ordinary voyage; this was an adventure that promised to test their mettle and change their lives forever. \n"
                    actions = listOf("Help Captain Blackheart navigate the storm", "Assist Shadow in the crow's nest", "Join Ironfist, Whisper and Swift in their tasks")
                }else {
                    val shipText =
                        generativeModel.generateContent(content { text("Sort description of a pirate ship (max 50 words, Language: Spanish)") }).text
                            ?: ""
                    val crewText =
                        generativeModel.generateContent(content { text("For this ship [$shipText]. Build a description of the crew ($crewSize people) (max 80 words, Language: Spanish)") }).text
                            ?: ""
                    adventure =
                        generativeModel.generateContent(content { text("Starting point for the adventure of the user with this ship [$shipText] and this crew [$crewText] (max 100 words, Language: Spanish):") }).text
                            ?: ""
                    val options =
                        generativeModel.generateContent(content { text("With this [$adventure] Write 3 possible actions for the user (max 30 words, Language: Spanish)  in StringList format  [\"option1\", \"option2\", \"option3\"]") }).text
                            ?: "[\"...\",\"...\",\"...\"]"
                    actions = Json.decodeFromString(options)
                       // options.removeSurrounding("{\"", "\"}").split("\", \"").map { it.trim() }
                    Log.d("JMMLOG", "$adventure -> $options -> $actions")
                }
                _uiState.value = UiState.Success(adventure, actions)
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = UiState.Error(e.localizedMessage ?: "")
            }
        }
    }

    fun sendAction(index: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if(isDebug){
                    adventure = "The salty spray whipped across the deck of the \"Black Kraken\", a weathered galleon carving through the storm-tossed seas. Captain Blackheart, a grizzled veteran with a missing eye and a voice like thunder, bellowed orders, his scarred face etched with a grim determination.  His first mate, Shadow, perched in the crow's nest, scanning the horizon for any sign of trouble. Below, the rest of the crew – the burly Ironfist, the agile thieves Whisper and Swift – worked with practiced efficiency, their eyes gleaming with the thrill of danger and the promise of treasure. This was no ordinary voyage; this was an adventure that promised to test their mettle and change their lives forever. \n" +
                            "\n" +
                            "Shadow, his keen eyes scanning the churning waves, spotted a break in the storm clouds revealing a distant, jagged island. \"Captain!\" he shouted, \"Land ahoy! Bearing 220!\" Blackheart, his missing eye seeming to pierce the darkness, abarked orders. Ironfist steered the ship, his hands a blur on the helm, while Whisper and Swift, their agility honed by years of seafaring, adjusted sails, catching the wind and guiding the \"Black Kraken\" towards the island, a potential haven in the tempest."
                    actions = listOf("Examine the island through a spyglass", "Ask Shadow for more details about the island", "Prepare for a possible encounter with danger")
                }else {
                    val selectedAction = actions.getOrNull(index) ?: "Do nothing"
                    val eventResult =
                        generativeModel.generateContent(content { text("With this adventure [$adventure] the user selected the action [$selectedAction]. Write the results of the action (max 50 words) and write what happens next (max 100 words, Language: Spanish):") }).text
                            ?: ""
                    adventure = "$adventure\n$eventResult"
                    val options =
                        generativeModel.generateContent(content { text("With this [$adventure] Write 3 possible actions for the user (max 30 words, Language: Spanish)  in StringList format [\"option1\", \"option2\", \"option3\"]") }).text
                            ?: "[\"...\",\"...\",\"...\"]"
                    actions = Json.decodeFromString(options)
                        //options.removeSurrounding("[\"", "\"]").split("\", \"").map { it.trim() }
                    Log.d("JMMLOG", "$adventure -> $options -> $actions")
                }
                _uiState.value = UiState.Success(adventure, actions)
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = UiState.Error(e.localizedMessage ?: "")
            }
        }
    }
}