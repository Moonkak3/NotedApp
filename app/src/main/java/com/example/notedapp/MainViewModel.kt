package com.example.notedapp

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.example.notedapp.models.DiaryEntry
import com.example.notedapp.models.Mood
import com.example.notedapp.models.Tag
import com.example.notedapp.models.Task
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class MainViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private lateinit var currUser: FirebaseUser
        private lateinit var database: DatabaseReference
        private var currTask: Task? = null
        private var currTag: Tag? = null
        private var currEntry: DiaryEntry? = null
    }

    private val TAG = "MainViewModel"

    fun firstTimeSetup() {
        Log.i(TAG, "Setting up new user")
        Firebase.database.reference.child("users").child(getCurrUserEmail())
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.value == null) {
                        // Add default tags and task
                        Log.i(TAG, "Initiating new user")
                        addTag(Tag("Homework", "#4adede", 0))
                        addTag(Tag("Urgent", "#B80F0A", 0))
                        addTag(Tag("CCA", "#1aa7ec", 0))
                        addTask(
                            Task(
                                "Set up Noted",
                                "Make this app your second brain, and never forget your to-dos",
                                System.currentTimeMillis(),
                                System.currentTimeMillis(),
                                arrayListOf(), false
                            )
                        )

                        database.child("tasks").get()
                            .addOnSuccessListener { dataSnapShot ->
                                for (snapShot in dataSnapShot.children) {
                                    val task = snapShot.getValue(Task::class.java)!!
                                    if (task.name == "Set up Noted") {
                                        addDiaryEntry(
                                            DiaryEntry(
                                                "Downloaded Noted",
                                                "And God said, 'Let there be light'. \n" +
                                                        "The glowing pixels emanating from the it's brilliance blessed me with its divine light. " +
                                                        "My IQ doubled, muscles started growing in the most obscure of places. \n" +
                                                        "Could this be the second coming of Jesus? \n" +
                                                        "No, it's the first coming of Noted.",
                                                System.currentTimeMillis(),
                                                arrayListOf(task.key),
                                                Mood.CHEERFUL
                                            )
                                        )
                                        break
                                    }
                                }
                            }
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }

    fun setCurrUser(user: FirebaseUser) {
        currUser = user
        database = Firebase.database.reference.child("users").child(getCurrUserEmail())
        Log.i(TAG, getCurrUserEmail())
    }

    fun getCurrUserEmail(formatted: Boolean = true): String {
        return if (formatted){
            currUser.email!!.replace('.', ',')
        } else {
            currUser.email!!
        }
    }

    fun getCurrUserName(): String {
        return currUser.displayName!!
    }

    fun setCurrTask(task: Task?) {
        currTask = task
    }

    fun getCurrTask(): Task? {
        return currTask
    }

    fun setCurrTag(tag: Tag?) {
        currTag = tag
    }

    fun getCurrTag(): Tag? {
        return currTag
    }

    fun setCurrEntry(entry: DiaryEntry?) {
        currEntry = entry
    }

    fun getCurrEntry(): DiaryEntry? {
        return currEntry
    }

    fun checkTask(task: Task, done: Boolean): com.google.android.gms.tasks.Task<Void> {
        Log.i(TAG, "${currUser.email} checked a task.")
        return database.child("tasks").child(task.key).child("done").setValue(done)
    }

    fun addTask(task: Task): com.google.android.gms.tasks.Task<Void> {
        Log.i(TAG, "${currUser.email} added a task.")

        // update numTasks
        if (task.key == "") {
            task.key = database.child("tasks").push().key!!
            for (tagKey in task.tagKeys) {
                changeNumTasks(tagKey, 1)
            }
        } else {
            database.child("tasks").child(task.key).child("tagKeys").get()
                .addOnSuccessListener { dataSnapShot ->
                    val oldTagKeys = ArrayList<String>()
                    for (snapShot in dataSnapShot.children) {
                        oldTagKeys.add(snapShot.getValue(String::class.java)!!)
                    }

                    for (oldTagKey in oldTagKeys) {
                        if (!task.tagKeys.contains(oldTagKey)) {
                            changeNumTasks(oldTagKey, -1)
                        }
                    }
                    for (tagKey in task.tagKeys) {
                        if (!oldTagKeys.contains(tagKey)) {
                            changeNumTasks(tagKey, 1)
                        }
                    }
                }
        }

        // adding task
        return database.child("tasks").child(task.key)
            .setValue(task)
    }

    fun deleteTask(task: Task): com.google.android.gms.tasks.Task<Void> {
        Log.i(TAG, "${currUser.email} deleted a task.")
        for (tagKey in task.tagKeys) {
            changeNumTasks(tagKey, -1)
        }
        return database.child("tasks").child(task.key)
            .removeValue()
    }

    fun addTag(tag: Tag): com.google.android.gms.tasks.Task<Void> {
        Log.i(TAG, "${currUser.email} added a tag.")
        if (tag.key == "") {
            tag.key = database.child("tags").push().key!!
        }
        return database.child("tags").child(tag.key)
            .setValue(tag)
    }

    fun deleteTag(tag: Tag): com.google.android.gms.tasks.Task<Void> {
        Log.i(TAG, "${currUser.email} added a tag.")
        return database.child("tags").child(tag.key)
            .removeValue()
    }

    private fun changeNumTasks(tagKey: String, change: Int) {
        database.child("tags").child(tagKey).child("numTasks").get().addOnSuccessListener {
            database.child("tags").child(tagKey)
                .child("numTasks").setValue(it.getValue(Int::class.java)!! + change)
        }
    }

    fun addDiaryEntry(entry: DiaryEntry): com.google.android.gms.tasks.Task<Void> {
        Log.i(TAG, "${currUser.email} added a diary entry.")
        return database.child("diaryEntries").child(entry.date.toString()).setValue(entry)
    }

    fun deleteDiaryEntry(entry: DiaryEntry): com.google.android.gms.tasks.Task<Void> {
        Log.i(TAG, "${currUser.email} deleted a diary entry.")
        return database.child("diaryEntries").child(entry.date.toString()).removeValue()
    }
}