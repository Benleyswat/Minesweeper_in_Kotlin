import Classes.Board
import Classes.GameStates
import javafx.animation.AnimationTimer
import javafx.application.Application
import javafx.event.EventHandler
import javafx.scene.Group
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.image.Image
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.text.FontWeight
import javafx.stage.Stage
import kotlin.system.exitProcess

class Game : Application() {

    private lateinit var mainScene: Scene
    private var canvas = Canvas(512.0, 512.0)
    private lateinit var graphicsContext: GraphicsContext

    private var palya: Board? = Board(9,9,10)
    private var generate = true
    private var tileSize: Double = 40.0                             //The size of the Images of the Mine Field
    private var palyaSarokX: Double = 31.0                          //The top-left corner of the Field
    private var palyaSarokY: Double = 31.0                          //The top-left corner of the Field

    private lateinit var home: Image
    private lateinit var covered: Image
    private lateinit var zero: Image
    private lateinit var one: Image
    private lateinit var two: Image
    private lateinit var three: Image
    private lateinit var four: Image
    private lateinit var five: Image
    private lateinit var six: Image
    private lateinit var seven: Image
    private lateinit var eight: Image
    private lateinit var bomb: Image
    private lateinit var flag: Image
    private lateinit var notflag: Image
    private var boardTileImages = arrayOf<Array<Image>>()

    private var gameStart: Long = 0
    private var elapsedSecs: Int = 0
    private var elapsedMiliSecs :Int = 0
    private var gameTimeSecs: Int = 0
    private var gameTimeMillis: Int = 0
    private var easyModeTOP5: MutableList<String> = mutableListOf()
    private var intermediateModeTOP5: MutableList<String> = mutableListOf()
    private var hardModeTOP5: MutableList<String> = mutableListOf()

    //"States" of the game
    private var state: GameStates = GameStates.MENU

    // use a set so duplicates are not possible
    private val currentlyActiveClicks = mutableSetOf<MouseButton>()
    private val currentMouse = mutableSetOf<MouseEvent>()
    private var mouseX: Double = 0.0
    private var mouseY: Double = 0.0

    override fun start(mainStage: Stage) {
        mainStage.title = "Minesweeper"

        val root = Group()
        mainScene = Scene(root)
        mainStage.scene = mainScene
        mainStage.isResizable = false

        root.children.add(canvas)

        prepareActionHandlers()

        graphicsContext = canvas.graphicsContext2D

        loadGraphics()

        // Main loop
        object : AnimationTimer() {
            override fun handle(currentNanoTime: Long) {
                tickAndRender(currentNanoTime)
                //Screen Resize for the different games
                when(palya?.getBombs()) {
                    10 -> {mainStage.height = 532.0
                        mainStage.width = 512.0
                        canvas.height = 532.0
                        canvas.width = 512.0}
                    40 -> {mainStage.height = 600.0
                        mainStage.width = 532.0
                        canvas.height = 600.0
                        canvas.width = 532.0}
                    99 -> {mainStage.height = 600.0
                        mainStage.width = 992.0
                        canvas.height = 600.0
                        canvas.width = 992.0}
                }
            }
        }.start()
        mainStage.show()
    }

    private fun prepareActionHandlers() {
        mainScene.onMousePressed = EventHandler { event ->
            currentlyActiveClicks.removeAll(currentlyActiveClicks)
            currentlyActiveClicks.add(event.button)
        }
        mainScene.onMouseReleased = EventHandler { event ->
            currentlyActiveClicks.removeAll(currentlyActiveClicks)
            currentlyActiveClicks.remove(event.button)
        }
        mainScene.onMouseMoved = EventHandler { event ->
            currentMouse.removeAll(currentMouse)
            currentMouse.add(event)
        }
    }

