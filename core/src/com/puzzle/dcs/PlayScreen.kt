package com.puzzle.dcs

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.Screen
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
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
import kotlin.math.absoluteValue
import kotlin.math.cos
import kotlin.math.sin

class PlayScreen(private val game: Core, fileName: String) : Screen {
    private val camera: OrthographicCamera
    private val spriteBatch = SpriteBatch()
    private val file: FileHandle
    private val json = Gson()
    private val stageData: StageData
    private val gridSize = 5.0f
    private val halfGrid = gridSize / 2.0f
    private val gridSize2 = Gdx.graphics.width / 10.0f
    private val halfGrid2 = gridSize2 / 2.0f
    private val world: World
    private val renderer: Box2DDebugRenderer
    private val wallSprite: Sprite
    private val squareSprite: Sprite
    private val triangleSprite: Sprite
    private val triangleSprites = mutableListOf<Sprite>()
    private val ladderSprite: Sprite
    private val playerSprite: Sprite
    private val goalSprite: Sprite
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
    private var stage: Stage

    private val topList = arrayOf(Vector2(halfGrid, halfGrid), Vector2(-halfGrid, halfGrid), Vector2(-halfGrid, -halfGrid), Vector2(halfGrid, -halfGrid))
    private val left = 0
    private val up = 1
    private val right = 2
    private val down = 3
    private val jump = 4
    private var start = false

    private val fontGenerator: FreeTypeFontGenerator
    private val fontGenerator2: FreeTypeFontGenerator
    private val bitmapFont: BitmapFont
    private val bitmapFont2: BitmapFont

    init {
        Box2D.init()
        camera = OrthographicCamera(50.0f, 50.0f / Gdx.graphics.width.toFloat() * Gdx.graphics.height.toFloat())
        camera.translate(25.0f, 25.0f / Gdx.graphics.width.toFloat() * Gdx.graphics.height)
        world = World(Vector2(0f, -8f), true)
        renderer = Box2DDebugRenderer()
        createCollision()

        file = Gdx.files.internal("stages/$fileName")
        wallSprite = Sprite(Texture(Gdx.files.internal("images/wall.png")))
        squareSprite = Sprite(Texture(Gdx.files.internal("images/square.png")))
        triangleSprite = Sprite(Texture(Gdx.files.internal("images/triangle.png")))
        ladderSprite = Sprite(Texture(Gdx.files.internal("images/ladder.png")))
        playerSprite = Sprite(Texture(Gdx.files.internal("images/ball.png")))
        goalSprite = Sprite(Texture(Gdx.files.internal("images/goal.png")))

        wallSprite.setOrigin(0f, 0f)
        wallSprite.setScale(gridSize2 / wallSprite.width)
        squareSprite.setOrigin(0f, 0f)
        squareSprite.setScale(gridSize2 / squareSprite.width)
        triangleSprite.setOrigin(0f, 0f)
        triangleSprite.setScale(gridSize2 / triangleSprite.width)
        repeat(4) { triangleSprites.add(triangleSprite) }
        ladderSprite.setOrigin(0f, 0f)
        ladderSprite.setScale(gridSize2 / ladderSprite.width)
        playerSprite.setOrigin(0f, 0f)
        playerSprite.setScale(gridSize2 / playerSprite.width / 1.5f)
        goalSprite.setOrigin(0f, 0f)
        goalSprite.setScale(gridSize2 / goalSprite.width)

        playerDef.type = BodyDef.BodyType.DynamicBody
        dynamicDef.type = BodyDef.BodyType.DynamicBody
        staticDef.type = BodyDef.BodyType.StaticBody
        dynamicDef.gravityScale = 0f

        circleShape = CircleShape()
        circleShape.radius = gridSize / 3f
        boxShape = PolygonShape()
        boxShape.setAsBox(halfGrid, halfGrid)
        ladderShape = PolygonShape()
        ladderShape.setAsBox(halfGrid, halfGrid)
        triangleShape = PolygonShape()
        goalShape = PolygonShape()
        goalShape.set(arrayOf(Vector2(halfGrid / 2, halfGrid), Vector2(-halfGrid / 2, halfGrid), Vector2(-halfGrid / 2, -halfGrid), Vector2(halfGrid / 2, -halfGrid)))
        playerFixtureDef.shape = circleShape
        playerFixtureDef.density = 0.5f // 仮    //密度
        playerFixtureDef.friction = 1.0f         //摩擦
        playerFixtureDef.restitution = 0.6f     //返還
        squareFixtureDef.shape = boxShape
        squareFixtureDef.density = 1000000f
        squareFixtureDef.friction = 1.0f
        squareFixtureDef.restitution = 0.3f
        ladderFixtureDef.shape = ladderShape
        ladderFixtureDef.isSensor = true
        triangleFixtureDef.shape = triangleShape
        triangleFixtureDef.density = 1000000f
        triangleFixtureDef.friction = 1.0f
        triangleFixtureDef.restitution = 0.3f
        goalFixtureDef.shape = goalShape

        if (file.exists()) {
            stageData = json.fromJson(file.readString(), StageData::class.java)
        } else {
            TODO("ファイルがないとき")
        }

        stageData.wall.forEach {
            it.x *= gridSize
            it.y *= gridSize
            staticDef.position.set(it.x, it.y)
            val body = world.createBody(staticDef)
            body.createFixture(squareFixtureDef)
            body.userData = it
            wallBodies.add(body)
            val b = world.createBody(dynamicDef)
            b.createFixture(squareFixtureDef)
        }
        stageData.square.forEach {
            it.x *= gridSize
            it.y *= gridSize
            dynamicDef.position.set(it.x, it.y)
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
            dynamicDef.position.set(it.x, it.y)
            val body = world.createBody(dynamicDef)
            body.userData = it
            body.createFixture(ladderFixtureDef)
            ladderBodies.add(body)
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
            goalBody.createFixture(goalFixtureDef)
        }

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
            Gdx.app.log("button", "${button[it].x},${button[it].y},${button[it].width},${button[it].height}")
        }
        Gdx.input.inputProcessor = stage

