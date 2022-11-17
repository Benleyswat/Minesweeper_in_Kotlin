package Classes

import kotlin.random.Random

//The class for the Minesweeper game's whole board
class Board (width: Int, height: Int, bombs: Int){

    //Sizes:
    //Beginner: 9x9 /w 10 bombs
    //Intermediate: 16x16 /w 40 bombs
    //Hard: 16x30 /w 99 bombs
    private var x: Int = width
    private var y: Int = height
    private var bombs: Int = bombs
    private var flags: Int = bombs //Maybe it can go to the Game class...

    //In this 2d array the bombs will be -1 and the other numbers represent how many bobs are next to them.
    private var boardTiles = arrayOf<Array<Int>>()

    init {
        for (i in 0 until x) {
            var array = arrayOf<Int>()
            for (j in 0 until y) {
                array += 0
            }
            boardTiles += array
        }
    }

    fun generateBombs(xval: Int, yval: Int) {
        var bombinit: Int = bombs
        //In case the two for cycle finishes with fewer bombs
        while (bombinit != 0) {

            var i: Int = Random.nextInt()%(x-1)
            if (i < 0) i = -i
            var j: Int = Random.nextInt()%(y-1)
            if (j < 0) j = -j

            //Making sure the 1st Click is not a bomb
            if (boardTiles[i][j] != -1 && i != xval && j != yval) {
                boardTiles[i][j] = -1
                bombinit -= 1
            }
        }
    }

    fun countBombs() {
        for (i in 0 until x) {
            for (j in 0 until y) {
                var bombsAruond: Int = 0

                if(boardTiles[i][j] != -1){
                    //Top-Left neighbour
                    if (i > 0 && j > 0 && boardTiles[i-1][j-1] == -1) { bombsAruond++ }
                    //Top neighbour
                    if (i > 0 && boardTiles[i-1][j] == -1) { bombsAruond++ }
                    //Top-Right neighbour
                    if (i > 0 && j < y-1 && boardTiles[i-1][j+1] == -1) { bombsAruond++ }
                    //Right neighbour
                    if (j < y-1 && boardTiles[i][j+1] == -1) { bombsAruond++ }
                    //Bottom-Right neighbour
                    if (i < x-1 && j < y-1 && boardTiles[i+1][j+1] == -1) { bombsAruond++ }
                    //Bottom neighbour
                    if (i < x-1 && boardTiles[i+1][j] == -1) { bombsAruond++ }
                    //Bottom-Left neighbour
                    if (i < x-1 && j > 0 && boardTiles[i+1][j-1] == -1 ) { bombsAruond++ }
                    //Left neighbour
                    if (j > 0 && boardTiles[i][j-1] == -1) { bombsAruond++ }
                    boardTiles[i][j] = bombsAruond
                }
            }
        }
    }

    fun getX(): Int {return x}
    fun getY(): Int {return y}
    fun getBombs(): Int {return bombs}
    fun getFlags(): Int {return flags}
    fun setFlags(x: Int) { flags = x}
    fun getBoardTiles(): Array<Array<Int>> {return boardTiles}

}