package it.polito.group06.views

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import it.polito.group06.R
import it.polito.group06.models.time_slot_adv_database.Advertisement
import it.polito.group06.viewmodels.AdvertisementViewModel
import it.polito.group06.viewmodels.UserProfileViewModel

class NewSingleTimeslot : Fragment(R.layout.new_time_slot_details_fragment) {

    private val advViewModel by viewModels<AdvertisementViewModel>()
    private val usrViewModel by viewModels<UserProfileViewModel>()
    private lateinit var newTitle: EditText
    private lateinit var newLocation: EditText
    private lateinit var newDate: EditText
    private lateinit var newDuration: EditText
    private lateinit var newDescription: EditText
    private lateinit var closeButton: ImageView
    private lateinit var confirmButton: ImageView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        this.newTitle = view.findViewById(R.id.newTitle)
        this.newLocation = view.findViewById(R.id.newLocation)
        this.newDate = view.findViewById(R.id.newDate)
        this.newDuration = view.findViewById(R.id.newDuration)
        this.newDescription = view.findViewById(R.id.newDescription)
        this.closeButton = view.findViewById(R.id.closeButton)
        this.confirmButton = view.findViewById(R.id.confirmButton)

        this.closeButton.setOnClickListener {
            findNavController().navigate(R.id.action_newTimeSlotDetailsFragment_to_ShowListTimeslots)
        }

        this.confirmButton.setOnClickListener {
            if (isAdvAvailable()) {
                advViewModel.insertAd(
                    Advertisement(
                        null,
                        newTitle.text.toString(),
                        newDescription.text.toString(),
                        newLocation.text.toString(),
                        newDate.text.toString(),
                        0f,
                        "Guido Saracco",
                        false
                    )
                )
            }
            findNavController().navigate(R.id.action_newTimeSlotDetailsFragment_to_ShowListTimeslots)
        }

        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (true) {
                    advViewModel.insertAd(
                        Advertisement(
                            null,
                            newTitle.text.toString(),
                            newDescription.text.toString(),
                            newLocation.text.toString(),
                            newDate.text.toString(),
                            0f,
                            "Guido Saracco",
                            false
                        )
                    )
                }
                findNavController().navigate(R.id.action_newTimeSlotDetailsFragment_to_ShowListTimeslots)
            }
        })
    }

    /**
     * isAdvAvailable is a method which returns whether it's possible to actually insert a new
     * advertisement. The criteria is that an advertisement should at least have a title, a location,
     * a date and a duration.
     *
     * @return whether it's possible to actually create an advertisement or not
     */
    private fun isAdvAvailable(): Boolean {
        return  !(newTitle.text.toString().isNullOrEmpty() ||
                newDuration.text.toString().isNullOrEmpty() ||
                newLocation.text.toString().isNullOrEmpty() ||
                newDate.text.toString().isNullOrEmpty())
    }
}