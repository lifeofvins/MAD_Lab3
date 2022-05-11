package it.polito.group06.views

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import it.polito.group06.R

class TimeSlotDetailsFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val moreButton = this.view?.findViewById<ImageView>(R.id.moreButtonID)
        moreButton?.setOnClickListener { this.activity?.openContextMenu(moreButton) }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.time_slot_details_fragment, container, false)
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) = TimeSlotDetailsFragment().apply { arguments = Bundle().apply { } }
    }
}