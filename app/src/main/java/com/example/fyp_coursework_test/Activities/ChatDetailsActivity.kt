package com.example.fyp_coursework_test.Activities

import com.example.fyp_coursework_test.Utility.CryptoUtil
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fyp_coursework_test.Adapters.ChatAdapter
import com.example.fyp_coursework_test.Models.KeyExchangeModel
import com.example.fyp_coursework_test.Models.MessageModel
import com.example.fyp_coursework_test.R
import com.example.fyp_coursework_test.databinding.ActivityChatDetailsBinding

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import java.util.Date
import javax.crypto.SecretKey

class ChatDetailsActivity : AppCompatActivity() {
    private lateinit var binding : ActivityChatDetailsBinding
    private lateinit var auth : FirebaseAuth
    private lateinit var database : FirebaseDatabase

    private val messageList = ArrayList<MessageModel>()

    private var chatType: Int = 0

    private lateinit var decryptedAesKeyMain: SecretKey

    companion object {
        const val CHAT_TYPE_DIRECT = 1
        const val CHAT_TYPE_GROUP = 2
        const val CHAT_TYPE_ISOC = 3
        const val TAG = "ChatTypeDirect"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("ChatDetailsActivity", "onCreate")

        setContent {
            // Link the Activity to the XML Layout
            binding = ActivityChatDetailsBinding.inflate(layoutInflater)
            setContentView(binding.root)

            // Initialise Firebase Tools
            auth = FirebaseAuth.getInstance()
            database = FirebaseDatabase.getInstance("https://fyp-coursework-test-default-rtdb.europe-west1.firebasedatabase.app")

            // Depending on chat type, different chat logic will be handled
            chatType = intent.getIntExtra("chatType",0)
            when (chatType) {
                CHAT_TYPE_DIRECT -> {chatTypeDirect()}
                CHAT_TYPE_GROUP -> {} // Custom Group Chat will be implemented in the future
                CHAT_TYPE_ISOC -> {chatTypeIsoc()}
            }

            buttonOnClickListeners()
        }
    }


    // Set all the onClickListeners for the buttons
    private fun buttonOnClickListeners() {

        // Navigate back to previous page
        binding.chatDetailsBackButton.setOnClickListener{
            finish()
        }
    }

    // Complete code logic for ISOC chat
    private fun chatTypeIsoc() {
        val senderID = auth.currentUser!!.uid
        val isocId = intent.getStringExtra("isocId")
        val isocName = intent.getStringExtra("isocName")
        val isocProfilePic = intent.getStringExtra("profilePic")

        // fill in the toolbar with correct isoc image and name
        binding.chatDetailsUsername.text = isocName
        Picasso.get().load(isocProfilePic)
            .placeholder(R.drawable.profile_circle_svgrepo_com)
            .into(binding.chatDetailsProfileImage)


        // set the chat recycler view adapter to isoc chat type
        val chatAdapter = ChatAdapter(this, CHAT_TYPE_ISOC, messageList)
        val layoutManager = LinearLayoutManager(this)
        binding.chatDetailsRecyclerView.layoutManager = layoutManager
        binding.chatDetailsRecyclerView.adapter = chatAdapter

        val isocChatRoom = isocId.toString()

        // check the database for all messages in the chat room and load the conversation
        database.reference
            .child("Chats")
            .child("Isoc")
            .child(isocChatRoom)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    try {
                        // Fill newMessages list with all the messages
                        val newMessages = ArrayList<MessageModel>()
                        for (snapshot: DataSnapshot in dataSnapshot.children) {
                            val messageModel = snapshot.getValue(MessageModel::class.java)
                            messageModel?.let {
                                newMessages.add(it)
                            }
                        }

                        // Update message list on the UI thread
                        runOnUiThread {
                            messageList.clear()
                            messageList.addAll(newMessages)
                            chatAdapter.notifyDataSetChanged()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                override fun onCancelled(p0: DatabaseError) {
                    // Handle onCancelled if needed
                }
            })

        // click listener to send messages to the database
        binding.chatDetailsSendMessageButton.setOnClickListener {
            val messageInput = binding.chatDetailsMessageInput.text.toString()
            val timestamp = Date().time

            val messageModel = MessageModel(senderID, messageInput, timestamp)

            binding.chatDetailsMessageInput.text.clear()

            // if message is valid, send to database
            if (messageInput.isNotEmpty()) {
                database.reference
                    .child("Chats")
                    .child("Isoc")
                    .child(isocChatRoom)
                    .push()
                    .setValue(messageModel)
            }
        }

    }

