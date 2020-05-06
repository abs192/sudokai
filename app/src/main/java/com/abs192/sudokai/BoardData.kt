package com.abs192.sudokai

import kotlin.math.max

class BoardData(var X: Int = 9, var Y: Int = 9) {

    var data: Array<Array<Entry>> = Array(X) {
        Array(Y) {
            Entry(Entry.Type.EMPTY)
        }
    }

    init {
        for (i in 0 until X) {
            for (j in 0 until Y) {
                data[i][j] = Entry(Entry.Type.EMPTY)
            }
        }
////        test Data
//        data[0][0] = Entry(Entry.Type.HARDCODED, 5)
//        data[0][1] = Entry(Entry.Type.HARDCODED, 3)
//        data[0][4] = Entry(Entry.Type.HARDCODED, 7)
//        data[1][0] = Entry(Entry.Type.HARDCODED, 6)
//        data[1][3] = Entry(Entry.Type.HARDCODED, 1)
//        data[1][4] = Entry(Entry.Type.HARDCODED, 9)
//        data[1][5] = Entry(Entry.Type.HARDCODED, 5)
//        data[2][1] = Entry(Entry.Type.HARDCODED, 9)
//        data[2][2] = Entry(Entry.Type.HARDCODED, 8)
//        data[2][7] = Entry(Entry.Type.HARDCODED, 6)
//        data[3][0] = Entry(Entry.Type.HARDCODED, 8)
//        data[3][4] = Entry(Entry.Type.HARDCODED, 6)
//        data[3][8] = Entry(Entry.Type.HARDCODED, 3)
//        data[4][0] = Entry(Entry.Type.HARDCODED, 4)
//        data[4][3] = Entry(Entry.Type.HARDCODED, 8)
//        data[4][5] = Entry(Entry.Type.HARDCODED, 3)
//        data[4][8] = Entry(Entry.Type.HARDCODED, 1)
//        data[5][0] = Entry(Entry.Type.HARDCODED, 7)
//        data[5][4] = Entry(Entry.Type.HARDCODED, 2)
//        data[5][8] = Entry(Entry.Type.HARDCODED, 6)
//        data[6][1] = Entry(Entry.Type.HARDCODED, 6)
//        data[6][6] = Entry(Entry.Type.HARDCODED, 2)
//        data[6][7] = Entry(Entry.Type.HARDCODED, 8)
//        data[7][3] = Entry(Entry.Type.HARDCODED, 4)
//        data[7][4] = Entry(Entry.Type.HARDCODED, 1)
//        data[7][5] = Entry(Entry.Type.HARDCODED, 9)
//        data[7][8] = Entry(Entry.Type.HARDCODED, 5)
//        data[8][4] = Entry(Entry.Type.HARDCODED, 8)
//        data[8][7] = Entry(Entry.Type.HARDCODED, 7)
//        data[8][8] = Entry(Entry.Type.HARDCODED, 9)
    }

    fun putSolution(x: Int, y: Int, num: Int) {
        data[x][y] = Entry(Entry.Type.SOLUTION, num)
    }

    fun editHardCoded(x: Int, y: Int, num: Int) {
        data[x][y] = Entry(Entry.Type.HARDCODED, num)
    }


    fun putEmpty(x: Int, y: Int) {
        data[x][y] = Entry(Entry.Type.EMPTY)
    }

    fun getNum(x: Int, y: Int): Int {
        return data[x][y].num
    }

    fun getSize(): Int {
        return max(X, Y)
    }

    fun isHardcoded(x: Int, y: Int): Boolean {
        return Entry.Type.HARDCODED == data[x][y].flag
    }

    fun isEmpty(x: Int, y: Int): Boolean {
        return Entry.Type.EMPTY == data[x][y].flag
    }

    fun getType(row: Int, col: Int): Entry.Type {
        return data[row][col].flag
    }

    /**
     * Removes all solved entries
     */
    fun refresh() {
        data.forEachIndexed { x, arrayOfEntrys ->
            arrayOfEntrys.forEachIndexed { y, entry ->
                if (entry.flag == Entry.Type.SOLUTION) {
                    data[x][y] = Entry(Entry.Type.EMPTY)
                }
            }
        }
    }

    fun clear() {
        data.forEachIndexed { x, arrayOfEntrys ->
            arrayOfEntrys.forEachIndexed { y, _ ->
                data[x][y] = Entry(Entry.Type.EMPTY)
            }
        }
    }


    fun copyFrom(initB: BoardData) {
        initB.data.forEachIndexed { x, arrayOfE ->
            arrayOfE.forEachIndexed { y, _ ->
                data[x][y] = Entry(initB.getType(x, y), initB.getNum(x, y))
            }
        }
    }

    fun isSolved(): Boolean {
        for (arrayOfEntrys in data.iterator()) {
            for (e in arrayOfEntrys.iterator()) {
                if (e.flag == Entry.Type.EMPTY) {
                    return false
                }
            }
        }
        return true
    }

//    fun clearData() {
//        solidData = Array(X) { kotlin.IntArray(Y) }
//        data = Array(X) { kotlin.IntArray(Y) }
//    }
}