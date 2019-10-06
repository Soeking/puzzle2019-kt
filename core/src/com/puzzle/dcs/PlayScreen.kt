package com.puzzle.dcs

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.Screen
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.*
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.google.gson.Gson
import com.badlogic.gdx.physics.box2d.FixtureDef
import kotlin.math.cos
import kotlin.math.sin


class PlayScreen(private val game: Core, private val fileName: String) : Screen, InputProcessor {
    private val camera: OrthographicCamera
    private val spriteBatch = SpriteBatch()
    private val file: FileHandle
    private val json = Gson()
    private lateinit var stageData: StageData
    private val gridSize = Gdx.graphics.width / 10f
    private val halfGrid = gridSize / 2f
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
    private val kinematicDef = BodyDef()
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

    private val topList = arrayOf(
        Vector2(halfGrid, halfGrid),
        Vector2(-halfGrid, halfGrid),
        Vector2(-halfGrid, -halfGrid),
        Vector2(halfGrid, -halfGrid)
    )
    private val left = 0
    private val up = 1
    private val right = 2
    private val down = 3

    init {
        Box2D.init()
        camera = OrthographicCamera(Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat())
        camera.translate(Gdx.graphics.width / 2f, Gdx.graphics.height / 2f)
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
        // moveArrow = Sprite(Texture(Gdx.files.internal("images/Arrow.png")))

        wallSprite.setOrigin(0f, 0f)
        wallSprite.setScale(gridSize / wallSprite.width)
        squareSprite.setOrigin(0f, 0f)
        squareSprite.setScale(gridSize / squareSprite.width)
        triangleSprite.setOrigin(0f, 0f)
        triangleSprite.setScale(gridSize / triangleSprite.width)
        repeat(4) { triangleSprites.add(triangleSprite) }
        ladderSprite.setOrigin(0f, 0f)
        ladderSprite.setScale(gridSize / ladderSprite.width)
        playerSprite.setOrigin(0f, 0f)
        playerSprite.setScale(gridSize / playerSprite.width / 1.5f)
        goalSprite.setOrigin(0f, 0f)
        goalSprite.setScale(gridSize / goalSprite.width)


        playerDef.type = BodyDef.BodyType.DynamicBody
        dynamicDef.type = BodyDef.BodyType.DynamicBody
        staticDef.type = BodyDef.BodyType.StaticBody
        kinematicDef.type = BodyDef.BodyType.DynamicBody
        kinematicDef.gravityScale = 0f
        dynamicDef.gravityScale = 0f

        circleShape = CircleShape()
        circleShape.radius = gridSize / 3f
        boxShape = PolygonShape()
        boxShape.setAsBox(halfGrid, halfGrid)
        ladderShape = PolygonShape()
        ladderShape.setAsBox(halfGrid, halfGrid)
        triangleShape = PolygonShape()
        goalShape = PolygonShape()
        goalShape.set(
            arrayOf(
                Vector2(halfGrid / 2, halfGrid),
                Vector2(-halfGrid / 2, halfGrid),
                Vector2(-halfGrid / 2, -halfGrid),
                Vector2(halfGrid / 2, -halfGrid)
            )
        )
        playerFixtureDef.shape = circleShape
        playerFixtureDef.density = 0.05f // 仮    //密度
        playerFixtureDef.friction = 1f         //摩擦
        playerFixtureDef.restitution = 1f     //返還
        squareFixtureDef.shape = boxShape
        squareFixtureDef.friction = 1f
        squareFixtureDef.restitution = 0f
        ladderFixtureDef.shape = ladderShape
        ladderFixtureDef.isSensor = true
        triangleFixtureDef.shape = triangleShape
        triangleFixtureDef.friction = 1f
        triangleFixtureDef.restitution = 0f
        goalFixtureDef.shape = goalShape

        if (file.exists()) {
            stageData = json.fromJson(file.readString(), StageData::class.java)
        } else {

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
            kinematicDef.position.set(it.x, it.y)
            dynamicDef.position.set(it.x, it.y)
            val body = world.createBody(kinematicDef)
            body.userData = it
            body.createFixture(squareFixtureDef)
            squareBodies.add(body)
        }
        stageData.triangle.forEach {
            it.x *= gridSize
            it.y *= gridSize
            kinematicDef.position.set(it.x, it.y)
            dynamicDef.position.set(it.x, it.y)
            val body = world.createBody(kinematicDef)
            body.userData = it
            triangleShape.set(createTriangleShape(it.rotate))
            triangleFixtureDef.shape = triangleShape
            body.createFixture(triangleFixtureDef)
            triangleBodies.add(body)
        }
        stageData.ladder.forEach {
            it.x *= gridSize
            it.y *= gridSize
            kinematicDef.position.set(it.x, it.y)
            dynamicDef.position.set(it.x, it.y)
            val body = world.createBody(kinematicDef)
            body.userData = it
            body.createFixture(ladderFixtureDef)
            ladderBodies.add(body)
        }
        stageData.start.let {
            it.x *= gridSize
            it.y *= gridSize
            playerDef.position.set(it.x, it.y + 2)
            playerBody = world.createBody(playerDef)
            playerFixture = playerBody.createFixture(playerFixtureDef)
            playerBody.resetMassData()
            playerBody.userData = it
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
            ImageButton(TextureRegionDrawable(TextureRegion(Texture(Gdx.files.internal("images/Arrow4.png")))))
        )
        repeat(4) {
            button[it].image.setScale(Gdx.graphics.width / 10.0f / button[it].width)
            button[it].image.setColor(button[it].image.color.r, button[it].image.color.g, button[it].image.color.b,0.5f)
            button[it].setScale(Gdx.graphics.width / 10.0f / button[it].width / 1f)
            //button[i].setScale(10f)
            //button[i].setOrigin(button[i].width / 2.0f, button[i].height / 2.0f)
            button[it].setOrigin(0.0f, 0.0f)
            //button[i].setPosition(Gdx.graphics.width / 12.0f * 3.0f + Gdx.graphics.width / 6.0f * (-Math.cos(Math.PI * i / 2.0).toFloat()), Gdx.graphics.height / 8.0f * 3.0f + Gdx.graphics.height / 4.0f * (Math.sin(Math.PI * i / 2.0)).toFloat())
            button[it].setPosition(
                Gdx.graphics.width / 10.0f / 3.0f * 2.0f + Gdx.graphics.width / 10.0f / 3.0f * 2.0f * (-cos(Math.PI * it / 2.0).toFloat()),
                Gdx.graphics.width / 10.0f / 3.0f * 2.0f + Gdx.graphics.width / 10.0f / 3.0f * 2.0f * (sin(Math.PI * it / 2.0).toFloat())
            )
            button[it].color.set(Color.BLACK)
            stage.addActor(button[it])
            //button[i].rotation(0.0f)
            Gdx.app.log("button","${button[it].x},${button[it].y},${button[it].width},${button[it].height}")
        }
        Gdx.input.inputProcessor = stage
        //button[0].setPosition(Gdx.graphics.width / 2f, Gdx.graphics.height / 2f)
        //button[0].setScale(gridSize / goalSprite.width)
        squareBodies.filter { (it.userData as Square).gravityID == 2 }.forEach {
            it.setLinearVelocity(0f, -12f)
        }
        triangleBodies.filter { (it.userData as Triangle).gravityID == 2 }.forEach {
            it.setLinearVelocity(0f, -12f)
        }

        circleShape.dispose()
        boxShape.dispose()
        ladderShape.dispose()
        triangleShape.dispose()
    }

