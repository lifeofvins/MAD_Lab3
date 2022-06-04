package it.polito.madcourse.group06.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.storage.FirebaseStorage
import it.polito.madcourse.group06.models.advertisement.Advertisement
import it.polito.madcourse.group06.models.mychat.MyChatModel
import it.polito.madcourse.group06.models.mychat.MyMessage
import it.polito.madcourse.group06.models.userprofile.UserProfile
import java.lang.Exception

class MyChatViewModel(application: Application) : AndroidViewModel(application) {
    private val db = FirebaseFirestore.getInstance()
    private var listenerRegistration: ListenerRegistration
    private val context = application

    /**
     * [UserProfile] with which the current user is chatting
     */
    private var _chattingUserPH = UserProfile(
        "", "", "", "", "", "", "",
        "", null, 0.0, 0.0, 0.0,
        ArrayList(), ArrayList(), null, ArrayList(), HashMap()
    )
    private val _pvtChattingUser = MutableLiveData<UserProfile>().also { it.value = _chattingUserPH }
    val chattingUser: LiveData<UserProfile> = this._pvtChattingUser

    /**
     * [MyChatModel] live data
     */
    private var _myChatPH = MyChatModel("", "", "", arrayListOf(), "-1")
    private val _pvtMyChat = MutableLiveData<MyChatModel>().also { it.value = _myChatPH }
    val myCurrentChat: LiveData<MyChatModel> = this._pvtMyChat

    /**
     * List of Chats
     */

    /**
     * List of Advertisements
     */
    private val _chats = MutableLiveData<List<MyChatModel>>()
    val listOfChats: LiveData<List<MyChatModel>> = _chats

    init {
        listenerRegistration = db.collection("Chat")
            .addSnapshotListener { value, error ->
                if (error != null) {
                    _chats.value = emptyList()
                } else {
                    _chats.value = value!!.mapNotNull { elem ->
                        elem.toMyChatModel()
                    }
                    for (c in _chats.value!!) {
                        if (c.chatID == myCurrentChat.value!!.chatID) {
                            this._myChatPH = c
                            this._pvtMyChat.value = this._myChatPH
                        }
                    }
                }
            }
    }

    /**
     * setChattingUserProfile sets the chattingUser in order to retrieve the information
     * of the user with which the current user is chatting and get them in the MyChat fragment
     * @param usr the user profile with which we are going to chat
     */
    fun setChattingUserProfile(usr: UserProfile) {
        this._chattingUserPH = usr
        this._pvtChattingUser.value = this._chattingUserPH
    }

    private fun createNewChat(advID: String, currentUserID: String, otherUserID: String) {
        var chatID = ""
        db
            .collection("Chat")
            .document().also { chatID = it.id }
            .set(
                mapOf(
                    "chat_id" to chatID,
                    "user_id" to currentUserID,
                    "other_user_id" to otherUserID,
                    "chat_content" to mutableListOf<MyMessage>(),
                    "adv_id" to advID,
                )
            )
        this._myChatPH = MyChatModel(chatID, currentUserID, otherUserID, arrayListOf(), advID)
        this._pvtMyChat.value = this._myChatPH
    }

    fun fetchChatByAdvertisementID(currentUserID: String, otherUserID: String, advertisementID: String) {
        db
            .collection("Chat")
            .whereEqualTo("user_id", currentUserID)
            .whereEqualTo("adv_id", advertisementID)
            .get()
            .addOnSuccessListener { query ->
                if (query.isEmpty) {
                    this.createNewChat(advertisementID, currentUserID, otherUserID)
                } else {
                    query.documents[0].also { docSnap ->
                        this._myChatPH = docSnap.toMyChatModel() ?: MyChatModel("", "", "", arrayListOf(), "")
                        this._pvtMyChat.value = this._myChatPH
                    }
                }
            }
    }

    fun addNewMessage(chatID: String, messages: ArrayList<MyMessage>) {
        db
            .collection("Chat")
            .document(chatID)
            .update(
                "chat_content", messages
            )
            .addOnSuccessListener {
                this._myChatPH.chatContent = messages
                this._pvtMyChat.value = this._myChatPH
            }
    }

    private fun DocumentSnapshot.toMyChatModel(): MyChatModel? {
        return try {
            val advID = this.get("adv_id") as String
            val chatContent: ArrayList<MyMessage>? = (this.get("chat_content") as ArrayList<HashMap<Any, Any>>)
                .toMyMessageArray()
            val chatID = this.get("chat_id") as String
            val currentID = this.get("user_id") as String
            val otherUserID = this.get("other_user_id") as String
            MyChatModel(chatID, currentID, otherUserID, chatContent!!, advID)
        } catch (e: Exception) {
            null
        }
    }

    private fun ArrayList<HashMap<Any, Any>>.toMyMessageArray(): ArrayList<MyMessage> {
        val out: ArrayList<MyMessage> = ArrayList()
        for (value in this) {
            val myMessage = MyMessage("", "", "", "", "", 0.0, "", false, 0)
            myMessage.senderID = value["sender_id"] as String
            myMessage.receiverID = value["receiver_id"] as String
            myMessage.msg = value["msg"] as String
            myMessage.location = value["location"] as String
            myMessage.startingTime = value["starting_time"] as String
            myMessage.duration = value["duration"] as Double
            myMessage.timestamp = value["timestamp"] as String
            myMessage.isAnOffer = value["is_an_offer"] as Boolean
            myMessage.hasItBeenAccepted = value["has_it_been_accepted"] as Int
            out.add(myMessage)
        }
        return out
    }

    override fun onCleared() {
        super.onCleared()
        listenerRegistration.remove()
    }
}