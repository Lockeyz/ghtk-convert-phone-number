package com.example.ghtk_convert_phone_number

import android.Manifest.permission.READ_CONTACTS
import android.Manifest.permission.WRITE_CONTACTS
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.ghtk_convert_phone_number.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), OnItemClickListener {

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: PhoneNumberAdapter
    private lateinit var contacts: MutableList<Contact>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val recyclerView = binding.recyclerView
        contacts = mutableListOf()

        // Kiểm tra quyền truy cập danh bạ và khởi tạo adapter
        if (hasContactsPermission()) {
            contacts = getContacts()
            adapter = PhoneNumberAdapter(contacts, this)
            recyclerView.adapter = adapter
        } else {
            requestContactsPermission()
        }

        val btnUpdateNumbers: Button = findViewById(R.id.btnUpdateNumbers)
        btnUpdateNumbers.setOnClickListener {
            updateSelectedContacts()
        }
    }

    private fun hasContactsPermission(): Boolean {
        val readPermission = ContextCompat.checkSelfPermission(this, READ_CONTACTS)
        val writePermission = ContextCompat.checkSelfPermission(this, WRITE_CONTACTS)
        return readPermission == PackageManager.PERMISSION_GRANTED && writePermission == PackageManager.PERMISSION_GRANTED
    }

    private fun requestContactsPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(READ_CONTACTS, WRITE_CONTACTS), 1)
    }

    private fun getContacts(): MutableList<Contact> {
        contacts.clear()
        val uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone._ID, //Data ID
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID, //Contact ID
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
        )
        val selection = "${ContactsContract.CommonDataKinds.Phone.NUMBER} LIKE ?"
        val selectionArgs = arrayOf("0%")

        val cursor = contentResolver.query(uri, projection, selection, selectionArgs, null)

        cursor?.use {
            while (it.moveToNext()) {
                val id =
                    it.getLong(it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone._ID))
                val contactId =
                    it.getLong(it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)) // RawContact ID
                val name =
                    it.getString(it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                val number =
                    it.getString(it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER))
                contacts.add(Contact(id, contactId, name, number))
            }
        }
        return contacts
    }

    // Cap nhat dau so 0167 thanh 037
    @SuppressLint("NotifyDataSetChanged")
    private fun updateSelectedContacts() {
        val selectedPhoneNumbers = adapter.getSelectedPhoneNumbers()

        selectedPhoneNumbers.forEach { phoneNumber ->
            val newNumber = phoneNumber.number.replaceFirst("0167", "037")

            // Cập nhật số điện thoại mới
            val contentValues = ContentValues().apply {
                put(ContactsContract.CommonDataKinds.Phone.NUMBER, newNumber)
            }

            try {
                // Sử dụng đúng URI và selection để cập nhật số điện thoại
                val updateUri = ContactsContract.Data.CONTENT_URI
                val updateSelection = "${ContactsContract.CommonDataKinds.Phone._ID} = ?"
                val updateSelectionArgs = arrayOf(phoneNumber.id.toString())

                val rowsUpdated = contentResolver.update(
                    updateUri,
                    contentValues,
                    updateSelection,
                    updateSelectionArgs
                )

                if (rowsUpdated > 0) {
                    // Đã cập nhật thành công
                    Log.e("Number Phone", "Updated: $newNumber")

                } else {
                    // Không thể cập nhật
                    Log.e("Number Phone", "Failed to update: ${phoneNumber.number}")
                }
            } catch (e: UnsupportedOperationException) {
                // Xử lý lỗi UnsupportedOperationException
                e.printStackTrace()
                Log.e(
                    "Number Phone",
                    "Failed to update due to unsupported operation: ${phoneNumber.number}"
                )
            }
        }
        getContacts()
        adapter.notifyDataSetChanged()
    }

    // Xoa so lien he khoi danh ba
    private fun deleteContact(position: Int) {
        val contactId = contacts[position].contactId

        val uri: Uri = ContactsContract.RawContacts.CONTENT_URI
        val where = "${ContactsContract.RawContacts.CONTACT_ID} = ?"
        val whereArgs = arrayOf(contactId.toString())
        contentResolver.delete(uri, where, whereArgs)
    }

    private fun showEditDialog(position: Int) {
        val contact = contacts[position]

        // Tạo một dialog để người dùng nhập thông tin mới
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_contact, null)
        val editText = dialogView.findViewById<EditText>(R.id.editTextPhoneNumber)

        editText.setText(contact.number)

        AlertDialog.Builder(this)
            .setTitle("Sửa số điện thoại")
            .setView(dialogView)
            .setPositiveButton("Lưu") { _, _ ->
                val newNumber = editText.text.toString()
                updateContact(contact.id, null, newNumber)
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateContact(contactId: Long, name: String?, newNumber: String) {
        val contentValues = ContentValues().apply {
            if (name != null) {
                put(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, name)
            }
            put(ContactsContract.CommonDataKinds.Phone.NUMBER, newNumber)
        }

        val updateUri = ContactsContract.Data.CONTENT_URI
        val updateSelection = "${ContactsContract.CommonDataKinds.Phone._ID} = ?"
        val updateSelectionArgs = arrayOf(contactId.toString())

        contentResolver.update(
            updateUri,
            contentValues,
            updateSelection,
            updateSelectionArgs)

        getContacts()
        adapter.notifyDataSetChanged()
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getContacts()
            adapter = PhoneNumberAdapter(contacts, this)
            findViewById<RecyclerView>(R.id.recyclerView).adapter = adapter
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onResume() {
        super.onResume()
        getContacts()
        adapter.notifyDataSetChanged()
    }

    override fun onEditClick(position: Int) {
        showEditDialog(position)
    }

    override fun onDeleteClick(position: Int) {
        deleteContact(position)
    }
}