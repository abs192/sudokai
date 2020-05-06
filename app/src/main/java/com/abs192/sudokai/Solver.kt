package com.abs192.sudokai

import java.util.*
import kotlin.math.sqrt

class Solver {

    private val tag = "Suduko Solver TAG"

    private val allowedBitFields = intArrayOf(
        0,
        1,
        1 shl 1,
        1 shl 2,
        1 shl 3,
        1 shl 4,
        1 shl 5,
        1 shl 6,
        1 shl 7,
        1 shl 8
    )
    private val allAllowed = arraySum(allowedBitFields)

    private fun arraySum(array: IntArray): Int {
        var sum = 0
        for (value in array) {
            sum += value
        }
        return sum
    }

//    private fun isSafe(
//        board: BoardData,
//        row: Int, col: Int,
//        num: Int
//    ): Boolean {
//        //row
//        for (d in board.data.indices) {
//            if (board.getNum(row, d) == num) {
//                return false
//            }
//        }
//        //column
//        for (r in board.data.indices) {
//            if (board.getNum(r, col) == num) {
//                return false
//            }
//        }
//        //box
//        val sqrt = sqrt(board.data.size.toDouble()).toInt()
//        val boxRowStart = row - row % sqrt
//        val boxColStart = col - col % sqrt
//        for (r in boxRowStart until boxRowStart + sqrt) {
//            for (d in boxColStart until boxColStart + sqrt) {
//                if (board.getNum(r, d) == num) {
//                    return false
//                }
//            }
//        }
//        return true
//    }
//
//    fun solveSudoku(boardData: BoardData): Boolean {
//
//        var size = boardData.getSize()
//        var row = -1
//        var col = -1
//        var isEmpty = true
//        for (i in 0 until size) {
//            for (j in 0 until size) {
////                Log.d(tag, "$i $j ${boardData.data[i][j]}")
//                if (boardData.isEmpty(i, j)) {
//                    row = i
//                    col = j
//                    isEmpty = false
//                    break
//                }
//            }
//            if (!isEmpty) {
//                break
//            }
//        }
//        // no empty space left
//        if (isEmpty) {
//            return true
//        }
//        // else for each-row backtrack
//        for (num in 1..size) {
//            if (isSafe(boardData, row, col, num)) {
//                boardData.putSolution(row, col, num)
//                if (solveSudoku(boardData)) {
//                    return true
//                } else {
//                    boardData.putEmpty(row, col)
//                }
//            }
//        }
//        return false
//    }

    fun solveBoard(board: BoardData): Boolean {
        val boardDataArray =
            Array(9) { IntArray(9) }
        for (i in 0 until 9) {
            for (j in 0 until 9) {
                boardDataArray[i][j] = board.getNum(i, j)
            }
        }
        solveBoard(boardDataArray)
        try {
            for (i in 0 until 9) {
                for (j in 0 until 9) {
                    if (board.isEmpty(i, j)) {
                        board.putSolution(i, j, boardDataArray[i][j])
                    }
                }
            }
        } catch (e: ExceptionInInitializerError) {
            return false
        }
        return true
    }

    fun solveBoard(board: Array<IntArray>): Int {
        val allowedValues =
            Array(9) { IntArray(9) }
        var placedNumberCount = 0
        for (allowedValuesRow in allowedValues) {
            Arrays.fill(allowedValuesRow, this.allAllowed)
        }
        for (x in 0..8) {
            for (y in 0..8) {
                if (board[x][y] > 0) {
                    allowedValues[x][y] = 0
                    applyAllowedValuesMask(board, allowedValues, x, y)
                    placedNumberCount++
                }
            }
        }
        return solveBoard(board, allowedValues, placedNumberCount)
    }

