package it.polito.madcourse.group06.views.timeslot

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Paint
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.snackbar.Snackbar
import it.polito.madcourse.group06.R
import it.polito.madcourse.group06.utilities.timeDoubleHourToString
import it.polito.madcourse.group06.viewmodels.AdvertisementViewModel
import it.polito.madcourse.group06.viewmodels.MyChatViewModel
import it.polito.madcourse.group06.viewmodels.UserProfileViewModel
import it.polito.madcourse.group06.views.profile.ShowProfileOtherFragment

class ShowSingleTimeslot : Fragment(R.layout.time_slot_details_fragment) {

    private val advViewModel: AdvertisementViewModel by activityViewModels()
    private val userProfileViewModel: UserProfileViewModel by activityViewModels()
    private val myChatViewModel by activityViewModels<MyChatViewModel>()

    private lateinit var advTitle: TextView
    private lateinit var advAccount: TextView
    private lateinit var advLocation: TextView
    private lateinit var advDate: TextView
    private lateinit var advStartingTime: TextView
    private lateinit var advEndingTime: TextView
    private lateinit var advDuration: TextView
    private lateinit var advDescription: TextView
    private lateinit var advRestrictions: TextView
    private lateinit var editButton: ImageView
    private lateinit var skillsChips: ChipGroup
    private lateinit var noSkillsProvidedLabel: TextView
    private lateinit var backgroundAd: ConstraintLayout
    private var currentAccountID: String = ""
    private var otherAccountID: String = ""
    private var isMine = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        this.advTitle = view.findViewById(R.id.advTitle)
        this.advAccount = view.findViewById(R.id.advAccount)
        this.advLocation = view.findViewById(R.id.advLocation)
        this.advDate = view.findViewById(R.id.advDate)
        this.advStartingTime = view.findViewById(R.id.advStartingTime)
        this.advEndingTime = view.findViewById(R.id.advEndingTime)
        this.advDuration = view.findViewById(R.id.advDuration)
        this.advDescription = view.findViewById(R.id.advDescription)
        this.advRestrictions = view.findViewById(R.id.showRestrictions)
        this.editButton = view.findViewById(R.id.moreButtonID)
        this.skillsChips = view.findViewById(R.id.showTimeslotSkills)
        this.noSkillsProvidedLabel = view.findViewById(R.id.noSkillsProvidedLabel)
        this.backgroundAd = view.findViewById(R.id.singleAdBackground)

        this.backgroundAd.setOnClickListener {
            AnimationUtils.loadAnimation(requireContext(), R.anim.slide_out_down).apply {
                setAnimationListener(
                    object : Animation.AnimationListener {
                        override fun onAnimationStart(arg0: Animation) {
                            backgroundAd.background=resources.getDrawable(R.drawable.transparent_background)
                        }
                        override fun onAnimationRepeat(arg0: Animation) {}
                        override fun onAnimationEnd(arg0: Animation) {
                            activity?.supportFragmentManager!!.findFragmentByTag("single_timeslot")
                                ?.also {
                                    activity?.supportFragmentManager?.beginTransaction()
                                        ?.remove(it)?.commit()
                                }
                        }
                    })
                view.startAnimation(this)
            }
        }

