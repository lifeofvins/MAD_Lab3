package it.polito.madcourse.group06.views.mychat

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import it.polito.madcourse.group06.R
import it.polito.madcourse.group06.models.mychat.ActiveChat
import it.polito.madcourse.group06.models.mychat.ActiveChatAdapter
import it.polito.madcourse.group06.models.userprofile.UserProfile
import it.polito.madcourse.group06.viewmodels.AdvertisementViewModel
import it.polito.madcourse.group06.viewmodels.MyChatViewModel
import it.polito.madcourse.group06.viewmodels.UserProfileViewModel

class ActiveChats : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var activeChatAdapter: ActiveChatAdapter
    private var listOfActiveChats: ArrayList<ActiveChat> = arrayListOf()
    private val myChatViewModel: MyChatViewModel by activityViewModels()
    private val userProfileViewModel: UserProfileViewModel by activityViewModels()
    private val advertisementViewModel: AdvertisementViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.active_chats_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        this.recyclerView = view.findViewById(R.id.activeChatsRecyclerViewID)
        this.recyclerView.layoutManager = LinearLayoutManager(requireContext())

        userProfileViewModel.currentUser.observe(viewLifecycleOwner) { currentUser ->
            advertisementViewModel.listOfAdvertisements.observe(viewLifecycleOwner) { listOfAdvertisement ->
                myChatViewModel.listOfChats.observe(viewLifecycleOwner) { listOfChats ->
                    userProfileViewModel.listOfUsers.observe(viewLifecycleOwner) { listOfUsers ->
                        for (chat in listOfChats) {
                            var chattingUser: UserProfile = UserProfile("", "", "", "", "", "", "", "", arrayListOf(), 0.0, 0.0, 0.0, arrayListOf(), arrayListOf(), "", arrayListOf(), hashMapOf())
                            if (chat.userID == currentUser.id!!) {
                                for (adv in listOfAdvertisement) {
                                    for (user in listOfUsers) {
                                        if (user.id!! == chat.otherUserID) {
                                            val activeChat = ActiveChat(
                                                adv.advTitle, adv.id!!, user.fullName!!, chat.chatID,
                                                chat.userID, chat.otherUserID
                                            )
                                            if (adv.id!! == chat.advID && !listOfActiveChats.contains(activeChat)) {
                                                chattingUser = user
                                                listOfActiveChats.add(activeChat)
                                            }
                                        }
                                    }
                                }
                            } else if (chat.otherUserID == currentUser.id!!) {
                                for (adv in listOfAdvertisement) {
                                    for (user in listOfUsers) {
                                        if (user.id!! == chat.userID) {
                                            val activeChat = ActiveChat(
                                                adv.advTitle, adv.id!!, user.fullName!!, chat.chatID,
                                                chat.userID, chat.otherUserID
                                            )
                                            if (adv.id!! == chat.advID && !listOfActiveChats.contains(activeChat)) {
                                                chattingUser = user
                                                listOfActiveChats.add(activeChat)
                                            }
                                        }
                                    }
                                }
                            }
                            activeChatAdapter = ActiveChatAdapter(listOfActiveChats, myChatViewModel, findNavController(), chattingUser)
                        }
                        listOfActiveChats = arrayListOf()
                        this.recyclerView.adapter = activeChatAdapter
                    }
                }
            }
        }

        activity?.onBackPressedDispatcher?.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().navigate(R.id.action_activeChats_to_ShowListOfServices)
                }
            })
    }
}