import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.varsitycollege.birdvue.databinding.ActivityAddSightingMapBinding

class AddSightingMapActivity : AppCompatActivity() {
    private val galleryPermission = 101
    private lateinit var binding: ActivityAddSightingMapBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddSightingMapBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.openGalleryButton.setOnClickListener {
            if (isGalleryPermissionSuccess()) {
                openGallery()
            } else {
                requestGalleryPermission()
            }
        }
    }

    private fun isGalleryPermissionSuccess(): Boolean {
        return (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
    }

    private fun requestGalleryPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), galleryPermission)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == galleryPermission) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery()
            } else {
                Toast.makeText(this, "Gallery permission is required to open this galery", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun openGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK)
        galleryIntent.type = "image/*"
        startActivityForResult(galleryIntent, galleryPermission)
    }
}
