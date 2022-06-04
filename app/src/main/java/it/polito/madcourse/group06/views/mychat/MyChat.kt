package it.polito.madcourse.group06.views.mychat

import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import it.polito.madcourse.group06.R
import it.polito.madcourse.group06.activities.TBMainActivity
import it.polito.madcourse.group06.models.mychat.MyChatAdapter
import it.polito.madcourse.group06.viewmodels.MyChatViewModel
import it.polito.madcourse.group06.viewmodels.UserProfileViewModel
import it.polito.madcourse.group06.models.mychat.MyMessage
import org.w3c.dom.Text
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.floor
import kotlin.math.min
import kotlin.math.roundToInt

class MyChat : Fragment() {

    private val myChatViewModel by activityViewModels<MyChatViewModel>()
    private val userProfileViewModel by activityViewModels<UserProfileViewModel>()

    private lateinit var recyclerView: RecyclerView
    private lateinit var chatAdapterCard: MyChatAdapter
    private lateinit var activityTB: TBMainActivity
    private lateinit var chatFullname: TextView
    private lateinit var chatNickname: TextView
    private lateinit var chattingUserProfilePicture: ImageView
    private lateinit var emptyChatMessage: TextView
    private lateinit var inputMessageBox: EditText
    private lateinit var sendMessageButton: ImageView
    private lateinit var backArrow: ImageView
    private lateinit var myPurposeContainer: LinearLayout
    private lateinit var chatAcceptButton: TextView
    private lateinit var chatRejectButton: TextView
    private lateinit var chatArrowUpButton: ImageView
    private lateinit var myLocation: EditText
    private lateinit var myStartingTime: TextView
    private lateinit var myDuration: TextView
    private lateinit var sendProposalButton: ImageView
    private var startingTimeHourProposal = 0
    private var startingTimeMinuteProposal = 0
    private var durationTimeProposal = 0.0
    private var listOfMessages = arrayListOf<MyMessage>()
    private var currentID = ""
    private var otherID = ""
    private var chatID = ""
    private var chatMenuArrowStartingPositionY = 0.0f
    private var chatMenuArrowStartingPositionX = 0.0f
    private var isAnswerMenuOpen = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        activityTB = requireActivity() as TBMainActivity
        return inflater.inflate(R.layout.chat_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activityTB.supportActionBar?.hide()
        this.recyclerView = view.findViewById(R.id.chatContentViewID)
        this.chatFullname = view.findViewById(R.id.chatFullnameID)
        this.chatNickname = view.findViewById(R.id.chatNicknameID)
        this.chattingUserProfilePicture = view.findViewById(R.id.chattingUserProfilePicture)
        this.emptyChatMessage = view.findViewById(R.id.emptyChatMessageID)
        this.inputMessageBox = view.findViewById(R.id.inputMessageBox)
        this.sendMessageButton = view.findViewById(R.id.chatSendMessageButtonID)
        this.backArrow = view.findViewById(R.id.chatBackArrowID)
        this.chatAcceptButton = view.findViewById(R.id.chatAcceptTextViewID)
        this.chatRejectButton = view.findViewById(R.id.chatRejectTextViewID)
        this.chatArrowUpButton = view.findViewById(R.id.chatMenuArrowID)
        this.myPurposeContainer = view.findViewById(R.id.myPurposeID)
        this.myLocation = view.findViewById(R.id.myLocationTVID)
        this.myStartingTime = view.findViewById(R.id.myStartingTimeTVID)
        this.myDuration = view.findViewById(R.id.myDurationTVID)
        this.sendProposalButton = view.findViewById(R.id.sendProposalButtonID)

        this.myPurposeContainer.alpha = 0f
        this.chatMenuArrowStartingPositionY = this.chatArrowUpButton.y
        this.chatMenuArrowStartingPositionX = this.chatArrowUpButton.x

        userProfileViewModel.currentUser.observe(viewLifecycleOwner) {
            this.currentID = it.id!!
        }
        myChatViewModel.chattingUser.observe(viewLifecycleOwner) {
            this.chatFullname.text = it.fullName
            this.chatNickname.text = "@${it.nickname}"
            userProfileViewModel.retrieveProfilePicture(this.chattingUserProfilePicture, it.imgPath!!)
            this.otherID = it.id!!
        }
        myChatViewModel.myCurrentChat.observe(viewLifecycleOwner) {
            this.chatID = it.chatID
            this.listOfMessages = it.chatContent
        }

        this.sendMessageButton.setOnClickListener {
            if (this.inputMessageBox.text.isNotEmpty()) {
                /**
                 * Add the new message on the remote DB
                 */
                val msg = MyMessage(
                    currentID, otherID,
                    this.inputMessageBox.text.toString(),
                    "", "", 0.0,
                    SimpleDateFormat(
                        "dd/MM/yyyy hh:mm",
                        Locale.getDefault()
                    ).format(Date()).toString(), false, 0
                )
                this.listOfMessages.add(msg)
                myChatViewModel.addNewMessage(this.chatID, this.listOfMessages)

                /**
                 * Add the message to the adapter
                 */
                chatAdapterCard.addMessage(msg)

                // Set the input message box to an empty textview
                this.inputMessageBox.setText("")

                // Set the linear layout of the recycler view
                val linearLayoutManager = LinearLayoutManager(this.context)
                linearLayoutManager.stackFromEnd = true
                this.recyclerView.layoutManager = linearLayoutManager
                this.recyclerView.adapter = chatAdapterCard
            }
        }

        this.myStartingTime.setOnClickListener { popTimePickerStarting(this.myStartingTime) }
        this.myDuration.setOnClickListener { popTimePickerDuration(this.myDuration) }

        this.sendProposalButton.setOnClickListener {
            if (this.myLocation.text.toString().isNotEmpty() &&
                this.myStartingTime.text.toString().isNotEmpty() &&
                this.myDuration.text.toString().isNotEmpty() /*&& isProposalValid()*/
            ) {
                val msg = MyMessage(
                    currentID, otherID,
                    "",
                    this.myLocation.text.toString(),
                    this.myStartingTime.text.toString(),
                    this.durationTimeProposal,
                    SimpleDateFormat(
                        "dd/MM/yyyy hh:mm",
                        Locale.getDefault()
                    ).format(Date()).toString(), true, 0
                )
                this.listOfMessages.add(msg)
                myChatViewModel.addNewMessage(this.chatID, this.listOfMessages)

                /**
                 * Add the message to the adapter
                 */
                chatAdapterCard.addMessage(msg)

                // Set the input message box to an empty textview
                this.inputMessageBox.setText("")

                // Set the linear layout of the recycler view
                val linearLayoutManager = LinearLayoutManager(this.context)
                linearLayoutManager.stackFromEnd = true
                this.recyclerView.layoutManager = linearLayoutManager
                this.recyclerView.adapter = chatAdapterCard
                closeProposal()
                resetProposalFields()
                this.isAnswerMenuOpen = !this.isAnswerMenuOpen
            } else {
                Toast.makeText(requireContext(), "Incomplete proposal.", Toast.LENGTH_LONG).show()
            }
        }

        this.chatArrowUpButton.setOnClickListener {
            if (!this.isAnswerMenuOpen) {
                openNewProposal()
                this.isAnswerMenuOpen = !this.isAnswerMenuOpen
            } else {
                closeProposal()
                this.isAnswerMenuOpen = !this.isAnswerMenuOpen
            }
        }

        this.backArrow.setOnClickListener {
            findNavController().navigate(R.id.action_myChat_to_ShowListOfServices)
            activityTB.supportActionBar?.show()
        }

        myChatViewModel.myCurrentChat.observe(viewLifecycleOwner) { chat ->
            this.emptyChatMessage.isVisible = chat.chatContent.isEmpty()
            chatAdapterCard = MyChatAdapter(chat.chatContent, currentID, otherID)

            val linearLayoutManager = LinearLayoutManager(this.context)
            linearLayoutManager.stackFromEnd = true
            this.recyclerView.layoutManager = linearLayoutManager
            this.recyclerView.adapter = chatAdapterCard
        }

        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigate(R.id.action_myChat_to_ShowListOfServices)
                activityTB.supportActionBar?.show()
            }
        })
    }

    /**
     * This permits having the right-to-left animation
     */
    override fun onCreateAnimation(transit: Int, enter: Boolean, nextAnim: Int): Animation? {
        val anim = AnimationUtils.loadAnimation(requireActivity(), R.anim.slide_in_from_right)
        anim.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {}
            override fun onAnimationRepeat(animation: Animation) {}
            override fun onAnimationEnd(animation: Animation) {
                view?.findViewById<ConstraintLayout>(R.id.filterBackground)?.background = resources.getDrawable(R.drawable.semi_transparent_background)
            }
        })
        return anim
    }

    private fun openNewProposal() {
        // should be opened
        this.recyclerView.animate().apply {
            alpha(0.3f)
        }.start()
        this.inputMessageBox.isEnabled = false
        this.sendMessageButton.isEnabled = false
        this.myLocation.isEnabled = true
        this.myStartingTime.isEnabled = true
        this.myDuration.isEnabled = true
        this.sendProposalButton.isEnabled = true
        this.inputMessageBox.animate().apply {
            alpha(0.3f)
        }.start()
        this.sendMessageButton.animate().apply {
            alpha(0.3f)
        }.start()

        this.chatArrowUpButton.setImageResource(R.drawable.ic_baseline_keyboard_arrow_up_24)
        this.chatArrowUpButton.backgroundTintList = AppCompatResources.getColorStateList(
            requireContext(),
            R.color.prussian_blue
        )
        this.myPurposeContainer.isGone = false
        this.myPurposeContainer.animate().apply {
            duration = 350
            alpha(1f)
            translationY(-25f)
        }.start()
        this.chatArrowUpButton.animate().apply {
            duration = 250
            rotationX(180f)
        }.start()
        this.sendProposalButton.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.slide_in_from_right))
        this.myLocation.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.slide_in_up))
        this.myStartingTime.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.slide_in_up))
        this.myDuration.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.slide_in_up))

    }

    private fun closeProposal() {
        // should be closed
        this.recyclerView.animate().apply {
            alpha(1f)
        }.start()
        this.inputMessageBox.isEnabled = true
        this.sendMessageButton.isEnabled = true
        this.myLocation.isEnabled = false
        this.myStartingTime.isEnabled = false
        this.myDuration.isEnabled = false
        this.sendProposalButton.isEnabled = false
        this.inputMessageBox.animate().apply {
            alpha(1f)
        }.start()
        this.sendMessageButton.animate().apply {
            alpha(1f)
        }.start()

        this.chatArrowUpButton.setImageResource(R.drawable.ic_add_black_24dp)
        this.chatArrowUpButton.backgroundTintList = AppCompatResources.getColorStateList(
            requireContext(),
            R.color.darkGray
        )
        this.myPurposeContainer.animate().apply {
            duration = 250
            alpha(0f)
            translationY(55f)
        }.withEndAction {
            this.myPurposeContainer.isGone = true
        }.start()
        this.chatArrowUpButton.animate().apply {
            duration = 250
            rotationX(0f)
        }.start()
    }

    private fun resetProposalFields() {
        this.myLocation.setText("")
        this.myStartingTime.setText("")
        this.myDuration.setText("")
    }


    /**
     * popTimePickerStarting is the callback to launch the TimePicker for inserting the starting time
     *
     * @param timeBox reference to the TextView of the starting time
     */
    private fun popTimePickerStarting(timeBox: TextView) {
        val onTimeSetListener: TimePickerDialog.OnTimeSetListener = TimePickerDialog.OnTimeSetListener() { timepicker, selectedHour, selectedMinute ->
            startingTimeHourProposal = selectedHour
            startingTimeMinuteProposal = selectedMinute
            timeBox.text = String.format(Locale.getDefault(), "%02d:%02d", startingTimeHourProposal, startingTimeMinuteProposal)
        }
        val timePickerDialog = TimePickerDialog(this.context, onTimeSetListener, startingTimeHourProposal, startingTimeMinuteProposal, true)
        timePickerDialog.setTitle("Select time")
        timePickerDialog.show()
    }

    /**
     * popTimePickerDuration is the callback to launch the TimePicker for inserting the duration
     *
     * @param timeBox reference to the TextView of the duration
     */
    private fun popTimePickerDuration(timeBox: TextView) {
        val onTimeSetListener: TimePickerDialog.OnTimeSetListener = TimePickerDialog.OnTimeSetListener() { timepicker, selectedHour, selectedMinute ->
            durationTimeProposal = selectedHour.toDouble() + selectedMinute.toDouble() / 60
            timeBox.text = String.format(Locale.getDefault(), "%d h %d min", selectedHour, selectedMinute)
        }
        val timePickerDialog: TimePickerDialog = TimePickerDialog(
            this.context, onTimeSetListener,
            floor(durationTimeProposal).toInt(), ((durationTimeProposal - floor(durationTimeProposal)) * 60).toInt(), true
        )
        timePickerDialog.setTitle("Select Duration")
        timePickerDialog.show()
    }

    /**
     * computeTimeDifference is a method which return the time difference from two "time-strings" and whether
     * they are acceptable or not.
     *
     * @param startingTime the starting time
     * @param endingTime the ending time
     * @return a Pair<Float, Boolean> where it's specified the time difference and its acceptability
     */
    private fun computeTimeDifference(startingTime: String, endingTime: String): Pair<Double, Boolean> {
        var timeDifference: Double = 0.0
        if (startingTime.isNullOrEmpty() || endingTime.isNullOrEmpty()) {
            return Pair(-1.0, false)
        }
        val startingHour = startingTime.split(":")[0].toInt()
        val startingMinute = startingTime.split(":")[1].toInt()
        val endingHour = endingTime.split(":")[0].toInt()
        val endingMinute = endingTime.split(":")[1].toInt()

        timeDifference += (endingHour - startingHour) + ((endingMinute - startingMinute) / 60.0)

        return Pair(
            (timeDifference * 100.0).roundToInt() / 100.0,
            (timeDifference * 100.0).roundToInt() / 100.0 >= 0
        )
    }
}