    private fun loadGraphics() {
        // prefixed with / to indicate that the files are
        // in the root of the "resources" folder
        home = Image(getResource("/Home.png"))
        covered = Image(getResource("/Covered.png"))
        zero = Image(getResource("/Zero.png"))
        one = Image(getResource("/One.png"))
        two = Image(getResource("/Two.png"))
        three = Image(getResource("/Three.png"))
        four = Image(getResource("/Four.png"))
        five = Image(getResource("/Five.png"))
        six = Image(getResource("/Six.png"))
        seven = Image(getResource("/Seven.png"))
        eight = Image(getResource("/Eight.png"))
        bomb = Image(getResource("/Bomb.png"))
        flag = Image(getResource("/Flag.png"))
        notflag = Image(getResource("/Not_flag.png"))
    }

    //Initialize the Board Images before every game for the right size
    private fun initBoardImages() {
        for (i in 0 until (palya?.getX() ?: 0)) {
            var array = arrayOf<Image>()
            for (j in 0 until (palya?.getY() ?: 0)) {
                array += covered
            }
            boardTileImages += array
        }
    }

    //Called when the Clicked
    private fun openAllsafeTiles(valx: Int, valy: Int) {
        val tileVal: Int = palya?.getBoardTiles()?.get(valx)?.get(valy) ?: -2         //If no palya it stays unopened
        when(tileVal) {
            //Game lost here
            -1 -> {boardTileImages[valx][valy] = bomb
                state = GameStates.GAMELOST
                gameTimeSecs = elapsedSecs
                gameTimeMillis = elapsedMiliSecs
            }
            //Recursive opening until the border has numbers,
            //the top-roght and right recursion somtimes messes up for OutOfBoundEx..... but it should be checked....
            0 -> {boardTileImages[valx][valy] = zero
                //Opens left
                if (valx > 0 && boardTileImages[valx-1][valy] == covered) {openAllsafeTiles(valx-1, valy)}
                //Opens top-left
                if (valx > 0 && valy > 0 && boardTileImages[valx-1][valy-1] == covered) {openAllsafeTiles(valx-1, valy-1)}
                //Opens top
                if (valy > 0 && boardTileImages[valx][valy-1] == covered) {openAllsafeTiles(valx, valy-1)}
                //Opens top-right
                if (valx < (palya?.getX() ?: 1)-1 && valy > 0) {if(boardTileImages[valx+1][valy-1] == covered) {openAllsafeTiles(valx+1, valy-1)}}
                //Opens right
                if (valx < (palya?.getX() ?: 1)-1) {if(boardTileImages[valx+1][valy] == covered) {openAllsafeTiles(valx+1, valy)}}
                //Opens bottom-right
                if (valx < (palya?.getX() ?: 1)-1 && valy < (palya?.getY() ?: 1)-1) {if (boardTileImages[valx+1][valy+1] == covered) {openAllsafeTiles(valx+1, valy+1)}}
                //Opens bottom
                if (valy < (palya?.getY() ?:1)-1) {if(boardTileImages[valx][valy+1] == covered) {openAllsafeTiles(valx, valy+1)}}
                //Opens botom-left
                if (valx > 0 && valy < (palya?.getY() ?: 1)-1) {if(boardTileImages[valx-1][valy+1] == covered) {openAllsafeTiles(valx-1, valy+1)}}
            }
            //Changes itself from the rest of the Images
            1 -> {boardTileImages[valx][valy] = one}
            2 -> {boardTileImages[valx][valy] = two}
            3 -> {boardTileImages[valx][valy] = three}
            4 -> {boardTileImages[valx][valy] = four}
            5 -> {boardTileImages[valx][valy] = five}
            6 -> {boardTileImages[valx][valy] = six}
            7 -> {boardTileImages[valx][valy] = seven}
            8 -> {boardTileImages[valx][valy] = eight}
        }
        return
    }

