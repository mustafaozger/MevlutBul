package com.example.mevltbul.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mevltbul.Classes.GroupMessage
import com.example.mevltbul.Classes.Marker
import com.example.mevltbul.Utils.Utils
import com.example.mevltbul.databinding.DesignNearEventListBinding
import com.example.mevltbul.databinding.ReceiveMessageBinding
import com.example.mevltbul.databinding.SendMessageBinding

class MessaeListPageAdapter(val context: Context, messageList:ArrayList<GroupMessage>, senderId:String, receiverGroupId:String):RecyclerView.Adapter<RecyclerView.ViewHolder?>() {
    lateinit var messageList :ArrayList<GroupMessage>
    val ITEM_SENT=1
    val ITEM_RECEIVE=2
    var senderId:String
    var receiverGroupId:String
    inner class SendMessageViewHolder(val binding:SendMessageBinding):RecyclerView.ViewHolder(binding.root){}
    inner class ReceiveMessageViewHolder(val binding:ReceiveMessageBinding):RecyclerView.ViewHolder(binding.root){}

    init {
        if (messageList!=null){
            this.messageList=messageList
        }
        this.senderId=senderId
        this.receiverGroupId=receiverGroupId
    }

    override fun getItemViewType(position: Int): Int {
        val message=messageList[position]
        return if (message.senderId==Utils.UID){
            TODO("FirebaseAuth.getInstance().uid == message.senderId yapılacak")

            ITEM_SENT
        } else{
            ITEM_RECEIVE
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType==ITEM_SENT){
            return SendMessageViewHolder(SendMessageBinding.inflate(LayoutInflater.from(context),parent,false))
        }else{
        return ReceiveMessageViewHolder(ReceiveMessageBinding.inflate(LayoutInflater.from(parent.context),parent,false))
        }
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int){
        val message=messageList[position]

        if (holder.javaClass==SendMessageViewHolder::class.java){
            val viewHolder=holder as SendMessageViewHolder
            viewHolder.binding.txtSendedMessage.text=message.message
        }else{
            val viewHolder=holder as ReceiveMessageViewHolder
            viewHolder.binding.txtRecievedMessage.text=message.message
        }

    }


}