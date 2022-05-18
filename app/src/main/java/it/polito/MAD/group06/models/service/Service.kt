package it.polito.MAD.group06.models.service
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * [Service] data class.
 * This data class contains the following information:
 * @param serviceName The name of the Service
 */


@Entity(tableName = "serviceTable")
data class Service(
    @PrimaryKey
    var serviceName: String
)
