package it.polito.kgame

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.*

object DbManager {

    /*
    This object Class contains FUNCTIONS to use with FireStore Database (db)
     */
    //database Collections
    const val ACCOUNTS = "Accounts"
    const val FAMILIES = "Families"
    const val FAM_COMPS = "Family Components"
    const val APPOINTMENTS = "Appointments"
    const val WEIGHTSESSIONS = "WeightSessions"

    //database KEYS
        //account
    const val MAIL = "mail"
    const val NICKNAME = "username"
    const val FAM_CODE = "familyCode"
    const val PROF_PIC = "profileImg"
    const val PAWN_CODE = "pawnCode"
    const val OBJ = "objective"
    const val POSITION = "position"
    const val STREAK = "goodWeightStreak"
    const val INGAME = "inGame"
        //family
    const val FAM_NAME = "familyName"
        //match
    const val MATCH_START_DATE = "lastMatchMillis"
    const val MATCH_STATE = "matchState"
    const val PLAYERS_IN_GAME = "playersInGame"
    const val LAST_WINNER = "lastWinner"
    //appointment
    const val TITLE = "titolo"
    const val DESCRIPTION = "descrizione"
    const val CALENDAR = "calendar"
    const val LOCATION = "luogo"
        //weigh
    const val WEIGHT = "peso"
    const val DATA = "data"
        //storage
    private var mStorageRef: StorageReference? = FirebaseStorage.getInstance().getReference("uploads")
    private var mUploadTask: StorageTask<*>? = null



    @SuppressLint("StaticFieldLeak")
    val db : FirebaseFirestore = FirebaseFirestore.getInstance()

    private val fbAuth : FirebaseAuth = FirebaseAuth.getInstance()
    val fbUser : FirebaseUser? = fbAuth.currentUser

    fun registerUser(nickname : String, mail : String) {

        val data : MutableMap<String,Any> = mutableMapOf()
        data[MAIL] = mail
        data[NICKNAME] = nickname
        data[POSITION] = 0
        data[INGAME] = false

        db.collection(ACCOUNTS)
                .document(mail)
                .set(data as Map<String, Any>)
                .addOnFailureListener {
                    println("save on db epic fail: $it")
                }
    }

    private fun createFamily(familyName: String?) {

        val data : MutableMap<String,Any> = mutableMapOf()
        if(familyName != null) data[FAM_NAME] = familyName
        data[MATCH_STATE] = "NONE"
        data[PLAYERS_IN_GAME] = 0

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
        data[FAM_CODE] = familyCode
        var comps = mutableListOf<User>()
        var user = User()
        GlobalScope.launch {
            comps = getFamilyComps(familyCode)!!
            user = getUser(fbUser?.email)!!

        }.invokeOnCompletion {
            if (user != null && comps != null) {
                user.pawnCode = comps.size
                user.id = fbUser?.uid
                user.mail = fbUser?.email
            }
            if (fbUser != null && user != null) {
                db.collection(FAMILIES)
                        .document(familyCode)
                        .collection(FAM_COMPS)
                        .document(fbUser.email)
                        .set(user)
            }
        }

        if (fbUser != null) {

            db.collection(ACCOUNTS)
                .document(fbUser.email)
                .update(data)
        }
    }

    fun setUpUserProfile(profilePic: Uri?, familyCode: String?, familyName: String?, context: Context) {

        if(profilePic != null) {
            uploadProfileImg(context, profilePic)
        }

        if (familyCode == null) {   //create family       
            createFamily(familyName)

        } else {              //join family
            joinFamily(familyCode)
        }
    }

    suspend fun getUserDoc(mail: String? = null) : DocumentReference? {
        return if (fbUser != null && fbUser.email != null) {
            withContext(Dispatchers.IO) {

                db.collection(ACCOUNTS)
                    .document(mail ?: fbUser.email!!)
            }
        } else{
            println("Error when retrieving user's document: DbManager.getUserDoc()")
            null
        }
    }

    suspend fun getAppointments() : MutableList<EventoInfo>? {
        val events : MutableList<EventoInfo> = mutableListOf()
        return if (fbUser != null && fbUser.email != null) {
            withContext(Dispatchers.IO) {
                db.collection(ACCOUNTS)
                    .document(fbUser.email!!)
                    .collection(APPOINTMENTS)
                    .get()
                    .addOnSuccessListener {
                        for (doc in it) {
                            events.add(doc.toObject())
                        }
                    }
                    .await()
                events
            }
        } else{
            println("Error when retrieving family document: DbManager.getUserDoc()")
            null
        }
    }

