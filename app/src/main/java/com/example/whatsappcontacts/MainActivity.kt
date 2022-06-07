package com.example.whatsappcontacts

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.ContactsContract
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.core.content.ContextCompat
import com.example.whatsappcontacts.ui.theme.WhatsAppContactsTheme
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList

var pos: Int = 0
var sPref: SharedPreferences? = null
val POSTION = "Postiion"
val DONE = "Done"
val INDEX = "Index"
var message = ""
var contacts = HashMap<String, String>()
var copyContacts = HashMap<String, String>()
var number = 0

class MainActivity : ComponentActivity() {
    private val TAG = "MainActivity"

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        contacts.clear()
        copyContacts.clear()
        sPref = getSharedPreferences("WhatsAppContacts", Context.MODE_PRIVATE)
        message = sPref!!.getString(MSG, " ")!!
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(android.Manifest.permission.READ_CONTACTS), 1001)
        } else {
            setContent()
        }
    }

    private fun setContent() {
        setContent {
            WhatsAppContactsTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    main(activity = this)
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == 1001 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            setContent()
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    @SuppressLint("Range")
    suspend fun getContacts(): List<Contact> {

        var tempList = sPref?.getString(DONE, "")
        var doneList =
            Gson().fromJson(tempList, Array<Contact>::class.java)?.toList()?.toMutableList()

        //This class provides applications access to the content model.
        //This class provides applications access to the content model.
        val cr: ContentResolver = contentResolver
//RowContacts for filter Account Types

//RowContacts for filter Account Types
        val contactCursor: Cursor? = cr.query(
            ContactsContract.RawContacts.CONTENT_URI, arrayOf(
                ContactsContract.RawContacts._ID,
                ContactsContract.RawContacts.CONTACT_ID
            ),
            ContactsContract.RawContacts.ACCOUNT_TYPE + "= ?", arrayOf("com.whatsapp"),
            null
        )

//ArrayList for Store Whatsapp Contact

//ArrayList for Store Whatsapp Contact
        val contactsList: ArrayList<Contact> = ArrayList()

        if (contactCursor != null) {
            if (contactCursor.count > 0) {
                if (contactCursor.moveToFirst()) {
                    do {
                        //whatsappContactId for get Number,Name,Id ect... from  ContactsContract.CommonDataKinds.Phone
                        val whatsappContactId: String =
                            contactCursor.getString(contactCursor.getColumnIndex(ContactsContract.RawContacts.CONTACT_ID))
                        if (whatsappContactId != null) {
                            //Get Data from ContactsContract.CommonDataKinds.Phone of Specific CONTACT_ID
                            val whatsAppContactCursor: Cursor? = cr.query(
                                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                arrayOf(
                                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                                    ContactsContract.CommonDataKinds.Phone.NUMBER,
                                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
                                ),
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                                arrayOf(whatsappContactId),
                                null
                            )
                            if (whatsAppContactCursor != null) {
                                whatsAppContactCursor.moveToFirst()
                                val id: String = whatsAppContactCursor.getString(
                                    whatsAppContactCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
                                )
                                val name: String = whatsAppContactCursor.getString(
                                    whatsAppContactCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                                )
                                val number: String = whatsAppContactCursor.getString(
                                    whatsAppContactCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                                )
                                whatsAppContactCursor.close()


                                var contact = Contact(name, number)
                                if (doneList == null || doneList.indexOf(contact) == -1) {
                                    contacts[name] = number
                                }
                                Log.i(TAG, " WhatsApp contact id  :  $id")
                                Log.i(TAG, " WhatsApp contact name :  $name")
                                Log.i(TAG, " WhatsApp contact number :  $number")
                            }
                        }
                    } while (contactCursor.moveToNext())
                    contactCursor.close()
                }
            }
        }

        copyContacts = contacts
        for (contact in contacts.entries) {
            contactsList.add(Contact(contact.key, contact.value))
        }
        contactsList.sortBy { contact -> contact.name }
        number = contactsList.size
        save(contactsList)
        Log.i(TAG, " WhatsApp contact size :  " + contactsList.size)
        return contactsList
    }

    fun sendMessage(number: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data =
            Uri.parse(
                "http://api.whatsapp.com/send?phone=" + number.toString() + "&text=" + sPref!!.getString(
                    MSG, ""
                )
            )
        intent.`package` = "com.whatsapp"
        startActivity(intent)
    }

    private fun savePosition() {
        sPref!!.edit().putInt(POSTION, pos).apply()
    }

    override fun onDestroy() {
        savePosition()
        super.onDestroy()
    }

}

class ContactsState(activity: MainActivity) {
    var contacts = mutableStateListOf<Contact>()
}


class NumState() {
    var num: Int by mutableStateOf(0)
}

data class Contact(var name: String, var number: String) {}


@SuppressLint("CoroutineCreationDuringComposition")
@OptIn(ExperimentalUnitApi::class)
@Composable
fun main(activity: MainActivity) {

    var contactsState = remember {
        ContactsState(activity = activity)
    }
    var numState = remember {
        NumState()
    }

    if(App.firstLaunch) {
        CoroutineScope(Dispatchers.IO).launch {
            var list = activity.getContacts()
            contactsState.contacts.clear()
            contactsState.contacts.addAll(list)
            App.firstLaunch = false
        }
    }

    Column() {
        Row(
            modifier = Modifier
                .padding(Dp(5f))
                .height(IntrinsicSize.Min)
                .width(IntrinsicSize.Max)
        ) {
            Text(
                style = TextStyle(fontSize = TextUnit(16f, TextUnitType.Sp)),
                modifier = Modifier
                    .padding(Dp(5f))
                    .align(alignment = Alignment.CenterVertically), text = "WhatsApp Contacts"
            )
            Spacer(modifier = Modifier.width(Dp(10f)))
            Button(onClick = {
                activity.startActivity(
                    Intent(
                        activity,
                        EnterMessageActivity::class.java
                    )
                )
            }) {
                Text(
                    modifier = Modifier
                        .width(IntrinsicSize.Min)
                        .height(IntrinsicSize.Min), text = "Set\nMessage",
                    style = TextStyle(
                        color = Color.White,
                        fontSize = TextUnit(12f, TextUnitType.Sp)
                    )
                )
            }
            Spacer(modifier = Modifier.width(Dp(10f)))
            Button(onClick = {
                sPref!!.edit().putString(DONE, "").apply()
                App.firstLaunch = true
                contactsState.contacts.clear()
            }) {
                Text(
                    modifier = Modifier
                        .width(IntrinsicSize.Min)
                        .height(IntrinsicSize.Min), text = "Reset\nPosition",
                    style = TextStyle(
                        color = Color.White,
                        fontSize = TextUnit(12f, TextUnitType.Sp)
                    )
                )
            }
        }


        if (contactsState.contacts.size > 0) {
            numState.num = contactsState.contacts.size
            Column() {

                Text(
                    modifier = Modifier
                        .padding(2.dp)
                        .align(CenterHorizontally),
                    text = "${numState.num} Contacts"
                )
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Dp(10f))
                ) {
                    itemsIndexed(contactsState.contacts) { index, item ->
                        ContactItem(
                            name = item.name,
                            number = item.number,
                            onClick = {
                                var tempList = sPref?.getString(DONE, "")
                                var doneList = Gson()
                                    .fromJson(tempList, Array<Contact>::class.java)
                                    ?.toList()
                                    ?.toMutableList()
                                if (doneList == null) {
                                    doneList = ArrayList<Contact>()
                                }
                                doneList.add(Contact(item.name, item.number))
                                sPref!!
                                    .edit()
                                    .putString(DONE, Gson().toJson(doneList))
                                    .apply()
                                sPref!!.edit().putInt(INDEX, index).apply()
                                activity.sendMessage(item.number)
                                contactsState.contacts.remove(item)
                            }
                        )
                    }
                }
            }
        } else {
            Loader()
        }
    }
}

