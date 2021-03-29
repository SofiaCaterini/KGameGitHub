package it.polito.kgame

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.tasks.await
import java.util.*

object DbManager {

    /*
    This object Class contains FUNCTIONS to use with FireStore Database (db)
     */


    //database Collections
    const val ACCOUNTS = "Accounts"
    const val FAMILIES = "Families"
    const val FAM_COMPS = "Family Components"
    const val MATCHES = "Matches"

    //database KEYS
        //account
    const val NICKNAME = "username"
    const val FAM_CODE = "familyCode"
    const val PROF_PIC = "profileImg"
    const val PAWN_CODE = "pawnCode"
    const val OBJ = "objective"
        //family
    const val FAM_NAME = "familyName"
        //match
    const val MATCH_START = "matchStartDate"
        //storage
    private var mStorageRef: StorageReference? = FirebaseStorage.getInstance().getReference("uploads")
    private var mUploadTask: StorageTask<*>? = null




    @SuppressLint("StaticFieldLeak")
    val db : FirebaseFirestore = FirebaseFirestore.getInstance()

    private val fbAuth : FirebaseAuth = FirebaseAuth.getInstance()
    private val fbUser : FirebaseUser? = fbAuth.currentUser

    fun registerUser(nickname : String, mail : String) {

        val data : MutableMap<String,String> = mutableMapOf()
        data[NICKNAME] = nickname

        db.collection(ACCOUNTS)
                .document(mail)
                .set(data as Map<String, Any>)
                .addOnFailureListener {
                    println("save on db epic fail: $it")
                }
    }

    private fun createFamily(familyName: String?) {

        val data : MutableMap<String,String> = mutableMapOf()
        if(familyName != null) data[FAM_NAME] = familyName

        val code = randomCode()
        var codeIsGood = false

        db.collection(FAMILIES).document(code).get().addOnSuccessListener {
            codeIsGood = !it.exists()

            if (codeIsGood) {
                db.collection(FAMILIES)
                    .document(code)
                    .set(data as Map<String, Any>)
                    .addOnSuccessListener {
                        joinFamily(code)            //if family is correctly created, add creator user to it
                    }
                    .addOnFailureListener {
                        println("save on db epic fail: $it")
                    }
            } else createFamily(familyName)
        }
    }

    private fun joinFamily(familyCode: String) {
        val data : MutableMap<String, Any> = mutableMapOf()
        val empty : MutableMap<String, Any> = mutableMapOf()
        data[FAM_CODE] = familyCode
        empty["exists"] = true
        if (fbUser != null) {
            db.collection(FAMILIES)
                .document(familyCode)
                .collection(FAM_COMPS)
                .document(fbUser.email)
                .set(empty)

            db.collection(ACCOUNTS)
                .document(fbUser.email)
                .update(data)
        }
    }

    fun setUpUserProfile(profilePic: Uri?, familyCode: String?, familyName: String?, context: Context) {

        if(profilePic != null) {
            uploadProfileImg(context, profilePic)
        }

        if (familyCode == null) {   //create family         //CHECK IF CODE NOT ALREADY USED!!
            createFamily(familyName)
        } else {              //join family
            joinFamily(familyCode)
        }
    }

    suspend fun getUserDoc() : DocumentReference? {

        return if (fbUser != null && fbUser.email != null) {
            withContext(Dispatchers.IO) {

                db.collection(ACCOUNTS)
                    .document(fbUser.email!!)
            }
        } else{
            println("Error when retrieving user's document: DbManager.getUserDoc()")
            null
        }
    }

    suspend fun getFamilyDoc(code: String) : DocumentReference? {

        return if (fbUser != null && fbUser.email != null) {
            withContext(Dispatchers.IO) {

                db.collection(FAMILIES)
                    .document(code)
            }
        } else{
            println("Error when retrieving family document: DbManager.getUserDoc()")
            null
        }
    }


