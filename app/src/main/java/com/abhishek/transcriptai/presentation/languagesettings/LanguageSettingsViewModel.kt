package com.abhishek.transcriptai.presentation.languagesettings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abhishek.transcriptai.domain.model.SubtitleLanguage
import com.abhishek.transcriptai.domain.model.SubtitleLanguages
import com.abhishek.transcriptai.domain.repository.LanguagePreferenceRepository
import com.abhishek.transcriptai.util.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Language Settings screen
 * Lists the available subtitle languages and saves the one the user picks
 *
 * @param languagePreferenceRepository Repository for the saved subtitle language choice
 */
@HiltViewModel
class LanguageSettingsViewModel @Inject constructor(
    private val languagePreferenceRepository: LanguagePreferenceRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Code of the language picked earlier, so it can be marked in the list
    val selectedCode: StateFlow<String?> = languagePreferenceRepository.observeLanguageChoice()
        .map { it.otherLanguageCode }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    // Languages matching the search by native or English name
    val languages: StateFlow<List<SubtitleLanguage>> = _searchQuery
        .map { query ->
            val trimmed = query.trim()
            if (trimmed.isEmpty()) {
                SubtitleLanguages.all
            } else {
                SubtitleLanguages.all.filter {
                    it.englishName.contains(trimmed, ignoreCase = true) ||
                        it.nativeName.contains(trimmed, ignoreCase = true)
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SubtitleLanguages.all)

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    /**
     * Save the picked language and select the "Other languages" option on the home screen
     *
     * @param language Language the user tapped
     * @param onSaved Called once the choice is saved
     */
    fun selectLanguage(language: SubtitleLanguage, onSaved: () -> Unit) {
        Logger.logI("LanguageSettingsViewModel: Language selected: ${language.englishName} (${language.code})")
        viewModelScope.launch {
            languagePreferenceRepository.setOtherLanguage(language.code)
            onSaved()
        }
    }
}
