package com.puzzle.dcs

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.Screen
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.*
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.*
import com.badlogic.gdx.scenes.scene2d.Stage
import com.google.gson.Gson
import com.badlogic.gdx.physics.box2d.FixtureDef
import kotlin.math.*
import com.badlogic.gdx.physics.box2d.Fixture
import com.badlogic.gdx.physics.box2d.RayCastCallback
import com.badlogic.gdx.physics.box2d.joints.DistanceJoint
import com.badlogic.gdx.physics.box2d.joints.DistanceJointDef
import com.puzzle.dcs.Core.Companion.gridSize2
import com.puzzle.dcs.Core.Companion.halfGrid2
import com.puzzle.dcs.type.*
import com.puzzle.dcs.util.*

class PlayScreen(private val game: Core, fileName: String) : Screen {
    private val camera: OrthographicCamera
    private val spriteBatch = SpriteBatch()
    private val file: FileHandle
    private val json = Gson()
    private lateinit var stageData: StageData
    private val gravityValue = 8f
    private val blockSpeed = 1.5f
    private val playerSpeed = 0.5f
    private val gridSize = 5.0f
    private val halfGrid = gridSize / 2.0f
    private val backgroundSize = Gdx.graphics.width
    private val halfBackGround = backgroundSize / 2.0f
    private val fixtureGrid = halfGrid * 0.91f

    private val world: World
    private val renderer: Box2DDebugRenderer
    private val pauseSprite: Sprite
    private val backgroundSprite: Sprite
    private val playerDef = BodyDef()
    private val dynamicDef = BodyDef()
    private val staticDef = BodyDef()
    private val bodies = mutableListOf<Body>()
    private val wallBodies = mutableListOf<Body>()
    private val playerBody: Body
    private val goalBody: Body
    private val circleShape: CircleShape
    private val boxShape: PolygonShape
    private val ladderShape: PolygonShape
    private val triangleShape: PolygonShape
    private val goalShape: PolygonShape
    private val playerFixtureDef = FixtureDef()
    private val squareFixtureDef = FixtureDef()
    private val ladderFixtureDef = FixtureDef()
    private val triangleFixtureDef = FixtureDef()
    private val goalFixtureDef = FixtureDef()
    private val playerFixture: Fixture
    private val joints = mutableListOf<DistanceJoint>()
    private var stage: Stage

    private val topList = arrayOf(Vector2(fixtureGrid, fixtureGrid), Vector2(-fixtureGrid, fixtureGrid), Vector2(-fixtureGrid, -fixtureGrid), Vector2(fixtureGrid, -fixtureGrid))
    private val goalX = arrayOf(Vector2(halfGrid, halfGrid / 2), Vector2(-halfGrid, halfGrid / 2), Vector2(-halfGrid, -halfGrid / 2), Vector2(halfGrid, -halfGrid / 2))
    private val goalY = arrayOf(Vector2(halfGrid / 2, halfGrid), Vector2(-halfGrid / 2, halfGrid), Vector2(-halfGrid / 2, -halfGrid), Vector2(halfGrid / 2, -halfGrid))
    private var isLand = false
    private var isTouchBlock = false
    private var touchGravity = mutableListOf<Int>()
    private var isStatic = 2

    private val fontGenerator2: FreeTypeFontGenerator
    private val fontGenerator3: FreeTypeFontGenerator
    private val bitmapFont2: BitmapFont
    private val bitmapFont3: BitmapFont

    private var moveButton: Array<Pixmap>
    private var tex: Array<Texture>
    private var jumpButton: Array<Pixmap>
    private var jtex: Array<Texture>
    private var laserButton: Array<Pixmap>
    private var ltex: Array<Texture>
    private var callback: RayCastCallback
    private var laserFixture: Fixture? = null
    private var laserTouchedPix: Pixmap
    private var ltouchtex: Texture

    private var moveGravityGroup: Int
    private var spriteAlpha: Float

    private var ladderTouchCount: Int

    private val deadLine: Array<Int>

    private val sound: Music
    private var soundID: Long = -1