    private fun solveBoard(
        board: Array<IntArray>,
        allowedValues: Array<IntArray>,
        placedNumberCount: Int
    ): Int {
        var placedNumberCount = placedNumberCount
        var lastPlacedNumbersCount = 0
        while (placedNumberCount - lastPlacedNumbersCount > 3 && placedNumberCount < 68 && placedNumberCount > 10) {
            lastPlacedNumbersCount = placedNumberCount
            placedNumberCount += moveNothingElseAllowed(board, allowedValues)
            placedNumberCount += moveNoOtherRowOrColumnAllowed(board, allowedValues)
            placedNumberCount += moveNothingElseAllowed(board, allowedValues)
            if (placedNumberCount < 35) {
                applyNakedPairs(allowedValues)
                applyLineCandidateConstraints(allowedValues)
            }
        }
        if (placedNumberCount < 81) {
            val bruteForcedBoard =
                attemptBruteForce(board, allowedValues, placedNumberCount)
            if (bruteForcedBoard != null) {
                placedNumberCount = 0
                for (x in 0..8) {
                    for (y in 0..8) {
                        board[x][y] = bruteForcedBoard[x][y]
                        if (bruteForcedBoard[x][y] > 0) {
                            placedNumberCount++
                        }
                    }
                }
            }
        }
        return placedNumberCount
    }

    private fun attemptBruteForce(
        board: Array<IntArray>,
        allowedValues: Array<IntArray>,
        placedNumberCount: Int
    ): Array<IntArray>? {
        for (x in 0..8) {
            val allowedValuesRow = allowedValues[x]
            val boardRow = board[x]
            for (y in 0..8) {
                if (boardRow[y] == 0) {
                    for (value in 1..9) {
                        if (allowedValuesRow[y] and this.allowedBitFields[value] > 0) {
                            val testBoard =
                                copyGameMatrix(board)
                            val testAllowedValues =
                                copyGameMatrix(allowedValues)
                            setValue(testBoard, testAllowedValues, value, x, y)
                            val placedNumbers =
                                solveBoard(testBoard, testAllowedValues, placedNumberCount + 1)
                            if (placedNumbers == 81) {
                                return testBoard
                            }
                        }
                    }
                    return null
                }
            }
        }
        return null
    }

    private fun moveNoOtherRowOrColumnAllowed(
        board: Array<IntArray>,
        allowedValues: Array<IntArray>
    ): Int {
        var moveCount = 0
        for (value in 1..9) {
            val allowedBitField = this.allowedBitFields[value]
            for (x in 0..8) {
                var allowedY = -1
                val allowedValuesRow = allowedValues[x]
                for (y in 0..8) {
                    if (allowedValuesRow[y] and allowedBitField > 0) {
                        if (allowedY < 0) {
                            allowedY = y
                        } else {
                            allowedY = -1
                            break
                        }
                    }
                }
                if (allowedY >= 0) {
                    setValue(board, allowedValues, value, x, allowedY)
                    moveCount++
                }
            }
            for (y in 0..8) {
                var allowedX = -1
                for (x in 0..8) {
                    if (allowedValues[x][y] and allowedBitField > 0) {
                        if (allowedX < 0) {
                            allowedX = x
                        } else {
                            allowedX = -1
                            break
                        }
                    }
                }
                if (allowedX >= 0) {
                    setValue(board, allowedValues, value, allowedX, y)
                    moveCount++
                }
            }
        }
        return moveCount
    }

    private fun moveNothingElseAllowed(
        board: Array<IntArray>,
        allowedValues: Array<IntArray>
    ): Int {
        var moveCount = 0
        for (x in 0..8) {
            val allowedValuesRow = allowedValues[x]
            for (y in 0..8) {
                val currentAllowedValues = allowedValuesRow[y]
                if (countSetBits(currentAllowedValues) == 1) {
                    setValue(board, allowedValues, getLastSetBitIndex(currentAllowedValues), x, y)
                    moveCount++
                }
            }
        }
        return moveCount
    }

