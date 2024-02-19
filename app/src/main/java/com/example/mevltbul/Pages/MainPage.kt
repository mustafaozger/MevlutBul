package com.example.mevltbul.Pages

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.navigation.Navigation
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.mevltbul.Adapter.MainPageExploreRcylerAdapter
import com.example.mevltbul.Classes.Marker
import com.example.mevltbul.Constants.Constants
import com.example.mevltbul.R
import com.example.mevltbul.ViewModel.DetailVM
import com.example.mevltbul.databinding.FragmentMainPageBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale

@AndroidEntryPoint
class MainPage: Fragment() ,OnMapReadyCallback{
        lateinit var binding: FragmentMainPageBinding
        lateinit var detailVM:DetailVM
        lateinit var locationManager: LocationManager
        lateinit var locationListener: LocationListener
        var mMap:GoogleMap?=null
        private var markerList=MutableLiveData<ArrayList<Marker>>()

    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bottomNavigationView=requireActivity().findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.visibility=View.VISIBLE
        val fabButton=requireActivity().findViewById<FloatingActionButton>(R.id.floatingActionButton)
        fabButton.visibility=View.VISIBLE

        val lastLocation:Location?=locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        if(lastLocation!=null){
            markerList=detailVM.getEventLists(lastLocation.latitude,lastLocation.longitude)

        }

    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val temp:DetailVM by viewModels()
        detailVM=temp
        locationManager=requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager



    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding=FragmentMainPageBinding.inflate(layoutInflater)
        val mapFragment=childFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)


        binding.chip.setOnClickListener {
            Navigation.findNavController(it).navigate(R.id.action_mainPage_to_mapPage)
        }



        return binding.root
    }

    override fun onStart() {
        super.onStart()
        markerList.observe(viewLifecycleOwner){list->
            if(list.size>0){
                binding.imgErrorMessage.visibility=View.GONE
                val adapter= MainPageExploreRcylerAdapter(requireContext(),list)
                binding.mainPageExploreRycler.adapter=adapter
                binding.mainPageExploreRycler.layoutManager=
                    StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL)
                Log.d("hatmaMarkerList","list is not empty")
                binding.progressBarEventList.visibility=View.GONE

            }else{
                binding.imgErrorMessage.visibility=View.VISIBLE
            }

        }


    }


    @SuppressLint("SuspiciousIndentation")
    override fun onMapReady(p0: GoogleMap) {
        mMap=p0



        locationListener=LocationListener{location ->
            val currentLocation=LatLng(location.latitude,location.longitude)
            if(mMap!=null){
                mMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation,8f))
            }
        }
        if (!Constants.checkPermission(requireContext())) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),1)
        }else{
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,10,10f,locationListener)
            showMarker()

        }

    }

    private fun showMarker(){
        val geocoder= Geocoder(requireContext(), Locale.getDefault())
        try {
             markerList.observe(viewLifecycleOwner){list->
                 for ( marker in list){
                      val addressList= marker.marker_latitude?.let { marker.marker_longtitude?.let { it1 ->
                        geocoder.getFromLocation(it.toDouble(),
                            it1.toDouble(),1)
                    } }
                    val latLng= marker.marker_latitude?.let { marker.marker_longtitude?.let { it1 ->
                        LatLng(it.toDouble(),
                            it1.toDouble())
                    } }
                    val markerOptions = latLng?.let {
                        MarkerOptions()
                            .position(it)
                            .icon(Constants.getMarker(requireContext()))
                    }
                     markerOptions?.let { mMap?.addMarker(it) }
                 }
                 binding.progressBarMap.visibility=View.GONE

             }


        }catch (e:Exception){
            e.printStackTrace()
        }
    }






}