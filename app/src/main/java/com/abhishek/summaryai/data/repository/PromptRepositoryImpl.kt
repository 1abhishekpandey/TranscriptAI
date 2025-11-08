package com.abhishek.summaryai.data.repository

import com.abhishek.summaryai.data.local.database.dao.PromptDao
import com.abhishek.summaryai.data.mapper.PromptMapper
import com.abhishek.summaryai.domain.model.Prompt
import com.abhishek.summaryai.domain.repository.PromptRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PromptRepositoryImpl(
    private val promptDao: PromptDao
) : PromptRepository {

    override fun getPrompts(): Flow<List<Prompt>> {
        return promptDao.getAllPrompts().map { entities ->
            PromptMapper.toDomainList(entities)
        }
    }

    override suspend fun getPromptById(id: String): Prompt? {
        val entity = promptDao.getPromptById(id)
        return entity?.let { PromptMapper.toDomain(it) }
    }

    override suspend fun savePrompt(prompt: Prompt) {
        val entity = PromptMapper.toEntity(prompt)
        promptDao.insertPrompt(entity)
    }

    override suspend fun deletePrompt(id: String) {
        promptDao.deletePrompt(id)
    }

    override suspend fun updateLastSelectedTimestamp(promptId: String, timestamp: Long) {
        promptDao.updateLastSelectedTimestamp(promptId, timestamp)
    }
}
