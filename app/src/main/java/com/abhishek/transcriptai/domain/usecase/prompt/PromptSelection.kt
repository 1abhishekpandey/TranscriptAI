package com.abhishek.transcriptai.domain.usecase.prompt

import com.abhishek.transcriptai.domain.model.Prompt

/**
 * Work out which prompt counts as selected.
 * The saved selection wins when that prompt still exists. Otherwise the first prompt in the list is used,
 * the same fallback the transcript page applies when nothing is saved yet.
 *
 * @param prompts All saved prompts, in display order
 * @param selectedPromptId The saved selection (null if none)
 * @return The ID of the selected prompt, or null when there are no prompts
 */
fun resolveSelectedPromptId(prompts: List<Prompt>, selectedPromptId: String?): String? {
    if (selectedPromptId != null && prompts.any { it.id == selectedPromptId }) return selectedPromptId
    return prompts.firstOrNull()?.id
}

/**
 * Pick the prompt that takes over the selection once the selected prompt is deleted.
 *
 * @param prompts All saved prompts, in display order, including the one being deleted
 * @param deletedPromptId The ID of the prompt being deleted
 * @return The ID of the first remaining prompt, or null when none is left
 */
fun nextPromptIdAfterDelete(prompts: List<Prompt>, deletedPromptId: String): String? =
    prompts.firstOrNull { it.id != deletedPromptId }?.id