    //Adds the Game time to the appropriate List of Times when a player won
    //Also makes the list Descending and only keeping the TOP 5 values
    private fun addWinTimeToHighScoreList() {
        val newTime = "$gameTimeSecs.$gameTimeMillis"
        easyModeTOP5 = mutableListOf("-","-","-","-","-")
        intermediateModeTOP5 = mutableListOf("-","-","-","-","-")
        hardModeTOP5 = mutableListOf("-","-","-","-","-")

        if ((palya?.getBombs() ?: 0) == 10){
            easyModeTOP5.add(newTime)
            easyModeTOP5.sortDescending()
            while (easyModeTOP5.size > 5){ easyModeTOP5.removeLast() }
        }
        else if ((palya?.getBombs() ?: 0) == 40){
            intermediateModeTOP5.add(newTime)
            intermediateModeTOP5.sortDescending()
            while (intermediateModeTOP5.size > 5){ intermediateModeTOP5.removeLast() }
        }
        else if ((palya?.getBombs() ?: 0) == 99){
            hardModeTOP5.add(newTime)
            hardModeTOP5.sortDescending()
            while (hardModeTOP5.size > 5){ hardModeTOP5.removeLast() }
        }
    }

    //Checks if the current Game is ending with a win or not.
    private fun checkIfWin() {
        var goodFlags = 0
        var nonBombsNotCovered = 0
        val nonBombTiles = (palya?.getX() ?: 0) * (palya?.getY() ?: 0) - (palya?.getBombs() ?: 0)
        //Checks if all the bombs have Flags placed on them
        if ((palya?.getFlags() ?: -1) == 0) {
            for (i in 0 until (palya?.getX() ?: 1)) {
                for (j in 0 until (palya?.getY() ?: 1)) {
                    if (palya?.getBoardTiles()?.get(i)?.get(j) == -1 && boardTileImages[i][j] == flag) {goodFlags++}
                }
            }
            if ((palya?.getBombs() ?: -1) == goodFlags) {
                gameTimeSecs = elapsedSecs
                gameTimeMillis = elapsedMiliSecs
                addWinTimeToHighScoreList()
                state = GameStates.GAMEWON
            }
        }
        //Checks if all non bomb Tiles are opened up
        else if (nonBombTiles > 0 && (palya?.getFlags() ?: -1) > 0) {
            for (i in 0 until (palya?.getX() ?: 1)) {
                for (j in 0 until (palya?.getY() ?: 1)) {
                    if (palya?.getBoardTiles()?.get(i)?.get(j) != -1 &&
                        boardTileImages[i][j] != flag && boardTileImages[i][j] != covered) {nonBombsNotCovered++}
                }
            }
            if(nonBombsNotCovered == nonBombTiles) {
                gameTimeSecs = elapsedSecs
                gameTimeMillis = elapsedMiliSecs
                addWinTimeToHighScoreList()
                state = GameStates.GAMEWON
            }
        }
    }

