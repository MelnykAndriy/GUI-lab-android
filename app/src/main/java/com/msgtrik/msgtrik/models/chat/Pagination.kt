package com.msgtrik.msgtrik.models.chat

data class Pagination(
    val total: Int,
    val pages: Int,
    val page: Int,
    val limit: Int
) 