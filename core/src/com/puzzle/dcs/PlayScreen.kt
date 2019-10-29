package com.puzzle.dcs

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.Screen
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.*
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.*
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.google.gson.Gson
import com.badlogic.gdx.physics.box2d.FixtureDef
import kotlin.math.*
import com.badlogic.gdx.physics.box2d.Fixture
import com.badlogic.gdx.physics.box2d.RayCastCallback
import com.badlogic.gdx.physics.box2d.joints.DistanceJoint
import com.badlogic.gdx.physics.box2d.joints.DistanceJointDef


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
    private val gridSize2 = Gdx.graphics.width / 12f
    private val halfGrid2 = gridSize2 / 2.0f
    private val fixtureGrid = halfGrid * 0.95f

    private val world: World
    private val renderer: Box2DDebugRenderer
    private val wallSprite: Sprite
    private val squareSprite: Sprite
    private val triangleSprite: Sprite
    private val ladderSprite: Sprite
    private val playerSprite: Sprite
    private val goalSprite: Sprite
    private val changeSprite: Sprite
    private val button: Array<ImageButton>
    private val playerDef = BodyDef()
    private val dynamicDef = BodyDef()
    private val staticDef = BodyDef()
    private val wallBodies = mutableListOf<Body>()
    private val squareBodies = mutableListOf<Body>()
    private val triangleBodies = mutableListOf<Body>()
    private val ladderBodies = mutableListOf<Body>()
    private val playerBody: Body
    private val goalBody: Body
    private val changeBodies = mutableListOf<Body>()
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
    private val jump = 4
    private var isLand = false
    private var isTouchBlock = false
    private var touchGravity = mutableListOf<Int>()
    private var isStatic = 2

    private val fontGenerator: FreeTypeFontGenerator
    private val fontGenerator2: FreeTypeFontGenerator
    private val bitmapFont: BitmapFont
    private val bitmapFont2: BitmapFont

    private var moveButton: Array<Pixmap>//Pixmap
    private var tex: Array<Texture>
    private var jumpButton: Array<Pixmap>
    private var jtex: Array<Texture>
    private var laserButton: Array<Pixmap>
    private var ltex: Array<Texture>
    private var callback: RayCastCallback
    private var laserFixture: Fixture? = null
    private var laserTouchedPix: Pixmap
    private var ltouchtex: Texture

    init {
        Box2D.init()
        camera = OrthographicCamera(50.0f, 50.0f / Gdx.graphics.width.toFloat() * Gdx.graphics.height.toFloat())
        camera.translate(25.0f, 25.0f / Gdx.graphics.width.toFloat() * Gdx.graphics.height)
        world = World(Vector2(0f, -gravityValue), true)
        renderer = Box2DDebugRenderer()
        createCollision()

        file = Gdx.files.internal("stages/$fileName")
        wallSprite = Sprite(Texture(Gdx.files.internal("images/wall.png")))
        squareSprite = Sprite(Texture(Gdx.files.internal("images/square.png")))
        triangleSprite = Sprite(Texture(Gdx.files.internal("images/triangle.png")))
        ladderSprite = Sprite(Texture(Gdx.files.internal("images/ladder.png")))
        playerSprite = Sprite(Texture(Gdx.files.internal("images/ball.png")))
        goalSprite = Sprite(Texture(Gdx.files.internal("images/goal.png")))
        changeSprite = Sprite(Texture(Gdx.files.internal("images/change.png")))

        wallSprite.setOrigin(0.0f, 0.0f)
        wallSprite.setScale(gridSize2 / wallSprite.width)
        wallSprite.setOrigin(wallSprite.width / 2.0f, wallSprite.height / 2.0f)

        squareSprite.setOrigin(0.0f, 0.0f)
        squareSprite.setScale(gridSize2 / squareSprite.width)
        squareSprite.setOrigin(squareSprite.width / 2.0f, squareSprite.height / 2.0f)

        changeSprite.setOrigin(0.0f, 0.0f)
        changeSprite.setScale(gridSize2 / changeSprite.width)
        changeSprite.setOrigin(changeSprite.width / 2.0f, changeSprite.height / 2.0f)

        triangleSprite.setOrigin(0.0f, 0.0f)
        triangleSprite.setScale(gridSize2 / triangleSprite.width)
        triangleSprite.setOrigin(triangleSprite.width / 2.0f, triangleSprite.height / 2.0f)

        ladderSprite.setOrigin(0f, 0f)
        ladderSprite.setScale(gridSize2 / ladderSprite.width)
        ladderSprite.setOrigin(ladderSprite.width / 2.0f, ladderSprite.height / 2.0f)

        playerSprite.setOrigin(0.0f, 0.0f)
        playerSprite.setScale(gridSize2 / playerSprite.width / 1.5f)
        playerSprite.setOrigin(playerSprite.width / 2.0f, playerSprite.height / 2.0f)

        goalSprite.setOrigin(0.0f, 0.0f)
        goalSprite.setScale(gridSize2 / goalSprite.width)
        goalSprite.setOrigin(goalSprite.width / 2.0f, goalSprite.height / 2.0f)

        playerDef.type = BodyDef.BodyType.DynamicBody
        dynamicDef.type = BodyDef.BodyType.DynamicBody
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

        if (file.exists()) {
            stageData = json.fromJson(file.readString(), StageData::class.java)
        } else {
            dispose()
            game.screen = StageSelect(game)
        }

        stageData.wall.forEach {
            it.x *= gridSize
            it.y *= gridSize
            staticDef.position.set(it.x, it.y)
            val body = world.createBody(staticDef)
            body.createFixture(squareFixtureDef)
            body.userData = it
            wallBodies.add(body)
        }
        stageData.square.forEach {
            it.x *= gridSize
            it.y *= gridSize
            dynamicDef.position.set(it.x, it.y)
            val body = world.createBody(dynamicDef)
            body.userData = it
            body.createFixture(squareFixtureDef)
            squareBodies.add(body)
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
            triangleBodies.add(body)
        }
        stageData.ladder.forEach {
            it.x *= gridSize
            it.y *= gridSize
            dynamicDef.position.set(it.x, it.y)
            val body = world.createBody(dynamicDef)
            body.userData = it
            body.createFixture(ladderFixtureDef)
            ladderBodies.add(body)
        }
        stageData.gravityChange.forEach {
            it.x *= gridSize
            it.y *= gridSize
            dynamicDef.position.set(it.x, it.y)
            val body = world.createBody(dynamicDef)
            body.userData = it
            body.createFixture(squareFixtureDef)
            changeBodies.add(body)
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
            goalFixtureDef.shape = goalShape
            goalBody.createFixture(goalFixtureDef)
        }
        chooseJointBody()

        stage = Stage()
        button = arrayOf(
                ImageButton(TextureRegionDrawable(TextureRegion(Texture(Gdx.files.internal("images/Arrow1.png"))))),
                ImageButton(TextureRegionDrawable(TextureRegion(Texture(Gdx.files.internal("images/Arrow2.png"))))),
                ImageButton(TextureRegionDrawable(TextureRegion(Texture(Gdx.files.internal("images/Arrow3.png"))))),
                ImageButton(TextureRegionDrawable(TextureRegion(Texture(Gdx.files.internal("images/Arrow4.png"))))),
                ImageButton(TextureRegionDrawable(TextureRegion(Texture(Gdx.files.internal("images/jumpbutton.png")))))
        )
        repeat(5) {
            button[it].image.setScale(Gdx.graphics.width / 10.0f / button[it].width)
            button[it].image.setColor(button[it].image.color.r, button[it].image.color.g, button[it].image.color.b, 0.5f)
            button[it].setScale(Gdx.graphics.width / 10.0f / button[it].width / 1f)
            button[it].setOrigin(0.0f, 0.0f)
            if (it == jump) {
                button[it].setPosition(
                        Gdx.graphics.width / 10.0f / 3.0f * 24.0f,
                        Gdx.graphics.width / 10.0f / 3.0f * 2.0f
                )
            } else {
                button[it].setPosition(
                        Gdx.graphics.width / 10.0f / 3.0f * 2.0f + Gdx.graphics.width / 10.0f / 3.0f * 2.0f * (-cos(Math.PI * it / 2.0).toFloat()),
                        Gdx.graphics.width / 10.0f / 3.0f * 2.0f + Gdx.graphics.width / 10.0f / 3.0f * 2.0f * (sin(Math.PI * it / 2.0).toFloat())
                )
                button[it].color.set(Color.BLACK)
            }
            stage.addActor(button[it])
        }
        val mu = InputMultiplexer()
        mu.addProcessor(Touch())
        mu.addProcessor(stage)
        Gdx.input.inputProcessor = mu

        /**↓ここからデバッグ用*/

        //フォント生成
        fontGenerator = FreeTypeFontGenerator(Gdx.files.internal("fonts/Roboto-Black.ttf"))
        fontGenerator2 = FreeTypeFontGenerator(Gdx.files.internal("fonts/meiryo.ttc"))
        val param = FreeTypeFontGenerator.FreeTypeFontParameter()
        param.size = 25
        param.color = Color.RED
        param.incremental = true
        bitmapFont = fontGenerator.generateFont(param)
        val param2 = FreeTypeFontGenerator.FreeTypeFontParameter()
        param2.size = 64
        param2.color = Color.GREEN
        param2.incremental = true
        bitmapFont2 = fontGenerator2.generateFont(param2)
        /**↑ここまで*/

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

        repeat(5) {
            touchCoordinate[it] = null
        }
        callback = RayCastCallback { fixture, point, normal, fraction ->
            laserFixture = fixture
            Gdx.app.log("callback", "${fixture.body.position.x}, ${fixture.body.position.y}, ${point.x}, ${point.y}, ${normal.x}, ${normal.y}, $fraction")
            fraction
        }
        laserFixture = null

        ThreadEnabled = true
        val th = DrawButtonThread(this)
        th.start()

        circleShape.dispose()
        boxShape.dispose()
        ladderShape.dispose()
        triangleShape.dispose()
    }

    private fun createCollision() {
        world.setContactListener(object : ContactListener {
            override fun beginContact(contact: Contact?) {
                contact?.let {
                    if (contact.fixtureA.body == playerBody && contact.fixtureB.body.userData is Ladder) ladderAction()
                    if (contact.fixtureB.body == playerBody && contact.fixtureA.body.userData is Ladder) ladderAction()
                }
            }

            override fun endContact(contact: Contact?) {
                contact?.let {
                    if (contact.fixtureA.body == playerBody || contact.fixtureB.body == playerBody) {
                        isLand = false
                        playerBody.gravityScale = 1f
                        playerBody.linearDamping = 0.6f
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
            val bodyList = mutableListOf<Body>()
            bodyList.addAll(squareBodies.filter { (it.userData as Square).gravityID == i })
            bodyList.addAll(triangleBodies.filter { (it.userData as Triangle).gravityID == i })
            bodyList.addAll(ladderBodies.filter { (it.userData as Ladder).gravityID == i })
            bodyList.addAll(changeBodies.filter { (it.userData as GravityChange).gravityID == i })
            for (j in bodyList.indices) {
                createJoint(bodyList[j], bodyList[(j + 1) % bodyList.size])
                createJoint(bodyList[j], bodyList[(j + 2) % bodyList.size])
                createJoint(bodyList[j], bodyList[(j + 3) % bodyList.size])
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
        Gdx.gl.glClearColor(1f, 1f, 1f, 0f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        world.contactList.forEach {
            collisionAction(it.fixtureA.body, it.fixtureB.body)
        }

        spriteBatch.begin()
        checkPlayer()
        bitmapFont.draw(spriteBatch, "(${playerBody.position.x.toInt()}, ${playerBody.position.y.toInt()})\n(${playerBody.linearVelocity.x.toInt()}, ${playerBody.linearVelocity.y.toInt()})", Gdx.graphics.width - 150.0f, Gdx.graphics.height - 20.0f)
        drawSprites()
        drawButton()
        spriteBatch.end()

        isTouchBlock = false
        isStatic++
        touchGravity.clear()
        camera.update()
        world.step(1 / 45f, 8, 3)
        //renderer.render(world, camera.combined)
    }

    /**↓ここからデバッグ用*/
    private var ochitattawa = 1000
    private var ochita = false
    private var ochita2 = false
    private var ochita3 = false

    private fun checkPlayer() {
        if (playerBody.position.x * world.gravity.x.absoluteValue < 0 || playerBody.position.y * world.gravity.y.absoluteValue < 0) {
            ochitattawa = 0
            ochita = true
            ochita2 = true
            ochita3 = true
        }
        if (ochitattawa < 1000) {
            ochitattawa += (Gdx.graphics.deltaTime * 1000).toInt()
            bitmapFont2.draw(spriteBatch, "落っこちんな～？", Gdx.graphics.width / 3.0f, Gdx.graphics.height / 2.0f * 1.5f)
            if (ochita3) {
                playerBody.setLinearVelocity((-1.0f - playerBody.position.x) * 5, (0.5f - playerBody.position.y) * 5)
                if (playerBody.position.x in -1.5..-0.5 &&
                        playerBody.position.y in 0.0..1.0) {
                    ochita3 = false
                }
            } else if (ochita2) {
                playerBody.setLinearVelocity((-1.0f - playerBody.position.x) * 5, (10.0f - playerBody.position.y) * 5)
                if (playerBody.position.x in -1.5..-0.5 &&
                        playerBody.position.y in 9.5..10.5) {
                    ochita2 = false
                }
            } else if (ochita) {
                playerBody.setLinearVelocity((10.0f - playerBody.position.x) * 5, (10.0f - playerBody.position.y) * 5)
                if (playerBody.position.x in 9.5..10.5 &&
                        playerBody.position.y in 9.5..10.5) {
                    ochita = false
                }
            }
        }
    }

    /**↑ここまで*/

    private fun collisionAction(a: Body, b: Body) {
        if (a == playerBody) {
            if (b == goalBody) onGoal(a, b)
            else if (b.userData is GravityChange) changeGravity(b.userData as GravityChange)
            if (b.userData !is Ladder) {
                if (isTouchBlock) {
                    val nowAngle = checkTouch(b)
                    if (nowAngle != null) {
                        touchGravity.forEach {
                            if (abs(it - nowAngle) == 2) {
                                if (playerBody.linearVelocity.x in -0.4..0.4 && playerBody.linearVelocity.y in -0.4..0.4) onGameover()
                            }
                        }
                        touchGravity.add(nowAngle)
                        //isLand = false
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
            else if (a.userData is GravityChange) changeGravity(a.userData as GravityChange)
            if (a.userData !is Ladder) {
                if (isTouchBlock) {
                    val nowAngle = checkTouch(a)
                    if (nowAngle != null) {
                        touchGravity.forEach {
                            if (abs(it - nowAngle) == 2) {
                                if (playerBody.linearVelocity.x in -0.4..0.4 && playerBody.linearVelocity.y in -0.4..0.4) onGameover()
                            }
                        }
                        touchGravity.add(nowAngle)
                        //isLand = false
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
        playerBody.gravityScale = 0f
        playerBody.linearDamping = 2f
        //isLand = true
    }

    private fun changeGravity(switch: GravityChange) {
        when (switch.setGravity) {
            0 -> {
                world.gravity = Vector2(gravityValue, 0f)
                (playerBody.userData as Start).gravity = switch.setGravity
            }
            1 -> {
                world.gravity = Vector2(0f, gravityValue)
                (playerBody.userData as Start).gravity = switch.setGravity
            }
            2 -> {
                world.gravity = Vector2(-gravityValue, 0f)
                (playerBody.userData as Start).gravity = switch.setGravity
            }
            3 -> {
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
        return if (abs(playerX - blockX) <= fixtureGrid) {
            if (playerY > blockY) 3
            else 1
        } else if (abs(playerY - blockY) <= fixtureGrid) {
            if (playerX > blockX) 2
            else 0
        } else null
    }

    private fun moveBlocks(block: Any, gravity: Int) {
        val id: Int = when (block) {
            is Square -> block.gravityID
            is Triangle -> block.gravityID
            is Ladder -> block.gravityID
            is GravityChange -> block.gravityID
            else -> 99
        }
        squareBodies.filter { (it.userData as Square).gravityID == id }.forEach {
            setMove(it, gravity)
            (it.userData as Square).gravity = gravity
        }
        triangleBodies.filter { (it.userData as Triangle).gravityID == id }.forEach {
            setMove(it, gravity)
            (it.userData as Triangle).gravity = gravity
        }
        ladderBodies.filter { (it.userData as Ladder).gravityID == id }.forEach {
            setMove(it, gravity)
            (it.userData as Ladder).gravity = gravity
        }
        changeBodies.filter { (it.userData as GravityChange).gravityID == id }.forEach {
            setMove(it, gravity)
            (it.userData as GravityChange).gravity = gravity
        }
    }

    private fun setMove(body: Body, gravity: Int) {
        body.type = BodyDef.BodyType.DynamicBody
        body.linearVelocity = when (gravity) {
            0 -> Vector2(blockSpeed, 0f)
            1 -> Vector2(0f, blockSpeed)
            2 -> Vector2(-blockSpeed, 0f)
            3 -> Vector2(0f, -blockSpeed)
            else -> Vector2(0f, 0f)
        }
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
        if (isStatic > 10) {
            squareBodies.filter { (it.userData as Square).gravityID == aid || (it.userData as Square).gravityID == bid }.forEach {
                it.type = BodyDef.BodyType.StaticBody
            }
            triangleBodies.filter { (it.userData as Triangle).gravityID == aid || (it.userData as Triangle).gravityID == bid }.forEach {
                it.type = BodyDef.BodyType.StaticBody
            }
            ladderBodies.filter { (it.userData as Ladder).gravityID == aid || (it.userData as Ladder).gravityID == bid }.forEach {
                it.type = BodyDef.BodyType.StaticBody
            }
            changeBodies.filter { (it.userData as GravityChange).gravityID == aid || (it.userData as GravityChange).gravityID == bid }.forEach {
                it.type = BodyDef.BodyType.StaticBody
            }
        }
    }

    var touched: Int = -1
    var jumpTouched: Int = -1
    var coordinate: Vector2 = Vector2(0.0f, 0.0f)
    var dis: Float = 0.0f
    var laserTouched: Int = -1
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

                    screen.moveButton[1 - screen.b].setColor(0.0f, 0.0f, 0.0f, 0.0f)
                    screen.moveButton[1 - screen.b].fill()
                    screen.moveButton[1 - screen.b].setColor(0.5f, 0.5f, 0.5f, 0.5f)
                    screen.moveButton[1 - screen.b].fillCircle(screen.moveButton[1 - screen.b].width / 2, screen.moveButton[1 - screen.b].height / 2, screen.moveButton[1 - screen.b].width / 2)

                    screen.jumpButton[1 - screen.b].setColor(0.0f, 0.0f, 0.0f, 0.0f)
                    screen.jumpButton[1 - screen.b].fill()
                    screen.jumpButton[1 - screen.b].setColor(0.0f, 1.0f, 0.0f, 0.5f)

                    screen.laserButton[1 - screen.b].setColor(0.0f, 0.0f, 0.0f, 0.0f)
                    screen.laserButton[1 - screen.b].fill()

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
            }
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

        bitmapFont.draw(spriteBatch, "$a", 0.0f, 30.0f)

        tex[0].draw(moveButton[b], 0, 0)
        spriteBatch.draw(tex[0], 0.0f, 0.0f)
        jtex[0].draw(jumpButton[b], 0, 0)
        spriteBatch.draw(jtex[0], Gdx.graphics.width - jumpButton[b].width.toFloat(), 0.0f)
        ltex[0].draw(laserButton[b], 0, 0)
        spriteBatch.draw(ltex[0], 0.0f, 0.0f)
        if (!a) b = 1 - b
        a = true

        if (laserFixture != null) {
            bitmapFont.draw(spriteBatch, "LASERTOUCHED : (${laserFixture!!.body.position.x}, ${laserFixture!!.body.position.y}), ${laserFixture!!.body}  $touchTime MILLISECOND", 0.0f, 50.0f)
            bitmapFont2.draw(spriteBatch, " 　↑　 \n← 　 →\n 　↓　 ", Gdx.graphics.width / 2.0f - 100.0f, Gdx.graphics.height / 2.0f + 100.0f)
            val playerX = halfGrid + playerBody.position.x - Gdx.graphics.width / 2.0f / gridSize2 * gridSize   //playerを真ん中に表示するための何か
            val playerY = halfGrid + playerBody.position.y - Gdx.graphics.height / 2.0f / gridSize2 * gridSize  //同上
            val sprite = Sprite(ltouchtex)
            sprite.setOriginCenter()
            sprite.setPosition((laserFixture!!.body.position.x - playerX) * gridSize2 / gridSize - sprite.width / 2.0f + halfGrid2,
                    (laserFixture!!.body.position.y - playerY) * gridSize2 / gridSize - sprite.width / 2.0f + halfGrid2)
            sprite.draw(spriteBatch)

            if (laserTouched >= 0 && touchCoordinate[laserTouched] == null) {
                laserTouched = -1
            } else if (laserTouched >= 0) {
                laser.x = touchCoordinate[laserTouched]!!.x
                laser.y = touchCoordinate[laserTouched]!!.y
            } else if (laserTouched == -1) {
                laserTouched = -2
                when (atan2(firstLaser.x - laser.x.toDouble(), firstLaser.y - laser.y.toDouble()) * 180.0 / Math.PI) {
                    in -135.0..-45.0 -> {
                        moveBlocks(laserFixture!!.body.userData, 0)
                    }
                    in -45.0..45.0 -> {
                        moveBlocks(laserFixture!!.body.userData, 3)
                    }
                    in 45.0..135.0 -> {
                        moveBlocks(laserFixture!!.body.userData, 2)
                    }
                    else -> {
                        moveBlocks(laserFixture!!.body.userData, 1)
                    }
                }
                laserFixture = null
            }
        }
    }

    private fun calcDistance(x1: Float, y1: Float, x2: Float, y2: Float) = sqrt(Math.pow(x1 - x2.toDouble(), 2.0) + Math.pow(y1 - y2.toDouble(), 2.0)).toFloat()

    private fun drawSprites() {
        val playerX = halfGrid + playerBody.position.x - Gdx.graphics.width / 2.0f / gridSize2 * gridSize   //playerを真ん中に表示するための何か
        val playerY = halfGrid + playerBody.position.y - Gdx.graphics.height / 2.0f / gridSize2 * gridSize  //同上
        wallBodies.forEach {
            drawMain(wallSprite, playerX, playerY, it.position.x, it.position.y, it.angle, 0)
        }
        squareBodies.forEach {
            drawMain(squareSprite, playerX, playerY, it.position.x, it.position.y, it.angle, 0)
        }
        triangleBodies.forEach {
            drawMain(triangleSprite, playerX, playerY, it.position.x, it.position.y, it.angle, (it.userData as Triangle).rotate)
        }
        ladderBodies.forEach {
            drawMain(ladderSprite, playerX, playerY, it.position.x, it.position.y, it.angle, (it.userData as Ladder).rotate)
        }
        changeBodies.forEach {
            drawMain(changeSprite, playerX, playerY, it.position.x, it.position.y, it.angle, (it.userData as GravityChange).setGravity + 1)
        }
        playerBody.let {
            drawMain(playerSprite, playerX, playerY, it.position.x, it.position.y, it.angle, (it.userData as Start).gravity + 1)
        }
        goalBody.let {
            drawMain(goalSprite, playerX, playerY, it.position.x, it.position.y, it.angle, (it.userData as Goal).gravity + 1)
        }
    }

    private fun drawMain(sprite: Sprite, playerX: Float, playerY: Float, x: Float, y: Float, angle: Float, rotate: Int) {
        sprite.setPosition((x - playerX) * gridSize2 / gridSize - sprite.width / 2f + halfGrid2, (y - playerY) * gridSize2 / gridSize - sprite.width / 2f + halfGrid2)
        sprite.rotation = angle / PI.toFloat() * 180f + rotate * 90f
        sprite.draw(spriteBatch)
    }

    private fun onGoal(a: Body, b: Body) {
        if ((a.userData as Start).gravity == (b.userData as Goal).gravity) {
            game.screen = StageSelect(game)
        }
    }

    private fun onGameover() {
        game.screen = StageSelect(game)
    }

    override fun resize(width: Int, height: Int) {

    }

    override fun show() {

    }

    override fun hide() {

    }

    override fun pause() {

    }

    override fun resume() {

    }

    override fun dispose() {
        wallBodies.forEach {
            world.destroyBody(it)
        }
        squareBodies.forEach {
            world.destroyBody(it)
        }
        triangleBodies.forEach {
            world.destroyBody(it)
        }
        ladderBodies.forEach {
            world.destroyBody(it)
        }
        world.destroyBody(playerBody)
        world.destroyBody(goalBody)
        joints.forEach {
            world.destroyJoint(it)
        }
        spriteBatch.dispose()
        stage.dispose()
    }
}