package it.polito.MAD.group06.viewmodels

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import it.polito.MAD.group06.models.advertisement.Advertisement
import it.polito.MAD.group06.remote.FirestoreDatabase
import it.polito.MAD.group06.repository.AdvertisementRepository
import java.lang.Exception
import kotlin.concurrent.thread

class AdvertisementViewModel(application: Application) : AndroidViewModel(application) {
    /**
     * Repository
     */
    private val repositoryAdv = AdvertisementRepository(application)
    private val db = FirestoreDatabase.getDatabase(application)
    private val context = application

    /**
     * Single [Advertisement]
     */
    private var _singleAdvertisementPH = Advertisement(
        null, "", "",
        "", "", "", "", 0f,
        "", -1
    )
    private val _pvtAdvertisement = MutableLiveData<Advertisement>().also {
        it.value = _singleAdvertisementPH
    }
    val advertisement: LiveData<Advertisement> = this._pvtAdvertisement

    /**
     * Insertion of a new [Advertisement]
     * @param ad a new advertisement
     */
    fun insertAdvertisement(ad: Advertisement) {
        db
            .collection("Advertisement")
            .document()
            .set(mapOf(ad.id.toString() to ad))
            .addOnSuccessListener {
                Toast.makeText(context, "Creation completed.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Creation failed", Toast.LENGTH_SHORT).show()
            }
    }

    fun removeAdvertisement(ad: Advertisement) {
        db
            .collection("Advertisement")
            .document(ad.id.toString())
            .delete()
            .addOnSuccessListener {
                Toast.makeText(context, "Deletion completed.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Deletion failed.", Toast.LENGTH_SHORT).show()
            }
    }

    fun removeAdvertisementByAccount(accountID: Int) {
        db
            .collection("Advertisement")
            .document()
            .delete()
            .addOnSuccessListener {
                Toast.makeText(context, "Deletion completed.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Deletion failed.", Toast.LENGTH_SHORT).show()
            }
    }

    fun editAdvertisement(ad: Advertisement) {
        db
            .collection("Advertisement")
            .document()
            .set(mapOf(ad.id.toString() to ad))
            .addOnSuccessListener {
                Toast.makeText(context, "Edit completed.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Edit failed.", Toast.LENGTH_SHORT).show()
            }
    }

    fun getAdvertisementByID(id: Int): Advertisement? {
        var outAdv: Advertisement? = null
        db
            .collection("Advertisement")
            .document(id.toString())
            .get()
            .addOnSuccessListener { dbAdv ->
                outAdv = dbAdv.toObject(Advertisement::class.java)
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error in get", Toast.LENGTH_SHORT).show()
            }

        return outAdv
    }

    fun getListOfAdvertisements(): List<Advertisement> {
        val outAdv: MutableList<Advertisement> = mutableListOf()

        db
            .collection("Advertisement")
            .addSnapshotListener { value, e ->
                if (e != null) {
                    throw Exception()
                } else {
                    for(elem in value!!) {
                        outAdv.add(elem.toObject(Advertisement::class.java))
                    }
                }
            }

        return outAdv
    }

    fun getListOfAdvertisementsByAccountID(accountID: Int): List<Advertisement>? {
        val outAdv: MutableList<Advertisement> = mutableListOf()

        db
            .collection("Advertisement")
            .whereEqualTo("accountID", accountID)
            .addSnapshotListener { value, e ->
                if (e != null) {
                    throw Exception()
                } else {
                    for(elem in value!!) {
                        outAdv.add(elem.toObject(Advertisement::class.java))
                    }
                }
            }

        return outAdv
    }

    /**
     * setSingleAdvertisement is a method to update the [_pvtAdvertisement] and it's called by the adapter to
     * trigger all the observators.
     * @param newAdv an object of class [Advertisement] which contains all the information to fill
     * the advertisement view.
     */
    fun setSingleAdvertisement(newAdv: Advertisement) {
        this._singleAdvertisementPH = newAdv
        this._pvtAdvertisement.value = _singleAdvertisementPH
    }

    /**
     * editSingleAdvertisement is a method to update the info about a single [Advertisement] with the
     * new ones read from the edit view.
     * @param updatedAdv the object of class [Advertisement] which contains all the information to update
     * the advertisement with a certain id.
     */
    fun editSingleAdvertisement(updatedAdv: Advertisement) {
        thread {
            repositoryAdv.updateAdv(updatedAdv)
        }
        this._singleAdvertisementPH = updatedAdv
    }

    /**
     * updateAccountName is a method to update the name of the creator of the advertisement
     * after it's been changed from the edit view for the profile.
     * @param advList a complete list of advertisement
     * @param accountName the new account name from the update profile
     */
    fun updateAccountName(advList: List<Advertisement>, accountName: String) {
        thread {
            for (adv in advList) {
                adv.advAccount = accountName
                repositoryAdv.updateAccountName(adv)
            }
        }
    }
}