    // Complete code logic for ISOC chat
    private fun chatTypeDirect() {
        val senderID = auth.currentUser!!.uid
        val receiverID = intent.getStringExtra("userID")
        val receiverUsername = intent.getStringExtra("username")
        val receiverProfilePic = intent.getStringExtra("profilePic")

        // fill in the toolbar with correct user image and name
        binding.chatDetailsUsername.text = receiverUsername
        Picasso.get().load(receiverProfilePic)
            .placeholder(R.drawable.profile_circle_svgrepo_com)
            .into(binding.chatDetailsProfileImage)

        // set the chat recycler view adapter to direct chat type
        val chatAdapter = ChatAdapter(this, CHAT_TYPE_DIRECT, messageList)
        val layoutManager = LinearLayoutManager(this)
        binding.chatDetailsRecyclerView.layoutManager = layoutManager
        binding.chatDetailsRecyclerView.adapter = chatAdapter

        // chat room IDs
        val senderRoom = senderID + receiverID
        val receiverRoom = receiverID + senderID

        Log.d("LogChatDetailsActivity", senderRoom)
        Log.d("LogChatDetailsActivity", receiverRoom)

        // Create a chatroom encrpyted on chat initiation
        database.reference
            .child("Chats")
            .child("Direct")
            .child(senderRoom)
            .child("EncryptedKey")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists()) {
                        chatInitiation(receiverID!!, senderRoom, receiverRoom)
                        decryptAndStoreAesKey(senderRoom, receiverRoom)
                        finish()
                        overridePendingTransition(0, 0)
                        startActivity(intent)
                        overridePendingTransition(0, 0)
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    // Handle onCancelled if needed
                }
            })

        decryptAndStoreAesKey(senderRoom, receiverRoom)