@Composable
fun Loader(){
    Column(modifier = Modifier
        .fillMaxWidth()) {
        Spacer(modifier = Modifier.height(200.dp))
        CircularProgressIndicator(
            modifier = Modifier
                .align(CenterHorizontally)
                .width(50.dp)
                .height(50.dp)
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            modifier = Modifier
                .align(CenterHorizontally)
                .width(IntrinsicSize.Max)
                .height(IntrinsicSize.Min), text = "Loading Contacts...."
        )
    }
}

@OptIn(ExperimentalUnitApi::class)
@Composable
fun ContactItem(
    name: String,
    number: String,
    onClick: () -> Unit
) {
    Column() {
        Spacer(modifier = Modifier.height(Dp(5f)))
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() },
            shape = RoundedCornerShape(10.dp),
            elevation = Dp(5f),
            border = BorderStroke(Dp(0.2f), Color.Green)
        ) {
            Column(
                modifier = Modifier
                    .padding(Dp(10f))
                    .width(IntrinsicSize.Max)
                    .height(IntrinsicSize.Min)
            ) {
                Text(
                    style = TextStyle(
                        fontSize = TextUnit(16f, TextUnitType.Sp),
                        fontWeight = FontWeight(700)
                    ), text = name
                )
                Text(
                    style = TextStyle(fontSize = TextUnit(14f, TextUnitType.Sp)),
                    text = number
                )
            }
        }
        Spacer(modifier = Modifier.height(Dp(5f)))
    }

}

fun save(list: ArrayList<Contact>) {
    // new file object
    var dir =
        File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                .toURI()
        )

    var file = File(dir, "Contacts.txt")

    var bf: BufferedWriter? = null

    try {

        // create new BufferedWriter for the output file
        bf = BufferedWriter(FileWriter(file))

        // iterate map entries

        for (contact in list) {

            // put key and value separated by a colon
            bf.write(
                contact.name + "\n"
                        + contact.number + "\n\n"
            )

            // new line
            bf.newLine()
        }

        bf.flush()
    } catch (e: IOException) {
        e.printStackTrace()
    } finally {

        try {

            // always close the writer
            bf?.close()
        } catch (e: Exception) {
        }
    }
}