    private fun applyNakedPairs(allowedValues: Array<IntArray>) {
        for (x in 0..8) {
            for (y in 0..8) {
                val value = allowedValues[x][y]
                if (countSetBits(value) == 2) {
                    for (scanningY in y + 1..8) {
                        if (allowedValues[x][scanningY] == value) {
                            val removeMask = value.inv()
                            for (applyY in 0..8) {
                                if (applyY != y && applyY != scanningY) {
                                    allowedValues[x][applyY] =
                                        allowedValues[x][applyY] and removeMask
                                }
                            }
                        }
                    }
                }
            }
        }
        for (y in 0..8) {
            for (x in 0..8) {
                val value = allowedValues[x][y]
                if (value != 0 && countSetBits(value) == 2) {
                    for (scanningX in x + 1..8) {
                        if (allowedValues[scanningX][y] == value) {
                            val removeMask = value.inv()
                            for (applyX in 0..8) {
                                if (applyX != x && applyX != scanningX) {
                                    allowedValues[applyX][y] =
                                        allowedValues[applyX][y] and removeMask
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun applyLineCandidateConstraints(allowedValues: Array<IntArray>) {
        for (value in 1..9) {
            val valueMask = this.allowedBitFields[value]
            val valueRemoveMask = valueMask.inv()
            val sectionAvailabilityColumn = IntArray(9)
            for (x in 0..8) {
                for (y in 0..8) {
                    if (allowedValues[x][y] and valueMask != 0) {
                        sectionAvailabilityColumn[x] =
                            sectionAvailabilityColumn[x] or (1 shl y / 3)
                    }
                }
                if (x == 2 || x == 5 || x == 8) {
                    for (scanningX in x - 2..x) {
                        val bitCount = countSetBits(sectionAvailabilityColumn[scanningX])
                        if (bitCount == 1) {
                            for (applyX in x - 2..x) {
                                if (scanningX != applyX) {
                                    for (applySectionY in 0..2) {
                                        if (sectionAvailabilityColumn[scanningX] and (1 shl applySectionY) != 0) {
                                            for (applyY in applySectionY * 3 until (applySectionY + 1) * 3) {
                                                allowedValues[applyX][applyY] =
                                                    allowedValues[applyX][applyY] and valueRemoveMask
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        if (bitCount == 2 && scanningX < x) {
                            for (scanningSecondPairX in scanningX + 1..x) {
                                if (sectionAvailabilityColumn[scanningX] == sectionAvailabilityColumn[scanningSecondPairX]
                                ) {
                                    val applyX: Int
                                    applyX = if (scanningSecondPairX != x) {
                                        x
                                    } else if (scanningSecondPairX - scanningX > 1) {
                                        scanningSecondPairX - 1
                                    } else {
                                        scanningX - 1
                                    }
                                    for (applySectionY in 0..2) {
                                        if (sectionAvailabilityColumn[scanningX] and (1 shl applySectionY) != 0) {
                                            for (applyY in applySectionY * 3 until (applySectionY + 1) * 3) {
                                                allowedValues[applyX][applyY] =
                                                    allowedValues[applyX][applyY] and valueRemoveMask
                                            }
                                        }
                                    }
                                    break
                                }
                            }
                        }
                    }
                }
            }
            val sectionAvailabilityRow = IntArray(9)
            for (y in 0..8) {
                for (x in 0..8) {
                    if (allowedValues[x][y] and valueMask != 0) {
                        sectionAvailabilityRow[y] =
                            sectionAvailabilityRow[y] or (1 shl x / 3)
                    }
                }
                if (y == 2 || y == 5 || y == 8) {
                    for (scanningY in y - 2..y) {
                        val bitCount = countSetBits(sectionAvailabilityRow[scanningY])
                        if (bitCount == 1) {
                            for (applyY in y - 2..y) {
                                if (scanningY != applyY) {
                                    for (applySectionX in 0..2) {
                                        if (sectionAvailabilityRow[scanningY] and (1 shl applySectionX) != 0) {
                                            for (applyX in applySectionX * 3 until (applySectionX + 1) * 3) {
                                                allowedValues[applyX][applyY] =
                                                    allowedValues[applyX][applyY] and valueRemoveMask
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        if (bitCount == 2 && scanningY < y) {
                            for (scanningSecondPairY in scanningY + 1..y) {
                                if (sectionAvailabilityRow[scanningY] == sectionAvailabilityRow[scanningSecondPairY]
                                ) {
                                    val applyY: Int
                                    applyY = if (scanningSecondPairY != y) {
                                        y
                                    } else if (scanningSecondPairY - scanningY > 1) {
                                        scanningSecondPairY - 1
                                    } else {
                                        scanningY - 1
                                    }
                                    for (applySectionX in 0..2) {
                                        if (sectionAvailabilityRow[scanningY] and (1 shl applySectionX) != 0) {
                                            for (applyX in applySectionX * 3 until (applySectionX + 1) * 3) {
                                                allowedValues[applyX][applyY] =
                                                    allowedValues[applyX][applyY] and valueRemoveMask
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun setValue(
        board: Array<IntArray>,
        allowedValues: Array<IntArray>,
        value: Int,
        x: Int,
        y: Int
    ) {
        board[x][y] = value
        allowedValues[x][y] = 0
        applyAllowedValuesMask(board, allowedValues, x, y)
    }

    private fun getLastSetBitIndex(value: Int): Int {
        var value = value
        var bitIndex = 0
        while (value > 0) {
            bitIndex++
            value = value shr 1
        }
        return bitIndex
    }

    private fun applyAllowedValuesMask(
        board: Array<IntArray>,
        allowedValues: Array<IntArray>, x: Int, y: Int
    ) {
        val mask = this.allowedBitFields[board[x][y]].inv()
        for (maskApplyX in 0..8) {
            allowedValues[maskApplyX][y] = allowedValues[maskApplyX][y] and mask
        }
        val allowedValuesRow = allowedValues[x]
        for (maskApplyY in 0..8) {
            allowedValuesRow[maskApplyY] = allowedValuesRow[maskApplyY] and mask
        }
        var sectionX1 = 0
        var sectionX2 = 0
        when (x) {
            0 -> {
                sectionX1 = x + 1
                sectionX2 = x + 2
            }
            1 -> {
                sectionX1 = x - 1
                sectionX2 = x + 1
            }
            2 -> {
                sectionX1 = x - 2
                sectionX2 = x - 1
            }
            3 -> {
                sectionX1 = x + 1
                sectionX2 = x + 2
            }
            4 -> {
                sectionX1 = x - 1
                sectionX2 = x + 1
            }
            5 -> {
                sectionX1 = x - 2
                sectionX2 = x - 1
            }
            6 -> {
                sectionX1 = x + 1
                sectionX2 = x + 2
            }
            7 -> {
                sectionX1 = x - 1
                sectionX2 = x + 1
            }
            8 -> {
                sectionX1 = x - 2
                sectionX2 = x - 1
            }
        }
        var sectionY1 = 0
        var sectionY2 = 0
        when (y) {
            0 -> {
                sectionY1 = y + 1
                sectionY2 = y + 2
            }
            1 -> {
                sectionY1 = y - 1
                sectionY2 = y + 1
            }
            2 -> {
                sectionY1 = y - 2
                sectionY2 = y - 1
            }
            3 -> {
                sectionY1 = y + 1
                sectionY2 = y + 2
            }
            4 -> {
                sectionY1 = y - 1
                sectionY2 = y + 1
            }
            5 -> {
                sectionY1 = y - 2
                sectionY2 = y - 1
            }
            6 -> {
                sectionY1 = y + 1
                sectionY2 = y + 2
            }
            7 -> {
                sectionY1 = y - 1
                sectionY2 = y + 1
            }
            8 -> {
                sectionY1 = y - 2
                sectionY2 = y - 1
            }
        }
        val allowedValuesRow1 = allowedValues[sectionX1]
        val allowedValuesRow2 = allowedValues[sectionX2]
        allowedValuesRow1[sectionY1] = allowedValuesRow1[sectionY1] and mask
        allowedValuesRow1[sectionY2] = allowedValuesRow1[sectionY2] and mask
        allowedValuesRow2[sectionY1] = allowedValuesRow2[sectionY1] and mask
        allowedValuesRow2[sectionY2] = allowedValuesRow2[sectionY2] and mask
    }


    private fun copyGameMatrix(matrix: Array<IntArray>): Array<IntArray> {
        return arrayOf(
            matrix[0].copyOf(9),
            matrix[1].copyOf(9),
            matrix[2].copyOf(9),
            matrix[3].copyOf(9),
            matrix[4].copyOf(9),
            matrix[5].copyOf(9),
            matrix[6].copyOf(9),
            matrix[7].copyOf(9),
            matrix[8].copyOf(9)
        )
    }

    private fun countSetBits(value: Int): Int {
        var value = value
        var count = 0
        while (value > 0) {
            value = value and value - 1
            count++
        }
        return count
    }
}