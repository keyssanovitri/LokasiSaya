package com.example.lokasisaya

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.CancellationTokenSource
import java.util.Locale

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private var peta: GoogleMap? = null
    private var lokasi: Location? = null
    private lateinit var tvJudul: TextView
    private lateinit var tvStatus: TextView
    private lateinit var tvKeterangan: TextView

    private val mintaIzin = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { hasil ->
        if (hasil.values.any { it }) {
            ambilLokasi()
        } else {
            tvJudul.text = "Izin lokasi ditolak"
            tvStatus.text = "Aktifkan izin lokasi di pengaturan aplikasi."
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvJudul = findViewById(R.id.tvJudul)
        tvStatus = findViewById(R.id.tvStatus)
        tvKeterangan = findViewById(R.id.tvKeterangan)

        val fragmentPeta = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        fragmentPeta.getMapAsync(this)

        if (izinDiberikan()) {
            ambilLokasi()
        } else {
            mintaIzin.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        peta = googleMap
        // Jika lokasi sudah didapat sebelum peta siap, pasang pin sekarang.
        lokasi?.let { tampilkanPin(it) }
    }

    @SuppressLint("MissingPermission")
    private fun ambilLokasi() {
        val fused = LocationServices.getFusedLocationProviderClient(this)
        fused.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, CancellationTokenSource().token)
            .addOnSuccessListener { hasil: Location? ->
                if (hasil != null) {
                    lokasi = hasil
                    tampilkanPin(hasil)
                } else {
                    tvJudul.text = "Lokasi belum tersedia"
                    tvStatus.text = "Pastikan GPS aktif."
                }
            }
    }

    private fun tampilkanPin(loc: Location) {
        val map = peta ?: return
        val posisi = LatLng(loc.latitude, loc.longitude)
        map.clear()
        map.addMarker(MarkerOptions().position(posisi).title("Lokasi Saya"))
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(posisi, 16f))
        tvJudul.text = "Lokasi Saya"
        tvStatus.text = String.format(Locale.US, "Lat %.6f Lon %.6f", loc.latitude, loc.longitude)
        tvKeterangan.visibility = View.VISIBLE
    }

    private fun izinDiberikan(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
    }
}