    suspend fun getUser() : User? {                                                         //DEPRECATED
                                                                                                //DEPRECATED
            if (fbUser != null) {                                                               //DEPRECATED
                return withContext(Dispatchers.IO) {                                            //DEPRECATED
                    val user = db.collection(ACCOUNTS)                                          //DEPRECATED
                            .document(fbUser.email)                                             //DEPRECATED
                            .get()                                                              //DEPRECATED
                            .await()                                                            //DEPRECATED
                    //user.toObject<User>()                                                     //DEPRECATED
                    if (user[FAM_CODE]!=null){                                                  //DEPRECATED
                                                                                                //DEPRECATED
                    }                                                                           //DEPRECATED
                                                                                                //DEPRECATED
                    User(                                                                       //DEPRECATED
                            fbUser.uid,                                                         //DEPRECATED
                            fbUser.email,                                                       //DEPRECATED
                            user[NICKNAME].toString(),                                          //DEPRECATED
                            user[FAM_CODE]?.toString(),                                         //DEPRECATED
                            user[PAWN_CODE]?.toString()?.toInt(),                               //DEPRECATED
                            user[OBJ]?.toString()?.toDouble())                                  //DEPRECATED
                }                                                                               //DEPRECATED
            }                                                                                   //DEPRECATED
            else {                                                                              //DEPRECATED
                println("Error while retrieving user: DbManager.getuser()")                     //DEPRECATED
                return null                                                                     //DEPRECATED
            }                                                                                   //DEPRECATED
        }    //DEPRECATED                         //DEPRECATED                                  //DEPRECATED


//        db.collection(ACCOUNTS)
//            .document(fbUser.email)
//            .get()
//            .addOnSuccessListener {
//                if (it.exists()) {
//                    println("STC ACCAAAAAAAAAAAAAAAAAAAAAAAAAA")
//
//                    nickname = it.getString(NICKNAME)
//                    familyCode = it.getString(FAM_CODE)
//                    profImg = it.getString(PROF_PIC)
//                    if (it.getLong(PAWN_CODE) in 0..2 && it.getLong(PAWN_CODE)?.toInt() != null) {
//                        pawnCode = it.getLong(PAWN_CODE)!!.toInt()
//                    }
//                    obj = it.getDouble(OBJ)
//
//
//                } else {
//                    println("Errore: l'account non esiste")
//                }
//            }.result?.toObject<User>()


        //User(fbUser.uid, fbUser.email, nickname, familyCode, pawnCode, obj)



