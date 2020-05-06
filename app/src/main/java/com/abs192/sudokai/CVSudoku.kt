package com.abs192.sudokai

import android.graphics.Bitmap
import android.graphics.Rect
import android.util.Log
import com.abs192.sudokai.cvHelpers.CVImageHelper
import com.abs192.sudokai.cvHelpers.CV2CanvasUtil
import com.abs192.sudokai.storage.Storage
import java.lang.Exception

class CVSudoku(var bitmap: Bitmap, private var classifer: Classifier, private var store: Storage) {

    private var tag: String = this.javaClass.simpleName

    var squares = ArrayList<Bitmap>()

    val results = ArrayList<String>()

    var boardBitmap: Bitmap? = null

    var isPresent = false
    var toMove = "w"
    private val cvImageHelper = CVImageHelper()

    init {
        bitmap = cvImageHelper.preprocess(bitmap)
//        boardBitmap = cvImageHelper.getBoard(bitmap)
//        boardBitmap?.let { squares = cvImageHelper.getSquares(it) }
        squares = cvImageHelper.getSquares(bitmap)
        Log.d(tag, "Squares ${squares.size}")
        classifySquares()
    }

    private fun classifySquares() {
        results.clear()
        squares.forEachIndexed { i, bitmap ->
            val x = i / 9
            val y = i % 9

            store.saveSquareImage(bitmap, x, y)
            Log.d(tag, "Saving square $x $y")
            val result = classifer.recognizeImage(bitmap)
            try {
                val pred = result[0].title
                Log.d(this.javaClass.name, "result $i $pred")
                results.add(pred)
            } catch (e: Exception) {
                results.add("-")
            }
        }
    }

    fun generatedBoardData(): BoardData {
        val boardData = BoardData()
        boardData.clear()
        results.forEachIndexed { index, s ->
            val row = index / 9
            val col = index % 9

            Log.d("CVSudko", "result board $index $s")
            if (s == "-") {
                boardData.putEmpty(row, col)
                Log.d("a", "empty")
            } else {
                try {
                    boardData.editHardCoded(row, col, s.toInt())
                    Log.d("a", "${s.toInt()}")
                } catch (e: NumberFormatException) {
                    boardData.putEmpty(row, col)
                    Log.d("a", "empty")
                }
            }
        }
        return boardData
    }
}