    init {
        Box2D.init()
        camera = OrthographicCamera(50.0f, 50.0f / Gdx.graphics.width.toFloat() * Gdx.graphics.height.toFloat())
        camera.translate(25.0f, 25.0f / Gdx.graphics.width.toFloat() * Gdx.graphics.height)
        world = World(Vector2(0f, -gravityValue), true)
        renderer = Box2DDebugRenderer()
        createCollision()

        file = Gdx.files.internal("stages/$fileName")
        backgroundSprite = Sprite(Texture(Gdx.files.internal("images/puzzle haikei4.png"))).apply {
            setOrigin(0.0f, 0.0f)
            setScale(backgroundSize / Goal.sprite.width)
            setOrigin(Goal.sprite.width / 2.0f, Goal.sprite.height / 2.0f)
        }
        pauseSprite = Sprite(Texture(Gdx.files.internal("images/stop.png"))).apply {
            setOrigin(0.0f, 0.0f)
            setScale(50.0f)
            setOrigin(width / 2.0f, height / 2.0f)
        }
        Wall.update()
        Square.update()
        Triangle.update()
        GravityChange.update()
        Ladder.update()
        Start.update()
        Goal.update()

        playerDef.type = BodyDef.BodyType.DynamicBody
        dynamicDef.type = BodyDef.BodyType.StaticBody
        staticDef.type = BodyDef.BodyType.StaticBody
        dynamicDef.gravityScale = 0f
        dynamicDef.fixedRotation = true
        staticDef.fixedRotation = true

        circleShape = CircleShape()
        circleShape.radius = gridSize / 3f
        boxShape = PolygonShape()
        boxShape.setAsBox(fixtureGrid, fixtureGrid)
        ladderShape = PolygonShape()
        ladderShape.setAsBox(fixtureGrid, fixtureGrid)
        triangleShape = PolygonShape()
        goalShape = PolygonShape()
        goalShape.set(arrayOf(Vector2(halfGrid / 2, halfGrid), Vector2(-halfGrid / 2, halfGrid), Vector2(-halfGrid / 2, -halfGrid), Vector2(halfGrid / 2, -halfGrid)))
        playerFixtureDef.shape = circleShape
        playerFixtureDef.density = 0.5f
        playerFixtureDef.friction = 1.0f
        playerFixtureDef.restitution = 0.6f
        squareFixtureDef.shape = boxShape
        squareFixtureDef.density = 1000000f
        squareFixtureDef.friction = 1.0f
        squareFixtureDef.restitution = 0.3f
        ladderFixtureDef.shape = ladderShape
        ladderFixtureDef.density = 1000000f
        ladderFixtureDef.isSensor = true
        triangleFixtureDef.shape = triangleShape
        triangleFixtureDef.density = 1000000f
        triangleFixtureDef.friction = 1.0f
        triangleFixtureDef.restitution = 0.3f
        goalFixtureDef.shape = goalShape
        goalFixtureDef.isSensor = true

        if (file.exists()) {
            stageData = json.fromJson(file.readString(), StageData::class.java)
        } else {
            remove()
            game.screen = StageSelect(game)
        }

        deadLine = arrayOf(0, 0)

        stageData.wall.forEach {
            it.x *= gridSize
            it.y *= gridSize
            staticDef.position.set(it.x, it.y)
            val body = world.createBody(staticDef)
            body.createFixture(squareFixtureDef)
            body.userData = it
            wallBodies.add(body)
            setDeadLine(it.x.toInt(), it.y.toInt())
        }
        stageData.square.forEach {
            it.x *= gridSize
            it.y *= gridSize
            dynamicDef.position.set(it.x, it.y)
            val body = world.createBody(dynamicDef)
            body.userData = it
            body.createFixture(squareFixtureDef)
            bodies.add(body)
            setDeadLine(it.x.toInt(), it.y.toInt())
        }
        stageData.triangle.forEach {
            it.x *= gridSize
            it.y *= gridSize
            dynamicDef.position.set(it.x, it.y)
            val body = world.createBody(dynamicDef)
            body.userData = it
            triangleShape.set(createTriangleShape(it.rotate))
            triangleFixtureDef.shape = triangleShape
            body.createFixture(triangleFixtureDef)
            bodies.add(body)
            setDeadLine(it.x.toInt(), it.y.toInt())
        }
        stageData.ladder.forEach {
            it.x *= gridSize
            it.y *= gridSize
            dynamicDef.position.set(it.x, it.y)
            val body = world.createBody(dynamicDef)
            body.userData = it
            body.createFixture(ladderFixtureDef)
            bodies.add(body)
            setDeadLine(it.x.toInt(), it.y.toInt())
        }
        stageData.gravityChange.forEach {
            it.x *= gridSize
            it.y *= gridSize
            dynamicDef.position.set(it.x, it.y)
            val body = world.createBody(dynamicDef)
            body.userData = it
            body.createFixture(squareFixtureDef)
            bodies.add(body)
            setDeadLine(it.x.toInt(), it.y.toInt())
        }
        stageData.start.let {
            it.x *= gridSize
            it.y *= gridSize
            playerDef.position.set(it.x, it.y + 1)
            playerBody = world.createBody(playerDef)
            playerFixture = playerBody.createFixture(playerFixtureDef)
            playerBody.resetMassData()
            playerBody.userData = it
            playerBody.linearDamping = 0.6f
        }
        stageData.goal.let {
            it.x *= gridSize
            it.y *= gridSize
            staticDef.position.set(it.x, it.y)
            goalBody = world.createBody(staticDef)
            goalBody.userData = it
            goalShape.set(if (it.gravity % 2 == 0) goalX else goalY)
            goalBody.createFixture(goalFixtureDef)
        }
        chooseJointBody()
        world.gravity = when (stageData.start.gravity) {
            0 -> Scale.EAST
            1 -> Scale.NORTH
            2 -> Scale.WEST
            3 -> Scale.SOUTH
            else -> Scale.SOUTH
        }.mul(gravityValue)

        stage = Stage()
        val mu = InputMultiplexer()
        mu.addProcessor(Touch())
        mu.addProcessor(stage)
        Gdx.input.inputProcessor = mu

        /**↓ここからデバッグ用*/

        //フォント生成
        fontGenerator2 = FreeTypeFontGenerator(Gdx.files.internal("fonts/meiryo.ttc"))
        val param = FreeTypeFontGenerator.FreeTypeFontParameter()
        param.size = 25
        param.color = Color.RED
        param.incremental = true
        val param2 = FreeTypeFontGenerator.FreeTypeFontParameter()
        param2.size = 64
        param2.color = Color.GREEN
        param2.incremental = true
        bitmapFont2 = fontGenerator2.generateFont(param2)
        /**↑ここまで*/

        fontGenerator3 = FreeTypeFontGenerator(Gdx.files.internal("fonts/meiryo.ttc"))
        val param3 = FreeTypeFontGenerator.FreeTypeFontParameter()
        param3.size = Gdx.graphics.width / 10
        param3.color = Color.RED
        param3.incremental = true
        bitmapFont3 = fontGenerator3.generateFont(param3)

        //ボタン君
        moveButton = arrayOf(Pixmap(Gdx.graphics.width / 5, Gdx.graphics.width / 5, Pixmap.Format.RGBA4444), Pixmap(Gdx.graphics.width / 5, Gdx.graphics.width / 5, Pixmap.Format.RGBA4444))
        jumpButton = arrayOf(Pixmap(Gdx.graphics.width / 5, Gdx.graphics.width / 5, Pixmap.Format.RGBA4444), Pixmap(Gdx.graphics.width / 5, Gdx.graphics.width / 5, Pixmap.Format.RGBA4444))
        laserButton = arrayOf(Pixmap(Gdx.graphics.width, Gdx.graphics.height, Pixmap.Format.RGBA4444), Pixmap(Gdx.graphics.width, Gdx.graphics.height, Pixmap.Format.RGBA4444))
        repeat(2) {
            moveButton[it].setColor(0.0f, 0.0f, 0.0f, 0.0f)
            moveButton[it].fill()
            jumpButton[it].setColor(0.0f, 0.0f, 0.0f, 0.0f)
            jumpButton[it].fill()
            laserButton[it].setColor(0.0f, 0.0f, 0.0f, 0.0f)
            laserButton[it].fill()
        }
        tex = arrayOf(Texture(moveButton[0]), Texture(moveButton[1]))
        jtex = arrayOf(Texture(jumpButton[0]), Texture(jumpButton[1]))
        ltex = arrayOf(Texture(laserButton[0]), Texture(laserButton[1]))

        laserTouchedPix = Pixmap(gridSize2.toInt(), gridSize2.toInt(), Pixmap.Format.RGBA4444)
        laserTouchedPix.setColor(0.0f, 1.0f, 0.0f, 0.5f)
        laserTouchedPix.fill()
        ltouchtex = Texture(laserTouchedPix)

        touchCoordinate.fill(null)
        callback = RayCastCallback { fixture, _, _, fraction ->
            laserFixture = fixture
            fraction
        }
        laserFixture = null

        moveGravityGroup = -1
        spriteAlpha = 1.0f

        ladderTouchCount = 0

        ThreadEnabled = true
        val th = DrawButtonThread(this)
        th.start()
        //ボタン君ここまで

        circleShape.dispose()
        boxShape.dispose()
        ladderShape.dispose()
        triangleShape.dispose()
        goalShape.dispose()

        sound = Gdx.audio.newMusic(Gdx.files.internal("sounds/R()75_loop.ogg"))

        StageLoaded = true
    }

