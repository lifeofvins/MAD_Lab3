package it.polito.madcourse.group06.views.profile

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.snackbar.Snackbar
import it.polito.madcourse.group06.R
import it.polito.madcourse.group06.viewmodels.UserProfileViewModel


class ShowProfileOtherFragment : Fragment() {
    private lateinit var fullnameOBJ: TextView
    private lateinit var nicknameOBJ: TextView
    private lateinit var qualificationOBJ: TextView
    private lateinit var descriptionOBJ: TextView
    private lateinit var emailOBJ: TextView
    private lateinit var locationOBJ: TextView
    private lateinit var skillsOBJ: TextView
    private lateinit var phoneOBJ: TextView
    private lateinit var profilePictureOBJ: ImageView
    private lateinit var rateOBJ: TextView
    private lateinit var skillsChips: ChipGroup

    private val userProfileViewModel: UserProfileViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.show_profile_other, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        this.fullnameOBJ = view.findViewById(R.id.fullname_ID_other)
        this.nicknameOBJ = view.findViewById(R.id.nickname_ID_other)
        this.qualificationOBJ = view.findViewById(R.id.qualification_ID_other)
        this.descriptionOBJ = view.findViewById(R.id.description_show_ID_other)
        this.emailOBJ = view.findViewById(R.id.email_show_ID_other)
        this.locationOBJ = view.findViewById(R.id.loc_show_ID_other)
        this.skillsOBJ = view.findViewById(R.id.skillsListID_other)
        this.phoneOBJ = view.findViewById(R.id.phone_show_ID_other)
        this.profilePictureOBJ = view.findViewById(R.id.profilePictureID_other)
        this.rateOBJ = view.findViewById(R.id.rate_other)
        this.skillsChips = view.findViewById(R.id.skill_chips_group_other)

        userProfileViewModel.currentUser.observe(this.viewLifecycleOwner) { userProfile ->
            Log.e("profile", userProfile.toString())
            // Fullname
            this.fullnameOBJ.text = userProfile.fullName

            // Nickname
            if (userProfile.nickname?.compareTo("") == 0) {
                this.nicknameOBJ.text = "No nickname provided."
            } else {
                this.nicknameOBJ.text = "@${userProfile.nickname}"
            }

            // Qualification
            this.qualificationOBJ.text = userProfile.qualification
            // Phone Number
            this.phoneOBJ.text = userProfile.phoneNumber
            // Location
            this.locationOBJ.text = userProfile.location

            // Skills
            if (userProfile.skills.isNullOrEmpty()) {
                this.skillsChips.isVisible = false
                this.skillsOBJ.isVisible = true
                this.skillsOBJ.text = getString(R.string.noskills)
            } else {
                this.skillsOBJ.isVisible = false
                this.skillsChips.isVisible = true
                this.skillsChips.removeAllViews()
                userProfile.skills!!.forEach { sk ->
                    this.skillsChips.addChip(requireContext(), sk)
                    this.skillsChips.setOnCheckedChangeListener { chipGroup, checkedId ->
                        val selectedService = chipGroup.findViewById<Chip>(checkedId)?.text
                        Toast.makeText(chipGroup.context, selectedService ?: "No Choice", Toast.LENGTH_LONG).show()
                    }
                }
            }

            // Email
            this.emailOBJ.text = userProfile.email
            // Description
            this.descriptionOBJ.text = userProfile.description

            // Profile Picture
            if (userProfile.imgPath.isNullOrEmpty()) {
                userProfileViewModel.retrieveStaticProfilePicture(profilePictureOBJ)
            } else {
                userProfileViewModel.retrieveProfilePicture(profilePictureOBJ, userProfile.imgPath!!)
            }
        }

        // Check this option to open onCreateOptionsMenu method
        setHasOptionsMenu(true)


        this.rateOBJ.setOnClickListener {
            Navigation.findNavController(view).navigate(R.id.action_showProfileOtherFragment_to_ratingFragment)
        }

        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigate(R.id.action_showProfileOtherFragment_to_showSingleTimeslot)
            }
        })

        // check this option to open onCreateOptionsMenu method
        setHasOptionsMenu(true)
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
            addView(this)
        }
    }
}