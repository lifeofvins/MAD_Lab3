package it.polito.group06.utilities

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import it.polito.group06.MVVM.UserProfileDatabase.UserProfile
import java.io.*

fun emptyUserProfile()=UserProfile(
        null,
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        null
    )
/**
 * This utility method changes an array list into a string with a specific format
 *
 * @param list  List of skills
 * @return string of skills
 */
fun fromArrayListToString(list: ArrayList<String>): String {
    var out = ""
    for (i in list.indices) {
        out += list[i]
        if (i != list.size - 1)
            out += ", "
    }
    return out
}


/**
 * This utility method changes string into an array list with a specific format
 *
 * @param list  string containing list of skills
 * @return array_list
 */
fun fromStringToArrayList(s:String): ArrayList<String> {
    var skillList=ArrayList<String>()
    if (s.compareTo("") != 0 && s.split(" ").size != s.length + 1) {
        val x: List<String> = s.split(",")
        for (i in x.indices) {
            if (x[i].compareTo("") != 0) {
                skillList.add(i, x[i].trim())
            }
        }
    }
    return skillList
}

/**
 * This method returns the Bitmap image from a file whose path is provided
 *
 * @param path  The path were to find the file
 * @return Bitmap image results
 */
fun getBitmapFromFile(path: String): Bitmap? {
    return BitmapFactory.decodeFile(path)
}

/**
 * This method creates an image file and throws an exception if the file could not be created
 *
 * @return Created file
 * @throws IOException
 */
@Throws(IOException::class)
fun createImageFile(profilePicturePath:String): File {
    // Create an image file name
    return File(profilePicturePath)
}

/**
 * This method stores the profile picture into a file
 *
 * @param bitmap  Bitmap image of the profile picture
 * @param dir  Directory where to store the profile picture
 */
fun saveProfilePicture(bitmap: Bitmap, dir: String) {
    val imageFile = File(dir, "profile_picture.jpg")
    try {
        // Compress the bitmap and save in jpg format
        val stream: OutputStream = FileOutputStream(imageFile)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream) ?: throw IOException()
        stream.flush()
        stream.close()
    } catch (e: IOException) {
        e.printStackTrace()
    }
}

fun parseSkillString(x: String): String {
    var out = ""
    var first = true
    for (kw in x.split(",")) {
        if (kw.trim().isNotEmpty() && kw.isNotEmpty()) {
            if (first) {
                out += kw.trim()
                first = false
            } else {
                out += ", "
                out += kw.trim()
            }
        }
    }
    return out
}

//SUPPORT METHODS FOR ROTATING IMAGES///////////////////////////////////////////////////////////
/**
 * This method is responsible for solving the rotation issue if exist. Also scale the images to
 * 1024x1024 resolution
 *
 * @param context       The current context
 * @param selectedImage The Image URI
 * @return Bitmap image results
 * @throws IOException
 */
@Throws(IOException::class)
fun handleSamplingAndRotationBitmap(context: Context, selectedImage: Uri?): Bitmap? {
    val MAX_HEIGHT = 1024
    val MAX_WIDTH = 1024

    // First decode with inJustDecodeBounds=true to check dimensions
    val options = BitmapFactory.Options()
    options.inJustDecodeBounds = true
    var imageStream: InputStream? = context.contentResolver.openInputStream(selectedImage!!)
    BitmapFactory.decodeStream(imageStream, null, options)
    imageStream?.close()

    // Calculate inSampleSize
    options.inSampleSize = calculateInSampleSize(options, MAX_WIDTH, MAX_HEIGHT)

    // Decode bitmap with inSampleSize set
    options.inJustDecodeBounds = false
    imageStream = context.contentResolver.openInputStream(selectedImage)
    var img = BitmapFactory.decodeStream(imageStream, null, options)
    img = img?.let { rotateImageIfRequired(context, it, selectedImage) }
    return img
}

/**
 * Calculate an inSampleSize for use in a [BitmapFactory.Options] object when decoding
 * bitmaps using the decode* methods from [BitmapFactory]. This implementation calculates
 * the closest inSampleSize that will result in the final decoded bitmap having a width and
 * height equal to or larger than the requested width and height. This implementation does not
 * ensure a power of 2 is returned for inSampleSize which can be faster when decoding but
 * results in a larger bitmap which isn't as useful for caching purposes.
 *
 * @param options   An options object with out* params already populated (run through a decode*
 * method with inJustDecodeBounds==true
 * @param reqWidth  The requested width of the resulting bitmap
 * @param reqHeight The requested height of the resulting bitmap
 * @return The value to be used for inSampleSize
 */
private fun calculateInSampleSize(
    options: BitmapFactory.Options,
    reqWidth: Int, reqHeight: Int
): Int {
    // Raw height and width of image
    val height = options.outHeight
    val width = options.outWidth
    var inSampleSize = 1
    if (height > reqHeight || width > reqWidth) {

        // Calculate ratios of height and width to requested height and width
        val heightRatio = Math.round(height.toFloat() / reqHeight.toFloat())
        val widthRatio = Math.round(width.toFloat() / reqWidth.toFloat())

        // Choose the smallest ratio as inSampleSize value, this will guarantee a final image
        // with both dimensions larger than or equal to the requested height and width.
        inSampleSize = if (heightRatio < widthRatio) heightRatio else widthRatio

        // This offers some additional logic in case the image has a strange
        // aspect ratio. For example, a panorama may have a much larger
        // width than height. In these cases the total pixels might still
        // end up being too large to fit comfortably in memory, so we should
        // be more aggressive with sample down the image (=larger inSampleSize).
        val totalPixels = (width * height).toFloat()

        // Anything more than 2x the requested pixels we'll sample down further
        val totalReqPixelsCap = (reqWidth * reqHeight * 2).toFloat()
        while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
            inSampleSize++
        }
    }
    return inSampleSize
}

/**
 * Rotate an image if required.
 *
 * @param img           The image bitmap
 * @param selectedImage Image URI
 * @return The resulted Bitmap after manipulation
 */
@Throws(IOException::class)
private fun rotateImageIfRequired(context: Context, img: Bitmap, selectedImage: Uri): Bitmap? {
    val input = context.contentResolver.openInputStream(selectedImage)
    val ei: ExifInterface = if (Build.VERSION.SDK_INT > 23)
        ExifInterface(input!!)
    else
        ExifInterface(selectedImage.path!!)
    val orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
    return when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(img, 90)
        ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(img, 180)
        ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(img, 270)
        else -> img
    }
}

private fun rotateImage(img: Bitmap, degree: Int): Bitmap? {
    val matrix = Matrix()
    matrix.postRotate(degree.toFloat())
    val rotatedImg = Bitmap.createBitmap(img, 0, 0, img.width, img.height, matrix, true)
    img.recycle()
    return rotatedImg
}