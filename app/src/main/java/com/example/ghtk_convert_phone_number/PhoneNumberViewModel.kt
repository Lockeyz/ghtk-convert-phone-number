package com.example.ghtk_convert_phone_number

import androidx.lifecycle.ViewModel

class PhoneNumberViewModel: ViewModel() {

    private var contacts = mutableListOf<Contact>()
    val selectedContacts = mutableListOf<Contact>()
}