package com.abs192.sudokai

import android.app.Dialog
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Point
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.abs192.sudokai.storage.Storage
import com.abs192.sudokai.views.GridCanvas
import com.abs192.sudokai.views.RVGridNumbersViewAdapter
import com.abs192.sudokai.views.RVGridViewAdapter
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*

class ResultActivity : AppCompatActivity() {

    private lateinit var classifier: Classifier

    private var resultLayout: ConstraintLayout? = null
    private var rvGrid: RecyclerView? = null
    private var rvGridNumbers: RecyclerView? = null
    private var gridCanvas: GridCanvas? = null
    private var progressBar: ProgressBar? = null
    private var buttonSolve: Button? = null
    private var buttonRefresh: Button? = null
    private var buttonEdit: ToggleButton? = null
    private var buttonShowImage: Button? = null
    private val solver = Solver()
    private val initBoardData = BoardData()

    private val storage = Storage(this)

    private var imageBitmap: Bitmap? = null

    private val mModelPath = "model.tflite"
    private val mLabelPath = "labels.txt"

    private var cvSudoku: CVSudoku? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        supportActionBar?.hide()
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_NOSENSOR

        setContentView(R.layout.activity_result)
        resultLayout = findViewById(R.id.resultLayout)
        buttonSolve = findViewById(R.id.buttonSolve)
        progressBar = findViewById(R.id.progressBar)
        buttonEdit = findViewById(R.id.buttonEdit)
        buttonRefresh = findViewById(R.id.buttonRefresh)
        buttonShowImage = findViewById(R.id.buttonShowImg)

        gridCanvas = findViewById(R.id.gridCanvas)

        rvGrid = findViewById(R.id.rvGrid)
        rvGridNumbers = findViewById(R.id.rvGridNumbers)

        imageBitmap = storage.getImage()

        classifier = Classifier(assets, mModelPath, mLabelPath)

        cvSudoku = imageBitmap?.let { CVSudoku(it, classifier, storage) }
        val boardData = initializeBoard(cvSudoku)!!
        initBoardData.copyFrom(boardData)

        rvGridNumbers?.layoutManager = GridLayoutManager(this, boardData.getSize())
        val gridAdapter = RVGridViewAdapter(this, boardData.getSize())
        rvGridNumbers?.adapter = gridAdapter

        rvGrid?.layoutManager = GridLayoutManager(this, boardData.getSize())
        val gridNumbersAdapter = RVGridNumbersViewAdapter(this, boardData)
        rvGrid?.adapter = gridNumbersAdapter

        buttonSolve?.setOnClickListener {
            progressBar?.visibility = View.VISIBLE
            enableDisableButtons(false, buttonSolve, buttonRefresh, buttonEdit, buttonShowImage)
            Thread(Runnable {
                solver.solveBoard(boardData)
                runOnUiThread {
                    gridNumbersAdapter.notifyDataSetChanged()
                    rvGridNumbers?.invalidate()
                    progressBar?.visibility = View.INVISIBLE
                    enableDisableButtons(
                        true,
                        buttonSolve,
                        buttonRefresh,
                        buttonEdit,
                        buttonShowImage
                    )
                    checkForError(boardData)
                }
            }).start()
        }

        buttonRefresh?.setOnClickListener {
            boardData.copyFrom(initBoardData)
            progressBar?.visibility = View.VISIBLE
            enableDisableButtons(false, buttonSolve, buttonRefresh, buttonEdit, buttonShowImage)
            Thread(Runnable {
                boardData.refresh()
                runOnUiThread {
                    gridNumbersAdapter.notifyDataSetChanged()
                    progressBar?.visibility = View.INVISIBLE
                    enableDisableButtons(
                        true,
                        buttonSolve,
                        buttonRefresh,
                        buttonEdit,
                        buttonShowImage
                    )
                }
            }).start()
        }

        buttonEdit?.setOnClickListener {
            if (buttonEdit?.isChecked!!) {
                resultLayout?.setBackgroundColor(
                    Color.GRAY
                )
            } else {
                resultLayout?.setBackgroundColor(
                    ContextCompat.getColor(
                        this@ResultActivity,
                        R.color.colorAccentDark
                    )
                )
            }
            gridNumbersAdapter.editMode(buttonEdit?.isChecked!!)
            enableDisableButtons(
                !(buttonEdit?.isChecked!!),
                buttonSolve,
                buttonRefresh,
                buttonShowImage
            )
        }

        buttonShowImage?.setOnClickListener {
            showImageDialog(cvSudoku?.bitmap!!)
        }
    }

    private fun initializeBoard(board: CVSudoku?): BoardData? {
        return board?.generatedBoardData()
    }

    fun showImageDialog(bitmap: Bitmap) {
        val builder = Dialog(this)
        builder.requestWindowFeature(Window.FEATURE_NO_TITLE);
        builder.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val imageView = ImageView(this)
        imageView.setImageBitmap(bitmap)
        builder.addContentView(
            imageView, RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
        builder.show()
    }


    private fun checkForError(boardData: BoardData) {
        if (!boardData.isSolved()) {
            resultLayout?.let {
                Snackbar
                    .make(
                        it,
                        "Invalid sudoku. Can't be solved.",
                        Snackbar.LENGTH_LONG
                    )
                    .show()
            }
        } else {
            resultLayout?.let {
                Snackbar
                    .make(it, "Solved.", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    fun enableDisableButtons(enable: Boolean, vararg buttons: View?) {
        buttons.forEach {
            it?.isEnabled = enable
        }
    }

}
