package com.ferran.taskinator

import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.graphics.BitmapFactory
import android.util.Base64
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream


object ImageCompressor {

    private const val MAX_IMAGE_SIZE: Int = 70 * 1024
    private const val IMAGE_RESIZE: Int = 400

    fun compressAndEncodeAsBase64(bitmap: Bitmap): String {
        return Base64.encodeToString(getBitmapAsByteArray(compress(bitmap)), Base64.DEFAULT)
    }

    fun compress(bitmap: Bitmap): Bitmap {

        val byteArray = getBitmapAsByteArray(bitmap)
        val outputStream = compress(byteArray)

        return BitmapFactory.decodeStream(ByteArrayInputStream(outputStream.toByteArray()))
    }

    private fun compress(targetArray: ByteArray): ByteArrayOutputStream {
        // First decode with inJustDecodeBounds=true to check dimensions of image
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeByteArray(targetArray, 0, targetArray.size, options)

        // Calculate inSampleSize(First we are going to resize the image to 200x200 image, in order to not have a big but very low quality image.
        //resizing the image will already reduce the file size, but after resizing we will check the file size and start to compress image
        options.inSampleSize = calculateInSampleSize(options, IMAGE_RESIZE, IMAGE_RESIZE)

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false
        options.inPreferredConfig = Bitmap.Config.ARGB_8888

        val bmpPic = BitmapFactory.decodeByteArray(targetArray, 0, targetArray.size, options)

        var compressQuality = 100 // quality decreasing by 5 every loop.
        var streamLength: Int
        do {
            val bmpStream = ByteArrayOutputStream()
            bmpPic.compress(CompressFormat.JPEG, compressQuality, bmpStream)
            val bmpPicByteArray = bmpStream.toByteArray()
            streamLength = bmpPicByteArray.size
            compressQuality -= 5
        } while (streamLength >= MAX_IMAGE_SIZE)

        //save the resized and compressed file to disk cache
        val bmpFile = ByteArrayOutputStream()
        bmpPic.compress(CompressFormat.JPEG, compressQuality, bmpFile)
        bmpFile.flush()
        bmpFile.close()
        return bmpFile
    }

    private fun getBitmapAsByteArray(compress: Bitmap): ByteArray {
        val byteArrayOutputStream = ByteArrayOutputStream()
        compress.compress(CompressFormat.JPEG, 100, byteArrayOutputStream)
        return byteArrayOutputStream.toByteArray()
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {

            val halfHeight = height / 2
            val halfWidth = width / 2

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while (halfHeight / inSampleSize > reqHeight && halfWidth / inSampleSize > reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }
}