    suspend fun getUserAppointmentColl() : CollectionReference? {
        return if (fbUser != null && fbUser.email != null) {
            withContext(Dispatchers.IO) {

                db.collection(ACCOUNTS)
                    .document(fbUser.email!!).collection(APPOINTMENTS)
            }
        } else{
            println("Error when retrieving user's document: DbManager.getUserAppointmentDoc()")
            null
        }
    }

    suspend fun getUserWeightColl() : CollectionReference? {
        return if (fbUser != null && fbUser.email != null) {
            withContext(Dispatchers.IO) {

                db.collection(ACCOUNTS)
                        .document(fbUser.email!!).collection(WEIGHTSESSIONS)
            }
        } else{
            println("Error when retrieving user's document: DbManager.getUserWeightDoc()")
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

    suspend fun getFamilyComps(code: String) : MutableList<User>? {
        val users : MutableList<User> = mutableListOf()
        return if (fbUser != null && fbUser.email != null) {
            withContext(Dispatchers.IO) {
                db.collection(FAMILIES)
                    .document(code)
                    .collection(FAM_COMPS)
                    .get()
                    .addOnSuccessListener {
                        for (doc in it) {
                            users.add(doc.toObject())
                        }
                    }
                    .await()
                users
            }
        } else{
            println("Error when retrieving family document: DbManager.getUserDoc()")
            null
        }
    }

    suspend fun getUser(mail: String? = null) : User? {
        var user = User()
        return if (fbUser != null && fbUser.email != null) {
            withContext(Dispatchers.IO) {
                db.collection(ACCOUNTS)
                        .document(mail ?: fbUser.email)
                        .get()
                        .addOnSuccessListener {
                            user = it.toObject()!!
                        }
                        .await()
                user
            }
        } else {
            println("Error when retrieving user: DbManager.getUser()")
            null
        }
    }



    fun updateUser(context: Context?, user: User) {
        val data : MutableMap<String, Any> = mutableMapOf()
        if(user.username != null) data[NICKNAME] = user.username!!
        if(user.pawnCode != null) data[PAWN_CODE] = user.pawnCode!!
        if(user.profileImg != null) data[PROF_PIC] = user.profileImg!!
        if(user.familyCode != null) data[FAM_CODE] = user.familyCode!!
        if(user.objective != null) data[OBJ] = user.objective!!
        data[POSITION] = user.position
        data[STREAK] = user.goodWeightStreak
        data[INGAME] = user.inGame!!



        if (fbUser != null) {
            db.collection(ACCOUNTS)         //update in accounts
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

            user.familyCode?.let { famCode ->          //update in families
                db.collection(FAMILIES)
                    .document(famCode)
                    .collection(FAM_COMPS)
                    .document(fbUser.email)
                    .update(data as Map<String, Any>)
            }

        }
    }

    fun updateFamily(context: Context?, family: Family) {
        val data : MutableMap<String, Any> = mutableMapOf()
        if(family.name != null) data[FAM_NAME] = family.name!!
        if(family.lastMatchMillis != null) data[MATCH_START_DATE] = family.lastMatchMillis!!
        data[MATCH_STATE] = family.matchState
        data[PLAYERS_IN_GAME] = family.playersInGame
        if(family.lastWinnerMail != null) data[LAST_WINNER] = family.lastWinnerMail!!

        family.code?.let { famCode ->          //update in families
            db.collection(FAMILIES)
                    .document(famCode)
                    .update(data as Map<String, Any>)
        }

    }

    fun deleteProfileInFamily(context: Context?){
        if (fbUser != null) {
            var user = User()
            GlobalScope.launch {
                user = getUser(fbUser.email)!!
            }.invokeOnCompletion {
                    val data : MutableMap<String, Any?> = mutableMapOf()
                    data[FAM_CODE] = null

                    db.collection(ACCOUNTS)         //update in accounts
                            .document(fbUser.email)
                            .update(data)

                    user.familyCode?.let { famCode ->          //update in families
                        db.collection(FAMILIES)
                                .document(famCode)
                                .collection(FAM_COMPS)
                                .document(fbUser.email)
                                .delete()
                                .addOnSuccessListener {
                                    if(context != null) Toast.makeText(
                                            context,
                                            R.string.succ_delete,
                                            Toast.LENGTH_SHORT
                                    ).show()
                                    println("update success")
                                }
                                .addOnFailureListener {
                                    if(context != null) Toast.makeText(
                                            context,
                                            R.string.fail_delete,
                                            Toast.LENGTH_SHORT
                                    ).show();
                                    println("update epic fail")
                                }
                    }
            }
        }
    }

    fun deleteUser(context: Context?){
        if (fbUser != null) {
            var user = User()
            GlobalScope.launch { user = getUser(fbUser.email)!! }
                .invokeOnCompletion {
                    //update in families
                db.collection(FAMILIES)
                    .document(user.familyCode!!)
                    .collection(FAM_COMPS)
                    .document(user.mail!!)
                    .delete()

                db.collection(ACCOUNTS)         //update in accounts
                    .document(fbUser.email)
                    .delete()
//                    .addOnSuccessListener {
//                        if(context != null) Toast.makeText(
//                                context,
//                                R.string.succ_delete,
//                                Toast.LENGTH_SHORT
//                        ).show()
//                        println("update success")
//                    }
//                    .addOnFailureListener {
//                        if(context != null) Toast.makeText(
//                                context,
//                                R.string.fail_delete,
//                                Toast.LENGTH_SHORT
//                        ).show();
//                        println("update epic fail")
//                    }
                fbUser.delete()
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
                                    GlobalScope.launch {
                                        if (it != null) {
                                            getUser(it.email)?.let { user ->
                                                user.profileImg = url.toString()
                                                updateUser(context, user) }
                                        }
                                    }
                                }
                                    /*db.collection(DbManager.ACCOUNTS)
                                            .document(it.email)
                                            .update(data as Map<String, Any>)
                                            .addOnSuccessListener {
                                                                    Toast.makeText(
                                                                            context,
                                                                            R.string.prof_pic_updated,
                                                                            Toast.LENGTH_SHORT
                                                                    ).show()
                                            }

                                }*/
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

    fun createAppointment(context: Context?, app:EventoInfo, code: Long) {
        val data : MutableMap<String,Any> = mutableMapOf()
        data[TITLE] = app.titolo!!
        data[CALENDAR] = app.cal?.timeInMillis!!
        data[DESCRIPTION] = app.descrizione!!
        data[LOCATION] = app.luogo!!

        if (fbUser != null) {
            db.collection(ACCOUNTS)
                .document(fbUser.email)
                .collection(APPOINTMENTS)    //update in impegni
                .document(code.toString())
                .set(data as Map<String, Any>)
                .addOnFailureListener {
                    println("save on db epic fail: $it")
                }
        }
    }

    fun deleteAppointment(app:EventoInfo){
        if (fbUser != null) {
            var chiave : String = ""
            db.collection(ACCOUNTS)
                    .document(fbUser.email)
                    .collection(APPOINTMENTS)
                    .whereEqualTo("calendar", app.cal?.timeInMillis?.toLong())//update in impegni
                    .get()
                    .addOnSuccessListener {
                        for (doc in it) {
                            chiave = doc.id
                            println("trovato: ${doc.id}")
                            println("tr: ${doc.reference}")
                            println("doc: $doc")
                            //doc.getDocumentReference(doc.reference.toString())?.delete()

                            db.collection(ACCOUNTS)
                                    .document(fbUser.email)
                                    .collection(APPOINTMENTS)
                                    .document(doc.id).delete()
                        }

                    }




        }


    }

    fun createWeight(context: Context?, weight: String, code: Long) {
        val data : MutableMap<String,Any> = mutableMapOf()
        data[WEIGHT] = weight
        data[DATA] = code

        if (fbUser != null) {
            db.collection(ACCOUNTS)
                    .document(fbUser.email)
                    .collection(WEIGHTSESSIONS)    //update in impegni
                    .document(code.toString())
                    .set(data as Map<String, Any>)
                    .addOnFailureListener {
                        println("save weight on db epic fail: $it")
                    }
        }
    }

    /*fun updateAppointment(context: Context?, app: EventoInfo) {
        val data : MutableMap<String, Any> = mutableMapOf()
        if(app.titolo != null) data[TITLE] = app.titolo!!
        if(app.cal != null) data[CALENDAR] = app.cal!!
        if(app.descrizione != null) data[DESCRIPTION] = app.descrizione!!
        if(app.luogo != null) data[LOCATION] = app.luogo!!


        if (fbUser != null) {
            db.collection(ACCOUNTS)         //update in accounts
                .document(fbUser.email)
                .collection(APPOINTMENTS)    //update in impegni
                .document("impegno")
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
    }*/
}