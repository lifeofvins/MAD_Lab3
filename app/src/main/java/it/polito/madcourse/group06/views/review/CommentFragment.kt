package it.polito.madcourse.group06.views.review

import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import it.polito.madcourse.group06.R
import it.polito.madcourse.group06.activities.TBMainActivity
import it.polito.madcourse.group06.viewmodels.SharedViewModel
import it.polito.madcourse.group06.viewmodels.UserProfileViewModel
import it.polito.madcourse.group06.views.profile.comments.CommentsAdapterCardLong

class CommentFragment : Fragment() {
    private val userProfileViewModel: UserProfileViewModel by activityViewModels()
    private val sharedViewModel: SharedViewModel by activityViewModels()

    private lateinit var sentenceCommentsDone: TextView
    private lateinit var sentenceCommentsRx: TextView

    private lateinit var recyclerViewCommentsDone: RecyclerView
    private lateinit var recyclerViewCommentsRx: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (requireActivity() as TBMainActivity).supportActionBar?.
        setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24)
        return inflater.inflate(R.layout.comment_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerViewCommentsDone = view.findViewById(R.id.rvCommentsDone)
        recyclerViewCommentsRx = view.findViewById(R.id.rvCommentsRx)

        sentenceCommentsDone = view.findViewById(R.id.sentence_comments_done)
        sentenceCommentsRx = view.findViewById(R.id.sentence_comments_rx)

        userProfileViewModel.otherUser.observe(this.viewLifecycleOwner) { userProfile ->
            // comments-done (recyclerView)
            recyclerViewCommentsDone.layoutManager = LinearLayoutManager(view.getContext())
            recyclerViewCommentsDone.adapter = userProfile.comments_services_done?.let { CommentsAdapterCardLong(it) }
            if (userProfile.comments_services_done.isNullOrEmpty()) {
                sentenceCommentsDone.visibility = View.GONE
            } else {
                sentenceCommentsDone.visibility = View.VISIBLE
            }

            // comments-rx (recyclerView)
            recyclerViewCommentsRx.layoutManager = LinearLayoutManager(view.getContext())
            recyclerViewCommentsRx.adapter = userProfile.comments_services_rx?.let { CommentsAdapterCardLong(it) }
            if (userProfile.comments_services_rx.isNullOrEmpty()) {
                sentenceCommentsRx.visibility = View.GONE
            } else {
                sentenceCommentsRx.visibility = View.VISIBLE
            }
        }

        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                activity?.supportFragmentManager!!.findFragmentByTag("comment_fragment")?.also { frag ->
                    sharedViewModel.updateSearchState()
                    activity?.supportFragmentManager?.beginTransaction()?.remove(frag)?.commit()
                    (requireActivity() as TBMainActivity).supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp)
                }
            }
        })
    }
}