package com.example.ghtk_convert_phone_number

data class PhoneNumber(
    val id: Long,
    val contactId: Long = 0,
    val number: String,
    var isSelected: Boolean = false
)
