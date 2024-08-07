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
import java.util.Locale

class PiratesViewModel : ViewModel() {
    private val _uiState: MutableStateFlow<UiState> =
        MutableStateFlow(UiState.Initial)
    val uiState: StateFlow<UiState> =
        _uiState.asStateFlow()

    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = BuildConfig.apiKey,
    )

    private val deviceLanguage = Locale.getDefault().language
    private var adventure:String=""
    private var actions:List<String> = emptyList()
    private val crewSize = 5
    private val regexGetBracketsText = Regex("\\[.*?\\]")
    private val emptyStringListJSON = "[\"...\",\"...\",\"...\"]"
    private val isDebug = false
    private val MAX_TRIES = 5

    fun start(){
        _uiState.value = UiState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            var tries:Int = MAX_TRIES
            if(isDebug){
                adventure = "The salty spray whipped across the deck of the \"Black Kraken\", a weathered galleon carving through the storm-tossed seas. Captain Blackheart, a grizzled veteran with a missing eye and a voice like thunder, bellowed orders, his scarred face etched with a grim determination.  His first mate, Shadow, perched in the crow's nest, scanning the horizon for any sign of trouble. Below, the rest of the crew – the burly Ironfist, the agile thieves Whisper and Swift – worked with practiced efficiency, their eyes gleaming with the thrill of danger and the promise of treasure. This was no ordinary voyage; this was an adventure that promised to test their mettle and change their lives forever. \n"
                actions = Json.decodeFromString("[\"Help Captain Blackheart navigate the storm\", \"Assist Shadow in the crow's nest\", \"Join Ironfist, Whisper and Swift in their tasks\"]")
            }else {
                while(tries>0){
                    val shipText = try {
                        generativeModel.generateContent(
                            content { text("Sort description of a pirate ship ${extraInstructions(50)}") }).text
                            ?: ""
                    } catch (e: Exception) {
                        e.printStackTrace()
                        tries--
                        continue
                    }
                    Log.d("JMMLOG", "shipText=$shipText")
                    val crewText = try {
                        generativeModel.generateContent(
                            content { text("For this ship [$shipText]. " +
                                    "Build a description of the crew ($crewSize people) ${extraInstructions(80)}") }).text
                            ?: ""
                    } catch (e: Exception) {
                        e.printStackTrace()
                        tries--
                        continue
                    }
                    Log.d("JMMLOG", "crewText=$crewText")
                    adventure = try {
                        generativeModel.generateContent(
                            content { text("Starting point for the adventure of the user " +
                                    "with this ship [$shipText] " +
                                    "and this crew [$crewText] ${extraInstructions(100)}:") }).text
                            ?: ""
                    } catch (e: Exception) {
                        e.printStackTrace()
                        tries--
                        continue
                    }
                    Log.d("JMMLOG", "adventure=$adventure")
                    actions = try {
                        val options = generativeModel.generateContent(content { text("With this [$adventure] " +
                                "Write 3 possible actions for the user ${extraInstructions(30)} " +
                                "in StringList format  [\"option1\", \"option2\", \"option3\"]") }).text
                            ?: emptyStringListJSON
                        Json.decodeFromString(regexGetBracketsText.find(options)?.value ?: emptyStringListJSON)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        tries--
                        continue
                    }
                    Log.d("JMMLOG", "actions=$actions")
                    break
                }
                if(tries==0){
                    _uiState.value = UiState.Error("Error generating adventure")
                }else{
                    _uiState.value = UiState.Success(adventure, actions)
                }
            }
        }
    }

    fun sendAction(index: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            if(isDebug){
                adventure = "The salty spray whipped across the deck of the \"Black Kraken\", a weathered galleon carving through the storm-tossed seas. Captain Blackheart, a grizzled veteran with a missing eye and a voice like thunder, bellowed orders, his scarred face etched with a grim determination.  His first mate, Shadow, perched in the crow's nest, scanning the horizon for any sign of trouble. Below, the rest of the crew – the burly Ironfist, the agile thieves Whisper and Swift – worked with practiced efficiency, their eyes gleaming with the thrill of danger and the promise of treasure. This was no ordinary voyage; this was an adventure that promised to test their mettle and change their lives forever. \n" +
                        "\n" +
                        "Shadow, his keen eyes scanning the churning waves, spotted a break in the storm clouds revealing a distant, jagged island. \"Captain!\" he shouted, \"Land ahoy! Bearing 220!\" Blackheart, his missing eye seeming to pierce the darkness, abarked orders. Ironfist steered the ship, his hands a blur on the helm, while Whisper and Swift, their agility honed by years of seafaring, adjusted sails, catching the wind and guiding the \"Black Kraken\" towards the island, a potential haven in the tempest."
                actions = Json.decodeFromString("[\"Help Captain Blackheart navigate the storm\", \"Assist Shadow in the crow's nest\", \"Join Ironfist, Whisper and Swift in their tasks\"]")
            }else {
                val selectedAction = actions.getOrNull(index) ?: "Do nothing"
                var tries: Int = MAX_TRIES
                while (tries > 0) {
                    adventure = cleanAdventure(adventure)
                    val eventResult = try {
                        generativeModel.generateContent(
                            content { text("With this adventure [$adventure] the user selected the action [$selectedAction]. " +
                                    "Write the results of the action (max 50 words) and write what happens next ${extraInstructions(100)}:") }).text
                            ?: ""
                    } catch (e: Exception) {
                        e.printStackTrace()
                        tries--
                        continue
                    }
                    adventure = "$adventure\n${cleanAdventure(eventResult)}"
                    Log.d("JMMLOG", "adventure=$adventure")
                    actions = try {
                        val options =
                            generativeModel.generateContent(
                                content { text("With this [$adventure] " +
                                        "Write 3 possible actions for the user ${extraInstructions(30)} " +
                                        "in StringList format [\"option1\", \"option2\", \"option3\"]") }).text
                                ?: emptyStringListJSON
                        Json.decodeFromString(
                            regexGetBracketsText.find(options)?.value ?: emptyStringListJSON
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                        tries--
                        continue
                    }
                    Log.d("JMMLOG", "actions=$actions")
                    break
                }
                if (tries == 0) {
                    _uiState.value = UiState.Error("Error generating adventure")
                } else {
                    _uiState.value = UiState.Success(adventure, actions)
                }
            }
        }
    }

    private suspend fun cleanAdventure(adventure:String):String{
        if(adventure.length < 3000){
            return adventure.lines()
                .filterNot { it.trimStart().startsWith("#") }
                .joinToString("\n")
        }else {
            var tries: Int = MAX_TRIES
            while (tries > 0) {
                val resumedAdventure = try {
                    generativeModel.generateContent(content { text("Resume this text [$adventure] ${extraInstructions(500)}") }).text ?: adventure
                } catch (e: Exception) {
                    e.printStackTrace()
                    tries--
                    continue
                }
                return resumedAdventure
            }
        }
        return adventure
    }

    private fun extraInstructions(words:Int?=null):String{
        if(words==null){
            return "(Language: $deviceLanguage)"
        }else{
            return "(max $words words, Language: $deviceLanguage)"
        }
    }
}