    private fun setDeadLine(X: Int, Y: Int) {
        deadLine[0] = max(deadLine[0], X + gridSize.toInt() * 5)
        deadLine[1] = max(deadLine[1], Y + gridSize.toInt() * 5)
    }

    private fun playSound() {
        if (soundID == -1L) {
            Gdx.app.log("SOUND", "play sound")
            soundID = 0
            sound.play();
            sound.volume = 0.125f
            sound.isLooping = true
        } else {
        }
    }

    private fun stopSound() {
        if (soundID == -1L) {
        } else {
            Gdx.app.log("SOUND", "stop sound")
            sound.stop()
        }
    }

    private fun createCollision() {
        world.setContactListener(object : ContactListener {
            override fun beginContact(contact: Contact?) {
                contact?.let {
                    if (contact.fixtureA.body == playerBody && contact.fixtureB.body.userData is Ladder) ladderAction()
                    else if (contact.fixtureB.body == playerBody && contact.fixtureA.body.userData is Ladder) ladderAction()
                }
            }

            override fun endContact(contact: Contact?) {
                contact?.let {
                    if (contact.fixtureA.body.userData is Ladder || contact.fixtureB.body.userData is Ladder) {
                        ladderTouchCount--
                    }
                    if (contact.fixtureA.body == playerBody || contact.fixtureB.body == playerBody) {
                        isLand = false
                        if (ladderTouchCount <= 0) {
                            playerBody.gravityScale = 1f
                            playerBody.linearDamping = 0.6f
                        }
                        isTouchBlock = false
                    }
                }
            }

            override fun preSolve(contact: Contact?, oldManifold: Manifold?) {

            }

            override fun postSolve(contact: Contact?, impulse: ContactImpulse?) {
                contact?.let {
                    if (contact.fixtureA.body == playerBody) jumpCheck(contact.fixtureA.body.position, contact.fixtureB.body.position)
                    if (contact.fixtureB.body == playerBody) jumpCheck(contact.fixtureB.body.position, contact.fixtureA.body.position)
                }
            }
        })
    }

    private fun createTriangleShape(rotate: Int): Array<Vector2> {
        val list = mutableListOf<Vector2>()
        list.addAll(topList.filter { it != topList[rotate] })
        return list.toTypedArray()
    }

