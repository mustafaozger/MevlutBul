package com.example.mevltbul.Repository

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.example.mevltbul.Classes.Marker
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import java.util.LinkedList
import java.util.Queue

class DetailPageDaoRepo{

    private  var db:FirebaseFirestore=Firebase.firestore
    private val storage = FirebaseStorage.getInstance()
    private val storageReference = storage.reference
    private val urlQueue:Queue<String> = LinkedList()
    private val markerList=MutableLiveData<ArrayList<Marker>>()

    init {

    }

    fun getEventLists():MutableLiveData<ArrayList<Marker>>{
        return markerList
    }




    fun publishDetail(
        marker_id: String?,
        marker_latitude: String? = null,
        marker_longitude: String? = null,
        marker_detail: String? = null,
        imageList: ArrayList<Uri?>,
        event_type:String?=null,
        event_date:String?=null,
        callback: (Boolean) -> Unit
    ) {
        val totalImages = imageList.size
        var uploadedImages = 0


        uploadImages(imageList, "markers") { imageUrl ->
            uploadedImages++
            if (uploadedImages == totalImages) {
                for ( i in 0..3){
                    if (urlQueue.size<4){
                        urlQueue.add(null)
                    }
                }
                val marker = Marker(
                    marker_id,
                    marker_latitude,
                    marker_longitude,
                    marker_detail,
                    urlQueue.poll(),
                    urlQueue.poll(),
                    urlQueue.poll(),
                    urlQueue.poll(),
                    event_type,
                    event_date
                )

                marker.marker_id?.let {
                    db.collection("images").document(it).set(marker)
                        .addOnSuccessListener {
                            callback(true)

                        }
                        .addOnFailureListener {
                            callback(false)
                        }
                }
            }
        }
    }

    private fun uploadImages(imageUris: ArrayList<Uri?>, folderName: String, callback: (String) ->  Unit) {
        val imageNamePrefix = "image_${System.currentTimeMillis()}"
        var uploadedCount = 0

        for (imageUri in imageUris) {
            val imageName = "$imageNamePrefix${uploadedCount++}"
            val imageRef: StorageReference = storageReference.child("$folderName/$imageName")

            if (imageUri != null) {
                imageRef.putFile(imageUri)
                    .addOnSuccessListener { taskSnapshot ->
                        taskSnapshot.metadata?.reference?.downloadUrl?.addOnSuccessListener { uri ->
                            urlQueue.add(uri.toString())
                            callback(uri.toString())

                        }
                    }
                    .addOnFailureListener { exception ->
                        exception.printStackTrace()
                    }
            }
        }
    }





    fun getEventListsFromDatabaseWitfFlow(latitude: Double, longitude: Double): Flow<ArrayList<Marker>> = flow {
        //I'm getting events in the area close to the user's location
        Log.d("hatamDetailPageDaoRepo", "2. getEventListsFromDatabas work ")
        val collection = db.collection( "images")
            .whereGreaterThanOrEqualTo("marker_latitude", (latitude - 0.5).toString())
            .whereLessThanOrEqualTo("marker_latitude", (latitude + 0.5).toString())

        try {
            val snapshot = collection.get().await()
            val list = ArrayList<Marker>()
            for (document in snapshot.documents) {
                Log.d("hatamDetailPageDaoRepo", "3. getEventListsFromDatabas collection get  ")
                if (document.exists()) {
                    val data = document.data

                    //Firestore does not directly support disparity filters (like >, <) on multiple fields at once. so that's why I had to use the following if check
                    val markerLongitude = data?.get("marker_longtitude") as? String

                    if (markerLongitude != null && markerLongitude.toDouble() in (longitude - 0.5)..(longitude + 0.5)) {
                        val marker = Marker(
                            data.get("marker_id") as String?,
                            data.get("marker_latitude") as String?,
                            data.get("marker_longtitude") as String?,
                            data.get("marker_detail") as String?,
                            data.get("photo1") as String?,
                            data.get("photo2") as String?,
                            data.get("photo3") as String?,
                            data.get("photo4") as String?,
                            data.get("event_type") as String?,
                            data.get("event_date") as String?,
                        )
                        list.add(marker)
                    } else {
                        Log.d("hatamDetailPageDaoRepo", "marker longitude is not in range {${markerLongitude}, ${data?.get("marker_latitude")}}")
                    }
                } else {
                    Log.d("hatamDetailPageDaoRepo", "document empty")
                }
            }
            emit(list)
        } catch (e: Exception) {
            Log.e("hatamDetailPageDaoRepo", "Error fetching data: ${e.message}", e)
            // Emit an empty list or handle the error as required
            emit(ArrayList())
        }

    }




}