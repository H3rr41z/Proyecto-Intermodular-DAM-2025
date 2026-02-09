package com.renaix.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.util.Base64
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * Utilidades para manejo de imágenes
 */
object ImageUtils {

    /**
     * Convierte una imagen URI a Base64
     * Incluye compresión automática
     */
    fun uriToBase64(context: Context, uri: Uri, quality: Int = 80): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            // Rotar la imagen según EXIF si es necesario
            val rotatedBitmap = rotateImageIfRequired(context, bitmap, uri)

            // Comprimir y convertir a Base64
            bitmapToBase64(rotatedBitmap, quality)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Convierte un Bitmap a Base64
     */
    fun bitmapToBase64(bitmap: Bitmap, quality: Int = 80): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
        val byteArray = outputStream.toByteArray()
        val base64String = Base64.encodeToString(byteArray, Base64.NO_WRAP)
        return "data:image/jpeg;base64,$base64String"
    }

    /**
     * Convierte Base64 a Bitmap
     */
    fun base64ToBitmap(base64String: String): Bitmap? {
        return try {
            // Remover el prefijo "data:image/jpeg;base64," si existe
            val cleanBase64 = base64String.substringAfter("base64,")
            val decodedBytes = Base64.decode(cleanBase64, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Redimensiona un Bitmap manteniendo la proporción
     */
    fun resizeBitmap(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        val ratioBitmap = width.toFloat() / height.toFloat()
        val ratioMax = maxWidth.toFloat() / maxHeight.toFloat()

        var finalWidth = maxWidth
        var finalHeight = maxHeight

        if (ratioMax > ratioBitmap) {
            finalWidth = (maxHeight.toFloat() * ratioBitmap).toInt()
        } else {
            finalHeight = (maxWidth.toFloat() / ratioBitmap).toInt()
        }

        return Bitmap.createScaledBitmap(bitmap, finalWidth, finalHeight, true)
    }

    /**
     * Rota una imagen según los datos EXIF
     */
    private fun rotateImageIfRequired(context: Context, img: Bitmap, selectedImage: Uri): Bitmap {
        val input = context.contentResolver.openInputStream(selectedImage)
        val ei = input?.let { ExifInterface(it) }
        input?.close()

        val orientation = ei?.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )

        return when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(img, 90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(img, 180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(img, 270f)
            else -> img
        }
    }

    /**
     * Rota una imagen en grados
     */
    private fun rotateImage(img: Bitmap, degree: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degree)
        return Bitmap.createBitmap(img, 0, 0, img.width, img.height, matrix, true)
    }

    /**
     * Comprime una imagen para reducir su tamaño
     */
    fun compressImage(
        bitmap: Bitmap,
        maxFileSizeKB: Int = 1024,
        quality: Int = 80
    ): Bitmap {
        var compressedBitmap = bitmap
        var currentQuality = quality

        var outputStream = ByteArrayOutputStream()
        compressedBitmap.compress(Bitmap.CompressFormat.JPEG, currentQuality, outputStream)

        // Reducir calidad hasta que el tamaño sea aceptable
        while (outputStream.toByteArray().size / 1024 > maxFileSizeKB && currentQuality > 10) {
            outputStream = ByteArrayOutputStream()
            currentQuality -= 10
            compressedBitmap.compress(Bitmap.CompressFormat.JPEG, currentQuality, outputStream)
        }

        return compressedBitmap
    }

    /**
     * Guarda un Bitmap en el almacenamiento local
     */
    fun saveBitmapToFile(context: Context, bitmap: Bitmap, fileName: String): File? {
        return try {
            val file = File(context.cacheDir, fileName)
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            outputStream.flush()
            outputStream.close()
            file
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Calcula el tamaño de una imagen en KB
     */
    fun getImageSizeKB(bitmap: Bitmap): Int {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        return outputStream.toByteArray().size / 1024
    }

    /**
     * Valida que una imagen no supere el tamaño máximo
     */
    fun isImageSizeValid(bitmap: Bitmap, maxSizeMB: Int = 5): Boolean {
        val sizeKB = getImageSizeKB(bitmap)
        return sizeKB <= maxSizeMB * 1024
    }

    /**
     * Optimiza una imagen para subir (redimensiona y comprime)
     */
    fun optimizeImageForUpload(
        context: Context,
        uri: Uri,
        maxWidth: Int = 1920,
        maxHeight: Int = 1080,
        quality: Int = 80
    ): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            var bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            // Rotar si es necesario
            bitmap = rotateImageIfRequired(context, bitmap, uri)

            // Redimensionar
            if (bitmap.width > maxWidth || bitmap.height > maxHeight) {
                bitmap = resizeBitmap(bitmap, maxWidth, maxHeight)
            }

            // Comprimir si el tamaño es muy grande
            if (getImageSizeKB(bitmap) > Constants.MAX_IMAGE_SIZE_MB * 1024) {
                bitmap = compressImage(bitmap, Constants.MAX_IMAGE_SIZE_MB * 1024, quality)
            }

            // Convertir a Base64
            bitmapToBase64(bitmap, quality)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Crea un thumbnail de una imagen
     */
    fun createThumbnail(bitmap: Bitmap, size: Int = 200): Bitmap {
        return resizeBitmap(bitmap, size, size)
    }
}