    private fun chooseJointBody() {
        val idNum = stageData.idMax
        for (i in 1..idNum) {
            val bodyList = bodies.filter { (it.userData as MovableBlock).gravityID == i }
            for (j in bodyList.indices) {
                createJoint(bodyList[j], bodyList[(j + 1) % bodyList.size])
                createJoint(bodyList[j], bodyList[(j + 2) % bodyList.size])
            }
        }
    }

    private fun createJoint(bodyA: Body, bodyB: Body) {
        val def = DistanceJointDef()
        def.initialize(bodyA, bodyB, bodyA.position, bodyB.position)
        joints.add(world.createJoint(def) as DistanceJoint)
        def.initialize(bodyA, bodyB, bodyA.position.sub(fixtureGrid, fixtureGrid), bodyB.position.add(fixtureGrid, fixtureGrid))
        joints.add(world.createJoint(def) as DistanceJoint)
    }

    override fun render(delta: Float) {
        playSound()
        Gdx.gl.glClearColor(0.31f, 0.19f, 0.75f, 0.2f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        world.contactList.forEach {
            collisionAction(it.fixtureA.body, it.fixtureB.body)
        }

        spriteBatch.begin()
        drawBackground()
        drawSprites()
        drawButton()
        checkFalled()
        spriteBatch.end()

        isTouchBlock = false
        isStatic++
        touchGravity.clear()
        camera.update()
        world.step(1 / 45f, 8, 3)
    }

    private fun collisionAction(a: Body, b: Body) {
        if (a == playerBody) {
            if (b == goalBody) onGoal(a, b)
            else if (b.userData is GravityChange) {
                if (abs(a.position.x - b.position.x) < gridSize * 3 / 4f || abs(a.position.y - b.position.y) < gridSize * 3 / 4f)
                    changeGravity(b.userData as GravityChange)
            }
            if (b.userData !is Ladder && b.userData !is Goal) {
                if (isTouchBlock) {
                    val nowAngle = checkTouch(b)
                    if (nowAngle != null) {
                        touchGravity.forEach {
                            if (abs(it - nowAngle) == 2) {
                                if (it.isOdd() && playerBody.linearVelocity.y.toInt() == 0) onGameover()
                                if (it.isEven() && playerBody.linearVelocity.x.toInt() == 0) onGameover()
                            }
                        }
                        touchGravity.add(nowAngle)
                    }
                } else {
                    isTouchBlock = true
                    val nowAngle = checkTouch(b)
                    nowAngle?.let {
                        touchGravity.add(it)
                    }
                }
            }
        } else if (b == playerBody) {
            if (a == goalBody) onGoal(b, a)
            else if (a.userData is GravityChange) {
                if (abs(a.position.x - b.position.x) < gridSize * 3 / 4f || abs(a.position.y - b.position.y) < gridSize * 3 / 4f)
                    changeGravity(a.userData as GravityChange)
            }
            if (a.userData !is Ladder && a.userData !is Goal) {
                if (isTouchBlock) {
                    val nowAngle = checkTouch(a)
                    if (nowAngle != null) {
                        touchGravity.forEach {
                            if (abs(it - nowAngle) == 2) {
                                if (it.isOdd() && playerBody.linearVelocity.y.toInt() == 0) onGameover()
                                if (it.isEven() && playerBody.linearVelocity.x.toInt() == 0) onGameover()
                            }
                        }
                        touchGravity.add(nowAngle)
                    }
                } else {
                    isTouchBlock = true
                    val nowAngle = checkTouch(a)
                    nowAngle?.let {
                        touchGravity.add(it)
                    }
                }
            }
        } else {
            if (a.type == BodyDef.BodyType.DynamicBody && b.type == BodyDef.BodyType.StaticBody) {
                toStatic(idCheck(a.userData, b.userData).second, 99)
            } else if (b.type == BodyDef.BodyType.DynamicBody && a.type == BodyDef.BodyType.StaticBody) {
                toStatic(idCheck(a.userData, b.userData).third, 99)
            } else {
                val x = idCheck(a.userData, b.userData)
                if (x.first) {
                    toStatic(x.second, x.third)
                }
            }
        }
    }

    private fun jumpCheck(playerPosition: Vector2, objectPosition: Vector2) {
        if (world.gravity.x > 0f) {
            if (playerPosition.x <= objectPosition.x + gridSize / 6f && playerPosition.y in (objectPosition.y - halfGrid)..(objectPosition.y + halfGrid)) isLand = true
        } else if (world.gravity.x < 0f) {
            if (playerPosition.x >= objectPosition.x - gridSize / 6f && playerPosition.y in (objectPosition.y - halfGrid)..(objectPosition.y + halfGrid)) isLand = true
        } else if (world.gravity.y > 0f) {
            if (playerPosition.y <= objectPosition.y + gridSize / 6f && playerPosition.x in (objectPosition.x - halfGrid)..(objectPosition.x + halfGrid)) isLand = true
        } else if (world.gravity.y < 0f) {
            if (playerPosition.y >= objectPosition.y - gridSize / 6f && playerPosition.x in (objectPosition.x - halfGrid)..(objectPosition.x + halfGrid)) isLand = true
        }
    }

    private fun ladderAction() {
        ladderTouchCount++
        playerBody.gravityScale = 0f
        playerBody.linearDamping = 2f
    }

    private fun changeGravity(switch: GravityChange) {
        when (switch.setGravity) {
            Gravity.EAST.dir -> {
                world.gravity = Vector2(gravityValue, 0f)
                (playerBody.userData as Start).gravity = switch.setGravity
            }
            Gravity.NORTH.dir -> {
                world.gravity = Vector2(0f, gravityValue)
                (playerBody.userData as Start).gravity = switch.setGravity
            }
            Gravity.WEST.dir -> {
                world.gravity = Vector2(-gravityValue, 0f)
                (playerBody.userData as Start).gravity = switch.setGravity
            }
            Gravity.SOUTH.dir -> {
                world.gravity = Vector2(0f, -gravityValue)
                (playerBody.userData as Start).gravity = switch.setGravity
            }
        }
    }

    private fun checkTouch(block: Body): Int? {
        val playerX = playerBody.position.x
        val playerY = playerBody.position.y
        val blockX = block.position.x
        val blockY = block.position.y
        return if (abs(playerX - blockX) <= halfGrid) {
            if (playerY > blockY) 3
            else 1
        } else if (abs(playerY - blockY) <= halfGrid) {
            if (playerX > blockX) 2
            else 0
        } else null
    }

    private fun moveBlocks(block: MovableBlock, gravity: Int) {
        val id: Int = block.gravityID
        bodies.filter { (it.userData as MovableBlock).gravityID == id }.forEach {
            setMove(it, gravity)
            (it.userData as MovableBlock).gravity = gravity
        }
    }

    private fun setMove(body: Body, gravity: Int) {
        body.type = BodyDef.BodyType.DynamicBody
        body.linearVelocity = when (gravity) {
            Gravity.EAST.dir -> Scale.EAST
            Gravity.NORTH.dir -> Scale.NORTH
            Gravity.WEST.dir -> Scale.WEST
            Gravity.SOUTH.dir -> Scale.SOUTH
            else -> Scale.ELSE
        }.mul(blockSpeed)
        isStatic = 0
    }

    private fun idCheck(a: Any, b: Any): Triple<Boolean, Int, Int> {
        val aid = when (a) {
            is Square -> a.gravityID
            is Triangle -> a.gravityID
            is Ladder -> a.gravityID
            is GravityChange -> a.gravityID
            else -> 99
        }
        val bid = when (b) {
            is Square -> b.gravityID
            is Triangle -> b.gravityID
            is Ladder -> b.gravityID
            is GravityChange -> b.gravityID
            else -> 99
        }
        return Triple(aid != bid, aid, bid)
    }

    private fun toStatic(aid: Int, bid: Int) {
        if (isStatic > 5) {
            bodies.filter { (it.userData as MovableBlock).gravityID == aid || (it.userData as MovableBlock).gravityID == bid }.forEach {
                it.setTransform((it.position.x + 0.5f).toInt().toFloat(), (it.position.y + 0.5f).toInt().toFloat(), it.angle)
                it.type = BodyDef.BodyType.StaticBody
            }
        }
    }

    var touched: Int = -1
    var jumpTouched: Int = -1
    var coordinate: Vector2 = Vector2(0.0f, 0.0f)
    var dis: Float = 0.0f
    var laserTouched: Int = -2
    var firstLaser: Vector2 = Vector2(0.0f, 0.0f)
    var ldis: Float = 0.0f
    var a: Boolean = false
    var b: Int = 0
    var laser: Vector2 = Vector2(0.0f, 0.0f)
    var alpha: Float = 0.0f

    class DrawButtonThread(private val screen: PlayScreen) : Thread() {
        override fun run() {
            while (ThreadEnabled) {
                try {

                    screen.moveButton[1 - screen.b].apply {
                        setColor(0.0f, 0.0f, 0.0f, 0.0f)
                        fill()
                        setColor(0.5f, 0.5f, 0.5f, 0.5f)
                        fillCircle(this.width / 2, this.height / 2, this.width / 2)
                    }

                    screen.jumpButton[1 - screen.b].apply {
                        setColor(0.0f, 0.0f, 0.0f, 0.0f)
                        fill()
                        setColor(0.0f, 1.0f, 0.0f, 0.5f)
                    }

                    screen.laserButton[1 - screen.b].apply {
                        setColor(0.0f, 0.0f, 0.0f, 0.0f)
                        fill()
                    }

                    if (screen.touched != -1) {
                        if (touchCoordinate[screen.touched] == null) {
                            screen.touched = -1
                        } else {
                            screen.coordinate.x = touchCoordinate[screen.touched]!!.x
                            screen.coordinate.y = touchCoordinate[screen.touched]!!.y
                            screen.dis = screen.calcDistance(screen.coordinate.x, screen.coordinate.y, screen.moveButton[1 - screen.b].width / 2.0f, screen.moveButton[1 - screen.b].width / 2.0f)
                            if (screen.dis > screen.moveButton[1 - screen.b].width / 4.0f) {
                                screen.coordinate.x = screen.moveButton[1 - screen.b].width / 2.0f + (screen.coordinate.x - screen.moveButton[1 - screen.b].width / 2.0f) / screen.dis * screen.moveButton[1 - screen.b].width / 4.0f
                                screen.coordinate.y = screen.moveButton[1 - screen.b].width / 2.0f + (screen.coordinate.y - screen.moveButton[1 - screen.b].width / 2.0f) / screen.dis * screen.moveButton[1 - screen.b].width / 4.0f
                            }
                        }
                    }
                    if (screen.touched == -1) {
                        screen.moveButton[1 - screen.b].setColor(1.0f, 0.0f, 0.0f, 0.5f)
                        screen.moveButton[1 - screen.b].fillCircle(screen.moveButton[1 - screen.b].width / 2, screen.moveButton[1 - screen.b].height / 2, screen.moveButton[1 - screen.b].width / 4)
                    } else {
                        screen.moveButton[1 - screen.b].setColor(1.0f, 0.5f, 0.5f, 0.5f)
                        screen.moveButton[1 - screen.b].fillCircle(screen.coordinate.x.toInt(), screen.moveButton[1 - screen.b].height - screen.coordinate.y.toInt(), screen.moveButton[1 - screen.b].width / 4)
                    }

                    if (screen.jumpTouched != -1) {
                        if (touchCoordinate[screen.jumpTouched] == null) {
                            screen.jumpTouched = -1
                        } else {
                            screen.jumpButton[1 - screen.b].setColor(0.8f, 1.0f, 0.8f, 0.5f)
                        }
                    }
                    screen.jumpButton[1 - screen.b].fillCircle(screen.jumpButton[1 - screen.b].width / 2, screen.jumpButton[1 - screen.b].height / 2, screen.jumpButton[1 - screen.b].width / 4)

                    if (screen.laserTouched >= 0 && screen.laserFixture == null) {
                        if (touchCoordinate[screen.laserTouched] == null) {
                            screen.alpha = 0.0f
                            screen.laserTouched = -1
                        } else {
                            screen.laserButton[1 - screen.b].setColor(1.0f, 0.0f, 0.0f, 0.5f)
                            screen.ldis = screen.calcDistance(touchCoordinate[screen.laserTouched]!!.x - screen.firstLaser.x, -(touchCoordinate[screen.laserTouched]!!.y - screen.firstLaser.y), 0.0f, 0.0f)
                            if (screen.ldis == 0.0f) {
                                repeat(4) {
                                    screen.laserButton[1 - screen.b].drawLine(Gdx.graphics.width / 2 + cos(it * Math.PI / 2.0).toInt(), Gdx.graphics.height / 2 + sin(it * Math.PI / 2.0).toInt(),
                                            (Gdx.graphics.width / 2) + cos(it * Math.PI / 2.0).toInt(),
                                            (Gdx.graphics.height / 2) + sin(it * Math.PI / 2.0).toInt())
                                }
                            } else {
                                screen.laser.x = (touchCoordinate[screen.laserTouched]!!.x - screen.firstLaser.x) / screen.ldis * Gdx.graphics.width / 10 + Gdx.graphics.width / 2
                                screen.laser.y = (touchCoordinate[screen.laserTouched]!!.y - screen.firstLaser.y) / screen.ldis * Gdx.graphics.width / 10 - Gdx.graphics.height / 2
                                repeat(4) {
                                    screen.laserButton[1 - screen.b].drawLine(Gdx.graphics.width / 2 + cos(it * Math.PI / 2.0).toInt(), Gdx.graphics.height / 2 + sin(it * Math.PI / 2.0).toInt(),
                                            screen.laser.x.toInt() + cos(it * Math.PI / 2.0).toInt(),
                                            -screen.laser.y.toInt() + sin(it * Math.PI / 2.0).toInt())
                                }

                            }
                        }
                    }

                    if (screen.laserFixture != null) {
                        screen.laserButton[1 - screen.b].setColor(0.3f, 0.3f, 0.3f, min(0.5f, screen.alpha))
                        screen.laserButton[1 - screen.b].fill()

                        screen.alpha += Gdx.graphics.deltaTime / 1.25f
                    }

                    screen.a = false
                    while (!screen.a) {
                        sleep(1)
                    }
                    super.run()
                } catch (e: Exception) {
                    e.stackTrace
                }

//                Gdx.app.log("thread", "DrawButtonThread is arriving")
            }
            screen.moveButton.forEach { it.dispose() }
            screen.jumpButton.forEach { it.dispose() }
            screen.laserButton.forEach { it.dispose() }

            Gdx.app.log("thread", "DrawButtonThread is dead")
        }
    }

    private var touchTime: Int = 10000

    private fun drawButton() {
        for (i in 0..4) {
            if (touchCoordinate[i] == null) continue
            if (touched == -1 && calcDistance(touchCoordinate[i]!!.x, touchCoordinate[i]!!.y, moveButton[b].width / 2.0f, moveButton[b].width / 2.0f) < moveButton[b].width / 2.0f) {
                touched = i
                coordinate.x = touchCoordinate[touched]!!.x
                coordinate.y = touchCoordinate[touched]!!.y
            } else if (jumpTouched == -1 && calcDistance(touchCoordinate[i]!!.x, touchCoordinate[i]!!.y, Gdx.graphics.width - jumpButton[b].width / 2.0f, jumpButton[b].width / 2.0f) < jumpButton[b].width / 4.0f) {
                jumpTouched = i
            } else if (touched != i && jumpTouched != i && laserTouched < 0) {
                laserTouched = i
                firstLaser.x = touchCoordinate[i]!!.x
                firstLaser.y = touchCoordinate[i]!!.y
            }
        }

        if (touched != -1) {
            playerBody.applyLinearImpulse(playerSpeed * (coordinate.x - moveButton[1 - b].width / 2.0f) / (moveButton[1 - b].width / 4.0f), playerSpeed * (coordinate.y - moveButton[1 - b].width / 2.0f) / (moveButton[1 - b].width / 4.0f), playerBody.position.x, playerBody.position.y, true)
        }
        if (jumpTouched != -1) {
            if (isLand)
                playerBody.applyLinearImpulse(world.gravity.x * -3f, world.gravity.y * -3f, playerBody.worldCenter.x, playerBody.worldCenter.y, true)
        }
        if (laserTouched == -1 && laserFixture == null) {
            laserTouched = -2
            world.rayCast(callback, playerBody.position, laser.sub(Vector2(Gdx.graphics.width / 2.0f, -Gdx.graphics.height / 2.0f)).add(playerBody.position))
            touchTime = 0
        }

        tex[0].draw(moveButton[b], 0, 0)
        spriteBatch.draw(tex[0], 0.0f, 0.0f)
        jtex[0].draw(jumpButton[b], 0, 0)
        spriteBatch.draw(jtex[0], Gdx.graphics.width - jumpButton[b].width.toFloat(), 0.0f)
        ltex[0].draw(laserButton[b], 0, 0)
        spriteBatch.draw(ltex[0], 0.0f, 0.0f)
        if (!a) b = 1 - b
        a = true

        if (laserFixture != null) {
            bitmapFont2.draw(spriteBatch, " 　↑　 \n← 　 →\n 　↓　 ", Gdx.graphics.width / 2.0f - 100.0f, Gdx.graphics.height / 2.0f + 100.0f)

            spriteAlpha -= Gdx.graphics.deltaTime
            if (spriteAlpha < 0.0f) spriteAlpha += 1.0f

            if (laserFixture!!.body.userData is MovableBlock)
                moveGravityGroup = (laserFixture!!.body.userData as MovableBlock).gravityID
            else laserFixture = null

            try {
                if (laserFixture!!.body.linearVelocity.x != 0.0f || laserFixture!!.body.linearVelocity.y != 0.0f) laserFixture = null
            } catch (e: java.lang.Exception) {
            }
            try {
                if (laserTouched >= 0 && touchCoordinate[laserTouched] == null) {
                    laserTouched = -1
                } else if (laserTouched >= 0) {
                    laser.x = touchCoordinate[laserTouched]!!.x
                    laser.y = touchCoordinate[laserTouched]!!.y
                } else if (laserTouched == -1) {
                    laserTouched = -2
                    when (atan2(firstLaser.x - laser.x.toDouble(), firstLaser.y - laser.y.toDouble()) * 180.0 / Math.PI) {
                        in -135.0..-45.0 -> {
                            moveBlocks(laserFixture!!.body.userData as MovableBlock, Gravity.EAST.dir)
                        }
                        in -45.0..45.0 -> {
                            moveBlocks(laserFixture!!.body.userData as MovableBlock, Gravity.SOUTH.dir)
                        }
                        in 45.0..135.0 -> {
                            moveBlocks(laserFixture!!.body.userData as MovableBlock, Gravity.WEST.dir)
                        }
                        else -> {
                            moveBlocks(laserFixture!!.body.userData as MovableBlock, Gravity.NORTH.dir)
                        }
                    }
                    spriteAlpha = 1.0f
                    laserFixture = null
                }
            } catch (e: java.lang.Exception) {

            }
        } else if (moveGravityGroup != -1) {
            moveGravityGroup = -1
            spriteAlpha = 1.0f
        }
    }

    private fun calcDistance(x1: Float, y1: Float, x2: Float, y2: Float) = sqrt((x1 - x2.toDouble()).pow(2.0) + (y1 - y2.toDouble()).pow(2.0)).toFloat()

    private fun drawBackground() {
        repeat(9) {
            backgroundSprite.setPosition((when {
                it % 3 == 0 -> -1f
                it % 3 == 1 -> 0f
                else -> 1f
            }) * backgroundSize - ((playerBody.position.x * halfGrid2 / halfGrid / 2.5f + halfBackGround - backgroundSprite.width / 2.0f) % backgroundSize) + Gdx.graphics.width / 2.0f, (when {
                it / 3 == 0 -> -1f
                it / 3 == 1 -> 0f
                else -> 1f
            }) * backgroundSize - ((playerBody.position.y * halfGrid2 / halfGrid / 2.5f + halfBackGround - backgroundSprite.height / 2.0f) % backgroundSize) + Gdx.graphics.height / 2.0f)
            backgroundSprite.draw(spriteBatch)
        }
    }

    private fun drawSprites() {
        val playerX = halfGrid + playerBody.position.x - Gdx.graphics.width / 2.0f / gridSize2 * gridSize   //playerを真ん中に表示するための何か
        val playerY = halfGrid + playerBody.position.y - Gdx.graphics.height / 2.0f / gridSize2 * gridSize  //同上
        bodies.forEach {
            drawMain((it.userData as MovableBlock).getSprite(), playerX, playerY, it.position.x, it.position.y, it.angle, (it.userData as MovableBlock).rotate, (it.userData as MovableBlock).gravityID)
            if (it.userData is GravityChange)
                drawMain(GravityChange.changeSprite, playerX, playerY, it.position.x, it.position.y, it.angle, (it.userData as GravityChange).setGravity + 1, (it.userData as GravityChange).gravityID)
        }
        wallBodies.forEach {
            drawMain((it.userData as Block).getSprite(), playerX, playerY, it.position.x, it.position.y, it.angle, 0, -2)
        }
        playerBody.let {
            drawMain((it.userData as Block).getSprite(), playerX, playerY, it.position.x, it.position.y, it.angle, (it.userData as Start).gravity + 1, -2)
        }
        goalBody.let {
            drawMain((it.userData as Block).getSprite(), playerX, playerY, it.position.x, it.position.y, it.angle, (it.userData as Goal).gravity + 1, -2)
        }
    }

    private fun drawMain(sprite: Sprite, playerX: Float, playerY: Float, x: Float, y: Float, angle: Float, rotate: Int, gravityGroup: Int) {
        sprite.setPosition((x - playerX) * gridSize2 / gridSize - sprite.width / 2f + halfGrid2, (y - playerY) * gridSize2 / gridSize - sprite.height / 2f + halfGrid2)
        sprite.rotation = angle / PI.toFloat() * 180f + rotate * 90f
        if (moveGravityGroup != -1) {
            if (gravityGroup == moveGravityGroup) sprite.setColor(sprite.color.r, sprite.color.g, sprite.color.b, spriteAlpha)
            else sprite.setColor(sprite.color.r, sprite.color.g, sprite.color.b, 1.0f)
        }
        sprite.draw(spriteBatch)
    }

    private var isGameover: Boolean = false
    private var isClear: Boolean = false

    private fun onGoal(a: Body, b: Body) {
        if ((a.userData as Start).gravity == (b.userData as Goal).gravity) {
            isClear = true
            playerBody.linearDamping = 4.3f
        }
    }

    private fun onGameover() {
        isGameover = true
    }

    private fun changeStageSelect() {
        stopSound()
        ThreadEnabled = false
        game.screen = StageSelect(game)
    }

    private var deadTime: Int = 0

    private fun checkFalled() {
        if (isClear) {
            bitmapFont3.draw(spriteBatch, "CLEAR!!!".substring(0, min(deadTime / 100, 8)), Gdx.graphics.width / 3.0f, Gdx.graphics.height / 2.0f + Gdx.graphics.width / 10.0f)
            deadTime += (Gdx.graphics.deltaTime * 1000.0).toInt()
            if (deadTime >= 1500) changeStageSelect()
        } else if (!(playerBody.position.x in (-5 * gridSize)..deadLine[0].toFloat() && playerBody.position.y in (-5 * gridSize)..deadLine[1].toFloat()) || isGameover) {
            bitmapFont3.setColor(bitmapFont3.color.r, bitmapFont3.color.g, bitmapFont3.color.b, min(deadTime / 2000.0f, 1.0f))
            if (isGameover) bitmapFont3.draw(spriteBatch, " PRESSED\nGAMEOVER", Gdx.graphics.width / 5.0f, Gdx.graphics.height / 2.0f + Gdx.graphics.width / 10.0f)
            else bitmapFont3.draw(spriteBatch, " FALLED \nGAMEOVER", Gdx.graphics.width / 5.0f, Gdx.graphics.height / 2.0f + Gdx.graphics.width / 10.0f)
            deadTime += (Gdx.graphics.deltaTime * 1000.0).toInt()
            if (deadTime >= 1500) changeStageSelect()
        }
    }

    override fun resize(width: Int, height: Int) {

    }

    override fun show() {

    }

    override fun hide() {
        remove()
    }

    override fun pause() {

    }

    override fun resume() {

    }

    override fun dispose() {
    }

    private fun remove() {
        stage.dispose()
        joints.forEach {
            world.destroyJoint(it)
        }
        wallBodies.forEach {
            for (i in (0 until it.fixtureList.size)) {
                it.destroyFixture(it.fixtureList[i])
            }
            it.fixtureList.clear()
            world.destroyBody(it)
        }
        wallBodies.clear()
        bodies.forEach {
            for (i in (0 until it.fixtureList.size)) {
                it.destroyFixture(it.fixtureList[i])
            }
            it.fixtureList.clear()
            world.destroyBody(it)
        }
        bodies.clear()
        for (i in (0 until playerBody.fixtureList.size)) {
            playerBody.destroyFixture(playerBody.fixtureList[i])
        }
        world.destroyBody(playerBody)
        for (i in (0 until goalBody.fixtureList.size)) {
            goalBody.destroyFixture(goalBody.fixtureList[i])
        }
        world.destroyBody(goalBody)
        world.dispose()
        spriteBatch.dispose()
        fontGenerator2.dispose()
        fontGenerator3.dispose()
        bitmapFont2.dispose()
        bitmapFont3.dispose()
        for (it in 0..1) {
                                       tex[it].textureData.disposePixmap()
            tex[it].dispose()
            jtex[it].textureData.disposePixmap()
            jtex[it].dispose()
            ltex[it].textureData.disposePixmap()
            ltex[it].dispose()
        }
        laserTouchedPix.dispose()
        ltouchtex.textureData.disposePixmap()
        ltouchtex.dispose()
        sound.dispose()

        alreadyRemoved = true
    }

    private var alreadyRemoved: Boolean = false

    protected fun finalize() {
        if (!alreadyRemoved) {
            remove()
        }
    }
}