    private fun createCollision() {
        world.setContactListener(object : ContactListener {
            override fun beginContact(contact: Contact?) {

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
        button()

        Gdx.gl.glClearColor(0.1f, 0.4f, 0.8f, 0f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        world.contactList.forEach {
            collisionAction(it.fixtureA.body, it.fixtureB.body)
        }

        spriteBatch.begin()
        //drawSprites()
        drawUI()
        spriteBatch.end()

        camera.update()
        world.step(Gdx.graphics.deltaTime, 1, 0)
        renderer.render(world, camera.combined)
    }

    private fun collisionAction(a: Body, b: Body) {
        if (a == playerBody) {

        } else if (b == playerBody) {

        } else {

        }
    }

    private val speed = 100000000.0f
    private var no = false

    private fun button() {
        //Gdx.app.log("TEST", "A")

        var temp = 0

        repeat(4) {
            if (button[it].isPressed) {
                no = true
                temp++
                //Gdx.app.log("pressed", "${i} , ${Gdx.graphics.deltaTime}")
                when (it) {
                    left -> {
                        //playerBody.position.set(playerBody.position.x - SPEED * Gdx.graphics.deltaTime, playerBody.position.y)
                        //playerBody.setLinearVelocity(-SPEED, playerBody.linearVelocity.y)
                        //playerBody.setLinearVelocity(playerBody.linearVelocity.add(Vector2(-SPEED, 0.0f)))
                        playerBody.applyForceToCenter(-speed, 0.0f, true)
                        //playerBody.applyLinearImpulse(-SPEED, 0.0f, playerBody.position.x, playerBody.position.y, true)
                        //playerBody.linearVelocity.x = -SPEED
                    }
                    up -> {
                        //playerBody.position.set(playerBody.position.x, playerBody.position.y + SPEED * Gdx.graphics.deltaTime)
                        //playerBody.setLinearVelocity(playerBody.linearVelocity.x, SPEED)
                        playerBody.applyForceToCenter(0.0f, speed, true)
                        //playerBody.applyLinearImpulse(0.0f, SPEED, playerBody.position.x, playerBody.position.y, true)
                        //playerBody.linearVelocity.y = SPEED
                    }
                    right -> {
                        //playerBody.position.set(playerBody.position.x + SPEED * Gdx.graphics.deltaTime, playerBody.position.y)
                        //playerBody.setLinearVelocity(SPEED, playerBody.linearVelocity.y)
                        playerBody.applyForceToCenter(speed, 0.0f, true)
                        //playerBody.applyLinearImpulse(SPEED, 0.0f, playerBody.position.x, playerBody.position.y, true)
                        //playerBody.linearVelocity.set(SPEED, playerBody.linearVelocity.y)
                        //playerBody.linearVelocity.x = SPEED
                    }
                    down -> {
                        //playerBody.position.set(playerBody.position.x, playerBody.position.y - SPEED * Gdx.graphics.deltaTime)
                        //playerBody.setLinearVelocity(playerBody.linearVelocity.x, -SPEED)
                        playerBody.applyForceToCenter(0.0f, -speed, true)
                        //playerBody.applyLinearImpulse(0.0f, -SPEED, playerBody.position.x, playerBody.position.y, true)
                        //playerBody.linearVelocity.y = -SPEED
                    }
                }
            }
        }
        if (temp == 0 && no) {
            no = false
            //playerBody.setLinearVelocity(0.0f, 0.0f)
        }

        /*if (button[Left].isPressed) {
            Gdx.app.log("TEST", "${playerBody.position.y}, ${playerBody.linearVelocity.y}")
        }*/
        //Gdx.app.log("TEST", "PO:${playerBody.position.y}, SPE:${playerBody.linearVelocity.y}")
    }

    private fun drawUI() {
        /* val sprite = moveArrow
        sprite.setPosition(Gdx.graphics.width / 15.0f, Gdx.graphics.height / 10.0f)
        sprite.setColor(sprite.color.r, sprite.color.g, sprite.color.b, 0.3f)
        sprite.draw(spriteBatch) */

        stage.act(Gdx.graphics.deltaTime)
        stage.draw()
    }

    private fun drawSprites() {
        wallBodies.forEach {
            val sprite = wallSprite
            sprite.setPosition(it.position.x - halfGrid, it.position.y - halfGrid)
            sprite.draw(spriteBatch)
        }
        squareBodies.forEach {
            val sprite = squareSprite
            sprite.setPosition(it.position.x - halfGrid, it.position.y - halfGrid)
            sprite.draw(spriteBatch)
        }
        triangleBodies.forEach {
            val sprite = triangleSprites[(it.userData as Triangle).rotate]
            sprite.setPosition(it.position.x - halfGrid, it.position.y - halfGrid)
            sprite.draw(spriteBatch)
        }
        ladderBodies.forEach {
            val sprite = ladderSprite
            sprite.setPosition(it.position.x - halfGrid, it.position.y - halfGrid)
            sprite.draw(spriteBatch)
        }
        playerBody.let {
            val sprite = playerSprite
            sprite.setPosition(it.position.x - gridSize / 3f, it.position.y - gridSize / 3f)
            sprite.draw(spriteBatch)
        }
        goalBody.let {
            val sprite = goalSprite
            sprite.setPosition(it.position.x - halfGrid, it.position.y - halfGrid)
            sprite.draw(spriteBatch)
        }
    }

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        return true
    }

    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
        return true
    }

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        return true
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

    override fun keyDown(keycode: Int): Boolean {
        return false
    }

    override fun keyTyped(character: Char): Boolean {
        return false
    }

    override fun keyUp(keycode: Int): Boolean {
        return false
    }

    override fun mouseMoved(screenX: Int, screenY: Int): Boolean {
        return false
    }

    override fun scrolled(amount: Int): Boolean {
        return false
    }
}