package com.example.android.unscramble.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.android.unscramble.data.MAX_NO_OF_WORDS
import com.example.android.unscramble.data.SCORE_INCREASE
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.example.android.unscramble.data.allWords
import kotlinx.coroutines.flow.update


/*
The ViewModel component holds and exposes the state that the UI consumes.
ViewModel stores the app-related data that isn't destroyed when the activity
is destroyed and recreated by the Android framework.

The app automatically retains ViewModel objects during configuration changes
so that the data they hold is immediately available after the recomposition.


StateFlow is a data holder observable flow that emits the current and new state updates.
Its value property reflects the current state value. To update state and send it to the flow,
assign a new value to the value property of the MutableStateFlow class.

In Android, StateFlow works well with classes that need to maintain an observable immutable state.

A StateFlow can be exposed from the GameUiState so that the composables can listen for UI state
updates and make the screen state survive configuration changes.


 */

data class GameUiState(
    val currentScrambledWord: String = "",
    val isGuessedWordWrong: Boolean = false,
    val score: Int = 0,
    val currentWordCount: Int = 1,
    val isGameOver: Boolean = false

)

class GameViewModel : ViewModel() {

    // Game UI state
    private val _uiState = MutableStateFlow(GameUiState())

    private lateinit var currentWord: String

    private var usedWords: MutableSet<String> = mutableSetOf()

    var userGuess by mutableStateOf("")
        private set



    // Backing property to avoid state updates from other classes
    // A backing property lets you return something from a getter other than the exact object.
    // The asStateFlow() makes this mutable state flow a read-only state flow.
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    init {
        resetGame()
    }

    // helper method to pick a random word and shuffle the order of its letter
    private fun pickRandomWordAndShuffle(): String {
        // Continue picking up a new random word until you get one that hasn't been used before
        currentWord = allWords.random()
        if (usedWords.contains(currentWord)) {
            return pickRandomWordAndShuffle()
        } else {
            usedWords.add(currentWord)
            return shuffleCurrentWord(currentWord)
        }
    }

    // helper method to shuffle the a word letter around
    private fun shuffleCurrentWord(word: String): String {
        val tempWord = word.toCharArray()
        // Scramble the word
        tempWord.shuffle()
        while (String(tempWord).equals(word)) {
            tempWord.shuffle()
        }
        return String(tempWord)
    }

    fun updateUserGuess(guessedWord: String){
        userGuess = guessedWord
    }

    fun checkUserGuess() {

        if (userGuess.equals(currentWord, ignoreCase = true)) {
            // User's guess is correct, increase the score
            // and call updateGameState() to prepare the game for next round
            val updatedScore = _uiState.value.score.plus(SCORE_INCREASE)
            updateGameState(updatedScore)

        } else {
            // User's guess is wrong, show an error

            // Note on copy() method: Use the copy() function to copy an object,
            // allowing you to alter some of its properties while keeping the rest unchanged.
            _uiState.update { currentState ->
                currentState.copy(isGuessedWordWrong = true)
            }
        }
        // Reset user guess
        updateUserGuess("")
    }

    private fun updateGameState(updatedScore: Int) {
        if (usedWords.size == MAX_NO_OF_WORDS) {
            _uiState.update { currentState ->
                currentState.copy(
                    isGuessedWordWrong = false,
                    score = updatedScore,
                    isGameOver = true
                )
            }
        }
        else {
            _uiState.update { currentState ->
                currentState.copy(
                    isGuessedWordWrong = false,
                    currentScrambledWord = pickRandomWordAndShuffle(),
                    score = updatedScore,
                    currentWordCount = currentState.currentWordCount.inc(),

                    )
            }
        }
    }

    fun skipWord() {
        updateGameState(_uiState.value.score)
        // Reset user guess
        updateUserGuess("")
    }

    // helper method to reset the game, clear the words from usedWords, and
    // init _uiState
    fun resetGame() {
        usedWords.clear()
        _uiState.value = GameUiState(currentScrambledWord = pickRandomWordAndShuffle())
    }
}