        squareBodies.filter { (it.userData as Square).gravityID == 2 }.forEach {
            it.setLinearVelocity(0f, -0.3f)
        }
        triangleBodies.filter { (it.userData as Triangle).gravityID == 2 }.forEach {
            it.setLinearVelocity(0f, -0.3f)
        }

        //フォント生成
        var file = Gdx.files.internal("fonts/Roboto-Black.ttf")
        var file2 = Gdx.files.internal("fonts/meiryo.ttc")
        fontGenerator = FreeTypeFontGenerator(file)
        fontGenerator2 = FreeTypeFontGenerator(file2)
        var param = FreeTypeFontGenerator.FreeTypeFontParameter()
        param.size = 25
        param.color = Color.RED
        param.incremental = true
        bitmapFont = fontGenerator.generateFont(param)
        var param2 = FreeTypeFontGenerator.FreeTypeFontParameter()
        param2.size = 64
        param2.color = Color.GREEN
        param2.incremental = true
        bitmapFont2 = fontGenerator2.generateFont(param2)
      
        circleShape.dispose()
        boxShape.dispose()
        ladderShape.dispose()
        triangleShape.dispose()
    }

    private fun createCollision() {
        world.setContactListener(object : ContactListener {
            override fun beginContact(contact: Contact?) {
                contact?.let {
                    if (contact.fixtureA.body == playerBody && contact.fixtureB.body.type == BodyDef.BodyType.StaticBody) start = true
                    if (contact.fixtureB.body == playerBody && contact.fixtureA.body.type == BodyDef.BodyType.StaticBody) start = true
                }
            }

            override fun endContact(contact: Contact?) {

            }

            override fun preSolve(contact: Contact?, oldManifold: Manifold?) {

            }

            override fun postSolve(contact: Contact?, impulse: ContactImpulse?) {

            }
        })
    }

    private fun createTriangleShape(rotate: Int): Array<Vector2> {
        val list = mutableListOf<Vector2>()
        list.addAll(topList.filter { it != topList[rotate] })
        return list.toTypedArray()
    }

    override fun render(delta: Float) {
        Gdx.gl.glClearColor(0.1f, 0.4f, 0.8f, 0f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        if (start) {
            world.contactList.forEach {
                collisionAction(it.fixtureA.body, it.fixtureB.body)
            }
        }

        spriteBatch.begin()
        checkPlayer()
        bitmapFont.draw(spriteBatch, "(${playerBody.position.x.toInt()}, ${playerBody.position.y.toInt()})\n(${playerBody.linearVelocity.x.toInt()}, ${playerBody.linearVelocity.y.toInt()})", Gdx.graphics.width - 150.0f, Gdx.graphics.height - 20.0f)
        drawSprites()
        spriteBatch.end()

        drawUI()
        button()

        //camera.translate(playerBody.position.x, playerBody.position.y)
        camera.update()
        world.step(Gdx.graphics.deltaTime, 1, 0)
        renderer.render(world, camera.combined)
    }

    private val speed = 0.5f
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
        if (ochitattawa < 1000 || ochita) {
            ochitattawa += (Gdx.graphics.deltaTime * 1000).toInt()
            bitmapFont2.draw(spriteBatch, "落っこちんな～？", Gdx.graphics.width / 3.0f, Gdx.graphics.height / 2.0f * 1.5f)
            if (ochita3) {
                playerBody.setLinearVelocity((-1.0f - playerBody.position.x) * 5, (0.5f - playerBody.position.y) * 5)
                if (playerBody.position.x <= -0.5f && playerBody.position.x >= -1.5f &&
                        playerBody.position.y <= 1.0f && playerBody.position.y >= 0.0f) {
                    ochita3 = false
                }
            }
            else if (ochita2) {
                playerBody.setLinearVelocity((-1.0f - playerBody.position.x) * 5, (10.0f - playerBody.position.y) * 5)
                if (playerBody.position.x <= -0.5f && playerBody.position.x >= -1.5f &&
                        playerBody.position.y <= 10.5f && playerBody.position.y >= 9.5f) {
                    ochita2 = false
                }
            }
            else if (ochita) {
                playerBody.setLinearVelocity((10.0f - playerBody.position.x) * 5, (10.0f - playerBody.position.y) * 5)
                playerBody.angularVelocity = -45.0f
                if (playerBody.position.x <= 10.5f && playerBody.position.x >= 9.5f &&
                        playerBody.position.y <= 10.5f && playerBody.position.y >= 9.5f) {
                    ochita = false
                }
            }
        }
    }

    private fun collisionAction(a: Body, b: Body) {
        if (a == playerBody) {
            if (b == goalBody) onGoal(a, b)
        } else if (b == playerBody) {
            if (a == goalBody) onGoal(b, a)
        } else {
            if (a.type == BodyDef.BodyType.DynamicBody && b.type == BodyDef.BodyType.StaticBody) {
                a.type = BodyDef.BodyType.StaticBody
            } else if (b.type == BodyDef.BodyType.DynamicBody && a.type == BodyDef.BodyType.StaticBody) {
                b.type = BodyDef.BodyType.StaticBody
            } else {
                if (idCheck(a.userData, b.userData)) {
                    a.type = BodyDef.BodyType.StaticBody
                    b.type = BodyDef.BodyType.StaticBody
                    Gdx.app.log("collide", "${a.position},${b.position}")
                }
            }
        }
    }

    private fun idCheck(a: Any?, b: Any?): Boolean {
        val aid = when (a) {
            is Square -> a.gravityID
            is Triangle -> a.gravityID
            is Ladder -> a.gravityID
            else -> 9
        }
        val bid = when (b) {
            is Square -> b.gravityID
            is Triangle -> b.gravityID
            is Ladder -> b.gravityID
            else -> 9
        }
        Gdx.app.log("id", "${aid},$bid")
        return aid != bid
    }

    private var no = false

    private fun button() {
        var temp = 0

        repeat(5) {
            if (button[it].isPressed) {
                no = true
                temp++
                when (it) {
                    left -> {
                        playerBody.applyLinearImpulse(-speed, 0.0f, playerBody.position.x, playerBody.position.y, true)
                    }
                    up -> {
                        playerBody.applyLinearImpulse(0.0f, speed, playerBody.position.x, playerBody.position.y, true)
                    }
                    right -> {
                        playerBody.applyLinearImpulse(speed, 0.0f, playerBody.position.x, playerBody.position.y, true)
                    }
                    down -> {
                        playerBody.applyLinearImpulse(0.0f, -speed, playerBody.position.x, playerBody.position.y, true)
                    }
                    jump -> {
                        if ((world.gravity.y == 0.0f && playerBody.linearVelocity.x.absoluteValue <= 0.05f) ||
                                (world.gravity.x == 0.0f && playerBody.linearVelocity.y.absoluteValue <= 0.05f))
                            playerBody.applyLinearImpulse(world.gravity.x * -10.0f, world.gravity.y * -10.0f, playerBody.position.x, playerBody.position.y, true)
                    }
                }
            }
        }
        if (temp == 0 && no) {
            no = false
        }
    }

    private fun drawUI() {
        stage.act(Gdx.graphics.deltaTime)
        stage.draw()
    }

    private fun drawSprites() {
        var playerX = playerBody.position.x - Gdx.graphics.width / 2.0f / gridSize2 * gridSize   //playerを真ん中に表示するための何か
        var playerY = playerBody.position.y - Gdx.graphics.height / 2.0f / gridSize2 * gridSize  //同上
        wallBodies.forEach {
            val sprite = wallSprite
            sprite.setPosition((it.position.x - playerX) * gridSize2 / gridSize - halfGrid2, (it.position.y - playerY) * gridSize2 / gridSize - halfGrid2)
            sprite.draw(spriteBatch)
        }
        squareBodies.forEach {
            val sprite = squareSprite
            sprite.setPosition((it.position.x - playerX) * gridSize2 / gridSize - halfGrid2, (it.position.y - playerY) * gridSize2 / gridSize - halfGrid2)
            sprite.draw(spriteBatch)
        }
        triangleBodies.forEach {
            val sprite = triangleSprites[(it.userData as Triangle).rotate]
            sprite.setPosition((it.position.x - playerX) * gridSize2 / gridSize - halfGrid2, (it.position.y - playerY) * gridSize2 / gridSize - halfGrid2)
            sprite.draw(spriteBatch)
        }
        ladderBodies.forEach {
            val sprite = ladderSprite
            sprite.setPosition((it.position.x - playerX) * gridSize2 / gridSize - halfGrid2, (it.position.y - playerY) * gridSize2 / gridSize - halfGrid2)
            sprite.draw(spriteBatch)
        }
        playerBody.let {
            val sprite = playerSprite
            sprite.setPosition((it.position.x - playerX) * gridSize2 / gridSize - gridSize2 / 3f, (it.position.y - playerY) * gridSize2 / gridSize - gridSize2 / 3f)
            sprite.draw(spriteBatch)
        }
        goalBody.let {
            val sprite = goalSprite
            sprite.setPosition((it.position.x - playerX) * gridSize2 / gridSize - halfGrid2, (it.position.y - playerY) * gridSize2 / gridSize - halfGrid2)
            sprite.draw(spriteBatch)
        }
    }

    private fun onGoal(a: Body, b: Body) {
        if ((a.userData as Start).gravity == (b.userData as Goal).gravity) {
            Gdx.app.log("goal", "ok")
            TODO("ゴールしたとき")
        }
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

    }
}