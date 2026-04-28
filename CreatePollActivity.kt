package com.example.caquickpoll

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import org.json.JSONArray

class CreatePollActivity : AppCompatActivity() {

    private lateinit var container: LinearLayout
    private lateinit var addBtn: Button
    private lateinit var createBtn: Button
    private lateinit var aiBtn: Button
    private lateinit var expiryInput: EditText

    private lateinit var pollImage: ImageView
    private lateinit var uploadBtn: Button
    private var imageUri: Uri? = null

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val questionViews = ArrayList<LinearLayout>()

    private val imagePicker =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                imageUri = uri
                pollImage.setImageURI(uri)
            }
        }

    private val aiLauncher =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->

            if (result.resultCode == RESULT_OK) {

                val data = result.data?.getStringExtra("ai_data")

                if (data.isNullOrEmpty()) {
                    toast("Failed to apply AI ❌")
                    return@registerForActivityResult
                }

                try {
                    val array = JSONArray(data)

                    container.removeAllViews()
                    questionViews.clear()

                    for (i in 0 until array.length()) {

                        val obj = array.getJSONObject(i)
                        val question = obj.getString("question")

                        val optionsJson = obj.getJSONArray("options")
                        val options = mutableListOf<String>()

                        for (j in 0 until optionsJson.length()) {
                            options.add(optionsJson.getString(j))
                        }

                        addQuestion(question, options)
                    }

                    toast("AI Applied ✨")

                } catch (e: Exception) {
                    e.printStackTrace()
                    toast("Failed to apply AI ❌")
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_poll)

        container = findViewById(R.id.questionContainer)
        addBtn = findViewById(R.id.addQuestionBtn)
        createBtn = findViewById(R.id.createBtn)
        aiBtn = findViewById(R.id.aiBtn)
        expiryInput = findViewById(R.id.expiryTime)

        pollImage = findViewById(R.id.pollImage)
        uploadBtn = findViewById(R.id.uploadImageBtn)

        uploadBtn.setOnClickListener {
            imagePicker.launch("image/*")
        }

        val pollId = intent.getStringExtra("pollId")

        if (pollId != null) {
            loadPollForEdit(pollId)
        } else {
            addQuestion()
        }

        addBtn.setOnClickListener {
            if (questionViews.size < 10) addQuestion()
            else toast("Max 10 questions")
        }

        createBtn.setOnClickListener {
            showNameDialog { name, type ->
                savePoll(name, type)
            }
        }

        aiBtn.setOnClickListener {
            val intent = Intent(this, AiChatActivity::class.java)
            aiLauncher.launch(intent)
        }
    }

    private fun loadPollForEdit(id: String) {

        db.collection("Polls").document(id).get()
            .addOnSuccessListener {

                val poll = it.toObject(Poll::class.java) ?: return@addOnSuccessListener

                container.removeAllViews()
                questionViews.clear()

                for (q in poll.questions) {

                    val question = q["question"]?.toString() ?: ""

                    val optionsRaw = q["options"] as? List<*> ?: emptyList<Any>()
                    val options = mutableListOf<String>()

                    for (item in optionsRaw) {
                        options.add(item.toString())
                    }

                    addQuestion(question, options)
                }


                if (!poll.imageUrl.isNullOrEmpty()) {
                    com.bumptech.glide.Glide.with(this)
                        .load(poll.imageUrl)
                        .into(pollImage)
                }
            }
            .addOnFailureListener {
                toast("Failed to load poll ❌")
            }
    }

    private fun addQuestion(questionText: String = "", optionsList: List<String> = listOf()) {

        val view = layoutInflater.inflate(R.layout.item_question, container, false) as LinearLayout

        val title = view.findViewById<TextView>(R.id.questionTitle)
        val questionEdit = view.findViewById<EditText>(R.id.question)
        val optionsContainer = view.findViewById<LinearLayout>(R.id.optionsContainer)
        val addOptionBtn = view.findViewById<TextView>(R.id.addOptionBtn)
        val removeQuestionBtn = view.findViewById<TextView>(R.id.removeQuestionBtn)

        title.text = "Question ${questionViews.size + 1}"
        questionEdit.setText(questionText)

        optionsContainer.removeAllViews()

        if (optionsList.isNotEmpty()) {
            for (opt in optionsList) {
                val optionView = createOptionView(optionsContainer)
                optionView.findViewById<EditText>(R.id.optionText).setText(opt)
                optionsContainer.addView(optionView)
            }
        } else {
            optionsContainer.addView(createOptionView(optionsContainer))
            optionsContainer.addView(createOptionView(optionsContainer))
        }

        addOptionBtn.setOnClickListener {
            optionsContainer.addView(createOptionView(optionsContainer))
        }

        removeQuestionBtn.setOnClickListener {
            if (questionViews.size > 1) {
                container.removeView(view)
                questionViews.remove(view)
            } else {
                toast("Minimum 1 question required")
            }
        }

        container.addView(view)
        questionViews.add(view)
    }

    private fun createOptionView(container: LinearLayout): LinearLayout {

        val optionView = layoutInflater.inflate(R.layout.item_option, container, false) as LinearLayout

        val remove = optionView.findViewById<ImageView>(R.id.removeOption)
        val camera = optionView.findViewById<ImageView>(R.id.addImage)

        remove.setOnClickListener {
            if (container.childCount > 2) {
                container.removeView(optionView)
            } else {
                toast("Minimum 2 options required")
            }
        }

        camera.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivity(Intent.createChooser(intent, "Select Image"))
        }

        return optionView
    }


    private fun uploadImage(uri: Uri, callback: (String) -> Unit) {
        val ref = FirebaseStorage.getInstance().reference
            .child("poll_images/${System.currentTimeMillis()}")

        ref.putFile(uri)
            .continueWithTask { ref.downloadUrl }
            .addOnSuccessListener { url ->
                callback(url.toString())
            }
            .addOnFailureListener {
                toast("Image upload failed ❌")
            }
    }

    private fun savePoll(name: String, type: String) {

        val list = ArrayList<HashMap<String, Any>>()

        for (view in questionViews) {

            val q = view.findViewById<EditText>(R.id.question).text.toString()
            val optionsContainer = view.findViewById<LinearLayout>(R.id.optionsContainer)

            val options = ArrayList<String>()
            val votes = ArrayList<Int>()

            for (i in 0 until optionsContainer.childCount) {
                val text = optionsContainer.getChildAt(i)
                    .findViewById<EditText>(R.id.optionText).text.toString()

                if (text.isNotEmpty()) {
                    options.add(text)
                    votes.add(0)
                }
            }

            if (q.isNotEmpty() && options.size >= 2) {
                list.add(hashMapOf("question" to q, "options" to options, "votes" to votes))
            }
        }

        val expiry = System.currentTimeMillis() +
                ((expiryInput.text.toString().toLongOrNull() ?: 24) * 3600000)


        if (imageUri != null) {

            uploadImage(imageUri!!) { imageUrl ->

                db.collection("Polls").add(
                    hashMapOf(
                        "pollName" to name,
                        "createdBy" to auth.currentUser?.uid,
                        "questions" to list,
                        "imageUrl" to imageUrl,
                        "expiryTime" to expiry,
                        "type" to type
                    )
                ).addOnSuccessListener {
                    toast("Saved 🎉")
                    finish()
                }
            }

        } else {

            db.collection("Polls").add(
                hashMapOf(
                    "pollName" to name,
                    "createdBy" to auth.currentUser?.uid,
                    "questions" to list,
                    "imageUrl" to "",
                    "expiryTime" to expiry,
                    "type" to type
                )
            ).addOnSuccessListener {
                toast("Saved 🎉")
                finish()
            }
        }
    }

    private fun showNameDialog(callback: (String, String) -> Unit) {
        val input = EditText(this)

        android.app.AlertDialog.Builder(this)
            .setTitle("Poll Name")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                callback(input.text.toString(), "published")
            }
            .setNeutralButton("Draft") { _, _ ->
                callback(input.text.toString(), "draft")
            }
            .show()
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}