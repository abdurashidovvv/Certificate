package com.abdurashidov.certificate

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.abdurashidov.certificate.databinding.ActivityMainBinding
import com.itextpdf.text.Document
import com.itextpdf.text.Image
import com.itextpdf.text.PageSize
import com.itextpdf.text.Paragraph
import com.itextpdf.text.pdf.PdfWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.lang.Byte
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    lateinit var imageUri:String
    var fileName=""
    var fileLocation=""
    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.getImg.setOnClickListener {
            selectImg()
        }

        binding.save.setOnClickListener {
            createImagePdf()
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun createImagePdf() {
        fileName=System.currentTimeMillis().toString()
        creatingPdf(fileName, imageUri)
    }

    private fun creatingPdf(fileName: String, imageUri: String) {
        GlobalScope.launch(Dispatchers.IO) {
            convertImageToPdf(fileName, imageUri)
        }
//        saveFileLocationInDb()
    }

    private fun convertImageToPdf(fileName: String, imageUri: String):String {
        var fileName=fileName
        val fileUri=imageUri
        val path:String=Environment.getExternalStorageDirectory().absolutePath+"/PDFfiles"
        val folder=File(path)
        if (!folder.exists()){
            val success=folder.mkdir()
            if (!success){
                Toast.makeText(this, "Can't create file", Toast.LENGTH_SHORT).show()
            }
        }
        fileName=fileName+".pdf"
        val storePath=File(folder, fileName)

        val document=Document(PageSize.A4, 35f, 35f, 50f, 35f)
        val documentRect=document.pageSize
        val writer=PdfWriter.getInstance(document, FileOutputStream(storePath))
        document.open()
        try {
            val bmp=MediaStore.Images.Media.getBitmap(baseContext.contentResolver, Uri.fromFile(File(imageUri)))
            bmp.compress(Bitmap.CompressFormat.PNG, 100, ByteArrayOutputStream())
            val image=Image.getInstance(imageUri)
            if (bmp.width > documentRect.width || bmp.height > documentRect.height) {
                image.scaleAbsolute(documentRect.width, documentRect.height)
            } else {
                image.scaleAbsolute(bmp.width.toFloat(), bmp.height.toFloat())
            }
            image.setAbsolutePosition((documentRect.width - image.scaledWidth) / 2, (documentRect.height - image.scaledHeight) / 2)
            image.border = Image.BOX
            image.borderWidth = 15f
            document.add(image)
            document.close()

        }catch (e:Exception){
            Log.d(TAG, "convertImageToPdf: ${e.message}")
        }
        fileLocation=storePath.absolutePath

        return storePath.absolutePath
    }

    private fun selectImg() {
        getImageContent.launch("image/")
    }

    val getImageContent=registerForActivityResult(ActivityResultContracts.GetContent()){uri->
        binding.getImg.setImageURI(uri)
        imageUri=uri.toString()
    }
}
