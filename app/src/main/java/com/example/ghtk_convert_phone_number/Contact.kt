package com.example.ghtk_convert_phone_number

data class Contact(
    val id: Long,
    val contactId: Long,
    val name: String,
    val number: String,
    var isSelected: Boolean = false
)
