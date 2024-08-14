package com.example.ghtk_convert_phone_number

import androidx.lifecycle.ViewModel

class PhoneNumberViewModel: ViewModel() {

    private var phoneNumbers = mutableListOf<PhoneNumber>()
    val selectedPhoneNumbers = mutableListOf<PhoneNumber>()
}