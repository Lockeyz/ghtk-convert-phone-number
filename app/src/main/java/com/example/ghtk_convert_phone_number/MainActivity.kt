package com.example.ghtk_convert_phone_number

import android.Manifest.permission.READ_CONTACTS
import android.Manifest.permission.WRITE_CONTACTS
import android.annotation.SuppressLint
import android.content.ContentProviderOperation
import android.content.ContentUris
import android.content.ContentValues
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity(), OnItemClickListener {

    private lateinit var adapter: PhoneNumberAdapter
    private lateinit var phoneNumbers: MutableList<PhoneNumber>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Kiểm tra quyền truy cập danh bạ và khởi tạo adapter
        if (hasContactsPermission()) {
            phoneNumbers = getPhoneNumbers()
            adapter = PhoneNumberAdapter(phoneNumbers, this)
            recyclerView.adapter = adapter
        } else {
            requestContactsPermission()
        }

        val btnUpdateNumbers: Button = findViewById(R.id.btnUpdateNumbers)
        btnUpdateNumbers.setOnClickListener {
            updateSelectedPhoneNumbers()
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

    private fun getPhoneNumbers(): MutableList<PhoneNumber> {
        val phoneNumbers = mutableListOf<PhoneNumber>()
//        val uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        val uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone._ID, //Data ID
            ContactsContract.CommonDataKinds.Phone.RAW_CONTACT_ID, //Raw Contact ID
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
                    it.getLong(it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.RAW_CONTACT_ID)) // RawContact ID
                val number =
                    it.getString(it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER))
                phoneNumbers.add(PhoneNumber(id, contactId, number))
            }
        }

        return phoneNumbers
    }

    // Cap nhat dau so 0167 thanh 037
    @SuppressLint("NotifyDataSetChanged")
    private fun updateSelectedPhoneNumbers() {
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
                phoneNumbers.remove(phoneNumber)
                adapter.notifyDataSetChanged()
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
    }

    // Xoa so lien he khoi danh ba
    private fun deleteContact(position: Int) {
        val contactId = phoneNumbers[position].contactId

//        val uri: Uri = ContentUris.withAppendedId(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, position.toLong())
        val uri: Uri = ContactsContract.RawContacts.CONTENT_URI
        val where = "${ContactsContract.RawContacts.CONTACT_ID} = ?"
        val whereArgs = arrayOf(contactId.toString())

        try {
            val rowsDeleted = contentResolver.delete(uri, where, whereArgs)
            if (rowsDeleted > 0) {
                // Xóa liên hệ thành công khỏi danh sách hiện tại
//                phoneNumbers.removeAt(position)
//                adapter.notifyItemRemoved(position)
                Log.e("Number Phone", "Contact deleted: $contactId")
            } else {
                Log.e("Number Phone", "Failed to delete contact: $contactId")
            }
        } catch (e: UnsupportedOperationException) {
            e.printStackTrace()
            Log.e(
                "Number Phone",
                "Failed to delete contact due to unsupported operation: $contactId"
            )
        }
    }

    private fun showEditDialog(position: Int) {
        val phoneNumber = phoneNumbers[position]

        // Tạo một dialog để người dùng nhập thông tin mới
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_contact, null)
        val editText = dialogView.findViewById<EditText>(R.id.editTextPhoneNumber)

        editText.setText(phoneNumber.number)

        AlertDialog.Builder(this)
            .setTitle("Sửa số điện thoại")
            .setView(dialogView)
            .setPositiveButton("Lưu") { _, _ ->
                val newNumber = editText.text.toString()
//                updateSelectedPhoneNumbers()
                updateContact(phoneNumber.id, null, newNumber)
//                refreshPhoneNumbers()
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

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

        try {
            val rowsUpdated = contentResolver.update(
                updateUri,
                contentValues,
                updateSelection,
                updateSelectionArgs
            )
            if (rowsUpdated > 0) {
                // Update in memory list as well
//                phoneNumbers.find { it.id == contactId }?.number = newNumber
                adapter.notifyDataSetChanged()
                Log.e("Number Phone", "Contact updated: $newNumber")
            } else {
                Log.e("Number Phone", "Failed to update contact: $contactId")
            }
        } catch (e: UnsupportedOperationException) {
            e.printStackTrace()
            Log.e(
                "Number Phone",
                "Failed to update contact due to unsupported operation: $contactId"
            )
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            val phoneNumbers = getPhoneNumbers()
            adapter = PhoneNumberAdapter(phoneNumbers, this)
            findViewById<RecyclerView>(R.id.recyclerView).adapter = adapter
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onResume() {
        super.onResume()
        adapter.notifyDataSetChanged()
    }

    override fun onEditClick(position: Int) {
        showEditDialog(position)
    }

    override fun onDeleteClick(position: Int) {
        deleteContact(position)
    }
}