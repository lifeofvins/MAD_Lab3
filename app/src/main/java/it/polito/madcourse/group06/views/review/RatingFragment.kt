package it.polito.madcourse.group06.views.review

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RatingBar
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.snackbar.Snackbar
import it.polito.madcourse.group06.R
import it.polito.madcourse.group06.activities.TBMainActivity
import it.polito.madcourse.group06.viewmodels.AdvertisementViewModel
import it.polito.madcourse.group06.viewmodels.UserProfileViewModel

class RatingFragment : Fragment() {
    private lateinit var activityTB: TBMainActivity

    private val userProfileViewModel by activityViewModels<UserProfileViewModel>()
    private val advertisementViewModel by activityViewModels<AdvertisementViewModel>()

    private var rxUserId: String? = ""
    private var advId: String? = ""
    private var advAccountId: String? = ""
    private var advTitle: String? = ""
    private var isServiceDone: Boolean = false


    override fun onAttach(context: Context) {
        super.onAttach(context)

        rxUserId = arguments?.getString("rx_user_id")
        advAccountId = arguments?.getString("adv_account_id")

        advId = arguments?.getString("adv_id")
        advTitle = arguments?.getString("adv_title")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        activityTB = requireActivity() as TBMainActivity
        return inflater.inflate(R.layout.rating_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<ConstraintLayout>(R.id.background_rating).setOnClickListener {
            // go back to timeslots list
            activity?.supportFragmentManager!!.findFragmentByTag("rating_fragment")?.also { frag ->
                activity?.supportFragmentManager?.beginTransaction()?.remove(frag)?.commit()
            }
        }

        val ratingBar = view.findViewById<RatingBar>(R.id.ratingBar)
        val submitRatingButton = view.findViewById<Button>(R.id.submitRating)


        ratingBar.setRating(0F)
        ratingBar.setOnRatingBarChangeListener { ratingBar, rating, fromUser ->
            if (rating <= 0) {
                // user is forbidden to vote 0 stars
                ratingBar.setRating(0.5F)
            }
        }

        activity?.onBackPressedDispatcher?.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (ratingBar.rating == 0F) {
                        // go back to timeslots list
                        activity?.supportFragmentManager!!.findFragmentByTag("rating_fragment")?.also { frag ->
                            activity?.supportFragmentManager?.beginTransaction()?.remove(frag)?.commit()
                        }
                    } else {
                        submitRatingButton.performClick()
                    }
                }
            })

        advAccountId?.let {
            rxUserId?.let {
                advId?.let {
                    advertisementViewModel.fetchSingleAdvertisementById(advId!!)

                    userProfileViewModel.currentUser.observe(this.viewLifecycleOwner) { currentUser ->
                        isServiceDone = (currentUser.id == advAccountId)
                        if (isServiceDone) {
                            userProfileViewModel.fetchUserProfileById(rxUserId)
                        } else {
                            userProfileViewModel.fetchUserProfileById(advAccountId)
                        }
                        userProfileViewModel.otherUser.observe(this.viewLifecycleOwner) { otherUser ->
                            if (!otherUser.id.isNullOrEmpty()) {
                                submitRatingButton?.setOnClickListener {
                                    if (ratingBar.rating == 0F) {
                                        Snackbar.make(
                                            requireView(),
                                            "Please vote your experience.",
                                            Snackbar.LENGTH_LONG
                                        ).show()
                                    } else {
                                        // save rating and comments
                                        val rating = ratingBar.rating.toDouble()
                                        val comment =
                                            view.findViewById<TextView>(R.id.comment_rating).text.toString()

                                        userProfileViewModel.commentAd(
                                            advTitle,
                                            comment,
                                            rating,
                                            isServiceDone
                                        )

                                        advertisementViewModel.deactivateAd(isServiceDone)

                                        // go back to timeslots list
                                        activity?.supportFragmentManager!!.findFragmentByTag("rating_fragment")?.also { frag ->
                                            activity?.supportFragmentManager?.beginTransaction()?.remove(frag)?.commit()
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}