//        Log.d(TAG, "chatTypeDirect:$decryptedAesKeyMain ")



        // check the database for all messages in the chat room and load the conversation
        database.reference
            .child("Chats")
            .child("Direct")
            .child(senderRoom)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val newMessages = ArrayList<MessageModel>()
                    for (snapshot: DataSnapshot in dataSnapshot.children) {

                        // Skip EncryptedKey message
                        if (snapshot.key == "EncryptedKey") {
                            continue
                        }

                        val messageModel = snapshot.getValue(MessageModel::class.java)
                        messageModel?.let {
                            // try to Decrypt the message with AES key
                            try {
                                val encryptedMessage = it.message
                                val decryptedMessage = CryptoUtil.decryptAes(encryptedMessage,
                                    decryptedAesKeyMain
                                )
                                it.message = decryptedMessage
                                newMessages.add(it)
                            } catch (e: Exception) {
                                Log.e("DecryptionError", "Error decrypting message: ${e.message}")
                            }

                            }

                    }

                    // Update message list on the UI thread
                    runOnUiThread {
                        messageList.clear()
                        messageList.addAll(newMessages)
                        chatAdapter.notifyDataSetChanged()
                    }
                }

                override fun onCancelled(p0: DatabaseError) {
                    Log.e("FirebaseError", "Firebase operation cancelled: ${p0.message}")
                }
            })


        Log.d("messageList3", messageList.toString())


        // click listener to send encrypted messages to the database
        binding.chatDetailsSendMessageButton.setOnClickListener {
            val messageInput = binding.chatDetailsMessageInput.text.toString()
            val encryptedMessage = CryptoUtil.encryptAes(messageInput, decryptedAesKeyMain)
            val timestamp = Date().time

            val messageModel = MessageModel(senderID, encryptedMessage, timestamp)

            binding.chatDetailsMessageInput.text.clear()

            database.reference
                .child("Chats")
                .child("Direct")
                .child(senderRoom)
                .push()
                .setValue(messageModel).addOnSuccessListener {
                    database.reference
                        .child("Chats")
                        .child("Direct")
                        .child(receiverRoom)
                        .push()
                        .setValue(messageModel)
                }
            }
    }


    // Generate an AES key, encrypt it and send to each other, upon chat initiation
    private fun chatInitiation(receiverID: String, senderRoom: String, receiverRoom: String) {
        val secretKey = CryptoUtil.generateAesKey()

        // Check database
        val usersReference = database.reference.child("Users")
        usersReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Retrieve both users public keys as string
                val senderPublicKeyString = snapshot.child(auth.currentUser!!.uid).child("publicKey").value as? String
                val recipientPublicKeyString = snapshot.child(receiverID).child("publicKey").value as? String

                if (senderPublicKeyString != null && recipientPublicKeyString != null) {
                    // convert public key string to a PublicKey object
                    val senderPublicKey = CryptoUtil.decodeStringToPublicKey(senderPublicKeyString)
                    val recipientPublicKey = CryptoUtil.decodeStringToPublicKey(recipientPublicKeyString)

                    // Encrypt the AES key with both public keys
                    val senderEncryptedKey = CryptoUtil.encryptAesKeyWithPublicKey(secretKey, senderPublicKey)
                    val recipientEncryptedKey = CryptoUtil.encryptAesKeyWithPublicKey(secretKey, recipientPublicKey)

                    // Convert to strings to store in Firebase database
                    val senderEncryptedKeyString = CryptoUtil.encodeKeyToString(senderEncryptedKey)
                    val recipientEncryptedKeyString = CryptoUtil.encodeKeyToString(recipientEncryptedKey)

                    // Store encrypted AES keys in both users' chat rooms
                    sendEncryptedKeyToChatRoom(senderEncryptedKeyString, receiverRoom)
                    sendEncryptedKeyToChatRoom(recipientEncryptedKeyString, senderRoom)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle errors here
            }
        })
    }

    // send aes key to chat room
    private fun sendEncryptedKeyToChatRoom(encryptedKey: String, room: String) {
        val encryptedKeyModel = KeyExchangeModel(encryptedKey = encryptedKey, senderID = auth.currentUser!!.uid)
        database.reference.child("Chats").child("Direct").child(room).child("EncryptedKey").setValue(encryptedKeyModel)
    }

    // decrypt the encrpyted AES key from firebase and store in  android Keystore
    private fun decryptAndStoreAesKey( senderRoom: String, receiverRoom: String) {
        Log.d("DecryptKey", "Attempting to decrypt and store AES key for user: ${auth.currentUser!!.uid}")

        // get user's private from keystore
        val privateKey = CryptoUtil.getPrivateKeyFromKeystore(auth.currentUser!!.uid)
        Log.d("DecryptKey", "Private key retrieved: $privateKey")

        if (privateKey == null) {
            Log.e("DecryptKeyError", "Private Key is null")
            return
        }

        // get the encrypted AES key from chatroom
        database.reference
            .child("Chats")
            .child("Direct")
            .child(receiverRoom).child("EncryptedKey")
            .child("encryptedKey")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val encryptedKeyString = snapshot.value as? String
                    if (encryptedKeyString.isNullOrEmpty()) {
                        Log.e("DecryptKeyError", "Encrypted key string is null or empty")
                        return
                    }

                    // try decrypting the AES key
                    try {
                        val encryptedKey = CryptoUtil.decodeStringToAesKeyByte(encryptedKeyString)
                        val decryptedAesKey = CryptoUtil.decryptAesKeyWithPrivateKey(encryptedKey!!, privateKey)
                        Log.d("DecryptKeySuccess", "AES key decrypted successfully")

                        // Store the decrypted AES key in Android Keystore
                        decryptedAesKeyMain = decryptedAesKey
                        Log.d(TAG, "onDataChange: $decryptedAesKeyMain")

                    } catch (e: Exception) {
                        Log.e("DecryptKeyException", "Error decrypting AES key: ${e.message}")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("FirebaseError", "Firebase operation cancelled: ${error.message}")
                }
            })
    }




}


