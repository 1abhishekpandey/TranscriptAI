package com.abhishek.summaryai.data.mapper

import com.abhishek.summaryai.data.local.database.entity.PromptEntity
import com.abhishek.summaryai.domain.model.Prompt

object PromptMapper {
    fun toDomain(entity: PromptEntity): Prompt {
        return Prompt(
            id = entity.id,
            text = entity.text,
            createdAt = entity.createdAt,
            lastModified = entity.lastModified,
            isDefault = entity.isDefault
        )
    }

    fun toEntity(domain: Prompt): PromptEntity {
        return PromptEntity(
            id = domain.id,
            text = domain.text,
            createdAt = domain.createdAt,
            lastModified = domain.lastModified,
            isDefault = domain.isDefault
        )
    }

    fun toDomainList(entities: List<PromptEntity>): List<Prompt> {
        return entities.map { toDomain(it) }
    }
}