    fun readNickname(context: Context) : String? {

        var ret : String? = null

        if (fbUser != null) {
            db.collection(DbManager.ACCOUNTS)
                    .document(fbUser.email)
                    .get()
                    .addOnSuccessListener {
                        if (it.exists()) {
                            ret = it.getString(NICKNAME).toString()
                        } else {
                            Toast.makeText(
                                    context,
                                    R.string.req_doc,
                                    Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    .addOnFailureListener {
                        println("Error: $it")
                    }
        }
        return ret
    }

    fun updateUser(context: Context?, user: User) {
        val data : MutableMap<String, Any> = mutableMapOf()
        if(user.username != null) data[NICKNAME] = user.username!!
        if(user.pawnCode != null) data[PAWN_CODE] = user.pawnCode!!
        if(user.profileImg != null) data[PROF_PIC] = user.profileImg!!
        if(user.familyCode != null) data[FAM_CODE] = user.familyCode!!

        //        data[PROF_PIC] = user.profileImg

        if (fbUser != null) {
            db.collection(ACCOUNTS)
                    .document(fbUser.email)
                    .update(data as Map<String, Any>)
                    .addOnSuccessListener {
                        if(context != null) Toast.makeText(
                                context,
                                R.string.succ_update,
                                Toast.LENGTH_SHORT
                        ).show()
                        println("update success")
                    }
                    .addOnFailureListener {
                        if(context != null) Toast.makeText(
                                context,
                                R.string.fail_update,
                                Toast.LENGTH_SHORT
                        ).show();
                        println("update epic fail")
                    }
        }
    }


    fun updateNickname(context: Context, newNick : String) {

        val data : MutableMap<String,String> = mutableMapOf()
        data[NICKNAME] = newNick

        if (fbUser != null) {
            db.collection(ACCOUNTS)
                    .document(fbUser.email)
                    .update(data as Map<String, Any>)
                    .addOnSuccessListener {
                        Toast.makeText(
                                context,
                                R.string.succ_newNN,
                                Toast.LENGTH_SHORT
                        ).show()
                        println("update Nickname success")
                    }
                    .addOnFailureListener {
                        Toast.makeText(
                                context,
                                R.string.fail_newNN,
                                Toast.LENGTH_SHORT
                        ).show();
                        println("update Nickname epic fail")
                    }
        }
    }

    fun updatePawnCode(context: Context, pawnCode : Int) {
        val data : MutableMap<String,Int> = mutableMapOf()
        data[PAWN_CODE] = pawnCode


        if (fbUser != null) {
            db.collection(ACCOUNTS)
                    .document(fbUser.email)
                    .update(data as Map<String, Any>)
                    .addOnSuccessListener {
                        Toast.makeText(
                                context,
                                R.string.succ_newPawn,
                                Toast.LENGTH_SHORT
                        ).show();
                        println("update Pawn success")
                    }
                    .addOnFailureListener {
                        Toast.makeText(
                                context,
                                R.string.fail_newPawn,
                                Toast.LENGTH_SHORT
                        ).show();
                        println("update Pawn epic fail")
                    }
        }
    }

//        private fun getFileExtension(uri: Uri): String? {
//        val cR: ContentResolver = requireContext().contentResolver
//        val mime = MimeTypeMap.getSingleton()
//        return mime.getExtensionFromMimeType(cR.getType(uri))
//    }

    fun uploadProfileImg(context: Context, imageUri : Uri?) {
        fun getFileExtension(uri: Uri): String? {
            val cR: ContentResolver = context.contentResolver
            val mime = MimeTypeMap.getSingleton()
            return mime.getExtensionFromMimeType(cR.getType(uri))
        }

        if (imageUri != null) {
            val fileReference = mStorageRef!!.child(
                    System.currentTimeMillis()
                            .toString() + "." + getFileExtension(imageUri)
            )
            mUploadTask = fileReference.putFile(imageUri)
                    .addOnSuccessListener { _ ->
                        val data : MutableMap<String,String> = mutableMapOf()

                        fileReference.downloadUrl.addOnCompleteListener () { taskSnapshot ->
                            val url = taskSnapshot.result
                            println ("url =" + url.toString())
                            data[DbManager.PROF_PIC] = url.toString()
                            fbUser.let {
                                if (it != null) {
                                    db.collection(DbManager.ACCOUNTS)
                                            .document(it.email)
                                            .update(data as Map<String, Any>)
                                            .addOnSuccessListener {
                                                                    Toast.makeText(
                                                                            context,
                                                                            R.string.prof_pic_updated,
                                                                            Toast.LENGTH_SHORT
                                                                    ).show()
                                            }
                                }
                            }

                        }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(
                                context,
                                e.message,
                                Toast.LENGTH_SHORT
                        ).show()
                    }

        } else {
            Toast.makeText(
                    context,
                    R.string.file_missing,
                    Toast.LENGTH_SHORT
            ).show()
            println("errore")
        }

    }

    private fun randomCode(): String {
        val allowedChars = "0123456789qwertyuiopasdfghjklzxcvbnm"
        val sizeOfRandomString = 6
        val random = Random()
        val sb = StringBuilder(sizeOfRandomString)
        for (i in 0 until sizeOfRandomString)
            sb.append(allowedChars[random.nextInt(allowedChars.length)])
        return sb.toString()
    }
}