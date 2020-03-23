package com.example.demoapplication

import android.animation.ObjectAnimator
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.demoapplication.databinding.ItemRecordListBinding

class PCMListAdapter : RecyclerView.Adapter<PCMListAdapter.PCMItemHolder>() {

    private val pcmFiles = ArrayList<PCMFileModel>()
    fun addAll(files: List<PCMFileModel>) {
        pcmFiles.clear()
        pcmFiles.addAll(files)
        notifyDataSetChanged()
    }

    class PCMItemHolder(val dataBing: ItemRecordListBinding) :
        RecyclerView.ViewHolder(dataBing.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PCMItemHolder {
        val inflate = DataBindingUtil.inflate<ItemRecordListBinding>(
            LayoutInflater.from(parent.context),
            R.layout.item_record_list,
            parent,
            false
        )

        return PCMItemHolder(inflate)
    }

    var onItemPlayClickListener: ((ItemRecordListBinding, PCMFileModel) -> Unit)? = null

    override fun getItemCount(): Int = pcmFiles.size

    override fun onBindViewHolder(holder: PCMItemHolder, position: Int) {
        Log.d("TAG", pcmFiles[position].name)
        holder.dataBing.setVariable(BR.itemData, pcmFiles[position])
        holder.dataBing.executePendingBindings()
        holder.dataBing.imageView.setOnClickListener {
            onItemPlayClickListener?.invoke(holder.dataBing, pcmFiles[position])
        }
    }
}