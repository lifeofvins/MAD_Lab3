package it.polito.madcourse.group06.models.mychat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import it.polito.madcourse.group06.R
import it.polito.madcourse.group06.models.userprofile.UserProfile
import it.polito.madcourse.group06.viewmodels.MyChatViewModel

class ActiveChatAdapter(
    _listOfChat: ArrayList<ActiveChat>,
    private val myChatViewModel: MyChatViewModel,
    private val navigation: NavController
) : RecyclerView.Adapter<ActiveChatViewHolder>() {
    private val listOfChat: ArrayList<ActiveChat> = _listOfChat
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActiveChatViewHolder {
        val vg = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.active_chat_item, parent, false)
        return ActiveChatViewHolder(vg)
    }

    override fun onBindViewHolder(holder: ActiveChatViewHolder, position: Int) {
        holder.bind(listOfChat[position].advTitle, listOfChat[position].userFullname) {
            myChatViewModel.setChattingUserProfile(listOfChat[position].otherUserObj)
            myChatViewModel.fetchChatByAdvertisementID(
                listOfChat[position].userID,
                listOfChat[position].otherUserID,
                listOfChat[position].advID
            )
            navigation.navigate(R.id.action_activeChats_to_myChat)
        }
    }

    override fun getItemCount(): Int {
        return listOfChat.size
    }

    fun addActiveChat(activeChat: ActiveChat) {
        listOfChat.add(listOfChat.size, activeChat)
        notifyItemInserted(listOfChat.size - 1)
    }
}