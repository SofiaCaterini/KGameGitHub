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
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_account.*
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.tasks.await

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
        data.put(NICKNAME, nickname)


        db.collection(ACCOUNTS)
                .document(mail)
                .set(data as Map<String, Any>)
                .addOnFailureListener {
                    println("save on db epic fail: $it")
                }
    }

    suspend fun getUserDoc() : DocumentReference? {

        if (fbUser != null && fbUser.email != null) {
            return withContext(Dispatchers.IO) {

                db.collection(ACCOUNTS)
                        .document(fbUser.email)
            }
        }
        else{
            println("Error when retrieving user's document: DbManager.getUserDoc()")
            return null
        }
    }


//        suspend fun getUser() : User? {                                                         //DEPRECATED
//                                                                                                //DEPRECATED
//            if (fbUser != null) {                                                               //DEPRECATED
//                return withContext(Dispatchers.IO) {                                            //DEPRECATED
//                    val user = db.collection(ACCOUNTS)                                          //DEPRECATED
//                            .document(fbUser.email)                                             //DEPRECATED
//                            .get()                                                              //DEPRECATED
//                            .await()                                                            //DEPRECATED
//                    //user.toObject<User>()                                                     //DEPRECATED
//                    if (user[FAM_CODE]==null){                                                  //DEPRECATED
//                                                                                                //DEPRECATED
//                    }                                                                           //DEPRECATED
//                    if (user.getLong(PAWN_CODE) in 0..2 &&                                      //DEPRECATED
//                            user.getLong(PAWN_CODE)?.toInt() != null) {                         //DEPRECATED
//                    }                                                                           //DEPRECATED
//                    User(                                                                       //DEPRECATED
//                            fbUser.uid,                                                         //DEPRECATED
//                            fbUser.email,                                                       //DEPRECATED
//                            user[NICKNAME].toString(),                                          //DEPRECATED
//                            user[FAM_CODE]?.toString(),                                         //DEPRECATED
//                            user[PAWN_CODE]?.toString()?.toInt(),                               //DEPRECATED
//                            user[OBJ]?.toString()?.toDouble())                                  //DEPRECATED
//                }                                                                               //DEPRECATED
//            }                                                                                   //DEPRECATED
//            else {                                                                              //DEPRECATED
//                println("Error while retrieving user: DbManager.getuser()")                     //DEPRECATED
//                return null                                                                     //DEPRECATED
//            }                                                                                   //DEPRECATED
//        }                                                                                       //DEPRECATED


    fun updateUser(context: Context, user: User) {
        val data : MutableMap<String, Any> = mutableMapOf()
        data[NICKNAME] = user.username!!
        data[PAWN_CODE] = user.pawnCode!!

        if (fbUser != null) {
            db.collection(ACCOUNTS)
                    .document(fbUser.email)
                    .update(data as Map<String, Any>)
                    .addOnSuccessListener {
                        Toast.makeText(
                                context,
                                R.string.succ_newNNorPP,
                                Toast.LENGTH_SHORT
                        ).show()
                        println("update Nickname/Pawn success")
                    }
                    .addOnFailureListener {
                        Toast.makeText(
                                context,
                                R.string.fail_newNNorPP,
                                Toast.LENGTH_SHORT
                        ).show();
                        println("update Nickname/Pawn epic fail")
                    }
        }
    }

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
                                                                            R.string.profPicUpdated,
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
}