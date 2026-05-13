package com.example.testdemo.view.activity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.testdemo.R
import com.example.testdemo.view.bottomsheet.AvatarBottomSheet
import com.example.testdemo.view.bottomsheet.BirthdayBottomSheet
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * 编辑个人资料页面
 */
class EditProfileActivity : AppCompatActivity() {

    companion object {
        fun newIntent(activity: Activity): Intent {
            return Intent(activity, EditProfileActivity::class.java)
        }
    }

    private lateinit var ivAvatar: ImageView
    private lateinit var avatarContainer: CardView
    private lateinit var btnMale: ConstraintLayout
    private lateinit var btnFemale: ConstraintLayout
    private lateinit var etNickname: EditText
    private lateinit var tvBirthday: TextView
    private lateinit var btnBirthday: ConstraintLayout
    private lateinit var btnComplete: TextView
    private lateinit var btnBack: ImageView

    private var selectedGender: Gender = Gender.MALE
    private var selectedYear: Int = 2026
    private var selectedMonth: Int = 1
    private var selectedDay: Int = 1
    private var currentPhotoUri: Uri? = null

    // 性别枚举
    private enum class Gender {
        MALE, FEMALE
    }

    // 相册选择器
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                updateAvatar(uri)
            }
        }
    }

    // 相机拍照
    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            currentPhotoUri?.let { uri ->
                updateAvatar(uri)
            }
        }
    }

    // 相机权限
    private val requestCameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openCamera()
        } else {
            Toast.makeText(this, "需要相机权限才能拍照", Toast.LENGTH_SHORT).show()
        }
    }

    // 存储权限
    private val  requestStoragePermissionLauncher= registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openAlbum()
        } else {
            Toast.makeText(this, "需要存储权限才能选择图片", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_profile)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
        setupListeners()
        updateGenderUI()
    }

    private fun initViews() {
        ivAvatar = findViewById(R.id.iv_avatar)
        avatarContainer = findViewById(R.id.avatar_container)
        btnMale = findViewById(R.id.btn_male)
        btnFemale = findViewById(R.id.btn_female)
        etNickname = findViewById(R.id.et_nickname)
        tvBirthday = findViewById(R.id.tv_birthday)
        btnBirthday = findViewById(R.id.btn_birthday)
        btnComplete = findViewById(R.id.btn_complete)
        btnBack = findViewById(R.id.btn_back)

        // 初始化生日为当前日期
        val calendar = Calendar.getInstance()
        selectedYear = calendar.get(Calendar.YEAR)
        selectedMonth = calendar.get(Calendar.MONTH) + 1
        selectedDay = calendar.get(Calendar.DAY_OF_MONTH)
        updateBirthdayText()
    }

    private fun setupListeners() {
        btnBack.setOnClickListener {
            finish()
        }
        avatarContainer.setOnClickListener {
            showAvatarBottomSheet()
        }
        findViewById<TextView>(R.id.tv_change_avatar).setOnClickListener {
            showAvatarBottomSheet()
        }
        btnMale.setOnClickListener {
            selectedGender = Gender.MALE
            updateGenderUI()
        }
        btnFemale.setOnClickListener {
            selectedGender = Gender.FEMALE
            updateGenderUI()
        }
        btnBirthday.setOnClickListener {
            showBirthdayBottomSheet()
        }
        btnComplete.setOnClickListener {
            saveProfile()
        }
    }

    private fun updateGenderUI() {
        when (selectedGender) {
            Gender.MALE -> {
                btnMale.background = ContextCompat.getDrawable(this, R.drawable.bg_gender_selected)
                btnMale.findViewById<TextView>(android.R.id.content)?.let {
                    (btnMale.getChildAt(0) as? TextView)?.setTextColor(
                        ContextCompat.getColor(this, R.color.color_2B66FF)
                    )
                }
                btnFemale.background = ContextCompat.getDrawable(this, R.drawable.bg_gender_unselected)
                (btnFemale.getChildAt(0) as? TextView)?.setTextColor(
                    ContextCompat.getColor(this, R.color.color_8E8E93)
                )
            }
            Gender.FEMALE -> {
                btnMale.background = ContextCompat.getDrawable(this, R.drawable.bg_gender_unselected)
                (btnMale.getChildAt(0) as? TextView)?.setTextColor(
                    ContextCompat.getColor(this, R.color.color_8E8E93)
                )
                btnFemale.background = ContextCompat.getDrawable(this, R.drawable.bg_gender_selected)
                (btnFemale.getChildAt(0) as? TextView)?.setTextColor(
                    ContextCompat.getColor(this, R.color.color_2B66FF)
                )
            }
        }
    }

    /**
     * 更新生日文本显示
     */
    private fun updateBirthdayText() {
        tvBirthday.text = String.format("%04d-%02d-%02d", selectedYear, selectedMonth, selectedDay)
    }

    private fun showAvatarBottomSheet() {
        val bottomSheet = AvatarBottomSheet()
        bottomSheet.setOnTakePhotoListener {
            checkCameraPermissionAndOpen()
        }
        bottomSheet.setOnSelectFromAlbumListener {
            checkStoragePermissionAndOpen()
        }
        bottomSheet.show(supportFragmentManager, "AvatarBottomSheet")
    }

    private fun showBirthdayBottomSheet() {
        val bottomSheet = BirthdayBottomSheet.newInstance(selectedYear, selectedMonth, selectedDay)
        bottomSheet.setOnConfirmListener { year, month, day ->
            selectedYear = year
            selectedMonth = month
            selectedDay = day
            updateBirthdayText()
        }
        bottomSheet.show(supportFragmentManager, "BirthdayBottomSheet")
    }

    private fun checkCameraPermissionAndOpen() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED -> {
                openCamera()
            }
            else -> {
                requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun checkStoragePermissionAndOpen() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        when {
            ContextCompat.checkSelfPermission(this, permission) ==
                    PackageManager.PERMISSION_GRANTED -> {
                openAlbum()
            }
            else -> {
                requestStoragePermissionLauncher.launch(permission)
            }
        }
    }

    private fun openCamera() {
        val photoFile = createImageFile()
        photoFile?.let { file ->
            currentPhotoUri = FileProvider.getUriForFile(
                this,
                "${packageName}.fileprovider",
                file
            )
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                putExtra(MediaStore.EXTRA_OUTPUT, currentPhotoUri)
            }
            takePictureLauncher.launch(intent)
        }
    }

    private fun openAlbum() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImageLauncher.launch(intent)
    }

    /**
     * 创建临时图片文件
     * 将文件保存在外部文件目录中
     */
    private fun createImageFile(): File? {
        return try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun updateAvatar(uri: Uri) {
        ivAvatar.setImageURI(uri)
    }

    private fun saveProfile() {
        val nickname = etNickname.text.toString().trim()
        if (nickname.isEmpty()) {
            Toast.makeText(this, "请输入昵称", Toast.LENGTH_SHORT).show()
            return
        }

        Toast.makeText(this, "保存成功！", Toast.LENGTH_LONG).show()
        finish()
    }
}