    private fun tickAndRender(currentNanoTime: Long) {
        // the time elapsed in miliSecs, like 7.53 secs or 18.67 secs
        val elapsedGameTime = (currentNanoTime - gameStart)/10000000
        elapsedSecs = (elapsedGameTime/100).toInt()
        elapsedMiliSecs = (elapsedGameTime - elapsedSecs * 100).toInt()

        graphicsContext.font = Font.font("Serif",18.0)
        // clear canvas
        graphicsContext.clearRect(0.0, 0.0, canvas.width, canvas.height)
        // draw background
        graphicsContext.fill = Color.BLACK
        graphicsContext.fillRoundRect(0.0, 0.0, canvas.width, canvas.height, 0.0 ,0.0)

        if (state == GameStates.MENU) {
            graphicsContext.fill = Color.BLUE
            //Start Button
            graphicsContext.fillRoundRect(128.0, 100.0, 256.0, 50.0, 6.0, 6.0)
            //High Scores Button
            graphicsContext.fillRoundRect(128.0, 175.0, 256.0, 50.0, 6.0, 6.0)
            //Exit Button
            graphicsContext.fillRoundRect(128.0, 250.0, 256.0, 50.0, 6.0, 6.0)

            graphicsContext.fill = Color.WHITE
            //Start Text
            graphicsContext.fillText("Start", 238.0, 131.0)
            //High Scores Text
            graphicsContext.fillText("High Scores", 212.0, 206.0)
            //Exit Text
            graphicsContext.fillText("Exit", 240.0, 279.0)
        }

        if (state == GameStates.HIGHSCORES) {
            graphicsContext.fill = Color.WHITE
            //Before any win
            if (easyModeTOP5.size == 0 && intermediateModeTOP5.size == 0 && hardModeTOP5.size == 0){
                graphicsContext.fillText("HighScores available after the 1st Win!", 114.0, 131.0)
            }
            //After 1st Win of the run of App
            else {
                graphicsContext.fillText("Easy", 84.0, 35.0)
                graphicsContext.fillText("Intermediate", 212.0, 35.0)
                graphicsContext.fillText("Hard", 382.0, 35.0)
                for (i in 0..4){
                    graphicsContext.fillText(easyModeTOP5[i], 82.0, 92.0 + i*32)
                    graphicsContext.fillText(intermediateModeTOP5[i], 239.0, 92.0 + i*32)
                    graphicsContext.fillText(hardModeTOP5[i], 379.0, 92.0 + i*32)
                }
            }
            graphicsContext.fill = Color.BLUE
            //Back Button
            graphicsContext.fillRoundRect(128.0, 325.0, 256.0, 50.0, 6.0, 6.0)

            graphicsContext.fill = Color.WHITE
            graphicsContext.font = Font.font("Serif",18.0)
            //Back Text
            graphicsContext.fillText("Back", 246.0, 353.0)
        }

        if (state == GameStates.START) {
            graphicsContext.fill = Color.BLUE
            //Easy Button
            graphicsContext.fillRoundRect(128.0, 100.0, 256.0, 50.0, 6.0, 6.0)
            //Intermediate Button
            graphicsContext.fillRoundRect(128.0, 175.0, 256.0, 50.0, 6.0, 6.0)
            //Hard Button
            graphicsContext.fillRoundRect(128.0, 250.0, 256.0, 50.0, 6.0, 6.0)
            //Back Button
            graphicsContext.fillRoundRect(128.0, 325.0, 256.0, 50.0, 6.0, 6.0)

            graphicsContext.fill = Color.WHITE
            //Easy Text
            graphicsContext.fillText("Easy", 239.0, 131.0)
            //Intermediate Text
            graphicsContext.fillText("Intermediate", 212.0, 206.0)
            //Hard Text
            graphicsContext.fillText("Hard", 240.0, 280.0)
            //Back Text
            graphicsContext.fillText("Back", 240.0, 354.0)
        }

        if (state == GameStates.GAME || state == GameStates.GAMEWON || state == GameStates.GAMELOST) {
            graphicsContext.fill = Color.GREY.darker()
            //BackGround
            graphicsContext.fillRoundRect(0.0, 0.0, canvas.width, canvas.height, 0.0 ,0.0)

            graphicsContext.fill = Color.BLACK
            //Timer & Flag Count BackGround         ??(for some reason at the Intermediate and Hard Difficoulty the Flag Counter is getting placed off)??
            graphicsContext.fillRoundRect(palyaSarokX, 28.0, tileSize*2, tileSize, 0.0, 0.0)
            graphicsContext.fillRoundRect(canvas.width-palyaSarokX-(2*tileSize), 28.0, tileSize*2, tileSize, 3.0, 3.0)

            graphicsContext.font = Font.font("Monospaced",20.0)
            graphicsContext.fill = Color.WHITE
            //Timer & Flag Count text
            if (gameTimeSecs == 0) {
                graphicsContext.fillText("$elapsedSecs.$elapsedMiliSecs",palyaSarokX + (tileSize/3), 28.0 + (tileSize/1.5))
            }
            else {
                graphicsContext.fillText("$gameTimeSecs.$gameTimeMillis",palyaSarokX + (tileSize/3), 28.0 + (tileSize/1.5))
            }
            graphicsContext.fillText(palya?.getFlags().toString(), canvas.width-palyaSarokX-(1.3*tileSize), 28.0 + (tileSize/1.5))

            //Home and Flag image
            graphicsContext.drawImage(home, canvas.width/2 - 50, 28.0, tileSize, tileSize)
            graphicsContext.drawImage(flag, canvas.width/2 + 10, 28.0, tileSize, tileSize)

            //Drawing the actual Mine Field
            for (i in 0 until (palya?.getX() ?: 1)) {
                for (j in 0 until (palya?.getY() ?: 1)) {
                    graphicsContext.drawImage(boardTileImages[i][j], palyaSarokX + i*tileSize, palyaSarokY + j*tileSize, tileSize, tileSize)
                }
            }

            if (state == GameStates.GAMEWON) {
                graphicsContext.fill = Color.BLACK
                graphicsContext.fillRoundRect((canvas.width/2)-120, (canvas.height/2)-60, 236.0, 60.0, 7.0, 7.0)

                graphicsContext.font = Font.font("Monospaced", FontWeight.BOLD,40.0)
                graphicsContext.fill = Color.RED
                graphicsContext.fillText("YOU WON", (canvas.width/2)-100, (canvas.height/2)-20)
            }
            if (state == GameStates.GAMELOST) {
                graphicsContext.fill = Color.BLACK
                graphicsContext.fillRoundRect((canvas.width/2)-120, (canvas.height/2)-60, 236.0, 60.0, 7.0, 7.0)

                graphicsContext.font = Font.font("Monospaced", FontWeight.BOLD,40.0)
                graphicsContext.fill = Color.RED
                graphicsContext.fillText("YOU LOST", (canvas.width/2)-100, (canvas.height/2)-20)
            }
        }
        // check for Clicks and completing their commands
        onClick()
    }

