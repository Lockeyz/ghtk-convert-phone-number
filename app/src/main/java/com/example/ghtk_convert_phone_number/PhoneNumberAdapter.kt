package com.example.ghtk_convert_phone_number

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.ghtk_convert_phone_number.databinding.ItemPhoneNumberBinding

class PhoneNumberAdapter(
    private val phoneNumbers: MutableList<PhoneNumber>,
    private val onItemClickListener: OnItemClickListener
) :
    RecyclerView.Adapter<PhoneNumberAdapter.PhoneNumberViewHolder>() {

    class PhoneNumberViewHolder(val binding: ItemPhoneNumberBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhoneNumberViewHolder {
        return PhoneNumberViewHolder(
            ItemPhoneNumberBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: PhoneNumberViewHolder, position: Int) {
//        val phoneNumber = phoneNumbers[position]

        holder.binding.txtPhoneNumber.text = phoneNumbers[position].number

        holder.binding.checkbox.isChecked = phoneNumbers[position].isSelected

        holder.binding.checkbox.setOnCheckedChangeListener { _, isChecked ->
            phoneNumbers[position].isSelected = isChecked
        }

        holder.binding.btnEdit.setOnClickListener {
            onItemClickListener.onEditClick(position)
            notifyItemChanged(position, "Contact Edited")
        }

        holder.binding.btnDelete.setOnClickListener {
            onItemClickListener.onDeleteClick(position)
//            phoneNumbers.removeAt(position)
            notifyItemChanged(position)
        }

    }

//    override fun onBindViewHolder(
//        holder: PhoneNumberViewHolder,
//        position: Int,
//        payloads: List<Any>
//    ) {
//        if (payloads.isNotEmpty()) {
//            for (payload in payloads) {
//                when (payload) {
////                    "Contact Edited" -> {
////
////                    }
//                    "Contact Deleted" -> {
//
//                    }
//                }
//            }
//        } else {
//            onBindViewHolder(holder, position)
//        }
//    }

    override fun getItemCount(): Int = phoneNumbers.size

    fun getSelectedPhoneNumbers(): List<PhoneNumber> {
        return phoneNumbers.filter { it.isSelected }
    }


}
