package com.example.ghtk_convert_phone_number

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.ghtk_convert_phone_number.databinding.ItemPhoneNumberBinding

class PhoneNumberAdapter(
    private val contacts: MutableList<Contact>,
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

        holder.binding.tvName.text = contacts[position].name
        holder.binding.tvPhoneNumber.text = contacts[position].number
        holder.binding.checkbox.isChecked = contacts[position].isSelected

        holder.binding.checkbox.setOnCheckedChangeListener { _, isChecked ->
            contacts[position].isSelected = isChecked
        }

        holder.binding.btnEdit.setOnClickListener {
            onItemClickListener.onEditClick(position)
            notifyItemChanged(position, "Contact Edited")
        }

        holder.binding.btnDelete.setOnClickListener {
            onItemClickListener.onDeleteClick(position)
            contacts.removeAt(position)
            notifyItemChanged(position)
        }

    }

    override fun onBindViewHolder(
        holder: PhoneNumberViewHolder,
        position: Int,
        payloads: List<Any>
    ) {
        if (payloads.isNotEmpty()) {
            for (payload in payloads) {
                when (payload) {
//                    "Contact Edited" -> {
//
//                    }
                    "Contact Deleted" -> {

                    }
                }
            }
        } else {
            onBindViewHolder(holder, position)
        }
    }

    override fun getItemCount(): Int = contacts.size

    fun getSelectedPhoneNumbers(): List<Contact> {
        return contacts.filter { it.isSelected }
    }


}