        advViewModel.advertisement.observe(viewLifecycleOwner) { singleAdvertisement ->
            userProfileViewModel.currentUser.observe(viewLifecycleOwner) { user ->
                currentAccountID = user.id!!
                otherAccountID = singleAdvertisement.accountID
                if (singleAdvertisement.accountID == currentAccountID) {
                    isMine = true
                    this.editButton.setImageResource(R.drawable.ic_edit_black_24dp)
                } else {
                    userProfileViewModel.fetchUserProfileById(singleAdvertisement.accountID)
                    userProfileViewModel.otherUser.observe(viewLifecycleOwner) {
                        myChatViewModel.setChattingUserProfile(it)
                    }
                }

                this.advAccount.setOnClickListener {
                    // show user profile of the owner of the advertisement
                    if (!isMine) {
                        userProfileViewModel.fetchUserProfileById(singleAdvertisement.accountID)
                        activity?.supportFragmentManager?.beginTransaction()?.add(R.id.nav_host_fragment_content_main, ShowProfileOtherFragment(), "other_user_profile")?.commit()
                    }
                }
            }

            this.advTitle.text = singleAdvertisement.advTitle
            this.advAccount.text = singleAdvertisement.advAccount
            this.advLocation.text = singleAdvertisement.advLocation
            this.advDate.text = singleAdvertisement.advDate
            this.advStartingTime.text = "Starting time: ${singleAdvertisement.advStartingTime}"
            this.advEndingTime.text = "Ending time: ${singleAdvertisement.advEndingTime}"
            this.advDuration.text = timeDoubleHourToString(singleAdvertisement.advDuration)
            if (singleAdvertisement.listOfSkills.size == 0) {
                this.noSkillsProvidedLabel.isVisible = true
                this.skillsChips.isVisible = false
            } else {
                this.noSkillsProvidedLabel.isVisible = false
                this.skillsChips.isVisible = true

                this.skillsChips.removeAllViews()
                for (skill in singleAdvertisement.listOfSkills) {
                    this.skillsChips.addChip(requireContext(), skill)
                }
            }
            if (singleAdvertisement.advDescription.isEmpty()) {
                this.advDescription.text = "No description provided"
            } else {
                this.advDescription.text = singleAdvertisement.advDescription
            }

            if (singleAdvertisement.advRestrictions.isEmpty()) {
                this.advRestrictions.text = "No restrictions"
            } else {
                this.advRestrictions.text = singleAdvertisement.advRestrictions
            }

            this.editButton.setOnClickListener {
                if (isMine) {
                    if (singleAdvertisement.rxUserId.isNullOrEmpty() && singleAdvertisement.ratingUserId.isNullOrEmpty()) {
                        // timeslot can be modified as it is not active
                        activity?.supportFragmentManager!!.findFragmentByTag("single_timeslot")?.also { frag ->
                            activity?.supportFragmentManager?.beginTransaction()?.remove(frag)
                                ?.commit()
                            Navigation.findNavController(view)
                                .navigate(R.id.action_ShowListTimeslots_to_editTimeSlotDetailsFragment)
                        }
                    } else {
                        // timeslot cannot be modified as it is active
                        Snackbar.make(
                            requireView(), "Error: you cannot modify this timeslot while it is active.", Snackbar.LENGTH_LONG
                        ).show()
                    }
                } else {
                    /**
                     * Call to a chat with this user
                     */
                    myChatViewModel.fetchChatByAdvertisementID(currentAccountID, otherAccountID, singleAdvertisement.id!!)
                    activity?.supportFragmentManager!!.findFragmentByTag("single_timeslot")?.also { frag ->
                        activity?.supportFragmentManager?.beginTransaction()?.remove(frag)
                            ?.commit()
                        Navigation.findNavController(view)
                            .navigate(R.id.action_ShowListTimeslots_to_myChat)
                    }
                }
            }
        }

        this.advAccount.setPaintFlags(this.advAccount.getPaintFlags() or Paint.UNDERLINE_TEXT_FLAG)

        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                AnimationUtils.loadAnimation(requireContext(), R.anim.slide_out_down).apply {
                    setAnimationListener(
                        object : Animation.AnimationListener {
                            override fun onAnimationStart(arg0: Animation) {
                                backgroundAd.background=resources.getDrawable(R.drawable.transparent_background)
                            }
                            override fun onAnimationRepeat(arg0: Animation) {}
                            override fun onAnimationEnd(arg0: Animation) {
                                activity?.supportFragmentManager!!.findFragmentByTag("single_timeslot")
                                    ?.also {
                                        activity?.supportFragmentManager?.beginTransaction()
                                            ?.remove(it)?.commit()
                                    }
                            }
                        })
                    view.startAnimation(this)
                }
            }
        })

    }

    override fun onCreateAnimation(transit: Int, enter: Boolean, nextAnim: Int): Animation? {
        val anim=AnimationUtils.loadAnimation(requireActivity(),R.anim.slide_in_up)
        anim.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {
                // additional functionality
            }

            override fun onAnimationRepeat(animation: Animation) {
                // additional functionality
            }

            override fun onAnimationEnd(animation: Animation) {
                // additional functionality
                backgroundAd.background=resources.getDrawable(R.drawable.semi_transparent_background)
            }
        })
        return anim
    }

    /**
     * Dinamically create a chip within a chip group
     *
     * @param context       parent view context
     * @param label         chip name
     */
    private fun ChipGroup.addChip(context: Context, label: String) {
        Chip(context).apply {
            id = View.generateViewId()
            text = label
            isClickable = true
            isCheckable = false
            isCheckedIconVisible = false
            isFocusable = true
            setTextColor(ContextCompat.getColor(context, R.color.white))
            chipBackgroundColor = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.prussian_blue))
            addView(this)
        }
    }

}