    private fun onClick() {
        if (currentMouse.size >= 1) {
            mouseX = currentMouse.first().x
            mouseY = currentMouse.first().y
        }
        if (currentlyActiveClicks.contains(MouseButton.PRIMARY)) {
            if (state == GameStates.MENU) {
                //Start Button
                if (mouseX in 128.0..384.0 && mouseY in 100.0..150.0) {
                    state = GameStates.START
                    currentlyActiveClicks.removeAll(currentlyActiveClicks)
                }
                //High Scores Button
                else if (mouseX in 128.0..384.0 && mouseY in 175.0..225.0) {
                    state = GameStates.HIGHSCORES
                    currentlyActiveClicks.removeAll(currentlyActiveClicks)
                }
                //Exit Button
                else if (mouseX in 128.0..384.0 && mouseY in 250.0..300.0) {
                    exitProcess(0)
                }
            }
            else if (state == GameStates.HIGHSCORES) {
                //Back Button
                if (mouseX in 128.0..384.0 && mouseY in 325.0..375.0) {
                    state = GameStates.MENU
                    currentlyActiveClicks.removeAll(currentlyActiveClicks)
                }
            }
            else if (state == GameStates.START) {
                //Easy Button
                if (mouseX in 128.0..384.0 && mouseY in 100.0..150.0) {
                    state = GameStates.GAME
                    tileSize = 45.0
                    palyaSarokX = (canvas.width-(9*tileSize))/2
                    palyaSarokY = 77.0
                    palya = Board(9, 9, 10)
                    initBoardImages()
                    gameStart = System.nanoTime()
                    currentlyActiveClicks.removeAll(currentlyActiveClicks)
                }
                //Intermediate Button
                else if (mouseX in 128.0..384.0 && mouseY in 175.0..225.0) {
                    state = GameStates.GAME
                    tileSize = 30.0
                    palyaSarokX = 16.0
                    palyaSarokY = 64.0
                    palya = Board(16, 16, 40)
                    initBoardImages()
                    gameStart = System.nanoTime()
                    currentlyActiveClicks.removeAll(currentlyActiveClicks)
                }
                //Hard Button
                else if (mouseX in 128.0..384.0 && mouseY in 250.0..300.0) {
                    state = GameStates.GAME
                    tileSize = 30.0
                    palyaSarokX = 16.0
                    palyaSarokY = 64.0
                    palya = Board(30, 16, 99)
                    initBoardImages()
                    gameStart = System.nanoTime()
                    currentlyActiveClicks.removeAll(currentlyActiveClicks)
                }
                //Back Button
                else if (mouseX in 128.0..384.0 && mouseY in 325.0..375.0) {
                    state = GameStates.MENU
                    currentlyActiveClicks.removeAll(currentlyActiveClicks)
                }
            }
            else if (state == GameStates.GAME) {
                //Home Button
                if (mouseX in (canvas.width/2 - 50)..(canvas.width/2 - 10) && mouseY in 28.0..73.0) {
                    state = GameStates.MENU
                    palya = Board(9,9,10)
                    generate = true
                    boardTileImages = emptyArray()
                    gameTimeSecs = 0
                    gameTimeMillis = 0
                    gameStart = 0
                    currentlyActiveClicks.removeAll(currentlyActiveClicks)
                }
                //Mine Field
                if (mouseX in palyaSarokX..(palyaSarokX+ (palya?.getX() ?: 0) *tileSize) && mouseY in palyaSarokY..(palyaSarokY+ (palya?.getY()?: 0) *tileSize)) {
                    val i: Int = (mouseX - palyaSarokX).toInt() / tileSize.toInt()
                    val j: Int = (mouseY - palyaSarokY).toInt() / tileSize.toInt()
                    //Not to explode on 1st Clicking
                    if (generate) {
                        palya!!.generateBombs(i, j)
                        palya!!.countBombs()
                        generate = false
                    }
                    if (boardTileImages[i][j] != flag && boardTileImages[i][j] == covered) {
                        openAllsafeTiles(i,j)
                    }
                    checkIfWin()
                    currentlyActiveClicks.removeAll(currentlyActiveClicks)
                }
            }
            else if (state == GameStates.GAMEWON || state == GameStates.GAMELOST) {
                //Home Button
                if (mouseX in (canvas.width/2 - 50)..(canvas.width/2 - 10) && mouseY in 28.0..73.0) {
                    state = GameStates.MENU
                    palya = Board(9,9,10)
                    generate = true
                    boardTileImages = emptyArray()
                    gameTimeSecs = 0
                    gameTimeMillis = 0
                    gameStart = 0
                    currentlyActiveClicks.removeAll(currentlyActiveClicks)
                }
            }
        }
        //Right Mouse Click for placin or removing Flags
        if (currentlyActiveClicks.contains(MouseButton.SECONDARY)) {
            if (state == GameStates.GAME) {
                //If the mouse is on the Field
                if (mouseX in palyaSarokX..(palyaSarokX+ (palya?.getX() ?: 0) *tileSize) && mouseY in palyaSarokY..(palyaSarokY+ (palya?.getY()?: 0) *tileSize)) {
                    val i: Int = (mouseX - palyaSarokX).toInt() / tileSize.toInt()
                    val j: Int = (mouseY - palyaSarokY).toInt() / tileSize.toInt()

                    //If there are flags to place and the tile is covered
                    if ((palya?.getFlags() ?: 0) > 0 && boardTileImages[i][j] == covered) {
                        boardTileImages[i][j] = flag
                        palya?.setFlags(palya!!.getFlags() - 1)
                    }
                    //If there are flags to remove
                    else if ((palya?.getFlags() ?: 0) < (palya?.getBombs() ?: 0) && boardTileImages[i][j] == flag) {
                        boardTileImages[i][j] = covered
                        palya?.setFlags(palya!!.getFlags() + 1)
                    }
                    checkIfWin()
                    currentlyActiveClicks.removeAll(currentlyActiveClicks)
                }
            }
        }

    }

}
